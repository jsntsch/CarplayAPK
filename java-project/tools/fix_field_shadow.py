# -*- coding: utf-8 -*-
"""修「字段名遮蔽类型名」造成的 `X cannot be dereferenced`。

缺陷成因
--------
R8 把字段名和类型名都压成一个字母，同一个 `c` 可能同时是：
  - 某个类的字段（`private final long c;`）
  - 同包的顶层类（`prototype.c`）
  - 外层类的嵌套类型（`CarLifeBridge.c` 这个枚举）
Java 的遮蔽规则（JLS 6.5.2）让 `c.BLOCKED` 里的 `c` 先按变量解释，javac
于是报 `long cannot be dereferenced`。

修法
----
给 `c` 补上足以绕开字段的限定：
  - 嵌套类型 → `CarLifeBridge.c.BLOCKED`
  - 同包顶层类 → `io.carplay.app.prototype.c.BLOCKED`

判据只有一条：**候选类型里必须恰好有一个真的声明了被访问的成员**。
0 个或 2 个以上一律不猜，打印出来交人工。

候选来源
--------
1. 本文件里声明的所有类型（含嵌套，按括号层级算出限定路径）
2. 同包的顶层类
3. 文件 import 进来的类

用法
----
    python tools/fix_field_shadow.py <compile-log> [--dry-run]
"""
import os
import re
import sys
from collections import defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.environ.get("CARPLAY_SRC", os.path.join(ROOT, "src", "main", "java"))

ERR_RE = re.compile(r"^(.*?):(\d+): error: .*cannot be dereferenced$")
MEM_RE = re.compile(r"(?<![\w$.])([A-Za-z_$][\w$]*)\s*\.\s*([A-Za-z_$][\w$]*)")
TYPE_DECL_RE = re.compile(r"\b(class|interface|enum)\s+([A-Za-z_$][\w$]*)")
IMPORT_RE = re.compile(r"^\s*import\s+([\w.]+)\s*;")

_pkg_index = None
_cache = {}


def norm(p):
    return p.replace("\\", "/")


def build_pkg_index():
    global _pkg_index
    idx = defaultdict(list)
    for dp, _dn, fns in os.walk(SRC):
        key = norm(dp)
        for fn in fns:
            if fn.endswith(".java"):
                idx[fn[:-5]].append(key)
    _pkg_index = idx
    return idx


def read(path):
    if path not in _cache:
        try:
            _cache[path] = open(path, encoding="utf-8", errors="replace").read()
        except OSError:
            _cache[path] = ""
    return _cache[path]


def type_tree(src):
    """返回 {限定路径: 该类型体内的源码}，含嵌套类型。

    用花括号栈跟踪：每个 `{` 记下它是由哪个类型声明打开的。
    """
    lines = src.split("\n")
    stack = []            # 每层: 类型限定名 or None
    result = {}
    body_start = {}
    for i, line in enumerate(lines):
        stripped = re.sub(r'"(\\.|[^"\\])*"', '""', line)
        stripped = re.sub(r"//.*$", "", stripped)
        m = TYPE_DECL_RE.search(stripped)
        opened = None
        if m:
            simple = m.group(2)
            # 找最内层已知类型当父
            parent = None
            for name in reversed(stack):
                if name:
                    parent = name
                    break
            opened = (parent + "." + simple) if parent else simple
        for ch in stripped:
            if ch == "{":
                stack.append(opened if opened else None)
                if opened:
                    body_start[opened] = i
                opened = None
            elif ch == "}":
                if stack:
                    name = stack.pop()
                    if name and name not in result:
                        s = body_start.get(name, i)
                        result[name] = "\n".join(lines[s:i + 1])
        # 声明后同一行还有 `{` 的情况已在上面的字符循环里处理
    return result


def declares_member(body, member):
    if re.search(r"\b" + re.escape(member) + r"\s*\(", body):
        return True
    if re.search(r"\b" + re.escape(member) + r"\s*(?:=|;|,)", body):
        return True
    return False


def candidates_for(java_path, simple):
    """返回 [(引用表达式, 定义体)]。

    引用表达式一律是**从包根开始的完全限定名**。原因：写成 `Outer.d` 也不行 ——
    `Outer` 里若有一个实例字段也叫 `d`，JLS 6.5.2 仍然优先当字段解释，javac 会
    改报 `non-static variable d cannot be referenced from a static context`。
    只有从包开始写才不可能被成员查找截胡。
    """
    dirpath = norm(os.path.dirname(java_path))
    pkg = os.path.relpath(dirpath, SRC).replace(os.sep, ".").replace("/", ".")
    src = read(java_path)
    out = []

    # 1. 本文件的类型（含嵌套）
    for path, body in type_tree(src).items():
        if path.split(".")[-1] == simple:
            out.append((pkg + "." + path, body))

    # 2. 同包的顶层类
    for p in _pkg_index.get(simple, []):
        if p == dirpath:
            body = read(os.path.join(p, simple + ".java"))
            if body and path_is_top(src, simple):
                out.append((pkg + "." + simple, body))

    # 3. import 进来的（本身已经是完全限定名）
    for line in src.split("\n"):
        m = IMPORT_RE.match(line)
        if not m:
            continue
        fq = m.group(1)
        if fq.split(".")[-1] == simple:
            rel = fq.replace(".", "/") + ".java"
            body = read(os.path.join(SRC, rel))
            if body:
                out.append((fq, body))
    return out


def path_is_top(src, simple):
    """该简单名是否作为本文件的顶层类型出现（避免和嵌套同名重复）。"""
    for path, _b in type_tree(src).items():
        if path == simple:
            return True
    return False


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return 2
    log = sys.argv[1]
    dry = "--dry-run" in sys.argv
    build_pkg_index()

    lines = open(log, encoding="utf-8", errors="replace").read().split("\n")
    plans = []
    unresolved = []
    i = 0
    while i < len(lines):
        m = ERR_RE.match(lines[i])
        if not m:
            i += 1
            continue
        if i + 2 >= len(lines):
            break
        lineno = int(m.group(2))
        code = lines[i + 1]
        caret = lines[i + 2]
        col = caret.index("^") if "^" in caret else 0
        rel = m.group(1).replace("src\\main\\java\\", "").replace("\\", "/")
        java_path = os.path.join(SRC, rel)
        if not os.path.exists(java_path):
            unresolved.append((rel, lineno, "源文件缺失"))
            i += 3
            continue
        best = None
        for mm in MEM_RE.finditer(code):
            name, member = mm.group(1), mm.group(2)
            if name in ("this", "super"):
                continue
            hits = [(expr, body) for expr, body in candidates_for(java_path, name)
                    if declares_member(body, member)]
            if len(hits) != 1:
                continue
            dist = abs(mm.start(1) - col)
            if best is None or dist < best[0]:
                best = (dist, mm.start(1), mm.end(2), name, member, hits[0][0])
        if best is None:
            unresolved.append((rel, lineno, "候选不唯一或成员不存在"))
            i += 3
            continue
        _d, s, e, name, member, expr = best
        plans.append((java_path, lineno, s, e, name, member, expr))
        i += 3

    print(f"可修 = {len(plans)}   待人工 = {len(unresolved)}")
    for u in unresolved[:20]:
        print("  待定:", u)

    from collections import Counter
    if plans:
        print("\n替换目标：")
        for k, v in Counter(p[6] for p in plans).most_common():
            print(f"  {v:3d}  {k}")

    changed = 0
    by_file = defaultdict(list)
    for java_path, lineno, s, e, name, member, expr in plans:
        by_file[java_path].append((lineno, s, e, name, expr))
    for java_path, spots in by_file.items():
        content = open(java_path, encoding="utf-8", errors="replace").read().split("\n")
        for lineno, s, e, name, expr in sorted(set(spots), key=lambda x: (-x[0], -x[1])):
            if lineno - 1 >= len(content):
                continue
            line = content[lineno - 1]
            # 写入前校验：该位置确实是那个简单名，否则说明行号/列号已经漂移
            if line[s:s + len(name)] != name:
                print(f"  跳过（位置不符）{os.path.basename(java_path)}:{lineno}")
                continue
            if dry:
                print(f"  {os.path.basename(java_path)}:{lineno}  {name}.{member} -> {expr}.{member}")
            content[lineno - 1] = line[:s] + expr + line[s + len(name):]
            changed += 1
        if not dry:
            open(java_path, "w", encoding="utf-8", newline="\n").write("\n".join(content))
    print(f"\nrewritten = {changed}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
