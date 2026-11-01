package io.carplay.app.secured;

import java.io.IOException;
import java.util.Map;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class d0 implements EventChannelConnector.b, AirPlaySession.l {
    public static /* synthetic */ void b(String str) {
        throw new IllegalArgumentException(str);
    }

    public static /* synthetic */ void c(String str) throws IOException {
        throw new IOException(str);
    }

    public static /* synthetic */ void d(String str) {
        throw new IllegalStateException(str);
    }

    public static /* synthetic */ void f(String str) throws r5 {
        throw new r5(str);
    }

    @Override // io.carplay.app.secured.EventChannelConnector.b
    public void a(long j) throws InterruptedException {
        Thread.sleep(j);
    }

    @Override // io.carplay.app.secured.AirPlaySession.l
    public void e(Map map) {
        AirPlaySession.R0(map);
    }
}
