/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 HidDescriptorParser。HID 报告描述符解析：识别 Apple 多点触控集合，按物理集合分配 slot 并提取 X/Y、Tip Switch、Contact ID 等字段。
 *   This file defines HidDescriptorParser. HID report-descriptor parser: detects Apple multitouch collections, assigns one slot per physical collection and extracts X/Y, Tip Switch and Contact ID fields.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;



/**
 * HID 报告描述符解析：识别 Apple 多点触控集合，按物理集合分配 slot 并提取 X/Y、Tip Switch、Contact ID 等字段。
 *
 * HID report-descriptor parser: detects Apple multitouch collections, assigns one slot per physical collection and extracts X/Y, Tip Switch and Contact ID fields.
 */
public final class HidDescriptorParser {
    
    private static final int DESCRIPTOR_MAX_BYTES = 4096;
    
    private static final int REPORT_MAX_BYTES = 1024;
    
    private static final int USAGE_MAX_COUNT = 256;
    
    private static final int CONTACT_SLOT_MAX = 10;
    
    private static final int COLLECTION_STACK_MAX = 16;
    
    private static final int GLOBAL_PUSH_STACK_MAX = 8;
    
    private static final int REPORT_ID_MAX_COUNT = 16;
    
    private static final int FIELD_COUNT_MAX = 256;
    
    private static final int REPORT_BITS_MAX = 8192;
    private final Map<Integer, d> a;
    private final boolean b;
    private final Set<Integer> c;

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class f extends ProtoMessageBase {
        private final int a;
        private final int b;
        private final int c;
        private final int d;
        private final int e;
        private final int f;
        private final int g;
        private final int h;
        private final boolean i;

        public f(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, boolean z) {
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
            if (!(obj instanceof f)) {
                return false;
            }
            f fVar = (f) obj;
            return this.i == fVar.i && this.a == fVar.a && this.b == fVar.b && this.c == fVar.c && this.d == fVar.d && this.e == fVar.e && this.f == fVar.f && this.g == fVar.g && this.h == fVar.h;
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

        public int p() {
            return this.b;
        }

        public boolean q() {
            return this.i;
        }

        public int r() {
            return this.f;
        }

        public int s() {
            return this.h;
        }

        public int t() {
            return this.e;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c), Integer.valueOf(this.d), Integer.valueOf(this.e), Integer.valueOf(this.f), Integer.valueOf(this.g), Integer.valueOf(this.h), Boolean.valueOf(this.i)};
            String[] strArrSplit = "a;b;c;d;e;f;g;h;i".length() == 0 ? new String[0] : "a;b;c;d;e;f;g;h;i".split(";");
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

        public int u() {
            return this.g;
        }

        
        public double v() {
            long numerator = ((long) this.c) - ((long) this.e);
            long denominator = ((long) this.f) - ((long) this.e);
            return ((double) numerator) / ((double) denominator);
        }

        
        public double w() {
            long numerator = ((long) this.d) - ((long) this.g);
            long denominator = ((long) this.h) - ((long) this.g);
            return ((double) numerator) / ((double) denominator);
        }

        public int x() {
            return this.c;
        }

        public int y() {
            return this.d;
        }

        public int z() {
            return this.a;
        }
    }

    private HidDescriptorParser(Map<Integer, d> map, boolean z) {
        this.a = Map.copyOf(map);
        this.b = z;
        HashSet hashSet = new HashSet();
        for (Map.Entry<Integer, d> entry : map.entrySet()) {
            if (!entry.getValue().c.isEmpty()) {
                hashSet.add(entry.getKey());
            }
        }
        this.c = Set.copyOf(hashSet);
    }

    
    public static IOException c(String str) {
        return new IOException(str);
    }

    private static boolean d(int i, int i2, int i3) {
        
        
        long lowerBound = i < 0 ? -(1L << (i3 - 1)) : 0L;
        if (i < 0) {
            i3--;
        }
        long upperBound = (1L << i3) - 1L;
        return ((long) i) >= lowerBound && ((long) i2) <= upperBound;
    }

    
    
    
    
    
    
    
    
    
    
    
    public static io.carplay.app.secured.HidDescriptorParser e(byte[] descriptor) throws IOException {
        if (descriptor == null || descriptor.length == 0 || descriptor.length > DESCRIPTOR_MAX_BYTES) {
            throw c("HID_DESCRIPTOR_LIMIT");
        }
        LinkedHashMap<Integer, d> reports = new LinkedHashMap<>();
        c global = new c(0);
        ArrayDeque<c> globalStack = new ArrayDeque<>();
        ArrayDeque<CollectionState> collectionStack = new ArrayDeque<>();
        ArrayList<Integer> usages = new ArrayList<>();
        boolean hasReportId = false;
        Integer usageMin = null;
        Integer usageMax = null;
        int dataFieldCount = 0;
        int touchApplications = 0;
        int touchSlot = 0;
        int offset = 0;

        while (offset < descriptor.length) {
            int prefix = descriptor[offset] & 255;
            if (prefix == 254) {
                throw c("HID_LONG_ITEM_UNSUPPORTED");
            }
            int dataSize = (prefix & 3) == 3 ? 4 : (prefix & 3);
            int itemType = (prefix >> 2) & 3;
            int itemTag = prefix >> 4;
            int dataOffset = offset + 1;
            if (dataOffset + dataSize > descriptor.length) {
                throw c("HID_TRUNCATED_ITEM");
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
                            throw c("HID_USAGE_PAGE_RANGE");
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
                            throw c("HID_LOGICAL_RANGE");
                        }
                        global.c = (int) logicalMax;
                        global.h = true;
                        break;
                    case 7:
                        if (rawValue > 32) {
                            throw c("HID_FIELD_SIZE_LIMIT");
                        }
                        global.d = (int) rawValue;
                        break;
                    case 8:
                        if (rawValue < 1 || rawValue > 255) {
                            throw c("HID_REPORT_ID_RANGE");
                        }
                        global.f = (int) rawValue;
                        hasReportId = true;
                        break;
                    case 9:
                        if (rawValue > REPORT_BITS_MAX) {
                            throw c("HID_REPORT_COUNT_LIMIT");
                        }
                        global.e = (int) rawValue;
                        break;
                    case 10:
                        if (dataSize != 0 || globalStack.size() >= GLOBAL_PUSH_STACK_MAX) {
                            throw c("HID_GLOBAL_STACK_LIMIT");
                        }
                        globalStack.push(global.a());
                        break;
                    case 11:
                        if (dataSize != 0 || globalStack.isEmpty()) {
                            throw c("HID_GLOBAL_STACK_INVALID");
                        }
                        global = globalStack.pop();
                        break;
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        
                        break;
                    default:
                        throw c("HID_GLOBAL_ITEM_UNSUPPORTED");
                }
            } else if (itemType == 2) {
                
                if (itemTag == 0) {
                    if (usages.size() >= USAGE_MAX_COUNT) {
                        throw c("HID_USAGE_LIMIT");
                    }
                    usages.add(g(rawValue, dataSize, global.a));
                } else if (itemTag == 1) {
                    usageMin = g(rawValue, dataSize, global.a);
                } else if (itemTag == 2) {
                    usageMax = g(rawValue, dataSize, global.a);
                } else if (itemTag < 3 || itemTag > 9) {
                    throw c("HID_LOCAL_ITEM_UNSUPPORTED");
                }
            } else if (itemType == 0) {
                
                ArrayList<Integer> itemUsages = new ArrayList<>(usages);
                if (usageMin == null) {
                    if (usageMax != null) {
                        throw c("HID_USAGE_RANGE");
                    }
                } else {
                    if (usageMax == null) {
                        throw c("HID_USAGE_RANGE");
                    }
                    int min = usageMin.intValue();
                    int max = usageMax.intValue();
                    if (min > max) {
                        throw c("HID_USAGE_RANGE");
                    }
                    long span = ((long) max) - ((long) min) + 1L;
                    if (span > ((long) (USAGE_MAX_COUNT - itemUsages.size()))) {
                        throw c("HID_USAGE_RANGE");
                    }
                    for (int usage = min; usage <= max; usage++) {
                        itemUsages.add(Integer.valueOf(usage));
                    }
                }

                if (itemTag == 10) {
                    
                    if (dataSize != 1 || rawValue > 2) {
                        throw c("HID_COLLECTION_LIMIT");
                    }
                    if (collectionStack.size() >= COLLECTION_STACK_MAX) {
                        throw c("HID_COLLECTION_LIMIT");
                    }
                    
                CollectionState parent = collectionStack.peek();
                int usage = itemUsages.isEmpty() ? 0 : itemUsages.get(0).intValue();
                boolean touchApplication = usage == 0x0D0004 && rawValue == 1;
                if (touchApplication) {
                    if (touchApplications + 1 > 1) {
                        throw c("HID_MULTIPLE_TOUCH_APPLICATIONS");
                    }
                    touchApplications++;
                }
                boolean touchCollection = touchApplication || (parent != null && CollectionState.h(parent));
                int slot;
                if (touchApplication || parent == null) {
                    slot = 1;
                } else {
                    slot = CollectionState.g(parent);
                }
                if (touchCollection && usage == 0x0D0022) {
                    slot = ++touchSlot;
                }
                    collectionStack.push(new CollectionState(touchCollection, slot));
                } else if (itemTag == 12) {
                    
                    if (dataSize != 0 || collectionStack.isEmpty()) {
                        throw c("HID_COLLECTION_INVALID");
                    }
                    collectionStack.pop();
                } else if (itemTag == 8) {
                    
                    if (global.d < 1 || global.e < 1
                            || ((long) global.d) * ((long) global.e) > REPORT_BITS_MAX) {
                        throw c("HID_REPORT_LIMIT");
                    }
                    Integer reportId = Integer.valueOf(global.f);
                    d report = reports.get(reportId);
                    if (report == null) {
                        if (reports.size() >= REPORT_ID_MAX_COUNT) {
                            throw c("HID_REPORT_ID_LIMIT");
                        }
                        report = new d(0);
                        reports.put(reportId, report);
                    }
                    long totalBits = ((long) report.a) + ((long) global.d) * ((long) global.e);
                    if (totalBits > REPORT_BITS_MAX) {
                        throw c("HID_REPORT_LIMIT");
                    }
                    CollectionState collection = collectionStack.peek();
                    
                    if ((rawValue & 1) == 0) {
                        if (((long) dataFieldCount) + ((long) global.e) > FIELD_COUNT_MAX) {
                            throw c("HID_FIELD_LIMIT");
                        }
                        dataFieldCount += global.e;
                        if (collection != null && CollectionState.h(collection)) {
                            if ((rawValue & 2) == 0) {
                                throw c("HID_NONABSOLUTE_VARIABLE_INPUT");
                            }
                            
                            if ((rawValue & (-99L)) != 0) {
                                throw c("HID_NONABSOLUTE_VARIABLE_INPUT");
                            }
                            if (itemUsages.isEmpty()) {
                                throw c("HID_MISSING_USAGE");
                            }
                            for (int k = 0; k < global.e; k++) {
                                int fieldUsage = itemUsages.get(Math.min(k, itemUsages.size() - 1)).intValue();
                                b field = new b(report.a + (k * global.d), global.d, global.b, global.c, 0);
                                if (fieldUsage == 0x00010030 || fieldUsage == 0x00010031) {
                                    
                                    if (!(global.g && global.h && global.b < global.c
                                            && d(global.b, global.c, global.d))) {
                                        throw c("HID_LOGICAL_RANGE");
                                    }
                                } else if (fieldUsage == 0x0D0042 || fieldUsage == 0x0D0033
                                        || fieldUsage == 0x0D0047 || fieldUsage == 0x0D0032) {
                                    
                                    if (global.d != 1) {
                                        throw c("HID_BOOLEAN_FIELD_SIZE");
                                    }
                                } else if (fieldUsage == 0x0D0051 || fieldUsage == 0x0D0038
                                        || fieldUsage == 0x0D0054) {
                                    
                                    if (global.d > 16 || global.b < 0) {
                                        throw c("HID_CONTACT_FIELD_RANGE");
                                    }
                                }
                                if (fieldUsage == 0x0D0054) {
                                    if (report.b != null) {
                                        throw c("HID_AMBIGUOUS_CONTACT_COUNT");
                                    }
                                    report.b = field;
                                } else {
                                    report.a(CollectionState.g(collection)).a(fieldUsage, field);
                                }
                            }
                        }
                    }
                    report.a = (int) totalBits;
                } else if (itemTag != 9 && itemTag != 11) {
                    
                    throw c("HID_MAIN_ITEM_UNSUPPORTED");
                }
                usages.clear();
                usageMin = null;
                usageMax = null;
            } else {
                throw c("HID_RESERVED_ITEM");
            }
            offset = nextOffset;
        }

        if (!globalStack.isEmpty() || !collectionStack.isEmpty()) {
            throw c("HID_UNBALANCED_STACK");
        }
        if (hasReportId && reports.containsKey(0)) {
            throw c("HID_MIXED_REPORT_IDS");
        }
        int supported = 0;
        for (d report : reports.values()) {
            long reportBytes = (((long) report.a) + 7L) / 8L + (hasReportId ? 1L : 0L);
            if (reportBytes > REPORT_MAX_BYTES) {
                throw c("HID_REPORT_LIMIT");
            }
            for (HidTouchParser parser : report.c.values()) {
                parser.b();
                supported++;
            }
        }
        if (supported == 0) {
            throw c("HID_NO_SUPPORTED_TOUCH");
        }
        return new io.carplay.app.secured.HidDescriptorParser(reports, hasReportId);
    }

    private static int g(long j, int i, int i2) throws IOException {
        if (i == 0) {
            throw c("HID_USAGE_RANGE");
        }
        if (i == 4) {
            return (int) j;
        }
        if (j > 65535) {
            throw c("HID_USAGE_RANGE");
        }
        return ((int) j) | (i2 << 16);
    }

    
    public f b(byte[] bArr, f fVar) throws IOException {
        b bVar;
        if (bArr == null || bArr.length == 0 || bArr.length > REPORT_MAX_BYTES) {
            throw c("HID_REPORT_LIMIT");
        }
        boolean z = this.b;
        int i = 0;
        int i2 = z ? bArr[0] & 255 : 0;
        d dVar = this.a.get(Integer.valueOf(i2));
        if (dVar == null) {
            throw c("HID_UNKNOWN_REPORT_ID");
        }
        if (dVar.c.isEmpty()) {
            return null;
        }
        if (bArr.length != ((dVar.a + 7) / 8) + (z ? 1 : 0)) {
            throw c("HID_REPORT_LENGTH");
        }
        int iN = 0;
        int i3 = 0;
        HidTouchParser eVar = null;
        for (HidTouchParser eVar2 : dVar.c.values()) {
            if (eVar2.c.n(bArr, z ? 1 : 0) != 0) {
                i++;
                if (i > 1) {
                    throw c("HID_MULTITOUCH_UNSUPPORTED");
                }
                b bVar2 = eVar2.e;
                if ((bVar2 != null && bVar2.n(bArr, z ? 1 : 0) == 0) || ((bVar = eVar2.f) != null && bVar.n(bArr, z ? 1 : 0) == 0)) {
                    throw c("HID_INVALID_CONTACT");
                }
                b bVar3 = eVar2.d;
                iN = bVar3 == null ? i3 : bVar3.n(bArr, z ? 1 : 0);
                eVar = eVar2;
            }
            i3++;
        }
        b bVar4 = dVar.b;
        if (bVar4 != null && bVar4.n(bArr, z ? 1 : 0) != i) {
            throw c("HID_CONTACT_COUNT_MISMATCH");
        }
        if (i == 0) {
            if (fVar != null && fVar.i && fVar.a == i2) {
                return new f(i2, fVar.b, fVar.c, fVar.d, fVar.e, fVar.f, fVar.g, fVar.h, false);
            }
            return null;
        }
        if (fVar == null || !fVar.i || (fVar.a == i2 && fVar.b == iN)) {
            return new f(i2, iN, eVar.a.j(bArr, z ? 1 : 0), eVar.b.j(bArr, z ? 1 : 0), eVar.a.c, eVar.a.d, eVar.b.c, eVar.b.d, true);
        }
        throw c("HID_CONTACT_CHANGED_WITHOUT_UP");
    }

    public Set<Integer> f() {
        return this.c;
    }

    /**
     * HidDescriptorParser 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in HidDescriptorParser, carrying local implementation detail.
     */
    public static final class c {
        int a;
        int b;
        int c;
        int d;
        int e;
        int f;
        boolean g;
        boolean h;

        public  c(int i) {
            this();
        }

        public c a() {
            c cVar = new c();
            cVar.a = this.a;
            cVar.b = this.b;
            cVar.c = this.c;
            cVar.d = this.d;
            cVar.e = this.e;
            cVar.f = this.f;
            cVar.g = this.g;
            cVar.h = this.h;
            return cVar;
        }

        private c() {
        }
    }

    /**
     * HidDescriptorParser 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in HidDescriptorParser, carrying local implementation detail.
     */
    public static final class HidTouchParser {
        b a;
        b b;
        b c;
        b d;
        b e;
        b f;

        public  HidTouchParser(int i) {
            this();
        }

        public void a(int i, b bVar) throws IOException {
            switch (i) {
                case 65584:
                    if (this.a != null) {
                        throw HidDescriptorParser.c("HID_AMBIGUOUS_AXIS");
                    }
                    this.a = bVar;
                    return;
                case 65585:
                    if (this.b != null) {
                        throw HidDescriptorParser.c("HID_AMBIGUOUS_AXIS");
                    }
                    this.b = bVar;
                    return;
                case 852016:
                case 852040:
                case 852041:
                    return;
                case 852018:
                    if (this.f != null) {
                        throw HidDescriptorParser.c("HID_AMBIGUOUS_IN_RANGE");
                    }
                    this.f = bVar;
                    return;
                case 852019:
                case 852034:
                    if (this.c != null) {
                        throw HidDescriptorParser.c("HID_AMBIGUOUS_TIP");
                    }
                    this.c = bVar;
                    return;
                case 852024:
                case 852049:
                    if (this.d != null) {
                        throw HidDescriptorParser.c("HID_AMBIGUOUS_CONTACT_ID");
                    }
                    this.d = bVar;
                    return;
                case 852039:
                    if (this.e != null) {
                        throw HidDescriptorParser.c("HID_AMBIGUOUS_CONFIDENCE");
                    }
                    this.e = bVar;
                    return;
                default:
                    throw HidDescriptorParser.c("HID_UNSUPPORTED_TOUCH_USAGE");
            }
        }

        public void b() throws IOException {
            if (this.a == null || this.b == null || this.c == null) {
                throw HidDescriptorParser.c("HID_INCOMPLETE_CONTACT");
            }
        }

        private HidTouchParser() {
        }
    }

    
    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class CollectionState extends ProtoMessageBase {
        private final boolean touch;
        private final int slot;

        private CollectionState(boolean touch, int slot) {
            this.touch = touch;
            this.slot = slot;
        }

        public static   int g(CollectionState state) {
            return state.slot;
        }

        public static   boolean h(CollectionState state) {
            return state.touch;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof CollectionState)) {
                return false;
            }
            CollectionState state = (CollectionState) obj;
            return this.touch == state.touch && this.slot == state.slot;
        }

        public final int hashCode() {
            boolean z = this.touch;
            return (Boolean.hashCode(z) * 31) + this.slot;
        }

        public int i() {
            return this.slot;
        }

        public boolean j() {
            return this.touch;
        }

        public final String toString() {
            Object[] objArr = {Boolean.valueOf(this.touch), Integer.valueOf(this.slot)};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(CollectionState.class, sb, "[");
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

        public  CollectionState(int i, int i2, boolean z) {
            this(z, i);
        }
    }

    /**
     * HidDescriptorParser 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in HidDescriptorParser, carrying local implementation detail.
     */
    public static final class d {
        int a;
        b b;
        final Map<Integer, HidTouchParser> c;

        private d() {
            this.c = new LinkedHashMap();
        }

        public HidTouchParser a(int i) throws IOException {
            HidTouchParser eVar = this.c.get(Integer.valueOf(i));
            if (eVar != null) {
                return eVar;
            }
            if (this.c.size() >= CONTACT_SLOT_MAX) {
                throw HidDescriptorParser.c("HID_SLOT_LIMIT");
            }
            HidTouchParser eVar2 = new HidTouchParser(0);
            this.c.put(Integer.valueOf(i), eVar2);
            return eVar2;
        }

        public  d(int i) {
            this();
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
        private final int d;

        private b(int i, int i2, int i3, int i4) {
            this.a = i;
            this.b = i2;
            this.c = i3;
            this.d = i4;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && this.b == bVar.b && this.c == bVar.c && this.d == bVar.d;
        }

        public final int hashCode() {
            int i = this.a;
            int i2 = this.b;
            return (((((i * 31) + i2) * 31) + this.c) * 31) + this.d;
        }

        public int i() {
            return this.b;
        }

        public int j(byte[] bArr, int i) throws IOException {
            int iN = n(bArr, i);
            if (iN < this.c || iN > this.d) {
                throw HidDescriptorParser.c("HID_COORDINATE_RANGE");
            }
            return iN;
        }

        public int k() {
            return this.d;
        }

        public int l() {
            return this.c;
        }

        public int m() {
            return this.a;
        }

        public int n(byte[] bArr, int i) throws IOException {
            int i2;
            int i3 = 0;
            long j = 0;
            while (true) {
                i2 = this.b;
                if (i3 >= i2) {
                    break;
                }
                int i4 = this.a + i3;
                j |= ((long) ((bArr[(i4 / 8) + i] >>> (i4 % 8)) & 1)) << i3;
                i3++;
            }
            if (this.c < 0 && ((1 << (i2 - 1)) & j) != 0) {
                j -= 1 << i2;
            }
            if (j < -2147483648L || j > 2147483647L) {
                throw HidDescriptorParser.c("HID_VALUE_RANGE");
            }
            return (int) j;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c), Integer.valueOf(this.d)};
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

        public  b(int i, int i2, int i3, int i4, int i5) {
            this(i, i2, i3, i4);
        }
    }
}
