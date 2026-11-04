/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 OutputLengthException。输出长度异常：输出缓冲区空间不足。
 *   This file defines OutputLengthException. Output-length exception raised when the output buffer is too small.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 输出长度异常：输出缓冲区空间不足。
 *
 * Output-length exception raised when the output buffer is too small.
 */
public class OutputLengthException extends DataLengthException {
    public OutputLengthException(String str) {
        super(str);
    }
}
