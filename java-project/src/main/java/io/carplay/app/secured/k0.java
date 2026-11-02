package io.carplay.app.secured;

import java.util.function.Consumer;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class k0 implements Consumer {
    public final /* synthetic */ int a = 0;
    public /* synthetic */ AirPlaySession.n b;

    public /* synthetic */ k0(AirPlaySession.n nVar) {
        this.b = nVar;
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        int i = this.a;
        this.b.a((String) obj);
    }

    public /* synthetic */ k0() {
    }
}
