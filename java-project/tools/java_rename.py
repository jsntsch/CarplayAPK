# -*- coding: utf-8 -*-
"""Java 源码语义改名驱动（调用 rename_engine/RenameTool.java）。

与旧的 apply_names_to_tree.py 的区别
------------------------------------
旧脚本在**文本层**用正则改类名引用，遇到 jadx 产物里「类名与字段/局部变量同名」
（如 `this.b.a(...)` 里的字段 b）必然误伤。本脚本把引用替换交给 javac：
RenameTool 用 Trees.getElement() 拿到每个标识符解析出的符号，只有确实是目标
ClassSymbol 时才改写，因此对单字母类（a / b / c / b0 ...）也是安全的。

流水线
------
1. 把 plan.tsv（`old_fq<TAB>newName`）写成 RenameTool 的 map 文件
2. dry-run 检查每个目标是否有命中（零命中 = 名字表写错）
3. 备份源码树
4. --apply 落地引用 + 类声明改名
5. 顶层类改文件名
6. 残留的完全限定名文本兜底（import / Class.forName 字符串）
7. compile_check 验收 0 error

用法
----
    python tools/java_rename.py --plan plan.tsv [--src DIR] [--no-compile]
"""
import argparse
import os
import re
import shutil
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
ENGINE_DIR = os.path.join(HERE, "rename_engine")
ENGINE_SRC = os.path.join(ENGINE_DIR, "RenameTool.java")
ENGINE_CLASSES = os.path.join(ENGINE_DIR, "classes")

JAVA = os.environ.get("CARPLAY_JAVA", r"C:/Program Files/Android/Android Studio/jbr/bin/java.exe")
JAVAC = os.environ.get("CARPLAY_JAVAC", r"C:/Program Files/Android/Android Studio/jbr/bin/javac.exe")
ANDROID_JAR = r"C:/Users/SCH/AppData/Local/Android/Sdk/platforms/android-36/android.jar"
BCPROV = (r"C:/Users/SCH/AppData/Local/Android/Sdk/cmdline-tools/latest/lib/external/"
          r"org/bouncycastle/bcprov-jdk18on/1.79/bcprov-jdk18on-1.79.jar")


def classpath():
    cp = [ANDROID_JAR]
    if os.path.exists(BCPROV):
        cp.append(BCPROV)
    return ";".join(cp)


def ensure_engine():
    """编译 RenameTool（增量：class 比源新就跳过）。"""
    cls = os.path.join(ENGINE_CLASSES, "RenameTool.class")
    if os.path.exists(cls) and os.path.getmtime(cls) > os.path.getmtime(ENGINE_SRC):
        return
    os.makedirs(ENGINE_CLASSES, exist_ok=True)
    r = subprocess.run([JAVAC, "-encoding", "UTF-8", "-nowarn", "-d", ENGINE_CLASSES, ENGINE_SRC],
                       capture_output=True, text=True, encoding="utf-8", errors="replace")
    if r.returncode != 0:
        print(r.stdout)
        print(r.stderr)
        raise SystemExit("RenameTool 编译失败")


def read_plan(path):
    plan = []
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            if not line.strip() or line.lstrip().startswith("#"):
                continue
            old, new = line.split("\t")[:2]
            plan.append((old.strip(), new.strip()))
    return plan


def write_files_list(src, out):
    n = 0
    with open(out, "w", encoding="utf-8") as f:
        for dp, _dn, fn in os.walk(src):
            for x in sorted(fn):
                if x.endswith(".java"):
                    f.write(os.path.join(dp, x) + "\n")
                    n += 1
    return n


def run_engine(src, plan, apply_=False, report=None):
    ensure_engine()
    tmp = tempfile.mkdtemp(prefix="rn_")
    map_path = os.path.join(tmp, "map.tsv")
    with open(map_path, "w", encoding="utf-8") as f:
        for old, new in plan:
            f.write("%s\t%s\n" % (old, new))
    list_path = os.path.join(tmp, "files.txt")
    n = write_files_list(src, list_path)

    cmd = [JAVA, "-Xss16m", "-cp", ENGINE_CLASSES, "RenameTool",
           "--cp", classpath(), "--map", map_path, "--files", list_path]
    if report:
        cmd += ["--report", report]
    if apply_:
        cmd.append("--apply")
    r = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    out = (r.stdout or "") + (r.stderr or "")
    return out, n, r.returncode


TOP_LEVEL_REF = re.compile(r"(?<![\w$.])(%s)(?![\w$])")


def fix_fqn_residue(src, plan):
    """兜底：把残留在文本里的完全限定名（点号形式）替换掉。

    RenameTool 已覆盖所有能解析的引用；这里只处理它拿不到的场合：
    import 语句尾、字符串常量里的 FQCN（如 Class.forName）。逐条做，避免
    前缀冲突（b 与 b2）。
    """
    # 只处理嵌套类：顶层类的 FQN 含包名，几乎不会出现在字符串里；
    # 且顶层改名后其 FQN 引用已被 MemberSelect 链覆盖。
    edits = []
    for old, new in plan:
        if "$" not in old:
            continue
        outer_old, inner_old = old.rsplit(".", 1)[1].split("$")
        pkg = old.rsplit(".", 1)[0]
        # 外层新名需要查表
        outer_new = None
        for o2, n2 in plan:
            if o2 == pkg + "." + outer_old:
                outer_new = n2
                break
        if outer_new is None:
            # 外层未改名，FQN 形如 pkg.Outer.inner
            fq_old = pkg + "." + outer_old + "." + inner_old
            fq_new = pkg + "." + outer_old + "." + new
        else:
            fq_old = pkg + "." + outer_new + "." + inner_old
            fq_new = pkg + "." + outer_new + "." + new
        edits.append((fq_old, fq_new))
    if not edits:
        return 0
    touched = 0
    for dp, _dn, fn in os.walk(src):
        for x in fn:
            if not x.endswith(".java"):
                continue
            p = os.path.join(dp, x)
            with open(p, encoding="utf-8") as f:
                text = f.read()
            orig = text
            for a, b in edits:
                text = re.sub(r"(?<![\w$.])" + re.escape(a) + r"(?![\w$])", b, text)
            if text != orig:
                with open(p, "w", encoding="utf-8") as f:
                    f.write(text)
                touched += 1
    return touched


def rename_files(src, plan):
    """顶层类改名后同步文件名。"""
    done, skipped = [], []
    for old, new in plan:
        if "$" in old:
            continue
        pkg = old.rsplit(".", 1)[0]
        old_simple = old.rsplit(".", 1)[1]
        d = os.path.join(src, *pkg.split("."))
        old_fp = os.path.join(d, old_simple + ".java")
        new_fp = os.path.join(d, new + ".java")
        if not os.path.exists(old_fp):
            skipped.append(old_simple)
            continue
        if os.path.exists(new_fp):
            raise SystemExit("文件名冲突: %s 已存在" % new_fp)
        os.rename(old_fp, new_fp)
        done.append((old_simple, new))
    return done, skipped


def compile_check(src):
    r = subprocess.run([sys.executable, os.path.join(HERE, "compile_check.py"), "--src", src],
                       capture_output=True, text=True, encoding="utf-8", errors="replace")
    return (r.stdout or "") + (r.stderr or ""), r.returncode


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--plan", required=True)
    ap.add_argument("--src", default=os.path.join(PROJ, "src", "main", "java"))
    ap.add_argument("--no-compile", action="store_true")
    ap.add_argument("--no-backup", action="store_true")
    ap.add_argument("--fqn-residue", action="store_true",
                    help="启用完全限定名文本兜底（默认关闭：会把 FQN 形式的方法调用误改成类名）")
    args = ap.parse_args()

    src = os.path.abspath(args.src)
    plan = read_plan(args.plan)

    print("=== 1. dry-run 校验（%d 个目标）===" % len(plan))
    out, n, rc = run_engine(src, plan, apply_=False)
    print(out)
    if "WARN_zero_hit" in out:
        raise SystemExit("存在零命中目标，名字表有误，已中止")

    if not args.no_backup:
        bak = src.rstrip("/\\") + ".bak"
        if os.path.exists(bak):
            shutil.rmtree(bak)
        shutil.copytree(src, bak)
        print("=== 2. 已备份到 %s ===" % bak)

    print("=== 3. 应用改名 ===")
    out, n, rc = run_engine(src, plan, apply_=True)
    print(out)

    print("=== 4. 构造器名修正 ===")
    import fix_ctor_names as FCN
    ct, cdet = FCN.fix_tree(src, plan)
    print("构造器修正 %d 处" % ct)
    for row in cdet:
        print("  %s: %s(...) -> %s(...) ×%d" % row)

    if args.fqn_residue:
        print("=== 4b. 完全限定名残留兜底 ===")
        t = fix_fqn_residue(src, plan)
        print("改动文件数 = %d" % t)

    print("=== 5. 顶层类文件改名 ===")
    done, skipped = rename_files(src, plan)
    for a, b in done:
        print("  %s.java -> %s.java" % (a, b))
    if skipped:
        print("  跳过(文件不存在): %s" % skipped)

    if args.no_compile:
        return
    print("=== 6. compile_check ===")
    log, rc = compile_check(src)
    print(log)
    sys.exit(rc)


if __name__ == "__main__":
    main()
