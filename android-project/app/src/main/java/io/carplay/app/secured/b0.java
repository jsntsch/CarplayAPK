/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 b0。后台任务：在独立线程中执行一次异步动作。（服务于 AirPlaySession）
 *   This file defines b0. Background task executing one asynchronous step on its own thread. (serves AirPlaySession.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;



/**
 * 后台任务：在独立线程中执行一次异步动作。（服务于 AirPlaySession）
 *
 * Background task executing one asynchronous step on its own thread. (serves AirPlaySession.)
 */
public final  class b0 implements Runnable {
    public final  int a;
    public  AirPlaySession b;

    public  b0(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        int i = this.a;
        AirPlaySession airPlaySession = this.b;
        switch (i) {
            case 0:
                airPlaySession.c1();
                break;
            case AudioSourceCapability.c :
                airPlaySession.N0();
                break;
            case 2:
                airPlaySession.r2();
                break;
            case AudioSourceCapability.d :
                airPlaySession.u2();
                break;
            case 4:
                airPlaySession.eventKeepAliveTick();
                break;
            case 5:
                airPlaySession.f0();
                break;
            default:
                airPlaySession.M1();
                break;
        }
    }
}
