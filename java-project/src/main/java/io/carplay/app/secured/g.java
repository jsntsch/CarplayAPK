package io.carplay.app.secured;

import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public final class g {
    private static final long f = 20000000;
    private static final int g = 4;
    private final int a;
    private boolean b;
    private long c;
    private long d;
    private int e;

    public g(int i) throws IOException {
        if (s.v(i, 1, 16) != 0) {
            this.a = i;
        } else {
            d0.c("AUDIO_PACER_RATE");
            throw null;
        }
    }

    public static long a(long j, long j2) {
        long j3 = j2 - j;
        if (j3 > 0) {
            return j3;
        }
        return 0L;
    }

    public synchronized long b(long j, int i, long j2) {
        long j3;
        try {
            if (i < 1 || ((i > 352 && i != 1024) || j2 < 0)) {
                throw new IOException("AUDIO_PACER_PACKET");
            }
            long j4 = j - this.c;
            if (!this.b || j2 > 0 || j4 > f || (j4 > 0 && this.e >= g)) {
                this.b = true;
                this.c = j;
                this.d = 0L;
                this.e = 0;
            } else if (j4 > 0) {
                this.e++;
            } else {
                this.e = 0;
            }
            j3 = this.c;
            long j5 = (((long) i) * 1000000000) + this.d;
            int i2 = this.a;
            this.c = (j5 / ((long) i2)) + j3;
            this.d = j5 % ((long) i2);
        } catch (Throwable th) {
            throw th;
        }
        return j3;
    }

    public synchronized void c() {
        this.b = false;
        this.c = 0L;
        this.d = 0L;
        this.e = 0;
    }
}
