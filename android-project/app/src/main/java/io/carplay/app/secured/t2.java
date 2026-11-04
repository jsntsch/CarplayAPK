/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 t2。连接失败日志：格式化 CONNECT_FAILURE 的 phase 与原因。
 *   This file defines t2. Connection-failure logging: formats CONNECT_FAILURE phase and reason.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.ArrayList;
import java.util.List;


/**
 * 连接失败日志：格式化 CONNECT_FAILURE 的 phase 与原因。
 *
 * Connection-failure logging: formats CONNECT_FAILURE phase and reason.
 */
public final class t2 {
    private t2() {
    }

    public static List<String> a(String str, Throwable th) {
        if (str == null || !str.matches("[A-Z0-9_]{1,48}")) {
            str = "UNKNOWN";
        }
        ArrayList arrayList = new ArrayList();
        int i = 0;
        while (th != null && i < 4) {
            arrayList.add("CONNECT_FAILURE phase=" + str + " cause=" + i + " type=" + th.getClass().getName());
            
            if (th.getMessage() != null) {
                String msg = th.getMessage();
                arrayList.add("  msg=" + (msg.length() > 220 ? msg.substring(0, 220) : msg));
            }
            if (th instanceof EventChannel.d) {
                EventChannel.d dVar = (EventChannel.d) th;
                arrayList.add("CONTROL_RESPONSE_REJECTED op=" + dVar.b + " status=" + dVar.a);
            }
            int i2 = 0;
            for (StackTraceElement stackTraceElement : th.getStackTrace()) {
                String className = stackTraceElement.getClassName();
                if (className.startsWith("io.carplay.") || className.startsWith("org.bouncycastle.") || className.startsWith("java.security.") || className.startsWith("javax.crypto.") || className.startsWith("android.net.")) {
                    int i3 = i2 + 1;
                    if (i2 < 8) {
                        arrayList.add("  at " + className + "." + stackTraceElement.getMethodName() + ":" + stackTraceElement.getLineNumber());
                    }
                    i2 = i3;
                }
            }
            i++;
            th = th.getCause();
        }
        return List.copyOf(arrayList);
    }
}
