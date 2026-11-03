package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public final class f4 {
    private volatile boolean a;
    private long b;
    private long c;
    private long d;
    private long e;

    public static final class a extends ProtoMessageBase {
        private final long a;

        public a(long j) {
            this.a = j;
        }

        public final boolean equals(Object obj) {
            return (obj instanceof a) && this.a == ((a) obj).a;
        }

        public long g() {
            return this.a;
        }

        public final int hashCode() {
            return Long.hashCode(this.a);
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a)};
            String[] strArrSplit = "a".length() == 0 ? new String[0] : "a".split(";");
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

    public synchronized void a(boolean z, long j, long j2) {
        this.a = z;
        this.c = j;
        this.d = j2;
        this.b = 0L;
        this.e++;
    }

    public boolean b() {
        return this.a;
    }

    public synchronized boolean c(a aVar) {
        if (this.a && this.b != 0 && aVar != null && aVar.g() == this.e) {
            this.a = false;
            return true;
        }
        return false;
    }

    /* JADX WARN: Code duplicated, block: B:13:0x001b  */
    public synchronized a d(long j) {
        a aVar;
        if (this.a) {
            long j2 = this.b;
            if (j2 == 0 || j <= j2) {
                aVar = null;
            } else {
                aVar = new a(this.e);
            }
        } else {
            aVar = null;
        }
        return aVar;
    }

    public synchronized void e(long j, long j2) {
        this.c = Math.max(this.c, j);
        this.d = Math.max(this.d, j2);
        if (this.a) {
            this.b = 0L;
            this.e++;
        }
    }

    public synchronized void f(long j, long j2, long j3) {
        if (this.a && j == this.c && j2 == this.d && this.b == 0 && j3 > 0) {
            this.b = j3;
        }
    }
}
