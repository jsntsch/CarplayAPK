package io.carplay.app.secured;

import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.Map;
import java.util.function.ToLongFunction;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class p implements ToLongFunction {
    public final /* synthetic */ int a;

    public /* synthetic */ p(int i) {
        this.a = i;
    }

    @Override // java.util.function.ToLongFunction
    public final long applyAsLong(Object obj) {
        switch (this.a) {
            case 0:
                return ((AudioCapabilities.a) obj).i();
            case AudioSourceCapability.c /* 1 */:
                return AudioCapabilities.o((AudioCapabilities.a) obj);
            case 2:
                return HidCapabilities.y((HidCapabilities.c) obj);
            case AudioSourceCapability.d /* 3 */:
                return AudioSourceCapability.i((HidCapabilities.c) obj);
            default:
                return WirelessCarPlayService.a2((Map.Entry) obj);
        }
    }
}
