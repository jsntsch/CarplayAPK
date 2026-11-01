#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""批量扫描 ext4 镜像内所有 APK 的 AndroidManifest.xml，按关键词定位组件

用法: python scan_manifests.py <image> [关键词正则]
"""
import sys, re, io, zipfile
import ext4

KW = re.compile(sys.argv[2] if len(sys.argv) > 2 else 'carplay|airplay|iap2|projection', re.I)


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


def axml_strings(data):
    """从二进制 AXML 提取字符串池内容（UTF-8 / UTF-16 两种编码）"""
    out = []
    try:
        if len(data) < 8 or data[:2] != b'\x03\x00':
            return out
        # 遍历 chunk，找 RES_STRING_POOL_TYPE(0x0001)
        off = 8
        while off + 8 <= len(data):
            ctype = int.from_bytes(data[off:off + 2], 'little')
            csize = int.from_bytes(data[off + 4:off + 8], 'little')
            if csize <= 0 or off + csize > len(data):
                break
            if ctype == 0x0001:
                hdr = int.from_bytes(data[off + 2:off + 4], 'little')
                strcount = int.from_bytes(data[off + 8:off + 12], 'little')
                flags = int.from_bytes(data[off + 16:off + 20], 'little')
                utf8 = bool(flags & 0x100)
                p = off + hdr
                offsets = [int.from_bytes(data[p + i * 4:p + i * 4 + 4], 'little') for i in range(strcount)]
                base = p + strcount * 4
                for so in offsets:
                    q = base + so
                    if q >= off + csize:
                        continue
                    try:
                        if utf8:
                            # 两段变长长度（UTF-16 长度 + UTF-8 长度）
                            n = data[q]
                            q += 2 if n & 0x80 else 1
                            n = data[q]
                            if n & 0x80:
                                n = ((n & 0x7f) << 8) | data[q + 1]
                                q += 2
                            else:
                                q += 1
                            s = data[q:q + n].decode('utf-8', 'replace')
                        else:
                            n = int.from_bytes(data[q:q + 2], 'little')
                            q += 2
                            if n & 0x8000:
                                n = ((n & 0x7fff) << 16) | int.from_bytes(data[q:q + 2], 'little')
                                q += 2
                            s = data[q:q + n * 2].decode('utf-16le', 'replace')
                        out.append(s)
                    except Exception:
                        pass
                return out
            off += csize
    except Exception:
        pass
    return out


def main():
    vol = ext4.Volume(open(sys.argv[1], 'rb'))
    hits = []

    def walk(d, prefix, depth):
        if depth > 6:
            return
        try:
            items = list(listdir(d))
        except Exception:
            return
        for name, sub in items:
            p = prefix + '/' + name
            if kind(sub) == 'd':
                walk(sub, p, depth + 1)
            elif name.lower().endswith('.apk'):
                try:
                    data = sub.open().read()
                except Exception as e:
                    print('READFAIL', p, e)
                    continue
                try:
                    z = zipfile.ZipFile(io.BytesIO(data))
                    man = z.read('AndroidManifest.xml')
                except Exception as e:
                    print('ZIPFAIL', p, e)
                    continue
                ss = axml_strings(man)
                m = [s for s in ss if KW.search(s)]
                if m:
                    hits.append((p, m))

    walk(vol.root, '', 0)
    for p, m in hits:
        print('### ' + p)
        for s in dict.fromkeys(m):
            print('    ' + s)
    print('--- apks with hits: %d ---' % len(hits))


if __name__ == '__main__':
    main()
