package io.carplay.app.secured;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class b0 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ AirPlaySession b;

    public /* synthetic */ b0(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        AirPlaySession i0Var = this.b;
        switch (i) {
            case 0:
                i0Var.c1();
                break;
            case p1.c /* 1 */:
                i0Var.N0();
                break;
            case 2:
                i0Var.r2();
                break;
            case p1.d /* 3 */:
                i0Var.u2();
                break;
            case 4:
                i0Var.P0();
                break;
            case 5:
                i0Var.f0();
                break;
            default:
                i0Var.M1();
                break;
        }
    }
}
