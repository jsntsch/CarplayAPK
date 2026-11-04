/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 w2。内部实现类：围绕 HexCodec 组织状态与操作，服务于本子系统。
 *   This file defines w2. Internal implementation class grouping state and operations around HexCodec.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 内部实现类：围绕 HexCodec 组织状态与操作，服务于本子系统。
 *
 * Internal implementation class grouping state and operations around HexCodec.
 */
public abstract class w2 {
    private static final HexCodec a = new HexCodec();

    public static byte[] a(String str) {
        try {
            return a.a(str, 0, str.length());
        } catch (Exception e) {
            throw new DecoderException("exception decoding Hex string: " + e.getMessage(), e);
        }
    }
}
