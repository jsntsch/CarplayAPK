package io.carplay.app.secured;

import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
/**
 * IccoaBridge
 *
 * <p>原混淆名：{@code io.carplay.app.secured.b3}（jadx 产物 b3.java）
 *
 * <p>命名依据：ICCOA callback size / missing sink
 */
public final class IccoaBridge {
    private final g4 a;
    private long b;
    private long c;
    private long d;
    private long e;
    private ConfigState f;
    private ConfigState g;
    private String h;
    private g4.g i;
    private String j;
    private String k;
    private int l;
    private int m;
    private int n;
    private int o;
    private g4.h p;
    private g4.e q;
    private int r;
    private long s;
    private long t;

    public enum TimeBase {
        UNKNOWN,
        MICROSECONDS
    }

    public enum ConfigState {
        UNCONFIGURED,
        CONFIGURING,
        CONFIGURED,
        INVALID
    }

    public enum KeyState {
        UNKNOWN,
        KEY,
        DELTA
    }

    public IccoaBridge(g4 g4Var) {
        ConfigState dVar = ConfigState.UNCONFIGURED;
        this.f = dVar;
        this.g = dVar;
        this.s = -1L;
        this.t = -1L;
        if (g4Var != null) {
            this.a = g4Var;
        } else {
            SyntheticRefs.b("missing sink");
            throw null;
        }
    }

    private static byte[] f(byte[] bArr, int i) {
        if (bArr != null && i >= 1 && i <= bArr.length && i <= 8388608) {
            return Arrays.copyOf(bArr, i);
        }
        SyntheticRefs.b("ICCOA callback size");
        return null;
    }

    private void p(long j, String str) {
        long j2 = this.b;
        if (j2 != 0 && j == j2 && this.h.equals(str)) {
            return;
        }
        SyntheticRefs.d("stale ICCOA generation/device");
    }

    private void q(b bVar) {
        if (bVar == null || bVar.a != this) {
            SyntheticRefs.d("foreign/missing audio ticket");
            return;
        }
        p(bVar.b, bVar.d);
        if (bVar.c != this.e || this.k == null) {
            SyntheticRefs.d("stale audio format epoch");
        }
    }

    private void r(g gVar) {
        if (gVar == null || gVar.a != this) {
            SyntheticRefs.d("foreign/missing video ticket");
            return;
        }
        p(gVar.b, gVar.d);
        if (gVar.c != this.d || this.j == null) {
            SyntheticRefs.d("stale video format epoch");
        }
    }

    private static boolean s(int i) {
        return (i >= 1 && i <= 7) || i == 16;
    }

    public synchronized void a(long j, String str, g4.g gVar) {
        if (j >= 1 && str != null) {
            if (!str.isEmpty() && str.length() <= 512 && gVar != null) {
                if (this.b != 0 || j <= this.c) {
                    throw new IllegalStateException("fresh generation required");
                }
                this.c = j;
                this.b = j;
                this.h = str;
                this.i = gVar;
            }
        }
        throw new IllegalArgumentException("ICCOA generation/device/source");
    }

    public synchronized a b(b bVar, int i, int i2, byte[] bArr, long j) {
        t(bVar);
        if (i != this.r || j < 0 || i2 % this.q.h() != 0) {
            throw new IllegalStateException("ICCOA PCM source/format/alignment/clock");
        }
        return new a(bVar, new g4.c(bVar.b, this.i, f(bArr, i2), j, false), 0);
    }

    public synchronized b c(long j, String str) throws Throwable {
        Throwable th = null;
        try {
            try {
                p(j, str);
                b bVar = new b(this, j, str, this.e, 0);
                t(bVar);
                return bVar;
            } catch (Throwable thCaught) {
                throw thCaught;
            }
        } catch (Throwable th2) {
            th = th2;
            throw th;
        }
    }

    public synchronized f d(g gVar, int i, int i2, byte[] bArr, long j, KeyState eVar) {
        u(gVar);
        if (i != 1 || eVar == null || eVar == KeyState.UNKNOWN || j < 0) {
            throw new IllegalStateException("ICCOA video type/format/clock");
        }
        return new f(gVar, new g4.c(gVar.b, this.i, f(bArr, i2), j, eVar == KeyState.KEY), 0);
    }

    public synchronized g e(long j, String str) throws Throwable {
        Throwable th = null;
        try {
            try {
                p(j, str);
                g gVar = new g(this, j, str, this.d, 0);
                u(gVar);
                return gVar;
            } catch (Throwable thCaught) {
                throw thCaught;
            }
        } catch (Throwable th2) {
            th = th2;
            throw th;
        }
    }

    public synchronized void g(b bVar, int i, g4.e eVar, TimeBase cVar, boolean z) {
        q(bVar);
        if (this.g != ConfigState.UNCONFIGURED) {
            throw new IllegalStateException("audio epoch configuring/configured/invalid; new initialization required");
        }
        if (!s(i) || !"audio/pcm".equals(this.k) || eVar == null || eVar.n() == g4.a.MICROPHONE || eVar.m() != this.n || eVar.i() != this.o || cVar != TimeBase.MICROSECONDS || !z) {
            throw new IllegalStateException("ICCOA PCM/type/clock unverified");
        }
        eVar.l();
        ConfigState dVar = ConfigState.CONFIGURING;
        this.g = dVar;
        try {
            this.a.a(bVar.b, this.i, eVar);
            q(bVar);
            if (this.g != dVar) {
                throw new IllegalStateException("audio configuration changed during callback");
            }
            this.r = i;
            this.q = eVar;
            this.g = ConfigState.CONFIGURED;
            if (this.b == bVar.b && this.e == bVar.c && this.g == dVar) {
                this.q = null;
                this.r = 0;
                this.g = ConfigState.INVALID;
            }
        } catch (Throwable th) {
            if (this.b == bVar.b && this.e == bVar.c && this.g == ConfigState.CONFIGURING) {
                this.q = null;
                this.r = 0;
                this.g = ConfigState.INVALID;
            }
            throw th;
        }
    }

    public synchronized void h(g gVar, g4.h hVar, TimeBase cVar, boolean z) {
        try {
            r(gVar);
            if (this.f != ConfigState.UNCONFIGURED) {
                throw new IllegalStateException("video epoch configuring/configured/invalid; new initialization required");
            }
            if (hVar == null || !hVar.k().equals(this.j) || hVar.l() != this.l || hVar.i() != this.m || cVar != TimeBase.MICROSECONDS || !z) {
                throw new IllegalStateException("ICCOA video format/packet/clock unverified");
            }
            byte[][] bArrG = hVar.g();
            if (bArrG.length == 0) {
                throw new IllegalStateException("missing verified codec configuration");
            }
            for (byte[] bArr : bArrG) {
                if (bArr.length == 0) {
                    throw new IllegalStateException("empty codec configuration");
                }
            }
            ConfigState dVar = ConfigState.CONFIGURING;
            this.f = dVar;
            try {
                this.a.b(gVar.b, this.i, hVar);
                r(gVar);
                if (this.f != dVar) {
                    throw new IllegalStateException("video configuration changed during callback");
                }
                this.p = hVar;
                this.f = ConfigState.CONFIGURED;
                if (this.b == gVar.b && this.d == gVar.c && this.f == dVar) {
                    this.p = null;
                    this.f = ConfigState.INVALID;
                }
            } catch (Throwable th) {
                if (this.b == gVar.b && this.d == gVar.c && this.f == ConfigState.CONFIGURING) {
                    this.p = null;
                    this.f = ConfigState.INVALID;
                }
                throw th;
            }
        } catch (Throwable th2) {
            throw th2;
        }
    }

    public synchronized void i(a aVar) {
        if (aVar == null) {
            throw new IllegalArgumentException("missing audio event");
        }
        t(aVar.a);
        if (aVar.b.j() < this.t) {
            throw new IllegalStateException("ICCOA PCM clock regression");
        }
        this.t = aVar.b.j();
        try {
            this.a.f(aVar.b);
        } catch (Error | RuntimeException e2) {
            if (this.b == aVar.a.b && this.e == aVar.a.c) {
                this.q = null;
                this.r = 0;
                this.g = ConfigState.INVALID;
            }
            throw e2;
        }
    }

    public synchronized void j(f fVar) {
        if (fVar == null) {
            throw new IllegalArgumentException("missing video event");
        }
        u(fVar.a);
        if (fVar.b.j() < this.s) {
            throw new IllegalStateException("ICCOA video clock regression");
        }
        this.s = fVar.b.j();
        try {
            this.a.c(fVar.b);
        } catch (Error | RuntimeException e2) {
            if (this.b == fVar.a.b && this.d == fVar.a.c) {
                this.p = null;
                this.f = ConfigState.INVALID;
            }
            throw e2;
        }
    }

    public synchronized void k(long j, String str) {
        long j2 = this.b;
        if (j2 != 0 && j == j2) {
            this.b = 0L;
            this.h = null;
            this.i = null;
            this.k = null;
            this.j = null;
            this.p = null;
            this.q = null;
            this.r = 0;
            ConfigState dVar = ConfigState.UNCONFIGURED;
            this.g = dVar;
            this.f = dVar;
            this.o = 0;
            this.n = 0;
            this.m = 0;
            this.l = 0;
            this.t = -1L;
            this.s = -1L;
            this.a.e(j, str);
        }
    }

    public synchronized void l(b bVar, int i, int i2, byte[] bArr, long j) {
        i(b(bVar, i, i2, bArr, j));
    }

    public synchronized b m(long j, String str, String str2, int i, int i2) {
        long jIncrementExact;
        p(j, str);
        jIncrementExact = Math.incrementExact(this.e);
        this.e = jIncrementExact;
        this.q = null;
        this.k = null;
        this.g = ConfigState.UNCONFIGURED;
        if (str2 == null || i < 4000 || i > 192000 || i2 < 1 || i2 > 8) {
            this.k = null;
            throw new IllegalArgumentException("ICCOA audio initialization");
        }
        this.k = str2;
        this.n = i;
        this.o = i2;
        return new b(this, j, str, jIncrementExact, 0);
    }

    public synchronized void n(g gVar, int i, int i2, byte[] bArr, long j, KeyState eVar) {
        j(d(gVar, i, i2, bArr, j, eVar));
    }

    public synchronized g o(long j, String str, String str2, int i, int i2) {
        p(j, str);
        this.d = Math.incrementExact(this.d);
        this.p = null;
        this.f = ConfigState.UNCONFIGURED;
        this.j = null;
        if ("video/avc".equals(str2) || "video/hevc".equals(str2)) {
            if (i >= 1 && i2 >= 1 && i <= 8192 && i2 <= 8192) {
                this.j = str2;
                this.l = i;
                this.m = i2;
                return new g(this, j, str, this.d, 0);
            }
        }
        this.j = null;
        throw new IllegalArgumentException("unsupported ICCOA video initialization");
    }

    public synchronized void t(b bVar) {
        q(bVar);
        if (this.g != ConfigState.CONFIGURED || this.q == null) {
            throw new IllegalStateException("audio epoch not configured");
        }
    }

    public synchronized void u(g gVar) {
        r(gVar);
        if (this.f != ConfigState.CONFIGURED || this.p == null) {
            throw new IllegalStateException("video epoch not configured");
        }
    }

    public static final class a {
        private final b a;
        private final g4.c b;

        private a(b bVar, g4.c cVar) {
            this.a = bVar;
            this.b = cVar;
        }

        public g4.c c() {
            return this.b;
        }

        public b d() {
            return this.a;
        }

        public /* synthetic */ a(b bVar, g4.c cVar, int i) {
            this(bVar, cVar);
        }
    }

    public static final class f {
        private final g a;
        private final g4.c b;

        private f(g gVar, g4.c cVar) {
            this.a = gVar;
            this.b = cVar;
        }

        public g4.c c() {
            return this.b;
        }

        public g d() {
            return this.a;
        }

        public /* synthetic */ f(g gVar, g4.c cVar, int i) {
            this(gVar, cVar);
        }
    }

    public static final class b {
        private final IccoaBridge a;
        private final long b;
        private final long c;
        private final String d;

        private b(IccoaBridge iccoaBridge, long j, String str, long j2) {
            this.a = iccoaBridge;
            this.b = j;
            this.d = str;
            this.c = j2;
        }

        public long e() {
            return this.c;
        }

        public long f() {
            return this.b;
        }

        public /* synthetic */ b(IccoaBridge iccoaBridge, long j, String str, long j2, int i) {
            this(iccoaBridge, j, str, j2);
        }
    }

    public static final class g {
        private final IccoaBridge a;
        private final long b;
        private final long c;
        private final String d;

        private g(IccoaBridge iccoaBridge, long j, String str, long j2) {
            this.a = iccoaBridge;
            this.b = j;
            this.d = str;
            this.c = j2;
        }

        public long e() {
            return this.c;
        }

        public long f() {
            return this.b;
        }

        public /* synthetic */ g(IccoaBridge iccoaBridge, long j, String str, long j2, int i) {
            this(iccoaBridge, j, str, j2);
        }
    }
}
