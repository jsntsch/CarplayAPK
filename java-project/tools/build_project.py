# -*- coding: utf-8 -*-
"""从 APK 重建可读 Java 工程（一条命令跑完全链路）。

    python tools/build_project.py --apk <apk> --work <构建目录>

链路
----
    APK
     │ 1. apktool d -f -r         反编译成 smali（只取代码，不解资源）
     ▼
    smali
     │ 2. dex_rename.py           按描述符改类型名（Lpkg/Cls; → Lpkg/New;）
     ▼
    smali（已改名）
     │ 3. apktool b               重新打包成 apk
     ▼
    改名后的 apk
     │ 4. jadx --no-res           反编译成 Java
     ▼
    src/main/java                 5. annotate.py 注入中文注释
                                  6. fix_jadx_defects.py 修反编译缺陷

为什么在 smali 层改名
--------------------
jadx 的 Java 里同名标识符有歧义：`secured/l.java`（顶层类）和
`secured/i0.java` 里的 `public interface l`（嵌套接口）同名，同一个 `l`
在三个地方分别表示「嵌套接口」「i0 的嵌套接口」「顶层类」，正则无法区分。
字节码里的类型永远写成带分号的描述符，不存在遮蔽，改一处即全站一致。

环境变量（均可省略，省略时用默认值）
------------------------------------
    APKTOOL_JAR   默认 apktool.jar
    JADX_CP       默认 jadx-lib/*
    JAVA          默认 java.exe
"""
import argparse
import os
import re
import shutil
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
sys.path.insert(0, HERE)


def run(cmd, **kw):
    print("  $", " ".join(str(c) for c in cmd[:6]), "..." if len(cmd) > 6 else "")
    r = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    if r.returncode != 0:
        tail = "\n".join((r.stdout or "").splitlines()[-8:] + (r.stderr or "").splitlines()[-8:])
        raise SystemExit(f"FAILED ({r.returncode})\n{tail}")
    return r.stdout or ""


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apk", required=True)
    ap.add_argument("--work", required=True, help="构建目录（编译产物、中间文件都放这里）")
    ap.add_argument("--apktool", default=os.environ.get("APKTOOL_JAR", "apktool.jar"))
    ap.add_argument("--jadx-cp", default=os.environ.get("JADX_CP", ""))
    ap.add_argument("--java", default=os.environ.get("JAVA", "java"))
    ap.add_argument("--skip-jadx", action="store_true")
    args = ap.parse_args()

    ren = os.path.join(args.work, "ren")
    dec = os.path.join(ren, "dec")
    out_apk = os.path.join(ren, "renamed.apk")
    jadx_out = os.path.join(ren, "jadx_out")
    os.makedirs(ren, exist_ok=True)

    print("[1/6] apktool d")
    shutil.rmtree(dec, ignore_errors=True)
    run([args.java, "-Xmx3g", "-jar", args.apktool, "d", "-f", "-r", "-o", dec, args.apk])

    print("[2/6] dex_rename（描述符级改名）")
    import dex_rename
    pairs = dex_rename.build_pairs()
    total, touched = dex_rename.rename_smali_text(dec, pairs)
    moved = 0
    for name in sorted(os.listdir(dec)):
        if name == "smali" or name.startswith("smali_classes"):
            sub = os.path.join(dec, name)
            if os.path.isdir(sub):
                moved += dex_rename.rename_files(sub, pairs)
    print(f"      descriptor hits = {total} in {touched} files, renamed files = {moved}")
    if total == 0:
        raise SystemExit("改名 0 处 —— 多半是 apk 已经改过名，或映射表对不上")

    print("[3/6] apktool b")
    run([args.java, "-Xmx3g", "-jar", args.apktool, "b", dec, "-o", out_apk])

    if args.skip_jadx:
        print("skip jadx")
        return 0

    print("[4/6] jadx")
    shutil.rmtree(jadx_out, ignore_errors=True)
    run([args.java, "-Xmx3g", "-cp", args.jadx_cp, "jadx.cli.JadxCLI",
         "-d", jadx_out, "--no-res", "-j", "8", out_apk])

    print("[5/6] 导入源码树")
    dst = os.path.join(PROJ, "src", "main", "java")
    shutil.rmtree(dst, ignore_errors=True)
    shutil.copytree(os.path.join(jadx_out, "sources"), dst)

    print("[6/6] 注释与缺陷修复")
    for mod in ("annotate", "fix_jadx_defects"):
        m = __import__(mod)
        m.main()
    print("done")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
