package io.carplay.app.prototype;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class a0 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ BluetoothRfcommProfiles b;

    public /* synthetic */ a0(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        BluetoothRfcommProfiles c0Var = this.b;
        switch (i) {
            case 0:
                c0Var.l();
                break;
            default:
                c0Var.p();
                break;
        }
    }
}
