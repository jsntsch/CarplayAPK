package org.bouncycastle.crypto.signers;

import io.carplay.app.secured.e2;
import io.carplay.app.secured.f2;
import io.carplay.app.secured.p2;
import org.bouncycastle.crypto.CipherParameters;

/* JADX INFO: loaded from: classes.dex */
abstract class a {
    public static e2 a(String str, int i, CipherParameters cipherParameters, boolean z) {
        return new p2(str, i, cipherParameters, b(z));
    }

    public static f2 b(boolean z) {
        return z ? f2.SIGNING : f2.VERIFYING;
    }
}
