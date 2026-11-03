package io.carplay.app.secured;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Supplier;

/* JADX INFO: loaded from: classes.dex */
/**
 * MicCapture
 *
 * <p>原混淆名：{@code io.carplay.app.secured.u1}（jadx 产物 u1.java）
 *
 * <p>命名依据：caller must confirm microphone format / microphone byte order/encoding unverified / 16000/mono/sig
 */
public final class MicCapture implements AutoCloseable {
    public static final int j = 65570;
    public static final int k = 65571;
    public static final int l = 65572;
    public static final int m = 360449;
    public static final int n = 5000;
    private final long a;
    private final MediaFormat.PcmFormat b;
    private final boolean c;
    private final o d;
    private final Supplier<io.carplay.app.secured.SessionVerificationTracker.C0003b> e;
    private boolean f;
    private boolean g;
    private boolean h;
    private long i;

    public MicCapture(long j2, MediaFormat.PcmFormat eVar, boolean z, boolean z2, o oVar, Supplier<io.carplay.app.secured.SessionVerificationTracker.C0003b> supplier) {
        if (j2 < 1) {
            SyntheticRefs.b("session generation must be positive");
            throw null;
        }
        Objects.requireNonNull(eVar);
        this.b = eVar;
        if (!z) {
            SyntheticRefs.b("caller must confirm microphone format");
            throw null;
        }
        eVar.l();
        if (eVar.k() == MediaFormat.Endian.UNKNOWN || eVar.j() == MediaFormat.f.UNKNOWN) {
            SyntheticRefs.b("microphone byte order/encoding unverified");
            throw null;
        }
        if (eVar.n() != MediaFormat.a.MICROPHONE || eVar.m() != 16000 || eVar.i() != 1 || eVar.g() != 16 || eVar.j() != MediaFormat.f.SIGNED_INTEGER) {
            SyntheticRefs.b("supported confirmed mic contract is 16000/mono/signed PCM16");
            throw null;
        }
        if (z2 && oVar == null) {
            SyntheticRefs.b("negotiated encryption needs explicit cipher");
            throw null;
        }
        this.a = j2;
        this.c = z2;
        this.d = oVar;
        this.e = supplier;
    }

    private void i() throws ProtocolIOException {
        try {
            Supplier<io.carplay.app.secured.SessionVerificationTracker.C0003b> supplier = this.e;
            io.carplay.app.secured.SessionVerificationTracker.C0003b c0003b = supplier == null ? null : supplier.get();
            if (c0003b != null && c0003b.k() == this.a && c0003b.t()) {
                return;
            }
            m();
            SyntheticRefs.f("normal microphone session not accepted or stale");
        } catch (RuntimeException e) {
            m();
            throw new ProtocolIOException("mic session acceptance unavailable", e);
        }
    }

    private void k(long j2) throws ProtocolIOException {
        if (this.g || j2 != this.a) {
            SyntheticRefs.f("cancelled or stale mic session");
        }
    }

    private void l(a aVar) throws ProtocolIOException {
        if (aVar == null || aVar.a != this) {
            SyntheticRefs.f("foreign or missing microphone ticket");
            return;
        }
        k(aVar.b);
        if (this.f && aVar.c == this.i) {
            return;
        }
        SyntheticRefs.f("stale or inactive microphone ticket");
    }

    private void m() {
        this.f = false;
        this.i = Math.incrementExact(this.i);
    }

    public synchronized a b() throws Throwable {
        Throwable th = null;
        try {
            try {
                k(this.a);
                if (!this.f) {
                    throw new ProtocolIOException("phone has not started microphone recording");
                }
                return new a(this, this.a, this.i, 0);
            } catch (Throwable thCaught) {
                throw thCaught;
            }
        } catch (Throwable th2) {
            th = th2;
            throw th;
        }
    }

    public MediaFormat.PcmFormat c() {
        return this.b;
    }

    @Override // java.lang.AutoCloseable
    public synchronized void close() {
        if (!this.g) {
            m();
            this.g = true;
        }
    }

    public synchronized boolean e(long j2, CarLifeFrame.c cVar) throws ProtocolIOException {
        k(j2);
        Objects.requireNonNull(cVar);
        int iH = cVar.i().h();
        if (iH != 65570 && iH != 65572 && iH != 65571) {
            return false;
        }
        if (cVar.h() != CarLifeFrame.a.CMD || cVar.i().g() != 0 || cVar.g().length != 0) {
            m();
            throw new ProtocolIOException("mic recording control must be empty phone CMD");
        }
        m();
        if (iH == 65570 || iH == 65572) {
            this.f = true;
        }
        return true;
    }

    public synchronized b f(a aVar, byte[] bArr, long j2) throws ProtocolIOException {
        byte[] bArrA;
        CarLifeFrame.b bVar;
        CarLifeFrame.a aVar2;
        l(aVar);
        Objects.requireNonNull(bArr);
        if (bArr.length == 0 || bArr.length > 5000 || bArr.length % this.b.h() != 0) {
            throw new ProtocolIOException("mic PCM length exceeds reader limit or sample alignment");
        }
        if (j2 < 0 || j2 > 4294967295L) {
            throw new ProtocolIOException("mic timestamp must be explicit opaque uint32");
        }
        bArrA = (byte[]) bArr.clone();
        if (this.c) {
            try {
                bArrA = this.d.a(bArrA);
                if (bArrA == null) {
                    m();
                    throw new ProtocolIOException("mic payload encryption returned null");
                }
            } catch (RuntimeException e) {
                m();
                throw new ProtocolIOException("mic payload encryption failed", e);
            }
        }
        if (bArrA.length == 0 || bArrA.length > 5000) {
            m();
            throw new ProtocolIOException("mic wire body exceeds official reader buffer");
        }
        bVar = new CarLifeFrame.b(bArrA.length, j2, 0, m);
        aVar2 = CarLifeFrame.a.VR;
        CarLifeFrame.d(aVar2, bVar, n);
        l(aVar);
        return new b(this, aVar, new CarLifeFrame.c(aVar2, bVar, bArrA), 0);
    }

    public synchronized boolean g() {
        return this.f && !this.g;
    }

    public synchronized void n(b bVar, OutputStream outputStream) throws IOException {
        if (bVar != null) {
            if (bVar.a == this) {
                l(bVar.b);
                i();
                l(bVar.b);
                if (bVar.d || this.h) {
                    throw new ProtocolIOException("mic payload already consumed or write reentry");
                }
                Objects.requireNonNull(outputStream);
                bVar.d = true;
                this.h = true;
                try {
                    CarLifeFrame.c cVar = bVar.c;
                    CarLifeFrame.l(outputStream, cVar.h(), cVar.i(), cVar.g(), n);
                    outputStream.flush();
                    this.h = false;
                } catch (IOException | RuntimeException e) {
                    this.h = false;
                    m();
                    throw e;
                }
            }
        }
        throw new ProtocolIOException("foreign or missing prepared mic payload");
    }

    public static final class a {
        private final MicCapture a;
        private final long b;
        private final long c;

        private a(MicCapture u1Var, long j, long j2) {
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

        public /* synthetic */ a(MicCapture u1Var, long j, long j2, int i) {
            this(u1Var, j, j2);
        }
    }

    public static final class b {
        private final MicCapture a;
        private final a b;
        private final CarLifeFrame.c c;
        private boolean d;

        private b(MicCapture u1Var, a aVar, CarLifeFrame.c cVar) {
            this.a = u1Var;
            this.b = aVar;
            this.c = cVar;
        }

        public /* synthetic */ b(MicCapture u1Var, a aVar, CarLifeFrame.c cVar, int i) {
            this(u1Var, aVar, cVar);
        }
    }

    public MicCapture(long j2, MediaFormat.PcmFormat eVar, boolean z, boolean z2, o oVar) {
        this(j2, eVar, z, z2, oVar, null);
    }
}
