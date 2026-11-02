package io.carplay.app.prototype;

import android.app.Activity;
import android.view.View;
import android.view.WindowInsets;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class l0 implements View.OnApplyWindowInsetsListener {
    public /* synthetic */ int a;
    public /* synthetic */ Activity b;

    @Override // android.view.View.OnApplyWindowInsetsListener
    public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
        return UiFactory.s(this.a, this.b, view, windowInsets);
    }
}
