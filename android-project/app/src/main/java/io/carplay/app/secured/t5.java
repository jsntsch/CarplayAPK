/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 t5。接收端配置消息：保存接收者 UUID、实验通道开关等 protobuf 字段。
 *   This file defines t5. Receiver configuration message storing receiver UUID and experimental-channel flags.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;


/**
 * 接收端配置消息：保存接收者 UUID、实验通道开关等 protobuf 字段。
 *
 * Receiver configuration message storing receiver UUID and experimental-channel flags.
 */
public final class t5 {

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class a extends ProtoMessageBase {
        private final b a;
        private final Map<String, String> b;
        private final boolean c;

        public a(b bVar, Map<String, String> map, boolean z) {
            this.a = bVar;
            this.b = map;
            this.c = z;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.c == aVar.c && Objects.equals(this.a, aVar.a) && Objects.equals(this.b, aVar.b);
        }

        public b g() {
            return this.a;
        }

        public boolean h() {
            return this.c;
        }

        public final int hashCode() {
            boolean z = this.c;
            b bVar = this.a;
            Map<String, String> map = this.b;
            return Objects.hashCode(map) + ((Objects.hashCode(bVar) + (Boolean.hashCode(z) * 31)) * 31);
        }

        public Map<String, String> i() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, Boolean.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(a.class, sb, "[");
            for (int i = 0; i < strArrSplit.length; i++) {
                sb.append(strArrSplit[i]);
                sb.append("=");
                sb.append(objArr[i]);
                if (i != strArrSplit.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class b extends ProtoMessageBase {
        private final String a;
        private final int b;

        public b(String str, int i) {
            this.a = str;
            this.b = i;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.b == bVar.b && Objects.equals(this.a, bVar.a);
        }

        public int g() {
            return this.b;
        }

        public String h() {
            return this.a;
        }

        public final int hashCode() {
            int i = this.b;
            return Objects.hashCode(this.a) + (i * 31);
        }

        public final String toString() {
            Object[] objArr = {this.a, Integer.valueOf(this.b)};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(b.class, sb, "[");
            for (int i = 0; i < strArrSplit.length; i++) {
                sb.append(strArrSplit[i]);
                sb.append("=");
                sb.append(objArr[i]);
                if (i != strArrSplit.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    private t5() {
    }

    public static a a(String str, b bVar, b bVar2, String str2, Supplier<b> supplier) {
        if (str == null || str.isBlank()) {
            SyntheticRefs.b("selected receiver key required");
            return null;
        }
        boolean z = false;
        if (b(bVar)) {
            return new a(bVar, Collections.EMPTY_MAP, false);
        }
        if (b(bVar2) && (str2 == null || str2.equals(str))) {
            z = true;
        }
        if (!z) {
            bVar2 = supplier.get();
        }
        if (!b(bVar2)) {
            SyntheticRefs.b("invalid generated native identity");
            return null;
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put(str.concat(".uuid"), bVar2.h());
        linkedHashMap.put(str.concat(".channel"), String.valueOf(bVar2.g()));
        if (z && str2 == null) {
            linkedHashMap.put("legacy_owner", str);
        }
        return new a(bVar2, Map.copyOf(linkedHashMap), z);
    }

    private static boolean b(b bVar) {
        if (bVar != null && bVar.h() != null) {
            try {
                UUID.fromString(bVar.h());
                if (bVar.g() >= 21000000 && bVar.g() <= 28999998 && bVar.g() != 21070001) {
                    return true;
                }
            } catch (IllegalArgumentException unused) {
            }
        }
        return false;
    }
}
