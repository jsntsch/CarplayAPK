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

/* JADX INFO: loaded from: classes.dex */
/**
 * MfiCertificateVerifier
 *
 * <p>原混淆名：{@code io.carplay.app.secured.a3}（jadx 产物 a3.java）
 *
 * <p>命名依据：MFI_FIRST_USE_TRUST_STORAGE_UNAVAILABLE / invalid verified leaf
 */
public final class MfiCertificateVerifier {
    private static final int O0 = 128;
    private static final long[] P0 = {2000, 6000, 14000};
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
    private m5 I;
    private long I0;
    private int J;
    private final j5 J0;
    private int K;
    private int K0;
    private int L;
    private boolean L0;
    private int M;
    private i5 M0;
    private int N;
    private Iap2Csm.a N0;
    private int O;
    private int P;
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
    private final c a;
    private int a0;
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
    private final Iap2Csm.f h;
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

    public enum DetectionState {
        COMPLETE_DETECTION_WITHOUT_LINK
    }

    public enum InputFlags {
        NO_PROTOCOL_INPUT
    }

    public interface c {
        default void a(String str, byte[] bArr) throws IOException {
            throw new IOException("MFI_FIRST_USE_TRUST_STORAGE_UNAVAILABLE");
        }

        void b(Iap2Csm.a aVar) throws IOException;

        default i5 c() throws IOException {
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

        default g5 j() {
            return g5.j();
        }

        default void k(Iap2Csm.a aVar) {
        }

        default String l() {
            return null;
        }

        void write(byte[] bArr) throws IOException;
    }

    public static final class d {
        final int a;
        final byte[] b;
        int c;

        public d(int i, byte[] bArr) {
            this.a = i;
            this.b = bArr;
        }
    }

    public static final class e extends w1 {
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

    public MfiCertificateVerifier(String str, String str2, boolean z, c cVar) {
        this.h = new Iap2Csm.f();
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
        this.a0 = -1;
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
        B(w1.e("IAP2_RX_ROUTE decision=", str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean J(d dVar) {
        return dVar.a == 17152;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean K(int i, Iap2Csm.e eVar) {
        return eVar.a == i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean L(d dVar) {
        return dVar.a == 44545;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void M(j5.c cVar) throws IOException {
        x(44545, Iap2Csm.B(4, new byte[]{cVar.g() ? (byte) 1 : (byte) 0}));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean N(d dVar) {
        return dVar.a == 44545 && dVar.c == 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean O(d dVar) {
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

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v0, types: [java.lang.StringBuilder] */
    /* JADX WARN: Type inference failed for: r2v3, types: [java.lang.String] */
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
            // smali 在循环结束后把同一寄存器复用给 String：空数组时为常量 "NONE"，
            // 否则为 StringBuilder 的字符串值；随后以 StringBuilder.append(String) 拼接。
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
                        if (bArr[i7 + 1] == 0) {
                            if (i6 != -1) {
                                SyntheticRefs.c("IAP2_MULTIPLE_CONTROL_SESSIONS");
                                return;
                            }
                            i6 = bArr[i7] & 255;
                            if (i6 != 0) {
                                byte b2 = bArr[i7 + 2];
                                if ((b2 & 255) < 1 || (b2 & 255) > 2) {
                                }
                            }
                            SyntheticRefs.c("IAP2_CONTROL_SESSION_INVALID");
                            return;
                        }
                    }
                    if (i6 < 0) {
                        SyntheticRefs.c("IAP2_CONTROL_SESSION_MISSING");
                        return;
                    }
                    this.p = i6;
                    B(w1.d("IAP2_LSP_SELECTED_CONTROL sid=", i6));
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
        g5 g5VarJ;
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
        z(22274, new byte[0]);
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
            this.a.h("IAP2_LOCAL_BLUETOOTH_IDENTIFIER_NOT_PROVIDED_NO_PLACEHOLDER_SENT");
        }
    }

    /* JADX WARN: Type inference fix 'apply assigned field type' failed
    java.lang.UnsupportedOperationException: ArgType.getObject(), call class: class jadx.core.dex.instructions.args.ArgType$PrimitiveArg
    	at jadx.core.dex.instructions.args.ArgType.getObject(ArgType.java:596)
    	at jadx.core.dex.attributes.nodes.ClassTypeVarsAttr.getTypeVarsMapFor(ClassTypeVarsAttr.java:35)
    	at jadx.core.dex.nodes.utils.TypeUtils.replaceClassGenerics(TypeUtils.java:177)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.insertExplicitUseCast(FixTypesVisitor.java:397)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryFieldTypeWithNewCasts(FixTypesVisitor.java:359)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.applyFieldType(FixTypesVisitor.java:309)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
     */
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
            if (this.b) {
                V("IAP2_POWER_CAPABILITIES subscribe_ae00=" + this.m.contains(44544) + " update_ae01=" + this.l.contains(44545));
            }
            if (!this.b) {
                c0("IAP2_PEER_START_DEPENDENCIES receives_4E0B=" + this.l.contains(19979) + " sends_5703=" + this.m.contains(22275));
            }
            this.a.h("IAP2_PEER_CAPABILITIES modern_start=" + this.m.contains(17153) + " modern_available=" + this.l.contains(17152) + " wifi_request=" + this.l.contains(22274) + " wireless_update=" + this.l.contains(19981) + " transport_notification=" + this.l.contains(19982));
            this.a.h("IAP2_PEER_TRANSPORT_COMPONENTS bluetooth=" + t(cVar, 17) + " wireless_carplay=" + t(cVar, 24) + " usb_device=" + t(cVar, 15) + " usb_host=" + t(cVar, 16));
            this.s = true;
            x(7426, new byte[0]);
            if (this.C0) {
                W();
            } else {
                this.E0 = true;
                x(43520, new byte[0]);
            }
            this.a.h("IAP2_IDENTIFIED");
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
                m5 m5VarF = m5.f(bArrV4);
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
                throw new IOException(w1.e("MFI_CERTIFICATE_INVALID reason=", strQ), e2);
            }
        }
        if (i2 == 43523) {
            m5 m5Var = this.I;
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
                if (!this.C0 || this.s) {
                    W();
                    return;
                } else {
                    this.D0 = true;
                    x(7424, new byte[0]);
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

    private void x(int i, byte[] bArr) throws IOException {
        if (this.j.size() < 32) {
            this.j.add(new d(i, Iap2Csm.o(i, bArr)));
        } else {
            SyntheticRefs.c("IAP2_TX_QUEUE_LIMIT");
        }
    }

    private void y() throws IOException {
        String str;
        if (!this.l.contains(17152)) {
            z(17152, new byte[0]);
            return;
        }
        if (!this.p0) {
            this.p0 = true;
            if (R()) {
                try {
                    this.r0 = io.carplay.app.secured.n.a(this.a.l());
                } catch (RuntimeException unused) {
                    this.r0 = null;
                }
            }
            this.q0 = this.r0 != null;
            c cVar = this.a;
            boolean zR = R();
            boolean z = this.q0;
            if (R()) {
                str = this.q0 ? "LOWERCASE" : "ABSENT";
            } else {
                str = "UNCHANGED";
            }
            cVar.h("IAP2_CARPLAY_AVAILABILITY_PLAN modern_only=" + zR + " local_bluetooth_identifier_present=" + z + " placeholder_sent=false identifier_wire_case=" + str);
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
        int i7 = this.a0;
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
                    this.a0 = i6;
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
                                x(7424, new byte[0]);
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
                                    x(this.C0 ? 43520 : 7424, new byte[0]);
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
                                        D(w1.d("OTHER_SESSION expected_sid=", this.p));
                                    } else {
                                        D(w1.d("CONTROL_ASSEMBLY csm_pending_before=", this.i.d()));
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
                    SyntheticRefs.c(w1.e("IAP2_ACK_TIMEOUT_", j0(this.A)));
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

    /* JADX WARN: Code duplicated, block: B:38:0x0094  */
    /* JADX WARN: Code duplicated, block: B:39:0x0097  */
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
        // smali: iget v15,A; const/16 0x4300; if-ne -> false，否则 true，汇合点只求值一次
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

    public static final class f extends w1 {
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
            w1.f(f.class, sb, "[");
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

        public /* synthetic */ f(int i, int i2, int i3, int i4, byte[] bArr, byte[] bArr2, int i5) {
            this(i, i2, i3, i4, bArr, bArr2);
        }
    }

    public MfiCertificateVerifier(String str, String str2, c cVar) {
        this(str, str2, false, cVar);
    }
}
