package io.carplay.app.secured;

import android.content.Context;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [V1.8.58][I] iAP2-over-WiFi 端点监听。
 *
 * <p>固件证据（`libNmeTransport.so`）：车机侧是 <b>客户端</b> ——
 * `CreateNmeIAPWifiTransport` 先从 metapool 取名为 `wifi` 的 tag，拿不到就
 * `no wifi tag` 直接失败；随后由 `NmeWifiTransportClient::wait_for_connect()`
 * 主动连向手机。因此手机必须<b>先监听</b>，车机才可能连上来。
 *
 * <p>本类只做三件事，全部被动：
 * <ol>
 *   <li>在可配置端口上监听，记录每一次 accept（谁连的、连了多久）；</li>
 *   <li>把收到的字节原样打进日志（前若干包），用于反推车机的链路帧格式；</li>
 *   <li>如配置了固定回包（`cp_i_syn_reply_hex`），对每个连接回一次同样的字节。</li>
 * </ol>
 *
 * <p>不做任何主动发送 —— 端点"上报"格式尚未取证，盲发曾导致 `PEER_RESTARTED`。
 *
 * <p>开关：`carplayapk-debug` 内 `cp_i_wifi_iap_listen`（默认 true）/ `cp_i_wifi_iap_port`
 * （默认 52800）/ `cp_i_syn_reply_hex`（默认空 = 不回）。
 */
public final class WifiIapListener {

    private static final int DEFAULT_PORT = 52800;
    private static final int MAX_PACKETS_LOGGED = 24;
    private static final AtomicInteger SEQ = new AtomicInteger();

    private static volatile WifiIapListener instance;

    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running;

    private WifiIapListener() {
    }

    public static WifiIapListener get() {
        if (instance == null) {
            synchronized (WifiIapListener.class) {
                if (instance == null) {
                    instance = new WifiIapListener();
                }
            }
        }
        return instance;
    }

    /** 幂等启动；开关关闭时只记一条日志。 */
    public synchronized void start(Context context) {
        if (running) {
            ControlChannel.Log("CP_I_LISTEN_ALREADY_RUNNING port=" + port());
            return;
        }
        if (!ControlChannel.CpWifiIapListenEnabled()) {
            ControlChannel.Log("CP_I_LISTEN_DISABLED key=cp_i_wifi_iap_listen");
            return;
        }
        final int listenPort = port();
        try {
            ServerSocket socket = new ServerSocket();
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(listenPort));
            this.serverSocket = socket;
            this.running = true;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    acceptLoop();
                }
            }, "cp-wifi-iap-listen-" + listenPort);
            thread.setDaemon(true);
            this.acceptThread = thread;
            thread.start();
            ControlChannel.Log("CP_I_LISTEN_START port=" + listenPort
                    + " reply_hex=" + (replyHex().isEmpty() ? "none" : replyHex()));
        } catch (Throwable failure) {
            ControlChannel.Log("CP_I_LISTEN_FAIL port=" + listenPort + " msg=" + failure);
        }
    }

    public synchronized void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Throwable ignored) {
        }
        serverSocket = null;
        acceptThread = null;
        ControlChannel.Log("CP_I_LISTEN_STOP");
    }

    private void acceptLoop() {
        while (running) {
            Socket client = null;
            try {
                client = serverSocket.accept();
                final Socket accepted = client;
                final String peer = describe(accepted);
                ControlChannel.Log("CP_I_ACCEPT peer=" + peer + " seq=" + SEQ.incrementAndGet());
                Thread reader = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handleConnection(accepted, peer);
                    }
                }, "cp-wifi-iap-conn");
                reader.setDaemon(true);
                reader.start();
            } catch (Throwable failure) {
                if (running) {
                    ControlChannel.Log("CP_I_ACCEPT_FAIL msg=" + failure);
                }
                closeQuietly(client);
                if (running) {
                    sleepQuietly(500L);
                }
            }
        }
    }

    private void handleConnection(Socket socket, String peer) {
        byte[] reply = parseHex(replyHex());
        boolean replied = false;
        int packets = 0;
        long openedAt = System.currentTimeMillis();
        try {
            socket.setSoTimeout(120000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            byte[] buffer = new byte[4096];
            while (running) {
                int read = in.read(buffer);
                if (read < 0) {
                    break;
                }
                packets++;
                if (packets <= MAX_PACKETS_LOGGED) {
                    ControlChannel.Log("CP_I_RX peer=" + peer + " n=" + read
                            + " packet=" + packets + " hex=" + hex(buffer, read));
                } else if (packets == MAX_PACKETS_LOGGED + 1) {
                    ControlChannel.Log("CP_I_RX_LOG_CAPPED peer=" + peer
                            + " cap=" + MAX_PACKETS_LOGGED);
                }
                if (!replied && reply.length > 0) {
                    out.write(reply);
                    out.flush();
                    replied = true;
                    ControlChannel.Log("CP_I_REPLY_SENT peer=" + peer
                            + " bytes=" + reply.length + " hex=" + hex(reply, reply.length));
                }
            }
        } catch (Throwable failure) {
            ControlChannel.Log("CP_I_CONN_END peer=" + peer + " packets=" + packets
                    + " dur_ms=" + (System.currentTimeMillis() - openedAt) + " msg=" + failure);
        } finally {
            closeQuietly(socket);
            ControlChannel.Log("CP_I_CONN_CLOSED peer=" + peer + " packets=" + packets
                    + " replied=" + replied
                    + " dur_ms=" + (System.currentTimeMillis() - openedAt));
        }
    }

    // ---------- helpers ----------

    private static int port() {
        Context ctx = ControlChannel.APP_CTX;
        if (ctx == null) {
            return DEFAULT_PORT;
        }
        try {
            int value = ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getInt("cp_i_wifi_iap_port", DEFAULT_PORT);
            return (value > 0 && value < 65536) ? value : DEFAULT_PORT;
        } catch (Throwable ignored) {
            return DEFAULT_PORT;
        }
    }

    private static String replyHex() {
        Context ctx = ControlChannel.APP_CTX;
        if (ctx == null) {
            return "";
        }
        try {
            String value = ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getString("cp_i_syn_reply_hex", "");
            return value == null ? "" : value.trim();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String describe(Socket socket) {
        try {
            return String.valueOf(socket.getInetAddress().getHostAddress()) + ":" + socket.getPort();
        } catch (Throwable ignored) {
            return "unknown";
        }
    }

    private static String hex(byte[] data, int length) {
        if (data == null || length <= 0) {
            return "";
        }
        int limit = Math.min(length, 96);
        StringBuilder builder = new StringBuilder(limit * 2);
        for (int i = 0; i < limit; i++) {
            builder.append(String.format(Locale.ROOT, "%02X", Integer.valueOf(data[i] & 255)));
        }
        if (length > limit) {
            builder.append("..(+" ).append(length - limit).append(')');
        }
        return builder.toString();
    }

    private static byte[] parseHex(String text) {
        if (text == null || text.isEmpty()) {
            return new byte[0];
        }
        StringBuilder clean = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.digit(c, 16) >= 0) {
                clean.append(c);
            }
        }
        if (clean.length() < 2 || (clean.length() % 2) != 0) {
            return new byte[0];
        }
        byte[] out = new byte[clean.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(clean.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }

    private static void closeQuietly(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (Throwable ignored) {
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
