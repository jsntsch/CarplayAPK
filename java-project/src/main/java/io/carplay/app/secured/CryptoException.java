package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public abstract class CryptoException extends Exception {
    private Throwable a;

    public CryptoException(String str) {
        super(str);
    }

    @Override // java.lang.Throwable
    public Throwable getCause() {
        return this.a;
    }
}
