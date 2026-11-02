package io.carplay.app.secured;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
/**
 * ProtobufReader
 *
 * <p>原混淆名：{@code io.carplay.app.secured.q5}（jadx 产物 q5.java）
 *
 * <p>命名依据：invalid field number / truncated protobuf field
 */
public final class ProtobufReader {

    public static final class a {
        private final ByteArrayOutputStream a = new ByteArrayOutputStream();

        private void f(int i, int i2) {
            if (i <= 0 || i > 536870911) {
                SyntheticRefs.b("invalid field number");
            } else {
                h(((long) i2) | (((long) i) << 3));
            }
        }

        private void h(long j) {
            while (true) {
                long j2 = (-128) & j;
                ByteArrayOutputStream byteArrayOutputStream = this.a;
                if (j2 == 0) {
                    byteArrayOutputStream.write((int) j);
                    return;
                } else {
                    byteArrayOutputStream.write((((int) j) & 127) | Iap2Csm.b);
                    j >>>= 7;
                }
            }
        }

        public a a(int i, boolean z) {
            f(i, 0);
            h(z ? 1L : 0L);
            return this;
        }

        public a b(int i, byte[] bArr) {
            Objects.requireNonNull(bArr);
            f(i, 2);
            h(bArr.length);
            this.a.writeBytes(bArr);
            return this;
        }

        public a c(int i, int i2) {
            f(i, 0);
            h(i2);
            return this;
        }

        public a d(int i, a aVar) {
            return b(i, aVar.g());
        }

        public a e(int i, String str) {
            Objects.requireNonNull(str);
            return b(i, str.getBytes(StandardCharsets.UTF_8));
        }

        public byte[] g() {
            return this.a.toByteArray();
        }
    }

    public static final class b {
        private final byte[] a;
        private int b;

        public b(byte[] bArr) {
            this.a = bArr;
        }

        public byte[] b(int i) throws r5 {
            if (i >= 0) {
                byte[] bArr = this.a;
                int length = bArr.length;
                int i2 = this.b;
                if (i <= length - i2) {
                    byte[] bArrCopyOfRange = Arrays.copyOfRange(bArr, i2, i2 + i);
                    this.b += i;
                    return bArrCopyOfRange;
                }
            }
            SyntheticRefs.f("truncated protobuf field");
            return null;
        }

        public long c() throws r5 {
            long j = 0;
            for (int i = 0; i < 10; i++) {
                int i2 = this.b;
                byte[] bArr = this.a;
                if (i2 >= bArr.length) {
                    SyntheticRefs.f("truncated protobuf varint");
                    return 0L;
                }
                this.b = i2 + 1;
                byte b = bArr[i2];
                if (i == 9 && (b & 254) != 0) {
                    SyntheticRefs.f("protobuf varint overflow");
                    return 0L;
                }
                j |= ((long) (b & 127)) << (i * 7);
                if ((b & 128) == 0) {
                    return j;
                }
            }
            SyntheticRefs.f("protobuf varint overflow");
            return 0L;
        }
    }

    public static final class d extends w1 {
        private final int a;
        private final int b;
        private final int c;

        public d(int i, int i2, int i3) {
            if (i < 0 || i2 < 0 || i3 < 0) {
                SyntheticRefs.b("negative protobuf limit");
                throw null;
            }
            this.a = i;
            this.b = i2;
            this.c = i3;
        }

        public static d j() {
            return new d(32767, 1024, 32767);
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof d)) {
                return false;
            }
            d dVar = (d) obj;
            return this.a == dVar.a && this.b == dVar.b && this.c == dVar.c;
        }

        public final int hashCode() {
            int i = this.a;
            return (((i * 31) + this.b) * 31) + this.c;
        }

        public int k() {
            return this.a;
        }

        public int l() {
            return this.c;
        }

        public int m() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(d.class, sb, "[");
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

    private ProtobufReader() {
    }

    public static e a(byte[] bArr) throws r5 {
        return b(bArr, d.j());
    }

    public static e b(byte[] bArr, d dVar) throws r5 {
        byte[] bArr2;
        byte[] bArrB;
        Objects.requireNonNull(bArr);
        Objects.requireNonNull(dVar);
        if (bArr.length > dVar.a) {
            SyntheticRefs.f("protobuf exceeds byte limit");
            return null;
        }
        b bVar = new b(bArr);
        ArrayList arrayList = new ArrayList();
        while (bVar.b < bArr.length) {
            if (arrayList.size() >= dVar.b) {
                SyntheticRefs.f("protobuf exceeds field limit");
                return null;
            }
            long jC = bVar.c();
            long jC2 = 0;
            if (jC < 0 || jC > 4294967295L) {
                SyntheticRefs.f("invalid protobuf tag");
                return null;
            }
            int i = (int) (jC >>> 3);
            int i2 = ((int) jC) & 7;
            if (i == 0 || i > 536870911) {
                SyntheticRefs.f("invalid protobuf field number");
                return null;
            }
            if (i2 != 0) {
                if (i2 == 1) {
                    bArrB = bVar.b(8);
                } else if (i2 == 2) {
                    long jC3 = bVar.c();
                    if (jC3 < 0 || jC3 > dVar.c || jC3 > 2147483647L) {
                        SyntheticRefs.f("protobuf field length overflow/limit");
                        return null;
                    }
                    bArrB = bVar.b((int) jC3);
                } else {
                    if (i2 != 5) {
                        SyntheticRefs.f(w1.d("unsupported protobuf wire type ", i2));
                        return null;
                    }
                    bArrB = bVar.b(4);
                }
                bArr2 = bArrB;
            } else {
                jC2 = bVar.c();
                bArr2 = null;
            }
            arrayList.add(new c(i, i2, jC2, bArr2, 0));
        }
        return new e(arrayList);
    }

    public static final class e {
        private final List<c> a;

        private e(List<c> list) {
            this.a = List.copyOf(list);
        }

        private c b(int i) {
            c cVar = null;
            for (c cVar2 : this.a) {
                if (cVar2.a == i) {
                    cVar = cVar2;
                }
            }
            return cVar;
        }

        private c k(int i, int i2) throws r5 {
            c cVarB = b(i);
            if (cVarB == null) {
                SyntheticRefs.f(w1.d("missing protobuf field ", i));
                return null;
            }
            if (cVarB.b == i2) {
                return cVarB;
            }
            SyntheticRefs.f(w1.d("wrong wire type for field ", i));
            return null;
        }

        public boolean a(int i) {
            return b(i) != null;
        }

        public List<e> c(int i, d dVar) throws r5 {
            ArrayList arrayList = new ArrayList();
            for (c cVar : this.a) {
                if (cVar.a == i) {
                    if (cVar.b != 2) {
                        SyntheticRefs.f("wrong submessage wire");
                        return null;
                    }
                    arrayList.add(ProtobufReader.b(cVar.d, dVar));
                }
            }
            return List.copyOf(arrayList);
        }

        public boolean d(int i, boolean z) throws r5 {
            return a(i) ? g(i) : z;
        }

        public int e(int i, int i2) throws r5 {
            return a(i) ? i(i) : i2;
        }

        public String f(int i, String str) throws r5 {
            return a(i) ? j(i) : str;
        }

        public boolean g(int i) throws r5 {
            long j = k(i, 0).c;
            if (j == 0 || j == 1) {
                return j == 1;
            }
            SyntheticRefs.f(w1.d("invalid bool at field ", i));
            return false;
        }

        public byte[] h(int i) throws r5 {
            return (byte[]) k(i, 2).d.clone();
        }

        public int i(int i) throws r5 {
            long j = k(i, 0).c;
            if (j <= 2147483647L && j >= -2147483648L) {
                return (int) j;
            }
            SyntheticRefs.f(w1.d("int32 overflow at field ", i));
            return 0;
        }

        public String j(int i) throws r5 {
            try {
                CharsetDecoder charsetDecoderNewDecoder = StandardCharsets.UTF_8.newDecoder();
                CodingErrorAction codingErrorAction = CodingErrorAction.REPORT;
                return charsetDecoderNewDecoder.onMalformedInput(codingErrorAction).onUnmappableCharacter(codingErrorAction).decode(ByteBuffer.wrap(h(i))).toString();
            } catch (CharacterCodingException e) {
                throw new r5(w1.d("invalid UTF-8 at field ", i), e);
            }
        }

    }

    public static final class c extends w1 {
        private final int a;
        private final int b;
        private final long c;
        private final byte[] d;

        private c(int i, int i2, long j, byte[] bArr) {
            this.a = i;
            this.b = i2;
            this.c = j;
            this.d = bArr;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof c)) {
                return false;
            }
            c cVar = (c) obj;
            return this.a == cVar.a && this.b == cVar.b && this.c == cVar.c && Objects.equals(this.d, cVar.d);
        }

        public final int hashCode() {
            int i = this.a;
            int i2 = this.b;
            long j = this.c;
            byte[] bArr = this.d;
            return Objects.hashCode(bArr) + w1.a(j, ((i * 31) + i2) * 31, 31);
        }

        public byte[] k() {
            return this.d;
        }

        public int l() {
            return this.a;
        }

        public long m() {
            return this.c;
        }

        public int n() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Long.valueOf(this.c), this.d};
            String[] strArrSplit = "a;b;c;d".length() == 0 ? new String[0] : "a;b;c;d".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(c.class, sb, "[");
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

        public /* synthetic */ c(int i, int i2, long j, byte[] bArr, int i3) {
            this(i, i2, j, bArr);
        }
    }
}
