package io.carplay.app.secured;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class u6 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ io.carplay.app.prototype.PairingSession b;

    public /* synthetic */ u6(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        io.carplay.app.prototype.PairingSession r0Var = this.b;
        switch (i) {
            case 0:
                r0Var.T0();
                break;
            case p1.c /* 1 */:
                r0Var.W0();
                break;
            case 2:
                r0Var.S0();
                break;
            case p1.d /* 3 */:
                r0Var.Q0();
                break;
            case 4:
                r0Var.d1();
                break;
            case 5:
                r0Var.L1();
                break;
            case 6:
                r0Var.D1();
                break;
            case 7:
                r0Var.P0();
                break;
            case 8:
                r0Var.Z0();
                break;
            case 9:
                r0Var.e1();
                break;
            case 10:
                r0Var.h1();
                break;
            case 11:
                r0Var.D1();
                break;
            default:
                r0Var.Q1();
                break;
        }
    }
}
