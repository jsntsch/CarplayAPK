package io.carplay.app.secured;

import java.util.function.Function;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class r implements Function {
    public final /* synthetic */ int a;

    public /* synthetic */ r(int i) {
        this.a = i;
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        switch (this.a) {
            case 0:
                return s.n((s.a) obj);
            case p1.c /* 1 */:
                int i = x.p;
                return ((x.c) obj).h();
            case 2:
                return Boolean.valueOf(((x.c) obj).t());
            case p1.d /* 3 */:
                return PairingTlv8.f((Integer) obj);
            case 4:
                return ((io.carplay.app.prototype.PairingSession.l) obj).a;
            case 5:
                return p6.b((p6.b) obj);
            case 6:
                return ((io.carplay.app.prototype.PairingSession.l) obj).a;
            default:
                return ((io.carplay.app.prototype.PairingSession.l) obj).b;
        }
    }
}
