package io.carplay.app.prototype;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class x implements ThreadFactory {
    @Override // java.util.concurrent.ThreadFactory
    public final Thread newThread(Runnable runnable) {
        ExecutorService executorService = UpdateChecker.a;
        Thread thread = new Thread(runnable, "CarPlus-release-check");
        thread.setDaemon(true);
        return thread;
    }
}
