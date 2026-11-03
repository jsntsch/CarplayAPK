package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public class m2 {
    private int a;
    private byte[] b;
    private int c;

    public m2(byte[] bArr, int i, int i2) {
        this.b = ByteArrays.d(bArr);
        this.c = i;
        this.a = i2;
    }

    public int a() {
        return this.c;
    }

    public byte[] b() {
        return ByteArrays.d(this.b);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof m2)) {
            return false;
        }
        m2 m2Var = (m2) obj;
        if (m2Var.c != this.c) {
            return false;
        }
        return ByteArrays.b(this.b, m2Var.b);
    }

    public int hashCode() {
        return ByteArrays.g(this.b) ^ this.c;
    }

    public m2(byte[] bArr, int i) {
        this(bArr, i, -1);
    }
}
