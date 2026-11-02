package io.carplay.app.secured;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

/* JADX INFO: loaded from: classes.dex */
public final class m5 {
    private final X509Certificate a;
    private final byte[] b;
    private final String c;
    private final boolean d;

    private m5(byte[] bArr, boolean z) throws NoSuchAlgorithmException, SignatureException, IOException, InvalidKeyException, CertificateException, NoSuchProviderException {
        if (bArr.length == 0 || bArr.length > 60000) {
            SyntheticRefs.c("MFi certificate size");
            throw null;
        }
        Collection<? extends Certificate> collectionGenerateCertificates = CertificateFactory.getInstance("X.509").generateCertificates(new ByteArrayInputStream(bArr));
        ArrayList arrayList = new ArrayList();
        for (Certificate certificate : collectionGenerateCertificates) {
            if (!(certificate instanceof X509Certificate)) {
                SyntheticRefs.c("MFi non-X509 certificate");
                throw null;
            }
            arrayList.add((X509Certificate) certificate);
        }
        if (arrayList.isEmpty() || arrayList.size() > 8) {
            SyntheticRefs.c("MFi certificate count");
            throw null;
        }
        int size = arrayList.size();
        X509Certificate x509Certificate = null;
        int i = 0;
        while (i < size) {
            Object obj = arrayList.get(i);
            i++;
            X509Certificate x509Certificate2 = (X509Certificate) obj;
            if (x509Certificate2.getBasicConstraints() < 0) {
                if (x509Certificate != null) {
                    SyntheticRefs.c("Ambiguous MFi leaf certificate");
                    throw null;
                }
                x509Certificate = x509Certificate2;
            }
        }
        if (x509Certificate == null) {
            SyntheticRefs.c("MFi leaf certificate missing");
            throw null;
        }
        Date date = new Date();
        boolean zC = c(x509Certificate, date, z);
        boolean[] keyUsage = x509Certificate.getKeyUsage();
        if (keyUsage != null && (keyUsage.length == 0 || !keyUsage[0])) {
            SyntheticRefs.c("MFi key disallows signing");
            throw null;
        }
        ArrayList arrayList2 = new ArrayList();
        X509Certificate x509Certificate3 = x509Certificate;
        while (true) {
            if (arrayList2.contains(x509Certificate3)) {
                SyntheticRefs.c("MFi certificate chain loop");
                throw null;
            }
            arrayList2.add(x509Certificate3);
            if (x509Certificate3.getIssuerX500Principal().equals(x509Certificate3.getSubjectX500Principal())) {
                x509Certificate3.verify(x509Certificate3.getPublicKey());
                break;
            }
            int size2 = arrayList.size();
            X509Certificate x509Certificate4 = null;
            int i2 = 0;
            while (i2 < size2) {
                Object obj2 = arrayList.get(i2);
                i2++;
                X509Certificate x509Certificate5 = (X509Certificate) obj2;
                if (x509Certificate3.getIssuerX500Principal().equals(x509Certificate5.getSubjectX500Principal())) {
                    if (x509Certificate4 != null) {
                        SyntheticRefs.c("Ambiguous MFi issuer");
                        throw null;
                    }
                    x509Certificate4 = x509Certificate5;
                }
            }
            if (x509Certificate4 == null) {
                break;
            }
            zC = c(x509Certificate4, date, z) ? true : zC;
            if (x509Certificate4.getBasicConstraints() < 0) {
                SyntheticRefs.c("MFi issuer is not CA");
                throw null;
            }
            x509Certificate3.verify(x509Certificate4.getPublicKey());
            x509Certificate3 = x509Certificate4;
        }
        this.d = zC;
        this.a = x509Certificate;
        this.c = g(MessageDigest.getInstance("SHA-256").digest(x509Certificate.getEncoded()));
        String algorithm = x509Certificate.getPublicKey().getAlgorithm();
        if ("EC".equals(algorithm)) {
            PublicKey publicKey = x509Certificate.getPublicKey();
            if (!(publicKey instanceof ECPublicKey) || ((ECPublicKey) publicKey).getParams().getCurve().getField().getFieldSize() != 256) {
                SyntheticRefs.c("MFi EC key must be P-256");
                throw null;
            }
            this.b = new byte[32];
        } else {
            if (!"RSA".equals(algorithm)) {
                SyntheticRefs.c(w1.e("Unsupported MFi public key algorithm ", algorithm));
                throw null;
            }
            PublicKey publicKey2 = x509Certificate.getPublicKey();
            if (!(publicKey2 instanceof RSAPublicKey) || ((RSAPublicKey) publicKey2).getModulus().bitLength() < 1024) {
                SyntheticRefs.c("MFi RSA key too short");
                throw null;
            }
            this.b = new byte[20];
        }
        new SecureRandom().nextBytes(this.b);
    }

    private static boolean c(X509Certificate x509Certificate, Date date, boolean z) throws CertificateNotYetValidException, CertificateExpiredException {
        try {
            x509Certificate.checkValidity(date);
            return false;
        } catch (CertificateExpiredException e) {
            if (z) {
                return true;
            }
            throw e;
        }
    }

    public static m5 f(byte[] bArr) {
        return new m5(bArr, true);
    }

    private static String g(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bArr) {
            sb.append(String.format(Locale.ROOT, "%02x", Integer.valueOf(b & 255)));
        }
        return sb.toString();
    }

    private static byte[] i(byte[] bArr) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int[] iArr = {0, 32};
        for (int i = 0; i < 2; i++) {
            int i2 = iArr[i];
            int i3 = i2;
            while (i3 < i2 + 31 && bArr[i3] == 0) {
                i3++;
            }
            int i4 = (i2 + 32) - i3;
            int i5 = (bArr[i3] & 128) != 0 ? 1 : 0;
            byteArrayOutputStream.write(2);
            byteArrayOutputStream.write(i4 + i5);
            if (i5 != 0) {
                byteArrayOutputStream.write(0);
            }
            byteArrayOutputStream.write(bArr, i3, i4);
        }
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
        byteArrayOutputStream2.write(48);
        byteArrayOutputStream2.write(byteArrayOutputStream.size());
        byteArrayOutputStream.writeTo(byteArrayOutputStream2);
        return byteArrayOutputStream2.toByteArray();
    }

    public byte[] a() {
        return this.a.getEncoded();
    }

    public byte[] b() {
        return (byte[]) this.b.clone();
    }

    public boolean d() {
        return this.d;
    }

    public String e() {
        return this.c;
    }

    public boolean h(String str) {
        if (str == null) {
            return false;
        }
        String str2 = this.c;
        Charset charset = StandardCharsets.US_ASCII;
        return MessageDigest.isEqual(str2.getBytes(charset), str.toLowerCase(Locale.ROOT).getBytes(charset));
    }

    public boolean j(byte[] bArr) throws NoSuchPaddingException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
        if (bArr.length == 0 || bArr.length > 1024) {
            SyntheticRefs.c("MFi response size");
            return false;
        }
        if ("EC".equals(this.a.getPublicKey().getAlgorithm())) {
            Signature signature = Signature.getInstance("NONEwithECDSA");
            signature.initVerify(this.a.getPublicKey());
            signature.update(this.b);
            if (bArr.length == 64) {
                bArr = i(bArr);
            }
            return signature.verify(bArr);
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(2, this.a.getPublicKey());
        byte[] bArr2 = new byte[35];
        System.arraycopy(new byte[]{48, 33, 48, 9, 6, 5, 43, 14, 3, 2, 26, 5, 0, 4, 20}, 0, bArr2, 0, 15);
        System.arraycopy(this.b, 0, bArr2, 15, 20);
        return MessageDigest.isEqual(bArr2, cipher.doFinal(bArr));
    }

    public m5(byte[] bArr) {
        this(bArr, false);
    }
}
