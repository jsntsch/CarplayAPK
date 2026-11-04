/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 l。后台任务：在独立线程中执行一次异步动作。
 *   This file defines l. Background task executing one asynchronous step on its own thread.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;



/**
 * 后台任务：在独立线程中执行一次异步动作。
 *
 * Background task executing one asynchronous step on its own thread.
 */
public final  class l implements Runnable {
    public final  int a;
    public  WifiNetworkMonitor.a b;

    public  l(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        int i = this.a;
        WifiNetworkMonitor.a aVar = this.b;
        switch (i) {
            case 0:
                aVar.f();
                break;
            default:
                aVar.d();
                break;
        }
    }
}
