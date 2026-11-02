package io.carplay.app.prototype;

import android.app.AlertDialog;
import android.view.View;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class o0 implements View.OnClickListener {
    public final /* synthetic */ int a;
    public /* synthetic */ Runnable b;
    public /* synthetic */ AlertDialog c;

    public /* synthetic */ o0(int i) {
        this.a = i;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        switch (this.a) {
            case 0:
                UiFactory.o(this.b, this.c, view);
                break;
            default:
                UiFactory.p(this.b, this.c, view);
                break;
        }
    }
}
