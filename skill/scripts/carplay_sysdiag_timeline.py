#!/usr/bin/env python3
# -*- coding: utf-8 -*-
r"""从 iOS sysdiagnose 抽取「原生无线 CarPlay 连接时间线」。

用途：把 iPhone 侧 carkitd / audiomxd(AirPlay) / wifid / bluetoothd 的日志还原成
与 references/native-carplay-handshake.md 同构的阶段化序列，用来对比我们自己的实现。

依赖（UnifiedLog 的 setup.py 在新 Python 上装不了，直接取源码放 PYTHONPATH）：
    pip install lz4 biplist
    curl -LO https://files.pythonhosted.org/packages/source/U/UnifiedLog/UnifiedLog-0.3.tar.gz
    tar xzf UnifiedLog-0.3.tar.gz
    export PYTHONPATH=$PWD/UnifiedLog-0.3          # Windows: set PYTHONPATH=%cd%\UnifiedLog-0.3

用法：
    python carplay_sysdiag_timeline.py <sysdiagnose目录> [-o timeline.txt]
    python carplay_sysdiag_timeline.py <sysdiagnose目录> --last-seconds 60 --all
    python carplay_sysdiag_timeline.py --tsv decoded.tsv        # 复用已解码结果，秒级迭代

要点：
  * iOS 17+ 的 dsc 是 v2 布局（24 字节 range entry、64 位虚拟偏移），官方 0.3 版按 v1 解析
    会得到垃圾数据 → 消息文本全丢。本脚本在运行时打补丁，不改第三方包。
  * logarchive 挂钟时间存在压缩（实测 1s 归档 ≈ 30s 真实），因此额外抓取日志正文内嵌时间戳
    （"with date …" / "addedAt=" / "( 十月 25, 15:58:21.221 )"）作为真实时间锚点。
"""
import argparse
import glob
import logging
import os
import re
import struct
import sys
from uuid import UUID

CARPLAY_PROCS = {
    'carkitd', 'CarPlay', 'ACCCarPlayService', 'caraccessoryd',
    'CarPlayTemplateUIHost', 'mediasetupd', 'mediaremoted', 'audiomxd',
    'wifid', 'bluetoothd',
}

NOISY = (
    'xpc', 'runningboard', 'baseboard', 'coreanimation', 'icon', 'reuse',
    'activity for state dumps', 'activating connection', 'invalidated because',
    'statedump', 'mxdebugutilities', 'printdebug', 'cmsmdebug',
    'does not have the com.apple.private.carkit.app',
    'cloudpairing got msg', 'cbmsgid', 'pairingagentretrieve',
    # WiFi 自动加入的逐网络刷屏，与 CarPlay 判断无关
    'known network', 'redundant invocation', 'removing redundant',
    'updated location', 'updated associated network', 'already associated',
    'scan channels', 'scan hidden', 'hidden network channels', 'auto-join completed',
    'auto-join: --', 'recent channels', 'remaining channels', 'deferred networks',
    'scan succeeded', 'scan cache results', 'standalone 6ghz', 'low-rssi networks',
    'maxscanchannelcount', 'hidden ssids', 'scheduling auto-join', 'unscheduling auto-join',
)

PHASES = [
    ('A', re.compile(
        r'ACL connected|Running SDP|full SDP|Service search response|Secure-Simple-Pairing|'
        r'pairing started|supports device|is asking to connect|WIAP version|A2DP Sink version', re.I)),
    ('B', re.compile(
        r'is not paired|Ignoring WiFi credentials|approval alert|transport identifiers|'
        r'requesting WiFi credentials|received WiFi credentials|adding network|saved WiFi|'
        r'in-car wifi|setting WiFiManager|CarPlay Control advertising|attempting connection', re.I)),
    ('C', re.compile(
        r'AUTO-JOIN|WiFi Network changed|WiFi eligible|DHCP|192\.168\.', re.I)),
    ('D', re.compile(
        r'CarPlayControl|Bonjour|APEndpointCarPlay|APBrowser|pending autoconnect|command .connect.', re.I)),
    ('E', re.compile(
        r'APTransportConnection|APTransportStream|APSenderSession|APEndpointStream|APKeyHolder|'
        r'SRV resolved|Resolved DNS|AuthenticationSucceeded|AuthorizationSucceeded|'
        r'EndpointActivated|Activated endpoint|isConnected = true|ScreenPixelSize|'
        r'usage mode|HDR|virtual display|PAIR|Request written|Response received', re.I)),
    ('F', re.compile(
        r'disconnected|Disconnected|Endpoint_Failed|StartupFailed|Session Breakdown|'
        r'reconnection time|Dissociated|Server stop', re.I)),
    ('G', re.compile(r'attach|Attach|reconnect|Reconnect|modesChanged|hidSendReport', re.I)),
]

PHASE_NAME = {
    'A': '蓝牙 ACL / SDP / 配对', 'B': 'iAP2 / 授权 / 凭据', 'C': 'WiFi 关联',
    'D': 'CarPlay Control / Bonjour', 'E': 'AirPlay 建链 / 认证', 'F': '断开',
    'G': '自动重连',
}

EMBEDDED_DATE = [
    re.compile(r'with date\s+\w+\s+(\w+\s+\d+\s+\d{2}:\d{2}:\d{2})\s+(\d{4})'),
    re.compile(r'\(\s*\S+\s+\d+,\s*(\d{2}:\d{2}:\d{2}\.\d+)\s*\)'),
    re.compile(r'addedAt=(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d+)'),
]


# ---------------------------------------------------------------- tracev3 补丁
def patch_unifiedlog():
    from UnifiedLog import dsc_file, tracev3_file

    def parse_dsc_v2(self, fobj):
        head = fobj.read(16)
        if head[0:4] != b'hcsd':
            return False
        major, minor, n_range, n_uuid = struct.unpack('<HHII', head[4:16])
        self._format_version = '{}.{}'.format(major, minor)
        esize = 24 if major >= 2 else 16
        for _ in range(n_range):
            raw = fobj.read(esize)
            if len(raw) < esize:
                break
            if major >= 2:
                v_off, data_off, data_len, uuid_idx, _pad = struct.unpack('<QIIII', raw)
            else:
                uuid_idx, v_off, data_off, data_len = struct.unpack('<IIII', raw)
            self.range_entries.append([uuid_idx, v_off, data_off, data_len])
        pos = fobj.tell()
        usize = 32 if major >= 2 else 28
        for _ in range(n_uuid):
            fobj.seek(pos)
            raw = fobj.read(usize)
            if len(raw) < usize:
                break
            pos += usize
            if major >= 2:
                v_off, size = struct.unpack('<QI', raw[:12])
                uuid_obj = UUID(bytes=raw[12:28])
                data_off = struct.unpack('<I', raw[28:])[0]
            else:
                v_off, size = struct.unpack('<II', raw[:8])
                uuid_obj = UUID(bytes=raw[8:24])
                data_off = struct.unpack('<I', raw[24:])[0]
            fobj.seek(data_off)
            path = self._ReadCString(fobj.read(1024))
            self.uuid_entries.append([v_off, size, uuid_obj, path, os.path.basename(path)])
        return True

    dsc_file.Dsc._ParseFileObject = parse_dsc_v2

    def find_offset(self, v_offset):
        for a in self.range_entries:
            if a[1] <= v_offset < a[1] + a[3]:
                ue = self.uuid_entries[a[0]] if 0 <= a[0] < len(self.uuid_entries) else None
                return a, ue
        return None, None

    dsc_file.Dsc.FindVirtualOffsetEntries = find_offset

    def linear_ts(self, items, ct):
        lst = items or []
        if not lst:
            return None
        if getattr(self, '_fixed_ts', None) is None:
            self._fixed_ts = max(lst, key=lambda t: t.continuousTime)
        return self._fixed_ts

    tracev3_file.TraceV3._FindClosestTimesyncItemInList = linear_ts

    orig_parse = tracev3_file.TraceV3.Parse

    def safe_parse(self, log_list_process_func=None):
        try:
            return orig_parse(self, log_list_process_func)
        except Exception:
            return True

    tracev3_file.TraceV3.Parse = safe_parse


# ---------------------------------------------------------------- 数据来源
def keep_row(row):
    _, _, proc, _, _, msg = row
    if proc not in CARPLAY_PROCS:
        return False
    low = msg.lower()
    if any(n in low for n in NOISY):
        return False
    if proc in ('wifid', 'bluetoothd') and not re.search(
            r'myBuick|10:E8:A7|carplay|AUTO-JOIN|ACL|SDP|pairing|WiAP', msg, re.I):
        return False
    return True


def rows_from_tracev3(logarchive, last_n):
    from UnifiedLog import Lib, tracev3_file, virtual_file, virtual_file_system

    vfs = virtual_file_system.VirtualFileSystem(virtual_file.VirtualFile)
    caches = Lib.CachedFiles(vfs)
    caches.ParseFolder(logarchive)
    ts = []
    Lib.ReadTimesyncFolder(os.path.join(logarchive, 'timesync'), ts, vfs)

    targets = sorted(glob.glob(os.path.join(logarchive, 'Persist', '*.tracev3')))
    if last_n and len(targets) > last_n:
        print('只解析最新 {} 个 Persist 分片（--all-files 可全量）'.format(last_n), file=sys.stderr)
        targets = targets[-last_n:]
    live = os.path.join(logarchive, 'logdata.LiveData.tracev3')
    if os.path.exists(live):
        targets.append(live)

    rows, seen = [], set()
    for path in targets:
        if os.path.getsize(path) == 0:
            continue
        holder = []
        tv = tracev3_file.TraceV3(vfs, virtual_file.VirtualFile(path, 'traceV3'), ts, logarchive, caches)
        tv.Parse(log_list_process_func=lambda logs, t, h=holder: h.extend(logs))
        for l in holder:
            try:
                ct, tns, proc, sub, cat, msg = int(l[2]), int(l[3]), l[11], l[13], l[14], l[22]
            except (IndexError, TypeError, ValueError):
                continue
            row = (ct, tns / 1e9, proc, sub, cat, msg)
            if not keep_row(row):
                continue
            key = (ct, proc, msg[:120])
            if key in seen:
                continue
            seen.add(key)
            rows.append(row)
    rows.sort(key=lambda r: r[0])
    return rows


def rows_from_tsv(path):
    rows = []
    with open(path, encoding='utf-8', errors='replace') as fh:
        fh.readline()
        for line in fh:
            p = line.rstrip('\n').split('\t')
            if len(p) < 13:
                continue
            try:
                hh, mm, ss = p[2].split(':')
                tsec = int(hh) * 3600 + int(mm) * 60 + float(ss)
                row = (int(p[1]), tsec, p[7], p[9], p[10], p[12])
            except ValueError:
                continue
            if keep_row(row):
                rows.append(row)
    rows.sort(key=lambda r: r[0])
    return rows


# ---------------------------------------------------------------- 输出
def phase_of(msg):
    for tag, rx in PHASES:
        if rx.search(msg):
            return tag
    return None


def embedded_time(msg):
    for rx in EMBEDDED_DATE:
        m = rx.search(msg)
        if m:
            g = m.groups()
            return ' '.join(g) if len(g) > 1 else g[0]
    return ''


TZ_OFFSET = 8 * 3600          # sysdiagnose 取自 +0800，时间戳统一按东八区显示


def arch(tsec):
    """归一化到「当日秒数」后输出 HH:MM:SS.mmm。

    tracev3 路径给的是 **epoch 秒**（如 1.79e9），TSV 路径给的是当日秒数（< 86400），
    两者用 2 天作为阈值区分。
    """
    if tsec > 86400 * 2:
        tsec = (tsec + TZ_OFFSET) % 86400
    return '{:02d}:{:02d}:{:06.3f}'.format(int(tsec // 3600), int(tsec % 3600 // 60), tsec % 60)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('sysdiagnose', nargs='?', help='sysdiagnose 目录（含 system_logs.logarchive）')
    ap.add_argument('--tsv', help='复用已解码的 TSV（13 列），跳过 tracev3 解析')
    ap.add_argument('-o', '--out')
    ap.add_argument('--all', action='store_true', help='输出全部匹配行（默认按阶段去重）')
    ap.add_argument('-n', '--last', type=int, default=4, help='只解析最新 N 个 Persist 分片（默认 4）')
    ap.add_argument('--all-files', action='store_true', help='解析全部 Persist 分片（慢）')
    ap.add_argument('--last-seconds', type=int, default=12,
                    help='只保留归档时间最后 N 秒（默认 12，刚好覆盖连接窗口；0=不限）')
    ap.add_argument('--since', help='起始归档时间 HH:MM:SS')
    ap.add_argument('--until', help='结束归档时间 HH:MM:SS')
    args = ap.parse_args()

    logging.basicConfig(level=logging.CRITICAL)

    if args.tsv:
        rows = rows_from_tsv(args.tsv)
    else:
        if not args.sysdiagnose:
            ap.error('需要 sysdiagnose 目录，或使用 --tsv')
        logarchive = os.path.join(args.sysdiagnose, 'system_logs.logarchive')
        if not os.path.isdir(logarchive):
            sys.exit('未找到 system_logs.logarchive：{}'.format(logarchive))
        patch_unifiedlog()
        rows = rows_from_tracev3(logarchive, 0 if args.all_files else args.last)

    if not rows:
        sys.exit('未解析到 CarPlay 相关日志（iOS 版本可能不受支持）')

    if args.last_seconds > 0:
        hi = max(r[1] for r in rows)
        rows = [r for r in rows if r[1] >= hi - args.last_seconds]
    if args.since:
        rows = [r for r in rows if arch(r[1]) >= args.since]
    if args.until:
        rows = [r for r in rows if arch(r[1]) <= args.until]

    out = open(args.out, 'w', encoding='utf-8') if args.out else sys.stdout
    out.write('# 归档时间\t内嵌真实时间\t进程\t阶段\t消息\n')
    shown, seen_norm = 0, set()
    for ct, tsec, proc, sub, cat, msg in rows:
        tag = phase_of(msg)
        if not tag:
            continue
        norm = re.sub(r'0x[0-9a-fA-F]+|\b\d+(\.\d+)?\b', '#', msg)[:110]
        if not args.all and (tag, norm) in seen_norm:
            continue
        seen_norm.add((tag, norm))
        out.write('{}\t{}\t{}\t[{}] {}\t{}\n'.format(
            arch(tsec), embedded_time(msg) or '-', proc, tag, cat, msg[:220]))
        shown += 1

    joined = '\n'.join(r[5] for r in rows)
    out.write('\n# ===== 汇总 =====\n')
    out.write('# CarPlay 相关日志 {} 条，按阶段去重后 {} 行\n'.format(len(rows), shown))
    ports = sorted({int(x) for x in re.findall(r'192\.168\.\d+\.\d+:(\d{3,5})', joined)
                    if int(x) < 10000})
    out.write('# 车机侧端口: {}\n'.format(', '.join(str(p) for p in ports) or '未提取'))
    disp = re.findall(r'(\d+\.\d+) x (\d+\.\d+)', joined)
    if disp:
        out.write('# 屏幕分辨率: {} x {}\n'.format(disp[0][0], disp[0][1]))
    for m in re.finditer(r'([A-Za-z\- ]+?reconnection time)[:：]\s*([0-9.]+)', joined):
        out.write('# {}: {}\n'.format(m.group(1).strip(), m.group(2)))
    for tag in sorted(PHASE_NAME):
        out.write('# 阶段 {} = {}\n'.format(tag, PHASE_NAME[tag]))
    if args.out:
        out.close()
        print('written {} lines -> {}'.format(shown, args.out))
    print('done: {} 条 CarPlay 相关日志'.format(len(rows)))


if __name__ == '__main__':
    main()
