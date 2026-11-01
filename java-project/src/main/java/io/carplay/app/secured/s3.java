package io.carplay.app.secured;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class s3 implements DialogInterface.OnClickListener {
    public final /* synthetic */ int a;
    public /* synthetic */ MainActivity b;
    public /* synthetic */ Object c;

    public /* synthetic */ s3(int i) {
        this.a = i;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        switch (this.a) {
            case 0:
                this.b.J1((SharedPreferences) this.c, dialogInterface, i);
                break;
            default:
                this.b.z1((String) this.c, dialogInterface, i);
                break;
        }
    }
}
