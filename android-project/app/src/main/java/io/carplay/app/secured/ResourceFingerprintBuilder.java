/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 ResourceFingerprintBuilder。资源指纹构造：按 resourceID / entity 生成稳定的比对指纹。
 *   This file defines ResourceFingerprintBuilder. Resource fingerprint builder producing stable digests from resourceID / entity.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 资源指纹构造：按 resourceID / entity 生成稳定的比对指纹。
 *
 * Resource fingerprint builder producing stable digests from resourceID / entity.
 */
final class ResourceFingerprintBuilder {
    private static final Set<String> a = Set.of("resourceID", "entity", "permanentEntity", "transferType", "transferPriority", "takeConstraint", "borrowConstraint", "unborrowConstraint", "appStateID", "state", "speechMode");

    private ResourceFingerprintBuilder() {
    }

    private static String a(Object obj) {
        if (!(obj instanceof String)) {
            return "NON_STRING";
        }
        String str = (String) obj;
        if (str.length() > 128) {
            return "OVERSIZE";
        }
        try {
            byte[] bArrDigest = MessageDigest.getInstance("SHA-256").digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {
                sb.append(Character.forDigit((bArrDigest[i] >>> 4) & 15, 16));
                sb.append(Character.forDigit(bArrDigest[i] & 15, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException unused) {
            return "HASH_UNAVAILABLE";
        }
    }

    private static int b(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Boolean) {
            return 1;
        }
        if ((obj instanceof Byte) || (obj instanceof Short) || (obj instanceof Integer) || (obj instanceof Long) || (obj instanceof BigInteger)) {
            return 2;
        }
        if (obj instanceof Number) {
            return 3;
        }
        if (obj instanceof String) {
            return 4;
        }
        if (obj instanceof byte[]) {
            return 5;
        }
        if (obj instanceof List) {
            return 6;
        }
        return obj instanceof Map ? 7 : 8;
    }

    
    public static String c(Map<?, ?> map, SessionStateMachine.a aVar) {
        String str;
        String strH = aVar.h();
        strH.getClass();
        switch (strH) {
            case "INITIAL_RESOURCE":
                str = "initialPermanentEntity";
                break;
            case "RESOURCE":
                str = "resources";
                break;
            case "APP_STATE":
                str = "appStates";
                break;
            default:
                str = "";
                break;
        }
        if (map != null && !str.isEmpty()) {
            Object obj = map.get(str);
            if (obj instanceof List) {
                List list = (List) obj;
                int i = aVar.i();
                if (i >= 0 && i < list.size()) {
                    Object obj2 = list.get(i);
                    if (obj2 instanceof Map) {
                        Map map2 = (Map) obj2;
                        StringBuilder sb = new StringBuilder();
                        int size = map2.size();
                        Iterator<String> it = a.iterator();
                        while (it.hasNext()) {
                            if (map2.containsKey(it.next())) {
                                size--;
                            }
                        }
                        if (size == 0) {
                            return null;
                        }
                        int i2 = 0;
                        for (Map.Entry entry : ((Map<?, ?>) map2).entrySet()) {
                            Object key = entry.getKey();
                            if (key instanceof String) {
                                if (a.contains((String) key)) {
                                    continue;
                                }
                            }
                            int i3 = i2 + 1;
                            if (i2 > 0) {
                                sb.append(',');
                            }
                            sb.append(a(entry.getKey()));
                            sb.append(':');
                            sb.append(b(entry.getValue()));
                            if (i3 == 4) {
                                i2 = i3;
                                return "MODE_INFO_UNKNOWN_SCHEMA kind=" + aVar.h() + " row=" + i + " key_fingerprints=" + String.valueOf(sb) + " unknown_fields=" + size + " emitted=" + i2 + " truncated=" + (size > i2) + " keys_logged=false values_logged=false";
                            }
                            i2 = i3;
                        }
                        if (size > i2) {
                        }
                        return "MODE_INFO_UNKNOWN_SCHEMA kind=" + aVar.h() + " row=" + i + " key_fingerprints=" + String.valueOf(sb) + " unknown_fields=" + size + " emitted=" + i2 + " truncated=" + (size > i2) + " keys_logged=false values_logged=false";
                    }
                }
            }
        }
        return null;
    }
}
