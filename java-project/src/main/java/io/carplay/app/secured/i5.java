package io.carplay.app.secured;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/* JADX INFO: loaded from: classes.dex */
public final class i5 {
    private final String a;
    private final String b;
    private final b c;

    public enum b {
        BLUETOOTH_ADAPTER,
        ANDROID_MODEL
    }

    public i5(String str, String str2, b bVar) {
        if (str == null || !str.matches("[a-fA-F0-9]{8}(?:-[a-fA-F0-9]{4}){3}-[a-fA-F0-9]{12}")) {
            d0.b("invalid temporary device UUID");
            throw null;
        }
        String strD = d(str2);
        if (strD == null) {
            d0.b("invalid device display name");
            throw null;
        }
        this.a = UUID.fromString(str).toString();
        this.b = strD;
        Objects.requireNonNull(bVar);
        this.c = bVar;
    }

    public static String d(String str) {
        if (str != null && !str.isBlank()) {
            int i = 0;
            while (i < str.length()) {
                char cCharAt = str.charAt(i);
                if (Character.isISOControl(cCharAt)) {
                    return null;
                }
                if (Character.isHighSurrogate(cCharAt)) {
                    i++;
                    if (i >= str.length() || !Character.isLowSurrogate(str.charAt(i))) {
                        return null;
                    }
                } else if (Character.isLowSurrogate(cCharAt)) {
                    return null;
                }
                i++;
            }
            if (str.getBytes(StandardCharsets.UTF_8).length <= 248) {
                return str;
            }
        }
        return null;
    }

    public String a() {
        return this.b;
    }

    public int b() {
        return this.b.getBytes(StandardCharsets.UTF_8).length;
    }

    public b c() {
        return this.c;
    }

    public String e() {
        return this.a;
    }

    public static final class a implements AutoCloseable {
        private final c a;
        private final i5 b;
        private boolean c;

        private a(c cVar, i5 i5Var) {
            this.a = cVar;
            this.b = i5Var;
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            this.a.e(this);
        }

        public i5 e() {
            return this.b;
        }

        public /* synthetic */ a(c cVar, i5 i5Var, int i) {
            this(cVar, i5Var);
        }
    }

    public static final class c {
        private final LongSupplier a;
        private final Supplier<String> b;
        private i5 c;
        private int d;
        private long e;

        public c(LongSupplier longSupplier, Supplier<String> supplier) {
            Objects.requireNonNull(longSupplier);
            this.a = longSupplier;
            Objects.requireNonNull(supplier);
            this.b = supplier;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ String d() {
            return UUID.randomUUID().toString();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public synchronized void e(a aVar) {
            if (aVar.c) {
                return;
            }
            aVar.c = true;
            int i = this.d - 1;
            this.d = i;
            if (i == 0) {
                this.e = this.a.getAsLong();
            }
        }

        public synchronized a c(String str, b bVar) {
            try {
                long asLong = this.a.getAsLong();
                if (this.c == null) {
                    this.c = new i5(this.b.get(), str, bVar);
                } else if (this.d == 0) {
                    long j = this.e;
                    if (asLong >= j && asLong - j >= 60000) {
                        this.c = new i5(this.b.get(), str, bVar);
                    }
                }
                int i = this.d;
                if (i == Integer.MAX_VALUE) {
                    throw new IllegalStateException("too many device metadata owners");
                }
                this.d = i + 1;
            } catch (Throwable th) {
                throw th;
            }
            return new a(this, this.c, 0);
        }

        public c(LongSupplier longSupplier) {
            this(longSupplier, new f1(4));
        }
    }
}
