/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 MfiCertificateVerifier。MFi 证书校验：解析并验证 Apple 签发的证书链、有效期与用途。
 *   This file defines MfiCertificateVerifier. MFi certificate verifier: parses and validates the Apple-issued certificate chain, validity window and key usage.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;



/**
 * MFi 证书校验：解析并验证 Apple 签发的证书链、有效期与用途。
 *
 * MFi certificate verifier: parses and validates the Apple-issued certificate chain, validity window and key usage.
 */
public final class MfiCertificateVerifier {
    private static final int O0 = 128;
    // [V1.8.41] 车机固件里出现的 EA 协议名（libNmeCarPlay / MediaManagerService 字符串表），
    // 按 com.buick.vip 优先排列：这是 sysdiagnose 里真实 iPhone 会话命中的那一个。
    private static final String[] EA_PROTOCOLS = {
            "com.buick.vip", "com.buick.noprotocol", "com.gm.noprotocol", "com.gm.vip"};
    /**
     * [V1.8.42] EAP 协议索引（param 10 条目的子参数 0）。
     * iOS sysdiagnose 已证实：com.buick.vip = 1、com.buick.noprotocol = 3。
     * 另两个为推断值（vip 组 1/2、noprotocol 组 3/4，buick 优先），
     * 待 IAP2_PEER_EAP_HEX 探针 dump 车机自己 0x1D01 的 4 条 param 10 原始字节后校准，
     * 因此日志固定带 eap_index_assumed=true，不要把本数组当成实测值。
     */
    private static final int[] EA_PROTO_INDEX = {1, 3, 4, 2};
    private static final long[] P0 = {2000, 6000, 14000};
    // 车机 0x1D01 param 17（BluetoothTransportComponent）里解析出的 ComponentID，
    // 用于回填 0x4E0E（DeviceTransportChannelID）。Info3.6 实测为 1。
    private static volatile int bleCompId = 1;
    private int A;
    private final boolean A0;
    private int B;
    private boolean B0;
    private int C;
    private boolean C0;
    private int D;
    private boolean D0;
    private long E;
    private boolean E0;
    private boolean F;
    private boolean F0;
    private int G;
    private boolean G0;
    private int H;
    private boolean H0;
    private RsaPkcs1Cipher I;
    private long I0;
    private int J;
    private final j5 J0;
    private int K;
    private int K0;
    private int L;
    private boolean L0;
    private int M;
    private DeviceMetadataStore M0;
    private int N;
    private Iap2Csm.a N0;
    private int O;
    private int P;
    /** [V1.8.52] A：车机 0x1D01 是否声明 param 24（WirelessCarPlayTransportComponent）。 */
    private boolean P24;
    private int Q;
    private int R;
    private int S;
    private int T;
    private int U;
    private int V;
    private int W;
    private int X;
    private int Y;
    private int Z;
    // [V1.8.41] EA（External Accessory）设备登记状态。
    // ea0 已发起过 0xEA00；ea1 当前尝试的协议索引；ea2 收到的 EA 数据包计数；ea3 最近一次应答 status。
    private boolean ea0;
    private int ea1;
    private int ea2;
    private int ea3 = -1;
    private final c a;

    /**
     * 车机是否完全没有「通过 iAP2 下发 WiFi 凭证」的能力。
     *
     * 判定依据（0x1D01 的 EA 列表，见 a0）三条同时成立：
     *   - 车机 RECEIVE 不含 0x5702(22274) ⇒ 它不接受手机的 WiFi 配置请求
     *   - 车机 SEND   不含 0x5703(22275) ⇒ 它不会下发 WiFi 凭证
     *   - 车机 SEND   不含 0x4301(17153) ⇒ 它也不支持新协议 StartSession 携带凭证
     *
     * 此时 Apple 规范里「蓝牙 iAP2 只负责传 WiFi 凭证」的路径在本机不可能走通，
     * 必须由用户手动把手机接入车机热点（走 AirPlay）。PairingSession 据此缩短无效等待，
     * 避免 3 × 45s 的「等凭证 → 超时 → 重连」空转。
     */
    public static volatile boolean PEER_WIFI_CREDENTIAL_CHANNEL_ABSENT = false;
    
    private int rxLastAck;
    private boolean b;
    private int b0;
    private boolean c;
    private int c0;
    private int d;
    private String d0;
    private final String e;
    private String e0;
    private final String f;
    private boolean f0;
    private final String g;
    private int g0;
    private final Iap2Csm.Iap2FrameValidator h;
    private int h0;
    private Iap2Csm.d i;
    private int i0;
    private final ArrayDeque<d> j;
    private int j0;
    private final ArrayDeque<f> k;
    private int k0;
    private Set<Integer> l;
    private int l0;
    private Set<Integer> m;
    private int m0;
    private int n;
    private Iap2Csm.g n0;
    private int o;
    private boolean o0;
    private int p;
    private boolean p0;
    private int q;
    private boolean q0;
    private boolean r;
    private String r0;
    private boolean s;
    private long s0;
    private boolean t;
    private int t0;
    private boolean u;
    private int u0;
    private boolean v;
    private int v0;
    private boolean w;
    private int w0;
    private boolean x;
    private boolean x0;
    private byte[] y;
    private boolean y0;
    private int z;
    private String z0;

    /**
     * 枚举：本子系统使用的状态/类型取值集合。
     *
     * Enumeration: state/type values used by this subsystem.
     */
    public enum DetectionState {
        COMPLETE_DETECTION_WITHOUT_LINK
    }

    /**
     * 枚举：本子系统使用的状态/类型取值集合。
     *
     * Enumeration: state/type values used by this subsystem.
     */
    public enum InputFlags {
        NO_PROTOCOL_INPUT
    }

    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface c {
        default void a(String str, byte[] bArr) throws IOException {
            throw new IOException("MFI_FIRST_USE_TRUST_STORAGE_UNAVAILABLE");
        }

        void b(Iap2Csm.a aVar) throws IOException;

        default DeviceMetadataStore c() throws IOException {
            return null;
        }

        default boolean d() {
            return false;
        }

        default Boolean e() {
            return null;
        }

        default void f(Iap2Csm.g gVar) {
        }

        void g(String str, byte[] bArr);

        void h(String str);

        default void i(Iap2Csm.a aVar) {
        }

        default DeviceTimeRange j() {
            return DeviceTimeRange.j();
        }

        default void k(Iap2Csm.a aVar) {
        }

        default String l() {
            return null;
        }

        void write(byte[] bArr) throws IOException;
    }

    /**
     * MfiCertificateVerifier 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in MfiCertificateVerifier, carrying local implementation detail.
     */
    public static final class d {
        final int a;
        final byte[] b;
        int c;

        public d(int i, byte[] bArr) {
            this.a = i;
            this.b = bArr;
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class e extends ProtoMessageBase {
        private final String a;

        public e(String str) {
            if (str == null || !str.matches("[a-fA-F0-9]{64}")) {
                SyntheticRefs.b("invalid verified leaf");
                throw null;
            }
            this.a = str;
        }

        public final boolean equals(Object obj) {
            return (obj instanceof e) && Objects.equals(this.a, ((e) obj).a);
        }

        public String g() {
            return this.a;
        }

        public final int hashCode() {
            return Objects.hashCode(this.a);
        }

        public final String toString() {
            Object[] objArr = {this.a};
            String[] strArrSplit = "a".length() == 0 ? new String[0] : "a".split(";");
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

    public MfiCertificateVerifier(String str, String str2, boolean z, c cVar) {
        this.h = new Iap2Csm.Iap2FrameValidator();
        this.i = new Iap2Csm.d();
        this.j = new ArrayDeque<>();
        this.k = new ArrayDeque<>();
        Set<Integer> set = Collections.EMPTY_SET;
        this.l = set;
        this.m = set;
        this.n = new SecureRandom().nextInt(256);
        this.o = -1;
        this.p = 1;
        this.q = 4096;
        this.A = -1;
        this.C = 1000;
        this.D = 5;
        this.J = -2;
        this.K = -2;
        this.L = -2;
        this.M = -2;
        this.Y = -1;
        this.Z = -1;
        this.rxLastAck = -1;
        this.b0 = -1;
        this.d0 = "NONE";
        this.e0 = "NONE";
        this.s0 = -1L;
        this.I0 = -1L;
        this.J0 = new j5();
        if (str != null && !str.matches("[a-fA-F0-9]{64}")) {
            SyntheticRefs.b("Certificate pin must be SHA-256 hex");
            throw null;
        }
        this.e = str;
        this.f = str2;
        this.A0 = z;
        this.a = cVar;
        this.g = UUID.randomUUID().toString();
    }

    private void B(String str) {
        if ((!this.b || this.c) && !this.u && this.n0 == null) {
            int i = this.W;
            if (i >= 128) {
                this.X = d0(this.X, 1);
            } else {
                this.W = i + 1;
                c0(str);
            }
        }
    }

    private void D(String str) {
        this.d0 = str;
        B(ProtoMessageBase.e("IAP2_RX_ROUTE decision=", str));
    }

    
    public static  boolean J(d dVar) {
        return dVar.a == 17152;
    }

    
    public static  boolean K(int i, Iap2Csm.e eVar) {
        return eVar.a == i;
    }

    
    public static  boolean L(d dVar) {
        return dVar.a == 44545;
    }

    
    public  void M(j5.c cVar) throws IOException {
        x(44545, Iap2Csm.B(4, new byte[]{cVar.g() ? (byte) 1 : (byte) 0}));
    }

    
    public static  boolean N(d dVar) {
        return dVar.a == 44545 && dVar.c == 0;
    }

    
    public static  boolean O(d dVar) {
        return dVar.a == 44545 && dVar.c == 0;
    }

    private static Set<Integer> P(byte[] bArr) throws IOException {
        if (bArr.length > 2048) {
            SyntheticRefs.c("IAP2_IDENTIFICATION_MESSAGE_LIST_LIMIT");
            return null;
        }
        HashSet hashSet = new HashSet();
        for (int i = 0; i < bArr.length; i += 2) {
            hashSet.add(Integer.valueOf(Iap2Csm.C(bArr, i)));
        }
        return Set.copyOf(hashSet);
    }

    
    
    
    private void Q(String str, Set<Integer> set) {
        int i;
        if (this.b) {
            return;
        }
        int[] array = set.stream().mapToInt(new q(2)).sorted().toArray();
        int iMax = Math.max(1, (array.length + 31) / 32);
        int i2 = 0;
        while (i2 < iMax) {
            StringBuilder sb = new StringBuilder();
            int i3 = i2 * 32;
            while (true) {
                i = i2 + 1;
                if (i3 >= Math.min(array.length, i * 32)) {
                    break;
                }
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(j0(array[i3]));
                i3++;
            }
            int length = array.length;
            
            
            String ids = array.length == 0 ? "NONE" : String.valueOf(sb);
            c0("IAP2_PEER_MESSAGE_IDS direction=" + str + " count=" + length + " part=" + i + "/" + iMax + " ids=" + ids);
            i2 = i;
        }
    }

    private boolean R() {
        return !this.b && this.m.contains(17153) && this.l.contains(17152) && !this.l.contains(22274);
    }

    public static MfiCertificateVerifier S(String str, String str2, c cVar) {
        if (str == null || !str.matches("[a-fA-F0-9]{64}")) {
            SyntheticRefs.b("pinned network identity required");
            return null;
        }
        MfiCertificateVerifier mfiCertificateVerifier = new MfiCertificateVerifier(str, str2, false, cVar);
        mfiCertificateVerifier.b = true;
        return mfiCertificateVerifier;
    }

    private int T() {
        int i = this.n;
        this.n = (i + 1) & 255;
        return i;
    }

    private void U(byte[] bArr) throws IOException {
        String str;
        if (bArr != null && bArr.length >= 13) {
            if ((bArr.length - 10) % 3 == 0) {
                boolean z = false;
                if (bArr[0] == 1) {
                    int i = bArr[1] & 255;
                    this.q = Iap2Csm.C(bArr, 2);
                    int iC = Iap2Csm.C(bArr, 4);
                    int iC2 = Iap2Csm.C(bArr, 6);
                    int i2 = bArr[8] & 255;
                    int i3 = bArr[9] & 255;
                    this.f0 = true;
                    this.g0 = i;
                    int i4 = this.q;
                    this.h0 = i4;
                    this.i0 = iC;
                    this.j0 = iC2;
                    this.k0 = i2;
                    this.l0 = i3;
                    this.m0 = (bArr.length - 10) / 3;
                    this.a.h("IAP2_LSP_RX window=" + i + " packet=" + i4 + " retry_ms=" + iC + " ack_ms=" + iC2 + " retries=" + i2 + " cumulative=" + i3 + " sessions=" + ((bArr.length - 10) / 3));
                    if (iC == 0 && iC2 == 0 && i2 == 0 && i3 == 0) {
                        z = true;
                    }
                    this.w = z;
                    if (i < 1 || i > 127) {
                        str = "WINDOW";
                    } else if (this.q < 24) {
                        str = "PACKET";
                    } else if (!z && iC < 20) {
                        str = "RETRY_TIMEOUT";
                    } else if (!z && (iC2 < 10 || iC2 > iC / 2)) {
                        str = "ACK_TIMEOUT";
                    } else if (z || (i2 >= 1 && i2 <= 30)) {
                        str = i3 > i ? "ACK_COUNT" : null;
                    } else {
                        str = "RETRY_COUNT";
                    }
                    if (str != null) {
                        SyntheticRefs.c("IAP2_LSP_RANGE_".concat(str));
                        return;
                    }
                    if (z) {
                        iC = 1000;
                    }
                    this.C = iC;
                    this.D = z ? 5 : Math.min(5, i2);
                    for (int i5 = 10; i5 < bArr.length; i5 += 3) {
                        B("IAP2_LSP_SESSION sid=" + (bArr[i5] & 255) + " type=" + (bArr[i5 + 1] & 255) + " version=" + (bArr[i5 + 2] & 255));
                    }
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    int i6 = -1;
                    for (int i7 = 10; i7 < bArr.length; i7 += 3) {
                        if (bArr[i7 + 1] != 0) {
                            continue;
                        }
                        if (i6 != -1) {
                            SyntheticRefs.c("IAP2_MULTIPLE_CONTROL_SESSIONS");
                            return;
                        }
                        i6 = bArr[i7] & 255;
                        if (i6 == 0) {
                            SyntheticRefs.c("IAP2_CONTROL_SESSION_INVALID");
                            return;
                        }
                        int iVer = bArr[i7 + 2] & 255;
                        if (iVer < 1 || iVer > 2) {
                            SyntheticRefs.c("IAP2_CONTROL_SESSION_INVALID");
                            return;
                        }
                    }
                    if (i6 < 0) {
                        SyntheticRefs.c("IAP2_CONTROL_SESSION_MISSING");
                        return;
                    }
                    this.p = i6;
                    B(ProtoMessageBase.d("IAP2_LSP_SELECTED_CONTROL sid=", i6));
                    this.a.h(this.w ? "IAP2_LSP_MODE_ZERO_ACK" : "IAP2_LSP_MODE_STANDARD_ACK");
                    return;
                }
            }
        }
        SyntheticRefs.c("IAP2_LSP_INVALID");
    }

    private void V(String str) {
        int i = this.K0;
        if (i < 16) {
            this.K0 = i + 1;
            c0(str);
        }
    }

    private void W() throws IOException {
        DeviceTimeRange g5VarJ;
        if (this.F0) {
            return;
        }
        if (!this.t || !this.s) {
            SyntheticRefs.c("IAP2_PUBLISH_BEFORE_AUTHENTICATION_IDENTIFICATION");
            return;
        }
        this.F0 = true;
        if (!this.L0) {
            this.L0 = true;
            if (this.b || R()) {
                this.M0 = this.a.c();
            }
        }
        boolean z = this.M0 != null && (this.b || R());
        if (z) {
            this.a.h("IAP2_PHONE_IDENTIFICATION snapshot_shared=true name_source=" + String.valueOf(this.M0.c()) + " name_utf8_bytes=" + this.M0.b() + " values_logged=false");
        }
        String strE = z ? this.M0.e() : this.g;
        String strA = z ? this.M0.a() : this.f;
        if (this.b) {
            z(19980, Iap2Csm.B(0, Iap2Csm.A(strE)));
            z(19977, Iap2Csm.B(0, Iap2Csm.A(strA)));
            z(19978, Iap2Csm.B(0, Iap2Csm.A("zh")));
            this.a.h("IAP2_NETWORK_READY fresh_proof=true wifi_bootstrap_replayed=false");
            return;
        }
        // [V1.8.43 U7] 无视车机宣告，强发 0x5702（RequestAccessoryWiFiConfigurationInformation）。
        // 规范 §23.2.7：蓝牙 iAP2 的唯一职责就是 5702→5703 传 WiFi 凭证；车机 0x1D01 的
        // RECEIVE 列表不含 22274 时，z() 会把它拦成 IAP2_TX_SKIPPED_NOT_ADVERTISED，凭证闭环
        // 永远不发生，配对状态机超时走 pair_failure。这里直接走 x() 入队（与 z() 唯一的差别
        // 就是跳过 this.l.contains 门控），不加开关；车机若真不支持会在链路层忽略，不影响其它消息。
        // 入站 22275(0x5703) 的处理已存在（a0() 内 IAP2_AUTHENTICATED_WIFI_CONFIGURATION_RECEIVED）。
        // [V1.8.52] A：param24 门控 —— 车机未声明 WirelessCarPlayTransportComponent 时不强发 5702。
        //   依据：社区 CSDN 136412276（车机配 param 24 后手机才发 AP 请求）+ V1.8.51 实测
        //   （车机 SEND=0 / wifi_request=false，强发 CarPlay 类消息触发 IAP2_PEER_RESTARTED）。
        if (ControlChannel.Iap2Param24GateEnabled() && !this.P24) {
            this.a.h("IAP2_FORCE_5702_GATED reason=peer_param24_absent p24=false"
                    + " peer_advertised=" + this.l.contains(22274));
            ControlChannel.Iap2Diag("U7_FORCE_5702_GATED param24=0");
        } else {
            x(22274, new byte[0]);
            this.a.h("IAP2_FORCE_5702_SENT reason=ignore_peer_advertisement"
                    + " peer_advertised=" + this.l.contains(22274) + " payload_bytes=0");
            ControlChannel.Iap2Diag("U7_FORCE_5702 peer_advertised=" + this.l.contains(22274));
        }
        z(19980, Iap2Csm.B(0, Iap2Csm.A(strE)));
        z(19977, Iap2Csm.B(0, Iap2Csm.A(strA)));
        z(19978, Iap2Csm.B(0, Iap2Csm.A("zh")));
        z(19981, Iap2Csm.B(0, new byte[]{1}));
        if (this.G0 && R() && this.l.contains(19979)) {
            try {
                g5VarJ = this.a.j();
            } catch (RuntimeException unused) {
                g5VarJ = null;
            }
            if (g5VarJ == null) {
                c0("IAP2_DEVICE_TIME_UPDATE_SKIPPED reason=LOCAL_CLOCK_UNAVAILABLE");
            } else {
                x(19979, g5VarJ.k());
                c0("IAP2_DEVICE_TIME_UPDATE_PLANNED recovery_only=true advertised=true epoch_bytes=8 zone_bytes=2 daylight_bytes=1 values_logged=false");
            }
        }
        y();
        if (this.l.contains(19982)) {
            io.carplay.app.secured.ControlChannel.Iap2Diag("PEER_ADVERTISED_19982=true (car supports transport_notification)");
            // 【官方规范依据，2026-11-06 核实 carplay.pdf §23.2.7.2】
            //   "For CarPlay over wireless, the iAP2 client must support ... RequestAccessoryWiFiConfigurationInformation
            //    and AccessoryWiFiConfigurationInformation. The iAP2 client over Bluetooth is used **only to provide
            //    Wi-Fi credentials** and must be disconnected once the CarPlay session is established."
            //   ⇒ 蓝牙 iAP2 的全部职责就是传 WiFi 凭证（0x5702 请求 / 0x5703 下发），0x4E0E 不是无线 CarPlay 的必需步骤。
            // 本机实测：0x4E09..0x4E0D 均被 ACK，唯 0x4E0E 不 ACK 且 1.5s 后车机发 SYN 重启、last_ack 停在 4E0D。
            // 已试载荷：MAC 串 / 空 / TLV(0,uint16 组件ID) / TLV(0,uint8 通道类型) / 补发 param17，
            // 全部被拒 ⇒ 该消息在本车机上不可用，且是链路重启的唯一触发点。故不再发送，让会话存活，
            // 以便观察车机是否主动下发 0x5703（AccessoryWiFiConfigurationInformation）。
            // [V1.8.52] C（复现用，默认 false）：开关打开才发 4E0E。
            // ⚠️ 历史 5 种载荷（MAC 串/空/TLV(0,u16 组件ID)/TLV(0,u8 通道类型)/补发 param17）全部不被 ACK，
            //    且是 IAP2_PEER_RESTARTED 的唯一触发点 ⇒ 默认保持「不发」。
            if (io.carplay.app.secured.ControlChannel.Iap2Tx4e0eEnabled()) {
                x(19982, new byte[0]);
                this.a.h("IAP2_TX_4E0E_FORCED payload_bytes=0 hist_payloads_rejected=5 expect_peer_restart=true");
                io.carplay.app.secured.ControlChannel.Iap2Diag("TX_4E0E_FORCED replay_only=true");
            } else {
                this.a.h("IAP2_TRANSPORT_CHANNEL_ID_SKIPPED reason=not_required_by_carplay_spec 4E0D_acked=true wait_for_5703=true");
            }
        }
    }

    
    private void X(long j) {
        if (this.b && this.t && this.s && this.l.contains(44545) && this.j.size() < 32) {
            if ((this.y == null || this.A != 44545) && !this.j.stream().anyMatch(new w0(2))) {
                j5 j5Var = this.J0;
                c cVar = this.a;
                Objects.requireNonNull(cVar);
                l0 l0Var = new l0(2);
                l0Var.b = cVar;
                VarHandle.storeStoreFence();
                j5.c cVarD = j5Var.d(j, l0Var);
                if (cVarD != null) {
                    j5 j5Var2 = this.J0;
                    z2 z2Var = new z2();
                    z2Var.a = this;
                    z2Var.b = cVarD;
                    VarHandle.storeStoreFence();
                    if (j5Var2.b(cVarD, z2Var)) {
                        V("IAP2_POWER_UPDATE_QUEUED requested_fields=1 values_logged=false");
                    }
                }
            }
        }
    }

    private void Z(long j) throws IOException {
        int i;
        long[] jArr;
        if (R() && this.t && !this.u && this.n0 == null) {
            long j2 = this.s0;
            if (j2 >= 0) {
                int i2 = this.t0;
                long[] jArr2 = P0;
                if (i2 >= jArr2.length) {
                    return;
                }
                long j3 = j - j2;
                if (j3 < 0) {
                    return;
                }
                if (j3 > 15000) {
                    this.t0 = jArr2.length;
                    return;
                }
                if (j3 < jArr2[i2]) {
                    return;
                }
                do {
                    i = this.t0 + 1;
                    this.t0 = i;
                    jArr = P0;
                    if (i >= jArr.length) {
                        break;
                    }
                } while (j3 >= jArr[i]);
                y();
                int i3 = this.u0 + 1;
                this.u0 = i3;
                this.a.h("IAP2_CARPLAY_AVAILABILITY_REANNOUNCE attempt=" + i3 + " max=3 elapsed_ms=" + j3 + " reason=START_PARAMETERS_MISSING");
            }
        }
    }

    private void a0(Iap2Csm.c cVar) throws IOException {
        int i = cVar.a;
        this.M = i;
        this.a.h(String.format(Locale.ROOT, "IAP2_RX_%04X", Integer.valueOf(i)));
        int i2 = cVar.a;
        // [V1.8.41] EA 会话消息走独立分支，避免落入下方 MFI / 22275 判定。
        if (i2 == 59904 || i2 == 59905 || i2 == 59906 || i2 == 59907) {
            eaOnMessage(i2, cVar);
            return;
        }
        if (i2 == 7425) {
            if (this.C0 && (!this.t || !this.D0)) {
                SyntheticRefs.c("IAP2_IDENTIFICATION_OUT_OF_ORDER");
                return;
            }
            if (this.s) {
                return;
            }
            int[] iArr = {0, 1, 2, 3, 4, 5};
            for (int i3 = 0; i3 < 6; i3++) {
                if (Iap2Csm.z(Iap2Csm.v(cVar.b, iArr[i3])).isEmpty()) {
                    SyntheticRefs.c("IAP2_IDENTIFICATION_EMPTY");
                    return;
                }
            }
            byte[] bArrV = Iap2Csm.v(cVar.b, 6);
            byte[] bArrV2 = Iap2Csm.v(cVar.b, 7);
            if (bArrV.length % 2 != 0 || bArrV2.length % 2 != 0) {
                SyntheticRefs.c("IAP2_IDENTIFICATION_MESSAGE_LIST");
                return;
            }
            this.m = P(bArrV);
            this.l = P(bArrV2);
            Q("SEND", this.m);
            Q("RECEIVE", this.l);
            // 诊断：转储车机 0x1D01 的参数 ID 与 EA 协议声明，确认它是否真的提供 com.apple.carplay。
            // Dump the head unit's 0x1D01 parameter ids and EA protocol text.
            StringBuilder sbParams = new StringBuilder("IAP2_PEER_1D01_PARAMS");
            int summarized = 0;
            for (int k = 0; k <= 30; k++) {
                // 诊断探针必须容错：q() 缺失返回 null、重复保留首值。用 v() 会让探针自己抛
                // Duplicate/Missing 异常并终止整条无线引导（V1.7.76 的实际故障）。
                // 保留 v.length == 0 的条目：车机若显式给出空 SEND 列表（param 6 长度 0），
                // 过滤掉就会误判成「车机根本没声明 param 6」。
                byte[] v = Iap2Csm.q(cVar.b, k);
                long occurrences = t(cVar, k);
                if (v != null) {
                    sbParams.append(' ').append(k).append('=').append(v.length);
                    if (occurrences > 1) {
                        sbParams.append('x').append(occurrences);
                    }
                    summarized += (int) Math.max(1, occurrences);
                }
            }
            sbParams.append(" total=").append(cVar.b.size()).append(" summarized=").append(summarized);
            this.a.h(sbParams.toString());
            // 兜底诊断：转储传输组件原始字节。若 0x4E0E 仍被拒，用它反推车机期望的 ComponentID
            // （iAP2 TransportComponent 的 ComponentID 通常在前 2 字节，大端）。
            // [V1.8.42] 清单 5：把 param 20(VehicleInformation) / 21(VehicleStatus) 也纳入诊断转储。
            // 只 dump 原始字节、不解析、不由手机发送：param 20/24 按规范属于 0x1D01（车机→手机）。
            for (int ck : new int[]{15, 16, 17, 20, 21, 24}) {
                byte[] comp = Iap2Csm.q(cVar.b, ck);
                if (comp != null && comp.length > 0) {
                    StringBuilder sbHex = new StringBuilder("IAP2_PEER_TRANSPORT_HEX param=").append(ck).append(" len=").append(comp.length).append(" hex=");
                    for (byte cb : comp) {
                        sbHex.append(String.format(Locale.ROOT, "%02X", Integer.valueOf(cb & 255)));
                    }
                    this.a.h(sbHex.toString());
                }
                // param 17/24 的值本身是嵌套 TLV：子 param 0 = ComponentID（uint16 大端），
                // 子 param 1 = ComponentName。实测 Info3.6 蓝牙组件为
                //   00 06 00 00 00 01 | 00 16 00 01 "CinemoBTComponent\0" | 00 04 00 02 | 00 0A 00 03 "E0:42\0"
                // ⇒ ComponentID = 1。0x4E0E 要用它回填，否则车机不 ACK 并重启链路。
                if ((ck == 17 || ck == 24) && comp != null && comp.length >= 6) {
                    int off = 0;
                    while (off + 6 <= comp.length) {
                        int tlvLen = ((comp[off] & 255) << 8) | (comp[off + 1] & 255);
                        int tlvId = ((comp[off + 2] & 255) << 8) | (comp[off + 3] & 255);
                        if (tlvLen < 4 || off + tlvLen > comp.length) {
                            break;
                        }
                        if (tlvId == 0) {
                            int cid = ((comp[off + 4] & 255) << 8) | (comp[off + 5] & 255);
                            if (ck == 17) {
                                bleCompId = cid;
                            }
                            this.a.h("IAP2_PEER_TRANSPORT_COMPONENT_ID param=" + ck + " id=" + cid);
                            break;
                        }
                        off += tlvLen;
                    }
                }
            }
            // [V1.8.42] 清单 4：param 10 = supported_external_accessory_protocol（重复 TLV）。
            // 旧探针读的是 param 9（maximum_current_drawn_from_device, Uint16 mA），
            // 打出 "text=.." 被误判成「车机 EA 列表为空」。这里逐条 dump 原始字节，
            // 车机自己发出的格式就是它认可的格式，用它校准我们 0x1D00 的 param 10 编码。
            int eapIdx = 0;
            for (Iap2Csm.e eap : cVar.b) {
                if (eap.a != 10) {
                    continue;
                }
                byte[] eapBytes = eap.b();
                StringBuilder sbEap = new StringBuilder("IAP2_PEER_EAP_HEX idx=").append(eapIdx)
                        .append(" len=").append(eapBytes.length).append(" hex=");
                for (byte eb : eapBytes) {
                    sbEap.append(String.format(Locale.ROOT, "%02X", Integer.valueOf(eb & 255)));
                }
                this.a.h(sbEap.toString());
                StringBuilder sbEapText = new StringBuilder("IAP2_PEER_EA_PROTOCOLS idx=")
                        .append(eapIdx).append(" text=");
                for (byte eb : eapBytes) {
                    sbEapText.append((eb >= 32 && eb < 127) ? (char) eb : '.');
                }
                this.a.h(sbEapText.toString());
                eapIdx++;
            }
            if (eapIdx == 0) {
                this.a.h("IAP2_PEER_EAP_HEX none param10_absent=true");
            }
            if (this.b) {
                V("IAP2_POWER_CAPABILITIES subscribe_ae00=" + this.m.contains(44544) + " update_ae01=" + this.l.contains(44545));
            }
            if (!this.b) {
                c0("IAP2_PEER_START_DEPENDENCIES receives_4E0B=" + this.l.contains(19979) + " sends_5703=" + this.m.contains(22275));
            }
            this.a.h("IAP2_PEER_CAPABILITIES modern_start=" + this.m.contains(17153) + " modern_available=" + this.l.contains(17152) + " wifi_request=" + this.l.contains(22274) + " wireless_update=" + this.l.contains(19981) + " transport_notification=" + this.l.contains(19982));
            io.carplay.app.secured.ControlChannel.Iap2Diag("PEER_CAPS modern_start=" + this.m.contains(17153) + " modern_available=" + this.l.contains(17152) + " wifi_request=" + this.l.contains(22274) + " transport_notification(19982)=" + this.l.contains(19982) + " bt_component=" + t(cVar, 17));
            this.a.h("IAP2_PEER_TRANSPORT_COMPONENTS bluetooth=" + t(cVar, 17) + " wireless_carplay=" + t(cVar, 24) + " usb_device=" + t(cVar, 15) + " usb_host=" + t(cVar, 16));
            // [V1.8.52] A：记录车机是否声明 param 24，供 5702/4300 门控使用（t() 返回 long，0=缺失）。
            this.P24 = t(cVar, 24) != 0;
            this.a.h("IAP2_PARAM24_STATE wireless_carplay=" + this.P24
                    + " gate=" + io.carplay.app.secured.ControlChannel.Iap2Param24GateEnabled());
            // Info3.6 实测：车机既不接收 0x5702、也不发送 0x5703、也不支持 0x4301，
            // 三条同时成立 ⇒ 蓝牙 iAP2 在本机拿不到任何 WiFi 凭证，只能靠手动接入车机热点。
            // 打出可执行结论，避免继续在 iAP2 上反复试错。
            boolean wifiCredentialAbsent = !this.l.contains(22274) && !this.m.contains(22275) && !this.m.contains(17153);
            PEER_WIFI_CREDENTIAL_CHANNEL_ABSENT = wifiCredentialAbsent;
            if (wifiCredentialAbsent) {
                this.a.h("IAP2_PEER_NO_WIFI_CREDENTIAL_CHANNEL"
                        + " wifi_request=false sends_5703=false modern_start=false"
                        + " reason=PEER_CANNOT_DELIVER_WIFI_CREDENTIALS"
                        + " action=CONNECT_RECEIVER_HOTSPOT_MANUALLY"
                        + " spec=bluetooth_iap2_is_credential_only");
            }
            this.s = true;
            x(7426, new byte[0]);
            if (this.C0) {
                W();
            } else {
                this.E0 = true;
                x(43520, new byte[0]);
            }
            this.a.h("IAP2_IDENTIFIED");
            io.carplay.app.secured.ControlChannel.Iap2Diag("IDENTIFIED peer_advertised_19982=" + this.l.contains(19982)
                    + " peer_advertised_wifi_request=" + this.l.contains(22274) + " ea_drop_19982=" + io.carplay.app.secured.ControlChannel.Iap2EaDrop19982());
            // [V1.8.49] EA00 时序 A/B：默认延后到 MFi 认证完成后（见 AA03 分支 this.t=true 之后）。
            // 依据 Spec §23.2.7 / §23.2.5.1、sysdiagnose（carPlayCapable 0→1 在 MFi 之后）、
            // 固件 "Received EAPSessionInitiated callback from Cinemo"（车机被动接收、回调在认证推进之后）。
            // 开关 false ⇒ V1.8.48 旧行为（识别完成即发），用于 A/B 对照。
            if (io.carplay.app.secured.ControlChannel.Iap2Ea00AfterAuthEnabled()) {
                this.a.h("IAP2_EA_SESSION_TIMING trigger=IDENTIFIED deferred_to=POST_MFI_AUTH");
                io.carplay.app.secured.ControlChannel.Iap2Diag("EA_SESSION_TIMING=DEFERRED_TO_POST_AUTH");
            } else {
                eaRequestSession();
                io.carplay.app.secured.ControlChannel.Iap2Diag("EA_SESSION_TIMING=IDENTIFIED_IMMEDIATE");
            }
            return;
        }
        if (i2 == 43526) {
            if ((!this.s && !this.C0) || !this.E0 || this.I != null || this.t) {
                SyntheticRefs.c("MFI_SERIAL_OUT_OF_ORDER");
                return;
            }
            byte[] bArrV3 = Iap2Csm.v(cVar.b, 0);
            if (bArrV3.length == 0 || bArrV3.length > 64) {
                SyntheticRefs.c("MFI_SERIAL_SIZE");
                return;
            } else {
                if (this.x) {
                    SyntheticRefs.c("MFI_FULL_CERTIFICATE_NOT_PROVIDED");
                    return;
                }
                this.x = true;
                x(43520, new byte[0]);
                this.a.h("MFI_SERIAL_ONLY_RECEIVED_REQUESTING_FULL_CERTIFICATE");
                return;
            }
        }
        if (i2 == 43521) {
            if ((!this.s && !this.C0) || !this.E0 || this.I != null) {
                SyntheticRefs.c("MFI_CERTIFICATE_OUT_OF_ORDER");
                return;
            }
            byte[] bArrV4 = Iap2Csm.v(cVar.b, 0);
            try {
                RsaPkcs1Cipher m5VarF = RsaPkcs1Cipher.f(bArrV4);
                this.I = m5VarF;
                if (m5VarF.d()) {
                    this.a.h("MFI_CERTIFICATE_EXPIRY_COMPATIBLE fresh_proof_required=true trust_policy_unchanged=true");
                }
                String str = this.z0;
                if (str == null) {
                    str = this.e;
                }
                if (!this.I.h(str)) {
                    if (str != null || !this.A0) {
                        this.a.g(this.I.e(), this.I.a());
                        throw new IOException("MFI_PEER_TRUST_REQUIRED");
                    }
                    this.B0 = true;
                    this.a.h("MFI_FIRST_USE_FRESH_PROOF_REQUIRED_BEFORE_REMEMBERING");
                }
                x(43522, Iap2Csm.B(0, this.I.b()));
                this.a.h("MFI_FRESH_CHALLENGE_SENT_TO_PINNED_PEER");
                return;
            } catch (GeneralSecurityException e2) {
                String strQ = q(e2);
                this.a.h("MFI_CERTIFICATE_REJECTED reason=" + strQ + " encoded_bytes=" + bArrV4.length + " container=" + ((bArrV4.length <= 0 || bArrV4[0] != 48) ? (bArrV4.length <= 0 || bArrV4[0] != 45) ? "OTHER" : "TEXT_LIKE" : "ASN1_LIKE"));
                throw new IOException(ProtoMessageBase.e("MFI_CERTIFICATE_INVALID reason=", strQ), e2);
            }
        }
        if (i2 == 43523) {
            RsaPkcs1Cipher m5Var = this.I;
            if (m5Var == null || this.t) {
                SyntheticRefs.c("MFI_RESPONSE_OUT_OF_ORDER");
                return;
            }
            try {
                if (!m5Var.j(Iap2Csm.v(cVar.b, 0))) {
                    throw new IOException("MFI_CHALLENGE_SIGNATURE_FAILED");
                }
                if (this.B0) {
                    try {
                        this.a.a(this.I.e(), this.I.a());
                        this.B0 = false;
                        this.a.h("MFI_FIRST_USE_PROOF_VERIFIED_AND_REMEMBERED");
                    } catch (IOException e3) {
                        throw new IOException("MFI_FIRST_USE_CERTIFICATE_ENCODING_FAILED", e3);
                    }
                }
                this.t = true;
                this.a.h("MFI_KEY_POSSESSION_VERIFIED_PINNED_APPLE_CHAIN_UNVERIFIED");
                x(43525, new byte[0]);
                // [V1.8.49] EA00 时序 A/B（默认）：MFi 认证完成后才发起 EA 会话请求。
                // 插在 AA05 发出之后、W()/bEaPayload 之前 ⇒ 下方两个 return 分支都必经。
                // eaRequestSession() 自带 ea0 防重复；开关 false 时此处不调用（旧路径已在 IDENTIFIED 发过）。
                if (io.carplay.app.secured.ControlChannel.Iap2Ea00AfterAuthEnabled()) {
                    this.a.h("IAP2_EA_SESSION_TIMING trigger=POST_MFI_AUTH authenticated=true after_msg=AA05");
                    io.carplay.app.secured.ControlChannel.Iap2Diag("EA_SESSION_TIMING=POST_AUTH");
                    eaRequestSession();
                }
                if (!this.C0 || this.s) {
                    W();
                    return;
                } else {
                this.D0 = true;
                x(7424, bIdent());
                return;
                }
            } catch (GeneralSecurityException e4) {
                throw new IOException("MFI_CHALLENGE_SIGNATURE_FAILED", e4);
            }
        }
        if (i2 == 43524) {
            SyntheticRefs.c("MFI_PEER_AUTHENTICATION_FAILED");
            return;
        }
        int i4 = 4;
        if (i2 == 44544 || i2 == 44546) {
            if (!this.b || !this.t || !this.s) {
                V("IAP2_POWER_REQUEST_IGNORED reason=NOT_AUTHENTICATED_NETWORK");
                return;
            }
            List<Iap2Csm.e> list = cVar.b;
            if (i2 == 44546) {
                if (!list.isEmpty()) {
                    V("IAP2_POWER_REQUEST_IGNORED reason=STOP_PARAMS");
                    return;
                }
                this.J0.e();
                this.j.removeIf(new w0(i4));
                V("IAP2_POWER_SUBSCRIPTION active=false in_flight_reliability_retained=true");
                return;
            }
            j5.b bVarC = j5.c(list);
            if (bVarC == null) {
                V("IAP2_POWER_REQUEST_IGNORED reason=FLAGS");
                return;
            }
            if (!this.l.contains(44545)) {
                V("IAP2_POWER_REQUEST_IGNORED reason=UPDATE_NOT_ADVERTISED");
                return;
            }
            this.J0.f(bVarC.g());
            if (!bVarC.g()) {
                this.j.removeIf(new w0(5));
            }
            V("IAP2_POWER_SUBSCRIPTION external_charger_requested=" + bVarC.g() + " supported_fields=1 values_logged=false");
            return;
        }
        if (i2 != 17153) {
            if (i2 == 22275) {
                if (this.b) {
                    SyntheticRefs.c("IAP2_NETWORK_UNEXPECTED_WIFI_CREDENTIALS");
                    return;
                }
                if (!this.t || (this.C0 && !this.s)) {
                    SyntheticRefs.c("IAP2_WIFI_CONFIGURATION_BEFORE_AUTHENTICATION");
                    return;
                }
                Iap2Csm.g gVarD = Iap2Csm.D(cVar);
                Iap2Csm.g gVar = this.n0;
                if (gVar != null) {
                    if (gVar.d(gVarD)) {
                        return;
                    }
                    SyntheticRefs.c("IAP2_WIFI_CONFIGURATION_CHANGED_RESTART_REQUIRED");
                    return;
                }
                this.n0 = gVarD;
                this.a.h("IAP2_AUTHENTICATED_WIFI_CONFIGURATION_RECEIVED security=" + gVarD.b + " ssid_bytes=" + gVarD.a.getBytes(StandardCharsets.UTF_8).length + " passphrase_present=" + (!gVarD.c().isEmpty()));
                this.a.f(gVarD);
                return;
            }
            return;
        }
        if (this.b) {
            SyntheticRefs.c("IAP2_NETWORK_UNEXPECTED_BOOTSTRAP");
            return;
        }
        if (!this.t || (this.C0 && !this.s)) {
            SyntheticRefs.c("CARPLAY_BOOTSTRAP_BEFORE_AUTHENTICATION");
            return;
        }
        boolean z = this.u;
        if (z) {
            if (!this.a.d() || this.d >= 4) {
                return;
            }
            Iap2Csm.a aVarU = Iap2Csm.u(this.N0, Iap2Csm.y(cVar, this.n0));
            if (Iap2Csm.w(this.N0, aVarU)) {
                return;
            }
            this.d++;
            this.N0 = aVarU;
            this.a.i(aVarU);
            this.a.h("IAP2_START_ENDPOINT_UPDATE count=" + this.d + " same_authenticated_link=true values_logged=false");
            return;
        }
        if (z) {
            return;
        }
        Iap2Csm.a aVarN = Iap2Csm.n(this.N0, Iap2Csm.y(cVar, this.n0));
        this.N0 = aVarN;
        this.a.h("IAP2_BOOTSTRAP_KEY_HINT kind=" + aVarN.e + " text_length=" + aVarN.f + " trusted=false");
        if (aVarN.e()) {
            this.u = true;
            this.a.b(aVarN);
            return;
        }
        if (this.n0 == null) {
            Iap2Csm.g gVarH = aVarN.h();
            this.n0 = gVarH;
            this.a.f(gVarH);
        }
        this.a.h("IAP2_AUTHENTICATED_START_HINT addresses=" + aVarN.d().size() + " port_present=" + (aVarN.h > 0) + " identifier_present=" + (!aVarN.c.isEmpty()) + " requires_endpoint_discovery=true");
        this.a.k(aVarN);
    }

    private void b0() throws IOException {
        this.z0 = this.I.e();
        this.w0++;
        c cVar = this.a;
        cVar.h("IAP2_DETECT_RESTART attempt=1 max=1 fresh_proof_required=true deadline_extended=false retired_tx=" + j0(this.y == null ? -2 : this.A) + " retired_availability_reannouncements=" + this.u0);
        io.carplay.app.secured.ControlChannel.Iap2Diag("DETECT_RESTART retired_tx=" + j0(this.y == null ? -2 : this.A) + " ea_drop_19982=" + io.carplay.app.secured.ControlChannel.Iap2EaDrop19982());
        this.y = null;
        this.F = false;
        this.A = -1;
        this.B = 0;
        this.j.clear();
        this.k.clear();
        this.N0 = null;
        this.J0.e();
        this.i = new Iap2Csm.d();
        this.r = false;
        this.v = false;
        this.x0 = true;
        this.s = false;
        this.t = false;
        this.I = null;
        this.B0 = false;
        this.x = false;
        this.D0 = false;
        this.E0 = false;
        this.F0 = false;
        Set<Integer> set = Collections.EMPTY_SET;
        this.l = set;
        this.m = set;
        this.o = -1;
        this.p = 1;
        this.q = 4096;
        this.w = false;
        this.f0 = false;
        this.C = 1000;
        this.D = 5;
        this.s0 = -1L;
        this.t0 = 0;
        this.u0 = 0;
        m0((byte[]) Iap2Csm.a.clone());
        this.S = d0(this.S, 1);
    }

    private void c0(String str) {
        try {
            this.a.h(str);
        } catch (RuntimeException unused) {
        }
    }

    private static int d0(int i, int i2) {
        if (i > Integer.MAX_VALUE - i2) {
            return Integer.MAX_VALUE;
        }
        return i + i2;
    }

    private void e0(Iap2Csm.b bVar, long j) throws IOException {
        f0(bVar, j, -1);
    }

    private void f0(Iap2Csm.b bVar, long j, int i) throws IOException {
        g0(bVar, j, i, false);
    }

    private void g() throws IOException {
        if (this.w) {
            return;
        }
        m0(Iap2Csm.j(new Iap2Csm.b(64, (this.n - 1) & 255, this.o, 0, null)));
    }

    private void g0(Iap2Csm.b bVar, long j, int i, boolean z) throws IOException {
        byte[] bArrJ = Iap2Csm.j(bVar);
        this.y = bArrJ;
        this.z = bVar.b;
        this.A = i;
        this.F = z;
        this.E = j;
        this.B = 0;
        n0((byte[]) bArrJ.clone(), i, 0);
    }

    public static MfiCertificateVerifier i(String str, String str2, c cVar) {
        if (str == null || !str.matches("[a-fA-F0-9]{64}")) {
            SyntheticRefs.b("pinned identity required for startup order fallback");
            return null;
        }
        MfiCertificateVerifier mfiCertificateVerifier = new MfiCertificateVerifier(str, str2, false, cVar);
        mfiCertificateVerifier.C0 = true;
        return mfiCertificateVerifier;
    }

    public static MfiCertificateVerifier j(String str, String str2, c cVar) {
        MfiCertificateVerifier mfiCertificateVerifierI = i(str, str2, cVar);
        mfiCertificateVerifierI.G0 = true;
        return mfiCertificateVerifierI;
    }

    private static String j0(int i) {
        if (i == -2) {
            return "NONE";
        }
        return i < 0 ? "SYN" : String.format(Locale.ROOT, "%04X", Integer.valueOf(i));
    }

    private void k(long j) {
        if (this.s0 < 0 && R() && this.t && !this.u && this.n0 == null && this.j.stream().noneMatch(new w0(3))) {
            this.s0 = j;
        }
    }

    private void l(byte[] bArr) {
        if (this.b || this.u || this.n0 != null) {
            return;
        }
        try {
            int length = 0;
            boolean z = true;
            boolean z2 = bArr.length >= 6 && bArr[0] == 64 && bArr[1] == 64 && Iap2Csm.C(bArr, 2) == bArr.length && Iap2Csm.C(bArr, 4) == 17152;
            List<Iap2Csm.e> listR = Iap2Csm.r(Iap2Csm.v(Iap2Csm.r(Arrays.copyOfRange(bArr, 6, bArr.length)), 1));
            byte[] bArrV = Iap2Csm.v(listR, 0);
            byte[] bArrQ = Iap2Csm.q(listR, 1);
            String lowerCase = null;
            String strZ = bArrQ == null ? null : Iap2Csm.z(bArrQ);
            if (this.q0 && R()) {
                lowerCase = this.r0.toLowerCase(Locale.ROOT);
            }
            boolean z3 = z2 && bArrV.length == 1;
            int i = bArrV.length == 1 ? bArrV[0] & 255 : -1;
            if (bArrQ == null) {
                z = false;
            }
            if (bArrQ != null) {
                length = bArrQ.length;
            }
            c0("IAP2_CARPLAY_AVAILABILITY_WIRE valid=" + z3 + " group=1 available=" + i + " identifier_present=" + z + " identifier_bytes=" + length + " identifier_matches_source=" + Objects.equals(strZ, lowerCase) + " csm_bytes=" + bArr.length + " first_frame_bytes=" + (Math.min(this.q - 10, bArr.length) + 10) + " control_sid=" + this.p + " values_logged=false");
        } catch (IOException | RuntimeException unused) {
            c0("IAP2_CARPLAY_AVAILABILITY_WIRE valid=false values_logged=false");
        }
    }

    private boolean m(long j, String str) throws IOException {
        return n(j, str, true);
    }

    private void m0(byte[] bArr) throws IOException {
        this.a.write(bArr);
        this.O = d0(this.O, bArr.length);
        if (Arrays.equals(bArr, Iap2Csm.a)) {
            return;
        }
        this.Q = d0(this.Q, 1);
    }

    private boolean n(long j, String str, boolean z) throws IOException {
        this.H0 = true;
        if (z) {
            m0((byte[]) Iap2Csm.a.clone());
            this.S = d0(this.S, 1);
        }
        e0(new Iap2Csm.b(128, T(), 0, 0, new byte[]{1, 1, 16, 0, 3, -24, 0, 50, 5, 1, 1, 0, 2}), j);
        this.a.h("IAP2_INITIAL_LINK_RECOVERY initiated=true first_attempt_unchanged=true fresh_proof_required=true deadline_extended=false evidence=" + str);
        return true;
    }

    private void n0(byte[] bArr, int i, int i2) throws IOException {
        String str = "id=" + j0(i) + " seq=" + (bArr[5] & 255) + " bytes=" + bArr.length + " retry=" + i2;
        this.J = i;
        // 诊断：0x4E09..0x4E0E 是旧协议 CarPlay 握手（0x4E0D=可用通知、0x4E0E=设备传输通道 ID）。
        // 车机每次收到 4E0E 就 IAP2_PEER_RESTARTED，故这里打印首包十六进制与调用栈，定位构造点与载荷。
        // Probe the legacy 0x4E0x handshake payload and caller; the head unit restarts iAP2 right after 4E0E.
        if (i >= 19977 && i <= 19982 && i2 == 0) {
            StringBuilder probe = new StringBuilder("IAP2_TX_4E0X_PROBE id=").append(j0(i)).append(" bytes=").append(bArr.length).append(" hex=");
            int probeLen = Math.min(bArr.length, 64);
            for (int k = 0; k < probeLen; k++) {
                probe.append(String.format(Locale.ROOT, "%02X", Integer.valueOf(bArr[k] & 255)));
            }
            StackTraceElement[] st = new Throwable().getStackTrace();
            for (int k = 1; k < Math.min(st.length, 6); k++) {
                probe.append(" | ").append(st[k].getClassName()).append('.').append(st[k].getMethodName()).append(':').append(st[k].getLineNumber());
            }
            this.a.h(probe.toString());
        }
        this.a.h("IAP2_TX_BEGIN ".concat(str));
        try {
            m0(bArr);
            this.K = i;
            if (i == 17152 && i2 > 0) {
                this.H = d0(this.H, 1);
            }
            this.a.h("IAP2_TX_SENT ".concat(str));
        } catch (IOException e2) {
            this.a.h("IAP2_TX_FAILED " + str + " type=" + e2.getClass().getSimpleName());
            throw new IOException("IAP2_TX_WRITE_FAILED " + str + " type=" + e2.getClass().getSimpleName(), e2);
        }
    }

    private static String q(GeneralSecurityException generalSecurityException) {
        if (generalSecurityException instanceof CertificateExpiredException) {
            return "EXPIRED";
        }
        if (generalSecurityException instanceof CertificateNotYetValidException) {
            return "NOT_YET_VALID";
        }
        if (generalSecurityException instanceof SignatureException) {
            return "CHAIN_SIGNATURE_INVALID";
        }
        if (generalSecurityException instanceof NoSuchAlgorithmException) {
            return "ALGORITHM_UNAVAILABLE";
        }
        if (generalSecurityException instanceof InvalidKeyException) {
            return "KEY_UNSUPPORTED";
        }
        return generalSecurityException instanceof CertificateException ? "CERTIFICATE_UNREADABLE" : "VALIDATION_FAILED";
    }

    private boolean r() {
        return s(2);
    }

    private boolean s(int i) {
        int i2 = this.R;
        return i2 >= i && this.P == 0 && ((long) this.N) == ((long) i2) * 6 && ((long) this.O) == ((long) this.S) * 6 && !this.r && !this.v && !this.s && !this.t && !this.u && this.n0 == null && this.y == null && this.j.isEmpty() && !A() && !this.H0;
    }

    private static long t(Iap2Csm.c cVar, int i) {
        Stream<Iap2Csm.e> stream = cVar.b.stream();
        u uVar = new u(2);
        uVar.b = i;
        VarHandle.storeStoreFence();
        return stream.filter(uVar).count();
    }

    public static MfiCertificateVerifier u(String str, String str2, c cVar) {
        MfiCertificateVerifier mfiCertificateVerifierS = S(str, str2, cVar);
        mfiCertificateVerifierS.c = true;
        return mfiCertificateVerifierS;
    }

    /**
     * [V1.8.41] EA 设备登记入口：识别完成后向车机请求 StartExternalAccessoryProtocolSession。
     *
     * 为什么必须补（libNmeCarPlay 反汇编 + MediaManagerService 字符串）：
     *   MediaManagerService 的 addEAPMetaEntries 只在 EA 会话建立后写
     *   CINEMO_METANAME_IAP_EAP_APP_SUPPORTS_CARPLAY，并置 Wireless Carplay Device connected；
     *   这是 GMCarPlay/MediaManagerService 认为「CarPlay 已连接」的唯一上游，
     *   也是 NmeCarPlay+0x998 的 HID 池能被填充的前提。
     *   原实现只在 Identification 的能力表里声明了 59904/59905，从不真正发起会话。
     *
     * 只发一次入口请求，失败按 EA_PROTOCOLS 顺序换下一个协议（见 m1）。
     */
    private void eaRequestSession() {
        if (!io.carplay.app.secured.ControlChannel.Iap2EaSessionEnabled()) {
            this.a.h("IAP2_EA_SESSION_SKIP switch=false");
            return;
        }
        if (this.ea0) {
            return;
        }
        this.ea0 = true;
        this.ea1 = 0;
        eaTryNextProtocol();
    }

    /** [V1.8.41] 按 EA_PROTOCOLS 顺序发下一个 0xEA00 请求。 */
    private void eaTryNextProtocol() {
        if (this.ea1 >= EA_PROTOCOLS.length) {
            this.a.h("IAP2_EA_SESSION_EXHAUSTED tried=" + EA_PROTOCOLS.length + " last_status=" + this.ea3);
            io.carplay.app.secured.ControlChannel.Iap2Diag("EA_SESSION_EXHAUSTED tried=" + EA_PROTOCOLS.length);
            return;
        }
        String proto = EA_PROTOCOLS[this.ea1];
        boolean advertised = this.l.contains(Integer.valueOf(59904));
        boolean indexMode = io.carplay.app.secured.ControlChannel.Iap2Ea00IndexEnabled();
        try {
            // [V1.8.48] A/B1：EA00 请求 param 0 编码方式（单变量开关 iap2_ea00_index，默认 true）。
            //   indexMode=true  → param 0 = Uint8 ProtocolIndex（EA_PROTO_INDEX[ea1]，com.buick.vip=1）。
            //     依据：sysdiagnose 车机 EAP 字典 ACCExternalAccessoryProtocolIndex=1、MatchAction=0、
            //     SessionUsesSocketInterfaceKey=1；车机 0x1D01 param 10 是结构化 EAP 条目（子0=id）。
            //     历史 param 0 = NUL 字符串时 EA00 只有链路 ACK、0 条应用层 RESP（EA 会话从未建立）。
            //   indexMode=false → V1.8.47 旧行为（param 0 = NUL 结尾协议名字符串）。
            byte[] p0 = indexMode
                    ? new byte[]{(byte) EA_PROTO_INDEX[this.ea1]}
                    : Iap2Csm.A(proto);
            // [V1.8.51] P0-a：规范/社区双证 EA00 = param0 protocol_id(Uint8) + param1 session_id(Uint16 BE)。
            //   wiomoc iap2/control_session_message/eap.py: StartExternalAccessoryProtocolSession
            //     { protocol_id: Uint8, session_id: Uint16 }
            //   CSDN 车机侧 _iap2_process_start_eap_session(): 分别解析 kIdEap_Identifier(u8) 与
            //     kIdEap_Session_Identifier(u16)，且「session id 后续用于发送接收数据」。
            // V1.8.50 及以前只发 param 0 ⇒ 车机拿不到会话 ID，link SYN 已注册的 sid=3(EA) 从未激活。
            // 单变量开关 iap2_ea00_session_id，默认 true；显式 false 回退旧行为做 A/B。
            boolean sessionIdMode = io.carplay.app.secured.ControlChannel.Iap2Ea00SessionIdEnabled();
            byte[] ea00 = Iap2Csm.B(0, p0);
            if (sessionIdMode) {
                ea00 = Iap2Csm.f(ea00, Iap2Csm.B(1, new byte[]{0x00, 0x01}));
            }
            x(59904, ea00);
            this.a.h("IAP2_EA_SESSION_REQ idx=" + this.ea1 + " proto=" + proto
                    + " param0_mode=" + (indexMode ? ("index=" + EA_PROTO_INDEX[this.ea1]) : "string")
                    + " param0_len=" + p0.length
                    + " param1_session_id=" + (sessionIdMode ? "1" : "absent")
                    + " ea00_params=" + (sessionIdMode ? 2 : 1)
                    + " advertised=" + advertised
                    + " peer_recv_ea02=" + this.l.contains(Integer.valueOf(59906))
                    + " peer_send_ea02=" + this.m.contains(Integer.valueOf(59906))
                    + " queue=" + this.j.size());
            io.carplay.app.secured.ControlChannel.Iap2Diag("EA_SESSION_REQ idx=" + this.ea1
                    + " proto=" + proto + " param0_mode=" + (indexMode ? "index" : "string")
                    + " param1_session_id=" + (sessionIdMode ? "1" : "absent")
                    + " advertised=" + advertised);
        } catch (Throwable th) {
            this.a.h("IAP2_EA_SESSION_REQ_FAILED proto=" + proto + " type=" + th.getClass().getSimpleName());
        }
    }

    /**
     * [V1.8.41] EA 消息入站处理（0xEA00 起停应答 / 0xEA02 数据 / 0xEA03 确认）。
     * 只按 param 列表打印结构，不假设编码：真机拿到实际 param 布局后再细化语义。
     */
    private void eaOnMessage(int i, Iap2Csm.c cVar) {
        StringBuilder sb = new StringBuilder("IAP2_EA_RX id=").append(j0(i)).append(" params=");
        for (Iap2Csm.e e : cVar.b) {
            byte[] eb = e.b();
            sb.append(e.a).append('=').append(eb.length).append('B');
            if (e.a == 0 && eb.length > 0 && eb.length <= 32) {
                sb.append('[');
                for (int k = 0; k < eb.length; k++) {
                    int v = eb[k] & 255;
                    sb.append(v >= 32 && v < 127 ? (char) v : '.');
                }
                sb.append(']');
            }
            sb.append(' ');
        }
        this.a.h(sb.toString());
        if (i == 59904) {
            byte[] st = null;
            for (Iap2Csm.e e : cVar.b) {
                if (e.a == 0) {
                    st = e.b();
                    break;
                }
            }
            // 0xEA00 双向同 ID，靠 param 0 区分方向：
            //   长度 2 = status 应答（我们发出的请求的回执）
            //   其余   = 协议名字符串，即车机反过来请求本机启动某个 EA 协议
            if (st != null && st.length != 2) {
                String reqProto = new String(st, StandardCharsets.UTF_8);
                int z = reqProto.indexOf(0);
                if (z >= 0) {
                    reqProto = reqProto.substring(0, z);
                }
                this.a.h("IAP2_EA_PEER_REQUEST proto=" + reqProto);
                try {
                    x(59904, Iap2Csm.B(0, new byte[]{0, 0}));
                    this.a.h("IAP2_EA_PEER_REQUEST_ACCEPTED proto=" + reqProto);
                } catch (Throwable th) {
                    this.a.h("IAP2_EA_PEER_REQUEST_ACCEPT_FAILED type=" + th.getClass().getSimpleName());
                }
                return;
            }
            int status = (st != null && st.length >= 2) ? (((st[0] & 255) << 8) | (st[1] & 255)) : -1;
            this.ea3 = status;
            String proto = EA_PROTOCOLS[Math.min(this.ea1, EA_PROTOCOLS.length - 1)];
            this.a.h("IAP2_EA_SESSION_RESP proto=" + proto + " status=" + status);
            if (status == 0) {
                this.a.h("V1848_EA_SESSION_ESTABLISHED proto=" + proto
                        + " param0_mode=" + (io.carplay.app.secured.ControlChannel.Iap2Ea00IndexEnabled() ? "index" : "string")
                        + " ea2=" + this.ea2);
                io.carplay.app.secured.ControlChannel.Iap2Diag("EA_SESSION_ESTABLISHED proto=" + proto);
            } else {
                // 该协议被拒，换下一个（最多 EA_PROTOCOLS.length 次）。
                this.ea1 += 1;
                eaTryNextProtocol();
            }
            return;
        }
        if (i == 59905) {
            this.a.h("IAP2_EA_SESSION_STOPPED");
            return;
        }
        // [V1.8.51] P0-a 判据：规范里车机对 EA00 的应答是 0xEA03
        // StatusExternalAccessoryProtocolSession { session_id: Uint16, status: SessionStatus(OK=0, CLOSE=1) }
        // （wiomoc eap.py + Wireshark iAP2 插件 ID 表）；不是 EA00 带 status。
        // 旧实现把 59907 当 IAP2_EA_DATA 计数 ⇒ 判据整体错位。此处为纯埋点，不改重试逻辑。
        if (i == 59907) {
            byte[] sidBytes = null;
            byte[] statusBytes = null;
            for (Iap2Csm.e e : cVar.b) {
                if (e.a == 0) {
                    sidBytes = e.b();
                } else if (e.a == 1) {
                    statusBytes = e.b();
                }
            }
            int sessionId = (sidBytes != null && sidBytes.length >= 2)
                    ? (((sidBytes[0] & 255) << 8) | (sidBytes[1] & 255)) : -1;
            int status = (statusBytes != null && statusBytes.length >= 1)
                    ? (statusBytes[0] & 255) : -1;
            String protoEA03 = EA_PROTOCOLS[Math.min(this.ea1, EA_PROTOCOLS.length - 1)];
            this.a.h("IAP2_EA03_STATUS proto=" + protoEA03 + " session_id=" + sessionId
                    + " status=" + status + " param_count=" + cVar.b.size());
            if (status == 0) {
                this.a.h("V1851_EA_SESSION_ESTABLISHED proto=" + protoEA03
                        + " session_id=" + sessionId + " ea2=" + this.ea2);
                io.carplay.app.secured.ControlChannel.Iap2Diag(
                        "EA03_SESSION_ESTABLISHED proto=" + protoEA03 + " session_id=" + sessionId);
            }
            return;
        }
        this.ea2 += 1;
        if (this.ea2 <= 5 || (this.ea2 % 50) == 0) {
            this.a.h("IAP2_EA_DATA id=" + j0(i) + " count=" + this.ea2 + " params=" + cVar.b.size());
        }
    }

    private void x(int i, byte[] bArr) throws IOException {
        if (this.j.size() < 32) {
            this.j.add(new d(i, Iap2Csm.o(i, bArr)));
        } else {
            SyntheticRefs.c("IAP2_TX_QUEUE_LIMIT");
        }
    }

    /**
     * [V1.8.48] P1：CarPlayAvailability(0x4300) 的本机蓝牙标识取值（单变量开关 iap2_avail_same_source，默认 true）。
     *   sameSource=true  → ControlChannel.LocalBluetoothIdentifier()（手动填写 MAC，与 Identification
     *      0x1D00 param 3/17 同源；其内部 n.a(DeviceMac()) 已做合法性校验，返回大写合法 MAC 或 null）。
     *   sameSource=false → V1.8.47 旧行为：n.a(this.a.l())，经 BluetoothAdapterResolver.a(ctx, remoteAddr)，
     *      当手动地址为空或等于远端地址时返回 null ⇒ availability_identifier_present=false。
     */
    private String bAvailId() {
        if (io.carplay.app.secured.ControlChannel.Iap2AvailSameSourceEnabled()) {
            try {
                return ControlChannel.LocalBluetoothIdentifier();
            } catch (RuntimeException e) {
                return null;
            }
        }
        try {
            return io.carplay.app.secured.n.a(this.a.l());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void y() throws IOException {
        String str;
        // [V1.8.50] ★ P1 死代码修复：标识求值 / q0 赋值 / 判据必须放在 17152 的 early-return 之前。
        // V1.8.48 把这段放在 if (!l.contains(17152)) { … return; } 之后，而 Info3.6 的 RECEIVE 列表
        // 恒不含 17152 ⇒ 整段从未执行（identifier_source 判据 0 次、availability_identifier_present 恒 false）。
        // 移动后只恢复字段赋值与可观测性，不改变协议行为：0x4300 仍由 z() 丢弃（不强发，强发已证伪）。
        // q0 的其它引用都带 R()（车机 SEND=0 ⇒ R() 恒 false），故不会引入第二个变量。
        if (!this.p0) {
            this.p0 = true;
            // 传统无线流程（车机声明 22274 wifi_request ⇒ R()=false）同样需要本机蓝牙标识：
            // Info3.6 靠它把 _carplay-ctrl 服务与 iAP2 会话绑定。原实现只在 modern-only
            // 时取标识，实测 Buick modern_only=false ⇒ 恒 absent ⇒ 车机不下发 connect。
            // [V1.8.48] P1：同源开关决定取值来源，判据日志打 identifier_source。
            this.r0 = bAvailId();
            this.q0 = this.r0 != null;
            c cVar = this.a;
            boolean zR = R();
            boolean z = this.q0;
            if (R()) {
                str = this.q0 ? "LOWERCASE" : "ABSENT";
            } else {
                str = "UNCHANGED";
            }
            String idSource = io.carplay.app.secured.ControlChannel.Iap2AvailSameSourceEnabled()
                    ? "manual_same_as_identification" : "resolver_legacy";
            cVar.h("IAP2_CARPLAY_AVAILABILITY_PLAN modern_only=" + zR + " local_bluetooth_identifier_present=" + z
                    + " identifier_source=" + idSource + " placeholder_sent=false identifier_wire_case=" + str);
            cVar.h("V1850_P1_REACHABLE identifier_source=" + idSource
                    + " local_bt_id_present=" + z + " peer_advertised_17152=" + this.l.contains(17152));
            io.carplay.app.secured.ControlChannel.Iap2Diag("CARPLAY_AVAILABILITY_PLAN local_bt_id_present=" + z
                    + " R(modern_only)=" + zR + " identifier_source=" + idSource
                    + " identifier_case=" + str + " local_bt_id=" + (this.r0 != null ? this.r0 : "null"));
        }
        if (!this.l.contains(17152)) {
            // [V1.8.37] 车机 0x1D01 的 RECEIVE 列表通常只含 EA 消息（Info3.6: 4E09..4E0E/EA00/EA01），
            // 不含 17152(0x4300)。原实现直接 z(17152) ⇒ NOT_ADVERTISED 丢弃 ⇒ 0x4300 从未发出
            // ⇒ 车机永不发 17153(CarPlayStartSession)，waiting_start 永久 true。
            // 开关打开时绕过广告位校验直接发送，并携带本机蓝牙标识。
            // [V1.8.52] A：param24 门控 —— 车机未声明 param 24 时不发 0x4300（否则触发 PEER_RESTARTED）。
            if (io.carplay.app.secured.ControlChannel.Iap2Param24GateEnabled() && !this.P24) {
                this.a.h("IAP2_AVAILABILITY_GATED reason=peer_param24_absent p24=false wire=false");
                io.carplay.app.secured.ControlChannel.Iap2Diag("AVAILABILITY_GATED param24=0");
                return;
            }
            if (io.carplay.app.secured.ControlChannel.P1_3ForceAvailabilityEnabled()) {
                String localId = bAvailId();
                byte[] availability = Iap2Csm.B(0, new byte[]{1});
                if (localId != null) {
                    availability = Iap2Csm.f(availability,
                            Iap2Csm.B(1, Iap2Csm.A(localId.toLowerCase(Locale.ROOT))));
                }
                x(17152, Iap2Csm.B(1, availability));
                this.a.h("IAP2_FORCE_AVAILABILITY_SENT with_id=" + (localId != null));
                io.carplay.app.secured.ControlChannel.Iap2Diag(
                        "FORCE_AVAILABILITY sent=true with_id=" + (localId != null));
            } else {
                z(17152, new byte[0]);
            }
            return;
        }
        byte[] bArrB = Iap2Csm.B(0, new byte[]{1});
        if (this.q0 && R()) {
            bArrB = Iap2Csm.f(bArrB, Iap2Csm.B(1, Iap2Csm.A(this.r0.toLowerCase(Locale.ROOT))));
        }
        x(17152, Iap2Csm.B(1, bArrB));
    }

    private void z(int i, byte[] bArr) throws IOException {
        if (this.l.contains(Integer.valueOf(i))) {
            x(i, bArr);
            return;
        }
        this.a.h("IAP2_TX_SKIPPED_NOT_ADVERTISED id=" + j0(i));
    }

    
    // 构造 iAP2 Identification(0x1D00) 的 EA 消息能力列表子项（index 6=to-send, index 7=to-receive）。
    // 此前手机发 0x1D00 的 payload 是 new byte[0]（空），车机据此在 0x1D01 的 SEND 列表也声明为空，
    // 导致 R() 的 this.m.contains(17153) 永远 false、车机永不发 CarPlayStartSession(17153)，手机永久 waiting。
    // 这里声明手机支持 CarPlay：to-send 含车机期望手机发的控制/HID 类消息（其 RECEIVE 列表 0x4E09..0x4E0E/0xEA00/0xEA01），
    // to-receive 含 CarPlayStartSession(17153) 与 CarPlayAvailability(17152)，让车机在 0x1D01 的 SEND 里声明 17153。
    /**
     * [V1.8.52] B：0x1D00 StartIdentification 载荷选择（默认 false = 历史 bEaPayload()）。
     * true = 发空载荷。依据：wiomoc `StartIdentification: pass`（无字段）、TI 官方 iAP2Info 无 1D00 参数、
     * CSDN 车机侧「收到 start identification 后再回 0x1D01 携全部能力」⇒ 1D00 按规范应为空。
     * 用于验证「我方 377B 载荷是否导致车机只给最小能力集（缺 param24 / SEND=0）」。
     */
    private static byte[] bIdent() {
        if (io.carplay.app.secured.ControlChannel.Iap2IdentEmptyEnabled()) {
            ControlChannel.Log("IAP2_IDENT_PAYLOAD_MODE empty=true bytes=0");
            return new byte[0];
        }
        byte[] p = bEaPayload();
        ControlChannel.Log("IAP2_IDENT_PAYLOAD_MODE empty=false bytes=" + (p == null ? -1 : p.length));
        return p;
    }

    private static byte[] bEaPayload() {
        try {
            // ⚠️ ID 必须按十六进制换算，不要手算十进制：
            //   0x4E09..0x4E0E = 19977..19982（旧协议 CarPlay 握手，车机 param 7 声明要收这 6 条）
            //   之前误写成 19945..19950 = 0x4DE9..0x4DEE，整体差 0x20，
            //   导致车机认为 0x4E0E（传输通道 ID）未被协商 → 不 ACK 并 IAP2_PEER_RESTARTED。
            //   0x5702 = 22274（手机请求 AP 信息）、0x5703 = 22275（车机下发 AP 信息，旧协议无线 CarPlay 必需）。
            //   0xEA00/0xEA01 = 59904/59905（外部配件协议会话起停）。
            // [V1.8.41] 补 0xEA02/0xEA03（59906/59907）= EA 会话数据与数据确认。
            // 原实现只声明起停、不声明数据通道 ⇒ 即使 EA 会话建立成功也无通道可走。
            int[] toSendRaw = {19977, 19978, 19979, 19980, 19981, 19982, 22274, 59904, 59905, 59906, 59907};
            int[] toReceiveRaw = {17153, 17152, 22275, 19977, 19978, 19979, 19980, 19981, 19982, 59904, 59905, 59906, 59907};
            // [V1.8.33] P1-1 EA 变体：去掉 19982（transport_notification），验证 V1.7.67 旧疑点
            // （19982 触发车机 IAP2_PEER_RESTARTED 重启引导、使 +0x9e0 列表永远为空）。
            // 默认 false == V1.8.32 行为；置 true 仅在此次构造生效，不改其它路径。
            boolean eaDrop = io.carplay.app.secured.ControlChannel.Iap2EaDrop19982();
            int[] toSend = eaDrop ? bEaDrop(toSendRaw, 19982) : toSendRaw;
            int[] toReceive = eaDrop ? bEaDrop(toReceiveRaw, 19982) : toReceiveRaw;
            io.carplay.app.secured.ControlChannel.Iap2Diag("EA_PAYLOAD_BUILD ea_drop_19982=" + eaDrop
                    + " to_send_len=" + toSend.length + " to_receive_len=" + toReceive.length);
            // 社区抓包/文档（CSDN iAP2 Accessory Identification）显示：StartIdentification 除消息收发列表外，
            // 还应携带设备身份参数 0=Name、1=Model、2=Manufacturer、3=Serial、4=Firmware、5=Hardware、8=Language。
            // 只发 6/7 会让对端无法把本机当成一台已知 CarPlay 设备；补齐后可对照车机 0x1D01 的响应变化。
            String serial = null;
            try {
                serial = ControlChannel.LocalBluetoothIdentifier();
            } catch (RuntimeException ignored) {
            }
            if (serial == null || serial.isEmpty()) {
                serial = "CP000000000000";
            }
            String name = null;
            try {
                name = ControlChannel.ServiceName();
            } catch (RuntimeException ignored) {
            }
            if (name == null || name.isEmpty()) {
                name = "CarPlayAPK";
            }
            // 传输组件 param 17（BluetoothTransportComponent）—— 这是 0x4E0E 的引用目标。
            // 车机侧同名 param 的实际结构（Info3.6 实测，42B 嵌套 TLV）：
            //   子 0 = ComponentID(uint16 大端) | 子 1 = ComponentName | 子 2 = 空 | 子 3 = 蓝牙地址
            // 手机若从不声明自己的传输组件，0x4E0E 里引用的组件 ID 就没有归属，车机不 ACK 并重启链路
            // （已实测：发 MAC 串 / 空 payload / 裸组件 ID 三种载荷全部被拒）。
            byte[] btComponent = Iap2Csm.f(
                    Iap2Csm.B(0, new byte[]{0x00, 0x01}),
                    Iap2Csm.B(1, Iap2Csm.A(name)),
                    Iap2Csm.B(2, new byte[0]),
                    Iap2Csm.B(3, Iap2Csm.A(serial)));
            byte[] payload = Iap2Csm.f(
                    Iap2Csm.B(0, Iap2Csm.A(name)),
                    // [V1.8.43 U5] 机型统一：0x1D00 param 1 = Model Identifier，
                    // 此前是 android.os.Build.MODEL(PKJ110)，与 TXT/SETUP/param 5 的 iPhone15,3 冲突。
                    Iap2Csm.B(1, Iap2Csm.A(ControlChannel.CARPLAY_MODEL)),
                    Iap2Csm.B(2, Iap2Csm.A("Apple Inc.")),
                    Iap2Csm.B(3, Iap2Csm.A(serial)),
                    Iap2Csm.B(4, Iap2Csm.A("17.0")),
                    Iap2Csm.B(5, Iap2Csm.A(ControlChannel.CARPLAY_MODEL)),
                    Iap2Csm.B(6, bEaIds(toSend)),
                    Iap2Csm.B(7, bEaIds(toReceive)));
            // [V1.8.42] 规范勘误（wiomoc/iap2 identification.py，已由车机 0x1D01 实测验证）：
            //   param 8 = power_providing_capability(Uint8)
            //   param 9 = maximum_current_drawn_from_device(Uint16, mA)
            //   param 12 = current_language(str)
            // 历史实现把 "zh\0" 写进了 param 8。默认保留历史行为，由 iap2_ident_strict 单变量 A/B。
            boolean identStrict = io.carplay.app.secured.ControlChannel.Iap2IdentStrictEnabled();
            if (identStrict) {
                payload = Iap2Csm.f(payload,
                        Iap2Csm.B(8, new byte[]{0x00}),
                        Iap2Csm.B(9, new byte[]{0x00, 0x00}));
            } else {
                payload = Iap2Csm.f(payload, Iap2Csm.B(8, Iap2Csm.A("zh")));
            }
            // [V1.8.42] param 10 = supported_external_accessory_protocol（重复 TLV）。
            // V1.8.41 曾把 4 个 NUL 协议串写进 param 9（Uint16 mA），属字段污染，已删除。
            // 每个条目本身也是嵌套 TLV，结构依据 ExternalAccessoryProtocol：
            //   子0 id(Uint8) | 子1 name(NUL str) | 子2 match_action(Uint8) | 子3 native_transport_id(Uint16)
            int eapCount = 0;
            if (io.carplay.app.secured.ControlChannel.Iap2EaParam10Enabled()) {
                for (int idx = 0; idx < EA_PROTOCOLS.length; idx++) {
                    byte[] eap = Iap2Csm.f(
                            Iap2Csm.B(0, new byte[]{(byte) EA_PROTO_INDEX[idx]}),
                            Iap2Csm.B(1, Iap2Csm.A(EA_PROTOCOLS[idx])),
                            Iap2Csm.B(2, new byte[]{0x00}),
                            Iap2Csm.B(3, new byte[]{0x00, 0x01}));
                    payload = Iap2Csm.f(payload, Iap2Csm.B(10, eap));
                    eapCount++;
                }
            }
            if (identStrict) {
                payload = Iap2Csm.f(payload, Iap2Csm.B(12, Iap2Csm.A("zh")));
            }
            payload = Iap2Csm.f(payload, Iap2Csm.B(17, btComponent));
            StringBuilder sb = new StringBuilder("IAP2_IDENTIFICATION_EA to_send=");
            for (int i2 : toSend) {
                sb.append(i2).append(',');
            }
            sb.append(" to_receive=");
            for (int i3 : toReceive) {
                sb.append(i3).append(',');
            }
            sb.append(" bytes=").append(payload.length).append(" transport17_bytes=").append(btComponent.length).append(" transport17_id=1");
            // [V1.8.42] 不再打印 ea_proto_list（param 9 污染已删除）。
            // eap_index_assumed=true 提醒：EA_PROTO_INDEX 里两个索引是推断值，需用 IAP2_PEER_EAP_HEX 校准。
            sb.append(" ea_param10_count=").append(eapCount)
                    .append(" ident_strict=").append(identStrict)
                    .append(" eap_index_assumed=true");
            // 注意：SyntheticRefs.c() 是 throw new IOException(...) 的占位实现，不是日志函数。
            // 用它打点会被本方法的 catch(IOException) 吞掉并返回空 payload，等于修复从未生效。
            // 这里必须用 ControlChannel.Log（public static，写 logcat tag CarPlayAPK）。
            ControlChannel.Log(sb.toString());
            return payload;
        } catch (IOException unused) {
            return new byte[0];
        }
    }

    private static byte[] bEaIds(int[] iArr) {
        byte[] bArr = new byte[iArr.length << 1];
        for (int i = 0; i < iArr.length; i++) {
            bArr[i << 1] = (byte) (iArr[i] >> 8);
            bArr[(i << 1) + 1] = (byte) iArr[i];
        }
        return bArr;
    }

    /** [V1.8.33] P1-1 辅助：从能力列表剔除指定消息 ID（仅用于 EA 变体测试，不影响其它路径）。 */
    private static int[] bEaDrop(int[] iArr, int id) {
        int c = 0;
        for (int v : iArr) {
            if (v != id) {
                c++;
            }
        }
        int[] out = new int[c];
        int k = 0;
        for (int v : iArr) {
            if (v != id) {
                out[k++] = v;
            }
        }
        return out;
    }

    public boolean A() {
        return (this.h.d() == 0 && this.i.d() == 0) ? false : true;
    }

    public String C() {
        String str;
        int i = this.p;
        int i2 = this.T;
        int i3 = this.U;
        int i4 = this.V;
        int i5 = this.Y;
        int i6 = this.Z;
        int i7 = this.rxLastAck;
        int i8 = this.b0;
        int i9 = this.c0;
        String str2 = this.d0;
        String str3 = this.e0;
        int iD = this.h.d();
        int iB = this.h.b();
        boolean z = this.f0;
        if (z) {
            str = " lsp_window=" + this.g0 + " lsp_packet=" + this.h0 + " lsp_retry_ms=" + this.i0 + " lsp_ack_ms=" + this.j0 + " lsp_retries=" + this.k0 + " lsp_cumulative=" + this.l0 + " lsp_sessions=" + this.m0;
        } else {
            str = "";
        }
        return "IAP2_INPUT control_sid=" + i + " rx_data_frames=" + i2 + " rx_duplicate_frames=" + i3 + " rx_other_session_frames=" + i4 + " rx_last_ctl=" + i5 + " rx_last_seq=" + i6 + " rx_last_ack=" + i7 + " rx_last_sid=" + i8 + " rx_last_payload_bytes=" + i9 + " rx_last_route=" + str2 + " rx_parse_failure=" + str3 + " link_pending_bytes=" + iD + " link_expected_bytes=" + iB + " lsp_seen=" + z + str + " csm_pending_bytes=" + this.i.d() + " csm_expected_bytes=" + this.i.b() + " rx_diagnostic_suppressed=" + this.X;
    }

    public InputFlags E() {
        if (this.b || this.N != 0 || this.O != 0 || this.r || this.v || this.y != null || !this.j.isEmpty() || A() || this.H0) {
            return null;
        }
        return InputFlags.NO_PROTOCOL_INPUT;
    }

    public boolean F(long j) throws IOException {
        return G(j, true);
    }

    public boolean G(long j, boolean z) throws IOException {
        if (!this.c) {
            return false;
        }
        if (this.S == 0) {
            return m(j, "CONTROL_POST_DETECT_ONLY");
        }
        if (r()) {
            return m(j, "CONTROL_POST_DETECT_ONLY");
        }
        if (z && s(1)) {
            long j2 = this.I0;
            if (j2 >= 0 && j >= j2 && j - j2 >= this.C) {
                return n(j, "CONTROL_POST_SINGLE_DETECT", false);
            }
        }
        return false;
    }

    public boolean H(long j) throws IOException {
        if (v() == null) {
            return false;
        }
        return m(j, "DETECT_ONLY");
    }

    public boolean I(long j) throws IOException {
        if (E() == null) {
            return false;
        }
        return m(j, "NO_INPUT");
    }

    public void Y(byte[] bArr, int i, int i2, long j) throws IOException {
        f next;
        if (i2 >= 0 && i >= 0 && i <= bArr.length - i2) {
            this.N = d0(this.N, i2);
        }
        B("IAP2_RX_READ bytes=" + i2 + " link_pending_before=" + this.h.d() + " csm_pending_before=" + this.i.d());
        try {
            List<Iap2Csm.b> listC = this.h.c(bArr, i, i2);
            B("IAP2_RX_ASSEMBLY layer=LINK decoded_frames=" + listC.size() + " pending_bytes=" + this.h.d() + " expected_bytes=" + this.h.b());
            for (Iap2Csm.b bVar : listC) {
                if (bVar == null) {
                    this.R = d0(this.R, 1);
                    if (this.c && this.I0 < 0) {
                        this.I0 = j;
                    }
                    boolean z = this.r;
                    if (z) {
                        int i3 = this.v0;
                        if (i3 < 2) {
                            this.v0 = i3 + 1;
                        }
                        if (this.v0 == 1) {
                            c cVar = this.a;
                            boolean z2 = this.t;
                            boolean z3 = !this.u && this.n0 == null;
                            long j2 = this.s0;
                            cVar.h("IAP2_DETECT_AFTER_LINK authenticated=" + z2 + " waiting_start=" + z3 + " since_availability_ack_ms=" + (j2 < 0 ? -1L : Math.max(0L, j - j2)));
                        }
                        if (this.v0 >= 2 && R() && this.t && !this.u && this.n0 == null && this.s0 >= 0 && (this.y == null || this.A == 17152)) {
                            if (this.w0 == 0) {
                                b0();
                            } else if (!this.y0) {
                                this.y0 = true;
                                this.a.h("IAP2_DETECT_RESTART_LIMIT max=1");
                            }
                        }
                    } else if (!this.x0 && !z && !this.v) {
                        m0((byte[]) Iap2Csm.a.clone());
                        this.S = d0(this.S, 1);
                    }
                } else {
                    this.P = d0(this.P, 1);
                    byte[] bArrB = bVar.b();
                    int i4 = bVar.a;
                    this.Y = i4;
                    int i5 = bVar.b;
                    this.Z = i5;
                    int i6 = bVar.c;
                    this.rxLastAck = i6;
                    int i7 = bVar.d;
                    this.b0 = i7;
                    this.c0 = bArrB == null ? 0 : bArrB.length;
                    B("IAP2_RX_FRAME ctl=" + i4 + " seq=" + i5 + " ack=" + i6 + " sid=" + i7 + " payload_bytes=" + (bArrB == null ? 0 : bArrB.length));
                    if ((bVar.a & 128) == 0 && bArrB != null && bArrB.length > 0) {
                        this.T = d0(this.T, 1);
                    }
                    int i8 = bVar.a;
                    if ((i8 & 16) != 0) {
                        SyntheticRefs.c("IAP2_PEER_RESET");
                        return;
                    }
                    if ((i8 & 32) != 0) {
                        SyntheticRefs.c("IAP2_EXTENDED_ACK_RECOVERY_REQUIRED");
                        return;
                    }
                    if (this.x0 && (i8 & 128) == 0) {
                        D("RETIRED_UNTIL_SYN");
                    } else {
                        this.v0 = 0;
                        if ((i8 & 128) != 0) {
                            this.M = -1;
                            Iterator<f> it = this.k.iterator();
                            while (true) {
                                if (!it.hasNext()) {
                                    next = null;
                                    break;
                                }
                                next = it.next();
                                if (next.h() == bVar.a && next.k() == bVar.b && next.g() == bVar.c && next.l() == bVar.d && Arrays.equals(next.i(), bArrB)) {
                                    break;
                                }
                            }
                            if (next != null) {
                                D("DUPLICATE_SYN");
                                this.a.h("IAP2_DUPLICATE_SYN_REPLY peer_seq=" + bVar.b);
                                n0((byte[]) next.j().clone(), -1, 0);
                            }
                        }
                        boolean z4 = this.H0 && !this.x0 && !this.r && !this.v && this.k.isEmpty() && bVar.a == 192 && this.y != null && this.A == -1 && bVar.c == this.z;
                        if ((bVar.a & 64) != 0 && this.y != null && bVar.c == this.z) {
                            int i9 = this.A;
                            this.L = i9;
                            this.a.h("IAP2_TX_ACK id=" + j0(i9) + " seq=" + this.z);
                            if (this.L == 17152 && this.F) {
                                this.G = d0(this.G, 1);
                            }
                            this.y = null;
                            this.F = false;
                            this.B = 0;
                            if (this.L == 17152) {
                                k(j);
                            }
                            if (this.L == 17152 && this.t && !this.u && this.n0 == null && R()) {
                                this.a.h("IAP2_AUTHENTICATED_WAITING_CARPLAY_START");
                            }
                        }
                        int i10 = bVar.a;
                        if ((i10 & 128) != 0) {
                            D("LINK_SYN");
                            if (this.r) {
                                SyntheticRefs.c("IAP2_PEER_RESTARTED");
                                return;
                            }
                            if (this.k.size() >= 8) {
                                SyntheticRefs.c("IAP2_LINK_NEGOTIATION_LIMIT");
                                return;
                            }
                            if (this.H0 && !this.x0 && !this.v && this.k.isEmpty() && bVar.a == 192 && !z4) {
                                SyntheticRefs.c("IAP2_INITIAL_SYN_ACK_MISMATCH");
                                return;
                            }
                            U(bArrB);
                            this.x0 = false;
                            int i11 = bVar.b;
                            this.o = i11;
                            if (z4) {
                                byte[] bArrJ = Iap2Csm.j(new Iap2Csm.b(64, (this.n - 1) & 255, i11, 0, null));
                                m0((byte[]) bArrJ.clone());
                                this.k.add(new f(bVar.a, bVar.b, bVar.c, bVar.d, (byte[]) bArrB.clone(), bArrJ, 0));
                                this.r = true;
                                this.a.h("IAP2_LINK_READY");
                                this.D0 = true;
                                this.E0 = false;
                                this.a.h("IAP2_STARTUP_ORDER mode=IDENTIFICATION_FIRST");
                                x(7424, bIdent());
                                i0(j);
                            } else {
                                byte[] bArr2 = (byte[]) bArrB.clone();
                                bArr2[1] = 1;
                                bArr2[2] = 16;
                                bArr2[3] = 0;
                                bArr2[9] = (byte) (!this.w ? 1 : 0);
                                e0(new Iap2Csm.b(192, T(), this.o, 0, bArr2), j);
                                this.k.add(new f(bVar.a, bVar.b, bVar.c, bVar.d, (byte[]) bArrB.clone(), (byte[]) this.y.clone(), 0));
                                this.v = true;
                                this.a.h("IAP2_LINK_NEGOTIATING");
                            }
                        } else {
                            if (this.v) {
                                if (i10 == 64 && this.y == null) {
                                    this.v = false;
                                    this.r = true;
                                    this.a.h("IAP2_LINK_READY");
                                    boolean z5 = this.C0;
                                    this.D0 = !z5;
                                    this.E0 = z5;
                                    this.a.h("IAP2_STARTUP_ORDER mode=".concat(z5 ? "AUTHENTICATION_FIRST" : "IDENTIFICATION_FIRST"));
                                    if (this.C0) {
                                        x(43520, new byte[0]);
                                    } else {
                                        x(7424, bIdent());
                                    }
                                } else {
                                    D("FINAL_ACK_PENDING");
                                }
                            }
                            if (!this.r) {
                                D("LINK_NOT_READY");
                            } else if (bArrB == null || bArrB.length <= 0) {
                                D("ACK_ONLY");
                                i0(j);
                            } else {
                                int i12 = bVar.b;
                                int i13 = this.o;
                                if (i12 == i13) {
                                    this.U = d0(this.U, 1);
                                    D("DUPLICATE_DATA");
                                    g();
                                } else {
                                    if (i12 != ((i13 + 1) & 255)) {
                                        D("OUT_OF_ORDER");
                                        SyntheticRefs.c("IAP2_OUT_OF_ORDER_PACKET");
                                        return;
                                    }
                                    if (bArrB.length + 10 > 4096) {
                                        SyntheticRefs.c("IAP2_PEER_EXCEEDED_RECEIVE_LIMIT");
                                        return;
                                    }
                                    this.o = i12;
                                    g();
                                    if (bVar.d != this.p) {
                                        this.V = d0(this.V, 1);
                                        D(ProtoMessageBase.d("OTHER_SESSION expected_sid=", this.p));
                                    } else {
                                        D(ProtoMessageBase.d("CONTROL_ASSEMBLY csm_pending_before=", this.i.d()));
                                        try {
                                            List<Iap2Csm.c> listC2 = this.i.c(bArrB);
                                            B("IAP2_RX_ASSEMBLY layer=CSM decoded_messages=" + listC2.size() + " pending_bytes=" + this.i.d() + " expected_bytes=" + this.i.b());
                                            Iterator<Iap2Csm.c> it2 = listC2.iterator();
                                            while (it2.hasNext()) {
                                                a0(it2.next());
                                            }
                                            i0(j);
                                        } catch (IOException e2) {
                                            this.e0 = "CSM";
                                            B("IAP2_RX_PARSE_FAILED layer=CSM type=IOException");
                                            throw e2;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e3) {
            this.e0 = "LINK";
            B("IAP2_RX_PARSE_FAILED layer=LINK type=IOException");
            throw e3;
        }
    }

    public boolean h() {
        return this.t;
    }

    public e h0() {
        if (!this.C0 && R() && this.r && this.s && this.t && this.F0 && this.I != null && !this.u && this.n0 == null && this.q0 && this.s0 >= 0 && this.G >= 1 && this.y == null && this.j.isEmpty() && !this.x0 && !A()) {
            return new e(this.I.e());
        }
        return null;
    }

    public void i0(long j) throws IOException {
        byte[] bArr = this.y;
        if (bArr != null) {
            if (j - this.E >= this.C) {
                int i = this.B + 1;
                this.B = i;
                if (i > this.D) {
                    SyntheticRefs.c(ProtoMessageBase.e("IAP2_ACK_TIMEOUT_", j0(this.A)));
                    return;
                } else {
                    n0((byte[]) bArr.clone(), this.A, this.B);
                    this.E = j;
                    return;
                }
            }
            return;
        }
        if (this.r) {
            X(j);
            if (this.j.isEmpty()) {
                Z(j);
            }
            if (this.j.isEmpty()) {
                return;
            }
            d dVarPeek = this.j.peek();
            int i2 = this.q - 10;
            if (i2 < 1) {
                SyntheticRefs.c("IAP2_TX_LIMIT");
                return;
            }
            if (dVarPeek.a == 17152 && dVarPeek.c == 0) {
                l(dVarPeek.b);
            }
            int iMin = Math.min(dVarPeek.b.length, dVarPeek.c + i2);
            byte[] bArrCopyOfRange = Arrays.copyOfRange(dVarPeek.b, dVarPeek.c, iMin);
            dVarPeek.c = iMin;
            if (iMin == dVarPeek.b.length) {
                this.j.remove();
            }
            Iap2Csm.b bVar = new Iap2Csm.b(64, T(), this.o, this.p, bArrCopyOfRange);
            if (!this.w) {
                g0(bVar, j, dVarPeek.a, iMin == dVarPeek.b.length);
                return;
            }
            n0(Iap2Csm.j(bVar), dVarPeek.a, 0);
            if (dVarPeek.a == 17152) {
                if (iMin == dVarPeek.b.length) {
                    this.G = d0(this.G, 1);
                }
                k(j);
            }
        }
    }

    public boolean k0() {
        return this.n0 != null;
    }

    public void l0() throws IOException {
        if (!this.t || this.n0 == null || this.o0 || this.u) {
            return;
        }
        this.o0 = true;
        this.a.h("IAP2_WIFI_TRANSPORT_READY_REQUESTING_CARPLAY_ENDPOINT");
        z(19981, Iap2Csm.B(0, new byte[]{1}));
        y();
    }

    public boolean o() {
        return this.u;
    }

    public void p() {
        this.J0.a();
    }

    public DetectionState v() {
        if (this.b || !r()) {
            return null;
        }
        return DetectionState.COMPLETE_DETECTION_WITHOUT_LINK;
    }

    
    
    public String w() {
        String str;
        boolean z;
        String str2;
        boolean z2 = this.t;
        String strJ0 = j0(this.J);
        String strJ1 = j0(this.K);
        String strJ2 = j0(this.L);
        String strJ3 = j0(this.M);
        int i = this.N;
        int i2 = this.O;
        int i3 = this.P;
        int i4 = this.Q;
        int i5 = this.R;
        int i6 = this.S;
        if (this.u || (this.b && this.t)) {
            str = "NONE";
        } else if (this.n0 != null) {
            str = "WIFI_OR_CARPLAY_START";
        } else if (this.t) {
            str = "CARPLAY_START_SESSION";
        } else if (this.s) {
            str = "ACCESSORY_AUTHENTICATION";
        } else {
            str = this.r ? "IDENTIFICATION" : "LINK";
        }
        boolean zR = R();
        boolean z3 = this.q0 && R();
        int i7 = this.u0;
        int i8 = this.w0;
        boolean z4 = this.x0;
        int i9 = this.G;
        int i10 = this.H;
        
        boolean z5 = this.A == 17152;
        if (this.y != null) {
            z = z3;
            if (this.C0) {
                str2 = "AUTH_FIRST";
            } else {
                str2 = "IDENT_FIRST";
            }
            return "IAP2_FINAL authenticated=" + z2 + " last_tx=" + strJ0 + " last_sent=" + strJ1 + " last_ack=" + strJ2 + " last_rx=" + strJ3 + " rx_bytes=" + i + " tx_bytes=" + i2 + " rx_frames=" + i3 + " tx_frames=" + i4 + " rx_detect=" + i5 + " tx_detect=" + i6 + " waiting=" + str + " modern_only=" + zR + " availability_identifier_present=" + z + " availability_reannouncements=" + i7 + " link_restarts=" + i8 + " awaiting_restart_syn=" + z4 + " availability_complete_deliveries=" + i9 + " availability_link_retries=" + i10 + " start_ack_pending=" + z5 + " startup_order=" + str2;
        }
        z = z3;
        if (this.C0) {
            str2 = "AUTH_FIRST";
        } else {
            str2 = "IDENT_FIRST";
        }
        return "IAP2_FINAL authenticated=" + z2 + " last_tx=" + strJ0 + " last_sent=" + strJ1 + " last_ack=" + strJ2 + " last_rx=" + strJ3 + " rx_bytes=" + i + " tx_bytes=" + i2 + " rx_frames=" + i3 + " tx_frames=" + i4 + " rx_detect=" + i5 + " tx_detect=" + i6 + " waiting=" + str + " modern_only=" + zR + " availability_identifier_present=" + z + " availability_reannouncements=" + i7 + " link_restarts=" + i8 + " awaiting_restart_syn=" + z4 + " availability_complete_deliveries=" + i9 + " availability_link_retries=" + i10 + " start_ack_pending=" + z5 + " startup_order=" + str2;
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class f extends ProtoMessageBase {
        private final int a;
        private final int b;
        private final int c;
        private final int d;
        private final byte[] e;
        private final byte[] f;

        private f(int i, int i2, int i3, int i4, byte[] bArr, byte[] bArr2) {
            this.a = i;
            this.b = i2;
            this.c = i3;
            this.d = i4;
            this.e = bArr;
            this.f = bArr2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof f)) {
                return false;
            }
            f fVar = (f) obj;
            return this.a == fVar.a && this.b == fVar.b && this.c == fVar.c && this.d == fVar.d && Objects.equals(this.e, fVar.e) && Objects.equals(this.f, fVar.f);
        }

        public int g() {
            return this.c;
        }

        public int h() {
            return this.a;
        }

        public final int hashCode() {
            int i = this.a;
            int i2 = this.b;
            int i3 = this.c;
            int i4 = this.d;
            byte[] bArr = this.e;
            byte[] bArr2 = this.f;
            return Objects.hashCode(bArr2) + ((Objects.hashCode(bArr) + (((((((i * 31) + i2) * 31) + i3) * 31) + i4) * 31)) * 31);
        }

        public byte[] i() {
            return this.e;
        }

        public byte[] j() {
            return this.f;
        }

        public int k() {
            return this.b;
        }

        public int l() {
            return this.d;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c), Integer.valueOf(this.d), this.e, this.f};
            String[] strArrSplit = "a;b;c;d;e;f".length() == 0 ? new String[0] : "a;b;c;d;e;f".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(f.class, sb, "[");
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

        public  f(int i, int i2, int i3, int i4, byte[] bArr, byte[] bArr2, int i5) {
            this(i, i2, i3, i4, bArr, bArr2);
        }
    }

    public MfiCertificateVerifier(String str, String str2, c cVar) {
        this(str, str2, false, cVar);
    }
}
