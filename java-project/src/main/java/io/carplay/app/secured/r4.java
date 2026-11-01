package io.carplay.app.secured;

import java.util.function.Consumer;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class r4 implements Consumer {
    public final /* synthetic */ int a;
    public /* synthetic */ t4.c b;

    public /* synthetic */ r4(int i) {
        this.a = i;
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        int i = this.a;
        this.b.a((String) obj);
    }
}
