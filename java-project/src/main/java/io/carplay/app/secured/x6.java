package io.carplay.app.secured;

import android.net.Network;
import io.carplay.app.prototype.PairingSession;
import java.net.InetAddress;
import java.util.function.Predicate;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class x6 implements Predicate {
    public /* synthetic */ PairingSession a;
    public /* synthetic */ Network b;
    public /* synthetic */ String c;

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return this.a.o1(this.b, this.c, (InetAddress) obj);
    }
}
