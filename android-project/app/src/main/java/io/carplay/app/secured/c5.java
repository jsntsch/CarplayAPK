/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 c5。内部实现类：围绕 CipherParameters 组织状态与操作，服务于本子系统。
 *   This file defines c5. Internal implementation class grouping state and operations around CipherParameters.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import org.bouncycastle.crypto.CipherParameters;


/**
 * 内部实现类：围绕 CipherParameters 组织状态与操作，服务于本子系统。
 *
 * Internal implementation class grouping state and operations around CipherParameters.
 */
public class c5 implements CipherParameters {
    private byte[] a;
    private CipherParameters b;

    public c5(CipherParameters cipherParameters, byte[] bArr, int i, int i2) {
        byte[] bArr2 = new byte[i2];
        this.a = bArr2;
        this.b = cipherParameters;
        System.arraycopy(bArr, i, bArr2, 0, i2);
    }

    public byte[] a() {
        return this.a;
    }

    public CipherParameters b() {
        return this.b;
    }

    public c5(CipherParameters cipherParameters, byte[] bArr) {
        this(cipherParameters, bArr, 0, bArr.length);
    }
}
