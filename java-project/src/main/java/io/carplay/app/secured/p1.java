package io.carplay.app.secured;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public final class p1 {
    public static final int c = 1;
    public static final int d = 3;
    private final x a;
    private final List<x.c> b;

    public static /* synthetic */ class a {
        static final /* synthetic */ int[] a;

        static {
            int[] iArr = new int[r6.a.values().length];
            a = iArr;
            try {
                iArr[r6.a.MEDIA.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                a[r6.a.TTS.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                a[r6.a.VR.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public static final class b extends w1 {
        private final int a;
        private final int b;
        private final int c;

        public b(int i, int i2, int i3) {
            this.a = i;
            this.b = i2;
            this.c = i3;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && this.b == bVar.b && this.c == bVar.c;
        }

        public int g() {
            return this.c;
        }

        public int h() {
            return this.b;
        }

        public final int hashCode() {
            int i = this.a;
            return (((i * 31) + this.b) * 31) + this.c;
        }

        public int i() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(b.class, sb, "[");
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

    private p1(x xVar, List<x.c> list) {
        this.a = xVar;
        this.b = List.copyOf(list);
    }

    public static List<s.d> d(s sVar, r6.a aVar, int i, int i2, int i3, Set<Integer> set) throws IOException {
        String str;
        int i4 = a.a[aVar.ordinal()];
        if (i4 == 1) {
            str = "media";
        } else if (i4 == 2) {
            str = "alert";
        } else {
            if (i4 != 3) {
                d0.c("AUDIO_SOURCE_CHANNEL_UNSUPPORTED");
                return null;
            }
            str = "speechRecognition";
        }
        ArrayList arrayList = new ArrayList();
        for (s.d dVar : sVar.x(str, i, i2, i3)) {
            if (!set.contains(Integer.valueOf(dVar.p()))) {
                arrayList.add(dVar);
                if (arrayList.size() == 3) {
                    break;
                }
            }
        }
        return List.copyOf(arrayList);
    }

    public static p1 f(x xVar, z zVar) {
        Objects.requireNonNull(xVar);
        Objects.requireNonNull(zVar);
        x.c cVarB = zVar.b(xVar, 1);
        ArrayList arrayList = new ArrayList(xVar.i());
        o1 o1Var = new o1();
        o1Var.a = cVarB;
        VarHandle.storeStoreFence();
        arrayList.sort(Comparator.comparing(o1Var).thenComparing(new r(2)).thenComparing(Comparator.comparingLong(new p(3)).reversed()));
        arrayList.removeIf(new w0(18));
        if (!arrayList.isEmpty()) {
            return new p1(xVar, arrayList);
        }
        d0.c("CAP_MAIN_DISPLAY_UNAVAILABLE");
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ Boolean h(x.c cVar, x.c cVar2) {
        return Boolean.valueOf(cVar2.p() != cVar.p());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ long i(x.c cVar) {
        return ((long) cVar.v()) * ((long) cVar.j());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean j(x.c cVar) {
        return (cVar.q() == 110 && cVar.k()) ? false : true;
    }

    public static boolean k(int i) {
        return i == 400 || i == 406 || i == 415;
    }

    public static b l(int i, int i2, int i3) {
        if (i < 8 || i2 < 8) {
            d0.b("receiver dimensions too small");
            return null;
        }
        double dMin = Math.min(1.0d, 1920.0d / ((double) Math.max(i, i2)));
        return new b(Math.max(8, (((int) (((double) i) * dMin)) / 8) * 8), Math.max(8, (((int) (((double) i2) * dMin)) / 8) * 8), Math.max(1, Math.min(60, i3)));
    }

    public s e() {
        return this.a.f();
    }

    public List<x.c> g() {
        return this.b;
    }

    public String m() {
        return "CAPABILITY_PLAN policy=1 fingerprint=" + this.a.o() + " display_candidates=" + this.b.size() + " audio_table_valid=" + this.a.h() + " max_route_attempts=3 physical_receipt_confirmed=false";
    }
}
