package io.carplay.app.secured;

import io.carplay.app.prototype.AirplayServiceProbe;
import io.carplay.app.prototype.PairingSession;
import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.concurrent.ThreadFactory;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class n0 implements ThreadFactory {
    public final /* synthetic */ int a;

    public /* synthetic */ n0(int i) {
        this.a = i;
    }

    @Override // java.util.concurrent.ThreadFactory
    public final Thread newThread(Runnable runnable) {
        switch (this.a) {
            case 0:
                return AirPlaySession.U0(runnable);
            case AudioSourceCapability.c /* 1 */:
                return AirPlaySession.V0(runnable);
            case 2:
                return AirplayServiceProbe.u(runnable);
            case AudioSourceCapability.d /* 3 */:
                return DisplaySession.C(runnable);
            case 4:
                return BtDiscovery.o(runnable);
            case 5:
                return PairingSession.R0(runnable);
            case 6:
                return WirelessCarPlayService.N1(runnable);
            default:
                return WirelessCarPlayService.O1(runnable);
        }
    }
}
