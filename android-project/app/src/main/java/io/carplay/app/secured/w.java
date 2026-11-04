/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 w。转换函数：把一个对象映射为另一个对象。（服务于 Integer）
 *   This file defines w. Function mapping one object to another. (serves Integer.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.function.BiFunction;



/**
 * 转换函数：把一个对象映射为另一个对象。（服务于 Integer）
 *
 * Function mapping one object to another. (serves Integer.)
 */
public final  class w implements BiFunction {
    public final  int a;

    public  w(int i) {
        this.a = i;
    }

    @Override 
    public final Object apply(Object obj, Object obj2) {
        Integer num = (Integer) obj;
        switch (this.a) {
            case 0:
                return Integer.valueOf(Integer.sum(num.intValue(), ((Integer) obj2).intValue()));
            case AudioSourceCapability.c :
                return DisplaySession.F(num, (Integer) obj2);
            default:
                return Integer.valueOf(Integer.sum(num.intValue(), ((Integer) obj2).intValue()));
        }
    }
}
