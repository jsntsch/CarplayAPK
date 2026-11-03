# -*- coding: utf-8 -*-
"""可复用的 javac 编译检查工具（让反编译出的源码能被 javac 编译）。

设计目标
--------
- 复现「CarplayAPK 源码工程」的编译基线：源码工程本就不能完整编译（缺 R 类 /
  .so / 资源），但用 android 平台 stub + bcprov 作为 classpath，并以 -nowarn
  压制 deprecation 警告后，jadx 反编译产物 + fix 链可以收敛到 0 个 error。
- 任一目录（src/main/java 或临时工作树）都能当被测对象，方便对比。

用法
----
    python tools/compile_check.py [--src DIR] [--log FILE] [--out DIR] [--quiet]

输出
----
- 把完整的 javac stdout+stderr 写进 --log（默认 <src>/../compile_check.log），
  供 fix_field_shadow.py 当输入。
- 终端打印 error 总数 + 按模板归类的 Top 错误，方便定位。
- 进程退出码 = min(error 数, 1)（0 表示 0 error；非 0 表示有 error）。

环境变量（均可省，省略用本机已验证路径）
----------------------------------------
    CARPLAY_JAVAC   javac 可执行文件
    CARPLAY_CP      classpath（android.jar;bcprov.jar）
"""
import argparse
import os
import re
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)

DEFAULT_JAVAC = r"C:/Program Files/Android/Android Studio/jbr/bin/javac.exe"
DEFAULT_ANDROID = r"C:/Users/SCH/AppData/Local/Android/Sdk/platforms/android-36/android.jar"
DEFAULT_BCPROV = (r"C:/Users/SCH/AppData/Local/Android/Sdk/cmdline-tools/latest/lib/"
                  r"external/org/bouncycastle/bcprov-jdk18on/1.79/bcprov-jdk18on-1.79.jar")


def resolve_javac():
    return os.environ.get("CARPLAY_JAVAC", DEFAULT_JAVAC)


def resolve_cp():
    if os.environ.get("CARPLAY_CP"):
        return os.environ["CARPLAY_CP"]
    return ";".join([DEFAULT_ANDROID, DEFAULT_BCPROV])


def gen_sources(src, out_list):
    """生成相对 src 的源文件清单（javac 需要在 cwd=src 下运行，才能报相对路径）。"""
    n = 0
    with open(out_list, "w", encoding="utf-8") as f:
        for dp, _dn, fn in os.walk(src):
            for x in fn:
                if x.endswith(".java"):
                    rel = os.path.relpath(os.path.join(dp, x), src).replace(os.sep, "/")
                    f.write(rel + "\n")
                    n += 1
    return n


def compile(src, log_path=None, out_dir=None, quiet=False):
    javac = resolve_javac()
    cp = resolve_cp()
    if not os.path.exists(javac):
        raise SystemExit("javac 不存在: %s" % javac)
    if not os.path.exists(DEFAULT_ANDROID):
        raise SystemExit("android.jar 不存在: %s" % DEFAULT_ANDROID)

    if out_dir is None:
        out_dir = tempfile.mkdtemp(prefix="cc_out_")
    os.makedirs(out_dir, exist_ok=True)
    if log_path is None:
        log_path = os.path.join(os.path.dirname(os.path.abspath(src)), "compile_check.log")

    src_abs = os.path.abspath(src)
    list_path = os.path.join(out_dir, "sources.txt")
    n = gen_sources(src_abs, list_path)

    cmd = [javac, "-J-Duser.language=en", "-source", "8", "-target", "8",
           "-proc:none", "-nowarn", "-Xmaxerrs", "10000", "-encoding", "UTF-8",
           "-d", out_dir, "-cp", cp, "@" + list_path]
    r = subprocess.run(cmd, cwd=src_abs, capture_output=True, text=True,
                       encoding="utf-8", errors="replace")
    log = (r.stdout or "") + (r.stderr or "")
    with open(log_path, "w", encoding="utf-8") as f:
        f.write(log)

    errs = re.findall(r"^.*error:.*$", log, flags=re.M)
    if not quiet:
        print("源文件数 = %d" % n)
        print("error 数 = %d   (log: %s)" % (len(errs), log_path))
        _summarize(log)
    return len(errs), log_path


def _summarize(log):
    lines = log.split("\n")
    errs = [l for l in lines if ": error:" in l]
    # 归类（去掉文件名/行号，保留 message 模板）
    tmpl = {}
    for l in errs:
        msg = l.split(": error:", 1)[1].strip()
        tmpl[msg] = tmpl.get(msg, 0) + 1
    if not tmpl:
        print("  ✓ 无 error")
        return
    print("  Top 错误模板:")
    for msg, c in sorted(tmpl.items(), key=lambda kv: -kv[1])[:15]:
        print("    %3d  %s" % (c, msg[:80]))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", default=os.path.join(PROJ, "src", "main", "java"))
    ap.add_argument("--log", default=None)
    ap.add_argument("--out", default=None)
    ap.add_argument("--quiet", action="store_true")
    args = ap.parse_args()
    n, _ = compile(args.src, args.log, args.out, args.quiet)
    sys.exit(1 if n else 0)


if __name__ == "__main__":
    main()
