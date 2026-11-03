# -*- coding: utf-8 -*-
"""改掉「与同名字段冲突」的嵌套类型名。

为什么要改类型名而不是字段名
----------------------------
R8 把字段名和嵌套类型名压成同一个字母，例如 `IccoaBridge` 里同时有
`private long d;` 和 `enum d { UNCONFIGURED... }`。此时表达式 `d.UNCONFIGURED`
按 JLS 6.5.2 先按变量解释，javac 报 `long cannot be dereferenced`。

**没法用限定名绕过**（实测）：`Outer.d.A`、`IccoaBridge.d.A` 一样被字段截胡。
只有两个出路——改字段名，或改类型名。这里选改类型名，因为字段名是
`iget`/`iput` 在 smali 里的标识，保留它才能和 APK 逐字段对照；嵌套类型名
只影响 `$Inner` 后缀，改了对阅读影响小、对语义零影响。

替换范围（一个都不能漏，漏一个就留一条编译错误）
------------------------------------------------
1. 类型声明      `enum d {`        -> `enum ConfigState {`
2. 成员访问      `d.UNCONFIGURED`  -> `ConfigState.UNCONFIGURED`
3. 类型位置      `d dVar = ...`    -> `ConfigState dVar = ...`
4. 构造          `new d(`          -> `new ConfigState(`
5. 继承/实现     `implements d`    -> `implements ConfigState`
6. 跨类限定      `IccoaBridge.d`   -> `IccoaBridge.ConfigState`

用法
----
    python tools/fix_type_shadow.py [--dry-run]
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.environ.get("CARPLAY_SRC", os.path.join(ROOT, "src", "main", "java"))

# (文件相对路径, 旧名, 新名, 成员名列表或 None, 所属外层类名或 None)
JOBS = [
    # ---- IccoaBridge：三个嵌套枚举与三个 long 字段同名 ----
    ("io/carplay/app/secured/IccoaBridge.java", "c", "TimeBase",
     ["UNKNOWN", "MICROSECONDS"], None),
    ("io/carplay/app/secured/IccoaBridge.java", "d", "ConfigState",
     ["UNCONFIGURED", "CONFIGURING", "CONFIGURED", "INVALID"], None),
    ("io/carplay/app/secured/IccoaBridge.java", "e", "KeyState",
     ["UNKNOWN", "KEY", "DELTA"], None),
    # ---- 其余文件：嵌套类型与同名字段冲突 ----
    ("io/carplay/app/secured/b.java", "a", "VerifyResult",
     ["UNKNOWN", "PASSED", "FAILED", "NOT_APPLICABLE"], "b"),
    # x 已在 rename_map.CORE 中改名为 HidCapabilities（含嵌套 g->HidTouchType）
    ("io/carplay/app/secured/HidCapabilities.java", "g", "HidTouchType",
     ["UNKNOWN", "TOUCH_SCREEN", "TOUCHPAD", "KNOB"], "HidCapabilities"),
    # f5 已在 rename_map.CORE 中改名为 PcmAudioOutput（含嵌套 c->PacketType）
    ("io/carplay/app/secured/PcmAudioOutput.java", "c", "PacketType",
     ["INIT", "DATA", "STOP", "PAUSE", "RESUME", "SEEK"], "PcmAudioOutput"),
    ("io/carplay/app/prototype/CarLifeBridge.java", "c", "ReviewState",
     ["REVIEWED", "CANDIDATE", "BLOCKED"], "CarLifeBridge"),
    ("io/carplay/app/secured/i6.java", "a", "TouchAction",
     ["DOWN", "MOVE", "UP"], "i6"),
    ("io/carplay/app/secured/MfiCertificateVerifier.java", "b", "InputFlags",
     ["NO_PROTOCOL_INPUT"], "MfiCertificateVerifier"),
    ("io/carplay/app/secured/g4.java", "b", "Endian",
     ["LITTLE_ENDIAN", "BIG_ENDIAN", "UNKNOWN"], "g4"),
    ("io/carplay/app/secured/l4.java", "a", "SwitchMap", ["a"], "l4"),
    # v1 已在 rename_map.CORE 中改名为 EncryptProofNeg（含嵌套 a->SwitchMap）
    ("io/carplay/app/secured/EncryptProofNeg.java", "a", "SwitchMap", ["a"], "EncryptProofNeg"),
    # ---- 说明：31 新增里的「嵌套类」条目（j1$b/j1$a/j1$c、z0$e、g4$e/g4$h、
    # i0$b、t4$a、y2$f、l1$d、x0$a、u1 之外的内层类）暂时 **不在此处改名**。
    # 原因：这些内层类与「同名的字段/方法/局部变量」大量共存，Java 文本层无法
    # 可靠区分（例如 VideoCipher 里既有嵌套类 b，又有方法 VideoCipher.b(...)；
    # g4 里既有枚举 e/h，又有 `instanceof h`/`(e) obj` 强转）。实测直接改会
    # 产生 300+ 处 "cannot find symbol / incompatible types"，远不如保持混淆名。
    # 这些名称仍保留在 rename_map.CORE 里作为分析结论，待后续用 smali 描述符级
    # 改名（dex_rename.py 路线）安全落地。
]

IDENT = r"[A-Za-z_$][\w$]*"


def all_java():
    for dp, _dn, fns in os.walk(SRC):
        for fn in fns:
            if fn.endswith(".java"):
                yield os.path.join(dp, fn)


def apply(rel, old, new, members, outer, text):
    """在 text 上做六类替换，返回 (新文本, 改动数)。"""
    n = 0

    # 1. 类型声明
    text, k = re.subn(r"\b(class|interface|enum)(\s+)" + old + r"\b", r"\1\2" + new, text)
    n += k

    # 2. 成员访问（限定成员名，避免误伤同名变量）
    if members:
        mem = "|".join(re.escape(m) for m in members)
        text, k = re.subn(r"(?<![\w.$])" + old + r"\.(" + mem + r")\b", new + r".\1", text)
        n += k

    # 3. 类型位置：`d dVar` / `d dVar =` / `d dVar;` / `d dVar,`
    text, k = re.subn(
        r"(?<![\w.$])" + old + r"(\s+)(" + IDENT + r")(\s*[=;,)]|\s*\{|\s*$)",
        new + r"\1\2\3", text, flags=re.M)
    n += k

    # 3b. 方法返回类型：`a e() {` —— 第三组是 `(`，上面那条覆盖不到
    text, k = re.subn(
        r"(?<![\w.$])" + old + r"(\s+)(" + IDENT + r")(\s*\()",
        new + r"\1\2\3", text)
    n += k

    # 4. 构造调用
    text, k = re.subn(r"\bnew\s+" + old + r"(\s*)\(([^)]*)\)", r"new " + new + r"\1(\2)", text)
    n += k

    # 5. 继承 / 实现
    text, k = re.subn(r"\b(implements|extends)(\s+)" + old + r"\b", r"\1\2" + new, text)
    n += k

    # 6. 跨类限定 `Outer.d`
    if outer:
        text, k = re.subn(r"\b" + outer + r"\." + old + r"\b", outer + "." + new, text)
        n += k

    # 7. 构造器 old( -> new(：只在「被重命名的嵌套类 new 的体内」做，避免误改
    #    外层或其它同名方法/构造器（如 DisplaySession 里还有别的 a() 方法）。
    text, k = _rename_ctor_in_body(text, new, old)
    n += k

    return text, n


def _rename_ctor_in_body(text, new, old):
    """在被重命名为 new 的嵌套类体内，把构造器 old( 改成 new(。返回 (text, count)。"""
    decls = ["class", "interface", "enum"]
    # 找到 new 的声明起点
    idx = -1
    for kw in decls:
        m = re.search(r"\b" + kw + r"\s+" + re.escape(new) + r"\b", text)
        if m:
            idx = m.start()
            break
    if idx < 0:
        return text, 0
    # 从声明处开始 brace 匹配，定位类体
    depth = 0
    started = False
    end = -1
    for i in range(idx, len(text)):
        c = text[i]
        if c == "{":
            depth += 1
            started = True
        elif c == "}":
            depth -= 1
            if started and depth == 0:
                end = i
                break
    if end < 0:
        return text, 0
    body = text[idx:end]
    # 体内：old( → new(（构造器，无返回类型；同体方法恰好同名极罕见，可接受）
    new_body, k = re.subn(r"(?<![\w.$])" + re.escape(old) + r"(?=\s*\()", new, body)
    if k:
        text = text[:idx] + new_body + text[end:]
    return text, k


def main():
    dry = "--dry-run" in sys.argv
    changed = {}
    for rel, old, new, members, outer in JOBS:
        target = os.path.join(SRC, rel)
        if not os.path.exists(target):
            print(f"{rel:46s} {old:3s} -> {new:14s} SKIP(文件不存在)")
            continue
        text = open(target, encoding="utf-8", errors="replace").read()
        # 幂等保护：若声明已经是 new 且已没有 old，说明这一条早已应用过
        # （39 名树本身就带着这批嵌套改名）。此时绝不能再跑一遍——rule 6 的
        # 跨文件扫描 `\bOuter.old\b` 会把「字段 Outer 上调方法 old(...)」误改
        # 成 `Outer.new(...)`（实测 4 处 this.b.a(...) 被改成 this.b.VerifyResult）。
        has_new = re.search(r"\b(class|interface|enum)\s+" + re.escape(new) + r"\b", text)
        has_old = re.search(r"\b(class|interface|enum)\s+" + re.escape(old) + r"\b", text)
        if has_new and not has_old:
            print(f"{rel:46s} {old:3s} -> {new:14s} skip(已应用)")
            continue
        new_text, n = apply(rel, old, new, members, outer, text)
        # 本文件还会被别的文件以 `Outer.old` 引用，需要全项目扫一遍
        if outer:
            for path in all_java():
                if os.path.abspath(path) == os.path.abspath(target):
                    continue
                other = open(path, encoding="utf-8", errors="replace").read()
                patched, k = re.subn(r"\b" + outer + r"\." + old + r"\b", outer + "." + new, other)
                if k:
                    n += k
                    if dry:
                        print(f"  {os.path.relpath(path, SRC)}: {outer}.{old} -> {outer}.{new} x{k}")
                    else:
                        open(path, "w", encoding="utf-8", newline="\n").write(patched)
        if n:
            changed[rel] = n
            if not dry:
                open(target, "w", encoding="utf-8", newline="\n").write(new_text)
        print(f"{rel:46s} {old:3s} -> {new:14s} hits={n}")

    print(f"\n总计改动 {sum(changed.values())} 处，涉及 {len(changed)} 个文件")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
