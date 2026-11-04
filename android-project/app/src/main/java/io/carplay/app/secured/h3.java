/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 h3。后台任务：在独立线程中执行一次异步动作。（服务于 LogActivity）
 *   This file defines h3. Background task executing one asynchronous step on its own thread. (serves LogActivity.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.LogActivity;



/**
 * 后台任务：在独立线程中执行一次异步动作。（服务于 LogActivity）
 *
 * Background task executing one asynchronous step on its own thread. (serves LogActivity.)
 */
public final  class h3 implements Runnable {
    public final  int a;
    public  LogActivity b;
    public  String c;

    public  h3(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        switch (this.a) {
            case 0:
                this.b.k(this.c);
                break;
            default:
                this.b.i(this.c);
                break;
        }
    }
}
