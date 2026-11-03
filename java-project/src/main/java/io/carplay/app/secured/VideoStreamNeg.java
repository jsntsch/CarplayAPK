package io.carplay.app.secured;

import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
/**
 * VideoStreamNeg
 *
 * <p>原混淆名：{@code io.carplay.app.secured.x1}（jadx 产物 x1.java）
 *
 * <p>命名依据：video payload decode failed / not a normal phone video payload / bounded video contract
 */
public final class VideoStreamNeg implements AutoCloseable {
    public static final int e = 131073;
    public static final int f = 131074;
    private final boolean a;
    private final o b;
    private final int c;
    private boolean d;

    public static final class a extends ProtoMessageBase {
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
    }

    public VideoStreamNeg(boolean z, o oVar, int i) {
        if (i < 1 || i > 8388608 || (z && oVar == null)) {
            SyntheticRefs.b("bounded video contract and explicit negotiated cipher required");
            throw null;
        }
        this.a = z;
        this.b = oVar;
        this.c = i;
    }

    public static CarLifeFrame.c c(long j) throws ProtocolIOException {
        CarLifeFrame.b bVar = new CarLifeFrame.b(0, j, 0, f);
        CarLifeFrame.a aVar = CarLifeFrame.a.VIDEO;
        CarLifeFrame.d(aVar, bVar, 1);
        return new CarLifeFrame.c(aVar, bVar, new byte[0]);
    }

    public synchronized a b(CarLifeFrame.c cVar) throws ProtocolIOException {
        if (this.d) {
            throw new ProtocolIOException("closed video stream");
        }
        if (cVar != null) {
            try {
                if (cVar.h() == CarLifeFrame.a.VIDEO && cVar.i().h() == 131073) {
                    byte[] bArrG = cVar.g();
                    if (bArrG.length < 1 || bArrG.length > this.c || cVar.i().g() != bArrG.length) {
                        throw new ProtocolIOException("video wire payload length");
                    }
                    if (this.a) {
                        bArrG = this.b.b(bArrG);
                    }
                    if (bArrG == null || bArrG.length < 1 || bArrG.length > this.c) {
                        throw new ProtocolIOException("invalid decoded video payload");
                    }
                    long j = cVar.i().j();
                    if (j < 0 || j > 4294967295L) {
                        throw new ProtocolIOException("video timestamp is not uint32");
                    }
                    return new a(bArrG, j);
                }
            } catch (RuntimeException e2) {
                this.d = true;
                throw new ProtocolIOException("video payload decode failed", e2);
            } catch (ProtocolIOException e3) {
                this.d = true;
                throw e3;
            }
        }
        throw new ProtocolIOException("not a normal phone video payload");
    }

    @Override // java.lang.AutoCloseable
    public synchronized void close() {
        this.d = true;
    }
}
