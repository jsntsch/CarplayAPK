package io.carplay.app.secured;

import android.bluetooth.BluetoothSocketException;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public abstract /* synthetic */ class t0 {
    public static /* bridge */ /* synthetic */ BluetoothSocketException b(Exception exc) {
        return (BluetoothSocketException) exc;
    }

    public static /* bridge */ /* synthetic */ boolean e(Exception exc) {
        return exc instanceof BluetoothSocketException;
    }
}
