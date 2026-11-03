package io.carplay.app.secured;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public final class IoStatsSnapshot {
    private static final int b = 256;
    private final EnumMap<b, a> a = new EnumMap<>(b.class);

    public enum b {
        NATIVE_ASSEMBLY,
        ADAPTER_QUEUE,
        DECODE,
        RENDER,
        ENCODE,
        SENDER_QUEUE,
        SOCKET_WRITE,
        SOURCE_TO_WRITE,
        TOUCH_WRITE,
        RECOVERY_WAIT
    }

    public static final class c extends ProtoMessageBase {
        private final long a;
        private final int b;
        private final long c;
        private final long d;
        private final long e;

        public c(long j, int i, long j2, long j3, long j4) {
            this.a = j;
            this.b = i;
            this.c = j2;
            this.d = j3;
            this.e = j4;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof c)) {
                return false;
            }
            c cVar = (c) obj;
            return this.b == cVar.b && this.a == cVar.a && this.c == cVar.c && this.d == cVar.d && this.e == cVar.e;
        }

        public long g() {
            return this.a;
        }

        public long h() {
            return this.e;
        }

        public final int hashCode() {
            int i = this.b;
            long j = this.a;
            long j2 = this.c;
            long j3 = this.d;
            long j4 = this.e;
            return Long.hashCode(j4) + ProtoMessageBase.a(j3, ProtoMessageBase.a(j2, ProtoMessageBase.a(j, i * 31, 31), 31), 31);
        }

        public long i() {
            return this.c;
        }

        public long j() {
            return this.d;
        }

        public int k() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), Integer.valueOf(this.b), Long.valueOf(this.c), Long.valueOf(this.d), Long.valueOf(this.e)};
            String[] strArrSplit = "a;b;c;d;e".length() == 0 ? new String[0] : "a;b;c;d;e".split(";");
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

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ a b(b bVar) {
        return new a(0);
    }

    public synchronized void c() {
        this.a.clear();
    }

    public synchronized void d(b bVar, long j, long j2) {
        long j3 = j2 - j;
        if (bVar == null || j3 < 0 || j3 > 60000000000L) {
            return;
        }
        try {
            a aVarComputeIfAbsent = this.a.computeIfAbsent(bVar, new r(5));
            long[] jArr = aVarComputeIfAbsent.a;
            int i = aVarComputeIfAbsent.d;
            jArr[i] = j3;
            aVarComputeIfAbsent.d = (i + 1) % b;
            int i2 = aVarComputeIfAbsent.c;
            if (i2 < b) {
                aVarComputeIfAbsent.c = i2 + 1;
            }
            long j4 = aVarComputeIfAbsent.b;
            if (j4 < Long.MAX_VALUE) {
                aVarComputeIfAbsent.b = j4 + 1;
            }
        } catch (Throwable th) {
            throw th;
        }
    }

    public synchronized Map<b, c> e() {
        EnumMap enumMap;
        try {
            enumMap = new EnumMap(b.class);
            Iterator it = this.a.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                a aVar = (a) entry.getValue();
                long[] jArrCopyOf = Arrays.copyOf(aVar.a, aVar.c);
                Arrays.sort(jArrCopyOf);
                if (jArrCopyOf.length > 0) {
                    enumMap.put((b) entry.getKey(), new c(aVar.b, aVar.c, jArrCopyOf[(jArrCopyOf.length - 1) / 2], jArrCopyOf[((int) Math.ceil(((double) jArrCopyOf.length) * 0.95d)) - 1], jArrCopyOf[jArrCopyOf.length - 1]));
                }
            }
        } catch (Throwable th) {
            throw th;
        }
        return Map.copyOf(enumMap);
    }

    public synchronized Map<b, c> f(long j) {
        return e();
    }

    public static final class a {
        final long[] a;
        long b;
        int c;
        int d;

        private a() {
            this.a = new long[IoStatsSnapshot.b];
        }

        public /* synthetic */ a(int i) {
            this();
        }
    }
}
