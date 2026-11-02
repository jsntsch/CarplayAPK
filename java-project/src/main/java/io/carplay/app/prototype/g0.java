package io.carplay.app.prototype;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.concurrent.atomic.AtomicBoolean;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class g0 implements View.OnClickListener {
    public /* synthetic */ AtomicBoolean a;
    public /* synthetic */ Button b;
    public /* synthetic */ TextView c;
    public /* synthetic */ Activity d;
    public /* synthetic */ int e;
    public /* synthetic */ AlertDialog f;

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        j0.i(this.a, this.b, this.c, this.d, this.e, this.f, view);
    }
}
