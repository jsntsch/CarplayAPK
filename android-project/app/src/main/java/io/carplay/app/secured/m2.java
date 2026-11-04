/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 m2。内部实现类：本子系统使用的数据结构与辅助方法。
 *   This file defines m2. Internal implementation class holding data structures and helpers for this subsystem.
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
public class m2 {
    private int a;
    private byte[] b;
    private int c;

    public m2(byte[] bArr, int i, int i2) {
        this.b = ByteArrays.d(bArr);
        this.c = i;
        this.a = i2;
    }

    public int a() {
        return this.c;
    }

    public byte[] b() {
        return ByteArrays.d(this.b);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof m2)) {
            return false;
        }
        m2 m2Var = (m2) obj;
        if (m2Var.c != this.c) {
            return false;
        }
        return ByteArrays.b(this.b, m2Var.b);
    }

    public int hashCode() {
        return ByteArrays.g(this.b) ^ this.c;
    }

    public m2(byte[] bArr, int i) {
        this(bArr, i, -1);
    }
}
