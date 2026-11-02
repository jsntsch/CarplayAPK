package io.carplay.app.prototype;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import io.carplay.app.secured.EventChannel;
import io.carplay.app.secured.PcmConverter;
import io.carplay.app.secured.VideoCipher;
import io.carplay.app.secured.d0;
import io.carplay.app.secured.d1;
import io.carplay.app.secured.j;
import io.carplay.app.secured.p6;
import io.carplay.app.secured.q6;
import io.carplay.app.secured.w1;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/* JADX INFO: loaded from: classes.dex */
/**
 * AvcEncoder
 *
 * <p>原混淆名：{@code io.carplay.app.prototype.f}（jadx 产物 f.java）
 *
 * <p>命名依据：AVC frame rate / AVC_ENCODER_FRAGMENT_SIZE
 */
final class AvcEncoder implements AutoCloseable {
    private long A;
    private long B;
    private volatile long C;
    private long D;
    private long E;
    private MediaCodec F;
    private MediaCodec G;
    private Surface H;
    private Surface I;
    private SurfaceTexture J;
    private HandlerThread K;
    private EGLDisplay L;
    private EGLContext M;
    private EGLSurface N;
    private int O;
    private int P;
    private byte[] Q;
    private byte[] R;
    private byte[] S;
    private final MediaCodec.BufferInfo T;
    private final MediaCodec.BufferInfo U;
    private final float[] V;
    private final FloatBuffer W;
    private final q6 a;
    private final int b;
    private final int c;
    private volatile int d;
    private a e;
    private final f f;
    private final AvcEncoder g;
    private final CountDownLatch h;
    private final VideoCipher.b i;
    private final ArrayBlockingQueue<h> j;
    private final Map<Long, d> k;
    private final p6 l;
    private final e m;
    private final b n;
    private final AtomicBoolean o;
    private final AtomicBoolean p;
    private final Object q;
    private final Thread r;
    private boolean s;
    private boolean t;
    private volatile byte[] u;
    private volatile byte[] v;
    private final g w;
    private long x;
    private long y;
    private long z;

    public static final class a {
        private final int a;
        private final int b;
        private int c;

        public a(int i, int i2) {
            if (i < 1 || i > 240 || i2 < 1 || i2 > 60) {
                d0.b("AVC frame rate");
                throw null;
            }
            this.a = i;
            int iMin = Math.min(i, i2);
            this.b = iMin;
            this.c = i - iMin;
        }

        public boolean a() {
            int i = this.c + this.b;
            this.c = i;
            int i2 = this.a;
            if (i < i2) {
                return false;
            }
            this.c = i - i2;
            return true;
        }
    }

    public static final class b {
        private static final int e = 4194304;
        private static final int f = 256;
        private ByteArrayOutputStream a;
        private long b;
        private int c;
        private int d;

        private IOException b(String str) {
            d();
            return new IOException(str);
        }

        public c a(byte[] bArr, long j, int i) throws IOException {
            if (bArr == null || bArr.length == 0 || bArr.length > 4194304) {
                throw b("AVC_ENCODER_FRAGMENT_SIZE");
            }
            boolean z = (i & 8) != 0;
            if (z && (i & 4) != 0) {
                throw b("AVC_ENCODER_FRAGMENT_TRUNCATED");
            }
            ByteArrayOutputStream byteArrayOutputStream = this.a;
            if (byteArrayOutputStream == null && !z) {
                return new c(bArr, j, i);
            }
            if (byteArrayOutputStream == null) {
                this.a = new ByteArrayOutputStream(Math.min(bArr.length, PcmConverter.t));
                this.b = j;
                this.c = 0;
                this.d = 0;
            }
            if (j != this.b) {
                throw b("AVC_ENCODER_FRAGMENT_PTS_CHANGED");
            }
            int i2 = this.d + 1;
            this.d = i2;
            if (i2 > f || bArr.length > 4194304 - this.a.size()) {
                throw b("AVC_ENCODER_FRAGMENT_LIMIT");
            }
            this.a.writeBytes(bArr);
            this.c |= i;
            if (z) {
                return null;
            }
            c cVar = new c(this.a.toByteArray(), this.b, this.c & (-9));
            d();
            return cVar;
        }

        public boolean c() {
            return this.a != null;
        }

        public void d() {
            this.a = null;
            this.c = 0;
            this.d = 0;
        }
    }

    public static final class c extends w1 {
        private final byte[] a;
        private final long b;
        private final int c;

        public c(byte[] bArr, long j, int i) {
            this.a = bArr;
            this.b = j;
            this.c = i;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof c)) {
                return false;
            }
            c cVar = (c) obj;
            return this.c == cVar.c && this.b == cVar.b && Objects.equals(this.a, cVar.a);
        }

        public byte[] g() {
            return this.a;
        }

        public int h() {
            return this.c;
        }

        public final int hashCode() {
            int i = this.c;
            long j = this.b;
            byte[] bArr = this.a;
            return Objects.hashCode(bArr) + w1.a(j, i * 31, 31);
        }

        public long i() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {this.a, Long.valueOf(this.b), Integer.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(c.class, sb, "[");
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
    }

    public static final class e {
        private long a;
        private boolean b = true;

        public synchronized boolean a(long j, boolean z) {
            if (j < this.a) {
                return false;
            }
            if (this.b && !z) {
                return false;
            }
            if (z) {
                this.b = false;
            }
            return true;
        }

        public synchronized void b(long j) {
            this.a = Math.max(this.a, j);
            this.b = true;
        }
    }

    public interface f {
        void a(String str);

        default void b(io.carplay.app.secured.h hVar) {
            d(hVar.a(), hVar.n());
        }

        default void c(int i) {
        }

        void d(byte[] bArr, long j);

        void e(String str);
    }

    public static final class g {
        private byte[] a;
        private int b;

        public synchronized String a(byte[] bArr, int i, int i2) {
            if (this.b < 16 && bArr != null && !Arrays.equals(this.a, bArr)) {
                byte[] bArr2 = (byte[]) bArr.clone();
                this.a = bArr2;
                this.b++;
                return j.a(bArr2, i, i2);
            }
            return null;
        }
    }

    public AvcEncoder(q6 q6Var, int i, int i2, f fVar, AvcEncoder avcEncoder) {
        this.h = new CountDownLatch(1);
        this.i = new VideoCipher.b();
        this.j = new ArrayBlockingQueue<>(8);
        this.k = new HashMap();
        this.l = new p6();
        this.m = new e();
        this.n = new b();
        this.o = new AtomicBoolean();
        this.p = new AtomicBoolean(true);
        this.q = new Object();
        this.s = true;
        this.w = new g();
        this.y = -1L;
        this.D = -1L;
        this.E = -1L;
        this.L = EGL14.EGL_NO_DISPLAY;
        this.M = EGL14.EGL_NO_CONTEXT;
        this.N = EGL14.EGL_NO_SURFACE;
        this.T = new MediaCodec.BufferInfo();
        this.U = new MediaCodec.BufferInfo();
        this.V = new float[16];
        FloatBuffer floatBufferAsFloatBuffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.W = floatBufferAsFloatBuffer;
        this.a = q6Var;
        this.c = Math.max(1, Math.min(240, i));
        int iMax = Math.max(1, Math.min(60, i2));
        this.b = iMax;
        this.d = iMax;
        this.f = fVar;
        this.g = avcEncoder;
        floatBufferAsFloatBuffer.put(new float[]{-1.0f, -1.0f, 0.0f, 0.0f, 1.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f}).position(0);
        io.carplay.app.prototype.a aVar = new io.carplay.app.prototype.a(0);
        aVar.b = this;
        VarHandle.storeStoreFence();
        Thread thread = new Thread(aVar, "CarPlus-auto-fit");
        this.r = thread;
        thread.setDaemon(true);
        thread.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void A(int i) {
        this.G.releaseOutputBuffer(i, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void B() {
        this.K.quitSafely();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void C(SurfaceTexture surfaceTexture) {
        synchronized (this.q) {
            this.t = true;
            this.q.notifyAll();
        }
    }

    private void D(Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException e2) {
            this.f.a("AUTO_FIT_RESOURCE_RELEASE_ERROR type=".concat(e2.getClass().getSimpleName()));
        }
    }

    private void E() {
        int i;
        MediaCodec[] mediaCodecArr = {this.F, this.G};
        int i2 = 0;
        while (true) {
            i = 2;
            if (i2 >= 2) {
                break;
            }
            MediaCodec mediaCodec = mediaCodecArr[i2];
            if (mediaCodec != null) {
                try {
                    mediaCodec.stop();
                } catch (RuntimeException unused) {
                }
                try {
                    mediaCodec.release();
                } catch (RuntimeException unused2) {
                }
            }
            i2++;
        }
        Surface surface = this.H;
        int i3 = 1;
        if (surface != null) {
            Objects.requireNonNull(surface);
            d1 d1Var = new d1(i3);
            d1Var.b = surface;
            VarHandle.storeStoreFence();
            D(d1Var);
        }
        SurfaceTexture surfaceTexture = this.J;
        if (surfaceTexture != null) {
            Objects.requireNonNull(surfaceTexture);
            d1 d1Var2 = new d1(i);
            d1Var2.b = surfaceTexture;
            VarHandle.storeStoreFence();
            D(d1Var2);
        }
        if (this.K != null) {
            io.carplay.app.prototype.a aVar = new io.carplay.app.prototype.a(i3);
            aVar.b = this;
            VarHandle.storeStoreFence();
            D(aVar);
        }
        if (this.L != EGL14.EGL_NO_DISPLAY) {
            int i4 = this.P;
            if (i4 != 0) {
                GLES20.glDeleteProgram(i4);
            }
            int i5 = this.O;
            if (i5 != 0) {
                GLES20.glDeleteTextures(1, new int[]{i5}, 0);
            }
            EGLDisplay eGLDisplay = this.L;
            EGLSurface eGLSurface = EGL14.EGL_NO_SURFACE;
            EGL14.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, EGL14.EGL_NO_CONTEXT);
            EGLSurface eGLSurface2 = this.N;
            if (eGLSurface2 != EGL14.EGL_NO_SURFACE) {
                EGL14.eglDestroySurface(this.L, eGLSurface2);
            }
            EGLContext eGLContext = this.M;
            if (eGLContext != EGL14.EGL_NO_CONTEXT) {
                EGL14.eglDestroyContext(this.L, eGLContext);
            }
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(this.L);
        }
        Surface surface2 = this.I;
        if (surface2 != null) {
            Objects.requireNonNull(surface2);
            d1 d1Var3 = new d1(i3);
            d1Var3.b = surface2;
            VarHandle.storeStoreFence();
            D(d1Var3);
        }
        this.j.clear();
        this.k.clear();
        this.n.d();
    }

    private void G() {
        this.R = null;
        this.S = null;
        this.m.b(this.C);
        this.p.set(true);
    }

    public static boolean H(int i, long j, boolean z) {
        if (i >= 8) {
            return true;
        }
        if (i <= 0 || j <= 150000000) {
            return false;
        }
        return z || j > 250000000;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void I() {
        h hVar;
        long j;
        int iDequeueInputBuffer;
        try {
            try {
                AvcEncoder avcEncoder = this.g;
                if (avcEncoder != null && !avcEncoder.h.await(2L, TimeUnit.SECONDS)) {
                    throw new IOException("previous hardware codec still closing");
                }
                if (this.o.get()) {
                    this.o.set(true);
                    try {
                        try {
                            E();
                        } catch (RuntimeException e2) {
                            this.f.a("AUTO_FIT_RELEASE_ERROR type=".concat(e2.getClass().getSimpleName()));
                        }
                        return;
                    } finally {
                        this.h.countDown();
                    }
                }
                K();
                h hVarPoll = this.j.poll(10L, TimeUnit.SECONDS);
                if (hVarPoll == null) {
                    throw new IOException("native IDR unavailable for automatic fit");
                }
                L();
                while (!this.o.get()) {
                    r();
                    if (hVarPoll == null) {
                        hVarPoll = this.j.poll();
                    }
                    if (hVarPoll == null || hVarPoll.k() >= this.C) {
                        hVar = hVarPoll;
                    } else {
                        hVarPoll = null;
                        hVar = null;
                    }
                    long j2 = 0;
                    long j3 = 1000000;
                    if (hVar != null && (iDequeueInputBuffer = this.F.dequeueInputBuffer(1000L)) >= 0) {
                        ByteBuffer inputBuffer = this.F.getInputBuffer(iDequeueInputBuffer);
                        if (inputBuffer == null || inputBuffer.capacity() < hVar.i().length) {
                            throw new IOException("decoder input capacity");
                        }
                        inputBuffer.clear();
                        inputBuffer.put(hVar.i());
                        long jNanoTime = System.nanoTime();
                        long j4 = this.z;
                        this.z = 1 + j4;
                        long j5 = (j4 * 1000000) / ((long) this.c);
                        if (this.k.size() >= 128) {
                            throw new IOException("AVC adapter metadata limit");
                        }
                        this.k.put(Long.valueOf(j5), new d(hVar, jNanoTime, 0L, 0));
                        this.l.d(p6.b.ADAPTER_QUEUE, hVar.c, jNanoTime);
                        this.F.queueInputBuffer(iDequeueInputBuffer, 0, hVar.i().length, j5, hVar.a.e() ? 1 : 0);
                        if (this.D < 0) {
                            this.D = System.nanoTime();
                        }
                        hVarPoll = null;
                    }
                    int iDequeueOutputBuffer = this.F.dequeueOutputBuffer(this.T, 2000L);
                    if (iDequeueOutputBuffer >= 0) {
                        this.D = -1L;
                        d dVar = this.k.get(Long.valueOf(this.T.presentationTimeUs));
                        long jNanoTime2 = System.nanoTime();
                        if (dVar != null) {
                            j = jNanoTime2;
                            this.l.d(p6.b.DECODE, dVar.b, j);
                        } else {
                            j = jNanoTime2;
                        }
                        boolean z = this.T.size > 0 && dVar != null && dVar.a.k() >= this.C && this.e.a();
                        synchronized (this.q) {
                            this.t = false;
                        }
                        this.F.releaseOutputBuffer(iDequeueOutputBuffer, z);
                        if (z) {
                            n();
                            if (this.o.get()) {
                                break;
                            }
                            s(this.T.presentationTimeUs);
                            long jNanoTime3 = System.nanoTime();
                            this.l.d(p6.b.RENDER, j, jNanoTime3);
                            this.k.put(Long.valueOf(this.T.presentationTimeUs), new d(dVar.a, dVar.b, jNanoTime3, 0));
                            this.y = this.T.presentationTimeUs;
                            r();
                        } else {
                            this.k.remove(Long.valueOf(this.T.presentationTimeUs));
                        }
                    }
                    if (this.p.compareAndSet(true, false)) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("request-sync", 0);
                        this.G.setParameters(bundle);
                    }
                    long jNanoTime4 = System.nanoTime();
                    long j6 = 5000000000L;
                    if (jNanoTime4 - this.B >= 5000000000L) {
                        this.B = jNanoTime4;
                        for (Map.Entry<p6.b, p6.c> entry : this.l.e().entrySet()) {
                            p6.c value = entry.getValue();
                            long j7 = j6;
                            long j8 = j2;
                            long j9 = j3;
                            this.f.a("AUTO_FIT_METRIC stage=" + String.valueOf(entry.getKey()) + " count=" + value.g() + " p50_ms=" + (value.i() / j3) + " p95_ms=" + (value.j() / j9));
                            j6 = j7;
                            j2 = j8;
                            j3 = j9;
                        }
                    }
                    long j10 = j6;
                    long j11 = j2;
                    long j12 = this.D;
                    if (j12 < j11 || jNanoTime4 - j12 <= j10) {
                        long j13 = this.E;
                        if (j13 >= j11 && jNanoTime4 - j13 > j10) {
                        }
                    }
                    throw new IOException("automatic fit codec progress stalled");
                }
                this.o.set(true);
                try {
                    try {
                        E();
                    } catch (RuntimeException e3) {
                        this.f.a("AUTO_FIT_RELEASE_ERROR type=" + e3.getClass().getSimpleName());
                    }
                } finally {
                    this.h.countDown();
                }
            } catch (Exception e4) {
                if (!this.o.get()) {
                    this.f.e(e4.getClass().getSimpleName());
                }
                this.o.set(true);
                try {
                    try {
                        E();
                    } catch (RuntimeException e5) {
                        this.f.a("AUTO_FIT_RELEASE_ERROR type=" + e5.getClass().getSimpleName());
                    }
                } finally {
                    this.h.countDown();
                }
            }
        } catch (Throwable th) {
            this.o.set(true);
            try {
                try {
                    E();
                } catch (RuntimeException e6) {
                    this.f.a("AUTO_FIT_RELEASE_ERROR type=" + e6.getClass().getSimpleName());
                }
                throw th;
            } finally {
                this.h.countDown();
            }
        }
    }

    private void K() throws IOException {
        int i;
        IOException iOException = null;
        int i2 = 0;
        IOException iOException2 = null;
        while (true) {
            int i3 = this.b;
            if (i2 >= (i3 > 30 ? 2 : 1)) {
                iOException = iOException2;
                break;
            }
            if (i2 != 0) {
                i3 = 30;
            }
            this.d = i3;
            try {
                M();
                break;
            } catch (IOException | RuntimeException e2) {
                Surface surface = this.I;
                if (surface != null) {
                    surface.release();
                    this.I = null;
                }
                MediaCodec mediaCodec = this.G;
                if (mediaCodec != null) {
                    try {
                        mediaCodec.release();
                    } catch (RuntimeException unused) {
                    }
                    this.G = null;
                }
                IOException iOException3 = new IOException("hardware AVC encoder setup failed", e2);
                if (i2 == 0 && (i = this.b) > 30) {
                    this.f.a("AUTO_FIT_FPS_FALLBACK requested=" + i + " actual=30");
                }
                i2++;
                iOException2 = iOException3;
            }
        }
        if (iOException != null) {
            throw iOException;
        }
        this.e = new a(this.c, this.d);
        this.f.c(this.d);
        N();
        HandlerThread handlerThread = new HandlerThread("CarPlus-frame-callback");
        this.K = handlerThread;
        handlerThread.start();
        SurfaceTexture surfaceTexture = new SurfaceTexture(this.O);
        this.J = surfaceTexture;
        surfaceTexture.setDefaultBufferSize(this.a.q(), this.a.p());
        SurfaceTexture surfaceTexture2 = this.J;
        io.carplay.app.prototype.b bVar = new io.carplay.app.prototype.b();
        bVar.a = this;
        VarHandle.storeStoreFence();
        surfaceTexture2.setOnFrameAvailableListener(bVar, new Handler(this.K.getLooper()));
        this.H = new Surface(this.J);
        this.f.a("AUTO_FIT_CODEC_READY fps=" + this.d + " source=" + this.a.q() + "x" + this.a.p() + " canvas=" + this.a.h() + "x" + this.a.g());
    }

    private void L() throws IOException {
        MediaFormat mediaFormatCreateVideoFormat = MediaFormat.createVideoFormat("video/avc", this.a.q(), this.a.p());
        mediaFormatCreateVideoFormat.setInteger("max-input-size", EventChannel.L);
        byte[] bArrQ = Q();
        byte[] bArrP = P();
        if (bArrQ == null || bArrP == null) {
            d0.c("native AVC config missing");
            return;
        }
        mediaFormatCreateVideoFormat.setByteBuffer("csd-0", ByteBuffer.wrap(io.carplay.app.secured.i.c(List.of(bArrQ))));
        mediaFormatCreateVideoFormat.setByteBuffer("csd-1", ByteBuffer.wrap(io.carplay.app.secured.i.c(List.of(bArrP))));
        MediaCodec mediaCodecCreateDecoderByType = MediaCodec.createDecoderByType("video/avc");
        this.F = mediaCodecCreateDecoderByType;
        boolean zIsFeatureSupported = mediaCodecCreateDecoderByType.getCodecInfo().getCapabilitiesForType("video/avc").isFeatureSupported("low-latency");
        if (zIsFeatureSupported) {
            mediaFormatCreateVideoFormat.setInteger("low-latency", 1);
        }
        this.F.configure(mediaFormatCreateVideoFormat, this.H, (MediaCrypto) null, 0);
        this.F.start();
        this.f.a("AUTO_FIT_DECODER low_latency=" + zIsFeatureSupported);
    }

    private void M() throws IOException {
        MediaFormat mediaFormatV = v(this.a.h(), this.a.g(), this.d);
        String strW = w(mediaFormatV, this.a.h(), this.a.g(), this.d);
        if (strW == null) {
            d0.c("hardware AVC encoder unavailable for receiver size");
            return;
        }
        MediaCodec mediaCodecCreateByCodecName = MediaCodec.createByCodecName(strW);
        this.G = mediaCodecCreateByCodecName;
        mediaCodecCreateByCodecName.configure(mediaFormatV, (Surface) null, (MediaCrypto) null, 1);
        this.I = this.G.createInputSurface();
        this.G.start();
    }

    private void N() throws IOException {
        EGLDisplay eGLDisplayEglGetDisplay = EGL14.eglGetDisplay(0);
        this.L = eGLDisplayEglGetDisplay;
        int[] iArr = new int[2];
        if (eGLDisplayEglGetDisplay == EGL14.EGL_NO_DISPLAY || !EGL14.eglInitialize(eGLDisplayEglGetDisplay, iArr, 0, iArr, 1)) {
            d0.c("fit EGL init failed");
            return;
        }
        EGLConfig[] eGLConfigArr = new EGLConfig[1];
        int[] iArr2 = new int[1];
        if (!EGL14.eglChooseConfig(this.L, new int[]{12324, 8, 12323, 8, 12322, 8, 12352, 4, 12339, 4, 12610, 1, 12344}, 0, eGLConfigArr, 0, 1, iArr2, 0) || iArr2[0] < 1) {
            d0.c("recordable EGL config unavailable");
            return;
        }
        this.M = EGL14.eglCreateContext(this.L, eGLConfigArr[0], EGL14.EGL_NO_CONTEXT, new int[]{12440, 2, 12344}, 0);
        EGLSurface eGLSurfaceEglCreateWindowSurface = EGL14.eglCreateWindowSurface(this.L, eGLConfigArr[0], this.I, new int[]{12344}, 0);
        this.N = eGLSurfaceEglCreateWindowSurface;
        EGLContext eGLContext = this.M;
        if (eGLContext == EGL14.EGL_NO_CONTEXT || eGLSurfaceEglCreateWindowSurface == EGL14.EGL_NO_SURFACE || !EGL14.eglMakeCurrent(this.L, eGLSurfaceEglCreateWindowSurface, eGLSurfaceEglCreateWindowSurface, eGLContext)) {
            d0.c("fit EGL surface failed");
            return;
        }
        int[] iArr3 = new int[1];
        GLES20.glGenTextures(1, iArr3, 0);
        int i = iArr3[0];
        this.O = i;
        GLES20.glBindTexture(36197, i);
        GLES20.glTexParameteri(36197, 10241, 9729);
        GLES20.glTexParameteri(36197, 10240, 9729);
        GLES20.glTexParameteri(36197, 10242, 33071);
        GLES20.glTexParameteri(36197, 10243, 33071);
        int iO = O(35633, "attribute vec4 aPosition;attribute vec2 aTexture;uniform mat4 uTransform;varying vec2 vTexture;void main(){gl_Position=aPosition;vTexture=(uTransform*vec4(aTexture,0.,1.)).xy;}");
        int iO2 = O(35632, "#extension GL_OES_EGL_image_external : require\nprecision mediump float;varying vec2 vTexture;uniform samplerExternalOES uTexture;void main(){gl_FragColor=texture2D(uTexture,vTexture);}");
        try {
            int iGlCreateProgram = GLES20.glCreateProgram();
            this.P = iGlCreateProgram;
            GLES20.glAttachShader(iGlCreateProgram, iO);
            GLES20.glAttachShader(this.P, iO2);
            GLES20.glLinkProgram(this.P);
            int[] iArr4 = new int[1];
            GLES20.glGetProgramiv(this.P, 35714, iArr4, 0);
            if (iArr4[0] == 0) {
                throw new IOException("fit shader link failed");
            }
            GLES20.glDeleteShader(iO);
            GLES20.glDeleteShader(iO2);
        } catch (Throwable th) {
            GLES20.glDeleteShader(iO);
            GLES20.glDeleteShader(iO2);
            throw th;
        }
    }

    private static int O(int i, String str) throws IOException {
        int iGlCreateShader = GLES20.glCreateShader(i);
        GLES20.glShaderSource(iGlCreateShader, str);
        GLES20.glCompileShader(iGlCreateShader);
        int[] iArr = new int[1];
        GLES20.glGetShaderiv(iGlCreateShader, 35713, iArr, 0);
        if (iArr[0] != 0) {
            return iGlCreateShader;
        }
        GLES20.glDeleteShader(iGlCreateShader);
        d0.c("fit shader compile failed");
        return 0;
    }

    public static boolean R(int i, int i2, int i3) {
        if (i >= 1 && i2 >= 1 && i <= 4096 && i2 <= 4096 && i3 >= 1 && i3 <= 60) {
            try {
                if (w(v(i, i2, i3), i, i2, i3) != null) {
                    return true;
                }
                if (i3 > 30 && w(v(i, i2, 30), i, i2, 30) != null) {
                    return true;
                }
            } catch (RuntimeException unused) {
            }
        }
        return false;
    }

    public static boolean S(int i, int i2, int i3) {
        if (i >= 8 && i2 >= 8 && i <= 1920 && i2 <= 1920 && (i & 1) == 0 && (i2 & 1) == 0 && i3 >= 1 && i3 <= 60) {
            try {
                if (w(v(i, i2, i3), i, i2, i3) != null) {
                    return true;
                }
            } catch (RuntimeException unused) {
            }
        }
        return false;
    }

    private void n() {
        long jNanoTime = System.nanoTime() + 2000000000;
        synchronized (this.q) {
            while (!this.t && !this.o.get()) {
                try {
                    long jNanoTime2 = jNanoTime - System.nanoTime();
                    if (jNanoTime2 <= 0) {
                        throw new IOException("decoder surface timeout");
                    }
                    TimeUnit.NANOSECONDS.timedWait(this.q, jNanoTime2);
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
        if (this.o.get()) {
            return;
        }
        this.J.updateTexImage();
        this.J.getTransformMatrix(this.V);
    }

    private static byte[] o(byte[] bArr, byte[] bArr2) {
        byte[] bArrCopyOf = Arrays.copyOf(bArr, bArr.length + bArr2.length);
        System.arraycopy(bArr2, 0, bArrCopyOf, bArr.length, bArr2.length);
        return bArrCopyOf;
    }

    private static byte[] p(ByteBuffer byteBuffer) {
        ByteBuffer byteBufferDuplicate = byteBuffer.duplicate();
        byte[] bArr = new byte[byteBufferDuplicate.remaining()];
        byteBufferDuplicate.get(bArr);
        return bArr;
    }

    public static c q(Supplier<ByteBuffer> supplier, Runnable runnable, int i, int i2, long j, int i3) {
        Throwable th = null;
        try {
            try {
                ByteBuffer byteBuffer = supplier.get();
                if (byteBuffer != null && i2 > 0) {
                    if (i < 0 || i > byteBuffer.capacity() || i2 > byteBuffer.capacity() - i || i2 > 4194304) {
                        throw new IOException("AVC_ENCODER_OUTPUT_RANGE");
                    }
                    ByteBuffer byteBufferDuplicate = byteBuffer.duplicate();
                    byteBufferDuplicate.clear();
                    byteBufferDuplicate.position(i);
                    byteBufferDuplicate.limit(i + i2);
                    c cVar = new c(p(byteBufferDuplicate), j, i3);
                    runnable.run();
                    return cVar;
                }
                c cVar2 = new c(new byte[0], j, i3);
                runnable.run();
                return cVar2;
            } catch (IOException | Error | RuntimeException e2) {
                throw e2;
            }
        } catch (Throwable th2) {
            try {
                runnable.run();
            } catch (Error | RuntimeException e3) {
                if (0 == 0) {
                    throw e3;
                }
                th.addSuppressed(e3);
            }
            throw th2;
        }
    }

    private void r() throws IOException {
        int iDequeueOutputBuffer;
        int i = 0;
        for (int i2 = 0; i2 < 16 && !this.o.get() && (iDequeueOutputBuffer = this.G.dequeueOutputBuffer(this.U, 0L)) != -1; i2++) {
            if (iDequeueOutputBuffer == -2) {
                if (this.n.c()) {
                    d0.c("AVC_ENCODER_FRAGMENT_FORMAT_CHANGED");
                    return;
                }
                MediaFormat outputFormat = this.G.getOutputFormat();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                String[] strArr = {"csd-0", "csd-1"};
                for (int i3 = 0; i3 < 2; i3++) {
                    ByteBuffer byteBuffer = outputFormat.getByteBuffer(strArr[i3]);
                    if (byteBuffer != null) {
                        byteArrayOutputStream.writeBytes(io.carplay.app.secured.i.a(p(byteBuffer)));
                    }
                }
                this.Q = byteArrayOutputStream.toByteArray();
                G();
            } else if (iDequeueOutputBuffer < 0) {
                continue;
            } else {
                io.carplay.app.prototype.c cVar = new io.carplay.app.prototype.c();
                cVar.a = this;
                cVar.b = iDequeueOutputBuffer;
                VarHandle.storeStoreFence();
                io.carplay.app.prototype.d dVar = new io.carplay.app.prototype.d(i);
                dVar.c = this;
                dVar.b = iDequeueOutputBuffer;
                VarHandle.storeStoreFence();
                MediaCodec.BufferInfo bufferInfo = this.U;
                c cVarQ = q(cVar, dVar, bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags);
                int length = cVarQ.g().length;
                b bVar = this.n;
                if (length != 0) {
                    c cVarA = bVar.a(cVarQ.g(), cVarQ.i(), cVarQ.h());
                    if (cVarA != null) {
                        byte[] bArrG = cVarA.g();
                        if ((cVarA.h() & 2) != 0) {
                            this.Q = io.carplay.app.secured.i.a(bArrG);
                            G();
                        } else {
                            boolean z = (cVarA.h() & 1) != 0;
                            d dVarRemove = this.k.remove(Long.valueOf(cVarA.i()));
                            if (dVarRemove == null || dVarRemove.a.k() < this.C) {
                                this.E = -1L;
                            } else {
                                this.l.d(p6.b.ENCODE, dVarRemove.c, System.nanoTime());
                                io.carplay.app.secured.h hVarU = u(this.Q, bArrG, z, dVarRemove.a.a, this.R, this.S);
                                if (hVarU != null) {
                                    this.R = hVarU.k();
                                    this.S = hVarU.h();
                                    if (this.m.a(dVarRemove.a.k(), hVarU.e())) {
                                        this.f.b(hVarU);
                                    }
                                    this.E = -1L;
                                }
                            }
                        }
                    }
                } else if (bVar.c() && (cVarQ.h() & 4) != 0) {
                    d0.c("AVC_ENCODER_FRAGMENT_TRUNCATED");
                    return;
                }
            }
        }
    }

    private void s(long j) throws IOException {
        GLES20.glViewport(0, 0, this.a.h(), this.a.g());
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(16384);
        GLES20.glViewport(this.a.k(), (this.a.g() - this.a.s()) - this.a.j(), this.a.t(), this.a.j());
        GLES20.glUseProgram(this.P);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(36197, this.O);
        int iGlGetAttribLocation = GLES20.glGetAttribLocation(this.P, "aPosition");
        int iGlGetAttribLocation2 = GLES20.glGetAttribLocation(this.P, "aTexture");
        this.W.position(0);
        GLES20.glVertexAttribPointer(iGlGetAttribLocation, 2, 5126, false, 16, (Buffer) this.W);
        GLES20.glEnableVertexAttribArray(iGlGetAttribLocation);
        this.W.position(2);
        GLES20.glVertexAttribPointer(iGlGetAttribLocation2, 2, 5126, false, 16, (Buffer) this.W);
        GLES20.glEnableVertexAttribArray(iGlGetAttribLocation2);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(this.P, "uTransform"), 1, false, this.V, 0);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(this.P, "uTexture"), 0);
        GLES20.glDrawArrays(5, 0, 4);
        if (GLES20.glGetError() != 0) {
            d0.c("fit GL draw failed");
            return;
        }
        EGLExt.eglPresentationTimeANDROID(this.L, this.N, j * 1000);
        if (!EGL14.eglSwapBuffers(this.L, this.N)) {
            d0.c("fit EGL swap failed");
        } else if (this.E < 0) {
            this.E = System.nanoTime();
        }
    }

    public static io.carplay.app.secured.h t(byte[] bArr, byte[] bArr2, boolean z, io.carplay.app.secured.h hVar, byte[] bArr3, byte[] bArr4) throws IOException {
        io.carplay.app.secured.h hVarD = io.carplay.app.secured.h.d(io.carplay.app.secured.i.b(bArr, bArr2, z), bArr3, bArr4, hVar.n(), hVar.c(), hVar.j());
        if (hVarD.k() != null && hVarD.h() != null) {
            return hVarD;
        }
        d0.c("AVC encoder output config missing");
        return null;
    }

    public static io.carplay.app.secured.h u(byte[] bArr, byte[] bArr2, boolean z, io.carplay.app.secured.h hVar, byte[] bArr3, byte[] bArr4) throws IOException {
        io.carplay.app.secured.h hVarD = io.carplay.app.secured.h.d(io.carplay.app.secured.i.b(bArr, bArr2, z || bArr3 == null || bArr4 == null), bArr3, bArr4, hVar.n(), hVar.c(), hVar.j());
        if (hVarD.k() != null && hVarD.h() != null) {
            return hVarD;
        }
        if (!hVarD.e()) {
            return null;
        }
        d0.c("AVC encoder IDR config missing");
        return null;
    }

    private static MediaFormat v(int i, int i2, int i3) {
        MediaFormat mediaFormatCreateVideoFormat = MediaFormat.createVideoFormat("video/avc", i, i2);
        mediaFormatCreateVideoFormat.setInteger("color-format", 2130708361);
        mediaFormatCreateVideoFormat.setInteger("frame-rate", i3);
        mediaFormatCreateVideoFormat.setInteger("i-frame-interval", 1);
        mediaFormatCreateVideoFormat.setInteger("bitrate", (int) Math.max(2000000L, Math.min(20000000L, (((((long) i) * ((long) i2)) * 4) * ((long) i3)) / 30)));
        mediaFormatCreateVideoFormat.setInteger("max-bframes", 0);
        return mediaFormatCreateVideoFormat;
    }

    private static String w(MediaFormat mediaFormat, int i, int i2, int i3) {
        String name = null;
        for (MediaCodecInfo mediaCodecInfo : new MediaCodecList(0).getCodecInfos()) {
            if (mediaCodecInfo.isEncoder() && mediaCodecInfo.isHardwareAccelerated()) {
                for (String str : mediaCodecInfo.getSupportedTypes()) {
                    if (str.equalsIgnoreCase("video/avc")) {
                        try {
                            MediaCodecInfo.CodecCapabilities capabilitiesForType = mediaCodecInfo.getCapabilitiesForType(str);
                            if (capabilitiesForType.isFormatSupported(mediaFormat) && capabilitiesForType.getVideoCapabilities().areSizeAndRateSupported(i, i2, i3)) {
                                name = mediaCodecInfo.getName();
                                break;
                            }
                        } catch (IllegalArgumentException unused) {
                            continue;
                        }
                    }
                }
                if (name != null) {
                    break;
                }
            }
        }
        return name;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void x(List list, long j, long j2, long j3) {
        this.u = this.i.m();
        this.v = this.i.i();
        m(new io.carplay.app.secured.h(list, this.u, this.v, list.stream().anyMatch(new io.carplay.app.prototype.e()), j, j2, j3));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean y(byte[] bArr) {
        return (bArr[0] & 31) == 5;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ ByteBuffer z(int i) {
        return this.G.getOutputBuffer(i);
    }

    public boolean F() {
        if (this.o.get()) {
            return false;
        }
        this.p.set(true);
        return true;
    }

    public synchronized void J(byte[] bArr, byte[] bArr2) {
        this.i.l(bArr, bArr2);
        this.u = (byte[]) bArr.clone();
        this.v = (byte[]) bArr2.clone();
    }

    public byte[] P() {
        if (this.v == null) {
            return null;
        }
        return (byte[]) this.v.clone();
    }

    public byte[] Q() {
        if (this.u == null) {
            return null;
        }
        return (byte[]) this.u.clone();
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        this.o.set(true);
        synchronized (this.q) {
            this.q.notifyAll();
        }
        this.r.interrupt();
    }

    /* JADX WARN: Type inference fix 'apply assigned field type' failed
    java.lang.UnsupportedOperationException: ArgType.getObject(), call class: class jadx.core.dex.instructions.args.ArgType$ArrayArg
    	at jadx.core.dex.instructions.args.ArgType.getObject(ArgType.java:596)
    	at jadx.core.dex.attributes.nodes.ClassTypeVarsAttr.getTypeVarsMapFor(ClassTypeVarsAttr.java:35)
    	at jadx.core.dex.nodes.utils.TypeUtils.replaceClassGenerics(TypeUtils.java:177)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.insertExplicitUseCast(FixTypesVisitor.java:397)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryFieldTypeWithNewCasts(FixTypesVisitor.java:359)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.applyFieldType(FixTypesVisitor.java:309)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
     */
    public synchronized void l(byte[] bArr) {
        if (this.o.get()) {
            return;
        }
        try {
            try {
                VideoCipher.b bVar = this.i;
                long jNanoTime = System.nanoTime();
                s sVar = new s(2);
                sVar.b = this;
                VarHandle.storeStoreFence();
                bVar.c(bArr, 0L, jNanoTime, 0L, sVar);
            } catch (Exception e2) {
                throw new IOException("native AVC fit input invalid", e2);
            }
        } catch (IOException e3) {
            throw e3;
        }
    }

    public synchronized void m(io.carplay.app.secured.h hVar) {
        try {
            if (this.o.get()) {
                return;
            }
            if (hVar == null) {
                throw new IOException("AVC unit missing");
            }
            byte[] bArrK = hVar.k();
            byte[] bArrH = hVar.h();
            if (bArrK != null) {
                this.u = bArrK;
            }
            if (bArrH != null) {
                this.v = bArrH;
            }
            String strA = this.w.a(bArrK, this.a.q(), this.a.p());
            if (strA != null) {
                this.f.a("AUTO_FIT_SOURCE_AVC ".concat(strA));
            }
            long jNanoTime = System.nanoTime();
            h hVarPeek = this.j.peek();
            if (H(this.j.size(), hVarPeek == null ? 0L : jNanoTime - hVarPeek.a.c(), hVar.e())) {
                this.j.clear();
                this.s = true;
                this.C = this.A + 1;
                this.m.b(this.C);
                this.p.set(true);
                long j = this.x + 1;
                this.x = j;
                if (j <= 3 || j % 100 == 0) {
                    this.f.a("AUTO_FIT_BACKPRESSURE_WAIT_IDR count=" + j);
                }
            }
            if (!this.s || hVar.e()) {
                byte[] bArrA = hVar.a();
                if (hVar.e()) {
                    if (this.u != null && this.v != null) {
                        bArrA = o(io.carplay.app.secured.i.c(List.of(this.u, this.v)), bArrA);
                        this.s = false;
                    }
                    return;
                }
                byte[] bArr = bArrA;
                ArrayBlockingQueue<h> arrayBlockingQueue = this.j;
                long j2 = this.A + 1;
                this.A = j2;
                if (!arrayBlockingQueue.offer(new h(hVar, bArr, jNanoTime, j2, 0))) {
                    throw new IOException("AVC adapter queue invariant");
                }
            }
        } catch (Throwable th) {
            throw th;
        }
    }

    public static final class d extends w1 {
        private final h a;
        private final long b;
        private final long c;

        private d(h hVar, long j, long j2) {
            this.a = hVar;
            this.b = j;
            this.c = j2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof d)) {
                return false;
            }
            d dVar = (d) obj;
            return this.b == dVar.b && this.c == dVar.c && Objects.equals(this.a, dVar.a);
        }

        public final int hashCode() {
            long j = this.b;
            long j2 = this.c;
            h hVar = this.a;
            return Objects.hashCode(hVar) + w1.a(j2, Long.hashCode(j) * 31, 31);
        }

        public long j() {
            return this.b;
        }

        public long k() {
            return this.c;
        }

        public h l() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {this.a, Long.valueOf(this.b), Long.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(d.class, sb, "[");
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

        public /* synthetic */ d(h hVar, long j, long j2, int i) {
            this(hVar, j, j2);
        }
    }

    public static final class h extends w1 {
        private final io.carplay.app.secured.h a;
        private final byte[] b;
        private final long c;
        private final long d;

        private h(io.carplay.app.secured.h hVar, byte[] bArr, long j, long j2) {
            this.a = hVar;
            this.b = bArr;
            this.c = j;
            this.d = j2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof h)) {
                return false;
            }
            h hVar = (h) obj;
            return this.c == hVar.c && this.d == hVar.d && Objects.equals(this.a, hVar.a) && Objects.equals(this.b, hVar.b);
        }

        public final int hashCode() {
            long j = this.c;
            long j2 = this.d;
            io.carplay.app.secured.h hVar = this.a;
            byte[] bArr = this.b;
            return Objects.hashCode(bArr) + ((Objects.hashCode(hVar) + w1.a(j2, Long.hashCode(j) * 31, 31)) * 31);
        }

        public byte[] i() {
            return this.b;
        }

        public long j() {
            return this.c;
        }

        public long k() {
            return this.d;
        }

        public io.carplay.app.secured.h l() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, Long.valueOf(this.c), Long.valueOf(this.d)};
            String[] strArrSplit = "a;b;c;d".length() == 0 ? new String[0] : "a;b;c;d".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(h.class, sb, "[");
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

        public /* synthetic */ h(io.carplay.app.secured.h hVar, byte[] bArr, long j, long j2, int i) {
            this(hVar, bArr, j, j2);
        }
    }

    public AvcEncoder(q6 q6Var, int i, f fVar, AvcEncoder avcEncoder) {
        this(q6Var, i, i, fVar, avcEncoder);
    }
}
