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
                return HidBackParser.h((HidBackParser.c) obj);
            case AudioSourceCapability.c /* 1 */:
                return HidConsumerParser.g((HidConsumerParser.c) obj);
            case 2:
                return MfiCertificateVerifier.L((MfiCertificateVerifier.d) obj);
            case AudioSourceCapability.d /* 3 */:
                return MfiCertificateVerifier.J((MfiCertificateVerifier.d) obj);
            case 4:
                return MfiCertificateVerifier.N((MfiCertificateVerifier.d) obj);
            case 5:
                return MfiCertificateVerifier.O((MfiCertificateVerifier.d) obj);
            case 6:
                return AudioCapabilities.p((AudioCapabilities.d) obj);
            case 7:
                return ((HidCapabilities.e) obj).k();
            case 8:
                return ((HidCapabilities.e) obj).j();
            case 9:
                return HidCapabilities.x((HidCapabilities.c) obj);
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
                return HidBackParser.e(((Integer) obj).intValue());
            case Iap2Csm.e /* 16 */:
                return HidConsumerParser.h(((Integer) obj).intValue());
            case 17:
                return SessionStateMachine.C(obj);
            case 18:
                return AudioSourceCapability.j((HidCapabilities.c) obj);
            case 19:
                return EncryptProofNeg.m((EncryptProofNeg.b) obj);
            default:
                return w4.h((byte[]) obj);
        }
    }
}
