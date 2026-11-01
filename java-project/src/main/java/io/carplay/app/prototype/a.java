package io.carplay.app.prototype;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class a implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ AvcEncoder b;

    public /* synthetic */ a(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        AvcEncoder fVar = this.b;
        switch (i) {
            case 0:
                fVar.I();
                break;
            default:
                fVar.B();
                break;
        }
    }
}
