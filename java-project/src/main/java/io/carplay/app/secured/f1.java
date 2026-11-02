package io.carplay.app.secured;

import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.function.Supplier;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class f1 implements Supplier {
    public final /* synthetic */ int a;

    public /* synthetic */ f1(int i) {
        this.a = i;
    }

    @Override // java.util.function.Supplier
    public final Object get() {
        switch (this.a) {
            case 0:
                return Iap2WirelessSetup.o();
            case p1.c /* 1 */:
                return Iap2WirelessSetup.q();
            case 2:
                return x.z();
            case p1.d /* 3 */:
                return AirPlaySession.X0();
            case 4:
                return i5.c.d();
            default:
                return WirelessCarPlayService.S1();
        }
    }
}
