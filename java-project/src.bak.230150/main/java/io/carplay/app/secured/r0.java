package io.carplay.app.secured;

import java.io.IOException;

import java.net.Socket;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class r0 implements EventChannelConnector.a {
    public /* synthetic */ AirPlaySession a;
    public /* synthetic */ int b;

    @Override // io.carplay.app.secured.EventChannelConnector.a
    public final Socket a(int i) throws IOException {
        return this.a.L0(this.b, i);
    }
}
