# -*- coding: utf-8 -*-
"""修「类型名被同名字段遮蔽」这一类 jadx 编译错误。

现象
----
    public enum c { BLOCKED, ... }          // 外层类的嵌套枚举
    class d { private final long c;         // 同名字段
              boolean g() { return this.a != c.BLOCKED; } }   // 报错

javac 按 JLS 6.5.2 的「遮蔽」规则：`c.BLOCKED` 里的 `c` 先当变量解释，
于是报 `long cannot be dereferenced`。R8 混淆后字段和类都叫单字母，这种
冲突在 jadx 产物里成片出现，与改名无关（原始 jadx 产物同样有）。

修法
----
只改**已经报错的那一处**：把被遮蔽的类型名补成限定名
（嵌套类补 `外层类.`，同包顶层类补全限定名）。只动报错位置，
不会把本来能编译的代码改坏。

已知风险（务必先读）
------------------
补限定名时要在「本文件的嵌套类型」和「同包顶层类型」之间二选一。
例如 `IccoaBridge` 里的 `d.UNCONFIGURED`，`d` 既可能是本文件里声明的
嵌套枚举，也可能是同包的 `secured.d`。本脚本按 javac 的解析顺序优先选
嵌套类型，**可能选错**：错误数会下降，但类型绑错了，语义已变。

因此它**没有接进 build_project.py 链路**，只能先用 `--dry-run` 看命中，
再逐条对着 smali 复核。

用法
----
    set CARPLAY_JAVAC=...\\javac.exe
    set CARPLAY_CP=android.jar;bcprov.jar
    set CARPLAY_OUT=...\\out\\classes
    python tools/fix_obscured_types.py [轮数]
"""
import collections
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
SRC = os.path.join(PROJ, "src", "main", "java")

ERR_RE = re.compile(r"^(.+?):(\d+): error: (.*)$")
DECL_RE = re.compile(r"\b(?:class|interface|enum)\s+(\w+)")


def run_javac():
    javac = os.environ["CARPLAY_JAVAC"]
    cp = os.environ["CARPLAY_CP"]
    out = os.environ["CARPLAY_OUT"]
    sources = os.path.join(os.path.dirname(out), "..", "work", "sources.txt")
    sources = os.environ.get("CARPLAY_SOURCES", sources)
    cmd = [javac, "-J-Duser.language=en", "-source", "8", "-target", "8",
           "-proc:none", "-nowarn", "-Xmaxerrs", "10000", "-encoding", "UTF-8",
           "-d", out, "-cp", cp, "@" + sources]
    r = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    return (r.stdout or "") + (r.stderr or "")


def parse_errors(log):
    """[(file, line, col, message)]，col 为 1-based。"""
    lines = log.split("\n")
    out = []
    for i, ln in enumerate(lines):
        m = ERR_RE.match(ln)
        if not m:
            continue
        col = None
        for j in range(i + 1, min(i + 3, len(lines))):
            if "^" in lines[j]:
                col = lines[j].index("^") + 1
                break
        if col:
            out.append((m.group(1), int(m.group(2)), col, m.group(3)))
    return out


def wanted(msg):
    return ("cannot be dereferenced" in msg
            or ("non-static variable" in msg and "static context" in msg)
            or "cannot find symbol" in msg)


def resolve(name, path, decls):
    top = os.path.basename(path)[:-5]
    if name == top:
        return None
    if name in decls:                      # 本文件里的嵌套类型
        return top + "." + name
    if os.path.exists(os.path.join(os.path.dirname(path), name + ".java")):
        with open(path, encoding="utf-8", errors="replace") as f:
            head = f.read(4000)
        m = re.search(r"^package\s+([\w.]+);", head, re.M)
        return (m.group(1) + "." + name) if m else name
    return None


def apply_fixes(errors, dry=False):
    by_file = collections.defaultdict(set)
    for f, l, c, msg in errors:
        if wanted(msg):
            by_file[f].add((l, c))
    changed = 0
    for f, spots in by_file.items():
        rel = f.replace("\\", "/")
        path = os.path.join(SRC, rel)
        if not os.path.exists(path):
            path = os.path.join(PROJ, rel)
        if not os.path.exists(path):
            continue
        with open(path, encoding="utf-8", errors="replace") as fh:
            text = fh.read()
        decls = set(DECL_RE.findall(text))
        lines = text.split("\n")
        hits = []
        for l, c in spots:
            if l - 1 >= len(lines):
                continue
            line = lines[l - 1]
            if c - 1 >= len(line):
                continue
            # 候选一：缺符号的报错点落在方法名上（`d0.c(` 的 ^ 在 `c` 下），
            # 真正要限定的是点号左边的接收者。
            # 候选二：`cannot be dereferenced` 的 ^ 直接落在被遮蔽的类型名上。
            cands = []
            m_before = re.search(r"([A-Za-z_$][\w$]*)\s*\.\s*$", line[:c])
            if m_before:
                cands.append((m_before.start(1), m_before.group(1)))
            m_at = re.match(r"[A-Za-z_$][\w$]*", line[c - 1:])
            if m_at:
                cands.append((c - 1, m_at.group(0)))
            for pos, name in cands:
                # 已经被限定过（前面就是点）就不要再加一层，
                # 否则多轮迭代会产出 `X.X.d` 这种加倍前缀。
                if pos > 0 and line[pos - 1] == ".":
                    continue
                q = resolve(name, path, decls)
                if q:
                    hits.append((l - 1, pos, name, q))
                    break
        for l, c, name, q in sorted(set(hits), key=lambda e: (-e[0], -e[1])):
            line = lines[l]
            if line[c:c + len(name)] != name:
                continue
            if dry:
                print(f"  {f}:{l + 1}  {name} -> {q}")
            lines[l] = line[:c] + q + line[c + len(name):]
            changed += 1
        if not dry:
            with open(path, "w", encoding="utf-8", newline="\n") as fh:
                fh.write("\n".join(lines))
    return changed


def main():
    dry = "--dry-run" in sys.argv
    nums = [a for a in sys.argv[1:] if a.isdigit()]
    rounds = int(nums[0]) if nums else 3
    for r in range(1, rounds + 1):
        errs = parse_errors(run_javac())
        n = apply_fixes(errs, dry)
        print(f"round {r}: errors={len(errs)} qualified={n}")
        if n == 0 or dry:
            break
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
