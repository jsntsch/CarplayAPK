package io.carplay.app.prototype;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import io.carplay.app.secured.ControlChannel;
import io.carplay.app.secured.Iap2Csm;
import io.carplay.app.secured.SyntheticRefs;
import io.carplay.app.secured.w1;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/* JADX INFO: loaded from: classes.dex */
/**
 * WifiNetworkMonitor
 *
 * <p>原混淆名：{@code io.carplay.app.prototype.m}（jadx 产物 m.java）
 *
 * <p>命名依据：CURRENT_WIFI_SELECTED_NETWORK_CHANGED / LOST
 */
final class WifiNetworkMonitor implements AutoCloseable {
    private final Context a;
    private final Network b;
    private final String c;
    private final d d;
    private final ConnectivityManager e;
    private final Handler f;
    private final long g;
    private boolean h;
    private boolean i;
    private boolean j;
    private boolean k;
    private String l;
    private List<LinkAddress> m;
    private AirplayServiceProbe n;
    private CarPlayCtrlAdvertiser o;
    private final Runnable p;
    private final ConnectivityManager.NetworkCallback q;

    public class a extends ConnectivityManager.NetworkCallback {
        public a() {
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void d() {
            WifiNetworkMonitor.this.s("CURRENT_WIFI_SELECTED_NETWORK_CHANGED");
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void e(LinkProperties linkProperties) {
            WifiNetworkMonitor.this.x(linkProperties);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void f() {
            WifiNetworkMonitor.this.s("CURRENT_WIFI_SELECTED_NETWORK_LOST");
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            if (!WifiNetworkMonitor.this.b.equals(network) || WifiNetworkMonitor.E(networkCapabilities)) {
                return;
            }
            Handler handler = WifiNetworkMonitor.this.f;
            l lVar = new l(1);
            lVar.b = this;
            VarHandle.storeStoreFence();
            handler.post(lVar);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            if (WifiNetworkMonitor.this.b.equals(network)) {
                Handler handler = WifiNetworkMonitor.this.f;
                g gVar = new g(1);
                gVar.b = this;
                gVar.c = linkProperties;
                VarHandle.storeStoreFence();
                handler.post(gVar);
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            if (WifiNetworkMonitor.this.b.equals(network)) {
                Handler handler = WifiNetworkMonitor.this.f;
                l lVar = new l(0);
                lVar.b = this;
                VarHandle.storeStoreFence();
                handler.post(lVar);
            }
        }
    }

    public class b implements CarPlayCtrlAdvertiser.b {
        public b() {
        }

        @Override // io.carplay.app.prototype.CarPlayCtrlAdvertiser.b
        public void a(String str) {
            WifiNetworkMonitor.this.z(str);
        }

        @Override // io.carplay.app.prototype.CarPlayCtrlAdvertiser.b
        public void b() {
            ControlChannel.MarkInvited();
            WifiNetworkMonitor.this.z("CURRENT_WIFI_INVITATION_HINT_ONLY authentication_required=true");
        }
    }

    public class c implements AirplayServiceProbe.d {
        public c() {
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void f(String str, int i, String str2) {
            WifiNetworkMonitor.this.q(str, i, str2);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void g(String str) {
            WifiNetworkMonitor.this.s(w1.e("CURRENT_WIFI_", str));
        }

        @Override // io.carplay.app.prototype.AirplayServiceProbe.d
        public void a(String str) {
            WifiNetworkMonitor.this.z(str);
        }

        @Override // io.carplay.app.prototype.AirplayServiceProbe.d
        public void b(String str) {
            Handler handler = WifiNetworkMonitor.this.f;
            g gVar = new g(2);
            gVar.b = this;
            gVar.c = str;
            VarHandle.storeStoreFence();
            handler.post(gVar);
        }

        @Override // io.carplay.app.prototype.AirplayServiceProbe.d
        public void c(String str, int i, String str2) {
            Handler handler = WifiNetworkMonitor.this.f;
            n nVar = new n();
            nVar.a = this;
            nVar.b = str;
            nVar.c = i;
            nVar.d = str2;
            VarHandle.storeStoreFence();
            handler.post(nVar);
        }
    }

    public interface d {
        void a(String str);

        void b(String str);

        void e(Network network, Iap2Csm.a aVar, String str, String str2);
    }

    public static final class e extends w1 {
        private final Network a;
        private final String b;
        private final Set<LinkAddress> c;

        public e(Network network, String str, Set<LinkAddress> set) {
            Objects.requireNonNull(network);
            Objects.requireNonNull(str);
            Set<LinkAddress> setCopyOf = Set.copyOf(set);
            this.a = network;
            this.b = str;
            this.c = setCopyOf;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof e)) {
                return false;
            }
            e eVar = (e) obj;
            return Objects.equals(this.a, eVar.a) && Objects.equals(this.b, eVar.b) && Objects.equals(this.c, eVar.c);
        }

        public Set<LinkAddress> g() {
            return this.c;
        }

        public String h() {
            return this.b;
        }

        public final int hashCode() {
            Network network = this.a;
            String str = this.b;
            Set<LinkAddress> set = this.c;
            return Objects.hashCode(set) + w1.b(str, Objects.hashCode(network) * 31, 31);
        }

        public Network i() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, this.c};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(e.class, sb, "[");
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

    public WifiNetworkMonitor(Context context, Network network, String str, long j, d dVar) {
        this.f = new Handler(Looper.getMainLooper());
        p pVar = new p(0);
        pVar.b = this;
        VarHandle.storeStoreFence();
        this.p = pVar;
        this.q = new a();
        if (j < 1 || j > 90000) {
            SyntheticRefs.b("current WLAN budget out of bounds");
            throw null;
        }
        this.a = context;
        Objects.requireNonNull(network);
        this.b = network;
        this.c = str;
        Objects.requireNonNull(dVar);
        this.d = dVar;
        this.g = j;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
        Objects.requireNonNull(connectivityManager);
        this.e = connectivityManager;
    }

    public static boolean A(Context context, e eVar) {
        e eVarR;
        return (eVar == null || eVar.h().isBlank() || eVar.g().isEmpty() || (eVarR = r(context, eVar.i())) == null || !eVar.h().equals(eVarR.h()) || !eVarR.g().containsAll(eVar.g())) ? false : true;
    }

    public static Network B(Context context) {
        if (context == null) {
            return null;
        }
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
            if (connectivityManager == null) {
                return null;
            }
            Network network = null;
            for (Network network2 : connectivityManager.getAllNetworks()) {
                if (C(context, network2)) {
                    if (network != null && !network.equals(network2)) {
                        return null;
                    }
                    network = network2;
                }
            }
            return network;
        } catch (RuntimeException unused) {
            return null;
        }
    }

    public static boolean C(Context context, Network network) {
        return r(context, network) != null;
    }

    private static Set<LinkAddress> D(LinkProperties linkProperties) {
        HashSet hashSet = new HashSet();
        for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
            InetAddress address = linkAddress.getAddress();
            if (address != null && !address.isAnyLocalAddress() && !address.isLoopbackAddress() && !address.isMulticastAddress()) {
                hashSet.add(linkAddress);
            }
        }
        return Set.copyOf(hashSet);
    }

    public static boolean E(NetworkCapabilities networkCapabilities) {
        return (networkCapabilities == null || !networkCapabilities.hasTransport(1) || networkCapabilities.hasTransport(4)) ? false : true;
    }

    private boolean p(LinkProperties linkProperties) {
        return linkProperties != null && Objects.equals(this.l, linkProperties.getInterfaceName()) && new HashSet(linkProperties.getLinkAddresses()).containsAll(this.m);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void q(String str, int i, String str2) {
        if (!this.h && !this.j) {
            if (!E(this.e.getNetworkCapabilities(this.b))) {
                s("CURRENT_WIFI_SELECTION_UNAVAILABLE");
                return;
            }
            LinkProperties linkProperties = this.e.getLinkProperties(this.b);
            if (!p(linkProperties)) {
                s("CURRENT_WIFI_LINK_CHANGED_RESTART_REQUIRED");
                return;
            }
            try {
                Iap2Csm.a aVarH = Iap2Csm.h(str, i, str2);
                boolean z = PairingSession.x1(this.a, this.b, aVarH, this.l).getAddress() instanceof Inet6Address;
                Stream<LinkAddress> stream = linkProperties.getLinkAddresses().stream();
                o oVar = new o();
                oVar.a = z;
                VarHandle.storeStoreFence();
                if (stream.noneMatch(oVar)) {
                    throw new IOException("CURRENT_WIFI_ADDRESS_FAMILY_UNAVAILABLE");
                }
                this.j = true;
                this.d.e(this.b, aVarH, this.l, this.c);
            } catch (Exception e2) {
                s("CURRENT_WIFI_INVALID_ENDPOINT ".concat(e2.getClass().getSimpleName()));
            }
        }
    }

    public static e r(Context context, Network network) {
        LinkProperties linkProperties;
        if (context != null && network != null) {
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
                if (connectivityManager == null) {
                    return null;
                }
                for (Network network2 : connectivityManager.getAllNetworks()) {
                    if (network.equals(network2)) {
                        if (E(connectivityManager.getNetworkCapabilities(network)) && (linkProperties = connectivityManager.getLinkProperties(network)) != null && linkProperties.getInterfaceName() != null && !linkProperties.getInterfaceName().isBlank()) {
                            Set<LinkAddress> setD = D(linkProperties);
                            if (setD.isEmpty()) {
                                return null;
                            }
                            return new e(network, linkProperties.getInterfaceName(), setD);
                        }
                        return null;
                    }
                }
            } catch (RuntimeException unused) {
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void s(String str) {
        if (this.h) {
            return;
        }
        close();
        this.d.b(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean t(boolean z, LinkAddress linkAddress) {
        return ((linkAddress.getAddress() instanceof Inet6Address) != z || linkAddress.getAddress().isAnyLocalAddress() || linkAddress.getAddress().isLoopbackAddress()) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void u() {
        s("CURRENT_WIFI_DISCOVERY_OR_AUTH_DEADLINE");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean v(InetAddress inetAddress) {
        Stream<LinkAddress> stream = this.m.stream();
        k kVar = new k(0);
        kVar.b = inetAddress;
        VarHandle.storeStoreFence();
        return stream.anyMatch(kVar);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean w(InetAddress inetAddress, LinkAddress linkAddress) {
        return ((linkAddress.getAddress() instanceof Inet6Address) != (inetAddress instanceof Inet6Address) || linkAddress.getAddress().isAnyLocalAddress() || linkAddress.getAddress().isLoopbackAddress()) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void x(LinkProperties linkProperties) {
        if (this.h) {
            return;
        }
        if (!E(this.e.getNetworkCapabilities(this.b))) {
            s("CURRENT_WIFI_SELECTION_UNAVAILABLE");
            return;
        }
        InetAddress inetAddressM = CarPlayCtrlAdvertiser.m(linkProperties);
        AirplayServiceProbe airplayServiceProbe = this.n;
        if (inetAddressM == null) {
            if (airplayServiceProbe != null) {
                s("CURRENT_WIFI_LOCAL_ADDRESS_LOST");
            }
            return;
        }
        if (airplayServiceProbe != null) {
            if (!p(linkProperties)) {
                s("CURRENT_WIFI_LINK_CHANGED_RESTART_REQUIRED");
            }
            return;
        }
        this.l = linkProperties.getInterfaceName();
        this.m = List.copyOf(linkProperties.getLinkAddresses());
        this.d.a("CURRENT_WIFI_DISCOVERING selected_network_only=true bootstrap_parameters_required=false authentication_required=true");
        try {
            CarPlayCtrlAdvertiser carPlayCtrlAdvertiser = new CarPlayCtrlAdvertiser(this.a, this.b, linkProperties, this.c, new b());
            this.o = carPlayCtrlAdvertiser;
            carPlayCtrlAdvertiser.q();
        } catch (Exception unused) {
            this.d.a("CURRENT_WIFI_CTRL_ADVERTISEMENT_UNAVAILABLE discovery_retained=true");
        }
        Context context = this.a;
        Network network = this.b;
        c cVar = new c();
        k kVar = new k(1);
        kVar.b = this;
        VarHandle.storeStoreFence();
        AirplayServiceProbe airplayServiceProbe2 = new AirplayServiceProbe(context, network, cVar, kVar);
        this.n = airplayServiceProbe2;
        airplayServiceProbe2.A();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void z(String str) {
        if (!this.h) {
            this.d.a(str);
        }
    }

    @Override // java.lang.AutoCloseable
    public synchronized void close() {
        try {
            if (this.h) {
                return;
            }
            this.h = true;
            this.f.removeCallbacks(this.p);
            AirplayServiceProbe airplayServiceProbe = this.n;
            if (airplayServiceProbe != null) {
                airplayServiceProbe.close();
            }
            CarPlayCtrlAdvertiser carPlayCtrlAdvertiser = this.o;
            if (carPlayCtrlAdvertiser != null) {
                carPlayCtrlAdvertiser.close();
            }
            if (this.k) {
                this.k = false;
                try {
                    this.e.unregisterNetworkCallback(this.q);
                } catch (RuntimeException unused) {
                }
            }
        } catch (Throwable th) {
            throw th;
        }
    }

    public synchronized void o() {
        if (!this.h) {
            this.f.removeCallbacks(this.p);
        }
    }

    public synchronized void y() {
        if (!this.h && !this.i) {
            this.i = true;
            if (!E(this.e.getNetworkCapabilities(this.b))) {
                s("CURRENT_WIFI_SELECTION_UNAVAILABLE");
                return;
            }
            this.f.postDelayed(this.p, this.g);
            try {
                this.k = true;
                this.e.registerNetworkCallback(new NetworkRequest.Builder().addTransportType(1).build(), this.q);
                x(this.e.getLinkProperties(this.b));
            } catch (RuntimeException e2) {
                s("CURRENT_WIFI_START_FAILED ".concat(e2.getClass().getSimpleName()));
            }
        }
    }

    public WifiNetworkMonitor(Context context, Network network, String str, d dVar) {
        this(context, network, str, 90000L, dVar);
    }
}
