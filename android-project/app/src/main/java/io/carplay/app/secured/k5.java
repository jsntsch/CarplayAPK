/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 k5。验证结果的默认实现：恒返回 VERIFIED。
 *   This file defines k5. Default verification result implementation that always reports VERIFIED.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;



/**
 * 验证结果的默认实现：恒返回 VERIFIED。
 *
 * Default verification result implementation that always reports VERIFIED.
 */
public final  class k5 implements l5 {
    @Override 
    public final l5.a f(String str, String str2, String str3, String str4, String str5) {
        return l5.e(str, str2, str3, str4, str5);
    }
}
