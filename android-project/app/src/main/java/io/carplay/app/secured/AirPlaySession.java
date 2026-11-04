/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 AirPlaySession。AirPlay 会话主流程：SETUP / INFO / RECORD / SET_PARAMETER、POST /command 的处理，以及视频、音频、HID、事件各通道的建立、保活与拆除。
 *   This file defines AirPlaySession. Main AirPlay session: handles SETUP / INFO / RECORD / SET_PARAMETER and POST /command, and owns setup, keep-alive and teardown of the video, audio, HID and event channels.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.invoke.VarHandle;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import javax.net.SocketFactory;



/**
 * AirPlay 会话主流程：SETUP / INFO / RECORD / SET_PARAMETER、POST /command 的处理，以及视频、音频、HID、事件各通道的建立、保活与拆除。
 *
 * Main AirPlay session: handles SETUP / INFO / RECORD / SET_PARAMETER and POST /command, and owns setup, keep-alive and teardown of the video, audio, HID and event channels.
 */
public final class AirPlaySession implements AutoCloseable {
    
    private static final int MAX_HID_INPUT_MODE_ATTEMPTS = 8;
    
    private static final long HID_INPUT_MODE_MIN_INTERVAL_MS = 2000L;
    
    private static final long HID_INPUT_MODE_SWEEP_INTERVAL_MS = 6000L;
    
    private static final int MAX_HID_INPUT_MODE_DEVICES = 8;
    private static int HIDMODE = 0;
    private static int SWEEP = 0;
    private static final String l1 = "application/x-apple-binary-plist";
    private long A0;
    private final IoStatsSnapshot B0;
    private long C0;
    private volatile boolean D;
    private long D0;
    private volatile boolean E;
    private long E0;
    private volatile long F0;
    private volatile long G0;
    private volatile Socket H0;
    private volatile long I;
    private final Map<String, p> I0;
    private final Map<String, h> J0;
    private final Map<String, i> K0;
    private boolean L0;
    private final Set<String> M0;
    private final HidCommandSender N0;
    private final AtomicBoolean hidEventWaitClaimed;
    private final AtomicBoolean eventTickClaimed;
    
    private final AtomicBoolean eventTickArmed;
    private AudioCapabilities Q;
    private String Q0;
    private t.a R;
    private boolean R0;
    private HidCapabilities S;
    private int S0;
    private Map<String, Object> T;
    private int T0;
    private final z U;
    private final Object U0;
    private List<k> V;
    private long V0;
    private Map<String, Object> W;
    private long W0;
    private boolean X;
    private long X0;
    private final ScheduledExecutorService Y;
    private volatile int Y0;
    private final ScheduledExecutorService Z;
    private volatile int Z0;
    private final SocketFactory a;
    private volatile EventChannel a0;   
    private int a1;
    
    private long unhandledCommandCount;
    private final j b;
    private volatile EventChannel eventChannel;
    private int b1;
    private volatile InetAddress c;
    private volatile Socket c0;
    
    private q6 touchTransform;
    private volatile int d;
    private volatile Socket d0;
    private boolean d1;
    private volatile String e;
    private volatile DatagramSocket e0;
    private int e1;
    private volatile DatagramSocket f0;
    private DeviceMetadataStore f1;
    private boolean g;
    private volatile KeepAliveServer g0;
    private boolean g1;
    private int h;
    private volatile boolean h0;
    private boolean h1;
    private String i;
    private long i0;
    private final AtomicBoolean i1;
    private volatile Iap2WirelessSetup j;
    private long j0;
    
    private final Set<d> duckedPrompts;
    private final PairingTlv8.a k;
    private long k0;
    
    private boolean duckAudioUnsupported;
    private final PairingTlv8.c l;
    private volatile boolean l0;
    private final String m;
    private byte[] m0;
    private final n n;
    private VideoCipher.AvcNalCipher n0;
    private k o0;
    private PairingTlv8.b p0;
    private final Object q0;
    private volatile Iap2WirelessSetup r0;
    private volatile Socket s0;
    private int t0;
    private boolean u0;
    private volatile g v0;
    private volatile long w0;
    private int x;
    
    private int eventFeedbackSentCount;
    
    private int eventFeedbackFailedCount;
    
    private int eventFeedbackSkippedCount;
    private long x0;
    private final long y0;
    private final long z0;
    public boolean zzChainOn;
    private final Object f = new Object();
    private final AtomicBoolean o = new AtomicBoolean();
    private final AtomicBoolean p = new AtomicBoolean();
    private final AtomicLong q = new AtomicLong();
    private final AtomicLong r = new AtomicLong();
    private final AtomicLong s = new AtomicLong();
    private final SessionStateMachine t = new SessionStateMachine(false);
    private final AtomicBoolean u = new AtomicBoolean();
    private final AtomicBoolean v = new AtomicBoolean();
    private final AtomicLong w = new AtomicLong();
    private final AtomicReference<o> y = new AtomicReference<>(new o(true, 0, 0, 0));
    private final AtomicReference<f> z = new AtomicReference<>(new f(0, 0L, false));
    private final AtomicLong A = new AtomicLong();
    private final AtomicReference<c> B = new AtomicReference<>();
    private final f4 C = new f4();
    private boolean F = true;
    private final Object G = new Object();
    private final Object H = new Object();
    private final Set<String> J = new HashSet();
    private final BlockingQueue<q> K = new ArrayBlockingQueue(12);
    private final Object L = new Object();
    private final Object M = new Object();
    private final VideoCipher.AvcVideoInput N = new VideoCipher.AvcVideoInput();
    private final Map<CarLifeFrame.a, d> O = new ConcurrentHashMap();
    private final Map<CarLifeFrame.a, Object> P = X();

    /**
     * 回调实现：实现 RemoteCommandHandler.b 定义的契约。
     *
     * Callback implementation of the RemoteCommandHandler.b contract.
     */
    public class a implements RemoteCommandHandler.b {
        public a() {
        }

        @Override 
        public boolean b(boolean z) {
            return AirPlaySession.this.n.h(z);
        }

        @Override 
        public boolean c(RemoteCommandHandler.d dVar) {
            return AirPlaySession.this.n.o(dVar);
        }
    }

    /**
     * 回调实现：实现 Iap2WirelessSetup.b 定义的契约。
     *
     * Callback implementation of the Iap2WirelessSetup.b contract.
     */
    public class Iap2PostResponseOwner implements Iap2WirelessSetup.b {
        private volatile boolean a;
        final  Iap2WirelessSetup b;
        final  AirPlaySession c;

        public Iap2PostResponseOwner(AirPlaySession airPlaySession, Iap2WirelessSetup iap2WirelessSetup) {
            this.b = iap2WirelessSetup;
            this.c = airPlaySession;
        }

        @Override 
        public synchronized void a(byte[] bArr) throws IOException {
            this.c.q0();
            EventChannel eventChannel = this.c.a0;
            if (this.a || bArr == null || bArr.length < 1 || bArr.length > 4096 || !this.c.D || eventChannel == null || !eventChannel.n()) {
                throw new IOException("control POST iAP unavailable");
            }
            byte[] bArrA = io.carplay.app.secured.Iap2PostResponseBuilder.a(AirPlaySession.R(this.c, Map.of("type", "iAPSendMessage", "params", Map.of("data", (byte[]) bArr.clone()))));
            if (bArrA != null) {
                try {
                    synchronized (this.c.q0) {
                        if (this.a || this.c.o.get() || !this.c.G0() || this.c.a0 != eventChannel || !eventChannel.n() || this.b == null || this.c.j != this.b || this.c.r0 != null || !this.b.s(bArrA)) {
                            throw new IOException("IAP2_POST_RESPONSE_OWNER");
                        }
                    }
                    this.c.n.a("NETWORK_IAP_POST_RESPONSE_QUEUED bytes=" + bArrA.length + " same_authenticated_owner=true values_logged=false");
                    Arrays.fill(bArrA, (byte) 0);
                } catch (Throwable th) {
                    Arrays.fill(bArrA, (byte) 0);
                    throw th;
                }
            }
        }

        @Override 
        public void close() {
            this.a = true;
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class g extends ProtoMessageBase {
        private final byte[] a;
        private final byte[] b;

        public g(byte[] bArr, byte[] bArr2) {
            byte[] bArr3 = (byte[]) bArr.clone();
            byte[] bArr4 = (byte[]) bArr2.clone();
            this.a = bArr3;
            this.b = bArr4;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof g)) {
                return false;
            }
            g gVar = (g) obj;
            return Objects.equals(this.a, gVar.a) && Objects.equals(this.b, gVar.b);
        }

        public byte[] g() {
            return (byte[]) this.b.clone();
        }

        public byte[] h() {
            return (byte[]) this.a.clone();
        }

        public final int hashCode() {
            byte[] bArr = this.a;
            byte[] bArr2 = this.b;
            return Objects.hashCode(bArr2) + (Objects.hashCode(bArr) * 31);
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(g.class, sb, "[");
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

    /**
     * AirPlaySession 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in AirPlaySession, carrying local implementation detail.
     */
    public static final class h {
        final Object a;
        final int b;
        final HidBackParser c;
        final Set<Integer> d = new HashSet();

        public h(Object obj, int i, HidBackParser x0Var) {
            this.a = obj;
            this.b = i;
            this.c = x0Var;
        }
    }

    /**
     * AirPlaySession 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in AirPlaySession, carrying local implementation detail.
     */
    public static final class i {
        final Object a;
        final int b;
        final HidConsumerParser c;
        final Map<Integer, Set<Integer>> d = new LinkedHashMap();
        long e;
        long f;
        boolean g;

        public i(Object obj, int i, HidConsumerParser y0Var) {
            this.a = obj;
            this.b = i;
            this.c = y0Var;
        }
    }

    @FunctionalInterface
    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface j {
        void a(DatagramSocket datagramSocket) throws IOException;
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class k extends ProtoMessageBase {
        private final int a;
        private final int b;
        private final int c;
        private final String d;

        public k(int i, int i2, int i3, String str) {
            this.a = i;
            this.b = i2;
            this.c = i3;
            this.d = str;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof k)) {
                return false;
            }
            k kVar = (k) obj;
            return this.a == kVar.a && this.b == kVar.b && this.c == kVar.c && Objects.equals(this.d, kVar.d);
        }

        public int g() {
            return this.c;
        }

        public int h() {
            return this.b;
        }

        public final int hashCode() {
            int i = this.a;
            int i2 = this.b;
            int i3 = this.c;
            return Objects.hashCode(this.d) + (((((i * 31) + i2) * 31) + i3) * 31);
        }

        public String i() {
            return this.d;
        }

        public int j() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c), this.d};
            String[] strArrSplit = "a;b;c;d".length() == 0 ? new String[0] : "a;b;c;d".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(k.class, sb, "[");
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

    @FunctionalInterface
    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface l {
        void e(Map<String, Object> map) throws IOException;
    }

    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface n {
        void a(String str);

        default void b(String str) {
        }

        default Boolean e() {
            return null;
        }

        default void f() {
        }

        default z.a g(HidCapabilities xVar) {
            return null;
        }

        default boolean h(boolean z) {
            return false;
        }

        boolean i();

        default void j(String str, boolean z) {
            b(str);
        }

        default void k(HidCapabilities xVar, HidCapabilities.c cVar) {
        }

        default boolean l() {
            return false;
        }

        default boolean m(int i) {
            return false;
        }

        default boolean n() {
            return false;
        }

        default boolean o(RemoteCommandHandler.d dVar) {
            return false;
        }

        default void p(boolean z) {
        }

        default void q(HidCapabilities xVar) {
        }

        void r(int i, int i2, boolean z) throws IOException;
    }

    /**
     * AirPlaySession 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in AirPlaySession, carrying local implementation detail.
     */
    public static final class p {
        final Object a;
        final int b;
        final HidDescriptorParser c;
        HidDescriptorParser.f d;
        boolean e;
        boolean f;

        public p(Object obj, int i, HidDescriptorParser z0Var) {
            this.a = obj;
            this.b = i;
            this.c = z0Var;
        }
    }

    
    public AirPlaySession(SocketFactory socketFactory, j jVar, String str, int i2, PairingTlv8.a aVar, PairingTlv8.c cVar, String str2, n nVar) throws IOException {
        Map<String, Object> map = Collections.EMPTY_MAP;
        this.T = map;
        this.U = new z(32);
        this.V = Collections.EMPTY_LIST;
        this.W = map;
        this.Y = Executors.newScheduledThreadPool(4, new n0(0));
        this.Z = Executors.newScheduledThreadPool(8, new n0(1));
        this.q0 = new Object();
        this.y0 = System.currentTimeMillis();
        this.z0 = System.nanoTime();
        this.A0 = -1L;
        this.B0 = new IoStatsSnapshot();
        this.I0 = new LinkedHashMap();
        this.J0 = new LinkedHashMap();
        this.K0 = new LinkedHashMap();
        this.M0 = new HashSet();
        this.N0 = new HidCommandSender();
        this.hidEventWaitClaimed = new AtomicBoolean();
        this.eventTickClaimed = new AtomicBoolean();
        this.eventTickArmed = new AtomicBoolean();
        this.U0 = new Object();
        this.Y0 = 1280;
        this.Z0 = 720;
        this.a1 = 1280;
        this.b1 = 720;
        this.e1 = 10000;
        this.i1 = new AtomicBoolean();
        this.duckedPrompts = Collections.newSetFromMap(new IdentityHashMap());
        Objects.requireNonNull(socketFactory);
        this.a = socketFactory;
        Objects.requireNonNull(jVar);
        this.b = jVar;
        Objects.requireNonNull(aVar);
        this.k = aVar;
        Objects.requireNonNull(cVar);
        this.l = cVar;
        this.m = str2;
        Objects.requireNonNull(nVar);
        this.n = nVar;
        if (i2 < 1 || i2 > 65535) {
            SyntheticRefs.c("invalid CarPlay control port");
            throw null;
        }
        this.d = i2;
        this.c = InetAddress.getByName(str);
        if (this.c.isAnyLocalAddress() || this.c.isLoopbackAddress() || this.c.isMulticastAddress()) {
            SyntheticRefs.c("invalid accessory endpoint");
            throw null;
        }
        this.Q = io.carplay.app.secured.AudioCapabilities.u(map);
        String hostAddress = this.c.getHostAddress();
        int iIndexOf = hostAddress.indexOf(37);
        hostAddress = iIndexOf >= 0 ? hostAddress.substring(0, iIndexOf) : hostAddress;
        if (hostAddress.indexOf(58) >= 0) {
            hostAddress = "[" + hostAddress + "]";
        }
        this.e = "rtsp://" + hostAddress + ":" + i2 + "/" + Long.toUnsignedString(E1());
    }

    private static String A0(Object obj) {
        String strB = io.carplay.app.secured.ProtoFieldAccessor.b(obj);
        if (strB == null) {
            return null;
        }
        return obj instanceof Number ? "n:".concat(strB) : "s:".concat(strB);
    }

    private static long A1(int i2, int i3) {
        long j2 = 0;
        if (i3 < 1 || i3 > 2) {
            return 0L;
        }
        if (i2 == 8000) {
            j2 = 4;
        } else if (i2 == 16000) {
            j2 = 16;
        } else if (i2 == 24000) {
            j2 = 64;
        } else if (i2 == 32000) {
            j2 = 256;
        } else if (i2 == 44100) {
            j2 = 1024;
        } else if (i2 == 48000) {
            j2 = 16384;
        }
        return i3 == 2 ? j2 * 2 : j2;
    }

    private static boolean D0(Throwable th) {
        int i2 = 0;
        while (th != null && i2 < 16) {
            if ((th instanceof InterruptedException) || (th instanceof ClosedByInterruptException)) {
                return true;
            }
            if ((th instanceof InterruptedIOException) && !(th instanceof SocketTimeoutException)) {
                return true;
            }
            i2++;
            th = th.getCause();
        }
        return false;
    }

    private static int D1(Map<String, Object> map, String str) throws IOException {
        int iM1 = m1(map.get(str), 0);
        if (iM1 >= 1 && iM1 <= 65535) {
            return iM1;
        }
        SyntheticRefs.c(ProtoMessageBase.e("invalid peer ", str));
        return 0;
    }

    private static long E1() {
        long j2 = ByteBuffer.wrap(DerCodec.q(8)).getLong() & Long.MAX_VALUE;
        if (j2 == 0) {
            return 1L;
        }
        return j2;
    }

    
    private void F1(d prompt, boolean duck) throws IOException {
        if (prompt == null || prompt.g != 101 || this.duckAudioUnsupported
                || this.duckedPrompts.contains(prompt) == duck) {
            return;
        }
        String type = duck ? "duckAudio" : "unduckAudio";
        try {
            Map<String, Object> params = Map.of(
                    "durationMs", Double.valueOf(150.0d),
                    "volume", Double.valueOf(duck ? -20.0d : 0.0d));
            Map<String, Object> response = g1(R1("POST", "/command",
                    Map.of("type", type, "params", params)));
            if (response.containsKey("status")) {
                Object status = response.get("status");
                boolean rejected;
                if (status instanceof Byte || status instanceof Short
                        || status instanceof Integer || status instanceof Long) {
                    rejected = ((Number) status).longValue() != 0L;
                } else {
                    rejected = true;
                }
                if (rejected) {
                    this.n.a("AUDIO_PROMPT_CONTEXT accepted=false active=" + duck
                            + " audio_retained=true");
                    return;
                }
            }
            if (duck) {
                this.duckedPrompts.add(prompt);
            } else {
                this.duckedPrompts.remove(prompt);
            }
            this.n.a("AUDIO_PROMPT_CONTEXT accepted=true active=" + duck
                    + " stream=101 independent_volume_unconfirmed=true");
        } catch (EventChannel.d rejected) {
            
            this.duckAudioUnsupported = true;
            this.duckedPrompts.clear();
            this.n.a("AUDIO_PROMPT_CONTEXT supported=false status=" + rejected.a
                    + " audio_retained=true");
        } catch (IOException e2) {
            if (Iap2Bootstrap.l(e2)) {
                v0("AUDIO_PROMPT_CONTEXT_IO", e2);
            }
            throw e2;
        }
    }

    private void G1() {
        if (this.o.get() || !this.D) {
            return;
        }
        int i2 = 0;
        if (this.u.compareAndSet(false, true)) {
            try {
                ScheduledExecutorService scheduledExecutorService = this.Y;
                b0 b0Var = new b0(i2);
                b0Var.b = this;
                VarHandle.storeStoreFence();
                scheduledExecutorService.schedule(b0Var, 50L, TimeUnit.MILLISECONDS);
            } catch (RejectedExecutionException unused) {
                this.u.set(false);
            }
        }
    }

    private void H1() throws EventChannel.d, IOException {
        if (!this.E || this.o.get()) {
            return;
        }
        if (this.t.K()) {
            l2();
        } else {
            c2();
        }
    }

    private EventChannel HidChannel(EventChannel eventChannel) {
        this.n.a("HID_CHAN=CONTROL_FACTORY");
        j("HID_CHAN=CONTROL_FACTORY");
        return eventChannel;
    }

    private void HidFail() {
        if (ControlChannel.LASTEVENT) {
            int i2 = ControlChannel.EVFAIL + 1;
            ControlChannel.EVFAIL = i2;
            if (i2 < 2) {
                ControlChannel.EVENT_OK = false;
                j("CarPlayHID HID_EVENT_REJECTED -> fallback CONTROL");
            }
        }
    }

    
    private void HidOK() {
        ControlChannel.ACCEPTED = true;
        ControlChannel.CTRLOK = true;
        ControlChannel.EVFAIL = 0;
        ControlChannel.HID_MODE_REPORTED = true;
        if (ControlChannel.LOGGED) {
            return;
        }
        ControlChannel.LOGGED = true;
        j("CarPlayHID HID_ACCEPTED -> retry stopped");
    }

    private void HidRetryTick() {
        
        
        if (this.eventTickArmed.compareAndSet(false, true)) {
            b0 b0Var = new b0(4);
            b0Var.b = this;
            this.Z.schedule(b0Var, 1L, TimeUnit.SECONDS);
        }
        long jUptimeMillis = SystemClock.uptimeMillis();
        
        
        
        if (!ControlChannel.HID_MODE_REPORTED
                && jUptimeMillis - ControlChannel.LASTAT >= HID_INPUT_MODE_SWEEP_INTERVAL_MS) {
            int i2 = ControlChannel.TRIES;
            if (i2 < 18) {
                i2 = 0;
                ControlChannel.TRIES = 0;
            }
            ControlChannel.TRIES = i2 + 1;
            int i3 = ControlChannel.MODE + 1;
            if (i3 > 5) {
                i3 = 0;
            }
            ControlChannel.MODE = i3;
            ControlChannel.LASTAT = jUptimeMillis;
            j("HID_SWEEP mode=" + ControlChannel.MODE);
            Set<String> set = this.J;
            synchronized (set) {
                set.clear();
            }
            this.i1.set(false);
            try {
                openHidGate();
            } catch (Throwable th) {
                j("HID_RETRY W() threw");
            }
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public  void I0(List<Map<String, Object>> commands, EventChannel control, long generation) {
        int slot = 0;
        try {
            for (; slot < commands.size(); slot++) {
                if (!d0(control)) {
                    return;
                }
                synchronized (this.H) {
                    if (this.I != generation) {
                        return;
                    }
                }
                Map<String, Object> command = commands.get(slot);
                Object paramsValue = command.get("params");
                if (!(paramsValue instanceof Map)) {
                    continue;
                }
                Map<?, ?> params = (Map<?, ?>) paramsValue;
                Object uuid = params.get("uuid");
                String deviceKey = (uuid instanceof byte[] ? "b:" : uuid instanceof Number ? "n:" : "s:")
                        + uuid;
                if (this.J.contains(deviceKey)) {
                    continue;
                }
                if (this.J.size() >= MAX_HID_INPUT_MODE_DEVICES) {
                    this.n.a("HID_INPUT_MODE_UNAVAILABLE reason=SESSION_DEVICE_LIMIT");
                    return;
                }
                this.J.add(deviceKey);
                byte[] body = BinaryPlist.f(command);
                j("CarPlayHID HID_CMD_REQ len=" + body.length + " body=" + new String(body));
                j("CarPlayHID HID_SEND len=" + body.length);
                this.n.a("CarPlayHID HID_SEND len=" + body.length);
                EventChannel carrier = HidChannel(control);
                CompletableFuture<EventChannel.b> pending = carrier == control
                        ? control.g("POST", "/command", l1, body)
                        : control.l("POST", "/command", l1, body);
                String sentLog = "HID_INPUT_MODE_SENT slot=" + slot
                        + (carrier == control ? " carrier=CONTROL" : " carrier=EVENT");
                this.n.a(sentLog);
                j(sentLog);
                j("HID_SEND_STATE done=" + pending.isDone() + " exc=" + pending.isCompletedExceptionally());
                pending.orTimeout(15L, TimeUnit.SECONDS);
                g0 callback = new g0();
                callback.a = this;
                callback.b = generation;
                callback.c = control;
                callback.d = slot;
                VarHandle.storeStoreFence();
                pending.whenComplete(callback);
            }
        } catch (IOException ioFailure) {
            if (!this.o.get() && this.a0 == control) {
                this.n.a("HID_INPUT_MODE_UNCONFIRMED slot=" + slot
                        + " carrier=CONTROL reason=RESPONSE_OR_IO");
                O1(this.eventChannel, "HID_WAIT");
            }
        } finally {
            this.i1.set(false);
        }
    }

    private boolean I1(String str) {
        this.n.a("AUDIO_USER_REQUEST_REJECTED code=" + str + " session_retained=" + G0());
        return false;
    }

    
    public  void J0(long j2, EventChannel eventChannel, int i2, EventChannel.b bVar, Throwable th) {
        j("CarPlayHID HID_CB: ACCEPTED branch reached");
        if (j2 == this.I && d0(eventChannel)) {
            j("CarPlayHID HID_CB: UNCONFIRMED branch reached");
            if (th != null) {
                HidFail();
                this.n.a("HID_INPUT_MODE_UNCONFIRMED slot=" + i2 + " carrier=CONTROL reason=TIMEOUT_OR_IO");
                O1(this.eventChannel, "HID_WAIT");
                return;
            }
            try {
                bVar.g();
                byte[] bArrC = bVar.c();
                j("CarPlayHID HID_CMD_RESP len=" + bArrC.length + " body=" + new String(bArrC));
                if (bArrC.length > 0) {
                    Map<String, Object> mapG1 = g1(BinaryPlist.e(bArrC));
                    if (mapG1.containsKey("status") && !w2(mapG1.get("status"))) {
                        throw new IOException("HID_INPUT_MODE_APPLICATION_REJECTED");
                    }
                }
                HidOK();
                this.n.a("HID_INPUT_MODE_ACCEPTED slot=" + i2 + " status=" + bVar.h() + " carrier=CONTROL report_received=false");
            } catch (IOException unused) {
                HidFail();
                this.n.a("HID_INPUT_MODE_UNCONFIRMED slot=" + i2 + " status=" + bVar.h() + " carrier=CONTROL reason=RESPONSE");
            }
        }
    }

    private byte[] J1(String str) {
        long j2 = this.V0 + 1;
        this.V0 = j2;
        if (j2 > 4 && j2 % 100 != 0) {
            return null;
        }
        this.n.a("COMMAND_REJECTED=" + str + " count=" + j2 + "; session retained");
        return null;
    }

    
    public  void K0(Map map) throws IOException {
        m2(map);
        this.n.a("CARPLAY_EVENT_CONNECTING");
        int iD1 = D1(map, "eventPort");
        r0 r0Var = new r0();
        r0Var.a = this;
        r0Var.b = iD1;
        VarHandle.storeStoreFence();
        c0 c0Var = new c0(0);
        SyntheticRefs syntheticRefs = new SyntheticRefs();
        AtomicBoolean atomicBoolean = this.o;
        Objects.requireNonNull(atomicBoolean);
        e0 e0Var = new e0();
        e0Var.a = atomicBoolean;
        VarHandle.storeStoreFence();
        f0 f0Var = new f0();
        f0Var.a = this;
        VarHandle.storeStoreFence();
        this.eventChannel = new EventChannel(EventChannelConnector.b(r0Var, c0Var, syntheticRefs, e0Var, f0Var), 15000);
        try {
            this.eventChannel.i(DerCodec.k(this.m0, "Events-Salt", "Events-Write-Encryption-Key"), DerCodec.k(this.m0, "Events-Salt", "Events-Read-Encryption-Key"));
        } catch (GeneralSecurityException e2) {
            throw new IOException("EVENT_CHANNEL_KEY_UNAVAILABLE", e2);
        }
        ScheduledExecutorService scheduledExecutorService = this.Y;
        b0 b0Var = new b0(1);
        b0Var.b = this;
        VarHandle.storeStoreFence();
        scheduledExecutorService.execute(b0Var);
        ScheduledExecutorService scheduledExecutorService2 = this.Y;
        b0 b0Var2 = new b0(2);
        b0Var2.b = this;
        VarHandle.storeStoreFence();
        scheduledExecutorService2.execute(b0Var2);
    }

    private byte[] K1(String str) throws IOException {
        J1(str);
        return BinaryPlist.f(Map.of("status", 1));
    }

    private void L1() {
        if (this.R0) {
            try {
                this.n.r(this.S0, this.T0, false);
            } catch (IOException unused) {
                J1("TOUCH_RELEASE_SOURCE_UNAVAILABLE");
            }
            this.R0 = false;
        }
        this.Q0 = null;
        for (p pVar : this.I0.values()) {
            HidDescriptorParser.f fVar = pVar.d;
            boolean z = fVar != null && fVar.q();
            pVar.e = z;
            if (!z) {
                pVar.d = null;
            }
        }
    }

    
    public  void M0(int i2) {
        this.n.a("CARPLAY_EVENT_TCP_RETRY attempt=" + i2 + " before_event_protocol=true");
    }

    
    public void M1() {
        synchronized (this) {
            try {
                if (G0()) {
                    ArrayList arrayList = new ArrayList();
                    for (Map.Entry<CarLifeFrame.a, d> entry : this.O.entrySet()) {
                        arrayList.add(Map.entry(entry.getKey(), entry.getValue()));
                    }
                    int size = arrayList.size();
                    int i2 = 0;
                    while (i2 < size) {
                        Object obj = arrayList.get(i2);
                        i2++;
                        Map.Entry entry2 = (Map.Entry) obj;
                        d dVar = (d) entry2.getValue();
                        synchronized (dVar) {
                            try {
                                if (!dVar.i.isClosed()) {
                                    long jNanoTime = System.nanoTime();
                                    long jMax = Math.max(1L, jNanoTime - dVar.I);
                                    int i3 = size;
                                    ArrayList arrayList2 = arrayList;
                                    String str = "AUDIO_FLOW channel=" + String.valueOf(entry2.getKey()) + " source_rate=" + dVar.d + " output_rate=" + dVar.a + " window_ms=" + (jMax / 1000000) + " converted_frames=" + (dVar.y - dVar.J) + " sent_frames=" + (dVar.z - dVar.K) + " sent_packets_total=" + dVar.A + " queued_frames=" + dVar.m.l() + " dropped_frames_total=" + dVar.u + " backpressure_events=" + dVar.v + " source_gap_max_ms=" + (dVar.C / 1000000) + " scheduler_late_max_us=" + (dVar.D / 1000) + " udp_write_max_us=" + (dVar.E / 1000) + " source_pcm_observed=" + dVar.x + " encode_max_us=" + (dVar.F / 1000) + " encoder_resets_total=" + dVar.H + " encoder_reset_max_us=" + (dVar.G / 1000) + " codec=" + (dVar.o == null ? "PCM16" : "AAC_LC");
                                    dVar.I = jNanoTime;
                                    dVar.J = dVar.y;
                                    dVar.K = dVar.z;
                                    dVar.C = 0L;
                                    dVar.D = 0L;
                                    dVar.E = 0L;
                                    dVar.F = 0L;
                                    dVar.G = 0L;
                                    if (!this.o.get()) {
                                        this.n.a(str);
                                    }
                                    size = i3;
                                    i2 = i2;
                                    arrayList = arrayList2;
                                }
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                    }
                }
            } catch (Throwable th2) {
                throw th2;
            }
        }
    }

    
    public  void N0() {
        try {
            EventChannel eventChannel = this.eventChannel;
            n nVar = this.n;
            ControlChannel.BOOTED = false;
            ControlChannel.EVENT_READY = true;
            nVar.a("V153_EVENT_READY=true");
            try {
                openHidGate();
            } catch (Throwable th) {
                this.n.a("V151_HID_CALL_FAILED type=" + th.getClass().getSimpleName());
            }
            EventChannel eventChannel2 = this.eventChannel;
            j0 j0Var = new j0(6);
            j0Var.b = this;
            VarHandle.storeStoreFence();
            eventChannel2.z(j0Var);
        } catch (Exception e2) {
            v0("EVENT_CHANNEL_".concat(e2.getClass().getSimpleName()), e2);
        }
    }

    private void N1(CarLifeFrame.a aVar, d dVar, String str, String str2, Exception exc) {
        boolean z;
        long j2;
        long j3;
        synchronized (this) {
            z = this.O.get(aVar) == dVar;
        }
        String simpleName = exc.getCause() == null ? "NONE" : exc.getCause().getClass().getSimpleName();
        synchronized (dVar) {
            j2 = dVar.A;
            j3 = dVar.y;
        }
        this.n.a("AUDIO_SENDER_FAILURE channel=" + String.valueOf(aVar) + " codec=" + str + " phase=" + str2 + " error=" + exc.getClass().getSimpleName() + " cause=" + simpleName + " sent_packets=" + j2 + " produced_frames=" + j3 + " remote_stream_retained=" + z + " values_logged=false");
        StringBuilder sb = new StringBuilder("AUDIO_");
        sb.append(str);
        sb.append("_");
        sb.append(str2);
        for (String str3 : t2.a(sb.toString(), exc)) {
            this.n.a(str3.startsWith("  at ") ? "AUDIO_OUTPUT_TRACE".concat(str3) : str3.replace("CONNECT_FAILURE", "AUDIO_OUTPUT_FAILURE").replace("CONTROL_RESPONSE_REJECTED", "AUDIO_RESPONSE_REJECTED"));
        }
    }

    
    public  byte[] O0(EventChannel.b bVar) throws IOException {
        ControlChannel.Log("INBOUND_REQ chan=EVENT uri=" + bVar.a + " keys=" + bVar.b.keySet() + " bodyLen=" + bVar.c().length);
        return s1(bVar, "EVENT");
    }

    private void O1(EventChannel eventChannel, String str) {
        AtomicBoolean atomicBoolean = str.equals("HID_WAIT") ? this.hidEventWaitClaimed : this.eventTickClaimed;
        if (eventChannel == null || !atomicBoolean.compareAndSet(false, true)) {
            return;
        }
        n nVar = this.n;
        String str2 = "CARPLAY_EVENT_INPUT point=" + str + " " + eventChannel.m();
        nVar.a(str2);
        j(str2);
    }

    
    
    public  void eventKeepAliveTick() {
        
        this.eventTickArmed.set(false);
        logEventTickRate();
        j("EVENT_KEEPALIVE_TICK");
        try {
            HidRetryTick();
            if (this.o.get()) {
                return;
            }
            long now = SystemClock.uptimeMillis();
            if (now - ControlChannel.LAST_FEEDBACK_AT < 800) {
                return;
            }
            ControlChannel.LAST_FEEDBACK_AT = now;
            
            
            
            
            
            EventChannel channel = this.eventChannel;
            if (channel == null) {
                return;
            }
            try {
                if (channel.sendFeedback()) {
                    int sent = this.eventFeedbackSentCount + 1;
                    this.eventFeedbackSentCount = sent;
                    if (sent <= 3 || sent % 100 == 0) {
                        this.n.a("CARPLAY_EVENT_FEEDBACK_SENT count=" + sent + " channel=EVENT body_bytes=0");
                    }
                } else {
                    
                    
                    int skipped = this.eventFeedbackSkippedCount + 1;
                    this.eventFeedbackSkippedCount = skipped;
                    if (skipped <= 3 || skipped % 100 == 0) {
                        this.n.a("CARPLAY_EVENT_FEEDBACK_SKIPPED count=" + skipped + " reason=CHANNEL_NOT_READY");
                    }
                }
            } catch (Exception sendFailure) {
                int failed = this.eventFeedbackFailedCount + 1;
                this.eventFeedbackFailedCount = failed;
                if (failed <= 3 || failed % 100 == 0) {
                    this.n.a("CARPLAY_EVENT_FEEDBACK_UNCONFIRMED count=" + failed
                            + " reason=" + sendFailure.getClass().getSimpleName());
                }
            }
        } catch (Throwable th) {
        }
    }

    private void P1(String str) {
        synchronized (this.t) {
            try {
                int i2 = this.x;
                if (i2 < 32) {
                    this.x = i2 + 1;
                    this.n.a("CARPLAY_RESOURCE_STATE source=" + str + " revision=" + this.w.get() + " " + this.t.P() + " display_confirmed=false");
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    
    public static  boolean Q0(byte[] bArr) {
        return bArr.length > 0 && (bArr[0] & 31) == 5;
    }

    private void Q1(long j2, long j3) {
        long j4 = this.C0;
        if (j4 == 0) {
            this.C0 = j2;
            this.D0 = j3;
            this.E0 = this.F0;
            return;
        }
        long j5 = j2 - j4;
        if (j5 < 5000000000L) {
            return;
        }
        StringBuilder sb = new StringBuilder("VIDEO_FLOW window=256 transport_received_fps_x100=");
        sb.append(((this.F0 - this.E0) * 100000000000L) / j5);
        sb.append(" sent_fps_x100=");
        sb.append(((j3 - this.D0) * 100000000000L) / j5);
        sb.append(" queue_depth=");
        sb.append(this.K.size());
        q qVarPeek = this.K.peek();
        sb.append(" oldest_us=");
        sb.append(qVarPeek != null ? Math.max(0L, j2 - qVarPeek.g()) / 1000 : 0L);
        for (Map.Entry<IoStatsSnapshot.b, IoStatsSnapshot.c> entry : this.B0.e().entrySet()) {
            sb.append(' ');
            sb.append(entry.getKey());
            sb.append("_p50_us=");
            sb.append(entry.getValue().i() / 1000);
            sb.append("_p95_us=");
            sb.append(entry.getValue().j() / 1000);
        }
        sb.append(" receiver_render_confirmed=false");
        this.n.a(sb.toString());
        this.C0 = j2;
        this.D0 = j3;
        this.E0 = this.F0;
    }

    public static   Object R(AirPlaySession airPlaySession, Map map) throws IOException {
        return airPlaySession.R1("POST", "/command", map);
    }

    
    public static  void R0(Map map) {
    }

    private Object R1(String str, String str2, Map<String, Object> map) throws IOException {
        q0();
        EventChannel eventChannel = this.a0;
        if (eventChannel == null) {
            SyntheticRefs.c("control not ready");
            return null;
        }
        f4.a aVarD = (str.equals("POST") && str2.equals("/feedback") && eventChannel.n()) ? this.C.d(System.nanoTime()) : null;
        EventChannel.b bVarV = eventChannel.v(str, str2, map != null ? l1 : null, map == null ? new byte[0] : BinaryPlist.f(map));
        bVarV.g();
        if (!this.o.get() && this.a0 == eventChannel && eventChannel.n() && this.C.c(aVarD)) {
            this.n.a("RECOVERY_MEDIA_AUDIO_RELEASED fresh_idr=true later_peer_feedback=true");
        }
        return bVarV.c().length == 0 ? Collections.EMPTY_MAP : BinaryPlist.e(bVarV.c());
    }

    
    public  byte[] S0(EventChannel.b bVar) throws IOException {
        ControlChannel.Log("INBOUND_REQ chan=CONTROL uri=" + bVar.a + " keys=" + bVar.b.keySet() + " bodyLen=" + bVar.c().length);
        return s1(bVar, "CONTROL");
    }

    private byte[] T(Map<String, Object> map) {
        String str;
        EventChannel eventChannel = this.a0;
        if (!this.D || this.o.get() || eventChannel == null || !eventChannel.n() || (str = this.m) == null || !str.matches("[a-fA-F0-9]{64}")) {
            return J1("REMOTE_TYPE_UNSUPPORTED");
        }
        try {
            Map<String, Object> mapG1 = g1(map.get("params"));
            if (mapG1.keySet().equals(Set.of("data"))) {
                Object obj = mapG1.get("data");
                if (obj instanceof byte[]) {
                    byte[] bArr = (byte[]) obj;
                    if (bArr.length >= 1 && bArr.length <= 16384) {
                        synchronized (this.q0) {
                            try {
                                if (!this.o.get() && this.r0 == null) {
                                    int i2 = 0;
                                    if (this.j == null) {
                                        String str2 = this.m;
                                        j0 j0Var = new j0(i2);
                                        j0Var.b = this;
                                        VarHandle.storeStoreFence();
                                        n nVar = this.n;
                                        Objects.requireNonNull(nVar);
                                        k0 k0Var = new k0();
                                        k0Var.b = nVar;
                                        VarHandle.storeStoreFence();
                                        DeviceMetadataStore i5Var = this.f1;
                                        n nVar2 = this.n;
                                        Objects.requireNonNull(nVar2);
                                        l0 l0Var = new l0(i2);
                                        l0Var.b = nVar2;
                                        VarHandle.storeStoreFence();
                                        m0 m0Var = new m0();
                                        m0Var.a = this;
                                        m0Var.b = eventChannel;
                                        VarHandle.storeStoreFence();
                                        this.j = Iap2WirelessSetup.n(str2, j0Var, k0Var, i5Var, l0Var, m0Var);
                                        this.n.a("NETWORK_IAP_ROUTE_SELECTED transport=CONTROL_POST trigger=AUTHENTICATED_PEER fresh_link_proof_required=true");
                                    }
                                    return !this.j.s(bArr) ? J1("IAP_NOT_ACCEPTED") : new byte[0];
                                }
                                return J1("IAP_ROUTE_ALREADY_SELECTED");
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                    }
                }
            }
            return J1("IAP_INVALID_DATA");
        } catch (IOException unused) {
            return J1("IAP_INVALID_PARAMS");
        }
    }

    
    public  void T0(String str, int i2, int i3, boolean z) {
        if (i2 != 200) {
            this.n.a("CARPLAY_RESPONSE op=" + str + " status=" + i2 + " cseq=" + i3 + " encrypted=" + z);
            return;
        }
        int i4 = ControlChannel.RESPCNT + 1;
        ControlChannel.RESPCNT = i4;
        if (i4 < 2) {
            return;
        }
        ControlChannel.RESPCNT = 0;
        String str2 = "CARPLAY_RESP op=" + str + " lastCseq=" + i3;
        j(str2);
        this.n.a(str2);
    }

    private static void T1(d dVar) throws IOException {
        long jNanoTime = System.nanoTime();
        dVar.H++;
        try {
            dVar.o.reset();
        } finally {
            dVar.G = Math.max(dVar.G, System.nanoTime() - jNanoTime);
        }
    }

    
    public static  Thread U0(Runnable runnable) {
        Thread thread = new Thread(runnable, "CarPlus-CarPlay");
        thread.setDaemon(true);
        return thread;
    }

    private static void U1(d dVar) throws IOException {
        PcmQueue pcmQueue = dVar.m;
        try {
            dVar.s = Math.addExact(dVar.s, Math.addExact(pcmQueue.o(pcmQueue.e() + 1), dVar.q));
            if (dVar.o != null) {
                T1(dVar);
            }
            dVar.q = 0L;
            dVar.l.reset();
            dVar.n.c();
            dVar.B = 0L;
        } catch (ArithmeticException e2) {
            throw new IOException("AUDIO_RETIREMENT_FRAME_LIMIT", e2);
        }
    }

    
    public static  Thread V0(Runnable runnable) {
        Thread thread = new Thread(runnable, "CarPlus-heartbeat");
        thread.setDaemon(true);
        return thread;
    }

    private void openHidGate() {
        List<Map<String, Object>> commands;
        long generation;
        if (this.eventChannel == null || !ControlChannel.EVENT_READY) {
            this.n.a("V150_HID_GATE=SKIP_NO_EVENT");
            return;
        }
        if (!this.zzChainOn) {
            this.zzChainOn = true;
            ControlChannel.LASTOBJ = this;
            this.n.a("HID_CHAIN_START obj=" + hashCode());
            ControlChannel.TRIES = 0;
            ControlChannel.MODE = 0;
            ControlChannel.HID_MODE_REPORTED = false;
            ControlChannel.SENTCNT = 0;
            ControlChannel.LASTMODE = 0;
            ControlChannel.GATECLOSED = false;
            b0 b0Var = new b0(4);
            b0Var.b = this;
            this.Z.schedule(b0Var, 1L, TimeUnit.SECONDS);
        }
        if (ControlChannel.GATECLOSED) {
            return;
        }
        
        
        
        
        
        
        int sentCount = ControlChannel.SENTCNT;
        if (sentCount >= MAX_HID_INPUT_MODE_ATTEMPTS) {
            ControlChannel.GATECLOSED = true;
            j("HID_GATE_CLOSED reason=CNT");
            this.n.a("HID_GATE_CLOSED reason=CNT");
            return;
        }
        boolean modeChanged = sentCount == 0 || ControlChannel.MODE != ControlChannel.LASTMODE;
        
        
        if (modeChanged && SystemClock.uptimeMillis() - ControlChannel.LASTSEND >= HID_INPUT_MODE_MIN_INTERVAL_MS) {
            ControlChannel.SENTCNT++;
            ControlChannel.LASTMODE = ControlChannel.MODE;
            ControlChannel.LASTSEND = SystemClock.uptimeMillis();
            String str = "HID_GATE_OPEN cnt=" + ControlChannel.SENTCNT + " mode=" + ControlChannel.MODE
                    + " devices=" + (this.I0.size() + this.J0.size() + this.K0.size());
            j(str);
            this.n.a(str);
            ControlChannel.PENDING = true;
            ControlChannel.LASTAT = SystemClock.uptimeMillis();
            q();
            EventChannel control = this.a0;
            k("W: d0 gatePassed=", true);
            synchronized (this.H) {
                try {
                    commands = z0();
                } catch (IOException descriptorFailure) {
                    this.n.a("HID_INPUT_MODE_UNAVAILABLE reason=AUDIO_DESCRIPTOR");
                    j("HID_INPUT_MODE_UNAVAILABLE reason=AUDIO_DESCRIPTOR");
                    return;
                }
                generation = this.I;
            }
            if (!commands.isEmpty() && this.i1.compareAndSet(false, true)) {
                try {
                    ScheduledExecutorService executor = this.Y;
                    h0 task = new h0(0);
                    task.c = this;
                    task.d = commands;
                    task.e = control;
                    task.b = generation;
                    VarHandle.storeStoreFence();
                    j("CarPlayHID HID_GATE: >>> EXECUTE send hidSetInputMode count=" + commands.size());
                    this.n.a("CarPlayHID HID_GATE: >>> EXECUTE send hidSetInputMode count=" + commands.size());
                    executor.execute(task);
                    return;
                } catch (RejectedExecutionException unused) {
                    this.i1.set(false);
                }
            }
            j("CarPlayHID HID_GATE: <<< SKIP send (no commands / CAS fail / rejected)");
            this.n.a("CarPlayHID HID_GATE: <<< SKIP send (no commands / CAS fail / rejected)");
        }
    }

    
    public static  boolean W0(Object obj) {
        return (obj instanceof Map) && m1(((Map) obj).get("resourceID"), 0) == 2;
    }

    private static Map<CarLifeFrame.a, Object> X() {
        EnumMap enumMap = new EnumMap(CarLifeFrame.a.class);
        for (CarLifeFrame.a aVar : CarLifeFrame.a.values()) {
            enumMap.put(aVar, new Object());
        }
        return enumMap;
    }

    
    public static  IOException X0() {
        return new IOException("optional command absent");
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private void Y(CarLifeFrame.a aVar, d dVar) {
        String stage = "WAIT_MODES";
        while (G0() && !dVar.i.isClosed()) {
            try {
                if (this.B.get() != null || this.C.b()) {
                    LockSupport.parkNanos(10000000L);
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    continue;
                }
                stage = "QUEUE";
                PcmQueue.a pending = dVar.m.p();
                if (pending == null) {
                    return;
                }
                long packetTime;
                List<byte[]> units;
                synchronized (dVar.r) {
                    synchronized (dVar) {
                        if (!G0() || dVar.i.isClosed()) {
                            return;
                        }
                        f fVar = this.z.get();
                        o2(dVar, fVar);
                        if (!dVar.m.g(pending) || !fVar.g()
                                || this.B.get() != null || this.C.b()) {
                            dVar.s = Math.addExact(dVar.s,
                                    Math.addExact(pending.e(), (long) pending.c()));
                            continue;
                        }
                        if (pending.e() != 0) {
                            T1(dVar);
                            dVar.s = Math.addExact(dVar.s, Math.addExact(pending.e(), dVar.q));
                            dVar.q = 0L;
                            dVar.n.c();
                        }
                        long backlog = Math.addExact(dVar.q, (long) pending.c());
                        dVar.q = backlog;
                        if (backlog > 8192L) {
                            throw new IOException("AUDIO_AAC_ENCODER_BACKLOG");
                        }
                        packetTime = dVar.m.e();
                    }
                    stage = "LOCAL_ENCODE";
                    long encodeStart = System.nanoTime();
                    try {
                        units = dVar.o.j(pending.d());
                    } catch (Throwable th) {
                        synchronized (dVar) {
                            dVar.F = Math.max(dVar.F, System.nanoTime() - encodeStart);
                        }
                        throw th;
                    }
                    synchronized (dVar) {
                        dVar.F = Math.max(dVar.F, System.nanoTime() - encodeStart);
                        if (!h0(dVar, packetTime)) {
                            continue;
                        }
                        if (units == null || units.size() > 8
                                || ((long) units.size()) * 1024L > dVar.q) {
                            throw new IOException("AUDIO_AAC_ENCODER_OUTPUT_LIMIT");
                        }
                        for (byte[] unit : units) {
                            if (unit == null || unit.length < 1 || unit.length > 16384) {
                                throw new IOException("AUDIO_AAC_ACCESS_UNIT_LIMIT");
                            }
                        }
                    }
                }
                
                for (byte[] unit : units) {
                    long deadline;
                    synchronized (dVar) {
                        if (!h0(dVar, packetTime)) {
                            break;
                        }
                        stage = "PACE";
                        deadline = dVar.n.b(System.nanoTime(), 1024, 0L);
                    }
                    while (true) {
                        long remaining = AudioPacer.a(System.nanoTime(), deadline);
                        if (remaining <= 0 || this.o.get() || dVar.i.isClosed()) {
                            break;
                        }
                        LockSupport.parkNanos(Math.min(remaining, 10000000L));
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                    }
                    synchronized (dVar.r) {
                        byte[] sealed;
                        synchronized (dVar) {
                            if (!h0(dVar, packetTime)) {
                                sealed = null;
                            } else {
                                dVar.D = Math.max(dVar.D,
                                        Math.max(0L, System.nanoTime() - deadline));
                                stage = "CIPHER_SKIP";
                                if (dVar.s != 0L) {
                                    dVar.k.e(dVar.s);
                                    dVar.s = 0L;
                                }
                                stage = "CIPHER_SEAL";
                                sealed = dVar.k.b(unit);
                                dVar.q -= 1024L;
                            }
                        }
                        if (sealed == null) {
                            break;
                        }
                        stage = "UDP_SEND";
                        long sendStart = System.nanoTime();
                        dVar.i.send(new DatagramPacket(sealed, sealed.length));
                        synchronized (dVar) {
                            dVar.E = Math.max(dVar.E, System.nanoTime() - sendStart);
                            dVar.z += 1024L;
                            dVar.A += 1L;
                        }
                        this.s.addAndGet(((long) dVar.b) * 2048L);
                    }
                }
            } catch (Exception e) {
                if (this.o.get() || dVar.i.isClosed()) {
                    return;
                }
                if (Iap2Bootstrap.l(e)) {
                    v0("AAC_SEND_".concat(e.getClass().getSimpleName()), e);
                    return;
                }
                dVar.close();
                N1(aVar, dVar, "AAC_LC", stage, e);
                this.n.a("AUDIO_SENDER_UNAVAILABLE channel=" + String.valueOf(aVar)
                        + " codec=AAC_LC video_retained=" + G0());
                return;
            }
        }
    }

    
    public  void Y0(List list) throws IOException {
        q qVar = new q(list, this.N.m(), this.N.i(), System.nanoTime(), this.w0, 0);
        if (qVar.l() != null && qVar.j() != null) {
            this.v0 = new g(qVar.l(), qVar.j());
        }
        try {
            p0(qVar);
        } catch (InterruptedException e2) {
            Thread.currentThread().interrupt();
            throw new IOException("VIDEO_QUEUE_INTERRUPTED", e2);
        }
    }

    
    
    public void Z0(d dVar) {
        byte[] bArr = new byte[2048];
        while (!this.o.get() && !dVar.j.isClosed()) {
            try {
                DatagramPacket datagramPacket = new DatagramPacket(bArr, 2048);
                dVar.j.receive(datagramPacket);
                datagramPacket.getAddress().equals(this.c);
            } catch (SocketTimeoutException unused) {
            } catch (IOException unused2) {
                return;
            }
        }
    }

    private boolean Z1(boolean z) {
        if (this.o.get() || !this.D) {
            return false;
        }
        synchronized (this.t) {
            try {
                Map<String, Object> mapQ = this.t.Q();
                if (!this.t.M()) {
                    this.n.a("CARPLAY_UI_RECLAIM_REJECTED reason=" + this.t.D());
                    return false;
                }
                boolean zEquals = mapQ.equals(this.t.Q());
                if (zEquals && ((!this.E || this.c0 != null) && !z)) {
                    return true;
                }
                if (!zEquals || (this.E && this.c0 == null)) {
                    q2();
                    this.v.set(true);
                }
                this.w.incrementAndGet();
                G1();
                if (z) {
                    n nVar = this.n;
                    StringBuilder sb = new StringBuilder("CARPLAY_PEER_UI_REFRESH_QUEUED resource_changed=");
                    sb.append(!zEquals);
                    sb.append(" display_confirmed=false");
                    nVar.a(sb.toString());
                }
                this.n.a("CARPLAY_UI_RESUME_REQUESTED session_retained=true");
                return true;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    
    
    public void a1(CarLifeFrame.a aVar, d dVar) {
        if (dVar.o != null) {
            Y(aVar, dVar);
            return;
        }
        String str = "WAIT_MODES";
        while (G0() && !dVar.i.isClosed()) {
            try {
                if (dVar.g != 100 || (this.B.get() == null && !this.C.b())) {
                    PcmQueue.a aVarP = dVar.m.p();
                    if (aVarP == null) {
                        return;
                    }
                    str = "PACE";
                    long jB = dVar.n.b(System.nanoTime(), aVarP.c(), aVarP.e());
                    do {
                        long jA = io.carplay.app.secured.AudioPacer.a(System.nanoTime(), jB);
                        if (jA <= 0 || this.o.get() || dVar.i.isClosed()) {
                            synchronized (dVar.r) {
                                try {
                                    synchronized (dVar) {
                                        try {
                                            str = "OWNERSHIP";
                                            if (G0() && !dVar.i.isClosed()) {
                                                if (dVar.g != 100 || (this.B.get() == null && !this.C.b())) {
                                                    f fVar = this.z.get();
                                                    if (dVar.g == 100) {
                                                        o2(dVar, fVar);
                                                    }
                                                    str = "CIPHER_SKIP";
                                                    long j2 = dVar.s;
                                                    if (j2 != 0) {
                                                        dVar.k.e(j2);
                                                        dVar.s = 0L;
                                                    }
                                                    if (!dVar.m.g(aVarP)) {
                                                        dVar.k.e(Math.addExact(aVarP.e(), aVarP.c()));
                                                        dVar.n.c();
                                                    } else if (dVar.g != 100 || fVar.g()) {
                                                        dVar.D = Math.max(dVar.D, Math.max(0L, System.nanoTime() - jB));
                                                        dVar.k.e(aVarP.e());
                                                        byte[] bArrD = aVarP.d();
                                                        byte[] bArrC = dVar.k.c(bArrD, dVar.b);
                                                        str = "UDP_SEND";
                                                        long jNanoTime = System.nanoTime();
                                                        dVar.i.send(new DatagramPacket(bArrC, bArrC.length));
                                                        long jNanoTime2 = System.nanoTime() - jNanoTime;
                                                        synchronized (dVar) {
                                                            dVar.E = Math.max(dVar.E, jNanoTime2);
                                                            dVar.z += (long) aVarP.c();
                                                            dVar.A++;
                                                        }
                                                        this.s.addAndGet(bArrD.length);
                                                    }
                                                }
                                            }
                                        } catch (Throwable th) {
                                            throw th;
                                        }
                                    }
                                } catch (Throwable th2) {
                                    throw th2;
                                }
                            }
                            break;
                        } else {
                            LockSupport.parkNanos(Math.min(jA, 10000000L));
                        }
                    } while (!Thread.currentThread().isInterrupted());
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    continue;
                }
                LockSupport.parkNanos(10000000L);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            } catch (Exception e2) {
                String str2 = str;
                if (this.o.get() || dVar.i.isClosed()) {
                    return;
                }
                if (Iap2Bootstrap.l(e2)) {
                    v0("PCM_SEND_".concat(e2.getClass().getSimpleName()), e2);
                    return;
                }
                dVar.close();
                N1(aVar, dVar, "PCM16", str2, e2);
                this.n.a("AUDIO_SENDER_UNAVAILABLE channel=" + String.valueOf(aVar) + " video_retained=" + G0());
                return;
            }
        }
    }

    
    public static  void b1(Socket socket) {
        g0(socket);
    }

    
    public  void c1() {
        boolean z = false;
        long j3 = this.w.get();
        long j2 = j3;
        try {
            if (this.o.get()) {
                if (z) {
                    return;
                }
                if (j2 != j3) {
                    return;
                } else {
                    return;
                }
            }
            g2();
            if (this.w.get() != j3) {
                if (z) {
                    return;
                }
                if (j2 != j3) {
                    return;
                } else {
                    return;
                }
            }
            H1();
            if (this.v.getAndSet(false) && !this.o.get() && this.t.K()) {
                boolean zI = this.n.i();
                this.n.a("CARPLAY_SCREEN_RESUME_KEYFRAME_REQUEST accepted=" + zI);
            }
            if (z) {
                return;
            }
        } catch (Exception e2) {
            if (!this.o.get()) {
                if (Iap2Bootstrap.l(e2)) {
                    v0("SCREEN_MODES_".concat(e2.getClass().getSimpleName()), e2);
                } else {
                    this.n.a("CARPLAY_MODES_UPDATE_REJECTED session_retained=true");
                    if (this.B.get() != null) {
                        this.n.a("AUDIO_DECLARATION_REJECTED main_audio_paused=true video_retained=true");
                    }
                }
            }
            if (z) {
                return;
            }
        } finally {
            this.u.set(false);
            if (!this.o.get() && this.w.get() != j3) {
                G1();
            }
        }
    }

    private void c2() throws IOException {
        synchronized (this.G) {
            try {
                Socket socket = this.c0;
                if (socket == null) {
                    return;
                }
                this.C.e(this.w0 + 1, this.y.get().i());
                this.c0 = null;
                g0(socket);
                synchronized (this.M) {
                    try {
                        VideoCipher.AvcNalCipher cVar = this.n0;
                        if (cVar != null) {
                            cVar.close();
                        }
                        this.n0 = null;
                        this.K.clear();
                        this.w0++;
                        this.i0 = 0L;
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                R1("TEARDOWN", this.e, Map.of("streams", List.of(Map.of("type", 110, "uuid", this.o0.i()))));
                this.n.a("CARPLAY_SCREEN_ROUTE_RETIRED native_session_retained=true");
                this.n.p(false);
            } catch (Throwable th2) {
                throw th2;
            }
        }
    }

    private boolean d0(EventChannel eventChannel) {
        return eventChannel != null && this.a0 == eventChannel && eventChannel.n();
    }

    
    public static  void d1(DatagramSocket datagramSocket, byte[] bArr) {
        try {
            datagramSocket.send(new DatagramPacket(bArr, bArr.length));
        } catch (Throwable th) {
        }
    }

    
    public  void e1(KeepAliveServer i1Var) {
        if (this.o.get()) {
            return;
        }
        try {
            i1Var.e(System.nanoTime() / 1000000);
        } catch (RuntimeException unused) {
            long jC = i1Var.c();
            if (jC <= 3 || jC % 100 == 0) {
                this.n.a("CARPLAY_UDP_KEEPALIVE_SEND_UNCONFIRMED attempts=" + jC);
            }
        }
    }

    
    public void f0() {
        long j2 = this.G0;
        Socket socket = this.H0;
        if (this.o.get() || socket == null || socket != this.c0 || j2 == 0 || System.nanoTime() - j2 <= 2000000000) {
            return;
        }
        w0("SCREEN_WRITE_STALLED", true);
    }

    
    public static  boolean f1(byte[] bArr) {
        return (bArr[0] & 31) == 5;
    }

    
    public static void g0(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException unused) {
            }
        }
    }

    private static Map<String, Object> g1(Object obj) throws IOException {
        String str;
        if (obj instanceof Map) {
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            for (Map.Entry entry : ((Map<?, ?>) obj).entrySet()) {
                Object key = entry.getKey();
                if (key instanceof String) {
                    linkedHashMap.put((String) key, entry.getValue());
                } else {
                    str = "nonstring property-list key";
                }
            }
            return linkedHashMap;
        }
        str = "expected property-list dictionary";
        SyntheticRefs.c(str);
        return null;
    }

    private boolean h0(d dVar, long j2) throws IOException {
        if (G0() && !dVar.i.isClosed()) {
            f fVar = this.z.get();
            o2(dVar, fVar);
            if (dVar.m.e() != j2) {
                return false;
            }
            if (fVar.g() && this.B.get() == null && !this.C.b()) {
                return true;
            }
            U1(dVar);
        }
        return false;
    }

    
    
    public boolean H0(EventChannel eventChannel) {
        Socket socket = this.c0;
        return !this.o.get() && this.a0 == eventChannel && eventChannel.n() && G0() && this.X && this.E && socket != null && !socket.isClosed() && this.c0 == socket && this.r0 == null;
    }

    private void h2() throws IOException {
        long jNanoTime = System.nanoTime();
        synchronized (this.M) {
            try {
                Socket socket = this.c0;
                if (!this.o.get() && this.l0 && socket != null) {
                    long j2 = this.i0;
                    if (j2 == 0) {
                        this.i0 = jNanoTime;
                        this.j0 = this.r.get();
                        this.k0 = this.q.get();
                        return;
                    }
                    long j3 = jNanoTime - j2;
                    if (j3 < 1000000000) {
                        return;
                    }
                    long j4 = this.r.get();
                    long j5 = this.q.get();
                    if (v2(socket, VideoEncoderStats.a(this.h0, j4 - this.j0, j5 - this.k0, this.K.size(), j3))) {
                        this.i0 = jNanoTime;
                        this.j0 = j4;
                        this.k0 = j5;
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public static void j(String str) {
        Log.d("CarPlayHID", str);
    }

    private Socket j0() throws IOException {
        synchronized (this) {
            this.g1 = true;
        }
        try {
            return y1();
        } catch (IOException e2) {
            if (this.o.get() || Thread.currentThread().isInterrupted() || D0(e2) || !Iap2Bootstrap.l(e2)) {
                throw e2;
            }
            throw new m(e2, 0);
        }
    }

    public static k j1(k kVar) {
        AudioSourceCapability.b bVarL = AudioSourceCapability.l(kVar.j(), kVar.h(), kVar.g());
        return new k(bVarL.i(), bVarL.h(), bVarL.g(), kVar.i());
    }

    public static void k(String str, boolean z) {
        Log.d("CarPlayHID", "CarPlayHID " + str + "=" + z);
    }

    private Socket k0(int i2) throws IOException {
        return L0(i2, 10000);
    }

    
    
    public Socket L0(int i2, int i3) throws IOException {
        String str = "EVTCP connect host=" + this.c + " port=" + i2 + " timeout_ms=" + i3;
        Log.d("CarPlusMetrics", str);
        this.n.a(str);
        q0();
        Socket socketCreateSocket = this.a.createSocket();
        this.d0 = socketCreateSocket;
        try {
            try {
                socketCreateSocket.connect(new InetSocketAddress(this.c, i2), i3);
                socketCreateSocket.setTcpNoDelay(true);
                socketCreateSocket.setKeepAlive(true);
                q0();
                if (this.d0 == socketCreateSocket) {
                    this.d0 = null;
                }
                return socketCreateSocket;
            } catch (IOException | RuntimeException e2) {
                g0(socketCreateSocket);
                throw e2;
            }
        } catch (Throwable th) {
            if (this.d0 == socketCreateSocket) {
                this.d0 = null;
            }
            throw th;
        }
    }

    public static String l1(Object obj) {
        if (!(obj instanceof Map)) {
            return "NETWORK_IAP_SETUP_REPLY envelope=NON_DICTIONARY";
        }
        Object obj2 = ((Map) obj).get("streams");
        if (!(obj2 instanceof List)) {
            return "NETWORK_IAP_SETUP_REPLY envelope=DICTIONARY streams=".concat(obj2 != null ? "NON_LIST" : "ABSENT_OR_NULL");
        }
        List list = (List) obj2;
        int i2 = 0;
        String str = "ABSENT";
        for (Object obj3 : list) {
            if (obj3 instanceof Map) {
                Map map = (Map) obj3;
                if (m1(map.get("type"), -1) == 130) {
                    i2++;
                    Object obj4 = map.get("dataPort");
                    if (obj4 == null) {
                        str = "ABSENT_OR_NULL";
                    } else if ((obj4 instanceof Byte) || (obj4 instanceof Short) || (obj4 instanceof Integer) || (obj4 instanceof Long)) {
                        Number number = (Number) obj4;
                        str = (number.longValue() < 1 || number.longValue() > 65535) ? "OUT_OF_RANGE" : "VALID_INTEGER";
                    } else {
                        str = "NON_INTEGER";
                    }
                }
            }
        }
        return "NETWORK_IAP_SETUP_REPLY envelope=DICTIONARY streams=LIST rows=" + Math.min(Iap2Csm.b, list.size()) + " type130_rows=" + Math.min(Iap2Csm.b, i2) + " data_port=" + str;
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private void l2() throws EventChannel.d, IOException {
        synchronized (this.G) {
            q0();
            if (this.c0 != null) {
                return;
            }
            long streamConnectionID = E1();
            List<k> candidates = (this.X || this.V.isEmpty()) ? List.of(this.o0) : this.V;
            Iterator<k> it = candidates.iterator();
            Map<String, Object> response = null;
            k chosen = null;
            EventChannel.d lastReject = null;
            int attempt = 0;
            while (it.hasNext()) {
                k candidate = it.next();
                int next = attempt + 1;
                if (next > 3) {
                    break;
                }
                Map<String, Object> stream = new LinkedHashMap<>();
                stream.put("type", 110);
                stream.put("streamConnectionID", Long.valueOf(streamConnectionID));
                stream.put("latencyMs", 100);
                stream.put("uuid", candidate.i());
                try {
                    response = n2(R1("SETUP", this.e, Map.of("streams", List.of(stream))), 110);
                    chosen = candidate;
                    break;
                } catch (EventChannel.d e) {
                    lastReject = e;
                    if (!AudioSourceCapability.k(e.a)) {
                        throw e;
                    }
                    this.n.a("DISPLAY_PLAN_REJECTED attempt=" + next + " status=" + e.a);
                    attempt = next;
                }
            }
            if (response == null) {
                if (lastReject != null) {
                    throw lastReject;
                }
                throw new IOException("DISPLAY_SUPPORTED_ROUTE_UNAVAILABLE");
            }
            synchronized (this.H) {
                this.o0 = chosen;
                if (!this.W.isEmpty()) {
                    x0(this.W);
                }
            }
            Socket socket = null;
            VideoCipher.AvcNalCipher cipher = null;
            try {
                socket = k0(D1(response, "dataPort"));
                cipher = new VideoCipher.AvcNalCipher(this.m0, streamConnectionID);
                synchronized (this.M) {
                    q0();
                    o oVar = this.y.get();
                    this.C.e(this.w0 + 1, oVar.i() + 1);
                    this.x0 = streamConnectionID;
                    this.c0 = socket;
                    this.n0 = cipher;
                    this.w0++;
                    this.K.clear();
                    this.i0 = 0L;
                    this.y.set(new o(oVar.g(), oVar.i() + 1, System.nanoTime()));
                }
                this.n.a("CARPLAY_SCREEN_ROUTE_OPENED epoch=" + this.w0);
                this.n.p(true);
                this.X = true;
                if (G0()) {
                    openHidGate();
                }
                if (this.S != null) {
                    for (HidCapabilities.c c : this.S.i()) {
                        if (c.q() == 110 && n0(c).equals(this.o0.i())) {
                            this.n.k(this.S, c);
                            break;
                        }
                    }
                }
            } catch (Throwable th) {
                g0(socket);
                if (cipher != null) {
                    cipher.close();
                }
                throw th;
            }
        }
    }

    private byte[] m0(i iVar, byte[] bArr) {
        if (!this.D || !G0()) {
            return J1("MEDIA_KEY_BEFORE_READY");
        }
        try {
            HidConsumerParser.d dVarE = iVar.c.e(bArr);
            if (dVarE == null) {
                return new byte[0];
            }
            long j2 = iVar.e + 1;
            iVar.e = j2;
            long j3 = 0;
            if (j2 <= 4 || j2 % 100 == 0) {
                this.n.a("HID_CONSUMER_REPORT index=" + iVar.b + " count=" + j2 + " bytes=" + bArr.length);
            }
            HashSet hashSet = new HashSet();
            Iterator<Set<Integer>> it = iVar.d.values().iterator();
            while (it.hasNext()) {
                hashSet.addAll(it.next());
            }
            HashSet hashSet2 = new HashSet(dVarE.h());
            for (Integer num : dVarE.h()) {
                int iIntValue = num.intValue();
                if (!hashSet.contains(num)) {
                    int i2 = io.carplay.app.secured.HidConsumerParser.i(iIntValue);
                    if (i2 != 0) {
                        try {
                            if (this.n.m(i2)) {
                                long j4 = iVar.f + 1;
                                iVar.f = j4;
                                if (j4 <= 4 || j4 % 100 == j3) {
                                    this.n.a("HID_CONSUMER_FORWARDED index=" + iVar.b + " count=" + j4 + " native_key=" + i2 + " response_confirmed=false");
                                }
                                j3 = 0;
                            } else {
                                hashSet2.remove(num);
                            }
                        } catch (RuntimeException unused) {
                            hashSet2.remove(num);
                        }
                    } else if (!iVar.g) {
                        iVar.g = true;
                        this.n.a("HID_CONSUMER_UNSUPPORTED index=" + iVar.b + " action_sent=false reason=NO_NATIVE_CONTRACT");
                    }
                }
            }
            boolean zIsEmpty = hashSet2.isEmpty();
            Map<Integer, Set<Integer>> map = iVar.d;
            if (zIsEmpty) {
                map.remove(Integer.valueOf(dVarE.g()));
            } else {
                map.put(Integer.valueOf(dVarE.g()), Set.copyOf(hashSet2));
            }
            return !hashSet2.containsAll(dVarE.h()) ? J1("MEDIA_KEY_SOURCE_UNAVAILABLE") : new byte[0];
        } catch (IOException unused2) {
            return J1("MEDIA_KEY_INVALID_REPORT");
        }
    }

    private static int m1(Object obj, int i2) {
        return obj instanceof Number ? ((Number) obj).intValue() : i2;
    }

    private void m2(Map<String, Object> map) throws IOException {
        try {
            Integer numB = io.carplay.app.secured.KeepAliveServer.b(map);
            if (numB == null) {
                numB = 5020;
                this.n.a("V170_KEEPALIVE_PORT_FALLBACK port=5020");
            }
            DatagramSocket datagramSocket = new DatagramSocket((SocketAddress) null);
            this.f0 = datagramSocket;
            try {
                int i2 = 0;
                datagramSocket.bind(new InetSocketAddress(0));
                this.b.a(datagramSocket);
                datagramSocket.connect(this.c, numB.intValue());
                q0();
                o0 o0Var = new o0();
                o0Var.a = datagramSocket;
                VarHandle.storeStoreFence();
                KeepAliveServer i1Var = new KeepAliveServer(o0Var);
                this.g0 = i1Var;
                ScheduledExecutorService scheduledExecutorService = this.Z;
                p0 p0Var = new p0(i2);
                p0Var.b = this;
                p0Var.c = i1Var;
                VarHandle.storeStoreFence();
                scheduledExecutorService.scheduleWithFixedDelay(p0Var, 0L, 1L, TimeUnit.SECONDS);
                this.n.a("CARPLAY_UDP_KEEPALIVE advertised=true interval_ms="
                        + KeepAliveServer.SEND_INTERVAL_MS + " expects_reply=false");
            } catch (IOException e2) {
                datagramSocket.close();
                throw e2;
            }
        } catch (IOException unused) {
            this.n.a("CARPLAY_UDP_KEEPALIVE disabled=INVALID_OPTIONAL_PORT");
        }
    }

    private String n0(HidCapabilities.c cVar) {
        String strA;
        Object obj = this.W.get("displays");
        if (!(obj instanceof List)) {
            return "";
        }
        List list = (List) obj;
        if (cVar.p() >= list.size()) {
            return "";
        }
        Object obj2 = list.get(cVar.p());
        return (!(obj2 instanceof Map) || (strA = io.carplay.app.secured.ProtoFieldAccessor.a((Map) obj2)) == null) ? "" : strA;
    }

    private static Map<String, Object> n2(Object obj, int i2) throws IOException {
        Object obj2 = g1(obj).get("streams");
        if (obj2 instanceof List) {
            Iterator it = ((List) obj2).iterator();
            while (it.hasNext()) {
                Map<String, Object> mapG1 = g1(it.next());
                if (m1(mapG1.get("type"), -1) == i2) {
                    return mapG1;
                }
            }
        }
        SyntheticRefs.c(ProtoMessageBase.d("peer refused media stream type ", i2));
        return null;
    }

    private static boolean o1(d dVar, byte[] bArr) {
        if (bArr == null) {
            return false;
        }
        int length = bArr.length;
        int i2 = dVar.e;
        if (length < i2 * 2 || bArr.length > 65536 || bArr.length % (i2 * 2) != 0) {
            return false;
        }
        dVar.x = true;
        return true;
    }

    private static void o2(d dVar, f fVar) throws IOException {
        if (dVar.w == fVar.h()) {
            return;
        }
        U1(dVar);
        dVar.w = fVar.h();
    }

    private void p0(q qVar) throws IOException, InterruptedException {
        this.F0++;
        q qVarPeek = this.K.peek();
        if ((this.K.size() >= 8 || (qVarPeek != null && System.nanoTime() - qVarPeek.g() > 100000000)) && qVar.i().stream().anyMatch(new w0(10))) {
            int size = this.K.size();
            this.K.clear();
            this.n.a("VIDEO_BACKLOG_TRIMMED_AT_IDR=" + size);
        }
        long jNanoTime = System.nanoTime() + 2000000000;
        while (!this.o.get()) {
            if (this.K.offer(qVar, 100L, TimeUnit.MILLISECONDS)) {
                return;
            }
            if (System.nanoTime() >= jNanoTime) {
                throw new SocketTimeoutException("video sender stalled longer than 2 seconds");
            }
        }
        SyntheticRefs.c("CarPlay closed while waiting to enqueue video");
    }

    private void logEventTickRate() {
        ControlChannel.P0CNT++;
        if (SystemClock.uptimeMillis() - ControlChannel.P0SEC < 1000) {
            ControlChannel.P0SEC = SystemClock.uptimeMillis();
            int i2 = ControlChannel.P0CNT;
            ControlChannel.P0CNT = 0;
            String str = "HID_P0_RATE n=" + i2;
            j(str);
            this.n.a(str);
        }
    }

    private void p2() {
        f fVar = this.z.get();
        boolean zJ = this.t.J();
        if (zJ != fVar.g()) {
            this.z.set(new f(0, fVar.h() + 1, zJ));
        }
    }

    private void q() {
        Map<String, p> map = this.I0;
        String string = map != null ? Integer.toString(map.size()) : "?";
        k kVar = this.o0;
        j("CarPlayHID STATE I0=" + string + " displayUUID=" + (kVar != null ? kVar.i() : "?"));
    }

    
    public void q0() throws IOException {
        if (this.o.get()) {
            SyntheticRefs.c("CarPlay connection closed");
        }
    }

    private void q2() {
        boolean zK = this.t.K();
        o oVar = this.y.get();
        if (zK != oVar.g()) {
            this.C.e(this.w0, oVar.i() + 1);
            this.y.set(new o(zK, oVar.i() + 1, zK ? System.nanoTime() : oVar.h(), 0));
            if (!zK) {
                synchronized (this.U0) {
                    L1();
                }
            }
            if (zK) {
                this.v.set(true);
            }
            this.n.a("CARPLAY_SCREEN_OWNER_PHONE=" + zK);
        }
    }

    private void r0() throws IOException {
        q0();
        if (this.l0) {
            return;
        }
        SyntheticRefs.c("CarPlay media not ready");
    }

    
    public void r2() {
        byte[] bArr = new byte[512];
        while (!this.o.get()) {
            try {
                DatagramPacket datagramPacket = new DatagramPacket(bArr, 512);
                this.e0.receive(datagramPacket);
                ControlChannel.LogTiming(datagramPacket);
                if (datagramPacket.getAddress().equals(this.c) && datagramPacket.getLength() == 32 && (bArr[0] & 255) == 128 && (bArr[1] & 255) == 210) {
                    long jA = VideoCipher.a(this.y0 + ((System.nanoTime() - this.z0) / 1000000));
                    byte[] bArr2 = new byte[32];
                    ByteBuffer byteBufferOrder = ByteBuffer.wrap(bArr2).order(ByteOrder.BIG_ENDIAN);
                    byteBufferOrder.put((byte) -128).put((byte) -45).putShort((short) 7);
                    System.arraycopy(bArr, 24, bArr2, 8, 8);
                    byteBufferOrder.putLong(16, jA);
                    byteBufferOrder.putLong(24, jA);
                    this.e0.send(new DatagramPacket(bArr2, 32, datagramPacket.getSocketAddress()));
                }
            } catch (SocketTimeoutException unused) {
            } catch (Exception e2) {
                if (!this.o.get()) {
                    v0("TIMING_CHANNEL_".concat(e2.getClass().getSimpleName()), e2);
                }
            }
        }
    }

    
    
    
    
    private byte[] s1(io.carplay.app.secured.EventChannel.b request, String origin) throws IOException {
        long startNanos = System.nanoTime();
        if (!request.f().endsWith("/command")) {
            return null;
        }
        Map<String, Object> envelope = g1(BinaryPlist.e(request.c()));
        String type = String.valueOf(envelope.get("type"));
        if (this.D && !this.o.get()) {
            String notice = this.N0.c(envelope, request.c().length, origin);
            if (notice != null) {
                this.n.a(notice);
            }
        }
        if ("hidSendReport".equals(type)) {
            return handleHidReport(envelope, startNanos);
        }
        if ("forceKeyFrame".equals(type)) {
            
            
            return this.n.i() ? new byte[0] : null;
        }
        long unhandled = this.unhandledCommandCount + 1;
        this.unhandledCommandCount = unhandled;
        if (unhandled <= 4 || unhandled % 100 == 0) {
            this.n.a("CARPLAY_COMMAND_UNHANDLED type=" + type + " count=" + unhandled);
        }
        return null;
    }

    
    private byte[] handleHidReport(Map<String, Object> envelope, long startNanos) throws IOException {
        if (!ControlChannel.HID_MODE_REPORTED) {
            ControlChannel.HID_MODE_REPORTED = true;
            j("HID_REPORT_RECEIVED -> sweep stop");
        }
        synchronized (this.U0) {
            if (this.o.get()) {
                return J1("TOUCH_SESSION_CLOSED");
            }
            Map<String, Object> params = envelope;
            if (envelope.containsKey("params")) {
                if (envelope.containsKey("uuid") || envelope.containsKey("hidReport")) {
                    return J1("TOUCH_AMBIGUOUS_ENVELOPE");
                }
                try {
                    params = g1(envelope.get("params"));
                } catch (IOException e2) {
                    return J1("TOUCH_INVALID_ENVELOPE");
                }
            }
            String uuid = A0(params.get("uuid"));
            p touch = uuid == null ? null : this.I0.get(uuid);
            Object reportValue = params.get("hidReport");
            if (!(reportValue instanceof byte[])) {
                return J1("TOUCH_MISSING_REPORT");
            }
            byte[] report = (byte[]) reportValue;
            if (touch != null) {
                return handleTouchReport(uuid, touch, report, startNanos);
            }
            return handleBackReport(uuid, report);
        }
    }

    
    private byte[] handleTouchReport(String uuid, p device, byte[] report, long startNanos) throws IOException {
        long received = this.X0 + 1;
        this.X0 = received;
        if (received <= 4 || received % 100 == 0) {
            this.n.a("HID_REPORT_RECEIVED count=" + received
                    + " index=" + device.b + " bytes=" + report.length);
        }
        if (!this.y.get().g()) {
            L1();
            return J1("TOUCH_SCREEN_NOT_OWNED");
        }
        if (this.Q0 != null && !this.Q0.equals(uuid)) {
            return J1("TOUCH_ANOTHER_DEVICE_ACTIVE");
        }
        HidDescriptorParser.f state;
        try {
            state = device.c.b(report, device.d);
        } catch (IOException e2) {
            L1();
            return J1("TOUCH_INVALID_REPORT_" + y0(e2));
        }
        if (state == null) {
            return new byte[0];
        }
        if (device.e) {
            device.d = state.q() ? state : null;
            if (!state.q()) {
                device.e = false;
            }
            return new byte[0];
        }
        if (this.o0 == null) {
            return J1("TOUCH_DISPLAY_UNAVAILABLE");
        }
        if (!device.f) {
            device.f = true;
            this.n.a("HID_TOUCH_RANGE index=" + device.b
                    + " logical_x=" + state.t() + ":" + state.r()
                    + " logical_y=" + state.u() + ":" + state.s()
                    + " display=" + this.o0.j() + "x" + this.o0.h()
                    + " source=" + this.Y0 + "x" + this.Z0
                    + " descriptor_driven=true");
        }
        boolean down = state.q();
        q6.a position;
        if (this.touchTransform == null) {
            int x = (int) Math.round(state.v() * this.o0.j());
            int y = (int) Math.round(state.w() * this.o0.h());
            position = new q6.a(
                    Math.min(this.Y0 - 1, (int) ((((long) x) * this.Y0) / this.o0.j())),
                    Math.min(this.Z0 - 1, (int) ((((long) y) * this.Z0) / this.o0.h())));
        } else {
            position = this.touchTransform.n(state.x(), state.y(), state.t(), state.r(), state.u(), state.s());
        }
        if (position == null) {
            device.e = state.q();
            if (!this.R0) {
                device.d = state.q() ? state : null;
                return new byte[0];
            }
        }
        int x2 = position == null ? this.S0 : position.g();
        int y2 = position == null ? this.T0 : position.h();
        if (!down) {
            if (!this.R0) {
                device.d = null;
                return new byte[0];
            }
            x2 = this.S0;
            y2 = this.T0;
        }
        try {
            this.n.r(x2, y2, down);
        } catch (IOException e3) {
            return J1("TOUCH_SOURCE_UNAVAILABLE");
        }
        this.B0.d(IoStatsSnapshot.b.TOUCH_WRITE, startNanos, System.nanoTime());
        this.R0 = down;
        this.Q0 = down ? uuid : null;
        device.d = (!down && !device.e) ? null : state;
        this.S0 = x2;
        this.T0 = y2;
        long forwarded = this.W0 + 1;
        this.W0 = forwarded;
        if (forwarded <= 4 || forwarded % 100 == 0) {
            this.n.a("TOUCH_FORWARDED=" + forwarded);
        }
        return new byte[0];
    }

    
    private byte[] handleBackReport(String uuid, byte[] report) throws IOException {
        i consumer = uuid == null ? null : this.K0.get(uuid);
        if (consumer != null) {
            return m0(consumer, report);
        }
        if (!this.D) {
            return J1("BACK_BEFORE_AUTHENTICATED_SESSION");
        }
        h back = uuid == null ? null : this.J0.get(uuid);
        if (back == null) {
            return J1("TOUCH_UNKNOWN_DEVICE");
        }
        HidBackParser.d parsed;
        try {
            parsed = back.c.f(report);
        } catch (IOException e2) {
            return J1("BACK_INVALID_REPORT");
        }
        if (parsed == null) {
            return new byte[0];
        }
        Integer key = Integer.valueOf(parsed.h());
        boolean wasEmpty = back.d.isEmpty();
        if (!parsed.g()) {
            back.d.remove(key);
            return new byte[0];
        }
        back.d.add(key);
        if (!wasEmpty) {
            return new byte[0];
        }
        if (!this.y.get().g()) {
            return J1("BACK_SCREEN_NOT_OWNED");
        }
        
        
        if (!this.n.l()) {
            back.d.remove(key);
            return J1("BACK_SOURCE_UNAVAILABLE");
        }
        this.n.a("HID_BACK_FORWARDED index=" + back.b + " native_key_written=true");
        return new byte[0];
    }

    private void u0(String str) {
        w0(str, true);
    }

    
    public void u2() {
        long lastEpoch = -1L;              
        long idrEpoch = -1L;               
        boolean requireIdr = true;         
        boolean waitingNativeIdr = false;  
        byte[] lastSps = null;             
        byte[] lastPps = null;             
        int diagnosticCount = 0;           
        long lastProgressLog = -1L;        
        boolean firstIdrSent = false;      
        while (!this.o.get()) {
            try {
                h2();
                q unit = this.K.poll(1L, TimeUnit.SECONDS);
                if (unit == null) {
                    continue;
                }
                synchronized (this.M) {
                    if (this.o.get()) {
                        return;
                    }
                    if (unit.h() != this.w0) {
                        continue;
                    }
                    Socket socket = this.c0;
                    VideoCipher.AvcNalCipher cipher = this.n0;
                    if (socket == null || cipher == null) {
                        continue;
                    }
                    o epoch = this.y.get();
                    if (lastEpoch != epoch.i()) {
                        lastEpoch = epoch.i();
                        if (idrEpoch >= 0) {
                            this.n.a("VIDEO_EPOCH_WAITING_IDR=" + idrEpoch);
                        }
                        requireIdr = true;
                        waitingNativeIdr = false;
                        lastSps = null;
                        lastPps = null;
                        lastProgressLog = -1L;
                        firstIdrSent = false;
                    }
                    if (!epoch.g() || unit.g() < epoch.h()) {
                        requireIdr = true;
                        continue;
                    }
                    long currentEpochId = idrEpoch;
                    boolean checkIdr = requireIdr;
                    boolean waiting = waitingNativeIdr;
                    long progressLog = lastProgressLog;
                    boolean firstIdr = firstIdrSent;
                    if (idrEpoch != unit.h()) {
                        idrEpoch = unit.h();
                        this.n.a("VIDEO_EPOCH_WAITING_IDR=" + idrEpoch);
                        currentEpochId = idrEpoch;
                        checkIdr = true;
                        waiting = false;
                        lastSps = null;
                        lastPps = null;
                        progressLog = -1L;
                        firstIdr = false;
                    }
                    if (checkIdr) {
                        if (unit.i().stream().noneMatch(new w0(11))) {
                            if (!waiting) {
                                this.n.a("WAITING_ACTUAL_NATIVE_IDR_AFTER_CONNECTION");
                                waiting = true;
                            }
                            idrEpoch = currentEpochId;
                            requireIdr = checkIdr;
                            waitingNativeIdr = waiting;
                            firstIdrSent = firstIdr;
                            lastProgressLog = progressLog;
                            continue;
                        }
                        this.n.a("ACTUAL_NATIVE_IDR_ACCEPTED");
                    }
                    if (unit.l() == null || unit.j() == null) {
                        throw new IOException("actual AVC parameters missing for queued unit");
                    }
                    if (!Arrays.equals(lastSps, unit.l()) || !Arrays.equals(lastPps, unit.j())) {
                        if (!v2(socket, VideoCipher.d(unit.l(), unit.j(), this.a1, this.b1))) {
                            idrEpoch = currentEpochId;
                            requireIdr = false;
                            waitingNativeIdr = waiting;
                            firstIdrSent = firstIdr;
                            lastProgressLog = progressLog;
                            continue;
                        }
                        this.n.a("ACTUAL_NATIVE_AVC_CONFIG_SENT");
                        this.n.a("CARPLAY_VIDEO_CONFIG width=" + this.a1 + " height=" + this.b1
                                + " encrypted=true");
                        if (diagnosticCount < 16) {
                            diagnosticCount++;
                            this.n.a("CARPLAY_AVC_DIAGNOSTIC "
                                    + AvcSpsParser.a(unit.l(), this.a1, this.b1));
                        }
                        lastSps = unit.l();
                        lastPps = unit.j();
                    }
                    long startNanos = System.nanoTime();
                    this.B0.d(IoStatsSnapshot.b.SENDER_QUEUE, unit.k(), startNanos);
                    byte[] encrypted = cipher.b(unit.i(),
                            VideoCipher.a(this.y0 + ((unit.g() - this.z0) / 1000000)));
                    long encryptDoneNanos = System.nanoTime();
                    if (!v2(socket, encrypted)) {
                        idrEpoch = currentEpochId;
                        requireIdr = false;
                        waitingNativeIdr = waiting;
                        firstIdrSent = firstIdr;
                        lastProgressLog = progressLog;
                        continue;
                    }
                    if (this.o.get()) {
                        return;
                    }
                    this.q.addAndGet(encrypted.length);
                    long units = this.r.incrementAndGet();
                    if (units == 1 || units % 300 == 0) {
                        this.n.a("VIDEO_UNITS_SENT=" + units);
                    }
                    long writeDoneNanos = System.nanoTime();
                    this.B0.d(IoStatsSnapshot.b.SOCKET_WRITE, encryptDoneNanos, writeDoneNanos);
                    this.B0.d(IoStatsSnapshot.b.SOURCE_TO_WRITE, unit.g(), writeDoneNanos);
                    Q1(writeDoneNanos, units);
                    if (progressLog < 0 || writeDoneNanos - progressLog >= 1000000000L) {
                        this.n.a("VIDEO_PROGRESS units=" + units);
                        progressLog = writeDoneNanos;
                    }
                    if (!firstIdr) {
                        this.C.f(currentEpochId, epoch.i(), writeDoneNanos);
                        this.n.a("VIDEO_EPOCH_FIRST_IDR_SENT=" + currentEpochId);
                        firstIdr = true;
                    }
                    idrEpoch = currentEpochId;
                    requireIdr = false;
                    waitingNativeIdr = waiting;
                    firstIdrSent = firstIdr;
                    lastProgressLog = progressLog;
                }
            } catch (Exception e) {
                if (this.o.get()) {
                    return;
                }
                v0("VIDEO_SEND_".concat(e.getClass().getSimpleName()), e);
                return;
            }
        }
    }

    private void v0(String str, Throwable th) {
        w0(str, Iap2Bootstrap.l(th));
    }

    private boolean v2(Socket socket, byte[] bArr) throws IOException {
        this.H0 = socket;
        this.G0 = System.nanoTime();
        boolean z = false;
        try {
            try {
                socket.getOutputStream().write(bArr);
                if (!this.o.get() && socket == this.c0) {
                    z = true;
                }
            } catch (IOException e2) {
                if (socket == this.c0 && !this.o.get()) {
                    throw e2;
                }
            }
            this.G0 = 0L;
            this.H0 = null;
            return z;
        } catch (Throwable th) {
            this.G0 = 0L;
            this.H0 = null;
            throw th;
        }
    }

    private void w0(String str, boolean z) {
        if (this.o.get() || !this.p.compareAndSet(false, true)) {
            return;
        }
        O1(this.eventChannel, "TRANSPORT_END");
        this.l0 = false;
        try {
            this.n.j(str, z);
        } finally {
            close();
        }
    }

    
    
    
    
    private void w1(CarLifeFrame.a aVar, int i2, int i3, int i4, AudioCapabilities.d dVar) throws Throwable {
        Throwable th = null;
        DatagramSocket datagramSocket;
        t tVar;
        VideoCipher.AacPacket aVar2;
        DatagramSocket datagramSocket2;
        VideoCipher.AacPacket aVar3;
        DatagramSocket datagramSocket3;
        DatagramSocket datagramSockeDisplaySession = null;
        try {
            if (dVar.g()) {
                try {
                    t tVarA = this.R.a(dVar.n(), dVar.l());
                    if (tVarA == null) {
                        try {
                            throw new IOException("AUDIO_AAC_ENCODER_UNAVAILABLE");
                        } catch (Throwable thCaught) {
                            datagramSocket = null;
                            aVar2 = null;
                            tVar = tVarA;
                            if (datagramSockeDisplaySession != null) {
                                datagramSockeDisplaySession.close();
                            }
                            if (datagramSocket != null) {
                                datagramSocket.close();
                            }
                            if (aVar2 != null) {
                                aVar2.close();
                            }
                            if (tVar != null) {
                                tVar.close();
                            }
                            throw thCaught;
                        }
                    }
                    tVar = tVarA;
                } catch (Throwable th2) {
                    th = th2;
                    datagramSocket = null;
                    tVar = null;
                    aVar2 = null;
                }
            } else {
                tVar = null;
            }
            try {
                long jE1 = E1();
                datagramSocket = new DatagramSocket((SocketAddress) null);
                try {
                    int i5 = 0;
                    datagramSocket.bind(new InetSocketAddress(0));
                    this.b.a(datagramSocket);
                    datagramSocket.setSoTimeout(1000);
                    LinkedHashMap linkedHashMap = new LinkedHashMap();
                    linkedHashMap.put("type", Integer.valueOf(dVar.p()));
                    linkedHashMap.put("audioType", dVar.j());
                    linkedHashMap.put("audioFormat", Long.valueOf(dVar.h()));
                    linkedHashMap.put("sampleRate", Integer.valueOf(dVar.n()));
                    if (dVar.g()) {
                        try {
                            linkedHashMap.put("spf", 1024);
                            Boolean bool = Boolean.FALSE;
                            linkedHashMap.put("supportsRTPPacketRedundancy", bool);
                            linkedHashMap.put("supportsHighAccuracyTimestamps", bool);
                        } catch (Throwable th3) {
                            th = th3;
                            aVar2 = null;
                            if (datagramSockeDisplaySession != null) {
                                datagramSockeDisplaySession.close();
                            }
                            if (datagramSocket != null) {
                                datagramSocket.close();
                            }
                            if (aVar2 != null) {
                                aVar2.close();
                            }
                            if (tVar != null) {
                                tVar.close();
                            }
                            throw th;
                        }
                    }
                    linkedHashMap.put("streamConnectionID", Long.valueOf(jE1));
                    linkedHashMap.put("audioLatencyMs", Integer.valueOf(dVar.i()));
                    linkedHashMap.put("controlPort", Integer.valueOf(datagramSocket.getLocalPort()));
                    linkedHashMap.put("input", Boolean.FALSE);
                    Map<String, Object> mapN2 = n2(R1("SETUP", this.e, Map.of("streams", List.of(linkedHashMap))), dVar.p());
                    if (mapN2.containsKey("controlPort")) {
                        datagramSocket.connect(this.c, D1(mapN2, "controlPort"));
                    }
                    DatagramSocket datagramSocket5 = new DatagramSocket((SocketAddress) null);
                    try {
                        datagramSocket5.bind(new InetSocketAddress(0));
                        this.b.a(datagramSocket5);
                        datagramSocket5.connect(this.c, D1(mapN2, "dataPort"));
                        datagramSocket2 = datagramSocket5;
                        try {
                            try {
                                VideoCipher.AacPacket aVar4 = new VideoCipher.AacPacket(this.m0, jE1, ByteBuffer.wrap(DerCodec.q(4)).getInt(), dVar.p());
                                try {
                                    r0();
                                    aVar3 = aVar4;
                                    try {
                                        datagramSocket3 = datagramSocket2;
                                        try {
                                            d dVar2 = new d(dVar, i2, i3, i4, datagramSocket3, datagramSocket, aVar3, tVar);
                                            this.O.put(aVar, dVar2);
                                            if (dVar.m()) {
                                                synchronized (this.t) {
                                                    try {
                                                        this.t.p();
                                                        if (this.t.j()) {
                                                            c cVar = new c(this.A.incrementAndGet(), i5);
                                                            this.B.set(cVar);
                                                            this.n.a("AUDIO_MAIN_MODES_PENDING generation=" + cVar.g() + " pcm_paused=true");
                                                            this.w.incrementAndGet();
                                                            G1();
                                                        } else {
                                                            this.n.a("AUDIO_RESOURCE_NOT_OWNED reason=" + this.t.D());
                                                        }
                                                        p2();
                                                    } catch (Throwable th4) {
                                                        throw th4;
                                                    }
                                                }
                                            }
                                            boolean z = true;
                                            p0 p0Var = new p0(1);
                                            p0Var.b = this;
                                            p0Var.c = dVar2;
                                            VarHandle.storeStoreFence();
                                            Thread thread = new Thread(p0Var, "CarPlus-audio-control");
                                            thread.setDaemon(true);
                                            thread.start();
                                            q0 q0Var = new q0(i5);
                                            q0Var.b = this;
                                            q0Var.c = aVar;
                                            q0Var.d = dVar2;
                                            VarHandle.storeStoreFence();
                                            Thread thread2 = new Thread(q0Var, "CarPlus-audio-send");
                                            thread2.setDaemon(true);
                                            dVar2.t = thread2;
                                            thread2.start();
                                            n nVar = this.n;
                                            String strValueOf = String.valueOf(aVar);
                                            int iN = dVar.n();
                                            int iL = dVar.l();
                                            int iP = dVar.p();
                                            String strJ = dVar.j();
                                            boolean z2 = (i2 == dVar.n() && i3 == dVar.l()) ? false : true;
                                            if (!dVar.m() || this.B.get() == null) {
                                                z = false;
                                            }
                                            nVar.a("AUDIO_READY " + strValueOf + " " + iN + "/" + iL + "/" + i4 + " stream=" + iP + " type=" + strJ + " converted=" + z2 + " modes_pending=" + z + " main_owned=" + this.t.J());
                                            if (dVar.g()) {
                                                this.n.a("AUDIO_CODEC_SELECTED channel=" + String.valueOf(aVar) + " codec=AAC_LC frame_samples=1024 raw_access_units=true receiver_playback_unconfirmed=true");
                                            }
                                        } catch (Throwable tBtDiscovery) {
                                            th = tBtDiscovery;
                                            datagramSockeDisplaySession = datagramSocket3;
                                            aVar2 = aVar3;
                                            if (datagramSockeDisplaySession != null) {
                                                datagramSockeDisplaySession.close();
                                            }
                                            if (datagramSocket != null) {
                                                datagramSocket.close();
                                            }
                                            if (aVar2 != null) {
                                                aVar2.close();
                                            }
                                            if (tVar != null) {
                                                tVar.close();
                                            }
                                            throw th;
                                        }
                                    } catch (Throwable th6) {
                                        th = th6;
                                        datagramSocket3 = datagramSocket2;
                                        datagramSockeDisplaySession = datagramSocket3;
                                        aVar2 = aVar3;
                                        if (datagramSockeDisplaySession != null) {
                                            datagramSockeDisplaySession.close();
                                        }
                                        if (datagramSocket != null) {
                                            datagramSocket.close();
                                        }
                                        if (aVar2 != null) {
                                            aVar2.close();
                                        }
                                        if (tVar != null) {
                                            tVar.close();
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th7) {
                                    th = th7;
                                    aVar3 = aVar4;
                                }
                            } catch (Throwable th8) {
                                th = th8;
                                datagramSocket2 = datagramSocket2;
                                datagramSockeDisplaySession = datagramSocket2;
                                aVar2 = null;
                                if (datagramSockeDisplaySession != null) {
                                    datagramSockeDisplaySession.close();
                                }
                                if (datagramSocket != null) {
                                    datagramSocket.close();
                                }
                                if (aVar2 != null) {
                                    aVar2.close();
                                }
                                if (tVar != null) {
                                    tVar.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th9) {
                            th = th9;
                        }
                    } catch (Throwable th10) {
                        th = th10;
                        datagramSocket2 = datagramSocket5;
                    }
                } catch (Throwable th11) {
                    th = th11;
                    datagramSockeDisplaySession = null;
                }
            } catch (Throwable th12) {
                th = th12;
                datagramSockeDisplaySession = null;
                datagramSocket = null;
            }
        } catch (Throwable th13) {
            th = th13;
            datagramSockeDisplaySession = null;
            datagramSocket = null;
            tVar = null;
            aVar2 = null;
            if (datagramSockeDisplaySession != null) {
                datagramSockeDisplaySession.close();
            }
            if (datagramSocket != null) {
                datagramSocket.close();
            }
            if (aVar2 != null) {
                aVar2.close();
            }
            if (tVar != null) {
                tVar.close();
            }
            throw th;
        }
    }

    private static boolean w2(Object obj) {
        return ((obj instanceof Byte) || (obj instanceof Short) || (obj instanceof Integer) || (obj instanceof Long)) && ((Number) obj).longValue() == 0;
    }

    
    
    
    
    
    private void x0(Map<String, Object> map) {
        List list;
        Iterator it;
        HidBackParser x0VarI;
        synchronized (this.H) {
            try {
                this.I++;
                synchronized (this.U0) {
                    try {
                        L1();
                        ControlChannel.PendClear();
                        this.I0.clear();
                        this.J0.clear();
                        this.K0.clear();
                        Object obj = map.get("hidDevices");
                        if (!(obj instanceof List)) {
                            this.n.a("HID_CAPABILITIES devices=0 touch=0");
                            return;
                        }
                        List list2 = (List) obj;
                        if (list2.size() > 32) {
                            this.n.a("HID_TOUCH_UNSUPPORTED reason=DEVICE_LIMIT");
                            return;
                        }
                        HashSet hashSet = new HashSet();
                        int i2 = 0;
                        for (Iterator it2 = list2.iterator(); it2.hasNext(); it2 = it) {
                            Object next = it2.next();
                            int i3 = i2 + 1;
                            if (next instanceof Map) {
                                Map map2 = (Map) next;
                                Object obj2 = map2.get("hidDescriptor");
                                Object obj3 = map2.get("uuid");
                                String strA0 = A0(obj3);
                                if (strA0 == null || !(obj2 instanceof byte[])) {
                                    list = list2;
                                    it = it2;
                                } else {
                                    byte[] bArr = (byte[]) obj2;
                                    ControlChannel.PendAdd(strA0);
                                    Object obj4 = map2.get("displayUUID");
                                    if (obj4 != null) {
                                        io.carplay.app.secured.ProtoFieldAccessor.c(obj4, this.o0.i());
                                    }
                                    if (!hashSet.contains(strA0)) {
                                        if (this.I0.containsKey(strA0) || this.J0.containsKey(strA0) || this.K0.containsKey(strA0)) {
                                            list = list2;
                                            it = it2;
                                            this.I0.remove(strA0);
                                            this.J0.remove(strA0);
                                            this.K0.remove(strA0);
                                            hashSet.add(strA0);
                                            this.n.a("HID_TOUCH_UNSUPPORTED index=" + i2 + " reason=DUPLICATE_DEVICE");
                                        } else {
                                    try {
                                        HidDescriptorParser z0VarE = io.carplay.app.secured.HidDescriptorParser.e(bArr);
                                        if (this.I0.size() >= 8) {
                                            this.n.a("HID_TOUCH_UNSUPPORTED index=" + i2 + " reason=TOUCH_DEVICE_LIMIT");
                                        } else {
                                            this.I0.put(strA0, new p(obj3, i2, z0VarE));
                                            int length = bArr.length;
                                            int size = z0VarE.f().size();
                                            boolean z = obj4 != null;
                                            String str = obj3 instanceof String ? "STRING" : "INTEGER";
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("HID_TOUCH_DESCRIPTOR index=");
                                            sb.append(i2);
                                            sb.append(" bytes=");
                                            sb.append(length);
                                            sb.append(" reports=");
                                            sb.append(size);
                                            sb.append(" display_bound=");
                                            sb.append(z);
                                            sb.append(" uuid_kind=");
                                            sb.append(str);
                                            this.n.a(sb.toString());
                                        }
                                    } catch (IOException unused) {
                                        try {
                                            x0VarI = io.carplay.app.secured.HidBackParser.i(bArr);
                                            if (this.J0.size() < 8) {
                                                this.J0.put(strA0, new h(obj3, i2, x0VarI));
                                                String str2 = "HID_BACK_DESCRIPTOR index=" + i2 + " bytes=" + bArr.length + " reports=" + x0VarI.j();
                                                this.n.a(str2);
                                                j(str2);
                                            }
                                        } catch (IOException unused2) {
                                            try {
                                                HidConsumerParser y0VarJ = io.carplay.app.secured.HidConsumerParser.j(bArr);
                                                if (this.K0.size() < 8) {
                                                    this.K0.put(strA0, new i(obj3, i2, y0VarJ));
                                                    this.n.a("HID_CONSUMER_DESCRIPTOR index=" + i2 + " bytes=" + bArr.length + " reports=" + y0VarJ.k() + " descriptor_driven=true");
                                                }
                                            } catch (IOException unused3) {
                                                StringBuilder sb2 = new StringBuilder();
                                                sb2.append("HID_TOUCH_UNSUPPORTED index=");
                                                sb2.append(i2);
                                                sb2.append(" bytes=");
                                                sb2.append(bArr.length);
                                                sb2.append(Arrays.toString(bArr));
                                                sb2.append(" reason=DESCRIPTOR");
                                                this.n.a(sb2.toString());
                                            }
                                        }
                                    }
                                        }
                                    }
                                    list = list2;
                                    it = it2;
                                }
                            } else {
                                list = list2;
                                it = it2;
                            }
                            i2 = i3;
                            list2 = list;
                        }
                        List list3 = list2;
                        this.n.a("HID_CAPABILITIES devices=" + list3.size() + " touch=" + this.I0.size());
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            } catch (Throwable th2) {
                throw th2;
            }
        }
    }

    
    public Iap2WirelessSetup.b x1() throws InterruptedException, IOException {
        long jNanoTime = System.nanoTime() + 5000000000L;
        while (!G0()) {
            q0();
            if (System.nanoTime() > jNanoTime) {
                SyntheticRefs.c("control POST iAP before record deadline");
                return null;
            }
            Thread.sleep(10L);
        }
        return new Iap2PostResponseOwner(this, this.j);
    }

    private static String y0(IOException iOException) {
        String message = iOException.getMessage() == null ? "" : iOException.getMessage();
        message.getClass();
        switch (message) {
            case "HID_REPORT_LIMIT":
            case "HID_UNKNOWN_REPORT_ID":
            case "HID_INVALID_CONTACT":
            case "HID_REPORT_LENGTH":
            case "HID_CONTACT_COUNT_MISMATCH":
            case "HID_MULTITOUCH_UNSUPPORTED":
            case "HID_CONTACT_CHANGED_WITHOUT_UP":
            case "HID_VALUE_RANGE":
            case "HID_COORDINATE_RANGE":
                return iOException.getMessage();
            default:
                return "HID_UNSUPPORTED";
        }
    }

    
    
    
    
    
    
    
    
    
    
    private java.net.Socket y1() throws java.io.IOException {
        
        if (Thread.currentThread().isInterrupted()) {
            this.n.a("control TCP cancelled");
            return null;
        }
        long startNanos = System.nanoTime();
        int attempt = 0;
        final int LIMIT = 2;
        java.net.Socket socket = null;
        try {
            while (true) {
                int endpointGen;
                int port;
                java.net.InetAddress addr;
                synchronized (this.f) {
                    endpointGen = this.h;
                    port = this.d;
                    addr = this.c;
                }
                q0();
                try {
                    socket = this.a.createSocket();
                } catch (RuntimeException e) {
                    g0(null);
                    throw e;
                } catch (java.io.IOException e) {
                    if (!retryControlConnect(e, attempt, endpointGen, startNanos)) {
                        throw e;
                    }
                    attempt++;
                    continue;
                }
                synchronized (this.f) {
                    if (this.h != endpointGen) {
                        g0(socket);
                        if (attempt < LIMIT) {
                            attempt++;
                            continue;
                        }
                        throw new java.io.IOException("startup endpoint update limit");
                    }
                    this.d0 = socket;
                }
                try {
                    long budgetNanos = java.util.concurrent.TimeUnit.MILLISECONDS.toNanos((long) this.e1 + 2000L) - (System.nanoTime() - startNanos);
                    if (budgetNanos <= 0L) {
                        throw new java.net.SocketTimeoutException("initial control endpoint budget");
                    }
                    java.net.InetSocketAddress ep = new java.net.InetSocketAddress(addr, port);
                    int connectTimeout = (int) Math.max(1L, Math.min((long) this.e1, budgetNanos / 1000000L));
                    socket.connect(ep, connectTimeout);
                    socket.setTcpNoDelay(true);
                    socket.setKeepAlive(true);
                    q0();
                    synchronized (this.f) {
                        if (this.h != endpointGen) {
                            g0(socket);
                            if (attempt < LIMIT) {
                                attempt++;
                                continue;
                            }
                            throw new java.io.IOException("startup endpoint update limit");
                        }
                        this.g = true;
                    }
                    if (this.d0 == socket) {
                        this.d0 = null;
                    }
                    return socket;
                } catch (RuntimeException e) {
                    g0(socket);
                    throw e;
                } catch (java.io.IOException e) {
                    g0(socket);
                    if (!retryControlConnect(e, attempt, endpointGen, startNanos)) {
                        throw e;
                    }
                    if (this.d0 == socket) {
                        this.d0 = null;
                    }
                    attempt++;
                    continue;
                }
            }
        } catch (java.io.IOException th) {
            if (this.d0 == socket) {
                this.d0 = null;
            }
            throw th;
        } catch (RuntimeException th) {
            if (this.d0 == socket) {
                this.d0 = null;
            }
            throw th;
        }
    }

    private boolean retryControlConnect(java.io.IOException e, int attempt, int endpointGen, long startNanos) throws java.io.IOException {
        
        
        
        q0();
        if (Thread.currentThread().isInterrupted()) {
            return false;
        }
        if (attempt >= 2) {
            return false;
        }
        long elapsed = System.nanoTime() - startNanos;
        long budget = java.util.concurrent.TimeUnit.MILLISECONDS.toNanos((long) this.e1 + 2000L);
        if (elapsed > budget) {
            return false;
        }
        boolean endpointChanged = (this.h != endpointGen);
        if (!endpointChanged && !(e instanceof java.net.ConnectException)) {
            return false;
        }
        if (!endpointChanged && (e instanceof java.net.ConnectException) && elapsed > 2000000000L) {
            return false;
        }
        int nextAttempt = attempt + 1;
        int delay = nextAttempt * 200;
        this.n.a("CARPLAY_CONTROL_TCP_RETRY attempt=" + nextAttempt + " delay_ms=" + delay + " before_protocol=true endpoint_changed=" + endpointChanged);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new java.io.IOException("control TCP cancelled", ie);
        }
        return true;
    }

    
    public Iap2WirelessSetup.b z1() throws IOException, InterruptedException {
        long jNanoTime = System.nanoTime() + 5000000000L;
        while (true) {
            Socket socketK0 = null;
            if (G0()) {
                long jE1 = E1();
                LinkedHashMap linkedHashMap = new LinkedHashMap();
                linkedHashMap.put("type", 130);
                linkedHashMap.put("controlType", 2);
                linkedHashMap.put("seed", Long.valueOf(jE1));
                linkedHashMap.put("clientUUID", UUID.randomUUID().toString());
                linkedHashMap.put("channelID", this.k.e() + "-RCS-1");
                linkedHashMap.put("clientTypeUUID", "E9459FD0-BCAD-4C45-820F-1E72447EF2F2");
                Boolean bool = Boolean.TRUE;
                linkedHashMap.put("wantsDedicatedSocket", bool);
                linkedHashMap.put("sendMessageAsIs", bool);
                try {
                    Object objR1 = R1("SETUP", this.e, Map.of("streams", List.of(linkedHashMap)));
                    this.n.a(l1(objR1));
                    socketK0 = k0(D1(n2(objR1, 130), "dataPort"));
                    this.s0 = socketK0;
                    try {
                        q0();
                        byte[] bArr = this.m0;
                        OutputStream outputStream = socketK0.getOutputStream();
                        d1 d1Var = new d1(3);
                        d1Var.b = socketK0;
                        VarHandle.storeStoreFence();
                        Iap2WirelessSetup.d dVar = new Iap2WirelessSetup.d(bArr, jE1, outputStream, d1Var);
                        this.n.a("NETWORK_IAP_ROUTE_OPENED type=130 fresh_link_proof_required=true");
                        return dVar;
                    } catch (Exception e2) {
                        g0(socketK0);
                        throw e2;
                    }
                } catch (Exception e3) {
                    if (socketK0 != null) {
                        g0(socketK0);
                    }
                    this.n.a("NETWORK_IAP_SETUP_FAILED stage=SETUP_OR_REPLY_DECODE kind=" + e3.getClass().getSimpleName() + " media_retained=" + G0());
                    throw e3;
                }
            }
            q0();
            if (System.nanoTime() > jNanoTime) {
                SyntheticRefs.c("network iAP before record deadline");
                return null;
            }
            Thread.sleep(10L);
        }
    }

    private void zzLog(String str, Map map) {
        this.n.a(str + " n=" + map.size() + " keys=" + map.keySet().toString());
    }

    public synchronized void B0(int i2) {
        if (this.g1 || this.o.get()) {
            throw new IllegalStateException("initial control connection already started/closed");
        }
        if (i2 < 1 || i2 > 10000) {
            throw new IllegalArgumentException("initial control timeout must be 1..10000 ms");
        }
        this.e1 = i2;
    }

    public void B1(DeviceMetadataStore i5Var) {
        if (this.a0 == null) {
            this.f1 = i5Var;
        } else {
            SyntheticRefs.d("phone identification after connect");
        }
    }

    public void C0(boolean z) {
        if (this.a0 == null) {
            this.F = z;
        } else {
            SyntheticRefs.d("screen intent after connect");
        }
    }

    public void C1(String str) {
        synchronized (this.f) {
            try {
                if (this.g || this.a0 != null) {
                    throw new IllegalStateException("transport identity after protocol");
                }
                this.i = io.carplay.app.secured.n.a(str);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public boolean E0(CarLifeFrame.a aVar, int i2, int i3, int i4) {
        d dVar = this.O.get(aVar);
        return G0() && dVar != null && dVar.b(i2, i3, i4);
    }

    public boolean F0(CarLifeFrame.a aVar) {
        d dVar = this.O.get(aVar);
        if (!G0() || dVar == null || dVar.i.isClosed()) {
            return false;
        }
        if (dVar.p) {
            return this.B.get() == null && !this.C.b() && this.t.J();
        }
        return true;
    }

    public boolean G0() {
        return this.l0 && !this.o.get();
    }

    public void S(Map<String, Object> map) throws IOException {
        zzLog("CARPLAY_INFO_KEYS", map);
        this.n.a("CARPLAY_INFO_FEATURES value=" + String.valueOf(map.get("features")));
        this.W = map;
        HidCapabilities xVarK = io.carplay.app.secured.HidCapabilities.K(map, this.T, Collections.EMPTY_SET);
        this.S = xVarK;
        z.a aVarG = this.n.g(xVarK);
        if (aVarG != null) {
            this.U.h(aVarG);
        }
        AudioSourceCapability p1VarF = AudioSourceCapability.f(this.S, this.U);
        ArrayList arrayList = new ArrayList();
        List list = (List) map.get("displays");
        for (HidCapabilities.c cVar : p1VarF.g()) {
            String strA = io.carplay.app.secured.ProtoFieldAccessor.a((Map) list.get(cVar.p()));
            if (strA != null) {
                arrayList.add(new k(cVar.v(), cVar.j(), Math.min(60, cVar.n()), strA));
            }
        }
        if (arrayList.isEmpty()) {
            SyntheticRefs.c("CAP_MAIN_DISPLAY_UNAVAILABLE");
            return;
        }
        this.V = List.copyOf(arrayList);
        boolean z = false;
        this.o0 = (k) arrayList.get(0);
        x0(map);
        this.n.a(this.S.O());
        this.n.a(p1VarF.m());
        this.n.q(this.S);
        Object obj = map.get("keepAliveSendStatsAsBody");
        if (Boolean.TRUE.equals(obj) || (((obj instanceof Byte) || (obj instanceof Short) || (obj instanceof Integer) || (obj instanceof Long)) && ((Number) obj).longValue() == 1)) {
            z = true;
        }
        this.h0 = z;
        this.Q = p1VarF.e();
        if (!this.S.h()) {
            this.n.a("AUDIO_CAPABILITIES_INVALID output_disabled=true");
        }
        if (!this.Q.l() && (this.Q.i() || (this.R != null && this.Q.h()))) {
            this.t.p();
        }
        this.n.a("AUDIO_CAPABILITIES legacy=" + this.Q.l() + " output_rows=" + this.Q.t() + " pcm=" + this.Q.i());
        Iterator<String> it = this.Q.f().iterator();
        while (it.hasNext()) {
            this.n.a(it.next());
        }
        Object obj2 = map.get("modes");
        if (map.containsKey("modes")) {
            if (!(obj2 instanceof Map)) {
                this.n.a("CARPLAY_INITIAL_MODES_REJECTED code=MODE_INFO_ENVELOPE_TYPE kind=ENVELOPE row=-1 authentication_already_verified=true");
                SyntheticRefs.c("CARPLAY_INFO_MODES_UNSUPPORTED");
                return;
            }
            Map map2 = (Map) obj2;
            boolean zX = this.t.x(g1(map2));
            SessionStateMachine.a aVarS = this.t.s();
            n nVar = this.n;
            String str = zX ? "CARPLAY_INITIAL_MODES_ACCEPTED" : "CARPLAY_INITIAL_MODES_REJECTED";
            nVar.a(str + " code=" + aVarS.g() + " kind=" + aVarS.h() + " row=" + aVarS.i() + " authentication_already_verified=true");
            n nVar2 = this.n;
            if (zX) {
                nVar2.a(this.t.w());
                P1("INITIAL_INFO");
                q2();
            } else {
                nVar2.a(SessionStateMachine.u(map2, aVarS));
                String strC = ResourceFingerprintBuilder.c(map2, aVarS);
                if (strC != null) {
                    this.n.a(strC);
                }
                SyntheticRefs.c("CARPLAY_INFO_MODES_UNSUPPORTED");
            }
        }
    }

    public synchronized void S1() {
        if (this.g1 || this.o.get() || this.m != null) {
            throw new IllegalStateException("candidate confirmation requires explicit unpinned pre-connect mode");
        }
        this.h1 = true;
    }

    public boolean U() {
        boolean z;
        synchronized (this.f) {
            try {
                z = !this.o.get() && !this.g && this.a0 == null && this.h < 4;
            } catch (Throwable th) {
                throw th;
            }
        }
        return z;
    }

    public g V() {
        return this.v0;
    }

    public void V1(g gVar) throws IOException {
        synchronized (this.L) {
            synchronized (this.M) {
                try {
                    q0();
                    this.C.e(this.w0 + 1, this.y.get().i());
                    this.w0++;
                    this.K.clear();
                    this.N.k();
                    this.v0 = gVar;
                    if (gVar != null) {
                        this.N.l(gVar.h(), gVar.g());
                    }
                    this.n.a("VIDEO_EPOCH_WAITING_IDR=" + this.w0);
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public void W1(g gVar, int i2, int i3) throws IOException {
        if (i2 < 1 || i3 < 1 || i2 > 4096 || i3 > 4096) {
            SyntheticRefs.c("encoded video dimensions outside supported bounds");
            return;
        }
        synchronized (this.L) {
            synchronized (this.M) {
                q0();
                V1(gVar);
                this.a1 = i2;
                this.b1 = i3;
            }
        }
    }

    public synchronized boolean X1() {
        EventChannel eventChannel = this.a0;
        if (G0() && this.D && eventChannel != null) {
            for (d dVar : this.O.values()) {
                synchronized (dVar) {
                    if (!dVar.p || dVar.i.isClosed() || !dVar.x) {
                    }
                }
                synchronized (this.t) {
                    if (this.t.J() && this.B.get() != null && this.u.get()) {
                        this.n.a("AUDIO_USER_REQUEST_PENDING coalesced=true playback_confirmed=false");
                        return true;
                    }
                    if (!eventChannel.n()) {
                        return I1("SESSION_NOT_READY");
                    }
                    synchronized (this.t) {
                        if (!G0()) {
                            return I1("SESSION_NOT_READY");
                        }
                        if (this.t.J() && this.B.get() == null) {
                            this.n.a("AUDIO_USER_REQUEST_ALREADY_OWNED");
                            return true;
                        }
                        if (this.t.J() && this.B.get() != null && this.u.get()) {
                            this.n.a("AUDIO_USER_REQUEST_PENDING coalesced=true playback_confirmed=false");
                            return true;
                        }
                        if (!this.t.l()) {
                            return I1("RESOURCE_CONSTRAINT");
                        }
                        c cVar = new c(this.A.incrementAndGet(), 0);
                        this.B.set(cVar);
                        p2();
                        this.w.incrementAndGet();
                        G1();
                        this.n.a("AUDIO_USER_REQUEST_PENDING coalesced=false generation=" + cVar.g() + " playback_confirmed=false");
                        return true;
                    }
                }
            }
            return I1("SOURCE_NOT_READY");
        }
        return I1("SESSION_NOT_READY");
    }

    public boolean Y1() {
        return Z1(false);
    }

    public synchronized void a0(t.a aVar) {
        if (this.g1 || this.o.get()) {
            throw new IllegalStateException("audio encoder after connection");
        }
        Objects.requireNonNull(aVar);
        this.R = aVar;
    }

    public void a2(g gVar) throws IOException {
        synchronized (this.L) {
            this.N.l(gVar.h(), gVar.g());
            this.v0 = gVar;
        }
    }

    public PairingTlv8.b b2() {
        if (G0()) {
            return this.p0;
        }
        return null;
    }

    public void c0(boolean z) {
        this.C.a(z, this.w0, this.y.get().i());
    }

    @Override 
    public void close() {
        if (this.o.compareAndSet(false, true)) {
            this.l0 = false;
            O1(this.eventChannel, "TRANSPORT_END");
            g0(this.s0);
            Iap2WirelessSetup iap2WirelessSetup = this.r0;
            if (iap2WirelessSetup != null) {
                iap2WirelessSetup.close();
            }
            Iap2WirelessSetup iap2WirelessSetup2 = this.j;
            if (iap2WirelessSetup2 != null) {
                iap2WirelessSetup2.close();
            }
            g0(this.d0);
            g0(this.c0);
            if (this.e0 != null) {
                this.e0.close();
            }
            if (this.f0 != null) {
                this.f0.close();
            }
            if (this.g0 != null) {
                this.g0.close();
            }
            this.Z.shutdownNow();
            if (this.eventChannel != null) {
                try {
                    this.eventChannel.close();
                } catch (IOException unusedIo) {
                }
            }
            if (this.a0 != null) {
                try {
                    this.a0.close();
                } catch (IOException unusedIo2) {
                }
            }
            synchronized (this.U0) {
                L1();
            }
            this.Y.shutdownNow();
            this.K.clear();
            synchronized (this) {
                try {
                    Iterator<d> it = this.O.values().iterator();
                    while (it.hasNext()) {
                        it.next().close();
                    }
                    this.O.clear();
                } catch (Throwable th) {
                    throw th;
                }
            }
            synchronized (this.L) {
                this.N.k();
            }
            VideoCipher.AvcNalCipher cVar = this.n0;
            if (cVar != null) {
                cVar.close();
            }
            byte[] bArr = this.m0;
            if (bArr != null) {
                Arrays.fill(bArr, (byte) 0);
            }
        }
    }

    public boolean d2() {
        if (this.o.get() || !this.D) {
            return false;
        }
        synchronized (this.t) {
            try {
                Map<String, Object> mapQ = this.t.Q();
                if (!this.t.T()) {
                    return false;
                }
                q2();
                if (mapQ.equals(this.t.Q()) && (!this.E || this.c0 == null)) {
                    return true;
                }
                this.w.incrementAndGet();
                G1();
                this.n.a("CARPLAY_RETURN_TO_CAR session_retained=true");
                return true;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public HidCapabilities e0() {
        return this.S;
    }

    public boolean e2() {
        return (this.o.get() || this.c0 == null) ? false : true;
    }

    public k f2() {
        return this.o0;
    }

    
    public void g2() throws IOException {
        Map<String, Object> mapQ;
        c cVar;
        boolean zCompareAndSet;
        q0();
        EventChannel eventChannel = this.a0;
        if (!this.D || eventChannel == null || !eventChannel.n()) {
            SyntheticRefs.c("SCREEN_MODES_BEFORE_AUTHENTICATED_SESSION");
            return;
        }
        synchronized (this.t) {
            mapQ = this.t.Q();
            cVar = this.B.get();
        }
        Map<String, Object> mapG1 = g1(R1("POST", "/command", mapQ));
        Object obj = mapG1.get("status");
        if (obj != null && ((!(obj instanceof Byte) && !(obj instanceof Short) && !(obj instanceof Integer) && !(obj instanceof Long)) || ((Number) obj).longValue() != 0)) {
            SyntheticRefs.c("SCREEN_MODES_APPLICATION_REJECTED");
            return;
        }
        if (mapG1.containsKey("status") && obj == null) {
            SyntheticRefs.c("SCREEN_MODES_APPLICATION_REJECTED");
            return;
        }
        q0();
        synchronized (this.t) {
            if (cVar != null) {
                try {
                    if (mapQ.equals(this.t.Q()) && this.t.J()) {
                        zCompareAndSet = this.B.compareAndSet(cVar, null);
                    } else {
                        zCompareAndSet = false;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                zCompareAndSet = false;
            }
        }
        if (zCompareAndSet) {
            this.n.a("AUDIO_MAIN_MODES_CONFIRMED generation=" + cVar.g() + " playback_confirmed=false");
        }
        this.n.a("CARPLAY_SCREEN_MODES_SENT screen_phone=" + this.t.K() + " display_confirmed=false");
    }

    
    public k i0() throws Throwable {
        Throwable th = null;
        int i2;
        EventChannel eventChannel;
        String str;
        String str2;
        synchronized (this) {
            q0();
            if (this.g1) {
                throw new IOException("CONTROL_CONNECT_ALREADY_STARTED");
            }
            i2 = 1;
            this.g1 = true;
        }
        this.n.a("CARPLAY_CONTROL_CONNECTING");
        Socket socketJ0 = j0();
        this.d0 = socketJ0;
        try {
            q0();
            eventChannel = new EventChannel(socketJ0, 15000);
            try {
                this.a0 = eventChannel;
                if (this.d0 == socketJ0) {
                    this.d0 = null;
                }
                this.e0 = new DatagramSocket((SocketAddress) null);
                this.e0.setReuseAddress(true);
                this.e0.bind(new InetSocketAddress(0));
                this.b.a(this.e0);
                this.e0.setSoTimeout(1000);
                EventChannel eventChannel2 = this.a0;
                int localPort = this.e0.getLocalPort();
                j0 j0Var = new j0(i2);
                j0Var.b = this;
                VarHandle.storeStoreFence();
                t0(eventChannel2, localPort, j0Var);
                boolean z = this.F;
                SessionStateMachine sessionStateMachine = this.t;
                if (!z) {
                    sessionStateMachine.T();
                } else if (!sessionStateMachine.M()) {
                    this.n.a("CARPLAY_INITIAL_SCREEN_CONSTRAINED");
                }
                P1("LOCAL_CLAIM");
                q2();
                g2();
                if (this.t.K()) {
                    l2();
                }
                R1("RECORD", this.e, Collections.EMPTY_MAP);
                this.l0 = true;
                this.E = true;
                H1();
                ScheduledExecutorService scheduledExecutorService = this.Y;
                b0 b0Var = new b0(3);
                b0Var.b = this;
                VarHandle.storeStoreFence();
                scheduledExecutorService.execute(b0Var);
                ScheduledExecutorService scheduledExecutorService2 = this.Z;
                b0 b0Var2 = new b0(4);
                b0Var2.b = this;
                VarHandle.storeStoreFence();
                TimeUnit timeUnit = TimeUnit.SECONDS;
                scheduledExecutorService2.scheduleWithFixedDelay(b0Var2, 1L, 1L, timeUnit);
                ScheduledExecutorService scheduledExecutorService3 = this.Z;
                b0 b0Var3 = new b0(5);
                b0Var3.b = this;
                VarHandle.storeStoreFence();
                scheduledExecutorService3.scheduleWithFixedDelay(b0Var3, 1L, 1L, timeUnit);
                ScheduledExecutorService scheduledExecutorService4 = this.Z;
                b0 b0Var4 = new b0(6);
                b0Var4.b = this;
                VarHandle.storeStoreFence();
                scheduledExecutorService4.scheduleWithFixedDelay(b0Var4, 5L, 5L, timeUnit);
                n nVar = this.n;
                nVar.a("CARPLAY_MEDIA_READY " + this.o0.j() + "x" + this.o0.h());
                nVar.a(this.u0 ? "CARPLAY_IAP_DIAG_U0=true" : "CARPLAY_IAP_DIAG_U0=false");
                nVar.a(this.r0 != null ? "CARPLAY_IAP_DIAG_R0=exists" : "CARPLAY_IAP_DIAG_R0=null");
                nVar.a(this.j != null ? "CARPLAY_IAP_DIAG_J=exists" : "CARPLAY_IAP_DIAG_J=null");
                nVar.a(this.a0 != null ? "CARPLAY_IAP_DIAG_A0=exists" : "CARPLAY_IAP_DIAG_A0=null");
                String str3 = this.m;
                if (str3 != null) {
                    str = str3.matches("[a-fA-F0-9]{64}") ? "CARPLAY_IAP_DIAG_M=64hex" : "CARPLAY_IAP_DIAG_M=present_non64hex";
                } else {
                    str = "CARPLAY_IAP_DIAG_M=null";
                }
                nVar.a(str);
                openHidGate();
                this.n.a("V153_MEDIA_PATH_CONTINUE");
                boolean z2 = this.u0;
                synchronized (this.q0) {
                    try {
                        if (this.j != null || (str2 = this.m) == null || str2.matches("[a-fA-F0-9]{64}")) {
                        }
                    } catch (Throwable thCaught) {
                        throw thCaught;
                    }
                }
                return this.o0;
            } catch (Throwable th2) {
                th = th2;
                if (eventChannel == null) {
                    g0(socketJ0);
                }
                if (this.d0 == socketJ0) {
                    this.d0 = null;
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            eventChannel = null;
        }
        throw th;
    }

    public boolean i1(int i2, int i3) {
        k kVar = this.o0;
        return kVar != null && i2 == kVar.j() && i3 == this.o0.h() && Math.max(i2, i3) <= 1920;
    }

    public long i2() {
        return this.s.get();
    }

    public long j2() {
        return this.q.get();
    }

    public void k1(int i2, int i3) throws IOException {
        if (i2 < 1 || i3 < 1 || i2 > 4096 || i3 > 4096) {
            SyntheticRefs.c("native video dimensions outside supported bounds");
            return;
        }
        synchronized (this.U0) {
            try {
                if (this.Y0 != i2 || this.Z0 != i3) {
                    L1();
                    this.touchTransform = null;
                    this.d1 = false;
                }
                this.Y0 = i2;
                this.Z0 = i3;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public long k2() {
        return this.r.get();
    }

    public boolean n1(CarLifeFrame.a aVar, int i2, int i3, int i4, byte[] bArr) {
        synchronized (this.P.get(aVar)) {
            d dVar = this.O.get(aVar);
            boolean z = false;
            if (G0() && dVar != null && dVar.b(i2, i3, i4)) {
                synchronized (dVar) {
                    try {
                        if (G0() && !dVar.i.isClosed() && o1(dVar, bArr)) {
                            z = true;
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                return z;
            }
            return false;
        }
    }

    public void o0(boolean z) {
        if (this.a0 == null) {
            this.u0 = z;
        } else {
            SyntheticRefs.d("network iAP setting after connect");
        }
    }

    public void p1(CarLifeFrame.a aVar, int i2, int i3, int i4) throws IOException {
        Throwable th;
        AirPlaySession airPlaySession;
        AirPlaySession airPlaySession2;
        CarLifeFrame.a aVar2;
        Exception exc;
        synchronized (this.P.get(aVar)) {
            try {
                synchronized (this) {
                    try {
                        r0();
                        d dVar = this.O.get(aVar);
                        if (dVar != null) {
                            try {
                                if (dVar.b(i2, i3, i4)) {
                                    if (aVar == CarLifeFrame.a.TTS) {
                                        F1(dVar, true);
                                    }
                                    this.n.a("AUDIO_ROUTE_REUSED ".concat(String.valueOf(aVar)));
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                airPlaySession = this;
                                throw th;
                            }
                        }
                        int i5 = 0;
                        if (dVar != null) {
                            if (aVar == CarLifeFrame.a.TTS) {
                                F1(dVar, false);
                            }
                            dVar.close();
                            try {
                                R1("TEARDOWN", this.e, Map.of("streams", List.of(Map.of("type", Integer.valueOf(dVar.g)))));
                                this.O.remove(aVar);
                            } catch (IOException e2) {
                                if (Iap2Bootstrap.l(e2)) {
                                    v0("AUDIO_TEARDOWN_IO", e2);
                                }
                                throw new e("AUDIO_RECONFIGURATION_UNAVAILABLE", e2);
                            }
                        }
                        try {
                            HashSet hashSet = new HashSet();
                            Iterator<d> it = this.O.values().iterator();
                            while (it.hasNext()) {
                                try {
                                    hashSet.add(Integer.valueOf(it.next().g));
                                } catch (Exception e3) {
                                    exc = e3;
                                    airPlaySession2 = this;
                                    aVar2 = aVar;
                                }
                            }
                            aVar2 = aVar;
                            try {
                                Iterator<AudioCapabilities.d> it2 = AudioSourceCapability.d(this.Q, aVar2, i2, i3, i4, hashSet).iterator();
                                Throwable eVar = null;
                                while (true) {
                                    if (!it2.hasNext()) {
                                        if (eVar != null) {
                                            throw eVar;
                                        }
                                        throw new e("AUDIO_SUPPORTED_ROUTE_UNAVAILABLE");
                                    }
                                    AudioCapabilities.d next = it2.next();
                                    if (next.g()) {
                                        try {
                                            if (this.R == null) {
                                                eVar = new e("AUDIO_AAC_ENCODER_UNAVAILABLE");
                                            }
                                        } catch (Exception e4) {
                                            exc = e4;
                                            airPlaySession2 = this;
                                        }
                                    }
                                    int i6 = i5 + 1;
                                    airPlaySession2 = this;
                                    try {
                                        try {
                                            airPlaySession2.w1(aVar2, i2, i3, i4, next);
                                            if (aVar2 != CarLifeFrame.a.TTS) {
                                                break;
                                            }
                                            airPlaySession2.F1(airPlaySession2.O.get(aVar2), true);
                                            break;
                                        } catch (EventChannel.d e5) {
                                            if (!AudioSourceCapability.k(e5.a)) {
                                                throw e5;
                                            }
                                            airPlaySession2.n.a("AUDIO_PLAN_REJECTED channel=" + String.valueOf(aVar2) + " attempt=" + i6 + " status=" + e5.a);
                                            i5 = i6;
                                            exc = e5;
                                        }
                                    } catch (Exception e6) {
                                        exc = e6;
                                    }
                                    if (Iap2Bootstrap.l(exc)) {
                                        airPlaySession2.v0("AUDIO_SETUP_" + exc.getClass().getSimpleName(), exc);
                                    }
                                    airPlaySession2.n.a("AUDIO_ROUTE_UNAVAILABLE channel=" + String.valueOf(aVar2) + " session_retained=" + airPlaySession2.G0());
                                    throw new e("CarPlay audio route unavailable", exc);
                                }
                            } catch (Exception e7) {
                                exc = e7;
                                airPlaySession2 = this;
                            }
                        } catch (Exception e8) {
                            exc = e8;
                            airPlaySession2 = this;
                            aVar2 = aVar;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        airPlaySession = this;
                        th = th;
                        throw th;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
            }
        }
    }

    public void q1(CarLifeFrame.a aVar) throws IOException {
        synchronized (this.P.get(aVar)) {
            try {
                d dVar = this.O.get(aVar);
                if (dVar != null && !this.o.get() && !dVar.i.isClosed()) {
                    synchronized (dVar.r) {
                        synchronized (dVar) {
                            try {
                                dVar.x = false;
                                if (!this.o.get() && !dVar.i.isClosed()) {
                                    U1(dVar);
                                }
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                    }
                    if (aVar == CarLifeFrame.a.TTS) {
                        F1(dVar, false);
                    }
                    this.n.a("AUDIO_SOURCE_STOP channel=" + String.valueOf(aVar) + " output_retained=true");
                }
            } catch (Throwable th2) {
                throw th2;
            }
        }
    }

    public byte[] r1(EventChannel.b bVar) throws IOException {
        return s1(bVar, "UNSPECIFIED");
    }

    public Map<String, Object> s0(EventChannel eventChannel, int i2) throws IOException, GeneralSecurityException {
        return t0(eventChannel, i2, new SyntheticRefs());
    }

    public void s2(q6 q6Var) {
        synchronized (this.U0) {
            if (q6Var != null) {
                try {
                    if (q6Var.q() == this.Y0) {
                        if (q6Var.p() != this.Z0) {
                        }
                    }
                    if (!this.d1) {
                        this.d1 = true;
                        this.n.a("TOUCH_GEOMETRY_RETIRED source_dimensions_changed=true");
                    }
                    return;
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (!Objects.equals(this.touchTransform, q6Var)) {
                L1();
                this.touchTransform = q6Var;
                if (q6Var != null) {
                    this.n.a("TOUCH_GEOMETRY_APPLIED source=" + q6Var.q() + "x" + q6Var.p() + " canvas=" + q6Var.h() + "x" + q6Var.g() + " viewport=" + q6Var.k() + "," + q6Var.s() + "," + q6Var.t() + "," + q6Var.j() + " direct_logical_mapping=true");
                }
            }
        }
    }

    public Map<String, Object> t0(EventChannel eventChannel, int i2, l lVar) throws IOException, GeneralSecurityException {
        q0();
        synchronized (this.f) {
            this.g = true;
            this.a0 = eventChannel;
        }
        j0 j0Var = new j0(4);
        j0Var.b = this;
        VarHandle.storeStoreFence();
        eventChannel.x(j0Var);
        j0 j0Var2 = new j0(5);
        j0Var2.b = this;
        VarHandle.storeStoreFence();
        eventChannel.y(j0Var2);
        this.n.a("CARPLAY_PAIRING_PROFILE hkp=2 verify_pd=1");
        this.n.a("CARPLAY_PAIR_SETUP");
        PairingTlv8 pairingTlv8 = new PairingTlv8(this.k, this.l);
        PairingTlv8.e eVarH = pairingTlv8.h(this.a0, "3939", this.m);
        this.m0 = eVarH.e();
        this.n.a("CARPLAY_PAIR_VERIFIED");
        if (eVarH.d()) {
            this.n.a("CARPLAY_PAIR_VERIFY_RESUMED_STORED_PEER fresh_signature=true new_pairing_saved=false");
        }
        if (this.h1 && this.m == null) {
            MfiSap.e(this.a0);
        }
        MfiSap.a aVarB = pairingTlv8.b(this.a0, this.m);
        if (eVarH.d()) {
            this.p0 = eVarH.c();
        }
        if (aVarB.c()) {
            this.n.a("MFI_SAP_CERTIFICATE_EXPIRY_COMPATIBLE fresh_proof_verified=true pinned=true encrypted_control=true");
        }
        this.n.a("MFI_FRESH_PROOF_VERIFIED_PINNED_APPLE_CA_UNVERIFIED=" + (!aVarB.a()));
        this.n.a("CARPLAY_INITIAL_SETUP");
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("name", "Car+ Rootless");
        linkedHashMap.put("model", "Android Car+ sender");
        String strE = this.i;
        if (strE == null) {
            strE = this.k.e();
        }
        linkedHashMap.put("deviceID", strE);
        n nVar = this.n;
        nVar.a("CARPLAY_SETUP_DEVICE_ID value=" + strE + " len=" + String.valueOf(strE).length());
        nVar.a("CARPLAY_SETUP_TRANSPORT_ID source=" + (this.i == null ? "PAIRING_ID_COMPAT_FALLBACK" : "BLUETOOTH_SNAPSHOT") + " values_logged=false");
        linkedHashMap.put("sessionUUID", UUID.randomUUID().toString());
        linkedHashMap.put("sourceVersion", "CarPlus-0.6");
        linkedHashMap.put("osBuildVersion", "21A329");
        this.n.a("AUDIO_WIRE_PROFILE profile=MODERN_RTP aad_bytes=8 compatibility_build_declared=true actual_android_build=false");
        linkedHashMap.put("timingPort", Integer.valueOf(i2));
        linkedHashMap.put("timingProtocol", "NTP");
        linkedHashMap.put("keepAliveLowPower", Boolean.TRUE);
        ArrayList arrayList = new ArrayList();
        arrayList.add("uiContext");
        arrayList.add("viewAreas");
        arrayList.add("cornerMasks");
        arrayList.add("focusTransfer");
        arrayList.add("h.264Level5.1");
        arrayList.add("mainBuffered");
        arrayList.add("altScreen");
        arrayList.add("enhancedSiri");
        arrayList.add("hevc");
        arrayList.add("sessionManagement");
        arrayList.add("logTransfer");
        arrayList.add("iAPChannel");
        arrayList.add("vehicleStateProtocol");
        arrayList.add("videoPlayback");
        linkedHashMap.put("features", arrayList);
        zzLog("CARPLAY_SETUP_KEYS", linkedHashMap);
        Map<String, Object> mapG1 = g1(R1("SETUP", this.e, linkedHashMap));
        this.T = mapG1;
        zzLog("CARPLAY_SETUP_RESP_KEYS", mapG1);
        q0();
        lVar.e(mapG1);
        q0();
        this.n.a("CARPLAY_INFO_AFTER_AUTHENTICATED_SETUP");
        S(g1(R1("GET", "/info", null)));
        q0();
        
        this.D = true;
        return mapG1;
    }

    public void t1(CarLifeFrame.a aVar, byte[] bArr, long j2) throws IOException {
        synchronized (this.P.get(aVar)) {
            try {
                r0();
                d dVar = this.O.get(aVar);
                if (dVar == null || dVar.i.isClosed()) {
                    throw new e("PCM before actual output negotiation");
                }
                synchronized (dVar) {
                    if (G0() && !dVar.i.isClosed()) {
                        o1(dVar, bArr);
                        if (!dVar.p || (this.B.get() == null && !this.C.b())) {
                            try {
                                synchronized (dVar) {
                                    if (G0() && !dVar.i.isClosed()) {
                                        if (dVar.p) {
                                            f fVar = this.z.get();
                                            o2(dVar, fVar);
                                            if (!fVar.g()) {
                                                return;
                                            }
                                        }
                                        long jNanoTime = System.nanoTime();
                                        long j3 = dVar.B;
                                        if (j3 != 0) {
                                            dVar.C = Math.max(dVar.C, Math.max(0L, jNanoTime - j3));
                                        }
                                        dVar.B = jNanoTime;
                                        byte[] bArrC = dVar.l.c(bArr);
                                        if (bArrC.length == 0) {
                                            return;
                                        }
                                        dVar.y += (long) (bArrC.length / (dVar.b * 2));
                                        long jI = dVar.m.i(bArrC);
                                        if (jI > 0) {
                                            dVar.u += jI;
                                            dVar.v++;
                                        }
                                    }
                                }
                            } catch (IOException e2) {
                                dVar.close();
                                throw new e("AUDIO_PCM_OUTPUT_UNAVAILABLE", e2);
                            }
                        }
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public boolean t2(String str, int i2) throws IOException {
        if (str == null || !str.matches("[0-9a-fA-F:.%a-zA-Z0-9_-]+") || i2 < 1 || i2 > 65535) {
            SyntheticRefs.c("invalid startup endpoint");
            return false;
        }
        int iIndexOf = str.indexOf(37);
        String strSubstring = iIndexOf >= 0 ? str.substring(0, iIndexOf) : str;
        if (!strSubstring.matches("[0-9a-fA-F:.]+") || (strSubstring.indexOf(58) < 0 && !strSubstring.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}"))) {
            SyntheticRefs.c("non-numeric startup endpoint");
            return false;
        }
        InetAddress byName = InetAddress.getByName(str);
        if (byName.isAnyLocalAddress() || byName.isLoopbackAddress() || byName.isMulticastAddress()) {
            SyntheticRefs.c("invalid startup endpoint address");
            return false;
        }
        synchronized (this.f) {
            try {
                if (!U()) {
                    return false;
                }
                if (this.c.equals(byName) && this.d == i2) {
                    return true;
                }
                this.c = byName;
                this.d = i2;
                this.h++;
                String hostAddress = byName.getHostAddress();
                int iIndexOf2 = hostAddress.indexOf(37);
                if (iIndexOf2 >= 0) {
                    hostAddress = hostAddress.substring(0, iIndexOf2);
                }
                if (hostAddress.indexOf(58) >= 0) {
                    hostAddress = "[" + hostAddress + "]";
                }
                this.e = "rtsp://" + hostAddress + ":" + this.d + "/" + Long.toUnsignedString(E1());
                g0(this.d0);
                this.n.a("CARPLAY_START_ENDPOINT_UPDATED before_protocol=true values_logged=false");
                return true;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    
    public void u1(byte[] bArr, long j2) throws IOException {
        r0();
        try {
            synchronized (this.L) {
                VideoCipher.AvcVideoInput bVar = this.N;
                j0 j0Var = new j0(7);
                j0Var.b = this;
                VarHandle.storeStoreFence();
                bVar.b(bArr, j0Var);
            }
        } catch (IOException e2) {
            if (Iap2Bootstrap.l(e2)) {
                v0("VIDEO_ENQUEUE_".concat(e2.getClass().getSimpleName()), e2);
            }
            throw e2;
        } catch (Exception e3) {
            throw new IOException("AVC assembly failed", e3);
        }
    }

    public boolean v1(io.carplay.app.secured.AvcAccessUnit hVar) throws IOException {
        r0();
        Objects.requireNonNull(hVar);
        try {
            synchronized (this.L) {
                try {
                    long j2 = hVar.j();
                    long j3 = this.A0;
                    if (j2 < j3) {
                        return false;
                    }
                    if (j3 >= 0 && hVar.j() > this.A0) {
                        synchronized (this.M) {
                            r0();
                            this.C.e(this.w0 + 1, this.y.get().i());
                            this.w0++;
                            this.K.clear();
                            this.n.a("VIDEO_EPOCH_WAITING_IDR=" + this.w0);
                        }
                    }
                    this.A0 = hVar.j();
                    q qVar = new q((List) hVar.g(), hVar.l(), hVar.i(), hVar.c(), this.w0, 0);
                    if (qVar.l() != null && qVar.j() != null) {
                        this.v0 = new g(qVar.l(), qVar.j());
                    }
                    p0(qVar);
                    return true;
                } catch (Throwable th) {
                    throw th;
                }
            }
        } catch (IOException e2) {
            if (!Iap2Bootstrap.l(e2)) {
                throw e2;
            }
            v0("VIDEO_ENQUEUE_".concat(e2.getClass().getSimpleName()), e2);
            throw e2;
        } catch (InterruptedException e3) {
            Thread.currentThread().interrupt();
            throw new IOException("VIDEO_QUEUE_INTERRUPTED", e3);
        }
    }

    public List<Map<String, Object>> z0() throws IOException {
        List<Map<String, Object>> listCopyOf;
        Integer numValueOf = Integer.valueOf(ControlChannel.MODE);
        synchronized (this.U0) {
            try {
                ArrayList arrayList = new ArrayList();
                Iterator<p> it = this.I0.values().iterator();
                while (it.hasNext()) {
                    Object obj = it.next().a;
                    if (obj != null) {
                        arrayList.add(Map.of("type", "hidSetInputMode", "params", Map.of("hidInputMode", numValueOf, "uuid", obj)));
                    }
                }
                Iterator<h> it2 = this.J0.values().iterator();
                while (it2.hasNext()) {
                    Object obj2 = it2.next().a;
                    if (obj2 != null) {
                        arrayList.add(Map.of("type", "hidSetInputMode", "params", Map.of("hidInputMode", numValueOf, "uuid", obj2)));
                    }
                }
                Iterator<i> it3 = this.K0.values().iterator();
                while (it3.hasNext()) {
                    Object obj3 = it3.next().a;
                    if (obj3 != null) {
                        arrayList.add(Map.of("type", "hidSetInputMode", "params", Map.of("hidInputMode", numValueOf, "uuid", obj3)));
                    }
                }
                ArrayList arrayList2 = ControlChannel.PEND;
                if (arrayList2 != null) {
                    Iterator iDisplaySession = arrayList2.iterator();
                    while (iDisplaySession.hasNext()) {
                        Object next = iDisplaySession.next();
                        if (this.I0.containsKey(next) || this.J0.containsKey(next) || this.K0.containsKey(next)) {
                            iDisplaySession.remove();
                        }
                    }
                }
                ArrayList arrayList3 = ControlChannel.PEND;
                if (arrayList3 != null) {
                    for (Object obj4 : arrayList3) {
                        if (obj4 != null) {
                            arrayList.add(Map.of("type", "hidSetInputMode", "params", Map.of("hidInputMode", numValueOf, "uuid", obj4)));
                        }
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append("HID_PEND_REPLAY total=");
                sb.append(arrayList.size());
                sb.append(" pend=");
                ArrayList arrayLisDisplaySession = ControlChannel.PEND;
                sb.append(arrayLisDisplaySession != null ? arrayLisDisplaySession.size() : -1);
                Log.i("CarPlayHID", sb.toString());
                listCopyOf = List.copyOf(arrayList);
            } catch (Throwable th) {
                throw th;
            }
        }
        return listCopyOf;
    }

    /**
     * 扩展实现：在 IOException 基础上补充 AirPlaySession 需要的局部行为。
     *
     * Extension of IOException adding the local behaviour AirPlaySession needs.
     */
    public static final class e extends IOException {
        private static final long a = 1;

        public e(String str, Throwable th) {
            super(str, th);
        }

        public e(String str) {
            super(str);
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class c extends ProtoMessageBase {
        private final long a;

        private c(long j) {
            this.a = j;
        }

        public final boolean equals(Object obj) {
            return (obj instanceof c) && this.a == ((c) obj).a;
        }

        public long g() {
            return this.a;
        }

        public final int hashCode() {
            return Long.hashCode(this.a);
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a)};
            String[] strArrSplit = "a".length() == 0 ? new String[0] : "a".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(c.class, sb, "[");
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

        public  c(long j, int i) {
            this(j);
        }
    }

    /**
     * 扩展实现：在 IOException 基础上补充 AirPlaySession 需要的局部行为。
     *
     * Extension of IOException adding the local behaviour AirPlaySession needs.
     */
    public static final class m extends IOException {
        private static final long a = 1;

        private m(IOException iOException) {
            super("INITIAL_CONTROL_CONNECT_FAILED", iOException);
        }

        public  m(IOException iOException, int i) {
            this(iOException);
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class f extends ProtoMessageBase {
        private final boolean a;
        private final long b;

        
        public  f(int i, long j, boolean z) {
            this(z, j);
        }

        private f(boolean z, long j) {
            this.a = z;
            this.b = j;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof f)) {
                return false;
            }
            f fVar = (f) obj;
            return this.a == fVar.a && this.b == fVar.b;
        }

        public boolean g() {
            return this.a;
        }

        public long h() {
            return this.b;
        }

        public final int hashCode() {
            boolean z = this.a;
            long j = this.b;
            return Long.hashCode(j) + (Boolean.hashCode(z) * 31);
        }

        public final String toString() {
            Object[] objArr = {Boolean.valueOf(this.a), Long.valueOf(this.b)};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
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
    }

    /**
     * 可关闭资源：持有需要显式释放的通道或缓冲区。
     *
     * Closeable resource holding a channel or buffer that must be released.
     */
    public static final class d implements AutoCloseable {
        long A;
        long B;
        long C;
        long D;
        long E;
        long F;
        long G;
        long H;
        long I;
        long J;
        long K;
        final int a;
        final int b;
        final int c;
        final int d;
        final int e;
        final int f;
        final int g;
        final int h;
        final DatagramSocket i;
        final DatagramSocket j;
        final VideoCipher.AacPacket k;
        final PcmConverter l;
        final PcmQueue m;
        final io.carplay.app.secured.AudioPacer n;
        final t o;
        final boolean p;
        long q;
        final Object r;
        long s;
        volatile Thread t;
        long u;
        long v;
        long w;
        boolean x;
        long y;
        long z;

        public d(AudioCapabilities.d dVar, int i, int i2, int i3, DatagramSocket datagramSocket, DatagramSocket datagramSocket2, VideoCipher.AacPacket aVar, t tVar) throws IOException {
            this.r = new Object();
            this.w = -1L;
            this.I = System.nanoTime();
            int iN = dVar.n();
            this.a = iN;
            int iL = dVar.l();
            this.b = iL;
            this.c = dVar.k();
            this.g = dVar.p();
            this.h = dVar.o();
            this.o = tVar;
            this.p = dVar.m();
            this.d = i;
            this.e = i2;
            this.f = i3;
            this.i = datagramSocket;
            this.j = datagramSocket2;
            this.k = aVar;
            this.l = new PcmConverter(i, i2, iN, iL);
            this.m = new PcmQueue(iN, iL, 0L, dVar.o());
            this.n = new io.carplay.app.secured.AudioPacer(iN);
        }

        public boolean b(int i, int i2, int i3) {
            return this.d == i && this.e == i2 && this.f == i3 && !this.i.isClosed();
        }

        @Override 
        public void close() {
            this.i.close();
            this.j.close();
            this.m.close();
            Thread thread = this.t;
            if (thread != null && thread != Thread.currentThread()) {
                thread.interrupt();
            }
            synchronized (this.r) {
                synchronized (this) {
                    try {
                        this.l.close();
                        this.k.close();
                        t tVar = this.o;
                        if (tVar != null) {
                            tVar.close();
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            }
        }

        public d(AudioCapabilities.d dVar, int i, int i2, int i3, DatagramSocket datagramSocket, DatagramSocket datagramSocket2, VideoCipher.AacPacket aVar) throws IOException {
            this(dVar, i, i2, i3, datagramSocket, datagramSocket2, aVar, null);
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class o extends ProtoMessageBase {
        private final boolean a;
        private final long b;
        private final long c;

        private o(boolean z, long j, long j2) {
            this.a = z;
            this.b = j;
            this.c = j2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof o)) {
                return false;
            }
            o oVar = (o) obj;
            return this.a == oVar.a && this.b == oVar.b && this.c == oVar.c;
        }

        public boolean g() {
            return this.a;
        }

        public long h() {
            return this.c;
        }

        public final int hashCode() {
            boolean z = this.a;
            long j = this.b;
            long j2 = this.c;
            return Long.hashCode(j2) + ProtoMessageBase.a(j, Boolean.hashCode(z) * 31, 31);
        }

        public long i() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {Boolean.valueOf(this.a), Long.valueOf(this.b), Long.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(o.class, sb, "[");
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

        public  o(boolean z, long j, long j2, int i) {
            this(z, j, j2);
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class q extends ProtoMessageBase {
        private final List<byte[]> a;
        private final byte[] b;
        private final byte[] c;
        private final long d;
        private final long e;
        private final long f;

        private q(List<byte[]> list, byte[] bArr, byte[] bArr2, long j, long j2, long j3) {
            this.a = list;
            this.b = bArr;
            this.c = bArr2;
            this.d = j;
            this.e = j2;
            this.f = j3;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof q)) {
                return false;
            }
            q qVar = (q) obj;
            return this.d == qVar.d && this.e == qVar.e && this.f == qVar.f && Objects.equals(this.a, qVar.a) && Objects.equals(this.b, qVar.b) && Objects.equals(this.c, qVar.c);
        }

        public long g() {
            return this.d;
        }

        public long h() {
            return this.e;
        }

        public final int hashCode() {
            long j = this.d;
            long j2 = this.e;
            long j3 = this.f;
            List<byte[]> list = this.a;
            byte[] bArr = this.b;
            byte[] bArr2 = this.c;
            return Objects.hashCode(bArr2) + ((Objects.hashCode(bArr) + ((Objects.hashCode(list) + ProtoMessageBase.a(j3, ProtoMessageBase.a(j2, Long.hashCode(j) * 31, 31), 31)) * 31)) * 31);
        }

        public List<byte[]> i() {
            return this.a;
        }

        public byte[] j() {
            return this.c;
        }

        public long k() {
            return this.f;
        }

        public byte[] l() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, this.c, Long.valueOf(this.d), Long.valueOf(this.e), Long.valueOf(this.f)};
            String[] strArrSplit = "a;b;c;d;e;f".length() == 0 ? new String[0] : "a;b;c;d;e;f".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(q.class, sb, "[");
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

        public  q(List<byte[]> list, byte[] bArr, byte[] bArr2, long j, long j2, int i) {
            this(list, bArr, bArr2, j, j2);
        }

        private q(List<byte[]> list, byte[] bArr, byte[] bArr2, long j, long j2) {
            this(list, bArr, bArr2, j, j2, System.nanoTime());
        }
    }
}
