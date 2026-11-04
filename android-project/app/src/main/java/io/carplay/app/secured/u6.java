/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 u6。后台任务：在独立线程中执行一次异步动作。（服务于 PairingSession）
 *   This file defines u6. Background task executing one asynchronous step on its own thread. (serves PairingSession.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.PairingSession;



/**
 * 后台任务：在独立线程中执行一次异步动作。（服务于 PairingSession）
 *
 * Background task executing one asynchronous step on its own thread. (serves PairingSession.)
 */
public final  class u6 implements Runnable {
    public final  int a;
    public  PairingSession b;

    public  u6(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        int i = this.a;
        PairingSession pairingSession = this.b;
        switch (i) {
            case 0:
                pairingSession.T0();
                break;
            case AudioSourceCapability.c :
                pairingSession.W0();
                break;
            case 2:
                pairingSession.S0();
                break;
            case AudioSourceCapability.d :
                pairingSession.Q0();
                break;
            case 4:
                pairingSession.d1();
                break;
            case 5:
                pairingSession.L1();
                break;
            case 6:
                pairingSession.D1();
                break;
            case 7:
                pairingSession.P0();
                break;
            case 8:
                pairingSession.Z0();
                break;
            case 9:
                pairingSession.e1();
                break;
            case 10:
                pairingSession.h1();
                break;
            case 11:
                pairingSession.D1();
                break;
            default:
                pairingSession.Q1();
                break;
        }
    }
}
