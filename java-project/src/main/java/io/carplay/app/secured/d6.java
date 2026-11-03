package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
abstract class d6 {
    public static void a(int i, int i2, int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4) {
        int i3 = i;
        int[] iArr5 = iArr3;
        char c = ' ';
        int i4 = 0;
        long j = 4294967295L;
        long j2 = 0;
        if (i2 == 0) {
            long j3 = 0;
            while (i4 <= i3) {
                long j4 = ((long) iArr5[i4]) & 4294967295L;
                long j5 = j2 + (((long) iArr[i4]) & 4294967295L) + j4;
                long j6 = j3 + j4 + (((long) iArr2[i4]) & 4294967295L);
                int i5 = (int) j6;
                j3 = j6 >>> 32;
                iArr5[i4] = i5;
                long j7 = j5 + (((long) i5) & 4294967295L);
                iArr[i4] = (int) j7;
                j2 = j7 >>> 32;
                i4++;
            }
            return;
        }
        if (i2 < 32) {
            int i6 = 0;
            long j8 = 0;
            long j9 = 0;
            int i7 = 0;
            int i8 = 0;
            while (i4 <= i3) {
                int i9 = iArr5[i4];
                char c2 = c;
                int i10 = -i2;
                long j10 = j;
                long j11 = j8 + (((long) iArr[i4]) & j10) + (((long) ((i6 >>> i10) | (i9 << i2))) & j10);
                int i11 = iArr2[i4];
                long j12 = j9 + (((long) i9) & j10) + (((long) ((i11 << i2) | (i7 >>> i10))) & j10);
                int i12 = (int) j12;
                j9 = j12 >>> c2;
                iArr5[i4] = i12;
                long j13 = j11 + (((long) ((i8 >>> i10) | (i12 << i2))) & j10);
                iArr[i4] = (int) j13;
                j8 = j13 >>> c2;
                i4++;
                i7 = i11;
                i8 = i12;
                i6 = i9;
                c = c2;
                j = j10;
            }
            return;
        }
        System.arraycopy(iArr5, 0, iArr4, 0, i3);
        int i13 = i2 >>> 5;
        int i14 = i2 & 31;
        if (i14 == 0) {
            long j14 = 0;
            for (int i15 = i13; i15 <= i3; i15++) {
                int i16 = i15 - i13;
                long j15 = j2 + (((long) iArr[i15]) & 4294967295L) + (((long) iArr4[i16]) & 4294967295L);
                long j16 = j14 + (((long) iArr5[i15]) & 4294967295L) + (((long) iArr2[i16]) & 4294967295L);
                iArr5[i15] = (int) j16;
                j14 = j16 >>> 32;
                long j17 = j15 + (((long) iArr5[i16]) & 4294967295L);
                iArr[i15] = (int) j17;
                j2 = j17 >>> 32;
            }
            return;
        }
        int i17 = i13;
        int i18 = 0;
        int i19 = 0;
        long j18 = 0;
        while (i17 <= i3) {
            int i20 = i17 - i13;
            int i21 = iArr4[i20];
            int i22 = -i14;
            int i23 = i14;
            long j19 = j2 + (((long) iArr[i17]) & 4294967295L) + (((long) ((i4 >>> i22) | (i21 << i14))) & 4294967295L);
            int i24 = iArr2[i20];
            long j20 = j18 + (((long) iArr5[i17]) & 4294967295L) + (((long) ((i24 << i23) | (i18 >>> i22))) & 4294967295L);
            iArr3[i17] = (int) j20;
            j18 = j20 >>> 32;
            int i25 = iArr3[i20];
            long j21 = j19 + (((long) ((i25 << i23) | (i19 >>> i22))) & 4294967295L);
            iArr[i17] = (int) j21;
            j2 = j21 >>> 32;
            i17++;
            i14 = i23;
            iArr5 = iArr3;
            i19 = i25;
            i18 = i24;
            i4 = i21;
            i3 = i;
        }
    }

    public static void b(int i, int i2, int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4) {
        int i3 = i2 >>> 5;
        int i4 = i2 & 31;
        char c = ' ';
        long j = 4294967295L;
        long j2 = 0;
        if (i4 == 0) {
            long j3 = 0;
            for (int i5 = i3; i5 <= i; i5++) {
                long j4 = j2 + (((long) iArr[i5]) & 4294967295L);
                long j5 = j3 + (((long) iArr2[i5]) & 4294967295L);
                int i6 = i5 - i3;
                long j6 = j4 + (((long) iArr3[i6]) & 4294967295L);
                long j7 = j5 + (((long) iArr4[i6]) & 4294967295L);
                iArr[i5] = (int) j6;
                j2 = j6 >>> 32;
                iArr2[i5] = (int) j7;
                j3 = j7 >>> 32;
            }
            return;
        }
        int i7 = i3;
        int i8 = 0;
        int i9 = 0;
        long j8 = 0;
        while (i7 <= i) {
            int i10 = i7 - i3;
            int i11 = iArr3[i10];
            int i12 = iArr4[i10];
            char c2 = c;
            int i13 = -i4;
            long j9 = j;
            long j10 = j2 + (((long) iArr[i7]) & j9);
            long j11 = j10 + (((long) ((i8 >>> i13) | (i11 << i4))) & j9);
            long j12 = j8 + (((long) iArr2[i7]) & j9) + (((long) ((i9 >>> i13) | (i12 << i4))) & j9);
            iArr[i7] = (int) j11;
            j2 = j11 >>> c2;
            iArr2[i7] = (int) j12;
            j8 = j12 >>> c2;
            i7++;
            c = c2;
            i9 = i12;
            i8 = i11;
            j = j9;
        }
    }

    public static int c(int i, int[] iArr) {
        int i2 = iArr[i] >> 31;
        while (i > 0 && iArr[i] == i2) {
            i--;
        }
        return IntegerMath.a(iArr[i] ^ i2) + (i * 32);
    }

    public static int d(int i, int[] iArr) {
        while (i > 0 && iArr[i] == 0) {
            i--;
        }
        return IntegerMath.a(iArr[i]) + (i * 32);
    }

    public static boolean e(int i, int[] iArr, int[] iArr2) {
        do {
            int i2 = iArr[i] - Integer.MIN_VALUE;
            int i3 = iArr2[i] - Integer.MIN_VALUE;
            if (i2 < i3) {
                return true;
            }
            if (i2 > i3) {
                return false;
            }
            i--;
        } while (i >= 0);
        return false;
    }

    public static void f(int i, int i2, int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4) {
        int i3 = i;
        int[] iArr5 = iArr3;
        char c = ' ';
        int i4 = 0;
        long j = 4294967295L;
        long j2 = 0;
        if (i2 == 0) {
            long j3 = 0;
            while (i4 <= i3) {
                long j4 = ((long) iArr5[i4]) & 4294967295L;
                long j5 = (j2 + (((long) iArr[i4]) & 4294967295L)) - j4;
                long j6 = (j3 + j4) - (((long) iArr2[i4]) & 4294967295L);
                int i5 = (int) j6;
                j3 = j6 >> 32;
                iArr5[i4] = i5;
                long j7 = j5 - (((long) i5) & 4294967295L);
                iArr[i4] = (int) j7;
                j2 = j7 >> 32;
                i4++;
            }
            return;
        }
        if (i2 < 32) {
            int i6 = 0;
            long j8 = 0;
            long j9 = 0;
            int i7 = 0;
            int i8 = 0;
            while (i4 <= i3) {
                int i9 = iArr5[i4];
                char c2 = c;
                int i10 = -i2;
                long j10 = j;
                long j11 = (j8 + (((long) iArr[i4]) & j10)) - (((long) ((i6 >>> i10) | (i9 << i2))) & j10);
                int i11 = iArr2[i4];
                long j12 = (j9 + (((long) i9) & j10)) - (((long) ((i11 << i2) | (i7 >>> i10))) & j10);
                int i12 = (int) j12;
                j9 = j12 >> c2;
                iArr5[i4] = i12;
                long j13 = j11 - (((long) ((i8 >>> i10) | (i12 << i2))) & j10);
                iArr[i4] = (int) j13;
                j8 = j13 >> c2;
                i4++;
                i7 = i11;
                i8 = i12;
                i6 = i9;
                c = c2;
                j = j10;
            }
            return;
        }
        System.arraycopy(iArr5, 0, iArr4, 0, i3);
        int i13 = i2 >>> 5;
        int i14 = i2 & 31;
        if (i14 == 0) {
            long j14 = 0;
            for (int i15 = i13; i15 <= i3; i15++) {
                int i16 = i15 - i13;
                long j15 = (j2 + (((long) iArr[i15]) & 4294967295L)) - (((long) iArr4[i16]) & 4294967295L);
                long j16 = (j14 + (((long) iArr5[i15]) & 4294967295L)) - (((long) iArr2[i16]) & 4294967295L);
                iArr5[i15] = (int) j16;
                j14 = j16 >> 32;
                long j17 = j15 - (((long) iArr5[i16]) & 4294967295L);
                iArr[i15] = (int) j17;
                j2 = j17 >> 32;
            }
            return;
        }
        int i17 = i13;
        int i18 = 0;
        int i19 = 0;
        long j18 = 0;
        while (i17 <= i3) {
            int i20 = i17 - i13;
            int i21 = iArr4[i20];
            int i22 = -i14;
            int i23 = i14;
            long j19 = (j2 + (((long) iArr[i17]) & 4294967295L)) - (((long) ((i4 >>> i22) | (i21 << i14))) & 4294967295L);
            int i24 = iArr2[i20];
            long j20 = (j18 + (((long) iArr5[i17]) & 4294967295L)) - (((long) ((i24 << i23) | (i18 >>> i22))) & 4294967295L);
            iArr3[i17] = (int) j20;
            j18 = j20 >> 32;
            int i25 = iArr3[i20];
            long j21 = j19 - (((long) ((i25 << i23) | (i19 >>> i22))) & 4294967295L);
            iArr[i17] = (int) j21;
            j2 = j21 >> 32;
            i17++;
            i14 = i23;
            iArr5 = iArr3;
            i19 = i25;
            i18 = i24;
            i4 = i21;
            i3 = i;
        }
    }

    public static void g(int i, int i2, int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4) {
        int i3 = i2 >>> 5;
        int i4 = i2 & 31;
        char c = ' ';
        long j = 4294967295L;
        long j2 = 0;
        if (i4 == 0) {
            long j3 = 0;
            for (int i5 = i3; i5 <= i; i5++) {
                long j4 = j2 + (((long) iArr[i5]) & 4294967295L);
                long j5 = j3 + (((long) iArr2[i5]) & 4294967295L);
                int i6 = i5 - i3;
                long j6 = j4 - (((long) iArr3[i6]) & 4294967295L);
                long j7 = j5 - (((long) iArr4[i6]) & 4294967295L);
                iArr[i5] = (int) j6;
                j2 = j6 >> 32;
                iArr2[i5] = (int) j7;
                j3 = j7 >> 32;
            }
            return;
        }
        int i7 = i3;
        int i8 = 0;
        int i9 = 0;
        long j8 = 0;
        while (i7 <= i) {
            int i10 = i7 - i3;
            int i11 = iArr3[i10];
            int i12 = iArr4[i10];
            char c2 = c;
            int i13 = -i4;
            long j9 = j;
            long j10 = j2 + (((long) iArr[i7]) & j9);
            long j11 = j10 - (((long) ((i8 >>> i13) | (i11 << i4))) & j9);
            long j12 = (j8 + (((long) iArr2[i7]) & j9)) - (((long) ((i9 >>> i13) | (i12 << i4))) & j9);
            iArr[i7] = (int) j11;
            j2 = j11 >> c2;
            iArr2[i7] = (int) j12;
            j8 = j12 >> c2;
            i7++;
            c = c2;
            i9 = i12;
            i8 = i11;
            j = j9;
        }
    }
}
