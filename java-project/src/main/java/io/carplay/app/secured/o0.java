package io.carplay.app.secured;

import java.net.DatagramSocket;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class o0 implements KeepAliveServer.a {
    public /* synthetic */ DatagramSocket a;

    @Override // io.carplay.app.secured.i1.a
    public final void a(byte[] bArr) {
        AirPlaySession.d1(this.a, bArr);
    }
}
