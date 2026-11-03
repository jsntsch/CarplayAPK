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
            case AudioSourceCapability.c /* 1 */:
                return Iap2WirelessSetup.q();
            case 2:
                return HidCapabilities.z();
            case AudioSourceCapability.d /* 3 */:
                return AirPlaySession.X0();
            case 4:
                return DeviceMetadataStore.c.d();
            default:
                return WirelessCarPlayService.S1();
        }
    }
}
