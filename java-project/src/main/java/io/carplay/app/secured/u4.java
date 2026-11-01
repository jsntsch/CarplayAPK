package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public final class u4 {

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

    private u4() {
    }

    public static boolean a(int i, int i2, int i3, int i4) {
        if (i3 >= 8 && i4 >= 8 && Math.max(i3, i4) <= 1920 && (i3 & 1) == 0 && (i4 & 1) == 0) {
            p1.b bVarL = p1.l(i3, i4, 30);
            if (i == bVarL.i() && i2 == bVarL.h() && (i != i3 || i2 != i4)) {
                return true;
            }
        }
        return false;
    }

    public static a b(int i, int i2, int i3) {
        p1.b bVarL = p1.l(i, i2, i3);
        return Math.max(i, i2) > 1920 ? new a(bVarL.i(), bVarL.h(), bVarL.g()) : new a(i & (-2), i2 & (-2), bVarL.g());
    }
}
