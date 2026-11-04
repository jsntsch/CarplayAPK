/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 l6。内部实现类：本子系统使用的数据结构与辅助方法。
 *   This file defines l6. Internal implementation class holding data structures and helpers for this subsystem.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.io.InputStream;


/**
 * 内部实现类：本子系统使用的数据结构与辅助方法。
 *
 * Internal implementation class holding data structures and helpers for this subsystem.
 */
public abstract class l6 {
    public static int a(InputStream inputStream, byte[] bArr) throws IOException {
        return b(inputStream, bArr, 0, bArr.length);
    }

    public static int b(InputStream inputStream, byte[] bArr, int i, int i2) throws IOException {
        int i3 = 0;
        while (i3 < i2) {
            int i4 = inputStream.read(bArr, i + i3, i2 - i3);
            if (i4 < 0) {
                break;
            }
            i3 += i4;
        }
        return i3;
    }
}
