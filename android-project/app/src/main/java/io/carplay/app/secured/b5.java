/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 b5。消费回调：处理上游产生的事件。（服务于 Service）
 *   This file defines b5. Consumer callback handling an upstream event. (serves Service.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.app.Service;
import io.carplay.app.prototype.PairingPreparationService;
import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.function.Consumer;



/**
 * 消费回调：处理上游产生的事件。（服务于 Service）
 *
 * Consumer callback handling an upstream event. (serves Service.)
 */
public final  class b5 implements Consumer {
    public final  int a;
    public  Service b;

    public  b5(int i) {
        this.a = i;
    }

    @Override 
    public final void accept(Object obj) {
        int i = this.a;
        Service service = this.b;
        switch (i) {
            case 0:
                ((PairingPreparationService) service).h((String) obj);
                break;
            default:
                ((WirelessCarPlayService) service).i1((String) obj);
                break;
        }
    }
}
