package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
abstract class c6 {
    private static final int[] a = {1559614445, 1477600026, -1560830762, 350157278, 0, 0, 0, 268435456};
    private static final int[] b = {-1424848535, -487721339, 580428573, 1745064566, -770181698, 1036971123, 461123738, -1582065343, 1268693629, -889041821, -731974758, 43769659, 0, 0, 0, 16777216};

    public static boolean a(byte[] bArr, int[] iArr) {
        b(bArr, iArr);
        return !m4.b(iArr, a);
    }

    public static void b(byte[] bArr, int[] iArr) {
        a2.d(bArr, 0, iArr, 0, 8);
    }

    public static void c(int[] iArr, int[] iArr2, int[] iArr3) {
        int[] iArr4 = new int[12];
        m4.d(iArr, iArr2, iArr4);
        if (iArr2[3] < 0) {
            m4.a(a, 0, iArr4, 4, 0);
            m4.g(iArr, 0, iArr4, 4, 0);
        }
        byte[] bArr = new byte[48];
        a2.g(iArr4, 0, 12, bArr, 0);
        b(d(bArr), iArr3);
    }

    public static byte[] d(byte[] bArr) {
        long jC = ((long) a2.c(bArr, 0)) & 4294967295L;
        long jB = ((long) (a2.b(bArr, 4) << 4)) & 4294967295L;
        long jC2 = ((long) a2.c(bArr, 7)) & 4294967295L;
        long jB2 = ((long) (a2.b(bArr, 11) << 4)) & 4294967295L;
        long jC3 = ((long) a2.c(bArr, 14)) & 4294967295L;
        long jB3 = ((long) (a2.b(bArr, 18) << 4)) & 4294967295L;
        long jC4 = ((long) a2.c(bArr, 21)) & 4294967295L;
        long jB4 = ((long) (a2.b(bArr, 25) << 4)) & 4294967295L;
        long jC5 = ((long) a2.c(bArr, 28)) & 4294967295L;
        long jB5 = a2.b(bArr, 32) << 4;
        long j = jB5 & 4294967295L;
        long jC6 = a2.c(bArr, 35);
        long j2 = jC6 & 4294967295L;
        long jB6 = a2.b(bArr, 39) << 4;
        long j3 = jB6 & 4294967295L;
        long jC7 = a2.c(bArr, 42);
        long jA = (((long) (a2.a(bArr, 46) << 4)) & 4294967295L) + ((jC7 & 4294967295L) >> 28);
        long j4 = jC5 - (jA * 5343);
        long j5 = (jC7 & 268435455) + (j3 >> 28);
        long j6 = (jC3 - (jA * (-50998291))) - (j5 * 19280294);
        long j7 = (jB3 - (jA * 19280294)) - (j5 * 127719000);
        long j8 = (jC4 - (jA * 127719000)) - (j5 * (-6428113));
        long j9 = (jB4 - (jA * (-6428113))) - (j5 * 5343);
        long j10 = (jB6 & 268435455) + (j2 >> 28);
        long j11 = (jB2 - (j5 * (-50998291))) - (j10 * 19280294);
        long j12 = j8 - (j10 * 5343);
        long j13 = (jC6 & 268435455) + (j >> 28);
        long j14 = jB - (j13 * (-50998291));
        long j15 = (jC2 - (j10 * (-50998291))) - (j13 * 19280294);
        long j16 = j11 - (j13 * 127719000);
        long j17 = (j6 - (j10 * 127719000)) - (j13 * (-6428113));
        long j18 = (j7 - (j10 * (-6428113))) - (j13 * 5343);
        long j19 = j4 + (j9 >> 28);
        long j20 = (jB5 & 268435455) + (j19 >> 28);
        long j21 = j19 & 268435455;
        long j22 = j21 >>> 27;
        long j23 = j20 + j22;
        long j24 = jC - (j23 * (-50998291));
        long j25 = j15 - (j23 * 127719000);
        long j26 = j16 - (j23 * (-6428113));
        long j27 = j17 - (j23 * 5343);
        long j28 = (j14 - (j23 * 19280294)) + (j24 >> 28);
        long j29 = j24 & 268435455;
        long j30 = j25 + (j28 >> 28);
        long j31 = j26 + (j30 >> 28);
        long j32 = j27 + (j31 >> 28);
        long j33 = j18 + (j32 >> 28);
        long j34 = j12 + (j33 >> 28);
        long j35 = (j9 & 268435455) + (j34 >> 28);
        long j36 = j21 + (j35 >> 28);
        long j37 = (j36 >> 28) - j22;
        long j38 = j29 + (j37 & (-50998291));
        long j39 = (j28 & 268435455) + (j37 & 19280294) + (j38 >> 28);
        long j40 = (j30 & 268435455) + (j37 & 127719000) + (j39 >> 28);
        long j41 = (j31 & 268435455) + (j37 & (-6428113)) + (j40 >> 28);
        long j42 = (j32 & 268435455) + (j37 & 5343) + (j41 >> 28);
        long j43 = (j33 & 268435455) + (j42 >> 28);
        long j44 = (j34 & 268435455) + (j43 >> 28);
        long j45 = (j35 & 268435455) + (j44 >> 28);
        long j46 = (j36 & 268435455) + (j45 >> 28);
        long j47 = 268435455 & j45;
        byte[] bArr2 = new byte[64];
        a2.h((j38 & 268435455) | ((j39 & 268435455) << 28), bArr2, 0);
        a2.h((j40 & 268435455) | ((j41 & 268435455) << 28), bArr2, 7);
        a2.h(((j43 & 268435455) << 28) | (j42 & 268435455), bArr2, 14);
        a2.h((j47 << 28) | (j44 & 268435455), bArr2, 21);
        a2.f((int) j46, bArr2, 28);
        return bArr2;
    }

    public static byte[] e(byte[] bArr) {
        long jC = ((long) a2.c(bArr, 0)) & 4294967295L;
        long jB = ((long) (a2.b(bArr, 4) << 4)) & 4294967295L;
        long jC2 = ((long) a2.c(bArr, 7)) & 4294967295L;
        long jB2 = ((long) (a2.b(bArr, 11) << 4)) & 4294967295L;
        long jC3 = ((long) a2.c(bArr, 14)) & 4294967295L;
        long jB3 = ((long) (a2.b(bArr, 18) << 4)) & 4294967295L;
        long jC4 = ((long) a2.c(bArr, 21)) & 4294967295L;
        long jB4 = ((long) (a2.b(bArr, 25) << 4)) & 4294967295L;
        long jC5 = ((long) a2.c(bArr, 28)) & 4294967295L;
        long jB5 = ((long) (a2.b(bArr, 32) << 4)) & 4294967295L;
        long jC6 = ((long) a2.c(bArr, 35)) & 4294967295L;
        long jB6 = ((long) (a2.b(bArr, 39) << 4)) & 4294967295L;
        long jC7 = ((long) a2.c(bArr, 42)) & 4294967295L;
        long jB7 = ((long) (a2.b(bArr, 46) << 4)) & 4294967295L;
        long jC8 = a2.c(bArr, 49);
        long j = jC8 & 4294967295L;
        long jB8 = ((long) (a2.b(bArr, 53) << 4)) & 4294967295L;
        long jC9 = a2.c(bArr, 56);
        long jB9 = ((long) (a2.b(bArr, 60) << 4)) & 4294967295L;
        long j2 = ((long) bArr[63]) & 255;
        long j3 = jB9 + ((jC9 & 4294967295L) >> 28);
        long j4 = jC9 & 268435455;
        long j5 = (jC7 - (j2 * (-6428113))) - (j3 * 5343);
        long j6 = (jC5 - (j3 * (-50998291))) - (j4 * 19280294);
        long j7 = ((jB5 - (j2 * (-50998291))) - (j3 * 19280294)) - (j4 * 127719000);
        long j8 = ((jC6 - (j2 * 19280294)) - (j3 * 127719000)) - (j4 * (-6428113));
        long j9 = ((jB6 - (j2 * 127719000)) - (j3 * (-6428113))) - (j4 * 5343);
        long j10 = jB8 + (j >> 28);
        long j11 = jC8 & 268435455;
        long j12 = j8 - (j10 * 5343);
        long j13 = (j7 - (j10 * (-6428113))) - (j11 * 5343);
        long j14 = (jB7 - (j2 * 5343)) + (j5 >> 28);
        long j15 = ((jC4 - (j10 * (-50998291))) - (j11 * 19280294)) - (j14 * 127719000);
        long j16 = (((jB4 - (j4 * (-50998291))) - (j10 * 19280294)) - (j11 * 127719000)) - (j14 * (-6428113));
        long j17 = ((j6 - (j10 * 127719000)) - (j11 * (-6428113))) - (j14 * 5343);
        long j18 = (j5 & 268435455) + (j9 >> 28);
        long j19 = (jC3 - (j14 * (-50998291))) - (j18 * 19280294);
        long j20 = ((jB3 - (j11 * (-50998291))) - (j14 * 19280294)) - (j18 * 127719000);
        long j21 = j16 - (j18 * 5343);
        long j22 = (j9 & 268435455) + (j12 >> 28);
        long j23 = (jB2 - (j18 * (-50998291))) - (j22 * 19280294);
        long j24 = (j15 - (j18 * (-6428113))) - (j22 * 5343);
        long j25 = (j12 & 268435455) + (j13 >> 28);
        long j26 = jB - (j25 * (-50998291));
        long j27 = (jC2 - (j22 * (-50998291))) - (j25 * 19280294);
        long j28 = j23 - (j25 * 127719000);
        long j29 = (j19 - (j22 * 127719000)) - (j25 * (-6428113));
        long j30 = (j20 - (j22 * (-6428113))) - (j25 * 5343);
        long j31 = j17 + (j21 >> 28);
        long j32 = j21 & 268435455;
        long j33 = (j13 & 268435455) + (j31 >> 28);
        long j34 = j31 & 268435455;
        long j35 = j34 >>> 27;
        long j36 = j33 + j35;
        long j37 = jC - (j36 * (-50998291));
        long j38 = j27 - (j36 * 127719000);
        long j39 = j28 - (j36 * (-6428113));
        long j40 = j29 - (j36 * 5343);
        long j41 = (j26 - (j36 * 19280294)) + (j37 >> 28);
        long j42 = j37 & 268435455;
        long j43 = j38 + (j41 >> 28);
        long j44 = j39 + (j43 >> 28);
        long j45 = j40 + (j44 >> 28);
        long j46 = j30 + (j45 >> 28);
        long j47 = j24 + (j46 >> 28);
        long j48 = j32 + (j47 >> 28);
        long j49 = j34 + (j48 >> 28);
        long j50 = (j49 >> 28) - j35;
        long j51 = j42 + (j50 & (-50998291));
        long j52 = (j41 & 268435455) + (j50 & 19280294) + (j51 >> 28);
        long j53 = (j43 & 268435455) + (j50 & 127719000) + (j52 >> 28);
        long j54 = (j44 & 268435455) + (j50 & (-6428113)) + (j53 >> 28);
        long j55 = (j45 & 268435455) + (j50 & 5343) + (j54 >> 28);
        long j56 = (j46 & 268435455) + (j55 >> 28);
        long j57 = (j47 & 268435455) + (j56 >> 28);
        long j58 = (j48 & 268435455) + (j57 >> 28);
        long j59 = (j49 & 268435455) + (j58 >> 28);
        long j60 = j58 & 268435455;
        byte[] bArr2 = new byte[32];
        a2.h((j51 & 268435455) | ((j52 & 268435455) << 28), bArr2, 0);
        a2.h((j53 & 268435455) | ((j54 & 268435455) << 28), bArr2, 7);
        a2.h(((j56 & 268435455) << 28) | (j55 & 268435455), bArr2, 14);
        a2.h((j60 << 28) | (j57 & 268435455), bArr2, 21);
        a2.f((int) j59, bArr2, 28);
        return bArr2;
    }

    public static boolean f(int[] iArr, int[] iArr2, int[] iArr3) {
        int[] iArr4 = new int[16];
        System.arraycopy(b, 0, iArr4, 0, 16);
        int[] iArr5 = new int[16];
        m4.f(iArr, iArr5);
        iArr5[0] = iArr5[0] + 1;
        int[] iArr6 = new int[16];
        int[] iArr7 = a;
        m4.c(iArr7, iArr, iArr6);
        int[] iArr8 = new int[16];
        int[] iArr9 = new int[4];
        System.arraycopy(iArr7, 0, iArr9, 0, 4);
        int[] iArr10 = new int[4];
        System.arraycopy(iArr, 0, iArr10, 0, 4);
        int[] iArr11 = new int[4];
        iArr11[0] = 1;
        int i = 1016;
        int[] iArr12 = iArr11;
        int[] iArr13 = new int[4];
        int[] iArr14 = iArr9;
        int[] iArr15 = iArr10;
        int i2 = 15;
        int iD = d6.d(15, iArr5);
        int[] iArr16 = iArr4;
        int[] iArr17 = iArr5;
        while (iD > 254) {
            i--;
            if (i < 0) {
                return false;
            }
            int iC = d6.c(i2, iArr6) - iD;
            int i3 = iC & (~(iC >> 31));
            if (iArr6[i2] < 0) {
                d6.a(i2, i3, iArr16, iArr17, iArr6, iArr8);
                d6.b(3, i3, iArr14, iArr13, iArr15, iArr12);
            } else {
                d6.f(i2, i3, iArr16, iArr17, iArr6, iArr8);
                d6.g(3, i3, iArr14, iArr13, iArr15, iArr12);
            }
            int[] iArr18 = iArr15;
            int[] iArr19 = iArr12;
            if (d6.e(i2, iArr16, iArr17)) {
                int i4 = iD >>> 5;
                int iD2 = d6.d(i4, iArr16);
                int[] iArr20 = iArr17;
                iArr17 = iArr16;
                iArr16 = iArr20;
                i2 = i4;
                iD = iD2;
                iArr15 = iArr14;
                iArr12 = iArr13;
                iArr13 = iArr19;
                iArr14 = iArr18;
            } else {
                iArr12 = iArr19;
                iArr15 = iArr18;
            }
        }
        System.arraycopy(iArr15, 0, iArr2, 0, 4);
        System.arraycopy(iArr12, 0, iArr3, 0, 4);
        return true;
    }

    public static void g(int i, int[] iArr) {
        n4.a(8, (~iArr[0]) & 1, a, iArr);
        n4.d(8, iArr, 1);
    }
}
