/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 ProtocolIOException。协议 IO 异常：协议层读写失败（对端关闭、帧非法等）。
 *   This file defines ProtocolIOException. Protocol IO exception for protocol-level read/write failures (peer closed, bad frame, …).
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;


/**
 * 协议 IO 异常：协议层读写失败（对端关闭、帧非法等）。
 *
 * Protocol IO exception for protocol-level read/write failures (peer closed, bad frame, …).
 */
public final class ProtocolIOException extends IOException {
    private static final long a = 1;

    public ProtocolIOException(String str) {
        super(str);
    }

    public ProtocolIOException(String str, Throwable th) {
        super(str, th);
    }
}
