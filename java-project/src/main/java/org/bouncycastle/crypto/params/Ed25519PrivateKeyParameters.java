package org.bouncycastle.crypto.params;

import io.carplay.app.secured.SyntheticRefs;
import io.carplay.app.secured.X25519;
import io.carplay.app.secured.e;
import io.carplay.app.secured.f;
import io.carplay.app.secured.l6;
import java.io.EOFException;
import java.io.InputStream;
import java.security.SecureRandom;

/* JADX INFO: loaded from: classes.dex */
public final class Ed25519PrivateKeyParameters extends f {
    public static final int KEY_SIZE = 32;
    public static final int SIGNATURE_SIZE = 64;
    private Ed25519PublicKeyParameters cachedPublicKey;
    private final byte[] data;

    public Ed25519PrivateKeyParameters(InputStream inputStream) throws EOFException {
        super(true);
        byte[] bArr = new byte[32];
        this.data = bArr;
        if (32 != l6.a(inputStream, bArr)) {
            throw new EOFException("EOF encountered in middle of Ed25519 private key");
        }
    }

    private static byte[] validate(byte[] bArr) {
        if (bArr.length == 32) {
            return bArr;
        }
        SyntheticRefs.b("'buf' must have length 32");
        return null;
    }

    public void encode(byte[] bArr, int i) {
        System.arraycopy(this.data, 0, bArr, i, 32);
    }

    public Ed25519PublicKeyParameters generatePublicKey() {
        Ed25519PublicKeyParameters ed25519PublicKeyParameters;
        synchronized (this.data) {
            try {
                if (this.cachedPublicKey == null) {
                    this.cachedPublicKey = new Ed25519PublicKeyParameters(X25519.p(this.data, 0));
                }
                ed25519PublicKeyParameters = this.cachedPublicKey;
            } catch (Throwable th) {
                throw th;
            }
        }
        return ed25519PublicKeyParameters;
    }

    public byte[] getEncoded() {
        return e.d(this.data);
    }

    public void sign(int i, byte[] bArr, byte[] bArr2, int i2, int i3, byte[] bArr3, int i4) {
        byte[] bArr4 = new byte[32];
        generatePublicKey().encode(bArr4, 0);
        if (i == 0) {
            if (bArr == null) {
                X25519.P(this.data, 0, bArr4, 0, bArr2, i2, i3, bArr3, i4);
                return;
            } else {
                SyntheticRefs.b("ctx");
                return;
            }
        }
        if (i == 1) {
            if (bArr == null) {
                throw new NullPointerException("'ctx' cannot be null");
            }
            if (bArr.length <= 255) {
                X25519.Q(this.data, 0, bArr4, 0, bArr, bArr2, i2, i3, bArr3, i4);
                return;
            } else {
                SyntheticRefs.b("ctx");
                return;
            }
        }
        if (i != 2) {
            SyntheticRefs.b("algorithm");
            return;
        }
        if (bArr == null) {
            throw new NullPointerException("'ctx' cannot be null");
        }
        if (bArr.length > 255) {
            SyntheticRefs.b("ctx");
        } else if (64 == i3) {
            X25519.R(this.data, 0, bArr4, 0, bArr, bArr2, i2, bArr3, i4);
        } else {
            SyntheticRefs.b("msgLen");
        }
    }

    public Ed25519PrivateKeyParameters(SecureRandom secureRandom) {
        super(true);
        byte[] bArr = new byte[32];
        this.data = bArr;
        X25519.o(secureRandom, bArr);
    }

    public Ed25519PrivateKeyParameters(byte[] bArr) {
        this(validate(bArr), 0);
    }

    public Ed25519PrivateKeyParameters(byte[] bArr, int i) {
        super(true);
        byte[] bArr2 = new byte[32];
        this.data = bArr2;
        System.arraycopy(bArr, i, bArr2, 0, 32);
    }

    public void sign(int i, Ed25519PublicKeyParameters ed25519PublicKeyParameters, byte[] bArr, byte[] bArr2, int i2, int i3, byte[] bArr3, int i4) {
        sign(i, bArr, bArr2, i2, i3, bArr3, i4);
    }
}
