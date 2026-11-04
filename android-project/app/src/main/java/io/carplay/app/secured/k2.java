/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 k2。内部实现类：本子系统使用的数据结构与辅助方法。
 *   This file defines k2. Internal implementation class holding data structures and helpers for this subsystem.
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
public class k2 {
    private byte[] a;
    private int b;

    public k2(byte[] bArr, int i) {
        this.a = ByteArrays.d(bArr);
        this.b = i;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof k2)) {
            return false;
        }
        k2 k2Var = (k2) obj;
        if (k2Var.b != this.b) {
            return false;
        }
        return ByteArrays.b(this.a, k2Var.a);
    }

    public int hashCode() {
        return ByteArrays.g(this.a) ^ this.b;
    }
}
