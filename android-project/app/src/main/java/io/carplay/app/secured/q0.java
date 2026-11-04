/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 q0。后台任务：在独立线程中执行一次异步动作。
 *   This file defines q0. Background task executing one asynchronous step on its own thread.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.WirelessCarPlayService;



/**
 * 后台任务：在独立线程中执行一次异步动作。
 *
 * Background task executing one asynchronous step on its own thread.
 */
public final  class q0 implements Runnable {
    public final  int a;
    public  Object b;
    public  Object c;
    public  Object d;

    public  q0(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        switch (this.a) {
            case 0:
                ((AirPlaySession) this.b).a1((CarLifeFrame.a) this.c, (AirPlaySession.d) this.d);
                break;
            case AudioSourceCapability.c :
                ((WirelessCarPlayService) this.c).g2((DisplaySession.VideoConfig) this.d, (AirPlaySession) this.b);
                break;
            default:
                ((WirelessCarPlayService) this.b).Q1((String) this.c, (Iap2Bootstrap.a) this.d);
                break;
        }
    }
}
