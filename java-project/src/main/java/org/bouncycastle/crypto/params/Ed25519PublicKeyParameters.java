package org.bouncycastle.crypto.params;

import io.carplay.app.secured.SyntheticRefs;
import io.carplay.app.secured.X25519;
import io.carplay.app.secured.f;
import io.carplay.app.secured.l6;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
public final class Ed25519PublicKeyParameters extends f {
    public static final int KEY_SIZE = 32;
    private final X25519.h publicPoint;

    public Ed25519PublicKeyParameters(InputStream inputStream) throws IOException {
        super(false);
        byte[] bArr = new byte[32];
        if (32 != l6.a(inputStream, bArr)) {
            throw new EOFException("EOF encountered in middle of Ed25519 public key");
        }
        this.publicPoint = parse(bArr, 0);
    }

    private static X25519.h parse(byte[] bArr, int i) {
        X25519.h hVarS = X25519.S(bArr, i);
        if (hVarS != null) {
            return hVarS;
        }
        SyntheticRefs.b("invalid public key");
        return null;
    }

    private static byte[] validate(byte[] bArr) {
        if (bArr.length == 32) {
            return bArr;
        }
        SyntheticRefs.b("'buf' must have length 32");
        return null;
    }

    public void encode(byte[] bArr, int i) {
        X25519.l(this.publicPoint, bArr, i);
    }

    public byte[] getEncoded() {
        byte[] bArr = new byte[32];
        encode(bArr, 0);
        return bArr;
    }

    public boolean verify(int i, byte[] bArr, byte[] bArr2, int i2, int i3, byte[] bArr3, int i4) {
        String str;
        if (i == 0) {
            if (bArr == null) {
                return X25519.T(bArr3, i4, this.publicPoint, bArr2, i2, i3);
            }
            SyntheticRefs.b("ctx");
            return false;
        }
        if (i == 1) {
            if (bArr == null) {
                throw new NullPointerException("'ctx' cannot be null");
            }
            if (bArr.length <= 255) {
                return X25519.U(bArr3, i4, this.publicPoint, bArr, bArr2, i2, i3);
            }
            SyntheticRefs.b("ctx");
            return false;
        }
        if (i != 2) {
            str = "algorithm";
        } else {
            if (bArr == null) {
                throw new NullPointerException("'ctx' cannot be null");
            }
            if (bArr.length > 255) {
                SyntheticRefs.b("ctx");
                return false;
            }
            if (64 == i3) {
                return X25519.V(bArr3, i4, this.publicPoint, bArr, bArr2, i2);
            }
            str = "msgLen";
        }
        SyntheticRefs.b(str);
        return false;
    }

    public Ed25519PublicKeyParameters(X25519.h hVar) {
        super(false);
        if (hVar == null) {
            throw new NullPointerException("'publicPoint' cannot be null");
        }
        this.publicPoint = hVar;
    }

    public Ed25519PublicKeyParameters(byte[] bArr) {
        this(validate(bArr), 0);
    }

    public Ed25519PublicKeyParameters(byte[] bArr, int i) {
        super(false);
        this.publicPoint = parse(bArr, i);
    }
}
