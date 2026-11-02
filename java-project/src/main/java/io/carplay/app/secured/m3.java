package io.carplay.app.secured;

import io.carplay.app.prototype.MainActivity;
import io.carplay.app.prototype.PairingSession;
import java.util.function.Predicate;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class m3 implements Predicate {
    public final /* synthetic */ int a;
    public /* synthetic */ String b;

    public /* synthetic */ m3(int i) {
        this.a = i;
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        int i = this.a;
        String str = this.b;
        PairingSession.l lVar = (PairingSession.l) obj;
        switch (i) {
            case 0:
                return MainActivity.R1(str, lVar);
            case p1.c /* 1 */:
                return MainActivity.G1(str, lVar);
            case 2:
                return MainActivity.Z0(str, lVar);
            default:
                return MainActivity.V0(str, lVar);
        }
    }
}
