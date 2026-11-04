/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 HexCodec。十六进制编解码：严格校验字符集与长度。
 *   This file defines HexCodec. Hex codec with strict character-set and length validation.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;


/**
 * 十六进制编解码：严格校验字符集与长度。
 *
 * Hex codec with strict character-set and length validation.
 */
public class HexCodec {
    protected final byte[] a = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
    protected final byte[] b = new byte[Iap2Csm.b];

    public HexCodec() {
        b();
    }

    public byte[] a(String str, int i, int i2) throws IOException {
        String str2;
        if (str == null) {
            throw new NullPointerException("'str' cannot be null");
        }
        if (i < 0 || i2 < 0 || i > str.length() - i2) {
            throw new IndexOutOfBoundsException("invalid offset and/or length specified");
        }
        if ((i2 & 1) == 0) {
            int i3 = i2 >>> 1;
            byte[] bArr = new byte[i3];
            for (int i4 = 0; i4 < i3; i4++) {
                int i5 = i + 1;
                byte b = this.b[str.charAt(i)];
                i += 2;
                int i6 = (b << 4) | this.b[str.charAt(i5)];
                if (i6 >= 0) {
                    bArr[i4] = (byte) i6;
                } else {
                    str2 = "invalid characters encountered in Hex string";
                }
            }
            return bArr;
        }
        str2 = "a hexadecimal encoding must have an even number of characters";
        SyntheticRefs.c(str2);
        return null;
    }

    public void b() {
        int i = 0;
        int i2 = 0;
        while (true) {
            byte[] bArr = this.b;
            if (i2 >= bArr.length) {
                break;
            }
            bArr[i2] = -1;
            i2++;
        }
        while (true) {
            byte[] bArr2 = this.a;
            int length = bArr2.length;
            byte[] bArr3 = this.b;
            if (i >= length) {
                bArr3[65] = bArr3[97];
                bArr3[66] = bArr3[98];
                bArr3[67] = bArr3[99];
                bArr3[68] = bArr3[100];
                bArr3[69] = bArr3[101];
                bArr3[70] = bArr3[102];
                return;
            }
            bArr3[bArr2[i]] = (byte) i;
            i++;
        }
    }
}
