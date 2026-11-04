/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 o。过滤谓词：按条件筛选候选对象。
 *   This file defines o. Predicate filtering candidate objects by a condition.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.net.LinkAddress;
import java.util.function.Predicate;



/**
 * 过滤谓词：按条件筛选候选对象。
 *
 * Predicate filtering candidate objects by a condition.
 */
public final  class o implements Predicate {
    public  boolean a;

    @Override 
    public final boolean test(Object obj) {
        return WifiNetworkMonitor.t(this.a, (LinkAddress) obj);
    }
}
