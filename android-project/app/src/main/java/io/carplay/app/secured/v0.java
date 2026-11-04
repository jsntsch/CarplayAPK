/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 v0。后台任务：在独立线程中执行一次异步动作。（服务于 AirplayServiceProbe）
 *   This file defines v0. Background task executing one asynchronous step on its own thread. (serves AirplayServiceProbe.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.AirplayServiceProbe;



/**
 * 后台任务：在独立线程中执行一次异步动作。（服务于 AirplayServiceProbe）
 *
 * Background task executing one asynchronous step on its own thread. (serves AirplayServiceProbe.)
 */
public final  class v0 implements Runnable {
    public final  int a;
    public  AirplayServiceProbe b;

    public  v0(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        int i = this.a;
        AirplayServiceProbe airplayServiceProbe = this.b;
        switch (i) {
            case 0:
                airplayServiceProbe.r();
                break;
            default:
                airplayServiceProbe.x();
                break;
        }
    }
}
