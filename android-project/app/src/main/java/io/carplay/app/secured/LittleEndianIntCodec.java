/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 LittleEndianIntCodec。小端 32 位整数编解码：协议字段与内存字节序互转。
 *   This file defines LittleEndianIntCodec. Little-endian 32-bit integer codec between protocol fields and memory byte order.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 小端 32 位整数编解码：协议字段与内存字节序互转。
 *
 * Little-endian 32-bit integer codec between protocol fields and memory byte order.
 */
public abstract class LittleEndianIntCodec {
    public static int a(byte[] bArr, int i) {
        return (bArr[i + 3] & 255) | (bArr[i] << 24) | ((bArr[i + 1] & 255) << 16) | ((bArr[i + 2] & 255) << 8);
    }

    public static long b(byte[] bArr, int i) {
        int iA = a(bArr, i);
        return (((long) a(bArr, i + 4)) & 4294967295L) | ((((long) iA) & 4294967295L) << 32);
    }

    public static void c(int i, byte[] bArr, int i2) {
        bArr[i2] = (byte) (i >>> 24);
        bArr[i2 + 1] = (byte) (i >>> 16);
        bArr[i2 + 2] = (byte) (i >>> 8);
        bArr[i2 + 3] = (byte) i;
    }

    public static void d(int i, byte[] bArr, int i2) {
        bArr[i2] = (byte) i;
        bArr[i2 + 1] = (byte) (i >>> 8);
        bArr[i2 + 2] = (byte) (i >>> 16);
        bArr[i2 + 3] = (byte) (i >>> 24);
    }

    public static void e(int[] iArr, byte[] bArr, int i) {
        for (int i2 : iArr) {
            d(i2, bArr, i);
            i += 4;
        }
    }

    public static int f(byte[] bArr, int i) {
        return (bArr[i + 3] << 24) | (bArr[i] & 255) | ((bArr[i + 1] & 255) << 8) | ((bArr[i + 2] & 255) << 16);
    }

    public static void g(byte[] bArr, int i, int[] iArr) {
        for (int i2 = 0; i2 < iArr.length; i2++) {
            iArr[i2] = f(bArr, i);
            i += 4;
        }
    }

    public static void h(byte[] bArr, int i, int[] iArr, int i2, int i3) {
        for (int i4 = 0; i4 < i3; i4++) {
            iArr[i2 + i4] = f(bArr, i);
            i += 4;
        }
    }

    public static int[] i(byte[] bArr, int i, int i2) {
        int[] iArr = new int[i2];
        g(bArr, i, iArr);
        return iArr;
    }

    public static void j(long j, byte[] bArr, int i) {
        c((int) (j >>> 32), bArr, i);
        c((int) (j & 4294967295L), bArr, i + 4);
    }

    public static void k(long j, byte[] bArr, int i) {
        d((int) (4294967295L & j), bArr, i);
        d((int) (j >>> 32), bArr, i + 4);
    }
}
