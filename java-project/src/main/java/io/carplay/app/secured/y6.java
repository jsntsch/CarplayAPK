package io.carplay.app.secured;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class y6 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ io.carplay.app.prototype.PairingSession b;
    public /* synthetic */ String c;

    public /* synthetic */ y6(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                this.b.q1(this.c);
                break;
            default:
                this.b.p1(this.c);
                break;
        }
    }
}
