/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 c。惰性取值：按需提供对象。（服务于 AvcEncoder）
 *   This file defines c. Supplier providing a value on demand. (serves AvcEncoder.)
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import java.util.function.Supplier;



/**
 * 惰性取值：按需提供对象。（服务于 AvcEncoder）
 *
 * Supplier providing a value on demand. (serves AvcEncoder.)
 */
public final  class c implements Supplier {
    public  AvcEncoder a;
    public  int b;

    @Override 
    public final Object get() {
        return this.a.z(this.b);
    }
}
