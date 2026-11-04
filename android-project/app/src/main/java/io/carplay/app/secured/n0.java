/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 n0。线程工厂：统一命名并创建后台线程。
 *   This file defines n0. Thread factory naming and creating background threads.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.AirplayServiceProbe;
import io.carplay.app.prototype.PairingSession;
import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.concurrent.ThreadFactory;



/**
 * 线程工厂：统一命名并创建后台线程。
 *
 * Thread factory naming and creating background threads.
 */
public final  class n0 implements ThreadFactory {
    public final  int a;

    public  n0(int i) {
        this.a = i;
    }

    @Override 
    public final Thread newThread(Runnable runnable) {
        switch (this.a) {
            case 0:
                return AirPlaySession.U0(runnable);
            case AudioSourceCapability.c :
                return AirPlaySession.V0(runnable);
            case 2:
                return AirplayServiceProbe.u(runnable);
            case AudioSourceCapability.d :
                return DisplaySession.C(runnable);
            case 4:
                return BtDiscovery.o(runnable);
            case 5:
                return PairingSession.R0(runnable);
            case 6:
                return WirelessCarPlayService.N1(runnable);
            default:
                return WirelessCarPlayService.O1(runnable);
        }
    }
}
