package io.carplay.app.secured;

import android.widget.RadioGroup;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class z3 implements RadioGroup.OnCheckedChangeListener {
    public /* synthetic */ MainActivity a;

    @Override // android.widget.RadioGroup.OnCheckedChangeListener
    public final void onCheckedChanged(RadioGroup radioGroup, int i) {
        this.a.m1(radioGroup, i);
    }
}
