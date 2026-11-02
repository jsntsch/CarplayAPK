package io.carplay.app.secured;

import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public interface t extends AutoCloseable {

    @FunctionalInterface
    public interface a {
        t a(int i, int i2);
    }

    @Override // java.lang.AutoCloseable
    void close();

    List<byte[]> j(byte[] bArr);

    void reset();
}
