package io.carplay.app.secured;

import android.os.SystemClock;
import java.util.function.LongSupplier;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class c0 implements LongSupplier {
    public final /* synthetic */ int a;

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
