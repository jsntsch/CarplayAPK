package io.carplay.app.secured;

import java.util.function.ToIntFunction;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class q implements ToIntFunction {
    public final /* synthetic */ int a;

    public /* synthetic */ q(int i) {
        this.a = i;
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        switch (this.a) {
            case 0:
                return s.m((s.a) obj);
            case p1.c /* 1 */:
                return ((x.c) obj).p();
            default:
                return ((Integer) obj).intValue();
        }
    }
}
