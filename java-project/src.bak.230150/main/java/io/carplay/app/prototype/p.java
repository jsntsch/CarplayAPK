package io.carplay.app.prototype;

import android.app.Activity;
import io.carplay.app.secured.p1;
import java.util.concurrent.ExecutorService;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class p implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;

    public /* synthetic */ p(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        Object obj = this.b;
        switch (i) {
            case 0:
                ((WifiNetworkMonitor) obj).u();
                break;
            case p1.c /* 1 */:
                ExecutorService executorService = UpdateChecker.a;
                ((UpdateChecker.a) obj).a(false);
                break;
            case 2:
                UiFactory.r((Activity) obj);
                break;
            default:
                int i2 = PairingSession.d.d;
                ((PairingSession) obj).H0();
                break;
        }
    }
}
