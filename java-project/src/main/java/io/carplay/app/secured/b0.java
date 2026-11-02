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
        AirPlaySession airPlaySession = this.b;
        switch (i) {
            case 0:
                airPlaySession.c1();
                break;
            case p1.c /* 1 */:
                airPlaySession.N0();
                break;
            case 2:
                airPlaySession.r2();
                break;
            case p1.d /* 3 */:
                airPlaySession.u2();
                break;
            case 4:
                airPlaySession.P0();
                break;
            case 5:
                airPlaySession.f0();
                break;
            default:
                airPlaySession.M1();
                break;
        }
    }
}
