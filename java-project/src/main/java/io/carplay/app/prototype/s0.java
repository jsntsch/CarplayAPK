package io.carplay.app.prototype;

import android.net.Network;
import io.carplay.app.secured.Iap2Csm;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class s0 implements Runnable {
    public /* synthetic */ WirelessCarPlayService.a a;
    public /* synthetic */ Network b;
    public /* synthetic */ Iap2Csm.a c;
    public /* synthetic */ String d;
    public /* synthetic */ long e;
    public /* synthetic */ PairingSession.i f;

    @Override // java.lang.Runnable
    public final void run() {
        this.a.w(this.b, this.c, this.d, this.e, this.f);
    }
}
