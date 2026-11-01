package io.carplay.app.secured;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class j0 implements Iap2WirelessSetup.c, AirPlaySession.l, EventChannel.c, EventChannel.e, VideoCipher.b.a {
    public final /* synthetic */ int a;
    public /* synthetic */ AirPlaySession b;

    public /* synthetic */ j0(AirPlaySession i0Var) {
        this.a = 3;
        this.b = i0Var;
    }

    @Override // io.carplay.app.secured.EventChannel.c
    public byte[] a(EventChannel.b bVar) {
        int i = this.a;
        AirPlaySession i0Var = this.b;
        switch (i) {
            case 4:
                return i0Var.S0(bVar);
            default:
                return i0Var.O0(bVar);
        }
    }

    @Override // io.carplay.app.secured.Iap2WirelessSetup.c
    public Iap2WirelessSetup.b b() {
        int i = this.a;
        AirPlaySession i0Var = this.b;
        switch (i) {
            case 0:
                return i0Var.x1();
            case p1.c /* 1 */:
            default:
                return i0Var.z1();
            case 2:
                return i0Var.z1();
        }
    }

    @Override // io.carplay.app.secured.EventChannel.e
    public void c(String str, int i, int i2, boolean z) {
        this.b.T0(str, i, i2, z);
    }

    @Override // io.carplay.app.secured.VideoCipher.b.a
    public void d(List list) throws IOException {
        this.b.Y0(list);
    }

    @Override // io.carplay.app.secured.AirPlaySession.l
    public void e(Map map) throws IOException {
        this.b.K0(map);
    }

    public /* synthetic */ j0(int i) {
        this.a = i;
    }
}
