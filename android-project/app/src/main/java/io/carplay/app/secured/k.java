/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 k。过滤谓词：按条件筛选候选对象。
 *   This file defines k. Predicate filtering candidate objects by a condition.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.function.IntPredicate;



/**
 * 过滤谓词：按条件筛选候选对象。
 *
 * Predicate filtering candidate objects by a condition.
 */
public final  class k implements IntPredicate {
    public final  int a;

    public  k(int i) {
        this.a = i;
    }

    @Override 
    public final boolean test(int i) {
        switch (this.a) {
            case 0:
                return BinaryPlist.g(i);
            case AudioSourceCapability.c :
                return ControlRequestParser.b(i);
            case 2:
                return Character.isISOControl(i);
            default:
                return Iap2Csm.k(i);
        }
    }
}
