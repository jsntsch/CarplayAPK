package io.carplay.app.secured;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
/**
 * ControlRequestParser
 *
 * <p>原混淆名：{@code io.carplay.app.secured.s0}（jadx 产物 s0.java）
 *
 * <p>命名依据：CTRL_HEADER_FIELD / transfer-encoding / content-length —— 控制通道 HTTP 报文解析
 */
public final class ControlRequestParser {
    public static final int a = 8192;

    public static final class a extends w1 {
        private final int a;
        private final boolean b;
        private final boolean c;

        public a(int i, boolean z, boolean z2) {
            this.a = i;
            this.b = z;
            this.c = z2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.b == aVar.b && this.c == aVar.c && this.a == aVar.a;
        }

        public boolean g() {
            return this.b;
        }

        public boolean h() {
            return this.c;
        }

        public final int hashCode() {
            boolean z = this.b;
            boolean z2 = this.c;
            return ((Boolean.hashCode(z2) + (Boolean.hashCode(z) * 31)) * 31) + this.a;
        }

        public int i() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Boolean.valueOf(this.b), Boolean.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(a.class, sb, "[");
            for (int i = 0; i < strArrSplit.length; i++) {
                sb.append(strArrSplit[i]);
                sb.append("=");
                sb.append(objArr[i]);
                if (i != strArrSplit.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    private ControlRequestParser() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean b(int i) {
        return (i < 32 && i != 9) || i == 127;
    }

    private static a c(String str) throws IOException {
        String[] strArrSplit = str.split("\r\n", -1);
        if (strArrSplit.length > 67 || strArrSplit.length < 3) {
            d0.c("CTRL_HEADER_LINES");
            return null;
        }
        String[] strArrSplit2 = strArrSplit[0].split(" ", -1);
        if (strArrSplit2.length != 3 || (!strArrSplit2[2].equals("HTTP/1.1") && !strArrSplit2[2].equals("HTTP/1.0") && !strArrSplit2[2].equals("RTSP/1.0") && !strArrSplit2[2].equals("EVENT/1.0"))) {
            d0.c("CTRL_REQUEST_LINE");
            return null;
        }
        HashSet hashSet = new HashSet();
        int i = 1;
        boolean z = false;
        for (int i2 = 1; i2 < strArrSplit.length; i2++) {
            String str2 = strArrSplit[i2];
            if (!str2.isEmpty()) {
                int iIndexOf = str2.indexOf(58);
                if (iIndexOf < 1 || str2.startsWith(" ") || str2.startsWith("\t")) {
                    d0.c("CTRL_HEADER_FIELD");
                    return null;
                }
                String lowerCase = str2.substring(0, iIndexOf).toLowerCase(Locale.ROOT);
                String strTrim = str2.substring(iIndexOf + 1).trim();
                if (!lowerCase.matches("[a-z0-9-]{1,64}") || strTrim.length() > 1024 || strTrim.chars().anyMatch(new k(i)) || !hashSet.add(lowerCase)) {
                    d0.c("CTRL_HEADER_FIELD");
                    return null;
                }
                if (lowerCase.equals("transfer-encoding") || (lowerCase.equals("content-length") && !strTrim.equals("0"))) {
                    d0.c("CTRL_BODY_NOT_SUPPORTED");
                    return null;
                }
                if (!lowerCase.equals("airplay-receiver-device-id")) {
                    continue;
                } else {
                    if (strTrim.isEmpty() || strTrim.length() > 128 || !strTrim.matches("[A-Za-z0-9:._-]+")) {
                        d0.c("CTRL_RECEIVER_IDENTIFIER");
                        return null;
                    }
                    z = true;
                }
            }
        }
        if (!strArrSplit2[0].equals("GET")) {
            return new a(405, false, z);
        }
        String str3 = strArrSplit2[1];
        if (str3.length() > 512) {
            return new a(414, false, z);
        }
        boolean z2 = str3.equals("/ctrl-int/1/connect") || str3.startsWith("/ctrl-int/1/connect?");
        return new a(z2 ? 200 : 404, z2, z);
    }

    public static a d(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        while (byteArrayOutputStream.size() < 8192) {
            int i2 = inputStream.read();
            if (i2 < 0) {
                d0.c("CTRL_HEADER_TRUNCATED");
                return null;
            }
            if (i2 == 0 || i2 > 127) {
                d0.c("CTRL_HEADER_ENCODING");
                return null;
            }
            byteArrayOutputStream.write(i2);
            i = (i << 8) | i2;
            if (i == 218762506) {
                return c(byteArrayOutputStream.toString(StandardCharsets.US_ASCII));
            }
        }
        d0.c("CTRL_HEADER_LIMIT");
        return null;
    }

    public static byte[] e(int i) {
        String str;
        if (i == 200) {
            str = "OK";
        } else if (i == 414) {
            str = "URI Too Long";
        } else if (i == 503) {
            str = "Service Unavailable";
        } else if (i != 404) {
            str = i != 405 ? "Bad Request" : "Method Not Allowed";
        } else {
            str = "Not Found";
        }
        return ("HTTP/1.1 " + i + " " + str + "\r\nContent-Length: 0\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII);
    }
}
