/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 z2。内部实现类：本子系统使用的数据结构与辅助方法。
 *   This file defines z2. Internal implementation class holding data structures and helpers for this subsystem.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.security.Provider;
import java.util.List;



/**
 * 内部实现类：本子系统使用的数据结构与辅助方法。
 *
 * Internal implementation class holding data structures and helpers for this subsystem.
 */
public final  class z2 implements j5.a, RsaAesCrypto.a, VideoCipher.AvcVideoInput.c {
    public  Object a;
    public  Object b;

    public  z2(DisplaySession t4Var, Provider provider) {
        this.a = t4Var;
        this.b = provider;
    }

    
    public  z2() {
    }

    @Override 
    public o a(byte[] bArr) {
        return ((DisplaySession) this.a).G((Provider) this.b, bArr);
    }

    @Override 
    public void c(List list, long j, long j2, long j3) throws IOException {
        ((w4) this.a).g((w4.b) this.b, list, j, j2, j3);
    }

    @Override 
    public void run() {
        try {
            ((MfiCertificateVerifier) this.a).M((j5.c) this.b);
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }
}
