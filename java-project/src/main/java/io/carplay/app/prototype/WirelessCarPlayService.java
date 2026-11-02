package io.carplay.app.prototype;

import android.R;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import io.carplay.app.secured.AirPlaySession;
import io.carplay.app.secured.AudioRelay;
import io.carplay.app.secured.DerCodec;
import io.carplay.app.secured.EcdsaSignature;
import io.carplay.app.secured.Iap2Csm;
import io.carplay.app.secured.MfiCertificateVerifier;
import io.carplay.app.secured.MfiSap;
import io.carplay.app.secured.PairingTlv8;
import io.carplay.app.secured.StagedPeer;
import io.carplay.app.secured.SyntheticRefs;
import io.carplay.app.secured.a4;
import io.carplay.app.secured.a7;
import io.carplay.app.secured.b5;
import io.carplay.app.secured.b7;
import io.carplay.app.secured.c0;
import io.carplay.app.secured.d7;
import io.carplay.app.secured.e7;
import io.carplay.app.secured.f1;
import io.carplay.app.secured.f7;
import io.carplay.app.secured.g7;
import io.carplay.app.secured.h5;
import io.carplay.app.secured.h7;
import io.carplay.app.secured.i5;
import io.carplay.app.secured.i6;
import io.carplay.app.secured.i7;
import io.carplay.app.secured.j6;
import io.carplay.app.secured.k1;
import io.carplay.app.secured.m1;
import io.carplay.app.secured.p6;
import io.carplay.app.secured.q6;
import io.carplay.app.secured.r6;
import io.carplay.app.secured.t4;
import io.carplay.app.secured.t5;
import io.carplay.app.secured.u4;
import io.carplay.app.secured.u5;
import io.carplay.app.secured.w1;
import io.carplay.app.secured.w4;
import io.carplay.app.secured.z;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/* JADX INFO: loaded from: classes.dex */
public final class WirelessCarPlayService extends Service {
    public static final String V0 = "io.carplay.app.prototype.WIRELESS_START";
    public static final String W0 = "io.carplay.app.prototype.WIRELESS_STOP";
    public static final String X0 = "io.carplay.app.prototype.WIRELESS_SOURCE";
    public static final String Y0 = "io.carplay.app.prototype.RETURN_TO_CAR";
    public static final String Z0 = "io.carplay.app.prototype.RESUME_UI";
    public static final String a1 = "io.carplay.app.prototype.RESUME_AUDIO";
    public static final String b1 = "wireless-session-status";
    public static final String c1 = "wireless-certificate-trust";
    private static final String d1 = "wireless-carplay-session";
    private static final int e1 = 74;
    private static volatile boolean f1;
    private static volatile boolean g1;
    private static volatile boolean h1;
    private static volatile l k1;
    private static volatile Iap2Csm.g l1;
    private String A;
    private volatile q6 A0;
    private PairingSession B;
    private k B0;
    private Network C;
    private k C0;
    private volatile AirPlaySession D;
    private Iap2Csm.a E;
    private volatile boolean E0;
    private String F;
    private q6 F0;
    private t4 G;
    private int G0;
    private AirPlaySession H;
    private int H0;
    private boolean I;
    private volatile long J0;
    private volatile AirPlaySession.g K;
    private long L;
    private boolean L0;
    private long M;
    private boolean M0;
    private long N;
    private long O;
    private boolean P0;
    private long Q;
    private int Q0;
    private long R;
    private int R0;
    private long S;
    private long T;
    private long U;
    private Runnable Z;
    private i5.a a;
    private Runnable a0;
    private boolean b0;
    private volatile boolean c0;
    private String d;
    private String e;
    private boolean f;
    private String g;
    private String i;
    private Network j;
    private long j0;
    private boolean k;
    private boolean k0;
    private WifiNetworkMonitor l;
    private boolean m;
    private String n;
    private long n0;
    private String o;
    private boolean p;
    private j q;
    private j6 q0;
    private boolean r;
    private i s0;
    private PowerManager.WakeLock v;
    private AirPlaySession w0;
    private volatile boolean x0;
    private volatile AvcEncoder z0;
    static final Object U0 = new Object();
    private static final i5.c i1 = new i5.c(new c0(1));
    private static final Object j1 = new Object();
    private long b = -1;
    private long c = -1;
    private String h = "NO_INPUT_NORMAL_DEADLINE";
    private final Object s = new Object();
    private final AtomicBoolean t = new AtomicBoolean();
    private final Object u = new Object();
    private final Handler w = new Handler(Looper.getMainLooper());
    private final ExecutorService x = Executors.newFixedThreadPool(3, new io.carplay.app.secured.n0(6));
    private final ExecutorService y = Executors.newSingleThreadExecutor(new io.carplay.app.secured.n0(7));
    private final StringBuilder z = new StringBuilder();
    private final u5 J = new u5();
    private final p6 P = new p6();
    private final Object V = new Object();
    private final Map<r6.a, Object> W = r1();
    private final EnumMap<r6.a, g> X = new EnumMap<>(r6.a.class);
    private final AudioRelay.a Y = new AudioRelay.a();
    private volatile int d0 = 1280;
    private volatile int e0 = 720;
    private volatile int f0 = 1280;
    private volatile int g0 = 720;
    private volatile int h0 = 30;
    private volatile boolean i0 = true;
    private long l0 = -1;
    private long m0 = -1;
    private long o0 = -1;
    private long p0 = -1;
    private long r0 = -1;
    private final Object t0 = new Object();
    private final AtomicBoolean u0 = new AtomicBoolean();
    private final AtomicLong v0 = new AtomicLong();
    private volatile int y0 = 30;
    private final AtomicBoolean D0 = new AtomicBoolean();
    private final Object I0 = new Object();
    private boolean K0 = true;
    private final w4 N0 = new w4();
    private final Object O0 = new Object();
    private final AtomicBoolean S0 = new AtomicBoolean();
    private final AtomicBoolean T0 = new AtomicBoolean();

    public class a implements PairingSession.k {
        final /* synthetic */ long a;
        final /* synthetic */ boolean b;
        final /* synthetic */ boolean c;
        final /* synthetic */ WirelessCarPlayService d;

        public a(WirelessCarPlayService wirelessCarPlayService, long j, boolean z, boolean z2) {
            this.a = j;
            this.b = z;
            this.c = z2;
            this.d = wirelessCarPlayService;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void w(Network network, Iap2Csm.a aVar, String str, long j, PairingSession.i iVar) {
            this.d.z1(network, aVar, str, j, iVar);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void x(Network network, Iap2Csm.a aVar, String str, long j) {
            this.d.y1(network, aVar, str, j);
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void a(String str) {
            synchronized (this.d.s) {
                try {
                    if (this.d.h1(this.a)) {
                        if (str.startsWith("IAP2_PEER_CAPABILITIES ")) {
                            this.d.b = -1L;
                        }
                        if (str.startsWith("IAP2_PHONE_IDENTIFICATION snapshot_shared=true ")) {
                            this.d.b = this.a;
                        }
                        this.d.i1(str);
                        if (str.equals("WIFI_NETWORK_READY_WAITING_CARPLAY_ENDPOINT")) {
                            WirelessCarPlayService.h1 = true;
                        }
                        if (!this.d.c0 && str.contains("RFCOMM_LISTENING")) {
                            this.d.a3("BLUETOOTH", "Waiting for selected receiver Bluetooth connection");
                        }
                        if (!this.d.c0 && str.equals("IAP2_AUTHENTICATED_WAITING_CARPLAY_START")) {
                            this.d.a3("WAITING_ACCESSORY_START", "Accessory proof verified; waiting for CarPlay start parameters");
                        }
                        if (!this.d.c0 && str.startsWith("IAP2_DETECT_RESTART attempt=")) {
                            this.d.a3("PAIRING", "Receiver restarted iAP2 detection; replacing the bootstrap link with fresh authentication");
                        }
                        if (!this.d.c0 && str.equals("RFCOMM_PHONE_DISCOVERY_FALLBACK_READY")) {
                            this.d.a3("BLUETOOTH_DISCOVERY_RECOVERY", "Preparing phone service records after selected receiver connection failures");
                        }
                        if (!this.d.c0 && (str.contains("WIFI_REQUEST") || str.contains("WIFI_NETWORK_REQUEST"))) {
                            this.d.a3("WIFI", "Connecting to authenticated receiver Wi-Fi; allow system authorization if prompted");
                        }
                        if (!this.d.c0 && str.equals("WIFI_NETWORK_READY_WAITING_CARPLAY_ENDPOINT")) {
                            this.d.a3("WAITING_CARPLAY_ENDPOINT", "Car Wi-Fi acquired; waiting for authenticated CarPlay endpoint");
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void b(String str) {
            synchronized (this.d.s) {
                try {
                    if (this.d.h1(this.a)) {
                        long jE = this.d.J.e();
                        this.d.C = null;
                        this.d.E = null;
                        this.d.F = null;
                        WirelessCarPlayService.l1 = null;
                        WirelessCarPlayService.h1 = false;
                        boolean zK = u5.k(str);
                        WirelessCarPlayService wirelessCarPlayService = this.d;
                        if (zK) {
                            wirelessCarPlayService.y2(w1.e("BOOTSTRAP_", str), jE, this.a);
                        } else {
                            wirelessCarPlayService.d3(this.a, jE, w1.e("Wireless bootstrap stopped: ", str));
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public i5 c() {
            return this.d.q2(this.a);
        }

        /* JADX WARN: Code duplicated, block: B:20:0x004a  */
        @Override // io.carplay.app.prototype.PairingSession.k
        public boolean d() {
            boolean z;
            synchronized (this.d.s) {
                try {
                    if (this.d.h1(this.a)) {
                        WirelessCarPlayService wirelessCarPlayService = this.d;
                        if (!wirelessCarPlayService.g1(wirelessCarPlayService.J.e()) || this.d.C == null || this.d.I || !(this.d.H == null || this.d.H.U())) {
                            z = false;
                        } else {
                            z = true;
                        }
                    } else {
                        z = false;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            return z;
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void e(Network network, Iap2Csm.a aVar, String str, String str2) {
            synchronized (this.d.s) {
                try {
                    if (this.d.h1(this.a)) {
                        long jE = this.d.J.e();
                        if (this.d.g1(jE)) {
                            this.d.C = network;
                            this.d.E = aVar;
                            this.d.F = str;
                            this.d.i = str2;
                            WirelessCarPlayService.h1 = true;
                            this.d.a3("PAIRING", "Selected Wi-Fi network acquired; beginning receiver authentication");
                            try {
                                ExecutorService executorService = this.d.x;
                                t0 t0Var = new t0(0);
                                t0Var.f = this;
                                t0Var.b = network;
                                t0Var.c = aVar;
                                t0Var.d = str;
                                t0Var.e = jE;
                                VarHandle.storeStoreFence();
                                executorService.execute(t0Var);
                            } catch (RejectedExecutionException unused) {
                            }
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void f(Network network, Iap2Csm.a aVar, String str, PairingSession.i iVar, String str2) {
            synchronized (this.d.s) {
                try {
                    if (this.d.h1(this.a)) {
                        long jE = this.d.J.e();
                        if (this.d.g1(jE)) {
                            this.d.C = null;
                            this.d.E = null;
                            this.d.F = null;
                            this.d.i = str2;
                            WirelessCarPlayService.h1 = false;
                            this.d.a3("PAIRING", "Checking the existing Wi-Fi using full receiver authentication");
                            try {
                                ExecutorService executorService = this.d.x;
                                s0 s0Var = new s0();
                                s0Var.a = this;
                                s0Var.b = network;
                                s0Var.c = aVar;
                                s0Var.d = str;
                                s0Var.e = jE;
                                s0Var.f = iVar;
                                VarHandle.storeStoreFence();
                                executorService.execute(s0Var);
                            } catch (RejectedExecutionException unused) {
                            }
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void g(String str, MfiCertificateVerifier.DetectionState aVar, PairingSession.k.a aVar2) {
            this.d.m1(this.a, str, aVar, aVar2);
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void h(String str, MfiCertificateVerifier.InputFlags bVar) {
            this.d.n1(this.a, str, bVar);
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public boolean i() {
            return this.c;
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void j(Network network, Iap2Csm.a aVar, String str) {
            synchronized (this.d.s) {
                try {
                    if (d() && network.equals(this.d.C)) {
                        long jE = this.d.J.e();
                        AirPlaySession airPlaySession = this.d.H;
                        if (airPlaySession == null) {
                            this.d.E = aVar;
                            this.d.F = str;
                            this.d.i1("CARPLAY_ENDPOINT_UPDATE_PENDING before_protocol=true");
                            return;
                        }
                        try {
                            InetSocketAddress inetSocketAddressX1 = PairingSession.x1(this.d, network, aVar, str);
                            if (airPlaySession.t2(inetSocketAddressX1.getAddress().getHostAddress(), inetSocketAddressX1.getPort())) {
                                synchronized (this.d.s) {
                                    try {
                                        if (this.d.g1(jE) && this.d.h1(this.a) && this.d.H == airPlaySession) {
                                            this.d.E = aVar;
                                            this.d.F = str;
                                        }
                                    } catch (Throwable th) {
                                        throw th;
                                    }
                                }
                            }
                        } catch (IOException unused) {
                            this.d.i1("CARPLAY_ENDPOINT_UPDATE_REJECTED values_logged=false");
                        }
                    }
                } catch (Throwable th2) {
                    throw th2;
                }
            }
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public boolean k() {
            return this.d.getSharedPreferences("wireless-ui", 0).getBoolean("enhanced_discovery_" + WirelessCarPlayService.r2(this.d.A), true);
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void l(String str) {
            WirelessCarPlayService wirelessCarPlayService = this.d;
            wirelessCarPlayService.p1(this.a, wirelessCarPlayService.c, str);
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void m(Network network, Iap2Csm.a aVar, String str) {
            e(network, aVar, str, null);
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void n(Network network, Iap2Csm.a aVar) {
            m(network, aVar, null);
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void o(String str, byte[] bArr) {
            synchronized (this.d.s) {
                synchronized (WirelessCarPlayService.U0) {
                    try {
                        if (!this.d.h1(this.a)) {
                            throw new IOException("MFI_FIRST_USE_CANCELLED");
                        }
                        try {
                            String strJ1 = WirelessCarPlayService.J1(MessageDigest.getInstance("SHA-256").digest(bArr));
                            if (!strJ1.equalsIgnoreCase(str)) {
                                throw new IOException("MFI_FIRST_USE_CERTIFICATE_HASH_MISMATCH");
                            }
                            SharedPreferences sharedPreferences = this.d.getSharedPreferences(WirelessCarPlayService.c1, 0);
                            String strR2 = WirelessCarPlayService.r2(this.d.A);
                            String string = sharedPreferences.getString(strR2, null);
                            if (string != null && !string.equalsIgnoreCase(strJ1)) {
                                throw new IOException("MFI_KNOWN_CERTIFICATE_CHANGED_CONFIRMATION_REQUIRED");
                            }
                            if (string == null) {
                                SharedPreferences.Editor editorPutString = sharedPreferences.edit().putString(strR2, strJ1);
                                if (this.d.A.equals(sharedPreferences.getString("pending_address", null)) && strJ1.equalsIgnoreCase(sharedPreferences.getString("pending_fingerprint", ""))) {
                                    editorPutString.remove("pending_address").remove("pending_fingerprint").remove("pending_certificate");
                                }
                                if (!editorPutString.commit()) {
                                    throw new IOException("MFI_FIRST_USE_TRUST_SAVE_FAILED");
                                }
                            }
                            this.d.i1("MFI_SELECTED_BONDED_PEER_FIRST_USE_PROOF_VERIFIED_TRUST_SAVED");
                        } catch (NoSuchAlgorithmException e) {
                            throw new IOException("MFI_FIRST_USE_CERTIFICATE_HASH_UNAVAILABLE", e);
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            }
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public boolean p() {
            return this.b;
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void q(String str, MfiCertificateVerifier.e eVar) {
            this.d.o1(this.a, str, eVar);
            this.d.q1(this.a, str, eVar);
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void r(String str, byte[] bArr) {
            try {
                synchronized (this.d.s) {
                    synchronized (WirelessCarPlayService.U0) {
                        if (this.d.h1(this.a)) {
                            String strJ1 = WirelessCarPlayService.J1(MessageDigest.getInstance("SHA-256").digest(bArr));
                            if (!strJ1.equalsIgnoreCase(str)) {
                                throw new IOException("certificate fingerprint mismatch");
                            }
                            if (!this.d.getSharedPreferences(WirelessCarPlayService.c1, 0).edit().putString("pending_address", this.d.A).putString("pending_fingerprint", strJ1).putString("pending_certificate", Base64.encodeToString(bArr, 2)).remove("pending_current_wifi").commit()) {
                                throw new IOException("pending trust persistence failed");
                            }
                            this.d.a3("TRUST_REQUIRED", "Open this app to inspect selected receiver certificate fingerprint. Explicit approval and a new start are required.");
                            if (this.d.J.i(this.a, this.d.J.e())) {
                                this.d.e3("Certificate confirmation required; no trust saved and no automatic reconnect");
                                ((NotificationManager) this.d.getSystemService(NotificationManager.class)).notify(76, new Notification.Builder(this.d, WirelessCarPlayService.d1).setSmallIcon(android.R.drawable.stat_notify_sync).setContentTitle("核对接收端证书后手动重试").setContentText("打开应用查看所选设备指纹；尚未保存信任").setContentIntent(PendingIntent.getActivity(this.d, 76, new Intent(this.d, (Class<?>) MainActivity.class).addFlags(536870912), 201326592)).setAutoCancel(true).build());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                WirelessCarPlayService wirelessCarPlayService = this.d;
                wirelessCarPlayService.d3(this.a, wirelessCarPlayService.J.e(), "Certificate trust request invalid: ".concat(e.getClass().getSimpleName()));
            }
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void s(Network network, Iap2Csm.a aVar, String str, PairingSession.i iVar) {
            f(network, aVar, str, iVar, null);
        }

        @Override // io.carplay.app.prototype.PairingSession.k
        public void t(Iap2Csm.g gVar) {
            synchronized (this.d.s) {
                try {
                    if (this.d.h1(this.a)) {
                        WirelessCarPlayService.l1 = gVar;
                        this.d.i1("WIFI_AUTHENTICATED_TARGET ssid=" + gVar.b() + " security=" + gVar.b + " passphrase_present=" + (!gVar.c().isEmpty()));
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public class b implements WifiNetworkMonitor.d {
        final /* synthetic */ long a;
        final /* synthetic */ long b;
        final /* synthetic */ WirelessCarPlayService c;

        public b(WirelessCarPlayService wirelessCarPlayService, long j, long j2) {
            this.a = j;
            this.b = j2;
            this.c = wirelessCarPlayService;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void d(Network network, Iap2Csm.a aVar, String str, long j) throws Throwable {
            this.c.y1(network, aVar, str, j);
        }

        @Override // io.carplay.app.prototype.WifiNetworkMonitor.d
        public void a(String str) {
            synchronized (this.c.s) {
                try {
                    if (this.c.h1(this.a)) {
                        this.c.i1(str);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override // io.carplay.app.prototype.WifiNetworkMonitor.d
        public void b(String str) {
            synchronized (this.c.s) {
                try {
                    if (this.c.h1(this.a)) {
                        if (this.c.k) {
                            WirelessCarPlayService.H1(this.c.j);
                        }
                        WirelessCarPlayService wirelessCarPlayService = this.c;
                        wirelessCarPlayService.d3(this.a, wirelessCarPlayService.J.e(), str);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override // io.carplay.app.prototype.WifiNetworkMonitor.d
        public void e(Network network, Iap2Csm.a aVar, String str, String str2) {
            synchronized (this.c.s) {
                if (this.c.h1(this.a) && this.c.g1(this.b) && network.equals(this.c.j)) {
                    this.c.C = network;
                    this.c.E = aVar;
                    this.c.F = str;
                    this.c.i = str2;
                    int i = 1;
                    WirelessCarPlayService.h1 = true;
                    this.c.a3("WIFI", "Selected current WLAN candidate found; full pairing and certificate authentication required");
                    try {
                        ExecutorService executorService = this.c.x;
                        long j = this.b;
                        t0 t0Var = new t0(i);
                        t0Var.f = this;
                        t0Var.b = network;
                        t0Var.c = aVar;
                        t0Var.d = str;
                        t0Var.e = j;
                        VarHandle.storeStoreFence();
                        executorService.execute(t0Var);
                    } catch (RejectedExecutionException unused) {
                    }
                }
            }
        }
    }

    public class c implements AirPlaySession.n {
        final /* synthetic */ long a;
        final /* synthetic */ WirelessCarPlayService b;

        public c(WirelessCarPlayService wirelessCarPlayService, long j) {
            this.a = j;
            this.b = wirelessCarPlayService;
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public void a(String str) {
            String str2;
            Log.d("CarPlusMetrics", str);
            synchronized (this.b.s) {
                try {
                    if (this.b.g1(this.a)) {
                        this.b.i1(str);
                        if (str.startsWith("VIDEO_EPOCH_WAITING_IDR=")) {
                            this.b.J.n(this.a);
                            this.b.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().remove("measured_sent_fps_x100").remove("measured_sent_fps_at_ms").apply();
                        }
                        this.b.G2(str);
                        if (str.startsWith("VIDEO_PROGRESS units=")) {
                            this.b.J.m(this.a, SystemClock.elapsedRealtime());
                        }
                        if (str.startsWith("VIDEO_EPOCH_FIRST_IDR_SENT=")) {
                            this.b.J.m(this.a, SystemClock.elapsedRealtime());
                            if (this.b.O != 0) {
                                p6 p6Var = this.b.P;
                                p6.b bVar = p6.b.RECOVERY_WAIT;
                                p6Var.d(bVar, this.b.O, System.nanoTime());
                                this.b.O = 0L;
                                p6.c cVar = this.b.P.e().get(bVar);
                                if (cVar != null) {
                                    this.b.i1("RECOVERY_WAIT count=" + cVar.g() + " p50_ms=" + (cVar.i() / 1000000) + " p95_ms=" + (cVar.j() / 1000000) + " receiver_render_confirmed=false");
                                }
                            }
                            this.b.a3("FORWARDING_NATIVE", "Actual source-derived IDR sent on authenticated transport; receiver display still requires confirmation");
                        }
                        if (str.equals("CARPLAY_SCREEN_OWNER_PHONE=false") && this.b.c0) {
                            this.b.a3("CAR_SCREEN_IN_USE", "Accessory has the screen resource; native source retained");
                        }
                        if (str.equals("CARPLAY_SCREEN_OWNER_PHONE=true") && this.b.c0) {
                            this.b.a3("RESUMING_NATIVE", "Screen resource returned; waiting for an actual IDR");
                        }
                        if (str.startsWith("AUDIO_MAIN_MODES_PENDING")) {
                            this.b.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("audio_state", "negotiating").apply();
                        }
                        if (str.startsWith("AUDIO_MAIN_MODES_CONFIRMED")) {
                            this.b.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("audio_state", "ready").apply();
                        }
                        if (str.startsWith("AUDIO_READY ")) {
                            SharedPreferences.Editor editorEdit = this.b.getSharedPreferences(WirelessCarPlayService.b1, 0).edit();
                            if (str.contains("modes_pending=true")) {
                                str2 = "negotiating";
                            } else {
                                str2 = ((str.contains("stream=100 ") || str.contains("stream=102 ")) && str.contains("main_owned=false")) ? "needs_user" : "ready";
                            }
                            editorEdit.putString("audio_state", str2).apply();
                        }
                        if (str.startsWith("AUDIO_RESOURCE_NOT_OWNED") || str.startsWith("AUDIO_USER_REQUEST_REJECTED")) {
                            this.b.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("audio_state", "needs_user").apply();
                        }
                        if (str.startsWith("AUDIO_USER_REQUEST_PENDING")) {
                            this.b.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("audio_state", "negotiating").apply();
                        }
                        if (str.startsWith("AUDIO_USER_REQUEST_ALREADY_OWNED")) {
                            this.b.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("audio_state", "ready").apply();
                        }
                        if (str.startsWith("AUDIO_DECLARATION_REJECTED")) {
                            this.b.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("audio_state", "unavailable").apply();
                        }
                        if (str.startsWith("AUDIO_ROUTE_UNAVAILABLE") || str.startsWith("AUDIO_SENDER_UNAVAILABLE")) {
                            this.b.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("audio_state", "unavailable").apply();
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public void b(String str) {
            Log.d("CarPlusMetrics", str);
            if (this.b.g1(this.a)) {
                this.b.x2(str, this.a);
            }
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public Boolean e() {
            synchronized (this.b.s) {
                try {
                    if (this.b.g1(this.a)) {
                        return this.b.E1();
                    }
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public void f() {
            this.b.w2(this.a);
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public z.a g(io.carplay.app.secured.x xVar) {
            return this.b.D2(xVar);
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public boolean h(boolean z) {
            return this.b.k1(this.a, new m1.c(z));
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public boolean i() {
            t4 t4Var;
            boolean z = false;
            if (!this.b.g1(this.a)) {
                return false;
            }
            AvcEncoder avcEncoder = this.b.z0;
            if (avcEncoder != null) {
                return avcEncoder.F();
            }
            synchronized (this.b.s) {
                t4Var = this.b.G;
            }
            if (!this.b.t.get() && t4Var != null && t4Var.z()) {
                z = true;
            }
            if (!z) {
                this.b.i1("Remote force-keyframe request not implemented/ready; no success acknowledgement");
            }
            return z;
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public void j(String str, boolean z) {
            Log.d("CarPlusMetrics", str);
            WirelessCarPlayService wirelessCarPlayService = this.b;
            long j = this.a;
            if (z) {
                wirelessCarPlayService.x2(str, j);
                return;
            }
            wirelessCarPlayService.c3(j, "Receiver transport/protocol failure: " + str);
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public void k(io.carplay.app.secured.x xVar, io.carplay.app.secured.x.c cVar) {
            if (this.b.g1(this.a)) {
                this.b.S2(xVar, cVar);
                this.b.A2(this.a, xVar);
            }
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public boolean l() {
            t4 t4Var;
            synchronized (this.b.s) {
                t4Var = this.b.G;
            }
            return this.b.g1(this.a) && t4Var != null && t4Var.l(14);
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public boolean m(int i) {
            t4 t4Var;
            synchronized (this.b.s) {
                t4Var = this.b.G;
            }
            return this.b.g1(this.a) && t4Var != null && t4Var.l(i);
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public boolean n() {
            t4 t4Var;
            synchronized (this.b.s) {
                t4Var = this.b.G;
            }
            return this.b.g1(this.a) && t4Var != null && t4Var.l(33);
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public boolean o(m1.d dVar) {
            return this.b.k1(this.a, dVar);
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public void p(boolean z) {
            this.b.v2(z, this.a);
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public void q(io.carplay.app.secured.x xVar) {
            if (this.b.g1(this.a)) {
                this.b.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("capability_summary", xVar.O()).apply();
            }
        }

        @Override // io.carplay.app.secured.AirPlaySession.n
        public void r(int i, int i2, boolean z) {
            t4 t4Var;
            i6.TouchAction aVar;
            synchronized (this.b.s) {
                t4Var = this.b.G;
            }
            if (!this.b.g1(this.a) || t4Var == null) {
                SyntheticRefs.c("native source not ready; touch rejected");
                return;
            }
            synchronized (this.b.O0) {
                try {
                    if (!this.b.g1(this.a)) {
                        throw new IOException("retired transport touch rejected");
                    }
                    if (!z && !this.b.P0) {
                        throw new IOException("touch UP without active native gesture");
                    }
                    if (z) {
                        aVar = this.b.P0 ? i6.TouchAction.MOVE : i6.TouchAction.DOWN;
                    } else {
                        aVar = i6.TouchAction.UP;
                    }
                    t4Var.T(aVar, i, i2);
                    this.b.P0 = z;
                    this.b.Q0 = i;
                    this.b.R0 = i2;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public class d implements AvcEncoder.f {
        final /* synthetic */ long a;
        final /* synthetic */ q6 b;
        final /* synthetic */ AtomicBoolean c;
        final /* synthetic */ WirelessCarPlayService d;

        public d(WirelessCarPlayService wirelessCarPlayService, long j, q6 q6Var, AtomicBoolean atomicBoolean) {
            this.a = j;
            this.b = q6Var;
            this.c = atomicBoolean;
            this.d = wirelessCarPlayService;
        }

        @Override // io.carplay.app.prototype.AvcEncoder.f
        public void a(String str) {
            if (this.a != this.d.J0 || this.d.t.get()) {
                return;
            }
            this.d.i1(str);
        }

        @Override // io.carplay.app.prototype.AvcEncoder.f
        public void b(io.carplay.app.secured.h hVar) {
            synchronized (this.d.I0) {
                try {
                    if (this.a == this.d.J0 && !this.d.t.get()) {
                        AirPlaySession airPlaySessionF1 = this.d.F1();
                        if (airPlaySessionF1 != null) {
                            airPlaySessionF1.s2(this.b);
                        }
                        boolean z = true;
                        if (this.c.compareAndSet(false, true)) {
                            SharedPreferences.Editor editorPutString = this.d.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("fit_mode", "fitted");
                            if (!this.d.K0 || !this.d.M0) {
                                z = false;
                            }
                            editorPutString.putBoolean("fill_display", z).apply();
                            this.d.i1("AUTO_FIT_FIRST_ENCODED_UNIT");
                        }
                        this.d.U2(hVar, true);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override // io.carplay.app.prototype.AvcEncoder.f
        public void c(int i) {
            if (this.a != this.d.J0 || this.d.t.get()) {
                return;
            }
            this.d.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putInt("output_fps", i).apply();
        }

        @Override // io.carplay.app.prototype.AvcEncoder.f
        public void d(byte[] bArr, long j) {
            throw new IOException("ADAPTER_COMPLETE_AU_REQUIRED");
        }

        @Override // io.carplay.app.prototype.AvcEncoder.f
        public void e(String str) {
            synchronized (this.d.I0) {
                try {
                    if (this.a == this.d.J0 && !this.d.t.get()) {
                        AvcEncoder avcEncoder = this.d.z0;
                        this.d.z0 = null;
                        this.d.A0 = null;
                        this.d.J0++;
                        if (avcEncoder != null) {
                            avcEncoder.close();
                        }
                        this.d.i1("AUTO_FIT_FAILED_FALLBACK_NATIVE type=" + str);
                        int i = this.d.y0;
                        int i2 = this.d.h0;
                        WirelessCarPlayService wirelessCarPlayService = this.d;
                        if (i > i2) {
                            wirelessCarPlayService.E0 = true;
                            this.d.a3("VIDEO_OUTPUT_UNAVAILABLE", "Native frame rate exceeds receiver maximum and hardware adaptation unavailable; native/control/audio retained");
                            this.d.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("fit_mode", "rate_unavailable").apply();
                            return;
                        }
                        AirPlaySession airPlaySessionF1 = wirelessCarPlayService.F1();
                        AirPlaySession.g gVarI = this.d.N0.i();
                        this.d.K = gVarI;
                        if (airPlaySessionF1 != null) {
                            try {
                                airPlaySessionF1.s2(null);
                                airPlaySessionF1.W1(gVarI, this.d.d0, this.d.e0);
                            } catch (IOException e) {
                                this.d.i1("VIDEO_FALLBACK_RESET_FAILED ".concat(e.getClass().getSimpleName()));
                            }
                        }
                        if (airPlaySessionF1 != null) {
                            this.d.a3("RESUMING_NATIVE", "Automatic fit unavailable; waiting for actual native IDR on passthrough route");
                        }
                        this.d.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("fit_mode", "fallback").putInt("output_width", this.d.d0).putInt("output_height", this.d.e0).apply();
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public class e implements t4.d {
        public e() {
        }

        @Override // io.carplay.app.secured.t4.d
        public void b(j6 j6Var) {
            if (j6Var != null) {
                WirelessCarPlayService.this.N2(j6Var);
            }
        }

        @Override // io.carplay.app.secured.t4.d
        public void c(j6 j6Var) {
            if (j6Var == null) {
                return;
            }
            WirelessCarPlayService.this.N2(j6Var);
            synchronized (WirelessCarPlayService.this.s) {
                if (!WirelessCarPlayService.this.t.get() && WirelessCarPlayService.this.G == j6Var && WirelessCarPlayService.this.k0) {
                    WirelessCarPlayService.this.O2(WirelessCarPlayService.this.J.e());
                }
            }
        }

        @Override // io.carplay.app.secured.t4.d
        public void d(r6.a aVar) {
            synchronized (WirelessCarPlayService.this.W.get(aVar)) {
                synchronized (WirelessCarPlayService.this.V) {
                    if (WirelessCarPlayService.this.t.get()) {
                        return;
                    }
                    WirelessCarPlayService.this.X.remove(aVar);
                    AirPlaySession airPlaySessionF1 = WirelessCarPlayService.this.F1();
                    if (airPlaySessionF1 != null) {
                        try {
                            airPlaySessionF1.q1(aVar);
                        } catch (IOException e) {
                            if (WirelessCarPlayService.this.F1() == airPlaySessionF1 && airPlaySessionF1.G0()) {
                                throw e;
                            }
                        }
                    }
                }
            }
        }

        @Override // io.carplay.app.secured.t4.d
        public void e(int i, int i2, int i3) {
            WirelessCarPlayService.this.l1(i, i2, i3);
        }

        @Override // io.carplay.app.secured.t4.d
        public void g(r6.a aVar, int i, int i2, int i3) {
            if (WirelessCarPlayService.this.t.get()) {
                SyntheticRefs.c("wireless session cancelled");
                return;
            }
            WirelessCarPlayService.this.I2(aVar, i, i2, i3);
            WirelessCarPlayService.this.i1("Actual native audio format observed=" + String.valueOf(aVar) + " " + i + "/" + i2 + "/" + i3);
        }

        @Override // io.carplay.app.secured.t4.h
        public void h(byte[] bArr, long j) {
            if (WirelessCarPlayService.this.t.get()) {
                SyntheticRefs.c("wireless session cancelled");
            } else {
                WirelessCarPlayService.this.K2(bArr, j);
            }
        }

        @Override // io.carplay.app.secured.t4.d
        public boolean i(r6.a aVar, int i, int i2, int i3) {
            AirPlaySession airPlaySessionF1 = WirelessCarPlayService.this.F1();
            return airPlaySessionF1 == null || airPlaySessionF1.E0(aVar, i, i2, i3);
        }

        @Override // io.carplay.app.secured.t4.d
        public void j(j6 j6Var, int i) {
            WirelessCarPlayService.this.E2(j6Var, i);
        }

        @Override // io.carplay.app.secured.t4.d
        public void k(r6.a aVar, byte[] bArr, long j) {
            if (WirelessCarPlayService.this.t.get()) {
                SyntheticRefs.c("wireless session cancelled");
            } else {
                WirelessCarPlayService.this.J2(aVar, bArr, j);
            }
        }

        @Override // io.carplay.app.secured.t4.d
        public void l(j6 j6Var, j6.b bVar) {
            synchronized (WirelessCarPlayService.this.s) {
                try {
                    if (!WirelessCarPlayService.this.t.get() && WirelessCarPlayService.this.G == j6Var) {
                        j6.b bVar2 = j6.b.OBSERVED;
                        WirelessCarPlayService wirelessCarPlayService = WirelessCarPlayService.this;
                        if (bVar == bVar2) {
                            wirelessCarPlayService.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("native_foreground_state", "observed").apply();
                        } else if (wirelessCarPlayService.q0 == j6Var && WirelessCarPlayService.this.r0 == WirelessCarPlayService.this.n0 && WirelessCarPlayService.this.m0 != WirelessCarPlayService.this.j0) {
                            WirelessCarPlayService.this.getSharedPreferences(WirelessCarPlayService.b1, 0).edit().putString("native_foreground_state", bVar == j6.b.WRITTEN ? "written" : "unconfirmed").apply();
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public class f implements PairingTlv8.c {
        final /* synthetic */ SharedPreferences a;
        final /* synthetic */ WirelessCarPlayService b;

        public f(WirelessCarPlayService wirelessCarPlayService, SharedPreferences sharedPreferences) {
            this.a = sharedPreferences;
            this.b = wirelessCarPlayService;
        }

        private String a(String str) throws IOException {
            try {
                return WirelessCarPlayService.J1(MessageDigest.getInstance("SHA-256").digest(str.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                throw new IOException("peer store key unavailable", e);
            }
        }

        @Override // io.carplay.app.secured.PairingTlv8.c
        public PairingTlv8.b b(String str) {
            String string = this.a.getString(a(str), null);
            if (string == null) {
                return null;
            }
            try {
                return new PairingTlv8.b(str, Base64.decode(string, 2));
            } catch (RuntimeException e) {
                throw new IOException("stored peer identity invalid", e);
            }
        }

        @Override // io.carplay.app.secured.PairingTlv8.c
        public void c(PairingTlv8.b bVar) {
            PairingTlv8.b bVarB = b(bVar.c());
            if (bVarB != null && !MessageDigest.isEqual(bVarB.e(), bVar.e())) {
                SyntheticRefs.c("stored peer key changed; explicit unpair required");
            } else {
                if (this.a.edit().putString(a(bVar.c()), Base64.encodeToString(bVar.e(), 2)).commit()) {
                    return;
                }
                SyntheticRefs.c("authenticated peer persistence failed");
            }
        }
    }

    private boolean A1(String str) {
        synchronized (this.s) {
            try {
                String str2 = this.g;
                this.g = null;
                if (!this.t.get() && g1(this.c) && !this.m && str2 != null && !this.c0 && this.H == null && this.C == null && l1 == null && !h1 && str2.equalsIgnoreCase(str)) {
                    this.m = true;
                    i1("IAP2_INITIAL_LINK_FALLBACK_CONSUMED max=1 recovery_budget_extended=false evidence=" + this.h);
                    return true;
                }
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void A2(long j2, io.carplay.app.secured.x xVar) {
        synchronized (this.s) {
            try {
                AirPlaySession airPlaySession = this.H;
                if (g1(j2) && this.I && airPlaySession != null && this.D == airPlaySession && airPlaySession.G0() && airPlaySession.e0() == xVar) {
                    AirPlaySession.k kVarF2 = airPlaySession.f2();
                    if (kVarF2 == null) {
                        return;
                    }
                    k kVar = new k(j2, airPlaySession, kVarF2, 0);
                    if (!kVar.equals(this.C0) && !kVar.equals(this.B0)) {
                        this.B0 = kVar;
                        T2();
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private boolean B1(String str, String str2) {
        synchronized (this.s) {
            try {
                String str3 = this.n;
                String str4 = this.o;
                this.n = null;
                this.o = null;
                if (!this.t.get() && g1(this.c) && !this.p && str4 != null && !this.c0 && this.H == null && this.C == null && l1 == null && !h1 && str3.equalsIgnoreCase(str) && str2 != null && str4.equalsIgnoreCase(str2)) {
                    if (getSharedPreferences("wireless-ui", 0).getBoolean("enhanced_discovery_" + r2(str), true)) {
                        this.p = true;
                        i1("PHONE_IDENTITY_FALLBACK_CONSUMED max=1 recovery_budget_extended=false");
                        return true;
                    }
                }
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private void B2(boolean z, boolean z2) {
        C2(z, z2, false);
    }

    private boolean C1(String str, long j2) {
        synchronized (this.s) {
            try {
                j jVar = this.q;
                this.q = null;
                if (jVar != null && !this.t.get() && g1(this.c) && !this.r && jVar.j() >= 0 && jVar.j() == j2 && !this.c0 && this.H == null && this.C == null && l1 == null && !h1 && jVar.g().equalsIgnoreCase(str)) {
                    if (getSharedPreferences("wireless-ui", 0).getBoolean("enhanced_discovery_" + r2(str), true)) {
                        this.r = true;
                        i1("PHONE_SERVICE_EOF_RECOVERY_CONSUMED max=1 recovery_budget_extended=false protocol_authentication_unchanged=true");
                        return true;
                    }
                }
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private void C2(boolean z, boolean z2, boolean z3) {
        synchronized (this.s) {
            try {
                if (this.t.get()) {
                    return;
                }
                if (z3 && !this.i0) {
                    this.k0 = false;
                    return;
                }
                if (this.i0 == z) {
                    this.i0 = !z;
                    this.j0++;
                    if (!z) {
                        this.n0++;
                    }
                }
                if (z || z3) {
                    this.k0 = false;
                }
                if (z3) {
                    this.m0 = this.j0;
                } else if (z2 && this.m0 != this.j0) {
                    long j2 = this.o0;
                    long j3 = this.n0;
                    if (j2 != j3 && this.p0 != j3) {
                        this.k0 = true;
                    }
                }
                if (this.k0) {
                    getSharedPreferences(b1, 0).edit().putString("native_foreground_state", "waiting").apply();
                }
                O2(this.J.e());
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private boolean D1(String str, String str2) {
        synchronized (this.s) {
            try {
                String str3 = this.d;
                String str4 = this.e;
                this.d = null;
                this.e = null;
                if (!this.t.get() && g1(this.c) && !this.f && str4 != null && !this.c0 && this.H == null && this.C == null && l1 == null && !h1 && str3.equalsIgnoreCase(str) && str2 != null && str4.equalsIgnoreCase(str2)) {
                    this.f = true;
                    i1("IAP2_AUTH_FIRST_FALLBACK_CONSUMED max=1 recovery_budget_extended=false");
                    return true;
                }
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public z.a D2(io.carplay.app.secured.x xVar) {
        String string = getSharedPreferences("carplay-capability-hints", 0).getString(xVar.o(), null);
        if (string == null) {
            return null;
        }
        try {
            String[] strArrSplit = string.split(",", -1);
            if (strArrSplit.length != 5) {
                return null;
            }
            return new z.a(xVar.o(), Integer.parseInt(strArrSplit[0]), Integer.parseInt(strArrSplit[1]), Integer.parseInt(strArrSplit[2]), Integer.parseInt(strArrSplit[3]));
        } catch (RuntimeException unused) {
            i1("CAPABILITY_HINT_IGNORED invalid=true");
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Boolean E1() {
        return s2(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void E2(j6 j6Var, int i2) {
        String str;
        synchronized (this.s) {
            try {
                if (!this.t.get() && this.G == j6Var) {
                    if (i2 == 65569) {
                        str = "GO_TO_DESKTOP";
                    } else if (i2 == 65564) {
                        str = "BACKGROUND";
                    } else {
                        str = i2 == 65592 ? "REQUEST_FOREGROUND" : "FOREGROUND";
                    }
                    i1("NATIVE_UI_SIGNAL kind=".concat(str));
                    boolean z = true;
                    boolean z2 = i2 == 65564 || i2 == 65569;
                    if (i2 != 65563) {
                        z = false;
                    }
                    C2(z2, false, z);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public AirPlaySession F1() {
        AirPlaySession airPlaySession = this.D;
        if (this.t.get() || airPlaySession == null || !airPlaySession.G0()) {
            return null;
        }
        return airPlaySession;
    }

    private t4.f F2() throws PackageManager.NameNotFoundException, IOException {
        int i2;
        SharedPreferences sharedPreferences = getSharedPreferences("own-receiver-identity", 0);
        String strE = w1.e("car_", this.A.toUpperCase(Locale.ROOT).replace(':', '_'));
        try {
            i2 = Integer.parseInt(sharedPreferences.getString(strE.concat(".channel"), "0"));
        } catch (NumberFormatException unused) {
            i1("NATIVE_PROFILE_INVALID_CHANNEL");
            i2 = 0;
        }
        t5.a aVarA = t5.a(strE, new t5.b(sharedPreferences.getString(strE.concat(".uuid"), null), i2), new t5.b(sharedPreferences.getString("receiver_uuid", null), sharedPreferences.getInt("experimental_channel", 0)), sharedPreferences.getString("legacy_owner", null), new f1(5));
        if (!aVarA.i().isEmpty()) {
            SharedPreferences.Editor editorEdit = sharedPreferences.edit();
            for (Map.Entry<String, String> entry : aVarA.i().entrySet()) {
                editorEdit.putString(entry.getKey(), entry.getValue());
            }
            if (!editorEdit.commit()) {
                SyntheticRefs.c("receiver identity persistence failed");
                return null;
            }
            i1("NATIVE_PROFILE_SAVED legacy_migrated=" + aVarA.h());
        }
        String strH = aVarA.g().h();
        int iG = aVarA.g().g();
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.PackageInfoFlags.of(0L));
        return new t4.f("Android", Build.MANUFACTURER, Build.MODEL, String.valueOf(Build.VERSION.SDK_INT), strH, packageInfo.versionName, Math.toIntExact(packageInfo.getLongVersionCode()), String.valueOf(iG));
    }

    private void G1(long j2, MfiSap.b bVar) {
        byte[] bArrA = bVar.a();
        String strJ1 = J1(MessageDigest.getInstance("SHA-256").digest(bArrA));
        synchronized (this.s) {
            synchronized (U0) {
                if (g1(j2) && this.j != null) {
                    if (!strJ1.equals(bVar.b()) || getSharedPreferences(c1, 0).contains(r2(this.A))) {
                        throw new IOException("current WLAN trust candidate changed");
                    }
                    if (!getSharedPreferences(c1, 0).edit().putString("pending_address", this.A).putString("pending_fingerprint", strJ1).putString("pending_certificate", Base64.encodeToString(bArrA, 2)).putBoolean("pending_current_wifi", true).commit()) {
                        throw new IOException("pending trust persistence failed");
                    }
                    c3(j2, "Current WLAN fresh certificate proof checked; explicit confirmation and a new start required; no media or new peer trust saved");
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void H1(Network network) {
        synchronized (j1) {
            try {
                l lVar = k1;
                if (lVar != null && lVar.i().i().equals(network)) {
                    k1 = null;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: H2, reason: merged with bridge method [inline-methods] */
    public void Q1(String str, u5.a aVar) {
        synchronized (this.s) {
            try {
                if (g1(aVar.h())) {
                    boolean z = aVar.i() == 0;
                    if (z) {
                        e3("CarPlay connection ended: " + str + "; automatic recovery unavailable/exhausted");
                        return;
                    }
                    synchronized (this.s) {
                        try {
                            if (g1(aVar.h())) {
                                int i2 = aVar.i();
                                this.D = null;
                                this.I = false;
                                g1 = false;
                                AirPlaySession airPlaySession = this.H;
                                this.H = null;
                                t4 t4Var = this.G;
                                boolean z2 = (i2 != 1 || this.C == null || this.E == null || str.startsWith("BOOTSTRAP_") || !g3(this.C)) ? false : true;
                                Network network = z2 ? this.C : null;
                                Iap2Csm.a aVar2 = z2 ? this.E : null;
                                String str2 = z2 ? this.F : null;
                                PairingSession pairingSession = z2 ? null : this.B;
                                if (!z2) {
                                    this.B = null;
                                    this.C = null;
                                    this.E = null;
                                    this.F = null;
                                    this.J.c();
                                }
                                if (airPlaySession != null && airPlaySession.V() != null) {
                                    this.K = airPlaySession.V();
                                }
                                if (airPlaySession != null) {
                                    this.L += airPlaySession.k2();
                                    this.M += airPlaySession.j2();
                                    this.N += airPlaySession.i2();
                                }
                                synchronized (this.O0) {
                                    if (!this.P0 || t4Var == null) {
                                        this.P0 = false;
                                    } else {
                                        try {
                                            t4Var.T(i6.TouchAction.UP, this.Q0, this.R0);
                                        } catch (IOException unused) {
                                            i1("Interrupted native gesture could not be released");
                                        }
                                        this.P0 = false;
                                    }
                                    throw th;
                                }
                                if (pairingSession != null) {
                                    pairingSession.close();
                                }
                                if (airPlaySession != null) {
                                    airPlaySession.close();
                                }
                                long jH = aVar.h();
                                String str3 = this.c0 ? "RECONNECTING" : "RETRYING_CONNECTION";
                                b3(jH, str3, "Recovering selected receiver " + i2 + "/3; native_source_retained=" + this.c0 + "; reason=" + str);
                                Runnable runnable = this.Z;
                                if (runnable != null) {
                                    this.w.removeCallbacks(runnable);
                                }
                                Runnable runnable2 = this.a0;
                                if (runnable2 != null) {
                                    this.w.removeCallbacks(runnable2);
                                }
                                if (g1(aVar.h())) {
                                    if (network == null) {
                                        io.carplay.app.secured.p0 p0Var = new io.carplay.app.secured.p0(6);
                                        p0Var.b = this;
                                        p0Var.c = aVar;
                                        VarHandle.storeStoreFence();
                                        this.Z = p0Var;
                                        this.w.postDelayed(p0Var, aVar.g());
                                        return;
                                    }
                                    i1("RECOVERY_REUSE_SELECTED_WIFI generation=" + aVar.h());
                                    g7 g7Var = new g7(1);
                                    g7Var.b = this;
                                    g7Var.c = aVar;
                                    g7Var.d = network;
                                    g7Var.e = aVar2;
                                    g7Var.f = str2;
                                    VarHandle.storeStoreFence();
                                    this.a0 = g7Var;
                                    this.w.postDelayed(g7Var, aVar.g());
                                }
                            }
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                }
            } catch (Throwable th2) {
                throw th2;
            }
        }
    }

    private void I1() throws IOException {
        CarLifeBridge.d dVarF = CarLifeBridge.f(this);
        i1(dVarF.h());
        if (!dVarF.g()) {
            SyntheticRefs.c(w1.e("NATIVE_COMPONENT_", dVarF.m()));
            return;
        }
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(CarLifeBridge.a, PackageManager.PackageInfoFlags.of(0L));
            String str = packageInfo.versionName;
            long longVersionCode = packageInfo.getLongVersionCode();
            if (str == null || str.length() > 32 || !str.matches("[0-9]+(?:\\.[0-9]+){1,5}")) {
                str = "UNAVAILABLE";
            }
            i1("NATIVE_HOST_BUILD code=" + longVersionCode + " version=" + str);
        } catch (Exception unused) {
            i1("NATIVE_HOST_BUILD unavailable=true");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void I2(r6.a aVar, int i2, int i3, int i4) {
        synchronized (this.W.get(aVar)) {
            synchronized (this.V) {
                if (!this.t.get()) {
                    this.X.put(aVar, new g(i2, i3, i4, 0));
                    AirPlaySession airPlaySessionF1 = F1();
                    if (airPlaySessionF1 != null) {
                        try {
                            airPlaySessionF1.p1(aVar, i2, i3, i4);
                        } catch (AirPlaySession.e unused) {
                            i1("AUDIO_OUTPUT_UNAVAILABLE channel=" + String.valueOf(aVar) + " native_session_retained=true");
                        } catch (IOException e2) {
                            if (F1() == airPlaySessionF1 && airPlaySessionF1.G0()) {
                                throw e2;
                            }
                            return;
                        }
                        return;
                    }
                    AudioRelay.a aVar2 = this.Y;
                    AudioRelay.a.EnumC0000a enumC0000a = AudioRelay.a.EnumC0000a.NO_TRANSPORT;
                    b5 b5Var = new b5(1);
                    b5Var.b = this;
                    VarHandle.storeStoreFence();
                    aVar2.a(aVar, enumC0000a, b5Var);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String J1(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        for (byte b2 : bArr) {
            sb.append(String.format(Locale.ROOT, "%02x", Integer.valueOf(b2 & 255)));
        }
        return sb.toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void J2(r6.a aVar, byte[] bArr, long j2) throws IOException {
        AirPlaySession airPlaySessionF1;
        g gVar;
        r6.a aVar2;
        byte[] bArr2;
        if (this.t.get()) {
            SyntheticRefs.c("wireless session cancelled");
            return;
        }
        synchronized (this.W.get(aVar)) {
            try {
                synchronized (this.V) {
                    if (this.t.get()) {
                        throw new IOException("wireless session cancelled");
                    }
                    airPlaySessionF1 = F1();
                    gVar = this.X.get(aVar);
                }
                int i2 = 1;
                if (airPlaySessionF1 == null) {
                    AudioRelay.a aVar3 = this.Y;
                    AudioRelay.a.EnumC0000a enumC0000a = AudioRelay.a.EnumC0000a.NO_TRANSPORT;
                    b5 b5Var = new b5(i2);
                    b5Var.b = this;
                    VarHandle.storeStoreFence();
                    aVar3.a(aVar, enumC0000a, b5Var);
                    return;
                }
                if (gVar == null) {
                    AudioRelay.a aVar4 = this.Y;
                    AudioRelay.a.EnumC0000a enumC0000a2 = AudioRelay.a.EnumC0000a.NO_FORMAT;
                    b5 b5Var2 = new b5(i2);
                    b5Var2.b = this;
                    VarHandle.storeStoreFence();
                    aVar4.a(aVar, enumC0000a2, b5Var2);
                }
                if (gVar != null) {
                    aVar2 = aVar;
                    bArr2 = bArr;
                    airPlaySessionF1.n1(aVar2, gVar.i(), gVar.h(), gVar.g(), bArr2);
                } else {
                    aVar2 = aVar;
                    bArr2 = bArr;
                }
                if (!airPlaySessionF1.F0(aVar2)) {
                    AudioRelay.a aVar5 = this.Y;
                    AudioRelay.a.EnumC0000a enumC0000a3 = AudioRelay.a.EnumC0000a.OUTPUT_NOT_READY;
                    b5 b5Var3 = new b5(i2);
                    b5Var3.b = this;
                    VarHandle.storeStoreFence();
                    aVar5.a(aVar2, enumC0000a3, b5Var3);
                    return;
                }
                try {
                    airPlaySessionF1.t1(aVar2, bArr2, j2);
                    AudioRelay.a aVar6 = this.Y;
                    AudioRelay.a.EnumC0000a enumC0000a4 = AudioRelay.a.EnumC0000a.CALLBACK_RETURNED;
                    b5 b5Var4 = new b5(i2);
                    b5Var4.b = this;
                    VarHandle.storeStoreFence();
                    aVar6.a(aVar2, enumC0000a4, b5Var4);
                    if (this.T0.compareAndSet(false, true)) {
                        i1("Actual native PCM entered wireless sender; no local audio playback");
                    }
                } catch (AirPlaySession.e unused) {
                } catch (IOException e2) {
                    if (F1() == airPlaySessionF1 && airPlaySessionF1.G0()) {
                        throw e2;
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public static boolean K1() {
        return f1 && h1;
    }

    /* JADX INFO: Access modifiers changed from: private */
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
    public void K2(byte[] bArr, long j2) {
        long j3 = this.J0;
        w4 w4Var = this.N0;
        long jNanoTime = System.nanoTime();
        k1 k1Var = new k1(3);
        k1Var.b = this;
        VarHandle.storeStoreFence();
        w4Var.e(bArr, j2, jNanoTime, j3, k1Var);
    }

    public static boolean L1() {
        return f1;
    }

    private void L2() {
        synchronized (this.u) {
            PowerManager.WakeLock wakeLock = this.v;
            this.v = null;
            if (wakeLock != null) {
                try {
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                    }
                } catch (RuntimeException unused) {
                    i1("SESSION_CPU_LEASE release_unconfirmed=true");
                }
            }
        }
    }

    private void M1() {
        PowerManager.WakeLock wakeLockNewWakeLock;
        synchronized (this.u) {
            if (this.t.get() || this.v != null) {
                return;
            }
            try {
                PowerManager powerManager = (PowerManager) getSystemService(PowerManager.class);
                if (powerManager == null) {
                    i1("SESSION_CPU_LEASE unavailable=true");
                    return;
                }
                wakeLockNewWakeLock = powerManager.newWakeLock(1, "CarPlus:ProjectionSession");
                try {
                    wakeLockNewWakeLock.setReferenceCounted(false);
                    wakeLockNewWakeLock.acquire();
                    this.v = wakeLockNewWakeLock;
                    i1("SESSION_CPU_LEASE held=true screen_forced_on=false interactive=" + powerManager.isInteractive() + " power_save=" + powerManager.isPowerSaveMode() + " device_idle=" + powerManager.isDeviceIdleMode());
                } catch (RuntimeException unused) {
                    if (wakeLockNewWakeLock != null) {
                        try {
                            if (wakeLockNewWakeLock.isHeld()) {
                                wakeLockNewWakeLock.release();
                            }
                        } catch (RuntimeException unused2) {
                        }
                    }
                    this.v = null;
                    i1("SESSION_CPU_LEASE unavailable=true");
                }
            } catch (RuntimeException unused3) {
                wakeLockNewWakeLock = null;
            }
        }
    }

    private void M2(long j2, AirPlaySession airPlaySession, Network network, String str) {
        synchronized (this.s) {
            synchronized (U0) {
                if (g1(j2) && this.H == airPlaySession && airPlaySession.G0() && str != null) {
                    int i2 = 0;
                    if (str.equalsIgnoreCase(getSharedPreferences(c1, 0).getString(r2(this.A), ""))) {
                        WifiNetworkMonitor.e eVarR = WifiNetworkMonitor.r(this, network);
                        if (eVarR != null) {
                            synchronized (j1) {
                                k1 = new l(this.A, str, eVarR, i2);
                            }
                            i1("CURRENT_WIFI_REUSE_ELIGIBLE authenticated=true process_local=true credentials_persisted=false");
                        }
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Thread N1(Runnable runnable) {
        Thread thread = new Thread(runnable, "CarPlus-wireless-session");
        thread.setDaemon(true);
        return thread;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void N2(j6 j6Var) {
        synchronized (this.s) {
            try {
                if (!this.t.get() && (j6Var == null || this.G == j6Var)) {
                    if (this.s0 != null) {
                        this.v0.incrementAndGet();
                        int i2 = 1;
                        if (this.u0.compareAndSet(false, true)) {
                            try {
                                ExecutorService executorService = this.y;
                                b7 b7Var = new b7(i2);
                                b7Var.b = this;
                                VarHandle.storeStoreFence();
                                executorService.execute(b7Var);
                            } catch (RejectedExecutionException unused) {
                                this.u0.set(false);
                            }
                        }
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Thread O1(Runnable runnable) {
        Thread thread = new Thread(runnable, "CarPlus-ui-intents");
        thread.setDaemon(true);
        return thread;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void O2(long j2) {
        try {
            ExecutorService executorService = this.y;
            a7 a7Var = new a7();
            a7Var.a = this;
            a7Var.b = j2;
            VarHandle.storeStoreFence();
            executorService.execute(a7Var);
        } catch (RejectedExecutionException unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void P1(long j2, AirPlaySession airPlaySession, t4 t4Var) {
        synchronized (this.s) {
            try {
                if (g1(j2) && this.I && this.H == airPlaySession && this.G == t4Var) {
                    i1("AUDIO_USER_INTENT accepted=" + airPlaySession.X1() + " current_transport=true playback_confirmed=false");
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private void P2(AirPlaySession airPlaySession) {
        g gVar;
        for (r6.a aVar : r6.a.values()) {
            synchronized (this.W.get(aVar)) {
                synchronized (this.V) {
                    if (this.t.get() || F1() != airPlaySession) {
                        return;
                    } else {
                        gVar = this.X.get(aVar);
                    }
                    throw th;
                }
                if (gVar != null) {
                    try {
                        try {
                            airPlaySession.p1(aVar, gVar.i(), gVar.h(), gVar.g());
                        } catch (AirPlaySession.e unused) {
                            i1("AUDIO_RESTORE_UNAVAILABLE channel=" + String.valueOf(aVar) + " video_retained=" + airPlaySession.G0());
                        }
                    } catch (IOException e2) {
                        if (F1() == airPlaySession && airPlaySession.G0()) {
                            throw e2;
                        }
                        return;
                    }
                }
            }
        }
    }

    private Network Q2() {
        l lVar = k1;
        if (lVar == null || !lVar.h().equals(this.A)) {
            return null;
        }
        String string = getSharedPreferences(c1, 0).getString(r2(this.A), null);
        if (string != null && lVar.g().equalsIgnoreCase(string) && WifiNetworkMonitor.A(this, lVar.i())) {
            return lVar.i().i();
        }
        synchronized (j1) {
            try {
                if (k1 == lVar) {
                    k1 = null;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void R1(long j2, AirPlaySession airPlaySession) {
        synchronized (this.s) {
            if (g1(j2) && this.I && this.H == airPlaySession && this.i0 && airPlaySession.e2()) {
                t4 t4Var = this.G;
                if (t4Var == null) {
                    return;
                }
                try {
                    i1("NATIVE_RECOVERY_VIDEO_SYNC command_pair_written=" + t4Var.f() + " source_session_retained=true idr_confirmed=false");
                } catch (IOException unused) {
                    i1("NATIVE_RECOVERY_VIDEO_SYNC unavailable=true source_session_retained=true");
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void R2(io.carplay.app.secured.h hVar) {
        String str;
        synchronized (this.I0) {
            try {
                if (hVar.j() == this.J0 && !this.t.get()) {
                    long jNanoTime = System.nanoTime();
                    p6 p6Var = this.P;
                    p6.b bVar = p6.b.NATIVE_ASSEMBLY;
                    p6Var.d(bVar, hVar.c(), jNanoTime);
                    long j2 = this.Q + 1;
                    this.Q = j2;
                    if (this.S == 0) {
                        this.S = jNanoTime;
                        this.R = j2;
                    }
                    if (jNanoTime - this.S >= 5000000000L) {
                        p6.c cVar = this.P.e().get(bVar);
                        long j3 = ((this.Q - this.R) * 100000000000L) / (jNanoTime - this.S);
                        long j4 = this.T;
                        long j5 = this.U;
                        if (cVar == null) {
                            str = "";
                        } else {
                            str = " assembly_p50_us=" + (cVar.i() / 1000) + " assembly_p95_us=" + (cVar.j() / 1000);
                        }
                        i1("VIDEO_NATIVE_FLOW received_fps_x100=" + j3 + " passthrough_enqueued=" + j4 + " adapted_enqueued=" + j5 + str + " actual_display_unconfirmed=true");
                        this.S = jNanoTime;
                        this.R = this.Q;
                    }
                    AvcEncoder avcEncoder = this.z0;
                    if (avcEncoder != null) {
                        avcEncoder.m(hVar);
                    } else {
                        if (this.E0) {
                            return;
                        }
                        U2(hVar, false);
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ t5.b S1() {
        int iNextInt;
        SecureRandom secureRandom = new SecureRandom();
        do {
            iNextInt = secureRandom.nextInt(7999999) + 21000000;
        } while (iNextInt == 21070001);
        return new t5.b(UUID.randomUUID().toString(), iNextInt);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void S2(io.carplay.app.secured.x xVar, io.carplay.app.secured.x.c cVar) {
        try {
            Stream<io.carplay.app.secured.x.c> stream = xVar.i().stream();
            a4 a4Var = new a4(2);
            a4Var.b = cVar;
            VarHandle.storeStoreFence();
            if (stream.filter(a4Var).count() > 1) {
                i1("CAPABILITY_HINT_SKIPPED reason=AMBIGUOUS_DISPLAY");
                return;
            }
            int i2 = 1;
            z.a aVarG = new z(32).g(xVar, 1, cVar, Math.min(60, cVar.n()));
            SharedPreferences sharedPreferences = getSharedPreferences("carplay-capability-hints", 0);
            synchronized (sharedPreferences) {
                try {
                    ArrayList arrayList = new ArrayList(sharedPreferences.getAll().entrySet());
                    io.carplay.app.secured.y yVar = new io.carplay.app.secured.y(i2);
                    yVar.b = aVarG;
                    VarHandle.storeStoreFence();
                    arrayList.removeIf(yVar);
                    arrayList.sort(Comparator.comparingLong(new io.carplay.app.secured.p(4)));
                    SharedPreferences.Editor editorEdit = sharedPreferences.edit();
                    while (arrayList.size() >= 32) {
                        editorEdit.remove((String) ((Map.Entry) arrayList.remove(0)).getKey());
                    }
                    editorEdit.putString(aVarG.g(), aVarG.j() + "," + aVarG.k() + "," + aVarG.i() + "," + aVarG.h() + "," + System.currentTimeMillis()).apply();
                } catch (Throwable th) {
                    throw th;
                }
            }
            i1("CAPABILITY_HINT_SAVED route_accepted=true display_receipt_confirmed=false");
        } catch (IOException | RuntimeException unused) {
            i1("CAPABILITY_HINT_UNAVAILABLE authentication_unchanged=true");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void T1(u5.a aVar, Network network, Iap2Csm.a aVar2, String str) {
        if (g1(aVar.h())) {
            try {
                ExecutorService executorService = this.x;
                g7 g7Var = new g7(0);
                g7Var.b = this;
                g7Var.d = network;
                g7Var.e = aVar2;
                g7Var.f = str;
                g7Var.c = aVar;
                VarHandle.storeStoreFence();
                executorService.execute(g7Var);
            } catch (RejectedExecutionException unused) {
            }
        }
    }

    private void T2() {
        int i2 = 0;
        if (this.D0.compareAndSet(false, true)) {
            try {
                ExecutorService executorService = this.y;
                b7 b7Var = new b7(i2);
                b7Var.b = this;
                VarHandle.storeStoreFence();
                executorService.execute(b7Var);
            } catch (RejectedExecutionException unused) {
                this.D0.set(false);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void U1(Network network, Iap2Csm.a aVar, String str, u5.a aVar2) {
        y1(network, aVar, str, aVar2.h());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void U2(io.carplay.app.secured.h hVar, boolean z) throws IOException {
        if (this.t.get()) {
            SyntheticRefs.c("wireless session cancelled");
            return;
        }
        AirPlaySession airPlaySessionF1 = F1();
        if (airPlaySessionF1 == null) {
            return;
        }
        try {
            if (airPlaySessionF1.v1(hVar)) {
                if (z) {
                    this.U++;
                } else {
                    this.T++;
                }
            }
            if (this.S0.compareAndSet(false, true)) {
                i1("Actual native video entered current wireless sender; waiting for first IDR send confirmation");
            }
        } catch (IOException e2) {
            if (F1() == airPlaySessionF1 && airPlaySessionF1.G0()) {
                throw e2;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void V1(u5.a aVar) {
        X2(aVar.h());
    }

    private static boolean V2(PackageManager packageManager, String str, String str2) {
        int length = str2.length() / 2;
        byte[] bArr = new byte[length];
        for (int i2 = 0; i2 < length; i2++) {
            int i3 = i2 * 2;
            bArr[i2] = (byte) Integer.parseInt(str2.substring(i3, i3 + 2), 16);
        }
        return packageManager.hasSigningCertificate(str, bArr, 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code duplicated, block: B:54:0x00cf A[Catch: all -> 0x002b, TryCatch #3 {, blocks: (B:5:0x000f, B:6:0x0011, B:10:0x0017, B:41:0x0096, B:43:0x009c, B:46:0x00a7, B:50:0x00b2, B:54:0x00cf, B:55:0x00d1, B:64:0x00de, B:65:0x00df, B:44:0x00a1, B:52:0x00c7, B:70:0x00f4, B:77:0x010b, B:83:0x0120, B:56:0x00d2, B:58:0x00d6, B:61:0x00db, B:7:0x0012, B:9:0x0016, B:19:0x0031, B:21:0x003b, B:23:0x0043, B:25:0x004d, B:27:0x0053, B:29:0x005b, B:32:0x006b, B:34:0x006f, B:37:0x0075, B:39:0x007b, B:40:0x0095, B:69:0x00f3, B:75:0x0108, B:76:0x010a), top: B:96:0x000f, outer: #1, inners: #0, #2, #4 }] */
    /* JADX WARN: Code duplicated, block: B:58:0x00d6 A[Catch: all -> 0x00d9, TryCatch #2 {all -> 0x00d9, blocks: (B:56:0x00d2, B:58:0x00d6, B:61:0x00db), top: B:94:0x00d2, outer: #3 }] */
    /* JADX WARN: Code duplicated, block: B:68:0x00ef  */
    /* JADX WARN: Code duplicated, block: B:94:0x00d2 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:99:? A[RETURN, SYNTHETIC] */
    public /* synthetic */ void W1() {
        t4 t4Var;
        boolean zI;
        long j2 = this.v0.get();
        try {
            synchronized (this.t0) {
                synchronized (this.s) {
                    i iVar = this.s0;
                    if (iVar == null) {
                        this.u0.set(false);
                        if (this.v0.get() != j2) {
                            N2(null);
                            return;
                        }
                        return;
                    }
                    if (g1(iVar.j()) && this.H == iVar.i() && this.j0 == iVar.k() && ((iVar.h() == null || this.G == iVar.h()) && iVar.i().e2() == iVar.g())) {
                        if (this.I && (t4Var = this.G) != null) {
                            if (iVar.h() == null) {
                                i iVar2 = new i(iVar.j(), iVar.i(), t4Var, iVar.g(), iVar.k(), 0);
                                this.s0 = iVar2;
                                iVar = iVar2;
                            }
                            try {
                                zI = iVar.g() ? t4Var.i() : t4Var.e();
                                if (!zI) {
                                    if (zI) {
                                        synchronized (this.s) {
                                            try {
                                                if (this.s0 == iVar) {
                                                    this.s0 = null;
                                                }
                                            } catch (Throwable th) {
                                                throw th;
                                            }
                                        }
                                    }
                                    this.u0.set(false);
                                    if (this.v0.get() != j2) {
                                        N2(null);
                                        return;
                                    }
                                    return;
                                }
                                i1("NATIVE_VIDEO_VISIBILITY kind=" + (iVar.g() ? "RESUME" : "PAUSE") + " command_written=true source_session_retained=true idr_confirmed=false");
                                if (zI) {
                                    synchronized (this.s) {
                                        if (this.s0 == iVar) {
                                            this.s0 = null;
                                        }
                                    }
                                }
                                this.u0.set(false);
                                if (this.v0.get() != j2) {
                                    N2(null);
                                    return;
                                }
                                return;
                            } catch (IOException unused) {
                                i1("NATIVE_VIDEO_VISIBILITY unavailable=true source_session_retained=true");
                                zI = true;
                            }
                            throw th;
                        }
                        this.u0.set(false);
                        if (this.v0.get() != j2) {
                            N2(null);
                            return;
                        }
                        return;
                    }
                    this.s0 = null;
                    this.u0.set(false);
                    if (this.v0.get() != j2) {
                        N2(null);
                    }
                }
            }
        } catch (Throwable th2) {
            this.u0.set(false);
            if (this.v0.get() != j2) {
                N2(null);
            }
            throw th2;
        }
    }

    private void W2() {
        X2(-1L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void X1(long j2) {
        if (g1(j2)) {
            synchronized (this.s) {
                try {
                    if (g1(j2)) {
                        AirPlaySession airPlaySession = this.I ? this.H : null;
                        long j3 = this.j0;
                        long j4 = this.n0;
                        boolean z = this.i0;
                        boolean z2 = this.k0;
                        boolean z3 = this.w0 == airPlaySession && this.l0 == j3;
                        if (airPlaySession == null) {
                            return;
                        }
                        synchronized (this.s) {
                            if (g1(j2) && j3 == this.j0 && this.H == airPlaySession && this.I) {
                                boolean zD2 = !z ? airPlaySession.d2() : airPlaySession.Y1();
                                if (!z3) {
                                    i1("UI_SESSION_INTENT kind=" + (!z ? "CAR" : "CARPLUS") + " accepted=" + zD2 + " session_retained=true");
                                    if (zD2) {
                                        synchronized (this.s) {
                                            try {
                                                if (g1(j2) && j3 == this.j0 && this.H == airPlaySession) {
                                                    this.w0 = airPlaySession;
                                                    this.l0 = j3;
                                                }
                                            } catch (Throwable th) {
                                                throw th;
                                            }
                                        }
                                    }
                                }
                                if (zD2 && z2) {
                                    synchronized (this.s) {
                                        try {
                                            if (g1(j2) && j3 == this.j0 && j4 == this.n0 && this.H == airPlaySession && this.I && this.i0 && this.k0) {
                                                t4 t4Var = this.G;
                                                if (t4Var != null) {
                                                    this.q0 = t4Var;
                                                    this.r0 = j4;
                                                }
                                                if (t4Var != null) {
                                                    try {
                                                        boolean zG = t4Var.g();
                                                        i1("NATIVE_FOREGROUND_REQUEST sent=" + zG + " acknowledged=false");
                                                        if (zG) {
                                                            synchronized (this.s) {
                                                                try {
                                                                    if (g1(j2) && j3 == this.j0 && j4 == this.n0 && this.H == airPlaySession && this.G == t4Var) {
                                                                        this.k0 = false;
                                                                        this.o0 = j4;
                                                                        this.p0 = j4;
                                                                        if (this.m0 != j3) {
                                                                            getSharedPreferences(b1, 0).edit().putString("native_foreground_state", "written").apply();
                                                                        }
                                                                    }
                                                                } catch (Throwable th2) {
                                                                    throw th2;
                                                                }
                                                            }
                                                        }
                                                    } catch (IOException unused) {
                                                        synchronized (this.s) {
                                                            try {
                                                                if (this.G == t4Var && j4 == this.n0) {
                                                                    this.p0 = j4;
                                                                    this.k0 = false;
                                                                }
                                                                i1("NATIVE_FOREGROUND_REQUEST unavailable=true");
                                                            } catch (Throwable th3) {
                                                                throw th3;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (Throwable th4) {
                                            throw th4;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Throwable th5) {
                    throw th5;
                }
            }
        }
    }

    private void X2(long j2) {
        if (this.j != null) {
            Y2(j2);
            return;
        }
        synchronized (this.s) {
            try {
                if (!this.t.get() && this.J.e() >= 0 && (j2 < 0 || g1(j2))) {
                    l1 = null;
                    h1 = false;
                    this.i = null;
                    long jB = this.J.b();
                    this.c = jB;
                    long jC = this.J.c();
                    ExecutorService executorService = this.x;
                    h7 h7Var = new h7();
                    h7Var.a = this;
                    h7Var.b = jB;
                    h7Var.c = jC;
                    h7Var.d = j2;
                    VarHandle.storeStoreFence();
                    executorService.execute(h7Var);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean Y1(io.carplay.app.secured.x.c cVar, io.carplay.app.secured.x.c cVar2) {
        return cVar2.q() == 110 && cVar2.k() && cVar2.v() == cVar.v() && cVar2.j() == cVar.j() && cVar2.n() >= Math.min(60, cVar.n());
    }

    private void Y2(long j2) {
        synchronized (this.s) {
            try {
                if (!this.t.get() && this.J.e() >= 0 && (j2 < 0 || g1(j2))) {
                    long jB = this.J.b();
                    this.c = jB;
                    long jC = this.J.c();
                    WifiNetworkMonitor wifiNetworkMonitor = this.l;
                    this.l = null;
                    this.C = null;
                    this.E = null;
                    this.F = null;
                    this.i = null;
                    if (wifiNetworkMonitor != null) {
                        wifiNetworkMonitor.close();
                    }
                    try {
                        ExecutorService executorService = this.x;
                        e7 e7Var = new e7();
                        e7Var.a = this;
                        e7Var.b = jC;
                        e7Var.c = jB;
                        VarHandle.storeStoreFence();
                        executorService.execute(e7Var);
                    } catch (RejectedExecutionException unused) {
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean Z1(z.a aVar, Map.Entry entry) {
        return ((String) entry.getKey()).equals(aVar.g());
    }

    private void Z2() {
        synchronized (this.s) {
            try {
                if (!this.c0 && !this.t.get()) {
                    AirPlaySession airPlaySession = this.H;
                    if (airPlaySession == null) {
                        i1("Native source not started: no authenticated transport");
                        return;
                    }
                    t4.VideoConfig gVarJ2 = j2();
                    int i2 = 1;
                    this.c0 = true;
                    g1 = false;
                    getSharedPreferences(b1, 0).edit().putBoolean("native_exact_geometry_requested", gVarJ2.l()).putInt("source_requested_width", gVarJ2.m()).putInt("source_requested_height", gVarJ2.k()).apply();
                    a3("NATIVE_SOURCE_ARMED", "Normal Car+ entry requested; source requested=" + this.f0 + "x" + this.g0 + " follows actual receiver declaration; original Car+ settings may override it. Continuous session, no fixed time limit.");
                    j1("SOURCE_ARMED");
                    ExecutorService executorService = this.x;
                    io.carplay.app.secured.q0 q0Var = new io.carplay.app.secured.q0(i2);
                    q0Var.c = this;
                    q0Var.d = gVarJ2;
                    q0Var.b = airPlaySession;
                    VarHandle.storeStoreFence();
                    executorService.execute(q0Var);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ long a2(Map.Entry entry) {
        try {
            String[] strArrSplit = String.valueOf(entry.getValue()).split(",", -1);
            if (strArrSplit.length == 5) {
                return Long.parseLong(strArrSplit[4]);
            }
            return 0L;
        } catch (RuntimeException unused) {
            return 0L;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code duplicated, block: B:46:0x00be  */
    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    public void a3(String str, String str2) {
        String str3;
        synchronized (this.s) {
            try {
                if (this.t.get()) {
                    return;
                }
                getSharedPreferences(b1, 0).edit().putString("state", str).putBoolean("running", f1).putBoolean("ready_for_source", g1).remove("reason").apply();
                i1(str2);
                switch (str.hashCode()) {
                    case -2026270421:
                        if (!str.equals("RECONNECTING")) {
                            str3 = "正在连接接收端，请保持无线 CarPlay 已开启";
                        } else {
                            str3 = "连接中断，正在自动恢复；保留 Car+ 会话";
                        }
                        break;
                    case -1752392807:
                        if (!str.equals("FORWARDING_NATIVE")) {
                            str3 = "正在连接接收端，请保持无线 CarPlay 已开启";
                        } else {
                            str3 = "画面发送中，请以车机显示为准";
                        }
                        break;
                    case -1684699706:
                        if (!str.equals("TRUST_REQUIRED")) {
                            str3 = "正在连接接收端，请保持无线 CarPlay 已开启";
                        } else {
                            str3 = "车机证书已变化，请打开软件核对";
                        }
                        break;
                    case -1667143371:
                        if (!str.equals("CAR_SCREEN_IN_USE")) {
                            str3 = "正在连接接收端，请保持无线 CarPlay 已开启";
                        } else {
                            str3 = "车机正在使用屏幕，连接保持中";
                        }
                        break;
                    case -1470717700:
                        if (!str.equals("READY_FOR_CARPLUS")) {
                            str3 = "正在连接接收端，请保持无线 CarPlay 已开启";
                        } else {
                            str3 = "接收端已就绪，请按原生 Car+ 提示连接";
                        }
                        break;
                    case -1263923451:
                        if (!str.equals("WAITING_NATIVE_VIDEO")) {
                            str3 = "正在连接接收端，请保持无线 CarPlay 已开启";
                        } else {
                            str3 = "车机通道已连接，等待 Car+ 输出画面";
                        }
                        break;
                    case -1113113514:
                        if (!str.equals("VIDEO_OUTPUT_UNAVAILABLE")) {
                            str3 = "正在连接接收端，请保持无线 CarPlay 已开启";
                        } else {
                            str3 = "连接保持中，画面帧率暂无法适配";
                        }
                        break;
                    case -83322200:
                        if (!str.equals("PAIRING")) {
                            str3 = "正在连接接收端，请保持无线 CarPlay 已开启";
                        } else {
                            str3 = "正在认证所选接收端";
                        }
                        break;
                    case 2664213:
                        if (!str.equals("WIFI")) {
                            str3 = "正在连接接收端，请保持无线 CarPlay 已开启";
                        } else {
                            str3 = "正在建立 WLAN，请处理系统连接提示";
                        }
                        break;
                    case 330508767:
                        if (!str.equals("NATIVE_SOURCE_ARMED")) {
                            str3 = "正在连接接收端，请保持无线 CarPlay 已开启";
                        } else {
                            str3 = "接收端已就绪，请按原生 Car+ 提示连接";
                        }
                        break;
                    case 601092172:
                        if (!str.equals("RESUMING_NATIVE")) {
                            str3 = "正在连接接收端，请保持无线 CarPlay 已开启";
                        } else {
                            str3 = "已重连，正在恢复画面";
                        }
                        break;
                    default:
                        str3 = "正在连接接收端，请保持无线 CarPlay 已开启";
                        break;
                }
                f3(str3);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Bottom block not found for handler: all -> 0x01bf */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public /* synthetic */ void b2() {
        /*
            Method dump skipped, instruction units count: 754
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: io.carplay.app.prototype.WirelessCarPlayService.b2():void");
    }

    private void b3(long j2, String str, String str2) {
        synchronized (this.s) {
            try {
                if (g1(j2)) {
                    a3(str, str2);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void c2(long j2, long j3, long j4) {
        boolean zG1;
        WirelessCarPlayService wirelessCarPlayService = this;
        try {
            wirelessCarPlayService.I1();
            h5 h5Var = null;
            String string = wirelessCarPlayService.getSharedPreferences(c1, 0).getString(r2(wirelessCarPlayService.A), null);
            synchronized (wirelessCarPlayService.s) {
                if (g1(j2) && wirelessCarPlayService.h1(j3)) {
                    boolean zD1 = wirelessCarPlayService.D1(wirelessCarPlayService.A, string);
                    boolean zB1 = wirelessCarPlayService.B1(wirelessCarPlayService.A, string);
                    boolean zA1 = wirelessCarPlayService.A1(wirelessCarPlayService.A);
                    a aVar = new a(wirelessCarPlayService, j3, wirelessCarPlayService.C1(wirelessCarPlayService.A, j4), zB1);
                    wirelessCarPlayService = this;
                    PairingSession pairingSession = new PairingSession(wirelessCarPlayService, aVar, string, zD1, zD1, zA1);
                    synchronized (wirelessCarPlayService.s) {
                        try {
                            zG1 = g1(j2);
                            if (zG1) {
                                wirelessCarPlayService.B = pairingSession;
                            }
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                    if (!zG1) {
                        pairingSession.close();
                        return;
                    }
                    if (wirelessCarPlayService.c0) {
                        wirelessCarPlayService.i1("Recovery listening for selected receiver; native source remains alive");
                    } else {
                        wirelessCarPlayService.a3("BLUETOOTH", "Listening for the selected paired receiver; system Wi-Fi consent may follow");
                    }
                    h5 h5VarI = PairingPreparationService.i();
                    String str = "enhanced_discovery_" + r2(wirelessCarPlayService.A);
                    int i2 = 1;
                    if (h5VarI != null) {
                        wirelessCarPlayService.getSharedPreferences("wireless-ui", 0).edit().putBoolean(str, true).apply();
                    } else if (wirelessCarPlayService.getSharedPreferences("wireless-ui", 0).getBoolean(str, false)) {
                        b5 b5Var = new b5(i2);
                        b5Var.b = wirelessCarPlayService;
                        VarHandle.storeStoreFence();
                        h5VarI = new h5(b5Var, new f7());
                        try {
                            h5VarI.s(wirelessCarPlayService);
                            wirelessCarPlayService.i1("PHONE_DISCOVERY_REMEMBERED_PEER enabled=true radio_confirmed=false");
                        } catch (Exception unused) {
                            h5VarI.close();
                            wirelessCarPlayService.i1("PHONE_DISCOVERY_UNAVAILABLE normal_secure_bootstrap_retained=true");
                            h5VarI = null;
                        }
                    }
                    if (h5VarI == null || h5VarI.m()) {
                        h5Var = h5VarI;
                    } else {
                        h5VarI.close();
                        wirelessCarPlayService.i1("PHONE_DISCOVERY_RETIRED_BEFORE_HANDOFF normal_secure_bootstrap_retained=true");
                    }
                    pairingSession.P1(wirelessCarPlayService.A, true, h5Var);
                }
            }
        } catch (Exception e2) {
            if (g1(j2)) {
                if (u5.l(e2)) {
                    wirelessCarPlayService.x2("BOOTSTRAP_START_".concat(e2.getClass().getSimpleName()), j2);
                } else {
                    wirelessCarPlayService.c3(j2, "Wireless start failed: ".concat(e2.getClass().getSimpleName()));
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void c3(long j2, String str) {
        if (this.J.j(j2)) {
            e3(str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void d2() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void d3(long j2, long j3, String str) {
        boolean zI;
        synchronized (this.s) {
            zI = this.J.i(j2, j3);
        }
        if (zI) {
            e3(str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void e2(long j2, long j3) {
        WirelessCarPlayService wirelessCarPlayService;
        long j4;
        long j5;
        boolean z;
        try {
            I1();
            String strG = io.carplay.app.prototype.r.a(this, this.A).g();
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BluetoothManager.class);
            BluetoothAdapter adapter = bluetoothManager == null ? null : bluetoothManager.getAdapter();
            try {
                if (adapter != null && adapter.isEnabled()) {
                    Stream<BluetoothDevice> stream = adapter.getBondedDevices().stream();
                    a4 a4Var = new a4(3);
                    a4Var.b = this;
                    VarHandle.storeStoreFence();
                    if (!stream.noneMatch(a4Var)) {
                        wirelessCarPlayService = this;
                        j4 = j2;
                        j5 = j3;
                        WifiNetworkMonitor wifiNetworkMonitor = new WifiNetworkMonitor(wirelessCarPlayService, this.j, strG, new b(wirelessCarPlayService, j4, j5));
                        synchronized (wirelessCarPlayService.s) {
                            try {
                                z = wirelessCarPlayService.g1(j5) && wirelessCarPlayService.h1(j4);
                                if (z) {
                                    wirelessCarPlayService.l = wifiNetworkMonitor;
                                }
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                        if (!z) {
                            wifiNetworkMonitor.close();
                            return;
                        } else {
                            wirelessCarPlayService.b3(j5, "WIFI", wirelessCarPlayService.k ? "Rediscovering the previously authenticated receiver on its retained WLAN; full fresh authentication required" : "Discovering services on the manually selected receiver WLAN");
                            wifiNetworkMonitor.y();
                            return;
                        }
                    }
                }
                throw new IOException("CURRENT_WIFI_PAIRED_BLUETOOTH_SELECTION_REQUIRED");
            } catch (Exception e2) {
                e = e2;
                wirelessCarPlayService.d3(j4, j5, "Current WLAN startup failed: ".concat(e.getClass().getSimpleName()));
            }
        } catch (Exception e3) {
            e = e3;
            wirelessCarPlayService = this;
            j4 = j2;
            j5 = j3;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void e3(String str) {
        PairingSession pairingSession;
        t4 t4Var;
        AirPlaySession airPlaySession;
        i5.a aVar;
        if (this.t.compareAndSet(false, true)) {
            L2();
            f1 = false;
            g1 = false;
            l1 = null;
            h1 = false;
            Runnable runnable = this.Z;
            if (runnable != null) {
                this.w.removeCallbacks(runnable);
            }
            Runnable runnable2 = this.a0;
            if (runnable2 != null) {
                this.w.removeCallbacks(runnable2);
            }
            synchronized (this.s) {
                this.J.h();
                this.s0 = null;
                this.g = null;
                this.d = null;
                this.e = null;
                this.n = null;
                this.o = null;
                this.c = -1L;
                this.D = null;
                this.I = false;
                pairingSession = this.B;
                this.B = null;
                t4Var = this.G;
                this.G = null;
                airPlaySession = this.H;
                this.H = null;
                aVar = this.a;
                this.a = null;
            }
            i1(str);
            getSharedPreferences(b1, 0).edit().putString("state", "STOPPED").putBoolean("running", false).putBoolean("ready_for_source", false).putString("reason", str).apply();
            WifiNetworkMonitor wifiNetworkMonitor = this.l;
            this.l = null;
            if (wifiNetworkMonitor != null) {
                wifiNetworkMonitor.close();
            }
            if (pairingSession != null) {
                pairingSession.close();
            }
            if (t4Var != null) {
                t4Var.close();
            }
            synchronized (this.I0) {
                try {
                    this.J0++;
                    AvcEncoder avcEncoder = this.z0;
                    this.z0 = null;
                    this.A0 = null;
                    this.F0 = null;
                    this.G0 = 0;
                    this.H0 = 0;
                    if (avcEncoder != null) {
                        avcEncoder.close();
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (airPlaySession != null) {
                i1("Final sender counters across transports: videoUnits=" + (airPlaySession.k2() + this.L) + ", videoBytes=" + (airPlaySession.j2() + this.M) + ", pcmBytes=" + (airPlaySession.i2() + this.N));
                airPlaySession.close();
            }
            this.N0.j();
            synchronized (this.O0) {
                this.P0 = false;
            }
            this.x.shutdownNow();
            this.y.shutdownNow();
            if (aVar != null) {
                aVar.close();
            }
            Handler handler = this.w;
            b7 b7Var = new b7(2);
            b7Var.b = this;
            VarHandle.storeStoreFence();
            handler.post(b7Var);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean f2(BluetoothDevice bluetoothDevice) {
        return this.A.equals(bluetoothDevice.getAddress());
    }

    private void f3(String str) {
        if (this.t.get()) {
            return;
        }
        ((NotificationManager) getSystemService(NotificationManager.class)).notify(e1, n2(str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean g1(long j2) {
        return !this.t.get() && this.J.a(j2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void g2(t4.VideoConfig gVar, AirPlaySession airPlaySession) {
        try {
            I1();
            i7 i7Var = new i7();
            i7Var.a = this;
            VarHandle.storeStoreFence();
            t4 t4Var = new t4(i7Var, F2(), new e(), gVar);
            synchronized (this.s) {
                try {
                    if (this.t.get()) {
                        t4Var.close();
                        return;
                    }
                    this.G = t4Var;
                    this.o0 = -1L;
                    this.p0 = -1L;
                    this.m0 = -1L;
                    this.q0 = null;
                    j6.a aVarK = t4Var.k();
                    i1("SOURCE_BACKEND_CAPABILITIES foreground=" + aVarK.g() + " visibility=" + aVarK.l() + " hard_keys=" + aVarK.h() + " night_mode=" + aVarK.j() + " view_area=" + aVarK.m() + " vehicle_information=" + aVarK.k() + " limited_ui=" + aVarK.i() + " display_confirmed=false");
                    try {
                        t4.e eVarP = t4Var.P();
                        t4Var.close();
                        synchronized (this.s) {
                            try {
                                if (this.G == t4Var) {
                                    this.G = null;
                                }
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                        i1("Native source outcome: ".concat(String.valueOf(eVarP)));
                        j1("SOURCE_ENDED");
                        i1("Initial transport counters: videoUnits=" + airPlaySession.k2() + ", videoBytes=" + airPlaySession.j2() + ", pcmBytes=" + airPlaySession.i2());
                        e3("Native source ended; receiver connection closed without automatic reconnect");
                    } catch (Throwable th2) {
                        t4Var.close();
                        synchronized (this.s) {
                            try {
                                if (this.G == t4Var) {
                                    this.G = null;
                                }
                                throw th2;
                            } catch (Throwable th3) {
                                throw th3;
                            }
                        }
                    }
                } catch (Throwable th4) {
                    throw th4;
                }
            }
        } catch (Exception e2) {
            if (this.t.get()) {
                return;
            }
            e3("Native source/forwarding failed: ".concat(e2.getClass().getSimpleName()));
        }
    }

    private boolean g3(Network network) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(ConnectivityManager.class);
            NetworkCapabilities networkCapabilities = connectivityManager == null ? null : connectivityManager.getNetworkCapabilities(network);
            return (networkCapabilities == null || !networkCapabilities.hasTransport(1) || networkCapabilities.hasTransport(4)) ? false : true;
        } catch (RuntimeException unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean h1(long j2) {
        return !this.t.get() && this.J.d(j2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void h2() {
        stopForeground(1);
        stopSelf();
    }

    public static Iap2Csm.g h3() {
        if (f1) {
            return l1;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void i1(String str) {
        Log.i("CarPlusWireless", str);
        AppLogger.g(this, str);
        synchronized (this.s) {
            StringBuilder sb = this.z;
            sb.append(Instant.now());
            sb.append(' ');
            sb.append(str);
            sb.append('\n');
            if (this.z.length() > 96000) {
                StringBuilder sb2 = this.z;
                sb2.delete(0, sb2.length() - 96000);
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File(getFilesDir(), "wireless-session-last.txt"));
                try {
                    fileOutputStream.write(this.z.toString().getBytes(StandardCharsets.UTF_8));
                    fileOutputStream.close();
                } catch (Throwable th) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            } catch (IOException unused) {
                Log.w("CarPlusWireless", "Local report unavailable");
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private h i2(AirPlaySession.k kVar) {
        boolean z = false;
        Object[] objArr = 0;
        Object[] objArr2 = 0;
        if (!this.K0) {
            return new h(AirPlaySession.j1(kVar), z, objArr2 == true ? 1 : 0);
        }
        u4.a aVarB = u4.b(kVar.j(), kVar.h(), kVar.g());
        boolean zS = AvcEncoder.S(aVarB.i(), aVarB.h(), aVarB.g());
        return new h(zS ? new AirPlaySession.k(aVarB.i(), aVarB.h(), aVarB.g(), kVar.i()) : AirPlaySession.j1(kVar), zS, objArr == true ? 1 : 0);
    }

    private void j1(String str) {
        synchronized (this.s) {
            try {
                if (this.t.get()) {
                    return;
                }
                i1(io.carplay.app.prototype.v.d(this, this.C) + " stage=" + str);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean k1(long j2, m1.a aVar) {
        t4 t4Var;
        boolean zC;
        synchronized (this.s) {
            boolean z = false;
            if (g1(j2) && this.I && (t4Var = this.G) != null) {
                AirPlaySession airPlaySession = this.H;
                j6.a aVarK = t4Var.k();
                if (aVar instanceof m1.c) {
                    m1.c cVar = (m1.c) aVar;
                    if (!aVarK.j()) {
                        return false;
                    }
                    zC = t4Var.b(cVar.g());
                } else {
                    if (!aVarK.m()) {
                        return false;
                    }
                    zC = t4Var.c((m1.d) aVar);
                }
                synchronized (this.s) {
                    if (zC) {
                        try {
                            if (g1(j2) && this.I && this.H == airPlaySession && this.G == t4Var) {
                                z = true;
                            }
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                }
                return z;
            }
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void l2(String str) {
        i1(str);
        if (str.startsWith("NATIVE_VIDEO_WAIT ")) {
            synchronized (this.s) {
                try {
                    if (!this.t.get() && this.c0 && this.G != null && this.I && !this.S0.get()) {
                        a3("WAITING_NATIVE_VIDEO", "Native source initialized but has not produced video bytes; receiver display not confirmed");
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void m1(long j2, String str, MfiCertificateVerifier.DetectionState aVar, PairingSession.k.a aVar2) {
        String str2;
        synchronized (this.s) {
            try {
                if (h1(j2) && g1(this.c) && !this.m && this.g == null && !this.c0 && this.H == null && this.C == null && l1 == null && !h1 && aVar == MfiCertificateVerifier.DetectionState.COMPLETE_DETECTION_WITHOUT_LINK && aVar2 != null && (str2 = this.A) != null && str2.equalsIgnoreCase(str)) {
                    this.g = this.A;
                    this.h = "DETECT_ONLY_" + aVar2.name();
                    i1("IAP2_DETECT_ONLY_FALLBACK_ELIGIBLE termination=" + aVar2.name() + " next_existing_retry_only=true");
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private boolean m2(String str) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void n1(long j2, String str, MfiCertificateVerifier.InputFlags bVar) {
        String str2;
        synchronized (this.s) {
            try {
                if (h1(j2) && g1(this.c) && !this.m && this.g == null && !this.c0 && this.H == null && this.C == null && l1 == null && !h1 && bVar != null && (str2 = this.A) != null && str2.equalsIgnoreCase(str)) {
                    this.g = this.A;
                    this.h = "NO_INPUT_NORMAL_DEADLINE";
                    i1("IAP2_INITIAL_LINK_FALLBACK_ELIGIBLE reason=NO_INPUT_NORMAL_DEADLINE next_existing_retry_only=true");
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private Notification n2(String str) {
        PendingIntent activity = PendingIntent.getActivity(this, e1, new Intent(this, (Class<?>) MainActivity.class).addFlags(872415232), 201326592);
        return new Notification.Builder(this, d1).setSmallIcon(android.R.drawable.stat_notify_sync).setContentTitle("OcarPlay").setContentText(str).setContentIntent(activity).setOngoing(true).addAction(new Notification.Action.Builder((Icon) null, "连接首页", activity).build()).addAction(new Notification.Action.Builder((Icon) null, "停止", PendingIntent.getService(this, 75, new Intent(this, (Class<?>) WirelessCarPlayService.class).setAction(W0), 201326592)).build()).build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void o1(long j2, String str, MfiCertificateVerifier.e eVar) {
        String str2;
        synchronized (this.s) {
            try {
                if (h1(j2) && g1(this.c) && !this.p && this.o == null && !this.c0 && this.H == null && this.C == null && l1 == null && !h1 && eVar != null && (str2 = this.A) != null && str2.equalsIgnoreCase(str)) {
                    if (getSharedPreferences("wireless-ui", 0).getBoolean("enhanced_discovery_" + r2(this.A), true)) {
                        String string = getSharedPreferences(c1, 0).getString(r2(this.A), null);
                        if (string != null && string.equalsIgnoreCase(eVar.g())) {
                            this.n = this.A;
                            this.o = string;
                            i1("PHONE_IDENTITY_FALLBACK_ELIGIBLE reason=AUTHENTICATED_MODERN_START_DEADLINE next_existing_retry_only=true");
                        }
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private PairingTlv8.a o2() throws IOException {
        SharedPreferences sharedPreferences = getSharedPreferences("carplay-own-identity", 0);
        String string = sharedPreferences.getString("identifier", null);
        String string2 = sharedPreferences.getString("ed25519_seed", null);
        String string3 = sharedPreferences.getString("ed25519_public", null);
        if (string == null && string2 == null && string3 == null) {
            PairingTlv8.a aVarD = PairingTlv8.a.d(UUID.randomUUID().toString());
            byte[] bArrF = aVarD.f();
            try {
                if (!sharedPreferences.edit().putString("identifier", aVarD.e()).putString("ed25519_seed", Base64.encodeToString(bArrF, 2)).putString("ed25519_public", Base64.encodeToString(aVarD.g(), 2)).commit()) {
                    throw new IOException("own identity persistence failed");
                }
                Arrays.fill(bArrF, (byte) 0);
                return aVarD;
            } catch (Throwable th) {
                Arrays.fill(bArrF, (byte) 0);
                throw th;
            }
        }
        if (string == null || string2 == null || string3 == null) {
            SyntheticRefs.c("incomplete own pairing identity; explicit reset required");
            return null;
        }
        byte[] bArrDecode = Base64.decode(string2, 2);
        byte[] bArrDecode2 = Base64.decode(string3, 2);
        try {
            byte[] bytes = "CarPlus local identity consistency".getBytes(StandardCharsets.UTF_8);
            if (!DerCodec.h(bArrDecode2, bytes, DerCodec.g(bArrDecode, bytes))) {
                throw new IOException("own pairing identity inconsistent");
            }
            PairingTlv8.a aVar = new PairingTlv8.a(string, bArrDecode, bArrDecode2);
            Arrays.fill(bArrDecode, (byte) 0);
            return aVar;
        } catch (Throwable th2) {
            Arrays.fill(bArrDecode, (byte) 0);
            throw th2;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void p1(long j2, long j3, String str) {
        String str2;
        synchronized (this.s) {
            try {
                if (h1(j2) && g1(j3) && this.c == j3 && !this.r && this.q == null && !this.c0 && this.H == null && this.C == null && l1 == null && !h1 && (str2 = this.A) != null && str2.equalsIgnoreCase(str)) {
                    if (getSharedPreferences("wireless-ui", 0).getBoolean("enhanced_discovery_" + r2(this.A), true)) {
                        this.q = new j(this.A, j2, j3, -1L, 0);
                        i1("PHONE_SERVICE_EOF_RECOVERY_ELIGIBLE reason=NO_PROTOCOL_EXCHANGE_PEER_EOF next_existing_retry_only=true");
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private PairingTlv8.c p2() {
        return new f(this, getSharedPreferences("carplay-authenticated-peers", 0));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void q1(long j2, String str, MfiCertificateVerifier.e eVar) {
        String str2;
        synchronized (this.s) {
            try {
                if (h1(j2) && g1(this.c) && !this.f && this.e == null && !this.c0 && this.H == null && this.C == null && l1 == null && !h1 && eVar != null && (str2 = this.A) != null && str2.equalsIgnoreCase(str)) {
                    String string = getSharedPreferences(c1, 0).getString(r2(this.A), null);
                    if (string != null && string.equalsIgnoreCase(eVar.g())) {
                        this.d = this.A;
                        this.e = string;
                        i1("IAP2_AUTH_FIRST_FALLBACK_ELIGIBLE reason=AUTHENTICATED_MODERN_START_DEADLINE next_existing_retry_only=true");
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public i5 q2(long j2) {
        i5 i5VarE;
        synchronized (this.s) {
            if (!h1(j2)) {
                throw new IOException("IAP2_PHONE_IDENTIFICATION_CANCELLED");
            }
            if (this.a == null) {
                String strD = null;
                try {
                    BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BluetoothManager.class);
                    BluetoothAdapter adapter = bluetoothManager == null ? null : bluetoothManager.getAdapter();
                    if (adapter != null) {
                        strD = i5.d(adapter.getName());
                    }
                } catch (RuntimeException unused) {
                }
                i5.b bVar = i5.b.BLUETOOTH_ADAPTER;
                if (strD == null) {
                    bVar = i5.b.ANDROID_MODEL;
                    strD = i5.d(Build.MODEL);
                    if (strD == null) {
                        strD = "Android";
                    }
                }
                this.a = i1.c(strD, bVar);
            }
            this.b = j2;
            i5VarE = this.a.e();
        }
        return i5VarE;
    }

    private static Map<r6.a, Object> r1() {
        EnumMap enumMap = new EnumMap(r6.a.class);
        for (r6.a aVar : r6.a.values()) {
            enumMap.put(aVar, new Object());
        }
        return enumMap;
    }

    public static String r2(String str) {
        return w1.e("approved_leaf_", str.toUpperCase(Locale.ROOT).replace(':', '_'));
    }

    public static boolean s1() {
        return f1 && g1;
    }

    public static Boolean s2(Context context) {
        int intExtra;
        try {
            Intent intentRegisterReceiver = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            if (intentRegisterReceiver == null || !intentRegisterReceiver.hasExtra("plugged") || (intExtra = intentRegisterReceiver.getIntExtra("plugged", -1)) < 0) {
                return null;
            }
            return Boolean.valueOf(intExtra != 0);
        } catch (RuntimeException unused) {
        }
        return null;
    }

    private void t1(long j2, long j3, u5.a aVar) {
        j jVar = this.q;
        if (jVar == null) {
            return;
        }
        this.q = (aVar.i() > 0 && jVar.i() == j2 && jVar.h() == j3) ? new j(jVar.g(), j2, j3, aVar.h(), 0) : null;
    }

    private void t2() {
        synchronized (this.s) {
            if (!this.t.get() && this.b0 && this.I && this.H != null && this.G != null) {
                long jE = this.J.e();
                AirPlaySession airPlaySession = this.H;
                t4 t4Var = this.G;
                try {
                    ExecutorService executorService = this.y;
                    io.carplay.app.secured.h0 h0Var = new io.carplay.app.secured.h0(1);
                    h0Var.d = this;
                    h0Var.b = jE;
                    h0Var.c = airPlaySession;
                    h0Var.e = t4Var;
                    VarHandle.storeStoreFence();
                    executorService.execute(h0Var);
                } catch (RejectedExecutionException unused) {
                }
            }
        }
    }

    private void u1(StagedPeer stagedPeer, AirPlaySession airPlaySession) throws IOException {
        if (!airPlaySession.G0()) {
            SyntheticRefs.c("PAIRING_COMMIT_BEFORE_READY");
            return;
        }
        PairingTlv8.b bVarB2 = airPlaySession.b2();
        if (bVarB2 == null) {
            stagedPeer.e();
        } else {
            stagedPeer.f(bVarB2);
        }
    }

    private void u2(long j2, String str) {
        synchronized (this.s) {
            try {
                if (g1(j2)) {
                    if (!this.k0) {
                        this.n0++;
                    }
                    this.m0 = -1L;
                    i1(str.equals("PEER") ? "PEER_UI_FOREGROUND_REFRESH requested=true display_confirmed=false" : "LOCAL_UI_FOREGROUND_REFRESH requested=true display_confirmed=false");
                    B2(false, true);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private void v1(Network network, Iap2Csm.a aVar, String str, long j2, AirPlaySession airPlaySession, PairingSession.i iVar, StagedPeer stagedPeer) throws SocketException {
        if (!g1(j2) || !airPlaySession.G0() || !iVar.b()) {
            throw new SocketException("EXISTING_WIFI_CANDIDATE_RETIRED_AFTER_AUTHENTICATION");
        }
        synchronized (this.s) {
            try {
                if (!g1(j2) || this.H != airPlaySession || !airPlaySession.G0() || !g3(network)) {
                    throw new SocketException("EXISTING_WIFI_CANDIDATE_RETIRED_BEFORE_COMMIT");
                }
                u1(stagedPeer, airPlaySession);
                this.C = network;
                this.E = aVar;
                this.F = str;
                h1 = true;
                i1("WIFI_EXISTING_TRANSPORT_COMMITTED full_pinned_authentication=true same_connection_retained=true");
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void v2(boolean z, long j2) {
        AirPlaySession airPlaySession;
        synchronized (this.t0) {
            synchronized (this.s) {
                if (g1(j2) && (airPlaySession = this.H) != null && airPlaySession.e2() == z) {
                    this.s0 = new i(j2, this.H, this.G, z, this.j0, 0);
                    N2(null);
                }
            }
        }
    }

    private void w1() {
        x1(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void w2(long j2) {
        u2(j2, "PEER");
    }

    private void x1(AirPlaySession airPlaySession) {
        String str;
        synchronized (this.I0) {
            try {
                if (this.t.get()) {
                    return;
                }
                synchronized (this.s) {
                    AirPlaySession airPlaySession2 = this.H;
                    if (airPlaySession == null || airPlaySession2 == airPlaySession) {
                        boolean z = airPlaySession2 != null && airPlaySession2.i1(this.d0, this.e0);
                        int iJ = (z || !this.K0) ? this.d0 : this.f0;
                        int iH = (z || !this.K0) ? this.e0 : this.g0;
                        int iMax = Math.max(1, Math.min(60, Math.min(this.y0, this.h0)));
                        boolean z2 = (this.K0 && q6.i(this.d0, this.e0, iJ, iH).o()) || this.y0 > iMax;
                        AirPlaySession.k kVarF2 = airPlaySession2 == null ? null : airPlaySession2.f2();
                        boolean z3 = this.K0 && kVarF2 != null && u4.a(this.d0, this.e0, kVarF2.j(), kVarF2.h());
                        boolean z4 = z3 && AvcEncoder.R(kVarF2.j(), kVarF2.h(), iMax);
                        String str2 = z ? "NATIVE_DISPLAY_MATCH" : "ESTABLISHED_SOURCE_POLICY";
                        if (z3 && !z4) {
                            str2 = "ALIGNMENT_CANVAS_ENCODER_UNCONFIRMED";
                        }
                        if (this.K0 && ((z2 || z4) && kVarF2 != null)) {
                            if (kVarF2.j() == iJ && kVarF2.h() == iH) {
                                str2 = "AUTHENTICATED_RECEIVER";
                            } else if (z4 || AvcEncoder.R(kVarF2.j(), kVarF2.h(), iMax)) {
                                iJ = kVarF2.j();
                                iH = kVarF2.h();
                                str2 = "AUTHENTICATED_RECEIVER";
                            } else {
                                str2 = "LEGACY_CANVAS_ENCODER_UNCONFIRMED";
                            }
                        }
                        q6 q6VarR = (this.K0 && this.M0) ? q6.r(this.d0, this.e0, iJ, iH) : q6.i(this.d0, this.e0, iJ, iH);
                        if (q6VarR.equals(this.F0) && this.G0 == iMax && this.H0 == this.y0) {
                            return;
                        }
                        this.F0 = null;
                        this.G0 = 0;
                        this.H0 = 0;
                        AvcEncoder avcEncoder = this.z0;
                        this.z0 = null;
                        this.A0 = null;
                        long j2 = this.J0 + 1;
                        this.J0 = j2;
                        this.E0 = false;
                        AirPlaySession.g gVarI = this.N0.i();
                        byte[] bArrH = gVarI == null ? null : gVarI.h();
                        byte[] bArrG = gVarI == null ? null : gVarI.g();
                        if (avcEncoder != null) {
                            avcEncoder.close();
                        }
                        boolean z5 = (this.K0 && (q6VarR.o() || z4)) || this.y0 > iMax;
                        if (airPlaySession2 == null) {
                            return;
                        }
                        boolean z6 = z5;
                        try {
                            airPlaySession2.s2(null);
                            airPlaySession2.W1(null, z6 ? q6VarR.h() : this.d0, z6 ? q6VarR.g() : this.e0);
                            synchronized (this.s) {
                                if (!this.t.get() && this.H == airPlaySession2) {
                                    this.F0 = q6VarR;
                                    this.G0 = iMax;
                                    this.H0 = this.y0;
                                    int i2 = this.f0;
                                    int i3 = this.g0;
                                    boolean z7 = this.K0;
                                    boolean z8 = this.M0;
                                    if (kVarF2 == null) {
                                        str = "UNKNOWN";
                                    } else {
                                        str = kVarF2.j() + "x" + kVarF2.h();
                                    }
                                    int iH2 = z6 ? q6VarR.h() : this.d0;
                                    int iG = z6 ? q6VarR.g() : this.e0;
                                    byte[] bArr = bArrG;
                                    q6 q6Var = q6VarR;
                                    i1("VIDEO_CANVAS_PLAN policy=" + str2 + " native_request=" + i2 + "x" + i3 + " auto_fit=" + z7 + " fill_display=" + z8 + " receiver=" + str + " output=" + iH2 + "x" + iG + " source=" + this.d0 + "x" + this.e0 + " target_fps=" + iMax + " display_confirmed=false");
                                    this.K = null;
                                    if (!z6) {
                                        getSharedPreferences(b1, 0).edit().putString("fit_mode", "native").putInt("output_width", this.d0).putInt("output_height", this.e0).putInt("output_fps", this.y0).apply();
                                        i1("VIDEO_ROUTE_NATIVE source=" + this.d0 + "x" + this.e0 + " requested=" + this.f0 + "x" + this.g0 + " source_declared_fps=" + this.y0 + " reason=COMPRESSED_PASSTHROUGH");
                                        return;
                                    }
                                    this.A0 = q6Var;
                                    getSharedPreferences(b1, 0).edit().putString("fit_mode", "starting").putInt("output_width", q6Var.h()).putInt("output_height", q6Var.g()).apply();
                                    i1("AUTO_FIT_START source=" + this.d0 + "x" + this.e0 + " canvas=" + q6Var.h() + "x" + q6Var.g() + " viewport=" + q6Var.k() + "," + q6Var.s() + "," + q6Var.t() + "," + q6Var.j() + " target_fps=" + iMax + " reason=" + (this.y0 > iMax ? "FRAME_RATE_LIMIT" : "GEOMETRY") + " display_mode=" + ((this.K0 && this.M0) ? "STRETCH" : "FIT"));
                                    byte[] bArr2 = bArrH;
                                    AvcEncoder avcEncoder2 = new AvcEncoder(q6Var, this.y0, iMax, new d(this, j2, q6Var, new AtomicBoolean()), avcEncoder);
                                    if (bArr2 != null && bArr != null) {
                                        avcEncoder2.J(bArr2, bArr);
                                    }
                                    this.z0 = avcEncoder2;
                                }
                            }
                        } catch (IOException e2) {
                            synchronized (this.s) {
                                if (!this.t.get() && this.H == airPlaySession2 && airPlaySession2.G0()) {
                                    throw e2;
                                }
                                i1("VIDEO_ROUTE_RETIRED native_session_retained=true");
                            }
                        }
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void x2(String str, long j2) {
        y2(str, j2, -1L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void y1(Network network, Iap2Csm.a aVar, String str, long j2) {
        z1(network, aVar, str, j2, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void y2(String str, long j2, long j3) {
        synchronized (this.s) {
            if (this.t.get()) {
                return;
            }
            u5 u5Var = this.J;
            u5.a aVarF = j3 < 0 ? u5Var.f(j2, SystemClock.elapsedRealtime()) : u5Var.g(j3, j2, SystemClock.elapsedRealtime());
            if (aVarF == null) {
                return;
            }
            t1(j3, j2, aVarF);
            if (this.O == 0) {
                this.O = System.nanoTime();
            }
            this.D = null;
            this.I = false;
            g1 = false;
            try {
                ExecutorService executorService = this.x;
                io.carplay.app.secured.q0 q0Var = new io.carplay.app.secured.q0(2);
                q0Var.b = this;
                q0Var.c = str;
                q0Var.d = aVarF;
                VarHandle.storeStoreFence();
                executorService.execute(q0Var);
            } catch (RejectedExecutionException unused) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Bottom block not found for handler: all -> 0x0376 */
    /* JADX WARN: Bottom block not found for handler: all -> 0x037a */
    /* JADX WARN: Bottom block not found for handler: all -> 0x037e */
    /* JADX WARN: Bottom block not found for handler: all -> 0x0382 */
    /* JADX WARN: Code duplicated, block: B:103:0x017e  */
    /* JADX WARN: Code duplicated, block: B:110:0x0194  */
    /* JADX WARN: Code duplicated, block: B:117:0x01a5 A[Catch: all -> 0x01df, TryCatch #30 {, blocks: (B:115:0x019f, B:117:0x01a5, B:119:0x01a9, B:121:0x01b1, B:124:0x01b9, B:126:0x01d1, B:127:0x01d4, B:136:0x01e1, B:137:0x01e8, B:138:0x01e9, B:139:0x01f0), top: B:368:0x019f }] */
    /* JADX WARN: Code duplicated, block: B:150:0x0210 A[Catch: all -> 0x0217, DONT_GENERATE, TRY_LEAVE, TryCatch #27 {, blocks: (B:148:0x020a, B:150:0x0210, B:156:0x021a, B:157:0x0270), top: B:364:0x020a }] */
    /* JADX WARN: Code duplicated, block: B:152:0x0213  */
    /* JADX WARN: Code duplicated, block: B:156:0x021a A[Catch: all -> 0x0217, TRY_ENTER, TryCatch #27 {, blocks: (B:148:0x020a, B:150:0x0210, B:156:0x021a, B:157:0x0270), top: B:364:0x020a }] */
    /* JADX WARN: Code duplicated, block: B:160:0x02a5 A[Catch: all -> 0x00e2, Exception -> 0x02ab, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:165:0x02b4 A[Catch: all -> 0x00e2, Exception -> 0x02ab, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:169:0x02be  */
    /* JADX WARN: Code duplicated, block: B:170:0x02c0 A[Catch: all -> 0x02cd, TryCatch #25 {all -> 0x02cd, blocks: (B:167:0x02ba, B:171:0x02c2, B:173:0x02c8, B:178:0x02d3, B:180:0x02da, B:181:0x02dd, B:179:0x02d8, B:176:0x02cf, B:170:0x02c0), top: B:359:0x02ba }] */
    /* JADX WARN: Code duplicated, block: B:173:0x02c8 A[Catch: all -> 0x02cd, TryCatch #25 {all -> 0x02cd, blocks: (B:167:0x02ba, B:171:0x02c2, B:173:0x02c8, B:178:0x02d3, B:180:0x02da, B:181:0x02dd, B:179:0x02d8, B:176:0x02cf, B:170:0x02c0), top: B:359:0x02ba }] */
    /* JADX WARN: Code duplicated, block: B:176:0x02cf A[Catch: all -> 0x02cd, TryCatch #25 {all -> 0x02cd, blocks: (B:167:0x02ba, B:171:0x02c2, B:173:0x02c8, B:178:0x02d3, B:180:0x02da, B:181:0x02dd, B:179:0x02d8, B:176:0x02cf, B:170:0x02c0), top: B:359:0x02ba }] */
    /* JADX WARN: Code duplicated, block: B:178:0x02d3 A[Catch: all -> 0x02cd, TryCatch #25 {all -> 0x02cd, blocks: (B:167:0x02ba, B:171:0x02c2, B:173:0x02c8, B:178:0x02d3, B:180:0x02da, B:181:0x02dd, B:179:0x02d8, B:176:0x02cf, B:170:0x02c0), top: B:359:0x02ba }] */
    /* JADX WARN: Code duplicated, block: B:179:0x02d8 A[Catch: all -> 0x02cd, TryCatch #25 {all -> 0x02cd, blocks: (B:167:0x02ba, B:171:0x02c2, B:173:0x02c8, B:178:0x02d3, B:180:0x02da, B:181:0x02dd, B:179:0x02d8, B:176:0x02cf, B:170:0x02c0), top: B:359:0x02ba }] */
    /* JADX WARN: Code duplicated, block: B:187:0x02ea A[Catch: all -> 0x00e2, Exception -> 0x02ab, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:192:0x02f6 A[Catch: all -> 0x030d, TryCatch #24 {all -> 0x030d, blocks: (B:190:0x02f0, B:192:0x02f6, B:194:0x0305, B:198:0x0316, B:197:0x030f, B:199:0x031a), top: B:357:0x02f0 }] */
    /* JADX WARN: Code duplicated, block: B:194:0x0305 A[Catch: all -> 0x030d, TryCatch #24 {all -> 0x030d, blocks: (B:190:0x02f0, B:192:0x02f6, B:194:0x0305, B:198:0x0316, B:197:0x030f, B:199:0x031a), top: B:357:0x02f0 }] */
    /* JADX WARN: Code duplicated, block: B:197:0x030f A[Catch: all -> 0x030d, TryCatch #24 {all -> 0x030d, blocks: (B:190:0x02f0, B:192:0x02f6, B:194:0x0305, B:198:0x0316, B:197:0x030f, B:199:0x031a), top: B:357:0x02f0 }] */
    /* JADX WARN: Code duplicated, block: B:201:0x031d A[Catch: all -> 0x00e2, Exception -> 0x02ab, TRY_ENTER, TRY_LEAVE, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:203:0x0322  */
    /* JADX WARN: Code duplicated, block: B:205:0x0326 A[Catch: all -> 0x00e2, Exception -> 0x02ab, TRY_ENTER, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:207:0x032f A[Catch: all -> 0x00e2, Exception -> 0x02ab, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:212:0x0345 A[Catch: all -> 0x034a, TryCatch #23 {all -> 0x034a, blocks: (B:210:0x033f, B:212:0x0345, B:218:0x034f, B:217:0x034c), top: B:355:0x033f }] */
    /* JADX WARN: Code duplicated, block: B:217:0x034c A[Catch: all -> 0x034a, TryCatch #23 {all -> 0x034a, blocks: (B:210:0x033f, B:212:0x0345, B:218:0x034f, B:217:0x034c), top: B:355:0x033f }] */
    /* JADX WARN: Code duplicated, block: B:220:0x0352  */
    /* JADX WARN: Code duplicated, block: B:25:0x0079 A[Catch: all -> 0x0388, Exception -> 0x038c, TRY_ENTER, TRY_LEAVE, TryCatch #20 {Exception -> 0x038c, blocks: (B:6:0x0014, B:8:0x001a, B:19:0x004d, B:25:0x0079), top: B:319:0x0014 }] */
    /* JADX WARN: Code duplicated, block: B:269:0x03a1 A[Catch: all -> 0x00e2, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:271:0x03a5 A[Catch: all -> 0x00e2, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:274:0x03ac A[Catch: all -> 0x00e2, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:276:0x03b0 A[Catch: all -> 0x00e2, TRY_LEAVE, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:283:0x03db A[Catch: all -> 0x00e2, TRY_ENTER, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:286:0x03e9 A[Catch: all -> 0x00e2, LOOP:0: B:284:0x03e3->B:286:0x03e9, LOOP_END, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:288:0x03f5 A[Catch: all -> 0x00e2, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:291:0x03fb A[Catch: all -> 0x00e2, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:296:0x0405 A[Catch: all -> 0x0410, TryCatch #26 {all -> 0x0410, blocks: (B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:361:0x0401, outer: #0 }] */
    /* JADX WARN: Code duplicated, block: B:302:0x0419 A[Catch: all -> 0x00e2, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:311:0x0436 A[Catch: all -> 0x00e2, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:312:0x0453 A[Catch: all -> 0x00e2, TRY_LEAVE, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:313:0x046f A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:323:0x0116 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:328:0x019c A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:337:0x00ab A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:348:0x00f9 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:353:0x0134 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:355:0x033f A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:357:0x02f0 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:359:0x02ba A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:361:0x0401 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:364:0x020a A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:368:0x019f A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:375:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:376:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:379:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:54:0x00ea A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:55:0x00ec A[Catch: all -> 0x00e2, Exception -> 0x00e6, TRY_LEAVE, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:60:0x00ff A[Catch: all -> 0x0102, TryCatch #17 {all -> 0x0102, blocks: (B:58:0x00f9, B:60:0x00ff, B:63:0x0107), top: B:348:0x00f9 }] */
    /* JADX WARN: Code duplicated, block: B:65:0x010a A[Catch: all -> 0x00e2, Exception -> 0x00e6, TRY_ENTER, TRY_LEAVE, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:67:0x010f  */
    /* JADX WARN: Code duplicated, block: B:69:0x0113 A[Catch: all -> 0x00e2, Exception -> 0x036d, TRY_ENTER, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:73:0x0121 A[Catch: all -> 0x0127, TryCatch #3 {all -> 0x0127, blocks: (B:71:0x0116, B:73:0x0121, B:79:0x012d, B:80:0x0130), top: B:323:0x0116 }] */
    /* JADX WARN: Code duplicated, block: B:78:0x012c  */
    /* JADX WARN: Code duplicated, block: B:85:0x013a A[Catch: all -> 0x0145, TryCatch #21 {all -> 0x0145, blocks: (B:83:0x0134, B:85:0x013a, B:87:0x0142, B:91:0x014b, B:92:0x014d), top: B:353:0x0134 }] */
    /* JADX WARN: Code duplicated, block: B:90:0x014a  */
    /* JADX WARN: Code duplicated, block: B:94:0x0150  */
    /* JADX WARN: Code duplicated, block: B:97:0x0158 A[Catch: all -> 0x00e2, Exception -> 0x00e6, TRY_LEAVE, TryCatch #0 {all -> 0x00e2, blocks: (B:27:0x0081, B:29:0x00a8, B:30:0x00aa, B:46:0x00d8, B:49:0x00de, B:267:0x039b, B:269:0x03a1, B:271:0x03a5, B:272:0x03a8, B:274:0x03ac, B:276:0x03b0, B:277:0x03b3, B:280:0x03b8, B:283:0x03db, B:284:0x03e3, B:286:0x03e9, B:288:0x03f5, B:291:0x03fb, B:292:0x03fe, B:293:0x0400, B:300:0x0413, B:302:0x0419, B:304:0x041f, B:308:0x042f, B:309:0x0430, B:311:0x0436, B:312:0x0453, B:55:0x00ec, B:56:0x00f1, B:57:0x00f8, B:65:0x010a, B:69:0x0113, B:70:0x0115, B:81:0x0131, B:82:0x0133, B:95:0x0152, B:97:0x0158, B:100:0x016e, B:101:0x0178, B:104:0x0188, B:111:0x0198, B:113:0x019c, B:114:0x019e, B:128:0x01d5, B:130:0x01d9, B:141:0x01f2, B:144:0x01fb, B:146:0x0203, B:147:0x0209, B:158:0x0271, B:160:0x02a5, B:163:0x02b0, B:165:0x02b4, B:166:0x02b9, B:184:0x02e0, B:185:0x02e1, B:187:0x02ea, B:188:0x02ed, B:189:0x02ef, B:201:0x031d, B:205:0x0326, B:207:0x032f, B:208:0x0332, B:209:0x033e, B:223:0x0357, B:225:0x0359, B:227:0x035b, B:237:0x0372, B:243:0x0379, B:247:0x037d, B:251:0x0381, B:294:0x0401, B:296:0x0405, B:299:0x0412), top: B:319:0x0014, inners: #4, #26 }] */
    /* JADX WARN: Code duplicated, block: B:99:0x016c  */
    /* JADX WARN: Instruction removed from duplicated block: B:311:0x0436, please report this as an issue */
    /* JADX WARN: Instruction removed from duplicated block: B:312:0x0453, please report this as an issue */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void z1(android.net.Network r25, io.carplay.app.secured.Iap2Csm.a r26, java.lang.String r27, long r28, io.carplay.app.prototype.PairingSession.i r30) throws java.lang.Throwable {
        /*
            Method dump skipped, instruction units count: 1146
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: io.carplay.app.prototype.WirelessCarPlayService.z1(android.net.Network, io.carplay.app.secured.Iap2Csm$a, java.lang.String, long, io.carplay.app.prototype.PairingSession$i):void");
    }

    private void z2(AirPlaySession airPlaySession, long j2) {
        try {
            ExecutorService executorService = this.y;
            d7 d7Var = new d7();
            d7Var.a = this;
            d7Var.b = j2;
            d7Var.c = airPlaySession;
            VarHandle.storeStoreFence();
            executorService.execute(d7Var);
        } catch (RejectedExecutionException unused) {
        }
    }

    public void G2(String str) {
        int i2;
        if (str.startsWith("VIDEO_FLOW ")) {
            Matcher matcher = Pattern.compile("(?:^| )sent_fps_x100=([0-9]{1,5})(?: |$)").matcher(str);
            if (!matcher.find() || (i2 = Integer.parseInt(matcher.group(1))) > 24000) {
                return;
            }
            getSharedPreferences(b1, 0).edit().putInt("measured_sent_fps_x100", i2).putLong("measured_sent_fps_at_ms", SystemClock.elapsedRealtime()).apply();
        }
    }

    public t4.VideoConfig j2() {
        return new t4.VideoConfig(0, this.f0, this.g0, false, this.h0, this.K0 && this.L0);
    }

    public AirPlaySession.k k2(AirPlaySession.k kVar) {
        h hVarI2 = i2(kVar);
        this.L0 = hVarI2.g();
        i1("NATIVE_GEOMETRY_PREFLIGHT supported_candidate=" + hVarI2.g() + " native_encoder_acceptance_confirmed=false fps=" + hVarI2.h().g());
        return hVarI2.h();
    }

    public void l1(int i2, int i3, int i4) {
        String str;
        synchronized (this.I0) {
            try {
                if (this.t.get()) {
                    return;
                }
                boolean z = this.x0;
                this.d0 = i2;
                this.e0 = i3;
                this.y0 = i4;
                this.x0 = true;
                if (!z) {
                    j1("FIRST_NATIVE_FORMAT");
                }
                AirPlaySession airPlaySessionF1 = F1();
                if (airPlaySessionF1 != null) {
                    airPlaySessionF1.k1(i2, i3);
                }
                getSharedPreferences(b1, 0).edit().putInt("native_width", i2).putInt("native_height", i3).putInt("native_fps", i4).apply();
                i1("Actual native video format=" + i2 + "x" + i3 + " fps=" + i4);
                int i5 = this.h0;
                if (i4 < this.h0) {
                    str = "SOURCE_DECLARED_LOWER";
                } else {
                    str = i4 > this.h0 ? "OUTPUT_RATE_ADAPTATION_REQUIRED" : "NONE";
                }
                i1("VIDEO_SOURCE_RATE requested=" + i5 + " init_declared=" + i4 + " cadence_confirmed=false fallback=" + str);
                w1();
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onDestroy() {
        e3("Service destroyed; all owned sessions closed");
        super.onDestroy();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i2, int i3) {
        String str;
        Network networkFromNetworkHandle;
        String action = intent == null ? W0 : intent.getAction();
        if (W0.equals(action)) {
            e3("Stopped by user");
            return 2;
        }
        if (a1.equals(action)) {
            if (!this.t.get() && this.b0) {
                t2();
            }
            return 2;
        }
        if (Y0.equals(action) || Z0.equals(action)) {
            if (!this.t.get() && this.b0) {
                if (Z0.equals(action)) {
                    u2(this.J.e(), "LOCAL");
                } else {
                    B2(true, false);
                }
            }
            return 2;
        }
        if (X0.equals(action)) {
            if (!g1 || this.t.get()) {
                i1("Native source not armed: receiver session is not ready");
                return 2;
            }
            Z2();
            return 2;
        }
        if (this.b0) {
            i1("Session already running; no automatic second connection");
            return 2;
        }
        EcdsaSignature.a aVarK0 = MainActivity.K0(this);
        if (aVarK0 == null || !aVarK0.a(this)) {
            i1("RELEASE_SESSION_BLOCKED no_fresh_integrity_permit");
            e3("Release verification required before starting a new session");
            return 2;
        }
        this.b0 = true;
        l1 = null;
        h1 = false;
        f1 = true;
        AppLogger.a(this);
        getSharedPreferences(b1, 0).edit().remove("native_width").remove("native_height").remove("native_fps").remove("receiver_width").remove("receiver_height").remove("receiver_fps").remove("output_fps").remove("audio_state").remove("fit_mode").remove("output_width").remove("output_height").remove("measured_sent_fps_x100").remove("measured_sent_fps_at_ms").remove("native_foreground_state").remove("native_exact_geometry_requested").remove("source_requested_width").remove("source_requested_height").remove("connection_route").apply();
        this.K0 = getSharedPreferences("wireless-ui", 0).getBoolean("auto_fit", true);
        this.M0 = intent != null && intent.getBooleanExtra("fill_display", false);
        getSharedPreferences(b1, 0).edit().putBoolean("fill_display", this.K0 && this.M0).apply();
        String stringExtra = intent == null ? null : intent.getStringExtra("selectedBtAddress");
        this.A = stringExtra;
        if (stringExtra == null || !BluetoothAdapter.checkBluetoothAddress(stringExtra)) {
            e3("Choose one paired receiver first");
            return 2;
        }
        ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(new NotificationChannel(d1, "Car+ wireless session", 2));
        startForeground(e1, n2("正在准备连接接收端"), 16);
        M1();
        a3("CHECKING", "Wireless session started; continuous until user stop/source exit/unrecoverable error; phone USB firmware interface unimplemented and unverified");
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.PackageInfoFlags.of(0L));
            i1("APP_BUILD version=" + packageInfo.versionName + " code=" + packageInfo.getLongVersionCode());
        } catch (Exception unused) {
            i1("APP_BUILD unavailable");
        }
        if (intent == null || !intent.getBooleanExtra("useCurrentWifi", false)) {
            Network networkQ2 = Q2();
            this.j = networkQ2;
            boolean z = networkQ2 != null;
            this.k = z;
            if (z) {
                i1("CURRENT_WIFI_REUSE verified_previous_transport=true fresh_authentication_required=true network_switch=false");
            }
        } else {
            long longExtra = intent.getLongExtra("currentWifiHandle", 0L);
            if (longExtra == 0) {
                networkFromNetworkHandle = null;
            } else {
                try {
                    networkFromNetworkHandle = Network.fromNetworkHandle(longExtra);
                } catch (RuntimeException unused2) {
                    this.j = null;
                }
            }
            this.j = networkFromNetworkHandle;
            Network network = this.j;
            if (network == null || !g3(network)) {
                e3("Selected current WLAN is unavailable; connect the receiver WLAN and select it again");
                return 2;
            }
        }
        SharedPreferences.Editor editorEdit = getSharedPreferences(b1, 0).edit();
        if (this.j == null) {
            str = "bluetooth";
        } else {
            str = this.k ? "retained_wifi" : "selected_wifi";
        }
        editorEdit.putString("connection_route", str).apply();
        W2();
        return 2;
    }

    public static final class h extends w1 {
        private final AirPlaySession.k a;
        private final boolean b;

        private h(AirPlaySession.k kVar, boolean z) {
            this.a = kVar;
            this.b = z;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof h)) {
                return false;
            }
            h hVar = (h) obj;
            return this.b == hVar.b && Objects.equals(this.a, hVar.a);
        }

        public boolean g() {
            return this.b;
        }

        public AirPlaySession.k h() {
            return this.a;
        }

        public final int hashCode() {
            boolean z = this.b;
            AirPlaySession.k kVar = this.a;
            return Objects.hashCode(kVar) + (Boolean.hashCode(z) * 31);
        }

        public final String toString() {
            Object[] objArr = {this.a, Boolean.valueOf(this.b)};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
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

        public /* synthetic */ h(AirPlaySession.k kVar, boolean z, int i) {
            this(kVar, z);
        }
    }

    public static final class g extends w1 {
        private final int a;
        private final int b;
        private final int c;

        private g(int i, int i2, int i3) {
            this.a = i;
            this.b = i2;
            this.c = i3;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof g)) {
                return false;
            }
            g gVar = (g) obj;
            return this.a == gVar.a && this.b == gVar.b && this.c == gVar.c;
        }

        public int g() {
            return this.c;
        }

        public int h() {
            return this.b;
        }

        public final int hashCode() {
            int i = this.a;
            return (((i * 31) + this.b) * 31) + this.c;
        }

        public int i() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(g.class, sb, "[");
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

        public /* synthetic */ g(int i, int i2, int i3, int i4) {
            this(i, i2, i3);
        }
    }

    public static final class k extends w1 {
        private final long a;
        private final AirPlaySession b;
        private final AirPlaySession.k c;

        private k(long j, AirPlaySession airPlaySession, AirPlaySession.k kVar) {
            this.a = j;
            this.b = airPlaySession;
            this.c = kVar;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof k)) {
                return false;
            }
            k kVar = (k) obj;
            return this.a == kVar.a && Objects.equals(this.b, kVar.b) && Objects.equals(this.c, kVar.c);
        }

        public AirPlaySession.k g() {
            return this.c;
        }

        public AirPlaySession h() {
            return this.b;
        }

        public final int hashCode() {
            long j = this.a;
            AirPlaySession airPlaySession = this.b;
            AirPlaySession.k kVar = this.c;
            return Objects.hashCode(kVar) + ((Objects.hashCode(airPlaySession) + (Long.hashCode(j) * 31)) * 31);
        }

        public long i() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), this.b, this.c};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(k.class, sb, "[");
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

        public /* synthetic */ k(long j, AirPlaySession airPlaySession, AirPlaySession.k kVar, int i) {
            this(j, airPlaySession, kVar);
        }
    }

    public static final class l extends w1 {
        private final String a;
        private final String b;
        private final WifiNetworkMonitor.e c;

        private l(String str, String str2, WifiNetworkMonitor.e eVar) {
            this.a = str;
            this.b = str2;
            this.c = eVar;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof l)) {
                return false;
            }
            l lVar = (l) obj;
            return Objects.equals(this.a, lVar.a) && Objects.equals(this.b, lVar.b) && Objects.equals(this.c, lVar.c);
        }

        public String g() {
            return this.b;
        }

        public String h() {
            return this.a;
        }

        public final int hashCode() {
            String str = this.a;
            String str2 = this.b;
            WifiNetworkMonitor.e eVar = this.c;
            return Objects.hashCode(eVar) + w1.b(str2, Objects.hashCode(str) * 31, 31);
        }

        public WifiNetworkMonitor.e i() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, this.c};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(l.class, sb, "[");
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

        public /* synthetic */ l(String str, String str2, WifiNetworkMonitor.e eVar, int i) {
            this(str, str2, eVar);
        }
    }

    public static final class j extends w1 {
        private final String a;
        private final long b;
        private final long c;
        private final long d;

        private j(String str, long j, long j2, long j3) {
            this.a = str;
            this.b = j;
            this.c = j2;
            this.d = j3;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof j)) {
                return false;
            }
            j jVar = (j) obj;
            return this.b == jVar.b && this.c == jVar.c && this.d == jVar.d && Objects.equals(this.a, jVar.a);
        }

        public String g() {
            return this.a;
        }

        public long h() {
            return this.c;
        }

        public final int hashCode() {
            long j = this.b;
            long j2 = this.c;
            long j3 = this.d;
            String str = this.a;
            return Objects.hashCode(str) + w1.a(j3, w1.a(j2, Long.hashCode(j) * 31, 31), 31);
        }

        public long i() {
            return this.b;
        }

        public long j() {
            return this.d;
        }

        public final String toString() {
            Object[] objArr = {this.a, Long.valueOf(this.b), Long.valueOf(this.c), Long.valueOf(this.d)};
            String[] strArrSplit = "a;b;c;d".length() == 0 ? new String[0] : "a;b;c;d".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(j.class, sb, "[");
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

        public /* synthetic */ j(String str, long j, long j2, long j3, int i) {
            this(str, j, j2, j3);
        }
    }

    public static final class i extends w1 {
        private final long a;
        private final AirPlaySession b;
        private final t4 c;
        private final boolean d;
        private final long e;

        private i(long j, AirPlaySession airPlaySession, t4 t4Var, boolean z, long j2) {
            this.a = j;
            this.b = airPlaySession;
            this.c = t4Var;
            this.d = z;
            this.e = j2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof i)) {
                return false;
            }
            i iVar = (i) obj;
            return this.d == iVar.d && this.a == iVar.a && this.e == iVar.e && Objects.equals(this.b, iVar.b) && Objects.equals(this.c, iVar.c);
        }

        public boolean g() {
            return this.d;
        }

        public t4 h() {
            return this.c;
        }

        public final int hashCode() {
            boolean z = this.d;
            long j = this.a;
            long j2 = this.e;
            AirPlaySession airPlaySession = this.b;
            t4 t4Var = this.c;
            return Objects.hashCode(t4Var) + ((Objects.hashCode(airPlaySession) + w1.a(j2, w1.a(j, Boolean.hashCode(z) * 31, 31), 31)) * 31);
        }

        public AirPlaySession i() {
            return this.b;
        }

        public long j() {
            return this.a;
        }

        public long k() {
            return this.e;
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), this.b, this.c, Boolean.valueOf(this.d), Long.valueOf(this.e)};
            String[] strArrSplit = "a;b;c;d;e".length() == 0 ? new String[0] : "a;b;c;d;e".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(i.class, sb, "[");
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

        public /* synthetic */ i(long j, AirPlaySession airPlaySession, t4 t4Var, boolean z, long j2, int i) {
            this(j, airPlaySession, t4Var, z, j2);
        }
    }
}
