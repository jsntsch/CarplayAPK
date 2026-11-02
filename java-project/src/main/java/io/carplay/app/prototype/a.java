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
        AvcEncoder avcEncoder = this.b;
        switch (i) {
            case 0:
                avcEncoder.I();
                break;
            default:
                avcEncoder.B();
                break;
        }
    }
}
