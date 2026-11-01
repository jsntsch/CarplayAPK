package io.carplay.app.secured;

import java.lang.invoke.VarHandle;

/* JADX INFO: loaded from: classes.dex */
public final class x5 {
    public final g6 a = new g6();
    public boolean b;
    public long c;

    public static final class a {
        public x5 a;
        public long b;
        public long c;
        public boolean d;
    }

    public final synchronized void a(boolean z, long j) {
        if (!this.b) {
            throw new IllegalStateException("release check not active");
        }
        this.a.a.set(z ? j + 30000 : 0L);
        this.b = false;
    }

    public final synchronized a b(long j) {
        a aVar = null;
        if (this.b) {
            return null;
        }
        long andSet = this.a.a.getAndSet(0L);
        if (andSet <= j || andSet - j > 30000) {
            andSet = 0;
        }
        if (andSet != 0) {
            long j2 = this.c;
            aVar = new a();
            aVar.a = this;
            aVar.b = j2;
            aVar.c = andSet;
            VarHandle.storeStoreFence();
        }
        return aVar;
    }
}
