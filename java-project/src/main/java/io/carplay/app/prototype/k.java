package io.carplay.app.prototype;

import android.net.LinkAddress;
import java.net.InetAddress;
import java.util.function.Predicate;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class k implements Predicate {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;

    public /* synthetic */ k(int i) {
        this.a = i;
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        int i = this.a;
        Object obj2 = this.b;
        switch (i) {
            case 0:
                return WifiNetworkMonitor.w((InetAddress) obj2, (LinkAddress) obj);
            default:
                return ((WifiNetworkMonitor) obj2).v((InetAddress) obj);
        }
    }
}
