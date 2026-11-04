/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 MediaSourceTracker。媒体源跟踪：记录原生媒体源是否建立，拒绝对过期/错误源的写入。
 *   This file defines MediaSourceTracker. Media source tracker: records whether a native media source exists and rejects writes to stale or mismatched sources.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.function.Supplier;


/**
 * 媒体源跟踪：记录原生媒体源是否建立，拒绝对过期/错误源的写入。
 *
 * Media source tracker: records whether a native media source exists and rejects writes to stale or mismatched sources.
 */
public final class MediaSourceTracker implements MediaFormat {
    private final MediaFormat a;
    private final Supplier<SessionVerificationTracker.C0003b> b;
    private long c;
    private long d;
    private MediaFormat.g e;
    private boolean f;
    private MediaFormat.PcmFormat g;
    private long h = -1;
    private long i = -1;

    public MediaSourceTracker(MediaFormat g4Var, Supplier<SessionVerificationTracker.C0003b> supplier) {
        if (g4Var == null || supplier == null) {
            SyntheticRefs.b("sink/evidence");
            throw null;
        }
        this.a = g4Var;
        this.b = supplier;
    }

    private void i(long j, MediaFormat.g gVar, boolean z) {
        long j2 = this.c;
        if (j2 == 0 || j != j2 || gVar != this.e) {
            SyntheticRefs.d("stale/wrong media source");
            return;
        }
        if (gVar == MediaFormat.g.NATIVE_CARPLUS) {
            SessionVerificationTracker.C0003b c0003b = this.b.get();
            if (c0003b == null || c0003b.k() != j || !c0003b.t() || (z && !c0003b.r())) {
                SyntheticRefs.d("native source not established");
            }
        }
    }

    @Override 
    public synchronized void a(long j, MediaFormat.g gVar, MediaFormat.PcmFormat eVar) {
        i(j, gVar, false);
        this.g = null;
        if (eVar == null) {
            throw new IllegalArgumentException("missing PCM format");
        }
        eVar.l();
        this.a.a(j, gVar, eVar);
        this.g = eVar;
    }

    @Override 
    public synchronized void b(long j, MediaFormat.g gVar, MediaFormat.VideoMime hVar) {
        i(j, gVar, false);
        if (hVar == null) {
            throw new IllegalArgumentException("missing video format");
        }
        this.f = false;
        this.a.b(j, gVar, hVar);
        this.f = true;
    }

    @Override 
    public synchronized void c(MediaFormat.c cVar) {
        i(cVar.g(), cVar.k(), true);
        if (!this.f || cVar.j() < this.h) {
            throw new IllegalStateException("video format/clock");
        }
        this.a.c(cVar);
        this.h = cVar.j();
    }

    @Override 
    public synchronized void e(long j, String str) {
        long j2 = this.c;
        if (j == j2 && j2 != 0) {
            this.c = 0L;
            this.e = null;
            this.f = false;
            this.g = null;
            this.i = -1L;
            this.h = -1L;
            this.a.e(j, str);
        }
    }

    @Override 
    public synchronized void f(MediaFormat.c cVar) {
        i(cVar.g(), cVar.k(), true);
        if (this.g == null || cVar.i().length % this.g.h() != 0 || cVar.j() < this.i) {
            throw new IllegalStateException("PCM format/alignment/clock");
        }
        this.a.f(cVar);
        this.i = cVar.j();
    }

    public synchronized void g(long j, MediaFormat.g gVar) {
        try {
            if (j < 1 || gVar == null) {
                throw new IllegalArgumentException("source generation");
            }
            if (this.c != 0 || j <= this.d) {
                throw new IllegalStateException("end active source/use a fresh generation");
            }
            this.c = j;
            this.e = gVar;
            this.d = j;
            this.f = false;
            this.g = null;
            this.i = -1L;
            this.h = -1L;
        } catch (Throwable th) {
            throw th;
        }
    }
}
