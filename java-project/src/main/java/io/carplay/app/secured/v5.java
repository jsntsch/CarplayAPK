package io.carplay.app.secured;

import java.util.function.Predicate;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class v5 implements Predicate {
    public /* synthetic */ long a;

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((ReleaseCatalog.a) obj).a == this.a;
    }
}
