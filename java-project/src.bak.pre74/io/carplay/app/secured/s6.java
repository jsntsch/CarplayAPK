package io.carplay.app.secured;

import android.bluetooth.BluetoothDevice;
import io.carplay.app.prototype.PairingSession;
import java.util.concurrent.atomic.AtomicInteger;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class s6 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ PairingSession b;
    public /* synthetic */ BluetoothDevice c;
    public /* synthetic */ AtomicInteger d;
    public /* synthetic */ int e;

    public /* synthetic */ s6(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                this.b.b1(this.e, this.c, this.d);
                break;
            default:
                this.b.c1(this.c, this.d, this.e);
                break;
        }
    }
}
