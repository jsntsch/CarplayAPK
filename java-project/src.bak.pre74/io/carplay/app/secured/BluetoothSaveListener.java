package io.carplay.app.secured;

import android.content.DialogInterface;
import android.widget.EditText;
import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
/**
 * BluetoothSaveListener
 *
 * <p>原混淆名：{@code io.carplay.app.secured.btsave}（jadx 产物 btsave.java）
 *
 * <p>命名依据：17 行，implements DialogInterface.OnClickListener；保存蓝牙名
 */
public final class BluetoothSaveListener implements DialogInterface.OnClickListener {
    public /* synthetic */ MainActivity a;
    public /* synthetic */ EditText b;

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        MainActivity.v170SaveName(this.a, this.b);
    }
}
