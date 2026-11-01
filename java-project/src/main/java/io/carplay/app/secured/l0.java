package io.carplay.app.secured;

import java.util.function.Supplier;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class l0 implements Supplier {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;

    public /* synthetic */ l0(AirPlaySession.n nVar) {
        this.a = 1;
        this.b = nVar;
    }

    @Override // java.util.function.Supplier
    public final Object get() {
        int i = this.a;
        Object obj = this.b;
        switch (i) {
            case 0:
                return ((AirPlaySession.n) obj).e();
            case p1.c /* 1 */:
                return ((AirPlaySession.n) obj).e();
            default:
                return ((MfiCertificateVerifier.c) obj).e();
        }
    }

    public /* synthetic */ l0(int i) {
        this.a = i;
    }
}
