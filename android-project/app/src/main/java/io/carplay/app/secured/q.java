/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 q。转换函数：把一个对象映射为另一个对象。
 *   This file defines q. Function mapping one object to another.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.function.ToIntFunction;



/**
 * 转换函数：把一个对象映射为另一个对象。
 *
 * Function mapping one object to another.
 */
public final  class q implements ToIntFunction {
    public final  int a;

    public  q(int i) {
        this.a = i;
    }

    @Override 
    public final int applyAsInt(Object obj) {
        switch (this.a) {
            case 0:
                return AudioCapabilities.m((AudioCapabilities.a) obj);
            case AudioSourceCapability.c :
                return ((HidCapabilities.c) obj).p();
            default:
                return ((Integer) obj).intValue();
        }
    }
}
