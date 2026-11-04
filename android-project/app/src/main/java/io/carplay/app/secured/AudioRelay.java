/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 AudioRelay。原生音频通道状态记录：首次初始化、首个有效 PCM、视频后等待等关键节点。
 *   This file defines AudioRelay. Native audio channel state recorder: first init, first valid PCM, post-video wait, …
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Consumer;



/**
 * 原生音频通道状态记录：首次初始化、首个有效 PCM、视频后等待等关键节点。
 *
 * Native audio channel state recorder: first init, first valid PCM, post-video wait, …
 */
public final class AudioRelay {
    private final CarLifeFrame.a a;
    private final Consumer<String> b;
    private final AtomicLong c = new AtomicLong();
    private final AtomicLong d = new AtomicLong();
    private final AtomicLong e = new AtomicLong();
    private final AtomicLong f = new AtomicLong();
    private final AtomicLong g = new AtomicLong();
    private final AtomicLong h = new AtomicLong();
    private final AtomicLong i = new AtomicLong();
    private final AtomicLong j = new AtomicLong();
    private final AtomicLong k = new AtomicLong();
    private final AtomicLongArray l = new AtomicLongArray(StreamState.values().length);
    private final AtomicBoolean m = new AtomicBoolean();
    private final AtomicBoolean n = new AtomicBoolean();
    private final AtomicBoolean o = new AtomicBoolean();
    private final AtomicBoolean p = new AtomicBoolean();
    private final AtomicBoolean q = new AtomicBoolean();
    private final AtomicBoolean r = new AtomicBoolean();
    private final AtomicBoolean s = new AtomicBoolean();

    /**
     * AudioRelay 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in AudioRelay, carrying local implementation detail.
     */
    public static final class a {
        private final AtomicLongArray a = new AtomicLongArray(CarLifeFrame.a.values().length);

        
        /**
         * 枚举：本子系统使用的状态/类型取值集合。
         *
         * Enumeration: state/type values used by this subsystem.
         */
        public enum Gate {
            NO_TRANSPORT,
            NO_FORMAT,
            OUTPUT_NOT_READY,
            CALLBACK_RETURNED
        }

        
        
        
        public void a(io.carplay.app.secured.CarLifeFrame.a channel,
                io.carplay.app.secured.AudioRelay.a.Gate gate,
                java.util.function.Consumer<java.lang.String> logger) {
            int index = channel.ordinal();
            long mask = 1L << gate.ordinal();
            while (true) {
                long current = this.a.get(index);
                if ((current & mask) != 0L) {
                    return;
                }
                if (this.a.compareAndSet(index, current, current | mask)) {
                    break;
                }
            }
            try {
                logger.accept("AUDIO_RELAY_PROGRESS channel=" + channel + " gate=" + gate
                        + " wire_sent_confirmed=false sound_confirmed=false");
            } catch (RuntimeException unused) {
                
            }
        }
    }

    /**
     * 枚举：本子系统使用的状态/类型取值集合。
     *
     * Enumeration: state/type values used by this subsystem.
     */
    public enum StreamState {
        NO_INIT,
        UNSUPPORTED_FORMAT,
        PAUSED,
        SESSION_GATE
    }

    public AudioRelay(CarLifeFrame.a aVar, Consumer<String> consumer) {
        if (aVar != CarLifeFrame.a.MEDIA && aVar != CarLifeFrame.a.TTS && aVar != CarLifeFrame.a.VR) {
            SyntheticRefs.b("native audio channel required");
            throw null;
        }
        this.a = aVar;
        Objects.requireNonNull(consumer);
        this.b = consumer;
    }

    private void d(String str) {
        try {
            this.b.accept("NATIVE_AUDIO_PROGRESS channel=" + String.valueOf(this.a) + " stage=" + str + " reader_connected=" + this.m.get() + " reader_ended=" + this.n.get() + " packets=" + this.c.get() + " wire_body_bytes=" + this.d.get() + " init_packets=" + this.e.get() + " valid_inits=" + this.f.get() + " data_packets=" + this.g.get() + " valid_pcm_bytes=" + this.h.get() + " callback_returned_bytes=" + this.i.get() + " stop_packets=" + this.j.get() + " vr_interrupt_packets=" + this.k.get() + " withheld_no_init=" + this.l.get(0) + " withheld_unsupported=" + this.l.get(1) + " withheld_paused=" + this.l.get(2) + " withheld_session_gate=" + this.l.get(3) + " playback_expected=unknown sound_confirmed=false");
        } catch (RuntimeException unused) {
        }
    }

    public void a(int i) {
        this.i.addAndGet(i);
    }

    public void b() {
        this.m.set(true);
    }

    public void c() {
        this.g.incrementAndGet();
    }

    public void e() {
        this.n.set(true);
    }

    public void f() {
        this.e.incrementAndGet();
    }

    public void g(boolean z) {
        this.f.incrementAndGet();
        if (this.o.compareAndSet(false, true)) {
            d("FIRST_INIT supported=" + z);
        }
    }

    public void h() {
        this.k.incrementAndGet();
    }

    public void i(int i) {
        this.c.incrementAndGet();
        this.d.addAndGet(i);
    }

    public void j() {
        if (this.s.compareAndSet(false, true)) {
            d("FINAL");
        }
    }

    public void k() {
        if (this.r.compareAndSet(false, true)) {
            d("WAIT_AFTER_VIDEO_10S");
        }
    }

    public void l() {
        this.j.incrementAndGet();
    }

    public void m(int i) {
        this.h.addAndGet(i);
        if (this.p.compareAndSet(false, true)) {
            d("FIRST_VALID_PCM");
        }
    }

    public void n(StreamState bVar) {
        this.l.incrementAndGet(bVar.ordinal());
        if (this.q.compareAndSet(false, true)) {
            d("FIRST_WITHHELD reason=".concat(String.valueOf(bVar)));
        }
    }
}
