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
                return HidCapabilities.A(i2, (HidCapabilities.e) obj);
            case AudioSourceCapability.c /* 1 */:
                return HidCapabilities.w(i2, (HidCapabilities.c) obj);
            default:
                return MfiCertificateVerifier.K(i2, (Iap2Csm.e) obj);
        }
    }
}
