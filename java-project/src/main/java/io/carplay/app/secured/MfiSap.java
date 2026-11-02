package io.carplay.app.secured;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* JADX INFO: loaded from: classes.dex */
/**
 * MfiSap
 *
 * <p>原混淆名：{@code io.carplay.app.secured.j4}（jadx 产物 j4.java）
 *
 * <p>命名依据：MFiSAP requires ... —— Apple MFi 认证协处理器协议
 */
public final class MfiSap {

    public static final class c extends GeneralSecurityException {
        private static final long a = 1;

        public c(String str) {
            super(str);
        }
    }

    private MfiSap() {
    }

    public static a a(EventChannel eventChannel, String str) throws GeneralSecurityException {
        if (str == null || !str.matches("(?i)[0-9a-f]{64}")) {
            throw new c("MFiSAP requires the explicit 64-hex leaf certificate fingerprint from this accessory's normal pairing");
        }
        if (!eventChannel.n()) {
            throw new GeneralSecurityException("MFiSAP requires pair-verified encrypted RTSP control");
        }
        DerCodec.b bVarW = DerCodec.w();
        EventChannel.b bVarV = eventChannel.v("POST", "/auth-setup", "application/octet-stream", DerCodec.d(new byte[]{1}, bVarW.b()));
        bVarV.g();
        return f(bVarW.a(), bVarW.b(), bVarV.c(), str);
    }

    private static byte[] b(byte[] bArr, byte[] bArr2) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.update(bArr);
        return Arrays.copyOf(messageDigest.digest(bArr2), 16);
    }

    private static byte[] c(byte[] bArr) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int[] iArr = {0, 32};
        int i = 0;
        while (true) {
            if (i >= 2) {
                return DerCodec.d(new byte[]{48, (byte) byteArrayOutputStream.size()}, byteArrayOutputStream.toByteArray());
            }
            int i2 = iArr[i];
            int i3 = i2;
            while (i3 < i2 + 31 && bArr[i3] == 0) {
                i3++;
            }
            int i4 = (i2 + 32) - i3;
            int i5 = (bArr[i3] & 128) == 0 ? 0 : 1;
            byteArrayOutputStream.write(2);
            byteArrayOutputStream.write(i4 + i5);
            if (i5 != 0) {
                byteArrayOutputStream.write(0);
            }
            byteArrayOutputStream.write(bArr, i3, i4);
            i++;
        }
    }

    private static int d(byte[] bArr, int i, int i2) throws IOException {
        if (i < 0 || i > bArr.length - 4) {
            d0.c("Truncated MFiSAP length");
            return 0;
        }
        long j = (((long) bArr[i + 3]) & 255) | ((((long) bArr[i]) & 255) << 24) | ((((long) bArr[i + 1]) & 255) << 16) | ((((long) bArr[i + 2]) & 255) << 8);
        if (j >= 1 && j <= i2) {
            return (int) j;
        }
        d0.c("MFiSAP field length exceeds bound");
        return 0;
    }

    public static void e(EventChannel eventChannel) {
        if (!eventChannel.n()) {
            throw new GeneralSecurityException("Candidate proof requires pair-verified encrypted control");
        }
        DerCodec.b bVarW = DerCodec.w();
        int i = 0;
        EventChannel.b bVarV = eventChannel.v("POST", "/auth-setup", "application/octet-stream", DerCodec.d(new byte[]{1}, bVarW.b()));
        bVarV.g();
        byte[] bArrC = bVarV.c();
        if (bArrC.length < 40 || bArrC.length > 65000) {
            throw new IOException("MFiSAP candidate size out of bounds");
        }
        int iD = d(bArrC, 32, 60000);
        if (iD > bArrC.length - 40) {
            throw new IOException("Truncated MFiSAP candidate certificate");
        }
        m5 m5VarF = m5.f(Arrays.copyOfRange(bArrC, 36, iD + 36));
        f(bVarW.a(), bVarW.b(), bArrC, m5VarF.e());
        throw new b(m5VarF, i);
    }

    public static a f(byte[] bArr, byte[] bArr2, byte[] bArr3, String str) throws Throwable {
        byte[] bArr4;
        byte[] bArrB;
        String str2;
        if (str == null || !str.matches("(?i)[0-9a-f]{64}")) {
            throw new c("Missing explicit accessory certificate pin");
        }
        byte[] bArr5 = null;
        if (bArr3.length < 40 || bArr3.length > 65000) {
            d0.c("MFiSAP response size out of bounds");
            return null;
        }
        int i = 0;
        byte[] bArrCopyOfRange = Arrays.copyOfRange(bArr3, 0, 32);
        int iD = d(bArr3, 32, 60000);
        int i2 = iD + 36;
        if (i2 > bArr3.length - 4) {
            d0.c("Truncated MFiSAP certificate");
            return null;
        }
        byte[] bArrCopyOfRange2 = Arrays.copyOfRange(bArr3, 36, i2);
        int iD2 = d(bArr3, i2, 1024);
        int i3 = iD + 40;
        if (iD2 < 1 || iD2 > bArr3.length - i3) {
            d0.c("Truncated or empty MFiSAP signature");
            return null;
        }
        if (iD2 + i3 != bArr3.length) {
            throw new GeneralSecurityException("MFiSAP extended/BAA response requires an explicit BAA trust policy");
        }
        m5 m5VarF = m5.f(bArrCopyOfRange2);
        if (!m5VarF.h(str)) {
            throw new c("MFiSAP accessory leaf fingerprint differs from the normally paired accessory");
        }
        X509Certificate x509Certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(m5VarF.a()));
        byte[] bArrX = DerCodec.x(bArr, bArrCopyOfRange);
        try {
            byte[] bArrB2 = b(DerCodec.a("AES-KEY"), bArrX);
            try {
                bArrB = b(DerCodec.a("AES-IV"), bArrX);
                try {
                    Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
                    cipher.init(2, new SecretKeySpec(bArrB2, "AES"), new IvParameterSpec(bArrB));
                    byte[] bArrDoFinal = cipher.doFinal(Arrays.copyOfRange(bArr3, i3, bArr3.length));
                    byte[] bArrD = DerCodec.d(bArrCopyOfRange, bArr2);
                    String algorithm = x509Certificate.getPublicKey().getAlgorithm();
                    if ("RSA".equalsIgnoreCase(algorithm)) {
                        str2 = "SHA1withRSA";
                    } else {
                        if (!"EC".equalsIgnoreCase(algorithm)) {
                            throw new GeneralSecurityException("Unsupported MFiSAP signature key " + algorithm);
                        }
                        str2 = "SHA256withECDSA";
                        if (bArrDoFinal.length == 64) {
                            bArrDoFinal = c(bArrDoFinal);
                        }
                    }
                    Signature signature = Signature.getInstance(str2);
                    signature.initVerify(x509Certificate.getPublicKey());
                    signature.update(bArrD);
                    if (!signature.verify(bArrDoFinal)) {
                        throw new GeneralSecurityException("MFiSAP fresh key-exchange challenge signature invalid");
                    }
                    a aVar = new a(m5VarF.e(), m5VarF.d(), i);
                    Arrays.fill(bArrX, (byte) 0);
                    if (bArrB2 != null) {
                        Arrays.fill(bArrB2, (byte) 0);
                    }
                    if (bArrB != null) {
                        Arrays.fill(bArrB, (byte) 0);
                    }
                    if (bArrDoFinal != null) {
                        Arrays.fill(bArrDoFinal, (byte) 0);
                    }
                    return aVar;
                } catch (Throwable th) {
                    th = th;
                    bArr4 = null;
                    bArr5 = bArrB2;
                    Arrays.fill(bArrX, (byte) 0);
                    if (bArr5 != null) {
                        Arrays.fill(bArr5, (byte) 0);
                    }
                    if (bArrB != null) {
                        Arrays.fill(bArrB, (byte) 0);
                    }
                    if (bArr4 != null) {
                        Arrays.fill(bArr4, (byte) 0);
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                bArr4 = null;
                bArrB = null;
            }
        } catch (Throwable th3) {
            th = th3;
            bArr4 = null;
            bArrB = null;
        }
    }

    public static final class a {
        private final String a;
        private final boolean b;

        private a(String str, boolean z) {
            this.a = str;
            this.b = z;
        }

        public boolean a() {
            return false;
        }

        public String b() {
            return this.a;
        }

        public boolean c() {
            return this.b;
        }

        public boolean d() {
            return true;
        }

        public /* synthetic */ a(String str, boolean z, int i) {
            this(str, z);
        }
    }

    public static final class b extends GeneralSecurityException {
        private static final long c = 1;
        private final byte[] a;
        private final String b;

        private b(m5 m5Var) {
            super("Explicit current-WLAN certificate confirmation required");
            this.a = m5Var.a();
            this.b = m5Var.e();
        }

        public byte[] a() {
            return (byte[]) this.a.clone();
        }

        public String b() {
            return this.b;
        }

        public /* synthetic */ b(m5 m5Var, int i) {
            this(m5Var);
        }
    }
}
