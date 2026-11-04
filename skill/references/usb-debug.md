# USB 调试 / Debugging over USB

一台连着 USB 的手机 + `adb`，就能覆盖安装、启动、日志取证、崩溃定位全过程。
A phone on USB plus `adb` covers install, launch, log forensics and crash triage.

## 1. 连接与确认 / Connect and confirm

```bash
adb kill-server && adb start-server
adb devices                 # 必须显示 device，不是 unauthorized / offline
adb shell getprop ro.build.version.release   # 系统版本（需 Android 13+）
adb shell getprop ro.product.model           # 机型
```

`unauthorized` = 手机上没点「允许 USB 调试」；`offline` = 数据线/驱动/端口问题。
`unauthorized` means the on-device prompt was not accepted; `offline` is cable/driver/port.

## 2. 安装与版本核对 / Install and identify

```bash
adb install -r android-project/dist/Carplay-V1.7.61-debug.apk

adb shell dumpsys package io.carplay.app | grep -E "versionName|versionCode|debuggable"
# 期望：versionName=1.7.61  versionCode=298  flags=[ DEBUGGABLE … ]
```

覆盖安装失败时的两类原因：

| 报错 / Error | 原因 / Cause | 处理 / Fix |
| --- | --- | --- |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | 签名不同（换过 keystore） | `adb uninstall io.carplay.app` 后重装 |
| `INSTALL_FAILED_VERSION_DOWNGRADE` | versionCode 比已装的小 | 提升 `appVersionCode`，或先卸载 |
| `INSTALL_FAILED_INSUFFICIENT_STORAGE` | 空间不足 | 清理或 `adb shell pm trim-caches` |

## 3. 启动与停止 / Launch and stop

```bash
adb shell am start -n io.carplay.app/.prototype.MainActivity
adb shell pidof io.carplay.app          # 拿 pid，用于 logcat 过滤
adb shell am force-stop io.carplay.app  # 彻底停（不是 kill，能保证进程状态干净）
```

`am force-stop` 与 `kill` 的区别：前者会清掉进程与后台任务，后者可能被系统立刻拉起。
`force-stop` clears the process and its jobs; a plain `kill` may be restarted immediately.

## 4. 日志的两条通道 / The two log channels

| 通道 / Channel | 位置 / Where | 内容 / Content |
| --- | --- | --- |
| logcat | `adb logcat` | `Log.d/i/w/e`、系统与框架日志 |
| 应用内日志 | `/data/data/io.carplay.app/files/connection-log-0..4.txt` | 会话关键事件、计数器（**排障主力**） |

```bash
# 应用内日志（唯一正确的读法）
adb shell run-as io.carplay.app cat files/connection-log-0.txt | tail -60

# 只在线程/进程维度看 logcat
adb logcat -d --pid=$(adb shell pidof io.carplay.app) | tail -100
```

> **不要**用 `adb shell cat /data/data/...`：非 debuggable 或非 root 会直接 `Permission denied`。
> `run-as` 是标准入口。
>
> Do not `cat` the path directly — use `run-as`, which requires a debuggable package.

## 5. 干净复现一次 / A clean run

```bash
adb shell am force-stop io.carplay.app
adb shell run-as io.carplay.app ls files/            # 看清有哪些文件
adb shell run-as io.carplay.app rm -f files/connection-log-0.txt \
    files/connection-log-1.txt files/connection-log-2.txt \
    files/connection-log-3.txt files/connection-log-4.txt \
    files/wireless-session-last.txt
adb shell am start -n io.carplay.app/.prototype.MainActivity
sleep 45
adb shell run-as io.carplay.app cat files/connection-log-0.txt | tail -40
```

**为什么不用通配符**：`rm -f files/connection-log-*.txt` 在 `adb shell` 里常被二次转义吃掉，
命令成功返回但文件还在，于是下一轮把上一轮的日志当新证据。
**Why not a glob**: the pattern is frequently swallowed by the extra shell layer; the command
succeeds while the old logs survive, and the next analysis mistakes them for fresh evidence.

## 6. 稳定跟踪 / Follow along

```bash
# 每 5 秒打印最新一行 AUDIO_FLOW（观察包率与丢帧是否稳定）
while true; do
  adb shell run-as io.carplay.app cat files/connection-log-0.txt | grep AUDIO_FLOW | tail -1
  sleep 5
done
```

判断「有没有在动」永远看**计数是否增长**，不要看最后一行内容是否漂亮。
Liveness is decided by counters increasing, not by how nice the last line looks.

## 7. 崩溃与 ANR / Crashes and ANRs

```bash
# 1) 有没有崩溃
adb logcat -d -b crash | tail -60

# 2) 有没有 ANR（应用无响应）
adb shell ls /data/anr/                 # 需要 root 或 debuggable + run-as
adb bugreport bugreport.zip             # 最完整：含 ANR trace、dumpsys、logcat
```

从 `bugreport.zip` 里取 ANR：

```bash
unzip -o bugreport.zip -d bugreport
ls bugreport/FS/data/anr/                # anr_YYYY-MM-DD-HH-MM-SS-XX
grep -n "held by thread\|waiting to lock\|Blocked" bugreport/FS/data/anr/anr_* | head
```

> 注意：ANR 文件里 `held by thread` 有时指向的是**误报**的监听线程，真正持锁者要看
> `"main" prio=5 ... waiting to lock <0x…> held by thread <id>` 的 id，再去同文件里找那个线程栈。
>
> The `held by thread` label can point at an unrelated watcher; trace the lock address to the
> actual owner thread inside the same file.

## 8. 清场 / Cleanup

```bash
adb shell am force-stop io.carplay.app
adb shell run-as io.carplay.app ls files/     # 需要留存再拉出来
adb pull /sdcard/Download/xxx 2>/dev/null     # 导出的日志/截图
```

临时截图与 `ui.xml` 用完即删，不留在仓库里。
Delete temporary screenshots and `ui.xml` dumps right after use; never commit them.
