/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 f2。枚举：本子系统使用的状态/类型取值集合。
 *   This file defines f2. Enumeration: state/type values used by this subsystem.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 枚举：本子系统使用的状态/类型取值集合。
 *
 * Enumeration: state/type values used by this subsystem.
 */
public enum f2 {
    AGREEMENT,
    ENCRYPTION,
    DECRYPTION,
    KEYGEN,
    SIGNING,
    VERIFYING,
    AUTHENTICATION,
    VERIFICATION,
    PRF,
    ANY
}
