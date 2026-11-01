# -*- coding: utf-8 -*-
"""生成 README.md 与 RENAME-MAP.md（内容来自 rename_map + features.json）。"""
import json, os
import rename_map as RM

ROOT = r"C:/Users/SCH/WorkBuddy/2026-10-24-16-47-09/OcarPlay-Java-V1729"
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
   │  jadx 1.5.6 --no-res
   ▼
_jadx_v1729/sources          （287 个 .java，全混淆名）
   │  tools/build_project.py  ← 按「类型位置」白名单替换，不是全局 replace
   ▼
src/main/java/...            （本工程，39 个类重命名）
   │  tools/annotate.py       ← 给重命名类注入中文职责注释
   ▼
最终产物
```

所有脚本都在 `tools/` 下，可重跑：

```bash
python tools/build_project.py   # 重命名 + 生成源码树
python tools/annotate.py        # 注入类注释
python tools/fix_varnames.py    # 修正注释里的方法指代
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

## 已知限制

- **不能编译**。缺 Android SDK 的 `R` 类、缺 `.so`、缺资源；部分类是 jadx 从
  dex 直接还原的，本身就不完全合法（原始 jadx 输出有 20 处语法错误，
  本工程**同样 20 处**，集中在 `k4` / `t4` / `BinaryPlist`，是 jadx 的已知问题，
  不是重命名引入的）。
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
