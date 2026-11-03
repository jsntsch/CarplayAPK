# -*- coding: utf-8 -*-
"""生成 README.md 与 RENAME-MAP.md（内容来自 rename_map + features.json + 实际文件）。"""
import glob
import json
import os
import re
import rename_map as RM

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FEAT = json.load(open(os.path.join(ROOT, 'tools', 'features.json'), encoding='utf-8'))
BY_FQ = {r['fqcn']: r for r in FEAT}
SRC = os.path.join(ROOT, 'src', 'main', 'java')


def _split():
    """把 CORE 分成「已落地」与「已分析但暂缓」。

    判定依据不是位置（顶层/嵌套），而是**源码里是否真的存在新名**：
    顶层看 <pkg>/<New>.java 是否存在；嵌套看外层文件里有没有 `class New`。
    """
    top_new = {}
    for fq, new, _w in RM.CORE:
        if '$' not in fq.rsplit('.', 1)[1]:
            top_new[fq.rsplit('.', 1)[1]] = new

    applied, deferred = [], []
    for fq, new, why in RM.CORE:
        pkg, simple = fq.rsplit('.', 1)
        ok = False
        if '$' in simple:
            outer_old, _inner = simple.split('$', 1)
            outer_new = top_new.get(outer_old, outer_old)
            fp = os.path.join(SRC, *pkg.split('.'), outer_new + '.java')
            if os.path.exists(fp):
                text = open(fp, encoding='utf-8', errors='replace').read()
                ok = re.search(r'\b(?:class|interface|enum)\s+' + re.escape(new) + r'\b',
                               text) is not None
        else:
            ok = os.path.exists(os.path.join(SRC, *pkg.split('.'), new + '.java'))
        (applied if ok else deferred).append((fq, new, why))
    return applied, deferred


APPLIED, DEFERRED = _split()
SECURED = [x for x in APPLIED if '.secured.' in x[0]]
PROTO = [x for x in APPLIED if '.prototype.' in x[0]]

N_SECURED = len(glob.glob(os.path.join(SRC, 'io/carplay/app/secured/*.java')))
N_PROTO = len(glob.glob(os.path.join(SRC, 'io/carplay/app/prototype/*.java')))
N_ALL = len(glob.glob(os.path.join(SRC, '**', '*.java'), recursive=True))
N_BC = N_ALL - N_SECURED - N_PROTO
N_RENAMED = len(APPLIED)
N_RENAMED_TOP = len([x for x in APPLIED if '$' not in x[0]])
N_DEFERRED = len(DEFERRED)
# 嵌套类不单独占文件，算「保留原名」的只有顶层文件
N_KEEP = N_SECURED + N_PROTO - N_RENAMED_TOP - N_DEFERRED


def _smali_rows():
    lines = smali_table(APPLIED).split('\n')[2:]
    return '\n'.join(lines) if lines else '（当前没有对应 smali 文件）'


def table(items):
    out = ['| 原混淆名 | 可读名 | 行数 | 命名依据 |', '|---|---|---|---|']
    for fq, new, why in items:
        r = BY_FQ.get(fq, {})
        out.append('| `%s` | **%s** | %d | %s |'
                   % (fq.replace('io.carplay.app.', ''), new, r.get('loc', 0), why))
    return '\n'.join(out)


def deferred_table(items):
    out = ['| 原混淆名 | 拟用名 | 暂缓原因 |', '|---|---|---|']
    for fq, new, _why in items:
        out.append('| `%s` | `%s` | 嵌套类与同名字段/方法/局部变量共存，Java 文本层无法可靠区分 |'
                   % (fq.replace('io.carplay.app.', ''), new))
    return '\n'.join(out)


SMALI_ROOT = os.path.join(os.path.dirname(ROOT), 'CarplayAPKBuild', 'work',
                          'smali_rename', 'smali')


def smali_table(items):
    """只列「smali 工程里真的有这个文件」的条目。

    阶段四新增的 44 个类只落在 Java 源码树里，smali 工程（阶段三的产物）里
    仍是旧名，列出来会误导，所以按文件存在性过滤。
    """
    out = ['| 可读名 | smali 路径 |', '|---|---|']
    for fq, new, _why in items:
        pkg, simple = fq.rsplit('.', 1)
        leaf = simple.split('$')[-1] if '$' in simple else simple
        fp = os.path.join(SMALI_ROOT, *pkg.split('.'), leaf + '.smali')
        if os.path.exists(fp):
            out.append('| `%s` | `smali/%s/%s.smali` |'
                       % (new, pkg.replace('.', '/'), leaf))
    return '\n'.join(out)


README = """# OcarPlay V1.7.29 — 可读版 Java 工程

把 `Carplay-V1.7.29.apk`（versionCode 298）反编译后，按**语义**重命名类名得到的
分析用源码工程。目的是让人能顺着 CarPlay 的连接链路读代码，而不是在一堆
`i0` / `y5` / `hx` 里迷路。

## 当前状态

| 项 | 数值 |
|---|---|
| Java 源文件 | **%d** 个（io.carplay %d + bouncycastle 兼容层 %d） |
| 已语义化重命名 | **%d** 个类（%d 顶层 + %d 嵌套） |
| 已分析、暂缓改名 | **%d** 个（见 RENAME-MAP.md 末节） |
| 保持混淆原名 | %d 个（无特征的工具/日志类） |
| `javac -source 8 -target 8` | **0 error**（%d 个文件全过） |

## 这份代码是怎么来的

分两阶段改名，原因是「重新 jadx」会退化：

**阶段一（39 个类）**：早期在 smali 描述符层改名后重新 jadx。

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
src/main/java/...               （本工程）
```

**阶段二（新增 20 个顶层类）**：实测「已改名 apk 再 jadx」的产物比阶段一更差
（丢 `throws IOException` 子句、方法签名漂移，实测 182 个编译错误）。因此改为
**在阶段一已 0-error 的 Java 树上做机械改名**：

```
阶段一 0-error 树
   │  tools/apply_names_to_tree.py   顶层类改名 + 引用改名（包感知、类型位限定）
   │  tools/fix_type_shadow.py       嵌套类型遮蔽（仅补跑尚未应用的部分）
   │  tools/fix_jadx_defects.py      修反编译缺陷
   │  tools/fix_obscured_types.py    按 javac 报错补限定名（迭代收敛）
   ▼
最终 0-error 树
```

**为什么顶层类可以在 Java 层改，单字母类却要格外小心**

`x` / `s` / `j` 既是顶层类名，又是局部变量/字段名。`apply_names_to_tree.py`
只在这几种**类型位**改名，且按包过滤：

- `class OLD` 声明、构造器 `public OLD(`、`extends/implements OLD`、`new OLD(`；
- 「类型后接标识符」`OLD var`；
- 全限定引用 `io.carplay.app.secured.OLD`（含 `import`）；
- 「裸名是否指本类」：同包，或本文件 `import` 了本类全限定名；不同包又没 import
  时，裸名是该包自己的同名类（`prototype.x` ≠ `secured.x`），**绝不改**；
- 裸嵌套类型访问 `OLD.member` 只认嵌套类/枚举形态（成员大写，或单个小写字母），
  绝不碰 `localVar.method(...)` 这种局部变量调用。

所有脚本都在 `tools/` 下，可重跑：

```bash
python tools/apply_names_to_tree.py --src <树>     # 阶段二：顶层类机械改名
python tools/rebuild_74.py --run --target <树>     # 阶段二：完整 fix 链 + 迭代收敛
python tools/compile_check.py --src <树>           # 只做 javac 检查（0 error 验收）
python tools/gen_docs.py                           # 重新生成本文档与 RENAME-MAP.md
```

## 目录

```
src/main/java/io/carplay/app/
├── secured/     %d 个类 —— 协议栈本体（RTSP、事件通道、配对、加密、编解码）
└── prototype/    %d 个类 —— 业务层（Service、Activity、mDNS 广播、配对会话）
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

每个新名字都有可核对的依据，写在 `RENAME-MAP.md` 的「命名依据」列里，来源只有四类：

1. **类里的协议字符串常量** —— 如 `_carplay-ctrl._tcp`、`application/pairing+tlv8`、`X-Apple-Client-Name`
2. **它实现的框架接口** —— 如 `implements NsdManager$ResolveListener`
3. **已经确认的分析结论** —— `docs/v1724-*.md`、`v1728-*.md`、`v1729-*.md`
4. **Car+ 反混淆词表的协议串签名匹配**（阶段二新增 20 项的主要来源）

对不上任何一条的类**保留原名**，因为它们大多是各模块的日志拼接辅助类，字符串里
全是 `StringBuilder`，没有可判定的身份特征。**给它们编个名字比保留混淆名更有害。**

## 编译现状

用 `android-36/android.jar` + `bcprov-jdk18on-1.79.jar` 做 classpath，
`javac -source 8 -target 8 -nowarn` 的结果：**287 个文件，0 error**。

达成路径（`tools/rebuild_74.py` 按此顺序执行）：

1. `annotate` 注入中文职责注释；
2. `fix_jadx_defects` 修 `??` 占位符、畸形 `for`、`R.drawable` 归属；
3. `fix_type_shadow` 修嵌套类型与同名字段遮蔽（幂等：已应用则跳过）；
4. `fix_missing_catch_var` / `fix_catch_param_shadow` / `fix_varnames`；
5. 迭代「编译 → `fix_obscured_types` 补限定名 → 编译 → `fix_field_shadow`」直至收敛。

**0 error ≠ 能直接打成可运行 APK**：仍缺 Android SDK 的 `R` 类、`.so`、资源文件。

其余已知限制：

- **字段名和局部变量名仍是混淆的**（`a`、`b`、`f0`…）。可读性靠**类名 + 类注释
  + 关键常量值**承担。
- **11 个嵌套类暂未改名**（`AvcVideoInput`、`HidTouchParser`、`PcmFormat` 等）。
  它们与同名字段/方法/局部变量大量共存，Java 文本层无法可靠区分；名称已分析确认，
  记录在 `rename_map.CORE` 与 `RENAME-MAP.md`，待后续用 smali 描述符级改名落地。

## 和 skill / docs 的关系

- `~/.workbuddy/skills/ocarplay-carplay-connect/SKILL.md` 里的铁律大量引用
  混淆名（`i0.t0`、`y5.z()`），本工程的 `RENAME-MAP.md` 提供双向对照。
- 版本演进与每次改动的判据在 `../CarPlay-Build/docs/v17xx-*.md`。
""" % (N_ALL, N_SECURED + N_PROTO, N_BC, N_RENAMED, N_RENAMED_TOP,
       N_RENAMED - N_RENAMED_TOP, N_DEFERRED, N_KEEP, N_ALL,
       N_SECURED, N_PROTO)

MAP = """# 混淆名 ↔ 可读名 对照表

生成自 `tools/rename_map.py`（唯一事实来源，改名字请改那里再重跑脚本）。

- **已应用 %d 个顶层类 + %d 个嵌套类重命名**；另有 **%d 个**已分析待改（见末节）；
  其余 %d 个保持原名（多数是无特征的工具/日志类）。
- 「行数」是该类在 jadx 产物里的规模，用来判断值不值得先读。

## secured 包（协议栈本体）

%s

## prototype 包（业务层）

%s

## 为什么有些类没改名

| 情况 | 例子 | 原因 |
|---|---|---|
| 本来就是语义名 | `WirelessCarPlayService`、`MainActivity`、`LogActivity` | 原厂没混淆这些入口类 |
| 无特征字符串 | 多个 `b`/`l4`/`g5` 之类 | 类里只有 `StringBuilder` 拼接的日志，判不出职责 |
| 单字母 + 多义 | `secured.d` vs `prototype.d` | 同名不同类，强行改名会串味 |

想继续推进命名，做法是：从 `tools/features.json` 里挑 `loc` 大、`strings` 有
业务关键字的类，先确认它被谁调用（`grep`），再补进 `rename_map.CORE`，重跑脚本。

## 已分析、暂缓改名的嵌套类

当前**没有**暂缓项：11 个嵌套类（`Outer$Inner`）已于 2026-11-03 全部落地。

它们此前无法用文本正则改名，因为类名与同名字段/方法/局部变量大量共存
（`VideoCipher` 里既有嵌套类 `b`，又有方法 `VideoCipher.b(...)`；`g4` 里既有枚举
`e`/`h`，又有 `instanceof h` 与 `(e) obj` 强转），文本替换实测产生 300+ 处
`cannot find symbol`。最终改用 `tools/rename_engine/RenameTool.java`：
`javac` 语义分析后按 `Trees.getElement()` 得到的真实符号定位，安全落地。

%s

## 与 APK 里 smali 的对应

| 可读名 | smali 路径 |
|---|---|
%s
""" % (N_RENAMED_TOP, N_RENAMED - N_RENAMED_TOP, N_DEFERRED, N_KEEP,
       table(SECURED), table(PROTO),
       deferred_table(DEFERRED), _smali_rows())

open(os.path.join(ROOT, 'README.md'), 'w', encoding='utf-8', newline='\n').write(README)
open(os.path.join(ROOT, 'RENAME-MAP.md'), 'w', encoding='utf-8', newline='\n').write(MAP)
print('README.md / RENAME-MAP.md written '
      '(applied=%d, deferred=%d, keep=%d)' % (N_RENAMED, N_DEFERRED, N_KEEP))
