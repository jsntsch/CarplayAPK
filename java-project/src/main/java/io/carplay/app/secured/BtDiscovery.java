package io.carplay.app.secured;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

/* JADX INFO: loaded from: classes.dex */
/**
 * BtDiscovery
 *
 * <p>原混淆名：{@code io.carplay.app.secured.h5}（jadx 产物 h5.java）
 *
 * <p>命名依据：00000000-deca-fade-deca-deafdecacafe / discovery_bluetooth_unavailable / eir_requested=true
 */
public final class BtDiscovery implements AutoCloseable {
    static final UUID s = UUID.fromString("00000000-deca-fade-deca-deafdecacafe");
    static final UUID t = UUID.fromString("02030302-1d19-415f-86f2-22a2106a0a77");
    static final UUID u = UUID.fromString("2d8d2466-e14d-451c-88bc-7301abea291a");
    public static final int v = 120;
    private BluetoothServerSocket e;
    private final Consumer<String> g;
    private Runnable h;
    private Consumer<BluetoothSocket> i;
    private String j;
    private boolean k;
    private boolean l;
    private boolean m;
    private boolean n;
    private int o;
    private long q;
    private final Runnable r;
    private final Object a = new Object();
    private final Handler b = new Handler(Looper.getMainLooper());
    private final ExecutorService c = Executors.newFixedThreadPool(3, new n0(4));
    private final List<BluetoothServerSocket> d = new ArrayList();
    private final List<BluetoothSocket> f = new ArrayList();
    private int p = 3;

    public BtDiscovery(Consumer<String> consumer, Runnable runnable) {
        d1 d1Var = new d1(5);
        d1Var.b = this;
        VarHandle.storeStoreFence();
        this.r = d1Var;
        this.g = consumer;
        this.h = runnable;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code duplicated, block: B:52:0x007f  */
    /* JADX INFO: renamed from: g, reason: merged with bridge method [inline-methods] */
    public void p(BluetoothServerSocket bluetoothServerSocket) {
        boolean z;
        int iMax;
        BluetoothSocket bluetoothSocketAccept = null;
        Runnable runnable;
        boolean z2 = false;
        while (true) {
            synchronized (this.a) {
                try {
                    if (this.k || !this.d.contains(bluetoothServerSocket)) {
                        break;
                    } else {
                        z = this.n && bluetoothServerSocket == this.e;
                        iMax = this.m ? 90000 : (int) Math.max(1L, this.q - SystemClock.elapsedRealtime());
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (z) {
                z2 = true;
            }
            try {
                if (z) {
                    bluetoothSocketAccept = bluetoothServerSocket.accept();
                } else {
                    bluetoothSocketAccept = bluetoothServerSocket.accept(iMax);
                }
            } catch (IOException | RuntimeException unused) {
                synchronized (this.a) {
                    try {
                        if (this.k || !this.d.contains(bluetoothServerSocket) || !this.n || bluetoothServerSocket != this.e || z2) {
                            if (this.k || !this.d.remove(bluetoothServerSocket)) {
                                runnable = null;
                            } else {
                                int i = this.o - 1;
                                this.o = i;
                                if (i == 0) {
                                    runnable = this.h;
                                } else {
                                    runnable = null;
                                }
                            }
                            if (runnable != null) {
                                close();
                                runnable.run();
                                return;
                            }
                            return;
                        }
                        z2 = true;
                    } catch (Throwable th2) {
                        throw th2;
                    }
                }
                continue;
            }
            k(bluetoothSocketAccept);
        }
    }

    private void k(BluetoothSocket bluetoothSocket) {
        Consumer<BluetoothSocket> consumer;
        boolean z;
        synchronized (this.a) {
            try {
                consumer = null;
                z = false;
                if (!this.k && !this.n) {
                    if (this.m) {
                        if (this.j.equalsIgnoreCase(bluetoothSocket.getRemoteDevice().getAddress())) {
                            consumer = this.i;
                        }
                    } else if (this.f.size() < 3) {
                        Stream<BluetoothSocket> stream = this.f.stream();
                        a4 a4Var = new a4(1);
                        a4Var.b = bluetoothSocket;
                        VarHandle.storeStoreFence();
                        if (stream.noneMatch(a4Var)) {
                            this.f.add(bluetoothSocket);
                            z = true;
                        }
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        if (consumer != null) {
            try {
                consumer.accept(bluetoothSocket);
            } catch (RuntimeException unused) {
                q(bluetoothSocket);
            }
        } else {
            if (z) {
                return;
            }
            q(bluetoothSocket);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void l() {
        synchronized (this.a) {
            if (!this.k && !this.m) {
                Runnable runnable = this.h;
                close();
                runnable.run();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean n(BluetoothSocket bluetoothSocket, BluetoothSocket bluetoothSocket2) {
        return bluetoothSocket2.getRemoteDevice().getAddress().equalsIgnoreCase(bluetoothSocket.getRemoteDevice().getAddress());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Thread o(Runnable runnable) {
        Thread thread = new Thread(runnable, "carplay-discovery");
        thread.setDaemon(true);
        return thread;
    }

    private static void q(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException unused) {
        }
    }

    private void u(Context context, int i) throws IOException {
        synchronized (this.a) {
            if (this.l || this.k) {
                throw new IOException("DISCOVERY_ALREADY_USED");
            }
            this.l = true;
        }
        try {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(BluetoothManager.class);
            BluetoothAdapter adapter = bluetoothManager == null ? null : bluetoothManager.getAdapter();
            if (adapter == null || !adapter.isEnabled()) {
                throw new IOException("DISCOVERY_BLUETOOTH_UNAVAILABLE");
            }
            UUID[] uuidArr = i == 0 ? new UUID[]{s, t, u} : i == 1 ? new UUID[]{t, u} : new UUID[]{u};
            String[] strArr = i == 0 ? new String[]{"CarPlus iAP2 Phone", "Wireless iAP v2", "CarPlus Wireless Discovery"} : i == 1 ? new String[]{"Wireless iAP v2", "CarPlus Wireless Discovery"} : new String[]{"CarPlus Wireless Discovery"};
            this.p = uuidArr.length;
            for (int i2 = 0; i2 < uuidArr.length; i2++) {
                BluetoothServerSocket bluetoothServerSocketListenUsingRfcommWithServiceRecord = adapter.listenUsingRfcommWithServiceRecord(strArr[i2], uuidArr[i2]);
                synchronized (this.a) {
                    try {
                        if (this.k) {
                            q(bluetoothServerSocketListenUsingRfcommWithServiceRecord);
                            throw new IOException("DISCOVERY_CANCELLED");
                        }
                        this.d.add(bluetoothServerSocketListenUsingRfcommWithServiceRecord);
                        if (uuidArr[i2].equals(u)) {
                            this.e = bluetoothServerSocketListenUsingRfcommWithServiceRecord;
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            }
            synchronized (this.a) {
                try {
                    if (this.k) {
                        throw new IOException("DISCOVERY_CANCELLED");
                    }
                    this.o = this.d.size();
                    this.q = SystemClock.elapsedRealtime() + 120000;
                    this.b.postDelayed(this.r, 120000L);
                    for (BluetoothServerSocket bluetoothServerSocket : this.d) {
                        ExecutorService executorService = this.c;
                        p0 p0Var = new p0(4);
                        p0Var.b = this;
                        p0Var.c = bluetoothServerSocket;
                        VarHandle.storeStoreFence();
                        executorService.execute(p0Var);
                    }
                } catch (Throwable th2) {
                    throw th2;
                }
            }
            this.g.accept("PHONE_DISCOVERY_RECORDS_READY secure_records=" + uuidArr.length + " eir_requested=true radio_confirmed=false");
        } catch (IOException | RuntimeException e) {
            close();
            throw e;
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        ArrayList arrayList = new ArrayList();
        synchronized (this.a) {
            try {
                if (this.k) {
                    return;
                }
                this.k = true;
                this.b.removeCallbacks(this.r);
                arrayList.addAll(this.d);
                arrayList.addAll(this.f);
                this.d.clear();
                this.f.clear();
                int i = 0;
                this.o = 0;
                int size = arrayList.size();
                while (i < size) {
                    Object obj = arrayList.get(i);
                    i++;
                    q((Closeable) obj);
                }
                this.c.shutdown();
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void i(String str, Consumer<BluetoothSocket> consumer, Runnable runnable) throws IOException {
        ArrayList arrayList;
        int i;
        BluetoothSocket bluetoothSocket;
        synchronized (this.a) {
            try {
                if (this.k || !this.l || this.o == 0 || this.m) {
                    throw new IOException("DISCOVERY_NOT_AVAILABLE");
                }
                this.m = true;
                this.j = str;
                this.i = consumer;
                this.h = runnable;
                this.b.removeCallbacks(this.r);
                arrayList = new ArrayList(this.f);
                this.f.clear();
                int size = arrayList.size();
                i = 0;
                bluetoothSocket = null;
                int i2 = 0;
                while (i2 < size) {
                    Object obj = arrayList.get(i2);
                    i2++;
                    BluetoothSocket bluetoothSocket2 = (BluetoothSocket) obj;
                    if (bluetoothSocket == null && str.equalsIgnoreCase(bluetoothSocket2.getRemoteDevice().getAddress())) {
                        bluetoothSocket = bluetoothSocket2;
                    }
                }
                arrayList.remove(bluetoothSocket);
            } catch (Throwable th) {
                throw th;
            }
        }
        int size2 = arrayList.size();
        while (i < size2) {
            Object obj2 = arrayList.get(i);
            i++;
            q((BluetoothSocket) obj2);
        }
        this.g.accept("PHONE_DISCOVERY_SELECTED_HANDOFF records_retained=true authentication_started=false");
        if (bluetoothSocket != null) {
            k(bluetoothSocket);
        }
    }

    public boolean m() {
        boolean z;
        synchronized (this.a) {
            try {
                z = !this.k && this.l && this.o == this.p && !this.m;
            } catch (Throwable th) {
                throw th;
            }
        }
        return z;
    }

    public boolean r() {
        ArrayList arrayList = new ArrayList();
        synchronized (this.a) {
            try {
                int i = 0;
                if (this.k) {
                    return false;
                }
                boolean z = true;
                this.n = true;
                this.b.removeCallbacks(this.r);
                ArrayList arrayList2 = new ArrayList(this.d);
                int size = arrayList2.size();
                int i2 = 0;
                while (i2 < size) {
                    Object obj = arrayList2.get(i2);
                    i2++;
                    BluetoothServerSocket bluetoothServerSocket = (BluetoothServerSocket) obj;
                    if (bluetoothServerSocket != this.e) {
                        this.d.remove(bluetoothServerSocket);
                        this.o--;
                        arrayList.add(bluetoothServerSocket);
                    }
                }
                arrayList.addAll(this.f);
                this.f.clear();
                BluetoothServerSocket bluetoothServerSocket2 = this.e;
                if (bluetoothServerSocket2 == null || !this.d.contains(bluetoothServerSocket2)) {
                    z = false;
                }
                int size2 = arrayList.size();
                while (i < size2) {
                    Object obj2 = arrayList.get(i);
                    i++;
                    q((Closeable) obj2);
                }
                this.g.accept("PHONE_DISCOVERY_IDENTITY_RETAINED registered=" + z + " protocol_listeners_retired=true radio_confirmed=false");
                return z;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void s(Context context) throws IOException {
        u(context, 0);
    }

    public void t(Context context) throws IOException {
        u(context, 2);
    }

    public void v(Context context) throws IOException {
        u(context, 1);
    }
}
