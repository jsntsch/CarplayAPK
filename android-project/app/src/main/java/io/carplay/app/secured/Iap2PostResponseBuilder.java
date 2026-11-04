/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 Iap2PostResponseBuilder。iAP2 POST 响应构造：生成 iAPSendMessage 信封与响应字段。
 *   This file defines Iap2PostResponseBuilder. iAP2 POST response builder: builds the iAPSendMessage envelope and response fields.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.util.Map;
import java.util.Set;



/**
 * iAP2 POST 响应构造：生成 iAPSendMessage 信封与响应字段。
 *
 * iAP2 POST response builder: builds the iAPSendMessage envelope and response fields.
 */
final class Iap2PostResponseBuilder {
    private Iap2PostResponseBuilder() {
    }

    public static byte[] a(Object obj) throws IOException {
        Object obj2;
        if (!(obj instanceof byte[])) {
            if (!(obj instanceof Map)) {
                SyntheticRefs.c("IAP2_POST_RESPONSE_ENVELOPE");
                return null;
            }
            Map map = (Map) obj;
            Object obj3 = map.get("type");
            if (obj3 != null && !"iAPSendMessage".equals(obj3)) {
                SyntheticRefs.c("IAP2_POST_RESPONSE_TYPE");
                return null;
            }
            if (!Set.of("type", "params", "data", "status").containsAll(map.keySet())) {
                SyntheticRefs.c("IAP2_POST_RESPONSE_FIELDS");
                return null;
            }
            if (map.containsKey("status")) {
                Object obj4 = map.get("status");
                if ((!(obj4 instanceof Byte) && !(obj4 instanceof Short) && !(obj4 instanceof Integer) && !(obj4 instanceof Long)) || ((Number) obj4).longValue() != 0) {
                    SyntheticRefs.c("IAP2_POST_RESPONSE_STATUS");
                    return null;
                }
            }
            Object obj5 = map.get("params");
            if (obj5 != null && !(obj5 instanceof Map)) {
                SyntheticRefs.c("IAP2_POST_RESPONSE_PARAMS");
                return null;
            }
            if (obj5 instanceof Map) {
                Map map2 = (Map) obj5;
                if (!Set.of("data").containsAll(map2.keySet())) {
                    SyntheticRefs.c("IAP2_POST_RESPONSE_FIELDS");
                    return null;
                }
                obj2 = map2.get("data");
            } else {
                obj2 = null;
            }
            if (obj2 == null) {
                obj = map.get("data");
            } else {
                if (map.containsKey("data")) {
                    SyntheticRefs.c("IAP2_POST_RESPONSE_AMBIGUOUS");
                    return null;
                }
                obj = obj2;
            }
        }
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            byte[] bArr = (byte[]) obj;
            if (bArr.length <= 16384) {
                if (bArr.length == 0) {
                    return null;
                }
                return (byte[]) bArr.clone();
            }
        }
        SyntheticRefs.c("IAP2_POST_RESPONSE_DATA");
        return null;
    }
}
