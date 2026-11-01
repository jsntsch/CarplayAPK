package io.carplay.app.secured;

import io.carplay.app.prototype.MainActivity;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class o3 implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ MainActivity b;

    public /* synthetic */ o3(int i) {
        this.a = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.A1();
                break;
            case p1.c /* 1 */:
                mainActivity.U1();
                break;
            case 2:
                mainActivity.W1();
                break;
            case p1.d /* 3 */:
                mainActivity.n1();
                break;
            case 4:
                mainActivity.o1();
                break;
            case 5:
                mainActivity.p1();
                break;
            case 6:
                mainActivity.Z1();
                break;
            case 7:
                mainActivity.n2();
                break;
            case 8:
                mainActivity.m2();
                break;
            case 9:
                mainActivity.Q0();
                break;
            case 10:
                mainActivity.s2();
                break;
            case 11:
                mainActivity.a1();
                break;
            case 12:
                mainActivity.d1();
                break;
            case 13:
                mainActivity.k2();
                break;
            case 14:
                mainActivity.e1();
                break;
            case 15:
                mainActivity.f1();
                break;
            case Iap2Csm.e /* 16 */:
                mainActivity.g1();
                break;
            case 17:
                mainActivity.j1();
                break;
            case 18:
                mainActivity.k1();
                break;
            case 19:
                mainActivity.l1();
                break;
            case 20:
                mainActivity.j2();
                break;
            case 21:
                mainActivity.d2();
                break;
            case 22:
                mainActivity.f2();
                break;
            default:
                mainActivity.x1();
                break;
        }
    }
}
