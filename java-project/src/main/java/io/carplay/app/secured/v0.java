package io.carplay.app.secured;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class v0 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ io.carplay.app.prototype.AirplayServiceProbe b;

    public /* synthetic */ v0(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        io.carplay.app.prototype.AirplayServiceProbe jVar = this.b;
        switch (i) {
            case 0:
                jVar.r();
                break;
            default:
                jVar.x();
                break;
        }
    }
}
