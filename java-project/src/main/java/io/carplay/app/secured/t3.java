package io.carplay.app.secured;

import android.content.DialogInterface;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class t3 implements DialogInterface.OnDismissListener {
    public final /* synthetic */ int a;
    public /* synthetic */ MainActivity b;

    public /* synthetic */ t3(int i) {
        this.a = i;
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public final void onDismiss(DialogInterface dialogInterface) {
        int i = this.a;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.K1(dialogInterface);
                break;
            case AudioSourceCapability.c /* 1 */:
                mainActivity.Q1(dialogInterface);
                break;
            case 2:
                mainActivity.P1(dialogInterface);
                break;
            default:
                mainActivity.M1(dialogInterface);
                break;
        }
    }
}
