package io.carplay.app.secured;

import android.bluetooth.BluetoothServerSocket;
import android.net.Uri;
import io.carplay.app.prototype.LogActivity;
import io.carplay.app.prototype.PairingSession;
import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.concurrent.atomic.AtomicInteger;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class p0 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;
    public /* synthetic */ Object c;

    public /* synthetic */ p0(t4 t4Var, r6.a aVar) {
        this.a = 3;
        this.b = t4Var;
        this.c = aVar;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                ((AirPlaySession) this.b).e1((i1) this.c);
                break;
            case p1.c /* 1 */:
                ((AirPlaySession) this.b).Z0((AirPlaySession.d) this.c);
                break;
            case 2:
                ((LogActivity) this.b).j((Uri) this.c);
                break;
            case p1.d /* 3 */:
                ((t4) this.b).E((r6.a) this.c);
                break;
            case 4:
                ((h5) this.b).p((BluetoothServerSocket) this.c);
                break;
            case 5:
                ((PairingSession) this.b).l1((AtomicInteger) this.c);
                break;
            default:
                ((WirelessCarPlayService) this.b).V1((u5.a) this.c);
                break;
        }
    }

    public /* synthetic */ p0(int i) {
        this.a = i;
    }
}
