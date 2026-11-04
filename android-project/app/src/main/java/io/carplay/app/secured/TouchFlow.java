/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 TouchFlow。触摸链路统计：累计 down/move/up 与失败次数，用于判定触摸是否真正转发。
 *   This file defines TouchFlow. Touch pipeline statistics: accumulates down/move/up and failure counters to prove whether touches are really forwarded.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.Arrays;



/**
 * 触摸链路统计：累计 down/move/up 与失败次数，用于判定触摸是否真正转发。
 *
 * Touch pipeline statistics: accumulates down/move/up and failure counters to prove whether touches are really forwarded.
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

    /**
     * TouchFlow 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in TouchFlow, carrying local implementation detail.
     */
    public static  class SwitchMap {
        static final  int[] a;

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

    /**
     * 枚举：本子系统使用的状态/类型取值集合。
     *
     * Enumeration: state/type values used by this subsystem.
     */
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
