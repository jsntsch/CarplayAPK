#!/usr/bin/env bash
# verify_dex_outs.sh —— 改完 smali 后必跑的硬校验
#
# 背景（V1.3.4 踩的坑）：
#   dex 方法头的 outs_size 由「该方法里最大的一次 invoke 参数数」决定，
#   但这个值在 apktool/smali 重新汇编时**不会**因为新插入的多参数 invoke 而扩大。
#   在 W()（原最大 5 字 invoke、outs=5）里插一条 7 字 invoke：
#     -> 编译 100% 通过
#     -> `cmd package compile -m verify` 报 0 错
#     -> 一进会话立刻 java.lang.VerifyError: invalid argument count (7) exceeds outsSize (5)
#
# 用法：
#   bash verify_dex_outs.sh Carplay-V1.3.5.apk
#   bash verify_dex_outs.sh Carplay-V1.3.5.apk "io/carplay/app/secured/i0"   # 只查指定类
#
# 退出码：0 全部通过；1 存在 outs 不足（必须修）；2 环境不满足

set -u

APK="${1:-}"
CLASS_FILTER="${2:-}"

if [ -z "$APK" ] || [ ! -f "$APK" ]; then
    echo "用法: bash $0 <apk> [类名过滤，如 io/carplay/app/secured/i0]"
    exit 2
fi

BT="${ANDROID_BUILD_TOOLS:-$HOME/AppData/Local/Android/Sdk/build-tools/37.0.0}"
DEXDUMP="$BT/dexdump.exe"
[ -f "$DEXDUMP" ] || DEXDUMP="$BT/dexdump"
if [ ! -x "$DEXDUMP" ]; then
    echo "!! 找不到 dexdump（试过 $BT）"
    echo "   设置 ANDROID_BUILD_TOOLS 指向 build-tools 目录"
    exit 2
fi

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

echo "==> 解包 $APK"
unzip -o -q "$APK" 'classes*.dex' -d "$WORK" || { echo "!! 解包失败"; exit 2; }

TOTAL=0
BAD=0

for dex in "$WORK"/classes*.dex; do
    name=$(basename "$dex")
    echo "==> dexdump $name"
    "$DEXDUMP" -d "$dex" > "$WORK/$name.txt" 2>/dev/null || {
        echo "!! dexdump 失败: $name"; exit 2; }

    RES=$(DEX="$WORK/$name.txt" FILTER="$CLASS_FILTER" DEXNAME="$name" python - <<'PY'
import io, os, re

path = os.environ['DEX']
flt = os.environ.get('FILTER', '')
lines = io.open(path, encoding='utf-8', errors='replace').read().split('\n')

cur_cls = ''
cur_m = ''
outs = None
bad = []
checked = 0

inv_re = re.compile(r'invoke-\S+\s+\{([^}]*)\}')

for l in lines:
    m = re.search(r"Class descriptor\s*:\s*'(L[^']+)'", l)
    if m:
        cur_cls = m.group(1)
        continue
    m = re.search(r"name\s+:\s*'([^']*)'", l)
    if m:
        cur_m = m.group(1)
        outs = None
        continue
    s = l.strip()
    if s.startswith('outs'):
        try:
            outs = int(s.split(':')[-1].strip())
        except Exception:
            pass
        continue
    mi = inv_re.search(l)
    if mi and outs is not None:
        if flt and flt not in cur_cls:
            continue
        n = len([x for x in mi.group(1).split(',') if x.strip()])
        checked += 1
        if n > outs:
            bad.append((cur_cls, cur_m, n, outs))

if bad:
    print('BAD %d %d' % (len(bad), checked))
    for c, m, n, o in bad[:30]:
        print('   %s->%s(): invoke 用 %d 字，outs 只有 %d' % (c, m, n, o))
else:
    print('OK %d' % checked)
PY
)
    echo "$RES" | grep -v '^BAD\|^OK' || true
    case "$RES" in
        OK*)  echo "    invoke 检查: $(echo "$RES" | head -1 | awk '{print $2}') 条，全部通过" ;;
        BAD*) echo "    !!! outs 不足"; BAD=$((BAD+1)) ;;
    esac
    n=$(echo "$RES" | head -1 | awk '{print $2}')
    TOTAL=$((TOTAL + ${n:-0}))
done

echo
if [ "$BAD" -gt 0 ]; then
    echo "==> 结论：存在 outs 不足，装上去必闪退（VerifyError），先修 smali"
    exit 1
fi
echo "==> 结论：共检查 $TOTAL 条 invoke，没有一条超过所属方法的 outs_size"
echo "    可以安装"
exit 0
