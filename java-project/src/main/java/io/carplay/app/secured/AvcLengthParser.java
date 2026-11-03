package io.carplay.app.secured;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public final class AvcLengthParser {
    private AvcLengthParser() {
    }

    public static byte[] a(byte[] bArr) throws IOException {
        byte b;
        if (bArr.length < 4 || bArr.length > 4194304) {
            SyntheticRefs.c("codec AVC buffer bounds");
            return null;
        }
        int i = 0;
        if (bArr[0] == 0 && bArr[1] == 0 && ((b = bArr[2]) == 1 || (b == 0 && bArr[3] == 1))) {
            return (byte[]) bArr.clone();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (i < bArr.length) {
            if (bArr.length - i < 4) {
                SyntheticRefs.c("truncated AVC length");
                return null;
            }
            long j = (((long) (bArr[i] & 255)) << 24) | (((long) (bArr[i + 1] & 255)) << 16) | (((long) (bArr[i + 2] & 255)) << 8) | ((long) (bArr[i + 3] & 255));
            int i2 = i + 4;
            if (j < 1 || j > bArr.length - i2) {
                SyntheticRefs.c("invalid AVC length");
                return null;
            }
            byteArrayOutputStream.writeBytes(new byte[]{0, 0, 0, 1});
            int i3 = (int) j;
            byteArrayOutputStream.write(bArr, i2, i3);
            i = i2 + i3;
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] b(byte[] bArr, byte[] bArr2, boolean z) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (z && bArr != null) {
            byteArrayOutputStream.writeBytes(bArr);
        }
        byteArrayOutputStream.writeBytes(a(bArr2));
        byteArrayOutputStream.writeBytes(new byte[]{0, 0, 0, 1, 9, -16, 0, 0, 0, 1});
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] c(List<byte[]> list) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (byte[] bArr : list) {
            byteArrayOutputStream.writeBytes(new byte[]{0, 0, 0, 1});
            byteArrayOutputStream.writeBytes(bArr);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
