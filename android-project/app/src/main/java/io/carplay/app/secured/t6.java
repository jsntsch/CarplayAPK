/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 t6。消费回调：处理上游产生的事件。（服务于 PairingSession）
 *   This file defines t6. Consumer callback handling an upstream event. (serves PairingSession.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.bluetooth.BluetoothSocket;
import io.carplay.app.prototype.PairingSession;
import java.util.function.Consumer;



/**
 * 消费回调：处理上游产生的事件。（服务于 PairingSession）
 *
 * Consumer callback handling an upstream event. (serves PairingSession.)
 */
public final  class t6 implements Consumer {
    public final  int a;
    public  PairingSession b;

    public  t6(int i) {
        this.a = i;
    }

    @Override 
    public final void accept(Object obj) {
        int i = this.a;
        PairingSession pairingSession = this.b;
        switch (i) {
            case 0:
                pairingSession.S1((String) obj);
                break;
            case AudioSourceCapability.c :
                pairingSession.U0((BluetoothSocket) obj);
                break;
            case 2:
                pairingSession.S1((String) obj);
                break;
            case AudioSourceCapability.d :
                pairingSession.f1((BluetoothSocket) obj);
                break;
            default:
                pairingSession.j1((BluetoothSocket) obj);
                break;
        }
    }
}
