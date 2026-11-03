package io.carplay.app.secured;

import android.view.View;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class x3 implements View.OnClickListener {
    public final /* synthetic */ int a;
    public /* synthetic */ MainActivity b;

    public /* synthetic */ x3(int i) {
        this.a = i;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        int i = this.a;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.c1(view);
                break;
            case AudioSourceCapability.c /* 1 */:
                mainActivity.b1(view);
                break;
            case 2:
                mainActivity.N1(view);
                break;
            default:
                mainActivity.O1(view);
                break;
        }
    }
}
