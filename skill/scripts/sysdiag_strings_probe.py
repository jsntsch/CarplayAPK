#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""直读 iOS 16/17 的 tracev3，按关键字取证（不依赖 UnifiedLog）。

为什么需要它
------------
`UnifiedLog 0.3` 对 iOS 17 的归档会解析出 **0 行**：`0x600d` 数据块里的条目改用了
`0x6004` 类型（proc 名与消息内联，不再走 catalog 引用）。旧库遇到未知 tag 后对齐错乱，
把整块都丢掉，且异常被内部吞掉（`Parse()` 仍返回 True）。

本脚本不还原结构：直接按 `bv41` 头解压每个数据块，再扫可打印字符串。
实测 iPhone13,2 / iOS 17.7.1 的一份 564 MB 归档：5548 个块 → 329 万条字符串，
其中 CarPlay 相关 9670 条 —— 足够回答「车机发了什么命令」「端口是多少」「有没有发 X」。

用法
----
    # 1) 全量导出（约 150 MB，30 s 左右）
    python sysdiag_strings_probe.py <sysdiagnose目录> -o strings.txt

    # 2) 只留关心进程（按 proc 名切分消息，输出 proc<TAB>msg）
    python sysdiag_strings_probe.py <sysdiagnose目录> -o cp.txt \
        --procs carkitd audiomxd bluetoothd wifid

    # 3) 关键字过滤（正则，可多次给）
    python sysdiag_strings_probe.py <sysdiagnose目录> -o hit.txt \
        --grep "hidSendReport|hidSetInputMode|/command|keepAliveType"

    # 4) 从已导出的全量文件二次过滤，不重复解压
    python sysdiag_strings_probe.py --from strings.txt -o hit.txt --grep "carplay-ctrl"

要点
----
  * 数据块头是 `bv41` + u32 未压缩长度 + u32 压缩长度，压缩体从偏移 12 开始；
    必须按压缩长度切片，多给 4 字节 padding 会让 lz4 报 14289。
  * 消息文本在 iOS 17 上多数已内联成形（值已代入），所以关键字过滤能直接拿到
    `Received at 15:59:07.797909` 这种带值的结果。
  * 不做时间戳还原：需要墙钟时直接 grep 消息里的内嵌时间。
"""
import argparse
import glob
import io
import os
import re
import struct
import sys

try:
    import lz4.block
except ImportError:
    sys.exit('需要 lz4：pip install lz4')

STR_RX = re.compile(rb'[ -~]{8,}')
DEFAULT_PROCS = ('carkitd', 'audiomxd', 'bluetoothd', 'wifid', 'CarPlay',
                 'ACCCarPlayService', 'caraccessoryd', 'mediasetupd')


def iter_chunks(path):
    """按 16 字节块头遍历 tracev3，产出 (tag, body)。"""
    try:
        data = open(path, 'rb').read()
    except OSError:
        return
    if len(data) < 16:
        return
    tag, _sub, length = struct.unpack('<IIQ', data[:16])
    if tag != 0x1000:
        return
    pos = 16 + length
    if length % 8:
        pos += 8 - length % 8
    while pos + 16 <= len(data):
        tag, _sub, length = struct.unpack('<IIQ', data[pos:pos + 16])
        yield tag, data[pos + 16:pos + 16 + length]
        step = length + (8 - length % 8 if length % 8 else 0)
        pos += 16 + step


def strings_of(logarchive):
    """产出 (块序号, 字符串)。只处理 0x600d + bv41 的压缩数据块。"""
    files = sorted(glob.glob(os.path.join(logarchive, 'Persist', '*.tracev3')))
    live = os.path.join(logarchive, 'logdata.LiveData.tracev3')
    if os.path.exists(live):
        files.append(live)
    idx = 0
    for path in files:
        for tag, body in iter_chunks(path):
            if tag != 0x600d or not body.startswith(b'bv41'):
                continue
            usize, csize = struct.unpack('<II', body[4:12])
            try:
                raw = lz4.block.decompress(body[12:12 + csize], uncompressed_size=usize)
            except Exception:
                continue
            idx += 1
            for m in STR_RX.finditer(raw):
                yield idx, m.group().decode('latin-1')


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('sysdiagnose', nargs='?', help='sysdiagnose 目录（含 system_logs.logarchive）')
    ap.add_argument('--from', dest='src', help='复用已导出的全量字符串文件')
    ap.add_argument('-o', '--out', help='输出文件（默认 stdout）')
    ap.add_argument('--procs', nargs='*', default=None,
                    help='只保留这些进程的消息（proc<TAB>msg）；不给则输出全部字符串')
    ap.add_argument('--grep', action='append', default=None, help='正则过滤，可多次给')
    args = ap.parse_args()

    if not args.src and not args.sysdiagnose:
        ap.error('需要 sysdiagnose 目录，或 --from 指向已导出文件')

    if args.src:
        def stream():
            with io.open(args.src, encoding='utf-8', errors='replace') as fh:
                for line in fh:
                    yield 0, line.rstrip('\n')
        it = stream()
    else:
        logarchive = os.path.join(args.sysdiagnose, 'system_logs.logarchive')
        if not os.path.isdir(logarchive):
            sys.exit('未找到 system_logs.logarchive：{}'.format(logarchive))
        it = strings_of(logarchive)

    procs = set(args.procs) if args.procs is not None else None
    patterns = [re.compile(p, re.I) for p in (args.grep or [])]
    out = io.open(args.out, 'w', encoding='utf-8', newline='\n') if args.out else sys.stdout

    last_proc = None
    n = 0
    for _idx, s in it:
        if procs is not None:
            if s in procs:
                last_proc = s
                continue
            if last_proc is None:
                continue
            line = '{}\t{}'.format(last_proc, s)
        else:
            line = s
        if patterns and not any(p.search(line) for p in patterns):
            continue
        out.write(line + '\n')
        n += 1

    if out is not sys.stdout:
        out.close()
    print('lines {}'.format(n), file=sys.stderr)


if __name__ == '__main__':
    main()
