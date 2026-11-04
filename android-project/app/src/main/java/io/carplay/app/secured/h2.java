/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 h2。内部实现类：围绕 HashSet 组织状态与操作，服务于本子系统。
 *   This file defines h2. Internal implementation class grouping state and operations around HashSet.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.security.Permission;
import java.util.HashSet;
import java.util.Set;


/**
 * 内部实现类：围绕 HashSet 组织状态与操作，服务于本子系统。
 *
 * Internal implementation class grouping state and operations around HashSet.
 */
public class h2 extends Permission {
    private final Set<String> a;

    public h2(String str) {
        super(str);
        HashSet hashSet = new HashSet();
        this.a = hashSet;
        hashSet.add(str);
    }

    public boolean equals(Object obj) {
        return (obj instanceof h2) && this.a.equals(((h2) obj).a);
    }

    @Override 
    public String getActions() {
        return this.a.toString();
    }

    public int hashCode() {
        return this.a.hashCode();
    }

    @Override 
    public boolean implies(Permission permission) {
        if (!(permission instanceof h2)) {
            return false;
        }
        h2 h2Var = (h2) permission;
        return getName().equals(h2Var.getName()) || this.a.containsAll(h2Var.a);
    }
}
