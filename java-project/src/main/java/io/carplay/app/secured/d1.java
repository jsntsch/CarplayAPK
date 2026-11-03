package io.carplay.app.secured;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import io.carplay.app.prototype.LogActivity;
import java.net.Socket;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class d1 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;

    public /* synthetic */ d1(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        Object obj = this.b;
        switch (i) {
            case 0:
                ((Iap2WirelessSetup) obj).t();
                break;
            case AudioSourceCapability.c /* 1 */:
                ((Surface) obj).release();
                break;
            case 2:
                ((SurfaceTexture) obj).release();
                break;
            case AudioSourceCapability.d /* 3 */:
                AirPlaySession.b1((Socket) obj);
                break;
            case 4:
                ((LogActivity) obj).h();
                break;
            default:
                ((BtDiscovery) obj).l();
                break;
        }
    }
}
