/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   [V1.8.38/39] 车机发现增强：监听 mDNS query 判断车机是否在 browse _carplay-ctrl._tcp，
 *   并可选向车机网关单播 mDNS 通告，绕过车机热点对客户端间 multicast 的隔离。
 *   注册后常驻 ARM 线程，每 3s 读一次开关（ctrl_mdns_enhance），开关变化即时开/关，
 *   不需要重启进程或重新注册 ctrl 服务。不触碰 AirPlay / RECORD / HID / BT iAP2。
 */

package io.carplay.app.prototype;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.RouteInfo;
import android.net.wifi.WifiManager;

import io.carplay.app.secured.ControlChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 车机侧 Bonjour browser 诊断与发现增强。
 *
 * 背景：固件 libNmeCarPlay.so 的 SendConnect@0x17e10 只有在
 * IosControlClientCallback(AddOrUpdateController) 把 (device_id, service_name) 写进
 * [obj+0x9e0] 表之后，才会发 GET /ctrl-int/1/connect。真机三轮抓包
 * V160_CTRL_TCP_ACCEPT 恒为 0，说明车机从未把手机服务登记进去。
 * 本类用于区分两种原因：
 *   (a) 车机根本没 browse _carplay-ctrl（状态机未进入） -> 无 CTRL_MDNS_QUERY；
 *   (b) 车机在 browse，但收不到手机的 mDNS 应答（热点隔离）-> 有 query 但无 connect。
 * 对 (b) 用单播通告绕过 multicast 隔离。
 */
final class CarPlayCtrlMdnsHelper {
    private static final String MCAST_GROUP = "224.0.0.251";
    private static final int MDNS_PORT = 5353;
    private static final String PTR_OWNER = "_carplay-ctrl._tcp.local";
    private static CarPlayCtrlMdnsHelper sInstance;

    private Context ctx;
    private Network network;
    private InetAddress local4;
    private InetAddress gateway;
    private int port;
    private String serviceName;
    private String iface;
    private Map<String, byte[]> txt;
    private byte[] announcePacket;
    private byte[] probePacket;

    private final AtomicBoolean alive = new AtomicBoolean(true);
    private volatile boolean socketOpen;
    private MulticastSocket socket;
    private WifiManager.MulticastLock lock;
    private Thread arm;
    private Thread rx;
    private Thread tx;
    private int sent;
    private int received;
    private int carplayCtrlQueries;

    /**
     * ctrl 服务注册成功后调用：只保存参数并起 ARM 线程，是否真正开探针由开关决定。
     * 这样 UI 开关 / adb 广播都能即时生效，不需要重启进程。
     */
    static synchronized void start(Network network, InetAddress local, int port,
                                   String serviceName, Map<String, byte[]> txt, String iface) {
        stop();
        Context ctx = ControlChannel.APP_CTX;
        if (ctx == null || local == null || serviceName == null || serviceName.length() == 0) {
            ControlChannel.Log("CTRL_MDNS_SKIPPED reason=NO_CONTEXT_OR_IDENTITY");
            return;
        }
        CarPlayCtrlMdnsHelper h = new CarPlayCtrlMdnsHelper();
        h.ctx = ctx;
        h.network = network;
        h.local4 = local;
        h.port = port;
        h.serviceName = serviceName;
        h.txt = txt;
        h.iface = iface;
        h.gateway = resolveGateway(ctx, network, local);
        try {
            h.announcePacket = buildAnnouncement(serviceName);
            h.probePacket = buildPtrQuery("_airplay._tcp.local");
        } catch (Throwable t) {
            ControlChannel.Log("CTRL_MDNS_PACKET_FAILED type=" + t.getClass().getSimpleName());
        }
        sInstance = h;
        ControlChannel.Log("CTRL_MDNS_ARMED port=" + port + " name=" + serviceName
                + " iface=" + iface + " local=" + local.getHostAddress()
                + " gateway=" + (h.gateway == null ? "none" : h.gateway.getHostAddress())
                + " txt_keys=" + (txt == null ? 0 : txt.size())
                + " switch=" + ControlChannel.CtrlMdnsEnhanceEnabled());
        h.arm = new Thread(h.new ArmLoop(), "carplayapk-mdns-arm");
        h.arm.setDaemon(true);
        h.arm.start();
    }

    static synchronized void stop() {
        CarPlayCtrlMdnsHelper h = sInstance;
        sInstance = null;
        if (h != null) {
            ControlChannel.Log("CTRL_MDNS_STOP announcement_sent=" + h.sent
                    + " peer_packets=" + h.received
                    + " carplay_ctrl_queries=" + h.carplayCtrlQueries);
            h.cleanup();
        }
    }

    /** 开关热生效：每 3s 比对一次，开则建 socket，关则释放。 */
    private final class ArmLoop implements Runnable {
        @Override
        public void run() {
            boolean active = false;
            while (alive.get()) {
                try {
                    boolean want = ControlChannel.CtrlMdnsEnhanceEnabled();
                    if (want && !active) {
                        active = openSocket();
                        if (!active) {
                            Thread.sleep(10000L);
                            continue;
                        }
                    } else if (!want && active) {
                        closeSocket();
                        active = false;
                        ControlChannel.Log("CTRL_MDNS_OFF_BY_SWITCH");
                    }
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    break;
                } catch (Throwable t) {
                    ControlChannel.Log("CTRL_MDNS_ARM_FAILED type=" + t.getClass().getSimpleName());
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            }
            closeSocket();
        }
    }

    private boolean openSocket() {
        try {
            WifiManager wm = (WifiManager) ctx.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                lock = wm.createMulticastLock("carplayapk-ctrl-mdns");
                lock.setReferenceCounted(false);
                lock.acquire();
            }
            MulticastSocket s = new MulticastSocket(null);
            s.setReuseAddress(true);
            s.bind(new InetSocketAddress(MDNS_PORT));
            NetworkInterface ni = iface == null ? null : NetworkInterface.getByName(iface);
            if (ni != null) {
                s.setNetworkInterface(ni);
            }
            socket = s;
            InetAddress group = InetAddress.getByName(MCAST_GROUP);
            if (ni != null) {
                s.joinGroup(new InetSocketAddress(group, MDNS_PORT), ni);
            } else {
                s.joinGroup(group);
            }
            socketOpen = true;
            rx = new Thread(new RxLoop(), "carplayapk-mdns-rx");
            rx.setDaemon(true);
            rx.start();
            tx = new Thread(new TxLoop(), "carplayapk-mdns-tx");
            tx.setDaemon(true);
            tx.start();
            ControlChannel.Log("CTRL_MDNS_STARTED iface=" + iface
                    + " local=" + local4.getHostAddress()
                    + " gateway=" + (gateway == null ? "none" : gateway.getHostAddress())
                    + " port=" + port + " name=" + serviceName
                    + " txt_keys=" + (txt == null ? 0 : txt.size()));
            return true;
        } catch (Throwable t) {
            ControlChannel.Log("CTRL_MDNS_START_FAILED type=" + t.getClass().getSimpleName()
                    + " msg=" + String.valueOf(t.getMessage()));
            closeSocket();
            return false;
        }
    }

    private void closeSocket() {
        socketOpen = false;
        MulticastSocket s = socket;
        socket = null;
        if (s != null) {
            try {
                s.close();
            } catch (Throwable ignored) {
            }
        }
        WifiManager.MulticastLock l = lock;
        lock = null;
        if (l != null) {
            try {
                if (l.isHeld()) {
                    l.release();
                }
            } catch (Throwable ignored) {
            }
        }
        Thread r = rx;
        rx = null;
        if (r != null) {
            r.interrupt();
        }
        Thread t = tx;
        tx = null;
        if (t != null) {
            t.interrupt();
        }
    }

    private void cleanup() {
        alive.set(false);
        Thread a = arm;
        arm = null;
        if (a != null) {
            a.interrupt();
        }
        closeSocket();
    }

    /** 只记录对端发来的 query：车机是否在 browse _carplay-ctrl 是第一判据。 */
    private final class RxLoop implements Runnable {
        @Override
        public void run() {
            byte[] buf = new byte[2048];
            while (alive.get() && socketOpen) {
                try {
                    MulticastSocket s = socket;
                    if (s == null || s.isClosed()) {
                        break;
                    }
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    s.receive(p);
                    InetAddress src = p.getAddress();
                    // [V1.8.40] 过滤本机所有地址（含 IPv6 隐私地址）：logs/v1.8.39 证明
                    // fe80::1f98...%wlan0 其实是本机 NsdManager 的 resolve，被误记成"车机在 resolve"。
                    if (src == null || (local4 != null && src.equals(local4)) || isLocalAddress(src)) {
                        continue;
                    }
                    received++;
                    byte[] data = p.getData();
                    int len = p.getLength();
                    String q = parseQuestions(data, len);
                    boolean carplay = q.contains("_carplay-ctrl");
                    if (carplay) {
                        carplayCtrlQueries++;
                        // 对端（车机）真正在 browse/resolve 本机 ctrl 服务的关键信号。
                        ControlChannel.Log("CTRL_MDNS_PEER_BROWSE from=" + src.getHostAddress()
                                + " qd=" + u16(data, 4) + " an=" + u16(data, 6)
                                + " q=" + q.trim());
                    }
                    if (q.length() > 0 || carplay) {
                        ControlChannel.Log("CTRL_MDNS_QUERY from=" + src.getHostAddress()
                                + " qd=" + u16(data, 4) + " an=" + u16(data, 6)
                                + " carplay_ctrl=" + carplay + " q=" + q.trim());
                    }
                } catch (Throwable t) {
                    if (alive.get() && socketOpen) {
                        ControlChannel.Log("CTRL_MDNS_RX_ENDED type="
                                + t.getClass().getSimpleName());
                    }
                    break;
                }
            }
        }
    }

    /** 每 5s 发一次完整 mDNS 应答：multicast + 车机网关单播，另周期性发 PTR query 探连通性。 */
    private final class TxLoop implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(1200L);
            } catch (InterruptedException e) {
                return;
            }
            InetAddress group;
            try {
                group = InetAddress.getByName(MCAST_GROUP);
            } catch (Throwable t) {
                return;
            }
            int n = 0;
            while (alive.get() && socketOpen) {
                try {
                    MulticastSocket s = socket;
                    if (s == null || s.isClosed()) {
                        break;
                    }
                    if (announcePacket != null) {
                        s.send(new DatagramPacket(announcePacket, announcePacket.length,
                                group, MDNS_PORT));
                        if (gateway != null) {
                            s.send(new DatagramPacket(announcePacket, announcePacket.length,
                                    gateway, MDNS_PORT));
                        }
                        sent++;
                        if (n <= 5 || n % 12 == 0) {
                            ControlChannel.Log("CTRL_MDNS_ANNOUNCE n=" + sent
                                    + " multicast=1 unicast=" + (gateway == null ? 0 : 1)
                                    + " to=" + (gateway == null ? "none" : gateway.getHostAddress()));
                        }
                    }
                    if (probePacket != null && n % 3 == 0) {
                        s.send(new DatagramPacket(probePacket, probePacket.length,
                                group, MDNS_PORT));
                        ControlChannel.Log("CTRL_MDNS_PROBE_QUERY n=" + (n + 1)
                                + " qname=_airplay._tcp.local");
                    }
                    n++;
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    break;
                } catch (Throwable t) {
                    if (alive.get() && socketOpen) {
                        ControlChannel.Log("CTRL_MDNS_TX_FAILED type="
                                + t.getClass().getSimpleName());
                    }
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            }
        }
    }

    /** [V1.8.40] 本机任意地址（IPv4/IPv6 隐私地址都算），用于把自发报文过滤掉。 */
    private static boolean isLocalAddress(InetAddress a) {
        if (a == null) {
            return true;
        }
        if (a.isLoopbackAddress() || a.isAnyLocalAddress() || a.isMulticastAddress()) {
            return true;
        }
        try {
            return NetworkInterface.getByInetAddress(a) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static InetAddress resolveGateway(Context ctx, Network net, InetAddress local) {
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null && net != null) {
                LinkProperties lp = cm.getLinkProperties(net);
                if (lp != null) {
                    for (RouteInfo r : lp.getRoutes()) {
                        if (r.hasGateway() && r.getGateway() instanceof Inet4Address) {
                            return r.getGateway();
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        if (local instanceof Inet4Address) {
            byte[] b = local.getAddress().clone();
            b[3] = (byte) 1;
            try {
                return InetAddress.getByAddress(b);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    /**
     * [V1.8.40] 只发 PTR 一条 Answer。
     * logs/v1.8.39 证明自造 SRV target "OPPO_X8U.local" 与 A 记录是错的：
     * 系统 mdnsd 并不认识这个主机名，车机若采信就解析不到 IP。
     * 只发 PTR 让车机走标准 resolve，SRV/TXT/A 全部由系统 mdnsd 应答，
     * 端口与主机名才是真实的。
     */
    private static byte[] buildAnnouncement(String svc) throws IOException {
        String inst = svc + "._carplay-ctrl._tcp.local";
        ByteArrayOutputStream o = new ByteArrayOutputStream(128);
        u16(o, 0);
        u16(o, 0x8400);
        u16(o, 0);
        u16(o, 1);
        u16(o, 0);
        u16(o, 0);
        // PTR _carplay-ctrl._tcp.local -> <instance>
        o.write(encodeName(PTR_OWNER));
        u16(o, 12);
        u16(o, 1);
        u32(o, 120);
        byte[] r = encodeName(inst);
        u16(o, r.length);
        o.write(r);
        return o.toByteArray();
    }

    private static byte[] buildPtrQuery(String qname) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        u16(o, 0x5a38);
        u16(o, 0);
        u16(o, 1);
        u16(o, 0);
        u16(o, 0);
        u16(o, 0);
        o.write(encodeName(qname));
        u16(o, 12);
        u16(o, 1);
        return o.toByteArray();
    }

    private static byte[] encodeName(String s) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (String part : s.split("\\.")) {
            if (part.length() == 0) {
                continue;
            }
            byte[] b = part.getBytes(StandardCharsets.UTF_8);
            int n = Math.min(b.length, 63);
            o.write(n);
            o.write(b, 0, n);
        }
        o.write(0);
        return o.toByteArray();
    }

    private static String parseQuestions(byte[] b, int len) {
        StringBuilder sb = new StringBuilder();
        if (b == null || len < 12) {
            return "";
        }
        int qd = u16(b, 4);
        int off = 12;
        for (int i = 0; i < qd && off < len; i++) {
            StringBuilder nm = new StringBuilder();
            int p = off;
            int guard = 0;
            while (p < len && guard++ < 16) {
                int n = b[p] & 0xff;
                if (n == 0) {
                    p++;
                    break;
                }
                if ((n & 0xc0) == 0xc0) {
                    p += 2;
                    break;
                }
                p++;
                if (p + n > len) {
                    break;
                }
                if (nm.length() > 0) {
                    nm.append('.');
                }
                nm.append(new String(b, p, n, StandardCharsets.US_ASCII));
                p += n;
            }
            off = p;
            if (off + 4 > len) {
                break;
            }
            int type = u16(b, off);
            off += 4;
            sb.append(nm).append('/').append(type).append(' ');
        }
        return sb.toString();
    }

    private static int u16(byte[] b, int off) {
        if (b == null || off + 1 >= b.length) {
            return 0;
        }
        return ((b[off] & 0xff) << 8) | (b[off + 1] & 0xff);
    }

    private static void u16(ByteArrayOutputStream o, int v) {
        o.write((v >>> 8) & 0xff);
        o.write(v & 0xff);
    }

    private static void u32(ByteArrayOutputStream o, long v) {
        o.write((int) (v >>> 24) & 0xff);
        o.write((int) (v >>> 16) & 0xff);
        o.write((int) (v >>> 8) & 0xff);
        o.write((int) v & 0xff);
    }
}
