package io.carplay.app.prototype;

import android.bluetooth.BluetoothProfile;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class b0 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;
    public /* synthetic */ Object c;
    public /* synthetic */ Object d;

    public /* synthetic */ b0(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                ((BluetoothRfcommProfiles) this.b).q((BluetoothRfcommProfiles.a) this.c, (BluetoothProfile) this.d);
                break;
            default:
                ((PairingSession.a) this.b).p((String) this.c, (byte[]) this.d);
                break;
        }
    }
}
