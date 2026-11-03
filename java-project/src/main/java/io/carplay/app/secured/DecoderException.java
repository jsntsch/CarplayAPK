package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public class DecoderException extends IllegalStateException {
    private Throwable a;

    public DecoderException(String str, Throwable th) {
        super(str);
        this.a = th;
    }

    @Override // java.lang.Throwable
    public Throwable getCause() {
        return this.a;
    }
}
