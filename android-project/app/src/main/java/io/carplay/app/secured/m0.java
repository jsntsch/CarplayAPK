/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 m0。惰性取值：按需提供对象。（服务于 AirPlaySession）
 *   This file defines m0. Supplier providing a value on demand. (serves AirPlaySession.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.function.BooleanSupplier;



/**
 * 惰性取值：按需提供对象。（服务于 AirPlaySession）
 *
 * Supplier providing a value on demand. (serves AirPlaySession.)
 */
public final  class m0 implements BooleanSupplier {
    public  AirPlaySession a;
    public  EventChannel b;

    @Override 
    public final boolean getAsBoolean() {
        return this.a.H0(this.b);
    }
}
