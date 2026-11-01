package io.carplay.app.secured;

import io.carplay.app.prototype.LogActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class h3 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ LogActivity b;
    public /* synthetic */ String c;

    public /* synthetic */ h3(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                this.b.k(this.c);
                break;
            default:
                this.b.i(this.c);
                break;
        }
    }
}
