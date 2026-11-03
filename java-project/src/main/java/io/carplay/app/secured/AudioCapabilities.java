package io.carplay.app.secured;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
/**
 * AudioCapabilities
 *
 * <p>原混淆名：{@code io.carplay.app.secured.s}（jadx 产物 s.java）
 *
 * <p>命名依据：audioformats / audiolatencies / audio_native_format_unsupported / audio_type_unsupported
 */
public final class AudioCapabilities {
    private static final int d = 128;
    private static final long e = 53244;
    private static final long f = 12582912;
    private static final Set<String> g = Set.of("default", "compatibility", "media", "alert", "telephony", "speechRecognition");
    private final List<b> a;
    private final List<c> b;
    private final boolean c;

    public static final class d extends ProtoMessageBase {
        private final int a;
        private final String b;
        private final long c;
        private final int d;
        private final int e;
        private final int f;
        private final int g;
        private final int h;
        private final boolean i;

        public d(int i, String str, long j, int i2, int i3, int i4, int i5, int i6, boolean z) {
            this.a = i;
            this.b = str;
            this.c = j;
            this.d = i2;
            this.e = i3;
            this.f = i4;
            this.g = i5;
            this.h = i6;
            this.i = z;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof d)) {
                return false;
            }
            d dVar = (d) obj;
            return this.i == dVar.i && this.a == dVar.a && this.d == dVar.d && this.e == dVar.e && this.f == dVar.f && this.g == dVar.g && this.h == dVar.h && this.c == dVar.c && Objects.equals(this.b, dVar.b);
        }

        public boolean g() {
            return (this.c & AudioCapabilities.f) != 0;
        }

        public long h() {
            return this.c;
        }

        public final int hashCode() {
            boolean z = this.i;
            int i = this.a;
            int i2 = this.d;
            int i3 = this.e;
            int i4 = this.f;
            int i5 = this.g;
            int i6 = this.h;
            long j = this.c;
            String str = this.b;
            return Objects.hashCode(str) + ProtoMessageBase.a(j, ((((((((((((Boolean.hashCode(z) * 31) + i) * 31) + i2) * 31) + i3) * 31) + i4) * 31) + i5) * 31) + i6) * 31, 31);
        }

        public int i() {
            return this.h;
        }

        public String j() {
            return this.b;
        }

        public int k() {
            return this.f;
        }

        public int l() {
            return this.e;
        }

        public boolean m() {
            return this.i;
        }

        public int n() {
            return this.d;
        }

        public int o() {
            return this.g;
        }

        public int p() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), this.b, Long.valueOf(this.c), Integer.valueOf(this.d), Integer.valueOf(this.e), Integer.valueOf(this.f), Integer.valueOf(this.g), Integer.valueOf(this.h), Boolean.valueOf(this.i)};
            String[] strArrSplit = "a;b;c;d;e;f;g;h;i".length() == 0 ? new String[0] : "a;b;c;d;e;f;g;h;i".split(";");
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

    private AudioCapabilities(List<b> list, List<c> list2, boolean z) {
        this.a = List.copyOf(list);
        this.b = List.copyOf(list2);
        this.c = z;
    }

    private static String e(Object obj) {
        if (!(obj instanceof String)) {
            return null;
        }
        String str = (String) obj;
        if (g.contains(str)) {
            return str;
        }
        return null;
    }

    private static IOException g(String str) {
        return new IOException(str);
    }

    private static Integer j(Object obj) {
        if (!(obj instanceof Byte) && !(obj instanceof Short) && !(obj instanceof Integer) && !(obj instanceof Long)) {
            return null;
        }
        long jLongValue = ((Number) obj).longValue();
        if (jLongValue < -2147483648L || jLongValue > 2147483647L) {
            return null;
        }
        return Integer.valueOf((int) jLongValue);
    }

    private static boolean k(Map<?, ?> map, String str, Integer num) {
        return map.get(str) != null && num == null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ int m(a aVar) {
        return aVar.a.p();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ String n(a aVar) {
        return aVar.a.j();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ long o(a aVar) {
        return aVar.a.h();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean p(d dVar) {
        return dVar.p() == 100;
    }

    private static Long q(Object obj) {
        Long lR = r(obj);
        if (lR != null) {
            return lR;
        }
        if (!(obj instanceof BigInteger)) {
            return null;
        }
        BigInteger bigInteger = (BigInteger) obj;
        if (bigInteger.signum() < 0 || bigInteger.bitLength() > 64) {
            return null;
        }
        return Long.valueOf(bigInteger.longValue());
    }

    private static Long r(Object obj) {
        if (!(obj instanceof Byte) && !(obj instanceof Short) && !(obj instanceof Integer) && !(obj instanceof Long)) {
            return null;
        }
        long jLongValue = ((Number) obj).longValue();
        if (jLongValue < 0) {
            return null;
        }
        return Long.valueOf(jLongValue);
    }

    private static Integer s(Map<?, ?> map, String str) {
        if (map.containsKey(str)) {
            return j(map.get(str));
        }
        return null;
    }

    public static AudioCapabilities u(Map<?, ?> map) throws IOException {
        char c2;
        char c3;
        Map<?, ?> map2 = map == null ? Collections.EMPTY_MAP : map;
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        boolean zContainsKey = map2.containsKey("audioFormats");
        boolean z = !zContainsKey;
        if (!zContainsKey) {
            arrayList.add(new b(100, null, e, 0));
        }
        Object obj = map2.get("audioFormats");
        if (zContainsKey) {
            if (obj instanceof List) {
                List list = (List) obj;
                if (list.size() <= 128) {
                    for (Object obj2 : list) {
                        if (obj2 instanceof Map) {
                            Map map3 = (Map) obj2;
                            Integer numJ = j(map3.get("type"));
                            String strE = e(map3.get("audioType"));
                            Long lQ = q(map3.get("audioOutputFormats"));
                            if (numJ != null && (numJ.intValue() == 100 || numJ.intValue() == 101 || numJ.intValue() == 102)) {
                                if (lQ != null && lQ.longValue() != 0 && (map3.get("audioType") == null || strE != null)) {
                                    if (numJ.intValue() != 101 || strE == null || strE.equals("default") || strE.equals("compatibility")) {
                                        arrayList.add(new b(numJ.intValue(), strE, lQ.longValue(), 0));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            throw g("AUDIO_FORMAT_TABLE_INVALID");
        }
        Object obj3 = map2.get("audioLatencies");
        if (obj3 != null) {
            if (obj3 instanceof List) {
                List list2 = (List) obj3;
                if (list2.size() <= 128) {
                    for (Object obj4 : list2) {
                        if (obj4 instanceof Map) {
                            Map map4 = (Map) obj4;
                            Integer numS = s(map4, "type");
                            Integer numS2 = s(map4, "sr");
                            Integer numS3 = s(map4, "ss");
                            Integer numS4 = s(map4, "ch");
                            String strE2 = e(map4.get("audioType"));
                            Long lR = r(map4.get("outputLatencyMicros"));
                            if (lR != null && lR.longValue() <= 2000000 && ((map4.get("audioType") == null || strE2 != null) && !k(map4, "type", numS) && !k(map4, "sr", numS2) && !k(map4, "ss", numS3) && !k(map4, "ch", numS4))) {
                                if (numS == null || numS.intValue() == 100) {
                                    c2 = 'e';
                                } else {
                                    c2 = 'e';
                                    if (numS.intValue() != 101) {
                                        c3 = 'f';
                                        if (numS.intValue() == 102) {
                                        }
                                    }
                                    if ((numS2 != null || numS2.intValue() >= 1) && ((numS3 == null || numS3.intValue() >= 1) && (numS4 == null || numS4.intValue() >= 1))) {
                                        arrayList2.add(new c(numS, strE2, numS2, numS3, numS4, lR.longValue(), 0));
                                    }
                                }
                                c3 = 'f';
                                if (numS2 != null) {
                                    arrayList2.add(new c(numS, strE2, numS2, numS3, numS4, lR.longValue(), 0));
                                } else {
                                    arrayList2.add(new c(numS, strE2, numS2, numS3, numS4, lR.longValue(), 0));
                                }
                            }
                        }
                    }
                }
            }
            throw g("AUDIO_LATENCY_TABLE_INVALID");
        }
        return new AudioCapabilities(arrayList, arrayList2, z);
    }

    public static long v(int i, int i2, int i3) {
        if (i3 == 16 && i2 >= 1) {
            int i4 = 2;
            if (i2 <= 2) {
                if (i != 8000) {
                    if (i == 16000) {
                        i4 = 4;
                    } else if (i == 24000) {
                        i4 = 6;
                    } else if (i == 32000) {
                        i4 = 8;
                    } else if (i != 44100) {
                        i4 = i != 48000 ? -1 : 14;
                    } else {
                        i4 = 10;
                    }
                }
                if (i4 < 0) {
                    return 0L;
                }
                return 1 << ((i4 + i2) - 1);
            }
        }
        return 0L;
    }

    private d w(b bVar, long j, int i, int i2, int i3) {
        String str = bVar.b == null ? "default" : bVar.b;
        long jMax = -1;
        for (c cVar : this.b) {
            if ((cVar.a == null || cVar.a.intValue() == bVar.a) && (cVar.b == null || cVar.b.equals(str))) {
                if (cVar.c == null || cVar.c.intValue() == i) {
                    if (cVar.d == null || cVar.d.intValue() == i3) {
                        if (cVar.e == null || cVar.e.intValue() == i2) {
                            jMax = Math.max(jMax, cVar.f);
                        }
                    }
                }
            }
        }
        return new d(bVar.a, str, j, i, i2, i3, (f & j) != 0 ? 1024 : 352, jMax < 0 ? 100 : (int) Math.max(32L, Math.min(2000L, (jMax + 999) / 1000)), bVar.a == 100 || bVar.a == 102);
    }

    private static int y(String str, b bVar) {
        String str2 = bVar.b;
        if (str.equals("alert") && bVar.a == 101) {
            if ("default".equals(str2)) {
                return 0;
            }
            if ("compatibility".equals(str2)) {
                return 1;
            }
            if (str2 == null) {
                return 2;
            }
        }
        if (bVar.a != 100 && (bVar.a != 102 || !str.equals("media"))) {
            return Integer.MAX_VALUE;
        }
        int i = str.equals("alert") ? 3 : 0;
        if (str.equals(str2)) {
            return i;
        }
        if ("default".equals(str2)) {
            return i + 1;
        }
        if ("compatibility".equals(str2)) {
            return i + 2;
        }
        if (str2 == null) {
            return i + 3;
        }
        return Integer.MAX_VALUE;
    }

    public d A(String str, int i, int i2, int i3) throws IOException {
        return x(str, i, i2, i3).get(0);
    }

    public List<String> f() {
        ArrayList arrayList = new ArrayList();
        for (b bVar : this.a) {
            int i = bVar.a;
            String str = bVar.b == null ? "UNSPECIFIED" : bVar.b;
            arrayList.add("AUDIO_OUTPUT_CAPABILITY stream=" + i + " type=" + str + " formats=0x" + Long.toHexString(bVar.c));
        }
        return List.copyOf(arrayList);
    }

    public boolean h() {
        for (b bVar : this.a) {
            if (bVar.a == 100 || bVar.a == 102) {
                if ((bVar.c & f) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean i() {
        for (b bVar : this.a) {
            if (bVar.a != 102 && (bVar.c & e) != 0) {
                return true;
            }
        }
        return false;
    }

    public boolean l() {
        return this.c;
    }

    public int t() {
        return this.a.size();
    }

    public List<d> x(String str, int i, int i2, int i3) throws IOException {
        int i4;
        int i5;
        d dVar;
        int i6;
        long j;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11 = i2;
        String strE = e("speechrecognition".equals(str) ? "speechRecognition" : str);
        if (strE == null) {
            throw g("AUDIO_TYPE_UNSUPPORTED");
        }
        long j2 = 0;
        if (v(i, i2, i3) == 0) {
            throw g("AUDIO_NATIVE_FORMAT_UNSUPPORTED");
        }
        ArrayList arrayList = new ArrayList();
        AudioCapabilities sVar = this;
        Iterator<b> it = sVar.a.iterator();
        while (true) {
            int i12 = 6;
            String str2 = "alert";
            int i13 = 16;
            i4 = 1;
            i5 = 0;
            if (!it.hasNext()) {
                break;
            }
            b next = it.next();
            int iY = y(strE, next);
            j2 = j2;
            if (iY != Integer.MAX_VALUE) {
                int i14 = 2;
                if (next.a != 102) {
                    int[] iArr = {48000, 44100, 32000, 24000, 16000, 8000};
                    int i15 = 0;
                    while (i15 < i12) {
                        int i16 = i4;
                        int i17 = iArr[i15];
                        int i18 = i5;
                        int i19 = i16;
                        while (i19 <= i14) {
                            long jV = v(i17, i19, i13);
                            if ((next.c & jV) == j2) {
                                i7 = i16;
                                it = it;
                                iArr = iArr;
                                i8 = iY;
                                i9 = i18;
                            } else {
                                int i20 = (i17 == i && i19 == i11) ? i16 : i18;
                                long jAbs = i17 == i ? j2 : ((((long) Math.abs(i17 - i)) * 1000) / ((long) Math.max(i17, i))) + 100;
                                if (i19 != i11) {
                                    jAbs += i11 != 2 ? 25L : 1000L;
                                }
                                if (strE.equals(str2)) {
                                    if (next.a == 101) {
                                        i10 = i18;
                                    } else {
                                        i10 = str2.equals(next.b) ? i16 : 2;
                                    }
                                    j = i10;
                                } else {
                                    j = j2;
                                }
                                i7 = i16;
                                i8 = iY;
                                i9 = i18;
                                arrayList.add(new a(sVar.w(next, jV, i17, i19, 16), (jAbs * 100) + (j * 10000000) + ((long) (i20 != 0 ? i18 : 1000000)) + ((long) iY), i9));
                            }
                            i19++;
                            i16 = i7;
                            i15 = i15;
                            sVar = this;
                            iY = i8;
                            str2 = str2;
                            it = it;
                            i13 = 16;
                            i14 = 2;
                            i11 = i2;
                            i18 = i9;
                            iArr = iArr;
                        }
                        sVar = this;
                        i5 = i18;
                        i4 = i16;
                        iArr = iArr;
                        i13 = 16;
                        i12 = 6;
                        i14 = 2;
                        i11 = i2;
                        i15++;
                        it = it;
                    }
                }
                Iterator<b> it2 = it;
                int i21 = i5;
                int i22 = iY;
                if (strE.equals("media") && (next.a == 100 || next.a == 102)) {
                    int i23 = 44100;
                    int[] iArr2 = {44100, 48000};
                    int i24 = i21;
                    while (i24 < 2) {
                        int i25 = iArr2[i24];
                        long j3 = i25 == i23 ? 4194304L : 8388608L;
                        if ((next.c & j3) == j2) {
                            i6 = i21;
                        } else {
                            i6 = 0;
                            arrayList.add(new a(w(next, j3, i25, 2, 16), ((long) i22) + ((long) (i25 == i ? i21 : 1000000)) + 1000000000 + ((long) (next.a == 102 ? i21 : 1000)), i6));
                        }
                        i24++;
                        i21 = i6;
                        i23 = 44100;
                    }
                }
                sVar = this;
                i11 = i2;
                it = it2;
            }
        }
        arrayList.sort(Comparator.comparingLong(new p(i5)).thenComparingInt(new q(i5)).thenComparing(new r(i5)).thenComparingLong(new p(i4)));
        ArrayList arrayList2 = new ArrayList();
        int size = arrayList.size();
        int i26 = 0;
        while (i26 < size) {
            Object obj = arrayList.get(i26);
            i26++;
            a aVar = (a) obj;
            if (!arrayList2.contains(aVar.a)) {
                arrayList2.add(aVar.a);
            }
        }
        if (strE.equals("alert") && !arrayList2.isEmpty() && ((d) arrayList2.get(0)).p() == 101 && (dVar = (d) arrayList2.stream().filter(new w0(6)).findFirst().orElse(null)) != null) {
            arrayList2.remove(dVar);
            arrayList2.add(1, dVar);
        }
        if (arrayList2.size() > 16) {
            arrayList2 = new ArrayList(arrayList2.subList(0, 16));
        }
        if (arrayList2.isEmpty()) {
            throw g("AUDIO_OUTPUT_FORMAT_NOT_ADVERTISED");
        }
        return List.copyOf(arrayList2);
    }

    public d z(String str, int i, int i2, int i3) throws IOException {
        int iY;
        if ("speechrecognition".equals(str)) {
            str = "speechRecognition";
        }
        String strE = e(str);
        if (strE == null) {
            throw g("AUDIO_TYPE_UNSUPPORTED");
        }
        long jV = v(i, i2, i3);
        if (jV == 0) {
            throw g("AUDIO_NATIVE_FORMAT_UNSUPPORTED");
        }
        b bVar = null;
        int i4 = Integer.MAX_VALUE;
        for (b bVar2 : this.a) {
            if (bVar2.a != 102 && (bVar2.c & jV) != 0 && (iY = y(strE, bVar2)) < i4) {
                bVar = bVar2;
                i4 = iY;
            }
        }
        if (bVar != null) {
            return w(bVar, jV, i, i2, i3);
        }
        throw g("AUDIO_OUTPUT_FORMAT_NOT_ADVERTISED");
    }

    public static final class a extends ProtoMessageBase {
        private final d a;
        private final long b;

        private a(d dVar, long j) {
            this.a = dVar;
            this.b = j;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.b == aVar.b && Objects.equals(this.a, aVar.a);
        }

        public d h() {
            return this.a;
        }

        public final int hashCode() {
            long j = this.b;
            d dVar = this.a;
            return Objects.hashCode(dVar) + (Long.hashCode(j) * 31);
        }

        public long i() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {this.a, Long.valueOf(this.b)};
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

        public /* synthetic */ a(d dVar, long j, int i) {
            this(dVar, j);
        }
    }

    public static final class b extends ProtoMessageBase {
        private final int a;
        private final String b;
        private final long c;

        private b(int i, String str, long j) {
            this.a = i;
            this.b = str;
            this.c = j;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && this.c == bVar.c && Objects.equals(this.b, bVar.b);
        }

        public final int hashCode() {
            int i = this.a;
            long j = this.c;
            String str = this.b;
            return Objects.hashCode(str) + ProtoMessageBase.a(j, i * 31, 31);
        }

        public long j() {
            return this.c;
        }

        public int k() {
            return this.a;
        }

        public String l() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), this.b, Long.valueOf(this.c)};
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

        public /* synthetic */ b(int i, String str, long j, int i2) {
            this(i, str, j);
        }
    }

    public static final class c extends ProtoMessageBase {
        private final Integer a;
        private final String b;
        private final Integer c;
        private final Integer d;
        private final Integer e;
        private final long f;

        private c(Integer num, String str, Integer num2, Integer num3, Integer num4, long j) {
            this.a = num;
            this.b = str;
            this.c = num2;
            this.d = num3;
            this.e = num4;
            this.f = j;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof c)) {
                return false;
            }
            c cVar = (c) obj;
            return this.f == cVar.f && Objects.equals(this.a, cVar.a) && Objects.equals(this.b, cVar.b) && Objects.equals(this.c, cVar.c) && Objects.equals(this.d, cVar.d) && Objects.equals(this.e, cVar.e);
        }

        public final int hashCode() {
            long j = this.f;
            Integer num = this.a;
            String str = this.b;
            Integer num2 = this.c;
            Integer num3 = this.d;
            Integer num4 = this.e;
            return Objects.hashCode(num4) + ((Objects.hashCode(num3) + ((Objects.hashCode(num2) + ProtoMessageBase.b(str, (Objects.hashCode(num) + (Long.hashCode(j) * 31)) * 31, 31)) * 31)) * 31);
        }

        public Integer m() {
            return this.d;
        }

        public Integer n() {
            return this.e;
        }

        public long o() {
            return this.f;
        }

        public Integer p() {
            return this.c;
        }

        public Integer q() {
            return this.a;
        }

        public String r() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, this.c, this.d, this.e, Long.valueOf(this.f)};
            String[] strArrSplit = "a;b;c;d;e;f".length() == 0 ? new String[0] : "a;b;c;d;e;f".split(";");
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

        public /* synthetic */ c(Integer num, String str, Integer num2, Integer num3, Integer num4, long j, int i) {
            this(num, str, num2, num3, num4, j);
        }
    }
}
