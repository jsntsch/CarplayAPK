package io.carplay.app.secured;

import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public interface j6 {

    public static final class a extends ProtoMessageBase {
        private final boolean a;
        private final boolean b;
        private final boolean c;
        private final boolean d;
        private final boolean e;
        private final boolean f;
        private final boolean g;

        public a(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7) {
            this.a = z;
            this.b = z2;
            this.c = z3;
            this.d = z4;
            this.e = z5;
            this.f = z6;
            this.g = z7;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.a == aVar.a && this.b == aVar.b && this.c == aVar.c && this.d == aVar.d && this.e == aVar.e && this.f == aVar.f && this.g == aVar.g;
        }

        public boolean g() {
            return this.a;
        }

        public boolean h() {
            return this.c;
        }

        public final int hashCode() {
            boolean z = this.a;
            boolean z2 = this.b;
            boolean z3 = this.c;
            boolean z4 = this.d;
            boolean z5 = this.e;
            boolean z6 = this.f;
            boolean z7 = this.g;
            return Boolean.hashCode(z7) + ((Boolean.hashCode(z6) + ((Boolean.hashCode(z5) + ((Boolean.hashCode(z4) + ((Boolean.hashCode(z3) + ((Boolean.hashCode(z2) + (Boolean.hashCode(z) * 31)) * 31)) * 31)) * 31)) * 31)) * 31);
        }

        public boolean i() {
            return this.g;
        }

        public boolean j() {
            return this.d;
        }

        public boolean k() {
            return this.f;
        }

        public boolean l() {
            return this.b;
        }

        public boolean m() {
            return this.e;
        }

        public final String toString() {
            Object[] objArr = {Boolean.valueOf(this.a), Boolean.valueOf(this.b), Boolean.valueOf(this.c), Boolean.valueOf(this.d), Boolean.valueOf(this.e), Boolean.valueOf(this.f), Boolean.valueOf(this.g)};
            String[] strArrSplit = "a;b;c;d;e;f;g".length() == 0 ? new String[0] : "a;b;c;d;e;f;g".split(";");
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

    public enum b {
        WRITTEN,
        OBSERVED,
        UNCONFIRMED
    }

    default boolean b(boolean z) {
        return false;
    }

    default boolean c(RemoteCommandHandler.d dVar) {
        return false;
    }

    boolean e() throws IOException;

    boolean f() throws IOException;

    boolean g() throws IOException;

    boolean i() throws IOException;

    a k();

    boolean l(int i);
}
