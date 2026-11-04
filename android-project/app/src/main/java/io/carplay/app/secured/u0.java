/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 u0。执行器：把任务提交到指定线程/线程池。（服务于 AirplayServiceProbe）
 *   This file defines u0. Executor submitting tasks to a thread or pool. (serves AirplayServiceProbe.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.AirplayServiceProbe;
import java.util.concurrent.Executor;



/**
 * 执行器：把任务提交到指定线程/线程池。（服务于 AirplayServiceProbe）
 *
 * Executor submitting tasks to a thread or pool. (serves AirplayServiceProbe.)
 */
public final  class u0 implements Executor {
    public  AirplayServiceProbe a;

    @Override 
    public final void execute(Runnable runnable) {
        this.a.v(runnable);
    }
}
