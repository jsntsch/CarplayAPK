/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 BinaryPlist。二进制 plist 编解码：解析配对记录（bplist00）并校验对象表边界。
 *   This file defines BinaryPlist. Binary plist codec: parses pairing records (bplist00) and validates the object table bounds.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;



/**
 * 二进制 plist 编解码：解析配对记录（bplist00）并校验对象表边界。
 *
 * Binary plist codec: parses pairing records (bplist00) and validates the object table bounds.
 */
public final class BinaryPlist {
    private static final byte[] a = "bplist00".getBytes(StandardCharsets.US_ASCII);
    private static final int b = 2097152;
    private static final int c = 16384;
    private static final int d = 64;

    /**
     * BinaryPlist 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in BinaryPlist, carrying local implementation detail.
     */
    public static final class a {
        private final byte[] a;
        private final int b;
        private final int c;
        private final int d;
        private final int e;
        private final int[] f;
        private final Object[] g;
        private final byte[] h;

        public a(byte[] bArr) throws IOException {
            if (bArr == null || bArr.length < 40 || bArr.length > BinaryPlist.b || !Arrays.equals(Arrays.copyOf(bArr, 8), BinaryPlist.a)) {
                SyntheticRefs.c("invalid plist header/limit");
                throw null;
            }
            byte[] bArr2 = (byte[]) bArr.clone();
            this.a = bArr2;
            int length = bArr2.length;
            int i = length - 32;
            int i2 = bArr2[length - 26] & 255;
            int i3 = bArr2[length - 25] & 255;
            this.d = i3;
            if (!f(i2) || !f(i3)) {
                SyntheticRefs.c("invalid plist table width");
                throw null;
            }
            int iA = a(length - 24, 8, BinaryPlist.c);
            this.b = iA;
            this.c = a(length - 16, 8, iA - 1);
            int iA2 = a(length - 8, 8, i);
            this.e = iA2;
            if (iA >= 1 && iA2 >= 8) {
                if ((((long) iA) * ((long) i2)) + ((long) iA2) <= i) {
                    this.f = new int[iA];
                    this.g = new Object[iA];
                    this.h = new byte[iA];
                    for (int i4 = 0; i4 < this.b; i4++) {
                        int[] iArr = this.f;
                        int i5 = this.e;
                        iArr[i4] = a((i4 * i2) + i5, i2, i5 - 1);
                        if (this.f[i4] < 8) {
                            SyntheticRefs.c("invalid plist object offset");
                            throw null;
                        }
                    }
                    return;
                }
            }
            SyntheticRefs.c("invalid plist offset table");
            throw null;
        }

        private int a(int i, int i2, int i3) throws IOException {
            long jE = e(i, i2);
            if (jE >= 0 && jE <= i3) {
                return (int) jE;
            }
            SyntheticRefs.c("plist numeric bound");
            return 0;
        }

        private void c(int i, long j) throws IOException {
            if (i < 8 || j < 0 || ((long) i) + j > this.e) {
                SyntheticRefs.c("plist object crosses table");
            }
        }

        private long e(int i, int i2) throws IOException {
            if (i < 0 || ((long) i) + ((long) i2) > this.a.length) {
                SyntheticRefs.c("truncated plist integer");
                return 0L;
            }
            long j = 0;
            for (int i3 = 0; i3 < i2; i3++) {
                j = (j << 8) | ((long) (this.a[i + i3] & 255));
            }
            return j;
        }

        private static boolean f(int i) {
            return i == 1 || i == 2 || i == 4 || i == 8;
        }

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        public Object b(int i, int i2) throws IOException {
            int i3;
            
            
            Object CopyOfRange = 0;
            if (i < 0 || i >= this.b || i2 > 64) {
                SyntheticRefs.c("plist reference/depth limit");
                return null;
            }
            byte[] bArr = this.h;
            byte b = bArr[i];
            if (b == 1) {
                SyntheticRefs.c("cyclic peer plist");
                return null;
            }
            if (b == 2) {
                return this.g[i];
            }
            bArr[i] = 1;
            int i4 = this.f[i];
            int i5 = i4 + 1;
            byte b2 = this.a[i4];
            int i6 = (b2 & 255) >>> 4;
            int iA = b2 & 15;
            if (i6 == 0) {
                if (iA != 0) {
                    if (iA == 8) {
                        CopyOfRange = Boolean.FALSE;
                    } else {
                        if (iA != 9) {
                            SyntheticRefs.c("unsupported plist singleton");
                            return null;
                        }
                        CopyOfRange = Boolean.TRUE;
                    }
                }
            } else if (i6 == 1) {
                if (iA > 3) {
                    SyntheticRefs.c("plist integer too large");
                    return null;
                }
                int i7 = 1 << iA;
                c(i5, i7);
                CopyOfRange = new BigInteger(1, Arrays.copyOfRange(this.a, i5, i7 + i5));
                if (((BigInteger) CopyOfRange).bitLength() < 64) {
                    CopyOfRange = Long.valueOf(((BigInteger) CopyOfRange).longValue());
                }
            } else if (i6 == 2) {
                if (iA == 2) {
                    c(i5, 4L);
                    CopyOfRange = Double.valueOf(Float.intBitsToFloat((int) e(i5, 4)));
                } else {
                    if (iA != 3) {
                        SyntheticRefs.c("invalid plist real width");
                        return null;
                    }
                    c(i5, 8L);
                    CopyOfRange = Double.valueOf(Double.longBitsToDouble(e(i5, 8)));
                }
            } else if (i6 == 8) {
                int i8 = iA + 1;
                c(i5, i8);
                CopyOfRange = new c(new BigInteger(1, Arrays.copyOfRange(this.a, i5, i8 + i5)));
            } else {
                if (iA == 15) {
                    c(i5, 1L);
                    int i9 = i4 + 2;
                    byte b3 = this.a[i5];
                    if (((b3 & 255) >>> 4) != 1 || (i3 = b3 & 15) > 3) {
                        SyntheticRefs.c("invalid plist extended length");
                        return null;
                    }
                    int i10 = 1 << i3;
                    iA = a(i9, i10, BinaryPlist.b);
                    i5 = i9 + i10;
                }
                if (i6 != 4) {
                    int i11 = 0;
                    if (i6 == 5) {
                        c(i5, iA);
                        while (true) {
                            byte[] bArr2 = this.a;
                            if (i11 >= iA) {
                                CopyOfRange = new String(bArr2, i5, iA, StandardCharsets.US_ASCII);
                                break;
                            }
                            if (bArr2[i5 + i11] < 0) {
                                SyntheticRefs.c("invalid ASCII plist string");
                                return null;
                            }
                            i11++;
                        }
                    } else if (i6 != 6) {
                        
                        
                        
                        
                        
                        if (i6 == 13) {
                            if (iA > 8192) {
                                SyntheticRefs.c("plist map limit");
                                return null;
                            }
                            c(i5, ((long) iA) * ((long) this.d) * 2);
                            CopyOfRange = new LinkedHashMap();
                            while (i11 < iA) {
                                int i12 = this.d;
                                int i13 = i2 + 1;
                                Object objB = b(a((i11 * i12) + i5, i12, this.b - 1), i13);
                                if (!(objB instanceof String)) {
                                    SyntheticRefs.c("invalid/duplicate dictionary key");
                                    return null;
                                }
                                String str = (String) objB;
                                if (((Map) CopyOfRange).containsKey(str)) {
                                    SyntheticRefs.c("invalid/duplicate dictionary key");
                                    return null;
                                }
                                int i14 = this.d;
                                ((Map) CopyOfRange).put(str, b(a(((i11 + iA) * i14) + i5, i14, this.b - 1), i13));
                                i11++;
                            }
                        } else {
                            if (i6 != 10) {
                                SyntheticRefs.c("unsupported plist object type");
                                return null;
                            }
                            if (iA > BinaryPlist.c) {
                                SyntheticRefs.c("plist array limit");
                                return null;
                            }
                            c(i5, ((long) iA) * ((long) this.d));
                            CopyOfRange = new ArrayList();
                            while (i11 < iA) {
                                int i15 = this.d;
                                ((List) CopyOfRange).add(b(a((i11 * i15) + i5, i15, this.b - 1), i2 + 1));
                                i11++;
                            }
                        }
                    } else {
                        if (iA > 1048576) {
                            SyntheticRefs.c("UTF16 plist length");
                            return null;
                        }
                        int i16 = iA * 2;
                        c(i5, i16);
                        CopyOfRange = new String(this.a, i5, i16, StandardCharsets.UTF_16BE);
                    }
                } else {
                    c(i5, iA);
                    CopyOfRange = Arrays.copyOfRange(this.a, i5, iA + i5);
                }
            }
            this.g[i] = CopyOfRange;
            this.h[i] = 2;
            return CopyOfRange;
        }

        public Object d() throws IOException {
            return b(this.c, 0);
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class c extends ProtoMessageBase {
        private final BigInteger a;

        public c(BigInteger bigInteger) {
            if (bigInteger == null || bigInteger.signum() < 0 || bigInteger.bitLength() > 128) {
                SyntheticRefs.b("plist UID bound");
                throw null;
            }
            this.a = bigInteger;
        }

        public final boolean equals(Object obj) {
            return (obj instanceof c) && Objects.equals(this.a, ((c) obj).a);
        }

        public BigInteger g() {
            return this.a;
        }

        public final int hashCode() {
            return Objects.hashCode(this.a);
        }

        public final String toString() {
            Object[] objArr = {this.a};
            String[] strArrSplit = "a".length() == 0 ? new String[0] : "a".split(";");
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

    private BinaryPlist() {
    }

    
    private static int c(Object obj, List<b> list, IdentityHashMap<Object, Boolean> identityHashMap, int i) throws IOException {
        int i2 = 0;
        if (i > 64 || list.size() >= c) {
            SyntheticRefs.c("plist structure exceeds limit");
            return 0;
        }
        int size = list.size();
        int[] iArr = null;
        list.add(null);
        int i3 = 1;
        if (obj == null) {
            list.set(size, new b(new byte[]{0}, i2, iArr));
            return size;
        }
        if (obj instanceof Boolean) {
            list.set(size, new b(new byte[]{(byte) (((Boolean) obj).booleanValue() ? 9 : 8)}, i2, iArr));
            return size;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() > 1048576) {
                SyntheticRefs.c("plist string exceeds limit");
                return 0;
            }
            boolean zAllMatch = str.chars().allMatch(new k(i2));
            list.set(size, new b(d(h(zAllMatch ? 5 : 6, str.length()), str.getBytes(zAllMatch ? StandardCharsets.US_ASCII : StandardCharsets.UTF_16BE)), i2, iArr));
            return size;
        }
        if (obj instanceof byte[]) {
            byte[] bArr = (byte[]) obj;
            if (bArr.length <= b) {
                list.set(size, new b(d(h(4, bArr.length), bArr), i2, iArr));
                return size;
            }
            SyntheticRefs.c("plist data exceeds limit");
            return 0;
        }
        if ((obj instanceof Double) || (obj instanceof Float)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(35);
            j(byteArrayOutputStream, Double.doubleToLongBits(((Number) obj).doubleValue()), 8);
            list.set(size, new b(byteArrayOutputStream.toByteArray(), i2, iArr));
            return size;
        }
        if (obj instanceof c) {
            byte[] byteArray = ((c) obj).g().toByteArray();
            int i4 = (byteArray.length <= 1 || byteArray[0] != 0) ? 0 : 1;
            list.set(size, new b(d(new byte[]{(byte) (((byteArray.length - i4) - 1) | Iap2Csm.b)}, Arrays.copyOfRange(byteArray, i4, byteArray.length)), i2, iArr));
            return size;
        }
        if (obj instanceof Number) {
            BigInteger bigIntegerValueOf = obj instanceof BigInteger ? (BigInteger) obj : BigInteger.valueOf(((Number) obj).longValue());
            if (bigIntegerValueOf.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0 || bigIntegerValueOf.bitLength() > 64) {
                SyntheticRefs.c("plist integer outside signed64/unsigned64 bounds");
                return 0;
            }
            if (bigIntegerValueOf.signum() < 0) {
                i3 = 8;
            } else if (bigIntegerValueOf.bitLength() > 8) {
                if (bigIntegerValueOf.bitLength() <= 16) {
                    i3 = 2;
                } else if (bigIntegerValueOf.bitLength() <= 32) {
                    i3 = 4;
                } else {
                    i3 = 8;
                }
            }
            ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
            byteArrayOutputStream2.write(16 | Integer.numberOfTrailingZeros(i3));
            j(byteArrayOutputStream2, bigIntegerValueOf.longValue(), i3);
            list.set(size, new b(byteArrayOutputStream2.toByteArray(), i2, iArr));
            return size;
        }
        if (obj instanceof List) {
            List list2 = (List) obj;
            if (identityHashMap.put(obj, Boolean.TRUE) != null) {
                SyntheticRefs.c("cyclic plist input");
                return 0;
            }
            int size2 = list2.size();
            int[] iArr2 = new int[size2];
            if (size2 > c) {
                SyntheticRefs.c("plist array limit");
                return 0;
            }
            for (int i5 = 0; i5 < size2; i5++) {
                iArr2[i5] = c(list2.get(i5), list, identityHashMap, i + 1);
            }
            identityHashMap.remove(obj);
            list.set(size, new b(h(10, size2), i2, iArr2));
            return size;
        }
        if (!(obj instanceof Map)) {
            SyntheticRefs.c("unsupported plist value type");
            return 0;
        }
        Map map = (Map) obj;
        if (map.size() > 8192) {
            SyntheticRefs.c("plist dictionary limit");
            return 0;
        }
        if (identityHashMap.put(obj, Boolean.TRUE) != null) {
            SyntheticRefs.c("cyclic plist input");
            return 0;
        }
        int[] iArr3 = new int[map.size() * 2];
        int i6 = 0;
        for (Map.Entry entry : ((Map<?, ?>) map).entrySet()) {
            Object key = entry.getKey();
            if (!(key instanceof String)) {
                SyntheticRefs.c("plist dictionary key must be string");
                return 0;
            }
            int i7 = i + 1;
            iArr3[i6] = c((String) key, list, identityHashMap, i7);
            iArr3[map.size() + i6] = c(entry.getValue(), list, identityHashMap, i7);
            i6++;
        }
        identityHashMap.remove(obj);
        list.set(size, new b(h(13, map.size()), i2, iArr3));
        return size;
    }

    private static byte[] d(byte[] bArr, byte[] bArr2) {
        byte[] bArrCopyOf = Arrays.copyOf(bArr, bArr.length + bArr2.length);
        System.arraycopy(bArr2, 0, bArrCopyOf, bArr.length, bArr2.length);
        return bArrCopyOf;
    }

    public static Object e(byte[] bArr) throws IOException {
        return new a(bArr).d();
    }

    public static byte[] f(Object obj) throws IOException {
        ArrayList arrayList = new ArrayList();
        int iC = c(obj, arrayList, new IdentityHashMap(), 0);
        int i = i(arrayList.size() - 1);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.writeBytes(a);
        int size = arrayList.size();
        int[] iArr = new int[size];
        for (int i2 = 0; i2 < arrayList.size(); i2++) {
            iArr[i2] = byteArrayOutputStream.size();
            b bVar = (b) arrayList.get(i2);
            byteArrayOutputStream.writeBytes(bVar.g());
            if (bVar.h() != null) {
                for (int i3 : bVar.h()) {
                    j(byteArrayOutputStream, i3, i);
                }
            }
            if (byteArrayOutputStream.size() > b) {
                SyntheticRefs.c("plist object table exceeds limit");
                return null;
            }
        }
        long size2 = byteArrayOutputStream.size();
        int i4 = i(size2);
        for (int i5 = 0; i5 < size; i5++) {
            j(byteArrayOutputStream, iArr[i5], i4);
        }
        byteArrayOutputStream.writeBytes(new byte[6]);
        byteArrayOutputStream.write(i4);
        byteArrayOutputStream.write(i);
        j(byteArrayOutputStream, arrayList.size(), 8);
        j(byteArrayOutputStream, iC, 8);
        j(byteArrayOutputStream, size2, 8);
        if (byteArrayOutputStream.size() <= b) {
            return byteArrayOutputStream.toByteArray();
        }
        SyntheticRefs.c("plist exceeds limit");
        return null;
    }

    
    public static  boolean g(int i) {
        return i < 128;
    }

    private static byte[] h(int i, int i2) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (i2 < 15) {
            byteArrayOutputStream.write((i << 4) | i2);
        } else {
            byteArrayOutputStream.write((i << 4) | 15);
            long j = i2;
            int i3 = i(j);
            byteArrayOutputStream.write(Integer.numberOfTrailingZeros(i3) | 16);
            j(byteArrayOutputStream, j, i3);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static int i(long j) {
        if (j <= 255) {
            return 1;
        }
        if (j <= 65535) {
            return 2;
        }
        return j <= 4294967295L ? 4 : 8;
    }

    private static void j(ByteArrayOutputStream byteArrayOutputStream, long j, int i) {
        for (int i2 = i - 1; i2 >= 0; i2--) {
            byteArrayOutputStream.write(((int) (j >>> (i2 * 8))) & 255);
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class b extends ProtoMessageBase {
        private final byte[] a;
        private final int[] b;

        private b(byte[] bArr, int[] iArr) {
            this.a = bArr;
            this.b = iArr;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return Objects.equals(this.a, bVar.a) && Objects.equals(this.b, bVar.b);
        }

        public byte[] g() {
            return this.a;
        }

        public int[] h() {
            return this.b;
        }

        public final int hashCode() {
            byte[] bArr = this.a;
            int[] iArr = this.b;
            return Objects.hashCode(iArr) + (Objects.hashCode(bArr) * 31);
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b};
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

        public  b(byte[] bArr, int i, int[] iArr) {
            this(bArr, iArr);
        }
    }
}
