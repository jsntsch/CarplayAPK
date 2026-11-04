/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 p0。后台任务：在独立线程中执行一次异步动作。
 *   This file defines p0. Background task executing one asynchronous step on its own thread.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.bluetooth.BluetoothServerSocket;
import android.net.Uri;
import io.carplay.app.prototype.LogActivity;
import io.carplay.app.prototype.PairingSession;
import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * 后台任务：在独立线程中执行一次异步动作。
 *
 * Background task executing one asynchronous step on its own thread.
 */
public final  class p0 implements Runnable {
    public final  int a;
    public  Object b;
    public  Object c;

    public  p0(DisplaySession t4Var, CarLifeFrame.a aVar) {
        this.a = 3;
        this.b = t4Var;
        this.c = aVar;
    }

    @Override 
    public final void run() {
        switch (this.a) {
            case 0:
                ((AirPlaySession) this.b).e1((KeepAliveServer) this.c);
                break;
            case AudioSourceCapability.c :
                ((AirPlaySession) this.b).Z0((AirPlaySession.d) this.c);
                break;
            case 2:
                ((LogActivity) this.b).j((Uri) this.c);
                break;
            case AudioSourceCapability.d :
                ((DisplaySession) this.b).E((CarLifeFrame.a) this.c);
                break;
            case 4:
                ((BtDiscovery) this.b).p((BluetoothServerSocket) this.c);
                break;
            case 5:
                ((PairingSession) this.b).l1((AtomicInteger) this.c);
                break;
            default:
                ((WirelessCarPlayService) this.b).V1((Iap2Bootstrap.a) this.c);
                break;
        }
    }

    public  p0(int i) {
        this.a = i;
    }
}
