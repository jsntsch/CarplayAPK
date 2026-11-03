package io.carplay.app.secured;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/* JADX INFO: loaded from: classes.dex */
final class j5 {
    static final long f = 5000;
    private static final Set<Integer> g = Set.of(0, 1, 2, 4, 5, 6);
    private boolean a;
    private boolean b;
    private long c;
    private long d = -1;
    private Boolean e;

    @FunctionalInterface
    public interface a {
        void run();
    }

    public static final class b extends ProtoMessageBase {
        private final boolean a;

        public b(boolean z) {
            this.a = z;
        }

        public final boolean equals(Object obj) {
            return (obj instanceof b) && this.a == ((b) obj).a;
        }

        public boolean g() {
            return this.a;
        }

        public final int hashCode() {
            return Boolean.hashCode(this.a);
        }

        public final String toString() {
            Object[] objArr = {Boolean.valueOf(this.a)};
            String[] strArrSplit = "a".length() == 0 ? new String[0] : "a".split(";");
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

    public static final class c extends ProtoMessageBase {
        private final long a;
        private final boolean b;

        public c(long j, boolean z) {
            this.a = j;
            this.b = z;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof c)) {
                return false;
            }
            c cVar = (c) obj;
            return this.b == cVar.b && this.a == cVar.a;
        }

        public boolean g() {
            return this.b;
        }

        public long h() {
            return this.a;
        }

        public final int hashCode() {
            boolean z = this.b;
            long j = this.a;
            return Long.hashCode(j) + (Boolean.hashCode(z) * 31);
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), Boolean.valueOf(this.b)};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
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

    public static b c(List<Iap2Csm.e> list) {
        if (list == null || list.size() > g.size()) {
            return null;
        }
        HashSet hashSet = new HashSet();
        for (Iap2Csm.e eVar : list) {
            if (eVar == null || !g.contains(Integer.valueOf(eVar.a)) || !hashSet.add(Integer.valueOf(eVar.a)) || eVar.b().length != 0) {
                return null;
            }
        }
        return new b(hashSet.contains(4));
    }

    public synchronized void a() {
        this.b = true;
        e();
    }

    public synchronized boolean b(c cVar, a aVar) {
        if (!this.b && this.a && cVar != null && cVar.h() == this.c) {
            aVar.run();
            this.e = Boolean.valueOf(cVar.g());
            return true;
        }
        return false;
    }

    public c d(long j, Supplier<Boolean> supplier) {
        Boolean bool;
        synchronized (this) {
            if (!this.b && this.a && j >= 0) {
                long j2 = this.d;
                if (j2 < 0 || j >= j2) {
                    long j3 = this.c;
                    this.d = j > 9223372036854770807L ? Long.MAX_VALUE : j + f;
                    try {
                        bool = supplier.get();
                    } catch (RuntimeException unused) {
                        bool = null;
                    }
                    synchronized (this) {
                        try {
                            if (!this.b && this.a && j3 == this.c && bool != null && !bool.equals(this.e)) {
                                return new c(j3, bool.booleanValue());
                            }
                            return null;
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                }
            }
            return null;
        }
    }

    public synchronized void e() {
        this.a = false;
        this.c++;
        this.d = -1L;
        this.e = null;
    }

    public synchronized void f(boolean z) {
        if (!this.b && this.a != z) {
            this.a = z;
            this.c++;
            this.d = -1L;
            this.e = null;
        }
    }
}
