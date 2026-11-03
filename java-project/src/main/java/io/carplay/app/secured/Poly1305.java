package io.carplay.app.secured;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.KeyParameter;

/* JADX INFO: loaded from: classes.dex */
/**
 * Poly1305
 *
 * <p>原混淆名：{@code io.carplay.app.secured.n5}（jadx 产物 n5.java）
 *
 * <p>命名依据：Poly1305 key must be 256 bits
 */
public class Poly1305 implements l3 {
    private int b;
    private int c;
    private int d;
    private int e;
    private int f;
    private int g;
    private int h;
    private int i;
    private int j;
    private int k;
    private int l;
    private int m;
    private int n;
    private int q;
    private int r;
    private int s;
    private int t;
    private int u;
    private final byte[] a = new byte[1];
    private final byte[] o = new byte[16];
    private int p = 0;

    private static final long f(int i, int i2) {
        return (((long) i) & 4294967295L) * ((long) i2);
    }

    private void g() {
        int i = this.p;
        if (i < 16) {
            this.o[i] = 1;
            for (int i2 = i + 1; i2 < 16; i2++) {
                this.o[i2] = 0;
            }
        }
        long jF = LittleEndianIntCodec.f(this.o, 0);
        long j = jF & 4294967295L;
        long jF2 = ((long) LittleEndianIntCodec.f(this.o, 4)) & 4294967295L;
        long jF3 = ((long) LittleEndianIntCodec.f(this.o, 8)) & 4294967295L;
        long jF4 = 4294967295L & ((long) LittleEndianIntCodec.f(this.o, 12));
        int i3 = (int) (((long) this.q) + (jF & 67108863));
        this.q = i3;
        this.r = (int) (((long) this.r) + ((((jF2 << 32) | j) >>> 26) & 67108863));
        this.s = (int) (((long) this.s) + (((jF2 | (jF3 << 32)) >>> 20) & 67108863));
        this.t = (int) (((long) this.t) + ((((jF4 << 32) | jF3) >>> 14) & 67108863));
        int i4 = (int) (((long) this.u) + (jF4 >>> 8));
        this.u = i4;
        if (this.p == 16) {
            this.u = i4 + 16777216;
        }
        long jF5 = f(i3, this.b) + f(this.r, this.j) + f(this.s, this.i) + f(this.t, this.h) + f(this.u, this.g);
        long jF6 = f(this.q, this.c) + f(this.r, this.b) + f(this.s, this.j) + f(this.t, this.i) + f(this.u, this.h);
        long jF7 = f(this.q, this.d) + f(this.r, this.c) + f(this.s, this.b) + f(this.t, this.j) + f(this.u, this.i);
        long jF8 = f(this.q, this.e) + f(this.r, this.d) + f(this.s, this.c) + f(this.t, this.b) + f(this.u, this.j);
        long jF9 = f(this.q, this.f) + f(this.r, this.e) + f(this.s, this.d) + f(this.t, this.c) + f(this.u, this.b);
        long j2 = jF6 + (jF5 >>> 26);
        long j3 = jF7 + (j2 >>> 26);
        this.s = ((int) j3) & 67108863;
        long j4 = jF8 + (j3 >>> 26);
        this.t = ((int) j4) & 67108863;
        long j5 = jF9 + (j4 >>> 26);
        this.u = ((int) j5) & 67108863;
        int i5 = (((int) (j5 >>> 26)) * 5) + (((int) jF5) & 67108863);
        this.r = (((int) j2) & 67108863) + (i5 >>> 26);
        this.q = i5 & 67108863;
    }

    private void i(byte[] bArr, byte[] bArr2) {
        if (bArr.length != 32) {
            SyntheticRefs.b("Poly1305 key must be 256 bits.");
            return;
        }
        int iF = LittleEndianIntCodec.f(bArr, 0);
        int iF2 = LittleEndianIntCodec.f(bArr, 4);
        int iF3 = LittleEndianIntCodec.f(bArr, 8);
        int iF4 = LittleEndianIntCodec.f(bArr, 12);
        this.b = 67108863 & iF;
        int i = ((iF >>> 26) | (iF2 << 6)) & 67108611;
        this.c = i;
        int i2 = ((iF2 >>> 20) | (iF3 << 12)) & 67092735;
        this.d = i2;
        int i3 = ((iF3 >>> 14) | (iF4 << 18)) & 66076671;
        this.e = i3;
        int i4 = (iF4 >>> 8) & 1048575;
        this.f = i4;
        this.g = i * 5;
        this.h = i2 * 5;
        this.i = i3 * 5;
        this.j = i4 * 5;
        this.k = LittleEndianIntCodec.f(bArr, 16);
        this.l = LittleEndianIntCodec.f(bArr, 4 + 16);
        this.m = LittleEndianIntCodec.f(bArr, 8 + 16);
        this.n = LittleEndianIntCodec.f(bArr, 16 + 12);
    }

    @Override // io.carplay.app.secured.l3
    public int a(byte[] bArr, int i) {
        if (i + 16 > bArr.length) {
            throw new OutputLengthException("Output buffer is too short.");
        }
        if (this.p > 0) {
            g();
        }
        int i2 = this.r;
        int i3 = this.q;
        int i4 = i2 + (i3 >>> 26);
        int i5 = this.s + (i4 >>> 26);
        int i6 = this.t + (i5 >>> 26);
        int i7 = i5 & 67108863;
        int i8 = this.u + (i6 >>> 26);
        int i9 = i6 & 67108863;
        int i10 = ((i8 >>> 26) * 5) + (i3 & 67108863);
        int i11 = i8 & 67108863;
        int i12 = (i4 & 67108863) + (i10 >>> 26);
        int i13 = i10 & 67108863;
        int i14 = i13 + 5;
        int i15 = (i14 >>> 26) + i12;
        int i16 = (i15 >>> 26) + i7;
        int i17 = (i16 >>> 26) + i9;
        int i18 = 67108863 & i17;
        int i19 = ((i17 >>> 26) + i11) - 67108864;
        int i20 = (i19 >>> 31) - 1;
        int i21 = ~i20;
        int i22 = (i13 & i21) | (i14 & 67108863 & i20);
        this.q = i22;
        int i23 = (i12 & i21) | (i15 & 67108863 & i20);
        this.r = i23;
        int i24 = (i7 & i21) | (i16 & 67108863 & i20);
        this.s = i24;
        int i25 = (i18 & i20) | (i9 & i21);
        this.t = i25;
        int i26 = (i11 & i21) | (i19 & i20);
        this.u = i26;
        long j = (((long) ((i23 << 26) | i22)) & 4294967295L) + (((long) this.k) & 4294967295L);
        long j2 = (((long) ((i23 >>> 6) | (i24 << 20))) & 4294967295L) + (((long) this.l) & 4294967295L);
        long j3 = (((long) ((i24 >>> 12) | (i25 << 14))) & 4294967295L) + (((long) this.m) & 4294967295L);
        long j4 = (((long) ((i25 >>> 18) | (i26 << 8))) & 4294967295L) + (((long) this.n) & 4294967295L);
        LittleEndianIntCodec.d((int) j, bArr, i);
        long j5 = j2 + (j >>> 32);
        LittleEndianIntCodec.d((int) j5, bArr, i + 4);
        long j6 = j3 + (j5 >>> 32);
        LittleEndianIntCodec.d((int) j6, bArr, i + 8);
        LittleEndianIntCodec.d((int) (j4 + (j6 >>> 32)), bArr, i + 12);
        h();
        return 16;
    }

    @Override // io.carplay.app.secured.l3
    public void b(byte[] bArr, int i, int i2) {
        int i3 = 0;
        while (i2 > i3) {
            if (this.p == 16) {
                g();
                this.p = 0;
            }
            int iMin = Math.min(i2 - i3, 16 - this.p);
            System.arraycopy(bArr, i3 + i, this.o, this.p, iMin);
            i3 += iMin;
            this.p += iMin;
        }
    }

    @Override // io.carplay.app.secured.l3
    public void c(CipherParameters cipherParameters) {
        if (!(cipherParameters instanceof KeyParameter)) {
            SyntheticRefs.b("Poly1305 requires a key.");
        } else {
            i(((KeyParameter) cipherParameters).getKey(), null);
            h();
        }
    }

    @Override // io.carplay.app.secured.l3
    public void d(byte b) {
        byte[] bArr = this.a;
        bArr[0] = b;
        b(bArr, 0, 1);
    }

    @Override // io.carplay.app.secured.l3
    public int e() {
        return 16;
    }

    public void h() {
        this.p = 0;
        this.u = 0;
        this.t = 0;
        this.s = 0;
        this.r = 0;
        this.q = 0;
    }
}
