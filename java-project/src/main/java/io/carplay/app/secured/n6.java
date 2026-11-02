package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
abstract class n6 {

    public static class a implements e2 {
        private final int a;
        private final int b;
        private final String c;
        private final f2 d;

        public a(int i, int i2, String str, f2 f2Var) {
            this.a = i;
            this.b = i2;
            this.c = str;
            this.d = f2Var;
        }
    }

    public static e2 a(q2 q2Var, int i, f2 f2Var) {
        return new a(q2Var.d() * 4, i, q2Var.c(), f2Var);
    }
}
