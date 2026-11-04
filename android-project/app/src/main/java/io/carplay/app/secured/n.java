/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 n。内部实现类：本子系统使用的数据结构与辅助方法。
 *   This file defines n. Internal implementation class holding data structures and helpers for this subsystem.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.Locale;


/**
 * 内部实现类：本子系统使用的数据结构与辅助方法。
 *
 * Internal implementation class holding data structures and helpers for this subsystem.
 */
public final class n {
    private n() {
    }

    public static String a(String str) {
        if (str == null) {
            return null;
        }
        String upperCase = str.trim().toUpperCase(Locale.ROOT);
        if (upperCase.matches("[0-9A-F]{2}(?::[0-9A-F]{2}){5}") && !upperCase.equals("02:00:00:00:00:00") && !upperCase.equals("00:00:00:00:00:00") && (Integer.parseInt(upperCase.substring(0, 2), 16) & 1) == 0) {
            return upperCase;
        }
        return null;
    }
}
