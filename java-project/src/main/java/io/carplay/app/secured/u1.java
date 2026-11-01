package io.carplay.app.secured;

import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.function.Supplier;

/* JADX INFO: loaded from: classes.dex */
public final class u1 implements AutoCloseable {
    public static final int j = 65570;
    public static final int k = 65571;
    public static final int l = 65572;
    public static final int m = 360449;
    public static final int n = 5000;
    private final long a;
    private final g4.e b;
    private final boolean c;
    private final o d;
    private final Supplier<io.carplay.app.secured.b.C0001b> e;
    private boolean f;
    private boolean g;
    private boolean h;
    private long i;

    public u1(long j2, g4.e eVar, boolean z, boolean z2, o oVar, Supplier<io.carplay.app.secured.b.C0001b> supplier) {
        if (j2 < 1) {
            d0.b("session generation must be positive");
            throw null;
        }
        Objects.requireNonNull(eVar);
        this.b = eVar;
        if (!z) {
            d0.b("caller must confirm microphone format");
            throw null;
        }
        eVar.l();
        if (eVar.k() == g4.b.UNKNOWN || eVar.j() == g4.f.UNKNOWN) {
            d0.b("microphone byte order/encoding unverified");
            throw null;
        }
        if (eVar.n() != g4.a.MICROPHONE || eVar.m() != 16000 || eVar.i() != 1 || eVar.g() != 16 || eVar.j() != g4.f.SIGNED_INTEGER) {
            d0.b("supported confirmed mic contract is 16000/mono/signed PCM16");
            throw null;
        }
        if (z2 && oVar == null) {
            d0.b("negotiated encryption needs explicit cipher");
            throw null;
        }
        this.a = j2;
        this.c = z2;
        this.d = oVar;
        this.e = supplier;
    }

    private void i() throws r5 {
        try {
            Supplier<io.carplay.app.secured.b.C0001b> supplier = this.e;
            io.carplay.app.secured.b.C0001b c0001b = supplier == null ? null : supplier.get();
            if (c0001b != null && c0001b.k() == this.a && c0001b.t()) {
                return;
            }
            m();
            d0.f("normal microphone session not accepted or stale");
        } catch (RuntimeException e) {
            m();
            throw new r5("mic session acceptance unavailable", e);
        }
    }

    private void k(long j2) throws r5 {
        if (this.g || j2 != this.a) {
            d0.f("cancelled or stale mic session");
        }
    }

    private void l(a aVar) throws r5 {
        if (aVar == null || aVar.a != this) {
            d0.f("foreign or missing microphone ticket");
            return;
        }
        k(aVar.b);
        if (this.f && aVar.c == this.i) {
            return;
        }
        d0.f("stale or inactive microphone ticket");
    }

    private void m() {
        this.f = false;
        this.i = Math.incrementExact(this.i);
    }

    public synchronized a b() throws Throwable {
        try {
            try {
                k(this.a);
                if (!this.f) {
                    throw new r5("phone has not started microphone recording");
                }
                return new a(this, this.a, this.i, 0);
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            throw th;
        }
    }

    public g4.e c() {
        return this.b;
    }

    @Override // java.lang.AutoCloseable
    public synchronized void close() {
        if (!this.g) {
            m();
            this.g = true;
        }
    }

    public synchronized boolean e(long j2, r6.c cVar) {
        k(j2);
        Objects.requireNonNull(cVar);
        int iH = cVar.i().h();
        if (iH != 65570 && iH != 65572 && iH != 65571) {
            return false;
        }
        if (cVar.h() != r6.a.CMD || cVar.i().g() != 0 || cVar.g().length != 0) {
            m();
            throw new r5("mic recording control must be empty phone CMD");
        }
        m();
        if (iH == 65570 || iH == 65572) {
            this.f = true;
        }
        return true;
    }

    public synchronized b f(a aVar, byte[] bArr, long j2) {
        byte[] bArrA;
        r6.b bVar;
        r6.a aVar2;
        try {
            l(aVar);
            Objects.requireNonNull(bArr);
            if (bArr.length == 0 || bArr.length > 5000 || bArr.length % this.b.h() != 0) {
                throw new r5("mic PCM length exceeds reader limit or sample alignment");
            }
            if (j2 < 0 || j2 > 4294967295L) {
                throw new r5("mic timestamp must be explicit opaque uint32");
            }
            bArrA = (byte[]) bArr.clone();
            if (this.c) {
                try {
                    bArrA = this.d.a(bArrA);
                    if (bArrA == null) {
                        m();
                        throw new r5("mic payload encryption returned null");
                    }
                } catch (RuntimeException | GeneralSecurityException e) {
                    m();
                    throw new r5("mic payload encryption failed", e);
                }
            }
            if (bArrA.length == 0 || bArrA.length > 5000) {
                m();
                throw new r5("mic wire body exceeds official reader buffer");
            }
            bVar = new r6.b(bArrA.length, j2, 0, m);
            aVar2 = r6.a.VR;
            r6.d(aVar2, bVar, n);
            l(aVar);
        } catch (Throwable th) {
            throw th;
        }
        return new b(this, aVar, new r6.c(aVar2, bVar, bArrA), 0);
    }

    public synchronized boolean g() {
        return this.f && !this.g;
    }

    public synchronized void n(b bVar, OutputStream outputStream) {
        if (bVar != null) {
            if (bVar.a == this) {
                l(bVar.b);
                i();
                l(bVar.b);
                if (bVar.d || this.h) {
                    throw new r5("mic payload already consumed or write reentry");
                }
                Objects.requireNonNull(outputStream);
                bVar.d = true;
                this.h = true;
                try {
                    r6.c cVar = bVar.c;
                    r6.l(outputStream, cVar.h(), cVar.i(), cVar.g(), n);
                    outputStream.flush();
                    this.h = false;
                } catch (Throwable th) {
                    this.h = false;
                    m();
                    throw th;
                }
            }
        }
        throw new r5("foreign or missing prepared mic payload");
    }

    public static final class a {
        private final u1 a;
        private final long b;
        private final long c;

        private a(u1 u1Var, long j, long j2) {
            this.a = u1Var;
            this.b = j;
            this.c = j2;
        }

        public long d() {
            return this.c;
        }

        public long e() {
            return this.b;
        }

        public /* synthetic */ a(u1 u1Var, long j, long j2, int i) {
            this(u1Var, j, j2);
        }
    }

    public static final class b {
        private final u1 a;
        private final a b;
        private final r6.c c;
        private boolean d;

        private b(u1 u1Var, a aVar, r6.c cVar) {
            this.a = u1Var;
            this.b = aVar;
            this.c = cVar;
        }

        public /* synthetic */ b(u1 u1Var, a aVar, r6.c cVar, int i) {
            this(u1Var, aVar, cVar);
        }
    }

    public u1(long j2, g4.e eVar, boolean z, boolean z2, o oVar) {
        this(j2, eVar, z, z2, oVar, null);
    }
}
