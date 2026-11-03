package io.carplay.app.secured;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public final class r6 {

    public enum a {
        CMD(7240, 8),
        VIDEO(8240, 12),
        MEDIA(9240, 12),
        TTS(9241, 12),
        VR(9242, 12),
        TOUCH(9340, 8),
        DATA(9440, 8);

        private final int a;
        private final int b;

        a(int i, int i2) {
            this.a = i;
            this.b = i2;
        }

        public int b() {
            return this.b;
        }

        public int c() {
            return this.a;
        }

        public boolean d() {
            return this.b == 8;
        }
    }

    public static final class b extends w1 {
        private final int a;
        private final long b;
        private final int c;
        private final int d;

        public b(int i, long j, int i2, int i3) {
            this.a = i;
            this.b = j;
            this.c = i2;
            this.d = i3;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && this.c == bVar.c && this.d == bVar.d && this.b == bVar.b;
        }

        public int g() {
            return this.a;
        }

        public int h() {
            return this.d;
        }

        public final int hashCode() {
            int i = this.a;
            int i2 = this.c;
            int i3 = this.d;
            return Long.hashCode(this.b) + (((((i * 31) + i2) * 31) + i3) * 31);
        }

        public int i() {
            return this.c;
        }

        public long j() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Long.valueOf(this.b), Integer.valueOf(this.c), Integer.valueOf(this.d)};
            String[] strArrSplit = "a;b;c;d".length() == 0 ? new String[0] : "a;b;c;d".split(";");
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

    public static final class c extends w1 {
        private final a a;
        private final b b;
        private final byte[] c;

        public c(a aVar, b bVar, byte[] bArr) {
            Objects.requireNonNull(aVar);
            Objects.requireNonNull(bVar);
            Objects.requireNonNull(bArr);
            byte[] bArr2 = (byte[]) bArr.clone();
            if (bArr2.length != bVar.g()) {
                SyntheticRefs.b("body length mismatch");
                throw null;
            }
            this.a = aVar;
            this.b = bVar;
            this.c = bArr2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof c)) {
                return false;
            }
            c cVar = (c) obj;
            return Objects.equals(this.a, cVar.a) && Objects.equals(this.b, cVar.b) && Objects.equals(this.c, cVar.c);
        }

        public byte[] g() {
            return (byte[]) this.c.clone();
        }

        public a h() {
            return this.a;
        }

        public final int hashCode() {
            a aVar = this.a;
            b bVar = this.b;
            byte[] bArr = this.c;
            return Objects.hashCode(bArr) + ((Objects.hashCode(bVar) + (Objects.hashCode(aVar) * 31)) * 31);
        }

        public b i() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, this.c};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
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

    private r6() {
    }

    private static void a(a aVar, long j, int i) throws r5 {
        if (j < 0 || j > i || j > 2147483647L || (aVar.d() && j >= 32768)) {
            throw new r5("body length exceeds channel limit: " + j);
        }
    }

    private static void b(a aVar, int i) {
        if (i >= 0) {
            return;
        }
        SyntheticRefs.b("negative body limit");
    }

    public static b c(a aVar, byte[] bArr, int i) throws r5 {
        Objects.requireNonNull(aVar);
        Objects.requireNonNull(bArr);
        b(aVar, i);
        if (bArr.length != aVar.b()) {
            SyntheticRefs.f("wrong header size");
            return null;
        }
        long j = aVar.d() ? j(bArr, 0) : k(bArr, 0);
        a(aVar, j, i);
        return new b((int) j, aVar.d() ? 0L : k(bArr, 4), aVar.d() ? j(bArr, 2) : 0, (int) k(bArr, aVar.d() ? 4 : 8));
    }

    public static byte[] d(a aVar, b bVar, int i) throws r5 {
        Objects.requireNonNull(aVar);
        Objects.requireNonNull(bVar);
        b(aVar, i);
        a(aVar, bVar.g(), i);
        if (bVar.i() < 0 || bVar.i() > 65535) {
            SyntheticRefs.f("reserved outside uint16");
            return null;
        }
        if (bVar.j() < 0 || bVar.j() > 4294967295L) {
            SyntheticRefs.f("timestamp outside uint32");
            return null;
        }
        byte[] bArr = new byte[aVar.b()];
        if (aVar.d()) {
            if (bVar.j() != 0) {
                SyntheticRefs.f("8-byte header has no timestamp");
                return null;
            }
            e(bArr, 0, bVar.g());
            e(bArr, 2, bVar.i());
            f(bArr, 4, bVar.h());
            return bArr;
        }
        if (bVar.i() != 0) {
            SyntheticRefs.f("12-byte header has no reserved field");
            return null;
        }
        f(bArr, 0, bVar.g());
        f(bArr, 4, (int) bVar.j());
        f(bArr, 8, bVar.h());
        return bArr;
    }

    private static void e(byte[] bArr, int i, int i2) {
        bArr[i] = (byte) (i2 >>> 8);
        bArr[i + 1] = (byte) i2;
    }

    private static void f(byte[] bArr, int i, int i2) {
        bArr[i] = (byte) (i2 >>> 24);
        bArr[i + 1] = (byte) (i2 >>> 16);
        bArr[i + 2] = (byte) (i2 >>> 8);
        bArr[i + 3] = (byte) i2;
    }

    public static void g(InputStream inputStream, byte[] bArr, int i, int i2) throws IOException {
        Objects.requireNonNull(inputStream);
        Objects.checkFromIndexSize(i, i2, bArr.length);
        int i3 = i2 + i;
        while (i < i3) {
            int i4 = inputStream.read(bArr, i, i3 - i);
            if (i4 < 0) {
                throw new EOFException("truncated CarLife frame");
            }
            if (i4 == 0) {
                int i5 = inputStream.read();
                if (i5 < 0) {
                    throw new EOFException("truncated CarLife frame");
                }
                bArr[i] = (byte) i5;
                i++;
            } else {
                i += i4;
            }
        }
    }

    public static byte[] h(InputStream inputStream, int i) throws IOException {
        if (i < 0) {
            SyntheticRefs.b("negative length");
            return null;
        }
        byte[] bArr = new byte[i];
        g(inputStream, bArr, 0, i);
        return bArr;
    }

    public static c i(InputStream inputStream, a aVar, int i) throws IOException {
        Objects.requireNonNull(inputStream);
        Objects.requireNonNull(aVar);
        b(aVar, i);
        int i2 = inputStream.read();
        if (i2 == -1) {
            return null;
        }
        int iB = aVar.b();
        byte[] bArr = new byte[iB];
        bArr[0] = (byte) i2;
        g(inputStream, bArr, 1, iB - 1);
        b bVarC = c(aVar, bArr, i);
        return new c(aVar, bVarC, h(inputStream, bVarC.g()));
    }

    private static int j(byte[] bArr, int i) {
        return (bArr[i + 1] & 255) | ((bArr[i] & 255) << 8);
    }

    private static long k(byte[] bArr, int i) {
        return (((long) bArr[i + 3]) & 255) | (((long) (bArr[i] & 255)) << 24) | (((long) (bArr[i + 1] & 255)) << 16) | (((long) (bArr[i + 2] & 255)) << 8);
    }

    public static void l(OutputStream outputStream, a aVar, b bVar, byte[] bArr, int i) throws IOException {
        Objects.requireNonNull(outputStream);
        Objects.requireNonNull(bArr);
        if (bArr.length != bVar.g()) {
            SyntheticRefs.f("body length mismatch");
        } else {
            outputStream.write(d(aVar, bVar, i));
            outputStream.write(bArr);
        }
    }
}
