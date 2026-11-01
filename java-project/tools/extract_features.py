# -*- coding: utf-8 -*-
"""从 jadx 反编译产物中提取「可用于语义命名」的类特征。

只收集客观证据，不做猜测性命名：
  - 继承 / 实现的系统类型（Activity、Service、NSD 的各种 Listener ...）
  - 字符串常量（`_carplay-ctrl._tcp`、`SETUP`、`hidSendReport` ...）
  - 方法名（`onServiceResolved` 这类框架回调直接暴露类身份）
  - 规模（方法数/字段数/行数）
"""
import json, os, re, sys
from collections import Counter

ROOT = sys.argv[1] if len(sys.argv) > 1 else r"C:/Users/SCH/WorkBuddy/2026-10-24-16-47-09/_jadx_v1729/sources"
OUT = sys.argv[2] if len(sys.argv) > 2 else "features.json"

RE_PACKAGE = re.compile(r"^package\s+([\w.]+);", re.M)
RE_CLASS = re.compile(
    r"^(?:public\s+|final\s+|abstract\s+|static\s+)*class\s+(\w+)"
    r"(?:<[^>]*>)?"
    r"(?:\s+extends\s+([\w.$]+))?"
    r"(?:\s+implements\s+([^{]+))?",
    re.M,
)
RE_STRING = re.compile(r'"((?:[^"\\]|\\.){3,120})"')
RE_METHOD = re.compile(r"^\s+(?:public|private|protected|static|final|synchronized|native|abstract|\s)*[\w.$<>\[\]]+\s+(\w+)\s*\(", re.M)
RE_FIELD = re.compile(r"^\s+(?:public|private|protected|static|final|volatile|transient|\s)*[\w.$<>\[\]]+\s+(\w+)\s*(?:=|;)", re.M)

NOISE = {"UTF-8","utf-8","GET","POST","null","true","false",""," ","Content-Length","Content-Type","CSeq","android.intent.action.MAIN"}


def main():
    rows = []
    for dirpath, _dirnames, filenames in os.walk(ROOT):
        for fn in filenames:
            if not fn.endswith(".java"):
                continue
            path = os.path.join(dirpath, fn)
            try:
                text = open(path, encoding="utf-8", errors="replace").read()
            except OSError:
                continue
            pm = RE_PACKAGE.search(text)
            pkg = pm.group(1) if pm else ""
            m = RE_CLASS.search(text)
            if not m:
                continue
            cname, extends, implements = m.group(1), m.group(2), m.group(3)
            impls = [x.strip().split("<")[0] for x in implements.split(",") if x.strip()] if implements else []

            strings, seen = [], set()
            for s in RE_STRING.findall(text):
                if s in NOISE or s in seen:
                    continue
                if not re.search(r"[_/.:A-Za-z]{4,}", s):
                    continue
                seen.add(s)
                strings.append(s)
                if len(strings) >= 24:
                    break

            methods = sorted(set(RE_METHOD.findall(text)))
            fields = sorted(set(RE_FIELD.findall(text)))
            rows.append({
                "file": os.path.relpath(path, ROOT).replace("\\", "/"),
                "pkg": pkg, "cls": cname,
                "fqcn": (pkg + "." + cname) if pkg else cname,
                "extends": extends, "implements": impls,
                "strings": strings, "methods": methods[:80],
                "field_count": len(fields), "loc": text.count("\n"),
            })

    rows.sort(key=lambda r: r["fqcn"])
    json.dump(rows, open(OUT, "w", encoding="utf-8"), ensure_ascii=False, indent=1)

    print("classes:", len(rows))
    print("by package:", Counter(r["pkg"] for r in rows).most_common())
    kind = Counter()
    for r in rows:
        ext = (r["extends"] or "").split(".")[-1]
        if ext:
            kind["extends:" + ext] += 1
        for i in r["implements"]:
            kind["impl:" + i.split(".")[-1]] += 1
    for k, v in kind.most_common(30):
        print("  %-46s %d" % (k, v))
    print("-> written", OUT)


if __name__ == "__main__":
    main()
