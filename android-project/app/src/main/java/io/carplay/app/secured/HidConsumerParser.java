/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 HidConsumerParser。HID Consumer 页解析：处理多媒体按键描述符（播放、音量等）。
 *   This file defines HidConsumerParser. HID Consumer-page parser for multimedia key descriptors (playback, volume, …).
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;



/**
 * HID Consumer 页解析：处理多媒体按键描述符（播放、音量等）。
 *
 * HID Consumer-page parser for multimedia key descriptors (playback, volume, …).
 */
public final class HidConsumerParser {
    
    private static final int DESCRIPTOR_MAX_BYTES = 4096;
    
    private static final int GLOBAL_PUSH_STACK_MAX = 8;
    
    private static final int USAGE_MAX_COUNT = 256;
    
    private static final int COLLECTION_DEPTH_MAX = 16;
    
    private static final int REPORT_BITS_MAX = 8192;
    
    private static final int REPORT_ID_MAX_COUNT = 16;
    
    private static final int FIELD_MAX_TOTAL = 128;

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
        private final Set<Integer> b;

        public d(int i, Set<Integer> set) {
            Set<Integer> setCopyOf = Set.copyOf(set);
            this.a = i;
            this.b = setCopyOf;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof d)) {
                return false;
            }
            d dVar = (d) obj;
            return this.a == dVar.a && Objects.equals(this.b, dVar.b);
        }

        public int g() {
            return this.a;
        }

        public Set<Integer> h() {
            return this.b;
        }

        public final int hashCode() {
            int i = this.a;
            return Objects.hashCode(this.b) + (i * 31);
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), this.b};
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

    private HidConsumerParser(Map<Integer, c> map, boolean z) {
        this.a = Map.copyOf(map);
        this.b = z;
        this.c = (int) map.values().stream().filter(new w0(1)).count();
    }

    public static   IOException c() {
        return f("HID_CONSUMER_VALUE_RANGE");
    }

    private static IOException f(String str) {
        return new IOException(str);
    }

    
    public static  boolean g(c cVar) {
        return !cVar.b.isEmpty();
    }

    
    public static boolean h(int i) {
        if (i == 786620 || i == 786637) {
            return true;
        }
        switch (i) {
            case 786608:
            case 786609:
                return true;
            default:
                switch (i) {
                    case 786611:
                    case 786612:
                    case 786613:
                    case 786614:
                    case 786615:
                    case 786616:
                    case 786617:
                        return true;
                    default:
                        return false;
                }
        }
    }

    public static int i(int i) {
        switch (i) {
            case 786608:
                return 31;
            case 786609:
                return 32;
            case 786610:
            case 786611:
            case 786612:
            default:
                return 0;
            case 786613:
                return 16;
            case 786614:
                return 15;
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static io.carplay.app.secured.HidConsumerParser j(byte[] descriptor) throws IOException {
        if (descriptor == null || descriptor.length == 0 || descriptor.length > DESCRIPTOR_MAX_BYTES) {
            throw f("HID_CONSUMER_DESCRIPTOR_LIMIT");
        }
        b global = new b(0);
        ArrayDeque<b> globalStack = new ArrayDeque<>();
        
        ArrayDeque<Boolean> collectionStack = new ArrayDeque<>();
        ArrayList<Integer> usages = new ArrayList<>();
        LinkedHashMap<Integer, c> reports = new LinkedHashMap<>();
        boolean hasReportId = false;
        Integer usageMin = null;
        Integer usageMax = null;
        int offset = 0;
        
        int fieldCount = 0;

        while (offset < descriptor.length) {
            int prefix = descriptor[offset] & 255;
            offset++;
            if (prefix == 0) {
                continue;
            }
            if (prefix == 254) {
                throw f("HID_CONSUMER_LONG_ITEM");
            }
            int dataSize = (prefix & 3) == 3 ? 4 : (prefix & 3);
            int itemType = (prefix >> 2) & 3;
            int itemTag = prefix >> 4;
            int dataOffset = offset;
            if (dataOffset + dataSize > descriptor.length) {
                throw f("HID_CONSUMER_TRUNCATED");
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
                            throw f("HID_CONSUMER_PAGE");
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
                            throw f("HID_CONSUMER_RANGE");
                        }
                        global.c = (int) logicalMax;
                        global.h = true;
                        break;
                    case 7:
                        if (rawValue > 32) {
                            throw f("HID_CONSUMER_SIZE");
                        }
                        global.d = (int) rawValue;
                        break;
                    case 8:
                        if (rawValue < 1 || rawValue > 255) {
                            throw f("HID_CONSUMER_ID");
                        }
                        global.f = (int) rawValue;
                        hasReportId = true;
                        break;
                    case 9:
                        if (rawValue > 512) {
                            throw f("HID_CONSUMER_COUNT");
                        }
                        global.e = (int) rawValue;
                        break;
                    case 10:
                        if (dataSize != 0 || globalStack.size() >= GLOBAL_PUSH_STACK_MAX) {
                            throw f("HID_CONSUMER_STACK");
                        }
                        globalStack.push(global.a());
                        break;
                    case 11:
                        if (dataSize != 0 || globalStack.isEmpty()) {
                            throw f("HID_CONSUMER_STACK");
                        }
                        global = globalStack.pop();
                        break;
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        break;
                    default:
                        throw f("HID_CONSUMER_GLOBAL");
                }
            } else if (itemType == 2) {
                if (itemTag == 0) {
                    if (usages.size() >= USAGE_MAX_COUNT) {
                        throw f("HID_CONSUMER_USAGE_LIMIT");
                    }
                    usages.add(l(rawValue, dataSize, global.a));
                } else if (itemTag == 1) {
                    usageMin = l(rawValue, dataSize, global.a);
                } else if (itemTag == 2) {
                    usageMax = l(rawValue, dataSize, global.a);
                } else if (itemTag < 3 || itemTag > 9) {
                    throw f("HID_CONSUMER_LOCAL");
                }
            } else if (itemType == 0) {
                ArrayList<Integer> itemUsages = new ArrayList<>(usages);
                if (usageMin == null) {
                    if (usageMax != null) {
                        throw f("HID_CONSUMER_USAGE_RANGE");
                    }
                } else {
                    if (usageMax == null) {
                        throw f("HID_CONSUMER_USAGE_RANGE");
                    }
                    int min = usageMin.intValue();
                    int max = usageMax.intValue();
                    if (min > max) {
                        throw f("HID_CONSUMER_USAGE_RANGE");
                    }
                    
                    if ((min >>> 16) != (max >>> 16)) {
                        throw f("HID_CONSUMER_USAGE_RANGE");
                    }
                    long span = ((long) max) - ((long) min);
                    if (span > ((long) (255 - itemUsages.size()))) {
                        throw f("HID_CONSUMER_USAGE_RANGE");
                    }
                    for (int usage = min; usage <= max; usage++) {
                        itemUsages.add(Integer.valueOf(usage));
                    }
                }

                if (itemTag == 10) {
                    
                    if (dataSize != 1 || rawValue > 2) {
                        throw f("HID_CONSUMER_COLLECTION");
                    }
                    if (collectionStack.size() >= COLLECTION_DEPTH_MAX) {
                        throw f("HID_CONSUMER_COLLECTION");
                    }
                    
                    boolean consumerCollection = false;
                    if (!collectionStack.isEmpty() && collectionStack.peek().booleanValue()) {
                        consumerCollection = true;
                    } else if (rawValue == 1 && itemUsages.contains(Integer.valueOf(0xC0001))) {
                        consumerCollection = true;
                    }
                    collectionStack.push(Boolean.valueOf(consumerCollection));
                } else if (itemTag == 12) {
                    
                    if (dataSize != 0 || collectionStack.isEmpty()) {
                        throw f("HID_CONSUMER_COLLECTION");
                    }
                    collectionStack.pop();
                } else if (itemTag == 8) {
                    
                    if (collectionStack.isEmpty()) {
                        throw f("HID_CONSUMER_INPUT_LIMIT");
                    }
                    if (global.d < 1 || global.e < 1
                            || ((long) global.d) * ((long) global.e) > REPORT_BITS_MAX) {
                        throw f("HID_CONSUMER_INPUT_LIMIT");
                    }
                    Integer reportId = Integer.valueOf(global.f);
                    c report = reports.get(reportId);
                    if (report == null) {
                        if (reports.size() >= REPORT_ID_MAX_COUNT) {
                            throw f("HID_CONSUMER_REPORT_LIMIT");
                        }
                        report = new c(0);
                        reports.put(reportId, report);
                    }
                    long totalBits = ((long) report.a) + ((long) global.d) * ((long) global.e);
                    if (totalBits > REPORT_BITS_MAX) {
                        throw f("HID_CONSUMER_REPORT_LIMIT");
                    }
                    
                    boolean keepField = (rawValue & 1) == 0
                            && !collectionStack.isEmpty()
                            && collectionStack.peek().booleanValue()
                            && usages.stream().anyMatch(new w0(16));
                    if (keepField) {
                        boolean variable = (rawValue & 2) != 0;
                        
                        if ((rawValue & (-0x43)) != 0) {
                            throw f("HID_CONSUMER_INPUT_TYPE");
                        }
                        if (global.d > 16 || !global.g || !global.h) {
                            throw f("HID_CONSUMER_INPUT_TYPE");
                        }
                        if (global.b < 0 || global.c < global.b) {
                            throw f("HID_CONSUMER_INPUT_TYPE");
                        }
                        if (((long) global.c) >= (1L << global.d)) {
                            throw f("HID_CONSUMER_INPUT_TYPE");
                        }
                        if (variable) {
                            if (global.b != 0 || global.c != 1) {
                                throw f("HID_CONSUMER_BOOLEAN_RANGE");
                            }
                        } else {
                            long span = ((long) global.c) - ((long) global.b) + 1L;
                            if (span != ((long) itemUsages.size())) {
                                throw f("HID_CONSUMER_ARRAY_RANGE");
                            }
                        }
                        boolean nullState = (rawValue & 0x40) != 0;
                        for (int k = 0; k < global.e; k++) {
                            int usage = itemUsages.get(Math.min(k, itemUsages.size() - 1)).intValue();
                            if (variable && !h(usage)) {
                                continue;
                            }
                            fieldCount++;
                            if (fieldCount > FIELD_MAX_TOTAL) {
                                throw f("HID_CONSUMER_FIELD_LIMIT");
                            }
                            List<Integer> fieldUsages = variable
                                    ? List.of(Integer.valueOf(usage))
                                    : List.copyOf(itemUsages);
                            report.b.add(new a(report.a + (k * global.d), global.d,
                                    global.b, global.c, fieldUsages, variable, nullState, 0));
                        }
                    }
                    report.a = (int) totalBits;
                } else if (itemTag != 9 && itemTag != 11) {
                    
                    
                    throw f("HID_CONSUMER_MAIN");
                }
                usages.clear();
                usageMin = null;
                usageMax = null;
            } else {
                throw f("HID_CONSUMER_RESERVED");
            }
            offset = nextOffset;
        }

        if (!collectionStack.isEmpty() || !globalStack.isEmpty()) {
            throw f("HID_CONSUMER_LAYOUT");
        }
        
        if (hasReportId && !reports.containsKey(Integer.valueOf(0))) {
            throw f("HID_CONSUMER_LAYOUT");
        }
        HidConsumerParser parser = new HidConsumerParser(reports, hasReportId);
        if (parser.c == 0) {
            throw f("HID_CONSUMER_NOT_DECLARED");
        }
        return parser;
    }

    private static int l(long j, int i, int i2) throws IOException {
        if (i == 4) {
            return (int) j;
        }
        if (j > 65535) {
            throw f("HID_CONSUMER_USAGE");
        }
        return ((int) j) | (i2 << 16);
    }

    public d e(byte[] bArr) throws IOException {
        if (bArr == null || bArr.length == 0 || bArr.length > 1025) {
            throw f("HID_CONSUMER_REPORT_LENGTH");
        }
        boolean z = this.b;
        int i = z ? bArr[0] & 255 : 0;
        c cVar = this.a.get(Integer.valueOf(i));
        if (cVar == null) {
            throw f("HID_CONSUMER_UNKNOWN_REPORT");
        }
        if (bArr.length != ((cVar.a + 7) / 8) + (z ? 1 : 0)) {
            throw f("HID_CONSUMER_REPORT_LENGTH");
        }
        if (cVar.b.isEmpty()) {
            return null;
        }
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        Iterator<a> it = cVar.b.iterator();
        while (it.hasNext()) {
            it.next().g(bArr, z ? 1 : 0, linkedHashSet);
        }
        return new d(i, linkedHashSet);
    }

    public int k() {
        return this.c;
    }

    /**
     * HidConsumerParser 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in HidConsumerParser, carrying local implementation detail.
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
     * HidConsumerParser 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in HidConsumerParser, carrying local implementation detail.
     */
    public static final class c {
        int a;
        final List<a> b;

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
    public static final class a extends ProtoMessageBase {
        private final int a;
        private final int b;
        private final int c;
        private final int d;
        private final List<Integer> e;
        private final boolean f;
        private final boolean g;

        private a(int i, int i2, int i3, int i4, List<Integer> list, boolean z, boolean z2) {
            this.a = i;
            this.b = i2;
            this.c = i3;
            this.d = i4;
            this.e = list;
            this.f = z;
            this.g = z2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.f == aVar.f && this.g == aVar.g && this.a == aVar.a && this.b == aVar.b && this.c == aVar.c && this.d == aVar.d && Objects.equals(this.e, aVar.e);
        }

        public void g(byte[] bArr, int i, Set<Integer> set) throws IOException {
            int i2 = 0;
            for (int i3 = 0; i3 < this.b; i3++) {
                int i4 = this.a + i3;
                i2 |= ((bArr[(i4 / 8) + i] >>> (i4 % 8)) & 1) << i3;
            }
            int i5 = this.c;
            if (i2 < i5 || i2 > this.d) {
                if (!this.g) {
                    throw HidConsumerParser.c();
                }
                return;
            }
            boolean z = this.f;
            List<Integer> list = this.e;
            Integer num = z ? list.get(0) : list.get(i2 - i5);
            int iIntValue = num.intValue();
            if (!(this.f && i2 == 0) && HidConsumerParser.h(iIntValue)) {
                set.add(num);
            }
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

        public  a(int i, int i2, int i3, int i4, List list, boolean z, boolean z2, int i5) {
            this(i, i2, i3, i4, list, z, z2);
        }
    }
}
