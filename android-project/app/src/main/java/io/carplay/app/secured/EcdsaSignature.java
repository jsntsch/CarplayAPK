/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 EcdsaSignature。ECDSA 签名校验：验证发布信息与回执的签名。
 *   This file defines EcdsaSignature. ECDSA signature verification for release metadata and receipts.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;



/**
 * ECDSA 签名校验：验证发布信息与回执的签名。
 *
 * ECDSA signature verification for release metadata and receipts.
 */
public final class EcdsaSignature {
    public byte[] a2;
    public byte[] b;

    /**
     * EcdsaSignature 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in EcdsaSignature, carrying local implementation detail.
     */
    public static final class a {
        private final EcdsaSignature a;
        private final Object b;
        private final AtomicBoolean c = new AtomicBoolean();

        private a(EcdsaSignature ecdsaSignature, Object obj) {
            this.a = ecdsaSignature;
            this.b = obj;
        }

        public static a b(EcdsaSignature ecdsaSignature, String str, long j, long j2, String str2, long j3, String str3, String str4, String str5, Object obj) throws GeneralSecurityException, IOException {
            if (obj == null || ecdsaSignature == null) {
                return null;
            }
            byte[] bArr = ecdsaSignature.a2;
            try {
                byte[] bArrDecode = Base64.getDecoder().decode(str);
                Signature signature = Signature.getInstance("SHA256withECDSA");
                signature.initVerify(KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(bArrDecode)));
                signature.update(bArr);
                if (!signature.verify(ecdsaSignature.b)) {
                    throw new GeneralSecurityException("release receipt signature invalid");
                }
                int i = -1;
                String[] strArrSplit = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bArr)).toString().split("\n", -1);
                if (strArrSplit.length < 8 || strArrSplit.length > 70 || !strArrSplit[0].equals("CARPLUS_RELEASES_V1") || !strArrSplit[strArrSplit.length - 1].isEmpty()) {
                    SyntheticRefs.c("release receipt schema");
                    return null;
                }
                long jB = EcdsaSignature.b(strArrSplit[1]);
                long jB2 = EcdsaSignature.b(strArrSplit[2]);
                long jB3 = EcdsaSignature.b(strArrSplit[3]);
                if (!strArrSplit[4].equals("io.carplay.app.prototype")) {
                    SyntheticRefs.c("release receipt package");
                    return null;
                }
                long jB4 = EcdsaSignature.b(strArrSplit[5]);
                int length = strArrSplit.length;
                int i2 = length - 7;
                b[] bVarArr = new b[i2];
                long j4 = 0;
                int i3 = 6;
                while (i3 < strArrSplit.length - 1) {
                    String[] strArrSplit2 = strArrSplit[i3].split("\t", i);
                    if (strArrSplit2.length != 4) {
                        SyntheticRefs.c("release receipt entry");
                        return null;
                    }
                    String[] strArr = strArrSplit;
                    long jB5 = EcdsaSignature.b(strArrSplit2[0]);
                    if (jB5 > j4 && jB5 <= 2147483647L) {
                        int i4 = length;
                        if (strArrSplit2[1].matches("[0-9]+(\\.[0-9]+){1,3}") && strArrSplit2[2].matches("[a-f0-9]{64}") && strArrSplit2[3].matches("[a-f0-9]{64}")) {
                            int i5 = i3 - 6;
                            String stCarLifeFrame = strArrSplit2[1];
                            b[] bVarArr2 = bVarArr;
                            String str7 = strArrSplit2[2];
                            int i6 = i3;
                            String str8 = strArrSplit2[3];
                            b bVar = new b();
                            bVar.a = jB5;
                            bVar.b = stCarLifeFrame;
                            bVar.c = str7;
                            bVar.d = str8;
                            VarHandle.storeStoreFence();
                            bVarArr2[i5] = bVar;
                            i3 = i6 + 1;
                            j4 = jB5;
                            strArrSplit = strArr;
                            length = i4;
                            bVarArr = bVarArr2;
                            i = -1;
                        }
                    }
                    SyntheticRefs.c("release receipt entry");
                    return null;
                }
                String[] strArr2 = strArrSplit;
                int i7 = length;
                b[] bVarArr3 = bVarArr;
                if (jB4 < 1 || jB4 > j4) {
                    SyntheticRefs.c("release receipt minimum");
                    return null;
                }
                String str9 = strArr2[4];
                if (jB < j2 || jB2 > j + 1800 || jB3 <= j || jB3 <= jB2 || jB3 - jB2 > 31968000 || !str9.equals(str2) || j3 < jB4 || bVarArr3[i7 - 8].a > j3) {
                    return null;
                }
                for (int i8 = 0; i8 < i2; i8++) {
                    b bVar2 = bVarArr3[i8];
                    if (bVar2.a == j3) {
                        if (bVar2.b.equals(str3) && bVar2.c.equals(str4) && bVar2.d.equals(str5)) {
                            return new a(ecdsaSignature, obj);
                        }
                        return null;
                    }
                }
                return null;
            } catch (IllegalArgumentException e) {
                throw new IOException("release receipt key", e);
            }
        }

        public static a c(EcdsaSignature ecdsaSignature, Object obj) {
            return new a(ecdsaSignature, obj);
        }

        public boolean a(Object obj) {
            return obj != null && this.b == obj && this.a != null && this.c.compareAndSet(false, true);
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class b extends ProtoMessageBase {
        public long a;
        public String b;
        public String c;
        public String d;

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && Objects.equals(this.b, bVar.b) && Objects.equals(this.c, bVar.c) && Objects.equals(this.d, bVar.d);
        }

        public final int hashCode() {
            long j = this.a;
            String str = this.b;
            String str2 = this.c;
            String str3 = this.d;
            return Objects.hashCode(str3) + ProtoMessageBase.b(str2, ProtoMessageBase.b(str, Long.hashCode(j) * 31, 31), 31);
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), this.b, this.c, this.d};
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
        public final boolean equals(Object obj) {
            return obj instanceof c;
        }

        public final int hashCode() {
            return ProtoMessageBase.a(0L, ProtoMessageBase.a(0L, ProtoMessageBase.a(0L, Long.hashCode(0L) * 31, 31), 31), 961);
        }

        public final String toString() {
            Object[] objArr = new Object[0];
            String[] strArrSplit = "".length() == 0 ? new String[0] : "".split(";");
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

    public static EcdsaSignature a(String str, String str2) throws IOException {
        if (str == null || str2 == null || str.length() > 12000 || str2.length() > 160) {
            SyntheticRefs.c("release receipt bounds");
            return null;
        }
        try {
            byte[] bArrDecode = Base64.getDecoder().decode(str);
            byte[] bArrDecode2 = Base64.getDecoder().decode(str2);
            EcdsaSignature ecdsaSignature = new EcdsaSignature();
            ecdsaSignature.a2 = bArrDecode;
            ecdsaSignature.b = bArrDecode2;
            VarHandle.storeStoreFence();
            return ecdsaSignature;
        } catch (IllegalArgumentException e) {
            throw new IOException("release receipt encoding", e);
        }
    }

    public static long b(String str) throws IOException {
        if (!str.matches("[0-9]{1,18}")) {
            SyntheticRefs.c("release receipt integer");
            return 0L;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            throw new IOException("release receipt integer", e);
        }
    }
}
