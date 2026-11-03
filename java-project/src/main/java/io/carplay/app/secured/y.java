package io.carplay.app.secured;

import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.Map;
import java.util.function.Predicate;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class y implements Predicate {
    public final /* synthetic */ int a;
    public /* synthetic */ z.a b;

    public /* synthetic */ y(int i) {
        this.a = i;
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        int i = this.a;
        z.a aVar = this.b;
        switch (i) {
            case 0:
                return z.f(aVar, (HidCapabilities.c) obj);
            default:
                return WirelessCarPlayService.Z1(aVar, (Map.Entry) obj);
        }
    }
}
