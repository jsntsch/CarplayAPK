package io.carplay.app.secured;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class q3 implements DialogInterface.OnDismissListener {
    public /* synthetic */ MainActivity a;
    public /* synthetic */ EditText b;
    public /* synthetic */ AlertDialog c;

    @Override // android.content.DialogInterface.OnDismissListener
    public final void onDismiss(DialogInterface dialogInterface) {
        this.a.F1(this.b, this.c, dialogInterface);
    }
}
