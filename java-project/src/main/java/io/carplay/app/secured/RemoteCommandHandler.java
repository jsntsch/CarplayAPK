package io.carplay.app.secured;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/* JADX INFO: loaded from: classes.dex */
public final class RemoteCommandHandler {

    public interface a {
    }

    public interface b {
        default boolean b(boolean z) {
            return false;
        }

        default boolean c(d dVar) {
            return false;
        }
    }

    public static final class c extends ProtoMessageBase implements a {
        private final boolean a;

        public c(boolean z) {
            this.a = z;
        }

        public final boolean equals(Object obj) {
            return (obj instanceof c) && this.a == ((c) obj).a;
        }

        public boolean g() {
            return this.a;
        }

        public final int hashCode() {
            return Boolean.hashCode(this.a);
        }

        public final String toString() {
            Object[] objArr = {Boolean.valueOf(this.a)};
            String[] strArrSplit = "a".length() == 0 ? new String[0] : "a".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(c.class, sb, "[");
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

    public static final class d extends ProtoMessageBase implements a {
        private final int a;
        private final HidCapabilities.a b;
        private final int c;
        private final List<Integer> d;

        public d(int i, HidCapabilities.a aVar, int i2, List<Integer> list) {
            List<Integer> listCopyOf = List.copyOf(list);
            this.a = i;
            this.b = aVar;
            this.c = i2;
            this.d = listCopyOf;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof d)) {
                return false;
            }
            d dVar = (d) obj;
            return this.a == dVar.a && this.c == dVar.c && Objects.equals(this.b, dVar.b) && Objects.equals(this.d, dVar.d);
        }

        public List<Integer> g() {
            return this.d;
        }

        public int h() {
            return this.c;
        }

        public final int hashCode() {
            int i = this.a;
            int i2 = this.c;
            HidCapabilities.a aVar = this.b;
            List<Integer> list = this.d;
            return Objects.hashCode(list) + ((Objects.hashCode(aVar) + (((i * 31) + i2) * 31)) * 31);
        }

        public HidCapabilities.a i() {
            return this.b;
        }

        public int j() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), this.b, Integer.valueOf(this.c), this.d};
            String[] strArrSplit = "a;b;c;d".length() == 0 ? new String[0] : "a;b;c;d".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(d.class, sb, "[");
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

    private RemoteCommandHandler() {
    }

    public static boolean a(a aVar, b bVar) {
        if (aVar == null || bVar == null) {
            throw new NullPointerException("command and handler required");
        }
        return aVar instanceof c ? bVar.b(((c) aVar).g()) : bVar.c((d) aVar);
    }

    public static Optional<a> b(Map<?, ?> map, String str, HidCapabilities.c cVar) throws IOException {
        Object obj = map.get("type");
        if (!"setNightMode".equals(obj) && !"updateViewArea".equals(obj)) {
            return Optional.empty();
        }
        if (e(map, Set.of("type", "params"))) {
            Object obj2 = map.get("params");
            if (obj2 instanceof Map) {
                Map map2 = (Map) obj2;
                if ("setNightMode".equals(obj)) {
                    if (e(map2, Set.of("nightMode"))) {
                        Object obj3 = map2.get("nightMode");
                        if (obj3 instanceof Boolean) {
                            return Optional.of(new c(((Boolean) obj3).booleanValue()));
                        }
                    }
                    throw c("REMOTE_NIGHT_MODE_INVALID");
                }
                if (cVar == null || str == null || str.isBlank() || str.length() > 128 || cVar.u().isEmpty()) {
                    throw c("REMOTE_VIEW_AREA_NOT_DECLARED");
                }
                if (!e(map2, Set.of("uuid", "viewAreaIndex", "animationDurationMillis", "adjacentViewAreas")) || !str.equals(map2.get("uuid"))) {
                    throw c("REMOTE_VIEW_AREA_BINDING_INVALID");
                }
                int size = cVar.u().size();
                int iD = d(map2.get("viewAreaIndex"));
                int iD2 = d(map2.get("animationDurationMillis"));
                if (iD >= 0 && iD < size && iD2 >= 0 && iD2 <= 5000) {
                    Object obj4 = map2.get("adjacentViewAreas");
                    if (obj4 instanceof List) {
                        List list = (List) obj4;
                        if (list.size() <= 16) {
                            ArrayList arrayList = new ArrayList();
                            HashSet hashSet = new HashSet();
                            Iterator it = list.iterator();
                            while (it.hasNext()) {
                                int iD3 = d(it.next());
                                if (iD3 < 0 || iD3 >= size || iD3 == iD || !hashSet.add(Integer.valueOf(iD3))) {
                                    throw c("REMOTE_VIEW_AREA_ADJACENCY_INVALID");
                                }
                                arrayList.add(Integer.valueOf(iD3));
                            }
                            return Optional.of(new d(iD, cVar.u().get(iD), iD2, arrayList));
                        }
                    }
                }
                throw c("REMOTE_VIEW_AREA_PARAMETERS_INVALID");
            }
        }
        throw c("REMOTE_COMMAND_ENVELOPE_INVALID");
    }

    private static IOException c(String str) {
        return new IOException(str);
    }

    private static int d(Object obj) {
        if (!(obj instanceof Byte) && !(obj instanceof Short) && !(obj instanceof Integer) && !(obj instanceof Long)) {
            return -1;
        }
        long jLongValue = ((Number) obj).longValue();
        if (jLongValue < 0 || jLongValue > 2147483647L) {
            return -1;
        }
        return (int) jLongValue;
    }

    private static boolean e(Map<?, ?> map, Set<String> set) {
        if (map.size() != set.size()) {
            return false;
        }
        Stream<?> stream = map.keySet().stream();
        a1 a1Var = new a1(1);
        a1Var.b = set;
        VarHandle.storeStoreFence();
        return stream.allMatch(a1Var);
    }
}
