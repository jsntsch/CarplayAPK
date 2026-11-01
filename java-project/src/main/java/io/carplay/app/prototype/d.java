package io.carplay.app.prototype;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class d implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ int b;
    public /* synthetic */ Object c;

    public /* synthetic */ d(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                ((AvcEncoder) this.c).A(this.b);
                break;
            default:
                ((PairingSession.e) this.c).d(this.b);
                break;
        }
    }
}
