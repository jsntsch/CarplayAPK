package io.carplay.app.secured;

import android.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import io.carplay.app.prototype.HelpActivity;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class v2 implements View.OnClickListener {
    public final /* synthetic */ int a;
    public /* synthetic */ String b;
    public /* synthetic */ KeyEvent.Callback c;
    public /* synthetic */ KeyEvent.Callback d;

    public /* synthetic */ v2(int i) {
        this.a = i;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        switch (this.a) {
            case 0:
                HelpActivity.f((TextView) this.c, (TextView) this.d, this.b, view);
                break;
            default:
                ((MainActivity) this.c).D1((AlertDialog) this.d, this.b, view);
                break;
        }
    }
}
