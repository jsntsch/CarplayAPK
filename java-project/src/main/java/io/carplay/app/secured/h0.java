package io.carplay.app.secured;

import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.List;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class h0 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ long b;
    public /* synthetic */ AirPlaySession c;
    public /* synthetic */ Object d;
    public /* synthetic */ AutoCloseable e;

    public /* synthetic */ h0(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                this.c.I0((List) this.d, (EventChannel) this.e, this.b);
                break;
            default:
                ((WirelessCarPlayService) this.d).P1(this.b, this.c, (t4) this.e);
                break;
        }
    }
}
