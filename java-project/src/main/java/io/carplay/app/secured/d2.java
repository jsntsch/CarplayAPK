package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public abstract class d2 extends Exception {
    private Throwable a;

    public d2(String str) {
        super(str);
    }

    @Override // java.lang.Throwable
    public Throwable getCause() {
        return this.a;
    }
}
