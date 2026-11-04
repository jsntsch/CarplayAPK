/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 h0。后台任务：在独立线程中执行一次异步动作。（服务于 AirPlaySession）
 *   This file defines h0. Background task executing one asynchronous step on its own thread. (serves AirPlaySession.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.List;



/**
 * 后台任务：在独立线程中执行一次异步动作。（服务于 AirPlaySession）
 *
 * Background task executing one asynchronous step on its own thread. (serves AirPlaySession.)
 */
public final  class h0 implements Runnable {
    public final  int a;
    public  long b;
    public  AirPlaySession c;
    public  Object d;
    public  AutoCloseable e;

    public  h0(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        switch (this.a) {
            case 0:
                this.c.I0((List) this.d, (EventChannel) this.e, this.b);
                break;
            default:
                ((WirelessCarPlayService) this.d).P1(this.b, this.c, (DisplaySession) this.e);
                break;
        }
    }
}
