package io.carplay.app.secured;

import android.bluetooth.BluetoothSocket;
import io.carplay.app.prototype.PairingSession;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class w6 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ PairingSession b;
    public /* synthetic */ BluetoothSocket c;

    public /* synthetic */ w6(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() throws Throwable {
        switch (this.a) {
            case 0:
                this.b.O0(this.c);
                break;
            case p1.c /* 1 */:
                this.b.V0(this.c);
                break;
            case 2:
                this.b.g1(this.c);
                break;
            default:
                this.b.k1(this.c);
                break;
        }
    }
}
