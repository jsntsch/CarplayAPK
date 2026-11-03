package io.carplay.app.secured;

import io.carplay.app.prototype.WirelessCarPlayService;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class b7 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ WirelessCarPlayService b;

    public /* synthetic */ b7(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        WirelessCarPlayService wirelessCarPlayService = this.b;
        switch (i) {
            case 0:
                wirelessCarPlayService.b2();
                break;
            case p1.c /* 1 */:
                wirelessCarPlayService.W1();
                break;
            default:
                wirelessCarPlayService.h2();
                break;
        }
    }
}
