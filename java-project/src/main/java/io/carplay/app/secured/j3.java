package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public abstract class j3 implements q2 {
    static final long[] p = {4794697086780616226L, 8158064640168781261L, -5349999486874862801L, -1606136188198331460L, 4131703408338449720L, 6480981068601479193L, -7908458776815382629L, -6116909921290321640L, -2880145864133508542L, 1334009975649890238L, 2608012711638119052L, 6128411473006802146L, 8268148722764581231L, -9160688886553864527L, -7215885187991268811L, -4495734319001033068L, -1973867731355612462L, -1171420211273849373L, 1135362057144423861L, 2597628984639134821L, 3308224258029322869L, 5365058923640841347L, 6679025012923562964L, 8573033837759648693L, -7476448914759557205L, -6327057829258317296L, -5763719355590565569L, -4658551843659510044L, -4116276920077217854L, -3051310485924567259L, 489312712824947311L, 1452737877330783856L, 2861767655752347644L, 3322285676063803686L, 5560940570517711597L, 5996557281743188959L, 7280758554555802590L, 8532644243296465576L, -9096487096722542874L, -7894198246740708037L, -6719396339535248540L, -6333637450476146687L, -4446306890439682159L, -4076793802049405392L, -3345356375505022440L, -2983346525034927856L, -860691631967231958L, 1182934255886127544L, 1847814050463011016L, 2177327727835720531L, 2830643537854262169L, 3796741975233480872L, 4115178125766777443L, 5681478168544905931L, 6601373596472566643L, 7507060721942968483L, 8399075790359081724L, 8693463985226723168L, -8878714635349349518L, -8302665154208450068L, -8016688836872298968L, -6606660893046293015L, -4685533653050689259L, -4147400797238176981L, -3880063495543823972L, -3348786107499101689L, -1523767162380948706L, -757361751448694408L, 500013540394364858L, 748580250866718886L, 1242879168328830382L, 1977374033974150939L, 2944078676154940804L, 3659926193048069267L, 4368137639120453308L, 4836135668995329356L, 5532061633213252278L, 6448918945643986474L, 6902733635092675308L, 7801388544844847127L};
    protected final f2 a;
    private long d;
    private long e;
    protected long f;
    protected long g;
    protected long h;
    protected long i;
    protected long j;
    protected long k;
    protected long l;
    protected long m;
    private int o;
    private byte[] b = new byte[8];
    private long[] n = new long[80];
    private int c = 0;

    public j3(f2 f2Var) {
        this.a = f2Var;
        p();
    }

    private long e(long j, long j2, long j3) {
        return ((~j) & j3) ^ (j2 & j);
    }

    private long f(long j, long j2, long j3) {
        return ((j & j3) ^ (j & j2)) ^ (j2 & j3);
    }

    private long g(long j) {
        return (j >>> 7) ^ (((j << 63) | (j >>> 1)) ^ ((j << 56) | (j >>> 8)));
    }

    private long h(long j) {
        return (j >>> 6) ^ (((j << 45) | (j >>> 19)) ^ ((j << 3) | (j >>> 61)));
    }

    private long i(long j) {
        return ((j >>> 39) | (j << 25)) ^ (((j << 36) | (j >>> 28)) ^ ((j << 30) | (j >>> 34)));
    }

    private long j(long j) {
        return ((j >>> 41) | (j << 23)) ^ (((j << 50) | (j >>> 14)) ^ ((j << 46) | (j >>> 18)));
    }

    private void k() {
        long j = this.d;
        if (j > 2305843009213693951L) {
            this.e += j >>> 61;
            this.d = j & 2305843009213693951L;
        }
    }

    @Override // io.carplay.app.secured.q2
    public void b(byte[] bArr, int i, int i2) {
        while (this.c != 0 && i2 > 0) {
            q(bArr[i]);
            i++;
            i2--;
        }
        while (i2 >= this.b.length) {
            o(bArr, i);
            byte[] bArr2 = this.b;
            i += bArr2.length;
            i2 -= bArr2.length;
            this.d += (long) bArr2.length;
        }
        while (i2 > 0) {
            q(bArr[i]);
            i++;
            i2--;
        }
    }

    public void l() {
        k();
        long j = this.d << 3;
        long j2 = this.e;
        byte b = -128;
        while (true) {
            q(b);
            if (this.c == 0) {
                n(j, j2);
                m();
                return;
            }
            b = 0;
        }
    }

    public void m() {
        k();
        for (int i = 16; i <= 79; i++) {
            long[] jArr = this.n;
            long jH = h(jArr[i - 2]);
            long[] jArr2 = this.n;
            jArr[i] = jH + jArr2[i - 7] + g(jArr2[i - 15]) + this.n[i - 16];
        }
        long j = this.f;
        long j2 = this.g;
        long j3 = this.h;
        long j4 = this.i;
        long j5 = this.j;
        long j6 = j4;
        long j7 = this.k;
        int i2 = 0;
        int i3 = 0;
        long j8 = j3;
        long j9 = this.l;
        long j10 = this.m;
        long j11 = j;
        long j12 = j5;
        long j13 = j2;
        while (i2 < 10) {
            long j14 = j7;
            long j15 = j12;
            long j16 = j9;
            long j17 = j(j12) + e(j12, j14, j9);
            long[] jArr3 = p;
            int i4 = i3 + 1;
            long j18 = j17 + jArr3[i3] + this.n[i3] + j10;
            long j19 = j6 + j18;
            long j20 = j11;
            long j21 = j13;
            long j22 = j8;
            long jI = i(j11) + f(j20, j21, j22) + j18;
            long j23 = j(j19) + e(j19, j15, j14) + jArr3[i4];
            int i5 = i3 + 2;
            long j24 = j23 + this.n[i4] + j16;
            long j25 = j22 + j24;
            long jI2 = i(jI) + f(jI, j20, j21) + j24;
            int i6 = i3 + 3;
            long j26 = j(j25) + e(j25, j19, j15) + jArr3[i5] + this.n[i5] + j14;
            long j27 = j21 + j26;
            long jI3 = i(jI2) + f(jI2, jI, j20) + j26;
            long j28 = j(j27) + e(j27, j25, j19) + jArr3[i6];
            int i7 = i3 + 4;
            long j29 = j28 + this.n[i6] + j15;
            long j30 = j20 + j29;
            long jI4 = i(jI3) + f(jI3, jI2, jI) + j29;
            long j31 = j(j30) + e(j30, j27, j25) + jArr3[i7];
            int i8 = i3 + 5;
            long j32 = j31 + this.n[i7] + j19;
            long j33 = jI + j32;
            long jI5 = i(jI4) + f(jI4, jI3, jI2) + j32;
            long j34 = j(j33) + e(j33, j30, j27) + jArr3[i8];
            int i9 = i3 + 6;
            long j35 = j34 + this.n[i8] + j25;
            long j36 = jI2 + j35;
            long jI6 = i(jI5) + f(jI5, jI4, jI3) + j35;
            int i10 = i3 + 7;
            long j37 = j(j36) + e(j36, j33, j30) + jArr3[i9] + this.n[i9] + j27;
            long j38 = jI3 + j37;
            long jI7 = i(jI6) + f(jI6, jI5, jI4) + j37;
            i3 += 8;
            long j39 = j(j38) + e(j38, j36, j33) + jArr3[i10] + this.n[i10] + j30;
            long jI8 = i(jI7) + f(jI7, jI6, jI5) + j39;
            i2++;
            j13 = jI7;
            j8 = jI6;
            j12 = jI4 + j39;
            j10 = j33;
            j7 = j38;
            j6 = jI5;
            j11 = jI8;
            j9 = j36;
        }
        this.f += j11;
        this.g += j13;
        this.h += j8;
        this.i += j6;
        this.j += j12;
        this.k += j7;
        this.l += j9;
        this.m += j10;
        this.o = 0;
        for (int i11 = 0; i11 < 16; i11++) {
            this.n[i11] = 0;
        }
    }

    public void n(long j, long j2) {
        if (this.o > 14) {
            m();
        }
        long[] jArr = this.n;
        jArr[14] = j2;
        jArr[15] = j;
    }

    public void o(byte[] bArr, int i) {
        this.n[this.o] = y4.b(bArr, i);
        int i2 = this.o + 1;
        this.o = i2;
        if (i2 == 16) {
            m();
        }
    }

    public void p() {
        this.d = 0L;
        this.e = 0L;
        int i = 0;
        this.c = 0;
        int i2 = 0;
        while (true) {
            byte[] bArr = this.b;
            if (i2 >= bArr.length) {
                break;
            }
            bArr[i2] = 0;
            i2++;
        }
        this.o = 0;
        while (true) {
            long[] jArr = this.n;
            if (i == jArr.length) {
                return;
            }
            jArr[i] = 0;
            i++;
        }
    }

    public void q(byte b) {
        byte[] bArr = this.b;
        int i = this.c;
        int i2 = i + 1;
        this.c = i2;
        bArr[i] = b;
        if (i2 == bArr.length) {
            o(bArr, 0);
            this.c = 0;
        }
        this.d++;
    }
}
