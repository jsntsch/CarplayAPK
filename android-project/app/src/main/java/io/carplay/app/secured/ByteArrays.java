/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 ByteArrays。字节数组工具：非空、长度与区间校验，失败时抛出明确异常。
 *   This file defines ByteArrays. Byte-array helpers for null, length and range validation with explicit exceptions.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.Arrays;


/**
 * 字节数组工具：非空、长度与区间校验，失败时抛出明确异常。
 *
 * Byte-array helpers for null, length and range validation with explicit exceptions.
 */
public abstract class ByteArrays {
    public static boolean a(byte[] bArr, int i, int i2) {
        int i3 = 0;
        for (int i4 = 0; i4 < i2; i4++) {
            i3 |= bArr[i + i4];
        }
        return i3 == 0;
    }

    public static boolean b(byte[] bArr, byte[] bArr2) {
        return Arrays.equals(bArr, bArr2);
    }

    public static void c(byte[] bArr) {
        if (bArr != null) {
            Arrays.fill(bArr, (byte) 0);
        }
    }

    public static byte[] d(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
    }

    public static boolean e(int i, byte[] bArr, int i2, byte[] bArr2, int i3) {
        if (bArr == null) {
            throw new NullPointerException("'a' cannot be null");
        }
        if (bArr2 == null) {
            throw new NullPointerException("'b' cannot be null");
        }
        if (i < 0) {
            SyntheticRefs.b("'len' cannot be negative");
            return false;
        }
        if (i2 > bArr.length - i) {
            throw new IndexOutOfBoundsException("'aOff' value invalid for specified length");
        }
        if (i3 > bArr2.length - i) {
            throw new IndexOutOfBoundsException("'bOff' value invalid for specified length");
        }
        int i4 = 0;
        for (int i5 = 0; i5 < i; i5++) {
            i4 |= bArr[i2 + i5] ^ bArr2[i3 + i5];
        }
        return i4 == 0;
    }

    public static void f(byte[] bArr, int i, int i2, byte b) {
        Arrays.fill(bArr, i, i2, b);
    }

    public static int g(byte[] bArr) {
        if (bArr == null) {
            return 0;
        }
        int length = bArr.length;
        int i = length + 1;
        while (true) {
            length--;
            if (length < 0) {
                return i;
            }
            i = (i * 257) ^ bArr[length];
        }
    }

    public static void h(byte[] bArr, byte[] bArr2) {
        int length = bArr.length - 1;
        for (int i = 0; i <= length; i++) {
            bArr2[i] = bArr[length - i];
        }
    }

    public static boolean i(int i, int i2, int i3, int i4) {
        return i2 > 0 && i4 > 0 && i - i3 < i4 && i3 - i < i2;
    }
}
