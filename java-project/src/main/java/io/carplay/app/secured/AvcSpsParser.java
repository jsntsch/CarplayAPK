package io.carplay.app.secured;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
/**
 * AvcSpsParser
 *
 * <p>原混淆名：{@code io.carplay.app.secured.j}（jadx 产物 j.java）
 *
 * <p>命名依据：not_sps / sps_metadata / unsupported_profile / unsupported_bit_depth
 */
public final class AvcSpsParser {
    private static final int a = 4096;
    private static final int b = 8192;

    public static final class a {
        final byte[] a;
        int b;

        public a(byte[] bArr) {
            this.a = bArr;
        }

        public boolean a() throws c {
            return b(1) != 0;
        }

        public int b(int i) throws c {
            if (this.b > (this.a.length * 8) - i) {
                throw new c("TRUNCATED");
            }
            int i2 = 0;
            for (int i3 = 0; i3 < i; i3++) {
                byte[] bArr = this.a;
                int i4 = this.b;
                byte b = bArr[i4 / 8];
                this.b = i4 + 1;
                i2 = (i2 << 1) | ((b >>> (7 - (i4 % 8))) & 1);
            }
            return i2;
        }

        public int c() throws c {
            int iD = d();
            return (iD & 1) == 0 ? (-iD) / 2 : (int) ((((long) iD) + 1) / 2);
        }

        public int d() throws c {
            int i = 0;
            while (!a()) {
                i++;
                if (i > 30) {
                    throw new c("LIMIT");
                }
            }
            long jB = ((1 << i) - 1) + ((long) b(i));
            if (jB <= 2147483647L) {
                return (int) jB;
            }
            throw new c("LIMIT");
        }
    }

    public static final class b extends ProtoMessageBase {
        private final int a;
        private final int b;
        private final int c;
        private final int d;
        private final int e;
        private final int f;
        private final int g;
        private final String h;

        public b(int i, int i2, int i3, int i4, int i5, int i6, int i7, String str) {
            this.a = i;
            this.b = i2;
            this.c = i3;
            this.d = i4;
            this.e = i5;
            this.f = i6;
            this.g = i7;
            this.h = str;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && this.b == bVar.b && this.c == bVar.c && this.d == bVar.d && this.e == bVar.e && this.f == bVar.f && this.g == bVar.g && Objects.equals(this.h, bVar.h);
        }

        public final int hashCode() {
            int i = this.a;
            int i2 = this.b;
            int i3 = this.c;
            int i4 = this.d;
            int i5 = this.e;
            int i6 = this.f;
            int i7 = this.g;
            return Objects.hashCode(this.h) + (((((((((((((i * 31) + i2) * 31) + i3) * 31) + i4) * 31) + i5) * 31) + i6) * 31) + i7) * 31);
        }

        public int o() {
            return this.e;
        }

        public int p() {
            return this.d;
        }

        public int q() {
            return this.b;
        }

        public boolean r() {
            return this.d > 0 && this.e > 0 && this.f > 0 && this.g > 0;
        }

        public int s() {
            return this.c;
        }

        public int t() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c), Integer.valueOf(this.d), Integer.valueOf(this.e), Integer.valueOf(this.f), Integer.valueOf(this.g), this.h};
            String[] strArrSplit = "a;b;c;d;e;f;g;h".length() == 0 ? new String[0] : "a;b;c;d;e;f;g;h".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(b.class, sb, "[");
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

        public String u() {
            return this.h;
        }

        public int v() {
            return this.g;
        }

        public int w() {
            return this.f;
        }
    }

    public static final class c extends Exception {
        private static final long b = 1;
        final String a;

        public c(String str) {
            this.a = str;
        }
    }

    private AvcSpsParser() {
    }

    public static String a(byte[] bArr, int i, int i2) {
        String str;
        String str2;
        b bVarC = c(bArr);
        boolean zR = bVarC.r();
        int i3 = bVarC.a;
        int i4 = bVarC.b;
        int i5 = bVarC.c;
        String string = "UNKNOWN";
        if (zR) {
            str = bVarC.d + "x" + bVarC.e;
        } else {
            str = "UNKNOWN";
        }
        if (zR) {
            str2 = bVarC.f + "x" + bVarC.g;
        } else {
            str2 = "UNKNOWN";
        }
        if (zR) {
            string = Boolean.toString(bVarC.f == i && bVarC.g == i2);
        }
        return "profile_idc=" + i3 + " constraints=" + i4 + " level_idc=" + i5 + " coded=" + str + " visible=" + str2 + " declared=" + i + "x" + i2 + " dimensions_match=" + string + " sps_metadata=" + bVarC.h;
    }

    private static boolean b(int i) {
        return i == 44 || i == 83 || i == 86 || i == 100 || i == 110 || i == 118 || i == 122 || i == 128 || i == 244 || i == 134 || i == 135 || i == 138 || i == 139;
    }

    public static b c(byte[] bArr) {
        int i;
        int i2;
        int i3;
        int iD;
        boolean zA;
        int iD2;
        int iD3;
        int iD4;
        int iD5;
        if (bArr != null) {
            int i4 = 1;
            if (bArr.length >= 1 && (bArr[0] & 31) == 7) {
                if (bArr.length >= 4) {
                    i = bArr[1] & 255;
                    i2 = bArr[2] & 255;
                    i3 = bArr[3] & 255;
                } else {
                    i = -1;
                    i2 = -1;
                    i3 = -1;
                }
                if (bArr.length < 4) {
                    return f(i, i2, i3, "TRUNCATED");
                }
                if (bArr.length > a) {
                    return f(i, i2, i3, "LIMIT");
                }
                try {
                    a aVar = new a(d(bArr));
                    aVar.b(8);
                    aVar.b(8);
                    aVar.b(8);
                    if (aVar.d() > 31) {
                        throw new c("LIMIT");
                    }
                    if (b(i)) {
                        iD = aVar.d();
                        if (iD > 3) {
                            throw new c("UNSUPPORTED_CHROMA");
                        }
                        zA = iD == 3 ? aVar.a() : false;
                        if (aVar.d() > 6 || aVar.d() > 6) {
                            throw new c("UNSUPPORTED_BIT_DEPTH");
                        }
                        aVar.a();
                        if (aVar.a()) {
                            int i5 = 0;
                            while (true) {
                                if (i5 >= (iD == 3 ? 12 : 8)) {
                                    break;
                                }
                                if (aVar.a()) {
                                    e(aVar, i5 < 6 ? 16 : 64);
                                }
                                i5++;
                            }
                        }
                    } else {
                        if (i != 66 && i != 77 && i != 88) {
                            throw new c("UNSUPPORTED_PROFILE");
                        }
                        iD = 1;
                        zA = false;
                    }
                    if (aVar.d() > 12) {
                        throw new c("LIMIT");
                    }
                    int iD6 = aVar.d();
                    if (iD6 == 0) {
                        if (aVar.d() > 12) {
                            throw new c("LIMIT");
                        }
                    } else if (iD6 == 1) {
                        aVar.a();
                        aVar.c();
                        aVar.c();
                        int iD7 = aVar.d();
                        if (iD7 > 255) {
                            throw new c("LIMIT");
                        }
                        for (int i6 = 0; i6 < iD7; i6++) {
                            aVar.c();
                        }
                    } else if (iD6 != 2) {
                        throw new c("UNSUPPORTED_ORDER");
                    }
                    aVar.d();
                    aVar.a();
                    int iD8 = aVar.d();
                    int iD9 = aVar.d();
                    if (iD8 >= 512 || iD9 >= 512) {
                        throw new c("LIMIT");
                    }
                    boolean zA2 = aVar.a();
                    if (!zA2) {
                        aVar.a();
                    }
                    aVar.a();
                    if (aVar.a()) {
                        iD3 = aVar.d();
                        iD4 = aVar.d();
                        iD5 = aVar.d();
                        iD2 = aVar.d();
                    } else {
                        iD2 = 0;
                        iD3 = 0;
                        iD4 = 0;
                        iD5 = 0;
                    }
                    if (zA) {
                        iD = 0;
                    }
                    int i7 = (iD == 0 || iD == 3) ? 1 : 2;
                    int i8 = (iD == 1 ? 2 : 1) * (zA2 ? 1 : 2);
                    int i9 = (iD8 + 1) * 16;
                    int i10 = (iD9 + 1) * 16;
                    if (!zA2) {
                        i4 = 2;
                    }
                    int i11 = i10 * i4;
                    long j = ((long) i9) - ((((long) iD3) + ((long) iD4)) * ((long) i7));
                    long j2 = ((long) i11) - ((((long) iD5) + ((long) iD2)) * ((long) i8));
                    if (i11 > 8192 || j < 1 || j2 < 1) {
                        throw new c("INVALID_CROP");
                    }
                    return new b(i, i2, i3, i9, i11, (int) j, (int) j2, "PARSED");
                } catch (c e) {
                    return f(i, i2, i3, e.a);
                }
            }
        }
        return f(-1, -1, -1, "NOT_SPS");
    }

    private static byte[] d(byte[] bArr) throws c {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bArr.length - 1);
        int i = 0;
        for (int i2 = 1; i2 < bArr.length; i2++) {
            int i3 = bArr[i2] & 255;
            if (i < 2 || i3 != 3) {
                byteArrayOutputStream.write(i3);
                i = i3 == 0 ? i + 1 : 0;
            } else {
                int i4 = i2 + 1;
                if (i4 >= bArr.length || (bArr[i4] & 255) > 3) {
                    throw new c("INVALID_ESCAPE");
                }
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static void e(a aVar, int i) throws c {
        int i2 = 8;
        int i3 = 8;
        for (int i4 = 0; i4 < i; i4++) {
            if (i2 != 0) {
                int iC = aVar.c();
                if (iC < -128 || iC > 127) {
                    throw new c("LIMIT");
                }
                i2 = ((iC + i3) + 256) % 256;
            }
            if (i2 != 0) {
                i3 = i2;
            }
        }
    }

    private static b f(int i, int i2, int i3, String str) {
        return new b(i, i2, i3, -1, -1, -1, -1, str);
    }
}
