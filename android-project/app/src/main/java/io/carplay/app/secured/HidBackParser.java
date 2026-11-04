/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 HidBackParser。HID 按键/返回键解析：处理非触摸的按键描述符与事件映射。
 *   This file defines HidBackParser. HID button/back-key parser handling non-touch key descriptors and event mapping.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;



/**
 * HID 按键/返回键解析：处理非触摸的按键描述符与事件映射。
 *
 * HID button/back-key parser handling non-touch key descriptors and event mapping.
 */
public final class HidBackParser {
    
    private static final int d = 786980;
    
    private static final int e = 458793;
    
    private static final int DESCRIPTOR_MAX_BYTES = 4096;
    
    private static final int REPORT_BITS_MAX = 8192;
    
    private static final int REPORT_ID_MAX_COUNT = 16;
    
    private static final int COLLECTION_DEPTH_MAX = 16;
    
    private static final int FIELD_MAX_PER_REPORT = 128;
    
    private static final int USAGE_MAX_COUNT = 256;
    
    private static final int GLOBAL_PUSH_STACK_MAX = 8;
    private final Map<Integer, c> a;
    private final boolean b;
    private final int c;

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class d extends ProtoMessageBase {
        private final int a;
        private final boolean b;

        public d(int i, boolean z) {
            this.a = i;
            this.b = z;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof d)) {
                return false;
            }
            d dVar = (d) obj;
            return this.b == dVar.b && this.a == dVar.a;
        }

        public boolean g() {
            return this.b;
        }

        public int h() {
            return this.a;
        }

        public final int hashCode() {
            boolean z = this.b;
            return (Boolean.hashCode(z) * 31) + this.a;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Boolean.valueOf(this.b)};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(d.class, sb, "[");
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

    private HidBackParser(Map<Integer, c> map, boolean z) {
        this.a = Map.copyOf(map);
        this.b = z;
        
        
        int declared = 0;
        for (c report : map.values()) {
            if (!report.b.isEmpty()) {
                declared++;
            }
        }
        this.c = declared;
    }

    
    public static boolean e(int i) {
        return i == d || i == e;
    }

    
    public static IOException g(String str) {
        return new IOException(str);
    }

    
    public static  boolean h(c cVar) {
        return !cVar.b.isEmpty();
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static io.carplay.app.secured.HidBackParser i(byte[] descriptor) throws IOException {
        if (descriptor == null || descriptor.length == 0 || descriptor.length > DESCRIPTOR_MAX_BYTES) {
            throw g("HID_BACK_DESCRIPTOR_LIMIT");
        }
        b global = new b(0);
        ArrayDeque<b> globalStack = new ArrayDeque<>();
        ArrayList<Integer> usages = new ArrayList<>();
        LinkedHashMap<Integer, c> reports = new LinkedHashMap<>();
        boolean hasReportId = false;
        Integer usageMin = null;
        Integer usageMax = null;
        int depth = 0;
        int offset = 0;

        while (offset < descriptor.length) {
            int prefix = descriptor[offset] & 255;
            int dataSize = (prefix & 3) == 3 ? 4 : (prefix & 3);
            int itemType = (prefix >> 2) & 3;
            int itemTag = prefix >> 4;
            if (prefix == 254) {
                throw g("HID_BACK_LONG_ITEM");
            }
            int dataOffset = offset + 1;
            if (dataOffset + dataSize > descriptor.length) {
                throw g("HID_BACK_TRUNCATED");
            }
            long rawValue = 0;
            for (int i = 0; i < dataSize; i++) {
                rawValue |= ((long) (descriptor[dataOffset + i] & 255)) << (i * 8);
            }
            long signedValue;
            if (dataSize == 0) {
                signedValue = 0;
            } else if ((rawValue & (1L << ((dataSize * 8) - 1))) != 0) {
                signedValue = rawValue - (1L << (dataSize * 8));
            } else {
                signedValue = rawValue;
            }
            int nextOffset = dataOffset + dataSize;

            if (itemType == 1) {
                switch (itemTag) {
                    case 0:
                        if (rawValue > 65535) {
                            throw g("HID_BACK_PAGE");
                        }
                        global.a = (int) rawValue;
                        break;
                    case 1:
                        global.b = (int) signedValue;
                        global.g = true;
                        break;
                    case 2:
                        long logicalMax = global.b < 0 ? signedValue : rawValue;
                        if (logicalMax > 2147483647L) {
                            throw g("HID_BACK_RANGE");
                        }
                        global.c = (int) logicalMax;
                        global.h = true;
                        break;
                    case 7:
                        if (rawValue > 32) {
                            throw g("HID_BACK_SIZE");
                        }
                        global.d = (int) rawValue;
                        break;
                    case 8:
                        if (rawValue < 1 || rawValue > 255) {
                            throw g("HID_BACK_ID");
                        }
                        global.f = (int) rawValue;
                        hasReportId = true;
                        break;
                    case 9:
                        if (rawValue > 512) {
                            throw g("HID_BACK_COUNT");
                        }
                        global.e = (int) rawValue;
                        break;
                    case 10:
                        if (dataSize != 0 || globalStack.size() >= GLOBAL_PUSH_STACK_MAX) {
                            throw g("HID_BACK_STACK");
                        }
                        globalStack.push(global.a());
                        break;
                    case 11:
                        if (dataSize != 0 || globalStack.isEmpty()) {
                            throw g("HID_BACK_STACK");
                        }
                        global = globalStack.pop();
                        break;
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        break;
                    default:
                        throw g("HID_BACK_GLOBAL");
                }
            } else if (itemType == 2) {
                if (itemTag == 0) {
                    if (usages.size() >= USAGE_MAX_COUNT) {
                        throw g("HID_BACK_USAGE_LIMIT");
                    }
                    usages.add(k(rawValue, dataSize, global.a));
                } else if (itemTag == 1) {
                    usageMin = k(rawValue, dataSize, global.a);
                } else if (itemTag == 2) {
                    usageMax = k(rawValue, dataSize, global.a);
                } else if (itemTag < 3 || itemTag > 9) {
                    throw g("HID_BACK_LOCAL");
                }
            } else if (itemType == 0) {
                ArrayList<Integer> itemUsages = new ArrayList<>(usages);
                if (usageMin == null) {
                    if (usageMax != null) {
                        throw g("HID_BACK_USAGE_RANGE");
                    }
                } else {
                    if (usageMax == null) {
                        throw g("HID_BACK_USAGE_RANGE");
                    }
                    int min = usageMin.intValue();
                    int max = usageMax.intValue();
                    if (min > max) {
                        throw g("HID_BACK_USAGE_RANGE");
                    }
                    
                    if ((min >>> 16) != (max >>> 16)) {
                        throw g("HID_BACK_USAGE_RANGE");
                    }
                    long span = ((long) max) - ((long) min);
                    if (span > ((long) (255 - itemUsages.size()))) {
                        throw g("HID_BACK_USAGE_RANGE");
                    }
                    for (int usage = min; usage <= max; usage++) {
                        itemUsages.add(Integer.valueOf(usage));
                    }
                }

                if (itemTag == 10) {
                    
                    if (dataSize != 1 || rawValue > 2) {
                        throw g("HID_BACK_COLLECTION");
                    }
                    depth++;
                    if (depth > COLLECTION_DEPTH_MAX) {
                        throw g("HID_BACK_COLLECTION");
                    }
                } else if (itemTag == 12) {
                    
                    if (dataSize != 0 || depth <= 0) {
                        throw g("HID_BACK_COLLECTION");
                    }
                    depth--;
                } else if (itemTag == 8) {
                    
                    if (depth == 0) {
                        throw g("HID_BACK_INPUT_LIMIT");
                    }
                    if (global.d < 1 || global.e < 1
                            || ((long) global.d) * ((long) global.e) > REPORT_BITS_MAX) {
                        throw g("HID_BACK_INPUT_LIMIT");
                    }
                    Integer reportId = Integer.valueOf(global.f);
                    c report = reports.get(reportId);
                    if (report == null) {
                        if (reports.size() >= REPORT_ID_MAX_COUNT) {
                            throw g("HID_BACK_REPORT_LIMIT");
                        }
                        report = new c(0);
                        reports.put(reportId, report);
                    }
                    long totalBits = ((long) report.a) + ((long) global.d) * ((long) global.e);
                    if (totalBits > REPORT_BITS_MAX) {
                        throw g("HID_BACK_REPORT_LIMIT");
                    }
                    boolean keepField = (rawValue & 1) == 0;
                    if (keepField) {
                        boolean hasBackUsage = false;
                        for (Integer usage : itemUsages) {
                            if (e(usage.intValue())) {
                                hasBackUsage = true;
                                break;
                            }
                        }
                        keepField = hasBackUsage;
                    }
                    if (keepField) {
                        boolean variable = (rawValue & 2) != 0;
                        boolean nullState = (rawValue & 0x40) != 0;
                        
                        if ((rawValue & (-0x43)) != 0) {
                            throw g("HID_BACK_INPUT_TYPE");
                        }
                        if (global.d > 16 || !global.g || !global.h) {
                            throw g("HID_BACK_INPUT_TYPE");
                        }
                        if (global.b < 0 || global.c < global.b) {
                            throw g("HID_BACK_INPUT_TYPE");
                        }
                        if (((long) global.c) >= (1L << global.d)) {
                            throw g("HID_BACK_INPUT_TYPE");
                        }
                        if (variable) {
                            if (global.b != 0 || global.c != 1) {
                                throw g("HID_BACK_BOOLEAN_RANGE");
                            }
                        } else {
                            long span = ((long) global.c) - ((long) global.b) + 1L;
                            if (span != ((long) itemUsages.size())) {
                                throw g("HID_BACK_ARRAY_RANGE");
                            }
                        }
                        int fieldCount = 0;
                        for (int k = 0; k < global.e; k++) {
                            int usage = itemUsages.get(Math.min(k, itemUsages.size() - 1)).intValue();
                            if (variable && !e(usage)) {
                                continue;
                            }
                            fieldCount++;
                            if (fieldCount > FIELD_MAX_PER_REPORT) {
                                throw g("HID_BACK_FIELD_LIMIT");
                            }
                            report.b.add(new HidKeyboardParser(report.a + (k * global.d), global.d,
                                    global.b, global.c, List.copyOf(itemUsages), variable, nullState));
                        }
                    }
                    report.a = (int) totalBits;
                } else if (itemTag != 9 && itemTag != 11) {
                    throw g("HID_BACK_MAIN");
                }
                usages.clear();
                usageMin = null;
                usageMax = null;
            } else {
                throw g("HID_BACK_RESERVED");
            }
            offset = nextOffset;
        }

        if (depth != 0 || !globalStack.isEmpty()) {
            throw g("HID_BACK_LAYOUT");
        }
        if (hasReportId && reports.containsKey(0)) {
            throw g("HID_BACK_LAYOUT");
        }
        io.carplay.app.secured.HidBackParser parser = new io.carplay.app.secured.HidBackParser(reports, hasReportId);
        if (parser.c != 0) {
            return parser;
        }
        throw g("HID_BACK_NOT_DECLARED");
    }

    private static int k(long j, int i, int i2) throws IOException {
        if (i == 4) {
            return (int) j;
        }
        if (j > 65535) {
            throw g("HID_BACK_USAGE");
        }
        return ((int) j) | (i2 << 16);
    }

    public d f(byte[] bArr) throws IOException {
        if (bArr == null || bArr.length == 0 || bArr.length > 1025) {
            throw g("HID_BACK_REPORT_LENGTH");
        }
        boolean z = this.b;
        boolean zG = false;
        int i = z ? bArr[0] & 255 : 0;
        c cVar = this.a.get(Integer.valueOf(i));
        if (cVar == null) {
            throw g("HID_BACK_UNKNOWN_REPORT");
        }
        if (bArr.length != ((cVar.a + 7) / 8) + (z ? 1 : 0)) {
            throw g("HID_BACK_REPORT_LENGTH");
        }
        if (cVar.b.isEmpty()) {
            return null;
        }
        Iterator<HidKeyboardParser> it = cVar.b.iterator();
        while (it.hasNext()) {
            zG |= it.next().g(bArr, z ? 1 : 0);
        }
        return new d(i, zG);
    }

    public int j() {
        return this.c;
    }

    /**
     * HidBackParser 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in HidBackParser, carrying local implementation detail.
     */
    public static final class b {
        int a;
        int b;
        int c;
        int d;
        int e;
        int f;
        boolean g;
        boolean h;

        public  b(int i) {
            this();
        }

        public b a() {
            b bVar = new b();
            bVar.a = this.a;
            bVar.b = this.b;
            bVar.c = this.c;
            bVar.d = this.d;
            bVar.e = this.e;
            bVar.f = this.f;
            bVar.g = this.g;
            bVar.h = this.h;
            return bVar;
        }

        private b() {
        }
    }

    /**
     * HidBackParser 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in HidBackParser, carrying local implementation detail.
     */
    public static final class c {
        int a;
        final List<HidKeyboardParser> b;

        private c() {
            this.b = new ArrayList();
        }

        public  c(int i) {
            this();
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class HidKeyboardParser extends ProtoMessageBase {
        private final int a;
        private final int b;
        private final int c;
        private final int d;
        private final List<Integer> e;
        private final boolean f;
        private final boolean g;

        private HidKeyboardParser(int i, int i2, int i3, int i4, List<Integer> list, boolean z, boolean z2) {
            this.a = i;
            this.b = i2;
            this.c = i3;
            this.d = i4;
            this.e = list;
            this.f = z;
            this.g = z2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof HidKeyboardParser)) {
                return false;
            }
            HidKeyboardParser aVar = (HidKeyboardParser) obj;
            return this.f == aVar.f && this.g == aVar.g && this.a == aVar.a && this.b == aVar.b && this.c == aVar.c && this.d == aVar.d && Objects.equals(this.e, aVar.e);
        }

        public boolean g(byte[] bArr, int i) throws IOException {
            int i2 = 0;
            for (int i3 = 0; i3 < this.b; i3++) {
                int i4 = this.a + i3;
                i2 |= ((bArr[(i4 / 8) + i] >>> (i4 % 8)) & 1) << i3;
            }
            int i5 = this.c;
            if (i2 >= i5 && i2 <= this.d) {
                if (!this.f) {
                    int i6 = i2 - i5;
                    if (i6 >= this.e.size()) {
                        throw HidBackParser.g("HID_BACK_ARRAY_INDEX");
                    }
                    int iIntValue = this.e.get(i6).intValue();
                    if (iIntValue < 458753 || iIntValue > 458755) {
                        return HidBackParser.e(iIntValue);
                    }
                    throw HidBackParser.g("HID_BACK_KEYBOARD_ROLLOVER");
                }
                if (i2 != 0) {
                    return true;
                }
            } else if (!this.g) {
                throw HidBackParser.g("HID_BACK_VALUE_RANGE");
            }
            return false;
        }

        public int h() {
            return this.d;
        }

        public final int hashCode() {
            boolean z = this.f;
            boolean z2 = this.g;
            int i = this.a;
            int i2 = this.b;
            int i3 = this.c;
            int i4 = this.d;
            List<Integer> list = this.e;
            return Objects.hashCode(list) + ((((((((((Boolean.hashCode(z2) + (Boolean.hashCode(z) * 31)) * 31) + i) * 31) + i2) * 31) + i3) * 31) + i4) * 31);
        }

        public int i() {
            return this.c;
        }

        public boolean j() {
            return this.g;
        }

        public int k() {
            return this.a;
        }

        public int l() {
            return this.b;
        }

        public List<Integer> m() {
            return this.e;
        }

        public boolean n() {
            return this.f;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c), Integer.valueOf(this.d), this.e, Boolean.valueOf(this.f), Boolean.valueOf(this.g)};
            String[] strArrSplit = "a;b;c;d;e;f;g".length() == 0 ? new String[0] : "a;b;c;d;e;f;g".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(HidKeyboardParser.class, sb, "[");
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

        public  HidKeyboardParser(int i, int i2, int i3, int i4, List list, boolean z, boolean z2, int i5) {
            this(i, i2, i3, i4, list, z, z2);
        }
    }
}
