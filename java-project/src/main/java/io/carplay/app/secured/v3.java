package io.carplay.app.secured;

import android.app.AlertDialog;
import android.content.DialogInterface;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class v3 implements DialogInterface.OnShowListener {
    public /* synthetic */ MainActivity a;
    public /* synthetic */ AlertDialog b;

    @Override // android.content.DialogInterface.OnShowListener
    public final void onShow(DialogInterface dialogInterface) {
        this.a.u1(this.b, dialogInterface);
    }
}
