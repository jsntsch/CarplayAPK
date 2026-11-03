package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public final class CarLifeFrameCipher {
    private final o a;
    private final boolean b;

    public CarLifeFrameCipher(boolean z, o oVar) {
        if (z && oVar == null) {
            SyntheticRefs.b("negotiated cipher required");
            throw null;
        }
        this.b = z;
        this.a = oVar;
    }

    public byte[] a(CarLifeFrame.c cVar) throws ProtocolIOException {
        if (cVar == null || !cVar.h().d()) {
            SyntheticRefs.b("structured control only");
            return null;
        }
        if (cVar.i().g() != cVar.g().length || cVar.g().length > 32767) {
            SyntheticRefs.f("wire body length mismatch");
            return null;
        }
        byte[] bArrG = cVar.g();
        if (this.b && bArrG.length != 0) {
            bArrG = this.a.b(bArrG);
        }
        if (bArrG != null && bArrG.length <= 32767) {
            return (byte[]) bArrG.clone();
        }
        SyntheticRefs.f("invalid plaintext length");
        return null;
    }

    public CarLifeFrame.c b(CarLifeFrame.a aVar, int i, byte[] bArr) throws ProtocolIOException {
        if (aVar == null || !aVar.d() || bArr == null) {
            SyntheticRefs.b("structured control only");
            return null;
        }
        if (bArr.length > 32767) {
            SyntheticRefs.f("plaintext body exceeds signed short limit");
            return null;
        }
        byte[] bArrA = (!this.b || bArr.length == 0) ? (byte[]) bArr.clone() : this.a.a((byte[]) bArr.clone());
        if (bArrA != null && bArrA.length <= 32767) {
            return new CarLifeFrame.c(aVar, new CarLifeFrame.b(bArrA.length, 0L, 0, i), bArrA);
        }
        SyntheticRefs.f("ciphertext body exceeds signed short limit");
        return null;
    }
}
