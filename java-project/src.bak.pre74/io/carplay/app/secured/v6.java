package io.carplay.app.secured;

import android.bluetooth.BluetoothDevice;
import io.carplay.app.prototype.PairingSession;
import java.util.concurrent.atomic.AtomicInteger;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class v6 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ PairingSession b;
    public /* synthetic */ BluetoothDevice c;
    public /* synthetic */ AtomicInteger d;

    public /* synthetic */ v6(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                this.b.X0(this.c, this.d);
                break;
            case p1.c /* 1 */:
                this.b.n1(this.c, this.d);
                break;
            case 2:
                this.b.m1(this.c, this.d);
                break;
            default:
                this.b.Y0(this.c, this.d);
                break;
        }
    }
}
