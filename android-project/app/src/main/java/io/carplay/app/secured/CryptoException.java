/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 CryptoException。加密异常基类：区分可恢复与不可恢复的加密错误。
 *   This file defines CryptoException. Base crypto exception distinguishing recoverable from fatal crypto errors.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 加密异常基类：区分可恢复与不可恢复的加密错误。
 *
 * Base crypto exception distinguishing recoverable from fatal crypto errors.
 */
public abstract class CryptoException extends Exception {
    private Throwable a;

    public CryptoException(String str) {
        super(str);
    }

    @Override 
    public Throwable getCause() {
        return this.a;
    }
}
