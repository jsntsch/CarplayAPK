package io.carplay.app.secured;

import android.net.Network;
import io.carplay.app.prototype.WirelessCarPlayService;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class g7 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ WirelessCarPlayService b;
    public /* synthetic */ u5.a c;
    public /* synthetic */ Network d;
    public /* synthetic */ Iap2Csm.a e;
    public /* synthetic */ String f;

    public /* synthetic */ g7(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                this.b.U1(this.d, this.e, this.f, this.c);
                break;
            default:
                this.b.T1(this.c, this.d, this.e, this.f);
                break;
        }
    }
}
