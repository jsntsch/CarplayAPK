package io.carplay.app.prototype;

import android.app.AlertDialog;
import android.view.View;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class f0 implements View.OnClickListener {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;

    public /* synthetic */ f0(int i) {
        this.a = i;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        int i = this.a;
        Object obj = this.b;
        switch (i) {
            case 0:
                ((AlertDialog) obj).dismiss();
                break;
            default:
                ((Runnable) obj).run();
                break;
        }
    }
}
