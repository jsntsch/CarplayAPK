package org.bouncycastle.crypto.params;

import io.carplay.app.secured.d0;
import io.carplay.app.secured.e;
import io.carplay.app.secured.f;
import io.carplay.app.secured.k7;
import io.carplay.app.secured.l6;
import java.io.EOFException;
import java.io.InputStream;
import java.security.SecureRandom;

/* JADX INFO: loaded from: classes.dex */
public final class X25519PrivateKeyParameters extends f {
    public static final int KEY_SIZE = 32;
    public static final int SECRET_SIZE = 32;
    private final byte[] data;

    public X25519PrivateKeyParameters(InputStream inputStream) throws EOFException {
        super(true);
        byte[] bArr = new byte[32];
        this.data = bArr;
        if (32 != l6.a(inputStream, bArr)) {
            throw new EOFException("EOF encountered in middle of X25519 private key");
        }
    }

    private static byte[] validate(byte[] bArr) {
        if (bArr.length == 32) {
            return bArr;
        }
        d0.b("'buf' must have length 32");
        return null;
    }

    public void encode(byte[] bArr, int i) {
        System.arraycopy(this.data, 0, bArr, i, 32);
    }

    public X25519PublicKeyParameters generatePublicKey() {
        byte[] bArr = new byte[32];
        k7.e(this.data, 0, bArr, 0);
        return new X25519PublicKeyParameters(bArr, 0);
    }

    public void generateSecret(X25519PublicKeyParameters x25519PublicKeyParameters, byte[] bArr, int i) {
        byte[] bArr2 = new byte[32];
        x25519PublicKeyParameters.encode(bArr2, 0);
        if (k7.a(this.data, 0, bArr2, 0, bArr, i)) {
            return;
        }
        d0.d("X25519 agreement failed");
    }

    public byte[] getEncoded() {
        return e.d(this.data);
    }

    public X25519PrivateKeyParameters(SecureRandom secureRandom) {
        super(true);
        byte[] bArr = new byte[32];
        this.data = bArr;
        k7.d(secureRandom, bArr);
    }

    public X25519PrivateKeyParameters(byte[] bArr) {
        this(validate(bArr), 0);
    }

    public X25519PrivateKeyParameters(byte[] bArr, int i) {
        super(true);
        byte[] bArr2 = new byte[32];
        this.data = bArr2;
        System.arraycopy(bArr, i, bArr2, 0, 32);
    }
}
