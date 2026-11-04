/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 n6。内部实现类：本子系统使用的数据结构与辅助方法。
 *   This file defines n6. Internal implementation class holding data structures and helpers for this subsystem.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 内部实现类：本子系统使用的数据结构与辅助方法。
 *
 * Internal implementation class holding data structures and helpers for this subsystem.
 */
abstract class n6 {

    /**
     * 接口实现：实现 e2 定义的契约。
     *
     * Implementation of the e2 interface.
     */
    public static class a implements e2 {
        private final int a;
        private final int b;
        private final String c;
        private final f2 d;

        public a(int i, int i2, String str, f2 f2Var) {
            this.a = i;
            this.b = i2;
            this.c = str;
            this.d = f2Var;
        }
    }

    public static e2 a(q2 q2Var, int i, f2 f2Var) {
        return new a(q2Var.d() * 4, i, q2Var.c(), f2Var);
    }
}
