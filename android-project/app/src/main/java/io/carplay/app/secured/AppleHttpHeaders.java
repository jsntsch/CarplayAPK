/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 AppleHttpHeaders。AirPlay HTTP 头常量：X-Apple-* 请求头的名称定义。
 *   This file defines AppleHttpHeaders. AirPlay HTTP header constants defining the X-Apple-* request headers.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.Map;




/**
 * AirPlay HTTP 头常量：X-Apple-* 请求头的名称定义。
 *
 * AirPlay HTTP header constants defining the X-Apple-* request headers.
 */
public final class AppleHttpHeaders {
    public static volatile boolean LOGGED;

    public static void Apply(Map map) {
        if (map != null) {
            try {
                String strServiceName = ControlChannel.ServiceName();
                String strDeviceMac = ControlChannel.DeviceMac();
                map.put("X-Apple-Client-Name", strServiceName);
                map.put("X-Apple-ProtocolVersion", "1");
                if (strDeviceMac != null) {
                    map.put("X-Apple-Device-ID", strDeviceMac);
                }
                if (LOGGED) {
                    return;
                }
                LOGGED = true;
                ControlChannel.Log("V170_XAPPLE_HEADERS applied");
            } catch (Throwable th) {
            }
        }
    }
}
