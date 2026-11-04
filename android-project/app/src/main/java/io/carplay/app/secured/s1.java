/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 s1。消费回调：处理上游产生的事件。
 *   This file defines s1. Consumer callback handling an upstream event.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;



/**
 * 消费回调：处理上游产生的事件。
 *
 * Consumer callback handling an upstream event.
 */
public final  class s1 implements BiConsumer {
    public final  int a;
    public  Object b;

    public  s1(int i) {
        this.a = i;
    }

    @Override 
    public final void accept(Object obj, Object obj2) {
        int i = this.a;
        Object obj3 = this.b;
        switch (i) {
            case 0:
                t1.f((ProtobufReader.a) obj3, (String) obj, (Integer) obj2);
                break;
            default:
                EncryptProofNeg.g.i((LinkedHashMap) obj3, (String) obj, (Integer) obj2);
                break;
        }
    }
}
