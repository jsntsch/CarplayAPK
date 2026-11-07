/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 Iap2Csm。iAP2 CSM（连接状态管理）消息：重组分片、校验 magic 与长度。
 *   This file defines Iap2Csm. iAP2 CSM (connection state management) messages: reassembles fragments and validates magic and length.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;



/**
 * iAP2 CSM（连接状态管理）消息：重组分片、校验 magic 与长度。
 *
 * iAP2 CSM (connection state management) messages: reassembles fragments and validates magic and length.
 */
public final class Iap2Csm {
    public static final byte[] a = {-1, 85, 2, 0, -18, 16};
    public static final int b = 128;
    public static final int c = 64;
    public static final int d = 32;
    public static final int e = 16;

    /**
     * Iap2Csm 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in Iap2Csm, carrying local implementation detail.
     */
    public static final class b {
        public final int a;
        public final int b;
        public final int c;
        public final int d;
        private final byte[] e;

        public b(int i, int i2, int i3, int i4, byte[] bArr) {
            this.a = i & 255;
            this.b = i2 & 255;
            this.c = i3 & 255;
            this.d = i4 & 255;
            this.e = bArr == null ? null : (byte[]) bArr.clone();
        }

        public byte[] b() {
            byte[] bArr = this.e;
            if (bArr == null) {
                return null;
            }
            return (byte[]) bArr.clone();
        }
    }

    /**
     * Iap2Csm 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in Iap2Csm, carrying local implementation detail.
     */
    public static final class c {
        public final int a;
        public final List<e> b;

        public c(int i, List<e> list) {
            this.a = i;
            this.b = List.copyOf(list);
        }
    }

    /**
     * Iap2Csm 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in Iap2Csm, carrying local implementation detail.
     */
    public static final class d {
        private byte[] a = new byte[0];

        private static byte[] a(byte[] bArr, byte[] bArr2) {
            byte[] bArrCopyOf = Arrays.copyOf(bArr, bArr.length + bArr2.length);
            System.arraycopy(bArr2, 0, bArrCopyOf, bArr.length, bArr2.length);
            return bArrCopyOf;
        }

        public int b() {
            byte[] bArr = this.a;
            if (bArr.length >= 4 && bArr[0] == 64 && bArr[1] == 64) {
                return Iap2Csm.C(bArr, 2);
            }
            return -1;
        }

        public List<c> c(byte[] bArr) throws IOException {
            byte[] bArr2 = this.a;
            if (bArr2.length + bArr.length > 131072) {
                SyntheticRefs.c("CSM reassembly limit");
                return null;
            }
            byte[] bArrA = a(bArr2, bArr);
            ArrayList arrayList = new ArrayList();
            int i = 0;
            while (bArrA.length - i >= 6) {
                if (bArrA[i] != 64 || bArrA[i + 1] != 64) {
                    SyntheticRefs.c("CSM magic");
                    return null;
                }
                int iC = Iap2Csm.C(bArrA, i + 2);
                if (iC < 6) {
                    SyntheticRefs.c("CSM length");
                    return null;
                }
                if (iC > bArrA.length - i) {
                    break;
                }
                int iC2 = Iap2Csm.C(bArrA, i + 4);
                int i2 = i + 6;
                i += iC;
                arrayList.add(new c(iC2, Iap2Csm.r(Arrays.copyOfRange(bArrA, i2, i))));
            }
            this.a = Arrays.copyOfRange(bArrA, i, bArrA.length);
            return arrayList;
        }

        public int d() {
            return this.a.length;
        }
    }

    /**
     * Iap2Csm 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in Iap2Csm, carrying local implementation detail.
     */
    public static final class e {
        public final int a;
        private final byte[] b;

        public e(int i, byte[] bArr) {
            this.a = i;
            this.b = (byte[]) bArr.clone();
        }

        public byte[] b() {
            return (byte[]) this.b.clone();
        }
    }

    /**
     * Iap2Csm 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in Iap2Csm, carrying local implementation detail.
     */
    public static final class Iap2FrameValidator {
        private byte[] a = new byte[0];

        public void a() throws EOFException {
            if (this.a.length != 0) {
                throw new EOFException("Truncated iAP2 frame");
            }
        }

        public int b() {
            byte[] bArr = this.a;
            if (bArr.length < 2) {
                return -1;
            }
            byte b = bArr[0];
            if (b == -1 && bArr[1] == 85) {
                return Iap2Csm.a.length;
            }
            if (bArr.length >= 4 && b == -1 && bArr[1] == 90) {
                return Iap2Csm.C(bArr, 2);
            }
            return -1;
        }

        public List<b> c(byte[] bArr, int i, int i2) throws IOException {
            int iC;
            byte[] bArrCopyOfRange;
            if (i2 >= 0 && i >= 0 && i <= bArr.length - i2) {
                byte[] bArr2 = this.a;
                if (bArr2.length + i2 <= 131072) {
                    byte[] bArrCopyOf = Arrays.copyOf(bArr2, bArr2.length + i2);
                    System.arraycopy(bArr, i, bArrCopyOf, this.a.length, i2);
                    ArrayList arrayList = new ArrayList();
                    int i3 = 0;
                    while (i3 < bArrCopyOf.length) {
                        if ((bArrCopyOf[i3] & 255) != 255) {
                            SyntheticRefs.c("Unexpected iAP2 synchronization byte");
                            return null;
                        }
                        if (bArrCopyOf.length - i3 < 2) {
                            break;
                        }
                        byte b = bArrCopyOf[i3 + 1];
                        if (b == 85) {
                            if (bArrCopyOf.length - i3 < Iap2Csm.a.length) {
                                break;
                            }
                            int i4 = 0;
                            while (true) {
                                byte[] bArr3 = Iap2Csm.a;
                                if (i4 >= bArr3.length) {
                                    arrayList.add(null);
                                    iC = bArr3.length;
                                    break;
                                }
                                if (bArrCopyOf[i3 + i4] != bArr3[i4]) {
                                    SyntheticRefs.c("Invalid iAP2 detect");
                                    return null;
                                }
                                i4++;
                            }
                            i3 += iC;
                        } else {
                            if (b != 90) {
                                SyntheticRefs.c("Invalid iAP2 magic");
                                return null;
                            }
                            if (bArrCopyOf.length - i3 < 9) {
                                break;
                            }
                            if (Iap2Csm.e(bArrCopyOf, i3, 9) != 0) {
                                SyntheticRefs.c("iAP2 header checksum");
                                return null;
                            }
                            iC = Iap2Csm.C(bArrCopyOf, i3 + 2);
                            if (iC < 9) {
                                SyntheticRefs.c("iAP2 invalid length");
                                return null;
                            }
                            if (bArrCopyOf.length - i3 < iC) {
                                break;
                            }
                            if (iC > 9) {
                                int i5 = i3 + 9;
                                if (Iap2Csm.e(bArrCopyOf, i5, iC - 9) != 0) {
                                    SyntheticRefs.c("iAP2 payload checksum");
                                    return null;
                                }
                                bArrCopyOfRange = Arrays.copyOfRange(bArrCopyOf, i5, (i3 + iC) - 1);
                            } else {
                                bArrCopyOfRange = null;
                            }
                            arrayList.add(new b(bArrCopyOf[i3 + 4], bArrCopyOf[i3 + 5], bArrCopyOf[i3 + 6], bArrCopyOf[i3 + 7], bArrCopyOfRange));
                            i3 += iC;
                        }
                    }
                    this.a = Arrays.copyOfRange(bArrCopyOf, i3, bArrCopyOf.length);
                    return arrayList;
                }
            }
            SyntheticRefs.c("iAP2 receive limit");
            return null;
        }

        public int d() {
            return this.a.length;
        }
    }

    private Iap2Csm() {
    }

    public static byte[] A(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        return Arrays.copyOf(bytes, bytes.length + 1);
    }

    public static byte[] B(int i, byte[] bArr) throws IOException {
        if (bArr.length > 65531) {
            SyntheticRefs.c("iAP2 TLV too large");
            return null;
        }
        int length = bArr.length + 4;
        byte[] bArr2 = new byte[length];
        t(bArr2, 0, length);
        t(bArr2, 2, i);
        System.arraycopy(bArr, 0, bArr2, 4, bArr.length);
        return bArr2;
    }

    public static int C(byte[] bArr, int i) {
        return (bArr[i + 1] & 255) | ((bArr[i] & 255) << 8);
    }

    public static g D(c cVar) throws IOException {
        if (cVar.a != 22275) {
            SyntheticRefs.c("Not AccessoryWiFiConfigurationInformation");
            return null;
        }
        String strZ = z(v(cVar.b, 1));
        byte[] bArrQ = q(cVar.b, 2);
        byte[] bArrV = v(cVar.b, 3);
        if (bArrV.length != 1) {
            SyntheticRefs.c("IAP2_WIFI_CONFIG_SECURITY_WIDTH");
            return null;
        }
        int i = 0;
        int i2 = bArrV[0] & 255;
        if (i2 == 0 || i2 == 2) {
            return new g(i2, i, strZ, bArrQ == null ? "" : z(bArrQ));
        }
        SyntheticRefs.c(ProtoMessageBase.d("IAP2_WIFI_CONFIG_SECURITY_UNSUPPORTED_", i2));
        return null;
    }

    private static String d(byte[] bArr) throws IOException {
        if (bArr.length == 0 || bArr.length > 128 || bArr[bArr.length - 1] != 0) {
            SyntheticRefs.c("CARPLAY_ADDRESS_TERMINATOR_LIMIT");
            return null;
        }
        int length = bArr.length;
        while (length > 0 && bArr[length - 1] == 0) {
            length--;
        }
        String strZ = z(Arrays.copyOf(bArr, length + 1));
        if (p(strZ)) {
            return strZ;
        }
        SyntheticRefs.c("Non-numeric CarPlay peer address");
        return null;
    }

    
    public static byte e(byte[] bArr, int i, int i2) {
        int i3 = 0;
        for (int i4 = i; i4 < i + i2; i4++) {
            i3 += bArr[i4] & 255;
        }
        return (byte) (-i3);
    }

    public static byte[] f(byte[]... bArr) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (byte[] bArr2 : bArr) {
            if (byteArrayOutputStream.size() + bArr2.length > 65535) {
                SyntheticRefs.c("iAP2 message limit");
                return null;
            }
            byteArrayOutputStream.write(bArr2);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static a g(g gVar, String str, int i, String str2) throws IOException {
        if (str == null) {
            SyntheticRefs.c("CARPLAY_DISCOVERY_ENDPOINT_MISSING");
            return null;
        }
        int iIndexOf = str.indexOf(37);
        if (iIndexOf >= 0) {
            str = str.substring(0, iIndexOf);
        }
        if (!p(str) || i < 1 || i > 65535) {
            SyntheticRefs.c("CARPLAY_DISCOVERY_ENDPOINT_INVALID");
            return null;
        }
        if (str2 == null || str2.isBlank()) {
            str2 = "bonjour-candidate";
        }
        if (str2.length() <= 128) {
            return new a(gVar, str, i, str2);
        }
        SyntheticRefs.c("CARPLAY_DISCOVERY_IDENTIFIER_SIZE");
        return null;
    }

    public static a h(String str, int i, String str2) throws IOException {
        return g(null, str, i, str2);
    }

    public static a i(g gVar, String str, int i, String str2) throws IOException {
        if (gVar != null && str != null) {
            return g(gVar, str, i, str2);
        }
        SyntheticRefs.c("CARPLAY_DISCOVERY_ENDPOINT_MISSING");
        return null;
    }

    public static byte[] j(b bVar) throws IOException {
        byte[] bArr = bVar.e;
        int length = bArr == null ? 9 : bArr.length + 10;
        if (length > 65535) {
            SyntheticRefs.c("iAP2 frame too large");
            return null;
        }
        byte[] bArr2 = new byte[length];
        bArr2[0] = -1;
        bArr2[1] = 90;
        t(bArr2, 2, length);
        bArr2[4] = (byte) bVar.a;
        bArr2[5] = (byte) bVar.b;
        bArr2[6] = (byte) bVar.c;
        bArr2[7] = (byte) bVar.d;
        bArr2[8] = e(bArr2, 0, 8);
        if (bArr != null) {
            System.arraycopy(bArr, 0, bArr2, 9, bArr.length);
            bArr2[length - 1] = e(bArr, 0, bArr.length);
        }
        return bArr2;
    }

    
    public static  boolean k(int i) {
        return i == 58;
    }

    public static a l(a aVar, g gVar, String str, int i, String str2) throws IOException {
        a aVarI = i(gVar, str, i, str2);
        if (aVar == null) {
            return aVarI;
        }
        if (aVar.k.d(gVar)) {
            return m(aVar, aVarI);
        }
        SyntheticRefs.c("IAP2_WIFI_CONFIGURATION_CHANGED_RESTART_REQUIRED");
        return null;
    }

    private static a m(a aVar, a aVar2) throws IOException {
        ArrayList arrayList = new ArrayList(aVar.l);
        for (String str : aVar2.l) {
            if (!arrayList.contains(str)) {
                arrayList.add(str);
            }
        }
        if (arrayList.size() > 8) {
            SyntheticRefs.c("CARPLAY_ENDPOINT_ADDRESS_LIMIT");
            return null;
        }
        g gVar = aVar.k;
        int i = aVar.h;
        if (i <= 0) {
            i = aVar2.h;
        }
        String str2 = aVar.c.isEmpty() ? aVar2.c : aVar.c;
        String str3 = aVar.d.isEmpty() ? aVar2.d : aVar.d;
        byte[] bArr = aVar.j == null ? aVar2.j : aVar.j;
        String str4 = aVar.j == null ? aVar2.e : aVar.e;
        if (aVar.j == null) {
            aVar = aVar2;
        }
        return new a(gVar, arrayList, i, str2, str3, bArr, str4, aVar.f, 0);
    }

    public static a n(a aVar, a aVar2) throws IOException {
        int i;
        if (aVar == null) {
            return aVar2;
        }
        if (!aVar.k.d(aVar2.k)) {
            SyntheticRefs.c("IAP2_WIFI_CONFIGURATION_CHANGED_RESTART_REQUIRED");
            return null;
        }
        int i2 = aVar.h;
        if ((i2 <= 0 || (i = aVar2.h) <= 0 || i2 == i) && (aVar.c.isEmpty() || aVar2.c.isEmpty() || aVar.c.equals(aVar2.c))) {
            return m(aVar, aVar2);
        }
        SyntheticRefs.c("CARPLAY_ENDPOINT_CHANGED_RESTART_REQUIRED");
        return null;
    }

    public static byte[] o(int i, byte[] bArr) throws IOException {
        if (bArr.length > 65529) {
            SyntheticRefs.c("CSM message limit");
            return null;
        }
        int length = bArr.length + 6;
        byte[] bArr2 = new byte[length];
        bArr2[0] = 64;
        bArr2[1] = 64;
        t(bArr2, 2, length);
        t(bArr2, 4, i);
        System.arraycopy(bArr, 0, bArr2, 6, bArr.length);
        return bArr2;
    }

    private static boolean p(String str) {
        if (str != null && str.length() <= 64 && !str.isEmpty()) {
            if (str.indexOf(58) < 0) {
                if (!str.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}")) {
                    return false;
                }
                for (String str2 : str.split("\\.")) {
                    if (Integer.parseInt(str2) > 255) {
                        return false;
                    }
                }
            } else if (str.matches("[0-9a-fA-F:.]+") && str.chars().filter(new k(3)).count() >= 2) {
            }
            try {
                InetAddress byName = InetAddress.getByName(str);
                if (!byName.isAnyLocalAddress() && !byName.isLoopbackAddress() && !byName.isMulticastAddress()) {
                    return true;
                }
            } catch (UnknownHostException unused) {
            }
        }
        return false;
    }

    // [OBF-MAP] parseTlvMap(A) | 职责: TLV 解析实现之一，与 v() 是两份独立代码。
    // 踩坑(V1.7.78): 重复 TLV 曾抛异常导致 EA 恒空；只改本方法无效，0x1D01 走的是 v()。
    public static byte[] q(List<e> list, int i) throws IOException {
        byte[] bArr = null;
        for (e eVar : list) {
            if (eVar.a == i) {
                if (bArr != null) {
                    // Info3.6 在 0x1D01 中会重复同一个 param（实测 param 10，补齐身份字段后触发）。
                    // 真实 iPhone 容忍重复字段并取首个；这里不再抛 IOException 终止整条引导。
                    // Tolerate duplicate TLVs: keep the first value instead of aborting the bootstrap.
                    ControlChannel.Log(ProtoMessageBase.d("IAP2_TLV_DUPLICATE_FIELD_KEEP_FIRST ", i) + " first_bytes=" + bArr.length + " dup_bytes=" + eVar.b.length);
                    continue;
                }
                bArr = eVar.b;
            }
        }
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
    }

    public static List<e> r(byte[] bArr) throws IOException {
        ArrayList arrayList = new ArrayList();
        int i = 0;
        while (i < bArr.length) {
            if (bArr.length - i < 4) {
                SyntheticRefs.c("Truncated iAP2 TLV");
                return null;
            }
            int iC = C(bArr, i);
            int iC2 = C(bArr, i + 2);
            if (iC < 4 || iC > bArr.length - i) {
                SyntheticRefs.c("Invalid iAP2 TLV length");
                return null;
            }
            if (arrayList.size() >= 512) {
                SyntheticRefs.c("iAP2 TLV count limit");
                return null;
            }
            int i2 = i + 4;
            i += iC;
            arrayList.add(new e(iC2, Arrays.copyOfRange(bArr, i2, i)));
        }
        return arrayList;
    }

    
    public static String s(List<String> list) {
        for (String str : list) {
            if (str.indexOf(58) < 0) {
                return str;
            }
        }
        return list.isEmpty() ? "" : list.get(0);
    }

    private static void t(byte[] bArr, int i, int i2) {
        bArr[i] = (byte) (i2 >>> 8);
        bArr[i + 1] = (byte) i2;
    }

    public static a u(a aVar, a aVar2) throws IOException {
        if (aVar == null) {
            return aVar2;
        }
        if (!aVar.k.d(aVar2.k)) {
            SyntheticRefs.c("IAP2_WIFI_CONFIGURATION_CHANGED_RESTART_REQUIRED");
            return null;
        }
        if (!aVar.c.isEmpty() && !aVar2.c.isEmpty() && !aVar.c.equals(aVar2.c)) {
            SyntheticRefs.c("CARPLAY_ENDPOINT_IDENTITY_CHANGED");
            return null;
        }
        g gVar = aVar.k;
        List list = aVar2.l.isEmpty() ? aVar.l : aVar2.l;
        int i = aVar2.h;
        if (i <= 0) {
            i = aVar.h;
        }
        a aVar3 = new a(gVar, list, i, aVar.c.isEmpty() ? aVar2.c : aVar.c, aVar2.d.isEmpty() ? aVar.d : aVar2.d, aVar2.j == null ? aVar.j : aVar2.j, aVar2.j == null ? aVar.e : aVar2.e, aVar2.j == null ? aVar.f : aVar2.f, 0);
        if (aVar3.e()) {
            return aVar3;
        }
        SyntheticRefs.c("CARPLAY_ENDPOINT_INCOMPLETE");
        return null;
    }

    // [OBF-MAP] parseTlvMap(B) | 职责: 0x1D01 识别消息实际走的 TLV 解析实现。
    // 判读: 修重复 TLV 必须与 q() 两处同步（V1.7.77 只改 q() 曾白做一轮）。
    public static byte[] v(List<e> list, int i) throws IOException {
        byte[] bArr = null;
        for (e eVar : list) {
            if (eVar.a == i) {
                if (bArr != null) {
                    // Info3.6 的 0x1D01 会重复同一 param（实测 param 10 语言列表），真实 iPhone 取首个。
                    // 这里保留首值继续，不抛异常终止整条无线引导。Tolerate duplicate TLVs (first-wins).
                    ControlChannel.Log(ProtoMessageBase.d("IAP2_TLV_DUPLICATE_FIELD_KEEP_FIRST ", i) + " getter=v first_bytes=" + bArr.length + " dup_bytes=" + eVar.b.length);
                    continue;
                }
                bArr = eVar.b;
            }
        }
        if (bArr != null) {
            return (byte[]) bArr.clone();
        }
        SyntheticRefs.c(ProtoMessageBase.d("Missing iAP2 field ", i));
        return null;
    }

    public static boolean w(a aVar, a aVar2) {
        return aVar != null && aVar2 != null && aVar.h == aVar2.h && aVar.c.equals(aVar2.c) && new HashSet(aVar.l).equals(new HashSet(aVar2.l));
    }

    public static a x(c cVar) throws IOException {
        a aVarY = y(cVar, null);
        if (aVarY.e()) {
            return aVarY;
        }
        SyntheticRefs.c("CARPLAY_ENDPOINT_INCOMPLETE");
        return null;
    }

    public static a y(c cVar, g gVar) throws IOException {
        g gVar2;
        int i;
        String str;
        int i2;
        byte[] bArr = null;
        if (cVar.a != 17153) {
            SyntheticRefs.c("Not CarPlayStartSession");
            return null;
        }
        // 诊断：记录 0x4301 顶层字段 ID，复现时可确认车机实际编码布局（嵌套 vs 扁平）
        StringBuilder sbFields = new StringBuilder("CARPLAY_START_WIFI_TOPLEVEL_FIELDS");
        for (e eVar : cVar.b) {
            sbFields.append(' ').append(eVar.a);
        }
        ControlChannel.Log(sbFields.toString());

        byte[] bArrQ = q(cVar.b, 1);
        List<e> listR = bArrQ == null ? Collections.EMPTY_LIST : r(bArrQ);
        int i3 = 0;
        byte[] bArrQ2 = q(listR, 0);
        byte[] bArrQ3 = q(listR, 1);
        byte[] bArrQ4 = q(listR, 4);

        // 诊断：嵌套布局（SSID/Passphrase/Security 编码在 field1 内）是否齐备。
        // 社区抓包文档描述部分车机把 SSID/Passphrase/Security 作为顶层 field 1/2/3 直接编码。
        ControlChannel.Log("CARPLAY_START_WIFI_LAYOUT nested_ssid=" + (bArrQ2 != null) + " nested_pass=" + (bArrQ3 != null) + " nested_sec=" + (bArrQ4 != null));

        // 兜底：嵌套布局不完整时，尝试顶层扁平布局（field1=SSID, field2=Passphrase, field3=Security）
        if (bArrQ2 == null || bArrQ3 == null || bArrQ4 == null) {
            byte[] fSsid = q(cVar.b, 1);
            byte[] fPass = q(cVar.b, 2);
            byte[] fSec = q(cVar.b, 3);
            if (fSsid != null && fPass != null && fSec != null) {
                bArrQ2 = fSsid;
                bArrQ3 = fPass;
                bArrQ4 = fSec;
                ControlChannel.Log("CARPLAY_START_WIFI_LAYOUT flat");
            }
        }
        if (bArrQ2 == null && bArrQ3 == null) {
            if (bArrQ4 != null && (bArrQ4.length != 1 || gVar == null || g.e(bArrQ4[0] & 255) != g.e(gVar.b))) {
                SyntheticRefs.c("CARPLAY_START_WIFI_SECURITY");
                return null;
            }
            gVar2 = gVar;
        } else {
            if (bArrQ2 == null || bArrQ3 == null || bArrQ4 == null || bArrQ4.length != 1) {
                SyntheticRefs.c("CARPLAY_START_WIFI_FIELDS");
                return null;
            }
            g gVar3 = new g(bArrQ4[0] & 255, i3, z(bArrQ2), z(bArrQ3));
            if (gVar != null && !gVar.d(gVar3)) {
                SyntheticRefs.c("IAP2_WIFI_CONFIGURATION_CHANGED_RESTART_REQUIRED");
                return null;
            }
            gVar2 = gVar3;
        }
        if (gVar2 == null) {
            SyntheticRefs.c("CARPLAY_START_WIFI_CREDENTIALS_MISSING");
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (e eVar : listR) {
            if (eVar.a == 3) {
                String strD = d(eVar.b);
                if (!arrayList.contains(strD)) {
                    arrayList.add(strD);
                }
                if (arrayList.size() > 8) {
                    SyntheticRefs.c("CARPLAY_ENDPOINT_ADDRESS_LIMIT");
                    return null;
                }
            }
        }
        byte[] bArrQ5 = q(cVar.b, 2);
        if (bArrQ5 == null) {
            i = 0;
        } else {
            if (bArrQ5.length != 4) {
                SyntheticRefs.c("CarPlay port width");
                return null;
            }
            long jC = (((long) C(bArrQ5, 0)) << 16) | ((long) C(bArrQ5, 2));
            if (jC < 1 || jC > 65535) {
                SyntheticRefs.c("CarPlay port range");
                return null;
            }
            i = (int) jC;
        }
        byte[] bArrQ6 = q(cVar.b, 3);
        String strZRaw = bArrQ6 == null ? null : z(bArrQ6);
        if (bArrQ6 != null && strZRaw == null) {
            ControlChannel.Log("CARPLAY_START_IDENTIFIER_INVALID_TREATED_EMPTY");
        }
        String strZ = strZRaw == null ? "" : strZRaw;
        if (!strZ.isEmpty() && strZ.length() > 128) {
            SyntheticRefs.c("Accessory identifier size");
            return null;
        }
        byte[] bArrQ7 = q(cVar.b, 4);
        if (bArrQ7 != null) {
            String strZ2 = z(bArrQ7);
            int length = strZ2.length();
            str = strZ2.isEmpty() ? "empty_hint" : "opaque_hint";
            String strSubstring = (strZ2.startsWith("0x") || strZ2.startsWith("0X")) ? strZ2.substring(2) : strZ2;
            if (strSubstring.matches("[0-9a-fA-F]{64}")) {
                bArr = new byte[32];
                while (i3 < 32) {
                    int i4 = i3 * 2;
                    bArr[i3] = (byte) Integer.parseInt(strSubstring.substring(i4, i4 + 2), 16);
                    i3++;
                }
                str = "hex32_hint";
            } else if (strZ2.matches("[A-Za-z0-9+/]{43}=?")) {
                try {
                    byte[] bArrDecode = Base64.getDecoder().decode(strZ2);
                    if (bArrDecode.length == 32) {
                        str = "base64_32_hint";
                        bArr = bArrDecode;
                    }
                } catch (IllegalArgumentException unused) {
                }
            }
            i2 = length;
        } else {
            str = "absent";
            i2 = 0;
        }
        byte[] bArrQ8 = q(cVar.b, 5);
        return new a(gVar2, arrayList, i, strZ, bArrQ8 != null ? z(bArrQ8) : "", bArr, str, i2, 0);
    }

    public static String z(byte[] bArr) throws IOException {
        if (bArr.length == 0 || bArr.length > 1024 || bArr[bArr.length - 1] != 0) {
            SyntheticRefs.c("iAP2 string terminator/limit");
            return null;
        }
        for (int i = 0; i < bArr.length - 1; i++) {
            if (bArr[i] == 0) {
                SyntheticRefs.c("iAP2 embedded string terminator");
                return null;
            }
        }
        try {
            CharsetDecoder charsetDecoderNewDecoder = StandardCharsets.UTF_8.newDecoder();
            CodingErrorAction codingErrorAction = CodingErrorAction.REPORT;
            return charsetDecoderNewDecoder.onMalformedInput(codingErrorAction).onUnmappableCharacter(codingErrorAction).decode(ByteBuffer.wrap(bArr, 0, bArr.length - 1)).toString();
        } catch (CharacterCodingException e2) {
            throw new IOException("Invalid iAP2 UTF-8", e2);
        }
    }

    /**
     * Iap2Csm 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in Iap2Csm, carrying local implementation detail.
     */
    public static final class a {
        public final String a;
        public final String b;
        public final String c;
        public final String d;
        public final String e;
        public final int f;
        public final int g;
        public final int h;
        private final String i;
        private final byte[] j;
        private final g k;
        private final List<String> l;

        private a(g gVar, List<String> list, int i, String str, String str2, byte[] bArr, String str3, int i2) {
            this.k = gVar;
            this.a = gVar == null ? "" : gVar.a;
            this.i = gVar != null ? gVar.c() : "";
            this.g = gVar == null ? -1 : gVar.b;
            this.l = List.copyOf(list);
            this.b = Iap2Csm.s(list);
            this.h = i;
            this.c = str;
            this.d = str2;
            this.j = bArr == null ? null : (byte[]) bArr.clone();
            this.e = str3;
            this.f = i2;
        }

        public List<String> d() {
            return this.l;
        }

        public boolean e() {
            return (this.b.isEmpty() || this.h <= 0 || this.c.isEmpty()) ? false : true;
        }

        public String f() {
            return this.i;
        }

        public byte[] g() {
            byte[] bArr = this.j;
            if (bArr == null) {
                return null;
            }
            return (byte[]) bArr.clone();
        }

        public g h() {
            return this.k;
        }

        public a i(String str) throws IOException {
            if (!this.l.contains(str)) {
                SyntheticRefs.c("CARPLAY_ENDPOINT_ADDRESS_NOT_ADVERTISED");
                return null;
            }
            ArrayList arrayList = new ArrayList();
            arrayList.add(str);
            for (String str2 : this.l) {
                if (!str2.equals(str)) {
                    arrayList.add(str2);
                }
            }
            return new a(this.k, str, arrayList, this.h, this.c, this.d, this.j, this.e, this.f);
        }

        public String toString() {
            return "CarPlay endpoint " + this.b + ":" + this.h + "; Wi-Fi security=" + this.g;
        }

        public  a(g gVar, List list, int i, String str, String str2, byte[] bArr, String str3, int i2, int i3) {
            this(gVar, (List<String>) list, i, str, str2, bArr, str3, i2);
        }

        private a(g gVar, String str, int i, String str2, String str3, byte[] bArr, String str4, int i2) {
            this(gVar, (List<String>) ((str == null || str.isEmpty()) ? Collections.EMPTY_LIST : List.of(str)), i, str2, str3, bArr, str4, i2);
        }

        public  a(g gVar, String str, int i, String str2) {
            this(gVar, str, i, str2, "", (byte[]) null, "bonjour_untrusted", 0);
        }

        private a(g gVar, String str, List<String> list, int i, String str2, String str3, byte[] bArr, String str4, int i2) {
            this.k = gVar;
            this.a = gVar == null ? "" : gVar.a;
            this.i = gVar != null ? gVar.c() : "";
            this.g = gVar == null ? -1 : gVar.b;
            this.l = List.copyOf(list);
            this.b = str;
            this.h = i;
            this.c = str2;
            this.d = str3;
            this.j = bArr == null ? null : (byte[]) bArr.clone();
            this.e = str4;
            this.f = i2;
        }
    }

    /**
     * Iap2Csm 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in Iap2Csm, carrying local implementation detail.
     */
    public static final class g {
        public final String a;
        public final int b;
        private final String c;

        private g(String str, String str2, int i) throws IOException {
            int length = str.getBytes(StandardCharsets.UTF_8).length;
            if (length == 0 || length > 32) {
                SyntheticRefs.c("Invalid SSID size");
                throw null;
            }
            if (i != 0 && i != 2 && i != 3 && i != 4) {
                SyntheticRefs.c(ProtoMessageBase.d("Unsupported Wi-Fi security ", i));
                throw null;
            }
            if (i != 0 && (str2.length() < 8 || str2.length() > 63)) {
                SyntheticRefs.c("Unsupported Wi-Fi passphrase size");
                throw null;
            }
            if (i == 0 && !str2.isEmpty()) {
                SyntheticRefs.c("Open Wi-Fi must not include a passphrase");
                throw null;
            }
            this.a = str;
            this.c = str2;
            this.b = i;
        }

        
        public static int e(int i) {
            if (i == 3) {
                return 2;
            }
            return i;
        }

        public String b() {
            StringBuilder sb = new StringBuilder("\"");
            for (int i = 0; i < this.a.length(); i++) {
                char cCharAt = this.a.charAt(i);
                if (cCharAt == '\"' || cCharAt == '\\') {
                    sb.append('\\');
                    sb.append(cCharAt);
                } else if (Character.isISOControl(cCharAt) || cCharAt == 8232 || cCharAt == 8233) {
                    sb.append(String.format(Locale.ROOT, "\\u%04x", Integer.valueOf(cCharAt)));
                } else {
                    sb.append(cCharAt);
                }
            }
            sb.append('\"');
            return sb.toString();
        }

        public String c() {
            return this.c;
        }

        public boolean d(g gVar) {
            return gVar != null && this.a.equals(gVar.a) && this.c.equals(gVar.c) && e(this.b) == e(gVar.b);
        }

        public String toString() {
            return ProtoMessageBase.d("Authenticated Wi-Fi credentials; security=", this.b);
        }

        public  g(int i, int i2, String str, String str2) throws IOException {
            this(str, str2, i);
        }
    }
}
