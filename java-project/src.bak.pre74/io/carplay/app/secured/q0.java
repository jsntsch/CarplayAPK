package io.carplay.app.secured;

import io.carplay.app.prototype.WirelessCarPlayService;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class q0 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;
    public /* synthetic */ Object c;
    public /* synthetic */ Object d;

    public /* synthetic */ q0(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                ((AirPlaySession) this.b).a1((r6.a) this.c, (AirPlaySession.d) this.d);
                break;
            case p1.c /* 1 */:
                ((WirelessCarPlayService) this.c).g2((t4.VideoConfig) this.d, (AirPlaySession) this.b);
                break;
            default:
                ((WirelessCarPlayService) this.b).Q1((String) this.c, (u5.a) this.d);
                break;
        }
    }
}
