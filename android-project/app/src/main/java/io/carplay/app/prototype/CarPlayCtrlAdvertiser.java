/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 CarPlayCtrlAdvertiser。控制服务广播：注册 _carplay-ctrl._tcp，让车机发现本机并发起连接。
 *   This file defines CarPlayCtrlAdvertiser. Control service advertiser registering _carplay-ctrl._tcp so the head unit can find and connect to this phone.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.content.Context;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import io.carplay.app.secured.ControlChannel;
import io.carplay.app.secured.ControlRequestParser;
import io.carplay.app.secured.SyntheticRefs;
import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;



/**
 * 控制服务广播：注册 _carplay-ctrl._tcp，让车机发现本机并发起连接。
 *
 * Control service advertiser registering _carplay-ctrl._tcp so the head unit can find and connect to this phone.
 */
final class CarPlayCtrlAdvertiser implements AutoCloseable {
    private static final String r = "_carplay-ctrl._tcp";
    private static final String s = "950.7.1";
    private final NsdManager a;
    private final Network b;
    private final List<LinkAddress> c;
    private final String d;
    private final InetAddress e;
    private final String f;
    private final b g;
    private final c h;
    private final AtomicBoolean i;
    private final Object j;
    private final List<ServerSocket> k;
    private final Set<Socket> l;
    private boolean m;
    private boolean n;
    private int o;
    private int p;
    private final NsdManager.RegistrationListener q;

    /**
     * 回调实现：实现 NsdManager.RegistrationListener 定义的契约。
     *
     * Callback implementation of the NsdManager.RegistrationListener contract.
     */
    public class a implements NsdManager.RegistrationListener {
        // [V1.8.24] unregisterService() 是异步的：返回时 mDNS 记录可能仍在。
        // Q1() 在 StopEarly() 之后立刻重新注册同名服务 ⇒ NsdManager 检测冲突并自动改名
        // 为 "OPPO_X8U (2)"（历史 9 次）。close() 会等本 latch（带超时）再返回。
        public final java.util.concurrent.CountDownLatch unregistered =
                new java.util.concurrent.CountDownLatch(1);

        public a() {
        }

        @Override 
        public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
            CarPlayCtrlAdvertiser.this.r("CTRL_SERVICE_REGISTRATION_FAILED code=" + i + " original_path_retained=true");
            CarPlayCtrlAdvertiser.this.close();
        }

        @Override 
        public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
            CarPlayCtrlAdvertiser carPlayCtrlAdvertiser = CarPlayCtrlAdvertiser.this;
            carPlayCtrlAdvertiser.r("CTRL_SERVICE_LIVE name=" + nsdServiceInfo.getServiceName() + " port=" + nsdServiceInfo.getPort());
            // [V1.8.24] 规范 p.425：服务实例名必须是用户可见设备名。
            // 出现 (2) 后缀说明 NsdManager 因同名冲突改名，车机按蓝牙名匹配时会对不上。
            String expectedName = ControlChannel.ServiceName();
            String actualName = nsdServiceInfo.getServiceName();
            if (actualName != null && !expectedName.equals(actualName)) {
                carPlayCtrlAdvertiser.r("CTRL_SERVICE_NAME_CONFLICT expected=" + expectedName
                        + " actual=" + actualName + " reason=NSD_AUTO_RENAME");
            }
            // [V1.8.23] 车机必须先 browse 到本服务、再连入并发 POST /ctrl-int/1/<id>
            // （固件 0xa429f/0xa42b3），才会把会话登记为 CarPlay 并桥接 HMI 触摸。
            // 之前只看到 name="OPPO_X8U (2)"（NsdManager 冲突改名）与 port=0，
            // 且 TXT 只进 logcat、握手期常被冲掉 ⇒ 把实际广播内容落进应用日志。
            try {
                java.util.Map<String, byte[]> attrs = nsdServiceInfo.getAttributes();
                StringBuilder sb = new StringBuilder("CTRL_SERVICE_TXT name=" + nsdServiceInfo.getServiceName()
                        + " type=" + nsdServiceInfo.getServiceType() + " n=" + attrs.size());
                for (java.util.Map.Entry<String, byte[]> e : attrs.entrySet()) {
                    byte[] v = e.getValue();
                    sb.append(" | ").append(e.getKey()).append('=')
                      .append(v == null ? "null" : new String(v, java.nio.charset.StandardCharsets.UTF_8));
                }
                carPlayCtrlAdvertiser.r(sb.toString());
            } catch (Throwable ignored) {
                carPlayCtrlAdvertiser.r("CTRL_SERVICE_TXT_UNAVAILABLE");
            }
            CarPlayCtrlAdvertiser.u(carPlayCtrlAdvertiser, nsdServiceInfo);
            CarPlayCtrlAdvertiser.v(carPlayCtrlAdvertiser, nsdServiceInfo);
            boolean z = carPlayCtrlAdvertiser.i.get();
            CarPlayCtrlAdvertiser carPlayCtrlAdvertiser2 = CarPlayCtrlAdvertiser.this;
            if (z) {
                carPlayCtrlAdvertiser2.s();
            } else {
                carPlayCtrlAdvertiser2.r("CTRL_SERVICE_REGISTERED network_scoped=true real_identifier=true values_logged=false");
            }
        }

        @Override 
        public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
            unregistered.countDown();
        }

        @Override 
        public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
            unregistered.countDown();
        }
    }

    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface b {
        void a(String str);

        void b();
    }

    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface c {
        ServerSocket a(InetAddress inetAddress, int i) throws IOException;
    }

    /**
     * 后台任务：在独立线程中执行一次异步动作。
     *
     * Background task executing one asynchronous step on its own thread.
     */
    public class y implements Runnable {
        final  NsdServiceInfo b;

        public y(NsdServiceInfo nsdServiceInfo) {
            this.b = nsdServiceInfo;
        }

        @Override 
        public void run() {
            CarPlayCtrlAdvertiser.w();
            try {
                CarPlayCtrlAdvertiser.u(CarPlayCtrlAdvertiser.this, this.b);
            } catch (Throwable th) {
            }
        }
    }

    /**
     * mDNS 解析回调：拿到服务地址后继续建链。
     *
     * mDNS resolve callback continuing link setup once the address is known.
     */
    public class z implements NsdManager.ResolveListener {
        public z() {
        }

        @Override 
        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
            CarPlayCtrlAdvertiser.this.r("CTRL_RESOLVE_FAILED code=" + i + " name=" + nsdServiceInfo.getServiceName());
        }

        @Override 
        public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
            CarPlayCtrlAdvertiser.this.r("CTRL_RESOLVE_OK name=" + nsdServiceInfo.getServiceName() + " port=" + nsdServiceInfo.getPort() + " type=" + nsdServiceInfo.getServiceType() + " host=" + nsdServiceInfo.getHost());
        }
    }

    public CarPlayCtrlAdvertiser(Context context, Network network, LinkProperties linkProperties, String str, b bVar, c cVar) throws IOException {
        ControlChannel.APP_CTX = context;
        ControlChannel.INVITED = false;
        this.i = new AtomicBoolean();
        this.j = new Object();
        this.k = new ArrayList();
        this.l = new HashSet();
        this.q = new a();
        NsdManager nsdManager = (NsdManager) context.getSystemService(NsdManager.class);
        Objects.requireNonNull(nsdManager);
        this.a = nsdManager;
        Objects.requireNonNull(network);
        this.b = network;
        Objects.requireNonNull(linkProperties);
        this.c = List.copyOf(linkProperties.getLinkAddresses());
        this.d = linkProperties.getInterfaceName();
        InetAddress inetAddressM = m(linkProperties);
        this.e = inetAddressM;
        String strA = io.carplay.app.secured.n.a(str);
        this.f = strA;
        Objects.requireNonNull(bVar);
        this.g = bVar;
        Objects.requireNonNull(cVar);
        this.h = cVar;
        if (inetAddressM == null || strA == null) {
            SyntheticRefs.c("CTRL_SERVICE_VALID_LOCAL_IDENTITY_REQUIRED");
            throw null;
        }
    }

    
    
    // [OBF-MAP] ctrlAcceptLoop | 职责: ctrl ServerSocket 的 accept 循环，串行读请求写响应。
    // 判读: V1.8.19 实测 IPv4/IPv6 均能触发 V160_CTRL_TCP_ACCEPT；setSoTimeout 由 1500ms 放宽到 5000ms。
    public void l(ServerSocket serverSocket) {
        int i;
        while (!this.i.get()) {
            try {
                Socket socketAccept = serverSocket.accept();
                ControlChannel.MarkTcpAccept();
                // 记录对端地址：此前只计数，无法区分「车机从未连接」与「连了但不是车机」。
                ControlChannel.Log("V160_CTRL_PEER peer=" + socketAccept.getInetAddress()
                        + " remote_port=" + socketAccept.getPort()
                        + " local_port=" + socketAccept.getLocalPort());
                synchronized (this.j) {
                    try {
                        if (this.i.get()) {
                            p(socketAccept);
                            return;
                        }
                        this.l.add(socketAccept);
                        int i2 = this.o + 1;
                        this.o = i2;
                        if (i2 > 64) {
                            r("CTRL_SERVICE_REQUEST_LIMIT original_path_retained=true");
                            close();
                            synchronized (this.j) {
                                this.l.remove(socketAccept);
                            }
                            p(socketAccept);
                            return;
                        }
                        try {
                            try {
                                socketAccept.setSoTimeout(5000);
                                if (o(socketAccept.getInetAddress())) {
                                    try {
                                        ControlRequestParser.a aVarD = ControlRequestParser.d(socketAccept.getInputStream());
                                        synchronized (this.j) {
                                            if (this.i.get()) {
                                                synchronized (this.j) {
                                                    this.l.remove(socketAccept);
                                                }
                                                p(socketAccept);
                                                return;
                                            }
                                            socketAccept.getOutputStream().write(ControlRequestParser.e(aVarD.i()));
                                            socketAccept.getOutputStream().flush();
                                            if (aVarD.g() && (i = this.p) < 8) {
                                                int i3 = i + 1;
                                                this.p = i3;
                                                String receiverIdentifier = aVarD.j();
                                                ControlChannel.MarkCtrlInvitation(receiverIdentifier);
                                                r("CTRL_INVITATION_ACCEPTED count=" + i3
                                                        + " receiver_identifier_present=" + aVarD.h()
                                                        + " receiver_bound=" + (receiverIdentifier != null)
                                                        + " identity_chain_closed=" + ControlChannel.CTRL_IDENTITY_BOUND
                                                        + " trusted=false");
                                                this.g.b();
                                            }
                                        }
                                        synchronized (this.j) {
                                            this.l.remove(socketAccept);
                                        }
                                    } catch (IOException unused) {
                                        if (!this.i.get()) {
                                            socketAccept.getOutputStream().write(ControlRequestParser.e(400));
                                            socketAccept.getOutputStream().flush();
                                        }
                                        synchronized (this.j) {
                                            this.l.remove(socketAccept);
                                        }
                                    }
                                } else {
                                    synchronized (this.j) {
                                        this.l.remove(socketAccept);
                                    }
                                }
                            } catch (IOException e) {
                                if (!this.i.get() && this.o <= 8) {
                                    r("CTRL_REQUEST_ENDED type=" + e.getClass().getSimpleName());
                                }
                                synchronized (this.j) {
                                    this.l.remove(socketAccept);
                                }
                            }
                            p(socketAccept);
                        } catch (Throwable th) {
                            synchronized (this.j) {
                                this.l.remove(socketAccept);
                                p(socketAccept);
                                throw th;
                            }
                        }
                    } catch (Throwable th2) {
                        throw th2;
                    }
                }
            } catch (SocketTimeoutException unused2) {
            } catch (IOException e2) {
                if (!this.i.get()) {
                    r("CTRL_LISTENER_ENDED type=".concat(e2.getClass().getSimpleName()));
                }
                close();
                return;
            }
        }
    }

    
    public static  ServerSocket k(LinkProperties linkProperties, InetAddress inetAddress, int i) throws IOException {
        if (inetAddress instanceof Inet6Address) {
            Inet6Address inet6Address = (Inet6Address) inetAddress;
            if (inetAddress.isLinkLocalAddress()) {
                NetworkInterface byName = NetworkInterface.getByName(linkProperties.getInterfaceName());
                if (byName == null) {
                    SyntheticRefs.c("CTRL_INTERFACE_SCOPE_UNAVAILABLE");
                    return null;
                }
                inetAddress = Inet6Address.getByAddress((String) null, inet6Address.getAddress(), byName.getIndex());
            }
        }
        ServerSocket serverSocket = new ServerSocket();
        try {
            serverSocket.bind(new InetSocketAddress(inetAddress, i));
            serverSocket.setSoTimeout(1000);
            return serverSocket;
        } catch (IOException e) {
            try {
                serverSocket.close();
            } catch (IOException unused) {
            }
            throw e;
        }
    }

    public static InetAddress m(LinkProperties linkProperties) {
        InetAddress inetAddress = null;
        if (linkProperties != null && linkProperties.getInterfaceName() != null && !linkProperties.getInterfaceName().isEmpty()) {
            Iterator<LinkAddress> it = linkProperties.getLinkAddresses().iterator();
            while (it.hasNext()) {
                InetAddress address = it.next().getAddress();
                if (t(address) && (inetAddress == null || ((address instanceof Inet4Address) && !(inetAddress instanceof Inet4Address)))) {
                    inetAddress = address;
                }
            }
        }
        return inetAddress;
    }

    private boolean o(InetAddress inetAddress) {
        if (!t(inetAddress)) {
            return false;
        }
        Iterator<LinkAddress> it = this.c.iterator();
        while (it.hasNext()) {
            if (inetAddress.equals(it.next().getAddress())) {
                return false;
            }
        }
        for (LinkAddress linkAddress : this.c) {
            byte[] address = linkAddress.getAddress().getAddress();
            byte[] address2 = inetAddress.getAddress();
            int prefixLength = linkAddress.getPrefixLength();
            if (address.length == address2.length && prefixLength >= 1 && prefixLength <= address.length * 8) {
                for (int i = 0; i < prefixLength; i++) {
                    int i2 = i / 8;
                    if (((address2[i2] ^ address[i2]) & (1 << (7 - (i % 8)))) == 0) {
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static void p(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
            }
        }
    }

    
    public void r(String str) {
        if (this.i.get()) {
            return;
        }
        this.g.a(str);
    }

    
    public void s() {
        try {
            this.a.unregisterService(this.q);
        } catch (RuntimeException unused) {
        }
    }

    private static boolean t(InetAddress inetAddress) {
        return (inetAddress == null || inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isMulticastAddress()) ? false : true;
    }

    public static void u(CarPlayCtrlAdvertiser carPlayCtrlAdvertiser, NsdServiceInfo nsdServiceInfo) {
        NsdManager nsdManager = carPlayCtrlAdvertiser.a;
        z zVar = carPlayCtrlAdvertiser.new z();
        try {
            NsdServiceInfo nsdServiceInfo2 = new NsdServiceInfo();
            nsdServiceInfo2.setServiceName(nsdServiceInfo.getServiceName());
            nsdServiceInfo2.setServiceType(r);
            nsdServiceInfo2.setPort(nsdServiceInfo.getPort());
            nsdManager.resolveService(nsdServiceInfo2, zVar);
            carPlayCtrlAdvertiser.r("CTRL_RESOLVE_SUBMIT type=" + r + " name=" + nsdServiceInfo.getServiceName() + " port=" + nsdServiceInfo.getPort());
        } catch (Throwable th) {
            carPlayCtrlAdvertiser.r("CTRL_RESOLVE_THROW msg=" + th.getMessage());
        }
    }

    public static void v(CarPlayCtrlAdvertiser carPlayCtrlAdvertiser, NsdServiceInfo nsdServiceInfo) {
        try {
            new Thread(carPlayCtrlAdvertiser.new y(nsdServiceInfo)).start();
        } catch (Throwable th) {
        }
    }

    public static void w() {
        try {
            Thread.sleep(1500L);
        } catch (InterruptedException e) {
        }
    }

    @Override 
    public void close() {
        if (this.i.compareAndSet(false, true)) {
            synchronized (this.j) {
                try {
                    if (this.n) {
                        s();
                        // [V1.8.24] 等 mDNS 注销回调（或 400ms 超时）再放行，
                        // 否则 Q1() 紧接着的同名注册会被 NsdManager 自动改名成 "(2)"。
                        // 这里持有 j 锁，q() 也在 j 上同步，正好把注销→重注册串行化。
                        try {
                            ((a) this.q).unregistered.await(400L, java.util.concurrent.TimeUnit.MILLISECONDS);
                        } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    Iterator<ServerSocket> it = this.k.iterator();
                    while (it.hasNext()) {
                        p(it.next());
                    }
                    this.k.clear();
                    Iterator<Socket> it2 = this.l.iterator();
                    while (it2.hasNext()) {
                        p(it2.next());
                    }
                    this.l.clear();
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public boolean n(Network network, LinkProperties linkProperties) {
        return !this.i.get() && this.b.equals(network) && this.e.equals(m(linkProperties)) && this.d.equals(linkProperties.getInterfaceName()) && new HashSet(this.c).equals(new HashSet(linkProperties.getLinkAddresses()));
    }

    /** 启动即广播的实例：不依赖连接流程，App 一打开车机就能发现本机。 */
    private static CarPlayCtrlAdvertiser earlyAdvertiser;

    /**
     * App 启动即广播 `_carplay-ctrl`。
     *
     * 原实现只在用户点「连接车机」后由 `PairingSession.Q1()` 注册，
     * 车机若从自己那一侧先发起（车机点 CarPlay），此时手机上还没有这条记录，只能发现空结果。
     * 提前注册后，车机的 mDNS 查询在任何时刻都能命中。
     */
    public static synchronized void StartEarly(Context context) {
        if (earlyAdvertiser != null) {
            return;
        }
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return;
            }
            android.net.Network wifi = null;
            LinkProperties lp = null;
            for (android.net.Network candidate : cm.getAllNetworks()) {
                android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(candidate);
                if (caps == null || !caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)) {
                    continue;
                }
                LinkProperties candidateLp = cm.getLinkProperties(candidate);
                if (candidateLp != null && m(candidateLp) != null) {
                    wifi = candidate;
                    lp = candidateLp;
                    break;
                }
            }
            if (wifi == null || lp == null) {
                ControlChannel.Log("CTRL_EARLY_SKIPPED reason=NO_WIFI_NETWORK");
                return;
            }
            // 第 4 个参数是本机身份 MAC（构造里由 n.a() 校验 MAC 格式），不是服务名；
            // 服务名另由 setServiceName(ControlChannel.ServiceName()) 设置。
            String identity = ControlChannel.DeviceMac();
            if (identity == null) {
                ControlChannel.Log("CTRL_EARLY_SKIPPED reason=NO_LOCAL_IDENTITY");
                return;
            }
            earlyAdvertiser = new CarPlayCtrlAdvertiser(context, wifi, lp, identity, new b() {
                @Override
                public void a(String str) {
                    ControlChannel.Log("CTRL_EARLY " + str);
                }

                @Override
                public void b() {
                    ControlChannel.Log("CTRL_EARLY_PEER_CONNECTED");
                }
            });
            earlyAdvertiser.q();
            ControlChannel.Log("CTRL_EARLY_STARTED identity=" + identity + " iface=" + lp.getInterfaceName()
                    + " service_name=" + ControlChannel.ServiceName());
        } catch (Throwable failure) {
            ControlChannel.Log("CTRL_EARLY_FAILED msg=" + failure.getMessage());
        }
    }

    private static volatile boolean probeStarted = false;

    /**
     * 诊断：浏览局域网内与 CarPlay 相关的服务类型，确认车机侧究竟广播了什么。
     * 车机若不回连本机 ctrl，必须先分清「车机没广播」与「车机广播了但走的是另一条通道」，
     * 否则后续任何字段调整都没有判据。
     */
    public static void Probe(Context context) {
        if (probeStarted) {
            return;
        }
        NsdManager nsd = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        if (nsd == null) {
            ControlChannel.Log("CTRL_PROBE_SKIPPED reason=NO_NSD");
            return;
        }
        probeStarted = true;
        String[] types = {"_carplay._tcp", "_carplay-ctrl._tcp", "_airplay._tcp"};
        for (String type : types) {
            final String fType = type;
            NsdManager.DiscoveryListener listener = new NsdManager.DiscoveryListener() {
                @Override
                public void onStartDiscoveryFailed(String s, int i) {
                    ControlChannel.Log("CTRL_PROBE_START_FAILED type=" + fType + " err=" + i);
                }

                @Override
                public void onStopDiscoveryFailed(String s, int i) {
                }

                @Override
                public void onDiscoveryStarted(String s) {
                    ControlChannel.Log("CTRL_PROBE_STARTED type=" + fType);
                }

                @Override
                public void onDiscoveryStopped(String s) {
                }

                @Override
                public void onServiceFound(NsdServiceInfo info) {
                    ControlChannel.Log("CTRL_PROBE_FOUND type=" + fType + " name=" + info.getServiceName());
                    try {
                        nsd.resolveService(info, new NsdManager.ResolveListener() {
                            @Override
                            public void onResolveFailed(NsdServiceInfo si, int err) {
                                ControlChannel.Log("CTRL_PROBE_RESOLVE_FAILED name=" + si.getServiceName() + " err=" + err);
                            }

                            @Override
                            public void onServiceResolved(NsdServiceInfo si) {
                                ControlChannel.Log("CTRL_PROBE_RESOLVED name=" + si.getServiceName()
                                        + " type=" + si.getServiceType()
                                        + " host=" + si.getHost()
                                        + " port=" + si.getPort());
                            }
                        });
                    } catch (Throwable t) {
                        ControlChannel.Log("CTRL_PROBE_RESOLVE_THROW msg=" + t.getMessage());
                    }
                }

                @Override
                public void onServiceLost(NsdServiceInfo info) {
                }
            };
            try {
                nsd.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, listener);
            } catch (Throwable t) {
                ControlChannel.Log("CTRL_PROBE_REGISTER_THROW type=" + fType + " msg=" + t.getMessage());
            }
        }
    }

    /** 会话流程接管前关掉早期广播，避免同名服务重复注册（NsdManager 会直接注册失败）。 */
    public static synchronized void StopEarly() {
        if (earlyAdvertiser != null) {
            try {
                earlyAdvertiser.close();
            } catch (Throwable ignored) {
                // close 失败不影响后续会话广播。
            }
            earlyAdvertiser = null;
            ControlChannel.Log("CTRL_EARLY_STOPPED");
        }
    }

    public void q() {
        synchronized (this.j) {
            try {
                if (!this.m && !this.i.get()) {
                    this.m = true;
                    ControlChannel.ResetInvite();
                    try {
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(this.e);
                        for (LinkAddress linkAddress : this.c) {
                            if (t(linkAddress.getAddress()) && !arrayList.contains(linkAddress.getAddress())) {
                                arrayList.add(linkAddress.getAddress());
                            }
                        }
                        if (arrayList.size() > 8) {
                            throw new IOException("CTRL_LOCAL_ADDRESS_LIMIT");
                        }
                        int size = arrayList.size();
                        int i = 0;
                        int localPort = 0;
                        int i2 = 0;
                        while (i2 < size) {
                            Object obj = arrayList.get(i2);
                            i2++;
                            ServerSocket serverSocketA = this.h.a((InetAddress) obj, localPort);
                            this.k.add(serverSocketA);
                            if (localPort == 0) {
                                localPort = serverSocketA.getLocalPort();
                            }
                        }
                        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();
                        nsdServiceInfo.setServiceName(ControlChannel.ServiceName());
                        nsdServiceInfo.setServiceType("_carplay-ctrl._tcp");
                        nsdServiceInfo.setPort(localPort);
                        r("CTRL_ANNOUNCE name=" + nsdServiceInfo.getServiceName() + " port=" + localPort + " svc=" + ControlChannel.ServiceName() + " manual=" + ControlChannel.ManualName());
                        nsdServiceInfo.setNetwork(this.b);
                        ControlChannel.FillTxt(nsdServiceInfo, this.f);
                        this.n = true;
                        this.a.registerService(nsdServiceInfo, 1, this.q);
                        if (!this.i.get()) {
                            for (ServerSocket serverSocket : this.k) {
                                ReleaseCheckTask gVar = new ReleaseCheckTask(i);
                                gVar.b = this;
                                gVar.c = serverSocket;
                                VarHandle.storeStoreFence();
                                Thread thread = new Thread(gVar, "CarPlayAPK-ctrl-invitation");
                                thread.setDaemon(true);
                                thread.start();
                            }
                        }
                    } catch (IOException | RuntimeException e) {
                        r("CTRL_SERVICE_UNAVAILABLE type=" + e.getClass().getSimpleName() + " original_path_retained=true");
                        close();
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public CarPlayCtrlAdvertiser(Context context, Network network, LinkProperties linkProperties, String str, b bVar) throws IOException {
        this(context, network, linkProperties, str, bVar, a0(linkProperties));
    }

    private static i a0(LinkProperties linkProperties) {
        i iVar = new i();
        iVar.a = linkProperties;
        VarHandle.storeStoreFence();
        return iVar;
    }
}
