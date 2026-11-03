package io.carplay.app.secured;

import io.carplay.app.prototype.AirplayServiceProbe;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class v0 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ AirplayServiceProbe b;

    public /* synthetic */ v0(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        AirplayServiceProbe airplayServiceProbe = this.b;
        switch (i) {
            case 0:
                airplayServiceProbe.r();
                break;
            default:
                airplayServiceProbe.x();
                break;
        }
    }
}
