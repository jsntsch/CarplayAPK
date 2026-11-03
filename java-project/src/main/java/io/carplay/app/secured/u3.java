package io.carplay.app.secured;

import android.content.DialogInterface;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class u3 implements DialogInterface.OnClickListener {
    public final /* synthetic */ int a;
    public /* synthetic */ MainActivity b;

    public /* synthetic */ u3(int i) {
        this.a = i;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        int i2 = this.a;
        MainActivity mainActivity = this.b;
        switch (i2) {
            case 0:
                mainActivity.s1(dialogInterface, i);
                break;
            case AudioSourceCapability.c /* 1 */:
                mainActivity.t1(dialogInterface, i);
                break;
            case 2:
                mainActivity.v1(dialogInterface, i);
                break;
            case AudioSourceCapability.d /* 3 */:
                mainActivity.W0(dialogInterface, i);
                break;
            case 4:
                mainActivity.X0(dialogInterface, i);
                break;
            default:
                mainActivity.L1(dialogInterface, i);
                break;
        }
    }
}
