#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
xr.py — 找 .rodata 字符串的 RIP-relative 引用点，并反汇编周边函数体
      用于确认 Cinemo AirPlay 事件通道 /command 的 params key 结构

用法:
  python xr.py <elf> <string> [--ctx 160] [--max 6]
"""
import sys
import re
from elftools.elf.elffile import ELFFile
from capstone import Cs, CS_ARCH_X86, CS_MODE_64, CS_OP_IMM, CS_OP_MEM


def main():
    if len(sys.argv) < 3:
        print(__doc__)
        return 1
    path, target = sys.argv[1], sys.argv[2].encode()
    ctx = 200
    maxrefs = 8
    for i, a in enumerate(sys.argv):
        if a == '--ctx':
            ctx = int(sys.argv[i + 1])
        if a == '--max':
            maxrefs = int(sys.argv[i + 1])

    f = open(path, 'rb')
    elf = ELFFile(f)

    # 段映射表
    segs = []
    for s in elf.iter_segments():
        if s['p_type'] == 'PT_LOAD':
            segs.append((s['p_vaddr'], s['p_vaddr'] + s['p_filesz'], s['p_offset']))

    def va_to_off(va):
        for vstart, vend, off in segs:
            if vstart <= va < vend:
                return off + (va - vstart)
        return None

    def off_to_va(off):
        for vstart, vend, o in segs:
            if o <= off < o + (vend - vstart):
                return vstart + (off - o)
        return None

    # 找字符串地址
    data = f.read()
    addrs = []
    start = 0
    while True:
        p = data.find(target, start)
        if p < 0:
            break
        # 仅要求尾部 NUL 边界（Cinemo 部分常量前有 0x7f 前缀）
        if data[p + len(target)] == 0:
            va = off_to_va(p)
            if va is not None:
                addrs.append((va, p))
        start = p + 1

    print('### %s  target=%r  occurrences=%d' % (path, target.decode(), len(addrs)))
    for va, off in addrs:
        print('    file_off=0x%x  vaddr=0x%x' % (off, va))
    if not addrs:
        return 0

    # 扫描 .text 所有 RIP-relative lea
    text = None
    for s in elf.iter_sections():
        if s.name == '.text':
            text = s
            break
    if text is None:
        print('no .text')
        return 1

    tdata = text.data()
    tva = text['sh_addr']
    md = Cs(CS_ARCH_X86, CS_MODE_64)
    md.detail = True

    targets = {va: off for va, off in addrs}
    refs = []
    # 逐指令线性扫描（起点按 1 字节步进以便对齐到真实指令，先粗扫 lea 编码）
    i = 0
    n = len(tdata)
    while i < n - 7:
        b = tdata[i]
        # REX.W + 8D (lea) : 48/4c 8d
        if tdata[i] in (0x48, 0x4c) and tdata[i + 1] == 0x8d:
            modrm = tdata[i + 2]
            mod = modrm >> 6
            rm = modrm & 7
            if mod == 0 and rm == 5:  # RIP-relative
                disp = int.from_bytes(tdata[i + 3:i + 7], 'little', signed=True)
                insn_va = tva + i
                tgt = insn_va + 7 + disp
                if tgt in targets:
                    refs.append((insn_va, tgt))
        i += 1

    # 对每个引用点反汇编周边
    print('\n=== refs: %d ===' % len(refs))
    md2 = Cs(CS_ARCH_X86, CS_MODE_64)
    for k, (va, tgt) in enumerate(refs[:maxrefs]):
        print('\n--- ref#%d  lea@0x%x -> %r ---' % (k, va, target.decode()))
        lo = max(tva, va - ctx)
        base = lo
        code = tdata[lo - tva: va - tva + 64]
        # 用 capstone 对齐：从前一段开始解码，丢弃跨入前的指令
        for insn in md2.disasm(code, base):
            if insn.address >= va - ctx and insn.address <= va + ctx:
                mark = '  <<<' if insn.address == va else ''
                print('  0x%-8x %-32s %s%s' % (insn.address,
                                               insn.bytes.hex(),
                                               insn.mnemonic + ' ' + insn.op_str,
                                               mark))
    return 0


if __name__ == '__main__':
    sys.exit(main())
