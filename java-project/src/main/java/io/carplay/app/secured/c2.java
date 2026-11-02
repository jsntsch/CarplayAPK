package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public final class c2 {
    private final o a;
    private final boolean b;

    public c2(boolean z, o oVar) {
        if (z && oVar == null) {
            d0.b("negotiated cipher required");
            throw null;
        }
        this.b = z;
        this.a = oVar;
    }

    public byte[] a(r6.c cVar) throws r5 {
        if (cVar == null || !cVar.h().d()) {
            d0.b("structured control only");
            return null;
        }
        if (cVar.i().g() != cVar.g().length || cVar.g().length > 32767) {
            d0.f("wire body length mismatch");
            return null;
        }
        byte[] bArrG = cVar.g();
        if (this.b && bArrG.length != 0) {
            bArrG = this.a.b(bArrG);
        }
        if (bArrG != null && bArrG.length <= 32767) {
            return (byte[]) bArrG.clone();
        }
        d0.f("invalid plaintext length");
        return null;
    }

    public r6.c b(r6.a aVar, int i, byte[] bArr) throws r5 {
        if (aVar == null || !aVar.d() || bArr == null) {
            d0.b("structured control only");
            return null;
        }
        if (bArr.length > 32767) {
            d0.f("plaintext body exceeds signed short limit");
            return null;
        }
        byte[] bArrA = (!this.b || bArr.length == 0) ? (byte[]) bArr.clone() : this.a.a((byte[]) bArr.clone());
        if (bArrA != null && bArrA.length <= 32767) {
            return new r6.c(aVar, new r6.b(bArrA.length, 0L, 0, i), bArrA);
        }
        d0.f("ciphertext body exceeds signed short limit");
        return null;
    }
}
