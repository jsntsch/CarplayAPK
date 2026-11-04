/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 d1。后台任务：在独立线程中执行一次异步动作。
 *   This file defines d1. Background task executing one asynchronous step on its own thread.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import io.carplay.app.prototype.LogActivity;
import java.net.Socket;



/**
 * 后台任务：在独立线程中执行一次异步动作。
 *
 * Background task executing one asynchronous step on its own thread.
 */
public final  class d1 implements Runnable {
    public final  int a;
    public  Object b;

    public  d1(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        int i = this.a;
        Object obj = this.b;
        switch (i) {
            case 0:
                ((Iap2WirelessSetup) obj).t();
                break;
            case AudioSourceCapability.c :
                ((Surface) obj).release();
                break;
            case 2:
                ((SurfaceTexture) obj).release();
                break;
            case AudioSourceCapability.d :
                AirPlaySession.b1((Socket) obj);
                break;
            case 4:
                ((LogActivity) obj).h();
                break;
            default:
                ((BtDiscovery) obj).l();
                break;
        }
    }
}
