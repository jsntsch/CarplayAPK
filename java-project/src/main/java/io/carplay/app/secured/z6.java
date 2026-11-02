package io.carplay.app.secured;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import io.carplay.app.prototype.PairingSession;
import java.util.concurrent.atomic.AtomicInteger;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class z6 implements Runnable {
    public /* synthetic */ PairingSession a;
    public /* synthetic */ BluetoothServerSocket b;
    public /* synthetic */ BluetoothDevice c;
    public /* synthetic */ AtomicInteger d;

    @Override // java.lang.Runnable
    public final void run() throws Throwable {
        this.a.i1(this.b, this.c, this.d);
    }
}
