package io.carplay.app.secured;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Consumer;

/* JADX INFO: loaded from: classes.dex */
/**
 * AudioRelay
 *
 * <p>原混淆名：{@code io.carplay.app.secured.o4}（jadx 产物 o4.java）
 *
 * <p>命名依据：AUDIO_RELAY_PROGRESS channel= —— 音频转发进度
 */
public final class AudioRelay {
    private final r6.a a;
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
    private final AtomicLongArray l = new AtomicLongArray(b.values().length);
    private final AtomicBoolean m = new AtomicBoolean();
    private final AtomicBoolean n = new AtomicBoolean();
    private final AtomicBoolean o = new AtomicBoolean();
    private final AtomicBoolean p = new AtomicBoolean();
    private final AtomicBoolean q = new AtomicBoolean();
    private final AtomicBoolean r = new AtomicBoolean();
    private final AtomicBoolean s = new AtomicBoolean();

    public static final class a {
        private final AtomicLongArray a = new AtomicLongArray(r6.a.values().length);

        /* JADX INFO: renamed from: io.carplay.app.secured.AudioRelay$a$a, reason: collision with other inner class name */
        public enum EnumC0004a {
            NO_TRANSPORT,
            NO_FORMAT,
            OUTPUT_NOT_READY,
            CALLBACK_RETURNED
        }

        /* JADX WARN: Code restructure failed: missing block: B:15:?, code lost:
        
            return;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public void a(io.carplay.app.secured.r6.a r11, io.carplay.app.secured.AudioRelay.a.EnumC0004a r12, java.util.function.Consumer<java.lang.String> r13) {
            /*
                r10 = this;
                int r1 = r11.ordinal()
                r2 = 1
                int r0 = r12.ordinal()
                long r6 = r2 << r0
            Lc:
                java.util.concurrent.atomic.AtomicLongArray r0 = r10.a
                long r2 = r0.get(r1)
                long r4 = r2 & r6
                r8 = 0
                int r0 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1))
                if (r0 == 0) goto L1b
                goto L4e
            L1b:
                java.util.concurrent.atomic.AtomicLongArray r0 = r10.a
                long r4 = r2 | r6
                boolean r0 = r0.compareAndSet(r1, r2, r4)
                if (r0 == 0) goto Lc
                java.lang.String r10 = java.lang.String.valueOf(r11)     // Catch: java.lang.RuntimeException -> L4e
                java.lang.String r11 = java.lang.String.valueOf(r12)     // Catch: java.lang.RuntimeException -> L4e
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch: java.lang.RuntimeException -> L4e
                r12.<init>()     // Catch: java.lang.RuntimeException -> L4e
                java.lang.String r0 = "AUDIO_RELAY_PROGRESS channel="
                r12.append(r0)     // Catch: java.lang.RuntimeException -> L4e
                r12.append(r10)     // Catch: java.lang.RuntimeException -> L4e
                java.lang.String r10 = " gate="
                r12.append(r10)     // Catch: java.lang.RuntimeException -> L4e
                r12.append(r11)     // Catch: java.lang.RuntimeException -> L4e
                java.lang.String r10 = " wire_sent_confirmed=false sound_confirmed=false"
                r12.append(r10)     // Catch: java.lang.RuntimeException -> L4e
                java.lang.String r10 = r12.toString()     // Catch: java.lang.RuntimeException -> L4e
                r13.accept(r10)     // Catch: java.lang.RuntimeException -> L4e
            L4e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: io.carplay.app.secured.AudioRelay.a.a(io.carplay.app.secured.r6$a, io.carplay.app.secured.AudioRelay$a$a, java.util.function.Consumer):void");
        }
    }

    public enum b {
        NO_INIT,
        UNSUPPORTED_FORMAT,
        PAUSED,
        SESSION_GATE
    }

    public AudioRelay(r6.a aVar, Consumer<String> consumer) {
        if (aVar != r6.a.MEDIA && aVar != r6.a.TTS && aVar != r6.a.VR) {
            d0.b("native audio channel required");
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

    public void n(b bVar) {
        this.l.incrementAndGet(bVar.ordinal());
        if (this.q.compareAndSet(false, true)) {
            d("FIRST_WITHHELD reason=".concat(String.valueOf(bVar)));
        }
    }
}
