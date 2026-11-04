/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 a6。内部实现类：本子系统使用的数据结构与辅助方法。
 *   This file defines a6. Internal implementation class holding data structures and helpers for this subsystem.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 内部实现类：本子系统使用的数据结构与辅助方法。
 *
 * Internal implementation class holding data structures and helpers for this subsystem.
 */
public class a6 extends j3 {
    public a6(f2 f2Var) {
        super(f2Var);
        CryptoServicesRegistrar.a(r());
        p();
    }

    @Override 
    public int a(byte[] bArr, int i) {
        l();
        LittleEndianIntCodec.j(this.f, bArr, i);
        LittleEndianIntCodec.j(this.g, bArr, i + 8);
        LittleEndianIntCodec.j(this.h, bArr, i + 16);
        LittleEndianIntCodec.j(this.i, bArr, i + 24);
        LittleEndianIntCodec.j(this.j, bArr, i + 32);
        LittleEndianIntCodec.j(this.k, bArr, i + 40);
        LittleEndianIntCodec.j(this.l, bArr, i + 48);
        LittleEndianIntCodec.j(this.m, bArr, i + 56);
        p();
        return 64;
    }

    @Override 
    public String c() {
        return "SHA-512";
    }

    @Override 
    public int d() {
        return 64;
    }

    @Override 
    public void p() {
        super.p();
        this.f = 7640891576956012808L;
        this.g = -4942790177534073029L;
        this.h = 4354685564936845355L;
        this.i = -6534734903238641935L;
        this.j = 5840696475078001361L;
        this.k = -7276294671716946913L;
        this.l = 2270897969802886507L;
        this.m = 6620516959819538809L;
    }

    public e2 r() {
        return n6.a(this, 256, this.a);
    }

    public a6() {
        this(f2.ANY);
    }
}
