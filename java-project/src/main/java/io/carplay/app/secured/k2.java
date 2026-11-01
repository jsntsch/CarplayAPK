package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public class k2 {
    private byte[] a;
    private int b;

    public k2(byte[] bArr, int i) {
        this.a = e.d(bArr);
        this.b = i;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof k2)) {
            return false;
        }
        k2 k2Var = (k2) obj;
        if (k2Var.b != this.b) {
            return false;
        }
        return e.b(this.a, k2Var.a);
    }

    public int hashCode() {
        return e.g(this.a) ^ this.b;
    }
}
