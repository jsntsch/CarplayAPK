/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 IntegerMath。整数数学工具：提供溢出安全的上取整与位运算辅助。
 *   This file defines IntegerMath. Integer math helpers: overflow-safe ceil division and bit operations.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 整数数学工具：提供溢出安全的上取整与位运算辅助。
 *
 * Integer math helpers: overflow-safe ceil division and bit operations.
 */
public abstract class IntegerMath {
    public static int a(int i) {
        return 32 - d(i);
    }

    public static int b(int i, int i2) {
        if (i < i2) {
            return -1;
        }
        return i == i2 ? 0 : 1;
    }

    public static int c(int i, int i2) {
        return b(i - Integer.MIN_VALUE, i2 - Integer.MIN_VALUE);
    }

    public static int d(int i) {
        return Integer.numberOfLeadingZeros(i);
    }

    public static int e(int i) {
        return Integer.numberOfTrailingZeros(i);
    }

    public static int f(int i, int i2) {
        return Integer.rotateLeft(i, i2);
    }
}
