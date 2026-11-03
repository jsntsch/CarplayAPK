package io.carplay.app.secured;

import java.security.InvalidAlgorithmParameterException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/* JADX INFO: loaded from: classes.dex */
public final class g3 implements o {
    private final String a;
    private final Provider b;
    private final SecretKey c;
    private final AlgorithmParameterSpec d;

    public g3(String str, Provider provider, SecretKey secretKey, AlgorithmParameterSpec algorithmParameterSpec) throws NoSuchPaddingException, NoSuchAlgorithmException {
        Objects.requireNonNull(str);
        this.a = str;
        Objects.requireNonNull(provider);
        this.b = provider;
        Objects.requireNonNull(secretKey);
        this.c = secretKey;
        this.d = algorithmParameterSpec;
        String[] strArrSplit = str.split("/", -1);
        if (strArrSplit.length != 3 || strArrSplit[0].isBlank() || strArrSplit[1].isBlank() || strArrSplit[2].isBlank()) {
            SyntheticRefs.b("explicit algorithm/mode/padding required");
            throw null;
        }
        if (strArrSplit[1].equalsIgnoreCase("ECB") || algorithmParameterSpec != null) {
            Cipher.getInstance(str, provider);
        } else {
            SyntheticRefs.b("explicit parameters required for non-ECB mode");
            throw null;
        }
    }

    private byte[] c(int i, byte[] bArr) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(this.a, this.b);
        AlgorithmParameterSpec algorithmParameterSpec = this.d;
        SecretKey secretKey = this.c;
        if (algorithmParameterSpec == null) {
            cipher.init(i, secretKey);
        } else {
            cipher.init(i, secretKey, algorithmParameterSpec);
        }
        Objects.requireNonNull(bArr);
        return cipher.doFinal(bArr);
    }

    @Override // io.carplay.app.secured.o
    public byte[] a(byte[] bArr) {
        try {
            return c(1, bArr);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES encryption failed", e);
        }
    }

    @Override // io.carplay.app.secured.o
    public byte[] b(byte[] bArr) {
        try {
            return c(2, bArr);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES decryption failed", e);
        }
    }
}
