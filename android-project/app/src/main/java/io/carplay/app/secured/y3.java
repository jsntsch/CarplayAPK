/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 y3。后台任务：在独立线程中执行一次异步动作。（服务于 MainActivity）
 *   This file defines y3. Background task executing one asynchronous step on its own thread. (serves MainActivity.)
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
public final  class y3 implements Runnable {
    public  MainActivity a;
    public  int b;

    @Override 
    public final void run() {
        this.a.i1(this.b);
    }
}
