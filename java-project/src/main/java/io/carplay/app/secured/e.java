package io.carplay.app.secured;

import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public abstract class e {
    public static boolean a(byte[] bArr, int i, int i2) {
        int i3 = 0;
        for (int i4 = 0; i4 < i2; i4++) {
            i3 |= bArr[i + i4];
        }
        return i3 == 0;
    }

    public static boolean b(byte[] bArr, byte[] bArr2) {
        return Arrays.equals(bArr, bArr2);
    }

    public static void c(byte[] bArr) {
        if (bArr != null) {
            Arrays.fill(bArr, (byte) 0);
        }
    }

    public static byte[] d(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
    }

    public static boolean e(int i, byte[] bArr, int i2, byte[] bArr2, int i3) {
        if (bArr == null) {
            throw new NullPointerException("'a' cannot be null");
        }
        if (bArr2 == null) {
            throw new NullPointerException("'b' cannot be null");
        }
        if (i < 0) {
            d0.b("'len' cannot be negative");
            return false;
        }
        if (i2 > bArr.length - i) {
            throw new IndexOutOfBoundsException("'aOff' value invalid for specified length");
        }
        if (i3 > bArr2.length - i) {
            throw new IndexOutOfBoundsException("'bOff' value invalid for specified length");
        }
        int i4 = 0;
        for (int i5 = 0; i5 < i; i5++) {
            i4 |= bArr[i2 + i5] ^ bArr2[i3 + i5];
        }
        return i4 == 0;
    }

    public static void f(byte[] bArr, int i, int i2, byte b) {
        Arrays.fill(bArr, i, i2, b);
    }

    public static int g(byte[] bArr) {
        if (bArr == null) {
            return 0;
        }
        int length = bArr.length;
        int i = length + 1;
        while (true) {
            length--;
            if (length < 0) {
                return i;
            }
            i = (i * 257) ^ bArr[length];
        }
    }

    public static void h(byte[] bArr, byte[] bArr2) {
        int length = bArr.length - 1;
        for (int i = 0; i <= length; i++) {
            bArr2[i] = bArr[length - i];
        }
    }

    public static boolean i(int i, int i2, int i3, int i4) {
        return i2 > 0 && i4 > 0 && i - i3 < i4 && i3 - i < i2;
    }
}
