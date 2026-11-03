package io.carplay.app.secured;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;

/* JADX INFO: loaded from: classes.dex */
/**
 * RsaAesCrypto
 *
 * <p>原混淆名：{@code io.carplay.app.secured.f6}（jadx 产物 f6.java）
 *
 * <p>命名依据：invalid negotiated aes key length / verified target aes configuration required
 */
public final class RsaAesCrypto implements AutoCloseable {
    private KeyPair a;
    private final a b;
    private boolean c;

    @FunctionalInterface
    public interface a {
        o a(byte[] bArr);
    }

    public RsaAesCrypto(a aVar) throws NoSuchAlgorithmException {
        if (aVar == null) {
            SyntheticRefs.b("verified target AES configuration required");
            throw null;
        }
        this.b = aVar;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        this.a = keyPairGenerator.generateKeyPair();
    }

    public synchronized o b(String str) throws GeneralSecurityException {
        if (this.a == null || this.c) {
            throw new IllegalStateException("closed/consumed exchange");
        }
        this.c = true;
        if (str == null || str.length() > 4096) {
            throw new GeneralSecurityException("invalid RSA payload length");
        }
        byte[] bArrDecode = null;
        byte[] bArrDoFinal = null;
        o oVarA;
        try {
            try {
                bArrDecode = Base64.getDecoder().decode(str);
            } catch (IllegalArgumentException unused) {
                throw new GeneralSecurityException("invalid RSA payload encoding");
            }
            if (bArrDecode.length != 256) {
                throw new GeneralSecurityException("RSA-2048 ciphertext length mismatch");
            }
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(2, this.a.getPrivate());
            bArrDoFinal = cipher.doFinal(bArrDecode);
            if (bArrDoFinal.length != 16 && bArrDoFinal.length != 24 && bArrDoFinal.length != 32) {
                throw new GeneralSecurityException("invalid negotiated AES key length");
            }
            oVarA = this.b.a(bArrDoFinal);
            if (oVarA == null) {
                throw new GeneralSecurityException("missing negotiated body cipher");
            }
            return oVarA;
        } finally {
            if (bArrDecode != null) {
                Arrays.fill(bArrDecode, (byte) 0);
            }
            if (bArrDoFinal != null) {
                Arrays.fill(bArrDoFinal, (byte) 0);
            }
            this.a = null;
        }
    }

    public synchronized String c() {
        if (this.a == null || this.c) {
            throw new IllegalStateException("closed/consumed exchange");
        }
        return Base64.getEncoder().encodeToString(this.a.getPublic().getEncoded());
    }

    @Override // java.lang.AutoCloseable
    public synchronized void close() {
        this.a = null;
        this.c = true;
    }
}
