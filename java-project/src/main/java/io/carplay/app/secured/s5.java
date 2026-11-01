package io.carplay.app.secured;

import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public final class s5 extends w1 {
    private final String a;
    private final String b;
    private final int c;
    private final String d;
    private final int e;
    private final int f;
    private final int g;
    private final String h;
    private final String i;
    private final String j;
    private final String k;

    public s5(String str, String str2, int i, String str3, int i2, int i3, int i4, String str4, String str5, String str6, String str7) {
        m(str);
        m(str2);
        m(str3);
        m(str4);
        m(str5);
        m(str6);
        m(str7);
        if (i < 0 || i2 < 0 || i3 < 0 || i4 < 0) {
            d0.b("invalid real receiver statistics");
            throw null;
        }
        this.a = str;
        this.b = str2;
        this.c = i;
        this.d = str3;
        this.e = i2;
        this.f = i3;
        this.g = i4;
        this.h = str4;
        this.i = str5;
        this.j = str6;
        this.k = str7;
    }

    private static void m(String str) {
        if (str == null || str.isBlank() || str.length() > 1024) {
            d0.b("real receiver metadata required");
        }
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof s5)) {
            return false;
        }
        s5 s5Var = (s5) obj;
        return this.c == s5Var.c && this.e == s5Var.e && this.f == s5Var.f && this.g == s5Var.g && Objects.equals(this.a, s5Var.a) && Objects.equals(this.b, s5Var.b) && Objects.equals(this.d, s5Var.d) && Objects.equals(this.h, s5Var.h) && Objects.equals(this.i, s5Var.i) && Objects.equals(this.j, s5Var.j) && Objects.equals(this.k, s5Var.k);
    }

    public String g() {
        return this.i;
    }

    public String h() {
        return this.d;
    }

    public final int hashCode() {
        int i = this.c;
        int i2 = this.e;
        int i3 = this.f;
        int i4 = this.g;
        String str = this.a;
        String str2 = this.b;
        String str3 = this.d;
        String str4 = this.h;
        String str5 = this.i;
        String str6 = this.j;
        String str7 = this.k;
        return Objects.hashCode(str7) + w1.b(str6, w1.b(str5, w1.b(str4, w1.b(str3, w1.b(str2, w1.b(str, ((((((i * 31) + i2) * 31) + i3) * 31) + i4) * 31, 31), 31), 31), 31), 31), 31);
    }

    public int i() {
        return this.e;
    }

    public int j() {
        return this.g;
    }

    public byte[] k() {
        return new ProtobufReader.a().e(1, this.h).e(4, this.i).e(14, this.j).e(20, this.k).g();
    }

    public String l() {
        return this.j;
    }

    public String n() {
        return this.h;
    }

    public String o() {
        return this.a;
    }

    public String p() {
        return this.k;
    }

    public byte[] q() {
        return new ProtobufReader.a().e(1, this.a).e(2, this.b).c(3, this.c).e(4, this.d).c(5, this.e).c(6, this.f).c(7, this.g).g();
    }

    public int r() {
        return this.f;
    }

    public int s() {
        return this.c;
    }

    public String t() {
        return this.b;
    }

    public final String toString() {
        Object[] objArr = {this.a, this.b, Integer.valueOf(this.c), this.d, Integer.valueOf(this.e), Integer.valueOf(this.f), Integer.valueOf(this.g), this.h, this.i, this.j, this.k};
        String[] strArrSplit = "a;b;c;d;e;f;g;h;i;j;k".length() == 0 ? new String[0] : "a;b;c;d;e;f;g;h;i;j;k".split(";");
        StringBuilder sb = new StringBuilder();
        w1.f(s5.class, sb, "[");
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
