/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 j7。内部实现类：本子系统使用的数据结构与辅助方法。
 *   This file defines j7. Internal implementation class holding data structures and helpers for this subsystem.
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
abstract class j7 {
    public static void a(int[] iArr, int i, byte[] bArr) {
        int length = iArr.length * 2;
        int[] iArr2 = new int[length];
        int i2 = iArr[iArr.length - 1] >> 31;
        int length2 = iArr.length;
        int i3 = length;
        while (true) {
            length2--;
            if (length2 < 0) {
                break;
            }
            int i4 = iArr[length2];
            iArr2[i3 - 1] = (i2 << 16) | (i4 >>> 16);
            i3 -= 2;
            iArr2[i3] = i4;
            i2 = i4;
        }
        int i5 = 32 - i;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        while (i6 < length) {
            int i9 = iArr2[i6];
            while (i7 < 16) {
                int i10 = i9 >>> i7;
                if ((i10 & 1) == i8) {
                    i7++;
                } else {
                    int i11 = (i10 | 1) << i5;
                    bArr[(i6 << 4) + i7] = (byte) (i11 >> i5);
                    i7 += i;
                    i8 = i11 >>> 31;
                }
            }
            i6++;
            i7 -= 16;
        }
    }
}
