package io.carplay.app.secured;

import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public interface g4 {

    public enum a {
        MUSIC,
        NAVIGATION,
        SPEECH,
        MICROPHONE
    }

    public enum b {
        LITTLE_ENDIAN,
        BIG_ENDIAN,
        UNKNOWN
    }

    public static final class c extends w1 {
        private final long a;
        private final g b;
        private final byte[] c;
        private final long d;
        private final boolean e;

        public c(long j, g gVar, byte[] bArr, long j2, boolean z) {
            if (j < 1 || gVar == null || bArr == null || bArr.length == 0 || j2 < 0) {
                d0.b("media frame");
                throw null;
            }
            if (bArr.length > 8388608) {
                d0.b("frame too large");
                throw null;
            }
            byte[] bArr2 = (byte[]) bArr.clone();
            this.a = j;
            this.b = gVar;
            this.c = bArr2;
            this.d = j2;
            this.e = z;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof c)) {
                return false;
            }
            c cVar = (c) obj;
            return this.e == cVar.e && this.a == cVar.a && this.d == cVar.d && Objects.equals(this.b, cVar.b) && Objects.equals(this.c, cVar.c);
        }

        public long g() {
            return this.a;
        }

        public boolean h() {
            return this.e;
        }

        public final int hashCode() {
            boolean z = this.e;
            long j = this.a;
            long j2 = this.d;
            g gVar = this.b;
            byte[] bArr = this.c;
            return Objects.hashCode(bArr) + ((Objects.hashCode(gVar) + w1.a(j2, w1.a(j, Boolean.hashCode(z) * 31, 31), 31)) * 31);
        }

        public byte[] i() {
            return (byte[]) this.c.clone();
        }

        public long j() {
            return this.d;
        }

        public g k() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), this.b, this.c, Long.valueOf(this.d), Boolean.valueOf(this.e)};
            String[] strArrSplit = "a;b;c;d;e".length() == 0 ? new String[0] : "a;b;c;d;e".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(c.class, sb, "[");
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

    public enum AacAudioFormat {
        ANNEX_B,
        AVCC
    }

    public enum f {
        SIGNED_INTEGER,
        UNSIGNED_INTEGER,
        FLOAT,
        UNKNOWN
    }

    public enum g {
        NATIVE_CARPLUS,
        TRANSPORT_TEST
    }

    public static final class h extends w1 {
        private final String a;
        private final int b;
        private final int c;
        private final AacAudioFormat d;
        private final byte[][] e;

        public h(String str, int i, int i2, AacAudioFormat dVar, byte[][] bArr) {
            if (!"video/avc".equals(str) && !"video/hevc".equals(str)) {
                d0.b("unsupported video MIME");
                throw null;
            }
            if (i < 1 || i2 < 1 || i > 8192 || i2 > 8192 || dVar == null) {
                d0.b("video dimensions/layout");
                throw null;
            }
            byte[][] bArrH = h(bArr);
            this.a = str;
            this.b = i;
            this.c = i2;
            this.d = dVar;
            this.e = bArrH;
        }

        private static byte[][] h(byte[][] bArr) {
            if (bArr == null) {
                return new byte[0][];
            }
            byte[][] bArr2 = new byte[bArr.length][];
            for (int i = 0; i < bArr.length; i++) {
                bArr2[i] = (byte[]) bArr[i].clone();
            }
            return bArr2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof h)) {
                return false;
            }
            h hVar = (h) obj;
            return this.b == hVar.b && this.c == hVar.c && Objects.equals(this.a, hVar.a) && Objects.equals(this.d, hVar.d) && Objects.equals(this.e, hVar.e);
        }

        public byte[][] g() {
            return h(this.e);
        }

        public final int hashCode() {
            int i = this.b;
            int i2 = this.c;
            String str = this.a;
            AacAudioFormat dVar = this.d;
            byte[][] bArr = this.e;
            return Objects.hashCode(bArr) + ((Objects.hashCode(dVar) + w1.b(str, ((i * 31) + i2) * 31, 31)) * 31);
        }

        public int i() {
            return this.c;
        }

        public AacAudioFormat j() {
            return this.d;
        }

        public String k() {
            return this.a;
        }

        public int l() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {this.a, Integer.valueOf(this.b), Integer.valueOf(this.c), this.d, this.e};
            String[] strArrSplit = "a;b;c;d;e".length() == 0 ? new String[0] : "a;b;c;d;e".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(h.class, sb, "[");
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

    void a(long j, g gVar, e eVar);

    void b(long j, g gVar, h hVar);

    void c(c cVar);

    void e(long j, String str);

    void f(c cVar);

    public static final class e extends w1 {
        private final a a;
        private final int b;
        private final int c;
        private final int d;
        private final b e;
        private final f f;

        public e(a aVar, int i, int i2, int i3, b bVar, f fVar) {
            if (aVar == null || bVar == null || fVar == null || i < 4000 || i > 192000 || i2 < 1 || i2 > 8 || !(i3 == 8 || i3 == 16 || i3 == 24 || i3 == 32)) {
                d0.b("PCM format");
                throw null;
            }
            this.a = aVar;
            this.b = i;
            this.c = i2;
            this.d = i3;
            this.e = bVar;
            this.f = fVar;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof e)) {
                return false;
            }
            e eVar = (e) obj;
            return this.b == eVar.b && this.c == eVar.c && this.d == eVar.d && Objects.equals(this.a, eVar.a) && Objects.equals(this.e, eVar.e) && Objects.equals(this.f, eVar.f);
        }

        public int g() {
            return this.d;
        }

        public int h() {
            return (this.d / 8) * this.c;
        }

        public final int hashCode() {
            int i = this.b;
            int i2 = this.c;
            int i3 = this.d;
            a aVar = this.a;
            b bVar = this.e;
            f fVar = this.f;
            return Objects.hashCode(fVar) + ((Objects.hashCode(bVar) + ((Objects.hashCode(aVar) + (((((i * 31) + i2) * 31) + i3) * 31)) * 31)) * 31);
        }

        public int i() {
            return this.c;
        }

        public f j() {
            return this.f;
        }

        public b k() {
            return this.e;
        }

        public void l() {
            f fVar = this.f;
            if (fVar == f.UNKNOWN || fVar == f.FLOAT || ((fVar == f.UNSIGNED_INTEGER && this.d != 8) || (fVar == f.SIGNED_INTEGER && this.d == 8))) {
                d0.d("PCM sample encoding unsupported/unverified");
            } else {
                if (this.d <= 8 || this.e != b.UNKNOWN) {
                    return;
                }
                d0.d("PCM byte order unverified");
            }
        }

        public int m() {
            return this.b;
        }

        public a n() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {this.a, Integer.valueOf(this.b), Integer.valueOf(this.c), Integer.valueOf(this.d), this.e, this.f};
            String[] strArrSplit = "a;b;c;d;e;f".length() == 0 ? new String[0] : "a;b;c;d;e;f".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(e.class, sb, "[");
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

        public e(a aVar, int i, int i2, int i3, b bVar) {
            this(aVar, i, i2, i3, bVar, f.UNKNOWN);
        }
    }
}
