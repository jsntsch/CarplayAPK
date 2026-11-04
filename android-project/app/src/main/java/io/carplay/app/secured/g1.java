/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 g1。惰性取值：按需提供对象。
 *   This file defines g1. Supplier providing a value on demand.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.function.BooleanSupplier;



/**
 * 惰性取值：按需提供对象。
 *
 * Supplier providing a value on demand.
 */
public final  class g1 implements BooleanSupplier {
    public final  int a;

    public  g1(int i) {
        this.a = i;
    }

    @Override 
    public final boolean getAsBoolean() {
        switch (this.a) {
            case 0:
                return Iap2WirelessSetup.p();
            default:
                return Iap2WirelessSetup.r();
        }
    }
}
