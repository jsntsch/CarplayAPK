package org.bouncycastle.crypto.params;

import io.carplay.app.secured.ByteArrays;
import org.bouncycastle.crypto.CipherParameters;

/* JADX INFO: loaded from: classes.dex */
public class AEADParameters implements CipherParameters {
    private byte[] associatedText;
    private KeyParameter key;
    private int macSize;
    private byte[] nonce;

    public AEADParameters(KeyParameter keyParameter, int i, byte[] bArr, byte[] bArr2) {
        this.key = keyParameter;
        this.nonce = ByteArrays.d(bArr);
        this.macSize = i;
        this.associatedText = ByteArrays.d(bArr2);
    }

    public byte[] getAssociatedText() {
        return ByteArrays.d(this.associatedText);
    }

    public KeyParameter getKey() {
        return this.key;
    }

    public int getMacSize() {
        return this.macSize;
    }

    public byte[] getNonce() {
        return ByteArrays.d(this.nonce);
    }

    public AEADParameters(KeyParameter keyParameter, int i, byte[] bArr) {
        this(keyParameter, i, bArr, null);
    }
}
