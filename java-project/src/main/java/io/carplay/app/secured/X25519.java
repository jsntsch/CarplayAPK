package io.carplay.app.secured;

import java.security.SecureRandom;

/* JADX INFO: loaded from: classes.dex */
/**
 * X25519
 *
 * <p>原混淆名：{@code io.carplay.app.secured.r2}（jadx 产物 r2.java）
 *
 * <p>命名依据：This method is only for use by X25519；对应 hypercar srp6a.rs 同层
 */
public abstract class X25519 {
    private static final byte[] a = {83, 105, 103, 69, 100, 50, 53, 53, 49, 57, 32, 110, 111, 32, 69, 100, 50, 53, 53, 49, 57, 32, 99, 111, 108, 108, 105, 115, 105, 111, 110, 115};
    private static final int[] b = {-19, -1, -1, -1, -1, -1, -1, Integer.MAX_VALUE};
    private static final int[] c = {1886001095, 1339575613, 1980447930, 258412557, -95215574, -959694548, 2013120334, 2047061138};
    private static final int[] d = {-1886001114, -1339575614, -1980447931, -258412558, 95215573, 959694547, -2013120335, 100422509};
    private static final int[] e = {52811034, 25909283, 8072341, 50637101, 13785486, 30858332, 20483199, 20966410, 43936626, 4379245};
    private static final int[] f = {40265304, 26843545, 6710886, 53687091, 13421772, 40265318, 26843545, 6710886, 53687091, 13421772};
    private static final int[] g = {12052516, 1174424, 4087752, 38672185, 20040971, 21899680, 55468344, 20105554, 66708015, 9981791};
    private static final int[] h = {66430571, 45040722, 4842939, 15895846, 18981244, 46308410, 4697481, 8903007, 53646190, 12474675};
    private static final int[] i = {56195235, 47411844, 25868126, 40503822, 57364, 58321048, 30416477, 31930572, 57760639, 10749657};
    private static final int[] j = {45281625, 27714825, 18181821, 13898781, 114729, 49533232, 60832955, 30306712, 48412415, 4722099};
    private static final int[] k = {23454386, 55429651, 2809210, 27797563, 229458, 31957600, 54557047, 27058993, 29715967, 9444199};
    private static final Object l = new Object();
    private static e[] m = null;
    private static e[] n = null;
    private static int[] o = null;

    public static class b {
        int[] a;
        int[] b;
        int[] c;
        int[] d;
        int[] e;

        private b() {
            this.a = l7.j();
            this.b = l7.j();
            this.c = l7.j();
            this.d = l7.j();
            this.e = l7.j();
        }
    }

    public static class c {
        int[] a;
        int[] b;

        private c() {
            this.a = l7.j();
            this.b = l7.j();
        }
    }

    public static class d {
        int[] a;
        int[] b;
        int[] c;
        int[] d;

        private d() {
            this.a = l7.j();
            this.b = l7.j();
            this.c = l7.j();
            this.d = l7.j();
        }
    }

    public static class e {
        int[] a;
        int[] b;
        int[] c;

        private e() {
            this.a = l7.j();
            this.b = l7.j();
            this.c = l7.j();
        }
    }

    public static class f {
        int[] a;
        int[] b;
        int[] c;
        int[] d;

        private f() {
            this.a = l7.j();
            this.b = l7.j();
            this.c = l7.j();
            this.d = l7.j();
        }
    }

    public static class g {
        int[] a;
        int[] b;

        private g() {
            this.a = l7.j();
            this.b = l7.j();
        }
    }

    public static final class h {
        final int[] a;

        public h(int[] iArr) {
            this.a = iArr;
        }
    }

    private static void A(boolean z, f fVar, b bVar, g gVar) {
        int[] iArr;
        int[] iArr2;
        int[] iArr3 = bVar.a;
        int[] iArr4 = bVar.b;
        int[] iArr5 = gVar.a;
        int[] iArCarLifeFrame = bVar.c;
        int[] iArr7 = bVar.d;
        int[] iArr8 = bVar.e;
        if (z) {
            iArr2 = iArr3;
            iArr = iArr4;
        } else {
            iArr = iArr3;
            iArr2 = iArr4;
        }
        l7.c(iArr4, iArr3, iArr4, iArr3);
        l7.E(iArr, fVar.a, iArr);
        l7.E(iArr2, fVar.b, iArr2);
        l7.E(bVar.d, bVar.e, iArr5);
        l7.E(iArr5, fVar.c, iArr5);
        l7.E(bVar.c, fVar.d, iArCarLifeFrame);
        l7.c(iArr4, iArr3, iArr8, iArr7);
        l7.c(iArCarLifeFrame, iArr5, iArr2, iArr);
        l7.E(iArr3, iArr4, bVar.c);
        l7.E(iArr3, iArr7, bVar.a);
        l7.E(iArr4, iArr8, bVar.b);
    }

    private static void B(b bVar, d dVar) {
        l7.i(bVar.a, 0, dVar.a, 0);
        l7.i(bVar.b, 0, dVar.b, 0);
        l7.i(bVar.c, 0, dVar.c, 0);
        l7.E(bVar.d, bVar.e, dVar.d);
    }

    private static void C(c cVar, d dVar) {
        l7.i(cVar.a, 0, dVar.a, 0);
        l7.i(cVar.b, 0, dVar.b, 0);
        l7.H(dVar.c);
        l7.E(cVar.a, cVar.b, dVar.d);
    }

    private static void D(d dVar, f fVar) {
        l7.c(dVar.b, dVar.a, fVar.b, fVar.a);
        l7.E(dVar.d, j, fVar.c);
        int[] iArr = dVar.c;
        l7.a(iArr, iArr, fVar.d);
    }

    private static void E(b bVar) {
        int[] iArr = bVar.a;
        int[] iArr2 = bVar.b;
        int[] iArr3 = bVar.c;
        int[] iArr4 = bVar.d;
        int[] iArr5 = bVar.e;
        l7.a(iArr, iArr2, iArr4);
        l7.L(bVar.a, iArr);
        l7.L(bVar.b, iArr2);
        l7.L(bVar.c, iArr3);
        l7.a(iArr3, iArr3, iArr3);
        l7.c(iArr, iArr2, iArr5, iArr2);
        l7.L(iArr4, iArr4);
        l7.N(iArr5, iArr4, iArr4);
        l7.a(iArr3, iArr2, iArr);
        l7.f(iArr);
        l7.E(iArr, iArr2, bVar.c);
        l7.E(iArr, iArr4, bVar.a);
        l7.E(iArr2, iArr5, bVar.b);
    }

    private static void F(int i2, int i3, e eVar) {
        int i4 = i2 * 240;
        for (int i5 = 0; i5 < 8; i5++) {
            int i6 = ((i5 ^ i3) - 1) >> 31;
            l7.g(i6, o, i4, eVar.a, 0);
            l7.g(i6, o, i4 + 10, eVar.b, 0);
            l7.g(i6, o, i4 + 20, eVar.c, 0);
            i4 += 30;
        }
    }

    private static void G(c cVar, d[] dVarArr, int i2, int i3, g gVar) {
        d dVar = new d();
        C(cVar, dVar);
        dVarArr[i2] = dVar;
        d dVar2 = new d();
        x(dVar, dVar, dVar2, gVar);
        for (int i4 = 1; i4 < i3; i4++) {
            d dVar3 = new d();
            int i5 = i2 + i4;
            x(dVarArr[i5 - 1], dVar2, dVar3, gVar);
            dVarArr[i5] = dVar3;
        }
    }

    private static void H(c cVar, f[] fVarArr, int i2, g gVar) {
        d dVar = new d();
        C(cVar, dVar);
        d dVar2 = new d();
        x(dVar, dVar, dVar2, gVar);
        int i3 = 0;
        while (true) {
            f fVar = new f();
            D(dVar, fVar);
            fVarArr[i3] = fVar;
            i3++;
            if (i3 == i2) {
                return;
            } else {
                x(dVar, dVar2, dVar, gVar);
            }
        }
    }

    private static void I(b bVar) {
        l7.P(bVar.a);
        l7.H(bVar.b);
        l7.H(bVar.c);
        l7.P(bVar.d);
        l7.H(bVar.e);
    }

    public static void J() {
        synchronized (l) {
            try {
                if (o == null) {
                    d[] dVarArr = new d[96];
                    g gVar = new g();
                    c cVar = new c();
                    int[] iArr = e;
                    int i2 = 0;
                    l7.i(iArr, 0, cVar.a, 0);
                    int[] iArr2 = f;
                    l7.i(iArr2, 0, cVar.b, 0);
                    G(cVar, dVarArr, 0, 16, gVar);
                    c cVar2 = new c();
                    l7.i(g, 0, cVar2.a, 0);
                    l7.i(h, 0, cVar2.b, 0);
                    G(cVar2, dVarArr, 16, 16, gVar);
                    b bVar = new b();
                    l7.i(iArr, 0, bVar.a, 0);
                    l7.i(iArr2, 0, bVar.b, 0);
                    l7.H(bVar.c);
                    l7.i(bVar.a, 0, bVar.d, 0);
                    l7.i(bVar.b, 0, bVar.e, 0);
                    int i3 = 4;
                    d[] dVarArr2 = new d[4];
                    for (int i4 = 0; i4 < 4; i4++) {
                        dVarArr2[i4] = new d();
                    }
                    d dVar = new d();
                    int i5 = 0;
                    int i6 = 32;
                    while (i5 < 8) {
                        d dVar2 = new d();
                        int i7 = i2;
                        while (i7 < i3) {
                            if (i7 == 0) {
                                B(bVar, dVar2);
                            } else {
                                B(bVar, dVar);
                                x(dVar2, dVar, dVar2, gVar);
                            }
                            E(bVar);
                            B(bVar, dVarArr2[i7]);
                            if (i5 + i7 != 10) {
                                for (int i8 = 1; i8 < 8; i8++) {
                                    E(bVar);
                                }
                            }
                            i7++;
                            i3 = 4;
                        }
                        int[] iArr3 = dVar2.a;
                        l7.F(iArr3, iArr3);
                        int[] iArr4 = dVar2.d;
                        l7.F(iArr4, iArr4);
                        dVarArr[i6] = dVar2;
                        i6++;
                        for (int i9 = 0; i9 < 3; i9++) {
                            int i10 = 1 << i9;
                            int i11 = 0;
                            while (i11 < i10) {
                                d dVar3 = new d();
                                dVarArr[i6] = dVar3;
                                x(dVarArr[i6 - i10], dVarArr2[i9], dVar3, gVar);
                                i11++;
                                i6++;
                            }
                        }
                        i5++;
                        i3 = 4;
                        i2 = 0;
                    }
                    u(dVarArr);
                    m = new e[16];
                    for (int i12 = 0; i12 < 16; i12++) {
                        d dVar4 = dVarArr[i12];
                        e eVar = new e();
                        int[] iArr5 = dVar4.a;
                        l7.E(iArr5, dVar4.c, iArr5);
                        int[] iArCarLifeFrame = dVar4.b;
                        l7.E(iArCarLifeFrame, dVar4.c, iArCarLifeFrame);
                        l7.c(dVar4.b, dVar4.a, eVar.b, eVar.a);
                        l7.E(dVar4.a, dVar4.b, eVar.c);
                        int[] iArr7 = eVar.c;
                        l7.E(iArr7, k, iArr7);
                        l7.G(eVar.a);
                        l7.G(eVar.b);
                        l7.G(eVar.c);
                        m[i12] = eVar;
                    }
                    n = new e[16];
                    for (int i13 = 0; i13 < 16; i13++) {
                        d dVar5 = dVarArr[16 + i13];
                        e eVar2 = new e();
                        int[] iArr8 = dVar5.a;
                        l7.E(iArr8, dVar5.c, iArr8);
                        int[] iArr9 = dVar5.b;
                        l7.E(iArr9, dVar5.c, iArr9);
                        l7.c(dVar5.b, dVar5.a, eVar2.b, eVar2.a);
                        l7.E(dVar5.a, dVar5.b, eVar2.c);
                        int[] iArr10 = eVar2.c;
                        l7.E(iArr10, k, iArr10);
                        l7.G(eVar2.a);
                        l7.G(eVar2.b);
                        l7.G(eVar2.c);
                        n[i13] = eVar2;
                    }
                    o = l7.k(192);
                    e eVar3 = new e();
                    int i14 = 0;
                    for (int i15 = 32; i15 < 96; i15++) {
                        d dVaCarLifeFrame = dVarArr[i15];
                        int[] iArr11 = dVaCarLifeFrame.a;
                        l7.E(iArr11, dVaCarLifeFrame.c, iArr11);
                        int[] iArr12 = dVaCarLifeFrame.b;
                        l7.E(iArr12, dVaCarLifeFrame.c, iArr12);
                        l7.c(dVaCarLifeFrame.b, dVaCarLifeFrame.a, eVar3.b, eVar3.a);
                        l7.E(dVaCarLifeFrame.a, dVaCarLifeFrame.b, eVar3.c);
                        int[] iArr13 = eVar3.c;
                        l7.E(iArr13, k, iArr13);
                        l7.G(eVar3.a);
                        l7.G(eVar3.b);
                        l7.G(eVar3.c);
                        l7.i(eVar3.a, 0, o, i14);
                        l7.i(eVar3.b, 0, o, i14 + 10);
                        l7.i(eVar3.c, 0, o, i14 + 20);
                        i14 += 30;
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private static void K(byte[] bArr, int i2, byte[] bArr2) {
        System.arraycopy(bArr, i2, bArr2, 0, 32);
        bArr2[0] = (byte) (bArr2[0] & 248);
        byte b2 = (byte) (bArr2[31] & 127);
        bArr2[31] = b2;
        bArr2[31] = (byte) (b2 | 64);
    }

    private static void L(byte[] bArr, b bVar) {
        J();
        int[] iArr = new int[8];
        c6.b(bArr, iArr);
        c6.g(256, iArr);
        q(iArr);
        e eVar = new e();
        g gVar = new g();
        I(bVar);
        int i2 = 28;
        int i3 = 0;
        while (true) {
            int i4 = 0;
            while (i4 < 8) {
                int i5 = iArr[i4] >>> i2;
                int i6 = (i5 >>> 3) & 1;
                F(i4, (i5 ^ (-i6)) & 7, eVar);
                int i7 = i3 ^ i6;
                l7.h(i7, bVar.a);
                l7.h(i7, bVar.d);
                y(eVar, bVar, gVar);
                i4++;
                i3 = i6;
            }
            i2 -= 4;
            if (i2 < 0) {
                l7.h(i3, bVar.a);
                l7.h(i3, bVar.d);
                return;
            }
            E(bVar);
        }
    }

    private static void M(byte[] bArr, byte[] bArr2, int i2) {
        b bVar = new b();
        L(bArr, bVar);
        if (m(bVar, bArr2, i2) == 0) {
            throw new IllegalStateException();
        }
    }

    public static void N(k7.a aVar, byte[] bArr, int i2, int[] iArr, int[] iArr2) {
        if (aVar == null) {
            throw new NullPointerException("This method is only for use by X25519");
        }
        byte[] bArr2 = new byte[32];
        K(bArr, i2, bArr2);
        b bVar = new b();
        L(bArr2, bVar);
        if (c(bVar) == 0) {
            throw new IllegalStateException();
        }
        l7.i(bVar.b, 0, iArr, 0);
        l7.i(bVar.c, 0, iArr2, 0);
    }

    private static void O(int[] iArr, int[] iArr2, c cVar, int[] iArr3, c cVar2, b bVar) {
        int i2;
        J();
        byte[] bArr = new byte[256];
        int i3 = Iap2Csm.b;
        byte[] bArr2 = new byte[Iap2Csm.b];
        byte[] bArr3 = new byte[Iap2Csm.b];
        j7.a(iArr, 6, bArr);
        j7.a(iArr2, 4, bArr2);
        j7.a(iArr3, 4, bArr3);
        f[] fVarArr = new f[4];
        f[] fVarArr2 = new f[4];
        g gVar = new g();
        H(cVar, fVarArr, 4, gVar);
        H(cVar2, fVarArr2, 4, gVar);
        I(bVar);
        while (true) {
            i2 = i3 - 1;
            if (i2 < 0 || (bArr[i2] | bArr[i3 + 127] | bArr2[i2] | bArr3[i2]) != 0) {
                break;
            } else {
                i3 = i2;
            }
        }
        while (i2 >= 0) {
            byte b2 = bArr[i2];
            if (b2 != 0) {
                z(b2 < 0, m[(b2 >> 1) ^ (b2 >> 31)], bVar, gVar);
            }
            byte b3 = bArr[i2 + Iap2Csm.b];
            if (b3 != 0) {
                z(b3 < 0, n[(b3 >> 1) ^ (b3 >> 31)], bVar, gVar);
            }
            byte b4 = bArr2[i2];
            if (b4 != 0) {
                A(b4 < 0, fVarArr[(b4 >> 1) ^ (b4 >> 31)], bVar, gVar);
            }
            byte b5 = bArr3[i2];
            if (b5 != 0) {
                A(b5 < 0, fVarArr2[(b5 >> 1) ^ (b5 >> 31)], bVar, gVar);
            }
            E(bVar);
            i2--;
        }
        E(bVar);
        E(bVar);
    }

    public static void P(byte[] bArr, int i2, byte[] bArr2, int i3, byte[] bArr3, int i4, int i5, byte[] bArr4, int i6) {
        s(bArr, i2, bArr2, i3, null, (byte) 0, bArr3, i4, i5, bArr4, i6);
    }

    public static void Q(byte[] bArr, int i2, byte[] bArr2, int i3, byte[] bArr3, byte[] bArr4, int i4, int i5, byte[] bArr5, int i6) {
        s(bArr, i2, bArr2, i3, bArr3, (byte) 0, bArr4, i4, i5, bArr5, i6);
    }

    public static void R(byte[] bArr, int i2, byte[] bArr2, int i3, byte[] bArr3, byte[] bArr4, int i4, byte[] bArr5, int i5) {
        s(bArr, i2, bArr2, i3, bArr3, (byte) 1, bArr4, i4, 64, bArr5, i5);
    }

    public static h S(byte[] bArr, int i2) {
        byte[] bArrG = g(bArr, i2, 32);
        if (!e(bArrG)) {
            return null;
        }
        c cVar = new c();
        if (i(bArrG, false, cVar)) {
            return n(cVar);
        }
        return null;
    }

    public static boolean T(byte[] bArr, int i2, h hVar, byte[] bArr2, int i3, int i4) {
        return t(bArr, i2, hVar, null, (byte) 0, bArr2, i3, i4);
    }

    public static boolean U(byte[] bArr, int i2, h hVar, byte[] bArr2, byte[] bArr3, int i3, int i4) {
        return t(bArr, i2, hVar, bArr2, (byte) 0, bArr3, i3, i4);
    }

    public static boolean V(byte[] bArr, int i2, h hVar, byte[] bArr2, byte[] bArr3, int i3) {
        return t(bArr, i2, hVar, bArr2, (byte) 1, bArr3, i3, 64);
    }

    private static byte[] a(byte[] bArr, byte[] bArr2, byte[] bArr3) {
        int[] iArr = new int[16];
        c6.b(bArr, iArr);
        int[] iArr2 = new int[8];
        c6.b(bArr2, iArr2);
        int[] iArr3 = new int[8];
        c6.b(bArr3, iArr3);
        m4.e(iArr2, iArr3, iArr);
        byte[] bArr4 = new byte[64];
        LittleEndianShortCodec.g(iArr, 0, 16, bArr4, 0);
        return c6.e(bArr4);
    }

    private static boolean b(byte[] bArr, byte b2) {
        if (bArr == null && b2 == 0) {
            return true;
        }
        return bArr != null && bArr.length < 256;
    }

    private static int c(b bVar) {
        int[] iArrJ = l7.j();
        int[] iArrJ2 = l7.j();
        int[] iArrJ3 = l7.j();
        int[] iArrJ4 = l7.j();
        l7.L(bVar.a, iArrJ2);
        l7.L(bVar.b, iArrJ3);
        l7.L(bVar.c, iArrJ4);
        l7.E(iArrJ2, iArrJ3, iArrJ);
        l7.N(iArrJ2, iArrJ3, iArrJ2);
        l7.E(iArrJ2, iArrJ4, iArrJ2);
        l7.L(iArrJ4, iArrJ4);
        l7.E(iArrJ, i, iArrJ);
        l7.a(iArrJ, iArrJ4, iArrJ);
        l7.a(iArrJ, iArrJ2, iArrJ);
        l7.G(iArrJ);
        l7.G(iArrJ3);
        l7.G(iArrJ4);
        return l7.B(iArrJ) & (~l7.B(iArrJ3)) & (~l7.B(iArrJ4));
    }

    private static int d(c cVar) {
        int[] iArrJ = l7.j();
        int[] iArrJ2 = l7.j();
        int[] iArrJ3 = l7.j();
        l7.L(cVar.a, iArrJ2);
        l7.L(cVar.b, iArrJ3);
        l7.E(iArrJ2, iArrJ3, iArrJ);
        l7.N(iArrJ2, iArrJ3, iArrJ2);
        l7.E(iArrJ, i, iArrJ);
        l7.b(iArrJ);
        l7.a(iArrJ, iArrJ2, iArrJ);
        l7.G(iArrJ);
        l7.G(iArrJ3);
        return l7.B(iArrJ) & (~l7.B(iArrJ3));
    }

    private static boolean e(byte[] bArr) {
        int iC = LittleEndianShortCodec.c(bArr, 28) & Integer.MAX_VALUE;
        int i2 = b[7] ^ iC;
        int i3 = c[7] ^ iC;
        int i4 = d[7] ^ iC;
        for (int i5 = 6; i5 > 0; i5--) {
            int iC2 = LittleEndianShortCodec.c(bArr, i5 * 4);
            iC |= iC2;
            i2 |= b[i5] ^ iC2;
            i3 |= c[i5] ^ iC2;
            i4 |= iC2 ^ d[i5];
        }
        int iC3 = LittleEndianShortCodec.c(bArr, 0);
        if (iC == 0 && IntegerMath.c(iC3, 1) <= 0) {
            return false;
        }
        if (i2 == 0 && IntegerMath.c(iC3, b[0] - 1) >= 0) {
            return false;
        }
        return (((c[0] ^ iC3) | i3) != 0) & (((iC3 ^ d[0]) | i4) != 0);
    }

    private static boolean f(byte[] bArr) {
        int iC = LittleEndianShortCodec.c(bArr, 28) & Integer.MAX_VALUE;
        int[] iArr = b;
        if (iC < iArr[7]) {
            return true;
        }
        int[] iArr2 = new int[8];
        LittleEndianShortCodec.d(bArr, 0, iArr2, 0, 8);
        iArr2[7] = iArr2[7] & Integer.MAX_VALUE;
        return !m4.b(iArr2, iArr);
    }

    private static byte[] g(byte[] bArr, int i2, int i3) {
        byte[] bArr2 = new byte[i3];
        System.arraycopy(bArr, i2, bArr2, 0, i3);
        return bArr2;
    }

    private static q2 h() {
        a6 a6Var = new a6();
        if (a6Var.d() == 64) {
            return a6Var;
        }
        throw new IllegalStateException();
    }

    private static boolean i(byte[] bArr, boolean z, c cVar) {
        int i2 = (bArr[31] & 128) >>> 7;
        l7.p(bArr, cVar.b);
        int[] iArrJ = l7.j();
        int[] iArrJ2 = l7.j();
        l7.L(cVar.b, iArrJ);
        l7.E(i, iArrJ, iArrJ2);
        l7.O(iArrJ);
        l7.b(iArrJ2);
        if (!l7.M(iArrJ, iArrJ2, cVar.a)) {
            return false;
        }
        l7.G(cVar.a);
        if (i2 == 1 && l7.C(cVar.a)) {
            return false;
        }
        int[] iArr = cVar.a;
        if (z ^ (i2 != (iArr[0] & 1))) {
            l7.F(iArr, iArr);
            l7.G(cVar.a);
        }
        return true;
    }

    private static void j(q2 q2Var, byte b2, byte[] bArr) {
        byte[] bArr2 = a;
        int length = bArr2.length;
        int i2 = length + 2;
        int length2 = bArr.length + i2;
        byte[] bArr3 = new byte[length2];
        System.arraycopy(bArr2, 0, bArr3, 0, length);
        bArr3[length] = b2;
        bArr3[length + 1] = (byte) bArr.length;
        System.arraycopy(bArr, 0, bArr3, i2, bArr.length);
        q2Var.b(bArr3, 0, length2);
    }

    private static void k(c cVar, byte[] bArr, int i2) {
        l7.u(cVar.b, bArr, i2);
        int i3 = i2 + 31;
        bArr[i3] = (byte) (((cVar.a[0] & 1) << 7) | bArr[i3]);
    }

    public static void l(h hVar, byte[] bArr, int i2) {
        l7.t(hVar.a, 10, bArr, i2);
        int i3 = i2 + 31;
        bArr[i3] = (byte) (((hVar.a[0] & 1) << 7) | bArr[i3]);
    }

    private static int m(b bVar, byte[] bArr, int i2) {
        c cVar = new c();
        v(bVar, cVar);
        int iD = d(cVar);
        k(cVar, bArr, i2);
        return iD;
    }

    private static h n(c cVar) {
        int[] iArr = new int[20];
        l7.i(cVar.a, 0, iArr, 0);
        l7.i(cVar.b, 0, iArr, 10);
        return new h(iArr);
    }

    public static void o(SecureRandom secureRandom, byte[] bArr) {
        if (bArr.length == 32) {
            secureRandom.nextBytes(bArr);
        } else {
            SyntheticRefs.b("k");
        }
    }

    public static h p(byte[] bArr, int i2) {
        q2 q2VarH = h();
        byte[] bArr2 = new byte[64];
        q2VarH.b(bArr, i2, 32);
        q2VarH.a(bArr2, 0);
        byte[] bArr3 = new byte[32];
        K(bArr2, 0, bArr3);
        b bVar = new b();
        L(bArr3, bVar);
        c cVar = new c();
        v(bVar, cVar);
        if (d(cVar) != 0) {
            return n(cVar);
        }
        throw new IllegalStateException();
    }

    private static void q(int[] iArr) {
        for (int i2 = 0; i2 < iArr.length; i2++) {
            iArr[i2] = e3.a(iArr[i2]);
        }
    }

    private static void r(q2 q2Var, byte[] bArr, byte[] bArr2, byte[] bArr3, int i2, byte[] bArr4, byte b2, byte[] bArr5, int i3, int i4, byte[] bArCarLifeFrame, int i5) {
        if (bArr4 != null) {
            j(q2Var, b2, bArr4);
        }
        q2Var.b(bArr, 32, 32);
        q2Var.b(bArr5, i3, i4);
        q2Var.a(bArr, 0);
        byte[] bArrE = c6.e(bArr);
        byte[] bArr7 = new byte[32];
        M(bArrE, bArr7, 0);
        if (bArr4 != null) {
            j(q2Var, b2, bArr4);
        }
        q2Var.b(bArr7, 0, 32);
        q2Var.b(bArr3, i2, 32);
        q2Var.b(bArr5, i3, i4);
        q2Var.a(bArr, 0);
        byte[] bArrA = a(bArrE, c6.e(bArr), bArr2);
        System.arraycopy(bArr7, 0, bArCarLifeFrame, i5, 32);
        System.arraycopy(bArrA, 0, bArCarLifeFrame, i5 + 32, 32);
    }

    private static void s(byte[] bArr, int i2, byte[] bArr2, int i3, byte[] bArr3, byte b2, byte[] bArr4, int i4, int i5, byte[] bArr5, int i6) {
        if (!b(bArr3, b2)) {
            SyntheticRefs.b("ctx");
            return;
        }
        q2 q2VarH = h();
        byte[] bArCarLifeFrame = new byte[64];
        q2VarH.b(bArr, i2, 32);
        q2VarH.a(bArCarLifeFrame, 0);
        byte[] bArr7 = new byte[32];
        K(bArCarLifeFrame, 0, bArr7);
        r(q2VarH, bArCarLifeFrame, bArr7, bArr2, i3, bArr3, b2, bArr4, i4, i5, bArr5, i6);
    }

    private static boolean t(byte[] bArr, int i2, h hVar, byte[] bArr2, byte b2, byte[] bArr3, int i3, int i4) {
        if (!b(bArr2, b2)) {
            SyntheticRefs.b("ctx");
            return false;
        }
        byte[] bArrG = g(bArr, i2, 32);
        byte[] bArrG2 = g(bArr, i2 + 32, 32);
        if (!f(bArrG)) {
            return false;
        }
        int[] iArr = new int[8];
        if (!c6.a(bArrG2, iArr)) {
            return false;
        }
        c cVar = new c();
        if (!i(bArrG, true, cVar)) {
            return false;
        }
        c cVar2 = new c();
        l7.F(hVar.a, cVar2.a);
        l7.i(hVar.a, 10, cVar2.b, 0);
        byte[] bArr4 = new byte[32];
        l(hVar, bArr4, 0);
        q2 q2VarH = h();
        byte[] bArr5 = new byte[64];
        if (bArr2 != null) {
            j(q2VarH, b2, bArr2);
        }
        q2VarH.b(bArrG, 0, 32);
        q2VarH.b(bArr4, 0, 32);
        q2VarH.b(bArr3, i3, i4);
        q2VarH.a(bArr5, 0);
        int[] iArr2 = new int[8];
        c6.b(c6.e(bArr5), iArr2);
        int[] iArr3 = new int[4];
        int[] iArr4 = new int[4];
        if (!c6.f(iArr2, iArr3, iArr4)) {
            throw new IllegalStateException();
        }
        c6.c(iArr, iArr4, iArr);
        b bVar = new b();
        O(iArr, iArr3, cVar2, iArr4, cVar, bVar);
        return w(bVar);
    }

    private static void u(d[] dVarArr) {
        int length = dVarArr.length;
        int[] iArrK = l7.k(length);
        int[] iArrJ = l7.j();
        l7.i(dVarArr[0].c, 0, iArrJ, 0);
        l7.i(iArrJ, 0, iArrK, 0);
        int i2 = 0;
        while (true) {
            int i3 = i2 + 1;
            if (i3 >= length) {
                break;
            }
            l7.E(iArrJ, dVarArr[i3].c, iArrJ);
            l7.i(iArrJ, 0, iArrK, i3 * 10);
            i2 = i3;
        }
        l7.a(iArrJ, iArrJ, iArrJ);
        l7.A(iArrJ, iArrJ);
        int[] iArrJ2 = l7.j();
        while (i2 > 0) {
            int i4 = i2 - 1;
            l7.i(iArrK, i4 * 10, iArrJ2, 0);
            l7.E(iArrJ2, iArrJ, iArrJ2);
            l7.E(iArrJ, dVarArr[i2].c, iArrJ);
            l7.i(iArrJ2, 0, dVarArr[i2].c, 0);
            i2 = i4;
        }
        l7.i(iArrJ, 0, dVarArr[0].c, 0);
    }

    private static void v(b bVar, c cVar) {
        l7.z(bVar.c, cVar.b);
        l7.E(cVar.b, bVar.a, cVar.a);
        int[] iArr = cVar.b;
        l7.E(iArr, bVar.b, iArr);
        l7.G(cVar.a);
        l7.G(cVar.b);
    }

    private static boolean w(b bVar) {
        l7.G(bVar.a);
        l7.G(bVar.b);
        l7.G(bVar.c);
        return l7.C(bVar.a) && !l7.C(bVar.b) && l7.e(bVar.b, bVar.c);
    }

    private static void x(d dVar, d dVar2, d dVar3, g gVar) {
        int[] iArr = dVar3.a;
        int[] iArr2 = dVar3.b;
        int[] iArr3 = gVar.a;
        int[] iArr4 = gVar.b;
        l7.c(dVar.b, dVar.a, iArr2, iArr);
        l7.c(dVar2.b, dVar2.a, iArr4, iArr3);
        l7.E(iArr, iArr3, iArr);
        l7.E(iArr2, iArr4, iArr2);
        l7.E(dVar.d, dVar2.d, iArr3);
        l7.E(iArr3, j, iArr3);
        int[] iArr5 = dVar.c;
        l7.a(iArr5, iArr5, iArr4);
        l7.E(iArr4, dVar2.c, iArr4);
        l7.c(iArr2, iArr, iArr2, iArr);
        l7.c(iArr4, iArr3, iArr4, iArr3);
        l7.E(iArr, iArr2, dVar3.d);
        l7.E(iArr3, iArr4, dVar3.c);
        l7.E(iArr, iArr3, dVar3.a);
        l7.E(iArr2, iArr4, dVar3.b);
    }

    private static void y(e eVar, b bVar, g gVar) {
        int[] iArr = bVar.a;
        int[] iArr2 = bVar.b;
        int[] iArr3 = gVar.a;
        int[] iArr4 = bVar.d;
        int[] iArr5 = bVar.e;
        l7.c(iArr2, iArr, iArr2, iArr);
        l7.E(iArr, eVar.a, iArr);
        l7.E(iArr2, eVar.b, iArr2);
        l7.E(bVar.d, bVar.e, iArr3);
        l7.E(iArr3, eVar.c, iArr3);
        l7.c(iArr2, iArr, iArr5, iArr4);
        l7.c(bVar.c, iArr3, iArr2, iArr);
        l7.E(iArr, iArr2, bVar.c);
        l7.E(iArr, iArr4, bVar.a);
        l7.E(iArr2, iArr5, bVar.b);
    }

    private static void z(boolean z, e eVar, b bVar, g gVar) {
        int[] iArr;
        int[] iArr2;
        int[] iArr3 = bVar.a;
        int[] iArr4 = bVar.b;
        int[] iArr5 = gVar.a;
        int[] iArCarLifeFrame = bVar.d;
        int[] iArr7 = bVar.e;
        if (z) {
            iArr2 = iArr3;
            iArr = iArr4;
        } else {
            iArr = iArr3;
            iArr2 = iArr4;
        }
        l7.c(iArr4, iArr3, iArr4, iArr3);
        l7.E(iArr, eVar.a, iArr);
        l7.E(iArr2, eVar.b, iArr2);
        l7.E(bVar.d, bVar.e, iArr5);
        l7.E(iArr5, eVar.c, iArr5);
        l7.c(iArr4, iArr3, iArr7, iArCarLifeFrame);
        l7.c(bVar.c, iArr5, iArr2, iArr);
        l7.E(iArr3, iArr4, bVar.c);
        l7.E(iArr3, iArCarLifeFrame, bVar.a);
        l7.E(iArr4, iArr7, bVar.b);
    }
}
