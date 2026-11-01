#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""在 ext4 镜像中批量按内容搜索（限定扩展名与大小），输出命中文件与匹配串

用法: python content_scan.py <image> <正则> [扩展名如 .odex,.jar,.so] [最大MB] [最大深度]
"""
import sys, re
import ext4


def isize(ino):
    try:
        return int(ino.i_size_lo) | (int(ino.i_size_high) << 32)
    except Exception:
        return -1


def kind(ino):
    if isinstance(ino, ext4.Directory):
        return 'd'
    if isinstance(ino, ext4.File):
        return 'f'
    return '?'


def listdir(d):
    for e, ft in d.opendir():
        if e.name_str in ('.', '..'):
            continue
        yield e.name_str, d.volume.inodes[e.inode]


def find_strings(data, rx, minlen=6):
    pat = re.compile((rb'[\x09\x0a\x0d\x20-\x7e]{%d,}' % minlen))
    out = []
    for m in pat.finditer(data):
        s = m.group().decode('latin-1')
        if rx.search(s):
            out.append(s)
    return out


def main():
    vol = ext4.Volume(open(sys.argv[1], 'rb'))
    rx = re.compile(sys.argv[2], re.I)
    exts = tuple((sys.argv[3] if len(sys.argv) > 3 else '.odex,.jar,.so,.apk').split(','))
    maxmb = int(sys.argv[4]) if len(sys.argv) > 4 else 200
    maxdepth = int(sys.argv[5]) if len(sys.argv) > 5 else 8
    nhit = 0

    def walk(d, prefix, depth):
        nonlocal nhit
        if depth > maxdepth:
            return
        try:
            items = list(listdir(d))
        except Exception:
            return
        for name, sub in items:
            p = prefix + '/' + name
            if kind(sub) == 'd':
                walk(sub, p, depth + 1)
                continue
            low = name.lower()
            if not low.endswith(exts):
                continue
            sz = isize(sub)
            if sz > maxmb * 1024 * 1024 or sz <= 0:
                continue
            try:
                data = sub.open().read()
            except Exception as e:
                sys.stderr.write('READFAIL %s %s\n' % (p, e))
                continue
            m = find_strings(data, rx)
            if m:
                nhit += 1
                print('### %s  (%d bytes, %d hits)' % (p, sz, len(m)))
                for s in list(dict.fromkeys(m))[:12]:
                    print('    ' + s[:200])
            del data

    walk(vol.root, '', 0)
    print('--- files with hits: %d ---' % nhit)


if __name__ == '__main__':
    main()
