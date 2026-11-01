package io.carplay.app.secured;

import java.security.GeneralSecurityException;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public final class x1 implements AutoCloseable {
    public static final int e = 131073;
    public static final int f = 131074;
    private final boolean a;
    private final o b;
    private final int c;
    private boolean d;

    public static final class a extends w1 {
        private final byte[] a;
        private final long b;

        public a(byte[] bArr, long j) {
            this.a = (byte[]) bArr.clone();
            this.b = j;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.b == aVar.b && Objects.equals(this.a, aVar.a);
        }

        public long g() {
            return this.b;
        }

        public byte[] h() {
            return (byte[]) this.a.clone();
        }

        public final int hashCode() {
            long j = this.b;
            byte[] bArr = this.a;
            return Objects.hashCode(bArr) + (Long.hashCode(j) * 31);
        }

        public final String toString() {
            Object[] objArr = {this.a, Long.valueOf(this.b)};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
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

    public x1(boolean z, o oVar, int i) {
        if (i < 1 || i > 8388608 || (z && oVar == null)) {
            d0.b("bounded video contract and explicit negotiated cipher required");
            throw null;
        }
        this.a = z;
        this.b = oVar;
        this.c = i;
    }

    public static r6.c c(long j) throws r5 {
        r6.b bVar = new r6.b(0, j, 0, f);
        r6.a aVar = r6.a.VIDEO;
        r6.d(aVar, bVar, 1);
        return new r6.c(aVar, bVar, new byte[0]);
    }

    public synchronized a b(r6.c cVar) {
        byte[] bArrG;
        long j;
        try {
            if (this.d) {
                throw new r5("closed video stream");
            }
            if (cVar != null) {
                try {
                    try {
                        if (cVar.h() == r6.a.VIDEO && cVar.i().h() == 131073) {
                            bArrG = cVar.g();
                            if (bArrG.length < 1 || bArrG.length > this.c || cVar.i().g() != bArrG.length) {
                                throw new r5("video wire payload length");
                            }
                            if (this.a) {
                                bArrG = this.b.b(bArrG);
                            }
                            if (bArrG == null || bArrG.length < 1 || bArrG.length > this.c) {
                                throw new r5("invalid decoded video payload");
                            }
                            j = cVar.i().j();
                            if (j < 0 || j > 4294967295L) {
                                throw new r5("video timestamp is not uint32");
                            }
                        }
                    } catch (RuntimeException | GeneralSecurityException e2) {
                        this.d = true;
                        throw new r5("video payload decode failed", e2);
                    }
                } catch (r5 e3) {
                    this.d = true;
                    throw e3;
                }
            }
            throw new r5("not a normal phone video payload");
        } catch (Throwable th) {
            throw th;
        }
        return new a(bArrG, j);
    }

    @Override // java.lang.AutoCloseable
    public synchronized void close() {
        this.d = true;
    }
}
