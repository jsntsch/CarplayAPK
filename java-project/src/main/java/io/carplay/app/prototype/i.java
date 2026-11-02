package io.carplay.app.prototype;

import android.net.LinkProperties;
import java.net.InetAddress;
import java.net.ServerSocket;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class i implements CarPlayCtrlAdvertiser.c {
    public /* synthetic */ LinkProperties a;

    @Override // io.carplay.app.prototype.CarPlayCtrlAdvertiser.c
    public final ServerSocket a(InetAddress inetAddress, int i) {
        return CarPlayCtrlAdvertiser.k(this.a, inetAddress, i);
    }
}
