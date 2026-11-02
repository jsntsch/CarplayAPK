package io.carplay.app.secured;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
/**
 * VideoCipher
 *
 * <p>原混淆名：{@code io.carplay.app.secured.j1}（jadx 产物 j1.java）
 *
 * <p>命名依据：closed/exhausted video cipher + AVC NAL
 */
public final class VideoCipher {
    static final String a = "21A329";

    public static final class c implements AutoCloseable {
        private final byte[] a;
        private long b;
        private boolean c;

        public c(byte[] bArr, long j) {
            this.a = VideoCipher.b(bArr, j);
        }

        public synchronized byte[] b(List<byte[]> list, long j) {
            byte[] byteArray;
            byte[] bArr;
            byte[] bArr2;
            long j2;
            try {
                if (this.c || this.b == -1) {
                    throw new IOException("closed/exhausted video cipher");
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                for (byte[] bArr3 : list) {
                    if (bArr3.length < 1 || bArr3.length > 2097152) {
                        throw new IOException("AVC NAL limit");
                    }
                    int length = bArr3.length;
                    byteArrayOutputStream.write(length >>> 24);
                    byteArrayOutputStream.write(length >>> 16);
                    byteArrayOutputStream.write(length >>> 8);
                    byteArrayOutputStream.write(length);
                    byteArrayOutputStream.writeBytes(bArr3);
                }
                byteArray = byteArrayOutputStream.toByteArray();
                if (byteArray.length == 0 || byteArray.length > 4194304) {
                    throw new IOException("video access unit limit");
                }
                bArr = new byte[Iap2Csm.b];
                ByteBuffer.wrap(bArr).order(ByteOrder.LITTLE_ENDIAN).putInt(byteArray.length + 16).putLong(8, j);
                bArr2 = this.a;
                j2 = this.b;
                this.b = 1 + j2;
            } catch (Throwable th) {
                throw th;
            }
            return DerCodec.d(bArr, DerCodec.s(bArr2, DerCodec.m(j2), byteArray, bArr));
        }

        @Override // java.lang.AutoCloseable
        public synchronized void close() {
            this.c = true;
            Arrays.fill(this.a, (byte) 0);
        }
    }

    private VideoCipher() {
    }

    public static long a(long j) {
        return ((((long) Math.floorMod(j, 1000)) * 4294967296L) / 1000) | ((Math.floorDiv(j, 1000) + 2208988800L) << 32);
    }

    public static byte[] b(byte[] bArr, long j) {
        if (j <= 0) {
            SyntheticRefs.b("positive own stream identifier required");
            return null;
        }
        return DerCodec.k(bArr, "DataStream-Salt" + j, "DataStream-Output-Encryption-Key");
    }

    public static byte[] c(byte[] bArr, byte[] bArr2) {
        return e(bArr, bArr2, 0, 0);
    }

    public static byte[] d(byte[] bArr, byte[] bArr2, int i, int i2) throws IOException {
        if (i >= 1 && i2 >= 1 && i <= 4096 && i2 <= 4096) {
            return e(bArr, bArr2, i, i2);
        }
        SyntheticRefs.c("coded video dimensions outside supported bounds");
        return null;
    }

    private static byte[] e(byte[] bArr, byte[] bArr2, int i, int i2) throws IOException {
        if (bArr == null || bArr2 == null || bArr.length < 4 || bArr.length > 65535 || bArr2.length < 1 || bArr2.length > 65535 || (bArr[0] & 31) != 7 || (bArr2[0] & 31) != 8) {
            SyntheticRefs.c("actual AVC SPS/PPS required");
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(1);
        byteArrayOutputStream.write(bArr[1]);
        byteArrayOutputStream.write(bArr[2]);
        byteArrayOutputStream.write(bArr[3]);
        byteArrayOutputStream.write(255);
        byteArrayOutputStream.write(225);
        byteArrayOutputStream.write(bArr.length >>> 8);
        byteArrayOutputStream.write(bArr.length);
        byteArrayOutputStream.writeBytes(bArr);
        byteArrayOutputStream.write(1);
        byteArrayOutputStream.write(bArr2.length >>> 8);
        byteArrayOutputStream.write(bArr2.length);
        byteArrayOutputStream.writeBytes(bArr2);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        byte[] bArr3 = new byte[Iap2Csm.b];
        ByteBuffer byteBufferOrder = ByteBuffer.wrap(bArr3).order(ByteOrder.LITTLE_ENDIAN);
        byteBufferOrder.putInt(byteArray.length).put(4, (byte) 1);
        if (i != 0) {
            byteBufferOrder.put(6, (byte) 4);
            byteBufferOrder.putFloat(16, i).putFloat(20, i2);
        }
        return DerCodec.d(bArr3, byteArray);
    }

    public static final class b {
        private boolean c;
        private byte[] d;
        private byte[] e;
        private C0002b f;
        private C0002b g;
        private byte[] a = new byte[0];
        private final List<byte[]> b = new ArrayList();
        private C0002b[] h = new C0002b[0];

        @FunctionalInterface
        public interface a {
            void d(List<byte[]> list);
        }

        @FunctionalInterface
        public interface c {
            void c(List<byte[]> list, long j, long j2, long j3);
        }

        private static List<byte[]> d(List<byte[]> list) {
            ArrayList arrayList = new ArrayList();
            Iterator<byte[]> it = list.iterator();
            while (it.hasNext()) {
                arrayList.add((byte[]) it.next().clone());
            }
            return List.copyOf(arrayList);
        }

        private void e(c cVar) {
            if (this.c) {
                C0002b c0002b = this.g;
                cVar.c(List.copyOf(this.b), c0002b.a, c0002b.b, c0002b.c);
            }
            this.b.clear();
            this.c = false;
            this.g = null;
        }

        /* JADX WARN: Code duplicated, block: B:9:0x001c  */
        private static Integer f(byte[] bArr, boolean z) throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int i = 0;
            for (int i2 = 1; i2 < Math.min(bArr.length, 16); i2++) {
                int i3 = bArr[i2] & 255;
                if (i < 2 || i3 != 3) {
                    byteArrayOutputStream.write(i3);
                    if (i3 == 0) {
                        i++;
                    } else {
                        i = 0;
                    }
                } else {
                    i = 0;
                }
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            int i4 = 0;
            for (int i5 = 0; i5 <= 24; i5++) {
                if (i4 >= byteArray.length * 8) {
                    if (!z) {
                        return null;
                    }
                    SyntheticRefs.c("AVC slice header invalid");
                    return null;
                }
                int i6 = (byteArray[i4 / 8] >>> (7 - (i4 % 8))) & 1;
                i4++;
                if (i6 == 1) {
                    int i7 = i4;
                    int i8 = 0;
                    for (int i9 = 0; i9 < i5; i9++) {
                        if (i7 >= byteArray.length * 8) {
                            if (!z) {
                                return null;
                            }
                            SyntheticRefs.c("truncated AVC slice");
                            return null;
                        }
                        i8 = (i8 << 1) | ((byteArray[i7 / 8] >>> (7 - (i7 % 8))) & 1);
                        i7++;
                    }
                    return Integer.valueOf(((1 << i5) - 1) + i8);
                }
            }
            SyntheticRefs.c("AVC slice header invalid");
            return null;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void g(a aVar, List list, long j, long j2, long j3) {
            aVar.d(d(list));
        }

        private void h(byte[] bArr, C0002b c0002b, c cVar) throws IOException {
            int length = 0;
            int i = bArr[0] & 31;
            boolean z = i == 1 || i == 5;
            Integer numF = z ? f(bArr, true) : null;
            if (i == 9 || (((i == 6 || i == 7 || i == 8) && this.c) || (z && this.c && numF.intValue() == 0))) {
                e(cVar);
            }
            if (i == 7) {
                this.d = (byte[]) bArr.clone();
            }
            if (i == 8) {
                this.e = (byte[]) bArr.clone();
            }
            if (i == 1 || i == 5 || i == 6 || i == 7 || i == 8 || i == 9) {
                if (this.b.isEmpty()) {
                    this.g = c0002b;
                }
                this.b.add(bArr);
                this.c |= z;
            }
            Iterator<byte[]> it = this.b.iterator();
            while (it.hasNext()) {
                length += it.next().length;
            }
            if (length <= 4194304) {
                return;
            }
            SyntheticRefs.c("AVC access unit limit");
        }

        private static int j(byte[] bArr, int i) {
            return bArr[i + 2] == 1 ? 3 : 4;
        }

        private static C0002b n(int i, int i2, C0002b c0002b, C0002b[] c0002bArr, C0002b c0002b2) {
            if (i < i2) {
                if (i >= i2 - c0002bArr.length) {
                    return c0002bArr[i - (i2 - c0002bArr.length)];
                }
                if (c0002b != null) {
                    return c0002b;
                }
            }
            return c0002b2;
        }

        private static int o(byte[] bArr, int i) {
            byte b;
            int i2;
            while (true) {
                int i3 = i + 2;
                if (i3 >= bArr.length) {
                    return -1;
                }
                if (bArr[i] == 0 && bArr[i + 1] == 0 && ((b = bArr[i3]) == 1 || ((i2 = i + 3) < bArr.length && b == 0 && bArr[i2] == 1))) {
                    return i;
                }
                i++;
            }
        }

        private static C0002b[] p(int i, int i2, C0002b c0002b, C0002b[] c0002bArr, C0002b c0002b2) {
            int iMin = Math.min(3, i);
            C0002b[] c0002bArr2 = new C0002b[iMin];
            for (int i3 = 0; i3 < iMin; i3++) {
                c0002bArr2[i3] = n((i - iMin) + i3, i2, c0002b, c0002bArr, c0002b2);
            }
            return c0002bArr2;
        }

        public void b(byte[] bArr, a aVar) throws IOException {
            long jNanoTime = System.nanoTime();
            k1 k1Var = new k1(0);
            k1Var.b = aVar;
            VarHandle.storeStoreFence();
            c(bArr, 0L, jNanoTime, 0L, k1Var);
        }

        public void c(byte[] bArr, long j, long j2, long j3, c cVar) throws IOException {
            if (bArr == null || cVar == null || j3 < 0) {
                SyntheticRefs.c("AVC input invalid");
                return;
            }
            if (bArr.length == 0) {
                return;
            }
            if (bArr.length > 2097152 || this.a.length + bArr.length > 4194304) {
                SyntheticRefs.c("AnnexB pending limit");
                return;
            }
            C0002b c0002b = new C0002b(j, j2, j3, 0);
            byte[] bArr2 = this.a;
            int length = bArr2.length;
            C0002b c0002b2 = this.f;
            C0002b[] c0002bArr = this.h;
            byte[] bArrD = DerCodec.d(bArr2, bArr);
            this.a = bArrD;
            int iO = o(bArrD, 0);
            if (iO < 0) {
                byte[] bArr3 = this.a;
                if (bArr3.length > 4) {
                    SyntheticRefs.c("AVC start code missing");
                    return;
                }
                if (c0002b2 == null) {
                    this.f = c0002b;
                }
                this.h = p(bArr3.length, length, c0002b2, c0002bArr, c0002b);
                return;
            }
            if (iO > 0) {
                SyntheticRefs.c("unexpected bytes before AVC start code");
                return;
            }
            while (true) {
                int iJ = j(this.a, iO) + iO;
                int iO2 = o(this.a, iJ);
                if (iO2 < 0) {
                    break;
                }
                if (iO2 > iJ) {
                    h(Arrays.copyOfRange(this.a, iJ, iO2), n(iO, length, c0002b2, c0002bArr, c0002b), cVar);
                }
                iO = iO2;
            }
            this.f = n(iO, length, c0002b2, c0002bArr, c0002b);
            this.h = p(this.a.length, length, c0002b2, c0002bArr, c0002b);
            byte[] bArr4 = this.a;
            byte[] bArrCopyOfRange = Arrays.copyOfRange(bArr4, iO, bArr4.length);
            this.a = bArrCopyOfRange;
            int iJ2 = j(bArrCopyOfRange, 0);
            if (this.c) {
                byte[] bArr5 = this.a;
                if (bArr5.length > iJ2) {
                    int i = bArr5[iJ2] & 31;
                    Integer numF = (i == 1 || i == 5) ? f(Arrays.copyOfRange(bArr5, iJ2, Math.min(bArr5.length, iJ2 + 16)), false) : null;
                    if (i == 6 || i == 7 || i == 8 || i == 9 || (numF != null && numF.intValue() == 0)) {
                        e(cVar);
                    }
                }
            }
        }

        public byte[] i() {
            byte[] bArr = this.e;
            if (bArr == null) {
                return null;
            }
            return (byte[]) bArr.clone();
        }

        public void k() {
            this.a = new byte[0];
            this.b.clear();
            this.c = false;
            this.d = null;
            this.e = null;
            this.f = null;
            this.g = null;
            this.h = new C0002b[0];
        }

        public void l(byte[] bArr, byte[] bArr2) throws IOException {
            if (this.a.length != 0 || !this.b.isEmpty()) {
                SyntheticRefs.c("AVC resume parameters must precede incoming video");
                return;
            }
            VideoCipher.c(bArr, bArr2);
            this.d = (byte[]) bArr.clone();
            this.e = (byte[]) bArr2.clone();
        }

        public byte[] m() {
            byte[] bArr = this.d;
            if (bArr == null) {
                return null;
            }
            return (byte[]) bArr.clone();
        }

        /* JADX INFO: renamed from: io.carplay.app.secured.VideoCipher$b$b, reason: collision with other inner class name */
        public static final class C0002b extends w1 {
            private final long a;
            private final long b;
            private final long c;

            private C0002b(long j, long j2, long j3) {
                this.a = j;
                this.b = j2;
                this.c = j3;
            }

            public final boolean equals(Object obj) {
                if (!(obj instanceof C0002b)) {
                    return false;
                }
                C0002b c0002b = (C0002b) obj;
                return this.a == c0002b.a && this.b == c0002b.b && this.c == c0002b.c;
            }

            public final int hashCode() {
                long j = this.a;
                long j2 = this.b;
                long j3 = this.c;
                return Long.hashCode(j3) + w1.a(j2, Long.hashCode(j) * 31, 31);
            }

            public long j() {
                return this.b;
            }

            public long k() {
                return this.a;
            }

            public long l() {
                return this.c;
            }

            public final String toString() {
                Object[] objArr = {Long.valueOf(this.a), Long.valueOf(this.b), Long.valueOf(this.c)};
                String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
                StringBuilder sb = new StringBuilder();
                w1.f(C0002b.class, sb, "[");
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

            public /* synthetic */ C0002b(long j, long j2, long j3, int i) {
                this(j, j2, j3);
            }
        }
    }

    public static final class a implements AutoCloseable {
        private final byte[] a;
        private final int b;
        private final int c;
        private int d;
        private long e;
        private long f;
        private boolean g;

        public a(byte[] bArr, long j, int i, int i2) {
            if (i2 != 100 && i2 != 101 && i2 != 102) {
                SyntheticRefs.b("unsupported modern audio stream type");
                throw null;
            }
            this.a = VideoCipher.b(bArr, j);
            this.b = i;
            this.c = i2;
        }

        public synchronized byte[] b(byte[] bArr) {
            byte[] bArrArray;
            byte[] bArrM;
            if (this.g || this.f == -1) {
                throw new IOException("closed/exhausted audio cipher");
            }
            int i = this.c;
            if ((i != 100 && i != 102) || bArr == null || bArr.length < 1 || bArr.length > 16384) {
                throw new IOException("bounded raw AAC-LC packet required");
            }
            ByteBuffer byteBufferPut = ByteBuffer.allocate(12).order(ByteOrder.BIG_ENDIAN).put((byte) -128).put((byte) this.c);
            int i2 = this.d;
            this.d = i2 + 1;
            bArrArray = byteBufferPut.putShort((short) i2).putInt((int) this.e).putInt(this.b).array();
            this.e = (this.e + 1024) & 4294967295L;
            long j = this.f;
            this.f = 1 + j;
            bArrM = DerCodec.m(j);
            return DerCodec.d(bArrArray, DerCodec.s(this.a, bArrM, bArr, Arrays.copyOfRange(bArrArray, 4, 12)), Arrays.copyOfRange(bArrM, 4, 12));
        }

        public synchronized byte[] c(byte[] bArr, int i) {
            byte[] bArrArray;
            byte[] bArr2;
            byte[] bArrM;
            try {
                if (this.g || this.f == -1) {
                    throw new IOException("closed/exhausted audio cipher");
                }
                if (this.c == 102) {
                    throw new IOException("PCM is not a MainHigh AAC access unit");
                }
                if (i >= 1 && i <= 2 && bArr.length != 0) {
                    int i2 = i * 2;
                    if (bArr.length % i2 == 0 && bArr.length <= i * 704) {
                        ByteBuffer byteBufferPut = ByteBuffer.allocate(12).order(ByteOrder.BIG_ENDIAN).put((byte) -128).put((byte) this.c);
                        int i3 = this.d;
                        this.d = i3 + 1;
                        bArrArray = byteBufferPut.putShort((short) i3).putInt((int) this.e).putInt(this.b).array();
                        this.e += (long) (bArr.length / i2);
                        bArr2 = (byte[]) bArr.clone();
                        for (int i4 = 0; i4 < bArr2.length; i4 += 2) {
                            byte b = bArr2[i4];
                            int i5 = i4 + 1;
                            bArr2[i4] = bArr2[i5];
                            bArr2[i5] = b;
                        }
                        long j = this.f;
                        this.f = 1 + j;
                        bArrM = DerCodec.m(j);
                    }
                }
                throw new IOException("aligned bounded PCM16 packet required");
            } catch (Throwable th) {
                throw th;
            }
            return DerCodec.d(bArrArray, DerCodec.s(this.a, bArrM, bArr2, Arrays.copyOfRange(bArrArray, 4, 12)), Arrays.copyOfRange(bArrM, 4, 12));
        }

        @Override // java.lang.AutoCloseable
        public synchronized void close() {
            this.g = true;
            Arrays.fill(this.a, (byte) 0);
        }

        public synchronized void e(long j) {
            if (this.g || this.f == -1) {
                throw new IOException("closed/exhausted audio cipher");
            }
            if (j < 0) {
                throw new IOException("audio skip frame limit");
            }
            this.e = 4294967295L & (this.e + j);
        }

        public a(byte[] bArr, long j, int i) {
            this(bArr, j, i, 100);
        }
    }
}
