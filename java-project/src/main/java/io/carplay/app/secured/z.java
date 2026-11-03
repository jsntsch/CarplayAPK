package io.carplay.app.secured;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/* JADX INFO: loaded from: classes.dex */
public final class z {
    private final int a;
    private final LinkedHashMap<String, a> b = new LinkedHashMap<>(16, 0.75f, true);

    public static final class a extends ProtoMessageBase {
        private final String a;
        private final int b;
        private final int c;
        private final int d;
        private final int e;

        public a(String str, int i, int i2, int i3, int i4) {
            if (str == null || !str.matches("[0-9a-f]{64}") || i < 1 || i > 1000000 || i2 < 1 || i3 < 1 || i2 > 8192 || i3 > 8192 || i4 < 1 || i4 > 240) {
                SyntheticRefs.b("CAP_HINT_INVALID");
                throw null;
            }
            this.a = str;
            this.b = i;
            this.c = i2;
            this.d = i3;
            this.e = i4;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.b == aVar.b && this.c == aVar.c && this.d == aVar.d && this.e == aVar.e && Objects.equals(this.a, aVar.a);
        }

        public String g() {
            return this.a;
        }

        public int h() {
            return this.e;
        }

        public final int hashCode() {
            int i = this.b;
            int i2 = this.c;
            int i3 = this.d;
            int i4 = this.e;
            return Objects.hashCode(this.a) + (((((((i * 31) + i2) * 31) + i3) * 31) + i4) * 31);
        }

        public int i() {
            return this.d;
        }

        public int j() {
            return this.b;
        }

        public int k() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {this.a, Integer.valueOf(this.b), Integer.valueOf(this.c), Integer.valueOf(this.d), Integer.valueOf(this.e)};
            String[] strArrSplit = "a;b;c;d;e".length() == 0 ? new String[0] : "a;b;c;d;e".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(a.class, sb, "[");
            for (int i = 0; i < strArrSplit.length; i++) {
                sb.append(strArrSplit[i]);
                sb.append("=");
                sb.append(objArr[i]);
                if (i != strArrSplit.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    public z(int i) {
        if (i < 1 || i > 32) {
            SyntheticRefs.b("CAP_HINT_CACHE_BOUND");
            throw null;
        }
        this.a = i;
    }

    private static boolean c(HidCapabilities xVar, a aVar) {
        int i = 0;
        if (xVar.o().equals(aVar.g()) && xVar.E()) {
            Stream<HidCapabilities.c> stream = xVar.i().stream();
            y yVar = new y(i);
            yVar.b = aVar;
            VarHandle.storeStoreFence();
            if (stream.anyMatch(yVar)) {
                return true;
            }
        }
        return false;
    }

    private static String e(String str, int i) {
        return i + ":" + str;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean f(a aVar, HidCapabilities.c cVar) {
        return cVar.q() == 110 && cVar.k() && cVar.v() == aVar.k() && cVar.j() == aVar.i() && cVar.n() >= aVar.h();
    }

    public synchronized HidCapabilities.c b(HidCapabilities xVar, int i) throws IOException {
        Optional<a> optionalD = d(xVar, i);
        if (optionalD.isPresent()) {
            for (HidCapabilities.c cVar : xVar.i()) {
                if (cVar.q() == 110 && cVar.k() && cVar.v() == optionalD.get().k() && cVar.j() == optionalD.get().i() && cVar.n() >= optionalD.get().h()) {
                    return cVar;
                }
            }
        }
        return xVar.L();
    }

    public synchronized Optional<a> d(HidCapabilities xVar, int i) {
        a aVar;
        try {
            aVar = this.b.get(e(xVar.o(), i));
        } catch (Throwable th) {
            throw th;
        }
        return (aVar == null || !c(xVar, aVar)) ? Optional.empty() : Optional.of(aVar);
    }

    public synchronized a g(HidCapabilities xVar, int i, HidCapabilities.c cVar, int i2) throws IOException {
        a aVar;
        aVar = new a(xVar.o(), i, cVar.v(), cVar.j(), i2);
        if (!c(xVar, aVar) || !xVar.i().contains(cVar)) {
            throw new IOException("CAP_HINT_NOT_CURRENT");
        }
        this.b.put(e(aVar.g(), i), aVar);
        while (this.b.size() > this.a) {
            LinkedHashMap<String, a> linkedHashMap = this.b;
            linkedHashMap.remove(linkedHashMap.keySet().iterator().next());
        }
        return aVar;
    }

    public synchronized void h(a aVar) {
        this.b.put(e(aVar.g(), aVar.j()), aVar);
        while (this.b.size() > this.a) {
            LinkedHashMap<String, a> linkedHashMap = this.b;
            linkedHashMap.remove(linkedHashMap.keySet().iterator().next());
        }
    }

    public synchronized int i() {
        return this.b.size();
    }
}
