/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 EventChannel。事件通道：承载车机推送的触摸/按键/命令（/command、/feedback），并维持长连接与保活。
 *   This file defines EventChannel. Event channel: carries head-unit pushed touch, key and command traffic (/command, /feedback) and keeps the long-lived connection alive.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

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



/**
 * 事件通道：承载车机推送的触摸/按键/命令（/command、/feedback），并维持长连接与保活。
 *
 * Event channel: carries head-unit pushed touch, key and command traffic (/command, /feedback) and keeps the long-lived connection alive.
 */
public final class EventChannel implements Closeable {
    public static final int L = 4194304;
    public static final int M = 32768;
    private static final int N = 16;
    private static final byte[] EMPTY = new byte[0];
    
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
    
    private final AtomicInteger unhandledInboundRequests = new AtomicInteger();
    private volatile boolean w;
    private volatile boolean x;
    private volatile long y;
    private volatile long z;

    /**
     * EventChannel 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in EventChannel, carrying local implementation detail.
     */
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
            // 诊断（V1.7.94）：非 200 时把响应体打出来。车机对 /command 的拒绝原因
            // （缺键 / 值非法 / 时序不允许）通常写在 body 里，此前直接丢弃导致只能看到裸状态码。
            try {
                byte[] responseBody = this.c;
                String bodyText = responseBody == null || responseBody.length == 0
                        ? "EMPTY"
                        : ControlChannel.Hex(responseBody, Math.min(responseBody.length, 160));
                ControlChannel.Log("CONTROL_RESP_STATUS status=" + h() + " op=" + this.d
                        + " bodyLen=" + (responseBody == null ? 0 : responseBody.length)
                        + " bodyHex=" + bodyText);
            } catch (Throwable ignored) {
                // 诊断失败绝不影响正常异常路径。
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

    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface c {
        byte[] a(b bVar) throws IOException;
    }

    @FunctionalInterface
    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
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
                    
                    pending = this.pendingCommands.remove(Integer.valueOf(cseq));
                }
                listener = this.responseListener;
            }
            if (pending == null) {
                
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

    @Override 
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
        return g(str, str2, str3, bArr, null);
    }

    // [OBF-MAP] queueCommandWithHeaders | 职责: 与 g() 相同，但允许附加 RTSP 请求头。
    // 判读: V1.8.21 之前，R1() 的同步 /command 走 w() 并带 X-Apple-Session-ID，
    // 而 AirPlaySession.I0() 的异步 /command 走本文件的 g()，请求头里只有 CSeq/Content-Type，
    // 没有任何会话标识 ⇒ 同一端点两套语义。hidSetInputMode 因此无法被车机归属到当前会话。
    // 调用方传入的 headers 先写入，CSeq 后写入以免被覆盖。
    public synchronized CompletableFuture<b> g(String str, String str2, String str3, byte[] bArr, Map<String, String> extraHeaders) throws IOException {
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
            if (extraHeaders != null) {
                linkedHashMap.putAll(extraHeaders);
            }
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

    
    public synchronized boolean sendFeedback() throws IOException {
        if (this.closed || this.readKey == null || this.writeKey == null || !this.asyncStarted) {
            return false;
        }
        if (this.pendingCommands.size() >= 8) {
            
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
                            byte[] bArrA;
                            if (cVar == null) {
                                bArrA = null;
                            } else {
                                
                                try {
                                    bArrA = cVar.a(bVarS);
                                } catch (Throwable handlerFailure) {
                                    int failed = this.inboundHandlerFailures.incrementAndGet();
                                    if (failed <= 3 || failed % 20 == 0) {
                                        ControlChannel.Log("CARPLAY_INBOUND_HANDLER_FAILED uri=" + bVarS.a
                                                + " type=" + handlerFailure.getClass().getSimpleName()
                                                + " count=" + failed);
                                    }
                                    bArrA = null;
                                }
                            }
                            LinkedHashMap linkedHashMap2 = new LinkedHashMap();
                            if (bVarS.d("cseq") != null) {
                                linkedHashMap2.put("CSeq", bVarS.d("cseq"));
                            }
                            
                            
                            
                            if (bArrA == null) {
                                int unhandled = this.unhandledInboundRequests.incrementAndGet();
                                if (unhandled <= 3 || unhandled % 20 == 0) {
                                    ControlChannel.Log("CARPLAY_INBOUND_UNHANDLED uri=" + bVarS.a
                                            + " count=" + unhandled);
                                }
                                bArrA = new byte[0];
                            }
                            String str4 = "RTSP/1.0 200 OK";
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
                            
                            
                            
                            return bVarS;
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
                                String str2 = str + " 200 OK";
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

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
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

        public  a(String str, CompletableFuture completableFuture, int i) {
            this(str, completableFuture);
        }
    }

    /**
     * 扩展实现：在 IOException 基础上补充 EventChannel 需要的局部行为。
     *
     * Extension of IOException adding the local behaviour EventChannel needs.
     */
    public static final class d extends IOException {
        private static final long c = 1;
        public final int a;
        public final String b;

        private d(int i, String str) {
            super("CONTROL_RESPONSE_REJECTED status=" + i + " op=" + str);
            this.a = i;
            this.b = str;
        }

        public  d(String str, int i, int i2) {
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
