package io.carplay.app.secured;

import android.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class n3 implements View.OnClickListener {
    public final /* synthetic */ int a;
    public /* synthetic */ MainActivity b;
    public /* synthetic */ String c;
    public /* synthetic */ AlertDialog d;
    public /* synthetic */ EditText e;

    public /* synthetic */ n3(int i) {
        this.a = i;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        switch (this.a) {
            case 0:
                this.b.C1(this.e, this.c, this.d, view);
                break;
            default:
                this.b.E1(this.c, this.d, this.e, view);
                break;
        }
    }
}
