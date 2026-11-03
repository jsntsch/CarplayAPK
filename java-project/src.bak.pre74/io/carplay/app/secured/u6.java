package io.carplay.app.secured;

import io.carplay.app.prototype.PairingSession;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class u6 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ PairingSession b;

    public /* synthetic */ u6(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        PairingSession pairingSession = this.b;
        switch (i) {
            case 0:
                pairingSession.T0();
                break;
            case p1.c /* 1 */:
                pairingSession.W0();
                break;
            case 2:
                pairingSession.S0();
                break;
            case p1.d /* 3 */:
                pairingSession.Q0();
                break;
            case 4:
                pairingSession.d1();
                break;
            case 5:
                pairingSession.L1();
                break;
            case 6:
                pairingSession.D1();
                break;
            case 7:
                pairingSession.P0();
                break;
            case 8:
                pairingSession.Z0();
                break;
            case 9:
                pairingSession.e1();
                break;
            case 10:
                pairingSession.h1();
                break;
            case 11:
                pairingSession.D1();
                break;
            default:
                pairingSession.Q1();
                break;
        }
    }
}
