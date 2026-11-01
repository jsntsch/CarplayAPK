package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public abstract class m {
    public static int a(int i, int i2, int i3) {
        int i4 = i2 & ((i >>> i3) ^ i);
        return i ^ (i4 ^ (i4 << i3));
    }
}
