package io.carplay.app.prototype;

import io.carplay.app.secured.VideoCipher;
import java.util.List;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class s implements UpdateChecker.a, VideoCipher.b.c {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;

    public /* synthetic */ s(int i) {
        this.a = i;
    }

    @Override // io.carplay.app.prototype.UpdateChecker.a
    public void a(boolean z) {
        int i = this.a;
        MainActivity mainActivity = (MainActivity) this.b;
        switch (i) {
            case 0:
                mainActivity.U0(UpdateChecker);
                break;
            default:
                mainActivity.h1(UpdateChecker);
                break;
        }
    }

    @Override // io.carplay.app.secured.VideoCipher.b.c
    public void c(List list, long j, long j2, long j3) {
        ((AvcEncoder) this.b).x(list, j, j2, j3);
    }
}
