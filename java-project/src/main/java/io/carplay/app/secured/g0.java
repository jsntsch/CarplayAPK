package io.carplay.app.secured;

import java.util.function.BiConsumer;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class g0 implements BiConsumer {
    public /* synthetic */ AirPlaySession a;
    public /* synthetic */ long b;
    public /* synthetic */ EventChannel c;
    public /* synthetic */ int d;

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        this.a.J0(this.b, this.c, this.d, (EventChannel.b) obj, (Throwable) obj2);
    }
}
