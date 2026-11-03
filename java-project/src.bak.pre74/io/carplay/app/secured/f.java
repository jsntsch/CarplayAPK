package io.carplay.app.secured;

import org.bouncycastle.crypto.CipherParameters;

/* JADX INFO: loaded from: classes.dex */
public abstract class f implements CipherParameters {
    boolean privateKey;

    public f(boolean z) {
        this.privateKey = z;
    }

    public boolean isPrivate() {
        return this.privateKey;
    }
}
