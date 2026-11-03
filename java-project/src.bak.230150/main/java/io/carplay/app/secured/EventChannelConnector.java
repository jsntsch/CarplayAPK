package io.carplay.app.secured;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.LongSupplier;

/* JADX INFO: loaded from: classes.dex */
/**
 * EventChannelConnector
 *
 * <p>原混淆名：{@code io.carplay.app.secured.s2}（jadx 产物 s2.java）
 *
 * <p>命名依据：EVENT_CONNECT_CANCELLED / NO_SOCKET / INTERRUPTED —— 事件通道重试外壳
 */
final class EventChannelConnector {
    private static final long a = 2000000000;
    private static final long b = 500000000;

    @FunctionalInterface
    public interface a {
        Socket a(int i) throws IOException;
    }

    @FunctionalInterface
    public interface b {
        void a(long j) throws InterruptedException;
    }

    private EventChannelConnector() {
    }

    private static void a(BooleanSupplier booleanSupplier) throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted() || booleanSupplier.getAsBoolean()) {
            throw new InterruptedIOException("EVENT_CONNECT_CANCELLED");
        }
    }

    public static Socket b(a aVar, LongSupplier longSupplier, b bVar, BooleanSupplier booleanSupplier, IntConsumer intConsumer) throws IOException {
        Objects.requireNonNull(aVar);
        Objects.requireNonNull(longSupplier);
        Objects.requireNonNull(bVar);
        Objects.requireNonNull(booleanSupplier);
        Objects.requireNonNull(intConsumer);
        long asLong = longSupplier.getAsLong();
        int i = 10000;
        int i2 = 1;
        while (true) {
            a(booleanSupplier);
            long asLong2 = longSupplier.getAsLong();
            try {
                Socket socketA = aVar.a(i);
                if (socketA == null) {
                    throw new IOException("EVENT_CONNECT_NO_SOCKET");
                }
                try {
                    a(booleanSupplier);
                    return socketA;
                } catch (InterruptedIOException e) {
                    try {
                        socketA.close();
                        throw e;
                    } catch (IOException e2) {
                        e.addSuppressed(e2);
                        throw e;
                    }
                }
            } catch (ConnectException e3) {
                a(booleanSupplier);
                long asLong3 = longSupplier.getAsLong();
                long j = asLong3 - asLong2;
                long j2 = asLong3 - asLong;
                if (i2 >= 3 || j < 0 || j > b || j2 < 0 || j2 >= a) {
                    throw e3;
                }
                long j3 = i2 == 1 ? 150L : 300L;
                if (a - j2 <= j3 * 1000000) {
                    throw e3;
                }
                try {
                    bVar.a(j3);
                    a(booleanSupplier);
                    long asLong4 = longSupplier.getAsLong() - asLong;
                    if (asLong4 < 0 || asLong4 >= a) {
                        throw e3;
                    }
                    int i3 = (int) ((a - asLong4) / 1000000);
                    if (i3 < 1) {
                        throw e3;
                    }
                    i2++;
                    try {
                        intConsumer.accept(i2);
                    } catch (RuntimeException unused) {
                    }
                    i = i3;
                } catch (InterruptedException e4) {
                    Thread.currentThread().interrupt();
                    InterruptedIOException interruptedIOException = new InterruptedIOException("EVENT_CONNECT_INTERRUPTED");
                    interruptedIOException.initCause(e4);
                    throw interruptedIOException;
                }
            }
        }
    }
}
