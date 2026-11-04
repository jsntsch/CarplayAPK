/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 q0。后台任务：在独立线程中执行一次异步动作。（服务于 PairingSession）
 *   This file defines q0. Background task executing one asynchronous step on its own thread. (serves PairingSession.)
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;



/**
 * 后台任务：在独立线程中执行一次异步动作。（服务于 PairingSession）
 *
 * Background task executing one asynchronous step on its own thread. (serves PairingSession.)
 */
public final  class q0 implements Runnable {
    public  PairingSession a;
    public  InputStream b;
    public  ArrayBlockingQueue c;
    public  PairingSession.j d;

    @Override 
    public final void run() {
        this.a.a1(this.b, this.c, this.d);
    }
}
