/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 CarLifeFrameCipher。CarLife 帧加密：使用协商出的会话密钥对控制类帧做加解密。
 *   This file defines CarLifeFrameCipher. CarLife frame cipher: encrypts/decrypts control-class frames with the negotiated session key.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * CarLife 帧加密：使用协商出的会话密钥对控制类帧做加解密。
 *
 * CarLife frame cipher: encrypts/decrypts control-class frames with the negotiated session key.
 */
public final class CarLifeFrameCipher {
    private final o a;
    private final boolean b;

    public CarLifeFrameCipher(boolean z, o oVar) {
        if (z && oVar == null) {
            SyntheticRefs.b("negotiated cipher required");
            throw null;
        }
        this.b = z;
        this.a = oVar;
    }

    public byte[] a(CarLifeFrame.c cVar) throws ProtocolIOException {
        if (cVar == null || !cVar.h().d()) {
            SyntheticRefs.b("structured control only");
            return null;
        }
        if (cVar.i().g() != cVar.g().length || cVar.g().length > 32767) {
            SyntheticRefs.f("wire body length mismatch");
            return null;
        }
        byte[] bArrG = cVar.g();
        if (this.b && bArrG.length != 0) {
            bArrG = this.a.b(bArrG);
        }
        if (bArrG != null && bArrG.length <= 32767) {
            return (byte[]) bArrG.clone();
        }
        SyntheticRefs.f("invalid plaintext length");
        return null;
    }

    public CarLifeFrame.c b(CarLifeFrame.a aVar, int i, byte[] bArr) throws ProtocolIOException {
        if (aVar == null || !aVar.d() || bArr == null) {
            SyntheticRefs.b("structured control only");
            return null;
        }
        if (bArr.length > 32767) {
            SyntheticRefs.f("plaintext body exceeds signed short limit");
            return null;
        }
        byte[] bArrA = (!this.b || bArr.length == 0) ? (byte[]) bArr.clone() : this.a.a((byte[]) bArr.clone());
        if (bArrA != null && bArrA.length <= 32767) {
            return new CarLifeFrame.c(aVar, new CarLifeFrame.b(bArrA.length, 0L, 0, i), bArrA);
        }
        SyntheticRefs.f("ciphertext body exceeds signed short limit");
        return null;
    }
}
