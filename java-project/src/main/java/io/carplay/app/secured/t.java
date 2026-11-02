package io.carplay.app.secured;

import java.io.IOException;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public interface t extends AutoCloseable {

    @FunctionalInterface
    public interface a {
        t a(int i, int i2) throws IOException;
    }

    @Override // java.lang.AutoCloseable
    void close();

    List<byte[]> j(byte[] bArr) throws IOException;

    void reset() throws IOException;
}
