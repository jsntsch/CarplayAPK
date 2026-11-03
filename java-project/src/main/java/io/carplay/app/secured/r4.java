package io.carplay.app.secured;

import java.util.function.Consumer;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class r4 implements Consumer {
    public final /* synthetic */ int a;
    public /* synthetic */ DisplaySession.c b;

    /* smali 中为 public synthetic constructor <init>(I)V，jadx 漏生成 */
    public r4(int i) {
        this.a = i;
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        int i = this.a;
        this.b.a((String) obj);
    }
}
