# -*- coding: utf-8 -*-
"""在 smali 层做类型改名，而不是在 Java 文本层做正则替换。

为什么不在 Java 层改
--------------------
jadx 输出的 Java 里，同一个简单名可能同时表示「同包顶层类」和「某个类的
嵌套类」。例如 `secured/l.java`（顶层类）与 `secured/i0.java` 里的
`public interface l`（嵌套接口）同名。文本替换无法区分两者：

    i0 内  `implements l`        -> 必须保持 `l`（嵌套）
    d0 内  `implements i0.l`     -> 必须变 `AirPlaySession.l`
    其它类 `l.foo()`             -> 必须变 `BinaryPlist.foo()`

同一个 `l` 三种语义，只能靠作用域分析，正则做不到。

smali 层没有这个问题
--------------------
类型在字节码里永远写成描述符 `Lio/carplay/app/secured/i0;`，带结尾分号，
不存在遮蔽、不存在局部变量同名、不存在泛型参数位置遗漏。改描述符 = 全站
一致，引用点与定义点一起改，天然不会漏。嵌套类写成 `L.../i0$l;`，用
`(?=[;$])` 边界一起带走。

所以：先改 dex，再反编译，Java 里的类名天生就是对的。
"""
import os
import re
import sys
import shutil

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import rename_map as RM


def build_pairs():
    """(old_slash, new_slash) 列表，按长度倒序避免前缀吞并。"""
    pairs = []
    for fqcn, new, _ in RM.CORE:
        old_slash = fqcn.replace(".", "/")
        new_slash = old_slash.rsplit("/", 1)[0] + "/" + new
        pairs.append((old_slash, new_slash))
    pairs.sort(key=lambda p: -len(p[0]))
    return pairs


def rename_smali_text(root, pairs):
    total = 0
    touched = 0
    for dirpath, _d, fns in os.walk(root):
        for fn in fns:
            if not fn.endswith(".smali"):
                continue
            p = os.path.join(dirpath, fn)
            with open(p, encoding="utf-8", errors="surrogateescape") as f:
                src = f.read()
            out = src
            n_file = 0
            for old, new in pairs:
                # 边界只允许 `;`（类型结束）或 `$`（嵌套类），
                # 这样 `.../secured/d` 不会命中 `.../secured/d1`。
                pat = "L" + old + r"(?=[;$])"
                out, n = re.subn(pat, "L" + new, out)
                n_file += n
            if n_file:
                with open(p, "w", encoding="utf-8", errors="surrogateescape") as f:
                    f.write(out)
                total += n_file
                touched += 1
    return total, touched


def rename_files(smali_root, pairs):
    """把 i0.smali / i0$l.smali 一并改名，保持目录结构与描述符一致。"""
    moves = []
    for dirpath, _d, fns in os.walk(smali_root):
        for fn in fns:
            if not fn.endswith(".smali"):
                continue
            p = os.path.join(dirpath, fn)
            stem = os.path.relpath(p, smali_root).replace(os.sep, "/")[:-6]
            for old, new in pairs:
                if stem == old or stem.startswith(old + "$"):
                    new_stem = new + stem[len(old):]
                    moves.append((p, os.path.join(smali_root, new_stem + ".smali")))
                    break
    for src, dst in moves:
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        if os.path.exists(dst):
            os.remove(dst)
        os.rename(src, dst)
    return len(moves)


def main():
    if len(sys.argv) < 2:
        print("usage: dex_rename.py <apktool-decoded-dir>")
        return 2
    root = sys.argv[1]
    pairs = build_pairs()
    print(f"map entries = {len(pairs)}")
    total, touched = rename_smali_text(root, pairs)
    print(f"descriptor hits = {total} in {touched} smali files")
    moved = 0
    for name in sorted(os.listdir(root)):
        if name == "smali" or name.startswith("smali_classes"):
            sub = os.path.join(root, name)
            if os.path.isdir(sub):
                moved += rename_files(sub, pairs)
    print(f"renamed files = {moved}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
