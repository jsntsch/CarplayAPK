package org.bouncycastle.crypto.params;

import io.carplay.app.secured.d0;
import io.carplay.app.secured.e;
import io.carplay.app.secured.f;
import io.carplay.app.secured.l6;
import java.io.EOFException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
public final class X25519PublicKeyParameters extends f {
    public static final int KEY_SIZE = 32;
    private final byte[] data;

    public X25519PublicKeyParameters(InputStream inputStream) throws EOFException {
        super(false);
        byte[] bArr = new byte[32];
        this.data = bArr;
        if (32 != l6.a(inputStream, bArr)) {
            throw new EOFException("EOF encountered in middle of X25519 public key");
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

    public byte[] getEncoded() {
        return e.d(this.data);
    }

    public X25519PublicKeyParameters(byte[] bArr) {
        this(validate(bArr), 0);
    }

    public X25519PublicKeyParameters(byte[] bArr, int i) {
        super(false);
        byte[] bArr2 = new byte[32];
        this.data = bArr2;
        System.arraycopy(bArr, i, bArr2, 0, 32);
    }
}
