package io.carplay.app.secured;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public final class o5 implements l5, AutoCloseable {
    public static final int c = 1048576;
    private static final char[] d = "0123456789abcdef".toCharArray();
    private final byte[] a;
    private boolean b;

    public o5(byte[] bArr) {
        if (bArr == null || bArr.length == 0 || bArr.length > 1048576) {
            d0.b("A nonempty bounded prefix must be explicitly configured");
            throw null;
        }
        this.a = (byte[]) bArr.clone();
    }

    public static o5 g() {
        return new o5("CARLIFE".getBytes(StandardCharsets.US_ASCII));
    }

    public static byte[] i(String str) {
        long jK = k(str);
        if (jK > 1048576) {
            d0.b("Input exceeds byte limit");
            return null;
        }
        byte[] bArr = new byte[(int) jK];
        int i = 0;
        for (int i2 = 0; i2 < str.length(); i2++) {
            char cCharAt = str.charAt(i2);
            if (cCharAt >= 1 && cCharAt <= 127) {
                bArr[i] = (byte) cCharAt;
                i++;
            } else if (cCharAt <= 2047) {
                int i3 = i + 1;
                bArr[i] = (byte) ((cCharAt >> 6) | 192);
                i += 2;
                bArr[i3] = (byte) ((cCharAt & '?') | Iap2Csm.b);
            } else {
                bArr[i] = (byte) ((cCharAt >> '\f') | 224);
                int i4 = i + 2;
                bArr[i + 1] = (byte) (((cCharAt >> 6) & 63) | Iap2Csm.b);
                i += 3;
                bArr[i4] = (byte) ((cCharAt & '?') | Iap2Csm.b);
            }
        }
        return bArr;
    }

    private static long k(String str) {
        long j = 0;
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            j += (cCharAt < 1 || cCharAt > 127) ? cCharAt <= 2047 ? 2L : 3L : 1L;
        }
        return j;
    }

    @Override // java.lang.AutoCloseable
    public synchronized void close() {
        Arrays.fill(this.a, (byte) 0);
        this.b = true;
    }

    @Override // io.carplay.app.secured.l5
    public synchronized l5.a f(String str, String str2, String str3, String str4, String str5) {
        int i;
        if (!this.b && str != null && str2 != null && str3 != null && str4 != null && str5 != null && str5.length() == 32) {
            for (i = 0; i < str5.length(); i++) {
                char cCharAt = str5.charAt(i);
                if (!((cCharAt >= '0' && cCharAt <= '9') || (cCharAt >= 'a' && cCharAt <= 'f'))) {
                    return l5.a.REJECTED;
                }
            }
            String[] strArr = {str2, str3, str4, str};
            long length = this.a.length;
            for (int i2 = 0; i2 < 4; i2++) {
                String str6 = strArr[i2];
                if (str6.length() > 1048576 - length) {
                    return l5.a.REJECTED;
                }
                length += k(str6);
                if (length > 1048576) {
                    return l5.a.REJECTED;
                }
            }
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(this.a);
            for (int i3 = 0; i3 < 4; i3++) {
                messageDigest.update(i(strArr[i3]));
            }
            byte[] bArrDigest = messageDigest.digest();
            byte[] bArr = new byte[32];
            for (int i4 = 0; i4 < bArrDigest.length; i4++) {
                int i5 = i4 * 2;
                char[] cArr = d;
                bArr[i5] = (byte) cArr[(bArrDigest[i4] & 255) >>> 4];
                bArr[i5 + 1] = (byte) cArr[bArrDigest[i4] & 15];
            }
            return MessageDigest.isEqual(bArr, str5.getBytes(StandardCharsets.US_ASCII)) ? l5.a.VERIFIED : l5.a.REJECTED;
        }
        return l5.a.REJECTED;
    }
}
