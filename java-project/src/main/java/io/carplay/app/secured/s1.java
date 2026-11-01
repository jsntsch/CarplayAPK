package io.carplay.app.secured;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class s1 implements BiConsumer {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;

    public /* synthetic */ s1(int i) {
        this.a = i;
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        int i = this.a;
        Object obj3 = this.b;
        switch (i) {
            case 0:
                t1.f((ProtobufReader.a) obj3, (String) obj, (Integer) obj2);
                break;
            default:
                v1.g.i((LinkedHashMap) obj3, (String) obj, (Integer) obj2);
                break;
        }
    }
}
