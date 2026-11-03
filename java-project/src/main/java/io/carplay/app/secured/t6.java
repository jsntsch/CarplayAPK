package io.carplay.app.secured;

import android.bluetooth.BluetoothSocket;
import io.carplay.app.prototype.PairingSession;
import java.util.function.Consumer;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class t6 implements Consumer {
    public final /* synthetic */ int a;
    public /* synthetic */ PairingSession b;

    public /* synthetic */ t6(int i) {
        this.a = i;
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        int i = this.a;
        PairingSession pairingSession = this.b;
        switch (i) {
            case 0:
                pairingSession.S1((String) obj);
                break;
            case AudioSourceCapability.c /* 1 */:
                pairingSession.U0((BluetoothSocket) obj);
                break;
            case 2:
                pairingSession.S1((String) obj);
                break;
            case AudioSourceCapability.d /* 3 */:
                pairingSession.f1((BluetoothSocket) obj);
                break;
            default:
                pairingSession.j1((BluetoothSocket) obj);
                break;
        }
    }
}
