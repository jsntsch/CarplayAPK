package io.carplay.app.secured;

import java.util.function.BooleanSupplier;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class m0 implements BooleanSupplier {
    public /* synthetic */ AirPlaySession a;
    public /* synthetic */ EventChannel b;

    @Override // java.util.function.BooleanSupplier
    public final boolean getAsBoolean() {
        return this.a.H0(this.b);
    }
}
