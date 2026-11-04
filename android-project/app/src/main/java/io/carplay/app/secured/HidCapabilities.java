/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 HidCapabilities。HID 能力集合：维护每个 HID 设备的描述符、字段表与报告分发。
 *   This file defines HidCapabilities. HID capability set: keeps per-device descriptors and field tables and dispatches reports.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;



/**
 * HID 能力集合：维护每个 HID 设备的描述符、字段表与报告分发。
 *
 * HID capability set: keeps per-device descriptors and field tables and dispatches reports.
 */
public final class HidCapabilities {
    public static final int p = 1;
    private static final BigInteger q;
    private static final Set<String> r;
    private static final Set<String> s;
    private static final Set<String> t;
    private final List<c> a;
    private final List<e> b;
    private final List<b> c;
    private final AudioCapabilities d;
    private final boolean e;
    private final boolean f;
    private final boolean g;
    private final boolean h;
    private final boolean i;
    private final boolean j;
    private final BigInteger k;
    private final Set<String> l;
    private final Set<String> m;
    private final String n;
    private final int o;

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class a extends ProtoMessageBase {
        private final int a;
        private final int b;
        private final int c;
        private final int d;
        private final int e;
        private final int f;
        private final int g;
        private final int h;
        private final boolean i;

        public a(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, boolean z) {
            this.a = i;
            this.b = i2;
            this.c = i3;
            this.d = i4;
            this.e = i5;
            this.f = i6;
            this.g = i7;
            this.h = i8;
            this.i = z;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.i == aVar.i && this.a == aVar.a && this.b == aVar.b && this.c == aVar.c && this.d == aVar.d && this.e == aVar.e && this.f == aVar.f && this.g == aVar.g && this.h == aVar.h;
        }

        public boolean g() {
            return this.i;
        }

        public int h() {
            return this.d;
        }

        public final int hashCode() {
            boolean z = this.i;
            int i = this.a;
            int i2 = this.b;
            int i3 = this.c;
            int i4 = this.d;
            int i5 = this.e;
            int i6 = this.f;
            int i7 = this.g;
            return (((((((((((((((Boolean.hashCode(z) * 31) + i) * 31) + i2) * 31) + i3) * 31) + i4) * 31) + i5) * 31) + i6) * 31) + i7) * 31) + this.h;
        }

        public int i() {
            return this.h;
        }

        public int j() {
            return this.g;
        }

        public int k() {
            return this.e;
        }

        public int l() {
            return this.f;
        }

        public int m() {
            return this.c;
        }

        public int n() {
            return this.a;
        }

        public int o() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c), Integer.valueOf(this.d), Integer.valueOf(this.e), Integer.valueOf(this.f), Integer.valueOf(this.g), Integer.valueOf(this.h), Boolean.valueOf(this.i)};
            String[] strArrSplit = "a;b;c;d;e;f;g;h;i".length() == 0 ? new String[0] : "a;b;c;d;e;f;g;h;i".split(";");
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
        private final String b;
        private final BigInteger c;
        private final BigInteger d;

        public b(int i, String str, BigInteger bigInteger, BigInteger bigInteger2) {
            this.a = i;
            this.b = str;
            this.c = bigInteger;
            this.d = bigInteger2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && Objects.equals(this.b, bVar.b) && Objects.equals(this.c, bVar.c) && Objects.equals(this.d, bVar.d);
        }

        public String g() {
            return this.b;
        }

        public BigInteger h() {
            return this.c;
        }

        public final int hashCode() {
            int i = this.a;
            String str = this.b;
            BigInteger bigInteger = this.c;
            BigInteger bigInteger2 = this.d;
            return Objects.hashCode(bigInteger2) + ((Objects.hashCode(bigInteger) + ProtoMessageBase.b(str, i * 31, 31)) * 31);
        }

        public BigInteger i() {
            return this.d;
        }

        public int j() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), this.b, this.c, this.d};
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
        private final int a;
        private final int b;
        private final boolean c;
        private final int d;
        private final int e;
        private final int f;
        private final long g;
        private final HidTouchType h;
        private final boolean i;
        private final List<a> j;
        private final int k;

        public c(int i, int i2, boolean z, int i3, int i4, int i5, long j, HidTouchType gVar, boolean z2, List<a> list, int i6) {
            List<a> listCopyOf = List.copyOf(list);
            this.a = i;
            this.b = i2;
            this.c = z;
            this.d = i3;
            this.e = i4;
            this.f = i5;
            this.g = j;
            this.h = gVar;
            this.i = z2;
            this.j = listCopyOf;
            this.k = i6;
        }

        
        public String h() {
            return this.b + ":" + this.c + ":" + this.d + ":" + this.e + ":" + this.f + ":" + this.g + ":" + String.valueOf(this.h) + ":" + this.i + ":" + String.valueOf(this.j) + ":" + this.k;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof c)) {
                return false;
            }
            c cVar = (c) obj;
            return this.c == cVar.c && this.i == cVar.i && this.a == cVar.a && this.b == cVar.b && this.d == cVar.d && this.e == cVar.e && this.f == cVar.f && this.k == cVar.k && this.g == cVar.g && Objects.equals(this.h, cVar.h) && Objects.equals(this.j, cVar.j);
        }

        public final int hashCode() {
            boolean z = this.c;
            boolean z2 = this.i;
            int i = this.a;
            int i2 = this.b;
            int i3 = this.d;
            int i4 = this.e;
            int i5 = this.f;
            int i6 = this.k;
            long j = this.g;
            HidTouchType gVar = this.h;
            List<a> list = this.j;
            return Objects.hashCode(list) + ((Objects.hashCode(gVar) + ProtoMessageBase.a(j, (((((((((((((Boolean.hashCode(z2) + (Boolean.hashCode(z) * 31)) * 31) + i) * 31) + i2) * 31) + i3) * 31) + i4) * 31) + i5) * 31) + i6) * 31, 31)) * 31);
        }

        public long i() {
            return this.g;
        }

        public int j() {
            return this.e;
        }

        public boolean k() {
            return this.i;
        }

        public int l() {
            return this.k;
        }

        public boolean m() {
            return (this.g & 2) != 0 || this.h == HidTouchType.KNOB;
        }

        public int n() {
            return this.f;
        }

        public HidTouchType o() {
            return this.h;
        }

        public int p() {
            return this.a;
        }

        public int q() {
            return this.b;
        }

        public boolean r() {
            return (this.g & 12) != 0 || this.h == HidTouchType.TOUCH_SCREEN;
        }

        public boolean s() {
            return (this.g & 16) != 0 || this.h == HidTouchType.TOUCHPAD;
        }

        public boolean t() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Boolean.valueOf(this.c), Integer.valueOf(this.d), Integer.valueOf(this.e), Integer.valueOf(this.f), Long.valueOf(this.g), this.h, Boolean.valueOf(this.i), this.j, Integer.valueOf(this.k)};
            String[] strArrSplit = "a;b;c;d;e;f;g;h;i;j;k".length() == 0 ? new String[0] : "a;b;c;d;e;f;g;h;i;j;k".split(";");
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

        public List<a> u() {
            return this.j;
        }

        public int v() {
            return this.d;
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class e extends ProtoMessageBase {
        private final int a;
        private final int b;
        private final boolean c;
        private final boolean d;
        private final boolean e;
        private final boolean f;
        private final boolean g;
        private final int h;
        private final String i;

        public e(int i, int i2, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, int i3, String str) {
            this.a = i;
            this.b = i2;
            this.c = z;
            this.d = z2;
            this.e = z3;
            this.f = z4;
            this.g = z5;
            this.h = i3;
            this.i = str;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof e)) {
                return false;
            }
            e eVar = (e) obj;
            return this.c == eVar.c && this.d == eVar.d && this.e == eVar.e && this.f == eVar.f && this.g == eVar.g && this.a == eVar.a && this.b == eVar.b && this.h == eVar.h && Objects.equals(this.i, eVar.i);
        }

        public int g() {
            return this.h;
        }

        public int h() {
            return this.b;
        }

        public final int hashCode() {
            boolean z = this.c;
            boolean z2 = this.d;
            boolean z3 = this.e;
            boolean z4 = this.f;
            boolean z5 = this.g;
            int i = this.a;
            int i2 = this.b;
            int i3 = this.h;
            String str = this.i;
            return Objects.hashCode(str) + ((((((((Boolean.hashCode(z5) + ((Boolean.hashCode(z4) + ((Boolean.hashCode(z3) + ((Boolean.hashCode(z2) + (Boolean.hashCode(z) * 31)) * 31)) * 31)) * 31)) * 31) + i) * 31) + i2) * 31) + i3) * 31);
        }

        public boolean i() {
            return this.c;
        }

        public boolean j() {
            return this.g;
        }

        public boolean k() {
            return this.f;
        }

        public String l() {
            return this.i;
        }

        public boolean m() {
            return this.d;
        }

        public int n() {
            return this.a;
        }

        public boolean o() {
            return this.e;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Boolean.valueOf(this.c), Boolean.valueOf(this.d), Boolean.valueOf(this.e), Boolean.valueOf(this.f), Boolean.valueOf(this.g), Integer.valueOf(this.h), this.i};
            String[] strArrSplit = "a;b;c;d;e;f;g;h;i".length() == 0 ? new String[0] : "a;b;c;d;e;f;g;h;i".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(e.class, sb, "[");
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
    public enum HidTouchType {
        UNKNOWN,
        TOUCH_SCREEN,
        TOUCHPAD,
        KNOB
    }

    static {
        BigInteger bigInteger = BigInteger.ONE;
        q = bigInteger.shiftLeft(64).subtract(bigInteger);
        r = Set.of("uiContext", "viewAreas", "cornerMasks", "focusTransfer", "h.264Level5.1", "mainBuffered", "altScreen", "enhancedSiri", "hevc", "sessionManagement", "logTransfer", "iAPChannel", "vehicleStateProtocol", "videoPlayback");
        s = Set.of("enhancedRequestCarUI", "vocoderInfo", "ProvidesHighAccuracyTimeStamps");
        t = Set.of("default", "compatibility", "media", "alert", "telephony", "speechRecognition");
    }

    private HidCapabilities(List<c> list, List<e> list2, List<b> list3, AudioCapabilities sVar, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, BigInteger bigInteger, Set<String> set, Set<String> set2, int i, List<String> lisDisplaySession) {
        this.a = List.copyOf(list);
        this.b = List.copyOf(list2);
        this.c = List.copyOf(list3);
        this.d = sVar;
        this.e = z;
        this.f = z2;
        this.g = z3;
        this.h = z4;
        this.i = z5;
        this.j = z6;
        this.k = bigInteger;
        this.l = Set.copyOf(set);
        this.m = Set.copyOf(set2);
        this.o = i;
        lisDisplaySession.sort(new v());
        this.n = q(ProtoMessageBase.e("CARPLAY_CAPABILITIES_1\n", String.join("\n", lisDisplaySession)));
    }

    
    public static  boolean A(int i, e eVar) {
        if (eVar.i() && eVar.o()) {
            return eVar.h() == -1 || eVar.h() == i;
        }
        return false;
    }

    private static List<?> B(Map<?, ?> map, String str, int i) throws IOException {
        Object obj = map.get(str);
        if (obj == null && !map.containsKey(str)) {
            return Collections.EMPTY_LIST;
        }
        if (obj instanceof List) {
            List<?> list = (List) obj;
            if (list.size() <= i) {
                return list;
            }
        }
        throw j("CAP_LIST_INVALID");
    }

    private static BigInteger C(Object obj) {
        if ((obj instanceof Byte) || (obj instanceof Short) || (obj instanceof Integer) || (obj instanceof Long)) {
            return BigInteger.valueOf(((Number) obj).longValue()).and(q);
        }
        if (obj instanceof BigInteger) {
            BigInteger bigInteger = (BigInteger) obj;
            if (bigInteger.signum() >= 0 && bigInteger.bitLength() <= 64) {
                return bigInteger;
            }
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() <= 21 && str.matches("0x[0-9a-fA-F]{1,8}(,0x[0-9a-fA-F]{1,8})?")) {
                String[] strArrSplit = str.split(",");
                BigInteger bigInteger2 = new BigInteger(strArrSplit[0].substring(2), 16);
                return strArrSplit.length == 1 ? bigInteger2 : new BigInteger(strArrSplit[1].substring(2), 16).shiftLeft(32).or(bigInteger2);
            }
        }
        return BigInteger.ZERO;
    }

    private static String G(Map<?, ?> map, Set<String> set) {
        String str;
        TreeSet treeSet = new TreeSet();
        for (String str2 : set) {
            Object obj = map.get(str2);
            if (obj instanceof Boolean) {
                str = str2 + "=" + ((Boolean) obj);
            } else if ((obj instanceof Byte) || (obj instanceof Short) || (obj instanceof Integer) || (obj instanceof Long)) {
                str = str2 + "=" + ((Number) obj).longValue();
            } else {
                continue;
            }
            treeSet.add(str);
        }
        return treeSet.toString();
    }

    private static int H(Map<?, ?> map, String str, int i, int i2, int i3) throws IOException {
        return map.containsKey(str) ? M(map, str, i2, i3) : i;
    }

    private static List<?> I(Map<?, ?> map, String str, int i) {
        try {
            return B(map, str, i);
        } catch (IOException unused) {
            return Collections.EMPTY_LIST;
        }
    }

    public static HidCapabilities J(Map<?, ?> map) throws IOException {
        return K(map, Collections.EMPTY_MAP, Collections.EMPTY_SET);
    }

    
    
    
    
    public static HidCapabilities K(Map<?, ?> map, Map<?, ?> map2, Set<String> set) throws IOException {
        String str;
        AudioCapabilities sVarU;
        boolean z;
        Map<?, ?> map3;
        String str2;
        String str3;
        int i;
        boolean z2;
        String str4;
        String str5;
        String stCarLifeFrame;
        String str7;
        String str8;
        ArrayList arrayList;
        HashMap map4;
        List<?> list;
        int i2;
        int iM = 110;
        HidTouchType gVar;
        ArrayList arrayList2;
        HashMap map5;
        String strB;
        String str9 = "originYPixels";
        String str10 = "originXPixels";
        String str11 = "maxFPS";
        String str12 = "audioFormats";
        if (map == null || map2 == null || set == null || map.size() > 256 || map2.size() > 128) {
            throw j("CAP_ENVELOPE_INVALID");
        }
        ArrayList arrayList3 = new ArrayList();
        ArrayList arrayLisDisplaySession = new ArrayList();
        HashMap map6 = new HashMap();
        arrayList = arrayList3;
        map4 = map6;
        List<?> listB = B(map, "displays", 16);
        HashMap map7 = new HashMap();
        for (Object obj : listB) {
            if ((obj instanceof Map) && (strB = ProtoFieldAccessor.b(ProtoFieldAccessor.a((Map) obj))) != null) {
                map7.merge(strB, 1, new w(0));
            }
        }
        int i3 = 0;
        int i4 = 0;
        while (true) {
            int i5 = i3;
            String str13 = "type";
            if (i4 >= listB.size()) {
                Object obj2 = "type";
                String str14 = str12;
                ArrayList arrayList5 = arrayList3;
                HashMap map8 = map6;
                boolean zQ = Q(map, "hidDevices", 32);
                ArrayList arrayList6 = new ArrayList();
                List<?> listI = I(map, "hidDevices", 32);
                int i6 = 0;
                while (i6 < listI.size()) {
                    Object obj3 = listI.get(i6);
                    if (obj3 instanceof Map) {
                        Map map9 = (Map) obj3;
                        String strB2 = ProtoFieldAccessor.b(map9.get("displayUUID"));
                        int iIntValue = map9.containsKey("displayUUID") ? strB2 == null ? -2 : ((Integer) map8.getOrDefault(strB2, -2)).intValue() : -1;
                        Object obj4 = map9.get("hidDescriptor");
                        byte[] bArr = obj4 instanceof byte[] ? (byte[]) obj4 : null;
                        f fVarS = s(bArr);
                        if (fVarS.a) {
                            try {
                                z2 = !HidDescriptorParser.e(bArr).f().isEmpty();
                            } catch (RuntimeException unused) {
                                z2 = false;
                            }
                        } else {
                            z2 = false;
                        }
                        i = i6;
                        e eVar = new e(i, iIntValue, P(map9.get("uuid")), fVarS.a, z2, fVarS.b, fVarS.c, bArr == null ? 0 : Math.min(4097, bArr.length), fVarS.d);
                        int i7 = iIntValue;
                        boolean z3 = z2;
                        arrayList6.add(eVar);
                        if (i7 == -1) {
                            str4 = "UNBOUND";
                        } else if (i7 == -2) {
                            str4 = "OTHER";
                        } else {
                            Stream stream = arrayLisDisplaySession.stream();
                            int i8 = 1;
                            u uVar = new u(i8);
                            uVar.b = i7;
                            VarHandle.storeStoreFence();
                            str4 = (String) stream.filter(uVar).map(new r(i8)).findFirst().orElse("OTHER");
                        }
                        arrayList5.add("H:" + str4 + ":" + eVar.i() + ":" + fVarS.a + ":" + z3 + ":" + fVarS.b + ":" + fVarS.c + ":" + fVarS.d);
                    } else {
                        listI = listI;
                        map8 = map8;
                        i = i6;
                    }
                    i6 = i + 1;
                    listI = listI;
                    map8 = map8;
                    arrayLisDisplaySession = arrayLisDisplaySession;
                }
                ArrayList arrayList7 = arrayLisDisplaySession;
                ArrayList arrayList8 = new ArrayList();
                try {
                    sVarU = io.carplay.app.secured.AudioCapabilities.u(map);
                    str = str14;
                    z = true;
                } catch (IOException unused2) {
                    str = str14;
                    sVarU = io.carplay.app.secured.AudioCapabilities.u(Map.of(str, Collections.EMPTY_LIST));
                    z = false;
                }
                String str15 = "UNSPECIFIED";
                if (map.containsKey(str)) {
                    Iterator<?> it = I(map, str, Iap2Csm.b).iterator();
                    while (it.hasNext()) {
                        Object next = it.next();
                        if (next instanceof Map) {
                            Map map10 = (Map) next;
                            int iV = v(map10.get(obj2), 0);
                            if (iV >= 1 && iV <= 65535) {
                                Object obj5 = map10.get("audioType");
                                Iterator<?> it2 = it;
                                if (obj5 instanceof String) {
                                    str3 = (String) obj5;
                                    if (!t.contains(str3)) {
                                        if (map10.get("audioType") != null) {
                                            str3 = "UNSUPPORTED";
                                        } else {
                                            str3 = str15;
                                        }
                                    }
                                } else if (map10.get("audioType") != null) {
                                    str3 = "UNSUPPORTED";
                                } else {
                                    str3 = str15;
                                }
                                b bVar = new b(iV, str3, C(map10.get("audioInputFormats")), C(map10.get("audioOutputFormats")));
                                arrayList8.add(bVar);
                                arrayList5.add("A:".concat(String.valueOf(bVar)));
                                it = it2;
                                str15 = str15;
                                obj2 = obj2;
                            }
                        }
                    }
                }
                String str16 = str15;
                for (Object obj6 : I(map, "audioLatencies", Iap2Csm.b)) {
                    if (obj6 instanceof Map) {
                        Map map11 = (Map) obj6;
                        String strG = G(map11, Set.of("type", "sr", "ss", "ch", "inputLatencyMicros", "outputLatencyMicros"));
                        Object obj7 = map11.get("audioType");
                        if (obj7 instanceof String) {
                            str2 = (String) obj7;
                            if (!t.contains(str2)) {
                                str2 = str16;
                            }
                        } else {
                            str2 = str16;
                        }
                        arrayList5.add("L:" + strG + ":" + str2);
                    }
                }
                boolean zContainsKey = map.containsKey("modes");
                boolean zX = !zContainsKey;
                if (zContainsKey) {
                    Object obj8 = map.get("modes");
                    if (obj8 instanceof Map) {
                        Map map12 = (Map) obj8;
                        LinkedHashMap linkedHashMap = new LinkedHashMap();
                        for (Map.Entry entry : ((Map<?, ?>) map12).entrySet()) {
                            Object key = entry.getKey();
                            if (key instanceof String) {
                                linkedHashMap.put((String) key, entry.getValue());
                            }
                        }
                        zX = new SessionStateMachine(false).x(linkedHashMap);
                        for (String str17 : List.of("resources", "initialPermanentEntity", "appStates")) {
                            for (Object obj9 : B(map12, str17, 16)) {
                                ArrayList arrayList9 = arrayList8;
                                if (obj9 instanceof Map) {
                                    arrayList5.add("M:" + str17 + ":" + G((Map) obj9, Set.of("resourceID", "transferType", "transferPriority", "takeConstraint", "borrowConstraint", "unborrowConstraint", "permanentEntity", "appStateID", "speechMode", "state")));
                                }
                                arrayList8 = arrayList9;
                                map12 = map12;
                            }
                        }
                    }
                }
                ArrayList arrayList10 = arrayList8;
                map3 = map2;
                boolean z4 = Q(map3, "enabledFeatures", 32);
                if (Q(map, "extendedFeatures", 32)) {
                    BigInteger bigIntegerC = C(map.get("features"));
                    Set<String> setN = n(map, "extendedFeatures", s);
                    Set<String> setN2 = n(map3, "enabledFeatures", r);
                    setN2.retainAll(set);
                    arrayList5.add("F:" + bigIntegerC.toString(16) + ":" + map.containsKey("features"));
                    arrayList5.add("E:".concat(String.valueOf(setN)));
                    arrayList5.add("N:".concat(String.valueOf(setN2)));
                    arrayList5.add("VALID:" + z + ":" + zQ + ":" + z4 + ":" + sVarU.l() + ":" + zContainsKey + ":" + zX + ":" + i5);
                    return new HidCapabilities(arrayList7, arrayList6, arrayList10, sVarU, z, zQ, z4, zContainsKey, zX, map.containsKey("features"), bigIntegerC, setN, setN2, i5, arrayList5);
                }
                map3 = map2;
                BigInteger bigIntegerC2 = C(map.get("features"));
                Set<String> setN3 = n(map, "extendedFeatures", s);
                Set<String> setN4 = n(map3, "enabledFeatures", r);
                setN4.retainAll(set);
                arrayList5.add("F:" + bigIntegerC2.toString(16) + ":" + map.containsKey("features"));
                arrayList5.add("E:".concat(String.valueOf(setN3)));
                arrayList5.add("N:".concat(String.valueOf(setN4)));
                arrayList5.add("VALID:" + z + ":" + zQ + ":" + z4 + ":" + sVarU.l() + ":" + zContainsKey + ":" + zX + ":" + i5);
                return new HidCapabilities(arrayList7, arrayList6, arrayList10, sVarU, z, zQ, z4, zContainsKey, zX, map.containsKey("features"), bigIntegerC2, setN3, setN4, i5, arrayList5);
            }
            Object obj10 = listB.get(i4);
            int i9 = i4;
            if (obj10 instanceof Map) {
                Map map13 = (Map) obj10;
                try {
                    if (map13.containsKey("type")) {
                        list = listB;
                        i2 = 1;
                        try {
                            iM = M(map13, "type", 1, 65535);
                        } catch (IOException unused3) {
                            str5 = str9;
                            stCarLifeFrame = str10;
                            str7 = str11;
                            str8 = str12;
                            arrayList = arrayList3;
                            map4 = map6;
                            i3 = i5 + 1;
                            arrayList3 = arrayList;
                            map6 = map4;
                            listB = list;
                            str12 = str8;
                            str11 = str7;
                            str10 = stCarLifeFrame;
                            str9 = str5;
                            i4 = i9 + 1;
                        }
                    } else {
                        list = listB;
                        i2 = 1;
                        iM = 110;
                    }
                    int i10 = iM;
                    str8 = str12;
                    try {
                        int iM2 = M(map13, "widthPixels", i2, ControlRequestParser.a);
                        int iM3 = M(map13, "heightPixels", i2, ControlRequestParser.a);
                        int iM4 = map13.get(str11) == null ? 30 : M(map13, str11, i2, 240);
                        long jLongValue = C(map13.get("features")).longValue();
                        int iV2 = v(map13.get("primaryInputDevice"), 0);
                        if (iV2 == 1) {
                            gVar = HidTouchType.TOUCH_SCREEN;
                        } else if (iV2 != 2) {
                            gVar = iV2 != 3 ? HidTouchType.UNKNOWN : HidTouchType.KNOB;
                        } else {
                            gVar = HidTouchType.TOUCHPAD;
                        }
                        HidTouchType gVar2 = gVar;
                        ArrayList arrayList11 = new ArrayList();
                        Iterator<?> it3 = B(map13, "viewAreas", 16).iterator();
                        while (it3.hasNext()) {
                            Object next2 = it3.next();
                            str7 = str11;
                            try {
                                if (!(next2 instanceof Map)) {
                                    throw j("CAP_VIEW_AREA_INVALID");
                                }
                                Map map14 = (Map) next2;
                                Iterator<?> iDisplaySession = it3;
                                int iH = H(map14, str10, 0, 0, iM2);
                                map5 = map6;
                                try {
                                    int iH2 = H(map14, str9, 0, 0, iM3);
                                    arrayList2 = arrayList3;
                                    try {
                                        int iM5 = M(map14, "widthPixels", 1, iM2);
                                        ArrayList arrayList12 = arrayLisDisplaySession;
                                        try {
                                            int iM6 = M(map14, "heightPixels", 1, iM3);
                                            ArrayList arrayList13 = arrayList11;
                                            String str18 = str13;
                                            long j = iM2;
                                            if (((long) iH) + ((long) iM5) <= j) {
                                                long j2 = iM3;
                                                if (((long) iH2) + ((long) iM6) <= j2) {
                                                    Object obj11 = map14.get("safeArea");
                                                    if (obj11 instanceof Map) {
                                                        map14 = (Map) obj11;
                                                    }
                                                    int iH3 = H(map14, str10, iH, 0, iM2);
                                                    stCarLifeFrame = str10;
                                                    try {
                                                        int iH4 = H(map14, str9, iH2, 0, iM3);
                                                        str5 = str9;
                                                        try {
                                                            int iH5 = H(map14, "widthPixels", iM5, 1, iM2);
                                                            int iH6 = H(map14, "heightPixels", iM6, 1, iM3);
                                                            int i11 = iM3;
                                                            if (((long) iH3) + ((long) iH5) > j || ((long) iH4) + ((long) iH6) > j2) {
                                                                throw j("CAP_SAFE_AREA_INVALID");
                                                            }
                                                            arrayList13.add(new a(iH, iH2, iM5, iM6, iH3, iH4, iH5, iH6, Boolean.TRUE.equals(map14.get("drawUIOutsideSafeArea"))));
                                                            arrayList11 = arrayList13;
                                                            it3 = iDisplaySession;
                                                            str13 = str18;
                                                            iM3 = i11;
                                                            str11 = str7;
                                                            map6 = map5;
                                                            arrayList3 = arrayList2;
                                                            arrayLisDisplaySession = arrayList12;
                                                            str10 = stCarLifeFrame;
                                                            str9 = str5;
                                                        } catch (IOException unused4) {
                                                            map4 = map5;
                                                            arrayList = arrayList2;
                                                            arrayLisDisplaySession = arrayList12;
                                                            i3 = i5 + 1;
                                                            arrayList3 = arrayList;
                                                            map6 = map4;
                                                            listB = list;
                                                            str12 = str8;
                                                            str11 = str7;
                                                            str10 = stCarLifeFrame;
                                                            str9 = str5;
                                                            i4 = i9 + 1;
                                                        }
                                                    } catch (RuntimeException unused5) {
                                                        str5 = str9;
                                                    }
                                                }
                                            }
                                            throw j("CAP_VIEW_AREA_INVALID");
                                        } catch (IOException unused6) {
                                            str5 = str9;
                                            stCarLifeFrame = str10;
                                        }
                                    } catch (IOException unused7) {
                                        str5 = str9;
                                        stCarLifeFrame = str10;
                                        map4 = map5;
                                        arrayList = arrayList2;
                                    }
                                } catch (RuntimeException unused8) {
                                    str5 = str9;
                                    stCarLifeFrame = str10;
                                    arrayList = arrayList3;
                                    map4 = map5;
                                }
                            } catch (IOException unused9) {
                                str5 = str9;
                                stCarLifeFrame = str10;
                                arrayList = arrayList3;
                                map4 = map6;
                            }
                            i3 = i5 + 1;
                        }
                        String str19 = str13;
                        int i12 = iM3;
                        str5 = str9;
                        stCarLifeFrame = str10;
                        str7 = str11;
                        arrayList2 = arrayList3;
                        map5 = map6;
                        ArrayList arrayList14 = arrayLisDisplaySession;
                        ArrayList arrayList15 = arrayList11;
                        int iH7 = H(map13, "initialViewArea", 0, 0, Math.max(0, arrayList15.size() - 1));
                        String strB3 = ProtoFieldAccessor.b(ProtoFieldAccessor.a(map13));
                        boolean z5 = strB3 != null && ((Integer) map7.getOrDefault(strB3, 0)).intValue() == 1;
                        c cVar = new c(i9, i10, !map13.containsKey(str19), iM2, i12, iM4, jLongValue, gVar2, z5, arrayList15, iH7);
                        arrayLisDisplaySession = arrayList14;
                        try {
                            arrayLisDisplaySession.add(cVar);
                            arrayList = arrayList2;
                            try {
                                arrayList.add("D:" + cVar.h());
                                if (z5) {
                                    map4 = map5;
                                    try {
                                        map4.put(strB3, Integer.valueOf(i9));
                                    } catch (RuntimeException unused10) {
                                        i3 = i5 + 1;
                                    }
                                } else {
                                    map4 = map5;
                                }
                                i3 = i5;
                            } catch (RuntimeException unused11) {
                                map4 = map5;
                            }
                        } catch (RuntimeException unused12) {
                            map4 = map5;
                            arrayList = arrayList2;
                        }
                    } catch (IOException unused13) {
                        str5 = str9;
                        stCarLifeFrame = str10;
                        str7 = str11;
                    }
                } catch (RuntimeException unused14) {
                    str5 = str9;
                    stCarLifeFrame = str10;
                    str7 = str11;
                    str8 = str12;
                    arrayList = arrayList3;
                    map4 = map6;
                    list = listB;
                }
            } else {
                str5 = str9;
                stCarLifeFrame = str10;
                str7 = str11;
                str8 = str12;
                arrayList = arrayList3;
                map4 = map6;
                list = listB;
                i3 = i5 + 1;
            }
            arrayList3 = arrayList;
            map6 = map4;
            listB = list;
            str12 = str8;
            str11 = str7;
            str10 = stCarLifeFrame;
            str9 = str5;
            i4 = i9 + 1;
        }
    }

    private static int M(Map<?, ?> map, String str, int i, int i2) throws IOException {
        int iV = v(map.get(str), Integer.MIN_VALUE);
        if (iV < i || iV > i2) {
            throw j("CAP_NUMERIC_FIELD_INVALID");
        }
        return iV;
    }

    private static boolean N(int i) {
        return i == 1 || i == 7 || i == 9 || i == 12 || i == 13;
    }

    private static boolean P(Object obj) {
        return ProtoFieldAccessor.d(obj) != null;
    }

    private static boolean Q(Map<?, ?> map, String str, int i) {
        try {
            B(map, str, i);
            return true;
        } catch (IOException unused) {
            return false;
        }
    }

    private static IOException j(String str) {
        return new IOException(str);
    }

    private static Set<String> n(Map<?, ?> map, String str, Set<String> set) {
        TreeSet treeSet = new TreeSet();
        for (Object obj : I(map, str, 32)) {
            if (obj instanceof String) {
                String str2 = (String) obj;
                if (set.contains(str2)) {
                    treeSet.add(str2);
                }
            }
        }
        return treeSet;
    }

    private static String q(String str) {
        try {
            byte[] bArrDigest = MessageDigest.getInstance("SHA-256").digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b2 : bArrDigest) {
                sb.append(String.format(Locale.ROOT, "%02x", Integer.valueOf(b2 & 255)));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e2) {
            throw new IllegalStateException("SHA256 unavailable", e2);
        }
    }

    
    private static f s(byte[] bArr) throws IOException {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        byte[] bArr2 = bArr;
        if (bArr2 == null || bArr2.length == 0 || bArr2.length > 4096) {
            return new f(false, false, false, "INVALID", 0);
        }
        int i6 = 0;
        d dVar = new d(i6);
        ArrayDeque arrayDeque = new ArrayDeque();
        ArrayList arrayList = new ArrayList();
        StringBuilder sb = new StringBuilder();
        int i7 = 0;
        int i8 = 0;
        int i9 = 0;
        boolean z = false;
        boolean z2 = false;
        while (i7 < bArr2.length) {
            try {
                int i10 = i7 + 1;
                byte b2 = bArr2[i7];
                int i11 = b2 & 255;
                if (i11 == 254) {
                    throw j("CAP_HID_LONG_ITEM");
                }
                int i12 = b2 & 3;
                if (i12 == 3) {
                    i12 = 4;
                }
                if (i10 > bArr2.length - i12) {
                    throw j("CAP_HID_TRUNCATED");
                }
                long j = 0;
                while (i6 < i12) {
                    j |= ((long) (bArr[i10 + i6] & 255)) << (i6 * 8);
                    i6++;
                    i8 = i8;
                    i9 = i9;
                }
                int i13 = i8;
                int i14 = i9;
                int i15 = i10 + i12;
                long j2 = 1;
                if (i12 == 0) {
                    j2 = 1;
                    i = 0;
                } else {
                    int i16 = i12 * 8;
                    i = (int) ((j & (1 << (i16 + (-1)))) != 0 ? j - (1 << i16) : j);
                }
                int i17 = (i11 >>> 2) & 3;
                int i18 = i11 >>> 4;
                if (i17 == 1) {
                    if (i18 == 0) {
                        dVar.a = (int) j;
                    } else if (i18 == 1) {
                        dVar.e = i;
                    } else if (i18 != 2) {
                        switch (i18) {
                            case 7:
                                dVar.b = (int) j;
                                break;
                            case 8:
                                dVar.d = (int) j;
                                break;
                            case 9:
                                dVar.c = (int) j;
                                break;
                            case 10:
                                if (arrayDeque.size() >= 16) {
                                    throw j("CAP_HID_STACK");
                                }
                                arrayDeque.push(dVar.a());
                                break;
                            case 11:
                                if (arrayDeque.isEmpty()) {
                                    throw j("CAP_HID_STACK");
                                }
                                dVar = (d) arrayDeque.pop();
                                break;
                        }
                    } else {
                        if (dVar.e >= 0) {
                            i = (int) j;
                        }
                        dVar.f = i;
                    }
                } else if (i17 != 2) {
                    if (i17 == 0) {
                        if (i18 == 10) {
                            int i19 = i13 + 1;
                            if (i19 > 32) {
                                throw j("CAP_HID_COLLECTION");
                            }
                            if (arrayList.contains(Integer.valueOf(t1.f))) {
                                z = true;
                            }
                            sb.append("C:");
                            sb.append(255 & j);
                            sb.append(':');
                            sb.append(arrayList);
                            sb.append(';');
                            i8 = i19;
                        } else if (i18 == 12) {
                            i8 = i13 - 1;
                            if (i8 < 0) {
                                throw j("CAP_HID_COLLECTION");
                            }
                            sb.append("E;");
                        } else {
                            if (i18 == 8) {
                                i9 = i14 + 1;
                                if (i9 > 256 || (i2 = dVar.b) < 0 || i2 > 32 || (i3 = dVar.c) < 0 || i3 > 1024 || (i4 = dVar.d) < 0 || i4 > 255) {
                                    throw j("CAP_HID_FIELDS");
                                }
                                if (arrayList.contains(65591) || arrayList.contains(65592)) {
                                    z = true;
                                }
                                if ((j & j2) == 0 && ((i5 = dVar.a) == 7 || i5 == 9 || i5 == 12)) {
                                    z2 = true;
                                }
                                sb.append(N(dVar.a) ? dVar.a : 0);
                                sb.append('/');
                                sb.append(dVar.d);
                                sb.append('/');
                                sb.append(dVar.b);
                                sb.append('/');
                                sb.append(dVar.c);
                                sb.append('/');
                                sb.append(dVar.e);
                                sb.append('/');
                                sb.append(dVar.f);
                                sb.append('/');
                                sb.append(511 & j);
                                sb.append('/');
                                sb.append(arrayList);
                                sb.append(';');
                                i8 = i13;
                            } else {
                                i8 = i13;
                            }
                            arrayList.clear();
                        }
                        i9 = i14;
                        arrayList.clear();
                    }
                    i7 = i15;
                    i6 = 0;
                    bArr2 = bArr;
                } else {
                    if (arrayList.size() >= 256) {
                        throw j("CAP_HID_FIELDS");
                    }
                    if (i18 == 0 || i18 == 1 || i18 == 2) {
                        int i20 = i12 == 4 ? (int) (j >>> 16) : dVar.a;
                        arrayList.add(Integer.valueOf(N(i20) ? (i20 << 16) | (((int) j) & 65535) : 0));
                    }
                }
                i8 = i13;
                i9 = i14;
                i7 = i15;
                i6 = 0;
                bArr2 = bArr;
            } catch (IOException unused) {
                return new f(false, false, false, "INVALID", 0);
            }
        }
        int i21 = i9;
        if (i8 == 0 && arrayDeque.isEmpty() && i21 != 0) {
            return new f(true, z, z2, q(sb.toString()), 0);
        }
        throw j("CAP_HID_INCOMPLETE");
    }

    private static int v(Object obj, int i) {
        if ((obj instanceof Byte) || (obj instanceof Short) || (obj instanceof Integer) || (obj instanceof Long)) {
            long jLongValue = ((Number) obj).longValue();
            if (jLongValue >= -2147483648L && jLongValue <= 2147483647L) {
                return (int) jLongValue;
            }
        }
        return i;
    }

    
    public static  boolean w(int i, c cVar) {
        return cVar.p() == i;
    }

    
    public static  boolean x(c cVar) {
        return cVar.q() == 110 && cVar.k();
    }

    
    public static  long y(c cVar) {
        return ((long) cVar.v()) * ((long) cVar.j());
    }

    
    public static  IOException z() {
        return j("CAP_MAIN_DISPLAY_UNAVAILABLE");
    }

    public boolean D() {
        return this.h;
    }

    public boolean E() {
        return this.i;
    }

    public Set<String> F() {
        return this.m;
    }

    public c L() throws IOException {
        java.util.Optional<c> optionalMin = this.a.stream().filter(new w0(9)).min(Comparator.comparing(new r(2)).thenComparing(Comparator.comparingLong(new p(2)).reversed()).thenComparingInt(new q(1)));
        return optionalMin.orElseThrow(HidCapabilities::z);
    }

    public String O() {
        c cVarL;
        String str;
        try {
            cVarL = L();
        } catch (IOException unused) {
            cVarL = null;
        }
        int iP = cVarL == null ? -3 : cVarL.p();
        Stream<e> stream = this.b.stream();
        u uVar = new u(0);
        uVar.b = iP;
        VarHandle.storeStoreFence();
        long jCount = stream.filter(uVar).count();
        int size = this.a.size();
        int i = this.o;
        if (cVarL == null) {
            str = "NONE";
        } else {
            str = cVarL.v() + "x" + cVarL.j() + "@" + cVarL.n();
        }
        return "CAPS schema=1 displays=" + size + " rejected_displays=" + i + " main=" + str + " input=" + String.valueOf(cVarL == null ? HidTouchType.UNKNOWN : cVarL.o()) + " hid=" + this.b.size() + " hid_valid=" + this.f + " touch=" + jCount + " knobs=" + this.b.stream().filter(new w0(7)).count() + " keys=" + this.b.stream().filter(new w0(8)).count() + " audio_rows=" + this.c.size() + " audio_valid=" + this.e + " audio_legacy=" + this.d.l() + " modes_declared=" + this.h + " modes_valid=" + this.i + " feature_bits=" + this.k.toString(16) + " feature_lists_valid=" + this.g + " negotiated=" + this.m.size() + " iap_channel=" + u() + " fingerprint=" + this.n;
    }

    public AudioCapabilities f() {
        return this.d;
    }

    public List<b> g() {
        return this.c;
    }

    public boolean h() {
        return this.e;
    }

    public List<c> i() {
        return this.a;
    }

    public Set<String> k() {
        return this.l;
    }

    public boolean l() {
        return this.g;
    }

    public boolean m() {
        return this.j;
    }

    public String o() {
        return this.n;
    }

    public boolean p(int i) {
        return i >= 0 && i < 64 && this.k.testBit(i);
    }

    public List<e> r() {
        return this.b;
    }

    public boolean t() {
        return this.f;
    }

    public String toString() {
        return O();
    }

    public boolean u() {
        return this.m.contains("iAPChannel");
    }

    /**
     * HidCapabilities 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in HidCapabilities, carrying local implementation detail.
     */
    public static final class d {
        int a;
        int b;
        int c;
        int d;
        int e;
        int f;

        public  d(int i) {
            this();
        }

        public d a() {
            d dVar = new d();
            dVar.a = this.a;
            dVar.b = this.b;
            dVar.c = this.c;
            dVar.d = this.d;
            dVar.e = this.e;
            dVar.f = this.f;
            return dVar;
        }

        private d() {
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class f extends ProtoMessageBase {
        private final boolean a;
        private final boolean b;
        private final boolean c;
        private final String d;

        private f(boolean z, boolean z2, boolean z3, String str) {
            this.a = z;
            this.b = z2;
            this.c = z3;
            this.d = str;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof f)) {
                return false;
            }
            f fVar = (f) obj;
            return this.a == fVar.a && this.b == fVar.b && this.c == fVar.c && Objects.equals(this.d, fVar.d);
        }

        public final int hashCode() {
            boolean z = this.a;
            boolean z2 = this.b;
            boolean z3 = this.c;
            String str = this.d;
            return Objects.hashCode(str) + ((Boolean.hashCode(z3) + ((Boolean.hashCode(z2) + (Boolean.hashCode(z) * 31)) * 31)) * 31);
        }

        public String k() {
            return this.d;
        }

        public boolean l() {
            return this.c;
        }

        public boolean m() {
            return this.b;
        }

        public boolean n() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Boolean.valueOf(this.a), Boolean.valueOf(this.b), Boolean.valueOf(this.c), this.d};
            String[] strArrSplit = "a;b;c;d".length() == 0 ? new String[0] : "a;b;c;d".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(f.class, sb, "[");
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

        public  f(boolean z, boolean z2, boolean z3, String str, int i) {
            this(z, z2, z3, str);
        }
    }
}
