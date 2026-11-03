package io.carplay.app.secured;

import java.lang.invoke.VarHandle;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public final class v1 implements AutoCloseable {
    private boolean A;
    private final long a;
    private final io.carplay.app.secured.b b;
    private final s5 c;
    private final boolean d;
    private final g e;
    private final f6.a f;
    private final l4 g;
    private f6 k;
    private o l;
    private long m;
    private long n;
    private boolean o;
    private boolean p;
    private boolean q;
    private boolean r;
    private boolean s;
    private boolean t;
    private boolean u;
    private boolean v;
    private boolean w;
    private boolean x;
    private boolean y;
    private boolean z;
    private final Map<Long, b> h = new HashMap();
    private final Map<Long, Long> i = new HashMap();
    private c2 j = new c2(false, null);
    private VerifyState B = VerifyState.UNKNOWN;
    private io.carplay.app.secured.b.VerifyResult C = io.carplay.app.secured.b.VerifyResult.UNKNOWN;

    public static /* synthetic */ class SwitchMap {
        static final /* synthetic */ int[] a;

        static {
            int[] iArr = new int[l5.a.values().length];
            a = iArr;
            try {
                iArr[l5.a.VERIFIED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                a[l5.a.REJECTED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                a[l5.a.CONTRACT_UNAVAILABLE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public static final class b extends w1 {
        private final long a;
        private final long b;
        private final TlvTag c;
        private final int d;
        private final byte[] e;

        public b(long j, long j2, TlvTag fVar, int i, byte[] bArr) {
            byte[] bArr2 = (byte[]) bArr.clone();
            this.a = j;
            this.b = j2;
            this.c = fVar;
            this.d = i;
            this.e = bArr2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.d == bVar.d && this.a == bVar.a && this.b == bVar.b && Objects.equals(this.c, bVar.c) && Objects.equals(this.e, bVar.e);
        }

        public long g() {
            return this.a;
        }

        public long h() {
            return this.b;
        }

        public final int hashCode() {
            int i = this.d;
            long j = this.a;
            long j2 = this.b;
            TlvTag fVar = this.c;
            byte[] bArr = this.e;
            return Objects.hashCode(bArr) + ((Objects.hashCode(fVar) + w1.a(j2, w1.a(j, i * 31, 31), 31)) * 31);
        }

        public int i() {
            return this.d;
        }

        public byte[] j() {
            return (byte[]) this.e.clone();
        }

        public TlvTag k() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), Long.valueOf(this.b), this.c, Integer.valueOf(this.d), this.e};
            String[] strArrSplit = "a;b;c;d;e".length() == 0 ? new String[0] : "a;b;c;d;e".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(b.class, sb, "[");
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

    public static final class c extends w1 {
        private final VerifyState a;
        private final io.carplay.app.secured.b.VerifyResult b;
        private final boolean c;

        public c(VerifyState eVar, io.carplay.app.secured.b.VerifyResult aVar, boolean z) {
            this.a = eVar;
            this.b = aVar;
            this.c = z;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof c)) {
                return false;
            }
            c cVar = (c) obj;
            return this.c == cVar.c && Objects.equals(this.a, cVar.a) && Objects.equals(this.b, cVar.b);
        }

        public io.carplay.app.secured.b.VerifyResult g() {
            return this.b;
        }

        public VerifyState h() {
            return this.a;
        }

        public final int hashCode() {
            boolean z = this.c;
            VerifyState eVar = this.a;
            io.carplay.app.secured.b.VerifyResult aVar = this.b;
            return Objects.hashCode(aVar) + ((Objects.hashCode(eVar) + (Boolean.hashCode(z) * 31)) * 31);
        }

        public boolean i() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, Boolean.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(c.class, sb, "[");
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

    public static final class d extends w1 {
        private final long a;
        private final String b;
        private final boolean c;

        public d(long j, String str, boolean z) {
            if (str == null || str.isBlank()) {
                SyntheticRefs.b("actual peer evidence reference required");
                throw null;
            }
            this.a = j;
            this.b = str;
            this.c = z;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof d)) {
                return false;
            }
            d dVar = (d) obj;
            return this.c == dVar.c && this.a == dVar.a && Objects.equals(this.b, dVar.b);
        }

        public boolean g() {
            return this.c;
        }

        public long h() {
            return this.a;
        }

        public final int hashCode() {
            boolean z = this.c;
            long j = this.a;
            String str = this.b;
            return Objects.hashCode(str) + w1.a(j, Boolean.hashCode(z) * 31, 31);
        }

        public String i() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), this.b, Boolean.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(d.class, sb, "[");
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

    public enum VerifyState {
        UNKNOWN,
        VERIFIED,
        REJECTED,
        CONTRACT_UNAVAILABLE
    }

    public enum TlvTag {
        VERSION,
        RECEIVER_INFO,
        RECEIVER_STATISTICS,
        FEATURES,
        RSA_PUBLIC_KEY,
        AES_RECEIVED,
        PHONE_CHALLENGE,
        VERIFIED_PHONE_RESULT,
        VIDEO_INIT,
        VIDEO_START
    }

    public static final class g extends w1 {
        private final Map<String, Integer> a;
        private final boolean b;

        public g(Map<String, Integer> map, boolean z) {
            Objects.requireNonNull(map);
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            if (map.size() > 128) {
                SyntheticRefs.b("too many capabilities");
                throw null;
            }
            s1 s1Var = new s1(1);
            s1Var.b = linkedHashMap;
            VarHandle.storeStoreFence();
            map.forEach(s1Var);
            Map<String, Integer> mapCopyOf = Map.copyOf(linkedHashMap);
            Integer num = mapCopyOf.get("CONTENT_ENCRYPTION");
            if (num != null && num.intValue() != 0 && num.intValue() != 1) {
                SyntheticRefs.b("CONTENT_ENCRYPTION must be explicit0/1");
                throw null;
            }
            this.a = mapCopyOf;
            this.b = z;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void i(Map map, String str, Integer num) {
            if (str == null || str.isBlank() || str.length() > 128 || num == null) {
                SyntheticRefs.b("capability");
            } else {
                map.put(str, num);
            }
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof g)) {
                return false;
            }
            g gVar = (g) obj;
            return this.b == gVar.b && Objects.equals(this.a, gVar.a);
        }

        public boolean h() {
            return this.b;
        }

        public final int hashCode() {
            boolean z = this.b;
            Map<String, Integer> map = this.a;
            return Objects.hashCode(map) + (Boolean.hashCode(z) * 31);
        }

        public Map<String, Integer> j() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {this.a, Boolean.valueOf(this.b)};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(g.class, sb, "[");
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

    public v1(long j, io.carplay.app.secured.b bVar, s5 s5Var, boolean z, g gVar, f6.a aVar, l5 l5Var) {
        Objects.requireNonNull(bVar);
        this.b = bVar;
        if (j < 1 || bVar.m().k() != j || bVar.m().i()) {
            SyntheticRefs.b("active ledger generation required");
            throw null;
        }
        this.a = j;
        Objects.requireNonNull(s5Var);
        this.c = s5Var;
        this.d = z;
        Objects.requireNonNull(gVar);
        this.e = gVar;
        this.f = aVar;
        this.g = new l4(j, l5Var == null ? l5.b() : l5Var);
    }

    private void c(long j) throws r5 {
        if (j != this.a || this.b.m().k() != this.a) {
            SyntheticRefs.f("stale session generation");
        } else if (this.o || this.p || this.b.m().i()) {
            SyntheticRefs.f("closed/failed negotiation session");
        }
    }

    private b e(TlvTag fVar, int i, byte[] bArr) {
        b bVar = new b(this.a, Math.incrementExact(this.m), fVar, i, bArr);
        this.m = bVar.h();
        this.h.put(Long.valueOf(bVar.h()), bVar);
        return bVar;
    }

    private static void f(byte[] bArr) throws r5 {
        if (bArr.length == 0) {
            return;
        }
        SyntheticRefs.f("expected empty command body");
    }

    private void k(String str) {
        this.p = true;
        this.h.clear();
        this.i.clear();
        f6 f6Var = this.k;
        if (f6Var != null) {
            f6Var.close();
        }
        this.g.close();
        this.l = null;
        this.j = new c2(false, null);
        long jK = this.b.m().k();
        long j = this.a;
        if (jK == j) {
            this.b.d(j, str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean m(b bVar) {
        return bVar.k() == TlvTag.VERIFIED_PHONE_RESULT;
    }

    private void v(b bVar) throws r5 {
        Objects.requireNonNull(bVar);
        c(bVar.g());
        if (this.h.get(Long.valueOf(bVar.h())) == bVar) {
            return;
        }
        SyntheticRefs.f("foreign/already sent command");
    }

    private void y() {
        if (this.B == VerifyState.VERIFIED) {
            io.carplay.app.secured.b.VerifyResult aVar = this.C;
            io.carplay.app.secured.b.VerifyResult aVar2 = io.carplay.app.secured.b.VerifyResult.PASSED;
            if (aVar == aVar2 && this.y) {
                this.b.f(this.a, aVar2);
            }
        }
    }

    @Override // java.lang.AutoCloseable
    public synchronized void close() {
        try {
            if (this.o) {
                return;
            }
            this.o = true;
            this.h.clear();
            this.i.clear();
            f6 f6Var = this.k;
            if (f6Var != null) {
                f6Var.close();
            }
            this.g.close();
            this.l = null;
            this.j = new c2(false, null);
            if (this.b.m().k() == this.a && !this.b.m().i()) {
                this.b.d(this.a, "controller closed");
            }
        } catch (Throwable th) {
            throw th;
        }
    }

    public synchronized r6.c g(b bVar) throws r5 {
        r6.c cVarB;
        v(bVar);
        try {
            long j = this.n;
            cVarB = this.j.b(r6.a.CMD, bVar.i(), bVar.j());
            v(bVar);
            if (this.n != j) {
                throw new r5("crypto state changed during encoding");
            }
            this.i.put(Long.valueOf(bVar.h()), Long.valueOf(j));
        } catch (RuntimeException e2) {
            k("control encryption failed");
            throw new r5("control encryption failed", e2);
        }
        return cVarB;
    }

    public synchronized boolean i() {
        return (!this.x || this.o || this.p) ? false : true;
    }

    public synchronized c l() {
        return new c(this.B, this.C, this.y);
    }

    public synchronized l4.d n() {
        return this.g.m();
    }

    public synchronized List<b> o(long j, r6.c cVar) throws r5 {
        VerifyState eVar;
        c(j);
            Objects.requireNonNull(cVar);
            if (cVar.h() != r6.a.CMD) {
                throw new r5("engine accepts this session CMD only");
            }
            try {
                try {
                    byte[] bArrA = this.j.a(cVar);
                    ArrayList arrayList = new ArrayList();
                    int iH = cVar.i().h();
                    switch (iH) {
                        case t1.b /* 65538 */:
                            if (!this.r || this.s) {
                                throw new r5("unexpected version status");
                            }
                            int i = ProtobufReader.a(bArrA).i(1);
                            this.b.o(this.a, i == 1);
                            if (i != 1) {
                                k("version rejected");
                                return Collections.EMPTY_LIST;
                            }
                            this.s = true;
                            if (this.d) {
                                arrayList.add(e(TlvTag.RECEIVER_INFO, t1.c, this.c.k()));
                                arrayList.add(e(TlvTag.RECEIVER_STATISTICS, 98343, this.c.q()));
                            }
                            break;
                        case 65540:
                            this.g.b(this.a, iH, bArrA);
                            break;
                        case t1.f /* 65544 */:
                            if (!this.A) {
                                throw new r5("INIT_DONE without caller INIT transmission");
                            }
                            t1.h(bArrA);
                            this.b.g(this.a);
                            break;
                        case l4.i /* 65609 */:
                            if (!this.z) {
                                throw new r5("proof before actual challenge transmission");
                            }
                            l5.a aVarC = this.g.c(this.a, iH, bArrA);
                            c(this.a);
                            int i2 = SwitchMap.a[aVarC.ordinal()];
                            if (i2 == 1) {
                                eVar = VerifyState.VERIFIED;
                            } else if (i2 == 2) {
                                eVar = VerifyState.REJECTED;
                            } else {
                                if (i2 != 3) {
                                    throw new IncompatibleClassChangeError();
                                }
                                eVar = VerifyState.CONTRACT_UNAVAILABLE;
                            }
                            this.B = eVar;
                            if (aVarC != l5.a.REJECTED) {
                                y();
                            } else {
                                this.b.f(this.a, io.carplay.app.secured.b.VerifyResult.FAILED);
                                k("modern phone proof rejected");
                            }
                            break;
                        case 65611:
                            boolean zG = ProtobufReader.a(bArrA).g(1);
                            this.C = zG ? io.carplay.app.secured.b.VerifyResult.PASSED : io.carplay.app.secured.b.VerifyResult.FAILED;
                            if (!zG) {
                                this.b.f(this.a, io.carplay.app.secured.b.VerifyResult.FAILED);
                                k("phone eligibility result rejected");
                            } else {
                                y();
                            }
                            break;
                        case t1.h /* 65617 */:
                            f(bArrA);
                            if (this.t) {
                                throw new r5("duplicate feature request");
                            }
                            this.t = true;
                            Integer num = this.e.j().get("CONTENT_ENCRYPTION");
                            if (this.e.h() && num != null) {
                                if (num.intValue() == 1 && this.f == null) {
                                    throw new r5("verified AES factory is required before advertising encryption");
                                }
                                arrayList.add(e(TlvTag.FEATURES, t1.i, t1.d(this.e.j())));
                            }
                            break;
                        case t1.j /* 65642 */:
                            f(bArrA);
                            if (this.u) {
                                Integer num2 = 1;
                                if (num2.equals(this.e.j().get("CONTENT_ENCRYPTION")) && this.k == null && !this.x) {
                                    this.k = new f6(this.f);
                                    c(this.a);
                                    arrayList.add(e(TlvTag.RSA_PUBLIC_KEY, t1.k, t1.i(this.k.c())));
                                }
                                break;
                            }
                            throw new r5("unexpected RSA request");
                        case t1.l /* 65644 */:
                            f6 f6Var = this.k;
                            if (f6Var == null || !this.v || this.l != null) {
                                throw new r5("unexpected AES seed");
                            }
                            o oVarB = f6Var.b(ProtobufReader.a(bArrA).j(1));
                            c(this.a);
                            this.l = oVarB;
                            arrayList.add(e(TlvTag.AES_RECEIVED, t1.m, new byte[0]));
                            break;
                        case t1.n /* 65646 */:
                            f(bArrA);
                            if (!this.w || this.l == null || this.x) {
                                throw new r5("ENCRYPT_READY before actual key/ack or duplicate");
                            }
                            this.x = true;
                            this.b.c(this.a, io.carplay.app.secured.b.VerifyResult.PASSED);
                            this.b.e(this.a, true);
                            break;
                    }
                    return List.copyOf(arrayList);
                } catch (r5 e2) {
                    k("malformed or unexpected negotiation command");
                    throw e2;
                }
            } catch (IllegalStateException | GeneralSecurityException e3) {
                k("negotiation state/crypto failure");
                throw new r5("negotiation failed", e3);
            }
    }

    public synchronized void p(d dVar) throws r5 {
        Objects.requireNonNull(dVar);
        c(dVar.h());
        if (this.u && this.e.h()) {
            Integer num = 0;
            if (num.equals(this.e.j().get("CONTENT_ENCRYPTION"))) {
                this.b.e(this.a, dVar.g());
                if (!dVar.g()) {
                    k("observed peer feature failure");
                }
            }
        }
        throw new r5("this contract requires actual ENCRYPT_READY instead of external feature completion");
    }

    public synchronized b q() throws r5 {
        l4.b bVarF;
        c(this.a);
        if (!this.s) {
            throw new r5("modern challenge requires actual matched version");
        }
        bVarF = this.g.f(this.a);
        return e(TlvTag.PHONE_CHALLENGE, bVarF.i(), bVarF.g());
    }

    public synchronized b r() throws r5 {
        c(this.a);
        if (this.g.m() != l4.d.VERIFIED) {
            throw new r5("no verified phone proof");
        }
        if (this.y || this.h.values().stream().anyMatch(new w0(19))) {
            throw new r5("proof result already issued");
        }
        return e(TlvTag.VERIFIED_PHONE_RESULT, 98378, new ProtobufReader.a().a(1, true).g());
    }

    public synchronized b s(int i, int i2) throws r5 {
        byte[] bArrJ;
        c(this.a);
        if (this.q) {
            throw new r5("version already requested");
        }
        bArrJ = t1.j(i, i2);
        this.q = true;
        return e(TlvTag.VERSION, t1.a, bArrJ);
    }

    public synchronized b t(int i, int i2, int i3) throws r5 {
        c(this.a);
        return e(TlvTag.VIDEO_INIT, t1.e, t1.k(i, i2, i3));
    }

    public synchronized b u() throws r5 {
        c(this.a);
        return e(TlvTag.VIDEO_START, t1.g, new byte[0]);
    }

    public synchronized void w(b bVar) throws r5 {
        v(bVar);
            if (!Long.valueOf(this.n).equals(this.i.get(Long.valueOf(bVar.h())))) {
                throw new r5("command not encoded in current crypto epoch");
            }
            this.h.remove(Long.valueOf(bVar.h()));
            this.i.remove(Long.valueOf(bVar.h()));
            int iOrdinal = bVar.k().ordinal();
            if (iOrdinal != 0) {
                switch (iOrdinal) {
                    case p1.d /* 3 */:
                        this.u = true;
                        Integer num = 0;
                        if (num.equals(this.e.j().get("CONTENT_ENCRYPTION"))) {
                            this.b.c(this.a, io.carplay.app.secured.b.VerifyResult.NOT_APPLICABLE);
                        }
                        break;
                    case 4:
                        this.v = true;
                        break;
                    case 5:
                        o oVar = this.l;
                        if (oVar == null) {
                            throw new r5("no accepted session seed");
                        }
                        this.j = new c2(true, oVar);
                        this.w = true;
                        this.n = Math.incrementExact(this.n);
                        break;
                    case 6:
                        this.z = true;
                        break;
                    case 7:
                        this.y = true;
                        y();
                        break;
                    case 8:
                        this.A = true;
                        break;
                }
            } else {
                this.r = true;
            }
    }

    public synchronized io.carplay.app.secured.b.C0003b x() {
        return this.b.m();
    }
}
