/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 g0。消费回调：处理上游产生的事件。（服务于 AirPlaySession）
 *   This file defines g0. Consumer callback handling an upstream event. (serves AirPlaySession.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.function.BiConsumer;



/**
 * 消费回调：处理上游产生的事件。（服务于 AirPlaySession）
 *
 * Consumer callback handling an upstream event. (serves AirPlaySession.)
 */
public final  class g0 implements BiConsumer {
    public  AirPlaySession a;
    public  long b;
    public  EventChannel c;
    public  int d;

    @Override 
    public final void accept(Object obj, Object obj2) {
        this.a.J0(this.b, this.c, this.d, (EventChannel.b) obj, (Throwable) obj2);
    }
}
