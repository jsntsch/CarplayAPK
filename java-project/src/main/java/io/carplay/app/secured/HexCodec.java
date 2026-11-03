package io.carplay.app.secured;

import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public class HexCodec {
    protected final byte[] a = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
    protected final byte[] b = new byte[Iap2Csm.b];

    public HexCodec() {
        b();
    }

    public byte[] a(String str, int i, int i2) throws IOException {
        String str2;
        if (str == null) {
            throw new NullPointerException("'str' cannot be null");
        }
        if (i < 0 || i2 < 0 || i > str.length() - i2) {
            throw new IndexOutOfBoundsException("invalid offset and/or length specified");
        }
        if ((i2 & 1) == 0) {
            int i3 = i2 >>> 1;
            byte[] bArr = new byte[i3];
            for (int i4 = 0; i4 < i3; i4++) {
                int i5 = i + 1;
                byte b = this.b[str.charAt(i)];
                i += 2;
                int i6 = (b << 4) | this.b[str.charAt(i5)];
                if (i6 >= 0) {
                    bArr[i4] = (byte) i6;
                } else {
                    str2 = "invalid characters encountered in Hex string";
                }
            }
            return bArr;
        }
        str2 = "a hexadecimal encoding must have an even number of characters";
        SyntheticRefs.c(str2);
        return null;
    }

    public void b() {
        int i = 0;
        int i2 = 0;
        while (true) {
            byte[] bArr = this.b;
            if (i2 >= bArr.length) {
                break;
            }
            bArr[i2] = -1;
            i2++;
        }
        while (true) {
            byte[] bArr2 = this.a;
            int length = bArr2.length;
            byte[] bArr3 = this.b;
            if (i >= length) {
                bArr3[65] = bArr3[97];
                bArr3[66] = bArr3[98];
                bArr3[67] = bArr3[99];
                bArr3[68] = bArr3[100];
                bArr3[69] = bArr3[101];
                bArr3[70] = bArr3[102];
                return;
            }
            bArr3[bArr2[i]] = (byte) i;
            i++;
        }
    }
}
