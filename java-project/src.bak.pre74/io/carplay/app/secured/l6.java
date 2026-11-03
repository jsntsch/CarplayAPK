package io.carplay.app.secured;

import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
public abstract class l6 {
    public static int a(InputStream inputStream, byte[] bArr) throws IOException {
        return b(inputStream, bArr, 0, bArr.length);
    }

    public static int b(InputStream inputStream, byte[] bArr, int i, int i2) throws IOException {
        int i3 = 0;
        while (i3 < i2) {
            int i4 = inputStream.read(bArr, i + i3, i2 - i3);
            if (i4 < 0) {
                break;
            }
            i3 += i4;
        }
        return i3;
    }
}
