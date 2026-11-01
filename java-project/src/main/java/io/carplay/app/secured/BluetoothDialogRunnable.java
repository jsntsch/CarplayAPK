package io.carplay.app.secured;

import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
/**
 * BluetoothDialogRunnable
 *
 * <p>原混淆名：{@code io.carplay.app.secured.btdlg}（jadx 产物 btdlg.java）
 *
 * <p>命名依据：14 行，implements Runnable；蓝牙对话框延迟任务
 */
public final class BluetoothDialogRunnable implements Runnable {
    public /* synthetic */ MainActivity a;

    @Override // java.lang.Runnable
    public final void run() {
        MainActivity.v170NameDialog(this.a);
    }
}
