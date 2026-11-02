package io.carplay.app.secured;

import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public final class b {
    private long a;
    private boolean b;
    private boolean c;
    private boolean d;
    private boolean e;
    private a f;
    private a g;
    private a h;
    private a i;
    private long j;
    private long k;
    private long l;
    private String m;

    public enum a {
        UNKNOWN,
        PASSED,
        FAILED,
        NOT_APPLICABLE
    }

    /* JADX INFO: renamed from: io.carplay.app.secured.b$b, reason: collision with other inner class name */
    public static final class C0003b extends w1 {
        private final long a;
        private final boolean b;
        private final a c;
        private final a d;
        private final a e;
        private final a f;
        private final boolean g;
        private final long h;
        private final long i;
        private final long j;
        private final boolean k;
        private final String l;
        private final boolean m;

        public C0003b(long j, boolean z, a aVar, a aVar2, a aVar3, a aVar4, boolean z2, long j2, long j3, long j4, boolean z3, String str, boolean z4) {
            this.a = j;
            this.b = z;
            this.c = aVar;
            this.d = aVar2;
            this.e = aVar3;
            this.f = aVar4;
            this.g = z2;
            this.h = j2;
            this.i = j3;
            this.j = j4;
            this.k = z3;
            this.l = str;
            this.m = z4;
        }

        private static boolean g(a aVar) {
            return aVar == a.PASSED || aVar == a.NOT_APPLICABLE;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof C0003b)) {
                return false;
            }
            C0003b c0003b = (C0003b) obj;
            return this.b == c0003b.b && this.g == c0003b.g && this.k == c0003b.k && this.m == c0003b.m && this.a == c0003b.a && this.h == c0003b.h && this.i == c0003b.i && this.j == c0003b.j && Objects.equals(this.c, c0003b.c) && Objects.equals(this.d, c0003b.d) && Objects.equals(this.e, c0003b.e) && Objects.equals(this.f, c0003b.f) && Objects.equals(this.l, c0003b.l);
        }

        public a h() {
            return this.e;
        }

        public final int hashCode() {
            boolean z = this.b;
            boolean z2 = this.g;
            boolean z3 = this.k;
            boolean z4 = this.m;
            long j = this.a;
            long j2 = this.h;
            long j3 = this.i;
            long j4 = this.j;
            a aVar = this.c;
            a aVar2 = this.d;
            a aVar3 = this.e;
            a aVar4 = this.f;
            String str = this.l;
            return Objects.hashCode(str) + ((Objects.hashCode(aVar4) + ((Objects.hashCode(aVar3) + ((Objects.hashCode(aVar2) + ((Objects.hashCode(aVar) + w1.a(j4, w1.a(j3, w1.a(j2, w1.a(j, (Boolean.hashCode(z4) + ((Boolean.hashCode(z3) + ((Boolean.hashCode(z2) + (Boolean.hashCode(z) * 31)) * 31)) * 31)) * 31, 31), 31), 31), 31)) * 31)) * 31)) * 31)) * 31);
        }

        public boolean i() {
            return this.k;
        }

        public a j() {
            return this.d;
        }

        public long k() {
            return this.a;
        }

        public a l() {
            return this.f;
        }

        public boolean m() {
            return this.g;
        }

        public long n() {
            return this.j;
        }

        public boolean o() {
            return r() && this.g && this.h > 0 && this.i > 0 && this.j > 0;
        }

        public long p() {
            return this.i;
        }

        public boolean q() {
            return this.m;
        }

        public boolean r() {
            return t() && this.m;
        }

        public long s() {
            return this.h;
        }

        public boolean t() {
            if (this.k || !this.b) {
                return false;
            }
            a aVar = this.c;
            a aVar2 = a.PASSED;
            return aVar == aVar2 && this.d == aVar2 && g(this.e) && g(this.f);
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), Boolean.valueOf(this.b), this.c, this.d, this.e, this.f, Boolean.valueOf(this.g), Long.valueOf(this.h), Long.valueOf(this.i), Long.valueOf(this.j), Boolean.valueOf(this.k), this.l, Boolean.valueOf(this.m)};
            String[] strArrSplit = "a;b;c;d;e;f;g;h;i;j;k;l;m".length() == 0 ? new String[0] : "a;b;c;d;e;f;g;h;i;j;k;l;m".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(C0003b.class, sb, "[");
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

        public String u() {
            return this.l;
        }

        public boolean v() {
            return this.b;
        }

        public a w() {
            return this.c;
        }
    }

    public b() {
        a aVar = a.UNKNOWN;
        this.f = aVar;
        this.g = aVar;
        this.h = aVar;
        this.i = aVar;
        this.m = "not started";
    }

    private void b(long j) {
        long j2 = this.a;
        if (j != j2 || j2 == 0) {
            d0.d("stale session generation");
        } else if (this.d) {
            d0.d("session already failed");
        }
    }

    private void l(long j) {
        b(j);
        if (m().r()) {
            return;
        }
        d0.d("native source not established");
    }

    public synchronized long a() {
        long jIncrementExact;
        jIncrementExact = Math.incrementExact(this.a);
        this.a = jIncrementExact;
        this.d = false;
        this.c = false;
        this.b = false;
        this.e = false;
        a aVar = a.UNKNOWN;
        this.i = aVar;
        this.h = aVar;
        this.g = aVar;
        this.f = aVar;
        this.l = 0L;
        this.k = 0L;
        this.j = 0L;
        this.m = "awaiting observations";
        return jIncrementExact;
    }

    public synchronized void c(long j, a aVar) {
        b(j);
        if (aVar == null) {
            throw new IllegalArgumentException("missing encryption verdict");
        }
        this.h = aVar;
        if (aVar == a.FAILED) {
            d(j, "content key exchange failed");
        }
    }

    public synchronized void d(long j, String str) {
        try {
            long j2 = this.a;
            if (j != j2 || j2 == 0) {
                throw new IllegalStateException("stale session generation");
            }
            this.d = true;
            if (str == null) {
                str = "failed";
            }
            this.m = str;
        } catch (Throwable th) {
            throw th;
        }
    }

    public synchronized void e(long j, boolean z) {
        try {
            b(j);
            this.g = z ? a.PASSED : a.FAILED;
            if (!z) {
                d(j, "feature contract failed");
            }
        } catch (Throwable th) {
            throw th;
        }
    }

    public synchronized void f(long j, a aVar) {
        b(j);
        if (aVar == null) {
            throw new IllegalArgumentException("missing identity verdict");
        }
        this.i = aVar;
        if (aVar == a.FAILED) {
            d(j, "identity proof rejected");
        }
    }

    public synchronized void g(long j) {
        b(j);
        this.c = true;
    }

    public synchronized void h(long j) {
        l(j);
        this.l = Math.incrementExact(this.l);
    }

    public synchronized void i(long j, int i) {
        l(j);
        if (i <= 0) {
            throw new IllegalArgumentException("empty PCM");
        }
        this.k = Math.addExact(this.k, i);
    }

    public synchronized void j(long j) {
        b(j);
        this.e = true;
    }

    public synchronized void k(long j) {
        l(j);
        this.j = Math.incrementExact(this.j);
    }

    public synchronized C0003b m() {
        return new C0003b(this.a, this.b, this.f, this.g, this.h, this.i, this.c, this.j, this.k, this.l, this.d, this.m, this.e);
    }

    public synchronized void n(long j) {
        b(j);
        this.b = true;
    }

    public synchronized void o(long j, boolean z) {
        try {
            b(j);
            this.f = z ? a.PASSED : a.FAILED;
            if (!z) {
                d(j, "version rejected");
            }
        } catch (Throwable th) {
            throw th;
        }
    }
}
