package io.carplay.app.secured;

import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.Map;
import java.util.function.ToLongFunction;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class p implements ToLongFunction {
    public final /* synthetic */ int a;

    public /* synthetic */ p(int i) {
        this.a = i;
    }

    @Override // java.util.function.ToLongFunction
    public final long applyAsLong(Object obj) {
        switch (this.a) {
            case 0:
                return ((s.a) obj).i();
            case p1.c /* 1 */:
                return s.o((s.a) obj);
            case 2:
                return x.y((x.c) obj);
            case p1.d /* 3 */:
                return p1.i((x.c) obj);
            default:
                return WirelessCarPlayService.a2((Map.Entry) obj);
        }
    }
}
