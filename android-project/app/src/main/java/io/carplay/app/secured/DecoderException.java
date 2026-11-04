/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 DecoderException。解码异常：编解码器内部状态非法。
 *   This file defines DecoderException. Decoder exception for illegal codec state.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 解码异常：编解码器内部状态非法。
 *
 * Decoder exception for illegal codec state.
 */
public class DecoderException extends IllegalStateException {
    private Throwable a;

    public DecoderException(String str, Throwable th) {
        super(str);
        this.a = th;
    }

    @Override 
    public Throwable getCause() {
        return this.a;
    }
}
