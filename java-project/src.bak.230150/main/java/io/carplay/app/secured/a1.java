package io.carplay.app.secured;

import java.util.Set;
import java.util.function.Predicate;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class a1 implements Predicate {
    public final /* synthetic */ int a;
    public /* synthetic */ Set b;

    public /* synthetic */ a1(int i) {
        this.a = i;
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        int i = this.a;
        Set set = this.b;
        switch (i) {
            case 0:
                return SessionStateMachine.B(set, obj);
            default:
                return set.contains(obj);
        }
    }
}
