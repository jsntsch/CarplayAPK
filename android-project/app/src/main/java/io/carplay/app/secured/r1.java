/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 r1。线程异常兜底：记录并处理未捕获异常。
 *   This file defines r1. Thread uncaught-exception handler recording and containing crashes.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.CarPlusApplication;



/**
 * 线程异常兜底：记录并处理未捕获异常。
 *
 * Thread uncaught-exception handler recording and containing crashes.
 */
public final  class r1 implements Thread.UncaughtExceptionHandler {
    public  CarPlusApplication a;
    public  Thread.UncaughtExceptionHandler b;

    @Override 
    public final void uncaughtException(Thread thread, Throwable th) {
        this.a.b(this.b, thread, th);
    }
}
