/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 MfiAuthSession。MFi 认证会话：驱动 MFi 挑战-响应流程并维护认证代次。
 *   This file defines MfiAuthSession. MFi authentication session: drives the MFi challenge-response flow and tracks authentication generations.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Objects;


/**
 * MFi 认证会话：驱动 MFi 挑战-响应流程并维护认证代次。
 *
 * MFi authentication session: drives the MFi challenge-response flow and tracks authentication generations.
 */
public final class MfiAuthSession implements AutoCloseable {
    public static final int g = 65540;
    public static final int h = 98376;
    public static final int i = 65609;
    private final long a;
    private final l5 b;
    private final SecureRandom c;
    private d d;
    private c e;
    private String f;

    /**
     * MfiAuthSession 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in MfiAuthSession, carrying local implementation detail.
     */
    public static  class SwitchMap {
        static final  int[] a;

        static {
            int[] iArr = new int[l5.a.values().length];
            a = iArr;
            try {
                iArr[l5.a.VERIFIED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                a[l5.a.REJECTED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                a[l5.a.CONTRACT_UNAVAILABLE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class b extends ProtoMessageBase {
        private final long a;
        private final int b;
        private final String c;
        private final byte[] d;

        public b(long j, int i, String str, byte[] bArr) {
            byte[] bArr2 = (byte[]) bArr.clone();
            this.a = j;
            this.b = i;
            this.c = str;
            this.d = bArr2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.b == bVar.b && this.a == bVar.a && Objects.equals(this.c, bVar.c) && Objects.equals(this.d, bVar.d);
        }

        public byte[] g() {
            return (byte[]) this.d.clone();
        }

        public long h() {
            return this.a;
        }

        public final int hashCode() {
            int i = this.b;
            long j = this.a;
            String str = this.c;
            byte[] bArr = this.d;
            return Objects.hashCode(bArr) + ProtoMessageBase.b(str, ProtoMessageBase.a(j, i * 31, 31), 31);
        }

        public int i() {
            return this.b;
        }

        public String j() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), Integer.valueOf(this.b), this.c, this.d};
            String[] strArrSplit = "a;b;c;d".length() == 0 ? new String[0] : "a;b;c;d".split(";");
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

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class c extends ProtoMessageBase {
        private final String a;
        private final String b;
        private final String c;

        public c(String str, String str2, String str3) {
            this.a = str;
            this.b = str2;
            this.c = str3;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof c)) {
                return false;
            }
            c cVar = (c) obj;
            return Objects.equals(this.a, cVar.a) && Objects.equals(this.b, cVar.b) && Objects.equals(this.c, cVar.c);
        }

        public String g() {
            return this.a;
        }

        public String h() {
            return this.b;
        }

        public final int hashCode() {
            String str = this.a;
            String str2 = this.b;
            String str3 = this.c;
            return Objects.hashCode(str3) + ProtoMessageBase.b(str2, Objects.hashCode(str) * 31, 31);
        }

        public String i() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, this.c};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(c.class, sb, "[");
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
    public enum d {
        WAITING_FOR_MD_INFO,
        READY,
        CHALLENGE_ISSUED,
        VERIFYING,
        VERIFIED,
        REJECTED,
        CONTRACT_UNAVAILABLE,
        CLOSED
    }

    public MfiAuthSession(long j, l5 l5Var, SecureRandom secureRandom) {
        this.d = d.WAITING_FOR_MD_INFO;
        if (j < 1) {
            SyntheticRefs.b("positive session generation required");
            throw null;
        }
        this.a = j;
        Objects.requireNonNull(l5Var);
        this.b = l5Var;
        Objects.requireNonNull(secureRandom);
        this.c = secureRandom;
    }

    private static ProtobufReader.e i(byte[] bArr) throws ProtocolIOException {
        if (bArr != null) {
            return ProtobufReader.a(bArr);
        }
        SyntheticRefs.f("missing protobuf body");
        return null;
    }

    private void k(long j) throws ProtocolIOException {
        if (j == this.a) {
            return;
        }
        SyntheticRefs.f("stale or foreign generation");
    }

    private void l(d dVar) throws ProtocolIOException {
        d dVar2 = this.d;
        if (dVar2 == dVar) {
            return;
        }
        SyntheticRefs.f("unexpected modern proof state: ".concat(String.valueOf(dVar2)));
    }

    public synchronized void b(long j, int i2, byte[] bArr) throws ProtocolIOException {
        k(j);
        l(d.WAITING_FOR_MD_INFO);
        if (i2 != 65540) {
            throw new ProtocolIOException("expected MD_INFO");
        }
        ProtobufReader.e eVarI = i(bArr);
        this.e = new c(eVarI.f(4, ""), eVarI.f(14, ""), eVarI.f(20, ""));
        this.d = d.READY;
    }

    public synchronized l5.a c(long j, int i2, byte[] bArr) throws ProtocolIOException {
        l5.a aVarF;
        d dVar;
        k(j);
        l(d.CHALLENGE_ISSUED);
        this.d = d.VERIFYING;
        try {
            if (i2 != 65609) {
                throw new ProtocolIOException("expected MD_AUTHEN_RESPONSE");
            }
            String strJ = i(bArr).j(1);
            if (strJ.isEmpty()) {
                throw new ProtocolIOException("empty mobile proof");
            }
            try {
                aVarF = this.b.f(this.f, this.e.g(), this.e.h(), this.e.i(), strJ);
                if (aVarF == null) {
                    aVarF = l5.a.CONTRACT_UNAVAILABLE;
                }
            } catch (RuntimeException unused) {
                aVarF = l5.a.REJECTED;
            }
            this.f = null;
            if (this.d != d.VERIFYING) {
                return l5.a.REJECTED;
            }
            int i3 = SwitchMap.a[aVarF.ordinal()];
            if (i3 == 1) {
                dVar = d.VERIFIED;
            } else if (i3 == 2) {
                dVar = d.REJECTED;
            } else {
                if (i3 != 3) {
                    throw new IncompatibleClassChangeError();
                }
                dVar = d.CONTRACT_UNAVAILABLE;
            }
            this.d = dVar;
            return aVarF;
        } catch (ProtocolIOException e) {
            this.d = d.REJECTED;
            this.f = null;
            throw e;
        }
    }

    @Override 
    public synchronized void close() {
        this.d = d.CLOSED;
        this.f = null;
        this.e = null;
    }

    public long e() {
        return this.a;
    }

    public synchronized b f(long j) throws ProtocolIOException {
        String str;
        byte[] bArrG;
            k(j);
            l(d.READY);
            byte[] bArr = new byte[16];
            this.c.nextBytes(bArr);
            char[] cArr = new char[32];
            char[] charArray = "0123456789abcdef".toCharArray();
            for (int i2 = 0; i2 < 16; i2++) {
                byte b2 = bArr[i2];
                int i3 = i2 * 2;
                cArr[i3] = charArray[(b2 & 255) >>> 4];
                cArr[i3 + 1] = charArray[b2 & 15];
            }
            String str2 = new String(cArr);
            this.f = str2;
            str = "2.0;" + str2;
            bArrG = new ProtobufReader.a().e(1, str).g();
            this.d = d.CHALLENGE_ISSUED;
        return new b(this.a, h, str, bArrG);
    }

    public synchronized c g() {
        return this.e;
    }

    public synchronized d m() {
        return this.d;
    }

    public MfiAuthSession(long j, l5 l5Var) {
        this(j, l5Var, new SecureRandom());
    }

    public MfiAuthSession(long j) {
        this(j, l5.b());
    }
}
