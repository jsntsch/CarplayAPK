# _carplay-ctrl TXT 键值规则 / TXT Record Rules

适用：`android-project/app/src/main/java/io/carplay/app/secured/ControlChannel.java` 的
`FillTxt(NsdServiceInfo, String)`；取值集中在同文件的 `TXT_*` 常量。
本文件是**当前实现的唯一规则表**：改值只改 `TXT_*`，不要在各调用点硬编码。

## 1. 当前键集（V1.8.24 / 361，共 10 键）

| 键 | 值 | 来源 / 说明 |
| --- | --- | --- |
| `deviceid` | 手动填的本机蓝牙 MAC | `carplayapk-local-transport` 的 `phone_bluetooth_address`；无值时回退配对快照 |
| `btaddr` | 同 `deviceid` | AirPlay 2 蓝牙联动校验 |
| `id` | 同 `deviceid` | 历史已有键，保留避免 ctrl 发现回归 |
| `model` | `iPhone15,3` | 固定 |
| `manufacturer` | `Apple Inc.` | 固定 |
| `srcvers` | **`27.2.0`** | V1.8.24 由 `27.2` 补齐为 x.y.z（规范 Table 23-20 要求）；固件 `0x8e7b0` 解析两者均 270200 |
| `osvers` | `1.0` | 车机 CarPlay 模块固件版本占位值，不影响连接 |
| `protovers` | **`1.1`** | AirPlay 2 必填；V1.7.70 为 `1.0`，**V1.7.71 改 `1.1`** |
| `features` | **`0x445F8A00,0x1C340`** | 64 位能力位域，高低 32 位逗号分隔；V1.7.70 为基础位域 `0x30e44`，**V1.7.71 改为纯无线 CarPlay 位域** |
| `flags` | `0x4` | 历史已有键，保留 |

> `osvers=1.0`（车机固件占位）与 `protovers=1.1`（AirPlay 协议版本）是两个不同字段，不要合并或互相覆盖。
> `id` / `flags` 不在现场 8 条规则里，因从 0.8.x 起就在广播中，删掉有 ctrl 发现回归风险，故保留。

### 1.1 规范符合性（V1.8.24 核查，别误判）

| 规范位置 | 服务 | 键 |
| --- | --- | --- |
| Table 23-20 (p.425) | **`_carplay-ctrl._tcp`（手机广播，本表）** | `id` = Apple device Bluetooth MAC；`srcvers` = CarPlay source version，格式 `x.y.z` |
| Table 23-22 (p.427) | `_airplay._tcp`（**车机**广播） | `deviceid` / `features` / `model` / `protovers` / `srcvers` / `flags` |

即本表 10 键里只有 `id` / `srcvers` 属于规范为 `_carplay-ctrl` 定义的字段，
其余是 `_airplay._tcp` 的键或历史遗留。**不要因为"多了 8 个键"就精简**：
Buick 固件 TXT 键表 @0xa0a3a 含 `deviceid/flags/model/protovers/pi/srcvers/id`，
`0x52c2c` 读 `srcvers`；BrowseCallback `0x53c70` 读的
`removedServices`/`services`/`ifname` 来自 Bonjour info 字典而非 TXT，
缺失只跳分支，**没有按 TXT 键拒绝服务的硬门控**（见 `troubleshooting.md` 案例 13）。
V1.7.70 曾因补/删键出过回归。

## 2. 服务实例名

`CarPlayCtrlAdvertiser.java:493`：`setServiceName(ControlChannel.ServiceName())`。

- `ServiceName()` = 手动 `bluetooth_name`，为空时兜底固定 `CarPlayAPK`。
- V1.7.69 起**不再读取系统蓝牙名 / 地址**（用户要求「只手动填」）。
- ⇒ 只有用户在设置页填的名字就是手机蓝牙名时，「实例名 = 蓝牙名称」才成立。
- **不要加 `iPhone_` 前缀**：原生 `iPhone_SCH._carplay-ctrl._tcp.local.` 是 iOS audiomxd 的命名习惯，
  车机匹配的是手机蓝牙名称本身；`CtrlServiceName()` 是死代码。
- 日志 `CTRL_ANNOUNCE name= svc= manual=`、`CTRL_SERVICE_LIVE name= port=` 可验实际生效值。
  `manual` 空且 `svc=CarPlayAPK` ⇒ 用户没填名字，去设置页补。

## 3. 三个 `features` 不能混填

| 位置 | 类型 | 方向 | 当前值 |
| --- | --- | --- | --- |
| TXT `features` | 十六进制能力位域字符串 | 手机广播 | `0x445F8A00,0x1C340` |
| AirPlay SETUP plist `features` | 14 项能力名 `List<String>`，**交集语义** | 手机 → 车机 | `uiContext` / `viewAreas` / … （`AirPlaySession` 4400 起） |
| 车机 `/info` 响应 `features` | BigInteger 位掩码 | 车机 → 手机 | 车机能力，手机只读、不填 |

SETUP 的 `name` / `model` / `deviceID` 同样是**手机**身份（`deviceID`=本机 MAC、`name`=蓝牙名、
`model`=`Build.MODEL`），与车机 `/info` 响应里的同名字段方向相反。

## 4. SETUP 方向（勿写反）

- 源码 `AirPlaySession.R1("SETUP", this.e, plist)` → `eventChannel.v()` 是**手机发请求**，`g1()` 只整理
  车机响应 Map；`zzLog` 打 `CARPLAY_SETUP_KEYS` / `CARPLAY_SETUP_RESP_KEYS`。
- 固件 `libNmeCarPlay.so` 的 `SETUP` 仅 @0xa0d60，引用 0x41222 是把收到的 RTSP 方法与
  `OPTIONS/FLUSH/RECORD/SETUP/TEARDOWN/GET/POST` 逐个比对（`call 0x8f990`）⇒ **车机解析 SETUP**；
  0x58852 / 0x5918f / 0x5b48c 查 `deviceID`；0x3e5cf 的 `streams` 在 `_UpdateStreams(AirPlayReceiverSessionRef)`。
- ⇒ **带 `deviceID/name/model/features` 的会话 SETUP 是手机 → 车机**；车机发、手机解析的是
  **SETUP 响应**（`streams` / 端口 / 加密参数）与 **`/info` 响应**（车机自己的身份 + 能力位域）。
  `EventChannel.q()` 里的 `str.equals("SETUP")` 只给入站 `SETUP /stream` 打 `STREAM_SETUP` 标签，不带身份字段。

## 5. 证据与边界

- 固件 TXT 键表 0xa0a3a 起 `deviceid/flags/model/protovers/pi/srcvers/id`；ctrl 客户端区
  0x52b00–0x53060 读 `srcvers`(0x52c2c) / `name`(0x52cd9) / `services`(0x52d18)。
  固件所有 AirPlay 版本常量是 `320.17.3`，与手机 `srcvers=27.2.0` 不一致，**尚无证据证明车机会拒绝**；
  `srcvers` 只补了格式段（x.y.z），解析数值不变。
- iOS sysdiagnose **没有打印 TXT 内容**（564MB 归档 → 329 万行全量直读，`srcvers`/`protovers`/
  `txtRecord`/`950.7.1`/`320.17.3` 全 0 命中）。dsc 静态串 @0x39d84a4 给出 iOS TXT 键全集，
  其中 `name` 是服务实例名、`services` 不是 TXT 键。⇒ 本表取值来自**现场规则 + 固件键池对照**，
  **不是 iPhone 实测值**。`950.7.1` 出自 OcarPlay 反编译，非原生值。
- 取真值只能同网抓：`dns-sd -L "<实例名>" _carplay-ctrl._tcp local`
  或 `tcpdump -i any -s0 -w carplay.pcap port 5353`（Wireshark 过滤 `dns && udp.port==5353` → TXT）。

## 6. 变更史与现场验证

- V1.7.70 / 307：按 8 条规则收敛为 10 键（撤销曾擅自补的 22 键分组字段）；
  `features=0x30e44`、`protovers=1.0`。
- V1.7.71 / 308：`features=0x445F8A00,0x1C340`、`protovers=1.1`，日志新增 `protovers=`。
- 现场确认：
  ```bash
  adb logcat -d -s CarPlayAPK | grep V170_TXT_FILLED      # n=10 / srcvers / features / protovers
  adb shell run-as io.carplay.app cat shared_prefs/carplayapk-local-transport.xml
  ```
- **待验证**：这套 TXT 是否被车机接受、能否让 Buick 把会话认成 CarPlay，均**未在真车验证**
  （车机已换且触摸正常）。别把「按规则填了」写成「车机认了」。
