package io.carplay.app.prototype;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Network;
import android.widget.Button;
import android.widget.TextView;
import java.util.concurrent.atomic.AtomicBoolean;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class i0 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ int b;
    public /* synthetic */ Object c;
    public /* synthetic */ Object d;
    public /* synthetic */ Object e;
    public /* synthetic */ Object f;
    public /* synthetic */ Object g;

    public /* synthetic */ i0(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                DonationDialog.j((Activity) this.c, this.b, (AtomicBoolean) this.d, (AlertDialog) this.e, (Button) this.f, (TextView) this.g);
                break;
            default:
                ((PairingSession.f) this.c).e((Network) this.d, (String) this.e, (String) this.f, this.b, (String) this.g);
                break;
        }
    }
}
