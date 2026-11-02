package io.carplay.app.secured;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
final class i1 implements AutoCloseable {
    static final long e = 10000;
    private final a a;
    private long b = -1;
    private long c;
    private boolean d;

    @FunctionalInterface
    public interface a {
        void a(byte[] bArr);
    }

    public i1(a aVar) {
        Objects.requireNonNull(aVar);
        this.a = aVar;
    }

    public static Integer b(Map<?, ?> map) throws IOException {
        Object obj = map.get("keepAlivePort");
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof Byte) && !(obj instanceof Short) && !(obj instanceof Integer) && !(obj instanceof Long)) {
            SyntheticRefs.c("KEEPALIVE_PORT_INVALID");
            return null;
        }
        long jLongValue = ((Number) obj).longValue();
        if (jLongValue == 0) {
            return null;
        }
        if (jLongValue >= 1 && jLongValue <= 65535) {
            return Integer.valueOf((int) jLongValue);
        }
        SyntheticRefs.c("KEEPALIVE_PORT_INVALID");
        return null;
    }

    public synchronized long c() {
        return this.c;
    }

    @Override // java.lang.AutoCloseable
    public synchronized void close() {
        this.d = true;
    }

    public synchronized boolean e(long j) {
        if (!this.d && j >= 0) {
            long j2 = this.b;
            if (j2 >= 0 && j >= j2 && j - j2 < e) {
                return false;
            }
            this.b = j;
            this.c++;
            this.a.a(DerCodec.q(4));
            return true;
        }
        return false;
    }
}
