# -*- coding: utf-8 -*-
"""把「74 条语义命名」完整落到源码工程，并复现 0 error 编译基线。

为什么需要这个脚本
------------------
build_project.py 的 step6 只跑了 annotate + fix_jadx_defects 两个脚本，
而原始 39 名树能编译到 0 error，是因为还跑了另外 5 个 fix 脚本
（fix_type_shadow / fix_missing_catch_var / fix_catch_param_shadow /
fix_varnames / fix_obscured_types / fix_field_shadow）。本脚本把整条 fix
链按正确顺序跑完，并迭代「编译 → 修遮蔽类型 → 编译 → 修字段遮蔽」直到 error
数收敛。

改名安全性
----------
74 名里有 3 个单字母类（x→HidCapabilities、s→AudioCapabilities、
j→AvcSpsParser），只能在 smali（描述符）层改名，不能做 Java 文本层全局替换
（会命中同名局部变量）。本脚本不负责改名——改名在 build_project.py 的
dex_rename 阶段完成，本脚本只消费已经改好名的 jadx 产物。

用法
----
    # 1) 先把干净 74 名 jadx 产物拷进 src/main/java（会覆盖 39 名树，
    #    但 src.bak.pre74 已存 0-error 39 名树作安全网）
    python tools/rebuild_74.py --swap <clean_74_sources_dir>

    # 2) 在已 swap 的 src/main/java 上跑完整 fix 链 + 迭代收敛
    python tools/rebuild_74.py --run

    # 3) 一键：swap + run
    python tools/rebuild_74.py --swap <dir> --run
"""
import argparse
import os
import shutil
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
SRC = os.path.join(PROJ, "src", "main", "java")
TARGET = SRC  # 可被 --target 覆盖；所有 fix 脚本通过 CARPLAY_SRC 环境变量指向它
BUILD = r"D:/VSCode/CarplayAPKBuild"

JAVAC = r"C:/Program Files/Android/Android Studio/jbr/bin/javac.exe"
ANDROID_JAR = r"C:/Users/SCH/AppData/Local/Android/Sdk/platforms/android-36/android.jar"
BCPROV = (r"C:/Users/SCH/AppData/Local/Android/Sdk/cmdline-tools/latest/lib/"
          r"external/org/bouncycastle/bcprov-jdk18on/1.79/bcprov-jdk18on-1.79.jar")


def step(msg):
    print("\n=== " + msg + " ===")


def run_py(mod, cwd=None, env=None, args=None):
    cmd = [sys.executable, os.path.join(HERE, mod + ".py")]
    if args:
        cmd += args
    _env = dict(os.environ)
    _env["CARPLAY_SRC"] = TARGET
    if env:
        _env.update(env)
    r = subprocess.run(cmd, cwd=cwd or TARGET, env=_env, capture_output=True, text=True,
                       encoding="utf-8", errors="replace")
    out = (r.stdout or "") + (r.stderr or "")
    print(out.strip()[-2000:] if out.strip() else "(no output)")
    return r.returncode


def compile_count(log_path, out_dir):
    """调用 compile_check.py 返回 error 数，并把 log 写到 log_path。"""
    r = subprocess.run(
        [sys.executable, os.path.join(HERE, "compile_check.py"),
         "--src", TARGET, "--log", log_path, "--out", out_dir, "--quiet"],
        capture_output=True, text=True, encoding="utf-8", errors="replace")
    try:
        with open(log_path, encoding="utf-8", errors="replace") as f:
            n = sum(1 for ln in f if ": error:" in ln)
    except OSError:
        n = -1
    return n


def do_swap(clean_dir):
    step("swap: 用干净 74 名 jadx 产物覆盖目标目录 %s" % TARGET)
    if not os.path.isdir(clean_dir):
        raise SystemExit("干净 74 名目录不存在: " + clean_dir)
    shutil.rmtree(TARGET, ignore_errors=True)
    shutil.copytree(clean_dir, TARGET)
    n = sum(1 for _ in _walk(TARGET))
    print("已拷贝 %d 个 .java 文件到 %s" % (n, TARGET))


def _walk(d):
    for dp, _dn, fn in os.walk(d):
        for x in fn:
            if x.endswith(".java"):
                yield os.path.join(dp, x)


def do_run():
    out_dir = os.path.join(BUILD, "rebuild74_classes")
    os.makedirs(out_dir, exist_ok=True)
    log_path = os.path.join(BUILD, "rebuild74_compile.log")

    # ---- 确定性 fix 链（不依赖编译，幂等）----
    step("1/7 annotate（注入注释）")
    run_py("annotate")
    step("2/7 fix_jadx_defects（修反编译缺陷）")
    run_py("fix_jadx_defects", args=[TARGET])
    step("3/7 fix_type_shadow（嵌套类型遮蔽字段）")
    run_py("fix_type_shadow")
    step("4/7 fix_missing_catch_var（补 catch 转发变量声明）")
    run_py("fix_missing_catch_var")
    step("5/7 fix_catch_param_shadow（catch 参数与 th 变量冲突）")
    run_py("fix_catch_param_shadow")
    step("6/7 fix_varnames（修正注释里的方法名指代）")
    run_py("fix_varnames")

    # ---- 迭代：编译 → 修遮蔽类型 → 编译 → 修字段遮蔽 ----
    env = dict(os.environ)
    env["CARPLAY_JAVAC"] = JAVAC
    env["CARPLAY_CP"] = ";".join([ANDROID_JAR, BCPROV])
    env["CARPLAY_OUT"] = out_dir
    env["CARPLAY_SOURCES"] = os.path.join(out_dir, "sources.txt")
    # 预生成 sources 清单供 fix_obscured_types 使用（相对 TARGET）
    with open(env["CARPLAY_SOURCES"], "w", encoding="utf-8") as f:
        for p in _walk(TARGET):
            rel = os.path.relpath(p, TARGET).replace(os.sep, "/")
            f.write(rel + "\n")

    prev = None
    for it in range(1, 8):
        n = compile_count(log_path, out_dir)
        step("迭代 %d 编译结果：error = %d" % (it, n))
        if n == 0:
            print("✓ 收敛到 0 error")
            return 0
        if n == prev:
            print("error 数不再下降（卡在 %d），停止迭代" % n)
            break
        prev = n
        # fix_obscured_types：自迭代 3 轮，按 javac 报错逐条补限定名
        step("fix_obscured_types（迭代）")
        run_py("fix_obscured_types", env=env)
        # fix_field_shadow：吃刚生成的编译 log，补字段遮蔽的限定名
        step("fix_field_shadow（吃编译 log）")
        run_py("fix_field_shadow", env=env, args=[log_path])

    # 末轮再编译一次看最终数
    n = compile_count(log_path, out_dir)
    print("\n最终 error 数 = %d  (log: %s)" % (n, log_path))
    return 1 if n else 0


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--swap", metavar="DIR", help="干净 74 名 jadx 产物目录，拷入目标目录")
    ap.add_argument("--run", action="store_true", help="在目标目录上跑完整 fix 链")
    ap.add_argument("--target", default=SRC,
                    help="目标源码目录（默认 src/main/java；可用任意临时目录做试验）")
    args = ap.parse_args()
    global TARGET
    TARGET = args.target
    if args.swap:
        do_swap(args.swap)
    if args.run:
        return sys.exit(do_run())
    if not args.swap and not args.run:
        print(__doc__)
        return 2
    return 0


if __name__ == "__main__":
    main()
