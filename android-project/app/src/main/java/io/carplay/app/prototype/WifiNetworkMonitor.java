/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 WifiNetworkMonitor。WiFi 状态监听：跟踪关联结果、网络切换与重连触发。
 *   This file defines WifiNetworkMonitor. WiFi monitor tracking association results, network switches and reconnect triggers.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

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
import io.carplay.app.secured.ProtoMessageBase;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;



/**
 * WiFi 状态监听：跟踪关联结果、网络切换与重连触发。
 *
 * WiFi monitor tracking association results, network switches and reconnect triggers.
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

    /**
     * 扩展实现：在 ConnectivityManager.NetworkCallback 基础上补充 WifiNetworkMonitor 需要的局部行为。
     *
     * Extension of ConnectivityManager.NetworkCallback adding the local behaviour WifiNetworkMonitor needs.
     */
    public class a extends ConnectivityManager.NetworkCallback {
        public a() {
        }

        
        public  void d() {
            WifiNetworkMonitor.this.s("CURRENT_WIFI_SELECTED_NETWORK_CHANGED");
        }

        
        public  void e(LinkProperties linkProperties) {
            WifiNetworkMonitor.this.x(linkProperties);
        }

        
        public  void f() {
            WifiNetworkMonitor.this.s("CURRENT_WIFI_SELECTED_NETWORK_LOST");
        }

        @Override 
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

        @Override 
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            if (WifiNetworkMonitor.this.b.equals(network)) {
                Handler handler = WifiNetworkMonitor.this.f;
                ReleaseCheckTask gVar = new ReleaseCheckTask(1);
                gVar.b = this;
                gVar.c = linkProperties;
                VarHandle.storeStoreFence();
                handler.post(gVar);
            }
        }

        @Override 
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

    /**
     * 回调实现：实现 CarPlayCtrlAdvertiser.b 定义的契约。
     *
     * Callback implementation of the CarPlayCtrlAdvertiser.b contract.
     */
    public class b implements CarPlayCtrlAdvertiser.b {
        public b() {
        }

        @Override 
        public void a(String str) {
            WifiNetworkMonitor.this.z(str);
        }

        @Override 
        public void b() {
            ControlChannel.MarkInvited();
            WifiNetworkMonitor.this.z("CURRENT_WIFI_INVITATION_HINT_ONLY authentication_required=true");
        }
    }

    /**
     * 回调实现：实现 AirplayServiceProbe.d 定义的契约。
     *
     * Callback implementation of the AirplayServiceProbe.d contract.
     */
    public class c implements AirplayServiceProbe.d {
        public c() {
        }

        
        public  void f(String str, int i, String str2) {
            WifiNetworkMonitor.this.q(str, i, str2);
        }

        
        public  void g(String str) {
            WifiNetworkMonitor.this.s(ProtoMessageBase.e("CURRENT_WIFI_", str));
        }

        @Override 
        public void a(String str) {
            WifiNetworkMonitor.this.z(str);
        }

        @Override 
        public void b(String str) {
            Handler handler = WifiNetworkMonitor.this.f;
            ReleaseCheckTask gVar = new ReleaseCheckTask(2);
            gVar.b = this;
            gVar.c = str;
            VarHandle.storeStoreFence();
            handler.post(gVar);
        }

        @Override 
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

    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface d {
        void a(String str);

        void b(String str);

        void e(Network network, Iap2Csm.a aVar, String str, String str2);
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class e extends ProtoMessageBase {
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
            return Objects.hashCode(set) + ProtoMessageBase.b(str, Objects.hashCode(network) * 31, 31);
        }

        public Network i() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, this.c};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
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

    
    public synchronized void s(String str) {
        if (this.h) {
            return;
        }
        close();
        this.d.b(str);
    }

    
    public static  boolean t(boolean z, LinkAddress linkAddress) {
        return ((linkAddress.getAddress() instanceof Inet6Address) != z || linkAddress.getAddress().isAnyLocalAddress() || linkAddress.getAddress().isLoopbackAddress()) ? false : true;
    }

    
    public  void u() {
        s("CURRENT_WIFI_DISCOVERY_OR_AUTH_DEADLINE");
    }

    
    public  boolean v(InetAddress inetAddress) {
        Stream<LinkAddress> stream = this.m.stream();
        k kVar = new k(0);
        kVar.b = inetAddress;
        VarHandle.storeStoreFence();
        return stream.anyMatch(kVar);
    }

    
    public static  boolean w(InetAddress inetAddress, LinkAddress linkAddress) {
        return ((linkAddress.getAddress() instanceof Inet6Address) != (inetAddress instanceof Inet6Address) || linkAddress.getAddress().isAnyLocalAddress() || linkAddress.getAddress().isLoopbackAddress()) ? false : true;
    }

    
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

    
    public synchronized void z(String str) {
        if (!this.h) {
            this.d.a(str);
        }
    }

    @Override 
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
