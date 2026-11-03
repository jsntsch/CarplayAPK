# -*- coding: utf-8 -*-
"""消除 catch 参数名与方法级 `th` 变量的冲突。

背景
----
`fix_missing_catch_var.py` 给方法补了 `Throwable th = null;` 之后，同一方法里
jadx 生成的 `catch (Throwable th)` 参数就与外层局部变量重名（JLS 14.20：catch
参数的作用域是整块，且不能与外层局部变量同名），javac 报
`variable th is already defined`。

jadx 之所以会这么写，是因为在这种情况下 catch 参数与它自己的转发变量是同一个
槽位，反编译结果就是一段自赋值：

    } catch (Throwable th) {
        th = th;              // 无意义
        datagramSocket = null;
        ...
        throw th;             // 抛的是 catch 参数
    }

修法
----
把**该方法内**所有 `catch (Throwable th)` 的参数改名为 `thCaught`，块内的
`th` 引用一起改（catch 参数在块内遮蔽外层，所以块内每一个 `th` 都指它，
改名后语义完全一致），再删掉自赋值行 `thCaught = thCaught;`。

只处理「方法体内确实有方法级 `Throwable th` 声明」的方法，其余保持原样。

用法
----
    python tools/fix_catch_param_shadow.py [--dry-run]
"""
import os
import re
import sys

SRC = os.environ.get("CARPLAY_SRC",
    os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                   "src", "main", "java"))
OLD = "th"
NEW = "thCaught"
CATCH_RE = re.compile(r"catch\s*\(\s*Throwable\s+" + OLD + r"\s*\)\s*\{")
DECL_RE = re.compile(r"^\s*Throwable\s+" + OLD + r"\s*(?:=\s*null\s*)?;\s*$")
METHOD_START_RE = re.compile(
    r"^\s*(?:@\w+\s+)*(?:public|private|protected|static|final|synchronized|abstract|native|default|strictfp|\w).*\)"
    r"(?:\s+throws\s+[\w.,\s]+)?\s*\{\s*$"
)


def strip_literals(line):
    s = re.sub(r'"(\\.|[^"\\])*"', '""', line)
    s = re.sub(r"'(\\.|[^'\\])*'", "''", s)
    return s


def brace_blocks(lines):
    """返回 [(start, end, is_method)]，行号 0-based 闭区间。"""
    stack = []
    blocks = []
    in_comment = False
    for i, line in enumerate(lines):
        s = strip_literals(line)
        if in_comment:
            if "*/" in s:
                in_comment = False
                s = s.split("*/", 1)[1]
            else:
                continue
        while "/*" in s:
            head, rest = s.split("/*", 1)
            if "*/" in rest:
                s = head + rest.split("*/", 1)[1]
            else:
                s = head
                in_comment = True
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


def rename_in_range(lines, start, end):
    """把 [start, end] 行内的独立标识符 th 换成 thCaught。返回改动数。"""
    pat = re.compile(r"(?<![\w.$])" + OLD + r"(?![\w])")
    n = 0
    for i in range(start, end + 1):
        new = pat.sub(NEW, lines[i])
        if new != lines[i]:
            n += 1
            lines[i] = new
    return n


def fix_file(path, dry=False):
    lines = open(path, encoding="utf-8", errors="replace").read().split("\n")
    methods = [b for b in brace_blocks(lines) if b[2]]
    # 哪些方法体内有方法级 Throwable th 声明
    targets = []
    for start, end, _ in methods:
        body = lines[start + 1:end]
        if any(DECL_RE.match(l) for l in body):
            catches = [i for i in range(start, end + 1) if CATCH_RE.search(lines[i])]
            if catches:
                targets.append((start, end, catches))
    if not targets:
        return 0

    changed = 0
    # 从后往前改，避免行号位移
    for start, end, catches in sorted(targets, key=lambda t: -t[0]):
        # 每个 catch 块单独定位范围
        blocks = brace_blocks(lines)
        for cl in sorted(catches, reverse=True):
            blk = None
            for bs, be, _ in blocks:
                if bs == cl:
                    blk = (bs, be)
                    break
            if blk is None:
                continue
            bs, be = blk
            n = rename_in_range(lines, cl, be)
            changed += n
            # 删掉自赋值
            for i in range(be, bs - 1, -1):
                if re.match(r"^\s*" + NEW + r"\s*=\s*" + NEW + r"\s*;\s*$", lines[i]):
                    if dry:
                        print(f"  {os.path.basename(path)}:{i + 1}  - 自赋值")
                    del lines[i]
                    changed += 1
    if changed and not dry:
        open(path, "w", encoding="utf-8", newline="\n").write("\n".join(lines))
    return changed


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
    print(f"catch params renamed = {total}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
