package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public class a6 extends j3 {
    public a6(f2 f2Var) {
        super(f2Var);
        CryptoServicesRegistrar.a(r());
        p();
    }

    @Override // io.carplay.app.secured.q2
    public int a(byte[] bArr, int i) {
        l();
        LittleEndianIntCodec.j(this.f, bArr, i);
        LittleEndianIntCodec.j(this.g, bArr, i + 8);
        LittleEndianIntCodec.j(this.h, bArr, i + 16);
        LittleEndianIntCodec.j(this.i, bArr, i + 24);
        LittleEndianIntCodec.j(this.j, bArr, i + 32);
        LittleEndianIntCodec.j(this.k, bArr, i + 40);
        LittleEndianIntCodec.j(this.l, bArr, i + 48);
        LittleEndianIntCodec.j(this.m, bArr, i + 56);
        p();
        return 64;
    }

    @Override // io.carplay.app.secured.q2
    public String c() {
        return "SHA-512";
    }

    @Override // io.carplay.app.secured.q2
    public int d() {
        return 64;
    }

    @Override // io.carplay.app.secured.j3
    public void p() {
        super.p();
        this.f = 7640891576956012808L;
        this.g = -4942790177534073029L;
        this.h = 4354685564936845355L;
        this.i = -6534734903238641935L;
        this.j = 5840696475078001361L;
        this.k = -7276294671716946913L;
        this.l = 2270897969802886507L;
        this.m = 6620516959819538809L;
    }

    public e2 r() {
        return n6.a(this, 256, this.a);
    }

    public a6() {
        this(f2.ANY);
    }
}
