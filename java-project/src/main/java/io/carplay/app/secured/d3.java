package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public abstract class d3 {
    public static int a(int i) {
        return 32 - d(i);
    }

    public static int b(int i, int i2) {
        if (i < i2) {
            return -1;
        }
        return i == i2 ? 0 : 1;
    }

    public static int c(int i, int i2) {
        return b(i - Integer.MIN_VALUE, i2 - Integer.MIN_VALUE);
    }

    public static int d(int i) {
        return Integer.numberOfLeadingZeros(i);
    }

    public static int e(int i) {
        return Integer.numberOfTrailingZeros(i);
    }

    public static int f(int i, int i2) {
        return Integer.rotateLeft(i, i2);
    }
}
