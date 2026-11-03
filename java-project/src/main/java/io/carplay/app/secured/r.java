package io.carplay.app.secured;

import io.carplay.app.prototype.PairingSession;
import java.util.function.Function;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class r implements Function {
    public final /* synthetic */ int a;

    public /* synthetic */ r(int i) {
        this.a = i;
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        switch (this.a) {
            case 0:
                return AudioCapabilities.n((AudioCapabilities.a) obj);
            case AudioSourceCapability.c /* 1 */:
                int i = HidCapabilities.p;
                return ((HidCapabilities.c) obj).h();
            case 2:
                return Boolean.valueOf(((HidCapabilities.c) obj).t());
            case AudioSourceCapability.d /* 3 */:
                return PairingTlv8.f((Integer) obj);
            case 4:
                return ((PairingSession.l) obj).a;
            case 5:
                return IoStatsSnapshot.b((IoStatsSnapshot.b) obj);
            case 6:
                return ((PairingSession.l) obj).a;
            default:
                return ((PairingSession.l) obj).b;
        }
    }
}
