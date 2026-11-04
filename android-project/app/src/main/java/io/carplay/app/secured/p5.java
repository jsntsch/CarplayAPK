/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 p5。键值映射辅助：维护有序映射并提供带默认值的查询。
 *   This file defines p5. Key-value mapping helper offering ordered iteration and defaulted lookups.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Security;
import java.util.Map;


/**
 * 键值映射辅助：维护有序映射并提供带默认值的查询。
 *
 * Key-value mapping helper offering ordered iteration and defaulted lookups.
 */
public abstract class p5 {
    private static final ThreadLocal a = new ThreadLocal();

    /**
     * 接口实现：实现 PrivilegedAction 定义的契约。
     *
     * Implementation of the PrivilegedAction interface.
     */
    public static class a implements PrivilegedAction {
        final  String a;

        public a(String str) {
            this.a = str;
        }

        @Override 
        public Object run() {
            return Security.getProperty(this.a);
        }
    }

    /**
     * 接口实现：实现 PrivilegedAction 定义的契约。
     *
     * Implementation of the PrivilegedAction interface.
     */
    public static class b implements PrivilegedAction {
        final  String a;

        public b(String str) {
            this.a = str;
        }

        @Override 
        public Object run() {
            return System.getProperty(this.a);
        }
    }

    public static String a(String str) {
        String str2;
        String str3 = (String) AccessController.doPrivileged(new a(str));
        if (str3 != null) {
            return str3;
        }
        Map map = (Map) a.get();
        return (map == null || (str2 = (String) map.get(str)) == null) ? (String) AccessController.doPrivileged(new b(str)) : str2;
    }

    public static boolean b(String str) {
        try {
            return c(a(str));
        } catch (AccessControlException unused) {
            return false;
        }
    }

    private static boolean c(String str) {
        if (str == null || str.length() != 4) {
            return false;
        }
        return (str.charAt(0) == 't' || str.charAt(0) == 'T') && (str.charAt(1) == 'r' || str.charAt(1) == 'R') && ((str.charAt(2) == 'u' || str.charAt(2) == 'U') && (str.charAt(3) == 'e' || str.charAt(3) == 'E'));
    }
}
