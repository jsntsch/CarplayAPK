#!/usr/bin/env bash
# verify_built_apk.sh —— 构建产物核验 / verify a built APK
#
# 用途 / Purpose
#   一次把「装的是哪版、签名对不对、能不能调试、dex 里有没有本次改动」四件事查完。
#   Checks identity, signature, debuggability and whether the change actually made it into the dex.
#
# 用法 / Usage
#   bash verify_built_apk.sh [apk 路径]
#   bash verify_built_apk.sh dist/Carplay-V1.7.61-debug.apk "HID_AMBIGUOUS_CONTACT_ID"
#
# 第二个参数是可选的「dex 锚点」：本次改动引入的字符串常量或方法名，用来证明产物不是旧的。
# 注意：javac 未开 -g，局部变量名不会进 dex，不能用变量名当锚点。
# The optional second argument is a dex anchor (a symbol your change introduced) proving freshness.
#
# 依赖 / Requires: aapt2, apksigner, dexdump, unzip
set -euo pipefail

APK="${1:-}"
ANCHOR="${2:-}"

if [ -z "$APK" ] || [ ! -f "$APK" ]; then
  echo "用法: bash verify_built_apk.sh <apk> [dex 锚点]" >&2
  exit 2
fi

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/AppData/Local/Android/Sdk}}"

# 选「确实存在该工具」的最高版本 build-tools：不同版本装的工具集不完全一样
# pick the newest build-tools that actually ships the tool (tool sets differ per version)
pick_tool() {
  local name="$1" d c
  for d in $(ls -d "$SDK"/build-tools/* 2>/dev/null | sort -V -r); do
    for c in "$d/$name" "$d/$name.exe" "$d/$name.bat"; do
      [ -f "$c" ] && { printf '%s\n' "$c"; return 0; }
    done
  done
  return 1
}

AAPT2="$(pick_tool aapt2 || true)"
APKSIGNER="$(pick_tool apksigner || true)"
DEXDUMP="$(pick_tool dexdump || true)"
[ -n "$AAPT2" ] || { echo "找不到 aapt2，请设置 ANDROID_HOME" >&2; exit 3; }

fail=0
ok()   { printf '  \033[32mOK\033[0m   %s\n' "$1"; }
bad()  { printf '  \033[31mFAIL\033[0m %s\n' "$1"; fail=1; }
info() { printf '  ..   %s\n' "$1"; }

echo "== 1. 文件 / file =="
printf '  %s  (%s bytes)\n' "$APK" "$(wc -c < "$APK" | tr -d ' ')"
unzip -t "$APK" >/dev/null 2>&1 && ok "zip 结构完整 / zip intact" || bad "zip 结构损坏 / zip corrupted"

echo "== 2. 清单 / manifest =="
BADGE="$("$AAPT2" dump badging "$APK" 2>/dev/null | head -1)"
echo "  $BADGE"
PKG="$(printf '%s' "$BADGE" | sed -n "s/.*package: name='\([^']*\)'.*/\1/p")"
VNAME="$(printf '%s' "$BADGE" | sed -n "s/.*versionName='\([^']*\)'.*/\1/p")"
VCODE="$(printf '%s' "$BADGE" | sed -n "s/.*versionCode='\([^']*\)'.*/\1/p")"
[ -n "$PKG" ] && ok "包名 / package = $PKG" || bad "读不到包名 / cannot read package"
info "versionName=$VNAME  versionCode=$VCODE"

# 文件名与 versionName 必须一致，否则现场分不清装的是哪版
BASE="$(basename "$APK" .apk)"
case "$BASE" in
  *"$VNAME"*) ok "文件名与 versionName 一致 / name matches version" ;;
  *)          bad "文件名与 versionName 不一致 / name mismatch: $BASE vs $VNAME" ;;
esac

echo "== 3. 可调试标志 / debuggable =="
# 变体预期：-debug 应可调试，-release 应不可调试，其余只报告不判定
DBG="false"
"$AAPT2" dump badging "$APK" 2>/dev/null | grep -q "application-debuggable" && DBG="true"
case "$BASE" in
  *-debug)   [ "$DBG" = "true" ]  && ok "debuggable=true（run-as 可读日志）/ run-as will work" \
                                   || bad "-debug 包却不可调试 / debug variant is not debuggable" ;;
  *-release) [ "$DBG" = "false" ] && ok "debuggable=false（发布变体，run-as 不可用）/ release variant is not debuggable" \
                                   || bad "-release 包仍可调试 / release variant is still debuggable" ;;
  *)         info "debuggable=$DBG（未从文件名判定变体）/ variant not inferred from name" ;;
esac

echo "== 4. 签名 / signature =="
if "$APKSIGNER" verify --print-certs "$APK" >/tmp/_sig.txt 2>&1; then
  ok "签名校验通过 / signature valid"
  grep -m1 "Signer #1 certificate DN" /tmp/_sig.txt | sed 's/^/  /' || true
else
  bad "签名校验失败 / signature invalid"
  sed -n '1,5p' /tmp/_sig.txt
fi
rm -f /tmp/_sig.txt

echo "== 5. dex 新鲜度 / dex freshness =="
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
unzip -o -q "$APK" "classes*.dex" -d "$TMP" 2>/dev/null || bad "解不出 classes.dex"
if [ -n "$ANCHOR" ]; then
  # 多 dex 工程：锚点可能落在任意 classesN.dex，必须全部扫
  # multi-dex: scan every classesN.dex
  n=0
  for d in "$TMP"/classes*.dex; do
    c=$("$DEXDUMP" -d "$d" 2>/dev/null | grep -c -- "$ANCHOR" || true)
    n=$((n + c))
  done
  if [ "${n:-0}" -gt 0 ]; then
    ok "dex 锚点命中 $n 次：$ANCHOR"
  else
    bad "dex 里找不到锚点：$ANCHOR —— 产物可能是旧的 / stale artifact"
  fi
else
  info "未提供锚点；建议传入字符串常量或方法名 / pass a string constant or method name"
fi
ls -l "$TMP"/classes*.dex | sed 's/^/  /'

echo
if [ "$fail" -eq 0 ]; then
  echo "结论 / verdict: PASS —— 可以安装验证 / ready to install"
else
  echo "结论 / verdict: FAIL —— 先修正上面标 FAIL 的项 / fix the FAIL items first"
fi
exit "$fail"
