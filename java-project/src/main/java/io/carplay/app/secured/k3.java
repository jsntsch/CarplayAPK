package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public abstract class k3 {
    public static int a(long j, long j2) {
        if (j < j2) {
            return -1;
        }
        return j == j2 ? 0 : 1;
    }

    public static int b(long j, long j2) {
        return a(j - Long.MIN_VALUE, j2 - Long.MIN_VALUE);
    }
}
