package org.bouncycastle.crypto.signers;

import io.carplay.app.secured.Iap2Csm;
import io.carplay.app.secured.d0;
import io.carplay.app.secured.e;
import io.carplay.app.secured.i2;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

/* JADX INFO: loaded from: classes.dex */
public class Ed25519Signer {
    private final b buffer = new b();
    private boolean forSigning;
    private Ed25519PrivateKeyParameters privateKey;
    private Ed25519PublicKeyParameters publicKey;

    public static final class b extends ByteArrayOutputStream {
        private b() {
        }

        public synchronized byte[] b(Ed25519PrivateKeyParameters ed25519PrivateKeyParameters) {
            byte[] bArr;
            bArr = new byte[64];
            ed25519PrivateKeyParameters.sign(0, null, ((ByteArrayOutputStream) this).buf, 0, ((ByteArrayOutputStream) this).count, bArr, 0);
            reset();
            return bArr;
        }

        public synchronized boolean c(Ed25519PublicKeyParameters ed25519PublicKeyParameters, byte[] bArr) {
            if (64 != bArr.length) {
                reset();
                return false;
            }
            boolean zVerify = ed25519PublicKeyParameters.verify(0, null, ((ByteArrayOutputStream) this).buf, 0, ((ByteArrayOutputStream) this).count, bArr, 0);
            reset();
            return zVerify;
        }

        @Override // java.io.ByteArrayOutputStream
        public synchronized void reset() {
            e.f(((ByteArrayOutputStream) this).buf, 0, ((ByteArrayOutputStream) this).count, (byte) 0);
            ((ByteArrayOutputStream) this).count = 0;
        }
    }

    public byte[] generateSignature() {
        Ed25519PrivateKeyParameters ed25519PrivateKeyParameters;
        if (this.forSigning && (ed25519PrivateKeyParameters = this.privateKey) != null) {
            return this.buffer.b(ed25519PrivateKeyParameters);
        }
        d0.d("Ed25519Signer not initialised for signature generation.");
        return null;
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        this.forSigning = z;
        Ed25519PublicKeyParameters ed25519PublicKeyParameters = null;
        if (z) {
            this.privateKey = (Ed25519PrivateKeyParameters) cipherParameters;
        } else {
            this.privateKey = null;
            ed25519PublicKeyParameters = (Ed25519PublicKeyParameters) cipherParameters;
        }
        this.publicKey = ed25519PublicKeyParameters;
        i2.a(org.bouncycastle.crypto.signers.a.a("Ed25519", Iap2Csm.b, cipherParameters, z));
        reset();
    }

    public void reset() {
        this.buffer.reset();
    }

    public void update(byte b2) throws IOException {
        this.buffer.write(b2);
    }

    public boolean verifySignature(byte[] bArr) {
        Ed25519PublicKeyParameters ed25519PublicKeyParameters;
        if (!this.forSigning && (ed25519PublicKeyParameters = this.publicKey) != null) {
            return this.buffer.c(ed25519PublicKeyParameters, bArr);
        }
        d0.d("Ed25519Signer not initialised for verification");
        return false;
    }

    public void update(byte[] bArr, int i, int i2) throws IOException {
        this.buffer.write(bArr, i, i2);
    }
}
