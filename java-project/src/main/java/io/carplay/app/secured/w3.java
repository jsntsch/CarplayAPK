package io.carplay.app.secured;

import android.widget.CompoundButton;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class w3 implements CompoundButton.OnCheckedChangeListener {
    public /* synthetic */ MainActivity a;

    @Override // android.widget.CompoundButton.OnCheckedChangeListener
    public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        this.a.q1(compoundButton, z);
    }
}
