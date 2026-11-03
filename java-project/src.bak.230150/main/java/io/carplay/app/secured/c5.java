package io.carplay.app.secured;

import org.bouncycastle.crypto.CipherParameters;

/* JADX INFO: loaded from: classes.dex */
public class c5 implements CipherParameters {
    private byte[] a;
    private CipherParameters b;

    public c5(CipherParameters cipherParameters, byte[] bArr, int i, int i2) {
        byte[] bArr2 = new byte[i2];
        this.a = bArr2;
        this.b = cipherParameters;
        System.arraycopy(bArr, i, bArr2, 0, i2);
    }

    public byte[] a() {
        return this.a;
    }

    public CipherParameters b() {
        return this.b;
    }

    public c5(CipherParameters cipherParameters, byte[] bArr) {
        this(cipherParameters, bArr, 0, bArr.length);
    }
}
