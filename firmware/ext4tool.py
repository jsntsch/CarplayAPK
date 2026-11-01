#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""ext4 镜像目录遍历 / 文件提取工具 (纯 Python, 无需 root / 挂载)

用法:
  python ext4tool.py ls   <image> [子路径]          列出目录
  python ext4tool.py find <image> <正则> [最大深度] 全盘按名/路径搜索
  python ext4tool.py cat  <image> <路径>            输出文件到 stdout
  python ext4tool.py get  <image> <路径> <输出>     提取文件或整个目录到本地
"""
import sys, os, re
import ext4


def open_vol(path):
    return ext4.Volume(open(path, 'rb'))


def kind(ino):
    if isinstance(ino, ext4.Directory):
        return 'd'
    if isinstance(ino, ext4.File):
        return 'f'
    if isinstance(ino, ext4.SymbolicLink):
        return 'l'
    return '?'


def isize(ino):
    try:
        return int(ino.i_size_lo) | (int(ino.i_size_high) << 32)
    except Exception:
        try:
            return int(ino.size)
        except Exception:
            return -1


def listdir(dirino):
    """yield (name, inode)"""
    for d, ft in dirino.opendir():
        name = d.name_str
        if name in ('.', '..'):
            continue
        yield name, dirino.volume.inodes[d.inode]


def resolve(vol, path):
    return vol.inode_at(path)


def read_file(ino):
    f = ino.open()
    try:
        return f.read()
    finally:
        try:
            f.close()
        except Exception:
            pass


def cmd_ls(args):
    vol = open_vol(args[0])
    p = args[1] if len(args) > 1 else '/'
    ino = resolve(vol, p)
    rows = []
    for name, sub in listdir(ino):
        try:
            size = isize(sub)
        except Exception:
            size = -1
        rows.append((kind(sub), size, name))
    for t, s, n in sorted(rows, key=lambda x: (x[0] != 'd', x[2])):
        print('%s %12d  %s' % (t, s, n))


def cmd_find(args):
    vol = open_vol(args[0])
    pat = re.compile(args[1], re.I)
    maxd = int(args[2]) if len(args) > 2 else 99
    hits = []

    def walk(dirino, prefix, depth):
        if depth > maxd:
            return
        try:
            items = list(listdir(dirino))
        except Exception as e:
            sys.stderr.write('ERR list %s: %s\n' % (prefix, e))
            return
        for name, sub in items:
            path = prefix + '/' + name
            if pat.search(name) or pat.search(path):
                try:
                    size = isize(sub)
                except Exception:
                    size = -1
                hits.append((kind(sub), size, path))
            if kind(sub) == 'd':
                walk(sub, path, depth + 1)

    walk(vol.root, '', 0)
    for t, s, p in sorted(hits, key=lambda x: x[2]):
        print('%s %12d  %s' % (t, s, p))
    sys.stderr.write('total %d hits\n' % len(hits))


def cmd_cat(args):
    vol = open_vol(args[0])
    sys.stdout.buffer.write(read_file(resolve(vol, args[1])))


def cmd_get(args):
    vol = open_vol(args[0])
    src, dst = args[1], args[2]
    ino = resolve(vol, src)
    if kind(ino) != 'd':
        data = read_file(ino)
        os.makedirs(os.path.dirname(os.path.abspath(dst)) or '.', exist_ok=True)
        with open(dst, 'wb') as o:
            o.write(data)
        print('saved %d bytes -> %s' % (len(data), dst))
        return 0

    n = 0
    for name, sub in listdir(ino):
        sub_src = src.rstrip('/') + '/' + name
        sub_dst = dst.rstrip('/') + '/' + name
        if kind(sub) == 'd':
            n += cmd_get([args[0], sub_src, sub_dst])
        else:
            try:
                data = read_file(sub)
            except Exception as ex:
                sys.stderr.write('fail %s: %s\n' % (sub_src, ex))
                continue
            os.makedirs(os.path.dirname(os.path.abspath(sub_dst)) or '.', exist_ok=True)
            with open(sub_dst, 'wb') as o:
                o.write(data)
            n += 1
    return n


if __name__ == '__main__':
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(1)
    cmd = sys.argv[1]
    fn = {'ls': cmd_ls, 'find': cmd_find, 'cat': cmd_cat, 'get': cmd_get}.get(cmd)
    if not fn:
        print(__doc__)
        sys.exit(1)
    fn(sys.argv[2:])
