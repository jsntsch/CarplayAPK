package io.carplay.app.prototype;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class h0 implements DialogInterface.OnShowListener {
    public final /* synthetic */ int a;
    public /* synthetic */ AlertDialog b;
    public /* synthetic */ Activity c;

    public /* synthetic */ h0(int i) {
        this.a = i;
    }

    @Override // android.content.DialogInterface.OnShowListener
    public final void onShow(DialogInterface dialogInterface) {
        switch (this.a) {
            case 0:
                j0.l(this.b, this.c, dialogInterface);
                break;
            default:
                m0.q(this.b, this.c, dialogInterface);
                break;
        }
    }
}
