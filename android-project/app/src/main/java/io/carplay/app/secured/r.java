/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 r。转换函数：把一个对象映射为另一个对象。
 *   This file defines r. Function mapping one object to another.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.PairingSession;
import java.util.function.Function;



/**
 * 转换函数：把一个对象映射为另一个对象。
 *
 * Function mapping one object to another.
 */
public final  class r implements Function {
    public final  int a;

    public  r(int i) {
        this.a = i;
    }

    @Override 
    public final Object apply(Object obj) {
        switch (this.a) {
            case 0:
                return AudioCapabilities.n((AudioCapabilities.a) obj);
            case AudioSourceCapability.c :
                int i = HidCapabilities.p;
                return ((HidCapabilities.c) obj).h();
            case 2:
                return Boolean.valueOf(((HidCapabilities.c) obj).t());
            case AudioSourceCapability.d :
                return PairingTlv8.f((Integer) obj);
            case 4:
                return ((PairingSession.l) obj).a;
            case 5:
                return IoStatsSnapshot.b((IoStatsSnapshot.b) obj);
            case 6:
                return ((PairingSession.l) obj).a;
            default:
                return ((PairingSession.l) obj).b;
        }
    }
}
