package io.carplay.app.secured;

import java.io.IOException;
import java.security.Provider;
import java.util.List;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class z2 implements j5.a, RsaAesCrypto.a, VideoCipher.AvcVideoInput.c {
    public /* synthetic */ Object a;
    public /* synthetic */ Object b;

    public /* synthetic */ z2(DisplaySession t4Var, Provider provider) {
        this.a = t4Var;
        this.b = provider;
    }

    /* jadx 把上面这个合成构造器的调用点展开成了 `new z2()` + 逐字段赋值，
       故此处补一个无参构造器与之对应（语义等价：只做 Object.<init>）。 */
    public /* synthetic */ z2() {
    }

    @Override // io.carplay.app.secured.RsaAesCrypto.a
    public o a(byte[] bArr) {
        return ((DisplaySession) this.a).G((Provider) this.b, bArr);
    }

    @Override // io.carplay.app.secured.VideoCipher.AvcVideoInput.c
    public void c(List list, long j, long j2, long j3) throws IOException {
        ((w4) this.a).g((w4.b) this.b, list, j, j2, j3);
    }

    @Override // io.carplay.app.secured.j5.a
    public void run() {
        try {
            ((MfiCertificateVerifier) this.a).M((j5.c) this.b);
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }
}
