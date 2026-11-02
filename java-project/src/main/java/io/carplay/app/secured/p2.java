package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public class p2 implements e2 {
    private final String a;
    private final int b;
    private final Object c;
    private final f2 d;

    public p2(String str, int i, Object obj, f2 f2Var) {
        this.a = str;
        this.b = i;
        this.c = obj;
        if (obj instanceof f2) {
            d0.b("params should not be CryptoServicePurpose");
            throw null;
        }
        this.d = f2Var;
    }
}
