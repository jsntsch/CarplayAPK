package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public class o2 extends IllegalStateException {
    private Throwable a;

    public o2(String str, Throwable th) {
        super(str);
        this.a = th;
    }

    @Override // java.lang.Throwable
    public Throwable getCause() {
        return this.a;
    }
}
