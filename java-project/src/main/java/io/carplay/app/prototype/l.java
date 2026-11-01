package io.carplay.app.prototype;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class l implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ WifiNetworkMonitor.a b;

    public /* synthetic */ l(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        WifiNetworkMonitor.a aVar = this.b;
        switch (i) {
            case 0:
                aVar.f();
                break;
            default:
                aVar.d();
                break;
        }
    }
}
