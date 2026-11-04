/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 v。比较器：定义候选对象的排序规则。
 *   This file defines v. Comparator defining the ordering of candidates.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.Comparator;



/**
 * 比较器：定义候选对象的排序规则。
 *
 * Comparator defining the ordering of candidates.
 */
public final  class v implements Comparator {
    @Override 
    public final int compare(Object obj, Object obj2) {
        return ((String) obj).compareTo((String) obj2);
    }
}
