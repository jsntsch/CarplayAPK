/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 k0。消费回调：处理上游产生的事件。
 *   This file defines k0. Consumer callback handling an upstream event.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.function.Consumer;



/**
 * 消费回调：处理上游产生的事件。
 *
 * Consumer callback handling an upstream event.
 */
public final  class k0 implements Consumer {
    public final  int a = 0;
    public  AirPlaySession.n b;

    public  k0(AirPlaySession.n nVar) {
        this.b = nVar;
    }

    @Override 
    public final void accept(Object obj) {
        int i = this.a;
        this.b.a((String) obj);
    }

    public  k0() {
    }
}
