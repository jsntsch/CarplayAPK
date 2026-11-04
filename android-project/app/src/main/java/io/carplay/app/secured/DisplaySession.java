/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 DisplaySession。显示会话：把解码后的视频帧、触摸与按键事件编码成 CarLife 原生帧写入车机通道。
 *   This file defines DisplaySession. Display session: encodes decoded video frames plus touch/key events into CarLife native frames and writes them to the head-unit channel.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.VarHandle;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;



/**
 * 显示会话：把解码后的视频帧、触摸与按键事件编码成 CarLife 原生帧写入车机通道。
 *
 * Display session: encodes decoded video frames plus touch/key events into CarLife native frames and writes them to the head-unit channel.
 */
public final class DisplaySession implements AutoCloseable, j6 {
    private final Object A;
    private final Object B;
    private final TouchFlow C;
    private long D;
    private volatile o E;
    private volatile boolean F;
    private volatile long G;
    private volatile String H;
    private boolean I;
    private boolean J;
    private boolean K;
    private boolean L;
    private boolean M;
    private boolean N;
    private boolean O;
    private boolean P;
    private boolean Q;
    private volatile boolean R;
    private volatile boolean S;
    private volatile boolean T;
    private boolean U;
    private int V;
    private int W;
    private int X;
    private volatile String Y;
    private volatile String Z;
    private final c a;
    private volatile CarLifeFrameCipher HidCommandSender;
    private final f b;
    private volatile TouchGestureEncoder b0;
    private final h c;
    private RsaAesCrypto c0;
    private final d d;
    private final VideoConfig e;
    private final Map<CarLifeFrame.a, Socket> f;
    private final List<Thread> g;
    private final AtomicBoolean h;
    private final AtomicLong i;
    private final AtomicLong j;
    private final AtomicLong k;
    private final AtomicLong l;
    private final AtomicLong m;
    private final Map<CarLifeFrame.a, AudioRelay> n;
    private volatile long o;
    private final AtomicReference<String> p;
    private final AtomicBoolean q;
    private final Object r;
    private long s;
    private int t;
    private long u;
    private boolean v;
    private boolean w;
    private boolean x;
    private volatile boolean y;
    private int z;

    @FunctionalInterface
    /**
     * 显示会话回调接口：接收原生通道的帧与错误事件。
     *
     * Display-session callback interface receiving native-channel frames and errors.
     */
    public interface c {
        void a(String str);
    }

    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface d extends h {
        default void a() {
        }

        default void b(j6 j6Var) {
        }

        default void c(j6 j6Var) {
            a();
        }

        default void d(CarLifeFrame.a aVar) throws IOException {
        }

        default void e(int i, int i2, int i3) throws IOException {
        }

        default void f(int i) {
        }

        void g(CarLifeFrame.a aVar, int i, int i2, int i3) throws IOException;

        default boolean i(CarLifeFrame.a aVar, int i, int i2, int i3) {
            return true;
        }

        default void j(j6 j6Var, int i) {
            f(i);
        }

        void k(CarLifeFrame.a aVar, byte[] bArr, long j) throws IOException;

        default void l(j6 j6Var, j6.b bVar) {
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class e extends ProtoMessageBase {
        private final boolean a;
        private final boolean b;
        private final boolean c;
        private final String d;
        private final String e;
        private final boolean f;
        private final boolean g;
        private final int h;
        private final int i;
        private final int j;
        private final long k;
        private final long l;
        private final long m;
        private final long n;
        private final long o;
        private final String p;

        public e(boolean z, boolean z2, boolean z3, String str, String str2, boolean z4, boolean z5, int i, int i2, int i3, long j, long j2, long j3, long j4, long j5, String str3) {
            this.a = z;
            this.b = z2;
            this.c = z3;
            this.d = str;
            this.e = str2;
            this.f = z4;
            this.g = z5;
            this.h = i;
            this.i = i2;
            this.j = i3;
            this.k = j;
            this.l = j2;
            this.m = j3;
            this.n = j4;
            this.o = j5;
            this.p = str3;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof e)) {
                return false;
            }
            e eVar = (e) obj;
            return this.a == eVar.a && this.b == eVar.b && this.c == eVar.c && this.f == eVar.f && this.g == eVar.g && this.h == eVar.h && this.i == eVar.i && this.j == eVar.j && this.k == eVar.k && this.l == eVar.l && this.m == eVar.m && this.n == eVar.n && this.o == eVar.o && Objects.equals(this.d, eVar.d) && Objects.equals(this.e, eVar.e) && Objects.equals(this.p, eVar.p);
        }

        public long g() {
            return this.o;
        }

        public boolean h() {
            return this.a;
        }

        public final int hashCode() {
            boolean z = this.a;
            boolean z2 = this.b;
            boolean z3 = this.c;
            boolean z4 = this.f;
            boolean z5 = this.g;
            int i = this.h;
            int i2 = this.i;
            int i3 = this.j;
            long j = this.k;
            long j2 = this.l;
            long j3 = this.m;
            long j4 = this.n;
            long j5 = this.o;
            String str = this.d;
            String str2 = this.e;
            String str3 = this.p;
            return Objects.hashCode(str3) + ProtoMessageBase.b(str2, ProtoMessageBase.b(str, ProtoMessageBase.a(j5, ProtoMessageBase.a(j4, ProtoMessageBase.a(j3, ProtoMessageBase.a(j2, ProtoMessageBase.a(j, (((((((Boolean.hashCode(z5) + ((Boolean.hashCode(z4) + ((Boolean.hashCode(z3) + ((Boolean.hashCode(z2) + (Boolean.hashCode(z) * 31)) * 31)) * 31)) * 31)) * 31) + i) * 31) + i2) * 31) + i3) * 31, 31), 31), 31), 31), 31), 31), 31);
        }

        public boolean i() {
            return this.f;
        }

        public int j() {
            return this.j;
        }

        public int k() {
            return this.i;
        }

        public boolean l() {
            return this.g;
        }

        public boolean m() {
            return this.c;
        }

        public String n() {
            return this.e;
        }

        public String o() {
            return this.d;
        }

        public long p() {
            return this.l;
        }

        public long q() {
            return this.m;
        }

        public long r() {
            return this.k;
        }

        public String s() {
            return this.p;
        }

        public boolean t() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {Boolean.valueOf(this.a), Boolean.valueOf(this.b), Boolean.valueOf(this.c), this.d, this.e, Boolean.valueOf(this.f), Boolean.valueOf(this.g), Integer.valueOf(this.h), Integer.valueOf(this.i), Integer.valueOf(this.j), Long.valueOf(this.k), Long.valueOf(this.l), Long.valueOf(this.m), Long.valueOf(this.n), Long.valueOf(this.o), this.p};
            String[] strArrSplit = "a;b;c;d;e;f;g;h;i;j;k;l;m;n;o;p".length() == 0 ? new String[0] : "a;b;c;d;e;f;g;h;i;j;k;l;m;n;o;p".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(e.class, sb, "[");
            for (int i = 0; i < strArrSplit.length; i++) {
                sb.append(strArrSplit[i]);
                sb.append("=");
                sb.append(objArr[i]);
                if (i != strArrSplit.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        public long u() {
            return this.n;
        }

        public int v() {
            return this.h;
        }
    }

    @FunctionalInterface
    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface h {
        void h(byte[] bArr, long j) throws IOException;
    }

    private DisplaySession(c cVar, f fVar, h hVar, d dVar, VideoConfig gVar) {
        this.f = new EnumMap(CarLifeFrame.a.class);
        this.g = new ArrayList();
        this.h = new AtomicBoolean();
        this.i = new AtomicLong();
        this.j = new AtomicLong();
        this.k = new AtomicLong();
        this.l = new AtomicLong();
        this.m = new AtomicLong();
        this.n = new EnumMap(CarLifeFrame.a.class);
        this.p = new AtomicReference<>();
        this.q = new AtomicBoolean();
        this.r = new Object();
        this.A = new Object();
        this.B = new Object();
        this.C = new TouchFlow();
        this.Y = "UNKNOWN";
        this.Z = "UNKNOWN";
        int i = 0;
        this.HidCommandSender = new CarLifeFrameCipher(false, null);
        this.a = cVar == null ? new q4() : cVar;
        Objects.requireNonNull(fVar);
        this.b = fVar;
        Objects.requireNonNull(hVar);
        this.c = hVar;
        this.d = dVar;
        Objects.requireNonNull(gVar);
        this.e = gVar;
        CarLifeFrame.a[] aVarArr = {CarLifeFrame.a.MEDIA, CarLifeFrame.a.TTS, CarLifeFrame.a.VR};
        for (int i2 = 0; i2 < 3; i2++) {
            CarLifeFrame.a aVar = aVarArr[i2];
            Map<CarLifeFrame.a, AudioRelay> map = this.n;
            c cVar2 = this.a;
            Objects.requireNonNull(cVar2);
            r4 r4Var = new r4(i);
            r4Var.b = cVar2;
            VarHandle.storeStoreFence();
            map.put(aVar, new AudioRelay(aVar, r4Var));
        }
    }

    
    public static  void A(byte[] bArr, long j) {
    }

    
    public static  void B(String str) {
    }

    
    public static  Thread C(Runnable runnable) {
        Thread thread = new Thread(runnable, "CarLife-native-deadline");
        thread.setDaemon(true);
        return thread;
    }

    
    public  void D() {
        I(System.nanoTime());
    }

    
    public static  Integer F(Integer num, Integer num2) {
        return Integer.valueOf(Math.min(4, num2.intValue() + num.intValue()));
    }

    
    public  o G(Provider provider, byte[] bArr) {
        try {
            AesCipher g3Var = new AesCipher("AES/ECB/PKCS5Padding", provider, new SecretKeySpec(bArr, "AES"), null);
            this.E = g3Var;
            return g3Var;
        } catch (GeneralSecurityException e) {
            return null;
        }
    }

    private void H(CarLifeFrame.a aVar, Exception exc) {
        String str;
        if (this.h.get()) {
            return;
        }
        String str2 = exc == null ? "NATIVE_CHANNEL_EOF" : "NATIVE_CHANNEL_FAIL";
        String str3 = str2 + " channel=" + aVar.name();
        this.p.compareAndSet(null, str3.replace(' ', '_'));
        if (exc == null) {
            str = "";
        } else if (exc instanceof ProtocolIOException) {
            str = "PROTOCOL";
        } else if (exc instanceof SocketTimeoutException) {
            str = "TIMEOUT";
        } else {
            str = exc instanceof IOException ? "IO" : "OTHER";
        }
        c cVar = this.a;
        String strConcat = exc != null ? " error=".concat(str) : "";
        cVar.a(str3 + strConcat + " " + X());
    }

    private void I(long j) {
        boolean z;
        long j2 = this.o;
        if (!this.h.get() && j2 != 0 && j - j2 >= 10000000000L) {
            Iterator<AudioRelay> it = this.n.values().iterator();
            while (it.hasNext()) {
                it.next().k();
            }
        }
        synchronized (this.r) {
            try {
                if (this.h.get() || !this.v || !this.w || this.x || j - this.u < 3000000000L) {
                    z = false;
                } else {
                    this.x = true;
                    z = true;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        if (z) {
            this.a.a("NATIVE_FOREGROUND_UNCONFIRMED request_sent=true status_observed=false retry_sent=false");
            if (this.d != null && !this.h.get()) {
                this.d.l(this, j6.b.UNCONFIRMED);
            }
        }
        synchronized (this.r) {
            try {
                if (!this.h.get() && this.T && this.j.get() <= 0) {
                    int i = 3;
                    if (this.t < 3) {
                        long jY = Y(j);
                        int i2 = this.t;
                        if (i2 != 0) {
                            i = i2 == 1 ? 10 : 20;
                        }
                        if (jY < ((long) i) * 1000) {
                            return;
                        }
                        this.t = i2 + 1;
                        this.a.a("NATIVE_VIDEO_WAIT elapsed_ms=" + jY + " " + X());
                    }
                }
            } catch (Throwable th2) {
                throw th2;
            }
        }
    }

    private boolean J() {
        return !this.h.get() && this.S && this.R && "VERIFIED".equals(this.Y) && "PASSED".equals(this.Z);
    }

    
    
    public void E(CarLifeFrame.a aVar) {
        b bVar;
        long j;
        long j2;
        AudioRelay audioRelay = this.n.get(aVar);
        try {
            try {
                b bVar2 = new b(W(aVar));
                if (audioRelay != null) {
                    audioRelay.b();
                }
                boolean z = true;
                boolean z2 = false;
                boolean z3 = aVar == CarLifeFrame.a.MEDIA || aVar == CarLifeFrame.a.TTS || aVar == CarLifeFrame.a.VR;
                d dVar = this.d;
                Exception exc = null;
                NativeAudioChannel aVar2 = (dVar == null || !z3) ? null : new NativeAudioChannel(aVar, dVar, this.a, audioRelay);
                long j3 = 0;
                while (!this.h.get() && (!this.e.h() || j3 < 200000)) {
                    long j4 = j3 + 1;
                    CarLifeFrame.c cVarI = CarLifeFrame.i(bVar2, aVar, aVar.d() ? 32767 : 2097152);
                    if (cVarI == null) {
                        H(aVar, exc);
                        if (aVar == CarLifeFrame.a.VIDEO && !this.h.get()) {
                            this.H = "VIDEO EOF";
                            this.a.a("NATIVE_REQUIRED_VIDEO_CHANNEL_ENDED source_retained=false");
                            close();
                        }
                        if (audioRelay == null) {
                            return;
                        }
                        audioRelay.e();
                    }
                    byte[] bArrG = cVarI.g();
                    int iH = cVarI.i().h();
                    if (aVar == CarLifeFrame.a.VIDEO && iH == 131073) {
                        j = j4;
                        this.i.addAndGet(bArrG.length);
                        if (this.F && bArrG.length > 0) {
                            o oVar = this.E;
                            Objects.requireNonNull(oVar);
                            bArrG = oVar.b(bArrG);
                        }
                        if (this.j.addAndGet(bArrG.length) > 33554432 && this.e.h()) {
                            throw new IOException("video budget");
                        }
                        long jIncrementAndGet = this.l.incrementAndGet();
                        if (bArrG.length <= 0 || !this.q.compareAndSet(z2, z)) {
                            bVar = bVar2;
                            j2 = 3;
                        } else {
                            this.o = System.nanoTime();
                            c cVar = this.a;
                            int length = bArrG.length;
                            long jY = Y(System.nanoTime());
                            String strX = X();
                            j2 = 3;
                            StringBuilder sb = new StringBuilder();
                            bVar = bVar2;
                            sb.append("NATIVE_VIDEO_FIRST_BYTES bytes=");
                            sb.append(length);
                            sb.append(" after_start_ms=");
                            sb.append(jY);
                            sb.append(" ");
                            sb.append(strX);
                            cVar.a(sb.toString());
                            if (this.d != null && !this.h.get()) {
                                try {
                                    this.d.b(this);
                                } catch (RuntimeException unused) {
                                    this.a.a("NATIVE_FIRST_VIDEO_CALLBACK_UNAVAILABLE media_retained=true");
                                }
                            }
                        }
                        if (jIncrementAndGet <= j2) {
                            this.a.a("Actual VIDEO chunk bytes=" + bArrG.length + " encrypted=" + this.F + " signature=" + V(bArrG));
                        }
                        if (this.d == null || J()) {
                            this.c.h(bArrG, cVarI.i().j());
                        }
                    } else {
                        bVar = bVar2;
                        j = j4;
                        if (aVar == CarLifeFrame.a.MEDIA || aVar == CarLifeFrame.a.TTS || aVar == CarLifeFrame.a.VR) {
                            long jIncrementAndGet2 = this.m.incrementAndGet();
                            this.k.addAndGet(bArrG.length);
                            if (jIncrementAndGet2 <= 12) {
                                this.a.a(String.format(Locale.ROOT, "Actual %s packet id=0x%x wireBytes=%d; PCM producer/format not assumed", aVar, Integer.valueOf(iH), Integer.valueOf(bArrG.length)));
                            }
                            if (this.k.get() > 16777216 && this.e.h()) {
                                throw new IOException("audio budget");
                            }
                            if (aVar2 != null) {
                                aVar2.a(cVarI, this.F, this.E, J());
                            } else if (audioRelay != null) {
                                audioRelay.i(bArrG.length);
                            }
                        } else if (j <= 3) {
                            this.a.a(String.format(Locale.ROOT, "RX %s id=0x%x bytes=%d", aVar, Integer.valueOf(iH), Integer.valueOf(bArrG.length)));
                        }
                    }
                    bVar2 = bVar;
                    j3 = j;
                    z = true;
                    z2 = false;
                    exc = null;
                }
                if (audioRelay == null) {
                    return;
                }
            } catch (Exception e2) {
                if (!this.h.get()) {
                    H(aVar, e2);
                    this.H = String.valueOf(aVar) + " " + e2.getClass().getSimpleName() + ": " + Q(e2);
                    c cVar2 = this.a;
                    String str = this.H;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Media reader stopped: ");
                    sb2.append(str);
                    cVar2.a(sb2.toString());
                    close();
                }
                if (audioRelay == null) {
                    return;
                }
            }
            audioRelay.e();
        } catch (Throwable th) {
            if (audioRelay != null) {
                audioRelay.e();
            }
            throw th;
        }
    }

    private int N(int i) throws SocketTimeoutException {
        if (this.e.i() == 0) {
            return i;
        }
        long jNanoTime = this.G - System.nanoTime();
        if (jNanoTime > 0) {
            return (int) Math.max(1L, Math.min(i, (jNanoTime + 999999) / 1000000));
        }
        throw new SocketTimeoutException("deadline");
    }

    private void O() {
        try {
            this.a.a(this.C.d());
        } catch (RuntimeException unused) {
        }
    }

    private static String Q(Exception exc) {
        String message = exc.getMessage();
        if (message == null || !(message.startsWith("video sender stalled") || message.startsWith("AVC ") || message.startsWith("AnnexB ") || message.startsWith("CarPlay ") || message.startsWith("actual AVC ") || message.startsWith("native PCM "))) {
            return exc.getCause() == null ? "media operation failed" : "media operation failed; cause=".concat(exc.getCause().getClass().getSimpleName());
        }
        return message.substring(0, Math.min(message.length(), 180));
    }

    private static String R(Exception exc) {
        return exc instanceof ProtocolIOException ? exc.getMessage() : "bounded session ended";
    }

    private void S(int i, byte[] bArr) throws IOException {
        synchronized (this.A) {
                if (this.h.get()) {
                    throw new IOException("cancelled");
                }
                CarLifeFrameCipher c2Var = this.HidCommandSender;
                CarLifeFrame.a aVar = CarLifeFrame.a.CMD;
                CarLifeFrame.c cVarB = c2Var.b(aVar, i, bArr);
                CarLifeFrame.l(W(aVar).getOutputStream(), aVar, cVarB.i(), cVarB.g(), 32767);
                W(aVar).getOutputStream().flush();
                this.a.a(String.format(Locale.ROOT, "TX CMD 0x%x body=%d encrypted=%s", Integer.valueOf(i), Integer.valueOf(cVarB.g().length), Boolean.valueOf(this.F)));
        }
    }

    private static String V(byte[] bArr) {
        if (bArr.length >= 3 && (bArr[0] & 255) == 255 && (bArr[1] & 255) == 216 && (bArr[2] & 255) == 255) {
            return "JPEG_SOI_observed";
        }
        if (bArr.length < 4 || bArr[0] != 0 || bArr[1] != 0) {
            return "unclassified";
        }
        byte b2 = bArr[2];
        if (b2 != 1) {
            return (b2 == 0 && bArr[3] == 1) ? "AnnexB_start_code_observed_codec_unconfirmed" : "unclassified";
        }
        return "AnnexB_start_code_observed_codec_unconfirmed";
    }

    private Socket W(CarLifeFrame.a aVar) throws IOException {
        Socket socket;
        synchronized (this.f) {
            socket = this.f.get(aVar);
            if (socket == null) {
                throw new IOException("closed socket");
            }
        }
        return socket;
    }

    private String X() {
        String str;
        if (!this.S) {
            str = "BEFORE_ENCRYPTION_READY";
        } else if (!this.R) {
            str = "BEFORE_VIDEO_INIT";
        } else if (this.T) {
            str = this.j.get() == 0 ? "WAITING_FIRST_VIDEO" : "VIDEO_BYTES_OBSERVED";
        } else {
            str = "BEFORE_VIDEO_START";
        }
        return "encryption_ready=" + this.S + " init_done=" + this.R + " start_sent=" + this.T + " video_packets=" + this.l.get() + " video_bytes=" + this.j.get() + " source_phase=" + str + " native_foreground_observed=" + this.y;
    }

    private long Y(long j) {
        if (this.T) {
            return Math.max(0L, (j - this.s) / 1000000);
        }
        return -1L;
    }

    private void a0(int i) throws IOException {
        try {
            S(i, new byte[0]);
        } catch (IOException e2) {
            throw e2;
        } catch (Exception e3) {
            throw new IOException("native video visibility write failed", e3);
        }
    }

    public static   int v(DisplaySession t4Var) throws SocketTimeoutException {
        return t4Var.N(500);
    }

    private void x() throws IOException {
        if (this.M) {
            return;
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        if (this.e.l()) {
            linkedHashMap.put("DISABLE_RESOLUTION_OPTION", 1);
        }
        linkedHashMap.put("CONTENT_ENCRYPTION", 1);
        S(t1.i, t1.d(linkedHashMap));
        this.M = true;
        this.a.a("Sent actual supported CONTENT_ENCRYPTION=1 subset; no borrowed car channel");
        if (this.e.l()) {
            this.a.a("NATIVE_GEOMETRY_REQUEST target=" + this.e.m() + "x" + this.e.k() + " exact_source_requested=true applied_confirmed=false; waiting for actual VIDEO_INIT_DONE");
        }
    }

    private void y() throws InterruptedException, IOException {
        long jMin = Math.min(this.G, System.nanoTime() + 25000000000L);
        InetAddress byAddress = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        while (!this.h.get() && System.nanoTime() < jMin) {
            Socket socket = new Socket();
            synchronized (this.f) {
                if (this.h.get()) {
                    socket.close();
                    throw new IOException("cancelled");
                }
                this.f.put(CarLifeFrame.a.CMD, socket);
            }
            try {
                socket.connect(new InetSocketAddress(byAddress, 7240), N(500));
                socket.setTcpNoDelay(true);
                for (CarLifeFrame.a aVar : CarLifeFrame.a.values()) {
                    if (aVar != CarLifeFrame.a.CMD) {
                        Socket socket2 = new Socket();
                        synchronized (this.f) {
                            if (this.h.get()) {
                                socket2.close();
                                throw new IOException("cancelled");
                            }
                            this.f.put(aVar, socket2);
                        }
                        socket2.connect(new InetSocketAddress(byAddress, aVar.c()), N(800));
                        socket2.setTcpNoDelay(true);
                    }
                }
                return;
            } catch (ConnectException unused) {
                socket.close();
                synchronized (this.f) {
                    this.f.remove(CarLifeFrame.a.CMD, socket);
                    Thread.sleep(400L);
                }
            }
        }
        throw new SocketTimeoutException("listener wait deadline");
    }

    public void K(byte[] bArr) {
        int i;
        char c2;
        if (this.h.get() || (i = this.z) >= 4) {
            return;
        }
        this.z = i + 1;
        try {
            ProtobufReader.e eVarB = ProtobufReader.b(bArr, new ProtobufReader.d(4096, 65, 2048));
            int i2 = eVarB.i(1);
            List<ProtobufReader.e> listC = eVarB.c(2, new ProtobufReader.d(2048, 32, 1024));
            if (i2 < 0 || i2 > 32 || listC.size() != i2) {
                throw new ProtocolIOException("module count");
            }
            boolean z = false;
            boolean z2 = false;
            String str = "UNDECLARED";
            String str2 = str;
            String str3 = str2;
            boolean z3 = false;
            for (ProtobufReader.e eVar : listC) {
                int i3 = eVar.i(1);
                int i4 = eVar.i(2);
                String str4 = "UNKNOWN";
                if (i3 == 5) {
                    if (z) {
                        throw new ProtocolIOException("duplicate module");
                    }
                    if (i4 == 1) {
                        str4 = "ADB";
                    } else if (i4 == 2) {
                        str4 = "AOA";
                    } else if (i4 == 3) {
                        str4 = "NCM_ANDROID";
                    } else if (i4 == 4) {
                        str4 = "NCM_IOS";
                    } else if (i4 == 5) {
                        str4 = "WIFI";
                    }
                    z = true;
                    str = str4;
                } else if (i3 != 6) {
                    c2 = 4;
                    if (i3 != 4) {
                        continue;
                    } else {
                        if (z2) {
                            throw new ProtocolIOException("duplicate module");
                        }
                        if (i4 == 0) {
                            str4 = "IDLE";
                        } else if (i4 == 1) {
                            str4 = "RUNNING";
                        }
                        z2 = true;
                        str3 = str4;
                    }
                } else {
                    if (z3) {
                        throw new ProtocolIOException("duplicate module");
                    }
                    if (i4 == 0) {
                        str4 = "VEHICLE";
                    } else if (i4 == 1) {
                        str4 = "PHONE";
                    }
                    z3 = true;
                    str2 = str4;
                }
                c2 = 4;
            }
            this.a.a("NATIVE_MODULE_STATUS rows=" + i2 + " connect=" + str + " mic=" + str2 + " vr=" + str3 + " diagnostic_only=true");
        } catch (ProtocolIOException unused) {
            this.a.a("NATIVE_MODULE_STATUS_IGNORED reason=UNSUPPORTED_TELEMETRY diagnostic_only=true source_retained=true");
        }
    }

    public void M(int i, byte[] bArr) {
        try {
            w(i, bArr);
        } catch (ProtocolIOException unused) {
            this.a.a("NATIVE_UI_STATUS_IGNORED reason=UNSUPPORTED_STATUS_BODY action_sent=false source_retained=true");
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public e P() throws IOException {
        Exception e = null;
        Throwable th = null;
        CarLifeChannel r9;
        String str;
        RsaAesCrypto f6Var;
        Iterator<Thread> it;
        Iterator<AudioRelay> it2;
        CarLifeFrameCipher c2Var;
        ScheduledExecutorService scheduledExecutorService;
        CarLifeChannel r10;
        RsaAesCrypto f6Var2;
        Iterator<Thread> it3;
        Iterator<AudioRelay> iDisplaySession;
        CarLifeFrame.a aVar;
        CarLifeFrame.c cVarI;
        byte[] bArrA;
        int iH;
        int i = 3;
        int iIntValue;
        int i2;
        l5.a aVarC;
        boolean zG;
        String str2;
        RsaAesCrypto f6Var3;
        long j;
        TimeUnit timeUnit;
        final int i3 = 1;
        Integer num = 1;
        synchronized (this) {
            if (this.I) {
                throw new IllegalStateException("one instance per experiment");
            }
            this.I = true;
        }
        this.G = this.e.i() == 0 ? Long.MAX_VALUE : System.nanoTime() + TimeUnit.SECONDS.toNanos(this.e.i());
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new n0(3));
        final int i4 = 0;
        if (this.e.i() != 0) {
            Runnable runnable = new Runnable() { 
                public final  DisplaySession b;

                {
                    this.b = DisplaySession.this;
                }

                @Override 
                public final void run() {
                    int i5 = i4;
                    DisplaySession t4Var = this.b;
                    switch (i5) {
                        case 0:
                            t4Var.close();
                            break;
                        default:
                            t4Var.D();
                            break;
                    }
                }
            };
            long jI = this.e.i();
            timeUnit = TimeUnit.SECONDS;
            scheduledExecutorService.schedule(runnable, jI, timeUnit);
        }
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() { 
            public final  DisplaySession b;

            {
                this.b = DisplaySession.this;
            }

            @Override 
            public final void run() {
                int i5 = i3;
                DisplaySession t4Var = this.b;
                switch (i5) {
                    case 0:
                        t4Var.close();
                        break;
                    default:
                        t4Var.D();
                        break;
                }
            }
        }, 1L, 1L, TimeUnit.SECONDS);
        CarLifeChannel o5VarG = CarLifeChannel.g();
        MfiAuthSession l4Var = new MfiAuthSession(1L, o5VarG);
        String str3 = "deadline";
        Exception exc = null;
        try {
            try {
                io.carplay.app.secured.AesEcbCipher.a aVarA = io.carplay.app.secured.AesEcbCipher.a();
                try {
                    if (!aVarA.i()) {
                        throw new ProtocolIOException("local default AES differs from reviewed explicit ECB/PKCS5 contract");
                    }
                    this.a.a("Local AES provider=" + aVarA.j() + "; explicit AES/ECB/PKCS5Padding; actual peer READY still required");
                    y();
                    this.J = true;
                    this.a.a("All seven localhost channels connected; not media/auth acceptance");
                    long j2 = 1;
                    CarLifeFrame.a[] aVarArr = {CarLifeFrame.a.VIDEO, CarLifeFrame.a.MEDIA, CarLifeFrame.a.TTS, CarLifeFrame.a.VR, CarLifeFrame.a.DATA};
                    for (int i5 = 0; i5 < 5; i5++) {
                        CarLifeFrame.a aVar2 = aVarArr[i5];
                        Thread thread = new Thread(new p0(this, aVar2), "CarLife-native-" + aVar2.name());
                        thread.setDaemon(true);
                        this.g.add(thread);
                        thread.start();
                    }
                    S(t1.a, t1.j(4, 0));
                    b bVar = new b(W(CarLifeFrame.a.CMD));
                    HashMap map = new HashMap();
                    long j3 = 0;
                    while (true) {
                        if (this.h.get()) {
                            break;
                        } else {
                            try {
                                try {
                                    if (System.nanoTime() < this.G) {
                                        {
                                            long j4 = j3 + j2;
                                            if (j3 < 4096) {
                                                j3 = j4;
                                                try {
                                                    aVar = CarLifeFrame.a.CMD;
                                                    cVarI = CarLifeFrame.i(bVar, aVar, 32767);
                                                    if (cVarI != null) {
                                                        bArrA = this.HidCommandSender.a(cVarI);
                                                        iH = cVarI.i().h();
                                                        if (map.containsKey(Integer.valueOf(iH))) {
                                                            if (map.size() < 256) {
                                                                iIntValue = ((Integer) map.merge(Integer.valueOf(iH), num, new w(2))).intValue();
                                                            } else {
                                                                i = 3;
                                                                iIntValue = 4;
                                                            }
                                                        } else {
                                                            iIntValue = ((Integer) map.merge(Integer.valueOf(iH), num, new w(i3))).intValue();
                                                        }
                                                        i = 3;
                                                        if (iIntValue <= i) {
                                                            this.a.a(String.format(Locale.ROOT, "RX CMD 0x%x body=%d", Integer.valueOf(iH), Integer.valueOf(bArrA.length)));
                                                        }
                                                            switch (iH) {
                                                                case t1.b :
                                                                    if (this.K || ProtobufReader.a(bArrA).i(1) != 1) {
                                                                        throw new ProtocolIOException("version rejected/duplicate");
                                                                    }
                                                                    this.K = true;
                                                                    if (!this.K || !this.L || this.N) {
                                                                        j = 1;
                                                                    } else {
                                                                        j = 1;
                                                                        MfiAuthSession.b bVarF = l4Var.f(1L);
                                                                        S(bVarF.i(), bVarF.g());
                                                                        this.N = true;
                                                                    }
                                                                    if (this.H != null) {
                                                                        throw new IOException(this.H);
                                                                    }
                                                                    if (this.e.h() || !this.T || this.j.get() < 8388608) {
                                                                        j2 = j;
                                                                        num = num;
                                                                        map = map;
                                                                        exc = null;
                                                                    } else {
                                                                        str3 = "8MiB actual video received; bounded experiment complete";
                                                                    }
                                                                    break;
                                                                case 65540:
                                                                    if (this.L) {
                                                                        throw new ProtocolIOException("duplicate MD_INFO");
                                                                    }
                                                                    this.L = true;
                                                                    l4Var.b(1L, iH, bArrA);
                                                                    if (!this.K || !this.L || this.N) {
                                                                        j = 1;
                                                                    } else {
                                                                        j = 1;
                                                                        MfiAuthSession.b bVarMd = l4Var.f(1L);
                                                                        S(bVarMd.i(), bVarMd.g());
                                                                        this.N = true;
                                                                    }
                                                                    if (this.H != null) {
                                                                        throw new IOException(this.H);
                                                                    }
                                                                    if (this.e.h()) {
                                                                    }
                                                                    j2 = j;
                                                                    num = num;
                                                                    map = map;
                                                                    exc = null;
                                                                    break;
                                                                case t1.f :
                                                                    if (!this.O || this.R) {
                                                                        throw new ProtocolIOException("unsolicited/duplicate INIT_DONE");
                                                                    }
                                                                    t1.b bVarH = t1.h(bArrA);
                                                                    this.V = bVarH.i();
                                                                    this.W = bVarH.h();
                                                                    int iG = bVarH.g();
                                                                    this.X = iG;
                                                                    int i6 = this.V;
                                                                    if (i6 > 4096 || (i2 = this.W) > 4096 || iG > 240) {
                                                                        throw new ProtocolIOException("video format outside integration limits");
                                                                    }
                                                                    this.b0 = new TouchGestureEncoder(i6, i2);
                                                                    this.R = true;
                                                                    d dVar = this.d;
                                                                    if (dVar != null) {
                                                                        dVar.e(this.V, this.W, this.X);
                                                                    }
                                                                    this.a.a("Actual VIDEO_INIT_DONE " + this.V + "x" + this.W + " @" + this.X + "; not decoded frames");
                                                                    if (this.e.l()) {
                                                                        c cVar = this.a;
                                                                        int iM = this.e.m();
                                                                        int iK = this.e.k();
                                                                        int i7 = this.V;
                                                                        int i8 = this.W;
                                                                        boolean z = i7 == this.e.m() && this.W == this.e.k();
                                                                        StringBuilder sb = new StringBuilder();
                                                                        sb.append("NATIVE_GEOMETRY_RESULT requested=");
                                                                        sb.append(iM);
                                                                        sb.append("x");
                                                                        sb.append(iK);
                                                                        sb.append(" actual=");
                                                                        sb.append(i7);
                                                                        sb.append("x");
                                                                        sb.append(i8);
                                                                        sb.append(" dimensions_match=");
                                                                        sb.append(z);
                                                                        sb.append(" source_format_observed=true display_confirmed=false");
                                                                        cVar.a(sb.toString());
                                                                    }
                                                                    U();
                                                                    if (this.K) {
                                                                        j = 1;
                                                                    } else {
                                                                        j = 1;
                                                                    }
                                                                    if (this.H != null) {
                                                                        throw new IOException(this.H);
                                                                    }
                                                                    if (this.e.h()) {
                                                                    }
                                                                    j2 = j;
                                                                    num = num;
                                                                    map = map;
                                                                    exc = null;
                                                                    break;
                                                                case 65563:
                                                                case 65564:
                                                                case 65569:
                                                                case 65592:
                                                                    M(iH, bArrA);
                                                                    if (this.K) {
                                                                        j = 1;
                                                                    } else {
                                                                        j = 1;
                                                                    }
                                                                    if (this.H != null) {
                                                                        throw new IOException(this.H);
                                                                    }
                                                                    if (this.e.h()) {
                                                                    }
                                                                    j2 = j;
                                                                    num = num;
                                                                    map = map;
                                                                    exc = null;
                                                                    break;
                                                                case 65574:
                                                                    K(bArrA);
                                                                    if (this.K) {
                                                                        j = 1;
                                                                    } else {
                                                                        j = 1;
                                                                    }
                                                                    if (this.H != null) {
                                                                        throw new IOException(this.H);
                                                                    }
                                                                    if (this.e.h()) {
                                                                    }
                                                                    j2 = j;
                                                                    num = num;
                                                                    map = map;
                                                                    exc = null;
                                                                    break;
                                                                case MfiAuthSession.i :
                                                                    if (!this.N) {
                                                                        throw new ProtocolIOException("unsolicited proof");
                                                                    }
                                                                    aVarC = l4Var.c(j2, iH, bArrA);
                                                                    this.Y = aVarC.name();
                                                                    if (aVarC != l5.a.VERIFIED) {
                                                                        throw new ProtocolIOException("phone proof not verified");
                                                                    }
                                                                    S(98378, t1.e(true));
                                                                    this.a.a("Fresh phone proof VERIFIED; no claim about separate phone eligibility");
                                                                    S(t1.c, this.b.g());
                                                                    if (this.b.i() != null) {
                                                                        S(98343, this.b.n());
                                                                        this.a.a("Own local experimental receiver UUID/channel sent; not a borrowed vehicle or officially allocated manufacturer identity");
                                                                    }
                                                                    x();
                                                                    if (this.K) {
                                                                        j = 1;
                                                                    } else {
                                                                        j = 1;
                                                                    }
                                                                    if (this.H != null) {
                                                                        throw new IOException(this.H);
                                                                    }
                                                                    if (this.e.h()) {
                                                                    }
                                                                    j2 = j;
                                                                    num = num;
                                                                    map = map;
                                                                    exc = null;
                                                                    break;
                                                                case 65611:
                                                                    zG = ProtobufReader.a(bArrA).g(1);
                                                                    if (zG) {
                                                                        str2 = "PASSED";
                                                                    } else {
                                                                        str2 = "FAILED";
                                                                    }
                                                                    this.Z = str2;
                                                                    if (this.K) {
                                                                        j = 1;
                                                                    } else {
                                                                        j = 1;
                                                                    }
                                                                    if (this.H != null) {
                                                                        throw new IOException(this.H);
                                                                    }
                                                                    if (this.e.h()) {
                                                                    }
                                                                    j2 = j;
                                                                    num = num;
                                                                    map = map;
                                                                    exc = null;
                                                                    break;
                                                                case t1.h :
                                                                    if ("VERIFIED".equals(this.Y)) {
                                                                        x();
                                                                    }
                                                                    if (this.K) {
                                                                        j = 1;
                                                                    } else {
                                                                        j = 1;
                                                                    }
                                                                    if (this.H != null) {
                                                                        throw new IOException(this.H);
                                                                    }
                                                                    if (this.e.h()) {
                                                                    }
                                                                    j2 = j;
                                                                    num = num;
                                                                    map = map;
                                                                    exc = null;
                                                                    break;
                                                                case 65674: 
                                                                case t1.j :
                                                                    if (!this.M || this.c0 != null || this.S) {
                                                                        throw new ProtocolIOException("unexpected RSA request");
                                                                    }
                                                                    RsaAesCrypto f6Var4 = new RsaAesCrypto(new z2(this, Cipher.getInstance("AES").getProvider()));
                                                                    this.c0 = f6Var4;
                                                                    S(t1.k, t1.i(f6Var4.c()));
                                                                    this.P = true;
                                                                    if (this.K) {
                                                                        j = 1;
                                                                    } else {
                                                                        j = 1;
                                                                    }
                                                                    if (this.H != null) {
                                                                        throw new IOException(this.H);
                                                                    }
                                                                    if (this.e.h()) {
                                                                    }
                                                                    j2 = j;
                                                                    num = num;
                                                                    map = map;
                                                                    exc = null;
                                                                    break;
                                                                case t1.l :
                                                                    f6Var3 = this.c0;
                                                                    if (f6Var3 == null || !this.P || this.Q) {
                                                                        throw new ProtocolIOException("unexpected encrypted seed");
                                                                    }
                                                                    f6Var3.b(ProtobufReader.a(bArrA).j(1));
                                                                    S(t1.m, new byte[0]);
                                                                    this.HidCommandSender = new CarLifeFrameCipher(true, this.E);
                                                                    this.F = true;
                                                                    this.Q = true;
                                                                    if (this.K) {
                                                                        j = 1;
                                                                    } else {
                                                                        j = 1;
                                                                    }
                                                                    if (this.H != null) {
                                                                        throw new IOException(this.H);
                                                                    }
                                                                    if (this.e.h()) {
                                                                    }
                                                                    j2 = j;
                                                                    num = num;
                                                                    map = map;
                                                                    exc = null;
                                                                    break;
                                                                case t1.n :
                                                                    if (bArrA.length != 0 || !this.Q || this.S) {
                                                                        throw new ProtocolIOException("unexpected encryption READY");
                                                                    }
                                                                    this.S = true;
                                                                    this.a.a("Actual MD_ENCRYPT_READY observed; own experimental receiver metadata only");
                                                                    S(t1.e, Z());
                                                                    this.O = true;
                                                                    if (this.K) {
                                                                        j = 1;
                                                                    } else {
                                                                        j = 1;
                                                                    }
                                                                    if (this.H != null) {
                                                                        throw new IOException(this.H);
                                                                    }
                                                                    if (this.e.h()) {
                                                                    }
                                                                    j2 = j;
                                                                    num = num;
                                                                    map = map;
                                                                    exc = null;
                                                                    break;
                                                                default:
                                                                    if (this.K) {
                                                                        j = 1;
                                                                    } else {
                                                                        j = 1;
                                                                    }
                                                                    if (this.H != null) {
                                                                        throw new IOException(this.H);
                                                                    }
                                                                    if (this.e.h()) {
                                                                    }
                                                                    j2 = j;
                                                                    num = num;
                                                                    map = map;
                                                                    exc = null;
                                                                    break;
                                                            }
                                                    } else {
                                                        H(aVar, exc);
                                                        str3 = "CMD peer EOF";
                                                    }
                                                } catch (Exception e2) {
                                                    H(CarLifeFrame.a.CMD, e2);
                                                    throw e2;
                                                }
                                            }
                                        }
                                    }
                                } catch (Throwable thCaught) {
                                    r10 = o5VarG;
                                    this.a.a("NATIVE_SOURCE_FINAL first_channel_end=" + ((String) Objects.requireNonNullElse(this.p.get(), "NONE")) + " " + X());
                                    close();
                                    scheduledExecutorService.shutdownNow();
                                    l4Var.close();
                                    r10.close();
                                    f6Var2 = this.c0;
                                    if (f6Var2 != null) {
                                        f6Var2.close();
                                    }
                                    it3 = this.g.iterator();
                                    while (it3.hasNext()) {
                                        try {
                                            it3.next().join(500L);
                                        } catch (InterruptedException unused) {
                                            Thread.currentThread().interrupt();
                                            iDisplaySession = this.n.values().iterator();
                                            while (iDisplaySession.hasNext()) {
                                                iDisplaySession.next().j();
                                            }
                                            this.E = null;
                                            this.HidCommandSender = new CarLifeFrameCipher(false, null);
                                            throw thCaught;
                                        }
                                    }
                                    iDisplaySession = this.n.values().iterator();
                                    while (iDisplaySession.hasNext()) {
                                        iDisplaySession.next().j();
                                    }
                                    this.E = null;
                                    this.HidCommandSender = new CarLifeFrameCipher(false, null);
                                    throw thCaught;
                                }
                            } catch (Exception e3) {
                                e = e3;
                                r9 = o5VarG;
                                if (this.H != null) {
                                    str = this.H;
                                } else {
                                    str = e.getClass().getSimpleName() + ": " + R(e);
                                }
                                str3 = str;
                                this.a.a("NATIVE_SOURCE_FINAL first_channel_end=" + ((String) Objects.requireNonNullElse(this.p.get(), "NONE")) + " " + X());
                                close();
                                scheduledExecutorService.shutdownNow();
                                l4Var.close();
                                r9.close();
                                f6Var = this.c0;
                                if (f6Var != null) {
                                    f6Var.close();
                                }
                                it = this.g.iterator();
                                while (it.hasNext()) {
                                    try {
                                        it.next().join(500L);
                                    } catch (InterruptedException unused2) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                                it2 = this.n.values().iterator();
                                while (it2.hasNext()) {
                                    it2.next().j();
                                }
                                this.E = null;
                                c2Var = new CarLifeFrameCipher(false, null);
                            }
                        }
                    }
                    if (this.h.get()) {
                        str3 = this.H == null ? "stopped/deadline" : this.H;
                    }
                    this.a.a("NATIVE_SOURCE_FINAL first_channel_end=" + ((String) Objects.requireNonNullElse(this.p.get(), "NONE")) + " " + X());
                    close();
                    scheduledExecutorService.shutdownNow();
                    l4Var.close();
                    o5VarG.close();
                    RsaAesCrypto f6Var5 = this.c0;
                    if (f6Var5 != null) {
                        f6Var5.close();
                    }
                    Iterator<Thread> it5 = this.g.iterator();
                    while (it5.hasNext()) {
                        try {
                            it5.next().join(500L);
                        } catch (InterruptedException unused3) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    Iterator<AudioRelay> it6 = this.n.values().iterator();
                    while (it6.hasNext()) {
                        it6.next().j();
                    }
                    this.E = null;
                    c2Var = new CarLifeFrameCipher(false, null);
                    this.HidCommandSender = c2Var;
                    e eVar = new e(this.J, this.K, this.L, this.Y, this.Z, this.S, this.R, this.V, this.W, this.X, this.i.get(), this.j.get(), this.k.get(), this.l.get(), this.m.get(), str3);
                    this.a.a("G3 outcome=".concat(String.valueOf(eVar)));
                    this.a.a("Byte/packet counts are not decoded frames, native producer attribution, or complete identity/media/CarPlay acceptance");
                    return eVar;
                } catch (Exception e4) {
                    e = e4;
                    r9 = o5VarG;
                    if (this.H != null) {
                        str = this.H;
                    } else {
                        str = e.getClass().getSimpleName() + ": " + R(e);
                    }
                    str3 = str;
                    this.a.a("NATIVE_SOURCE_FINAL first_channel_end=" + ((String) Objects.requireNonNullElse(this.p.get(), "NONE")) + " " + X());
                    close();
                    scheduledExecutorService.shutdownNow();
                    l4Var.close();
                    r9.close();
                    f6Var = this.c0;
                    if (f6Var != null) {
                        f6Var.close();
                    }
                    it = this.g.iterator();
                    while (it.hasNext()) {
                        it.next().join(500L);
                    }
                    it2 = this.n.values().iterator();
                    while (it2.hasNext()) {
                        it2.next().j();
                    }
                    this.E = null;
                    c2Var = new CarLifeFrameCipher(false, null);
                }
            } catch (Throwable th2) {
                th = th2;
                r10 = o5VarG;
                this.a.a("NATIVE_SOURCE_FINAL first_channel_end=" + ((String) Objects.requireNonNullElse(this.p.get(), "NONE")) + " " + X());
                close();
                scheduledExecutorService.shutdownNow();
                l4Var.close();
                r10.close();
                f6Var2 = this.c0;
                if (f6Var2 != null) {
                    f6Var2.close();
                }
                it3 = this.g.iterator();
                while (it3.hasNext()) {
                    it3.next().join(500L);
                }
                iDisplaySession = this.n.values().iterator();
                while (iDisplaySession.hasNext()) {
                    iDisplaySession.next().j();
                }
                this.E = null;
                this.HidCommandSender = new CarLifeFrameCipher(false, null);
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            
        } catch (Throwable th3) {
            th = th3;
        }
        r9 = o5VarG;
        if (this.H != null) {
            str = this.H;
        } else {
            str = e.getClass().getSimpleName() + ": " + R(e);
        }
        str3 = str;
        this.a.a("NATIVE_SOURCE_FINAL first_channel_end=" + ((String) Objects.requireNonNullElse(this.p.get(), "NONE")) + " " + X());
        close();
        scheduledExecutorService.shutdownNow();
        l4Var.close();
        r9.close();
        f6Var = this.c0;
        if (f6Var != null) {
            f6Var.close();
        }
        it = this.g.iterator();
        while (it.hasNext()) {
            try {
                it.next().join(500L);
            } catch (InterruptedException unused4) {
                Thread.currentThread().interrupt();
            }
        }
        it2 = this.n.values().iterator();
        while (it2.hasNext()) {
            it2.next().j();
        }
        this.E = null;
        c2Var = new CarLifeFrameCipher(false, null);
        this.HidCommandSender = c2Var;
        e eVar2 = new e(this.J, this.K, this.L, this.Y, this.Z, this.S, this.R, this.V, this.W, this.X, this.i.get(), this.j.get(), this.k.get(), this.l.get(), this.m.get(), str3);
        this.a.a("G3 outcome=".concat(String.valueOf(eVar2)));
        this.a.a("Byte/packet counts are not decoded frames, native producer attribution, or complete identity/media/CarPlay acceptance");
        return eVar2;
    }

    public void T(TouchGestureEncoder.TouchAction aVar, int i, int i2) throws IOException {
        long jNanoTime = System.nanoTime();
        synchronized (this.B) {
            long jNanoTime2 = System.nanoTime();
            if (!J() || this.b0 == null) {
                if (this.C.a(TouchFlow.b.NOT_READY)) {
                    O();
                }
                throw new IOException("native session not ready for touch");
            }
            try {
                TouchGestureEncoder.b bVarA = this.b0.a(aVar, i, i2);
                try {
                    CarLifeFrameCipher c2Var = this.HidCommandSender;
                    CarLifeFrame.a aVar2 = CarLifeFrame.a.TOUCH;
                    CarLifeFrame.c cVarB = c2Var.b(aVar2, bVarA.g(), bVarA.h());
                    Socket socketW = W(aVar2);
                    CarLifeFrame.l(socketW.getOutputStream(), aVar2, cVarB.i(), cVarB.g(), 32767);
                    socketW.getOutputStream().flush();
                    this.C.e(aVar, jNanoTime, jNanoTime2, System.nanoTime());
                    long j = this.D + 1;
                    this.D = j;
                    if (j <= 4 || j % 100 == 0) {
                        this.a.a("NATIVE_TOUCH_SENT action=" + aVar.name() + " count=" + j);
                        O();
                    }
                } catch (IOException e2) {
                    close();
                    if (!this.C.a(TouchFlow.b.IO)) {
                        throw e2;
                    }
                    O();
                    throw e2;
                }
            } catch (IllegalArgumentException | IllegalStateException e4) {
                if (!this.C.a(TouchFlow.b.INPUT)) {
                    throw e4;
                }
                O();
                throw e4;
            }
        }
    }

    public void U() throws IOException {
        synchronized (this.A) {
            if (this.h.get() || !this.S || !this.R || this.T) {
                throw new ProtocolIOException("native video START not eligible");
            }
            try {
                S(t1.g, new byte[0]);
                this.s = System.nanoTime();
                this.T = true;
            } catch (IOException e2) {
                throw e2;
            } catch (Exception e3) {
                throw new IOException("native video START write failed", e3);
            }
        }
        if (this.d == null || this.h.get()) {
            return;
        }
        this.d.c(this);
    }

    public byte[] Z() {
        return t1.k(this.e.m(), this.e.k(), this.e.j());
    }

    @Override 
    public void close() {
        if (this.h.compareAndSet(false, true)) {
            TouchGestureEncoder i6Var = this.b0;
            if (i6Var != null) {
                i6Var.b();
            }
            synchronized (this.f) {
                Iterator<Socket> it = this.f.values().iterator();
                while (it.hasNext()) {
                    try {
                        it.next().close();
                    } catch (IOException unused) {
                    }
                }
                this.f.clear();
            }
        }
    }

    @Override 
    public boolean e() throws IOException {
        synchronized (this.A) {
            try {
                if (J() && this.T && this.j.get() != 0 && !this.U) {
                    a0(98314);
                    if (this.h.get()) {
                        return false;
                    }
                    this.U = true;
                    return true;
                }
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    @Override 
    public boolean f() throws IOException {
        synchronized (this.A) {
            try {
                if (J() && this.T && this.j.get() != 0 && !this.U) {
                    a0(98314);
                    if (this.h.get()) {
                        return false;
                    }
                    this.U = true;
                    a0(t1.g);
                    if (this.h.get()) {
                        return false;
                    }
                    this.U = false;
                    return true;
                }
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    @Override 
    public boolean g() throws IOException {
        if (this.h.get() || !this.S || !this.R || !this.T) {
            return false;
        }
        synchronized (this.A) {
            if (!this.h.get() && this.S && this.R && this.T) {
                synchronized (this.r) {
                    if (this.h.get()) {
                        return false;
                    }
                    this.v = true;
                    this.w = false;
                    this.x = false;
                    try {
                        S(98341, new byte[0]);
                        synchronized (this.r) {
                            try {
                                if (this.v) {
                                    this.u = System.nanoTime();
                                    this.w = true;
                                }
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                        if (this.d != null && !this.h.get()) {
                            this.d.l(this, j6.b.WRITTEN);
                        }
                        return true;
                    } catch (Exception e2) {
                        synchronized (this.r) {
                            this.v = false;
                            throw new IOException("native foreground request write failed", e2);
                        }
                    }
                }
            }
            return false;
        }
    }

    @Override 
    public boolean i() throws IOException {
        synchronized (this.A) {
            try {
                if (J() && this.T && this.U) {
                    a0(t1.g);
                    if (this.h.get()) {
                        return false;
                    }
                    this.U = false;
                    return true;
                }
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    @Override 
    public j6.a k() {
        return new j6.a(true, true, true, false, false, false, false);
    }

    @Override 
    public boolean l(int i) {
        String str;
        switch (i) {
            case 14:
                str = "BACK";
                break;
            case 15:
                str = "MEDIA_PREVIOUS";
                break;
            case Iap2Csm.e :
                str = "MEDIA_NEXT";
                break;
            default:
                switch (i) {
                    case 31:
                        str = "MEDIA_PLAY";
                        break;
                    case 32:
                        str = "MEDIA_PAUSE";
                        break;
                    case 33:
                        str = "VOICE_START";
                        break;
                    default:
                        SyntheticRefs.b("unsupported native key");
                        return false;
                }
                break;
        }
        synchronized (this.B) {
            if (!J() || !this.T) {
                return false;
            }
            try {
                CarLifeFrameCipher c2Var = this.HidCommandSender;
                CarLifeFrame.a aVar = CarLifeFrame.a.TOUCH;
                CarLifeFrame.c cVarB = c2Var.b(aVar, 425992, new ProtobufReader.a().c(1, i).g());
                Socket socketW = W(aVar);
                CarLifeFrame.l(socketW.getOutputStream(), aVar, cVarB.i(), cVarB.g(), 32767);
                socketW.getOutputStream().flush();
                this.a.a("NATIVE_HARD_KEY_WRITTEN kind=" + str + " response_confirmed=false");
                return true;
            } catch (IOException e2) {
                this.a.a("NATIVE_HARD_KEY_FAILED kind=" + str);
                return false;
            }
        }
    }

    public void w(int i, byte[] bArr) throws ProtocolIOException {
        if (i != 65563 && i != 65564 && i != 65592 && i != 65569) {
            SyntheticRefs.f("unknown native UI command");
            return;
        }
        if (bArr == null || bArr.length != 0) {
            SyntheticRefs.f("invalid native UI status body");
            return;
        }
        if (this.h.get() || !this.S || !this.R || this.d == null) {
            return;
        }
        if (i == 65563) {
            this.y = true;
        } else if (i == 65564 || i == 65569) {
            this.y = false;
        }
        if (i == 65563) {
            synchronized (this.r) {
                try {
                    if (this.v && !this.h.get()) {
                        this.v = false;
                        this.a.a("NATIVE_FOREGROUND_STATUS_OBSERVED after_request=true acknowledgement=false");
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
        this.d.j(this, i);
        if (i == 65563) {
            this.d.l(this, j6.b.OBSERVED);
        }
    }

    public boolean z() {
        return false;
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class f extends ProtoMessageBase {
        private final String a;
        private final String b;
        private final String c;
        private final String d;
        private final String e;
        private final String f;
        private final int g;
        private final String h;

        public f(String str, String str2, String str3, String str4, String str5, String stCarLifeFrame, int i, String str7) {
            String[] strArr = {str, str2, str3, str4};
            for (int i2 = 0; i2 < 4; i2++) {
                String str8 = strArr[i2];
                if (str8 == null || str8.isBlank() || str8.length() > 1024) {
                    SyntheticRefs.b("actual receiver metadata required");
                    throw null;
                }
            }
            if (str7 != null && (str5 == null || str5.isBlank() || stCarLifeFrame == null || stCarLifeFrame.isBlank() || i < 0 || !str7.matches("2[0-9]{7}") || str7.equals("20000000") || str7.equals("20000001"))) {
                SyntheticRefs.b("own experimental receiver identity required");
                throw null;
            }
            this.a = str;
            this.b = str2;
            this.c = str3;
            this.d = str4;
            this.e = str5;
            this.f = stCarLifeFrame;
            this.g = i;
            this.h = str7;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof f)) {
                return false;
            }
            f fVar = (f) obj;
            return this.g == fVar.g && Objects.equals(this.a, fVar.a) && Objects.equals(this.b, fVar.b) && Objects.equals(this.c, fVar.c) && Objects.equals(this.d, fVar.d) && Objects.equals(this.e, fVar.e) && Objects.equals(this.f, fVar.f) && Objects.equals(this.h, fVar.h);
        }

        public byte[] g() {
            return new ProtobufReader.a().e(1, this.a).e(4, this.b).e(14, this.c).e(20, this.d).g();
        }

        public String h() {
            return this.b;
        }

        public final int hashCode() {
            int i = this.g;
            String str = this.a;
            String str2 = this.b;
            String str3 = this.c;
            String str4 = this.d;
            String str5 = this.e;
            String stCarLifeFrame = this.f;
            String str7 = this.h;
            return Objects.hashCode(str7) + ProtoMessageBase.b(stCarLifeFrame, ProtoMessageBase.b(str5, ProtoMessageBase.b(str4, ProtoMessageBase.b(str3, ProtoMessageBase.b(str2, ProtoMessageBase.b(str, i * 31, 31), 31), 31), 31), 31), 31);
        }

        public String i() {
            return this.h;
        }

        public String j() {
            return this.c;
        }

        public String k() {
            return this.a;
        }

        public String l() {
            return this.e;
        }

        public String m() {
            return this.d;
        }

        public byte[] n() {
            return new ProtobufReader.a().e(1, this.e).e(2, this.f).c(3, this.g).e(4, this.h).c(5, 1).c(6, 0).c(7, 0).g();
        }

        public int o() {
            return this.g;
        }

        public String p() {
            return this.f;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, this.c, this.d, this.e, this.f, Integer.valueOf(this.g), this.h};
            String[] strArrSplit = "a;b;c;d;e;f;g;h".length() == 0 ? new String[0] : "a;b;c;d;e;f;g;h".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(f.class, sb, "[");
            for (int i = 0; i < strArrSplit.length; i++) {
                sb.append(strArrSplit[i]);
                sb.append("=");
                sb.append(objArr[i]);
                if (i != strArrSplit.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        public f(String str, String str2, String str3, String str4) {
            this(str, str2, str3, str4, null, null, 0, null);
        }
    }

    /**
     * DisplaySession 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in DisplaySession, carrying local implementation detail.
     */
    public static final class NativeAudioChannel {
        private final CarLifeFrame.a a;
        private final d b;
        private final c c;
        private final AudioRelay d;
        private int e;
        private int f;
        private int g;
        private int h;
        private boolean i;
        private boolean j;
        private boolean k;
        private boolean l;

        public NativeAudioChannel(CarLifeFrame.a aVar, d dVar, c cVar, AudioRelay audioRelay) {
            if (aVar != CarLifeFrame.a.MEDIA && aVar != CarLifeFrame.a.TTS && aVar != CarLifeFrame.a.VR) {
                SyntheticRefs.b("native audio channel required");
                throw null;
            }
            this.a = aVar;
            Objects.requireNonNull(dVar);
            this.b = dVar;
            Objects.requireNonNull(cVar);
            this.c = cVar;
            Objects.requireNonNull(audioRelay);
            this.d = audioRelay;
        }

        private void b() throws IOException {
            this.b.g(this.a, this.e, this.f, this.g);
            this.l = true;
        }

        private static byte[] c(byte[] bArr, boolean z, o oVar) throws ProtocolIOException {
            if (!z || bArr.length == 0) {
                return bArr;
            }
            if (oVar == null) {
                SyntheticRefs.f("audio AES negotiated without cipher");
                return null;
            }
            byte[] bArrB = oVar.b(bArr);
            if (bArrB != null) {
                return bArrB;
            }
            throw new ProtocolIOException("null decrypted audio body");
        }

        public void a(CarLifeFrame.c cVar, boolean z, o oVar, boolean z2) throws IOException {
            int i;
            int i2;
            int i3;
            AudioRelay.StreamState bVar;
            int i4;
            int i5;
            int i6;
            int i7;
            if (cVar.h() != this.a) {
                SyntheticRefs.f("native audio channel mismatch");
                return;
            }
            this.d.i(cVar.g().length);
            CarLifeFrame.a aVar = this.a;
            CarLifeFrame.a aVar2 = CarLifeFrame.a.MEDIA;
            if (aVar == aVar2) {
                i = 196609;
            } else {
                i = aVar == CarLifeFrame.a.TTS ? 262145 : 327682;
            }
            if (aVar == aVar2) {
                i2 = 196614;
            } else {
                i2 = aVar == CarLifeFrame.a.TTS ? 262147 : 327683;
            }
            if (aVar == aVar2) {
                i3 = 196610;
            } else {
                i3 = aVar == CarLifeFrame.a.TTS ? 262146 : 327684;
            }
            int iH = cVar.i().h();
            if (iH == i) {
                this.d.f();
                boolean z3 = this.i && this.j && this.l;
                int i8 = this.e;
                int i9 = this.f;
                int i10 = this.g;
                this.i = false;
                this.j = false;
                this.l = false;
                this.k = false;
                ProtobufReader.e eVarA = ProtobufReader.a(c(cVar.g(), z, oVar));
                this.e = eVarA.i(1);
                this.f = eVarA.i(2);
                int i11 = eVarA.i(3);
                this.g = i11;
                int i12 = this.e;
                if (i12 <= 0 || (i4 = this.f) <= 0 || i11 <= 0) {
                    SyntheticRefs.f("invalid explicit PCM format");
                    return;
                }
                this.i = true;
                boolean z4 = i12 >= 4000 && i12 <= 192000 && (i4 == 1 || i4 == 2) && i11 == 16;
                this.j = z4;
                this.d.g(z4);
                this.l = z3 && this.j && (i5 = this.e) == i8 && (i6 = this.f) == i9 && (i7 = this.g) == i10 && this.b.i(this.a, i5, i6, i7);
                this.c.a("Native " + String.valueOf(this.a) + " INIT rate=" + this.e + " channels=" + this.f + " bits=" + this.g + " forwarding=" + this.j);
                if (this.l) {
                    this.c.a("NATIVE_AUDIO_FORMAT_REUSED channel=".concat(String.valueOf(this.a)));
                }
                if (z2 && this.j && !this.l) {
                    b();
                    return;
                }
                return;
            }
            if (iH != i2) {
                if (iH == i3) {
                    this.d.l();
                    boolean z5 = this.i;
                    this.i = false;
                    this.j = false;
                    this.l = false;
                    this.k = false;
                    if (z5) {
                        this.b.d(this.a);
                        return;
                    }
                    return;
                }
                CarLifeFrame.a aVar3 = this.a;
                if (aVar3 == aVar2 && iH == 196611) {
                    this.k = true;
                    return;
                }
                if (aVar3 == aVar2 && iH == 196612) {
                    this.k = false;
                    return;
                } else {
                    if (aVar3 == CarLifeFrame.a.VR && iH == 327686) {
                        this.d.h();
                        return;
                    }
                    return;
                }
            }
            this.d.c();
            boolean z6 = this.i;
            if (z6 && this.j && !this.k && z2) {
                byte[] bArrC = c(cVar.g(), z && this.a != aVar2, oVar);
                if (bArrC.length == 0 || bArrC.length % (this.f * 2) != 0) {
                    SyntheticRefs.f("partial/empty native PCM16 frame");
                    return;
                }
                this.d.m(bArrC.length);
                if (!this.l) {
                    b();
                }
                this.b.k(this.a, bArrC, cVar.i().j());
                this.d.a(bArrC.length);
                return;
            }
            AudioRelay audioRelay = this.d;
            if (!z6) {
                bVar = AudioRelay.StreamState.NO_INIT;
            } else if (this.j) {
                bVar = this.k ? AudioRelay.StreamState.PAUSED : AudioRelay.StreamState.SESSION_GATE;
            } else {
                bVar = AudioRelay.StreamState.UNSUPPORTED_FORMAT;
            }
            audioRelay.n(bVar);
            int i13 = this.h;
            this.h = i13 + 1;
            if (i13 < 3) {
                this.c.a("Native " + String.valueOf(this.a) + " PCM withheld: initialized=" + this.i + " supported=" + this.j + " paused=" + this.k + " sessionAccepted=" + z2);
            }
        }

        public NativeAudioChannel(CarLifeFrame.a aVar, d dVar, c cVar) {
            
            
            
            
            Objects.requireNonNull(cVar);
            r4 r4Var = new r4(1);
            r4Var.b = cVar;
            VarHandle.storeStoreFence();
            AudioRelay audioRelay = new AudioRelay(aVar, r4Var);
            if (aVar != CarLifeFrame.a.MEDIA && aVar != CarLifeFrame.a.TTS && aVar != CarLifeFrame.a.VR) {
                SyntheticRefs.b("native audio channel required");
                throw null;
            }
            this.a = aVar;
            Objects.requireNonNull(dVar);
            this.b = dVar;
            Objects.requireNonNull(cVar);
            this.c = cVar;
            Objects.requireNonNull(audioRelay);
            this.d = audioRelay;
        }
    }

    /**
     * 扩展实现：在 InputStream 基础上补充 DisplaySession 需要的局部行为。
     *
     * Extension of InputStream adding the local behaviour DisplaySession needs.
     */
    public final class b extends InputStream {
        private final Socket a;
        private final InputStream b;

        public b(Socket socket) throws IOException {
            this.a = socket;
            this.b = socket.getInputStream();
        }

        @Override 
        public int read(byte[] bArr, int i, int i2) throws IOException {
            if (i2 == 0) {
                return 0;
            }
            while (!DisplaySession.this.h.get()) {
                this.a.setSoTimeout(DisplaySession.v(DisplaySession.this));
                try {
                    return this.b.read(bArr, i, i2);
                } catch (SocketTimeoutException e) {
                    if (System.nanoTime() >= DisplaySession.this.G) {
                        throw e;
                    }
                }
            }
            SyntheticRefs.c("cancelled");
            return 0;
        }

        @Override 
        public int read() throws IOException {
            byte[] bArr = new byte[1];
            if (read(bArr, 0, 1) < 0) {
                return -1;
            }
            return bArr[0] & 255;
        }
    }

    
    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class VideoConfig extends ProtoMessageBase {
        private final int a;
        private final int b;
        private final int c;
        private final boolean d;
        private final int e;
        private final boolean f;

        public VideoConfig(int i, int i2, int i3, boolean z, int i4, boolean z2) {
            if (i < 0 || i > 1800 || ((z && i == 0) || i2 < 1 || i2 > 4096 || i3 < 1 || i3 > 4096 || i4 < 1 || i4 > 60)) {
                SyntheticRefs.b("duration 0 (continuous non-diagnostic) or 1..1800 seconds; dimensions 1..4096 and fps 1..60 required");
                throw null;
            }
            if (z2 && (i2 < 8 || i3 < 8 || Math.max(i2, i3) > 1920 || (i2 & 1) != 0 || (i3 & 1) != 0)) {
                SyntheticRefs.b("exact native geometry requires even dimensions 8..1920");
                throw null;
            }
            this.a = i;
            this.b = i2;
            this.c = i3;
            this.d = z;
            this.e = i4;
            this.f = z2;
        }

        public static VideoConfig g() {
            return new VideoConfig(55, 1280, 720, true);
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof VideoConfig)) {
                return false;
            }
            VideoConfig gVar = (VideoConfig) obj;
            return this.d == gVar.d && this.f == gVar.f && this.a == gVar.a && this.b == gVar.b && this.c == gVar.c && this.e == gVar.e;
        }

        public boolean h() {
            return this.d;
        }

        public final int hashCode() {
            boolean z = this.d;
            boolean z2 = this.f;
            int i = this.a;
            int i2 = this.b;
            int i3 = this.c;
            return ((((((((Boolean.hashCode(z2) + (Boolean.hashCode(z) * 31)) * 31) + i) * 31) + i2) * 31) + i3) * 31) + this.e;
        }

        public int i() {
            return this.a;
        }

        public int j() {
            return this.e;
        }

        public int k() {
            return this.c;
        }

        public boolean l() {
            return this.f;
        }

        public int m() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c), Boolean.valueOf(this.d), Integer.valueOf(this.e), Boolean.valueOf(this.f)};
            String[] strArrSplit = "a;b;c;d;e;f".length() == 0 ? new String[0] : "a;b;c;d;e;f".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(AudioPacer.class, sb, "[");
            for (int i = 0; i < strArrSplit.length; i++) {
                sb.append(strArrSplit[i]);
                sb.append("=");
                sb.append(objArr[i]);
                if (i != strArrSplit.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        public VideoConfig(int i, int i2, int i3, boolean z) {
            this(i, i2, i3, z, 30);
        }

        public VideoConfig(int i, int i2, int i3, boolean z, int i4) {
            this(i, i2, i3, z, i4, false);
        }
    }

    public DisplaySession(c cVar, f fVar, h hVar) {
        this(cVar, fVar, hVar, null, DisplaySession.VideoConfig.g());
    }

    
    public DisplaySession(c cVar, f fVar, d dVar, VideoConfig gVar) {
        this(cVar, fVar, dVar, dVar, gVar);
        Objects.requireNonNull(dVar);
    }

    public DisplaySession(c cVar, f fVar) {
        this(cVar, fVar, new s4());
    }
}
