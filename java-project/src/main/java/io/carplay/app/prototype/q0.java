package io.carplay.app.prototype;

import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class q0 implements Runnable {
    public /* synthetic */ PairingSession a;
    public /* synthetic */ InputStream b;
    public /* synthetic */ ArrayBlockingQueue c;
    public /* synthetic */ PairingSession.j d;

    @Override // java.lang.Runnable
    public final void run() {
        this.a.a1(this.b, this.c, this.d);
    }
}
