package io.carplay.app.secured;

import java.util.function.BiFunction;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class w implements BiFunction {
    public final /* synthetic */ int a;

    public /* synthetic */ w(int i) {
        this.a = i;
    }

    @Override // java.util.function.BiFunction
    public final Object apply(Object obj, Object obj2) {
        Integer num = (Integer) obj;
        switch (this.a) {
            case 0:
                return Integer.valueOf(Integer.sum(num.intValue(), ((Integer) obj2).intValue()));
            case p1.c /* 1 */:
                return t4.F(num, (Integer) obj2);
            default:
                return Integer.valueOf(Integer.sum(num.intValue(), ((Integer) obj2).intValue()));
        }
    }
}
