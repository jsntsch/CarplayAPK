package io.carplay.app.secured;

import java.util.function.Function;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class o1 implements Function {
    public /* synthetic */ HidCapabilities.c a;

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return AudioSourceCapability.h(this.a, (HidCapabilities.c) obj);
    }
}
