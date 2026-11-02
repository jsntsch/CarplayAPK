package io.carplay.app.secured;

import io.carplay.app.prototype.AirplayServiceProbe;
import java.util.concurrent.Executor;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class u0 implements Executor {
    public /* synthetic */ AirplayServiceProbe a;

    @Override // java.util.concurrent.Executor
    public final void execute(Runnable runnable) {
        this.a.v(runnable);
    }
}
