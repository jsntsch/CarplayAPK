/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 p。后台任务：在独立线程中执行一次异步动作。（服务于 ExecutorService）
 *   This file defines p. Background task executing one asynchronous step on its own thread. (serves ExecutorService.)
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.app.Activity;



/**
 * 后台任务：在独立线程中执行一次异步动作。（服务于 ExecutorService）
 *
 * Background task executing one asynchronous step on its own thread. (serves ExecutorService.)
 */
public final  class p implements Runnable {
    public final  int a;
    public  Object b;

    public  p(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        int i = this.a;
        Object obj = this.b;
        switch (i) {
            case 0:
                ((WifiNetworkMonitor) obj).u();
                break;
            case 2:
                UiFactory.r((Activity) obj);
                break;
            default:
                int i2 = PairingSession.d.d;
                ((PairingSession) obj).H0();
                break;
        }
    }
}
