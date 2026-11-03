package io.carplay.app.secured;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/* JADX INFO: loaded from: classes.dex */
/**
 * EventChannel
 *
 * <p>原混淆名：{@code io.carplay.app.secured.y5}（jadx 产物 y5.java）
 *
 * <p>命名依据：RTSP 事件通道：cseq / event response sequence / RTSP connection closed；铁律 27 已确认

 * <p>事件通道 = AirPlay 的 RTSP 事件流（车机 → 手机）。触摸、方向盘按键、
 * 车机发来的 {@code POST /command} 全部走这里。它自己实现 RTSP 的
 * 请求/应答解析（cseq、Content-Length、状态行），不依赖外部库。
 *
 * <p>关键点：<b>连上了不等于会收到东西</b>。TCP 建链成功只说明端口对，
 * 车机愿不愿意推事件取决于会话协商出来的 enabledFeatures。
 * 判定「车机到底有没有发过东西」的唯一硬指标是 {@code INBOUND_REQ chan=EVENT} 计数。
 */
public final class EventChannel implements Closeable {
    public static final int L = 4194304;
    public static final int M = 32768;
    private static final int N = 16;
    private static final byte[] EMPTY = new byte[0];
    /** 保活请求在 {@code pendingCommands} 里的标签，也是响应回调里给出的事件名。 */
    private static final String FEEDBACK_TAG = "FEEDBACK";
    private volatile long A;
    private volatile long B;
    private volatile long C;
    private volatile long D;
    private volatile int E;
    private volatile int F;
    private volatile int G;
    private volatile int H;
    private volatile int I;
    private volatile int J;
    private volatile String K;
    private final InputStream a;
    private final OutputStream b;
    private final Closeable c;
    private final Socket d;
    private final int e;
    private byte[] readKey;
    private byte[] writeKey;
    private byte[] h;
    private int i;
    private int cSeqCounter;
    private long k;
    private long l;
    private long m;
    private int n;
    private volatile boolean closed;
    private c requestHandler;
    private e responseListener;
    private final Map<Integer, a> pendingEventRequests;
    private final Map<Integer, a> pendingCommands;
    private boolean asyncStarted;
    private boolean u;
    private final AtomicBoolean v;
    private final AtomicInteger unmatchedResponses = new AtomicInteger();
    private final AtomicInteger inboundHandlerFailures = new AtomicInteger();
    private volatile boolean w;
    private volatile boolean x;
    private volatile long y;
    private volatile long z;

    public static final class b {
        public final String a;
        public final Map<String, String> b;
        private final byte[] c;
        private String d = "OTHER";

        public b(String str, Map<String, String> map, byte[] bArr) {
            this.a = str;
            this.b = Collections.unmodifiableMap(map);
            this.c = bArr;
        }

        public byte[] c() {
            return (byte[]) this.c.clone();
        }

        public String d(String str) {
            return this.b.get(str.toLowerCase(Locale.ROOT));
        }

        public String e() {
            return this.a.split(" ", 3)[0];
        }

        public String f() {
            String[] strArrSplit = this.a.split(" ", 3);
            return strArrSplit.length > 1 ? strArrSplit[1] : "";
        }

        public void g() throws IOException {
            if (h() == 200) {
                return;
            }
            throw new d(this.d, EventChannel.normalizeStatus(h()), 0);
        }

        public int h() {
            String[] strArrSplit = this.a.split(" ", 3);
            try {
                if (strArrSplit.length > 1) {
                    return Integer.parseInt(strArrSplit[1]);
                }
            } catch (NumberFormatException unused) {
            }
            return -1;
        }
    }

    public interface c {
        byte[] a(b bVar) throws IOException;
    }

    @FunctionalInterface
    public interface e {
        void c(String str, int i, int i2, boolean z);
    }

    private EventChannel(InputStream inputStream, OutputStream outputStream, Closeable closeable, Socket socket, int i) {
        this.h = new byte[0];
        this.pendingEventRequests = new LinkedHashMap();
        this.pendingCommands = new LinkedHashMap();
        this.v = new AtomicBoolean();
        this.G = -1;
        this.I = -1;
        this.K = "IDLE";
        this.a = new BufferedInputStream(inputStream, M);
        this.b = outputStream;
        this.c = closeable;
        this.d = socket;
        this.e = i;
        this.n = i;
    }

    private static void A(String str) {
        if (str == null || str.indexOf(13) >= 0 || str.indexOf(10) >= 0) {
            SyntheticRefs.b("Invalid RTSP line");
        }
    }

    private static void B(String str) {
        String str2;
        if (!str.isEmpty()) {
            for (int i = 0; i < str.length(); i++) {
                char cCharAt = str.charAt(i);
                if (cCharAt <= ' ' || cCharAt >= 127 || cCharAt == ':') {
                    str2 = "Invalid RTSP token";
                }
            }
            return;
        }
        str2 = "Empty token";
        SyntheticRefs.b(str2);
    }

    private static int C(int i) {
        if (i >= 1 && i <= 120000) {
            return i;
        }
        SyntheticRefs.b("Timeout must be 1..120000 ms");
        return 0;
    }

    /**
     * 处理事件通道上的 RTSP 响应（读线程唯一入口）。
     *
     * <p>补丁（2026-11-03，对应 smali 版 {@code y5.c()} 的 {@code :cond_4}）：
     * <b>对不上号的 CSeq 只记账丢弃，绝不抛 IOException。</b>
     * 原实现走 {@code SyntheticRefs.c(...)} 抛异常，读线程 {@code runReadLoop} 捕获后
     * 会 {@code close()} 整条事件通道——只要有一条保活 /feedback 的应答没登记，
     * 事件连接就被动断开，真机表现为每 10.4s 断一次、约 1 分钟耗尽重试后投屏中断。
     *
     * <p>同时补上「{@code /command} 队列的应答也在这里配对」：保活 /feedback 与
     * 排队的 {@code /command} 都登记在 {@code pendingCommands}，而原实现只查
     * {@code pendingEventRequests}，等于自断了配对路径。
     */
    private void handleEventResponse(b response) throws IOException {
        String cseqText = response.d("cseq");
        if (cseqText == null || cseqText.isEmpty() || cseqText.length() > 10) {
            SyntheticRefs.c("Invalid RTSP event response sequence");
            return;
        }
        for (int i = 0; i < cseqText.length(); i++) {
            char ch = cseqText.charAt(i);
            if (ch < '0' || ch > '9') {
                SyntheticRefs.c("Invalid RTSP event response sequence");
                return;
            }
        }
        try {
            int cseq = Integer.parseInt(cseqText);
            a pending;
            e listener;
            synchronized (this) {
                pending = this.pendingEventRequests.remove(Integer.valueOf(cseq));
                if (pending == null) {
                    // 补丁：排队 /command（含保活 /feedback）的应答也走这里配对
                    pending = this.pendingCommands.remove(Integer.valueOf(cseq));
                }
                listener = this.responseListener;
            }
            if (pending == null) {
                // 补丁：未知序号静默丢弃，只记数（绝不 close 事件通道）
                int unmatched = this.unmatchedResponses.incrementAndGet();
                if (unmatched <= 3 || unmatched % 50 == 0) {
                    ControlChannel.Log("CARPLAY_RESP_UNMATCHED cseq=" + cseq + " count=" + unmatched);
                }
                return;
            }
            response.d = pending.g();
            if (listener != null) {
                try {
                    listener.c(response.d, normalizeStatus(response.h()), cseq, this.readKey != null);
                } catch (RuntimeException unused) {
                }
            }
            pending.h().complete(response);
        } catch (NumberFormatException unused2) {
            SyntheticRefs.c("Invalid RTSP event response sequence");
        }
    }

    /**
     * 同步路径（{@link #w}）里配对 {@code pendingCommands} 的应答。
     *
     * @return true 表示已配对并完成 future
     */
    private boolean matchQueuedCommandResponse(b response) {
        String cseqText = response.d("cseq");
        if (cseqText != null) {
            try {
                int cseq = Integer.parseInt(cseqText);
                if (Integer.toString(cseq).equals(cseqText)) {
                    a pending = this.pendingCommands.remove(Integer.valueOf(cseq));
                    if (pending != null) {
                        response.d = pending.g();
                        e listener = this.responseListener;
                        if (listener != null) {
                            try {
                                listener.c(response.d, normalizeStatus(response.h()), cseq, true);
                            } catch (RuntimeException unused) {
                            }
                        }
                        pending.h().complete(response);
                        return true;
                    }
                }
            } catch (NumberFormatException unused2) {
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int normalizeStatus(int i) {
        if (i < 100 || i > 599) {
            return -1;
        }
        return i;
    }

    private void k() throws IOException {
        if (this.closed) {
            SyntheticRefs.c("RTSP connection closed");
        }
    }

    private int nextCSeq() {
        while (true) {
            int i = this.cSeqCounter;
            int i2 = i == Integer.MAX_VALUE ? 1 : i + 1;
            this.cSeqCounter = i2;
            if (!this.pendingEventRequests.containsKey(Integer.valueOf(i2)) && !this.pendingCommands.containsKey(Integer.valueOf(this.cSeqCounter))) {
                return this.cSeqCounter;
            }
        }
    }

    public static EventChannel p(String str, int i, int i2) throws IOException {
        if (i2 < 1 || i2 > 120000) {
            SyntheticRefs.b("Timeout must be 1..120000 ms");
            return null;
        }
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(str, i), i2);
            socket.setSoTimeout(i2);
            socket.setTcpNoDelay(true);
            return new EventChannel(socket.getInputStream(), socket.getOutputStream(), socket, socket, i2);
        } catch (IOException e2) {
            socket.close();
            throw e2;
        }
    }

    private static String q(String str, String str2) {
        if (str.equals("GET") && str2.equals("/info")) {
            return "INFO";
        }
        if (!str.equals("POST")) {
            if (str2.equals("/stream") || str2.startsWith("rtsp://")) {
                if (str.equals("SETUP")) {
                    return "STREAM_SETUP";
                }
                if (str.equals("RECORD")) {
                    return "STREAM_RECORD";
                }
                if (str.equals("TEARDOWN")) {
                    return "STREAM_TEARDOWN";
                }
            }
            return "OTHER";
        }
        str2.getClass();
        switch (str2) {
            case "/auth-setup":
                return "MFI_AUTH_SETUP";
            case "/pair-setup":
                return "PAIR_SETUP";
            case "/feedback":
                return "FEEDBACK";
            case "/command":
                return "COMMAND";
            case "/pair-verify":
                return "PAIR_VERIFY";
            default:
                return "OTHER";
        }
    }

    private b s() throws IOException {
        this.E = 0;
        this.F = 0;
        this.G = -1;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            char c2 = 0;
            while (true) {
                char c3 = 1;
                if (c2 == 4) {
                    String[] strArrSplit = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.US_ASCII).split("\r\n");
                    if (strArrSplit.length == 0 || strArrSplit[0].length() > 4096) {
                        SyntheticRefs.c("Malformed RTSP start line");
                        return null;
                    }
                    LinkedHashMap linkedHashMap = new LinkedHashMap();
                    for (int i = 1; i < strArrSplit.length; i++) {
                        if (!strArrSplit[i].isEmpty()) {
                            int iIndexOf = strArrSplit[i].indexOf(58);
                            if (iIndexOf < 1) {
                                SyntheticRefs.c("Malformed RTSP header");
                                return null;
                            }
                            String lowerCase = strArrSplit[i].substring(0, iIndexOf).trim().toLowerCase(Locale.ROOT);
                            if (linkedHashMap.put(lowerCase, strArrSplit[i].substring(iIndexOf + 1).trim()) != null) {
                                SyntheticRefs.c(ProtoMessageBase.e("Duplicate RTSP header: ", lowerCase));
                                return null;
                            }
                        }
                    }
                    if (linkedHashMap.containsKey("transfer-encoding")) {
                        SyntheticRefs.c("Unsupported RTSP transfer encoding");
                        return null;
                    }
                    try {
                        int i2 = linkedHashMap.containsKey("content-length") ? Integer.parseInt((String) linkedHashMap.get("content-length")) : 0;
                        if (i2 < 0 || i2 > 4194304) {
                            SyntheticRefs.c("RTSP body length out of bounds");
                            return null;
                        }
                        this.G = i2;
                        byte[] bArr = new byte[i2];
                        int i3 = 0;
                        while (i3 < i2) {
                            this.K = "PLAIN_BODY";
                            int iT = t();
                            if (iT < 0) {
                                throw new EOFException("RTSP body truncated");
                            }
                            bArr[i3] = (byte) iT;
                            i3++;
                            this.F = i3;
                        }
                        this.B++;
                        if (strArrSplit[0].startsWith("RTSP/") || strArrSplit[0].startsWith("HTTP/")) {
                            this.D++;
                        } else {
                            this.C++;
                        }
                        this.K = "MESSAGE_COMPLETE";
                        return new b(strArrSplit[0], linkedHashMap, bArr);
                    } catch (NumberFormatException e2) {
                        throw new IOException("Invalid RTSP content length", e2);
                    }
                }
                this.K = "PLAIN_HEADER";
                int iT2 = t();
                if (iT2 < 0) {
                    throw new EOFException("RTSP header truncated");
                }
                byteArrayOutputStream.write(iT2);
                this.E = byteArrayOutputStream.size();
                if (byteArrayOutputStream.size() > 32768) {
                    SyntheticRefs.c("RTSP header too large");
                    return null;
                }
                if (c2 == 0) {
                    if (iT2 != 13) {
                        break;
                    }
                    c2 = c3;
                } else if (c2 != 1) {
                    c3 = 3;
                    if (c2 == 2) {
                        if (iT2 != 13) {
                            break;
                        }
                        c2 = c3;
                    } else {
                        if (c2 == 3 && iT2 != 10) {
                            break;
                        }
                        c2 = 4;
                    }
                } else if (iT2 == 10) {
                    c2 = 2;
                } else {
                    if (iT2 != 13) {
                        break;
                    }
                    c2 = c3;
                }
            }
        }
    }

    private int t() throws IOException {
        if (this.readKey == null) {
            int iU = u();
            if (iU >= 0) {
                this.A++;
            }
            return iU;
        }
        while (true) {
            int i = this.i;
            byte[] bArr = this.h;
            if (i != bArr.length) {
                this.i = i + 1;
                int i2 = bArr[i] & 255;
                this.A++;
                this.J = this.h.length - this.i;
                return i2;
            }
            this.K = "RECORD_HEADER";
            this.H = 0;
            this.I = -1;
            int iU2 = u();
            int iU3 = u();
            if (iU2 < 0 || iU3 < 0) {
                throw new EOFException("Control record header truncated");
            }
            int i3 = ((iU3 << 8) | iU2) + 16;
            this.I = i3;
            this.K = "RECORD_BODY";
            byte[] bArr2 = new byte[i3];
            int i4 = 0;
            while (i4 < i3) {
                int iU4 = u();
                if (iU4 < 0) {
                    throw new EOFException("Control record truncated");
                }
                bArr2[i4] = (byte) iU4;
                i4++;
                this.H = i4;
            }
            if (this.k == -1) {
                SyntheticRefs.c("Control nonce exhausted");
                return 0;
            }
            try {
                this.K = "RECORD_AUTHENTICATE";
                byte[] bArr3 = this.readKey;
                long j = this.k;
                this.k = j + 1;
                this.h = DerCodec.o(bArr3, DerCodec.m(j), bArr2, new byte[]{(byte) iU2, (byte) iU3});
                this.i = 0;
                this.H = 0;
                this.I = -1;
                this.J = this.h.length;
            } catch (GeneralSecurityException | RuntimeException e2) {
                throw new IOException("Control record authentication failed", e2);
            }
        }
    }

    private int u() throws IOException {
        int iMin;
        int i;
        long j = this.m;
        if (j != 0) {
            long jNanoTime = j - System.nanoTime();
            if (jNanoTime <= 0) {
                throw new SocketTimeoutException("RTSP request deadline exceeded");
            }
            if (this.d != null && ((iMin = (int) Math.min(this.e, Math.max(1L, (jNanoTime + 999999) / 1000000))) > (i = this.n) || i - iMin > 100)) {
                this.d.setSoTimeout(iMin);
                this.n = iMin;
            }
        }
        this.y++;
        int i2 = this.a.read();
        if (i2 >= 0) {
            this.z++;
        }
        return i2;
    }

    public synchronized void writeMessage(String str, Map<String, String> map, byte[] bArr) throws IOException {
        AppleHttpHeaders.Apply(map);
        try {
            k();
            A(str);
            if (bArr.length > 4194304) {
                throw new IOException("RTSP body too large");
            }
            StringBuilder sb = new StringBuilder(str);
            sb.append("\r\n");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                B(entry.getKey());
                A(entry.getValue());
                if (!entry.getKey().equalsIgnoreCase("Content-Length")) {
                    sb.append(entry.getKey());
                    sb.append(": ");
                    sb.append(entry.getValue());
                    sb.append("\r\n");
                }
            }
            sb.append("Content-Length: ");
            sb.append(bArr.length);
            sb.append("\r\n\r\n");
            byte[] bytes = sb.toString().getBytes(StandardCharsets.US_ASCII);
            if (bytes.length > 32768) {
                throw new IOException("RTSP header too large");
            }
            byte[] bArrD = DerCodec.d(bytes, bArr);
            if (this.writeKey == null) {
                this.b.write(bArrD);
            } else {
                int i = 0;
                while (i < bArrD.length) {
                    if (this.l == -1) {
                        throw new IOException("Control nonce exhausted");
                    }
                    int i2 = i + 16384;
                    byte[] bArrCopyOfRange = Arrays.copyOfRange(bArrD, i, Math.min(bArrD.length, i2));
                    byte[] bArr2 = {(byte) bArrCopyOfRange.length, (byte) (bArrCopyOfRange.length >>> 8)};
                    try {
                        byte[] bArr3 = this.writeKey;
                        long j = this.l;
                        this.l = 1 + j;
                        byte[] bArrS = DerCodec.s(bArr3, DerCodec.m(j), bArrCopyOfRange, bArr2);
                        this.b.write(bArr2);
                        this.b.write(bArrS);
                        i = i2;
                    } catch (GeneralSecurityException | RuntimeException e2) {
                        throw new IOException("Control encryption failed", e2);
                    }
                }
            }
            this.b.flush();
        } catch (RuntimeException th) {
            throw th;
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        a[] aVarArr;
        this.closed = true;
        int i = 0;
        try {
            if (this.c != null && this.v.compareAndSet(false, true)) {
                this.c.close();
            }
            synchronized (this) {
                try {
                    ArrayList arrayList = new ArrayList(this.pendingEventRequests.values());
                    arrayList.addAll(this.pendingCommands.values());
                    aVarArr = (a[]) arrayList.toArray(new a[0]);
                    this.pendingEventRequests.clear();
                    this.pendingCommands.clear();
                    byte[] bArr = this.readKey;
                    if (bArr != null) {
                        Arrays.fill(bArr, (byte) 0);
                    }
                    byte[] bArr2 = this.writeKey;
                    if (bArr2 != null) {
                        Arrays.fill(bArr2, (byte) 0);
                    }
                } catch (RuntimeException th) {
                    throw th;
                }
            }
            int length = aVarArr.length;
            while (i < length) {
                aVarArr[i].h().completeExceptionally(new IOException("RTSP event connection closed"));
                i++;
            }
        } catch (Throwable th2) {
            synchronized (this) {
                try {
                    ArrayList arrayList2 = new ArrayList(this.pendingEventRequests.values());
                    arrayList2.addAll(this.pendingCommands.values());
                    a[] aVarArr2 = (a[]) arrayList2.toArray(new a[0]);
                    this.pendingEventRequests.clear();
                    this.pendingCommands.clear();
                    byte[] bArr3 = this.readKey;
                    if (bArr3 != null) {
                        Arrays.fill(bArr3, (byte) 0);
                    }
                    byte[] bArr4 = this.writeKey;
                    if (bArr4 != null) {
                        Arrays.fill(bArr4, (byte) 0);
                    }
                    int length2 = aVarArr2.length;
                    while (i < length2) {
                        aVarArr2[i].h().completeExceptionally(new IOException("RTSP event connection closed"));
                        i++;
                    }
                    throw th2;
                } catch (RuntimeException th3) {
                    throw th3;
                }
            }
        }
    }

    public synchronized CompletableFuture<b> g(String str, String str2, String str3, byte[] bArr) throws IOException {
        CompletableFuture<b> completableFuture;
        try {
            k();
            if (this.asyncStarted || this.readKey == null || this.writeKey == null) {
                throw new IOException("Queued command requires encrypted main control");
            }
            if (!"POST".equals(str) || !"/command".equals(str2) || !"application/x-apple-binary-plist".equals(str3)) {
                throw new IOException("Only optional plist commands can be queued");
            }
            B(str);
            A(str2);
            if (bArr == null) {
                throw new NullPointerException("body");
            }
            if (this.pendingCommands.size() >= 16) {
                throw new IOException("Too many pending RTSP control commands");
            }
            int cseq = nextCSeq();
            completableFuture = new CompletableFuture<>();
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            linkedHashMap.put("CSeq", Integer.toString(cseq));
            if (str3 != null) {
                linkedHashMap.put("Content-Type", str3);
            }
            this.pendingCommands.put(Integer.valueOf(cseq), new a(q(str, str2), completableFuture, 0));
            try {
                writeMessage(str + " " + str2 + " RTSP/1.0", linkedHashMap, bArr);
            } catch (IOException | RuntimeException e2) {
                this.pendingCommands.remove(Integer.valueOf(cseq));
                completableFuture.completeExceptionally(e2);
                close();
                throw e2;
            }
        } catch (RuntimeException th) {
            throw th;
        }
        return completableFuture;
    }

    /**
     * 事件通道保活：在 event 连接上发一条空体 {@code POST /feedback RTSP/1.0}。
     *
     * <p>背景（2026-11-03 真机日志 OcarPlay-log-20261103-090755）：本 App 对事件通道
     * 只读不写，车机 accept 后约 10.4s 收不到任何 RTSP 报文就 FIN 掉这条连接
     * （{@code CARPLAY_EVENT_INPUT point=TRANSPORT_END ... read_calls=2 raw_bytes=0}），
     * App 又把每次 EOF 升级成整会话重认证，3 次重试耗尽后约 60s 断开投屏。
     * AirPlay 的约定正好是 device 在事件连接上周期上报 /feedback（既是保活，
     * 也是播放进度回执，见 {@code POST /feedback} 走 event 连接而非 control 连接）。
     *
     * <p>为什么必须走 {@code pendingCommands} 队列而不是 {@code v(...)}：{@code v()} 会同步等应答，
     * 与读线程抢同一个 BufferedInputStream；写进队列后由读线程的
     * {@link #handleEventResponse} 按 CSeq 匹配并完成 future。
     *
     * @return true 已写出；false 通道未就绪（未加密 / 读线程未启动 / 已关闭）
     */
    public synchronized boolean sendFeedback() throws IOException {
        if (this.closed || this.readKey == null || this.writeKey == null || !this.asyncStarted) {
            return false;
        }
        if (this.pendingCommands.size() >= 8) {
            // 车机长时间不回 /feedback 时，先丢旧的保活请求，避免队列无限堆积
            Iterator<Map.Entry<Integer, a>> entryIterator = this.pendingCommands.entrySet().iterator();
            while (entryIterator.hasNext()) {
                if (FEEDBACK_TAG.equals(entryIterator.next().getValue().g())) {
                    entryIterator.remove();
                }
            }
        }
        int cseq = nextCSeq();
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("CSeq", Integer.toString(cseq));
        this.pendingCommands.put(Integer.valueOf(cseq), new a(FEEDBACK_TAG, new CompletableFuture<>()));
        try {
            writeMessage("POST /feedback RTSP/1.0", headers, EMPTY);
        } catch (IOException | RuntimeException writeFailure) {
            this.pendingCommands.remove(Integer.valueOf(cseq));
            throw writeFailure;
        }
        return true;
    }

    public synchronized void i(byte[] bArr, byte[] bArr2) throws IOException {
        k();
        if (this.asyncStarted) {
            throw new IOException("Encryption change on event connection");
        }
        if (this.readKey != null) {
            throw new IOException("Control encryption already active");
        }
        if (bArr.length != 32 || bArr2.length != 32) {
            throw new IllegalArgumentException("Control key length");
        }
        this.readKey = (byte[]) bArr.clone();
        this.writeKey = (byte[]) bArr2.clone();
        this.l = 0L;
        this.k = 0L;
    }

    public CompletableFuture<b> l(String str, String str2, String str3, byte[] bArr) throws IOException {
        CompletableFuture<b> completableFuture = new CompletableFuture<>();
        // jadx 漏了方法级声明：下面 try/catch 里用 `e = null;` / `e = e2;` 转发异常，
        // 但没有 `Throwable e;`，于是 `e` 落到字段 `private final int e;` 上。
        // 补声明后 `e.addSuppressed(...)` 恢复为异常语义，字段仍由 `this.e` 访问。
        Throwable e = null;
        synchronized (this) {
            try {
                try {
                    k();
                    B(str);
                    A(str2);
                    if (bArr == null) {
                        throw new NullPointerException("body");
                    }
                    if (this.pendingEventRequests.size() >= 16) {
                        throw new IOException("Too many pending RTSP event requests");
                    }
                    this.asyncStarted = true;
                    int cseq = nextCSeq();
                    LinkedHashMap linkedHashMap = new LinkedHashMap();
                    linkedHashMap.put("CSeq", Integer.toString(cseq));
                    if (str3 != null) {
                        linkedHashMap.put("Content-Type", str3);
                    }
                    this.pendingEventRequests.put(Integer.valueOf(cseq), new a(q(str, str2), completableFuture, 0));
                    try {
                        writeMessage(str + " " + str2 + " RTSP/1.0", linkedHashMap, bArr);
                        e = null;
                    } catch (IOException | RuntimeException e2) {
                        e = e2;
                        this.pendingEventRequests.remove(Integer.valueOf(cseq));
                        this.closed = true;
                    }
                } catch (RuntimeException th) {
                    throw th;
                }
            } catch (IOException | RuntimeException e3) {
                completableFuture.completeExceptionally(e3);
            }
        }
        if (e != null) {
            completableFuture.completeExceptionally(e);
            try {
                close();
            } catch (IOException e4) {
                e.addSuppressed(e4);
            }
        }
        return completableFuture;
    }

    public String m() {
        return "RTSP_INPUT reader_started=" + this.w + " reader_ended=" + this.x + " closed=" + this.closed + " stage=" + this.K + " read_calls=" + this.y + " raw_bytes=" + this.z + " plain_bytes=" + this.A + " messages=" + this.B + " requests=" + this.C + " responses=" + this.D + " unmatched_responses=" + this.unmatchedResponses.get() + " header_bytes=" + this.E + " body_bytes=" + this.F + " body_expected=" + this.G + " record_bytes=" + this.H + " record_expected=" + this.I + " pending_plain_bytes=" + this.J;
    }

    public synchronized boolean n() {
        return (this.closed || this.readKey == null || this.writeKey == null) ? false : true;
    }

    public synchronized b r() throws IOException {
        b bVarS;
        k();
        if (this.asyncStarted) {
            throw new IOException("Synchronous read on event connection");
        }
        this.m = (((long) this.e) * 1000000) + System.nanoTime();
        try {
            bVarS = s();
            this.m = 0L;
        } catch (Throwable th) {
            this.m = 0L;
            throw th;
        }
        return bVarS;
    }

    public synchronized b v(String str, String str2, String str3, byte[] bArr) throws IOException {
        return w(str, str2, str3, bArr, Collections.EMPTY_MAP);
    }

    public synchronized b w(String str, String str2, String str3, byte[] bArr, Map<String, String> map) throws IOException {
        b bVarS;
        try {
            k();
            if (this.asyncStarted) {
                throw new IOException("Synchronous request on event connection");
            }
            B(str);
            A(str2);
            int cseq = nextCSeq();
            LinkedHashMap linkedHashMap = new LinkedHashMap(map);
            linkedHashMap.put("CSeq", Integer.toString(cseq));
            if (str3 != null) {
                linkedHashMap.put("Content-Type", str3);
            }
            this.m = (((long) this.e) * 1000000) + System.nanoTime();
            try {
                try {
                    writeMessage(str + " " + str2 + " RTSP/1.0", linkedHashMap, bArr);
                    for (int i = 0; i < 32; i++) {
                        bVarS = s();
                        if (!bVarS.a.startsWith("RTSP/") && !bVarS.a.startsWith("HTTP/")) {
                            c cVar = this.requestHandler;
                            byte[] bArrA = cVar == null ? null : cVar.a(bVarS);
                            LinkedHashMap linkedHashMap2 = new LinkedHashMap();
                            if (bVarS.d("cseq") != null) {
                                linkedHashMap2.put("CSeq", bVarS.d("cseq"));
                            }
                            String str4 = "RTSP/1.0 " + (bArrA == null ? "501 Not Implemented" : "200 OK");
                            if (bArrA == null) {
                                bArrA = new byte[0];
                            }
                            writeMessage(str4, linkedHashMap2, bArrA);
                        } else if (!matchQueuedCommandResponse(bVarS)) {
                            String strD = bVarS.d("cseq");
                            if (strD != null && !strD.equals(Integer.toString(cseq))) {
                                throw new IOException("RTSP CSeq mismatch");
                            }
                            bVarS.d = q(str, str2);
                            e eVar = this.responseListener;
                            if (eVar != null) {
                                try {
                                    eVar.c(bVarS.d, normalizeStatus(bVarS.h()), cseq, this.readKey != null);
                                } catch (RuntimeException unused) {
                                }
                            }
                            this.m = 0L;
                        }
                    }
                    throw new IOException("Too many unsolicited RTSP requests");
                } catch (IOException | RuntimeException e2) {
                    close();
                    throw e2;
                }
            } catch (Throwable th) {
                this.m = 0L;
                throw th;
            }
        } catch (RuntimeException th2) {
            throw th2;
        }
    }

    public synchronized void x(c cVar) {
        this.requestHandler = cVar;
    }

    public synchronized void y(e eVar) {
        this.responseListener = eVar;
    }

    public void z(c cVar) throws IOException {
        if (cVar == null) {
            throw new NullPointerException("handler");
        }
        synchronized (this) {
            k();
            if (this.u) {
                throw new IOException("RTSP event reader already active");
            }
            this.asyncStarted = true;
            this.u = true;
            this.w = true;
            this.m = 0L;
        }
        try {
            try {
                Socket socket = this.d;
                if (socket != null) {
                    socket.setSoTimeout(0);
                }
                while (!this.closed) {
                    b bVarS = s();
                    if (bVarS.a.startsWith("RTSP/") || bVarS.a.startsWith("HTTP/")) {
                        handleEventResponse(bVarS);
                    } else {
                        byte[] replyBody;
                        try {
                            replyBody = cVar.a(bVarS);
                        } catch (Throwable handlerFailure) {
                            // 补丁（2026-11-03）：入站请求处理器的异常只回 501，绝不能关掉整条事件通道。
                            // 背景：AirPlaySession.s1(...) 仍是 jadx 桩，车机一旦发 /command
                            // 就会抛 UnsupportedOperationException，原逻辑会一路冒到读线程的
                            // catch 里 close()，把保活刚救回来的事件连接又打断。
                            int failed = this.inboundHandlerFailures.incrementAndGet();
                            if (failed <= 3 || failed % 20 == 0) {
                                ControlChannel.Log("CARPLAY_INBOUND_HANDLER_FAILED uri=" + bVarS.a
                                        + " type=" + handlerFailure.getClass().getSimpleName()
                                        + " count=" + failed);
                            }
                            replyBody = null;
                        }
                        byte[] bArrA = replyBody;
                        LinkedHashMap linkedHashMap = new LinkedHashMap();
                        if (bVarS.d("cseq") != null) {
                            linkedHashMap.put("CSeq", bVarS.d("cseq"));
                        }
                        if (bArrA != null && bArrA.length > 0) {
                            linkedHashMap.put("Content-Type", "application/x-apple-binary-plist");
                        }
                        String str = bVarS.a.endsWith("HTTP/1.1") ? "HTTP/1.1" : "RTSP/1.0";
                        synchronized (this) {
                            try {
                                String str2 = str + (bArrA == null ? " 501 Not Implemented" : " 200 OK");
                                if (bArrA == null) {
                                    bArrA = new byte[0];
                                }
                                writeMessage(str2, linkedHashMap, bArrA);
                            } catch (RuntimeException th) {
                                throw th;
                            }
                        }
                    }
                }
                this.x = true;
                this.K = "ENDED";
                close();
            } catch (IOException | RuntimeException e2) {
                try {
                    close();
                } catch (IOException e3) {
                    e2.addSuppressed(e3);
                }
                throw e2;
            }
        } catch (Throwable th2) {
            this.x = true;
            this.K = "ENDED";
            close();
            throw th2;
        }
    }

    public static final class a extends ProtoMessageBase {
        private final String a;
        private final CompletableFuture<b> b;

        private a(String str, CompletableFuture<b> completableFuture) {
            this.a = str;
            this.b = completableFuture;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return Objects.equals(this.a, aVar.a) && Objects.equals(this.b, aVar.b);
        }

        public String g() {
            return this.a;
        }

        public CompletableFuture<b> h() {
            return this.b;
        }

        public final int hashCode() {
            String str = this.a;
            CompletableFuture<b> completableFuture = this.b;
            return Objects.hashCode(completableFuture) + (Objects.hashCode(str) * 31);
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(a.class, sb, "[");
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

        public /* synthetic */ a(String str, CompletableFuture completableFuture, int i) {
            this(str, completableFuture);
        }
    }

    public static final class d extends IOException {
        private static final long c = 1;
        public final int a;
        public final String b;

        private d(int i, String str) {
            super("CONTROL_RESPONSE_REJECTED status=" + i + " op=" + str);
            this.a = i;
            this.b = str;
        }

        public /* synthetic */ d(String str, int i, int i2) {
            this(i, str);
        }
    }

    public EventChannel(Socket socket, int i) throws IOException {
        this(socket.getInputStream(), socket.getOutputStream(), socket, socket, C(i));
        if (!socket.isConnected() || socket.isClosed()) {
            SyntheticRefs.c("Socket must be connected");
            throw null;
        }
        socket.setSoTimeout(i);
        socket.setTcpNoDelay(true);
    }

    public EventChannel(InputStream inputStream, OutputStream outputStream, Closeable closeable) {
        this(inputStream, outputStream, closeable, null, 15000);
    }
}
