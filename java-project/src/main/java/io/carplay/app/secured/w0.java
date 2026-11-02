package io.carplay.app.secured;

import io.carplay.app.prototype.AirplayServiceProbe;
import java.net.InetAddress;
import java.util.function.Predicate;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class w0 implements Predicate {
    public final /* synthetic */ int a;

    public /* synthetic */ w0(int i) {
        this.a = i;
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        switch (this.a) {
            case 0:
                return x0.h((x0.c) obj);
            case p1.c /* 1 */:
                return y0.g((y0.c) obj);
            case 2:
                return MfiCertificateVerifier.L((MfiCertificateVerifier.d) obj);
            case p1.d /* 3 */:
                return MfiCertificateVerifier.J((MfiCertificateVerifier.d) obj);
            case 4:
                return MfiCertificateVerifier.N((MfiCertificateVerifier.d) obj);
            case 5:
                return MfiCertificateVerifier.O((MfiCertificateVerifier.d) obj);
            case 6:
                return s.p((s.d) obj);
            case 7:
                return ((x.e) obj).k();
            case 8:
                return ((x.e) obj).j();
            case 9:
                return x.x((x.c) obj);
            case 10:
                return AirPlaySession.Q0((byte[]) obj);
            case 11:
                return AirPlaySession.f1((byte[]) obj);
            case 12:
                return "url".equals((String) obj);
            case 13:
                return AirPlaySession.W0(obj);
            case 14:
                return AirplayServiceProbe.w((InetAddress) obj);
            case 15:
                return x0.e(((Integer) obj).intValue());
            case Iap2Csm.e /* 16 */:
                return y0.h(((Integer) obj).intValue());
            case 17:
                return SessionStateMachine.C(obj);
            case 18:
                return p1.j((x.c) obj);
            case 19:
                return v1.m((v1.b) obj);
            default:
                return w4.h((byte[]) obj);
        }
    }
}
