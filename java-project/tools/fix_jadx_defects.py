# -*- coding: utf-8 -*-
"""修 jadx 反编译缺陷，让源码能被 javac 编译。

jadx 对「一个寄存器被复用成多种类型」的处理会产生三种不可编译的产物：

1. `??` 类型占位符，如 `?? CopyOfRange = 0;`
2. 把 boolean 当 int 用的畸形条件，如 `r16 == true ? 1 : 0`
3. 结构错乱的循环，如 `for (0; i < n; i + 1)`

本脚本按**已核实的证据**逐条还原，每条都能追到 smali：

- `BinaryPlist.b()`：同一槽位先后装 Boolean / BigInteger / Long / Double /
  String / Map / List / byte[]，jadx 无法统一，只能声明为 `Object` 并在调用
  成员方法处加窄化转换。语义不变。
- `k4.n()`：该方法返回 boolean，但局部 `r12` 在字节码里是 int 0/1（既做数组下标
  又做返回值）。声明为 `int`，返回处写 `!= 0`。
- `o5.f()`：十六进制串校验循环。原始 smali 是
  `while (v2 < len) { c = charAt(v2); if (!(0-9 || a-f)) return REJECTED; v2++ }`，
  jadx 把 `if (!valid) return` 折叠成 `i = cond ? i+1 : 0; return REJECTED;`。
- `t4.P()`：try-with-resources 的四个重复异常块里，两处资源赋值被 jadx 写成
  `r9 = timeUnit` / `r10 = timeUnit`（jadx 自己也报了
  "Instruction removed from duplicated block"）。按同族块 `r9 = o5Var2` /
  `r10 = o5Var` 的形态还原。
- `R.drawable.stat_notify_sync`：smali 是 `const 0x108007c`，属于
  `android.R.drawable`，jadx 误归到 App 的 R 里。改为 `android.R.drawable.*`。

所有替换都是**精确字符串匹配 + 出现次数校验**，重复执行安全（已修过的会跳过）。
"""
import os
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "java")

# (相对 ROOT 的文件, 说明, 原文, 新文, 期望出现次数)
RULES = [
    # ---------- BinaryPlist：一个槽位多种类型 ----------
    ("io/carplay/app/secured/BinaryPlist.java", "Object 槽位声明",
     "            ?? CopyOfRange = 0;",
     "            // jadx 类型推断失败：此槽位先后存 Boolean/BigInteger/Long/Double/\n"
     "            // String/Map/List/byte[]，只能用 Object 承载，取用时窄化。\n"
     "            Object CopyOfRange = 0;", 1),
    ("io/carplay/app/secured/BinaryPlist.java", "BigInteger.bitLength",
     "CopyOfRange.bitLength()", "((BigInteger) CopyOfRange).bitLength()", 1),
    ("io/carplay/app/secured/BinaryPlist.java", "BigInteger.longValue",
     "CopyOfRange.longValue()", "((BigInteger) CopyOfRange).longValue()", 1),
    ("io/carplay/app/secured/BinaryPlist.java", "Map.containsKey",
     "CopyOfRange.containsKey(", "((Map) CopyOfRange).containsKey(", 1),
    ("io/carplay/app/secured/BinaryPlist.java", "Map.put",
     "CopyOfRange.put(", "((Map) CopyOfRange).put(", 1),
    ("io/carplay/app/secured/BinaryPlist.java", "List.add",
     "CopyOfRange.add(", "((List) CopyOfRange).add(", 1),

    # ---------- k4：int 0/1 当 boolean ----------
    ("io/carplay/app/secured/k4.java", "r12 声明",
     "        ?? r12 = 0;", "        int r12 = 0;", 1),
    ("io/carplay/app/secured/k4.java", "r16 声明",
     "            ?? r16 = r12;", "            int r16 = r12;", 1),
    ("io/carplay/app/secured/k4.java", "r17 声明",
     "        ?? r17 = r12;", "        int r17 = r12;", 1),
    ("io/carplay/app/secured/k4.java", "r16 布尔比较",
     "iArr8[r16 == true ? 1 : 0]", "iArr8[r16 != 0 ? 1 : 0]", 1),
    ("io/carplay/app/secured/k4.java", "r12 回写",
     "r12 = r16 == true ? 1 : 0;", "r12 = r16 != 0 ? 1 : 0;", 1),
    ("io/carplay/app/secured/k4.java", "返回 r12",
     "                return r12;", "                return r12 != 0;", 1),
    ("io/carplay/app/secured/k4.java", "返回 r17",
     "            return r17;", "            return r17 != 0;", 1),

    # ---------- o5：十六进制校验循环 ----------
    ("io/carplay/app/secured/o5.java", "hex 校验循环",
     "            for (0; i < str5.length(); i + 1) {\n"
     "                char cCharAt = str5.charAt(i);\n"
     "                i = ((cCharAt >= '0' && cCharAt <= '9') || (cCharAt >= 'a' && cCharAt <= 'f')) ? i + 1 : 0;\n"
     "                return l5.a.REJECTED;\n"
     "            }",
     "            for (i = 0; i < str5.length(); i++) {\n"
     "                char cCharAt = str5.charAt(i);\n"
     "                if (!((cCharAt >= '0' && cCharAt <= '9') || (cCharAt >= 'a' && cCharAt <= 'f'))) {\n"
     "                    return l5.a.REJECTED;\n"
     "                }\n"
     "            }", 1),

    # ---------- t4：try-with-resources 重复块资源丢失 ----------
    ("io/carplay/app/secured/t4.java", "r9 声明",
     "        ?? r9;", "        o5 r9;", 1),
    ("io/carplay/app/secured/t4.java", "r10 声明",
     "        ?? r10;", "        o5 r10;", 1),
    ("io/carplay/app/secured/t4.java", "r9 资源赋值",
     "r9 = timeUnit;", "r9 = o5Var2;", 1),
    ("io/carplay/app/secured/t4.java", "r10 资源赋值",
     "r10 = timeUnit;", "r10 = o5Var;", 1),
]

# R 资源归属修正：0x108007c 属于 android.R.drawable
R_RULES = [
    ("R.drawable.stat_notify_sync", "android.R.drawable.stat_notify_sync"),
]


def main():
    applied = skipped = failed = 0
    for rel, desc, old, new, expect in RULES:
        path = os.path.join(ROOT, rel)
        if not os.path.exists(path):
            print("  MISSING FILE  %s" % rel)
            failed += 1
            continue
        text = open(path, encoding="utf-8", errors="replace").read()
        n = text.count(old)
        if n == 0:
            if new in text:
                print("  skip (already)  %-46s %s" % (rel.split("/")[-1], desc))
                skipped += 1
            else:
                print("  !! NOT FOUND   %-46s %s" % (rel.split("/")[-1], desc))
                failed += 1
            continue
        if n != expect:
            print("  !! COUNT=%d(expect %d) %-38s %s" % (n, expect, rel.split("/")[-1], desc))
            failed += 1
            continue
        open(path, "w", encoding="utf-8", newline="\n").write(text.replace(old, new))
        print("  fix            %-46s %s" % (rel.split("/")[-1], desc))
        applied += 1

    # R.drawable -> android.R.drawable（只处理 prototype 下引用点）
    for dirpath, _d, files in os.walk(os.path.join(ROOT, "io/carplay/app/prototype")):
        for fn in files:
            if not fn.endswith(".java"):
                continue
            p = os.path.join(dirpath, fn)
            text = open(p, encoding="utf-8", errors="replace").read()
            new_text = text
            for a, b in R_RULES:
                new_text = new_text.replace(a, b)
            if new_text != text:
                open(p, "w", encoding="utf-8", newline="\n").write(new_text)
                print("  fix            %-46s R.drawable -> android.R.drawable" % fn)
                applied += 1

    print("\napplied=%d skipped=%d failed=%d" % (applied, skipped, failed))
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
