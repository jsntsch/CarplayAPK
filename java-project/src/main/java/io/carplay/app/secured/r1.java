package io.carplay.app.secured;

import io.carplay.app.prototype.CarPlusApplication;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class r1 implements Thread.UncaughtExceptionHandler {
    public /* synthetic */ CarPlusApplication a;
    public /* synthetic */ Thread.UncaughtExceptionHandler b;

    @Override // java.lang.Thread.UncaughtExceptionHandler
    public final void uncaughtException(Thread thread, Throwable th) {
        this.a.b(this.b, thread, th);
    }
}
