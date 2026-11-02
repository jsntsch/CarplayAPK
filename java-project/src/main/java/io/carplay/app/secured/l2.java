package io.carplay.app.secured;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;

/* JADX INFO: loaded from: classes.dex */
public class l2 implements CipherParameters {
    private BigInteger a;
    private BigInteger b;
    private BigInteger c;
    private m2 d;

    public l2(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, m2 m2Var) {
        this.a = bigInteger3;
        this.c = bigInteger;
        this.b = bigInteger2;
        this.d = m2Var;
    }

    public BigInteger a() {
        return this.a;
    }

    public BigInteger b() {
        return this.c;
    }

    public BigInteger c() {
        return this.b;
    }

    public m2 d() {
        return this.d;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof l2)) {
            return false;
        }
        l2 l2Var = (l2) obj;
        return l2Var.b().equals(this.c) && l2Var.c().equals(this.b) && l2Var.a().equals(this.a);
    }

    public int hashCode() {
        return a().hashCode() ^ (b().hashCode() ^ c().hashCode());
    }
}
