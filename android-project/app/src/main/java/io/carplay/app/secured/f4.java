/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 f4。内部实现类：围绕 StringBuilder 组织状态与操作，服务于本子系统。
 *   This file defines f4. Internal implementation class grouping state and operations around StringBuilder.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 内部实现类：围绕 StringBuilder 组织状态与操作，服务于本子系统。
 *
 * Internal implementation class grouping state and operations around StringBuilder.
 */
public final class f4 {
    private volatile boolean a;
    private long b;
    private long c;
    private long d;
    private long e;

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class a extends ProtoMessageBase {
        private final long a;

        public a(long j) {
            this.a = j;
        }

        public final boolean equals(Object obj) {
            return (obj instanceof a) && this.a == ((a) obj).a;
        }

        public long g() {
            return this.a;
        }

        public final int hashCode() {
            return Long.hashCode(this.a);
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a)};
            String[] strArrSplit = "a".length() == 0 ? new String[0] : "a".split(";");
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

    public synchronized void a(boolean z, long j, long j2) {
        this.a = z;
        this.c = j;
        this.d = j2;
        this.b = 0L;
        this.e++;
    }

    public boolean b() {
        return this.a;
    }

    public synchronized boolean c(a aVar) {
        if (this.a && this.b != 0 && aVar != null && aVar.g() == this.e) {
            this.a = false;
            return true;
        }
        return false;
    }

    
    public synchronized a d(long j) {
        a aVar;
        if (this.a) {
            long j2 = this.b;
            if (j2 == 0 || j <= j2) {
                aVar = null;
            } else {
                aVar = new a(this.e);
            }
        } else {
            aVar = null;
        }
        return aVar;
    }

    public synchronized void e(long j, long j2) {
        this.c = Math.max(this.c, j);
        this.d = Math.max(this.d, j2);
        if (this.a) {
            this.b = 0L;
            this.e++;
        }
    }

    public synchronized void f(long j, long j2, long j3) {
        if (this.a && j == this.c && j2 == this.d && this.b == 0 && j3 > 0) {
            this.b = j3;
        }
    }
}
