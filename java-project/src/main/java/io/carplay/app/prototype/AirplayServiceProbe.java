package io.carplay.app.prototype;

import android.content.Context;
import android.net.Network;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import io.carplay.app.secured.u0;
import io.carplay.app.secured.v0;
import io.carplay.app.secured.w0;
import io.carplay.app.secured.ProtoMessageBase;
import java.lang.invoke.VarHandle;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/* JADX INFO: loaded from: classes.dex */
/**
 * AirplayServiceProbe
 *
 * <p>原混淆名：{@code io.carplay.app.prototype.j}（jadx 产物 j.java）
 *
 * <p>命名依据：_airplay._tcp 发现 + candidate 选择；对应 hypercar net/AirplayServiceProbe.kt

 * <p>发现车机的 {@code _airplay._tcp} 服务并从中挑一个候选（candidate）来连。
 * 车机可能同时广播多条记录（不同网卡、IPv4/IPv6、CarPlay 与 AirPlay 并存），
 * 所以这里有「多候选择优」和「拒绝非车机网络」的逻辑。
 */
public final class AirplayServiceProbe implements AutoCloseable {
    private static final String s = "_airplay._tcp";
    private final Object a;
    private final NsdManager b;
    private final Network c;
    private final d d;
    private final Predicate<InetAddress> e;
    private final AtomicBoolean f;
    private final ScheduledExecutorService g;
    private final Executor h;
    private final ArrayDeque<NsdServiceInfo> i;
    private final Set<String> j;
    private final Map<String, c> k;
    private boolean l;
    private boolean m;
    private boolean n;
    private ScheduledFuture<?> o;
    private ScheduledFuture<?> p;
    private NsdManager.ResolveListener q;
    private final NsdManager.DiscoveryListener r;

    public class a implements NsdManager.DiscoveryListener {
        public a() {
        }

        @Override // android.net.nsd.NsdManager.DiscoveryListener
        public void onDiscoveryStarted(String str) {
            AirplayServiceProbe.this.B("ENDPOINT_DISCOVERY_STARTED_UNTRUSTED");
        }

        @Override // android.net.nsd.NsdManager.DiscoveryListener
        public void onDiscoveryStopped(String str) {
            synchronized (AirplayServiceProbe.this.a) {
                AirplayServiceProbe.this.n = false;
            }
        }

        @Override // android.net.nsd.NsdManager.DiscoveryListener
        public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
            AirplayServiceProbe.this.s(nsdServiceInfo);
        }

        @Override // android.net.nsd.NsdManager.DiscoveryListener
        public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
        }

        @Override // android.net.nsd.NsdManager.DiscoveryListener
        public void onStartDiscoveryFailed(String str, int i) {
            AirplayServiceProbe.this.C("ENDPOINT_DISCOVERY_START_FAILED code=" + i);
        }

        @Override // android.net.nsd.NsdManager.DiscoveryListener
        public void onStopDiscoveryFailed(String str, int i) {
            AirplayServiceProbe.this.C("ENDPOINT_DISCOVERY_STOP_FAILED code=" + i);
        }
    }

    public class b implements NsdManager.ResolveListener {
        public b() {
        }

        @Override // android.net.nsd.NsdManager.ResolveListener
        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
            AirplayServiceProbe.this.z(this, null);
        }

        @Override // android.net.nsd.NsdManager.ResolveListener
        public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
            AirplayServiceProbe.this.z(this, nsdServiceInfo);
        }
    }

    public interface d {
        void a(String str);

        void b(String str);

        void c(String str, int i, String str2);
    }

    public AirplayServiceProbe(Context context, Network network, d dVar, Predicate<InetAddress> predicate) {
        this.a = new Object();
        this.f = new AtomicBoolean();
        this.g = Executors.newSingleThreadScheduledExecutor(new io.carplay.app.secured.n0(2));
        u0 u0Var = new u0();
        u0Var.a = this;
        VarHandle.storeStoreFence();
        this.h = u0Var;
        this.i = new ArrayDeque<>();
        this.j = new HashSet();
        this.k = new LinkedHashMap();
        this.r = new a();
        NsdManager nsdManager = (NsdManager) context.getSystemService(NsdManager.class);
        Objects.requireNonNull(nsdManager);
        this.b = nsdManager;
        Objects.requireNonNull(network);
        this.c = network;
        Objects.requireNonNull(dVar);
        this.d = dVar;
        Objects.requireNonNull(predicate);
        this.e = predicate;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void B(String str) {
        synchronized (this.a) {
            try {
                if (!this.f.get()) {
                    this.d.a(str);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void C(String str) {
        synchronized (this.a) {
            try {
                if (this.f.compareAndSet(false, true)) {
                    p();
                    this.d.b(str);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private void p() {
        ScheduledFuture<?> scheduledFuture = this.o;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        ScheduledFuture<?> scheduledFuture2 = this.p;
        if (scheduledFuture2 != null) {
            scheduledFuture2.cancel(false);
        }
        if (this.n) {
            this.n = false;
            try {
                this.b.stopServiceDiscovery(this.r);
            } catch (RuntimeException unused) {
            }
        }
        NsdManager.ResolveListener resolveListener = this.q;
        this.q = null;
        this.i.clear();
        if (resolveListener != null && Build.VERSION.SDK_INT >= 34) {
            try {
                this.b.stopServiceResolution(resolveListener);
            } catch (RuntimeException unused2) {
            }
        }
        this.g.shutdown();
    }

    private void q() {
        if (this.f.get()) {
            return;
        }
        if (this.k.size() != 1) {
            C("ENDPOINT_DISCOVERY_NO_UNIQUE_CANDIDATE");
            return;
        }
        c next = this.k.values().iterator().next();
        if (this.f.compareAndSet(false, true)) {
            p();
            this.d.c(next.g(), next.i(), next.h());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void r() {
        synchronized (this.a) {
            try {
                if (this.f.get()) {
                    return;
                }
                this.m = false;
                if (this.n) {
                    this.n = false;
                    try {
                        this.b.stopServiceDiscovery(this.r);
                    } catch (RuntimeException unused) {
                    }
                }
                y();
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void s(NsdServiceInfo nsdServiceInfo) {
        synchronized (this.a) {
            try {
                if (!this.f.get() && this.m) {
                    if (nsdServiceInfo != null && this.c.equals(nsdServiceInfo.getNetwork())) {
                        String serviceType = nsdServiceInfo.getServiceType();
                        String serviceName = nsdServiceInfo.getServiceName();
                        if (serviceType != null && (s.equalsIgnoreCase(serviceType) || "_airplay._tcp.".equalsIgnoreCase(serviceType))) {
                            if (serviceName != null && !serviceName.isEmpty() && serviceName.length() <= 256) {
                                if (this.j.add(serviceName.concat("\n_airplay._tcp"))) {
                                    if (this.j.size() > 8) {
                                        C("ENDPOINT_DISCOVERY_SERVICE_LIMIT");
                                        return;
                                    }
                                    if (this.o == null) {
                                        ScheduledExecutorService scheduledExecutorService = this.g;
                                        v0 v0Var = new v0(0);
                                        v0Var.b = this;
                                        VarHandle.storeStoreFence();
                                        this.o = scheduledExecutorService.schedule(v0Var, 2L, TimeUnit.SECONDS);
                                    }
                                    this.i.add(nsdServiceInfo);
                                    y();
                                    return;
                                }
                                return;
                            }
                        }
                        return;
                    }
                    B("ENDPOINT_DISCOVERY_REJECTED_NETWORK");
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private static String t(NsdServiceInfo nsdServiceInfo) {
        Map<String, byte[]> attributes = nsdServiceInfo.getAttributes();
        if (attributes == null) {
            return "bonjour-candidate";
        }
        Iterator it = List.of("deviceid", "pi").iterator();
        while (it.hasNext()) {
            byte[] bArr = attributes.get((String) it.next());
            if (bArr != null && bArr.length >= 1 && bArr.length <= 128) {
                String str = new String(bArr, StandardCharsets.US_ASCII);
                if (str.matches("[A-Za-z0-9:._-]{1,128}")) {
                    return str;
                }
            }
        }
        return "bonjour-candidate";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Thread u(Runnable runnable) {
        Thread thread = new Thread(runnable, "CarPlus-endpoint-discovery");
        thread.setDaemon(true);
        return thread;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void v(Runnable runnable) {
        if (this.f.get()) {
            return;
        }
        try {
            this.g.execute(runnable);
        } catch (RejectedExecutionException unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean w(InetAddress inetAddress) {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void x() {
        C("ENDPOINT_DISCOVERY_DEADLINE");
    }

    private void y() {
        if (this.f.get() || this.q != null) {
            return;
        }
        NsdServiceInfo nsdServiceInfoPoll = this.i.poll();
        if (nsdServiceInfoPoll == null) {
            if (this.m) {
                return;
            }
            q();
            return;
        }
        b bVar = new b();
        this.q = bVar;
        try {
            this.b.resolveService(nsdServiceInfoPoll, this.h, bVar);
        } catch (RuntimeException e) {
            this.q = null;
            B("ENDPOINT_DISCOVERY_RESOLVE_EXCEPTION type=".concat(e.getClass().getSimpleName()));
            y();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void z(NsdManager.ResolveListener resolveListener, NsdServiceInfo nsdServiceInfo) {
        String hostAddress;
        synchronized (this.a) {
            try {
                if (!this.f.get() && this.q == resolveListener) {
                    InetAddress inetAddress = null;
                    this.q = null;
                    if (nsdServiceInfo != null && this.c.equals(nsdServiceInfo.getNetwork()) && nsdServiceInfo.getPort() >= 1 && nsdServiceInfo.getPort() <= 65535) {
                        List<InetAddress> hostAddresses = Build.VERSION.SDK_INT >= 34 ? nsdServiceInfo.getHostAddresses() : Collections.EMPTY_LIST;
                        if (hostAddresses == null || hostAddresses.isEmpty()) {
                            hostAddresses = nsdServiceInfo.getHost() == null ? Collections.EMPTY_LIST : List.of(nsdServiceInfo.getHost());
                        }
                        for (InetAddress inetAddress2 : hostAddresses) {
                            if (inetAddress2 != null && !inetAddress2.isAnyLocalAddress() && !inetAddress2.isLoopbackAddress() && !inetAddress2.isMulticastAddress() && this.e.test(inetAddress2) && (hostAddress = inetAddress2.getHostAddress()) != null && hostAddress.length() <= 128 && (inetAddress == null || (((inetAddress2 instanceof Inet4Address) && !(inetAddress instanceof Inet4Address)) || ((inetAddress2 instanceof Inet4Address) == (inetAddress instanceof Inet4Address) && hostAddress.compareTo(inetAddress.getHostAddress()) < 0)))) {
                                inetAddress = inetAddress2;
                            }
                        }
                        if (inetAddress != null) {
                            this.k.putIfAbsent(Base64.getEncoder().encodeToString(inetAddress.getAddress()) + ":" + nsdServiceInfo.getPort(), new c(nsdServiceInfo.getPort(), 0, inetAddress.getHostAddress(), t(nsdServiceInfo)));
                        }
                        if (this.k.size() > 1) {
                            C("ENDPOINT_DISCOVERY_AMBIGUOUS");
                            return;
                        }
                    }
                    y();
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void A() {
        synchronized (this.a) {
            if (!this.f.get() && !this.l) {
                this.l = true;
                this.m = true;
                ScheduledExecutorService scheduledExecutorService = this.g;
                v0 v0Var = new v0(1);
                v0Var.b = this;
                VarHandle.storeStoreFence();
                this.p = scheduledExecutorService.schedule(v0Var, 20L, TimeUnit.SECONDS);
                this.n = true;
                try {
                    this.b.discoverServices(s, 1, this.c, this.h, this.r);
                } catch (RuntimeException e) {
                    C("ENDPOINT_DISCOVERY_START_EXCEPTION type=".concat(e.getClass().getSimpleName()));
                }
            }
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        synchronized (this.a) {
            try {
                if (this.f.compareAndSet(false, true)) {
                    p();
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public static final class c extends ProtoMessageBase {
        private final String a;
        private final int b;
        private final String c;

        private c(String str, int i, String str2) {
            this.a = str;
            this.b = i;
            this.c = str2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof c)) {
                return false;
            }
            c cVar = (c) obj;
            return this.b == cVar.b && Objects.equals(this.a, cVar.a) && Objects.equals(this.c, cVar.c);
        }

        public String g() {
            return this.a;
        }

        public String h() {
            return this.c;
        }

        public final int hashCode() {
            int i = this.b;
            String str = this.a;
            String str2 = this.c;
            return Objects.hashCode(str2) + ProtoMessageBase.b(str, i * 31, 31);
        }

        public int i() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {this.a, Integer.valueOf(this.b), this.c};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
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

        public /* synthetic */ c(int i, int i2, String str, String str2) {
            this(str, i, str2);
        }
    }

    public AirplayServiceProbe(Context context, Network network, d dVar) {
        this(context, network, dVar, new w0(14));
    }
}
