package io.carplay.app.secured;

import java.security.GeneralSecurityException;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public final class f5 {
    private final r6.a a;
    private final AacAudioFormat b;
    private final boolean c;
    private final o d;
    private final int e;
    private a f;
    private boolean g;
    private boolean h;
    private boolean i;
    private boolean j;
    private long k;
    private long l;

    public static final class a extends w1 {
        private final int a;
        private final int b;
        private final int c;

        public a(int i, int i2, int i3) {
            this.a = i;
            this.b = i2;
            this.c = i3;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.a == aVar.a && this.b == aVar.b && this.c == aVar.c;
        }

        public int g() {
            return this.c;
        }

        public int h() {
            return this.b;
        }

        public final int hashCode() {
            int i = this.a;
            return (((i * 31) + this.b) * 31) + this.c;
        }

        public int i() {
            return (this.c / 8) * this.b;
        }

        public int j() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(a.class, sb, "[");
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

    public static final class b extends w1 {
        private final c a;
        private final int b;
        private final long c;
        private final long d;
        private final a e;
        private final byte[] f;

        public b(c cVar, int i, long j, long j2, a aVar, byte[] bArr) {
            Objects.requireNonNull(bArr);
            byte[] bArr2 = (byte[]) bArr.clone();
            this.a = cVar;
            this.b = i;
            this.c = j;
            this.d = j2;
            this.e = aVar;
            this.f = bArr2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.b == bVar.b && this.c == bVar.c && this.d == bVar.d && Objects.equals(this.a, bVar.a) && Objects.equals(this.e, bVar.e) && Objects.equals(this.f, bVar.f);
        }

        public long g() {
            return this.d;
        }

        public a h() {
            return this.e;
        }

        public final int hashCode() {
            int i = this.b;
            long j = this.c;
            long j2 = this.d;
            c cVar = this.a;
            a aVar = this.e;
            byte[] bArr = this.f;
            return Objects.hashCode(bArr) + ((Objects.hashCode(aVar) + ((Objects.hashCode(cVar) + w1.a(j2, w1.a(j, i * 31, 31), 31)) * 31)) * 31);
        }

        public c i() {
            return this.a;
        }

        public int j() {
            return this.b;
        }

        public byte[] k() {
            return (byte[]) this.f.clone();
        }

        public long l() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {this.a, Integer.valueOf(this.b), Long.valueOf(this.c), Long.valueOf(this.d), this.e, this.f};
            String[] strArrSplit = "a;b;c;d;e;f".length() == 0 ? new String[0] : "a;b;c;d;e;f".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(b.class, sb, "[");
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

    public enum c {
        INIT,
        DATA,
        STOP,
        PAUSE,
        RESUME,
        SEEK
    }

    public enum AacAudioFormat {
        UNKNOWN,
        NATIVE_MIXED_MUSIC,
        PCM_MEDIA,
        ENCODED_MUSIC,
        NAVI_TTS,
        VR_PROMPT
    }

    public f5(r6.a aVar, AacAudioFormat dVar, boolean z, o oVar, int i) {
        Objects.requireNonNull(aVar);
        this.a = aVar;
        Objects.requireNonNull(dVar);
        this.b = dVar;
        this.c = z;
        this.d = oVar;
        if (i < 0) {
            d0.b("negative body limit");
            throw null;
        }
        this.e = i;
        int iOrdinal = dVar.ordinal();
        if (iOrdinal != 0) {
            if (iOrdinal == 1 || iOrdinal == 2 || iOrdinal == 3) {
                if (aVar == r6.a.MEDIA) {
                    return;
                }
            } else if (iOrdinal != 4) {
                if (iOrdinal != 5) {
                    throw new IncompatibleClassChangeError();
                }
                if (aVar == r6.a.VR) {
                    return;
                }
            } else if (aVar == r6.a.TTS) {
                return;
            }
            d0.b("producer/channel mismatch");
            throw null;
        }
    }

    private b b(r6.c cVar) throws r5 {
        int i;
        int i2;
        int i3;
        c cVar2;
        Objects.requireNonNull(cVar);
        if (this.b == AacAudioFormat.UNKNOWN) {
            d0.f("unconfirmed audio producer; output refused");
            return null;
        }
        if (cVar.h() != this.a) {
            d0.f("wrong audio channel");
            return null;
        }
        if (cVar.i().g() > this.e) {
            d0.f("audio body limit");
            return null;
        }
        int iH = cVar.i().h();
        r6.a aVar = this.a;
        r6.a aVar2 = r6.a.MEDIA;
        if (aVar == aVar2) {
            i = 196609;
        } else {
            i = aVar == r6.a.TTS ? 262145 : 327682;
        }
        if (aVar == aVar2) {
            i2 = 196614;
        } else {
            i2 = aVar == r6.a.TTS ? 262147 : 327683;
        }
        if (aVar == aVar2) {
            i3 = 196610;
        } else {
            i3 = aVar == r6.a.TTS ? 262146 : 327684;
        }
        byte[] bArrG = cVar.g();
        if (iH == i) {
            ProtobufReader.e eVarA = ProtobufReader.a(c(bArrG, true));
            int i4 = eVarA.i(1);
            int i5 = eVarA.i(2);
            int i6 = eVarA.i(3);
            if (i4 < 4000 || i4 > 48000 || !((i5 == 1 || i5 == 2) && (i6 == 8 || i6 == 16))) {
                d0.f("unconfirmed PCM format");
                return null;
            }
            this.f = new a(i4, i5, i6);
            this.g = false;
            this.h = false;
            this.j = false;
            this.l = 0L;
            return e(c.INIT, iH, cVar.i().j(), new byte[0]);
        }
        if (this.f == null) {
            d0.f("audio data/control before INIT");
            return null;
        }
        if (iH == i2) {
            if (this.g || this.h) {
                d0.f("audio data while paused/stopped");
                return null;
            }
            byte[] bArrC = c(bArrG, this.b != AacAudioFormat.NATIVE_MIXED_MUSIC);
            if (bArrC.length == 0 || bArrC.length % this.f.i() != 0) {
                d0.f("partial/empty PCM frame");
                return null;
            }
            long j = cVar.i().j();
            if (j < 0 || j > 4294967295L) {
                d0.f("invalid audio timestamp");
                return null;
            }
            if (this.j) {
                long jD = d(this.k, j);
                if (jD > 600000) {
                    d0.f("out-of-order or ambiguous audio clock gap");
                    return null;
                }
                this.l += jD;
            }
            this.j = true;
            this.k = j;
            return e(c.DATA, iH, j, bArrC);
        }
        if (bArrG.length != 0) {
            d0.f("unsupported control body");
            return null;
        }
        if (iH == i3) {
            this.h = true;
            cVar2 = c.STOP;
        } else {
            r6.a aVar3 = this.a;
            if (aVar3 == aVar2 && iH == 196611) {
                this.g = true;
                cVar2 = c.PAUSE;
            } else if (aVar3 == aVar2 && iH == 196612 && !this.h) {
                this.g = false;
                cVar2 = c.RESUME;
            } else {
                if (aVar3 != aVar2 || iH != 196613 || this.h) {
                    d0.f(w1.e("unsupported audio message/state: 0x", Integer.toHexString(iH)));
                    return null;
                }
                this.j = false;
                cVar2 = c.SEEK;
            }
        }
        return e(cVar2, iH, cVar.i().j(), new byte[0]);
    }

    private byte[] c(byte[] bArr, boolean z) throws r5 {
        if (!this.c || !z || bArr.length == 0) {
            return bArr;
        }
        o oVar = this.d;
        if (oVar == null) {
            d0.f("negotiated audio crypto has no verified implementation");
            return null;
        }
        try {
            byte[] bArrB = oVar.b(bArr);
            if (bArrB == null || bArrB.length > this.e) {
                throw new r5("invalid decrypted body");
            }
            return bArrB;
        } catch (GeneralSecurityException e) {
            throw new r5("audio decryption failed", e);
        }
    }

    public static long d(long j, long j2) {
        if ((j & (-4294967296L)) == 0 && ((-4294967296L) & j2) == 0) {
            return 4294967295L & (j2 - j);
        }
        d0.b("not uint32");
        return 0L;
    }

    private b e(c cVar, int i, long j, byte[] bArr) {
        return new b(cVar, i, j, this.l, this.f, bArr);
    }

    public synchronized b a(r6.c cVar) {
        if (this.i) {
            throw new r5("audio stream is failed; create a fresh session");
        }
        try {
        } catch (r5 e) {
            this.i = true;
            throw e;
        }
        return b(cVar);
    }
}
