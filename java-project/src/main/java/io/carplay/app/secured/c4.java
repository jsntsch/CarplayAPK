package io.carplay.app.secured;

import android.content.DialogInterface;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class c4 implements DialogInterface.OnDismissListener {
    public /* synthetic */ MainActivity a;
    public /* synthetic */ boolean b;

    @Override // android.content.DialogInterface.OnDismissListener
    public final void onDismiss(DialogInterface dialogInterface) {
        this.a.y1(this.b, dialogInterface);
    }
}
