/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 p2。内部实现类：本子系统使用的数据结构与辅助方法。
 *   This file defines p2. Internal implementation class holding data structures and helpers for this subsystem.
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
public class p2 implements e2 {
    private final String a;
    private final int b;
    private final Object c;
    private final f2 d;

    public p2(String str, int i, Object obj, f2 f2Var) {
        this.a = str;
        this.b = i;
        this.c = obj;
        if (obj instanceof f2) {
            SyntheticRefs.b("params should not be CryptoServicePurpose");
            throw null;
        }
        this.d = f2Var;
    }
}
