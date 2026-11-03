package io.carplay.app.secured;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.io.OutputStream;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/* JADX INFO: loaded from: classes.dex */
/**
 * Iap2WirelessSetup
 *
 * <p>原混淆名：{@code io.carplay.app.secured.e1}（jadx 产物 e1.java）
 *
 * <p>命名依据：IP iAP must not replay wireless setup —— Wi-Fi 上的 iAP2 通道
 */
final class Iap2WirelessSetup implements AutoCloseable {
    private final ArrayBlockingQueue<byte[]> a;
    private final Object b;
    private final AtomicBoolean c;
    private final c d;
    private final Consumer<String> e;
    private final MfiCertificateVerifier f;
    private final boolean g;
    private final BooleanSupplier h;
    private volatile b i;
    private Thread j;
    private int k;

    public class a implements MfiCertificateVerifier.c {
        final /* synthetic */ i5 a;
        final /* synthetic */ Supplier b;
        final /* synthetic */ Consumer c;
        final /* synthetic */ Iap2WirelessSetup d;

        public a(Iap2WirelessSetup iap2WirelessSetup, i5 i5Var, Supplier supplier, Consumer consumer) {
            this.a = i5Var;
            this.b = supplier;
            this.c = consumer;
            this.d = iap2WirelessSetup;
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public void b(Iap2Csm.a aVar) throws IOException {
            throw new IOException("IP iAP must not replay wireless setup");
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public i5 c() {
            return this.a;
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public Boolean e() {
            if (this.d.c.get()) {
                return null;
            }
            return (Boolean) this.b.get();
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public void g(String str, byte[] bArr) {
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public void h(String str) {
            if (this.d.c.get()) {
                return;
            }
            this.c.accept("NETWORK_" + str);
        }

        @Override // io.carplay.app.secured.MfiCertificateVerifier.c
        public void write(byte[] bArr) throws IOException {
            try {
                b bVar = this.d.i;
                if (bVar == null) {
                    throw new IOException("network iAP output unavailable");
                }
                bVar.a(bArr);
            } catch (IOException e) {
                throw e;
            } catch (Exception e2) {
                throw new IOException("network iAP write failed", e2);
            }
        }
    }

    public interface b extends AutoCloseable {
        void a(byte[] bArr) throws IOException;

        @Override // java.lang.AutoCloseable
        void close();
    }

    @FunctionalInterface
    public interface c {
        b b() throws IOException, InterruptedException;
    }

    public static final class d implements b {
        private final OutputStream a;
        private final Runnable b;
        private final byte[] c;
        private long d;
        private long e = 4294967296L;
        private boolean f;

        public d(byte[] bArr, long j, OutputStream outputStream, Runnable runnable) {
            this.c = VideoCipher.b(bArr, j);
            this.a = outputStream;
            this.b = runnable;
        }

        @Override // io.carplay.app.secured.Iap2WirelessSetup.b
        public synchronized void a(byte[] bArr) throws IOException {
            if (this.f || bArr == null || bArr.length == 0 || bArr.length > 4096 || this.d == -1) {
                throw new IOException("network iAP output bounds");
            }
            ByteBuffer byteBufferOrder = ByteBuffer.allocate(bArr.length + 32).order(ByteOrder.BIG_ENDIAN);
            byteBufferOrder.putInt(bArr.length + 32).put("sync".getBytes(StandardCharsets.US_ASCII));
            byteBufferOrder.position(16);
            ByteBuffer byteBufferPutInt = byteBufferOrder.putInt(1668246893);
            long j = this.e;
            this.e = j + 1;
            byteBufferPutInt.putLong(j).putInt(0).put(bArr);
            byte[] bArrArray = byteBufferOrder.array();
            byte[] bArr2 = {(byte) bArrArray.length, (byte) (bArrArray.length >>> 8)};
            byte[] bArr3 = this.c;
            long j2 = this.d;
            this.d = 1 + j2;
            byte[] bArrS;
            try {
                bArrS = DerCodec.s(bArr3, DerCodec.m(j2), bArrArray, bArr2);
            } catch (GeneralSecurityException e) {
                throw new IOException("network iAP output encryption failed", e);
            }
            this.a.write(bArr2);
            this.a.write(bArrS);
            this.a.flush();
        }

        @Override // io.carplay.app.secured.Iap2WirelessSetup.b, java.lang.AutoCloseable
        public void close() {
            this.b.run();
            synchronized (this) {
                this.f = true;
                Arrays.fill(this.c, (byte) 0);
            }
        }
    }

    private Iap2WirelessSetup(String str, c cVar, Consumer<String> consumer, i5 i5Var, boolean z, Supplier<Boolean> supplier, BooleanSupplier booleanSupplier) {
        this.a = new ArrayBlockingQueue<>(32);
        this.b = new Object();
        this.c = new AtomicBoolean();
        this.d = cVar;
        this.e = consumer;
        this.g = z;
        this.h = booleanSupplier;
        a aVar = new a(this, i5Var, supplier, consumer);
        this.f = z ? MfiCertificateVerifier.u(str, "CarPlus", aVar) : MfiCertificateVerifier.S(str, "CarPlus", aVar);
    }

    public static Iap2WirelessSetup l(String str, c cVar, Consumer<String> consumer, i5 i5Var) {
        return m(str, cVar, consumer, i5Var, new f1(0));
    }

    public static Iap2WirelessSetup m(String str, c cVar, Consumer<String> consumer, i5 i5Var, Supplier<Boolean> supplier) {
        return n(str, cVar, consumer, i5Var, supplier, new g1(0));
    }

    public static Iap2WirelessSetup n(String str, c cVar, Consumer<String> consumer, i5 i5Var, Supplier<Boolean> supplier, BooleanSupplier booleanSupplier) {
        return new Iap2WirelessSetup(str, cVar, consumer, i5Var, true, supplier, booleanSupplier);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Boolean o() {
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean p() {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Boolean q() {
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean r() {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void t() {
        this.e.accept("CARPLAY_IAP_THREAD_RUNNING");
        try {
            try {
                b bVarB = this.d.b();
                synchronized (this.b) {
                    if (this.c.get()) {
                        bVarB.close();
                        close();
                        return;
                    }
                    this.i = bVarB;
                    long jNanoTime = System.nanoTime();
                    while (!this.c.get()) {
                        byte[] bArrPoll = this.a.poll(50L, TimeUnit.MILLISECONDS);
                        boolean z = false;
                        if (bArrPoll != null) {
                            synchronized (this.b) {
                                try {
                                    if (this.c.get()) {
                                        Arrays.fill(bArrPoll, (byte) 0);
                                        break;
                                    }
                                    this.k -= bArrPoll.length;
                                    try {
                                        this.f.Y(bArrPoll, 0, bArrPoll.length, System.nanoTime() / 1000000);
                                        Arrays.fill(bArrPoll, (byte) 0);
                                    } catch (Throwable th) {
                                        Arrays.fill(bArrPoll, (byte) 0);
                                        throw th;
                                    }
                                } catch (Throwable th2) {
                                    throw th2;
                                }
                            }
                        }
                        if (this.g) {
                            this.h.getAsBoolean();
                            synchronized (this.b) {
                                try {
                                    if (!this.c.get() && this.k == 0 && this.a.isEmpty()) {
                                        z = true;
                                    }
                                } catch (Throwable th3) {
                                    throw th3;
                                }
                            }
                            this.f.G(System.nanoTime() / 1000000, z);
                        }
                        this.f.i0(System.nanoTime() / 1000000);
                        if (System.nanoTime() - jNanoTime > 30000000000L && !this.f.h()) {
                            throw new IOException("network iAP handshake deadline");
                        }
                    }
                    close();
                }
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
                close();
            } catch (Exception e) {
                if (!this.c.get()) {
                    this.e.accept("NETWORK_IAP_ENDED kind=" + e.getClass().getSimpleName() + " media_result_unconfirmed=true");
                    String message = e.getMessage();
                    if (message == null || !message.matches("(?:IAP2_[A-Z0-9_]{1,72}|MFI_[A-Z0-9_]{1,72})")) {
                        message = "UNCLASSIFIED";
                    }
                    this.e.accept("NETWORK_IAP_FINAL reason=" + message + "; " + this.f.w() + "; " + this.f.C());
                }
                close();
            }
        } catch (Throwable th4) {
            close();
            throw th4;
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        if (this.c.compareAndSet(false, true)) {
            this.f.p();
            b bVar = this.i;
            if (bVar != null) {
                bVar.close();
            }
            synchronized (this.b) {
                while (true) {
                    try {
                        byte[] bArrPoll = this.a.poll();
                        if (bArrPoll == null) {
                            break;
                        } else {
                            Arrays.fill(bArrPoll, (byte) 0);
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                this.k = 0;
                Thread thread = this.j;
                if (thread != null && thread != Thread.currentThread()) {
                    this.j.interrupt();
                }
            }
        }
    }

    public boolean s(byte[] bArr) {
        int i = 0;
        if (bArr == null || bArr.length == 0 || bArr.length > 16384) {
            return false;
        }
        synchronized (this.b) {
            try {
                if (!this.c.get() && this.k <= PcmConverter.t - bArr.length) {
                    if (this.j == null) {
                        if ((bArr[0] & 255) == 255) {
                            if (bArr.length > 1) {
                                byte b2 = bArr[1];
                                if ((b2 & 255) != 85 && (b2 & 255) != 90) {
                                }
                            }
                            d1 d1Var = new d1(i);
                            d1Var.b = this;
                            VarHandle.storeStoreFence();
                            Thread thread = new Thread(d1Var, "CarPlus-network-iAP2");
                            this.j = thread;
                            thread.setDaemon(true);
                        }
                        return false;
                    }
                    if (!this.a.offer((byte[]) bArr.clone())) {
                        return false;
                    }
                    this.k += bArr.length;
                    if (this.j.getState() == Thread.State.NEW) {
                        this.j.start();
                    }
                    return true;
                }
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void u() {
        synchronized (this.b) {
            if (!this.c.get() && this.j == null) {
                d1 d1Var = new d1(0);
                d1Var.b = this;
                VarHandle.storeStoreFence();
                Thread thread = new Thread(d1Var, "CarPlus-network-iAP2");
                this.j = thread;
                thread.setDaemon(true);
                this.j.start();
            }
        }
    }

    public Iap2WirelessSetup(String str, c cVar, Consumer<String> consumer, i5 i5Var) {
        this(str, cVar, consumer, i5Var, new f1(1));
    }

    public Iap2WirelessSetup(String str, c cVar, Consumer<String> consumer, i5 i5Var, Supplier<Boolean> supplier) {
        this(str, cVar, consumer, i5Var, false, supplier, new g1(1));
    }

    public Iap2WirelessSetup(String str, c cVar, Consumer<String> consumer) {
        this(str, cVar, consumer, null);
    }
}
