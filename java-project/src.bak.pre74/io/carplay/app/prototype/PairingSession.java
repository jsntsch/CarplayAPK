package io.carplay.app.prototype;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.InetAddresses;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.TransportInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.SystemClock;
import io.carplay.app.secured.ControlChannel;
import io.carplay.app.secured.Iap2Csm;
import io.carplay.app.secured.MfiCertificateVerifier;
import io.carplay.app.secured.SyntheticRefs;
import io.carplay.app.secured.h5;
import io.carplay.app.secured.i5;
import io.carplay.app.secured.s6;
import io.carplay.app.secured.t6;
import io.carplay.app.secured.u6;
import io.carplay.app.secured.v6;
import io.carplay.app.secured.w1;
import io.carplay.app.secured.w6;
import io.carplay.app.secured.x6;
import io.carplay.app.secured.y6;
import io.carplay.app.secured.z6;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.VarHandle;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/* JADX INFO: loaded from: classes.dex */
/**
 * PairingSession
 *
 * <p>原混淆名：{@code io.carplay.app.prototype.r0}（jadx 产物 r0.java）
 *
 * <p>命名依据：AirPlay 配对 UUID 00000000-deca-fade-deca-deafdecacafe；对应 hypercar pairing.rs
 */
public final class PairingSession implements AutoCloseable {
    public static final UUID T = UUID.fromString("00000000-deca-fade-deca-deafdecacafe");
    public static final UUID U = UUID.fromString("00000000-deca-fade-deca-deafdecacaff");
    private static final long V = 2147483647L;
    private static final long W = 3000;
    private static final int X = 3;
    private LinkProperties A;
    private AirplayServiceProbe B;
    private CarPlayCtrlAdvertiser C;
    private r.a D;
    private boolean E;
    private int F;
    private int G;
    private boolean H;
    private volatile g I;
    private boolean J;
    private boolean K;
    private Runnable L;
    private long M;
    private Runnable N;
    private boolean O;
    private boolean P;
    private boolean Q;
    private h R;
    private final Runnable S;
    private final Context a;
    private final k b;
    private final String c;
    private final boolean d;
    private final boolean e;
    private final boolean f;
    private boolean g;
    private final Handler h;
    private final Object i;
    private final ExecutorService j;
    private BluetoothServerSocket k;
    private BluetoothSocket l;
    private BluetoothSocket m;
    private h5 n;
    private h5 o;
    private BluetoothRfcommProfiles p;
    private boolean q;
    private boolean r;
    private ConnectivityManager.NetworkCallback s;
    private boolean t;
    private boolean u;
    private boolean v;
    private boolean w;
    private Iap2Csm.g x;
    private Iap2Csm.a y;
    private Network z;

    public class a implements MfiCertificateVerifier.c {
        final /* synthetic */ BluetoothSocket a;
        final /* synthetic */ OutputStream b;
        final /* synthetic */ PairingSession c;

        public a(PairingSession pairingSession, BluetoothSocket bluetoothSocket, OutputStream outputStream) {
            this.a = bluetoothSocket;
            this.b = outputStream;
            this.c = pairingSession;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void o(Iap2Csm.a aVar) {
            this.c.G0(aVar);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void p(String str, byte[] bArr) {
            this.c.b.r(str, bArr);
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public void a(String str, byte[] bArr) throws IOException {
            synchronized (this.c.i) {
                try {
                    if (this.c.u) {
                        throw new IOException("MFI_FIRST_USE_CANCELLED");
                    }
                    this.c.b.o(str, (byte[]) bArr.clone());
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public void b(Iap2Csm.a aVar) {
            this.c.E1(aVar);
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public i5 c() throws IOException {
            if (!this.c.isClosed()) {
                return this.c.b.c();
            }
            SyntheticRefs.c("Cancelled");
            return null;
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public boolean d() {
            return !this.c.isClosed() && this.c.b.d();
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public void f(Iap2Csm.g gVar) {
            this.c.F1(gVar);
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public void g(String str, byte[] bArr) {
            byte[] bArr2 = (byte[]) bArr.clone();
            Handler handler = this.c.h;
            b0 b0Var = new b0(1);
            b0Var.b = this;
            b0Var.c = str;
            b0Var.d = bArr2;
            VarHandle.storeStoreFence();
            handler.post(b0Var);
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public void h(String str) {
            this.c.S1(str);
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public void i(Iap2Csm.a aVar) {
            Handler handler = this.c.h;
            io.carplay.app.prototype.g gVar = new io.carplay.app.prototype.g(4);
            gVar.b = this;
            gVar.c = aVar;
            VarHandle.storeStoreFence();
            handler.post(gVar);
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public void k(Iap2Csm.a aVar) {
            this.c.E1(aVar);
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public String l() {
            r.a aVar;
            synchronized (this.c.i) {
                try {
                    if (this.c.D == null) {
                        PairingSession pairingSession = this.c;
                        pairingSession.D = io.carplay.app.prototype.r.a(pairingSession.a, this.a.getRemoteDevice().getAddress());
                    }
                    aVar = this.c.D;
                } catch (Throwable th) {
                    throw th;
                }
            }
            this.c.S1("LOCAL_BLUETOOTH_TRANSPORT_ID source=" + aVar.h() + " value_logged=false");
            return aVar.g();
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public void write(byte[] bArr) throws IOException {
            if (this.c.isClosed()) {
                SyntheticRefs.c("Cancelled");
                return;
            }
            try {
                this.b.write(bArr);
                this.b.flush();
            } catch (IOException e) {
                throw PairingSession.x0(e);
            }
        }
    }

    public class b extends ConnectivityManager.NetworkCallback {
        final /* synthetic */ Network a;
        final /* synthetic */ PairingSession b;

        public b(PairingSession pairingSession, Network network) {
            this.a = network;
            this.b = pairingSession;
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            if (this.b.v1(this) && this.a.equals(network)) {
                this.b.r1(network, linkProperties);
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            if (this.b.v1(this) && this.a.equals(network)) {
                this.b.T1("WIFI_SELECTED_NETWORK_LOST");
            }
        }
    }

    public class c extends ConnectivityManager.NetworkCallback {
        final /* synthetic */ long a;
        final /* synthetic */ PairingSession b;

        public c(PairingSession pairingSession, long j) {
            this.a = j;
            this.b = pairingSession;
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            this.b.t1(network, null, this);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            if (this.b.v1(this)) {
                this.b.r1(network, linkProperties);
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            synchronized (this.b.i) {
                if (!this.b.u && this.b.s == this && network.equals(this.b.z)) {
                    this.b.T1("WIFI_SELECTED_NETWORK_LOST");
                }
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onUnavailable() {
            if (this.b.v1(this)) {
                this.b.T1("WIFI_CONNECTION_NOT_ESTABLISHED elapsed_ms=" + Math.max(0L, SystemClock.elapsedRealtime() - this.a));
            }
        }
    }

    public class d extends ConnectivityManager.NetworkCallback {
        public static final /* synthetic */ int d = 0;
        final /* synthetic */ Network a;
        final /* synthetic */ h b;
        final /* synthetic */ PairingSession c;

        public d(PairingSession pairingSession, Network network, h hVar) {
            this.a = network;
            this.b = hVar;
            this.c = pairingSession;
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            if (this.a.equals(network)) {
                synchronized (this.c.i) {
                    try {
                        if (this.c.v1(this)) {
                            String strU1 = networkCapabilities == null ? null : PairingSession.U1(networkCapabilities);
                            boolean z = (PairingSession.V1(networkCapabilities) && (strU1 == null || this.c.x.a.equals(strU1))) ? false : true;
                            PairingSession pairingSession = this.c;
                            if (z) {
                                pairingSession.T1("WIFI_SELECTED_NETWORK_LOST");
                                return;
                            }
                            Handler handler = pairingSession.h;
                            PairingSession pairingSession2 = this.c;
                            Objects.requireNonNull(pairingSession2);
                            p pVar = new p(3);
                            pVar.b = pairingSession2;
                            VarHandle.storeStoreFence();
                            handler.post(pVar);
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            synchronized (this.c.i) {
                try {
                    if (this.c.v1(this) && this.a.equals(network)) {
                        h hVar = this.b;
                        hVar.d = linkProperties;
                        if (hVar.f) {
                            this.c.A = linkProperties;
                        }
                        Handler handler = this.c.h;
                        PairingSession pairingSession = this.c;
                        Objects.requireNonNull(pairingSession);
                        p pVar = new p(3);
                        pVar.b = pairingSession;
                        VarHandle.storeStoreFence();
                        handler.post(pVar);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            synchronized (this.c.i) {
                if (this.c.v1(this) && this.a.equals(network)) {
                    this.c.T1("WIFI_SELECTED_NETWORK_LOST");
                }
            }
        }
    }

    public class e implements CarPlayCtrlAdvertiser.b {
        final /* synthetic */ int a;
        final /* synthetic */ PairingSession b;

        public e(PairingSession pairingSession, int i) {
            this.a = i;
            this.b = pairingSession;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void d(int i) {
            synchronized (this.b.i) {
                if (!this.b.u && this.b.G == i) {
                    this.b.H0();
                    this.b.R1();
                }
            }
        }

        @Override // io.carplay.app.prototype.CarPlayCtrlAdvertiser.b
        public void a(String str) {
            this.b.S1(str);
        }

        @Override // io.carplay.app.prototype.CarPlayCtrlAdvertiser.b
        public void b() {
            ControlChannel.MarkInvited();
            Handler handler = this.b.h;
            int i = this.a;
            io.carplay.app.prototype.d dVar = new io.carplay.app.prototype.d(1);
            dVar.c = this;
            dVar.b = i;
            VarHandle.storeStoreFence();
            handler.post(dVar);
        }
    }

    public class f implements AirplayServiceProbe.d {
        final /* synthetic */ Network a;
        final /* synthetic */ String b;
        final /* synthetic */ PairingSession c;

        public f(PairingSession pairingSession, Network network, String str) {
            this.a = network;
            this.b = str;
            this.c = pairingSession;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void e(Network network, String str, String str2, int i, String str3) {
            synchronized (this.c.i) {
                try {
                    if (this.c.o1(network, str, InetAddresses.parseNumericAddress(str2))) {
                        PairingSession pairingSession = this.c;
                        pairingSession.y = Iap2Csm.l(pairingSession.y, this.c.x, str2, i, str3);
                        this.c.S1("CARPLAY_DISCOVERY_CANDIDATE_REQUIRES_FULL_PINNED_AUTHENTICATION");
                        this.c.H0();
                    }
                } catch (IOException | IllegalArgumentException unused) {
                    this.c.S1("CARPLAY_DISCOVERY_INVALID_ENDPOINT");
                }
            }
        }

        @Override // io.carplay.app.prototype.AirplayServiceProbe.d
        public void a(String str) {
            this.c.S1(str);
        }

        @Override // io.carplay.app.prototype.AirplayServiceProbe.d
        public void b(String str) {
            this.c.S1("CARPLAY_DISCOVERY_ENDED " + str);
        }

        @Override // io.carplay.app.prototype.AirplayServiceProbe.d
        public void c(String str, int i, String str2) {
            Handler handler = this.c.h;
            Network network = this.a;
            String str3 = this.b;
            i0 i0Var = new i0(1);
            i0Var.c = this;
            i0Var.d = network;
            i0Var.e = str3;
            i0Var.f = str;
            i0Var.b = i;
            i0Var.g = str2;
            VarHandle.storeStoreFence();
            handler.post(i0Var);
        }
    }

    public final class h implements i {
        final ConnectivityManager a;
        final Network b;
        ConnectivityManager.NetworkCallback c;
        LinkProperties d;
        boolean e;
        boolean f;
        boolean g;

        public h(ConnectivityManager connectivityManager, Network network) {
            this.a = connectivityManager;
            this.b = network;
        }

        @Override // io.carplay.app.prototype.PairingSession.i
        public boolean a() {
            synchronized (PairingSession.this.i) {
                if (this.e && !this.f && c()) {
                    this.g = true;
                    PairingSession.this.R = null;
                    PairingSession.this.s = null;
                    PairingSession.this.v = false;
                    PairingSession.this.z = null;
                    PairingSession.this.A = null;
                    PairingSession.this.J = false;
                    try {
                        this.a.unregisterNetworkCallback(this.c);
                    } catch (RuntimeException unused) {
                    }
                    Handler handler = PairingSession.this.h;
                    PairingSession pairingSession = PairingSession.this;
                    Objects.requireNonNull(pairingSession);
                    u6 u6Var = new u6(11);
                    u6Var.b = pairingSession;
                    VarHandle.storeStoreFence();
                    handler.post(u6Var);
                    return true;
                }
                return false;
            }
        }

        @Override // io.carplay.app.prototype.PairingSession.i
        public boolean b() {
            synchronized (PairingSession.this.i) {
                try {
                    if (this.e && !this.f && c()) {
                        this.f = true;
                        PairingSession.this.v = true;
                        PairingSession.this.z = this.b;
                        PairingSession.this.A = this.d;
                        PairingSession.this.J = true;
                        PairingSession.this.I = null;
                        PairingSession.this.h.removeCallbacks(PairingSession.this.S);
                        PairingSession.this.D0();
                        PairingSession.this.S1("WIFI_EXISTING_TRANSPORT_AUTHENTICATED selected_network_retained=true");
                        Handler handler = PairingSession.this.h;
                        PairingSession pairingSession = PairingSession.this;
                        Objects.requireNonNull(pairingSession);
                        u6 u6Var = new u6(12);
                        u6Var.b = pairingSession;
                        VarHandle.storeStoreFence();
                        handler.post(u6Var);
                        return true;
                    }
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        public boolean c() {
            if (!PairingSession.this.u && PairingSession.this.R == this && PairingSession.this.s == this.c && !this.g) {
                NetworkCapabilities networkCapabilities = this.a.getNetworkCapabilities(this.b);
                String strU1 = networkCapabilities == null ? null : PairingSession.U1(networkCapabilities);
                if (PairingSession.V1(networkCapabilities) && (strU1 == null || PairingSession.this.x.a.equals(strU1))) {
                    boolean z = false;
                    int i = 0;
                    for (Network network : this.a.getAllNetworks()) {
                        if (this.b.equals(network)) {
                            z = true;
                        }
                        if (PairingSession.V1(this.a.getNetworkCapabilities(network))) {
                            i++;
                        }
                    }
                    if (z && (this.f || i == 1)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public interface i {
        boolean a();

        boolean b();
    }

    public interface k {

        public enum a {
            NORMAL_DEADLINE,
            PEER_EOF
        }

        void a(String str);

        void b(String str);

        default i5 c() throws IOException {
            return null;
        }

        default boolean d() {
            return false;
        }

        default void e(Network network, Iap2Csm.a aVar, String str, String str2) {
            m(network, aVar, str);
        }

        default void f(Network network, Iap2Csm.a aVar, String str, i iVar, String str2) {
            s(network, aVar, str, iVar);
        }

        default void g(String str, MfiCertificateVerifier.DetectionState aVar, a aVar2) {
        }

        default void h(String str, MfiCertificateVerifier.InputFlags bVar) {
        }

        default boolean i() {
            return false;
        }

        default void j(Network network, Iap2Csm.a aVar, String str) {
        }

        default boolean k() {
            return false;
        }

        default void l(String str) {
        }

        default void m(Network network, Iap2Csm.a aVar, String str) {
            n(network, aVar);
        }

        void n(Network network, Iap2Csm.a aVar);

        default void o(String str, byte[] bArr) throws IOException {
            throw new IOException("MFI_FIRST_USE_TRUST_STORAGE_UNAVAILABLE");
        }

        default boolean p() {
            return false;
        }

        default void q(String str, MfiCertificateVerifier.e eVar) {
        }

        default void r(String str, byte[] bArr) {
        }

        default void s(Network network, Iap2Csm.a aVar, String str, i iVar) {
            iVar.a();
        }

        default void t(Iap2Csm.g gVar) {
        }
    }

    public PairingSession(Context context, k kVar, String str, boolean z, boolean z2, boolean z3) {
        this.h = new Handler(Looper.getMainLooper());
        this.i = new Object();
        this.j = Executors.newFixedThreadPool(2, new io.carplay.app.secured.n0(5));
        u6 u6Var = new u6(2);
        u6Var.b = this;
        VarHandle.storeStoreFence();
        this.S = u6Var;
        if (z && (str == null || !str.matches("[a-fA-F0-9]{64}"))) {
            SyntheticRefs.b("pinned startup order fallback required");
            throw null;
        }
        if (z2 && !z) {
            SyntheticRefs.b("bounded pinned startup recovery required");
            throw null;
        }
        if (z3 && z) {
            SyntheticRefs.b("initial link and authenticated startup recovery are exclusive");
            throw null;
        }
        this.a = context.getApplicationContext();
        this.b = kVar;
        this.c = str;
        this.d = z;
        this.e = z2;
        this.f = z3;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: A0, reason: merged with bridge method [inline-methods] */
    public void i1(BluetoothServerSocket bluetoothServerSocket, BluetoothDevice bluetoothDevice, AtomicInteger atomicInteger) {
        long jElapsedRealtime = SystemClock.elapsedRealtime() + V;
        while (!isClosed()) {
            try {
                int iElapsedRealtime = (int) (jElapsedRealtime - SystemClock.elapsedRealtime());
                if (iElapsedRealtime <= 0) {
                    throw new IOException("Selected-peer accept timeout");
                }
                BluetoothSocket bluetoothSocketAccept = bluetoothServerSocket.accept(iElapsedRealtime);
                if (bluetoothSocketAccept.getRemoteDevice().getAddress().equals(bluetoothDevice.getAddress())) {
                    if (E0(bluetoothSocketAccept)) {
                        S1("RFCOMM_SELECTED_PEER_ACCEPTED");
                        k1(bluetoothSocketAccept);
                        return;
                    }
                    return;
                }
                z1(bluetoothSocketAccept);
                S1("RFCOMM_IGNORED_UNSELECTED_PEER");
            } catch (Exception e2) {
                I0("listen", e2, atomicInteger);
                return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: renamed from: A1, reason: merged with bridge method [inline-methods] */
    public void a1(InputStream inputStream, ArrayBlockingQueue<m> arrayBlockingQueue, j jVar) {
        try {
            byte[] bArr = new byte[4096];
            while (!isClosed()) {
                int i3 = inputStream.read(bArr);
                jVar.a.incrementAndGet();
                if (i3 < 0) {
                    jVar.e = "EOF";
                    arrayBlockingQueue.put(new m(null, new EOFException("RFCOMM_EOF")));
                    return;
                } else if (i3 > 0) {
                    long j2 = i3;
                    jVar.b.addAndGet(j2);
                    arrayBlockingQueue.put(new m(Arrays.copyOf(bArr, i3), null));
                    jVar.c.addAndGet(j2);
                }
            }
            jVar.e = "OWNER_CLOSED";
        } catch (IOException e2) {
            jVar.e = "IO_FAILURE";
            if (!isClosed()) {
                return;
            }
            try {
                arrayBlockingQueue.put(new m(null, N1("RFCOMM_READ_FAILED", e2)));
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
        } catch (InterruptedException unused2) {
            jVar.e = "INTERRUPTED";
            Thread.currentThread().interrupt();
        }
    }

    private static String B0(BluetoothDevice bluetoothDevice) {
        try {
            ParcelUuid[] uuids = bluetoothDevice.getUuids();
            if (uuids == null) {
                return "UNAVAILABLE";
            }
            for (ParcelUuid parcelUuid : uuids) {
                if (parcelUuid != null && U.equals(parcelUuid.getUuid())) {
                    return "CAFF_PRESENT";
                }
            }
            return "CAFF_NOT_IN_CACHE";
        } catch (RuntimeException unused) {
            return "UNAVAILABLE";
        }
    }

    private boolean B1(BluetoothDevice bluetoothDevice, AtomicInteger atomicInteger) {
        synchronized (this.i) {
            try {
                int i2 = 0;
                if (this.u || this.m != null || this.n != null || this.o != null || this.k == null || this.r || SystemClock.elapsedRealtime() + W >= this.M) {
                    return false;
                }
                try {
                    if (!this.b.k()) {
                        return false;
                    }
                    int i3 = 1;
                    this.r = true;
                    t6 t6Var = new t6(i2);
                    t6Var.b = this;
                    VarHandle.storeStoreFence();
                    u6 u6Var = new u6(i2);
                    u6Var.b = this;
                    VarHandle.storeStoreFence();
                    h5 h5Var = new h5(t6Var, u6Var);
                    synchronized (this.i) {
                        if (!this.u && this.m == null) {
                            this.o = h5Var;
                            try {
                                h5Var.v(this.a);
                                synchronized (this.i) {
                                    if (!this.u && this.m == null) {
                                        String address = bluetoothDevice.getAddress();
                                        t6 t6Var2 = new t6(i3);
                                        t6Var2.b = this;
                                        VarHandle.storeStoreFence();
                                        u6 u6Var2 = new u6(i3);
                                        u6Var2.b = this;
                                        VarHandle.storeStoreFence();
                                        h5Var.i(address, t6Var2, u6Var2);
                                        if (!this.u && this.m == null) {
                                            v6 v6Var = new v6(i2);
                                            v6Var.b = this;
                                            v6Var.c = bluetoothDevice;
                                            v6Var.d = atomicInteger;
                                            VarHandle.storeStoreFence();
                                            this.L = v6Var;
                                            this.h.postDelayed(v6Var, W);
                                            S1("RFCOMM_PHONE_DISCOVERY_FALLBACK_READY");
                                            S1("RFCOMM_PHONE_DISCOVERY_FALLBACK_POLICY temporary=true extra_secure_dials=1 deadline_extended=false radio_confirmed=false");
                                            return true;
                                        }
                                        return true;
                                    }
                                    h5Var.close();
                                    return true;
                                }
                            } catch (IOException | RuntimeException unused) {
                                synchronized (this.i) {
                                    try {
                                        if (this.o == h5Var) {
                                            this.o = null;
                                        }
                                        h5Var.close();
                                        S1("RFCOMM_PHONE_DISCOVERY_FALLBACK_UNAVAILABLE normal_listener_preserved=true");
                                        return false;
                                    } catch (Throwable th) {
                                        throw th;
                                    }
                                }
                            }
                        }
                        return true;
                    }
                } catch (RuntimeException unused2) {
                    return false;
                }
            } catch (Throwable th2) {
                throw th2;
            }
        }
    }

    public static List<l> C0(Context context) {
        if (context.checkSelfPermission("android.permission.BLUETOOTH_CONNECT") != 0) {
            return Collections.EMPTY_LIST;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(BluetoothManager.class);
        BluetoothAdapter adapter = bluetoothManager == null ? null : bluetoothManager.getAdapter();
        if (adapter == null) {
            return Collections.EMPTY_LIST;
        }
        ArrayList arrayList = new ArrayList();
        for (BluetoothDevice bluetoothDevice : adapter.getBondedDevices()) {
            arrayList.add(new l(bluetoothDevice.getName() == null ? "Bluetooth device" : bluetoothDevice.getName(), bluetoothDevice.getAddress(), 0));
        }
        arrayList.sort(Comparator.comparing(new io.carplay.app.secured.r(6)).thenComparing(new io.carplay.app.secured.r(7)));
        return List.copyOf(arrayList);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v3 */
    private boolean C1(ConnectivityManager connectivityManager, NetworkRequest networkRequest, ConnectivityManager.NetworkCallback networkCallback, boolean z) {
        synchronized (this.i) {
            try {
                if (this.u) {
                    return false;
                }
                this.s = networkCallback;
                try {
                    if (z) {
                        connectivityManager.requestNetwork(networkRequest, networkCallback, 45000);
                    } else {
                        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
                    }
                    return true;
                } catch (RuntimeException e2) {
                    this.s = null;
                    try {
                        connectivityManager.unregisterNetworkCallback(networkCallback);
                    } catch (RuntimeException unused) {
                    }
                    throw e2;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void D0() {
        Runnable runnable = this.N;
        if (runnable != null) {
            this.h.removeCallbacks(runnable);
            this.N = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void D1() {
        synchronized (this.i) {
            if (!this.u && !this.O && this.s == null) {
                this.O = true;
                D0();
                Iap2Csm.g gVar = this.x;
                try {
                    if (this.a.checkSelfPermission("android.permission.NEARBY_WIFI_DEVICES") != 0) {
                        T1("NEARBY_WIFI_DEVICES_PERMISSION_REQUIRED");
                        return;
                    }
                    WifiNetworkSpecifier.Builder isHiddenSsid = new WifiNetworkSpecifier.Builder().setSsid(gVar.a).setIsHiddenSsid(true);
                    int i2 = gVar.b;
                    if (i2 == 2 || i2 == 3) {
                        isHiddenSsid.setWpa2Passphrase(gVar.c());
                    } else if (i2 == 4) {
                        isHiddenSsid.setWpa3Passphrase(gVar.c());
                    } else if (i2 != 0) {
                        throw new IOException("WIFI_SECURITY_UNSUPPORTED");
                    }
                    NetworkRequest networkRequestBuild = new NetworkRequest.Builder().addTransportType(1).removeCapability(12).setNetworkSpecifier(isHiddenSsid.build()).build();
                    ConnectivityManager connectivityManager = (ConnectivityManager) this.a.getSystemService(ConnectivityManager.class);
                    if (connectivityManager == null) {
                        throw new IOException("CONNECTIVITY_MANAGER_UNAVAILABLE");
                    }
                    c cVar = new c(this, SystemClock.elapsedRealtime());
                    S1("WIFI_NETWORK_REQUEST_SUBMITTED system_consent_may_be_required=true");
                    C1(connectivityManager, networkRequestBuild, cVar, true);
                } catch (Exception e2) {
                    T1("WIFI_REQUEST_FAILED: ".concat(e2.getClass().getSimpleName()));
                }
            }
        }
    }

    private boolean E0(BluetoothSocket bluetoothSocket) {
        synchronized (this.i) {
            try {
                if (!this.u && this.m == null) {
                    this.m = bluetoothSocket;
                    Runnable runnable = this.L;
                    if (runnable != null) {
                        this.h.removeCallbacks(runnable);
                    }
                    this.h.removeCallbacks(this.S);
                    this.h.postDelayed(this.S, V);
                    z1(this.k);
                    this.k = null;
                    h5 h5Var = this.n;
                    if (h5Var != null && (!this.q || !h5Var.r())) {
                        this.n.close();
                        this.n = null;
                    }
                    h5 h5Var2 = this.o;
                    if (h5Var2 != null && (!this.q || !h5Var2.r())) {
                        this.o.close();
                        this.o = null;
                    }
                    BluetoothSocket bluetoothSocket2 = this.l;
                    if (bluetoothSocket2 != bluetoothSocket) {
                        z1(bluetoothSocket2);
                    }
                    this.l = null;
                    return true;
                }
                z1(bluetoothSocket);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void E1(Iap2Csm.a aVar) {
        synchronized (this.i) {
            try {
                if (this.u || this.J) {
                    return;
                }
                try {
                    Iap2Csm.a aVar2 = this.y;
                    if (aVar2 != null && aVar2.e.equals("bonjour_untrusted") && this.y.e()) {
                        Iap2Csm.g gVarH = aVar.h();
                        Iap2Csm.a aVar3 = this.y;
                        this.y = Iap2Csm.l(aVar, gVarH, aVar3.b, aVar3.h, aVar3.c);
                    } else {
                        this.y = Iap2Csm.n(this.y, aVar);
                    }
                    F1(aVar.h());
                    Handler handler = this.h;
                    u6 u6Var = new u6(8);
                    u6Var.b = this;
                    VarHandle.storeStoreFence();
                    handler.post(u6Var);
                } catch (IOException unused) {
                    T1("CARPLAY_ENDPOINT_MERGE_REJECTED");
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code duplicated, block: B:60:0x008e A[Catch: all -> 0x0091, TryCatch #1 {all -> 0x0091, blocks: (B:58:0x008a, B:60:0x008e, B:63:0x0094, B:65:0x0098, B:68:0x009e, B:92:0x0120), top: B:98:0x008a }] */
    /* JADX WARN: Code duplicated, block: B:70:0x00a1  */
    /* JADX WARN: Code duplicated, block: B:71:0x00a4  */
    /* JADX WARN: Code duplicated, block: B:98:0x008a A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX INFO: renamed from: F0, reason: merged with bridge method [inline-methods] */
    public void c1(BluetoothDevice bluetoothDevice, AtomicInteger atomicInteger, int i2) {
        Exception e2;
        BluetoothSocket bluetoothSocketCreateRfcommSocketToServiceRecord;
        String str;
        boolean z = true;
        if (i2 > 1) {
            synchronized (this.i) {
                try {
                    if (!this.u && this.m == null && SystemClock.elapsedRealtime() < this.M) {
                    }
                    return;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        int i3 = 0;
        try {
            bluetoothSocketCreateRfcommSocketToServiceRecord = bluetoothDevice.createRfcommSocketToServiceRecord(U);
            try {
                synchronized (this.i) {
                    if (!this.u && this.m == null) {
                        this.l = bluetoothSocketCreateRfcommSocketToServiceRecord;
                        w6 w6Var = new w6(i3);
                        w6Var.b = this;
                        w6Var.c = bluetoothSocketCreateRfcommSocketToServiceRecord;
                        VarHandle.storeStoreFence();
                        this.h.postDelayed(w6Var, 20000L);
                        try {
                            bluetoothSocketCreateRfcommSocketToServiceRecord.connect();
                            try {
                                this.h.removeCallbacks(w6Var);
                                if (E0(bluetoothSocketCreateRfcommSocketToServiceRecord)) {
                                    S1("RFCOMM_SELECTED_PEER_CONNECTED");
                                    k1(bluetoothSocketCreateRfcommSocketToServiceRecord);
                                    return;
                                }
                                return;
                            } catch (Exception e3) {
                                e2 = e3;
                                i3 = 1;
                                z1(bluetoothSocketCreateRfcommSocketToServiceRecord);
                                synchronized (this.i) {
                                    try {
                                        if (this.l == bluetoothSocketCreateRfcommSocketToServiceRecord) {
                                            this.l = null;
                                        }
                                        if (!this.u && this.m == null) {
                                            if (z) {
                                                str = "CONNECT";
                                            } else {
                                                str = "CREATE";
                                            }
                                            S1("RFCOMM_CONNECT_DIAGNOSTIC attempt=" + i2 + " stage=" + str + " elapsed_ms=" + (SystemClock.elapsedRealtime() - jElapsedRealtime) + " error_code=" + M1(e2) + " service_cache=" + B0(bluetoothDevice));
                                            if (!z && i3 == 0 && i2 < 3 && H1(e2) && K1(bluetoothDevice, atomicInteger, i2 + 1)) {
                                                S1(w1.e("RFCOMM_CONNECT_UNAVAILABLE: ", J1(e2)));
                                                return;
                                            }
                                            if (!z && i3 == 0 && i2 == 3 && H1(e2) && B1(bluetoothDevice, atomicInteger)) {
                                                return;
                                            }
                                            I0("connect", e2, atomicInteger);
                                            return;
                                        }
                                        return;
                                    } catch (Throwable th2) {
                                        throw th2;
                                    }
                                }
                            }
                        } catch (Throwable th3) {
                            try {
                                this.h.removeCallbacks(w6Var);
                                throw th3;
                            } catch (Exception e4) {
                                e2 = e4;
                                z1(bluetoothSocketCreateRfcommSocketToServiceRecord);
                                synchronized (this.i) {
                                    if (this.l == bluetoothSocketCreateRfcommSocketToServiceRecord) {
                                        this.l = null;
                                    }
                                    if (!this.u) {
                                        if (z) {
                                            str = "CONNECT";
                                        } else {
                                            str = "CREATE";
                                        }
                                        S1("RFCOMM_CONNECT_DIAGNOSTIC attempt=" + i2 + " stage=" + str + " elapsed_ms=" + (SystemClock.elapsedRealtime() - jElapsedRealtime) + " error_code=" + M1(e2) + " service_cache=" + B0(bluetoothDevice));
                                        if (!z) {
                                        }
                                        if (!z) {
                                        }
                                        I0("connect", e2, atomicInteger);
                                        return;
                                    }
                                    return;
                                }
                            }
                        }
                    }
                    z1(bluetoothSocketCreateRfcommSocketToServiceRecord);
                }
            } catch (Exception e5) {
                e2 = e5;
                z = false;
            }
        } catch (Exception e6) {
            e2 = e6;
            z = false;
            bluetoothSocketCreateRfcommSocketToServiceRecord = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code duplicated, block: B:19:0x0026  */
    /* JADX WARN: Code duplicated, block: B:21:0x002c  */
    public void F1(Iap2Csm.g gVar) {
        synchronized (this.i) {
            try {
                if (this.u) {
                    return;
                }
                boolean z = true;
                if (this.w) {
                    if (!this.x.d(gVar)) {
                    }
                    if (z) {
                        T1("WIFI_CONFIGURATION_CHANGED_RESTART_REQUIRED");
                        return;
                    }
                    Handler handler = this.h;
                    u6 u6Var = new u6(5);
                    u6Var.b = this;
                    VarHandle.storeStoreFence();
                    handler.post(u6Var);
                }
                this.w = true;
                this.x = gVar;
                this.b.t(gVar);
                z = false;
                if (z) {
                    T1("WIFI_CONFIGURATION_CHANGED_RESTART_REQUIRED");
                    return;
                }
                Handler handler2 = this.h;
                u6 u6Var2 = new u6(5);
                u6Var2.b = this;
                VarHandle.storeStoreFence();
                handler2.post(u6Var2);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void G0(Iap2Csm.a aVar) {
        synchronized (this.i) {
            try {
                if (!this.u && this.J && this.v && this.b.d()) {
                    try {
                        this.y = Iap2Csm.u(this.y, aVar);
                        Iap2Csm.a aVarL0 = L0(this.A);
                        if (aVarL0 == null) {
                            return;
                        }
                        LinkProperties linkProperties = this.A;
                        String interfaceName = linkProperties == null ? null : linkProperties.getInterfaceName();
                        InetAddress numericAddress = InetAddresses.parseNumericAddress(aVarL0.b);
                        if ((numericAddress instanceof Inet6Address) && numericAddress.isLinkLocalAddress() && (interfaceName == null || interfaceName.isEmpty())) {
                            return;
                        }
                        this.b.j(this.z, aVarL0, interfaceName);
                    } catch (IOException unused) {
                        S1("CARPLAY_START_ENDPOINT_UPDATE_REJECTED");
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private void G1(ConnectivityManager connectivityManager, ConnectivityManager.NetworkCallback networkCallback) {
        synchronized (this.i) {
            try {
                if (!this.u && this.s == networkCallback) {
                    this.s = null;
                    boolean z = this.Q;
                    this.Q = true;
                    CarPlayCtrlAdvertiser carPlayCtrlAdvertiser = this.C;
                    if (carPlayCtrlAdvertiser != null) {
                        this.G++;
                        carPlayCtrlAdvertiser.close();
                        this.C = null;
                    }
                    try {
                        connectivityManager.unregisterNetworkCallback(networkCallback);
                    } catch (RuntimeException unused) {
                    }
                    if (isClosed()) {
                        return;
                    }
                    S1("WIFI_SSID_VISIBILITY_CHANGED authentication_required=true");
                    Handler handler = this.h;
                    u6 u6Var = !z ? new u6(5) : new u6(6);
                    u6Var.b = this;
                    VarHandle.storeStoreFence();
                    handler.post(u6Var);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void H0() {
        synchronized (this.i) {
            try {
                h hVar = this.R;
                boolean z = this.u;
                if (hVar != null) {
                    if (!z && !hVar.e && this.y != null && hVar.c()) {
                        LinkProperties linkProperties = hVar.d;
                        String interfaceName = linkProperties == null ? null : linkProperties.getInterfaceName();
                        Iap2Csm.a aVarL0 = L0(hVar.d);
                        if (aVarL0 == null) {
                            return;
                        }
                        InetAddress numericAddress = InetAddresses.parseNumericAddress(aVarL0.b);
                        if ((numericAddress instanceof Inet6Address) && numericAddress.isLinkLocalAddress() && (interfaceName == null || interfaceName.isEmpty())) {
                            return;
                        }
                        hVar.e = true;
                        S1("WIFI_EXISTING_TRANSPORT_CANDIDATE ssid_redacted=true candidates=1 requires_full_pinned_authentication=true");
                        k kVar = this.b;
                        String strG = null;
                        Network network = hVar.b;
                        r.a aVar = this.D;
                        if (aVar != null) {
                            strG = aVar.g();
                        }
                        kVar.f(network, aVarL0, interfaceName, hVar, strG);
                        return;
                    }
                    return;
                }
                if (!z && !this.J && this.v && this.y != null) {
                    LinkProperties linkProperties2 = this.A;
                    String interfaceName2 = linkProperties2 == null ? null : linkProperties2.getInterfaceName();
                    Iap2Csm.a aVarL1 = L0(this.A);
                    if (aVarL1 == null) {
                        return;
                    }
                    InetAddress numericAddress2 = InetAddresses.parseNumericAddress(aVarL1.b);
                    if ((numericAddress2 instanceof Inet6Address) && numericAddress2.isLinkLocalAddress() && (interfaceName2 == null || interfaceName2.isEmpty())) {
                        return;
                    }
                    this.J = true;
                    this.I = null;
                    this.h.removeCallbacks(this.S);
                    S1("WIFI_AND_CARPLAY_ENDPOINT_READY_SOCKET_BINDING_REQUIRED");
                    k kVar2 = this.b;
                    Network network2 = this.z;
                    r.a aVar2 = this.D;
                    kVar2.e(network2, aVarL1, interfaceName2, aVar2 == null ? null : aVar2.g());
                    AirplayServiceProbe airplayServiceProbe = this.B;
                    this.B = null;
                    if (airplayServiceProbe != null) {
                        airplayServiceProbe.close();
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private static boolean H1(Exception exc) {
        int iM1;
        if (exc instanceof IOException) {
            return Build.VERSION.SDK_INT < 34 || !io.carplay.app.secured.t0.e(exc) || (iM1 = M1(exc)) == 0 || iM1 == 1 || iM1 == 2 || iM1 == 12 || iM1 == 14 || iM1 == 18;
        }
        return false;
    }

    private void I0(String str, Exception exc, AtomicInteger atomicInteger) {
        synchronized (this.i) {
            if (!this.u && this.m == null) {
                S1("RFCOMM_" + str.toUpperCase(Locale.ROOT) + "_UNAVAILABLE: " + J1(exc));
                if (atomicInteger.decrementAndGet() == 0) {
                    T1("RFCOMM_SELECTED_PEER_UNREACHABLE");
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Bottom block not found for handler: all -> 0x01f0 */
    /* JADX WARN: Code duplicated, block: B:116:0x0201  */
    /* JADX WARN: Code duplicated, block: B:141:0x0276  */
    /* JADX WARN: Code duplicated, block: B:152:0x0289  */
    /* JADX WARN: Code duplicated, block: B:154:0x028c A[Catch: all -> 0x0287, TryCatch #5 {all -> 0x0287, blocks: (B:143:0x0279, B:145:0x027d, B:147:0x0281, B:154:0x028c, B:155:0x0293), top: B:212:0x0279 }] */
    /* JADX WARN: Code duplicated, block: B:159:0x0297  */
    /* JADX WARN: Code duplicated, block: B:188:0x02da  */
    /* JADX WARN: Code duplicated, block: B:190:0x02dd A[Catch: all -> 0x02d6, TryCatch #4 {all -> 0x02d6, blocks: (B:170:0x02b9, B:172:0x02bd, B:174:0x02c1, B:176:0x02c5, B:178:0x02c9, B:180:0x02cd, B:182:0x02d1, B:190:0x02dd, B:191:0x02e9), top: B:210:0x02b9 }] */
    /* JADX WARN: Code duplicated, block: B:196:0x02f0  */
    /* JADX WARN: Code duplicated, block: B:197:0x02f3  */
    /* JADX WARN: Code duplicated, block: B:200:0x0337  */
    /* JADX WARN: Code duplicated, block: B:201:0x0346  */
    /* JADX WARN: Code duplicated, block: B:210:0x02b9 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:212:0x0279 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:228:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Instruction removed from duplicated block: B:197:0x02f3, please report this as an issue */
    /* JADX WARN: Instruction removed from duplicated block: B:201:0x0346, please report this as an issue */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r12v10 */
    /* JADX WARN: Type inference failed for: r12v12, types: [boolean] */
    /* JADX WARN: Type inference failed for: r12v27 */
    /* JADX WARN: Type inference failed for: r12v28 */
    /* JADX WARN: Type inference failed for: r12v29 */
    /* JADX WARN: Type inference failed for: r12v3 */
    /* JADX WARN: Type inference failed for: r12v4 */
    /* JADX WARN: Type inference failed for: r12v5 */
    /* JADX WARN: Type inference failed for: r12v6 */
    /* JADX WARN: Type inference failed for: r12v9 */
    /* JADX WARN: Type inference failed for: r16v0 */
    /* JADX WARN: Type inference failed for: r16v1, types: [io.carplay.app.secured.MfiCertificateVerifier] */
    /* JADX WARN: Type inference failed for: r16v11 */
    /* JADX WARN: Type inference failed for: r16v12 */
    /* JADX WARN: Type inference failed for: r16v5 */
    /* JADX WARN: Type inference failed for: r16v9 */
    /* JADX INFO: renamed from: I1, reason: merged with bridge method [inline-methods] and merged with bridge method [inline-methods] and merged with bridge method [inline-methods] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void k1(android.bluetooth.BluetoothSocket r23) {
        /*
            Method dump skipped, instruction units count: 861
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: io.carplay.app.prototype.PairingSession.k1(android.bluetooth.BluetoothSocket):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: J0, reason: merged with bridge method [inline-methods] */
    public boolean o1(Network network, String str, InetAddress inetAddress) {
        g gVar = this.I;
        if (gVar == null || !network.equals(gVar.j()) || !Objects.equals(str, gVar.g())) {
            return false;
        }
        if (inetAddress instanceof Inet6Address) {
            return gVar.i();
        }
        return gVar.h();
    }

    private static String J1(Exception exc) {
        String message = exc.getMessage();
        return exc.getClass().getSimpleName() + ": " + (message == null ? "no detail" : message.substring(0, Math.min(message.length(), 240)));
    }

    private boolean K0(LinkProperties linkProperties, InetAddress inetAddress) {
        if (s1(linkProperties, inetAddress)) {
            return true;
        }
        if (this.g) {
            return false;
        }
        this.g = true;
        S1("WIFI_LINK_PROPERTIES_PENDING required_family=" + (inetAddress instanceof Inet6Address ? 6 : 4) + " values_logged=false deadline_extended=false");
        return false;
    }

    private boolean K1(BluetoothDevice bluetoothDevice, AtomicInteger atomicInteger, int i2) {
        long j2 = ((long) (i2 - 1)) * W;
        synchronized (this.i) {
            try {
                int i3 = 0;
                if (!this.u && this.m == null && (this.k != null || this.n != null)) {
                    if (i2 <= 3 && SystemClock.elapsedRealtime() + j2 < this.M) {
                        s6 s6Var = new s6(i3);
                        s6Var.b = this;
                        s6Var.e = i2;
                        s6Var.c = bluetoothDevice;
                        s6Var.d = atomicInteger;
                        VarHandle.storeStoreFence();
                        this.L = s6Var;
                        this.h.postDelayed(s6Var, j2);
                        S1("RFCOMM_SELECTED_PEER_RETRY_SCHEDULED attempt=" + i2 + " delay_ms=" + j2 + " listener_preserved=true");
                        return true;
                    }
                }
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private Iap2Csm.a L0(LinkProperties linkProperties) {
        Iap2Csm.a aVar = this.y;
        if (aVar != null && aVar.e()) {
            InetAddress numericAddress = InetAddresses.parseNumericAddress(this.y.b);
            boolean zS1 = s1(linkProperties, numericAddress);
            Iap2Csm.a aVar2 = this.y;
            if (zS1) {
                return aVar2;
            }
            for (String str : aVar2.d()) {
                if (s1(linkProperties, InetAddresses.parseNumericAddress(str))) {
                    try {
                        return this.y.i(str);
                    } catch (IOException unused) {
                        return null;
                    }
                }
            }
            K0(linkProperties, numericAddress);
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void L1() {
        int i2;
        synchronized (this.i) {
            if (!this.u && !this.O && this.s == null) {
                Iap2Csm.g gVar = this.x;
                if (isClosed()) {
                    return;
                }
                if (this.a.checkSelfPermission("android.permission.NEARBY_WIFI_DEVICES") != 0) {
                    T1("NEARBY_WIFI_DEVICES_PERMISSION_REQUIRED");
                    return;
                }
                try {
                    ConnectivityManager connectivityManager = (ConnectivityManager) this.a.getSystemService(ConnectivityManager.class);
                    int i3 = 4;
                    int i4 = 0;
                    if (connectivityManager != null) {
                        for (Network network : connectivityManager.getAllNetworks()) {
                            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                            if (networkCapabilities != null && networkCapabilities.hasTransport(1) && !networkCapabilities.hasTransport(4)) {
                                if (gVar.a.equals(U1(networkCapabilities))) {
                                    b bVar = new b(this, network);
                                    if (C1(connectivityManager, new NetworkRequest.Builder().addTransportType(1).removeCapability(12).build(), bVar, false)) {
                                        NetworkCapabilities networkCapabilities2 = connectivityManager.getNetworkCapabilities(network);
                                        if (v1(bVar)) {
                                            if (!V1(networkCapabilities2)) {
                                                T1("WIFI_SELECTED_NETWORK_LOST");
                                                return;
                                            }
                                            String strU1 = U1(networkCapabilities2);
                                            if (strU1 != null) {
                                                if (!gVar.a.equals(strU1)) {
                                                    T1("WIFI_SELECTED_NETWORK_LOST");
                                                    return;
                                                } else {
                                                    S1("ALREADY_CONNECTED_WIFI_SSID_MATCH_AUTHENTICATED_PEER");
                                                    t1(network, connectivityManager.getLinkProperties(network), bVar);
                                                    return;
                                                }
                                            }
                                            Network[] allNetworks = connectivityManager.getAllNetworks();
                                            int length = allNetworks.length;
                                            while (i4 < length) {
                                                if (network.equals(allNetworks[i4])) {
                                                    G1(connectivityManager, bVar);
                                                    return;
                                                }
                                                i4++;
                                            }
                                            T1("WIFI_SELECTED_NETWORK_LOST");
                                            return;
                                        }
                                        return;
                                    }
                                    return;
                                }
                            }
                        }
                    }
                    Network network2 = null;
                    if (connectivityManager != null) {
                        Network[] allNetworks2 = connectivityManager.getAllNetworks();
                        int length2 = allNetworks2.length;
                        int i5 = 0;
                        i2 = 0;
                        while (i4 < length2) {
                            Network network3 = allNetworks2[i4];
                            NetworkCapabilities networkCapabilities3 = connectivityManager.getNetworkCapabilities(network3);
                            if (V1(networkCapabilities3)) {
                                i5++;
                                if (U1(networkCapabilities3) == null) {
                                    i2++;
                                    network2 = network3;
                                }
                            }
                            i4++;
                        }
                        i4 = i5;
                    } else {
                        i2 = 0;
                    }
                    S1("WIFI_EXISTING_TRANSPORT_SCAN wifi_candidates=" + i4 + " ssid_redacted_candidates=" + i2);
                    if (i4 != 1 || network2 == null) {
                        D1();
                        return;
                    }
                    synchronized (this.i) {
                        try {
                            if (this.u) {
                                return;
                            }
                            Iap2Csm.a aVar = this.y;
                            if (aVar != null && aVar.e()) {
                                D0();
                                u1(connectivityManager, network2);
                                return;
                            }
                            if (!this.P) {
                                this.P = true;
                                u6 u6Var = new u6(i3);
                                u6Var.b = this;
                                VarHandle.storeStoreFence();
                                this.N = u6Var;
                                this.h.postDelayed(u6Var, 750L);
                                S1("WIFI_EXISTING_REDACTED_WAITING_ENDPOINT candidates=1 grace_ms=750");
                            }
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                } catch (Exception e2) {
                    T1("WIFI_REQUEST_FAILED: ".concat(e2.getClass().getSimpleName()));
                }
            }
        }
    }

    private static int M1(Exception exc) {
        if (Build.VERSION.SDK_INT < 34 || !io.carplay.app.secured.t0.e(exc)) {
            return -1;
        }
        return io.carplay.app.secured.t0.b(exc).getErrorCode();
    }

    private static SocketException N1(String str, IOException iOException) {
        SocketException socketException = new SocketException(str);
        socketException.initCause(iOException);
        return socketException;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void O0(BluetoothSocket bluetoothSocket) {
        synchronized (this.i) {
            try {
                if (this.m != bluetoothSocket) {
                    z1(bluetoothSocket);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void V0(BluetoothSocket bluetoothSocket) {
        I1(bluetoothSocket);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void g1(BluetoothSocket bluetoothSocket) {
        I1(bluetoothSocket);
    }

    private void I1(BluetoothSocket bluetoothSocket) {
        throw new UnsupportedOperationException("Method not decompiled: io.carplay.app.prototype.PairingSession.I1(android.bluetooth.BluetoothSocket):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void P0() {
        Q1();
        H0();
        R1();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void Q0() {
        Q1();
        H0();
        R1();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void Q1() {
        synchronized (this.i) {
            try {
                if (!this.u && this.v && s1(this.A, null)) {
                    CarPlayCtrlAdvertiser carPlayCtrlAdvertiser = this.C;
                    if (carPlayCtrlAdvertiser == null || !carPlayCtrlAdvertiser.n(this.z, this.A)) {
                        CarPlayCtrlAdvertiser carPlayCtrlAdvertiser2 = this.C;
                        if (carPlayCtrlAdvertiser2 != null) {
                            this.G++;
                            carPlayCtrlAdvertiser2.close();
                            this.C = null;
                        }
                        r.a aVar = this.D;
                        if (aVar != null && aVar.g() != null) {
                            int i2 = this.F;
                            if (i2 >= 3) {
                                return;
                            }
                            this.F = i2 + 1;
                            int i3 = this.G + 1;
                            this.G = i3;
                            try {
                                CarPlayCtrlAdvertiser carPlayCtrlAdvertiser3 = new CarPlayCtrlAdvertiser(this.a, this.z, this.A, this.D.g(), new e(this, i3));
                                this.C = carPlayCtrlAdvertiser3;
                                carPlayCtrlAdvertiser3.q();
                            } catch (IOException | RuntimeException e2) {
                                S1("CTRL_SERVICE_UNAVAILABLE type=" + e2.getClass().getSimpleName() + " original_path_retained=true");
                            }
                            return;
                        }
                        if (!this.E) {
                            this.E = true;
                            S1("CTRL_SERVICE_SKIPPED reason=LOCAL_BLUETOOTH_IDENTIFIER_UNAVAILABLE original_path_retained=true");
                        }
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Thread R0(Runnable runnable) {
        Thread thread = new Thread(runnable, "carplay-bootstrap");
        thread.setDaemon(true);
        return thread;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void R1() {
        synchronized (this.i) {
            if (!this.u && !this.J && L0(this.A) == null && this.v && s1(this.A, null) && !this.H) {
                this.H = true;
                Network network = this.z;
                String interfaceName = this.A.getInterfaceName();
                try {
                    Context context = this.a;
                    f fVar = new f(this, network, interfaceName);
                    x6 x6Var = new x6();
                    x6Var.a = this;
                    x6Var.b = network;
                    x6Var.c = interfaceName;
                    VarHandle.storeStoreFence();
                    AirplayServiceProbe airplayServiceProbe = new AirplayServiceProbe(context, network, fVar, x6Var);
                    this.B = airplayServiceProbe;
                    airplayServiceProbe.A();
                } catch (RuntimeException e2) {
                    S1("CARPLAY_DISCOVERY_UNAVAILABLE type=".concat(e2.getClass().getSimpleName()));
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void S0() {
        boolean z;
        synchronized (this.i) {
            try {
                z = this.r && this.m == null;
            } catch (Throwable th) {
                throw th;
            }
        }
        T1(z ? "RFCOMM_SELECTED_PEER_UNREACHABLE" : "WIRELESS_BOOTSTRAP_DEADLINE");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void S1(String str) {
        Handler handler = this.h;
        y6 y6Var = new y6(1);
        y6Var.b = this;
        y6Var.c = str;
        VarHandle.storeStoreFence();
        handler.post(y6Var);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void T0() {
        S1("RFCOMM_PHONE_DISCOVERY_FALLBACK_LISTENERS_ENDED");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void T1(String str) {
        BluetoothRfcommProfiles bluetoothRfcommProfiles;
        ConnectivityManager connectivityManager;
        synchronized (this.i) {
            try {
                if (this.u) {
                    return;
                }
                this.u = true;
                this.I = null;
                CarPlayCtrlAdvertiser carPlayCtrlAdvertiser = this.C;
                if (carPlayCtrlAdvertiser != null) {
                    carPlayCtrlAdvertiser.close();
                    this.C = null;
                }
                D0();
                this.R = null;
                z1(this.k);
                this.k = null;
                h5 h5Var = this.n;
                if (h5Var != null) {
                    h5Var.close();
                    this.n = null;
                }
                h5 h5Var2 = this.o;
                if (h5Var2 != null) {
                    h5Var2.close();
                    this.o = null;
                }
                z1(this.l);
                this.l = null;
                z1(this.m);
                this.m = null;
                ConnectivityManager.NetworkCallback networkCallback = this.s;
                this.s = null;
                AirplayServiceProbe airplayServiceProbe = this.B;
                this.B = null;
                synchronized (this.i) {
                    bluetoothRfcommProfiles = this.p;
                    this.p = null;
                }
                if (bluetoothRfcommProfiles != null) {
                    bluetoothRfcommProfiles.close();
                }
                this.h.removeCallbacks(this.S);
                Runnable runnable = this.L;
                if (runnable != null) {
                    this.h.removeCallbacks(runnable);
                }
                this.j.shutdownNow();
                if (airplayServiceProbe != null) {
                    airplayServiceProbe.close();
                }
                if (networkCallback != null && (connectivityManager = (ConnectivityManager) this.a.getSystemService(ConnectivityManager.class)) != null) {
                    try {
                        connectivityManager.unregisterNetworkCallback(networkCallback);
                    } catch (RuntimeException unused) {
                    }
                }
                Handler handler = this.h;
                y6 y6Var = new y6(0);
                y6Var.b = this;
                y6Var.c = str;
                VarHandle.storeStoreFence();
                handler.post(y6Var);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void U0(BluetoothSocket bluetoothSocket) {
        if (!E0(bluetoothSocket)) {
            z1(bluetoothSocket);
            return;
        }
        S1("RFCOMM_SELECTED_PEER_ACCEPTED");
        try {
            ExecutorService executorService = this.j;
            w6 w6Var = new w6(1);
            w6Var.b = this;
            w6Var.c = bluetoothSocket;
            VarHandle.storeStoreFence();
            executorService.execute(w6Var);
        } catch (RejectedExecutionException unused) {
            z1(bluetoothSocket);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String U1(NetworkCapabilities networkCapabilities) {
        TransportInfo transportInfo = networkCapabilities.getTransportInfo();
        String ssid = transportInfo instanceof WifiInfo ? ((WifiInfo) transportInfo).getSSID() : null;
        if (ssid != null && ssid.length() >= 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        if ("<unknown ssid>".equals(ssid)) {
            return null;
        }
        return ssid;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean V1(NetworkCapabilities networkCapabilities) {
        return (networkCapabilities == null || !networkCapabilities.hasTransport(1) || networkCapabilities.hasTransport(4)) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void W0() {
        S1("RFCOMM_PHONE_DISCOVERY_FALLBACK_LISTENERS_ENDED");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void X0(BluetoothDevice bluetoothDevice, AtomicInteger atomicInteger) {
        synchronized (this.i) {
            if (!this.u && this.m == null && SystemClock.elapsedRealtime() < this.M) {
                S1("RFCOMM_SELECTED_PEER_DISCOVERY_CONNECT_START attempt=4 secure=true");
                try {
                    ExecutorService executorService = this.j;
                    v6 v6Var = new v6(3);
                    v6Var.b = this;
                    v6Var.c = bluetoothDevice;
                    v6Var.d = atomicInteger;
                    VarHandle.storeStoreFence();
                    executorService.execute(v6Var);
                } catch (RejectedExecutionException unused) {
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void Y0(BluetoothDevice bluetoothDevice, AtomicInteger atomicInteger) {
        c1(bluetoothDevice, atomicInteger, 4);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void Z0() {
        Q1();
        H0();
        R1();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void b1(int i2, BluetoothDevice bluetoothDevice, AtomicInteger atomicInteger) {
        synchronized (this.i) {
            if (!this.u && this.m == null && SystemClock.elapsedRealtime() < this.M) {
                S1(w1.d("RFCOMM_SELECTED_PEER_RETRY_CONNECT_START attempt=", i2));
                try {
                    ExecutorService executorService = this.j;
                    s6 s6Var = new s6(1);
                    s6Var.b = this;
                    s6Var.c = bluetoothDevice;
                    s6Var.d = atomicInteger;
                    s6Var.e = i2;
                    VarHandle.storeStoreFence();
                    executorService.execute(s6Var);
                } catch (RejectedExecutionException unused) {
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void d1() {
        synchronized (this.i) {
            this.N = null;
        }
        D1();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void e1() {
        S1("RFCOMM_PHONE_IDENTITY_LISTENER_ENDED");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void f1(BluetoothSocket bluetoothSocket) {
        if (!E0(bluetoothSocket)) {
            z1(bluetoothSocket);
            return;
        }
        S1("RFCOMM_SELECTED_PEER_ACCEPTED");
        try {
            ExecutorService executorService = this.j;
            w6 w6Var = new w6(2);
            w6Var.b = this;
            w6Var.c = bluetoothSocket;
            VarHandle.storeStoreFence();
            executorService.execute(w6Var);
        } catch (RejectedExecutionException unused) {
            z1(bluetoothSocket);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void h1() {
        S1("RFCOMM_PHONE_IDENTITY_LISTENER_ENDED");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isClosed() {
        boolean z;
        synchronized (this.i) {
            z = this.u;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void j1(BluetoothSocket bluetoothSocket) {
        if (!E0(bluetoothSocket)) {
            z1(bluetoothSocket);
            return;
        }
        S1("RFCOMM_SELECTED_PEER_ACCEPTED");
        try {
            ExecutorService executorService = this.j;
            w6 w6Var = new w6(3);
            w6Var.b = this;
            w6Var.c = bluetoothSocket;
            VarHandle.storeStoreFence();
            executorService.execute(w6Var);
        } catch (RejectedExecutionException unused) {
            z1(bluetoothSocket);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void l1(AtomicInteger atomicInteger) {
        I0("listen", new IOException("prepared discovery listeners ended"), atomicInteger);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void m1(BluetoothDevice bluetoothDevice, AtomicInteger atomicInteger) {
        synchronized (this.i) {
            if (!this.u && this.m == null) {
                S1("RFCOMM_SELECTED_PEER_ACTIVE_CONNECT_START");
                try {
                    ExecutorService executorService = this.j;
                    v6 v6Var = new v6(1);
                    v6Var.b = this;
                    v6Var.c = bluetoothDevice;
                    v6Var.d = atomicInteger;
                    VarHandle.storeStoreFence();
                    executorService.execute(v6Var);
                } catch (RejectedExecutionException unused) {
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void n1(BluetoothDevice bluetoothDevice, AtomicInteger atomicInteger) {
        c1(bluetoothDevice, atomicInteger, 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void p1(String str) {
        if (isClosed()) {
            return;
        }
        this.b.a(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void q1(String str) {
        this.b.b(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void r1(Network network, LinkProperties linkProperties) {
        synchronized (this.i) {
            if (!this.u && network.equals(this.z)) {
                this.A = linkProperties;
                y1();
                Handler handler = this.h;
                u6 u6Var = new u6(7);
                u6Var.b = this;
                VarHandle.storeStoreFence();
                handler.post(u6Var);
            }
        }
    }

    private static boolean s1(LinkProperties linkProperties, InetAddress inetAddress) {
        if (linkProperties != null && linkProperties.getInterfaceName() != null && !linkProperties.getInterfaceName().isEmpty()) {
            Iterator<LinkAddress> it = linkProperties.getLinkAddresses().iterator();
            while (it.hasNext()) {
                InetAddress address = it.next().getAddress();
                if (address != null && !address.isAnyLocalAddress() && !address.isLoopbackAddress() && !address.isMulticastAddress() && (inetAddress == null || (inetAddress instanceof Inet6Address) == (address instanceof Inet6Address))) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void t1(Network network, LinkProperties linkProperties, ConnectivityManager.NetworkCallback networkCallback) {
        synchronized (this.i) {
            try {
                if (!this.u && this.s == networkCallback) {
                    boolean z = this.v && !network.equals(this.z);
                    if (!z) {
                        if (!this.v) {
                            this.v = true;
                            this.z = network;
                            this.A = linkProperties;
                            y1();
                        }
                    }
                    if (z) {
                        T1("WIFI_SELECTED_NETWORK_REPLACED");
                        return;
                    }
                    S1("WIFI_NETWORK_READY_WAITING_CARPLAY_ENDPOINT");
                    Handler handler = this.h;
                    u6 u6Var = new u6(3);
                    u6Var.b = this;
                    VarHandle.storeStoreFence();
                    handler.post(u6Var);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private void u1(ConnectivityManager connectivityManager, Network network) {
        h hVar = new h(connectivityManager, network);
        d dVar = new d(this, network, hVar);
        synchronized (this.i) {
            if (!this.u && this.s == null) {
                hVar.c = dVar;
                this.R = hVar;
                if (C1(connectivityManager, new NetworkRequest.Builder().addTransportType(1).removeCapability(12).build(), dVar, false)) {
                    synchronized (this.i) {
                        try {
                            if (hVar.c()) {
                                hVar.d = connectivityManager.getLinkProperties(network);
                                H0();
                            } else {
                                if (!this.u) {
                                    T1("WIFI_SELECTED_NETWORK_LOST");
                                }
                            }
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean v1(ConnectivityManager.NetworkCallback networkCallback) {
        boolean z;
        synchronized (this.i) {
            try {
                z = !this.u && this.s == networkCallback;
            } catch (Throwable th) {
                throw th;
            }
        }
        return z;
    }

    public static InetSocketAddress w1(Context context, Network network, Iap2Csm.a aVar) throws IOException {
        return x1(context, network, aVar, null);
    }

    public static /* bridge */ /* synthetic */ SocketException x0(IOException iOException) {
        return N1("RFCOMM_WRITE_FAILED", iOException);
    }

    public static InetSocketAddress x1(Context context, Network network, Iap2Csm.a aVar, String str) throws IOException {
        try {
            InetAddress numericAddress = InetAddresses.parseNumericAddress(aVar.b);
            if (numericAddress instanceof Inet6Address) {
                Inet6Address inet6Address = (Inet6Address) numericAddress;
                if (inet6Address.isLinkLocalAddress()) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
                    LinkProperties linkProperties = connectivityManager == null ? null : connectivityManager.getLinkProperties(network);
                    if (linkProperties != null && linkProperties.getInterfaceName() != null) {
                        str = linkProperties.getInterfaceName();
                    }
                    NetworkInterface byName = str == null ? null : NetworkInterface.getByName(str);
                    if (byName == null) {
                        SyntheticRefs.c("CARPLAY_WIFI_INTERFACE_SCOPE_UNAVAILABLE");
                        return null;
                    }
                    numericAddress = Inet6Address.getByAddress((String) null, inet6Address.getAddress(), byName.getIndex());
                }
            }
            if (!numericAddress.isAnyLocalAddress() && !numericAddress.isLoopbackAddress() && !numericAddress.isMulticastAddress()) {
                return new InetSocketAddress(numericAddress, aVar.h);
            }
            SyntheticRefs.c("CARPLAY_INVALID_PEER_ADDRESS_SCOPE");
            return null;
        } catch (IllegalArgumentException e2) {
            throw new IOException("CARPLAY_INVALID_NUMERIC_ADDRESS", e2);
        }
    }

    private void y1() {
        LinkProperties linkProperties;
        if (this.u || this.J || !this.v || this.z == null || (linkProperties = this.A) == null || linkProperties.getInterfaceName() == null || this.A.getInterfaceName().isEmpty()) {
            this.I = null;
            return;
        }
        Iterator<LinkAddress> it = this.A.getLinkAddresses().iterator();
        boolean z = false;
        boolean z2 = false;
        while (it.hasNext()) {
            InetAddress address = it.next().getAddress();
            if (address != null && !address.isAnyLocalAddress() && !address.isLoopbackAddress() && !address.isMulticastAddress()) {
                if (address instanceof Inet6Address) {
                    z2 = true;
                } else {
                    z = true;
                }
            }
        }
        this.I = new g(this.z, this.A.getInterfaceName(), z, z2, 0);
    }

    private static void z1(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
            }
        }
    }

    public void O1(String str, boolean z) {
        P1(str, z, null);
    }

    /* JADX WARN: Code duplicated, block: B:128:0x01a3 A[Catch: Exception -> 0x0062, TryCatch #7 {Exception -> 0x0062, blocks: (B:15:0x0023, B:20:0x0039, B:23:0x0041, B:24:0x004a, B:26:0x0050, B:32:0x0068, B:35:0x006f, B:36:0x0084, B:46:0x0093, B:51:0x009e, B:53:0x00b3, B:58:0x00c0, B:60:0x00c4, B:62:0x00cc, B:65:0x00d5, B:67:0x00d9, B:69:0x00e3, B:70:0x00e5, B:80:0x00f7, B:83:0x0112, B:86:0x011b, B:87:0x0138, B:97:0x0149, B:99:0x0150, B:103:0x0175, B:98:0x014d, B:105:0x0179, B:106:0x017e, B:116:0x0191, B:119:0x0196, B:126:0x01a0, B:128:0x01a3, B:131:0x01da, B:129:0x01b9, B:134:0x01f8, B:49:0x0098, B:50:0x0099, B:135:0x01f9, B:18:0x0032, B:71:0x00e6, B:73:0x00ea, B:74:0x00ed, B:78:0x00f4, B:79:0x00f6, B:107:0x017f, B:109:0x0183, B:112:0x0189, B:88:0x0139, B:90:0x013d, B:93:0x0142, B:94:0x0144, B:122:0x019a, B:123:0x019d), top: B:158:0x0023, inners: #0, #1, #2, #3, #4 }] */
    /* JADX WARN: Code duplicated, block: B:129:0x01b9 A[Catch: Exception -> 0x0062, TryCatch #7 {Exception -> 0x0062, blocks: (B:15:0x0023, B:20:0x0039, B:23:0x0041, B:24:0x004a, B:26:0x0050, B:32:0x0068, B:35:0x006f, B:36:0x0084, B:46:0x0093, B:51:0x009e, B:53:0x00b3, B:58:0x00c0, B:60:0x00c4, B:62:0x00cc, B:65:0x00d5, B:67:0x00d9, B:69:0x00e3, B:70:0x00e5, B:80:0x00f7, B:83:0x0112, B:86:0x011b, B:87:0x0138, B:97:0x0149, B:99:0x0150, B:103:0x0175, B:98:0x014d, B:105:0x0179, B:106:0x017e, B:116:0x0191, B:119:0x0196, B:126:0x01a0, B:128:0x01a3, B:131:0x01da, B:129:0x01b9, B:134:0x01f8, B:49:0x0098, B:50:0x0099, B:135:0x01f9, B:18:0x0032, B:71:0x00e6, B:73:0x00ea, B:74:0x00ed, B:78:0x00f4, B:79:0x00f6, B:107:0x017f, B:109:0x0183, B:112:0x0189, B:88:0x0139, B:90:0x013d, B:93:0x0142, B:94:0x0144, B:122:0x019a, B:123:0x019d), top: B:158:0x0023, inners: #0, #1, #2, #3, #4 }] */
    /* JADX WARN: Code duplicated, block: B:131:0x01da A[Catch: Exception -> 0x0062, TRY_LEAVE, TryCatch #7 {Exception -> 0x0062, blocks: (B:15:0x0023, B:20:0x0039, B:23:0x0041, B:24:0x004a, B:26:0x0050, B:32:0x0068, B:35:0x006f, B:36:0x0084, B:46:0x0093, B:51:0x009e, B:53:0x00b3, B:58:0x00c0, B:60:0x00c4, B:62:0x00cc, B:65:0x00d5, B:67:0x00d9, B:69:0x00e3, B:70:0x00e5, B:80:0x00f7, B:83:0x0112, B:86:0x011b, B:87:0x0138, B:97:0x0149, B:99:0x0150, B:103:0x0175, B:98:0x014d, B:105:0x0179, B:106:0x017e, B:116:0x0191, B:119:0x0196, B:126:0x01a0, B:128:0x01a3, B:131:0x01da, B:129:0x01b9, B:134:0x01f8, B:49:0x0098, B:50:0x0099, B:135:0x01f9, B:18:0x0032, B:71:0x00e6, B:73:0x00ea, B:74:0x00ed, B:78:0x00f4, B:79:0x00f6, B:107:0x017f, B:109:0x0183, B:112:0x0189, B:88:0x0139, B:90:0x013d, B:93:0x0142, B:94:0x0144, B:122:0x019a, B:123:0x019d), top: B:158:0x0023, inners: #0, #1, #2, #3, #4 }] */
    public void P1(String str, boolean z, h5 h5Var) {
        synchronized (this.i) {
            if (!this.t && !this.u) {
                this.n = h5Var;
                this.t = true;
            } else {
                if (h5Var != null) {
                    h5Var.close();
                }
                throw new IllegalStateException("Bootstrap instance already used");
            }
        }
        if (this.a.checkSelfPermission("android.permission.BLUETOOTH_CONNECT") != 0) {
            T1("BLUETOOTH_CONNECT_PERMISSION_REQUIRED");
            return;
        }
        try {
            BluetoothManager bluetoothManager = (BluetoothManager) this.a.getSystemService(BluetoothManager.class);
            BluetoothAdapter adapter = bluetoothManager == null ? null : bluetoothManager.getAdapter();
            if (adapter != null && adapter.isEnabled()) {
                BluetoothDevice bluetoothDevice = null;
                for (BluetoothDevice bluetoothDevice2 : adapter.getBondedDevices()) {
                    if (bluetoothDevice2.getAddress().equalsIgnoreCase(str)) {
                        bluetoothDevice = bluetoothDevice2;
                    }
                }
                if (bluetoothDevice == null) {
                    T1("SELECT_A_BONDED_RECEIVER_FIRST");
                    return;
                }
                int i2 = 2;
                try {
                    Context context = this.a;
                    ExecutorService executorService = this.j;
                    t6 t6Var = new t6(i2);
                    t6Var.b = this;
                    VarHandle.storeStoreFence();
                    BluetoothRfcommProfiles bluetoothRfcommProfiles = new BluetoothRfcommProfiles(context, adapter, bluetoothDevice, executorService, t6Var);
                    synchronized (this.i) {
                        try {
                            if (this.u) {
                                bluetoothRfcommProfiles.close();
                                return;
                            }
                            this.p = bluetoothRfcommProfiles;
                            bluetoothRfcommProfiles.u();
                            this.D = io.carplay.app.prototype.r.a(this.a, bluetoothDevice.getAddress());
                            boolean z2 = false;
                            boolean z3 = this.b.p() && this.b.k();
                            if (z3 || (this.c != null && this.b.k() && this.b.i())) {
                                z2 = true;
                            }
                            this.q = z2;
                            BluetoothServerSocket bluetoothServerSocketListenUsingRfcommWithServiceRecord = h5Var == null ? adapter.listenUsingRfcommWithServiceRecord("CarPlus iAP2 Phone", T) : null;
                            synchronized (this.i) {
                                try {
                                    if (this.u) {
                                        z1(bluetoothServerSocketListenUsingRfcommWithServiceRecord);
                                    } else {
                                        this.k = bluetoothServerSocketListenUsingRfcommWithServiceRecord;
                                        this.M = SystemClock.elapsedRealtime() + V;
                                        this.h.postDelayed(this.S, V);
                                        S1("RFCOMM_LISTENING_SELECTED_PEER");
                                        AtomicInteger atomicInteger = new AtomicInteger(z ? 2 : 1);
                                        if (this.q && h5Var == null) {
                                            t6 t6Var2 = new t6(i2);
                                            t6Var2.b = this;
                                            VarHandle.storeStoreFence();
                                            u6 u6Var = new u6(9);
                                            u6Var.b = this;
                                            VarHandle.storeStoreFence();
                                            h5 h5Var2 = new h5(t6Var2, u6Var);
                                            synchronized (this.i) {
                                                if (!this.u && this.m == null) {
                                                    this.o = h5Var2;
                                                    Context context2 = this.a;
                                                    try {
                                                        if (z3) {
                                                            h5Var2.v(context2);
                                                        } else {
                                                            h5Var2.t(context2);
                                                        }
                                                        String address = bluetoothDevice.getAddress();
                                                        t6 t6Var3 = new t6(3);
                                                        t6Var3.b = this;
                                                        VarHandle.storeStoreFence();
                                                        u6 u6Var2 = new u6(10);
                                                        u6Var2.b = this;
                                                        VarHandle.storeStoreFence();
                                                        h5Var2.i(address, t6Var3, u6Var2);
                                                        S1(z3 ? "RFCOMM_PRE_PROTOCOL_DISCOVERY_READY secure_records=2 extra_secure_dials=0 deadline_extended=false radio_confirmed=false" : "RFCOMM_PHONE_IDENTITY_RECOVERY_READY secure_records=1 radio_confirmed=false");
                                                    } catch (IOException | RuntimeException unused) {
                                                        h5Var2.close();
                                                        synchronized (this.i) {
                                                            try {
                                                                if (this.o == h5Var2) {
                                                                    this.o = null;
                                                                }
                                                                S1(z3 ? "RFCOMM_PRE_PROTOCOL_DISCOVERY_UNAVAILABLE ordinary_path_retained=true" : "RFCOMM_PHONE_IDENTITY_RECOVERY_UNAVAILABLE ordinary_path_retained=true");
                                                            } catch (Throwable th) {
                                                                throw th;
                                                            }
                                                        }
                                                    }
                                                    if (h5Var == null) {
                                                        ExecutorService executorService2 = this.j;
                                                        z6 z6Var = new z6();
                                                        z6Var.a = this;
                                                        z6Var.b = bluetoothServerSocketListenUsingRfcommWithServiceRecord;
                                                        z6Var.c = bluetoothDevice;
                                                        z6Var.d = atomicInteger;
                                                        VarHandle.storeStoreFence();
                                                        executorService2.execute(z6Var);
                                                    } else {
                                                        String address2 = bluetoothDevice.getAddress();
                                                        t6 t6Var4 = new t6(4);
                                                        t6Var4.b = this;
                                                        VarHandle.storeStoreFence();
                                                        io.carplay.app.secured.p0 p0Var = new io.carplay.app.secured.p0(5);
                                                        p0Var.b = this;
                                                        p0Var.c = atomicInteger;
                                                        VarHandle.storeStoreFence();
                                                        h5Var.i(address2, t6Var4, p0Var);
                                                    }
                                                    if (z) {
                                                        v6 v6Var = new v6(i2);
                                                        v6Var.b = this;
                                                        v6Var.c = bluetoothDevice;
                                                        v6Var.d = atomicInteger;
                                                        VarHandle.storeStoreFence();
                                                        this.L = v6Var;
                                                        this.h.postDelayed(v6Var, 4000L);
                                                        S1("RFCOMM_SELECTED_PEER_ACTIVE_FALLBACK_SCHEDULED");
                                                    }
                                                }
                                                h5Var2.close();
                                            }
                                        } else {
                                            if (h5Var == null) {
                                                ExecutorService executorService3 = this.j;
                                                z6 z6Var2 = new z6();
                                                z6Var2.a = this;
                                                z6Var2.b = bluetoothServerSocketListenUsingRfcommWithServiceRecord;
                                                z6Var2.c = bluetoothDevice;
                                                z6Var2.d = atomicInteger;
                                                VarHandle.storeStoreFence();
                                                executorService3.execute(z6Var2);
                                            } else {
                                                String address3 = bluetoothDevice.getAddress();
                                                t6 t6Var5 = new t6(4);
                                                t6Var5.b = this;
                                                VarHandle.storeStoreFence();
                                                io.carplay.app.secured.p0 p0Var2 = new io.carplay.app.secured.p0(5);
                                                p0Var2.b = this;
                                                p0Var2.c = atomicInteger;
                                                VarHandle.storeStoreFence();
                                                h5Var.i(address3, t6Var5, p0Var2);
                                            }
                                            if (z) {
                                                v6 v6Var2 = new v6(i2);
                                                v6Var2.b = this;
                                                v6Var2.c = bluetoothDevice;
                                                v6Var2.d = atomicInteger;
                                                VarHandle.storeStoreFence();
                                                this.L = v6Var2;
                                                this.h.postDelayed(v6Var2, 4000L);
                                                S1("RFCOMM_SELECTED_PEER_ACTIVE_FALLBACK_SCHEDULED");
                                            }
                                        }
                                    }
                                } catch (Throwable th2) {
                                    throw th2;
                                }
                            }
                            return;
                        } catch (Throwable th3) {
                            throw th3;
                        }
                    }
                } catch (RuntimeException unused2) {
                    S1("RFCOMM_SELECTED_PROFILE_SNAPSHOT hfp=UNKNOWN a2dp=UNKNOWN observation_unavailable=true read_only=true cause_unconfirmed=true values_logged=false");
                }
            }
            T1("BLUETOOTH_ENABLE_BY_USER_REQUIRED");
        } catch (Exception e2) {
            T1(w1.e("RFCOMM_START_FAILED: ", J1(e2)));
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        T1("STOPPED_BY_OWNER");
    }

    public static final class l {
        public final String a;
        public final String b;

        private l(String str, String str2) {
            this.a = str;
            this.b = str2;
        }

        public String toString() {
            return this.a + " (" + this.b + ")";
        }

        public /* synthetic */ l(String str, String str2, int i) {
            this(str, str2);
        }
    }

    public static final class m extends w1 {
        private final byte[] a;
        private final IOException b;

        private m(byte[] bArr, IOException iOException) {
            this.a = bArr;
            this.b = iOException;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof m)) {
                return false;
            }
            m mVar = (m) obj;
            return Objects.equals(this.a, mVar.a) && Objects.equals(this.b, mVar.b);
        }

        public byte[] g() {
            return this.a;
        }

        public IOException h() {
            return this.b;
        }

        public final int hashCode() {
            byte[] bArr = this.a;
            IOException iOException = this.b;
            return Objects.hashCode(iOException) + (Objects.hashCode(bArr) * 31);
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(m.class, sb, "[");
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

        public /* synthetic */ m(byte[] bArr, IOException iOException, int i) {
            this(bArr, iOException);
        }
    }

    public static final class g extends w1 {
        private final Network a;
        private final String b;
        private final boolean c;
        private final boolean d;

        private g(Network network, String str, boolean z, boolean z2) {
            this.a = network;
            this.b = str;
            this.c = z;
            this.d = z2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof g)) {
                return false;
            }
            g gVar = (g) obj;
            return this.c == gVar.c && this.d == gVar.d && Objects.equals(this.a, gVar.a) && Objects.equals(this.b, gVar.b);
        }

        public String g() {
            return this.b;
        }

        public boolean h() {
            return this.c;
        }

        public final int hashCode() {
            boolean z = this.c;
            boolean z2 = this.d;
            Network network = this.a;
            String str = this.b;
            return Objects.hashCode(str) + ((Objects.hashCode(network) + ((Boolean.hashCode(z2) + (Boolean.hashCode(z) * 31)) * 31)) * 31);
        }

        public boolean i() {
            return this.d;
        }

        public Network j() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, Boolean.valueOf(this.c), Boolean.valueOf(this.d)};
            String[] strArrSplit = "a;b;c;d".length() == 0 ? new String[0] : "a;b;c;d".split(";");
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

        public /* synthetic */ g(Network network, String str, boolean z, boolean z2, int i) {
            this(network, str, z, z2);
        }
    }

    public static final class j {
        final AtomicLong a;
        final AtomicLong b;
        final AtomicLong c;
        final AtomicLong d;
        volatile String e;

        private j() {
            this.a = new AtomicLong();
            this.b = new AtomicLong();
            this.c = new AtomicLong();
            this.d = new AtomicLong();
            this.e = "OPEN";
        }

        public boolean a(ArrayBlockingQueue<m> arrayBlockingQueue) {
            return !arrayBlockingQueue.isEmpty() || this.b.get() > this.d.get();
        }

        public String b(int i) {
            long j = this.d.get();
            long j2 = this.b.get();
            return "RFCOMM_INPUT_TOTALS read_calls=" + this.a.get() + " read_bytes=" + j2 + " enqueued_bytes=" + this.c.get() + " consumed_bytes=" + j + " undelivered_bytes=" + Math.max(0L, j2 - j) + " queue_depth=" + i + " reader_end=" + this.e;
        }

        public /* synthetic */ j(int i) {
            this();
        }
    }

    public PairingSession(Context context, k kVar, String str) {
        this(context, kVar, str, false);
    }

    public PairingSession(Context context, k kVar, String str, boolean z) {
        this(context, kVar, str, z, false);
    }

    public PairingSession(Context context, k kVar, String str, boolean z, boolean z2) {
        this(context, kVar, str, z, z2, false);
    }

    public PairingSession(Context context, k kVar) {
        this(context, kVar, null);
    }
}
