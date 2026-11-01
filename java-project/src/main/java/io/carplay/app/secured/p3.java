package io.carplay.app.secured;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class p3 implements DialogInterface.OnShowListener {
    public /* synthetic */ MainActivity a;
    public /* synthetic */ AlertDialog b;
    public /* synthetic */ EditText c;
    public /* synthetic */ String d;

    @Override // android.content.DialogInterface.OnShowListener
    public final void onShow(DialogInterface dialogInterface) {
        this.a.B1(this.b, this.c, this.d, dialogInterface);
    }
}
