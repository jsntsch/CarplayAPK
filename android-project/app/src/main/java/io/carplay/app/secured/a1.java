/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 a1。过滤谓词：按条件筛选候选对象。
 *   This file defines a1. Predicate filtering candidate objects by a condition.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.Set;
import java.util.function.Predicate;



/**
 * 过滤谓词：按条件筛选候选对象。
 *
 * Predicate filtering candidate objects by a condition.
 */
public final  class a1 implements Predicate {
    public final  int a;
    public  Set b;

    public  a1(int i) {
        this.a = i;
    }

    @Override 
    public final boolean test(Object obj) {
        int i = this.a;
        Set set = this.b;
        switch (i) {
            case 0:
                return SessionStateMachine.B(set, obj);
            default:
                return set.contains(obj);
        }
    }
}
