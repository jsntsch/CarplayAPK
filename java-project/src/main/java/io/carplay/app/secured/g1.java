package io.carplay.app.secured;

import java.util.function.BooleanSupplier;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class g1 implements BooleanSupplier {
    public final /* synthetic */ int a;

    public /* synthetic */ g1(int i) {
        this.a = i;
    }

    @Override // java.util.function.BooleanSupplier
    public final boolean getAsBoolean() {
        switch (this.a) {
            case 0:
                return Iap2WirelessSetup.p();
            default:
                return Iap2WirelessSetup.r();
        }
    }
}
