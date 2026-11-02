# -*- coding: utf-8 -*-
"""生成 README.md 与 RENAME-MAP.md（内容来自 rename_map + features.json）。"""
import json, os
import rename_map as RM

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FEAT = json.load(open(os.path.join(ROOT, 'tools', 'features.json'), encoding='utf-8'))
BY_FQ = {r['fqcn']: r for r in FEAT}

SECURED = [x for x in RM.CORE if '.secured.' in x[0]]
PROTO = [x for x in RM.CORE if '.prototype.' in x[0]]


def table(items):
    out = ['| 原混淆名 | 可读名 | 行数 | 命名依据 |', '|---|---|---|---|']
    for fq, new, why in items:
        r = BY_FQ.get(fq, {})
        out.append('| `%s` | **%s** | %d | %s |' % (fq.replace('io.carplay.app.', ''), new, r.get('loc', 0), why))
    return '\n'.join(out)


README = """# OcarPlay V1.7.29 — 可读版 Java 工程

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

| 阶段 | 语法错误 | 语义错误 | 说明 |
|---|---|---|---|
| 原始 jadx 产物 | 20 | 未统计（被语法错误挡住） | 语法错误集中在 `k4` / `t4` / `o5` / `BinaryPlist` |
| 本工程 | **0** | **426** | 20 处语法缺陷已修，语义错误如实保留 |

**为什么「原始 20 处 = 本工程 20 处」不能当作验收标准**：javac 一旦遇到语法
错误就不进入语义分析阶段，此时报出来的 20 条全是语法类，语义错误一条都
不会出现。把 20 处语法缺陷修掉后，才看到真正的问题量。

**这 426 条全部是 jadx 的反编译缺陷，不是重命名引入的**，可以直接在原始
jadx 产物里找到同样的写法：

| 原始 jadx 位置 | 本工程位置 | 缺陷 |
|---|---|---|
| `prototype/u.java:45` `private final long c;` | `CarLifeBridge.java` | 字段 `c` 遮蔽了嵌套枚举 `c` |
| `prototype/u.java:66` `this.a != c.BLOCKED` | 同上 | 于是报 `long cannot be dereferenced` |
| `secured/i0.java:485` `d0.c("invalid CarPlay control port")` | `AirPlaySession.java:507` | `d0` 同时是字段名和类名 |

试过写脚本自动补限定名（`tools/fix_obscured_types.py`），一轮能消掉 277 条，
但补限定名时要在「本文件嵌套类型」和「同包顶层类型」之间二选一，选错会把
错误从「找不到符号」变成「类型不匹配」——错误数下降、语义却已绑错。对一个
要拿去和原 APK 对照的工程，这种改动比不改更危险，所以**撤回了，保留纯净态**。


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
"""

MAP = """# 混淆名 ↔ 可读名 对照表

生成自 `tools/rename_map.py`（唯一事实来源，改名字请改那里再重跑脚本）。

- **共 39 个类重命名**，其余 248 个保持原名（多数是无特征的工具/日志类）。
- 「行数」是该类在 jadx 产物里的规模，用来判断值不值得先读。

## secured 包（协议栈本体）

%s

## prototype 包（业务层）

%s

## 为什么有些类没改名

| 情况 | 例子 | 原因 |
|---|---|---|
| 本来就是语义名 | `WirelessCarPlayService`、`MainActivity`、`LogActivity` | 原厂没混淆这些入口类 |
| 无特征字符串 | `t4`(3003 行)、`x`(1428)、`y0`(895) | 类里只有 `StringBuilder` 拼接的日志，判不出职责 |
| 疑似日志辅助 | `b`、`r6`、`l4`、`f5`、`u5` | 同上，且被几十个类调用 |
| 单字母 + 多义 | `secured.d` vs `prototype.d` | 同名不同类，强行改名会串味 |

想继续推进命名，做法是：从 `tools/features.json` 里挑 `loc` 大、`strings` 有
业务关键字的类，先确认它被谁调用（`grep`），再补进 `rename_map.CORE`，重跑脚本。

## 与 APK 里 smali 的对应

| 可读名 | smali 路径 |
|---|---|
%s
""" % (
    table(SECURED),
    table(PROTO),
    '\n'.join('| `%s` | `%s/%s.smali` |' % (new, 'smali/' + fq.rsplit('.', 1)[0].replace('.', '/'), fq.rsplit('.', 1)[1])
              for fq, new, _ in RM.CORE),
)

open(os.path.join(ROOT, 'README.md'), 'w', encoding='utf-8', newline='\n').write(README)
open(os.path.join(ROOT, 'RENAME-MAP.md'), 'w', encoding='utf-8', newline='\n').write(MAP)
print('README.md / RENAME-MAP.md written')
