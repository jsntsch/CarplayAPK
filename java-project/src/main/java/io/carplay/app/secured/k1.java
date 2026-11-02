package io.carplay.app.secured;

import android.net.Network;
import io.carplay.app.prototype.WirelessCarPlayService;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.List;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class k1 implements VideoCipher.b.c, w4.b, AirPlaySession.j {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;

    public /* synthetic */ k1(int i) {
        this.a = i;
    }

    @Override // io.carplay.app.secured.AirPlaySession.j
    public void a(DatagramSocket datagramSocket) throws IOException {
        ((Network) this.b).bindSocket(datagramSocket);
    }

    @Override // io.carplay.app.secured.w4.b
    public void b(h hVar) {
        int i = this.a;
        Object obj = this.b;
        switch (i) {
            case p1.c /* 1 */:
                w4.f((w4.a) obj, hVar);
                break;
            default:
                ((WirelessCarPlayService) obj).R2(hVar);
                break;
        }
    }

    @Override // io.carplay.app.secured.VideoCipher.b.c
    public void c(List list, long j, long j2, long j3) {
        VideoCipher.b.g((VideoCipher.b.a) this.b, list, j, j2, j3);
    }
}
