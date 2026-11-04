/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 s0。后台任务：在独立线程中执行一次异步动作。（服务于 Network）
 *   This file defines s0. Background task executing one asynchronous step on its own thread. (serves Network.)
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.net.Network;
import io.carplay.app.secured.Iap2Csm;



/**
 * 后台任务：在独立线程中执行一次异步动作。（服务于 Network）
 *
 * Background task executing one asynchronous step on its own thread. (serves Network.)
 */
public final  class s0 implements Runnable {
    public  WirelessCarPlayService.a a;
    public  Network b;
    public  Iap2Csm.a c;
    public  String d;
    public  long e;
    public  PairingSession.i f;

    @Override 
    public final void run() {
        this.a.w(this.b, this.c, this.d, this.e, this.f);
    }
}
