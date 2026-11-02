package io.carplay.app.secured;

import java.util.function.IntPredicate;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class k implements IntPredicate {
    public final /* synthetic */ int a;

    public /* synthetic */ k(int i) {
        this.a = i;
    }

    @Override // java.util.function.IntPredicate
    public final boolean test(int i) {
        switch (this.a) {
            case 0:
                return BinaryPlist.g(i);
            case p1.c /* 1 */:
                return ControlRequestParser.b(i);
            case 2:
                return Character.isISOControl(i);
            default:
                return Iap2Csm.k(i);
        }
    }
}
