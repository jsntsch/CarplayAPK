/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 o3。后台任务：在独立线程中执行一次异步动作。（服务于 MainActivity）
 *   This file defines o3. Background task executing one asynchronous step on its own thread. (serves MainActivity.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.MainActivity;



/**
 * 后台任务：在独立线程中执行一次异步动作。（服务于 MainActivity）
 *
 * Background task executing one asynchronous step on its own thread. (serves MainActivity.)
 */
public final  class o3 implements Runnable {
    public final  int a;
    public  MainActivity b;

    public  o3(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        int i = this.a;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.A1();
                break;
            case AudioSourceCapability.c :
                mainActivity.U1();
                break;
            case 2:
                mainActivity.W1();
                break;
            case AudioSourceCapability.d :
                mainActivity.n1();
                break;
            case 4:
                mainActivity.o1();
                break;
            case 5:
                mainActivity.p1();
                break;
            case 6:
                mainActivity.Z1();
                break;
            case 7:
                mainActivity.n2();
                break;
            case 8:
                mainActivity.m2();
                break;
            case 9:
                mainActivity.Q0();
                break;
            case 10:
                mainActivity.s2();
                break;
            case 11:
                mainActivity.a1();
                break;
            case 12:
                mainActivity.d1();
                break;
            case 13:
                mainActivity.k2();
                break;
            case 17:
                mainActivity.j1();
                break;
            case 18:
                mainActivity.k1();
                break;
            case 19:
                mainActivity.l1();
                break;
            case 20:
                mainActivity.j2();
                break;
            case 21:
                mainActivity.d2();
                break;
            case 22:
                mainActivity.f2();
                break;
            default:
                mainActivity.x1();
                break;
        }
    }
}
