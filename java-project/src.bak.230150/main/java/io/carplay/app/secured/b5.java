package io.carplay.app.secured;

import android.app.Service;
import io.carplay.app.prototype.PairingPreparationService;
import io.carplay.app.prototype.WirelessCarPlayService;
import java.util.function.Consumer;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class b5 implements Consumer {
    public final /* synthetic */ int a;
    public /* synthetic */ Service b;

    public /* synthetic */ b5(int i) {
        this.a = i;
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        int i = this.a;
        Service service = this.b;
        switch (i) {
            case 0:
                ((PairingPreparationService) service).h((String) obj);
                break;
            default:
                ((WirelessCarPlayService) service).i1((String) obj);
                break;
        }
    }
}
