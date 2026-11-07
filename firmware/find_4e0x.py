#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
find_4e0x.py — 在 ARM64 ELF 中定位 iAP2 CarPlay 旧协议消息 ID（0x4E09..0x4E0E）
               的立即数引用点与 .rodata 表项，用于反推车机期望的 payload 结构。

用法:
  python find_4e0x.py <elf> [--imm] [--rodata] [--ctx N]
"""
import sys
from elftools.elf.elffile import ELFFile
from capstone import (
    Cs,
    CS_ARCH_ARM64,
    CS_ARCH_X86,
    CS_MODE_64,
    CS_MODE_ARM,
    CS_OP_IMM,
)


def make_disasm(machine):
    if machine == "EM_AARCH64":
        md = Cs(CS_ARCH_ARM64, CS_MODE_ARM)
    else:
        md = Cs(CS_ARCH_X86, CS_MODE_64)
    md.detail = True
    return md

SHF_EXECINSTR = 0x4
DEFAULT_TARGETS = {
    0x4E09: "4E09",
    0x4E0A: "4E0A",
    0x4E0B: "4E0B",
    0x4E0C: "4E0C",
    0x4E0D: "4E0D",
    0x4E0E: "4E0E",
}


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return 1
    path = sys.argv[1]
    do_imm = "--imm" in sys.argv
    do_rodata = "--rodata" in sys.argv
    if not do_imm and not do_rodata:
        do_imm = do_rodata = True
    ctx = 40
    for i, a in enumerate(sys.argv):
        if a == "--ctx":
            ctx = int(sys.argv[i + 1])

    f = open(path, "rb")
    elf = ELFFile(f)
    print(f"file={path}")
    print(f"machine={elf['e_machine']} class={elf.elfclass}")

    if do_imm:
        md = make_disasm(elf["e_machine"])
        for sec in elf.iter_sections():
            if not (sec["sh_flags"] & SHF_EXECINSTR):
                continue
            if sec["sh_type"] != "SHT_PROGBITS":
                continue
            code = sec.data()
            base = sec["sh_addr"]
            print(f"\n[IMM] section {sec.name} addr=0x{base:x} size={len(code)}")
            hits = []
            insns = list(md.disasm(code, base))
            for idx, insn in enumerate(insns):
                for op in insn.operands:
                    if op.type == CS_OP_IMM and op.imm in DEFAULT_TARGETS:
                        hits.append((idx, insn, DEFAULT_TARGETS[op.imm]))
            print(f"[IMM] hits={len(hits)}")
            for idx, insn, tag in hits:
                print(f"\n--- {tag} @0x{insn.address:x}: {insn.mnemonic} {insn.op_str}")
                lo = max(0, idx - ctx // 2)
                hi = min(len(insns), idx + ctx // 2)
                for j in range(lo, hi):
                    mark = ">>" if j == idx else "  "
                    print(f" {mark} 0x{insns[j].address:x}  {insns[j].mnemonic}\t{insns[j].op_str}")

    if do_rodata:
        for sec in elf.iter_sections():
            if sec.name not in (".rodata", ".data.rel.ro", ".data"):
                continue
            data = sec.data()
            base = sec["sh_addr"]
            print(f"\n[RODATA] section {sec.name} addr=0x{base:x} size={len(data)}")
            for tid, tag in DEFAULT_TARGETS.items():
                pat_le16 = tid.to_bytes(2, "little")
                pat_le32 = tid.to_bytes(4, "little")
                pat_be16 = tid.to_bytes(2, "big")
                for name, pat in (("le16", pat_le16), ("le32", pat_le32), ("be16", pat_be16)):
                    start = 0
                    cnt = 0
                    while True:
                        p = data.find(pat, start)
                        if p < 0 or cnt >= 12:
                            break
                        print(f"  {tag} {name} @0x{base + p:x} (off 0x{p:x})")
                        start = p + 1
                        cnt += 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
