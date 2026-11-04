/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 a5。后台任务：在独立线程中执行一次异步动作。（服务于 PairingPreparationService）
 *   This file defines a5. Background task executing one asynchronous step on its own thread. (serves PairingPreparationService.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.PairingPreparationService;



/**
 * 后台任务：在独立线程中执行一次异步动作。（服务于 PairingPreparationService）
 *
 * Background task executing one asynchronous step on its own thread. (serves PairingPreparationService.)
 */
public final  class a5 implements Runnable {
    public final  int a;
    public  PairingPreparationService b;

    public  a5(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        int i = this.a;
        PairingPreparationService pairingPreparationService = this.b;
        switch (i) {
            case 0:
                pairingPreparationService.g();
                break;
            default:
                pairingPreparationService.d();
                break;
        }
    }
}
