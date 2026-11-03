package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public abstract class k4 {
    private static int a(int i, int[] iArr, int[] iArr2) {
        int i2 = i - 1;
        int i3 = 0;
        for (int i4 = 0; i4 < i2; i4++) {
            int i5 = iArr[i4] + iArr2[i4] + i3;
            iArr[i4] = 1073741823 & i5;
            i3 = i5 >> 30;
        }
        int i6 = iArr[i2] + iArr2[i2] + i3;
        iArr[i2] = i6;
        return i6 >> 30;
    }

    private static void b(int i, int i2, int[] iArr) {
        int i3 = i - 1;
        int i4 = 0;
        for (int i5 = 0; i5 < i3; i5++) {
            int i6 = ((iArr[i5] ^ i2) - i2) + i4;
            iArr[i5] = 1073741823 & i6;
            i4 = i6 >> 30;
        }
        iArr[i3] = ((iArr[i3] ^ i2) - i2) + i4;
    }

    private static void c(int i, int i2, int[] iArr, int[] iArr2) {
        int i3 = i - 1;
        int i4 = iArr[i3] >> 31;
        int i5 = 0;
        for (int i6 = 0; i6 < i3; i6++) {
            int i7 = (((iArr[i6] + (iArr2[i6] & i4)) ^ i2) - i2) + i5;
            iArr[i6] = i7 & 1073741823;
            i5 = i7 >> 30;
        }
        int i8 = (((iArr[i3] + (i4 & iArr2[i3])) ^ i2) - i2) + i5;
        iArr[i3] = i8;
        int i9 = i8 >> 31;
        int i10 = 0;
        for (int i11 = 0; i11 < i3; i11++) {
            int i12 = iArr[i11] + (iArr2[i11] & i9) + i10;
            iArr[i11] = i12 & 1073741823;
            i10 = i12 >> 30;
        }
        iArr[i3] = iArr[i3] + (i9 & iArr2[i3]) + i10;
    }

    private static void d(int i, int[] iArr, int[] iArr2) {
        int i2 = 0;
        long j = 0;
        int i3 = 0;
        int i4 = 0;
        while (i > 0) {
            while (i2 < Math.min(32, i)) {
                j |= ((long) iArr[i3]) << i2;
                i2 += 30;
                i3++;
            }
            iArr2[i4] = (int) j;
            j >>>= 32;
            i2 -= 32;
            i -= 32;
            i4++;
        }
    }

    private static int e(int i, int i2, int i3, int[] iArr) {
        int i4;
        int i5 = 30;
        int i6 = 1;
        int i7 = 1;
        int i8 = 0;
        int i9 = 0;
        while (true) {
            int iE = IntegerMath.e(((-1) << i5) | i3);
            int i10 = i3 >> iE;
            i6 <<= iE;
            i8 <<= iE;
            i -= iE;
            i5 -= iE;
            if (i5 <= 0) {
                iArr[0] = i6;
                iArr[1] = i8;
                iArr[2] = i9;
                iArr[3] = i7;
                return i;
            }
            if (i <= 0) {
                i = 2 - i;
                int i11 = -i2;
                int i12 = -i6;
                int i13 = -i8;
                i4 = ((-1) >>> (32 - (i > i5 ? i5 : i))) & 63 & (((i10 * i10) - 2) * i10 * i11);
                i10 = i11;
                i2 = i10;
                int i14 = i9;
                i9 = i12;
                i6 = i14;
                int i15 = i7;
                i7 = i13;
                i8 = i15;
            } else {
                i4 = ((-1) >>> (32 - (i > i5 ? i5 : i))) & 15 & (((((i2 + 1) & 4) << 1) + i2) * (-i10));
            }
            i3 = i10 + (i2 * i4);
            i9 += i6 * i4;
            i7 += i4 * i8;
        }
    }

    private static void f(int i, int[] iArr, int[] iArr2) {
        int i2 = 0;
        long j = 0;
        int i3 = 0;
        int i4 = 0;
        while (i > 0) {
            if (i2 < Math.min(30, i)) {
                j |= (((long) iArr[i3]) & 4294967295L) << i2;
                i2 += 32;
                i3++;
            }
            iArr2[i4] = ((int) j) & 1073741823;
            j >>>= 30;
            i2 -= 30;
            i -= 30;
            i4++;
        }
    }

    private static int g(int i, int[] iArr, int i2) {
        int i3 = i2 ^ iArr[0];
        for (int i4 = 1; i4 < i; i4++) {
            i3 |= iArr[i4];
        }
        return n4.b(i3);
    }

    private static boolean h(int i, int[] iArr, int i2) {
        int i3 = i2 ^ iArr[0];
        if (i3 != 0) {
            return false;
        }
        for (int i4 = 1; i4 < i; i4++) {
            i3 |= iArr[i4];
        }
        return i3 == 0;
    }

    private static int i(int i) {
        return (int) (((((long) i) * 188898) + ((long) (i < 46 ? 308405 : 181188))) >>> 16);
    }

    private static int j(int i) {
        return (int) (((((long) i) * 150964) + 99243) >>> 16);
    }

    private static int k(int i, int i2, int i3, int[] iArr) {
        int i4 = 1073741824;
        int i5 = 1073741824;
        int i6 = 0;
        int i7 = 0;
        for (int i8 = 0; i8 < 30; i8++) {
            int i9 = i >> 31;
            int i10 = -(i3 & 1);
            int i11 = i3 - ((i2 ^ i9) & i10);
            int i12 = i7 - ((i4 ^ i9) & i10);
            int i13 = i5 - ((i6 ^ i9) & i10);
            int i14 = (~i9) & i10;
            i = (i ^ i14) + 1;
            i2 += i11 & i14;
            i4 += i12 & i14;
            i6 += i14 & i13;
            i3 = i11 >> 1;
            i7 = i12 >> 1;
            i5 = i13 >> 1;
        }
        iArr[0] = i4;
        iArr[1] = i6;
        iArr[2] = i7;
        iArr[3] = i5;
        return i;
    }

    public static int l(int i) {
        int i2 = (2 - (i * i)) * i;
        int i3 = (2 - (i * i2)) * i2;
        int i4 = (2 - (i * i3)) * i3;
        return (2 - (i * i4)) * i4;
    }

    public static int m(int[] iArr, int[] iArr2, int[] iArr3) {
        int length = iArr.length;
        int iD = (length * 32) - IntegerMath.d(iArr[length - 1]);
        int i = (iD + 29) / 30;
        int[] iArr4 = new int[4];
        int[] iArr5 = new int[i];
        int[] iArCarLifeFrame = new int[i];
        int[] iArr7 = new int[i];
        int[] iArr8 = new int[i];
        int[] iArr9 = new int[i];
        iArCarLifeFrame[0] = 1;
        f(iD, iArr2, iArr8);
        f(iD, iArr, iArr9);
        System.arraycopy(iArr9, 0, iArr7, 0, i);
        int iL = l(iArr9[0]);
        int iJ = j(iD);
        int iK = 0;
        for (int i2 = 0; i2 < iJ; i2 += 30) {
            iK = k(iK, iArr7[0], iArr8[0], iArr4);
            q(i, iArr5, iArCarLifeFrame, iArr4, iL, iArr9);
            r(i, iArr7, iArr8, iArr4);
        }
        int i3 = iArr7[i - 1] >> 31;
        b(i, i3, iArr7);
        c(i, i3, iArr5, iArr9);
        d(iD, iArr5, iArr3);
        return g(i, iArr7, 1) & g(i, iArr8, 0);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r12v0 */
    /* JADX WARN: Type inference failed for: r12EncryptProofNeg, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r12v3 */
    /* JADX WARN: Type inference failed for: r16v0, types: [boolean] */
    /* JADX WARN: Type inference failed for: r16EncryptProofNeg */
    /* JADX WARN: Type inference failed for: r16v2 */
    /* JADX WARN: Type inference failed for: r16v3 */
    public static boolean n(int[] iArr, int[] iArr2, int[] iArr3) {
        int length = iArr.length;
        int iD = (length * 32) - IntegerMath.d(iArr[length - 1]);
        int i = (iD + 29) / 30;
        int iC = iD - n4.c(length, iArr2);
        int[] iArr4 = new int[4];
        int[] iArr5 = new int[i];
        int[] iArCarLifeFrame = new int[i];
        int[] iArr7 = new int[i];
        int[] iArr8 = new int[i];
        int[] iArr9 = new int[i];
        int r12 = 0;
        iArCarLifeFrame[0] = 1;
        f(iD, iArr2, iArr8);
        f(iD, iArr, iArr9);
        System.arraycopy(iArr9, 0, iArr7, 0, i);
        int iE = -iC;
        int iL = l(iArr9[0]);
        int i2 = i(iD);
        int iP = i;
        while (!h(iP, iArr8, r12)) {
            if (iC >= i2) {
                return r12 != 0;
            }
            iC += 30;
            int r16 = r12;
            iE = e(iE, iArr7[r12], iArr8[r16 != 0 ? 1 : 0], iArr4);
            q(i, iArr5, iArCarLifeFrame, iArr4, iL, iArr9);
            r(iP, iArr7, iArr8, iArr4);
            iP = p(iP, iArr7, iArr8);
            r12 = r16 != 0 ? 1 : 0;
        }
        int r17 = r12;
        int i3 = iArr7[iP - 1] >> 31;
        int iO = iArr5[i - 1] >> 31;
        if (iO < 0) {
            iO = a(i, iArr5, iArr9);
        }
        if (i3 < 0) {
            iO = o(i, iArr5);
            o(iP, iArr7);
        }
        if (!h(iP, iArr7, 1)) {
            return r17 != 0;
        }
        if (iO < 0) {
            a(i, iArr5, iArr9);
        }
        d(iD, iArr5, iArr3);
        return true;
    }

    private static int o(int i, int[] iArr) {
        int i2 = i - 1;
        int i3 = 0;
        for (int i4 = 0; i4 < i2; i4++) {
            int i5 = i3 - iArr[i4];
            iArr[i4] = 1073741823 & i5;
            i3 = i5 >> 30;
        }
        int i6 = i3 - iArr[i2];
        iArr[i2] = i6;
        return i6 >> 30;
    }

    private static int p(int i, int[] iArr, int[] iArr2) {
        int i2 = i - 1;
        int i3 = iArr[i2];
        int i4 = iArr2[i2];
        int i5 = i - 2;
        if (((i5 >> 31) | ((i3 >> 31) ^ i3) | ((i4 >> 31) ^ i4)) != 0) {
            return i;
        }
        iArr[i5] = (i3 << 30) | iArr[i5];
        iArr2[i5] = iArr2[i5] | (i4 << 30);
        return i - 1;
    }

    private static void q(int i, int[] iArr, int[] iArr2, int[] iArr3, int i2, int[] iArr4) {
        int i3 = i;
        int i4 = iArr3[0];
        int i5 = iArr3[1];
        int i6 = iArr3[2];
        int i7 = iArr3[3];
        int i8 = i3 - 1;
        int i9 = iArr[i8] >> 31;
        int i10 = iArr2[i8] >> 31;
        int i11 = (i4 & i9) + (i5 & i10);
        int i12 = (i9 & i6) + (i10 & i7);
        int i13 = iArr4[0];
        long j = i4;
        long j2 = iArr[0];
        long j3 = i5;
        long j4 = iArr2[0];
        long j5 = (j3 * j4) + (j * j2);
        long j6 = i6;
        long j7 = i7;
        long j8 = (j7 * j4) + (j2 * j6);
        int i14 = i11 - (((((int) j5) * i2) + i11) & 1073741823);
        long j9 = i13;
        long j10 = i14;
        long j11 = (j9 * j10) + j5;
        long j12 = i12 - (((((int) j8) * i2) + i12) & 1073741823);
        char c = 30;
        long j13 = j11 >> 30;
        long j14 = ((j9 * j12) + j8) >> 30;
        int i15 = 1;
        while (i15 < i3) {
            int i16 = iArr4[i15];
            char c2 = c;
            long j15 = j12;
            long j16 = iArr[i15];
            long j17 = j * j16;
            long j18 = iArr2[i15];
            long j19 = i16;
            long j20 = j10;
            long jC = ProtoMessageBase.c(j19, j20, (j3 * j18) + j17, j13);
            long jC2 = ProtoMessageBase.c(j19, j15, (j18 * j7) + (j6 * j16), j14);
            int i17 = i15 - 1;
            iArr[i17] = ((int) jC) & 1073741823;
            j13 = jC >> c2;
            iArr2[i17] = ((int) jC2) & 1073741823;
            j14 = jC2 >> c2;
            i15++;
            i3 = i;
            c = c2;
            j10 = j20;
            j12 = j15;
        }
        iArr[i8] = (int) j13;
        iArr2[i8] = (int) j14;
    }

    private static void r(int i, int[] iArr, int[] iArr2, int[] iArr3) {
        int i2 = iArr3[0];
        int i3 = 1;
        int i4 = iArr3[1];
        int i5 = iArr3[2];
        int i6 = iArr3[3];
        long j = i2;
        long j2 = iArr[0];
        long j3 = i4;
        long j4 = iArr2[0];
        long j5 = (j3 * j4) + (j * j2);
        long j6 = i5;
        long j7 = i6;
        long j8 = ((j4 * j7) + (j2 * j6)) >> 30;
        int i7 = 1;
        long j9 = j5 >> 30;
        while (i7 < i) {
            long j10 = iArr[i7];
            int i8 = i3;
            long j11 = j6;
            long j12 = iArr2[i7];
            long jC = ProtoMessageBase.c(j3, j12, j * j10, j9);
            long j13 = j7;
            long jC2 = ProtoMessageBase.c(j13, j12, j11 * j10, j8);
            int i9 = i7 - 1;
            iArr[i9] = ((int) jC) & 1073741823;
            j9 = jC >> 30;
            iArr2[i9] = ((int) jC2) & 1073741823;
            j8 = jC2 >> 30;
            i7++;
            i3 = i8;
            j7 = j13;
            j6 = j11;
        }
        int i10 = i - i3;
        iArr[i10] = (int) j9;
        iArr2[i10] = (int) j8;
    }
}
