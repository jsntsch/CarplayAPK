package io.carplay.app.secured;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Arrays;
import java.util.Objects;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes.dex */
public final class c {

    public static final class a extends w1 {
        private final String a;
        private final String b;
        private final boolean c;
        private final int d;
        private final String e;

        public a(String str, String str2, boolean z, int i, String str3) {
            this.a = str;
            this.b = str2;
            this.c = z;
            this.d = i;
            this.e = str3;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.c == aVar.c && this.d == aVar.d && Objects.equals(this.a, aVar.a) && Objects.equals(this.b, aVar.b) && Objects.equals(this.e, aVar.e);
        }

        public int g() {
            return this.d;
        }

        public String h() {
            return this.b;
        }

        public final int hashCode() {
            boolean z = this.c;
            int i = this.d;
            String str = this.a;
            String str2 = this.b;
            String str3 = this.e;
            return Objects.hashCode(str3) + w1.b(str2, w1.b(str, ((Boolean.hashCode(z) * 31) + i) * 31, 31), 31);
        }

        public boolean i() {
            return this.c;
        }

        public String j() {
            return this.a;
        }

        public String k() {
            return this.e;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, Boolean.valueOf(this.c), Integer.valueOf(this.d), this.e};
            String[] strArrSplit = "a;b;c;d;e".length() == 0 ? new String[0] : "a;b;c;d;e".split(";");
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

    private c() {
    }

    public static a a() throws BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] bArr = new byte[16];
        for (int i = 0; i < 16; i++) {
            bArr[i] = (byte) i;
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(bArr, "AES");
        Provider provider = Cipher.getInstance("AES").getProvider();
        int[] iArr = {0, 1, 15, 16, 17, 31, 32, 257};
        int i2 = 0;
        boolean zEquals = true;
        for (int i3 = 0; i3 < 8; i3++) {
            int i4 = iArr[i3];
            byte[] bArr2 = new byte[i4];
            for (int i5 = 0; i5 < i4; i5++) {
                bArr2[i5] = (byte) ((i5 * 31) + 7);
            }
            Cipher cipher = Cipher.getInstance("AES", provider);
            Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding", provider);
            cipher.init(1, secretKeySpec);
            cipher2.init(1, secretKeySpec);
            byte[] bArrDoFinal = cipher.doFinal(bArr2);
            byte[] bArrDoFinal2 = cipher2.doFinal(bArr2);
            boolean zEquals2 = Arrays.equals(bArrDoFinal, bArrDoFinal2) & zEquals;
            Cipher cipher3 = Cipher.getInstance("AES", provider);
            cipher3.init(2, secretKeySpec);
            zEquals = zEquals2 & Arrays.equals(cipher3.doFinal(bArrDoFinal2), bArr2);
            i2++;
        }
        Arrays.fill(bArr, (byte) 0);
        return new a(provider.getName(), provider.getInfo(), zEquals, i2, "this process/provider and synthetic fixtures only; not phone-plugin or session acceptance");
    }
}
