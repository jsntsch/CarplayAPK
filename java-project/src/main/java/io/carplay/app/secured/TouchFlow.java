package io.carplay.app.secured;

import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
/**
 * TouchFlow
 *
 * <p>原混淆名：{@code io.carplay.app.secured.v4}（jadx 产物 v4.java）
 *
 * <p>命名依据：NATIVE_TOUCH_FLOW down= / failed=
 */
final class TouchFlow {
    private static final int o = 128;
    private final long[] a = new long[128];
    private final long[] b = new long[128];
    private long c;
    private long d;
    private long e;
    private long f;
    private long g;
    private long h;
    private long i;
    private long j;
    private long k;
    private long l;
    private int m;
    private int n;

    public static /* synthetic */ class SwitchMap {
        static final /* synthetic */ int[] a;

        static {
            int[] iArr = new int[TouchGestureEncoder.TouchAction.values().length];
            a = iArr;
            try {
                iArr[TouchGestureEncoder.TouchAction.DOWN.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                a[TouchGestureEncoder.TouchAction.MOVE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                a[TouchGestureEncoder.TouchAction.UP.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public enum b {
        NOT_READY,
        INPUT,
        IO,
        CRYPTO
    }

    private static long b(long j) {
        return j == Long.MAX_VALUE ? j : j + 1;
    }

    private static long c(long[] jArr, int i) {
        if (jArr.length == 0) {
            return 0L;
        }
        return jArr[(((jArr.length * i) + 99) / 100) - 1];
    }

    public synchronized boolean a(b bVar) {
        boolean z;
        try {
            this.f = b(this.f);
            int iOrdinal = bVar.ordinal();
            z = true;
            if (iOrdinal == 0) {
                this.g = b(this.g);
            } else if (iOrdinal == 1) {
                this.h = b(this.h);
            } else if (iOrdinal == 2) {
                this.i = b(this.i);
            } else if (iOrdinal == 3) {
                this.j = b(this.j);
            }
            long j = this.f;
            if (j > 4 && j % 100 != 0) {
                z = false;
            }
        } catch (Throwable th) {
            throw th;
        }
        return z;
    }

    public synchronized String d() {
        long[] jArrCopyOf;
        long[] jArrCopyOf2;
        jArrCopyOf = Arrays.copyOf(this.a, this.m);
        jArrCopyOf2 = Arrays.copyOf(this.b, this.m);
        Arrays.sort(jArrCopyOf);
        Arrays.sort(jArrCopyOf2);
        return "NATIVE_TOUCH_FLOW down=" + this.c + " move=" + this.d + " up=" + this.e + " failed=" + this.f + " not_ready=" + this.g + " invalid_input=" + this.h + " io_failed=" + this.i + " crypto_failed=" + this.j + " samples=" + this.k + " window_samples=" + this.m + " invalid_samples=" + this.l + " lock_wait_p50_us=" + (c(jArrCopyOf, 50) / 1000) + " lock_wait_p95_us=" + (c(jArrCopyOf, 95) / 1000) + " lock_wait_max_us=" + (c(jArrCopyOf, 100) / 1000) + " flush_p50_us=" + (c(jArrCopyOf2, 50) / 1000) + " flush_p95_us=" + (c(jArrCopyOf2, 95) / 1000) + " flush_max_us=" + (c(jArrCopyOf2, 100) / 1000) + " local_flush_only=true native_app_response_observed=false";
    }

    public synchronized void e(TouchGestureEncoder.TouchAction aVar, long j, long j2, long j3) {
        try {
            int i = SwitchMap.a[aVar.ordinal()];
            if (i == 1) {
                this.c = b(this.c);
            } else if (i == 2) {
                this.d = b(this.d);
            } else if (i == 3) {
                this.e = b(this.e);
            }
            long j4 = j2 - j;
            long j5 = j3 - j2;
            if (j4 >= 0 && j5 >= 0) {
                long[] jArr = this.a;
                int i2 = this.n;
                jArr[i2] = j4;
                this.b[i2] = j5;
                this.n = (i2 + 1) % 128;
                int i3 = this.m;
                if (i3 < 128) {
                    this.m = i3 + 1;
                }
                this.k = b(this.k);
                return;
            }
            this.l = b(this.l);
        } catch (Throwable th) {
            throw th;
        }
    }
}
