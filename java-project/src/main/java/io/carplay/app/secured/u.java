package io.carplay.app.secured;

import java.util.function.Predicate;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class u implements Predicate {
    public final /* synthetic */ int a;
    public /* synthetic */ int b;

    public /* synthetic */ u(int i) {
        this.a = i;
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        int i = this.a;
        int i2 = this.b;
        switch (i) {
            case 0:
                return x.A(i2, (x.e) obj);
            case p1.c /* 1 */:
                return x.w(i2, (x.c) obj);
            default:
                return MfiCertificateVerifier.K(i2, (Iap2Csm.e) obj);
        }
    }
}
