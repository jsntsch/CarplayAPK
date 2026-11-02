package io.carplay.app.secured;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class e0 implements BooleanSupplier {
    public /* synthetic */ AtomicBoolean a;

    @Override // java.util.function.BooleanSupplier
    public final boolean getAsBoolean() {
        return this.a.get();
    }
}
