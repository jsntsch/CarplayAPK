package io.carplay.app.secured;

import java.io.EOFException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedChannelException;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: classes.dex */
public final class u5 {
    private long a;
    private long b;
    private long c = -1;
    private long d = -1;
    private int e;
    private boolean f;

    public static final class a extends w1 {
        private final long a;
        private final int b;
        private final long c;

        public a(long j, int i, long j2) {
            this.a = j;
            this.b = i;
            this.c = j2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.b == aVar.b && this.a == aVar.a && this.c == aVar.c;
        }

        public long g() {
            return this.c;
        }

        public long h() {
            return this.a;
        }

        public final int hashCode() {
            int i = this.b;
            long j = this.a;
            long j2 = this.c;
            return Long.hashCode(j2) + w1.a(j, i * 31, 31);
        }

        public int i() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), Integer.valueOf(this.b), Long.valueOf(this.c)};
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

    public static boolean k(String str) {
        return str.equals("RFCOMM_SELECTED_PEER_UNREACHABLE") || str.equals("WIFI_SELECTED_NETWORK_LOST") || str.equals("WIFI_SELECTED_NETWORK_REPLACED") || str.equals("WIRELESS_BOOTSTRAP_DEADLINE") || str.startsWith("SocketException:") || str.startsWith("SocketTimeoutException:") || str.startsWith("EOFException:") || str.startsWith("IOException: IAP2_ACK_TIMEOUT_") || str.startsWith("IOException: IAP2_IDENTIFICATION_AUTH_BOOTSTRAP_DEADLINE") || str.startsWith("IOException: IAP2_PEER_RESTARTED") || str.startsWith("IOException: IAP2_PEER_RESET");
    }

    public static boolean l(Throwable th) {
        int i = 0;
        boolean z = false;
        while (th != null && i < 16) {
            if ((th instanceof GeneralSecurityException) || (th instanceof SecurityException)) {
                return false;
            }
            if ((th instanceof SocketException) || (th instanceof SocketTimeoutException) || (th instanceof EOFException) || (th instanceof ClosedChannelException)) {
                z = true;
            }
            i++;
            th = th.getCause();
        }
        return th == null && z;
    }

    public synchronized boolean a(long j) {
        return !this.f && this.a == j;
    }

    public synchronized long b() {
        if (this.f) {
            return -1L;
        }
        this.d = -1L;
        this.c = -1L;
        long j = this.a + 1;
        this.a = j;
        return j;
    }

    public synchronized long c() {
        long j;
        if (this.f) {
            j = -1;
        } else {
            j = this.b + 1;
            this.b = j;
        }
        return j;
    }

    public synchronized boolean d(long j) {
        return !this.f && this.b == j;
    }

    public synchronized long e() {
        return this.f ? -1L : this.a;
    }

    public synchronized a f(long j, long j2) {
        long j3;
        try {
            if (!a(j)) {
                return null;
            }
            long j4 = this.c;
            if (j4 >= 0 && j2 - j4 >= 30000) {
                long j5 = this.d;
                if (j5 >= 0 && j2 >= j5 && j2 - j5 <= 5000) {
                    this.e = 0;
                }
            }
            this.d = -1L;
            this.c = -1L;
            long j6 = this.a + 1;
            this.a = j6;
            int i = this.e;
            if (i >= 3) {
                return new a(j6, 0, 0L);
            }
            int i2 = i + 1;
            this.e = i2;
            if (i2 == 1) {
                j3 = 1000;
            } else {
                j3 = i2 == 2 ? 3000L : 8000L;
            }
            return new a(j6, i2, j3);
        } catch (Throwable th) {
            throw th;
        }
    }

    public synchronized a g(long j, long j2, long j3) {
        return d(j) ? f(j2, j3) : null;
    }

    public synchronized void h() {
        this.f = true;
        this.a++;
        this.b++;
        this.d = -1L;
        this.c = -1L;
    }

    public synchronized boolean i(long j, long j2) {
        return d(j) && j(j2);
    }

    public synchronized boolean j(long j) {
        if (!a(j)) {
            return false;
        }
        h();
        return true;
    }

    public synchronized void m(long j, long j2) {
        if (a(j)) {
            long j3 = this.d;
            if (j3 < 0 || j2 < j3 || j2 - j3 > 5000) {
                this.c = j2;
            }
            this.d = j2;
        }
    }

    public synchronized void n(long j) {
        if (a(j)) {
            this.d = -1L;
            this.c = -1L;
        }
    }
}
