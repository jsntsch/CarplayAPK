package io.carplay.app.secured;

import android.bluetooth.BluetoothSocket;
import java.util.function.Consumer;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class t6 implements Consumer {
    public final /* synthetic */ int a;
    public /* synthetic */ io.carplay.app.prototype.PairingSession b;

    public /* synthetic */ t6(int i) {
        this.a = i;
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        int i = this.a;
        io.carplay.app.prototype.PairingSession r0Var = this.b;
        switch (i) {
            case 0:
                r0Var.S1((String) obj);
                break;
            case p1.c /* 1 */:
                r0Var.U0((BluetoothSocket) obj);
                break;
            case 2:
                r0Var.S1((String) obj);
                break;
            case p1.d /* 3 */:
                r0Var.f1((BluetoothSocket) obj);
                break;
            default:
                r0Var.j1((BluetoothSocket) obj);
                break;
        }
    }
}
