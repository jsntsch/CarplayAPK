package io.carplay.app.prototype;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/* JADX INFO: loaded from: classes.dex */
/**
 * BluetoothRfcommProfiles
 *
 * <p>原混淆名：{@code io.carplay.app.prototype.c0}（jadx 产物 c0.java）
 *
 * <p>命名依据：RFCOMM_SELECTED_PROFILE_SNAPSHOT；对应 hypercar bluetooth/BtProfiles.kt
 */
final class BluetoothRfcommProfiles implements AutoCloseable {
    private static final long m = 1500;
    private final Context b;
    private final BluetoothAdapter c;
    private final BluetoothDevice d;
    private final Consumer<String> e;
    private final Executor f;
    private boolean i;
    private boolean j;
    private final Runnable k;
    private final Runnable l;
    private final Object a = new Object();
    private final Handler g = new Handler(Looper.getMainLooper());
    private final a[] h = {new a(1, "hfp"), new a(2, "a2dp")};

    public final class a implements BluetoothProfile.ServiceListener {
        final int a;
        final String b;
        String c = "UNKNOWN";
        boolean d;
        boolean e;
        BluetoothProfile f;
        BluetoothProfile g;

        public a(int i, String str) {
            this.a = i;
            this.b = str;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            BluetoothRfcommProfiles.this.r(this, i, bluetoothProfile);
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int i) {
            synchronized (BluetoothRfcommProfiles.this.a) {
                if (!BluetoothRfcommProfiles.this.j && !this.d && i == this.a) {
                    this.d = true;
                    this.c = "UNKNOWN";
                    BluetoothRfcommProfiles.this.o(false);
                }
            }
        }
    }

    public BluetoothRfcommProfiles(Context context, BluetoothAdapter bluetoothAdapter, BluetoothDevice bluetoothDevice, Executor executor, Consumer<String> consumer) {
        a0 a0Var = new a0(0);
        a0Var.b = this;
        VarHandle.storeStoreFence();
        this.k = a0Var;
        a0 a0Var2 = new a0(1);
        a0Var2.b = this;
        VarHandle.storeStoreFence();
        this.l = a0Var2;
        this.b = context.getApplicationContext();
        this.c = bluetoothAdapter;
        this.d = bluetoothDevice;
        this.f = executor;
        this.e = consumer;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void l() {
        boolean profileProxy;
        synchronized (this.a) {
            try {
                if (this.j) {
                    return;
                }
                this.g.postDelayed(this.l, m);
                for (a aVar : this.h) {
                    synchronized (this.a) {
                        try {
                            if (this.j) {
                                return;
                            }
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                    try {
                        profileProxy = this.c.getProfileProxy(this.b, aVar, aVar.a);
                    } catch (RuntimeException unused) {
                        profileProxy = false;
                    }
                    if (!profileProxy) {
                        synchronized (this.a) {
                            try {
                                if (!this.j && !aVar.e) {
                                    aVar.d = true;
                                    aVar.c = "UNKNOWN";
                                }
                            } catch (Throwable th2) {
                                throw th2;
                            }
                        }
                    }
                }
                o(false);
            } catch (Throwable th3) {
                throw th3;
            }
        }
    }

    private void m() {
        int i;
        this.g.removeCallbacks(this.k);
        this.g.removeCallbacks(this.l);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        synchronized (this.a) {
            try {
                for (a aVar : this.h) {
                    BluetoothProfile bluetoothProfile = aVar.f;
                    if (bluetoothProfile != null) {
                        arrayList.add(bluetoothProfile);
                        arrayList2.add(aVar);
                        aVar.f = null;
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        for (i = 0; i < arrayList.size(); i++) {
            n((a) arrayList2.get(i), (BluetoothProfile) arrayList.get(i));
        }
    }

    private void n(a aVar, BluetoothProfile bluetoothProfile) {
        synchronized (this.a) {
            try {
                if (aVar.g == bluetoothProfile) {
                    return;
                }
                aVar.g = bluetoothProfile;
                try {
                    this.c.closeProfileProxy(aVar.a, bluetoothProfile);
                } catch (RuntimeException unused) {
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void o(boolean z) {
        synchronized (this.a) {
            try {
                if (this.j) {
                    return;
                }
                if (!z) {
                    for (a aVar : this.h) {
                        if (!aVar.d) {
                            return;
                        }
                    }
                }
                a[] aVarArr = this.h;
                a aVar2 = aVarArr[0];
                String str = aVar2.b;
                String str2 = aVar2.c;
                a aVar3 = aVarArr[1];
                try {
                    this.e.accept("RFCOMM_SELECTED_PROFILE_SNAPSHOT " + str + "=" + str2 + " " + aVar3.b + "=" + aVar3.c + " bounded_ms=1500 read_only=true cause_unconfirmed=true values_logged=false");
                } catch (RuntimeException unused) {
                } finally {
                    this.j = true;
                }
                m();
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void p() {
        o(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void r(a aVar, int i, BluetoothProfile bluetoothProfile) {
        Object obj = this.a;
        int i2 = 0;
        if (bluetoothProfile == null) {
            synchronized (obj) {
                try {
                    if (!this.j && !aVar.d) {
                        aVar.d = true;
                        aVar.c = "UNKNOWN";
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            o(false);
            return;
        }
        synchronized (obj) {
            try {
                if (aVar.f != bluetoothProfile && aVar.g != bluetoothProfile) {
                    boolean z = (this.j || aVar.d || aVar.e || i != aVar.a) ? false : true;
                    if (UpdateChecker) {
                        aVar.e = true;
                        aVar.f = bluetoothProfile;
                    }
                    if (!z) {
                        n(aVar, bluetoothProfile);
                        return;
                    }
                    try {
                        Executor executor = this.f;
                        b0 b0Var = new b0(i2);
                        b0Var.b = this;
                        b0Var.c = aVar;
                        b0Var.d = bluetoothProfile;
                        VarHandle.storeStoreFence();
                        executor.execute(b0Var);
                    } catch (RuntimeException unused) {
                        t(aVar, bluetoothProfile);
                        synchronized (this.a) {
                            try {
                                if (!this.j) {
                                    aVar.d = true;
                                    aVar.c = "UNKNOWN";
                                    aVar.e = false;
                                }
                                o(false);
                            } catch (Throwable th2) {
                                throw th2;
                            }
                        }
                    }
                }
            } catch (Throwable th3) {
                throw th3;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: s, reason: merged with bridge method [inline-methods] */
    public void q(a aVar, BluetoothProfile bluetoothProfile) {
        synchronized (this.a) {
            if (!this.j && !aVar.d && aVar.f == bluetoothProfile) {
                String str = "UNKNOWN";
                try {
                    int connectionState = bluetoothProfile.getConnectionState(this.d);
                    if (connectionState == 0) {
                        str = "DISCONNECTED";
                    } else if (connectionState == 1) {
                        str = "CONNECTING";
                    } else if (connectionState != 2) {
                        str = connectionState != 3 ? "UNKNOWN" : "DISCONNECTING";
                    } else {
                        str = "CONNECTED";
                    }
                } catch (RuntimeException unused) {
                } finally {
                    t(aVar, bluetoothProfile);
                }
                synchronized (this.a) {
                    try {
                        if (!this.j && !aVar.d) {
                            aVar.c = str;
                            aVar.d = true;
                            aVar.e = false;
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                o(false);
            }
        }
    }

    private void t(a aVar, BluetoothProfile bluetoothProfile) {
        boolean z;
        synchronized (this.a) {
            try {
                z = aVar.f == bluetoothProfile;
                if (UpdateChecker) {
                    aVar.f = null;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        if (UpdateChecker) {
            n(aVar, bluetoothProfile);
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        synchronized (this.a) {
            try {
                if (this.j) {
                    return;
                }
                this.j = true;
                m();
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void u() {
        synchronized (this.a) {
            if (!this.i && !this.j) {
                this.i = true;
                this.g.post(this.k);
            }
        }
    }
}
