/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 MaxBytesExceededException。字节上限异常：单次读取/累积超过允许的最大字节数。
 *   This file defines MaxBytesExceededException. Max-bytes exception raised when a read or accumulation exceeds the allowed limit.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 字节上限异常：单次读取/累积超过允许的最大字节数。
 *
 * Max-bytes exception raised when a read or accumulation exceeds the allowed limit.
 */
public class MaxBytesExceededException extends RuntimeCryptoException {
    public MaxBytesExceededException(String str) {
        super(str);
    }
}
