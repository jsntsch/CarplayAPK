# -*- coding: utf-8 -*-
"""把 CORE 里「尚未应用」的顶层类重命名，从 0-error 的 39 名树推导到 74 名树。

为什么不直接 re-jadx
-------------------
re-jadx 重建后的 apk 会产生更差的产物（实测丢 throws 子句、方法签名漂移），
导致 182 个编译错误，且 39 名树（0 error）那种干净产物无法复现。因此改为：
在已 0-error 的 39 名树上，仅做「类改名 + 引用改名」这一机械步骤，保留全部
干净的代码与注释。

安全性（避免单字母碰撞）
------------------------
改名只在「类型出现的位置」做，绝不碰同名单字母局部变量：
  1. 声明   `class/interface/enum OLD`
  2. 限定   `pkg.OLD` / `Outer.OLD`（OLD 前面是点）
  3. 类型位 `OLD` 后接 空白/</(/[/,/;/)/&/|
            （覆盖 声明变量、参数、返回、强转、泛型、数组；**不**覆盖 `OLD.member`）
对单字母类（x/s/j）刻意跳过规则 2（限定 member 访问），只改声明/继承/强转/
类型位，把极少数 `x.MEMBER` 静态访问留给「编译→对照」环节手动收尾。

用法
----
    python tools/apply_names_to_tree.py [--src DIR] [--dry-run]
默认 src = java-project/src/main/java。会就地改名（先备份或在副本上跑）。
"""
import argparse
import os
import re
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
sys.path.insert(0, HERE)
import rename_map  # noqa: E402

SRC = os.path.join(PROJ, "src", "main", "java")
IDENT = r"[A-Za-z_$][\w$]*"


def classify():
    """返回待处理的顶层重命名 [(old_fq, old_simple, new_simple, pkg_rel)]。"""
    todo = []
    for old_fq, new_simple, _why in rename_map.CORE:
        if "$" in old_fq:
            continue  # 嵌套类交给 fix_type_shadow
        pkg, simple = old_fq.rsplit(".", 1)
        pkg_rel = pkg.replace(".", "/")
        todo.append((old_fq, simple, new_simple, pkg_rel))
    return todo


def ref_replace(old, new, text, single, is_target, own_is_old, bare_ok, class_pkg):
    """old/new：简单名。single：是否单字母类。is_target：当前文件即被改名的文件。
    own_is_old：当前文件自己声明了一个名为 old 的类（可能是同名的不同类）。
    bare_ok：本文件里的裸名 old 是否确实指代「本类」——同包，或 import 了本类的全限定名。
             不同包又没 import，则裸名 old 指的是那个包自己的同名类，绝不能改。
    class_pkg：被改名类的包全限定名（如 io.carplay.app.secured），用于精确匹配全限定引用。
    """
    # 本文件自己声明了名为 old 的类（跨包同名的不同类，如 prototype/a0.java 里的
    # a0 与 secured/a0.java 里的 a0），且本文件不是被改名的那个：此处的 old 是
    # 「另一个同名类」，绝不能改名（否则 class 名与文件名不匹配，且引用会错指）。
    if own_is_old and not is_target:
        return text, 0

    n = 0
    # 1) 声明（仅目标文件里改类声明）
    if is_target:
        text, k = re.subn(r"\b(class|interface|enum)(\s+)" + old + r"\b",
                          r"\1\2" + new, text)
        n += k
        # 构造器（必须带访问修饰符）：`public x(` -> `public HidCapabilities(`。
        # 不能写成「任何 OLD(」——那会把 `public boolean x(...)` 这种「方法名恰好
        # 叫 x」的方法也改掉（实测 HidCapabilities 的静态方法 x 被改成 HidCapabilities）。
        text, k = re.subn(
            r"\b(public|private|protected)(\s+)" + old + r"(\s*\()",
            r"\1\2" + new + r"\3", text)
        n += k

    # 全限定引用（含 import 语句、内联全限定引用）。只匹配「本类自己的包」，
    # 避免把 prototype.x 误改成 secured.x 的新名。始终安全，与裸名无关。
    text, k = re.subn(
        r"(" + re.escape(class_pkg) + r"\.)" + old + r"\b", r"\1" + new, text)
    n += k

    if not bare_ok:
        # 不同包且未 import 本类：裸名 old 是别的同名类，只保留上面的全限定改名。
        return text, n

    # 2) extends / implements
    text, k = re.subn(r"\b(extends|implements)(\s+)" + old + r"\b",
                      r"\1\2" + new, text)
    n += k
    # 3) new OLD(
    text, k = re.subn(r"\bnew\s+" + old + r"\s*\(", "new " + new + "(", text)
    n += k

    if single:
        # 单字母类：只在「类型后接标识符」(j foo) 这种真正的类型声明/参数位改名，
        # 绝不碰 `int j =`、`return j;`、`= j` 等变量用法。
        text, k = re.subn(r"(?<![\w.$])" + old + r"(?=\s+[A-Za-z_$])", new, text)
        n += k
        # 裸嵌套类型访问 OLD.member：只认「嵌套类/枚举」形态——成员以大写字母
        # 开头（如 HidTouchType，已语义化的嵌套类型），或成员是单个小写字母
        # （如 `x.c cVar`、`s.d dVar` 这类未改名的混淆嵌套类）。
        # 绝不碰 `s.equalsIgnoreCase(...)` 这种「局部变量调方法」（成员名小写多字母）。
        text, k = re.subn(r"(?<![\w.$])" + old + r"(?=\.(?:[A-Z]|[a-z]\b))", new, text)
        n += k
        # 方法引用 OLD::member（类名做静态方法引用）
        text, k = re.subn(r"(?<![\w.$])" + old + r"(?=::)", new, text)
        n += k
    else:
        # 多字母类：几乎不可能与局部变量撞名，用较宽规则。
        # 1) 限定访问 pkg.OLD / Outer.OLD —— 但跳过「变量.方法(...)」调用
        #    （OLD 后面紧跟 `(`）。否则 mainActivity.n1() 会被误改成
        #    mainActivity.VideoEncoderStats()（n1 是方法名，不是类）。
        text, k = re.subn(r"(?<=[\w.$])" + old + r"\b(?!\s*\()", new, text)
        n += k
        # 2) 类型位：OLD 后接 空白/</[/,/;/)/&/| —— 刻意 **不含 `(`**：
        #    `OLD(` 基本是方法声明/调用（OLD 作方法名），改了会把方法名改坏。
        text, k = re.subn(r"(?<![\w.$])" + old + r"(?=[\s<\[,;)&|])", new, text)
        n += k
        # 3) 裸静态/嵌套访问 OLD.member（多字母不会与局部变量撞名，安全）
        text, k = re.subn(r"(?<![\w.$])" + old + r"(?=\.[A-Za-z_$])", new, text)
        n += k
    return text, n


def file_package(text):
    m = re.search(r"^\s*package\s+([\w.]+)\s*;", text, re.M)
    return m.group(1) if m else ""


def imports_fqn(text, fqn):
    """本文件是否 import 了 fqn（或它的嵌套类 fqn.Xxx）。"""
    return bool(re.search(
        r"^\s*import\s+" + re.escape(fqn) + r"(\s*;|\s*\.)", text, re.M))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", default=SRC)
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()
    root = args.src
    todo = classify()

    total_files = 0
    total_refs = 0
    for old_fq, old, new, pkg_rel in todo:
        class_pkg = old_fq.rsplit(".", 1)[0]   # 本类所在包全限定名
        old_file = os.path.join(root, pkg_rel, old + ".java")
        new_file = os.path.join(root, pkg_rel, new + ".java")
        if os.path.exists(new_file):
            continue  # 39 名树已应用（39 名之一）
        if not os.path.exists(old_file):
            continue  # 不在本树（理论上不应发生）
        # target_path 必须是「移动后的新路径」：文件先被 move 成 New.java，
        # 之后遍历到它时路径已经是 new_file；若拿 old_file 比较则永远不相等，
        # 导致 `class OLD` 声明不被改名（实测 CarLifeFrame.java 里还写 class r6）。
        target_path = os.path.abspath(new_file)
        # 改名文件
        if not args.dry_run:
            shutil.move(old_file, new_file)
        total_files += 1
        # 全树替换引用
        single = len(old) == 1
        refs = 0
        for dp, _dn, fns in os.walk(root):
            for fn in fns:
                if not fn.endswith(".java"):
                    continue
                p = os.path.join(dp, fn)
                t = open(p, encoding="utf-8", errors="replace").read()
                # is_target 必须是「逐文件」判断：只有真正被改名的那个文件才改
                # 类声明 / 构造器；其它文件一律视作非目标，避免 prototype/a0.java
                # 这种「同名不同类」文件被误改成 HidCommandSender（#1 致命 bug）。
                is_target = (os.path.abspath(p) == target_path)
                own_is_old = bool(re.search(
                    r"\b(class|interface|enum)\s+" + old + r"\b", t))
                # 裸名是否指向本类：同包，或本文件 import 了本类的全限定名。
                # 不同包又没 import 时，裸名 old 是那个包自己的同名类（如
                # prototype.x / prototype.a0），绝不能改。
                bare_ok = (file_package(t) == class_pkg) or imports_fqn(t, old_fq)
                t2, k = ref_replace(old, new, t, single, is_target,
                                    own_is_old, bare_ok, class_pkg)
                if k:
                    refs += k
                    if not args.dry_run:
                        open(p, "w", encoding="utf-8", newline="\n").write(t2)
        total_refs += refs
        print("  %-22s -> %-22s  file=1 refs=%d%s" %
              (old, new, refs, " (single-letter)" if single else ""))
    print("\n改名文件数=%d  引用替换处=%d" % (total_files, total_refs))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
