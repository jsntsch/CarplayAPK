# -*- coding: utf-8 -*-
"""改正「类已改名、构造器名还是旧名」的破损构造器。

为什么单独一个脚本
------------------
Java 构造函数没有返回类型，jadx 反编译出来是 `public b(int i) {...}` 这种
「类名即函数名」的写法。把类 b 改名为 Iap2PostResponseOwner 之后，javac 会把
`public b(...)` 解析成「缺返回类型的方法」，报
    invalid method declaration; return type required
RenameTool 里靠 MethodTree.getReturnType()==null 判断构造器在这类树上失效
（javac 把 b 当成了返回类型），所以用文本层按「类体范围 + 声明位」补一遍。

安全性
------
1. 只在目标类**自己的类体范围内**替换（brace 匹配，跳过字符串/注释）；
2. 只匹配「行首 + 纯修饰符 + 旧名 + ( 或 <」，`Object b(...)` 这种带返回类型的
   方法不会命中（修饰符后面紧跟的是类型名而不是旧名）；
3. 旧名只会出现在它自己的类里，跨类不可能重名。
"""
import os
import re

MODS = r"(?:(?:public|private|protected|static|final|abstract|synchronized|native|strictfp)\s+|/\*.*?\*/\s+)*"
DECL_TAIL = r"(?=\s*[<(])"


def _skip_string(text, i):
    """i 指向引号，返回字符串结束后的位置。"""
    q = text[i]
    i += 1
    n = len(text)
    while i < n:
        ch = text[i]
        if ch == "\\":
            i += 2
            continue
        if ch == q:
            return i + 1
        if ch == "\n":
            return i
        i += 1
    return i


def _find_open_brace(text, start):
    """从 start 起找类头的第一个 {（跳过注释/字符串）。"""
    i = start
    n = len(text)
    while i < n:
        ch = text[i]
        if ch == "/" and i + 1 < n and text[i + 1] == "/":
            j = text.find("\n", i)
            i = n if j < 0 else j + 1
            continue
        if ch == "/" and i + 1 < n and text[i + 1] == "*":
            j = text.find("*/", i + 2)
            i = n if j < 0 else j + 2
            continue
        if ch in "\"'":
            i = _skip_string(text, i)
            continue
        if ch == "{":
            return i
        i += 1
    return -1


def class_body_range(text, class_name):
    """返回 (body_start, body_end)，body_end 是右花括号之后的位置。"""
    m = re.search(r"\b(?:class|interface|enum|record)\s+" + re.escape(class_name) + r"\b", text)
    if not m:
        return -1, -1
    open_pos = _find_open_brace(text, m.end())
    if open_pos < 0:
        return -1, -1
    depth = 0
    i = open_pos
    n = len(text)
    while i < n:
        ch = text[i]
        if ch == "/" and i + 1 < n and text[i + 1] == "/":
            j = text.find("\n", i)
            i = n if j < 0 else j + 1
            continue
        if ch == "/" and i + 1 < n and text[i + 1] == "*":
            j = text.find("*/", i + 2)
            i = n if j < 0 else j + 2
            continue
        if ch in "\"'":
            i = _skip_string(text, i)
            continue
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                return open_pos, i + 1
        i += 1
    return open_pos, n


def _match_paren(text, open_idx):
    """从 '(' 起做括号匹配，返回对应的 ')' 位置。"""
    depth = 0
    i = open_idx
    n = len(text)
    while i < n:
        ch = text[i]
        if ch == "/" and i + 1 < n and text[i + 1] == "/":
            j = text.find("\n", i)
            i = n if j < 0 else j + 1
            continue
        if ch == "/" and i + 1 < n and text[i + 1] == "*":
            j = text.find("*/", i + 2)
            i = n if j < 0 else j + 2
            continue
        if ch in "\"'":
            i = _skip_string(text, i)
            continue
        if ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1


def _is_declaration(text, name_pos, name_len):
    """名字后面必须是「( ... ) 换行/空格 { 或 throws ... {」才算构造器声明。

    这是和「方法调用语句」的唯一可靠区分：`c(a, b);` 以分号收尾，
    构造器必定带 `{` 函数体。
    """
    i = name_pos + name_len
    while i < len(text) and text[i] in " \t":
        i += 1
    if i < len(text) and text[i] == "<":
        depth = 0
        while i < len(text):
            if text[i] == "<":
                depth += 1
            elif text[i] == ">":
                depth -= 1
                if depth == 0:
                    i += 1
                    break
            i += 1
        while i < len(text) and text[i] in " \t":
            i += 1
    if i >= len(text) or text[i] != "(":
        return False
    close = _match_paren(text, i)
    if close < 0:
        return False
    j = close + 1
    while j < len(text) and text[j] in " \t\r\n":
        j += 1
    if text.startswith("throws", j):
        k = text.find("{", j)
        semi = text.find(";", j)
        if k < 0:
            return False
        return not (0 <= semi < k)
    return j < len(text) and text[j] == "{"


def fix_file(path, outer_name, old_ctor, new_ctor):
    with open(path, encoding="utf-8") as f:
        text = f.read()
    bs, be = class_body_range(text, outer_name)
    if bs < 0:
        return 0
    body = text[bs:be]
    pat = re.compile(r"(?m)^([ \t]*" + MODS + ")" + re.escape(old_ctor) + DECL_TAIL)
    hits = []

    def _sub(m):
        name_pos = bs + m.start(0) + len(m.group(1))
        if not _is_declaration(text, name_pos, len(old_ctor)):
            return m.group(0)          # 方法调用语句，原样保留
        hits.append(name_pos)
        return m.group(1) + new_ctor

    new_body = pat.sub(_sub, body)
    if hits:
        with open(path, "w", encoding="utf-8") as f:
            f.write(text[:bs] + new_body + text[be:])
    return len(hits)


def fix_tree(src, plan):
    """plan: [(old_fq, new_name)]。顶层类与嵌套类都处理。返回 (总修改处数, 明细)。"""
    top_new = {o.rsplit(".", 1)[1]: n for o, n in plan if "$" not in o}
    total = 0
    detail = []
    for old, new in plan:
        pkg = old.rsplit(".", 1)[0]
        if "$" not in old:
            # 顶层类：类声明此刻已被引擎改成新名，文件名可能已改也可能还没改
            old_simple = old.rsplit(".", 1)[1]
            d = os.path.join(src, *pkg.split("."))
            fp = None
            for cand in (new + ".java", old_simple + ".java"):
                if os.path.exists(os.path.join(d, cand)):
                    fp = os.path.join(d, cand)
                    break
            if fp is None:
                continue
            n = fix_file(fp, new, old_simple, new)
            if n:
                detail.append((old_simple, old_simple, new, n))
                total += n
            continue
        outer_old, inner_old = old.rsplit(".", 1)[1].split("$")
        outer_now = top_new.get(outer_old, outer_old)
        fp = os.path.join(src, *pkg.split("."), outer_now + ".java")
        if not os.path.exists(fp):
            continue
        n = fix_file(fp, outer_now, inner_old, new)
        if n:
            detail.append((outer_now, inner_old, new, n))
            total += n
    return total, detail


if __name__ == "__main__":
    import argparse
    import sys

    HERE = os.path.dirname(os.path.abspath(__file__))
    PROJ = os.path.dirname(HERE)
    ap = argparse.ArgumentParser()
    ap.add_argument("--plan", required=True)
    ap.add_argument("--src", default=os.path.join(PROJ, "src", "main", "java"))
    a = ap.parse_args()
    entries = []
    for line in open(a.plan, encoding="utf-8"):
        line = line.rstrip("\n")
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        o, n = line.split("\t")[:2]
        entries.append((o.strip(), n.strip()))
    t, d = fix_tree(a.src, entries)
    print("构造器修正 %d 处" % t)
    for row in d:
        print("  %s: %s(...) -> %s(...) ×%d" % row)
