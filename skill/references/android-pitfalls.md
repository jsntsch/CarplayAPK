# Android 平台级坑与对策

这部分与 iAP2 协议无关，但同样能让整轮联调白做。按「平台 → 症状 → 对策」组织。

## 1. RFCOMM 通道（SCN）

### 1.1 SCN 是动态分配的

同一台车机、不同时段，`…caff` 服务对应的 RFCOMM 通道会变。实测序列：`5 → 6 → 7`。

**不要缓存 SCN。** 每次连接都重新探测。

### 1.2 `isConnected == true` 不代表连对了服务

`createRfcommSocket()` 连上任意一个已开放的通道都会返回 `isConnected = true`。车机可能同时开了多个 RFCOMM 服务（HFP、PBAP、iAP2、SPP）。

**唯一判据**：连上后 2~3 秒内能否读到 `FF 55` 开头的帧。

实现要点：

```
for each candidate SCN:
    socket.connect()
    probe = readRaw(timeout=2500ms)
    if probe 以 FF 55 开头:
        保存 socket；把 probe 放入 preRead 队列交给上层
        return true      # 否则第一个 MARKER 会被丢掉
    else:
        close(); 继续下一个
```

预读数据必须转交上层。丢掉首个 MARKER 会让车机以为没人应答，9 秒后放弃。

### 1.3 OPPO/ColorOS 会清 SDP 缓存

日志特征：

```
oplus_clear_sdp_cache
bta_jv_start_discovery_cback: result:SDP_CFG_FAILED
OnConnectFail: reason:CONTROLLER_BUSY(0x3a)
```

缓存为空时按 UUID 连接必须现场做 SDP 查询，而现场查询常被 `CONTROLLER_BUSY` 打断，最终 `scn=0`（失败）。

对策：

1. `fetchUuidsWithSdp()` 预热缓存，等 `ACTION_UUID` 广播（约 1~2 秒）
2. 反射 `BluetoothDevice.getServiceChannel(ParcelUuid)`（@hide）直接读通道号
3. 读不到就退回「顺序探测 + 预读验证」

### 1.4 重复快速重连后车机会拒绝所有 RFCOMM

车机对上一次连接还有残留状态（认为链路仍活着）。表现：所有 SCN 都连不上，或连上零字节。

重置流程（必须等足时间）：

```bash
adb shell svc bluetooth disable
sleep 20        # 让车机侧检测到断链并释放
adb shell svc bluetooth enable
sleep 20        # 等蓝牙栈就绪
adb shell "dumpsys bluetooth_manager" | grep "ACL BR/EDR:Y"   # 确认车机主动回连
```

**ColorOS 上 `svc bluetooth disable` 常被系统秒拉回**（`settings get global bluetooth_on` 立刻变 1）。要真正复位用：

```bash
adb shell "cmd bluetooth_manager disable"; sleep 10
adb shell "cmd bluetooth_manager enable";  sleep 12
```

### 1.5 连接泄漏

`RFCOMM_CreateConnectionWithSecurity: already at opened state 2` —— 上一次的 socket 没关。每次 `connect` 前先 `close()` 并等待 1.2 秒。

### 1.6 异常被吞

`catch (_: Exception) {}` 是排查大敌。`connect()` 133 ms 就返回、`isConnected=false` 却没任何错误信息——全都因为异常被吞。所有 catch 必须 `Log.e` 出 `javaClass.simpleName + message`。

### 1.7 iAP2 服务记录是有寿命的（车机反复敲门的真凶）

`adapter.listenUsingRfcommWithServiceRecord(name, uuid)` 注册的 SDP 记录**只在监听存活期间可见**。很多实现会套一个「等待配对窗口」，超时就关闭监听。

实测后果：监听窗口一过，车机在 SDP 里查不到 iAP2 服务 → 挂断 → 1~2 秒后重试，形成无限 ACL 打环。

判据与修法见 `scripts/bt_flap_diagnose.sh` 与 `references/troubleshooting.md` 第 7 节。要点：

- 窗口值常见写法是 `90000 ms`（90 秒），用在「accept 循环截止 / postDelayed 定时关闭 / 截止时间戳」三处，**三处必须一起改**，只改一处等于没改
- 改成 `0x7fffffff`（约 24.8 天）即可随进程常驻
- 进程被杀 = 监听消失 = 车机回到打环。要彻底根治需要常驻前台服务。

## 2. WiFi 连接

### 2.1 不要用 WifiNetworkSpecifier

普通 App 调用会报：

```
com.ocarplay was not granted either of these permissions:
android.permission.CHANGE_NETWORK_STATE or android.permission.WRITE_SETTINGS
```

而且 Specifier 建立的是「本 App 专属」网络，其它组件（mDNS、系统服务）用不到。

### 2.2 用 WifiNetworkSuggestion

只需 `CHANGE_WIFI_STATE`。系统完成关联，结果是**系统级 WiFi**，mDNS 组播与 RTSP socket 都能直接用。

```kotlin
val builder = WifiNetworkSuggestion.Builder().setSsid(ssid)
builder.setWpa2Passphrase(psk)          // OPEN 时不设置
wm.addNetworkSuggestions(listOf(builder.build()))
```

返回 `0` = 成功，`2` = 已存在（也算成功），`3` = 用户此前拒绝过（需去系统设置恢复）。

### 2.3 提交成功 ≠ 已连接（最容易漏的一步）

`addNetworkSuggestions` 是异步的，只代表「建议被接受」。若代码只挂 `onStateChanged` 回调、而内部只在提交时发一次 `CONNECTING` 事件，`CONNECTED` 永远不触发，后续发现流程一步都不走。

日志特征：提交建议后整条链路再无输出。

**必须显式轮询等待关联。**

### 2.4 SSID 会被脱敏（Android 13+）

`WifiInfo.getSsid()` 在缺少位置权限时返回 `<unknown ssid>`，即便已连上。`ColorOS` 对 ephemeral 网络还可能返回 `netId = -1`、`ipAddress = 0`。

三判据递进：

```kotlin
// 判据一：SSID 直接匹配
if (ssid == target) return CONNECTED

// 判据二：SSID 脱敏但已拿到 IP
if (ssid 含 "unknown" || ssid == "0x") && ip != 0) return CONNECTED

// 判据三：connectionInfo 不可靠，改从 ConnectivityManager 反查
val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
cm.allNetworks.forEach { n ->
    val caps = cm.getNetworkCapabilities(n) ?: return@forEach
    if (!caps.hasTransport(TRANSPORT_WIFI)) return@forEach
    val lp = cm.getLinkProperties(n) ?: return@forEach
    // lp.linkAddresses 里取 IPv4 非链路本地地址
    // lp.routes 里取 isDefaultRoute 的 gateway
}
```

第三判据同时能拿到**真实网关 IP**，用于 mDNS 失败时兜底直探 `:7000`（不要硬编码 `192.168.5.1`）。

### 2.5 无 Internet 的 ephemeral 网络

车机热点通常不提供外网，系统会标记 `isUsable: false` 并可能主动断开。表现：`handleWCNSelectNocandidates`。

- 保留 suggestion，系统会在下次扫描到该 AP 时自动重连
- 不要把「能访问外网」当成关联成功的判据
- 该网络是 ephemeral，`cmd wifi list-networks` 里看不到，别以为没保存

### 2.6 `cmd wifi status` 会说谎

会话正在正常推流时，`cmd wifi status` 仍可能打印 `Wifi is disabled` / `not connected`——因为这条链路不走常规 WiFi 客户端状态机。

**判断是否真的连上，只看 `ip -4 addr show wlan0` 里有没有同网段地址**（实测 `192.168.5.101`）+ `/proc/net/tcp` 里到车机是否有连接。不要用 `cmd wifi status` 下结论。

### 2.7 手机同时开着 USB 网络共享会污染路由

`ip route` 出现 `10.189.218.0/24 dev rndis0` 时，`adb shell` 里的 `ping` / `nc` 探测会走错接口，得出「车机不可达」的假结论。

**不要用 adb shell 探测车机连通性**，必须让 App 自己在目标网络里探。
（唯一例外：车机已在同网段且 `ip route` 有 `192.168.5.0/24 dev wlan0` 时，`nc` 探端口是有效的——实测可用。）

## 3. mDNS / NsdManager

- 在无 Internet 的 ephemeral 网络上，`NsdManager` 的注册与发现可能受限，日志会出现 `Skipping SocketKey{...} as ifIndex ... was requested`
- 注册失败不应阻断发现流程，两者独立
- 发现顺序：**先注册自己**（`_carplay-ctrl._tcp`），再发现车机。车机建立 AirPlay 会话时会反查对端 control 服务
- 从结果里解析服务时，`serviceType` 可能带前导点（`._airplay._tcp`），比较前先规范化

## 4. 反编译 / 重打包（apktool 链路）

改别人 APK 验证协议假设时，这条链路的坑比协议本身更容易骗人。

### 4.1 `zipalign` 拒绝覆盖旧文件，会让 `&&` 链静默断掉

```
./build && zipalign ... && apksigner ... && adb install ...
```

`zipalign` 对已存在的输出文件直接失败 → 后续 `apksigner` 根本不执行 → `adb install` 装的是**上一次的包**。表现：改了半天代码「完全没生效」。

**对策：构建脚本开头先删全部产物。**

```bash
rm -f "$OUT.apk" "$OUT-aligned.apk"
```

同理 `apksigner` 输出的 idsig 文件也会导致重签失败。

### 4.2 `rm -rf` 清不掉含 `$` 的目录

smali 里形如 `a3$a.smali` 的匿名内部类文件名，`rm -rf <dir>` 在某些 shell 组合下会失败，apktool 于是复用旧 manifest / 旧 smali，报：

```
smali has not changed
AndroidManifest.xml has not changed
```

版本号卡在旧值就是这个原因。

**对策：Windows 下用 PowerShell 删。**

```bash
powershell -NoProfile -Command "Remove-Item -Recurse -Force 'C:\path\build' -ErrorAction SilentlyContinue"
```

### 4.3 `MSYS_NO_PATHCONV` 会把产物写到字面量目录

`export MSYS_NO_PATHCONV=1` 是给 `adb shell` 用的，**不要带进构建脚本**。若脚本里拼的是 Windows 绝对路径（`C:/Users/...`），java.exe 会把它当相对路径，产物落到 `C:\c\Users\...` 这种目录里，然后构建"成功"但目标位置没有新包。

典型现场：`ls /c/c/Users/...` 能翻出一堆 apk。

**对策：构建命令前 `unset MSYS_NO_PATHCONV`；脚本内部用 `cygpath -w` 转路径给 java。**

### 4.4 只校验「包存在」等于没校验

每次出包必须验三样，任一项不对就停下：

```bash
aapt2 dump badging "$APK" | grep -i versionName       # 版本号是否是新值
unzip -p "$APK" classes.dex | grep -c "你的插桩字符串"   # 改动是否真的进去了
adb shell dumpsys package <pkg> | grep versionCode     # 装到手机上的是否是新包
```

## 5. smali 条件码语义（改门控必错的地方）

写反不会报错，只会让判断恒真/恒假，而且症状很像「逻辑本来就不通」。

| 指令 | 语义 | 记忆 |
|---|---|---|
| `if-eqz vA, :label` | `vA == 0` 则跳 | **e**quals **z**ero |
| `if-nez vA, :label` | `vA != 0` 则跳 | **n**ot **e**quals **z**ero |
| `if-eq vA, vB, :label` | 两值相等则跳 | 对象/整数通用 |
| `if-ne vA, vB, :label` | 两值**不**相等则跳 | 最容易写反的一个 |

实测踩过两次：

- 想做「每 N 次才换模式」，写成 `if-nez` → 变成「非 0 就跳」，结果每个 tick 都换
- 想做「等于控制通道则放行」，第一处标签写反 → 门控恒为 false，整体流程看起来像「通道不接受」

改完必须用日志确认门控的真实返回值（`gatePassed==true/false`），不要靠读代码确认。

## 6. logcat 取证

### 6.1 过滤等级会把插桩整条滤掉（最坑的一次误判）

`logcat -s TAG:I` 只放行 `I` 及以上。若插桩用的是 `Log.d`，**一行都抓不到**，于是误判「这段代码没执行」，进而推翻正确结论。

**对策：取证期间一律 `-s TAG:V`。**

### 6.2 `logcat -f` 后台落盘不可靠

`nohup logcat -f /sdcard/x.log &` 多次出现「进程在、文件不增长」或被杀后写坏。联调时用它做长时抓取可以，但**每次判断结论前先用 `logcat -d` 直接读缓冲复核**。

```bash
adb shell "logcat -c"                                   # 开抓前清空
adb shell "logcat -d -v time -s A:V B:V" | tail -40      # 随时复核
```

### 6.3 一次性拉全缓冲，再本地过滤

设备侧 `grep` 在行数大时容易截断。做法：`adb shell "logcat -d -v time" > local.txt`，然后在 PC 上 `grep`/`awk`。按时间窗口切片用 `awk '$2>="22:31:50.900" && $2<="22:31:51.200"'`。

## 7. socket 与私有数据取证

### 7.1 `ss` 在 ColorOS 上没权限

```
ss -tanp  →  netlink denied
```

**改用 `/proc/net/tcp` 与 `/proc/net/tcp6`，这两个是可读的。**

```bash
adb shell "cat /proc/net/tcp /proc/net/tcp6"
```

字段含义（空格分隔）：`sl local_address rem_address st tx_queue:rx_queue ...`

- 地址是**十六进制、每字节倒序**：`192.168.5.1` → `0105A8C0`，`192.168.5.101` → `6505A8C0`
- 端口是普通十六进制：`5001=0x1389`、`6030=0x178E`、`7000=0x1B58`
- `st`：`01`=ESTABLISHED、`06`=TIME_WAIT、`08`=CLOSE_WAIT
- `tx:rx` 是**队列里滞留的字节数**，不是累计量。两侧长期 `0:0` = 双向都没在传

`TIME_WAIT` 堆积说明我方已主动关闭；对「谁先断」的判定要用这个，不要靠猜。

### 7.2 裸探测端口是否有数据推来

不确定「车机不推」还是「我们这条 socket 特殊」时，直接在手机上另开一条：

```bash
adb shell "cd /data/local/tmp; rm -f probe.bin; timeout 12 nc -w 8 192.168.5.1 5001 > probe.bin; ls -l probe.bin"
```

`probe.bin` 为 0 字节 → 车机对该端口任何新连接都不主动发，问题在对端策略或会话状态，不在客户端实现。

### 7.3 私有数据（shared_prefs）用 run-as 直接读

比猜日志快得多，尤其适合读应用缓存下来的「车机能力摘要 / 上次失败原因 / 已选设备」：

```bash
adb shell "run-as <pkg> ls /data/data/<pkg>/shared_prefs/"
adb shell "run-as <pkg> cat /data/data/<pkg>/shared_prefs/xxx.xml"
```

实测可读到：`selected_address`（上次选中的车机 MAC）、`car_<MAC>.uuid/.channel`、`state=FORWARDING_NATIVE main=1280x768@30`、`reason=CURRENT_WIFI_SELECTED_NETWORK_LOST`。

### 7.4 `exported=false` 的 Service 拉不起来

`am start-foreground-service` 报 `Permission Denial: not exported`；`run-as <pkg> am ...` 同样不行（仍是 shell uid 调用 AMS）。

**只能走 UI**。UI 自动化很脆弱，先用 `uiautomator dump` 拿坐标再 `input tap`，并在每次点击前确认 `dumpsys window | grep mCurrentFocus`，否则会点到其它应用（实测误点进过 QQ 和系统蓝牙设置页）。

## 8. 其它

### 8.1 uiautomator dump 失败

BLE 扫描高频重绘导致 `could not get idle state`。对策：UI 更新用 `uiHandler` + 250ms 节流。

### 8.2 蓝牙重启后车机弹窗

车机会弹「请求访问你的短信」遮挡 UI，自动化点击前需先 `KEYCODE_BACK` 或点掉。

### 8.3 Git Bash 路径转换

`adb shell` 传 `/sdcard/...` 会被转成 Windows 路径。前置：

```bash
export MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL='*'
```

注意：这个变量只对 `adb` 类命令开，构建脚本前要 `unset`（见 4.3）。

### 8.4 Kotlin 字符串模板

`"ip=$ipToString(x)"` 编译报 `Function invocation expected`。必须 `"ip=${ipToString(x)}"`。

### 8.5 应用界面误操作会打断会话

用 `input tap` 驱动 UI 调试时会切走焦点，投屏会话随之掉线。要么先做完取证再碰 UI，要么把 UI 操作放到独立阶段。

## 9. Kotlin `require*` 辅助方法是 `throw`，不是日志（本项目踩得最贵的一次）

反编译出来的 apk 里，这类「小工具类」是 Kotlin 契约函数（`require`/`check`/`requireNotNull`）编译后的产物：

```smali
# io/carplay/app/secured/d0.smali
.method public static synthetic c(Ljava/lang/String;)V
    new-instance v0, Ljava/io/IOException;
    invoke-direct {v0, p0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V
    throw v0            # ← 每调一次就抛一次
.end method
```

同族签名（`b`=IllegalArgumentException、`c`=IOException、`d`=IllegalStateException、`f`=…）**全部是 throw**。
原始 dex 里它们只出现在错误分支，所以「有 272 个调用点」不等于「正常路径会抛」。

**判别方法**：看到 `invoke-static {vX}, Lxxx;->c(Ljava/lang/String;)V` 想当日志用时，先看该类的 `.method` 体里
有没有 `throw`。有就一律不能用。

**本项目实际踩坑清单**（V1.6.0 引入，`hx.smali` 9 处，V1.7.10 修复）：

| 方法 | 埋点 | 触发时机 | 症状 |
|---|---|---|---|
| `MarkTcpAccept` | `V160_CTRL_TCP_ACCEPT` | 车机每次连 ctrl | 请求处理中断、车机收不到 200 |
| `MarkInvited` | `V160_CTRL_INVITATION_ACCEPTED` | 车机下发 connect | 邀请路径死掉，且 `INVITED` 已置 true |
| `AwaitInvitation` ×4 | `V160_INVITATION_*` | 连 7000 前 | **立刻抛异常 → 6 s 等待根本没发生** |
| `BluetoothId` ×3 | `V160_TXT_ID=*` | 死代码 | 无害但污染 |

**正确的日志出口**：自己新加一个静态方法，内部 `android.util.Log.i(TAG, msg)`：

```smali
.method public static Log(Ljava/lang/String;)V
    .locals 1
    const-string v0, "OcarPlay"
    invoke-static {v0, p0}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
    return-void
.end method
```

**批量修复时注意**：如果只按「marker 之后」替换，会把 marker 之前的调用点漏掉 —— 这正是本项目
V1.7.4 修了 `SaveName` 却漏掉 `V1.6.0` 段 9 处的原因。按「调用点列表」替换，不要按行号区间替换。

> 另一个同族坑：`AwaitInvitation()` 的调用点在 `:try_end_12` 与 `:try_start_13` 之间 —— 即**不在任何 try 内**。
> 它在 smali 里抛 IOException 会直接冒到方法外（外层只有 `catchall` 覆盖后面那段），表现为「连接流程莫名中断」。

## 10. iOS 17 的 sysdiagnose：`UnifiedLog` 会给你 0 行

`0x600d` 数据块里的条目在 iOS 16/17 上换成了 `0x6004` 类型（proc 名与消息**内联**，不再走 catalog 引用）。
`UnifiedLog 0.3` 只认 `0x6001/0x6002/0x6003`，遇到 `0x6004` 记一条 `Unexpected tag value` 后对齐错乱，
后续 `proc_info` 查不到 → 每条日志都被 `skipping log` 丢弃。**而且 `Parse()` 的异常被内部 `except` 吞掉，
返回值仍是 `True`**，所以脚本「跑成功了但 0 条」。

判别：日志里出现

```
Unexpected tag value 0x6004 @ 0x2490 (Expected 0x6001, 0x6002 or 0x6003)
Could not find proc_info with proc_id1=281474976710656 proc_id2=0
```

就是这个问题。

可用替代：`scripts/sysdiag_strings_probe.py` —— 按 `bv41` 头解压 + 扫字符串。
注意压缩体的切片长度必须用头部给的 **压缩长度**（`body[12:12+csize]`），多带 4 字节 padding 会让
lz4 报 `Decompression failed ... Error code: 14289`。

---

## 11. 「日志没出现」不等于「事件没发生」——三种假日志

把结论建立在「某条日志一次都没打」上是这个项目最贵的错误来源。已知三种成因，全都不会报错：

| 成因 | 现象 | 判别 | 实例 |
|---|---|---|---|
| **日志方法其实是 throw** | 调用点抛异常，后续代码不执行；且异常常被上层 catch 吞掉 | 看方法体：`d0.c(String)` = `throw new IOException(msg)` | `MarkTcpAccept` 在 accept 循环里抛 → 整个 ctrl accept 线程死掉 |
| **条件码写反，方法提前 return** | 只在「本该打印」的分支上 return，反而最该打的那次不打 | 逐条念一遍：`if-eqz` 是零则跳、`if-nez` 是非零则跳 | `hx.MarkInvited` 用 `if-eqz` 守卫 `sput` → `INVITED` 恒 false |
| **方法根本没被调用** | 静态代码看着没问题，运行路径不经过它 | 在方法第一行无条件插一条打点，先证明进得来 | `i0.W()` 的 `EVENT_READY` 门控把所有分支挡在外面 |

### 11.1 写入前的自检清单

1. **这个「日志方法」的方法体是什么？** 打开那个类，读 `b/c/d/f` 这几个单字母方法。
   Kotlin 的 `require*` 编译出来就是 `throw`，不是 `Log`。
2. **守卫条件的方向对不对？** 把注释和代码一起念：`if-eqz v0, :ret` 念成
   「v0 为零就去 ret」。然后问：**第一次调用时 v0 是几？** 大多数「只打一次」的标志位
   第一次都是 0，所以用 `if-eqz` 守卫「已置位则跳过」必然第一次就跳过。
3. **这个方法有没有前置门控？** 有的话，门控不满足时补一条 `SKIP_*` 日志，
   否则你在外面永远分不清「没进方法」和「进了但没走到」。
4. **结论的强度**：只有一条打点、且它可能是假日志时，结论最多写成「未观测到」，
   不能写成「不存在」。本项目 2026-10-31 就因此把「车机从不下发 ctrl 邀请」当成了事实。

### 11.2 正确的插桩尺寸

- 一个流程至少 **进/出两点**：`X_ENTER` 与 `X_EXIT result=...`。
- 分支点上要有 **`SKIP_<原因>`**，不要只打成功分支。
- 数值用统一出口（如 `hx.LogInt(String prefix, int v)`），别在每个调用点各 new 一个 `StringBuilder` —— 后者最容易把 `.locals` 写爆。
