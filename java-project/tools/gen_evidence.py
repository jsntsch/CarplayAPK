# -*- coding: utf-8 -*-
"""为未命名（混淆名）类生成证据报告，供人工/模型做语义命名。

原则：没有证据就不改名。这里只**收集证据**，不下判断。

证据维度
--------
1. 结构：包、类型关键字、父类、接口、行数、字段数、方法数
2. 常量：类内字符串字面量里「像协议字段」的（含下划线 / 斜杠 / 全大写）
3. 类型引用：字段/方法参数/返回值里出现的其他类
4. 被谁用：其他文件里出现该类简单名的次数（use_count）+ 引用方名单
5. 方法签名：帮助判断职责（onXxx / parse / encode / init ...）

输出
----
    docs/evidence.tsv   一行一个类，便于 grep / 排序
    docs/EVIDENCE.md    分类整理，便于通读
"""
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
SRC = os.path.join(PROJ, "src", "main", "java")

sys.path.insert(0, HERE)
import rename_map as RM  # noqa: E402

CLASS_DECL = re.compile(
    r"^(?P<mods>(?:(?:public|private|protected|static|final|abstract|strictfp)\s+)*)"
    r"(?P<kind>class|interface|enum)\s+(?P<name>[A-Za-z_$][\w$]*)"
    r"(?:\s*<[^{]*?>)?(?:\s+extends\s+(?P<extends>[^{]+?))?"
    r"(?:\s+implements\s+(?P<implements>[^{]+?))?\s*\{?\s*$", re.M)

FIELD = re.compile(
    r"^\s{4,}(?:(?:public|private|protected|static|final|volatile|transient|/\*.*?\*/)\s+)+"
    r"(?P<type>[A-Za-z_$][\w.$]*(?:<[^;{]*>)?(?:\[\])*)\s+(?P<name>[A-Za-z_$][\w$]*)"
    r"\s*(?:=[^;]*)?;", re.M)

METHOD = re.compile(
    r"^\s{4,}(?:(?:public|private|protected|static|final|abstract|synchronized|native|default|"
    r"strictfp|/\*.*?\*/)\s+)+(?P<ret>[A-Za-z_$][\w.$<>\[\], ?]*?)\s+"
    r"(?P<name>[A-Za-z_$][\w$]*)\s*\((?P<args>[^)]*)\)", re.M)

STRING = re.compile(r'"([^"\\\n]{3,80})"')

PROTO_HINT = re.compile(r"[a-z0-9]+_[a-z0-9_]+|/|^[A-Z][A-Z0-9_]{3,}$|^[a-z]+/[a-z0-9.+-]+$")


def read(p):
    with open(p, encoding="utf-8", errors="replace") as f:
        return f.read()


def simple_of_decl(text):
    m = CLASS_DECL.search(text)
    return m


def collect():
    files = {}
    for dp, _dn, fn in os.walk(SRC):
        for x in fn:
            if x.endswith(".java"):
                p = os.path.join(dp, x)
                files[os.path.basename(x)[:-5]] = (p, read(p))
    return files


def is_obf(name):
    return re.fullmatch(r"[a-z][a-z0-9]{0,3}", name) is not None


def main():
    files = collect()
    kept = set()
    for e in RM.CORE:
        kept.add(e[0].rsplit(".", 1)[1].split("$")[0])
    for k in RM.KEEP:
        kept.add(k.rsplit(".", 1)[1])

    # 反查：每个混淆类被谁引用
    users = {}
    for host, (p, text) in files.items():
        for name in files:
            if name == host:
                continue
            n = len(re.findall(r"(?<![\w$])" + re.escape(name) + r"(?![\w$])", text))
            if n:
                users.setdefault(name, []).append((host, n))

    rows = []
    for name, (p, text) in files.items():
        if not is_obf(name):
            continue
        if name in ("R",):
            continue
        pkg = re.search(r"^package\s+([\w.]+);", text, re.M)
        pkg = pkg.group(1) if pkg else ""
        loc = text.count("\n") + 1
        m = simple_of_decl(text)
        kind = m.group("kind") if m else "?"
        ext = (m.group("extends") or "").strip() if m else ""
        impl = (m.group("implements") or "").strip() if m else ""
        fields = [(mm.group("type"), mm.group("name")) for mm in FIELD.finditer(text)]
        methods = [(mm.group("ret").strip(), mm.group("name"), mm.group("args").strip())
                   for mm in METHOD.finditer(text)]
        strings = []
        seen = set()
        for s in STRING.findall(text):
            if s in seen:
                continue
            seen.add(s)
            if PROTO_HINT.search(s):
                strings.append(s)
        u = users.get(name, [])
        u.sort(key=lambda x: -x[1])
        rows.append(dict(name=name, pkg=pkg, loc=loc, kind=kind, ext=ext, impl=impl,
                         fields=fields, methods=methods, strings=strings[:14],
                         users=u[:6], use_total=sum(x[1] for x in u)))

    # 排序：先按「证据强度」
    def score(r):
        s = 0
        s += min(len(r["strings"]), 8)
        s += min(r["use_total"], 20) * 0.5
        s += 3 if r["impl"] else 0
        s += 2 if r["ext"] and r["ext"] not in ("Object",) else 0
        s += min(len(r["methods"]), 10) * 0.3
        return -s

    rows.sort(key=score)

    os.makedirs(os.path.join(PROJ, "docs"), exist_ok=True)
    tsv = os.path.join(PROJ, "docs", "evidence.tsv")
    with open(tsv, "w", encoding="utf-8") as f:
        f.write("name\tpkg\tloc\tkind\textends\timplements\tuse_total\tusers\tstrings\tmethods\n")
        for r in rows:
            f.write("\t".join([
                r["name"], r["pkg"], str(r["loc"]), r["kind"], r["ext"], r["impl"],
                str(r["use_total"]),
                ",".join("%s(%d)" % (u[0], u[1]) for u in r["users"]),
                " | ".join(r["strings"]),
                ",".join(mm[1] for mm in r["methods"][:14]),
            ]) + "\n")

    md = os.path.join(PROJ, "docs", "EVIDENCE.md")
    with open(md, "w", encoding="utf-8") as f:
        f.write("# 未命名类证据表\n\n")
        f.write("共 %d 个未命名类。按「证据强度」降序；证据不足者保留原名。\n\n" % len(rows))
        for r in rows:
            f.write("## `%s`（%d 行，%s）\n\n" % (r["name"], r["loc"], r["kind"]))
            f.write("- 包：`%s`\n" % r["pkg"])
            if r["ext"]:
                f.write("- extends：`%s`\n" % r["ext"])
            if r["impl"]:
                f.write("- implements：`%s`\n" % r["impl"])
            if r["users"]:
                f.write("- 被引用 %d 次：%s\n" % (
                    r["use_total"], ", ".join("`%s`×%d" % (u[0], u[1]) for u in r["users"])))
            if r["fields"]:
                f.write("- 字段（%d）：%s\n" % (len(r["fields"]), ", ".join(
                    "%s %s" % (t, n) for t, n in r["fields"][:8])))
            if r["methods"]:
                f.write("- 方法（%d）：%s\n" % (len(r["methods"]), ", ".join(
                    "%s %s(%s)" % (t, n, a[:40]) for t, n, a in r["methods"][:8])))
            if r["strings"]:
                f.write("- 常量：%s\n" % "、".join("`%s`" % s for s in r["strings"][:10]))
            f.write("\n")

    print("未命名类 %d 个" % len(rows))
    print("evidence.tsv ->", tsv)
    print("EVIDENCE.md  ->", md)


if __name__ == "__main__":
    main()
