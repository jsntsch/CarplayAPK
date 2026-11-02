package io.carplay.app.secured;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public final class h {
    private final List<byte[]> a;
    private final byte[] b;
    private final byte[] c;
    private final boolean d;
    private final long e;
    private final long f;
    private final long g;
    private final int h;

    public h(List<byte[]> list, byte[] bArr, byte[] bArr2, boolean z, long j, long j2, long j3) {
        if (list == null || list.isEmpty() || list.size() > 4096 || j3 < 0) {
            SyntheticRefs.b("AVC unit bounds");
            throw null;
        }
        ArrayList arrayList = new ArrayList();
        boolean z2 = false;
        boolean z3 = false;
        int iAddExact = 0;
        for (byte[] bArr3 : list) {
            if (bArr3 == null || bArr3.length == 0 || bArr3.length > 2097152) {
                SyntheticRefs.b("AVC NAL bounds");
                throw null;
            }
            iAddExact = Math.addExact(iAddExact, bArr3.length + 4);
            if (iAddExact > 4194304) {
                SyntheticRefs.b("AVC unit limit");
                throw null;
            }
            byte b = bArr3[0];
            boolean z4 = true;
            z3 |= (b & 31) == 5;
            if ((b & 31) != 1 && (b & 31) != 5) {
                z4 = false;
            }
            z2 |= z4;
            arrayList.add((byte[]) bArr3.clone());
        }
        if (!z2 || z3 != z) {
            SyntheticRefs.b("AVC unit picture mismatch");
            throw null;
        }
        this.a = List.copyOf(arrayList);
        this.b = bArr == null ? null : (byte[]) bArr.clone();
        this.c = bArr2 != null ? (byte[]) bArr2.clone() : null;
        this.d = z;
        this.e = j & 4294967295L;
        this.f = j2;
        this.g = j3;
        this.h = iAddExact;
    }

    public static h d(byte[] bArr, byte[] bArr2, byte[] bArr3, long j, long j2, long j3) throws IOException {
        byte[] bArrA = i.a(bArr);
        ArrayList arrayList = new ArrayList();
        int iM = m(bArrA, 0);
        if (iM != 0) {
            SyntheticRefs.c("AVC complete unit prefix");
            return null;
        }
        byte[] bArr4 = bArr2;
        int i = iM;
        byte[] bArr5 = bArr3;
        boolean z = false;
        while (i >= 0) {
            int i2 = i + (bArrA[i + 2] == 1 ? 3 : 4);
            int iM2 = m(bArrA, i2);
            int length = iM2 < 0 ? bArrA.length : iM2;
            if (length > i2) {
                byte[] bArrCopyOfRange = Arrays.copyOfRange(bArrA, i2, length);
                int i3 = bArrCopyOfRange[0] & 31;
                if (i3 == 7) {
                    bArr4 = bArrCopyOfRange;
                }
                if (i3 == 8) {
                    bArr5 = bArrCopyOfRange;
                }
                z |= i3 == 5;
                arrayList.add(bArrCopyOfRange);
            }
            i = iM2;
        }
        try {
            return new h(arrayList, bArr4, bArr5, z, j, j2, j3);
        } catch (IllegalArgumentException e) {
            throw new IOException("AVC complete unit invalid", e);
        }
    }

    private static int m(byte[] bArr, int i) {
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

    public byte[] a() {
        return i.c(this.a);
    }

    public int b() {
        return this.h;
    }

    public long c() {
        return this.f;
    }

    public boolean e() {
        return this.d;
    }

    public List<byte[]> f() {
        ArrayList arrayList = new ArrayList();
        Iterator<byte[]> it = this.a.iterator();
        while (it.hasNext()) {
            arrayList.add((byte[]) it.next().clone());
        }
        return List.copyOf(arrayList);
    }

    public List<byte[]> g() {
        return this.a;
    }

    public byte[] h() {
        byte[] bArr = this.c;
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
    }

    public byte[] i() {
        return this.c;
    }

    public long j() {
        return this.g;
    }

    public byte[] k() {
        byte[] bArr = this.b;
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
    }

    public byte[] l() {
        return this.b;
    }

    public long n() {
        return this.e;
    }
}
