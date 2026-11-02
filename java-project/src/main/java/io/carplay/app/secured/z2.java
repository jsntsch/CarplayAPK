package io.carplay.app.secured;

import java.io.IOException;
import java.security.Provider;
import java.util.List;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class z2 implements j5.a, f6.a, VideoCipher.b.c {
    public /* synthetic */ Object a;
    public /* synthetic */ Object b;

    public /* synthetic */ z2(t4 t4Var, Provider provider) {
        this.a = t4Var;
        this.b = provider;
    }

    @Override // io.carplay.app.secured.f6.a
    public o a(byte[] bArr) {
        return ((t4) this.a).G((Provider) this.b, bArr);
    }

    @Override // io.carplay.app.secured.VideoCipher.b.c
    public void c(List list, long j, long j2, long j3) {
        ((w4) this.a).g((w4.b) this.b, list, j, j2, j3);
    }

    @Override // io.carplay.app.secured.j5.a
    public void run() {
        ((MfiCertificateVerifier) this.a).M((j5.c) this.b);
    }
}
