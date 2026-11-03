package io.carplay.app.secured;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import io.carplay.app.prototype.MainActivity;
import io.carplay.app.prototype.PairingSession;
import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.function.Predicate;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class a4 implements Predicate {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;

    public /* synthetic */ a4(int i) {
        this.a = i;
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        int i = this.a;
        Object obj2 = this.b;
        switch (i) {
            case 0:
                return MainActivity.r1((PairingSession.l) obj2, (PairingSession.l) obj);
            case p1.c /* 1 */:
                return h5.n((BluetoothSocket) obj2, (BluetoothSocket) obj);
            case 2:
                return WirelessCarPlayService.Y1((x.c) obj2, (x.c) obj);
            default:
                return ((WirelessCarPlayService) obj2).f2((BluetoothDevice) obj);
        }
    }
}
