#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""strings 替代工具：提取 ASCII / UTF-16LE 字符串
用法: python strings.py <file> [-n 4] [--utf16] [--grep 正则]
"""
import sys, re, argparse


def iter_strings(data, minlen=4, utf16=False):
    if utf16:
        pat = re.compile((rb'(?:[\x20-\x7e]\x00){%d,}' % minlen))
        for m in pat.finditer(data):
            yield m.group().decode('utf-16le', 'replace')
        return
    pat = re.compile((rb'[\x09\x0a\x0d\x20-\x7e]{%d,}' % minlen))
    for m in pat.finditer(data):
        yield m.group().decode('latin-1')


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('file')
    ap.add_argument('-n', type=int, default=4)
    ap.add_argument('--utf16', action='store_true')
    ap.add_argument('--both', action='store_true', help='ASCII + UTF16')
    ap.add_argument('--grep', default=None)
    ap.add_argument('--sort', action='store_true')
    ap.add_argument('--count', type=int, default=0)
    a = ap.parse_args()
    data = open(a.file, 'rb').read()
    out = []
    out.extend(iter_strings(data, a.n, False))
    if a.utf16 or a.both:
        out.extend(iter_strings(data, a.n, True))
    if a.grep:
        rx = re.compile(a.grep, re.I)
        out = [s for s in out if rx.search(s)]
    if a.sort:
        out = sorted(set(out))
    if a.count:
        out = out[:a.count]
    sys.stdout.write('\n'.join(out) + ('\n' if out else ''))


if __name__ == '__main__':
    main()
