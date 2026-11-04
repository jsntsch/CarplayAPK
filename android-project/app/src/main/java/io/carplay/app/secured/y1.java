/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 y1。ChaCha20 变体：调整 nonce/计数器长度以匹配协议要求。
 *   This file defines y1. ChaCha20 variant adjusting nonce/counter length to the protocol requirement.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * ChaCha20 变体：调整 nonce/计数器长度以匹配协议要求。
 *
 * ChaCha20 variant adjusting nonce/counter length to the protocol requirement.
 */
public class y1 extends ChaCha20 {
    @Override 
    public void a() {
        int[] iArr = this.c;
        int i = iArr[12] + 1;
        iArr[12] = i;
        if (i != 0) {
            return;
        }
        SyntheticRefs.d("attempt to increase counter past 2^32.");
    }

    @Override 
    public void b(byte[] bArr) {
        z1.m(this.a, this.c, this.d);
        LittleEndianIntCodec.e(this.d, bArr, 0);
    }

    @Override 
    public String c() {
        return "ChaCha7539";
    }

    @Override 
    public int d() {
        return 12;
    }

    @Override 
    public void j() {
        this.c[12] = 0;
    }

    @Override 
    public void l(byte[] bArr, byte[] bArr2) {
        if (bArr != null) {
            if (bArr.length != 32) {
                throw new IllegalArgumentException(c() + " requires 256 bit key");
            }
            g(bArr.length, this.c, 0);
            LittleEndianIntCodec.h(bArr, 0, this.c, 4, 8);
        }
        LittleEndianIntCodec.h(bArr2, 0, this.c, 13, 3);
    }
}
