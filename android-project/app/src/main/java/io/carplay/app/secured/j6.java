/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 j6。回调接口：定义单一职责的调用契约，由调用方实现。
 *   This file defines j6. Callback interface defining a single-responsibility contract implemented by callers.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;


/**
 * 回调接口：定义单一职责的调用契约，由调用方实现。
 *
 * Callback interface defining a single-responsibility contract implemented by callers.
 */
public interface j6 {

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class a extends ProtoMessageBase {
        private final boolean a;
        private final boolean b;
        private final boolean c;
        private final boolean d;
        private final boolean e;
        private final boolean f;
        private final boolean g;

        public a(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7) {
            this.a = z;
            this.b = z2;
            this.c = z3;
            this.d = z4;
            this.e = z5;
            this.f = z6;
            this.g = z7;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.a == aVar.a && this.b == aVar.b && this.c == aVar.c && this.d == aVar.d && this.e == aVar.e && this.f == aVar.f && this.g == aVar.g;
        }

        public boolean g() {
            return this.a;
        }

        public boolean h() {
            return this.c;
        }

        public final int hashCode() {
            boolean z = this.a;
            boolean z2 = this.b;
            boolean z3 = this.c;
            boolean z4 = this.d;
            boolean z5 = this.e;
            boolean z6 = this.f;
            boolean z7 = this.g;
            return Boolean.hashCode(z7) + ((Boolean.hashCode(z6) + ((Boolean.hashCode(z5) + ((Boolean.hashCode(z4) + ((Boolean.hashCode(z3) + ((Boolean.hashCode(z2) + (Boolean.hashCode(z) * 31)) * 31)) * 31)) * 31)) * 31)) * 31);
        }

        public boolean i() {
            return this.g;
        }

        public boolean j() {
            return this.d;
        }

        public boolean k() {
            return this.f;
        }

        public boolean l() {
            return this.b;
        }

        public boolean m() {
            return this.e;
        }

        public final String toString() {
            Object[] objArr = {Boolean.valueOf(this.a), Boolean.valueOf(this.b), Boolean.valueOf(this.c), Boolean.valueOf(this.d), Boolean.valueOf(this.e), Boolean.valueOf(this.f), Boolean.valueOf(this.g)};
            String[] strArrSplit = "a;b;c;d;e;f;g".length() == 0 ? new String[0] : "a;b;c;d;e;f;g".split(";");
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
     * 枚举：本子系统使用的状态/类型取值集合。
     *
     * Enumeration: state/type values used by this subsystem.
     */
    public enum b {
        WRITTEN,
        OBSERVED,
        UNCONFIRMED
    }

    default boolean b(boolean z) {
        return false;
    }

    default boolean c(RemoteCommandHandler.d dVar) {
        return false;
    }

    boolean e() throws IOException;

    boolean f() throws IOException;

    boolean g() throws IOException;

    boolean i() throws IOException;

    a k();

    boolean l(int i);
}
