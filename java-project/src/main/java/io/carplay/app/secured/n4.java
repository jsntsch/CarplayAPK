package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public abstract class n4 {
    public static int a(int i, int i2, int[] iArr, int[] iArr2) {
        long j = ((long) (-(i2 & 1))) & 4294967295L;
        long j2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            long j3 = (((long) iArr2[i3]) & 4294967295L) + (((long) iArr[i3]) & j) + j2;
            iArr2[i3] = (int) j3;
            j2 = j3 >>> 32;
        }
        return (int) j2;
    }

    public static int b(int i) {
        return ((~i) & (i - 1)) >> 31;
    }

    public static int c(int i, int[] iArr) {
        for (int i2 = i - 1; i2 >= 0; i2--) {
            int i3 = iArr[i2];
            if (i3 != 0) {
                return d3.a(i3) + (i2 * 32);
            }
        }
        return 0;
    }

    public static int d(int i, int[] iArr, int i2) {
        while (true) {
            i--;
            if (i < 0) {
                return i2 << 31;
            }
            int i3 = iArr[i];
            iArr[i] = (i2 << 31) | (i3 >>> 1);
            i2 = i3;
        }
    }
}
