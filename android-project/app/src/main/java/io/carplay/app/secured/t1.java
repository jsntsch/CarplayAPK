/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 t1。协议命令号常量表：0x180xx / 0x100xx 命令与响应的取值定义。
 *   This file defines t1. Protocol command-ID table defining the 0x180xx / 0x100xx commands and responses.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.lang.invoke.VarHandle;
import java.util.Map;


/**
 * 协议命令号常量表：0x180xx / 0x100xx 命令与响应的取值定义。
 *
 * Protocol command-ID table defining the 0x180xx / 0x100xx commands and responses.
 */
public final class t1 {
    public static final int a = 98305;
    public static final int b = 65538;
    public static final int c = 98307;
    public static final int d = 65540;
    public static final int e = 98311;
    public static final int f = 65544;
    public static final int g = 98313;
    public static final int h = 65617;
    public static final int i = 98386;
    public static final int j = 65642;
    public static final int k = 98411;
    public static final int l = 65644;
    public static final int m = 98413;
    public static final int n = 65646;

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class a extends ProtoMessageBase {
        private final int a;
        private final int b;

        public a(int i, int i2) {
            this.a = i;
            this.b = i2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.a == aVar.a && this.b == aVar.b;
        }

        public int g() {
            return this.a;
        }

        public int h() {
            return this.b;
        }

        public final int hashCode() {
            return (this.a * 31) + this.b;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b)};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
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
        private final int a;
        private final int b;
        private final int c;

        public b(int i, int i2, int i3) {
            this.a = i;
            this.b = i2;
            this.c = i3;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && this.b == bVar.b && this.c == bVar.c;
        }

        public int g() {
            return this.c;
        }

        public int h() {
            return this.b;
        }

        public final int hashCode() {
            int i = this.a;
            return (((i * 31) + this.b) * 31) + this.c;
        }

        public int i() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
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

    private t1() {
    }

    public static byte[] b(String str) {
        return new ProtobufReader.a().e(1, str).g();
    }

    public static byte[] c(int i2, int i3, int i4) {
        return new ProtobufReader.a().c(1, i2).c(2, i3).c(3, i4).g();
    }

    public static byte[] d(Map<String, Integer> map) {
        ProtobufReader.a aVarC = new ProtobufReader.a().c(1, map.size());
        s1 s1Var = new s1(0);
        s1Var.b = aVarC;
        VarHandle.storeStoreFence();
        map.forEach(s1Var);
        return aVarC.g();
    }

    public static byte[] e(boolean z) {
        return new ProtobufReader.a().a(1, z).g();
    }

    
    public static  void f(ProtobufReader.a aVar, String str, Integer num) {
        aVar.d(2, new ProtobufReader.a().e(1, str).c(2, num.intValue()));
    }

    public static a g(byte[] bArr) throws ProtocolIOException {
        ProtobufReader.e eVarA = ProtobufReader.a(bArr);
        int i2 = eVarA.i(1);
        int i3 = eVarA.i(2);
        if (i2 >= 1 && i3 >= 0) {
            return new a(i2, i3);
        }
        SyntheticRefs.f("invalid version");
        return null;
    }

    public static b h(byte[] bArr) throws ProtocolIOException {
        ProtobufReader.e eVarA = ProtobufReader.a(bArr);
        int i2 = eVarA.i(1);
        int i3 = eVarA.i(2);
        int i4 = eVarA.i(3);
        if (i2 > 0 && i3 > 0 && i4 > 0) {
            return new b(i2, i3, i4);
        }
        SyntheticRefs.f("invalid video format");
        return null;
    }

    public static byte[] i(String str) {
        return new ProtobufReader.a().e(1, str).g();
    }

    public static byte[] j(int i2, int i3) {
        if (i2 >= 1 && i3 >= 0) {
            return new ProtobufReader.a().c(1, i2).c(2, i3).g();
        }
        SyntheticRefs.b("invalid protocol version");
        return null;
    }

    public static byte[] k(int i2, int i3, int i4) {
        if (i2 > 0 && i3 > 0 && i4 > 0) {
            return new ProtobufReader.a().c(1, i2).c(2, i3).c(3, i4).g();
        }
        SyntheticRefs.b("invalid video format");
        return null;
    }
}
