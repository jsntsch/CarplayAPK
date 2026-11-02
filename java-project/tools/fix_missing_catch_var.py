# -*- coding: utf-8 -*-
"""补回 jadx 漏掉的 catch 转发变量声明。

缺陷形态
--------
jadx 把 try-finally 打散成多段 try/catch 时，会用一个共享变量把异常暂存到
finally 清理之后再抛：

    } catch (Throwable th2) {
        th = th2;              // th 从未声明
        datagramSocket = null;
        ...
        throw th;
    }

正常情况 jadx 会在方法开头生成 `Throwable th;`，但当同一方法里有多条
catch 共用这个槽位、且外层还有 try 时，声明会被丢掉，于是 javac 报
`cannot find symbol: variable th`。

修法
----
在**该方法的函数体开头**补一行 `Throwable th = null;`。只加声明，不改任何
已有语句，方法是局部的，不影响外部。

为什么不改成 `throw th2;`
-------------------------
有的 catch 块只赋值不抛出（清理后继续往下走），单个块内无法判断该不该替换，
补声明对所有形态都成立。

用法
----
    python tools/fix_missing_catch_var.py [--dry-run]
"""
import os
import re
import sys

SRC = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                   "src", "main", "java")
VAR = "th"
USE_RE = re.compile(r"(?<![\w.$])" + VAR + r"(?![\w])")
# 只认「对 th 赋值」这个确定形态：`th = th2;`。
# 别的用法（`throw th;`、catch 参数 `catch (Throwable th)`）在 catch 块里
# 本来就有作用域，全量扫描会把 200 多个合法方法误判成缺声明。
ASSIGN_RE = re.compile(r"^\s*" + VAR + r"\s*=\s*[\w.$]+\s*;\s*$")
# 只认「语句级声明」：`Throwable th;` / `Throwable th = ...`。
# 不能写成 \bThrowable\s+th\b —— `catch (Throwable th)` 也会命中，
# 那样每个有 catch 的方法都会被误判成「已声明」。
DECL_RE = re.compile(r"\bThrowable\s+" + VAR + r"\s*[;=]")
METHOD_START_RE = re.compile(
    r"^\s{4}(?:@\w+\s+)*(?:public|private|protected|static|final|synchronized|abstract|native|default|strictfp|\w).*\)"
    r"(?:\s+throws\s+[\w.,\s]+)?\s*\{\s*$"
)


def brace_blocks(lines):
    """返回 [(start, end, is_method)]，行号 0-based，闭区间。"""
    stack = []
    blocks = []
    in_block_comment = False
    for i, line in enumerate(lines):
        s = line
        # 去掉字符串字面量与行注释，避免花括号误计
        s = re.sub(r'"(\\.|[^"\\])*"', '""', s)
        s = re.sub(r"'(\\.|[^'\\])*'", "''", s)
        if in_block_comment:
            if "*/" in s:
                in_block_comment = False
                s = s.split("*/", 1)[1]
            else:
                continue
        while "/*" in s:
            head, rest = s.split("/*", 1)
            if "*/" in rest:
                s = head + rest.split("*/", 1)[1]
            else:
                s = head
                in_block_comment = True
                break
        if "//" in s:
            s = s.split("//", 1)[0]
        for ch in s:
            if ch == "{":
                stack.append(i)
            elif ch == "}" and stack:
                start = stack.pop()
                blocks.append((start, i, bool(METHOD_START_RE.match(lines[start]))))
    return blocks


def fix_file(path, dry=False):
    raw = open(path, encoding="utf-8", errors="replace").read()
    lines = raw.split("\n")
    uses = [i for i, l in enumerate(lines) if ASSIGN_RE.match(l)]
    if not uses:
        return 0
    blocks = brace_blocks(lines)
    methods = [b for b in blocks if b[2]]
    targets = {}
    for u in uses:
        best = None
        for start, end, _ in methods:
            if start <= u <= end:
                if best is None or (end - start) < (best[1] - best[0]):
                    best = (start, end)
        if best is None:
            continue
        # 方法体内已经声明过就不再补
        body = "\n".join(lines[best[0]:best[1] + 1])
        if DECL_RE.search(body):
            continue
        targets.setdefault(best[0], best[1])

    fixed = 0
    for start, _end in sorted(targets.items(), reverse=True):
        # 缩进跟着方法体第一行
        indent = "        "
        for j in range(start + 1, min(start + 4, len(lines))):
            if lines[j].strip():
                indent = re.match(r"\s*", lines[j]).group(0)
                break
        if dry:
            print(f"  {os.path.basename(path)}:{start + 1}  + Throwable {VAR} = null;")
        else:
            lines.insert(start + 1, f"{indent}Throwable {VAR} = null;")
        fixed += 1
    if fixed and not dry:
        open(path, "w", encoding="utf-8", newline="\n").write("\n".join(lines))
    return fixed


def main():
    dry = "--dry-run" in sys.argv
    total = 0
    for dp, _dn, fns in os.walk(SRC):
        for fn in sorted(fns):
            if not fn.endswith(".java"):
                continue
            n = fix_file(os.path.join(dp, fn), dry)
            if n:
                print(f"{fn}: {n}")
                total += n
    print(f"declarations added = {total}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
