package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public abstract class m4 {
    public static int a(int[] iArr, int i, int[] iArr2, int i2, int i3) {
        long j = (((long) iArr[i]) & 4294967295L) + (((long) iArr2[i2]) & 4294967295L) + (((long) i3) & 4294967295L);
        iArr2[i2] = (int) j;
        int i4 = i2 + 1;
        long j2 = (((long) iArr[i + 1]) & 4294967295L) + (((long) iArr2[i4]) & 4294967295L) + (j >>> 32);
        iArr2[i4] = (int) j2;
        int i5 = i2 + 2;
        long j3 = (((long) iArr[i + 2]) & 4294967295L) + (((long) iArr2[i5]) & 4294967295L) + (j2 >>> 32);
        iArr2[i5] = (int) j3;
        int i6 = i2 + 3;
        long j4 = (((long) iArr[i + 3]) & 4294967295L) + (((long) iArr2[i6]) & 4294967295L) + (j3 >>> 32);
        iArr2[i6] = (int) j4;
        int i7 = i2 + 4;
        long j5 = (((long) iArr[i + 4]) & 4294967295L) + (((long) iArr2[i7]) & 4294967295L) + (j4 >>> 32);
        iArr2[i7] = (int) j5;
        int i8 = i2 + 5;
        long j6 = (((long) iArr[i + 5]) & 4294967295L) + (((long) iArr2[i8]) & 4294967295L) + (j5 >>> 32);
        iArr2[i8] = (int) j6;
        int i9 = i2 + 6;
        long j7 = (((long) iArr[i + 6]) & 4294967295L) + (((long) iArr2[i9]) & 4294967295L) + (j6 >>> 32);
        iArr2[i9] = (int) j7;
        int i10 = i2 + 7;
        long j8 = (((long) iArr[i + 7]) & 4294967295L) + (4294967295L & ((long) iArr2[i10])) + (j7 >>> 32);
        iArr2[i10] = (int) j8;
        return (int) (j8 >>> 32);
    }

    public static boolean b(int[] iArr, int[] iArr2) {
        for (int i = 7; i >= 0; i--) {
            int i2 = iArr[i] - Integer.MIN_VALUE;
            int i3 = iArr2[i] - Integer.MIN_VALUE;
            if (i2 < i3) {
                return false;
            }
            if (i2 > i3) {
                return true;
            }
        }
        return true;
    }

    public static void c(int[] iArr, int[] iArr2, int[] iArr3) {
        long j = ((long) iArr2[0]) & 4294967295L;
        long j2 = ((long) iArr2[1]) & 4294967295L;
        long j3 = ((long) iArr2[2]) & 4294967295L;
        long j4 = ((long) iArr2[3]) & 4294967295L;
        long j5 = ((long) iArr2[4]) & 4294967295L;
        long j6 = ((long) iArr2[5]) & 4294967295L;
        long j7 = ((long) iArr2[6]) & 4294967295L;
        long j8 = ((long) iArr2[7]) & 4294967295L;
        long j9 = ((long) iArr[0]) & 4294967295L;
        long j10 = j9 * j;
        iArr3[0] = (int) j10;
        long j11 = (j9 * j2) + (j10 >>> 32);
        iArr3[1] = (int) j11;
        long j12 = (j9 * j3) + (j11 >>> 32);
        iArr3[2] = (int) j12;
        long j13 = (j9 * j4) + (j12 >>> 32);
        iArr3[3] = (int) j13;
        long j14 = (j9 * j5) + (j13 >>> 32);
        iArr3[4] = (int) j14;
        long j15 = (j9 * j6) + (j14 >>> 32);
        iArr3[5] = (int) j15;
        long j16 = (j9 * j7) + (j15 >>> 32);
        iArr3[6] = (int) j16;
        long j17 = (j9 * j8) + (j16 >>> 32);
        iArr3[7] = (int) j17;
        int i = (int) (j17 >>> 32);
        iArr3[8] = i;
        int i2 = 1;
        for (int i3 = 8; i2 < i3; i3 = 8) {
            long j18 = ((long) iArr[i2]) & 4294967295L;
            long j19 = j5;
            long j20 = (j18 * j) + (((long) iArr3[i2]) & 4294967295L);
            iArr3[i2] = (int) j20;
            int i4 = i2 + 1;
            long j21 = (j18 * j2) + (((long) iArr3[i4]) & 4294967295L) + (j20 >>> 32);
            iArr3[i4] = (int) j21;
            int i5 = i2 + 2;
            long j22 = (j18 * j3) + (((long) iArr3[i5]) & 4294967295L) + (j21 >>> 32);
            iArr3[i5] = (int) j22;
            int i6 = i2 + 3;
            long j23 = (j18 * j4) + (((long) iArr3[i6]) & 4294967295L) + (j22 >>> 32);
            iArr3[i6] = (int) j23;
            int i7 = i2 + 4;
            long j24 = (j18 * j19) + (((long) iArr3[i7]) & 4294967295L) + (j23 >>> 32);
            iArr3[i7] = (int) j24;
            int i8 = i2 + 5;
            long j25 = (j18 * j6) + (((long) iArr3[i8]) & 4294967295L) + (j24 >>> 32);
            iArr3[i8] = (int) j25;
            int i9 = i2 + 6;
            long j26 = (j18 * j7) + (((long) iArr3[i9]) & 4294967295L) + (j25 >>> 32);
            iArr3[i9] = (int) j26;
            long j27 = j26 >>> 32;
            int i10 = i2 + 7;
            long j28 = (j18 * j8) + (((long) iArr3[i10]) & 4294967295L) + j27;
            iArr3[i10] = (int) j28;
            iArr3[i2 + 8] = (int) (j28 >>> 32);
            i2 = i4;
            j5 = j19;
        }
    }

    public static void d(int[] iArr, int[] iArr2, int[] iArr3) {
        long j = ((long) iArr[0]) & 4294967295L;
        long j2 = ((long) iArr[1]) & 4294967295L;
        long j3 = ((long) iArr[2]) & 4294967295L;
        long j4 = ((long) iArr[3]) & 4294967295L;
        long j5 = ((long) iArr[4]) & 4294967295L;
        long j6 = ((long) iArr[5]) & 4294967295L;
        long j7 = ((long) iArr[6]) & 4294967295L;
        long j8 = ((long) iArr[7]) & 4294967295L;
        long j9 = ((long) iArr2[0]) & 4294967295L;
        long j10 = j9 * j;
        iArr3[0] = (int) j10;
        long j11 = (j9 * j2) + (j10 >>> 32);
        iArr3[1] = (int) j11;
        long j12 = (j9 * j3) + (j11 >>> 32);
        iArr3[2] = (int) j12;
        long j13 = (j9 * j4) + (j12 >>> 32);
        iArr3[3] = (int) j13;
        long j14 = (j9 * j5) + (j13 >>> 32);
        iArr3[4] = (int) j14;
        long j15 = (j9 * j6) + (j14 >>> 32);
        iArr3[5] = (int) j15;
        long j16 = (j9 * j7) + (j15 >>> 32);
        iArr3[6] = (int) j16;
        long j17 = (j9 * j8) + (j16 >>> 32);
        iArr3[7] = (int) j17;
        iArr3[8] = (int) (j17 >>> 32);
        int i = 1;
        for (int i2 = 4; i < i2; i2 = 4) {
            long j18 = ((long) iArr2[i]) & 4294967295L;
            long j19 = j5;
            long j20 = (j18 * j) + (((long) iArr3[i]) & 4294967295L);
            iArr3[i] = (int) j20;
            int i3 = i + 1;
            long j21 = (j18 * j2) + (((long) iArr3[i3]) & 4294967295L) + (j20 >>> 32);
            iArr3[i3] = (int) j21;
            int i4 = i + 2;
            long j22 = (j18 * j3) + (((long) iArr3[i4]) & 4294967295L) + (j21 >>> 32);
            iArr3[i4] = (int) j22;
            int i5 = i + 3;
            long j23 = (j18 * j4) + (((long) iArr3[i5]) & 4294967295L) + (j22 >>> 32);
            iArr3[i5] = (int) j23;
            int i6 = i + 4;
            long j24 = (j18 * j19) + (((long) iArr3[i6]) & 4294967295L) + (j23 >>> 32);
            iArr3[i6] = (int) j24;
            int i7 = i + 5;
            long j25 = (j18 * j6) + (((long) iArr3[i7]) & 4294967295L) + (j24 >>> 32);
            iArr3[i7] = (int) j25;
            int i8 = i + 6;
            long j26 = (j18 * j7) + (((long) iArr3[i8]) & 4294967295L) + (j25 >>> 32);
            iArr3[i8] = (int) j26;
            long j27 = j26 >>> 32;
            int i9 = i + 7;
            long j28 = (j18 * j8) + (((long) iArr3[i9]) & 4294967295L) + j27;
            iArr3[i9] = (int) j28;
            iArr3[i + 8] = (int) (j28 >>> 32);
            i = i3;
            j5 = j19;
        }
    }

    public static int e(int[] iArr, int[] iArr2, int[] iArr3) {
        long j = ((long) iArr2[0]) & 4294967295L;
        long j2 = ((long) iArr2[1]) & 4294967295L;
        long j3 = ((long) iArr2[2]) & 4294967295L;
        long j4 = ((long) iArr2[3]) & 4294967295L;
        long j5 = ((long) iArr2[4]) & 4294967295L;
        long j6 = ((long) iArr2[5]) & 4294967295L;
        long j7 = ((long) iArr2[6]) & 4294967295L;
        long j8 = ((long) iArr2[7]) & 4294967295L;
        long j9 = 0;
        int i = 0;
        while (i < 8) {
            long j10 = ((long) iArr[i]) & 4294967295L;
            long j11 = (j10 * j) + (((long) iArr3[i]) & 4294967295L);
            int i2 = i;
            iArr3[i2] = (int) j11;
            int i3 = i2 + 1;
            long j12 = (j10 * j2) + (((long) iArr3[i3]) & 4294967295L) + (j11 >>> 32);
            iArr3[i3] = (int) j12;
            int i4 = i2 + 2;
            long j13 = (j10 * j3) + (((long) iArr3[i4]) & 4294967295L) + (j12 >>> 32);
            iArr3[i4] = (int) j13;
            int i5 = i2 + 3;
            long j14 = (j10 * j4) + (((long) iArr3[i5]) & 4294967295L) + (j13 >>> 32);
            iArr3[i5] = (int) j14;
            int i6 = i2 + 4;
            long j15 = (j10 * j5) + (((long) iArr3[i6]) & 4294967295L) + (j14 >>> 32);
            iArr3[i6] = (int) j15;
            int i7 = i2 + 5;
            long j16 = (j10 * j6) + (((long) iArr3[i7]) & 4294967295L) + (j15 >>> 32);
            iArr3[i7] = (int) j16;
            int i8 = i2 + 6;
            long j17 = (j10 * j7) + (((long) iArr3[i8]) & 4294967295L) + (j16 >>> 32);
            iArr3[i8] = (int) j17;
            int i9 = i2 + 7;
            long j18 = (j10 * j8) + (((long) iArr3[i9]) & 4294967295L) + (j17 >>> 32);
            iArr3[i9] = (int) j18;
            int i10 = i2 + 8;
            long j19 = (j18 >>> 32) + (((long) iArr3[i10]) & 4294967295L) + j9;
            iArr3[i10] = (int) j19;
            j9 = j19 >>> 32;
            i = i3;
        }
        return (int) j9;
    }

    public static void f(int[] iArr, int[] iArr2) {
        long j = ((long) iArr[0]) & 4294967295L;
        int i = 16;
        int i2 = 0;
        int i3 = 7;
        while (true) {
            int i4 = i3 - 1;
            long j2 = ((long) iArr[i3]) & 4294967295L;
            long j3 = j2 * j2;
            iArr2[i - 1] = (i2 << 31) | ((int) (j3 >>> 33));
            i -= 2;
            iArr2[i] = (int) (j3 >>> 1);
            i2 = (int) j3;
            if (i4 <= 0) {
                long j4 = j * j;
                long j5 = (j4 >>> 33) | (((long) (i2 << 31)) & 4294967295L);
                iArr2[0] = (int) j4;
                int i5 = ((int) (j4 >>> 32)) & 1;
                long j6 = ((long) iArr[1]) & 4294967295L;
                long j7 = ((long) iArr2[2]) & 4294967295L;
                long j8 = (j6 * j) + j5;
                int i6 = (int) j8;
                iArr2[1] = i5 | (i6 << 1);
                long j9 = ((long) iArr[2]) & 4294967295L;
                long j10 = ((long) iArr2[3]) & 4294967295L;
                long j11 = ((long) iArr2[4]) & 4294967295L;
                long j12 = (j9 * j) + j7 + (j8 >>> 32);
                int i7 = (int) j12;
                iArr2[2] = (i7 << 1) | (i6 >>> 31);
                long jC = w1.c(j9, j6, j12 >>> 32, j10);
                long j13 = j11 + (jC >>> 32);
                long j14 = ((long) iArr[3]) & 4294967295L;
                long j15 = (((long) iArr2[5]) & 4294967295L) + (j13 >>> 32);
                long j16 = j13 & 4294967295L;
                long j17 = (((long) iArr2[6]) & 4294967295L) + (j15 >>> 32);
                long j18 = j15 & 4294967295L;
                long j19 = (j14 * j) + (jC & 4294967295L);
                int i8 = (int) j19;
                iArr2[3] = (i7 >>> 31) | (i8 << 1);
                int i9 = i8 >>> 31;
                long jC2 = w1.c(j14, j6, j19 >>> 32, j16);
                long jC3 = w1.c(j14, j9, jC2 >>> 32, j18);
                long j20 = j17 + (jC3 >>> 32);
                long j21 = jC3 & 4294967295L;
                long j22 = ((long) iArr[4]) & 4294967295L;
                long j23 = (((long) iArr2[7]) & 4294967295L) + (j20 >>> 32);
                long j24 = (((long) iArr2[8]) & 4294967295L) + (j23 >>> 32);
                long j25 = j23 & 4294967295L;
                long j26 = (j22 * j) + (jC2 & 4294967295L);
                int i10 = (int) j26;
                iArr2[4] = i9 | (i10 << 1);
                long jC4 = w1.c(j22, j6, j26 >>> 32, j21);
                long jC5 = w1.c(j22, j9, jC4 >>> 32, j20 & 4294967295L);
                long jC6 = w1.c(j22, j14, jC5 >>> 32, j25);
                long j27 = jC5 & 4294967295L;
                long j28 = j24 + (jC6 >>> 32);
                long j29 = jC6 & 4294967295L;
                long j30 = ((long) iArr[5]) & 4294967295L;
                long j31 = (((long) iArr2[9]) & 4294967295L) + (j28 >>> 32);
                long j32 = (((long) iArr2[10]) & 4294967295L) + (j31 >>> 32);
                long j33 = (j30 * j) + (jC4 & 4294967295L);
                int i11 = (int) j33;
                iArr2[5] = (i10 >>> 31) | (i11 << 1);
                int i12 = i11 >>> 31;
                long jC7 = w1.c(j30, j6, j33 >>> 32, j27);
                long jC8 = w1.c(j30, j9, jC7 >>> 32, j29);
                long jC9 = w1.c(j30, j14, jC8 >>> 32, j28 & 4294967295L);
                long j34 = jC8 & 4294967295L;
                long jC10 = w1.c(j30, j22, jC9 >>> 32, j31 & 4294967295L);
                long j35 = jC9 & 4294967295L;
                long j36 = j32 + (jC10 >>> 32);
                long j37 = ((long) iArr[6]) & 4294967295L;
                long j38 = (((long) iArr2[11]) & 4294967295L) + (j36 >>> 32);
                long j39 = (((long) iArr2[12]) & 4294967295L) + (j38 >>> 32);
                long j40 = (j37 * j) + (jC7 & 4294967295L);
                int i13 = (int) j40;
                iArr2[6] = i12 | (i13 << 1);
                int i14 = i13 >>> 31;
                long jC11 = w1.c(j37, j6, j40 >>> 32, j34);
                long jC12 = w1.c(j37, j9, jC11 >>> 32, j35);
                long jC13 = w1.c(j37, j14, jC12 >>> 32, jC10 & 4294967295L);
                long j41 = jC12 & 4294967295L;
                long jC14 = w1.c(j37, j22, jC13 >>> 32, j36 & 4294967295L);
                long j42 = jC13 & 4294967295L;
                long jC15 = w1.c(j37, j30, jC14 >>> 32, j38 & 4294967295L);
                long j43 = j39 + (jC15 >>> 32);
                long j44 = ((long) iArr[7]) & 4294967295L;
                long j45 = (((long) iArr2[13]) & 4294967295L) + (j43 >>> 32);
                long j46 = (((long) iArr2[14]) & 4294967295L) + (j45 >>> 32);
                long j47 = j45 & 4294967295L;
                long j48 = (j44 * j) + (jC11 & 4294967295L);
                int i15 = (int) j48;
                iArr2[7] = (i15 << 1) | i14;
                int i16 = i15 >>> 31;
                long jC16 = w1.c(j44, j6, j48 >>> 32, j41);
                long jC17 = w1.c(j44, j9, jC16 >>> 32, j42);
                long jC18 = w1.c(j44, j14, jC17 >>> 32, jC14 & 4294967295L);
                long jC19 = w1.c(j44, j22, jC18 >>> 32, jC15 & 4294967295L);
                long jC20 = w1.c(j44, j30, jC19 >>> 32, j43 & 4294967295L);
                long jC21 = w1.c(j44, j37, jC20 >>> 32, j47);
                long j49 = j46 + (jC21 >>> 32);
                int i17 = (int) jC16;
                iArr2[8] = (i17 << 1) | i16;
                int i18 = (int) jC17;
                iArr2[9] = (i17 >>> 31) | (i18 << 1);
                int i19 = i18 >>> 31;
                int i20 = (int) jC18;
                iArr2[10] = i19 | (i20 << 1);
                int i21 = i20 >>> 31;
                int i22 = (int) jC19;
                iArr2[11] = i21 | (i22 << 1);
                int i23 = i22 >>> 31;
                int i24 = (int) jC20;
                iArr2[12] = i23 | (i24 << 1);
                int i25 = i24 >>> 31;
                int i26 = (int) jC21;
                iArr2[13] = i25 | (i26 << 1);
                int i27 = i26 >>> 31;
                int i28 = (int) j49;
                iArr2[14] = i27 | (i28 << 1);
                iArr2[15] = ((iArr2[15] + ((int) (j49 >>> 32))) << 1) | (i28 >>> 31);
                return;
            }
            i3 = i4;
        }
    }

    public static int g(int[] iArr, int i, int[] iArr2, int i2, int i3) {
        long j = ((((long) iArr2[i2]) & 4294967295L) - (((long) iArr[i]) & 4294967295L)) + (((long) i3) & 4294967295L);
        iArr2[i2] = (int) j;
        long j2 = j >> 32;
        int i4 = i2 + 1;
        long j3 = ((((long) iArr2[i4]) & 4294967295L) - (((long) iArr[i + 1]) & 4294967295L)) + j2;
        iArr2[i4] = (int) j3;
        int i5 = i2 + 2;
        long j4 = ((((long) iArr2[i5]) & 4294967295L) - (((long) iArr[i + 2]) & 4294967295L)) + (j3 >> 32);
        iArr2[i5] = (int) j4;
        int i6 = i2 + 3;
        long j5 = ((((long) iArr2[i6]) & 4294967295L) - (((long) iArr[i + 3]) & 4294967295L)) + (j4 >> 32);
        iArr2[i6] = (int) j5;
        int i7 = i2 + 4;
        long j6 = ((((long) iArr2[i7]) & 4294967295L) - (((long) iArr[i + 4]) & 4294967295L)) + (j5 >> 32);
        iArr2[i7] = (int) j6;
        int i8 = i2 + 5;
        long j7 = ((((long) iArr2[i8]) & 4294967295L) - (((long) iArr[i + 5]) & 4294967295L)) + (j6 >> 32);
        iArr2[i8] = (int) j7;
        int i9 = i2 + 6;
        long j8 = ((((long) iArr2[i9]) & 4294967295L) - (((long) iArr[i + 6]) & 4294967295L)) + (j7 >> 32);
        iArr2[i9] = (int) j8;
        int i10 = i2 + 7;
        long j9 = ((((long) iArr2[i10]) & 4294967295L) - (((long) iArr[i + 7]) & 4294967295L)) + (j8 >> 32);
        iArr2[i10] = (int) j9;
        return (int) (j9 >> 32);
    }
}
