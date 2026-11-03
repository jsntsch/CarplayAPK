package io.carplay.app.secured;

import android.os.SystemClock;
import java.util.function.LongSupplier;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class c0 implements LongSupplier {
    public final /* synthetic */ int a;

    /* smali 中为 public synthetic constructor <init>(I)V，jadx 漏生成 */
    public c0(int i) {
        this.a = i;
    }

    @Override // java.util.function.LongSupplier
    public final long getAsLong() {
        switch (this.a) {
            case 0:
                return System.nanoTime();
            default:
                return SystemClock.elapsedRealtime();
        }
    }
}
