package io.carplay.app.secured;

import io.carplay.app.prototype.PairingPreparationService;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class a5 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ PairingPreparationService b;

    public /* synthetic */ a5(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        PairingPreparationService pairingPreparationService = this.b;
        switch (i) {
            case 0:
                pairingPreparationService.g();
                break;
            default:
                pairingPreparationService.d();
                break;
        }
    }
}
