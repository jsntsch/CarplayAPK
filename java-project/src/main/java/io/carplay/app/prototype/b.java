package io.carplay.app.prototype;

import android.graphics.SurfaceTexture;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class b implements SurfaceTexture.OnFrameAvailableListener {
    public /* synthetic */ AvcEncoder a;

    @Override // android.graphics.SurfaceTexture.OnFrameAvailableListener
    public final void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.a.C(surfaceTexture);
    }
}
