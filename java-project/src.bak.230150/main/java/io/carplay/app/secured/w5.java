package io.carplay.app.secured;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/* JADX INFO: loaded from: classes.dex */
public final class w5 extends w1 {
    public long a;
    public long b;
    public long c;
    public String d;
    public long e;
    public List f;

    public static final class a extends w1 {
        public long a;
        public String b;
        public String c;
        public String d;

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.a == aVar.a && Objects.equals(this.b, aVar.b) && Objects.equals(this.c, aVar.c) && Objects.equals(this.d, aVar.d);
        }

        public final int hashCode() {
            long j = this.a;
            String str = this.b;
            String str2 = this.c;
            String str3 = this.d;
            return Objects.hashCode(str3) + w1.b(str2, w1.b(str, Long.hashCode(j) * 31, 31), 31);
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), this.b, this.c, this.d};
            String[] strArrSplit = "a;b;c;d".length() == 0 ? new String[0] : "a;b;c;d".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(a.class, sb, "[");
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

    public static long g(String str) throws IOException {
        if (!str.matches("[0-9]{1,18}")) {
            SyntheticRefs.c("release integer");
            return 0L;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            throw new IOException("release integer", e);
        }
    }

    public static URI j(URI uri, String str) throws IOException {
        try {
            if (str == null) {
                throw new IOException("missing catalog redirect");
            }
            URI uriResolve = uri.resolve(new URI(str));
            String host = uriResolve.getHost();
            if ("https".equalsIgnoreCase(uriResolve.getScheme()) && host != null && uriResolve.getUserInfo() == null && uriResolve.getFragment() == null && ((uriResolve.getPort() == -1 || uriResolve.getPort() == 443) && ((host.equalsIgnoreCase("gitee.com") || host.equalsIgnoreCase("raw.giteeusercontent.com")) && "/eyessx/carplus-carplay-updates/raw/master/version.json".equals(uriResolve.getPath())))) {
                return uriResolve;
            }
            throw new IOException("untrusted catalog redirect");
        } catch (IllegalArgumentException | URISyntaxException e) {
            throw new IOException("invalid catalog redirect", e);
        }
    }

    public static w5 k(String str, String str2, long j, long j2) throws GeneralSecurityException, IOException {
        if (str == null || str2 == null || str.length() > 12000 || str2.length() > 160) {
            SyntheticRefs.c("release envelope bounds");
            return null;
        }
        try {
            byte[] bArrDecode = Base64.getDecoder().decode(str);
            byte[] bArrDecode2 = Base64.getDecoder().decode(str2);
            byte[] bArrDecode3 = Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE0devRcwFWwRybNWPlxTwNYNeHOEGlbYjSP2OB1F6ie2QfasyA837zEoPV3peLD6EvhcxyW6BMMBZaRruALpHaw==");
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(bArrDecode3)));
            signature.update(bArrDecode);
            if (!signature.verify(bArrDecode2)) {
                throw new GeneralSecurityException("release signature invalid");
            }
            int i = -1;
            String[] strArrSplit = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bArrDecode)).toString().split("\n", -1);
            if (strArrSplit.length >= 8 && strArrSplit.length <= 70 && strArrSplit[0].equals("CARPLUS_RELEASES_V1")) {
                int i2 = 1;
                if (strArrSplit[strArrSplit.length - 1].isEmpty()) {
                    long jG = g(strArrSplit[1]);
                    long jG2 = g(strArrSplit[2]);
                    char c = 3;
                    long jG3 = g(strArrSplit[3]);
                    if (jG < j2 || jG2 > j + 1800 || jG3 <= j || jG3 <= jG2 || jG3 - jG2 > 31968000) {
                        SyntheticRefs.c("release stale/clock/rollback");
                        return null;
                    }
                    int i3 = 4;
                    if (!strArrSplit[4].equals("io.carplay.app.prototype")) {
                        SyntheticRefs.c("release package mismatch");
                        return null;
                    }
                    long jG4 = g(strArrSplit[5]);
                    ArrayList arrayList = new ArrayList();
                    long j3 = 0;
                    int i4 = 6;
                    while (i4 < strArrSplit.length - i2) {
                        int i5 = i2;
                        String[] strArrSplit2 = strArrSplit[i4].split("\t", i);
                        if (strArrSplit2.length != i3) {
                            SyntheticRefs.c("release entry schema");
                            return null;
                        }
                        int i6 = i4;
                        long jG5 = g(strArrSplit2[0]);
                        if (jG5 > j3 && jG5 <= 2147483647L) {
                            char c2 = c;
                            if (strArrSplit2[i5].matches("[0-9]+(\\.[0-9]+){1,3}") && strArrSplit2[2].matches("[a-f0-9]{64}") && strArrSplit2[c2].matches("[a-f0-9]{64}")) {
                                String str3 = strArrSplit2[i5];
                                int i7 = i3;
                                String str4 = strArrSplit2[2];
                                String str5 = strArrSplit2[c2];
                                a aVar = new a();
                                aVar.a = jG5;
                                aVar.b = str3;
                                aVar.c = str4;
                                aVar.d = str5;
                                VarHandle.storeStoreFence();
                                arrayList.add(aVar);
                                i3 = i7;
                                j3 = jG5;
                                i4 = i6 + 1;
                                i2 = i5;
                                c = c2;
                                i = -1;
                            }
                        }
                        SyntheticRefs.c("release entry invalid");
                        return null;
                    }
                    int i8 = i3;
                    if (jG4 < 1 || jG4 > j3) {
                        SyntheticRefs.c("release minimum version");
                        return null;
                    }
                    String str6 = strArrSplit[i8];
                    List listCopyOf = List.copyOf(arrayList);
                    w5 w5Var = new w5();
                    w5Var.a = jG;
                    w5Var.b = jG2;
                    w5Var.c = jG3;
                    w5Var.d = str6;
                    w5Var.e = jG4;
                    w5Var.f = listCopyOf;
                    VarHandle.storeStoreFence();
                    return w5Var;
                }
            }
            SyntheticRefs.c("release schema");
            return null;
        } catch (IllegalArgumentException e) {
            throw new IOException("release encoding", e);
        }
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof w5)) {
            return false;
        }
        w5 w5Var = (w5) obj;
        return this.a == w5Var.a && this.b == w5Var.b && this.c == w5Var.c && this.e == w5Var.e && Objects.equals(this.d, w5Var.d) && Objects.equals(this.f, w5Var.f);
    }

    public final a h() {
        List list = this.f;
        return (a) list.get(list.size() - 1);
    }

    public final int hashCode() {
        long j = this.a;
        long j2 = this.b;
        long j3 = this.c;
        long j4 = this.e;
        String str = this.d;
        List list = this.f;
        return Objects.hashCode(list) + w1.b(str, w1.a(j4, w1.a(j3, w1.a(j2, Long.hashCode(j) * 31, 31), 31), 31), 31);
    }

    public final boolean i(long j, String str, String str2, String str3) {
        Stream stream = this.f.stream();
        v5 v5Var = new v5();
        v5Var.a = j;
        VarHandle.storeStoreFence();
        a aVar = (a) stream.filter(v5Var).findFirst().orElse(null);
        return aVar != null && aVar.b.equals(str) && aVar.c.equals(str2) && aVar.d.equals(str3);
    }

    public final String toString() {
        Object[] objArr = {Long.valueOf(this.a), Long.valueOf(this.b), Long.valueOf(this.c), this.d, Long.valueOf(this.e), this.f};
        String[] strArrSplit = "a;b;c;d;e;f".length() == 0 ? new String[0] : "a;b;c;d;e;f".split(";");
        StringBuilder sb = new StringBuilder();
        w1.f(w5.class, sb, "[");
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
