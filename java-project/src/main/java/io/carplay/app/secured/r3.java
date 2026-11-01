package io.carplay.app.secured;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class r3 implements DialogInterface.OnClickListener {
    public /* synthetic */ MainActivity a;
    public /* synthetic */ SharedPreferences b;
    public /* synthetic */ String c;
    public /* synthetic */ String d;

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        this.a.I1(this.b, this.c, this.d, dialogInterface, i);
    }
}
