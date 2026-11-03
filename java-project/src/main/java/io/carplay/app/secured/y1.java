package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public class y1 extends ChaCha20 {
    @Override // io.carplay.app.secured.ChaCha20
    public void a() {
        int[] iArr = this.c;
        int i = iArr[12] + 1;
        iArr[12] = i;
        if (i != 0) {
            return;
        }
        SyntheticRefs.d("attempt to increase counter past 2^32.");
    }

    @Override // io.carplay.app.secured.ChaCha20
    public void b(byte[] bArr) {
        z1.m(this.a, this.c, this.d);
        LittleEndianIntCodec.e(this.d, bArr, 0);
    }

    @Override // io.carplay.app.secured.ChaCha20
    public String c() {
        return "ChaCha7539";
    }

    @Override // io.carplay.app.secured.ChaCha20
    public int d() {
        return 12;
    }

    @Override // io.carplay.app.secured.ChaCha20
    public void j() {
        this.c[12] = 0;
    }

    @Override // io.carplay.app.secured.ChaCha20
    public void l(byte[] bArr, byte[] bArr2) {
        if (bArr != null) {
            if (bArr.length != 32) {
                throw new IllegalArgumentException(c() + " requires 256 bit key");
            }
            g(bArr.length, this.c, 0);
            LittleEndianIntCodec.h(bArr, 0, this.c, 4, 8);
        }
        LittleEndianIntCodec.h(bArr2, 0, this.c, 13, 3);
    }
}
