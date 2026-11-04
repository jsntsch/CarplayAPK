/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 RuntimeCryptoException。运行时加密异常：无需显式捕获的加密错误。
 *   This file defines RuntimeCryptoException. Unchecked crypto exception for errors that need no explicit catch.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 运行时加密异常：无需显式捕获的加密错误。
 *
 * Unchecked crypto exception for errors that need no explicit catch.
 */
public abstract class RuntimeCryptoException extends RuntimeException {
    public RuntimeCryptoException(String str) {
        super(str);
    }
}
