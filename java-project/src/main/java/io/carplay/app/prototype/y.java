package io.carplay.app.prototype;

import java.util.concurrent.ExecutorService;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class y implements Runnable {
    public /* synthetic */ UpdateChecker.a a;
    public /* synthetic */ boolean b;

    @Override // java.lang.Runnable
    public final void run() {
        UpdateChecker.a aVar = this.a;
        boolean z = this.b;
        ExecutorService executorService = UpdateChecker.a;
        aVar.a(UpdateChecker);
    }
}
