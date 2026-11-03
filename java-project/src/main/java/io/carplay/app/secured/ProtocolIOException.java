package io.carplay.app.secured;

import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public final class ProtocolIOException extends IOException {
    private static final long a = 1;

    public ProtocolIOException(String str) {
        super(str);
    }

    public ProtocolIOException(String str, Throwable th) {
        super(str, th);
    }
}
