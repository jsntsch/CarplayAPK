package io.carplay.app.secured;

import java.io.IOException;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
/**
 * PcmQueue
 *
 * <p>原混淆名：{@code io.carplay.app.secured.e5}（jadx 产物 e5.java）
 *
 * <p>命名依据：PCM_QUEUE_FORMAT / PCM_QUEUE_PACKET
 */
public final class PcmQueue implements AutoCloseable {
    private final int a;
    private final int b;
    private final int c;
    private final boolean d;
    private final byte[] e;
    private int f;
    private int g;
    private long h;
    private long i;
    private boolean j;

    private PcmQueue(int i, int i2, long j, int i3, boolean z) throws IOException {
        if (s.v(i, i2, 16) == 0 || j < 0) {
            throw f("PCM_QUEUE_FORMAT");
        }
        if (i3 < 1 || (i3 > 352 && i3 != 1024)) {
            throw f("PCM_QUEUE_PACKET");
        }
        int i4 = i2 * 2;
        this.b = i4;
        int i5 = (i + 3) / 4;
        this.a = i5;
        this.e = new byte[i5 * i4];
        this.h = j;
        this.c = i3;
        this.d = z;
    }

    private void c() throws IOException {
        if (this.j) {
            throw f("PCM_QUEUE_CLOSED");
        }
    }

    private static IOException f(String str) {
        return new IOException(str);
    }

    private a m() {
        int i;
        if (this.j || (i = this.g) == 0) {
            return null;
        }
        if (this.d && i < this.c) {
            return null;
        }
        int iMin = Math.min(this.c, i);
        int iMin2 = Math.min(iMin, this.a - this.f);
        int i2 = this.b;
        byte[] bArr = new byte[iMin * i2];
        System.arraycopy(this.e, this.f * i2, bArr, 0, i2 * iMin2);
        byte[] bArr2 = this.e;
        int i3 = this.b;
        System.arraycopy(bArr2, 0, bArr, iMin2 * i3, (iMin - iMin2) * i3);
        this.f = (this.f + iMin) % this.a;
        this.g -= iMin;
        long j = this.i;
        this.i = 0L;
        return new a(this, this.h, bArr, iMin, j, 0);
    }

    public int b() {
        return this.a;
    }

    @Override // java.lang.AutoCloseable
    public synchronized void close() {
        if (this.j) {
            return;
        }
        this.j = true;
        this.f = 0;
        this.g = 0;
        this.i = 0L;
        Arrays.fill(this.e, (byte) 0);
        notifyAll();
    }

    public synchronized long e() {
        return this.h;
    }

    public synchronized boolean g(a aVar) {
        return !this.j && aVar != null && aVar.a == this && aVar.b() == this.h;
    }

    public synchronized long i(byte[] bArr) throws IOException {
        long j = 0L;
        c();
        if (bArr == null || bArr.length > 1048576 || bArr.length % this.b != 0) {
            throw f("PCM_QUEUE_INPUT");
        }
        {
            int length = bArr.length;
            int i = this.b;
            if (length % i == 0) {
                int length2 = bArr.length / i;
                int iMax = Math.max(0, (this.g + length2) - this.a);
                try {
                    j = iMax;
                    long jAddExact = Math.addExact(this.i, j);
                    int iMin = Math.min(this.g, iMax);
                    int i2 = iMax - iMin;
                    int i3 = this.f + iMin;
                    int i4 = this.a;
                    int i5 = i3 % i4;
                    this.f = i5;
                    int i6 = this.g - iMin;
                    this.g = i6;
                    this.i = jAddExact;
                    int i7 = length2 - i2;
                    int i8 = (i5 + i6) % i4;
                    int iMin2 = Math.min(i7, i4 - i8);
                    int i9 = this.b;
                    System.arraycopy(bArr, i2 * i9, this.e, i8 * i9, i9 * iMin2);
                    int i10 = this.b;
                    System.arraycopy(bArr, (i2 + iMin2) * i10, this.e, 0, (i7 - iMin2) * i10);
                    int i11 = this.g + i7;
                    this.g = i11;
                    if (i11 > 0) {
                        notifyAll();
                    }
                } catch (ArithmeticException unused) {
                    throw f("PCM_QUEUE_SKIP_LIMIT");
                }
            }
        }
        return j;
    }

    public synchronized a k() {
        return m();
    }

    public synchronized int l() {
        return this.g;
    }

    public synchronized void n(long j) throws IOException {
        o(j);
    }

    public synchronized long o(long j) throws IOException {
        long jAddExact;
        c();
        if (j <= this.h) {
            throw f("PCM_QUEUE_EPOCH");
        }
        try {
            jAddExact = Math.addExact(this.i, this.g);
            this.h = j;
            this.f = 0;
            this.g = 0;
            this.i = 0L;
            Arrays.fill(this.e, (byte) 0);
            notifyAll();
        } catch (ArithmeticException unused) {
            throw f("PCM_QUEUE_SKIP_LIMIT");
        }
        return jAddExact;
    }

    public synchronized a p() {
        while (true) {
            int i = this.g;
            if (i != 0 && (!this.d || i >= this.c)) {
                break;
            }
            if (this.j) {
                break;
            }
            try {
                wait();
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return m();
    }

    public static final class a {
        private final PcmQueue a;
        private final long b;
        private final long c;
        private final byte[] d;
        private final int e;

        private a(PcmQueue pcmQueue, long j, byte[] bArr, int i, long j2) {
            this.a = pcmQueue;
            this.b = j;
            this.d = bArr;
            this.e = i;
            this.c = j2;
        }

        public long b() {
            return this.b;
        }

        public int c() {
            return this.e;
        }

        public byte[] d() {
            return (byte[]) this.d.clone();
        }

        public long e() {
            return this.c;
        }

        public /* synthetic */ a(PcmQueue pcmQueue, long j, byte[] bArr, int i, long j2, int i2) {
            this(pcmQueue, j, bArr, i, j2);
        }
    }

    public PcmQueue(int i, int i2, long j) throws IOException {
        this(i, i2, j, 240, false);
    }

    public PcmQueue(int i, int i2, long j, int i3) throws IOException {
        this(i, i2, j, i3, true);
    }

    public PcmQueue(int i, int i2) throws IOException {
        this(i, i2, 0L);
    }
}
