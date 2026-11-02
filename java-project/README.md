# OcarPlay V1.7.29 — 可读版 Java 工程

把 `Carplay-V1.7.29.apk`（versionCode 298）反编译后，按**语义**重命名类名得到的
分析用源码工程。目的是让人能顺着 CarPlay 的连接链路读代码，而不是在一堆
`i0` / `y5` / `hx` 里迷路。

## 这份代码是怎么来的

```
Carplay-V1.7.29.apk
   │  apktool d -f -r           反编译成 smali（只取代码）
   ▼
smali（全混淆名）
   │  tools/dex_rename.py       按字节码描述符改名：Lpkg/Cls; → Lpkg/New;
   ▼
smali（已改名）
   │  apktool b                 重新打包
   │  jadx --no-res             反编译成 Java
   ▼
src/main/java/...               （本工程，39 个类重命名）
   │  tools/annotate.py         给重命名类注入中文职责注释
   │  tools/fix_jadx_defects.py 修反编译缺陷（?? 占位符、畸形 for 等）
   ▼
最终产物
```

**为什么在 smali 层改名，而不是在 Java 层做文本替换**

jadx 的 Java 里同一个简单名会有多种含义：`secured/l.java` 是顶层类，
而 `secured/i0.java` 里还有一个 `public interface l`。同一个 `l`，

- 在 i0 内部表示「i0 的嵌套接口」；
- 在 d0 里写作 `i0.l`，表示「i0 的嵌套接口」；
- 在其它类里写作 `l.foo()`，表示「顶层类」。

正则无法区分这三种语义，改错一处就是编译错误。字节码里类型永远写成
带分号的描述符 `Lio/carplay/app/secured/i0;`，不存在遮蔽、不存在泛型位置
遗漏，改描述符等于定义点和所有引用点同时改。所以改的是 dex，不是文本。

所有脚本都在 `tools/` 下，可重跑：

```bash
python tools/build_project.py --apk <apk> --work <构建目录>   # 全链路
python tools/annotate.py            # 只重注入类注释
python tools/fix_jadx_defects.py    # 只重跑反编译缺陷修复
python tools/fix_obscured_types.py  # 修「类型名被同名字段遮蔽」的编译错误
```

## 目录

```
src/main/java/io/carplay/app/
├── secured/     224 个类 —— 协议栈本体（RTSP、事件通道、配对、加密、编解码）
└── prototype/    52 个类 —— 业务层（Service、Activity、mDNS 广播、配对会话）
```

两个包名**故意保留原样**：包不影响阅读，改了要同步一堆 import，收益低风险高。

## 先读哪几个类

按「一条触摸从车机回到手机」的顺序：

| 顺序 | 类 | 原混淆名 | 干什么 |
|---|---|---|---|
| 1 | `CarPlayCtrlAdvertiser` | `prototype.h` | 广播 `_carplay-ctrl._tcp`，让车机知道这台手机在等它 |
| 2 | `AirplayServiceProbe` | `prototype.j` | 发现车机 `_airplay._tcp`，选一个候选去连 |
| 3 | `AirPlaySession` | `secured.i0` | SETUP / INFO / RECORD 全流程，4400 行，App 的心脏 |
| 4 | `EventChannel` | `secured.y5` | 事件通道：车机推过来的触摸、按键、`/command` 全走这 |
| 5 | `ControlChannel` | `secured.hx` | 控制通道：邀请与 iAP2 |

完整对照见 `RENAME-MAP.md`。

## 命名原则（重要）

**只改有证据的，不猜。**

每个新名字都有可核对的依据，写在 `RENAME-MAP.md` 的「命名依据」列里，来源只有三类：

1. **类里的协议字符串常量** —— 如 `_carplay-ctrl._tcp`、`application/pairing+tlv8`、`X-Apple-Client-Name`
2. **它实现的框架接口** —— 如 `implements NsdManager$ResolveListener`
3. **已经确认的分析结论** —— `docs/v1724-*.md`、`v1728-*.md`、`v1729-*.md`

对不上任何一条的类**保留原名**（如 `t4`、`x`、`y0`），因为它们大多是各模块的
日志拼接辅助类，字符串里全是 `StringBuilder`，没有可判定的身份特征。
**给它们编个名字比保留混淆名更有害。**

## 编译现状

用 `android-36/android.jar` + `bcprov-jdk18on-1.79.jar` 做 classpath，
`javac -source 8 -target 8` 的结果：

| 阶段 | 语义错误 | 动作 |
|---|---|---|
| 原始 jadx 产物 | 被 20 处语法错误挡住，未统计 | — |
| 44 类改名后 | 426 | 起点 |
| 改 4 个类名 + 补 catch 转发变量 | 267 | `d0→SyntheticRefs` 等 |
| 改 20 组嵌套类型名 | 180 | `fix_type_shadow.py` |
| 同包顶层类用 FQCN | 140 | 16 处 |
| 按文件逐个修（本轮） | **81** | 净减 59 条（-42%） |

`X cannot be dereferenced` 这类从 86 条降到 **0**。完整过程见
`CarplayAPKBuild/reports/compile-progress.md`，剩余清单见
`CarplayAPKBuild/logs/residual_81.txt`。

**为什么「原始 20 处 = 本工程 20 处」不能当作验收标准**：javac 一旦遇到语法
错误就不进入语义分析阶段，此时报出来的全是语法类，语义错误一条都不会出现。
这一轮又验证了两次：改完嵌套类型名后冒出的 2 条构造器错误，又把 160 多条
语义错误挡在后面，看起来「只剩 2 条」。

**426 条的根子是一个 Java 语言事实**：R8 把字段名和类型名压成同一个字母，
而 JLS 6.5.2 规定 ambiguous name 重新分类时**变量优先于类型**。所以
`d.UNCONFIGURED`、`IccoaBridge.d.UNCONFIGURED`、连全限定名
`io.carplay.app.secured.IccoaBridge.d.UNCONFIGURED` 全部被同名字段截胡，
报 `long cannot be dereferenced` 或 `non-static variable d ...`。
**限定名救不了，只能改名**：

- **嵌套类型** → 改类型名（字段名要留着和 APK 逐字段对照，类型名只影响 `$Inner` 后缀）
- **同包顶层类** → 全限定名可以（包作用域里没有字段查找）

本轮按文件修 59 条，靠的是三个从 smali 里挖出来的**结构性事实**：

1. **全库 0 个 `Throws` 注解**。`apktool d` 出来的 565 个 smali 里
   `Ldalvik/annotation/Throws` 出现 0 次，说明 `throws Throwable` / `throws IOException`
   全是 jadx 根据方法体反推的，**不是原 APK 的签名**。删掉它们才更接近原包。
   这一类清掉 19 条（7 个 `run()` + 12 处转发链）。
2. **d8 会把「子类构造器只调 super」优化掉**。`q$a`、`r4`、`AirPlaySession$f`、
   `PairingSession$m` 在 dex 里实例化时是 `new-instance 子类` 紧跟
   `invoke-direct 子类 -> 父类.<init>(...)`，子类自己没有 `<init>`；另有一批
   带一个多余 `int` 参数的 **synthetic 桥接构造器**（javac 给内部类私有构造器
   生成的占位参数）。jadx 会把这两类都还原错，必须按 smali 补构造器或还原实参。
3. **寄存器复用会让 jadx 造出成堆的假别名**。`t4.P()` 一个方法里
   `scheduledExecutorService` / `…2` / `…3` / `…4` / `…NewSingleThreadScheduledExecutor`
   六个名字互相赋值，而 smali 全方法只调**一次** `newSingleThreadScheduledExecutor`。
   合并成一个变量后该方法少 79 行，且删掉的都是 `x = x` 这种自赋值。

顺带修出的**语义错误**（不是编译错误，但影响等价性）：

- `PairingSession.a1` 里 `if (isClosed()) return;` 被 jadx 取反了。smali 是
  `if-nez p0, :cond_3`，即 `isClosed()` 为 **false** 时才跳过上报，应写
  `if (!isClosed())`。这类「条件方向搞反」在 jadx 输出里比想象中常见。
- `t4.P()` 里 `Throwable e` 实际只接 `catch (Exception e3/e4/e5)`，收窄成
  `Exception e` 才能喂给 `R(Exception)`。
- `AirPlaySession.t0` 末尾 `this.D = "videoPlayback"` 是假的：`D` 是
  `volatile boolean`，smali 是 `iput-boolean v1`，而 `v1` 在该方法唯一的 int
  赋值是 `const/4 v1, 0x1`。

**新发现的另一类缺口**：jadx 有 **15 处方法体完全没还原**，只留下
`throw new UnsupportedOperationException("Method not decompiled: ...")` 桩
（`AirPlaySession` 7 处、`WirelessCarPlayService` 2 处，`z0`/`y0`/`x0`/
`AudioRelay`/`PairingSession`/`CarLifeBridge` 各 1 处）。这些必须按 smali
重新还原方法体，不是改命名能解决的。

剩下 81 条集中在 30 个文件，性质是 jadx 方法体还原错误：`this = xxx` 这种
非法赋值、变量类型张冠李戴（`String e` 被赋 `Exception`）、raw type 泛型、
`Map` 未参数化、桥接方法被内联进实现体（`Q1` 在 smali 里只有 11 行转发桩，
Java 版却有 70 行）。继续修等于按 smali 重写方法体。

试过写脚本自动补限定名（`tools/fix_obscured_types.py`），一轮能消掉 277 条，
但它在「本文件嵌套类型」和「同包顶层类型」之间二选一时会绑错，错误数下降、
语义却已变。**已撤回**，改用「成员存在性唯一匹配 + 改名」的可证明路径。

试过写脚本自动补限定名（`tools/fix_obscured_types.py`），一轮能消掉 277 条，
但它在「本文件嵌套类型」和「同包顶层类型」之间二选一时会绑错，错误数下降、
语义却已变。**已撤回**，改用「成员存在性唯一匹配 + 改名」的可证明路径。


其余已知限制：

- 缺 Android SDK 的 `R` 类（仅 3 处 `R.drawable`，已改成 `android.R.drawable`）、
  缺 `.so`、缺资源，所以 `javac` 通过也**不等于**能直接打出可运行 APK。


- **字段名和局部变量名仍是混淆的**（`a`、`b`、`f0`…）。原因：`d`、`f`、`m`
  这类单字母类在 `secured` 和 `prototype` 两个包里含义完全不同，全局按名字
  替换会让「变量名」和「类型名」对不上（试过，出现了 `f avcEncoder` 这种）。
  可读性靠**类名 + 类注释 + 关键常量值**承担。
- **嵌套类没有改名**，保持 `h`、`b`、`c` 原样；只改顶层类。

## 和 skill / docs 的关系

- `~/.workbuddy/skills/ocarplay-carplay-connect/SKILL.md` 里的铁律大量引用
  混淆名（`i0.t0`、`y5.z()`），本工程的 `RENAME-MAP.md` 提供双向对照。
- 版本演进与每次改动的判据在 `../CarPlay-Build/docs/v17xx-*.md`。
