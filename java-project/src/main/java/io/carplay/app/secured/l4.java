package io.carplay.app.secured;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public final class l4 implements AutoCloseable {
    public static final int g = 65540;
    public static final int h = 98376;
    public static final int i = 65609;
    private final long a;
    private final l5 b;
    private final SecureRandom c;
    private AacAudioFormat d;
    private c e;
    private String f;

    public static /* synthetic */ class a {
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
        private final int b;
        private final String c;
        private final byte[] d;

        public b(long j, int i, String str, byte[] bArr) {
            byte[] bArr2 = (byte[]) bArr.clone();
            this.a = j;
            this.b = i;
            this.c = str;
            this.d = bArr2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.b == bVar.b && this.a == bVar.a && Objects.equals(this.c, bVar.c) && Objects.equals(this.d, bVar.d);
        }

        public byte[] g() {
            return (byte[]) this.d.clone();
        }

        public long h() {
            return this.a;
        }

        public final int hashCode() {
            int i = this.b;
            long j = this.a;
            String str = this.c;
            byte[] bArr = this.d;
            return Objects.hashCode(bArr) + w1.b(str, w1.a(j, i * 31, 31), 31);
        }

        public int i() {
            return this.b;
        }

        public String j() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), Integer.valueOf(this.b), this.c, this.d};
            String[] strArrSplit = "a;b;c;d".length() == 0 ? new String[0] : "a;b;c;d".split(";");
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
        private final String a;
        private final String b;
        private final String c;

        public c(String str, String str2, String str3) {
            this.a = str;
            this.b = str2;
            this.c = str3;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof c)) {
                return false;
            }
            c cVar = (c) obj;
            return Objects.equals(this.a, cVar.a) && Objects.equals(this.b, cVar.b) && Objects.equals(this.c, cVar.c);
        }

        public String g() {
            return this.a;
        }

        public String h() {
            return this.b;
        }

        public final int hashCode() {
            String str = this.a;
            String str2 = this.b;
            String str3 = this.c;
            return Objects.hashCode(str3) + w1.b(str2, Objects.hashCode(str) * 31, 31);
        }

        public String i() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, this.c};
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

    public enum AacAudioFormat {
        WAITING_FOR_MD_INFO,
        READY,
        CHALLENGE_ISSUED,
        VERIFYING,
        VERIFIED,
        REJECTED,
        CONTRACT_UNAVAILABLE,
        CLOSED
    }

    public l4(long j, l5 l5Var, SecureRandom secureRandom) {
        this.d = AacAudioFormat.WAITING_FOR_MD_INFO;
        if (j < 1) {
            d0.b("positive session generation required");
            throw null;
        }
        this.a = j;
        Objects.requireNonNull(l5Var);
        this.b = l5Var;
        Objects.requireNonNull(secureRandom);
        this.c = secureRandom;
    }

    private static ProtobufReader.e i(byte[] bArr) throws r5 {
        if (bArr != null) {
            return ProtobufReader.a(bArr);
        }
        d0.f("missing protobuf body");
        return null;
    }

    private void k(long j) throws r5 {
        if (j == this.a) {
            return;
        }
        d0.f("stale or foreign generation");
    }

    private void l(AacAudioFormat dVar) throws r5 {
        AacAudioFormat dVar2 = this.d;
        if (dVar2 == dVar) {
            return;
        }
        d0.f("unexpected modern proof state: ".concat(String.valueOf(dVar2)));
    }

    public synchronized void b(long j, int i2, byte[] bArr) {
        k(j);
        l(AacAudioFormat.WAITING_FOR_MD_INFO);
        if (i2 != 65540) {
            throw new r5("expected MD_INFO");
        }
        ProtobufReader.e eVarI = i(bArr);
        this.e = new c(eVarI.f(4, ""), eVarI.f(14, ""), eVarI.f(20, ""));
        this.d = AacAudioFormat.READY;
    }

    public synchronized l5.a c(long j, int i2, byte[] bArr) {
        l5.a aVarF;
        AacAudioFormat dVar;
        k(j);
        l(AacAudioFormat.CHALLENGE_ISSUED);
        this.d = AacAudioFormat.VERIFYING;
        try {
            if (i2 != 65609) {
                throw new r5("expected MD_AUTHEN_RESPONSE");
            }
            String strJ = i(bArr).j(1);
            if (strJ.isEmpty()) {
                throw new r5("empty mobile proof");
            }
            try {
                aVarF = this.b.f(this.f, this.e.g(), this.e.h(), this.e.i(), strJ);
                if (aVarF == null) {
                    aVarF = l5.a.CONTRACT_UNAVAILABLE;
                }
            } catch (RuntimeException | GeneralSecurityException unused) {
                aVarF = l5.a.REJECTED;
            }
            this.f = null;
            if (this.d != AacAudioFormat.VERIFYING) {
                return l5.a.REJECTED;
            }
            int i3 = a.a[aVarF.ordinal()];
            if (i3 == 1) {
                dVar = AacAudioFormat.VERIFIED;
            } else if (i3 == 2) {
                dVar = AacAudioFormat.REJECTED;
            } else {
                if (i3 != 3) {
                    throw new IncompatibleClassChangeError();
                }
                dVar = AacAudioFormat.CONTRACT_UNAVAILABLE;
            }
            this.d = dVar;
            return aVarF;
        } catch (r5 e) {
            this.d = AacAudioFormat.REJECTED;
            this.f = null;
            throw e;
        }
    }

    @Override // java.lang.AutoCloseable
    public synchronized void close() {
        this.d = AacAudioFormat.CLOSED;
        this.f = null;
        this.e = null;
    }

    public long e() {
        return this.a;
    }

    public synchronized b f(long j) {
        String str;
        byte[] bArrG;
        try {
            k(j);
            l(AacAudioFormat.READY);
            byte[] bArr = new byte[16];
            this.c.nextBytes(bArr);
            char[] cArr = new char[32];
            char[] charArray = "0123456789abcdef".toCharArray();
            for (int i2 = 0; i2 < 16; i2++) {
                byte b2 = bArr[i2];
                int i3 = i2 * 2;
                cArr[i3] = charArray[(b2 & 255) >>> 4];
                cArr[i3 + 1] = charArray[b2 & 15];
            }
            String str2 = new String(cArr);
            this.f = str2;
            str = "2.0;" + str2;
            bArrG = new ProtobufReader.a().e(1, str).g();
            this.d = AacAudioFormat.CHALLENGE_ISSUED;
        } catch (Throwable th) {
            throw th;
        }
        return new b(this.a, h, str, bArrG);
    }

    public synchronized c g() {
        return this.e;
    }

    public synchronized AacAudioFormat m() {
        return this.d;
    }

    public l4(long j, l5 l5Var) {
        this(j, l5Var, new SecureRandom());
    }

    public l4(long j) {
        this(j, l5.b());
    }
}
