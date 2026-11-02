package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public final class q6 extends w1 {
    private final int a;
    private final int b;
    private final int c;
    private final int d;
    private final int e;
    private final int f;
    private final int g;
    private final int h;

    public static final class a extends w1 {
        private final int a;
        private final int b;

        public a(int i, int i2) {
            this.a = i;
            this.b = i2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.a == aVar.a && this.b == aVar.b;
        }

        public int g() {
            return this.a;
        }

        public int h() {
            return this.b;
        }

        public final int hashCode() {
            return (this.a * 31) + this.b;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b)};
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

    public q6(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        this.a = i;
        this.b = i2;
        this.c = i3;
        this.d = i4;
        this.e = i5;
        this.f = i6;
        this.g = i7;
        this.h = i8;
    }

    public static q6 i(int i, int i2, int i3, int i4) {
        int iMax;
        int iMax2;
        if (i < 1 || i2 < 1 || i3 < 1 || i4 < 1 || i > 8192 || i2 > 8192 || i3 > 8192 || i4 > 8192) {
            SyntheticRefs.b("video geometry bounds");
            return null;
        }
        long j = i;
        long j2 = ((long) i4) * j;
        long j3 = i2;
        long j4 = ((long) i3) * j3;
        if (j2 > j4) {
            iMax2 = i3;
            iMax = Math.max(1, (int) (j4 / j));
        } else {
            iMax = i4;
            iMax2 = Math.max(1, (int) (j2 / j3));
        }
        return new q6(i, i2, i3, i4, (i3 - iMax2) / 2, (i4 - iMax) / 2, iMax2, iMax);
    }

    private static int l(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        if (i2 < i3 && i >= i2 && i <= i3) {
            long j = i2;
            long j2 = ((long) i3) - j;
            long jMin = Math.min((((long) i) - j) * ((long) i4), ((long) (i4 - 1)) * j2) - (((long) i5) * j2);
            if (jMin >= 0) {
                long j3 = ((long) i6) * j2;
                if (jMin < j3) {
                    return Math.min(i7 - 1, (int) ((jMin * ((long) i7)) / j3));
                }
            }
        }
        return -1;
    }

    public static q6 r(int i, int i2, int i3, int i4) {
        q6 q6VarI = i(i, i2, i3, i4);
        return new q6(q6VarI.a, q6VarI.b, i3, i4, 0, 0, i3, i4);
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof q6)) {
            return false;
        }
        q6 q6Var = (q6) obj;
        return this.a == q6Var.a && this.b == q6Var.b && this.c == q6Var.c && this.d == q6Var.d && this.e == q6Var.e && this.f == q6Var.f && this.g == q6Var.g && this.h == q6Var.h;
    }

    public int g() {
        return this.d;
    }

    public int h() {
        return this.c;
    }

    public final int hashCode() {
        int i = this.a;
        int i2 = this.b;
        int i3 = this.c;
        int i4 = this.d;
        int i5 = this.e;
        int i6 = this.f;
        return (((((((((((((i * 31) + i2) * 31) + i3) * 31) + i4) * 31) + i5) * 31) + i6) * 31) + this.g) * 31) + this.h;
    }

    public int j() {
        return this.h;
    }

    public int k() {
        return this.e;
    }

    public a m(int i, int i2, int i3, int i4) {
        int i5;
        if (i >= 0 && i2 >= 0 && i <= i3 && i2 <= i4 && i3 >= 1 && i4 >= 1) {
            int i6 = this.c;
            int iMin = Math.min(i6 - 1, (int) ((((long) i) * ((long) i6)) / ((long) i3)));
            int i7 = this.d;
            int iMin2 = Math.min(i7 - 1, (int) ((((long) i2) * ((long) i7)) / ((long) i4)));
            int i8 = this.e;
            if (iMin >= i8 && iMin2 >= (i5 = this.f)) {
                int i9 = this.g;
                if (iMin < i8 + i9 && iMin2 < i5 + this.h) {
                    int i10 = this.a;
                    int iMin3 = Math.min(i10 - 1, (int) ((((long) (iMin - i8)) * ((long) i10)) / ((long) i9)));
                    int i11 = this.b;
                    return new a(iMin3, Math.min(i11 - 1, (int) ((((long) (iMin2 - this.f)) * ((long) i11)) / ((long) this.h))));
                }
            }
        }
        return null;
    }

    public a n(int i, int i2, int i3, int i4, int i5, int i6) {
        int iL = l(i, i3, i4, this.c, this.e, this.g, this.a);
        int iL2 = l(i2, i5, i6, this.d, this.f, this.h, this.b);
        if (iL < 0 || iL2 < 0) {
            return null;
        }
        return new a(iL, iL2);
    }

    public boolean o() {
        int i;
        int i2;
        int i3 = this.a;
        int i4 = this.c;
        return i3 > i4 || (i = this.b) > (i2 = this.d) || Math.abs(((((double) i3) / ((double) i)) / (((double) i4) / ((double) i2))) - 1.0d) > 0.01d;
    }

    public int p() {
        return this.b;
    }

    public int q() {
        return this.a;
    }

    public int s() {
        return this.f;
    }

    public int t() {
        return this.g;
    }

    public final String toString() {
        Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c), Integer.valueOf(this.d), Integer.valueOf(this.e), Integer.valueOf(this.f), Integer.valueOf(this.g), Integer.valueOf(this.h)};
        String[] strArrSplit = "a;b;c;d;e;f;g;h".length() == 0 ? new String[0] : "a;b;c;d;e;f;g;h".split(";");
        StringBuilder sb = new StringBuilder();
        w1.f(q6.class, sb, "[");
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
