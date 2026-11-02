# -*- coding: utf-8 -*-
"""验证「改名是可逆的纯替换」—— 对齐原始 smali 与改名后 smali。

做法：把原始 smali 树按同一套映射**正向**改名（内容和路径都改），
再与 `dex_rename.py` 实际产出的树逐字节比对。全部相等 ⇒ 改名只动了
类型描述符，没有动任何指令、常量、注解、调试信息。

    python tools/verify_rename.py <原始smali树> <改名后smali树>

输出：文件数、指令数、方法数、字段数、字符串常量数、不一致文件清单。
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import dex_rename


def new_rel(rel, pairs):
    stem = rel[:-6] if rel.endswith(".smali") else rel
    for old, new in pairs:
        if stem == old or stem.startswith(old + "$"):
            return new + stem[len(old):] + ".smali"
    return rel


def apply_pairs(text, pairs):
    out = text
    for old, new in pairs:
        out = re.sub("L" + old + r"(?=[;$])", "L" + new, out)
    return out


def count(text, pattern):
    return len(re.findall(pattern, text, re.M))


def main():
    if len(sys.argv) < 3:
        print("usage: verify_rename.py <orig_smali_dir> <renamed_smali_dir>")
        return 2
    orig_root, ren_root = sys.argv[1], sys.argv[2]
    pairs = dex_rename.build_pairs()

    files = matched = 0
    mismatched = []
    missing = []
    stats = {"insn": 0, "method": 0, "field": 0, "string": 0, "class": 0}
    for dirpath, _d, fns in os.walk(orig_root):
        for fn in fns:
            if not fn.endswith(".smali"):
                continue
            files += 1
            p = os.path.join(dirpath, fn)
            rel = os.path.relpath(p, orig_root).replace(os.sep, "/")
            with open(p, encoding="utf-8", errors="surrogateescape") as f:
                src = f.read()
            expect = apply_pairs(src, pairs)
            target = os.path.join(ren_root, new_rel(rel, pairs))
            if not os.path.exists(target):
                missing.append(rel)
                continue
            with open(target, encoding="utf-8", errors="surrogateescape") as f:
                got = f.read()
            if got == expect:
                matched += 1
                stats["insn"] += count(src, r"^\s{4}[a-z]")
                stats["method"] += count(src, r"^\.method\b")
                stats["field"] += count(src, r"^\.field\b")
                stats["string"] += count(src, r'const-string[^/]*\s+\w+,\s+"')
                stats["class"] += count(src, r"^\.class\b")
            else:
                mismatched.append(rel)

    print(f"smali files      : {files}")
    print(f"identical        : {matched}")
    print(f"mismatched       : {len(mismatched)}")
    print(f"missing target   : {len(missing)}")
    print(f"classes          : {stats['class']}")
    print(f"methods          : {stats['method']}")
    print(f"fields           : {stats['field']}")
    print(f"string constants : {stats['string']}")
    for r in (mismatched + missing)[:10]:
        print("  !", r)
    return 0 if not mismatched and not missing else 1


if __name__ == "__main__":
    raise SystemExit(main())
