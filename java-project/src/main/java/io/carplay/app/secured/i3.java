package io.carplay.app.secured;

import android.view.View;
import io.carplay.app.prototype.LogActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class i3 implements View.OnClickListener {
    public final /* synthetic */ int a;
    public /* synthetic */ LogActivity b;

    public /* synthetic */ i3(int i) {
        this.a = i;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        int i = this.a;
        LogActivity logActivity = this.b;
        switch (i) {
            case 0:
                logActivity.l(view);
                break;
            case p1.c /* 1 */:
                logActivity.m(view);
                break;
            default:
                logActivity.n(view);
                break;
        }
    }
}
