package io.carplay.app.secured;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.KeyParameter;

/* JADX INFO: loaded from: classes.dex */
/**
 * ChaCha20
 *
 * <p>原混淆名：{@code io.carplay.app.secured.b6}（jadx 产物 b6.java）
 *
 * <p>命名依据：expand 16-byte k / 32-byte k
 */
public abstract class ChaCha20 {
    private static final int[] j = y4.i(m6.a("expand 16-byte kexpand 32-byte k"), 0, 8);
    protected static final byte[] k = m6.a("expand 32-byte k");
    protected static final byte[] l = m6.a("expand 16-byte k");
    protected int a;
    private int b;
    protected int[] c;
    protected int[] d;
    private byte[] e;
    private boolean f;
    private int g;
    private int h;
    private int i;

    public ChaCha20(int i) {
        this.b = 0;
        this.c = new int[16];
        this.d = new int[16];
        this.e = new byte[64];
        this.f = false;
        if (i <= 0 || (i & 1) != 0) {
            d0.b("'rounds' must be a positive, even number");
            throw null;
        }
        this.a = i;
    }

    private boolean f(int i) {
        int i2 = this.g + i;
        this.g = i2;
        if (i2 < i && i2 >= 0) {
            int i3 = this.h + 1;
            this.h = i3;
            if (i3 == 0) {
                int i4 = this.i + 1;
                this.i = i4;
                if ((i4 & 32) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void k() {
        this.g = 0;
        this.h = 0;
        this.i = 0;
    }

    public abstract void a();

    public abstract void b(byte[] bArr);

    public abstract String c();

    public abstract int d();

    public void e(boolean z, CipherParameters cipherParameters) {
        if (!(cipherParameters instanceof c5)) {
            throw new IllegalArgumentException(c() + " Init parameters must include an IV");
        }
        c5 c5Var = (c5) cipherParameters;
        byte[] bArrA = c5Var.a();
        if (bArrA == null || bArrA.length != d()) {
            throw new IllegalArgumentException(c() + " requires exactly " + d() + " bytes of IV");
        }
        CipherParameters cipherParametersB = c5Var.b();
        if (cipherParametersB == null) {
            if (!this.f) {
                throw new IllegalStateException(c() + " KeyParameter can not be null for first initialisation");
            }
            l(null, bArrA);
        } else {
            if (!(cipherParametersB instanceof KeyParameter)) {
                throw new IllegalArgumentException(c() + " Init parameters must contain a KeyParameter (or null for re-init)");
            }
            byte[] key = ((KeyParameter) cipherParametersB).getKey();
            l(key, bArrA);
            i2.a(new p2(c(), key.length * 8, cipherParameters, o6.a(z)));
        }
        i();
        this.f = true;
    }

    public void g(int i, int[] iArr, int i2) {
        int i3 = (i - 16) / 4;
        int[] iArr2 = j;
        iArr[i2] = iArr2[i3];
        iArr[i2 + 1] = iArr2[i3 + 1];
        iArr[i2 + 2] = iArr2[i3 + 2];
        iArr[i2 + 3] = iArr2[i3 + 3];
    }

    public int h(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        if (!this.f) {
            throw new IllegalStateException(c() + " not initialised");
        }
        if (i + i2 > bArr.length) {
            throw new n2("input buffer too short");
        }
        if (i3 + i2 > bArr2.length) {
            throw new x4("output buffer too short");
        }
        if (f(i2)) {
            throw new d4("2^70 byte limit per IV would be exceeded; Change IV");
        }
        for (int i4 = 0; i4 < i2; i4++) {
            byte[] bArr3 = this.e;
            int i5 = this.b;
            bArr2[i4 + i3] = (byte) (bArr3[i5] ^ bArr[i4 + i]);
            int i6 = (i5 + 1) & 63;
            this.b = i6;
            if (i6 == 0) {
                a();
                b(this.e);
            }
        }
        return i2;
    }

    public void i() {
        this.b = 0;
        k();
        j();
        b(this.e);
    }

    public abstract void j();

    public abstract void l(byte[] bArr, byte[] bArr2);

    public ChaCha20() {
        this(20);
    }
}
