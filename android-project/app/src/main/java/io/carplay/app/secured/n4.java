/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 n4。内部实现类：本子系统使用的数据结构与辅助方法。
 *   This file defines n4. Internal implementation class holding data structures and helpers for this subsystem.
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
public abstract class n4 {
    public static int a(int i, int i2, int[] iArr, int[] iArr2) {
        long j = ((long) (-(i2 & 1))) & 4294967295L;
        long j2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            long j3 = (((long) iArr2[i3]) & 4294967295L) + (((long) iArr[i3]) & j) + j2;
            iArr2[i3] = (int) j3;
            j2 = j3 >>> 32;
        }
        return (int) j2;
    }

    public static int b(int i) {
        return ((~i) & (i - 1)) >> 31;
    }

    public static int c(int i, int[] iArr) {
        for (int i2 = i - 1; i2 >= 0; i2--) {
            int i3 = iArr[i2];
            if (i3 != 0) {
                return IntegerMath.a(i3) + (i2 * 32);
            }
        }
        return 0;
    }

    public static int d(int i, int[] iArr, int i2) {
        while (true) {
            i--;
            if (i < 0) {
                return i2 << 31;
            }
            int i3 = iArr[i];
            iArr[i] = (i2 << 31) | (i3 >>> 1);
            i2 = i3;
        }
    }
}
