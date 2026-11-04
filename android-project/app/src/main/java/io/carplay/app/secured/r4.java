/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 r4。消费回调：处理上游产生的事件。
 *   This file defines r4. Consumer callback handling an upstream event.
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
public final  class r4 implements Consumer {
    public final  int a;
    public  DisplaySession.c b;

    
    public r4(int i) {
        this.a = i;
    }

    @Override 
    public final void accept(Object obj) {
        int i = this.a;
        this.b.a((String) obj);
    }
}
