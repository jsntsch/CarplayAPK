package io.carplay.app.secured;

import android.view.View;
import io.carplay.app.prototype.HelpActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class u2 implements View.OnClickListener {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;

    public /* synthetic */ u2(int i) {
        this.a = i;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        int i = this.a;
        Object obj = this.b;
        switch (i) {
            case 0:
                ((HelpActivity) obj).d(view);
                break;
            case p1.c /* 1 */:
                ((HelpActivity) obj).e(view);
                break;
            default:
                ((Runnable) obj).run();
                break;
        }
    }
}
