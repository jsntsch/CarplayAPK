# -*- coding: utf-8 -*-
"""快速查看某个类的结构：签名 + 常量，用于语义命名时取证。"""
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
SRC = os.path.join(PROJ, "src", "main", "java")

SIG = re.compile(
    r"^\s{4,}(?P<mods>(?:(?:public|private|protected|static|final|abstract|synchronized|"
    r"native|default|strictfp|/\*.*?\*/)\s+)*)"
    r"(?P<ret>[A-Za-z_$][\w.$<>\[\], ?]*?)\s+(?P<name>[A-Za-z_$][\w$]*)\s*\((?P<args>[^)]*)\)",
    re.M)
CTOR = re.compile(r"^\s{4,}(?:(?:public|private|protected)\s+)"
                  r"(?P<name>[A-Za-z_$][\w$]*)\s*\((?P<args>[^)]*)\)\s*(?:throws [\w., ]+)?\{", re.M)


def find(name):
    pkg = None
    if "/" in name:
        pkg, name = name.rsplit("/", 1)
    hits = []
    for dp, _dn, fn in os.walk(SRC):
        for x in fn:
            if x == name + ".java":
                hits.append(os.path.join(dp, x))
    if pkg:
        for h in hits:
            if os.path.relpath(h, SRC).replace("\\", "/").startswith(pkg.replace(".", "/") + "/"):
                return h
    return hits[0] if hits else None


def show(name, max_strings=30):
    p = find(name)
    if not p:
        print("未找到", name)
        return
    text = open(p, encoding="utf-8", errors="replace").read()
    print("=" * 70)
    print("%s  (%s, %d 行)" % (name, os.path.relpath(p, SRC).replace("\\", "/"), text.count("\n") + 1))
    m = re.search(r"^package\s+([\w.]+);", text, re.M)
    print("package:", m.group(1) if m else "?")
    for mm in re.finditer(r"^\s*(?:(?:public|private|protected|static|final|abstract)\s+)*"
                          r"(class|interface|enum)\s+([A-Za-z_$][\w$]*)"
                          r"(?:[^{]*?)(?:extends\s+([\w.$<>]+))?(?:[^{]*?implements\s+([\w.$<>, ]+))?\s*\{",
                          text, re.M):
        print("  %-9s %-24s extends=%-22s implements=%s" % (
            mm.group(1), mm.group(2), (mm.group(3) or "").strip(), (mm.group(4) or "").strip()))
    print("--- 方法 ---")
    for mm in SIG.finditer(text):
        print("  %-28s %s(%s)" % (mm.group("ret").strip(), mm.group("name"),
                                  mm.group("args").strip()[:70]))
    ctor = {mm.group("name") for mm in CTOR.finditer(text)}
    if ctor:
        print("--- 构造器 ---")
        for mm in CTOR.finditer(text):
            print("  %s(%s)" % (mm.group("name"), mm.group("args").strip()[:70]))
    print("--- 常量 ---")
    seen = []
    for s in re.findall(r'"([^"\\\n]{2,90})"', text):
        if s not in seen:
            seen.append(s)
    for s in seen[:max_strings]:
        print("  %s" % s)
    if len(seen) > max_strings:
        print("  ... 共 %d 条" % len(seen))


if __name__ == "__main__":
    for n in sys.argv[1:]:
        show(n)
