/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 w6。后台任务：在独立线程中执行一次异步动作。（服务于 PairingSession）
 *   This file defines w6. Background task executing one asynchronous step on its own thread. (serves PairingSession.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.bluetooth.BluetoothSocket;
import io.carplay.app.prototype.PairingSession;



/**
 * 后台任务：在独立线程中执行一次异步动作。（服务于 PairingSession）
 *
 * Background task executing one asynchronous step on its own thread. (serves PairingSession.)
 */
public final  class w6 implements Runnable {
    public final  int a;
    public  PairingSession b;
    public  BluetoothSocket c;

    public  w6(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        switch (this.a) {
            case 0:
                this.b.O0(this.c);
                break;
            case AudioSourceCapability.c :
                this.b.V0(this.c);
                break;
            case 2:
                this.b.g1(this.c);
                break;
            default:
                this.b.k1(this.c);
                break;
        }
    }
}
