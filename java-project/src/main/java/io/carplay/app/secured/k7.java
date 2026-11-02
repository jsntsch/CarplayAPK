package io.carplay.app.secured;

import java.security.SecureRandom;

/* JADX INFO: loaded from: classes.dex */
public abstract class k7 {

    public static class a {
        private static final a a = new a();

        private a() {
        }
    }

    public static boolean a(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3) {
        g(bArr, i, bArr2, i2, bArr3, i3);
        return !e.a(bArr3, i3, 32);
    }

    public static void b(byte[] bArr) {
        if (bArr.length != 32) {
            d0.b("k");
            return;
        }
        bArr[0] = (byte) (bArr[0] & 248);
        byte b = (byte) (bArr[31] & 127);
        bArr[31] = b;
        bArr[31] = (byte) (b | 64);
    }

    private static void c(byte[] bArr, int i, int[] iArr) {
        for (int i2 = 0; i2 < 8; i2++) {
            iArr[i2] = l7.s(bArr, (i2 * 4) + i);
        }
        iArr[0] = iArr[0] & (-8);
        int i3 = iArr[7] & Integer.MAX_VALUE;
        iArr[7] = i3;
        iArr[7] = i3 | 1073741824;
    }

    public static void d(SecureRandom secureRandom, byte[] bArr) {
        if (bArr.length != 32) {
            d0.b("k");
        } else {
            secureRandom.nextBytes(bArr);
            b(bArr);
        }
    }

    public static void e(byte[] bArr, int i, byte[] bArr2, int i2) {
        h(bArr, i, bArr2, i2);
    }

    private static void f(int[] iArr, int[] iArr2) {
        int[] iArrJ = l7.j();
        int[] iArrJ2 = l7.j();
        l7.c(iArr, iArr2, iArrJ, iArrJ2);
        l7.L(iArrJ, iArrJ);
        l7.L(iArrJ2, iArrJ2);
        l7.E(iArrJ, iArrJ2, iArr);
        l7.N(iArrJ, iArrJ2, iArrJ);
        l7.D(iArrJ, 121666, iArr2);
        l7.a(iArr2, iArrJ2, iArr2);
        l7.E(iArr2, iArrJ, iArr2);
    }

    public static void g(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3) {
        int[] iArr = new int[8];
        c(bArr, i, iArr);
        int[] iArrJ = l7.j();
        l7.o(bArr2, i2, iArrJ, 0);
        int[] iArrJ2 = l7.j();
        l7.i(iArrJ, 0, iArrJ2, 0);
        int[] iArrJ3 = l7.j();
        iArrJ3[0] = 1;
        int[] iArrJ4 = l7.j();
        iArrJ4[0] = 1;
        int[] iArrJ5 = l7.j();
        int[] iArrJ6 = l7.j();
        int[] iArrJ7 = l7.j();
        int i4 = 254;
        int i5 = 1;
        while (true) {
            l7.c(iArrJ4, iArrJ5, iArrJ6, iArrJ4);
            l7.c(iArrJ2, iArrJ3, iArrJ5, iArrJ2);
            l7.E(iArrJ6, iArrJ2, iArrJ6);
            l7.E(iArrJ4, iArrJ5, iArrJ4);
            l7.L(iArrJ5, iArrJ5);
            l7.L(iArrJ2, iArrJ2);
            l7.N(iArrJ5, iArrJ2, iArrJ7);
            l7.D(iArrJ7, 121666, iArrJ3);
            l7.a(iArrJ3, iArrJ2, iArrJ3);
            l7.E(iArrJ3, iArrJ7, iArrJ3);
            l7.E(iArrJ2, iArrJ5, iArrJ2);
            l7.c(iArrJ6, iArrJ4, iArrJ4, iArrJ5);
            l7.L(iArrJ4, iArrJ4);
            l7.L(iArrJ5, iArrJ5);
            l7.E(iArrJ5, iArrJ, iArrJ5);
            i4--;
            int i6 = (iArr[i4 >>> 5] >>> (i4 & 31)) & 1;
            int i7 = i5 ^ i6;
            l7.l(i7, iArrJ2, iArrJ4);
            l7.l(i7, iArrJ3, iArrJ5);
            if (i4 < 3) {
                break;
            } else {
                i5 = i6;
            }
        }
        for (int i8 = 0; i8 < 3; i8++) {
            f(iArrJ2, iArrJ3);
        }
        l7.z(iArrJ3, iArrJ3);
        l7.E(iArrJ2, iArrJ3, iArrJ2);
        l7.G(iArrJ2);
        l7.u(iArrJ2, bArr3, i3);
    }

    public static void h(byte[] bArr, int i, byte[] bArr2, int i2) {
        int[] iArrJ = l7.j();
        int[] iArrJ2 = l7.j();
        X25519.N(a.a, bArr, i, iArrJ, iArrJ2);
        l7.c(iArrJ2, iArrJ, iArrJ, iArrJ2);
        l7.z(iArrJ2, iArrJ2);
        l7.E(iArrJ, iArrJ2, iArrJ);
        l7.G(iArrJ);
        l7.u(iArrJ, bArr2, i2);
    }
}
