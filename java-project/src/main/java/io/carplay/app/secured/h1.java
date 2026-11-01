package io.carplay.app.secured;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public final class h1 {
    private h1() {
    }

    public static String a(Map<?, ?> map) {
        String str = null;
        String str2 = null;
        for (String str3 : List.of("uuid", "UUID", "displayUUID")) {
            if (map.containsKey(str3)) {
                String strD = d(map.get(str3));
                String strB = b(map.get(str3));
                if (strD == null || !(str2 == null || str2.equals(strB))) {
                    return null;
                }
                if (str == null) {
                    str2 = strB;
                    str = strD;
                }
            }
        }
        return str;
    }

    public static String b(Object obj) {
        String strD = d(obj);
        if (strD == null) {
            return null;
        }
        return strD.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}") ? strD.toLowerCase(Locale.ROOT) : strD;
    }

    public static boolean c(Object obj, Object obj2) {
        String strB = b(obj);
        return strB != null && strB.equals(b(obj2));
    }

    public static String d(Object obj) {
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.isBlank() || str.length() > 128 || str.chars().anyMatch(new k(2))) {
                return null;
            }
            return str;
        }
        if (!(obj instanceof byte[])) {
            if ((obj instanceof Byte) || (obj instanceof Short) || (obj instanceof Integer) || (obj instanceof Long)) {
                long jLongValue = ((Number) obj).longValue();
                if (jLongValue < 0) {
                    return null;
                }
                return Long.toString(jLongValue);
            }
            if (obj instanceof BigInteger) {
                BigInteger bigInteger = (BigInteger) obj;
                if (bigInteger.signum() >= 0 && bigInteger.bitLength() <= 63) {
                    return bigInteger.toString();
                }
            }
            return null;
        }
        byte[] bArr = (byte[]) obj;
        if (bArr.length != 16) {
            return null;
        }
        StringBuilder sb = new StringBuilder(36);
        for (int i = 0; i < 16; i++) {
            if (i == 4 || i == 6 || i == 8 || i == 10) {
                sb.append('-');
            }
            sb.append(Character.forDigit((bArr[i] >>> 4) & 15, 16));
            sb.append(Character.forDigit(bArr[i] & 15, 16));
        }
        return sb.toString();
    }
}
