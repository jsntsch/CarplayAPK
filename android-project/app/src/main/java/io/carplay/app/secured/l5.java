/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 l5。验证结果接口：返回 VERIFIED / REJECTED / CONTRACT_UNAVAILABLE 三态。
 *   This file defines l5. Verification-result interface returning VERIFIED / REJECTED / CONTRACT_UNAVAILABLE.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 验证结果接口：返回 VERIFIED / REJECTED / CONTRACT_UNAVAILABLE 三态。
 *
 * Verification-result interface returning VERIFIED / REJECTED / CONTRACT_UNAVAILABLE.
 */
public interface l5 {

    /**
     * 枚举：本子系统使用的状态/类型取值集合。
     *
     * Enumeration: state/type values used by this subsystem.
     */
    public enum a {
        VERIFIED,
        REJECTED,
        CONTRACT_UNAVAILABLE
    }

    static l5 b() {
        return new k5();
    }

    
    static  a e(String str, String str2, String str3, String str4, String str5) {
        return a.CONTRACT_UNAVAILABLE;
    }

    a f(String str, String str2, String str3, String str4, String str5);
}
