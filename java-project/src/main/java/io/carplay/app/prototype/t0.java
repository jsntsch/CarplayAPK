package io.carplay.app.prototype;

import android.net.Network;
import io.carplay.app.secured.Iap2Csm;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class t0 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ Network b;
    public /* synthetic */ Iap2Csm.a c;
    public /* synthetic */ String d;
    public /* synthetic */ long e;
    public /* synthetic */ Object f;

    public /* synthetic */ t0(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() throws Throwable {
        switch (this.a) {
            case 0:
                ((WirelessCarPlayService.a) this.f).x(this.b, this.c, this.d, this.e);
                break;
            default:
                ((WirelessCarPlayService.b) this.f).d(this.b, this.c, this.d, this.e);
                break;
        }
    }
}
