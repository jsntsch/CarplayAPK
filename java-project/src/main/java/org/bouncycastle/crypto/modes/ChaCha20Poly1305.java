package org.bouncycastle.crypto.modes;

import io.carplay.app.secured.Poly1305;
import io.carplay.app.secured.c5;
import io.carplay.app.secured.d0;
import io.carplay.app.secured.e;
import io.carplay.app.secured.f3;
import io.carplay.app.secured.k3;
import io.carplay.app.secured.l3;
import io.carplay.app.secured.n2;
import io.carplay.app.secured.p1;
import io.carplay.app.secured.w1;
import io.carplay.app.secured.x4;
import io.carplay.app.secured.y1;
import io.carplay.app.secured.y4;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

/* JADX INFO: loaded from: classes.dex */
public class ChaCha20Poly1305 {
    private static final long AAD_LIMIT = -1;
    private static final int BUF_SIZE = 64;
    private static final long DATA_LIMIT = 274877906880L;
    private static final int KEY_SIZE = 32;
    private static final int MAC_SIZE = 16;
    private static final int NONCE_SIZE = 12;
    private static final byte[] ZEROES = new byte[15];
    private long aadCount;
    private final byte[] buf;
    private int bufPos;
    private final y1 chacha20;
    private long dataCount;
    private byte[] initialAAD;
    private final byte[] key;
    private final byte[] mac;
    private final byte[] nonce;
    private final l3 poly1305;
    private int state;

    public ChaCha20Poly1305(l3 l3Var) {
        this.key = new byte[32];
        this.nonce = new byte[NONCE_SIZE];
        this.buf = new byte[80];
        this.mac = new byte[16];
        this.state = 0;
        if (l3Var == null) {
            throw new NullPointerException("'poly1305' cannot be null");
        }
        if (16 != l3Var.e()) {
            d0.b("'poly1305' must be a 128-bit MAC");
            throw null;
        }
        this.chacha20 = new y1();
        this.poly1305 = l3Var;
    }

    private void checkAAD() {
        int i = this.state;
        if (i == 1) {
            this.state = 2;
            return;
        }
        if (i != 2) {
            if (i == 4) {
                d0.d("ChaCha20Poly1305 cannot be reused for encryption");
            } else if (i == 5) {
                this.state = 6;
            } else if (i != 6) {
                throw new IllegalStateException();
            }
        }
    }

    private void checkData() {
        switch (this.state) {
            case p1.c /* 1 */:
            case 2:
                finishAAD(3);
                return;
            case p1.d /* 3 */:
            case 7:
                return;
            case 4:
                d0.d("ChaCha20Poly1305 cannot be reused for encryption");
                return;
            case 5:
            case 6:
                finishAAD(7);
                return;
            default:
                throw new IllegalStateException();
        }
    }

    private void finishAAD(int i) {
        padMAC(this.aadCount);
        this.state = i;
    }

    private void finishData(int i) {
        padMAC(this.dataCount);
        byte[] bArr = new byte[16];
        y4.k(this.aadCount, bArr, 0);
        y4.k(this.dataCount, bArr, 8);
        this.poly1305.b(bArr, 0, 16);
        this.poly1305.a(this.mac, 0);
        this.state = i;
    }

    private long incrementCount(long j, int i, long j2) {
        long j3 = i;
        if (k3.b(j, j2 - j3) <= 0) {
            return j + j3;
        }
        d0.d("Limit exceeded");
        return 0L;
    }

    private void initMAC() {
        byte[] bArr = new byte[64];
        try {
            this.chacha20.h(bArr, 0, 64, bArr, 0);
            this.poly1305.c(new KeyParameter(bArr, 0, 32));
        } finally {
            e.c(bArr);
        }
    }

    private void padMAC(long j) {
        int i = ((int) j) & 15;
        if (i != 0) {
            this.poly1305.b(ZEROES, 0, 16 - i);
        }
    }

    private void processData(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        if (i3 > bArr2.length - i2) {
            throw new x4("Output buffer too short");
        }
        this.chacha20.h(bArr, i, i2, bArr2, i3);
        this.dataCount = incrementCount(this.dataCount, i2, DATA_LIMIT);
    }

    private void reset(boolean z, boolean z2) {
        e.c(this.buf);
        if (z) {
            e.c(this.mac);
        }
        this.aadCount = 0L;
        this.dataCount = 0L;
        this.bufPos = 0;
        switch (this.state) {
            case p1.c /* 1 */:
            case 5:
                break;
            case 2:
            case p1.d /* 3 */:
            case 4:
                this.state = 4;
                return;
            case 6:
            case 7:
            case 8:
                this.state = 5;
                break;
            default:
                throw new IllegalStateException();
        }
        if (z2) {
            this.chacha20.i();
        }
        initMAC();
        byte[] bArr = this.initialAAD;
        if (bArr != null) {
            processAADBytes(bArr, 0, bArr.length);
        }
    }

    public int doFinal(byte[] bArr, int i) throws f3 {
        int i2;
        int i3;
        if (bArr == null) {
            throw new NullPointerException("'out' cannot be null");
        }
        if (i < 0) {
            d0.b("'outOff' cannot be negative");
            return 0;
        }
        checkData();
        e.c(this.mac);
        int i4 = this.state;
        if (i4 == 3) {
            int i5 = this.bufPos;
            int i6 = i5 + 16;
            if (i > bArr.length - i6) {
                throw new x4("Output buffer too short");
            }
            if (i5 > 0) {
                processData(this.buf, 0, i5, bArr, i);
                this.poly1305.b(bArr, i, this.bufPos);
            }
            finishData(4);
            System.arraycopy(this.mac, 0, bArr, this.bufPos + i, 16);
            i2 = i6;
        } else {
            if (i4 != 7) {
                throw new IllegalStateException();
            }
            int i7 = this.bufPos;
            if (i7 < 16) {
                throw new f3("data too short");
            }
            int i8 = i7 - 16;
            if (i > bArr.length - i8) {
                throw new x4("Output buffer too short");
            }
            if (i8 > 0) {
                this.poly1305.b(this.buf, 0, i8);
                i3 = i8;
                processData(this.buf, 0, i3, bArr, i);
            } else {
                i3 = i8;
            }
            finishData(8);
            if (!e.e(16, this.mac, 0, this.buf, i3)) {
                throw new f3("mac check in ChaCha20Poly1305 failed");
            }
            i2 = i3;
        }
        reset(false, true);
        return i2;
    }

    public String getAlgorithmName() {
        return "ChaCha20Poly1305";
    }

    public byte[] getMac() {
        return e.d(this.mac);
    }

    public int getOutputSize(int i) {
        int iMax = Math.max(0, i) + this.bufPos;
        int i2 = this.state;
        if (i2 == 1 || i2 == 2 || i2 == 3) {
            return iMax + 16;
        }
        if (i2 == 5 || i2 == 6 || i2 == 7) {
            return Math.max(0, iMax - 16);
        }
        throw new IllegalStateException();
    }

    public int getUpdateOutputSize(int i) {
        int iMax = Math.max(0, i) + this.bufPos;
        int i2 = this.state;
        if (i2 != 1 && i2 != 2 && i2 != 3) {
            if (i2 != 5 && i2 != 6 && i2 != 7) {
                throw new IllegalStateException();
            }
            iMax = Math.max(0, iMax - 16);
        }
        return iMax - (iMax % 64);
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        c5 c5Var;
        KeyParameter key;
        byte[] bArrA;
        if (cipherParameters instanceof AEADParameters) {
            AEADParameters aEADParameters = (AEADParameters) cipherParameters;
            int macSize = aEADParameters.getMacSize();
            if (128 != macSize) {
                d0.b(w1.d("Invalid value for MAC size: ", macSize));
                return;
            }
            key = aEADParameters.getKey();
            bArrA = aEADParameters.getNonce();
            c5Var = new c5(key, bArrA);
            this.initialAAD = aEADParameters.getAssociatedText();
        } else {
            if (!(cipherParameters instanceof c5)) {
                d0.b("invalid parameters passed to ChaCha20Poly1305");
                return;
            }
            c5Var = (c5) cipherParameters;
            key = (KeyParameter) c5Var.b();
            bArrA = c5Var.a();
            this.initialAAD = null;
        }
        if (key == null) {
            if (this.state == 0) {
                d0.b("Key must be specified in initial init");
                return;
            }
        } else if (32 != key.getKeyLength()) {
            d0.b("Key must be 256 bits");
            return;
        }
        if (bArrA == null || NONCE_SIZE != bArrA.length) {
            d0.b("Nonce must be 96 bits");
            return;
        }
        if (this.state != 0 && z && e.b(this.nonce, bArrA) && (key == null || e.b(this.key, key.getKey()))) {
            d0.b("cannot reuse nonce for ChaCha20Poly1305 encryption");
            return;
        }
        if (key != null) {
            key.copyTo(this.key, 0, 32);
        }
        System.arraycopy(bArrA, 0, this.nonce, 0, NONCE_SIZE);
        this.chacha20.e(true, c5Var);
        this.state = z ? 1 : 5;
        reset(true, false);
    }

    public void processAADByte(byte b) {
        checkAAD();
        this.aadCount = incrementCount(this.aadCount, 1, AAD_LIMIT);
        this.poly1305.d(b);
    }

    public void processAADBytes(byte[] bArr, int i, int i2) {
        if (bArr == null) {
            throw new NullPointerException("'in' cannot be null");
        }
        if (i < 0) {
            d0.b("'inOff' cannot be negative");
            return;
        }
        if (i2 < 0) {
            d0.b("'len' cannot be negative");
            return;
        }
        if (i > bArr.length - i2) {
            throw new n2("Input buffer too short");
        }
        checkAAD();
        if (i2 > 0) {
            this.aadCount = incrementCount(this.aadCount, i2, AAD_LIMIT);
            this.poly1305.b(bArr, i, i2);
        }
    }

    public int processByte(byte b, byte[] bArr, int i) {
        checkData();
        int i2 = this.state;
        if (i2 == 3) {
            byte[] bArr2 = this.buf;
            int i3 = this.bufPos;
            bArr2[i3] = b;
            int i4 = i3 + 1;
            this.bufPos = i4;
            if (i4 != 64) {
                return 0;
            }
            processData(bArr2, 0, 64, bArr, i);
            this.poly1305.b(bArr, i, 64);
            this.bufPos = 0;
            return 64;
        }
        if (i2 != 7) {
            throw new IllegalStateException();
        }
        byte[] bArr3 = this.buf;
        int i5 = this.bufPos;
        bArr3[i5] = b;
        int i6 = i5 + 1;
        this.bufPos = i6;
        if (i6 != bArr3.length) {
            return 0;
        }
        this.poly1305.b(bArr3, 0, 64);
        processData(this.buf, 0, 64, bArr, i);
        byte[] bArr4 = this.buf;
        System.arraycopy(bArr4, 64, bArr4, 0, 16);
        this.bufPos = 16;
        return 64;
    }

    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        String str;
        byte[] bArr3;
        int i4;
        int i5;
        int i6 = i;
        int i7 = i2;
        if (bArr == null) {
            throw new NullPointerException("'in' cannot be null");
        }
        if (i6 < 0) {
            str = "'inOff' cannot be negative";
        } else if (i7 < 0) {
            str = "'len' cannot be negative";
        } else {
            if (i6 > bArr.length - i7) {
                throw new n2("Input buffer too short");
            }
            if (i3 >= 0) {
                if (bArr == bArr2 && e.i(i, i7, i3, getUpdateOutputSize(i7))) {
                    bArr = new byte[i7];
                    System.arraycopy(bArr2, i, bArr, 0, i7);
                    i6 = 0;
                }
                checkData();
                int i8 = this.state;
                if (i8 != 3) {
                    if (i8 != 7) {
                        throw new IllegalStateException();
                    }
                    int i9 = 0;
                    for (int i10 = 0; i10 < i7; i10++) {
                        byte[] bArr4 = this.buf;
                        int i11 = this.bufPos;
                        bArr4[i11] = bArr[i6 + i10];
                        int i12 = i11 + 1;
                        this.bufPos = i12;
                        if (i12 == bArr4.length) {
                            this.poly1305.b(bArr4, 0, 64);
                            processData(this.buf, 0, 64, bArr2, i3 + i9);
                            byte[] bArr5 = this.buf;
                            System.arraycopy(bArr5, 64, bArr5, 0, 16);
                            this.bufPos = 16;
                            i9 += 64;
                        }
                    }
                    return i9;
                }
                if (this.bufPos == 0) {
                    bArr3 = bArr2;
                    i4 = i7;
                    i5 = 0;
                    break;
                }
                while (true) {
                    if (i7 <= 0) {
                        bArr3 = bArr2;
                        i4 = i7;
                        i5 = 0;
                        break;
                    }
                    i4 = i7 - 1;
                    byte[] bArr6 = this.buf;
                    int i13 = this.bufPos;
                    int i14 = i6 + 1;
                    bArr6[i13] = bArr[i6];
                    int i15 = i13 + 1;
                    this.bufPos = i15;
                    if (i15 == 64) {
                        bArr3 = bArr2;
                        processData(bArr6, 0, 64, bArr3, i3);
                        this.poly1305.b(bArr3, i3, 64);
                        this.bufPos = 0;
                        i6 = i14;
                        i5 = 64;
                        break;
                    }
                    i7 = i4;
                    i6 = i14;
                }
                while (i4 >= 64) {
                    int i16 = i3 + i5;
                    byte[] bArr7 = bArr;
                    int i17 = i6;
                    processData(bArr7, i17, 64, bArr3, i16);
                    this.poly1305.b(bArr3, i16, 64);
                    i6 = i17 + 64;
                    i4 -= 64;
                    i5 += 64;
                    bArr = bArr7;
                }
                byte[] bArr8 = bArr;
                if (i4 > 0) {
                    System.arraycopy(bArr8, i6, this.buf, 0, i4);
                    this.bufPos = i4;
                }
                return i5;
            }
            str = "'outOff' cannot be negative";
        }
        d0.b(str);
        return 0;
    }

    public void reset() {
        reset(true, true);
    }

    public ChaCha20Poly1305() {
        this(new Poly1305());
    }
}
