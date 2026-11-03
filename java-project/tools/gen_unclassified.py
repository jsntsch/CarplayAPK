# -*- coding: utf-8 -*-
"""生成 docs/UNCLASSIFIED.md：说明哪些类仍保留混淆原名、为什么、怎么继续。

分类依据
--------
- R8 合成桥接类：外层类声明带 `/* synthetic */`（R8 为 lambda/方法引用生成）
- BouncyCastle 兼容层：类体只跟 org.bouncycastle 类型打交道
- 待取证：有真实逻辑但当前证据不足
- 已是语义名：R / HelpActivity 之类本来就可读
"""
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
SRC = os.path.join(PROJ, "src", "main", "java")

sys.path.insert(0, HERE)
import rename_map as RM  # noqa: E402

BC_HINT = re.compile(r"org\.bouncycastle|CipherParameters")


def collect():
    covered = {e[1] for e in RM.CORE} | {k.rsplit(".", 1)[1] for k in RM.KEEP}
    rows = []
    for dp, _dn, fn in os.walk(SRC):
        for f in sorted(fn):
            if not f.endswith(".java"):
                continue
            name = f[:-5]
            if name in covered:
                continue
            p = os.path.join(dp, f)
            t = open(p, encoding="utf-8", errors="replace").read()
            rel = os.path.relpath(p, SRC).replace("\\", "/")
            first = next((l for l in t.split("\n")
                          if re.search(r"\b(class|interface|enum)\s+" + re.escape(name) + r"\b", l)), "")
            impl = re.search(r"implements\s+([\w.$]+)", first)
            rows.append(dict(
                name=name, loc=t.count("\n") + 1, rel=rel,
                synth=("synthetic" in first),
                semantic=bool(re.fullmatch(r"[A-Z][A-Za-z0-9]*", name)),
                bc=bool(BC_HINT.search(t)),
                impl=impl.group(1) if impl else "",
                first=first.strip()[:90],
            ))
    return rows


def main():
    rows = collect()
    semantic = [r for r in rows if r["semantic"]]
    synth = [r for r in rows if not r["semantic"] and r["synth"]]
    bc = [r for r in rows if not r["semantic"] and not r["synth"] and r["bc"]]
    rest = [r for r in rows if not r["semantic"] and not r["synth"] and not r["bc"]]

    out = []
    out.append("# 仍保留混淆原名的类（%d 个）\n" % len(rows))
    out.append("> 原则：**没有证据就不改名**。本文件说明剩下的类为什么保持原名，")
    out.append("> 以及要继续推进时该怎么取证。\n")
    out.append("| 分类 | 数量 | 说明 |")
    out.append("|---|---|---|")
    out.append("| R8 合成桥接类 | %d | 为 lambda / 方法引用生成，方法体是单行委托，没有独立语义 |" % len(synth))
    out.append("| BouncyCastle 兼容层 | %d | 类体只跟 BC 类型打交道，本身已是算法实现 |" % len(bc))
    out.append("| 待取证 | %d | 有真实逻辑，但字节码里没有可判定的协议串/强类型线索 |" % len(rest))
    out.append("| 已是语义名 | %d | 如 `R` / `HelpActivity`，无需改 |" % len(semantic))
    out.append("")

    out.append("## 1. R8 合成桥接类（%d）\n" % len(synth))
    out.append("这些类形如：\n")
    out.append("```java")
    out.append("public final /* synthetic */ class a implements Runnable {")
    out.append("    public /* synthetic */ AvcEncoder b;")
    out.append("    public final void run() { this.b.C(surfaceTexture); }")
    out.append("}")
    out.append("```\n")
    out.append("字母只是 R8 的去重编号。给它们编语义名等于给编译器生成的胶水代码")
    out.append("编故事，所以**有意保留原名**——真正有意义的语义在它们委托去的方法上。\n")
    out.append("```")
    for r in sorted(synth, key=lambda x: x["rel"]):
        out.append("%-8s %-44s %4d 行  %s" % (r["name"], r["rel"], r["loc"], r["impl"] or "-"))
    out.append("```\n")

    out.append("## 2. BouncyCastle 兼容层（%d）\n" % len(bc))
    out.append("```")
    for r in sorted(bc, key=lambda x: x["rel"]):
        out.append("%-32s %-44s %4d 行" % (r["name"], r["rel"], r["loc"]))
    out.append("```\n")

    out.append("## 3. 待取证（%d）\n" % len(rest))
    out.append("这些类有真实逻辑，只是证据还不够。继续命名的取证顺序：\n")
    out.append("1. `python tools/peek.py secured/<名>` —— 看父类/接口/字段/方法/字符串常量；")
    out.append("2. `grep -rn \"\\b<名>\\b\" src/main/java` —— 看它被谁 `new`、参数是什么；")
    out.append("3. 写进 `tools/plan_batchN.tsv`（`旧全限定名<TAB>新名`），跑")
    out.append("   `python tools/java_rename.py --plan tools/plan_batchN.tsv`；")
    out.append("4. `python tools/compile_check.py --src src/main/java` 必须仍是 0 error。\n")
    out.append("```")
    for r in sorted(rest, key=lambda x: (-x["loc"])):
        out.append("%-6s %-44s %4d 行  %s" % (r["name"], r["rel"], r["loc"], r["first"][:60]))
    out.append("```\n")

    if semantic:
        out.append("## 4. 已是语义名（%d）\n" % len(semantic))
        out.append("```")
        for r in sorted(semantic, key=lambda x: x["rel"]):
            out.append("%-24s %-44s %4d 行" % (r["name"], r["rel"], r["loc"]))
        out.append("```")

    p = os.path.join(PROJ, "docs", "UNCLASSIFIED.md")
    with open(p, "w", encoding="utf-8") as f:
        f.write("\n".join(out) + "\n")
    print("UNCLASSIFIED.md: 合成 %d / BC %d / 待取证 %d / 已语义 %d / 合计 %d"
          % (len(synth), len(bc), len(rest), len(semantic), len(rows)))


if __name__ == "__main__":
    main()
