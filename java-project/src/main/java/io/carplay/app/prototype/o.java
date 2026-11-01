package io.carplay.app.prototype;

import android.net.LinkAddress;
import java.util.function.Predicate;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class o implements Predicate {
    public /* synthetic */ boolean a;

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return WifiNetworkMonitor.t(this.a, (LinkAddress) obj);
    }
}
