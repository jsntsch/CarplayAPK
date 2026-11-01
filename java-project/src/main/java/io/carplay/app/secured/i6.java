package io.carplay.app.secured;

import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public final class i6 {
    private final int a;
    private final int b;
    private boolean c;

    public enum a {
        DOWN,
        MOVE,
        UP
    }

    public static final class b extends w1 {
        private final int a;
        private final byte[] b;

        public b(int i, byte[] bArr) {
            byte[] bArr2 = (byte[]) bArr.clone();
            this.a = i;
            this.b = bArr2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && Objects.equals(this.b, bVar.b);
        }

        public int g() {
            return this.a;
        }

        public byte[] h() {
            return (byte[]) this.b.clone();
        }

        public final int hashCode() {
            int i = this.a;
            return Objects.hashCode(this.b) + (i * 31);
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), this.b};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
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

    public i6(int i, int i2) {
        if (i < 1 || i2 < 1 || i > 8192 || i2 > 8192) {
            d0.b("touch dimensions");
            throw null;
        }
        this.a = i;
        this.b = i2;
    }

    public synchronized b a(a aVar, int i, int i2) {
        int i3;
        if (aVar != null && i >= 0 && i2 >= 0) {
            try {
                if (i < this.a && i2 < this.b) {
                    a aVar2 = a.DOWN;
                    if (aVar == aVar2 && this.c) {
                        throw new IllegalStateException("duplicate down");
                    }
                    if (aVar != aVar2 && !this.c) {
                        throw new IllegalStateException("no active gesture");
                    }
                    int iOrdinal = aVar.ordinal();
                    if (iOrdinal == 0) {
                        i3 = 425986;
                    } else if (iOrdinal == 1) {
                        i3 = 425988;
                    } else {
                        if (iOrdinal != 2) {
                            throw new IncompatibleClassChangeError();
                        }
                        i3 = 425987;
                    }
                    this.c = aVar != a.UP;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        throw new IllegalArgumentException("touch coordinates/action");
        return new b(i3, new ProtobufReader.a().c(1, i).c(2, i2).g());
    }

    public synchronized void b() {
        this.c = false;
    }
}
