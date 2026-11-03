package io.carplay.app.secured;

import java.lang.invoke.VarHandle;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/* JADX INFO: loaded from: classes.dex */
/**
 * SessionStateMachine
 *
 * <p>原混淆名：{@code io.carplay.app.secured.b1}（jadx 产物 b1.java）
 *
 * <p>命名依据：MODE_INITIAL / NOT_INITIALIZED 状态迁移
 */
public final class SessionStateMachine {
    private static final int j = 1;
    private static final int k = 2;
    private static final int l = 255;
    private static final BigInteger m;
    private static final Map<String, Object> n;
    private boolean a;
    private boolean b;
    private boolean c;
    private b d;
    private b e;
    private Map<Integer, Map<String, Object>> f = new LinkedHashMap();
    private String g = "MODE_INITIAL";
    private String h = "NOT_INITIALIZED";
    private a i = new a("MODE_INFO_NOT_INITIALIZED", "NONE", -1);

    public static final class a extends ProtoMessageBase {
        private final String a;
        private final String b;
        private final int c;

        public a(String str, String str2, int i) {
            this.a = str;
            this.b = str2;
            this.c = i;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.c == aVar.c && Objects.equals(this.a, aVar.a) && Objects.equals(this.b, aVar.b);
        }

        public String g() {
            return this.a;
        }

        public String h() {
            return this.b;
        }

        public final int hashCode() {
            int i = this.c;
            String str = this.a;
            String str2 = this.b;
            return Objects.hashCode(str2) + ProtoMessageBase.b(str, i * 31, 31);
        }

        public int i() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, Integer.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
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

    static {
        BigInteger bigInteger = BigInteger.ONE;
        m = bigInteger.shiftLeft(64).subtract(bigInteger);
        n = Map.of("status", 1);
    }

    public SessionStateMachine(boolean z) {
        int i = 0;
        this.d = new b(i);
        this.e = new b(i);
        this.a = z;
        this.b = z;
    }

    private static boolean A(Map<?, ?> map, Set<String> set) {
        int i = 0;
        if (map.size() <= set.size()) {
            Stream<?> stream = map.keySet().stream();
            a1 a1Var = new a1(i);
            a1Var.b = set;
            VarHandle.storeStoreFence();
            if (stream.allMatch(a1Var)) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean B(Set set, Object obj) {
        return (obj instanceof String) && set.contains(obj);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean C(Object obj) {
        return obj instanceof String;
    }

    private static List<?> E(Map<String, Object> map, String str, int i) {
        Object obj = map.containsKey(str) ? map.get(str) : Collections.EMPTY_LIST;
        if (!(obj instanceof List)) {
            return null;
        }
        List<?> list = (List) obj;
        if (list.size() <= i) {
            return list;
        }
        return null;
    }

    private static Integer F(Map<?, ?> map, String str, int i) {
        return (!map.containsKey(str) || map.get(str) == null) ? Integer.valueOf(i) : z(map.get(str));
    }

    private Map<String, Object> G(b bVar, b bVar2) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(bVar.d(1));
        if (this.b) {
            arrayList.add(bVar2.d(k));
        }
        return Map.of("appStates", List.copyOf(this.f.values()), "resources", List.copyOf(arrayList));
    }

    private static Map<Integer, Map<String, Object>> H(Map<String, Object> map, Map<Integer, Map<String, Object>> map2, boolean z) {
        Integer numZ;
        List<?> listE = E(map, "appStates", 3);
        if (listE == null) {
            return null;
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap(map2);
        HashSet hashSet = new HashSet();
        for (Object obj : listE) {
            if (obj instanceof Map) {
                Map map3 = (Map) obj;
                if (A(map3, Set.of("appStateID", "state", "speechMode")) && (numZ = z(map3.get("appStateID"))) != null && hashSet.add(numZ) && numZ.intValue() >= 1 && numZ.intValue() <= 3) {
                    if (numZ.intValue() == 1) {
                        Object obj2 = map3.get("speechMode");
                        Integer numZ2 = z(obj2);
                        if (m.equals(obj2)) {
                            numZ2 = -1;
                        }
                        Integer num = (z && numZ2 == null && ((obj2 instanceof Double) || (obj2 instanceof Float)) && ((Number) obj2).doubleValue() == -1.0d) ? -1 : numZ2;
                        if (num == null || !((num.intValue() == -1 || num.intValue() == 1 || num.intValue() == k) && g(map3, "state"))) {
                            return null;
                        }
                        linkedHashMap.put(numZ, Map.of("appStateID", numZ, "entity", Integer.valueOf(num.intValue() != -1 ? k : 0), "speechMode", num));
                    } else {
                        Object obj3 = map3.get("state");
                        if (obj3 instanceof Boolean) {
                            Boolean bool = (Boolean) obj3;
                            if (g(map3, "speechMode")) {
                                linkedHashMap.put(numZ, Map.of("appStateID", numZ, "entity", Integer.valueOf(bool.booleanValue() ? k : 0)));
                            }
                        }
                    }
                }
            }
            return null;
        }
        return linkedHashMap;
    }

    private Map<Integer, Map<String, Object>> I(Map<String, Object> map) {
        Map<Integer, Map<String, Object>> map2;
        List<?> listE = E(map, "appStates", 4);
        Map<Integer, Map<String, Object>> map3 = null;
        if (listE == null) {
            t("MODE_INFO_LIST", "APP_STATE", -1);
            return null;
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        HashSet hashSet = new HashSet();
        int i = 0;
        for (Object obj : listE) {
            if (obj instanceof Map) {
                Map map4 = (Map) obj;
                if (A(map4, Set.of("appStateID", "state", "speechMode"))) {
                    Integer numZ = z(map4.get("appStateID"));
                    if (numZ == null || numZ.intValue() < 0) {
                        map2 = map3;
                    } else {
                        map2 = map3;
                        if (numZ.intValue() <= 3 && hashSet.add(numZ)) {
                            Object obj2 = map4.get("state");
                            Object obj3 = map4.get("speechMode");
                            Integer numZ2 = obj3 == null ? -1 : z(obj3);
                            if (m.equals(obj3)) {
                                numZ2 = -1;
                            }
                            Integer num = (numZ2 == null && ((obj3 instanceof Double) || (obj3 instanceof Float)) && ((Number) obj3).doubleValue() == -1.0d) ? -1 : numZ2;
                            if ((obj2 == null || (obj2 instanceof Boolean)) && num != null) {
                                int iIntValue = num.intValue();
                                int i2 = k;
                                if (iIntValue == -1 || num.intValue() == 0 || num.intValue() == 1 || num.intValue() == k) {
                                    if (numZ.intValue() == 1) {
                                        if (num.intValue() == -1) {
                                            i2 = 0;
                                        }
                                        linkedHashMap.put(numZ, Map.of("appStateID", numZ, "entity", Integer.valueOf(i2), "speechMode", num));
                                    } else if (numZ.intValue() != 0) {
                                        if (!Boolean.TRUE.equals(obj2)) {
                                            i2 = 0;
                                        }
                                        linkedHashMap.put(numZ, Map.of("appStateID", numZ, "entity", Integer.valueOf(i2)));
                                    }
                                    i++;
                                    map3 = map2;
                                }
                            }
                            t("MODE_INFO_APP_VALUE", "APP_STATE", i);
                            return map2;
                        }
                    }
                    t("MODE_INFO_APP_ID", "APP_STATE", i);
                    return map2;
                }
            }
            Map<Integer, Map<String, Object>> map5 = map3;
            t("MODE_INFO_ROW", "APP_STATE", i);
            return map5;
        }
        return linkedHashMap;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean L(Integer num) {
        if (num != null) {
            return num.intValue() == 100 || num.intValue() == 500;
        }
        return false;
    }

    private Map<String, Object> N() {
        this.g = "MODE_REQUEST_REJECTED";
        return n;
    }

    private static boolean R(Map<String, Object> map) {
        if (map == null || !A(map, Set.of("resources", "appStates", "reasonStr", "initialPermanentEntity"))) {
            return false;
        }
        if (!map.containsKey("reasonStr")) {
            return true;
        }
        Object obj = map.get("reasonStr");
        return (obj instanceof String) && ((String) obj).length() <= 256;
    }

    private static boolean S(Map<String, Object> map) {
        if (map == null || map.size() > 16 || !map.keySet().stream().allMatch(new w0(17))) {
            return false;
        }
        if (!map.containsKey("reasonStr")) {
            return true;
        }
        Object obj = map.get("reasonStr");
        return (obj instanceof String) && ((String) obj).length() <= 256;
    }

    public static /* bridge */ /* synthetic */ Integer e(Map map, String str) {
        return F(map, str, 100);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean g(Map<?, ?> map, String... strArr) {
        for (String str : strArr) {
            if (map.get(str) != null) {
                return false;
            }
        }
        return true;
    }

    private boolean h(List<?> list, b bVar, b bVar2) {
        HashSet hashSet = new HashSet();
        int i = 0;
        for (Object obj : list) {
            if (obj instanceof Map) {
                Map map = (Map) obj;
                if (A(map, Set.of("resourceID", "transferType", "transferPriority", "takeConstraint", "borrowConstraint", "unborrowConstraint"))) {
                    Integer numZ = z(map.get("resourceID"));
                    Integer numZ2 = z(map.get("transferType"));
                    if (numZ == null || (!(numZ.intValue() == 1 || numZ.intValue() == k) || !hashSet.add(numZ) || numZ2 == null || numZ2.intValue() < 1 || numZ2.intValue() > 4)) {
                        return t("MODE_INFO_ID_TRANSFER", "RESOURCE", i);
                    }
                    Integer numF = F(map, "transferPriority", 100);
                    Integer numF2 = F(map, "takeConstraint", 100);
                    Integer numF3 = F(map, "borrowConstraint", 100);
                    Integer numF4 = F(map, "unborrowConstraint", 100);
                    if (!L(numF) || !m(numF2) || !m(numF3) || !m(numF4)) {
                        return t("MODE_INFO_RESOURCE_ENUM", "RESOURCE", i);
                    }
                    b bVar3 = numZ.intValue() == 1 ? bVar : bVar2;
                    if (numZ2.intValue() == 1) {
                        bVar3.a = k;
                        bVar3.b = k;
                        bVar3.c = 0;
                        bVar3.d = numF2.intValue();
                        bVar3.e = numF3.intValue();
                        bVar3.f = 100;
                    } else if (numZ2.intValue() == 3 && bVar3.b == 1) {
                        bVar3.a = k;
                        bVar3.c = 1;
                        bVar3.f = numF4.intValue();
                    }
                    i++;
                }
            }
            return t("MODE_INFO_ROW", "RESOURCE", i);
        }
        return true;
    }

    /* JADX WARN: Code duplicated, block: B:37:0x009e  */
    private static boolean i(List<?> list, b bVar, b bVar2, boolean z, boolean z2) {
        Map<?, ?> linkedHashMap;
        HashSet hashSet = new HashSet();
        for (Object obj : list) {
            if (obj instanceof Map) {
                Map<?, ?> map = (Map) obj;
                if (A(map, Set.of("resourceID", "transferType", "transferPriority", "takeConstraint", "borrowConstraint", "unborrowConstraint"))) {
                    Integer numZ = z(map.get("resourceID"));
                    Integer numZ2 = z(map.get("transferType"));
                    if (numZ != null && hashSet.add(numZ) && numZ2 != null && numZ2.intValue() >= 1 && numZ2.intValue() <= 4) {
                        b bVar3 = numZ.intValue() == 1 ? bVar : (numZ.intValue() == k && z) ? bVar2 : null;
                        if (bVar3 == null) {
                            return false;
                        }
                        if (z2 && numZ2.intValue() == 1) {
                            Integer num = 100;
                            if (num.equals(z(map.get("unborrowConstraint")))) {
                                linkedHashMap = new LinkedHashMap<>((Map<? extends Object, ? extends Object>) map);
                                linkedHashMap.remove("unborrowConstraint");
                            } else {
                                linkedHashMap = map;
                            }
                        } else {
                            linkedHashMap = map;
                        }
                        if (z2 && (numZ2.intValue() == k || numZ2.intValue() == 4)) {
                            if (!g(map, "transferPriority", "takeConstraint", "borrowConstraint", "unborrowConstraint")) {
                                return false;
                            }
                        } else if (!bVar3.e(linkedHashMap, numZ2.intValue())) {
                        }
                    }
                }
            }
            return false;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean m(Integer num) {
        if (num != null) {
            return num.intValue() == 100 || num.intValue() == 500 || num.intValue() == 1000;
        }
        return false;
    }

    private boolean n(boolean z, String str) {
        this.g = str;
        return z;
    }

    private static boolean o(Map<String, Object> map, String str) {
        if (!map.containsKey(str)) {
            return true;
        }
        Object obj = map.get(str);
        return (obj instanceof List) && ((List) obj).isEmpty();
    }

    private static boolean r(List<?> list, List<?> list2) {
        if (list2 == null) {
            list2 = Collections.EMPTY_LIST;
        }
        List[] listArr = {list, list2};
        for (int i = 0; i < k; i++) {
            for (Object obj : listArr[i]) {
                if ((obj instanceof Map) && ((Map) obj).containsKey("entity")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean t(String str, String str2, int i) {
        this.i = new a(str, str2, i);
        return n(false, "MODE_INFO_REJECTED");
    }

    public static String u(Map<?, ?> map, a aVar) {
        String str;
        String strH = aVar.h();
        strH.getClass();
        switch (strH) {
            case "INITIAL_RESOURCE":
                str = "initialPermanentEntity";
                break;
            case "RESOURCE":
                str = "resources";
                break;
            case "APP_STATE":
                str = "appStates";
                break;
            default:
                str = "";
                break;
        }
        Object obj = map.get(str);
        int i = aVar.i();
        if (obj instanceof List) {
            List list = (List) obj;
            if (i >= 0 && i < list.size()) {
                Object obj2 = list.get(i);
                if (obj2 instanceof Map) {
                    Map map2 = (Map) obj2;
                    String[] strArr = {"resourceID", "entity", "permanentEntity", "transferType", "transferPriority", "takeConstraint", "borrowConstraint", "unborrowConstraint", "appStateID", "state", "speechMode"};
                    int size = map2.size();
                    int i2 = 0;
                    int i3 = 0;
                    for (int i4 = 0; i4 < 11; i4++) {
                        if (map2.containsKey(strArr[i4])) {
                            int i5 = 1 << i4;
                            i2 |= i5;
                            size--;
                            if (z(map2.get(strArr[i4])) != null) {
                                i3 |= i5;
                            }
                        }
                    }
                    return "MODE_INFO_SHAPE row_map=true known_fields_mask=" + i2 + " integer_fields_mask=" + i3 + " unknown_fields=" + size;
                }
            }
        }
        return "MODE_INFO_SHAPE row_map=false";
    }

    public static Map<String, Object> v(boolean z) {
        return new SessionStateMachine(z).Q();
    }

    private boolean y(Map<String, Object> map, List<?> list, List<?> list2) {
        int i;
        int i2;
        if (!A(map, Set.of("resources", "appStates", "reasonStr")) || !list.isEmpty()) {
            return t("MODE_INFO_STATE_ENVELOPE", "ENVELOPE", -1);
        }
        if (list2.size() != k) {
            return t("MODE_INFO_STATE_COMPLETE", "RESOURCE", -1);
        }
        int i3 = 0;
        b bVar = new b(i3);
        b bVar2 = new b(i3);
        HashSet hashSet = new HashSet();
        int i4 = 0;
        for (Object obj : list2) {
            if (obj instanceof Map) {
                Map map2 = (Map) obj;
                if (A(map2, Set.of("resourceID", "entity", "permanentEntity"))) {
                    Integer numZ = z(map2.get("resourceID"));
                    Integer numZ2 = z(map2.get("entity"));
                    Integer numZ3 = z(map2.get("permanentEntity"));
                    if (numZ != null) {
                        if (numZ.intValue() != 1) {
                            int iIntValue = numZ.intValue();
                            i2 = k;
                            if (iIntValue == k) {
                            }
                        } else {
                            i2 = k;
                        }
                        if (hashSet.add(numZ) && numZ2 != null && ((numZ2.intValue() == 1 || numZ2.intValue() == i2) && numZ3 != null && (numZ3.intValue() == 1 || numZ3.intValue() == i2))) {
                            b bVar3 = numZ.intValue() == 1 ? bVar : bVar2;
                            bVar3.a = numZ2.intValue();
                            bVar3.b = numZ3.intValue();
                            bVar3.c = (numZ2.intValue() == k && numZ3.intValue() == 1) ? 1 : 0;
                            bVar3.d = 1000;
                            bVar3.e = 1000;
                            bVar3.f = 1000;
                            bVar3.g = true;
                            i4++;
                        }
                    }
                    return t("MODE_INFO_STATE_ENTITY", "RESOURCE", i4);
                }
            }
            return t("MODE_INFO_ROW", "RESOURCE", i4);
        }
        List<?> listE = E(map, "appStates", 4);
        if (listE == null) {
            return t("MODE_INFO_LIST", "APP_STATE", -1);
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        hashSet.clear();
        Iterator<?> it = listE.iterator();
        int i5 = 0;
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof Map) {
                Map map3 = (Map) next;
                if (A(map3, Set.of("appStateID", "entity", "speechMode"))) {
                    Integer numZ4 = z(map3.get("appStateID"));
                    Integer numZ5 = z(map3.get("entity"));
                    Object obj2 = map3.get("speechMode");
                    Integer numZ6 = m.equals(obj2) ? -1 : obj2 == null ? -1 : z(obj2);
                    if (numZ4 != null && numZ4.intValue() >= 0) {
                        Iterator<?> it2 = it;
                        if (numZ4.intValue() <= 3 && hashSet.add(numZ4) && numZ5 != null && numZ5.intValue() >= 0 && numZ5.intValue() <= k && numZ6 != null) {
                            if (numZ6.intValue() != -1 && numZ6.intValue() != 0 && numZ6.intValue() != 1) {
                                if (numZ6.intValue() != k) {
                                }
                            }
                            if (numZ4.intValue() == 0) {
                                if (numZ5.intValue() != 0 || (numZ6.intValue() != -1 && numZ6.intValue() != 0)) {
                                    return t("MODE_INFO_APP_VALUE", "APP_STATE", i5);
                                }
                            } else if (numZ4.intValue() == 1) {
                                if (numZ5.intValue() == 0) {
                                    i = -1;
                                    if (numZ6.intValue() == -1 || numZ6.intValue() == 0) {
                                    }
                                    return t("MODE_INFO_APP_VALUE", "APP_STATE", i5);
                                }
                                i = -1;
                                if (numZ5.intValue() != 0 && numZ6.intValue() == i) {
                                    return t("MODE_INFO_APP_VALUE", "APP_STATE", i5);
                                }
                                linkedHashMap.put(numZ4, Map.of("appStateID", numZ4, "entity", numZ5, "speechMode", numZ6));
                            } else {
                                if (obj2 != null) {
                                    return t("MODE_INFO_APP_VALUE", "APP_STATE", i5);
                                }
                                linkedHashMap.put(numZ4, Map.of("appStateID", numZ4, "entity", numZ5));
                            }
                            i5++;
                            it = it2;
                        }
                    }
                    return t("MODE_INFO_APP_VALUE", "APP_STATE", i5);
                }
            }
            return t("MODE_INFO_ROW", "APP_STATE", i5);
        }
        this.d = bVar;
        this.e = bVar2;
        this.f = linkedHashMap;
        this.c = true;
        this.h = "MODE_STATE";
        this.i = new a("MODE_INFO_ACCEPTED", "NONE", -1);
        return n(true, "MODE_INFO_ACCEPTED");
    }

    private static Integer z(Object obj) {
        if (!(obj instanceof Byte) && !(obj instanceof Short) && !(obj instanceof Integer) && !(obj instanceof Long)) {
            return null;
        }
        long jLongValue = ((Number) obj).longValue();
        if (jLongValue < -2147483648L || jLongValue > 2147483647L) {
            return null;
        }
        return Integer.valueOf((int) jLongValue);
    }

    public synchronized String D() {
        return this.g;
    }

    /* JADX WARN: Code duplicated, block: B:11:0x000f  */
    public synchronized boolean J() {
        boolean z = false;
        if (this.b) {
            z = this.e.a == 1;
        }
        return z;
    }

    public synchronized boolean K() {
        return this.d.a == 1;
    }

    public synchronized boolean M() {
        boolean zA;
        try {
            zA = this.d.a(500);
        } catch (Throwable th) {
            throw th;
        }
        return n(zA, zA ? "MODE_SCREEN_RECLAIMED" : "MODE_SCREEN_RECLAIM_DENIED");
    }

    public synchronized boolean O() {
        if (this.a && this.b) {
            this.e.f();
            return n(true, "MODE_AUDIO_RELEASED");
        }
        return n(false, "MODE_AUDIO_NOT_DECLARED");
    }

    public synchronized String P() {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        boolean z;
        b bVar;
        b bVar2 = this.d;
        i = bVar2.a;
        i2 = bVar2.b;
        i3 = bVar2.c;
        i4 = bVar2.d;
        i5 = bVar2.e;
        i6 = bVar2.f;
        z = this.b;
        bVar = this.e;
        return "screen_entity=" + i + " screen_permanent=" + i2 + " screen_borrows=" + i3 + " screen_take=" + i4 + " screen_borrow=" + i5 + " screen_unborrow=" + i6 + " audio_declared=" + z + " audio_entity=" + bVar.a + " audio_permanent=" + bVar.b + " audio_borrows=" + bVar.c + " audio_take=" + bVar.d + " audio_borrow=" + bVar.e + " audio_unborrow=" + bVar.f;
    }

    public synchronized Map<String, Object> Q() {
        return Map.of("type", "modesChanged", "params", G(this.d, this.e));
    }

    public synchronized boolean T() {
        this.d.f();
        return n(true, "MODE_SCREEN_YIELDED");
    }

    public synchronized boolean j() {
        return k(100);
    }

    public synchronized boolean k(int i) {
        try {
            if (this.a && L(Integer.valueOf(i))) {
                boolean zA = this.e.a(i);
                if (zA) {
                    this.b = true;
                }
                return n(zA, zA ? "MODE_AUDIO_CLAIMED" : "MODE_AUDIO_CLAIM_DENIED");
            }
            return n(false, "MODE_AUDIO_CLAIM_DENIED");
        } catch (Throwable th) {
            throw th;
        }
    }

    public synchronized boolean l() {
        return k(500);
    }

    public synchronized void p() {
        this.a = true;
        this.g = "MODE_AUDIO_ENABLED";
    }

    public synchronized Map<String, Object> q(Map<String, Object> map) {
        try {
            if (R(map) && o(map, "initialPermanentEntity")) {
                List<?> listE = E(map, "resources", k);
                Map<Integer, Map<String, Object>> mapH = H(map, this.f, false);
                b bVarC = this.d.c();
                b bVarC2 = this.e.c();
                if (listE != null && mapH != null && i(listE, bVarC, bVarC2, this.a, false)) {
                    this.d = bVarC;
                    this.e = bVarC2;
                    this.f = mapH;
                    Iterator<?> it = listE.iterator();
                    while (it.hasNext()) {
                        if (Integer.valueOf(k).equals(z(((Map) it.next()).get("resourceID")))) {
                            this.b = true;
                        }
                    }
                    this.g = "MODE_REQUEST_ACCEPTED";
                    return Map.of("status", 0, "params", G(this.d, this.e));
                }
                return N();
            }
            return N();
        } catch (Throwable th) {
            throw th;
        }
    }

    public synchronized a s() {
        return this.i;
    }

    public synchronized String w() {
        return "MODE_INFO_REPRESENTATION representation=" + this.h + " screen_constraints=" + this.d.b() + " audio_constraints=" + this.e.b();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public synchronized boolean x(Map<String, Object> map) {
        b bVar;
        int i = 0;
        if (this.c) {
            this.i = new a("MODE_INFO_ALREADY_INITIALIZED", "NONE", -1);
            return n(false, "MODE_INFO_ALREADY_INITIALIZED");
        }
        if (!S(map)) {
            return t("MODE_INFO_ENVELOPE", "ENVELOPE", -1);
        }
        List<?> listE = E(map, "initialPermanentEntity", k);
        List<?> listE2 = E(map, "resources", k);
        if (listE == null) {
            return t("MODE_INFO_LIST", "INITIAL_RESOURCE", -1);
        }
        if (listE2 == null) {
            return t("MODE_INFO_LIST", "RESOURCE", -1);
        }
        if (r(listE2, E(map, "appStates", 4))) {
            return y(map, listE, listE2);
        }
        Map<Integer, Map<String, Object>> mapI = I(map);
        if (mapI == null) {
            return false;
        }
        b bVar2 = new b(0);
        b bVar3 = new b(0);
        HashSet hashSet = new HashSet();
        Iterator<?> it = listE.iterator();
        int i2 = 0;
        while (true) {
            if (!it.hasNext()) {
                boolean z = true;
                if (!h(listE2, bVar2, bVar3)) {
                    return z;
                }
                this.d = bVar2;
                this.e = bVar3;
                this.f = mapI;
                this.c = true;
                this.h = "CHANGE_MODES";
                this.i = new a("MODE_INFO_ACCEPTED", "NONE", -1);
                return n(true, "MODE_INFO_ACCEPTED");
            }
            Object next = it.next();
            if (!(next instanceof Map)) {
                break;
            }
            Map map2 = (Map) next;
            int i3 = i;
            if (!A(map2, Set.of("resourceID", "permanentEntity", "takeConstraint", "borrowConstraint"))) {
                break;
            }
            Integer numZ = z(map2.get("resourceID"));
            Integer numZ2 = z(map2.get("permanentEntity"));
            if (numZ != null && ((numZ.intValue() == 1 || numZ.intValue() == k) && hashSet.add(numZ) && numZ2 != null && numZ2.intValue() >= 0 && numZ2.intValue() <= k)) {
                if (numZ.intValue() == 1) {
                    bVar = bVar2;
                } else {
                    bVar = numZ.intValue() == k ? bVar3 : null;
                }
                Integer numF = F(map2, "takeConstraint", 100);
                Integer numF2 = F(map2, "borrowConstraint", 100);
                if (m(numF) && m(numF2)) {
                    if (numZ2.intValue() == k) {
                        bVar.a = numZ2.intValue();
                        bVar.b = numZ2.intValue();
                        bVar.d = numF.intValue();
                        bVar.e = numF2.intValue();
                    }
                    i2++;
                    i = i3;
                }
                return t("MODE_INFO_CONSTRAINT", "INITIAL_RESOURCE", i2);
            }
            return t("MODE_INFO_ID_ENTITY", "INITIAL_RESOURCE", i2);
        }
        return t("MODE_INFO_ROW", "INITIAL_RESOURCE", i2);
    }

    public static final class b {
        int a;
        int b;
        int c;
        int d;
        int e;
        int f;
        boolean g;

        private b() {
            this.a = 1;
            this.b = 1;
            this.d = 100;
            this.e = 100;
            this.f = 100;
        }

        public boolean a(int i) {
            if (this.a == 1) {
                return true;
            }
            if (i < (this.b == 1 ? this.f : this.d)) {
                return false;
            }
            this.a = 1;
            this.b = 1;
            this.c = 0;
            this.d = 100;
            this.e = 100;
            this.f = 100;
            this.g = false;
            return true;
        }

        public String b() {
            return this.g ? "UNKNOWN_CONSERVATIVE_NEVER" : "DECLARATION_DEFAULT_OR_TRANSFER";
        }

        public b c() {
            b bVar = new b();
            bVar.a = this.a;
            bVar.b = this.b;
            bVar.c = this.c;
            bVar.d = this.d;
            bVar.e = this.e;
            bVar.f = this.f;
            bVar.g = this.g;
            return bVar;
        }

        public Map<String, Object> d(int i) {
            return Map.of("resourceID", Integer.valueOf(i), "entity", Integer.valueOf(this.a), "permanentEntity", Integer.valueOf(this.b));
        }

        public boolean e(Map<?, ?> map, int i) {
            if (i == 1) {
                Integer numE = SessionStateMachine.e(map, "transferPriority");
                Integer numE2 = SessionStateMachine.e(map, "takeConstraint");
                Integer numE3 = SessionStateMachine.e(map, "borrowConstraint");
                if (SessionStateMachine.L(numE) && SessionStateMachine.m(numE2) && SessionStateMachine.m(numE3) && SessionStateMachine.g(map, "unborrowConstraint") && (this.b != 1 || numE.intValue() >= this.d)) {
                    this.a = SessionStateMachine.k;
                    this.b = SessionStateMachine.k;
                    this.c = 0;
                    this.d = numE2.intValue();
                    this.e = numE3.intValue();
                    this.f = 100;
                    this.g = false;
                    return true;
                }
            } else if (i == 3) {
                Integer numE4 = SessionStateMachine.e(map, "transferPriority");
                Integer numE5 = SessionStateMachine.e(map, "unborrowConstraint");
                if (SessionStateMachine.L(numE4) && SessionStateMachine.m(numE5) && SessionStateMachine.g(map, "takeConstraint", "borrowConstraint") && this.b != SessionStateMachine.k) {
                    if (this.a == SessionStateMachine.k) {
                        int i2 = this.c;
                        if (i2 < SessionStateMachine.l) {
                            this.c = i2 + 1;
                            return true;
                        }
                    } else if (numE4.intValue() >= this.e) {
                        this.a = SessionStateMachine.k;
                        this.c = 1;
                        this.f = numE5.intValue();
                        return true;
                    }
                }
            } else if (SessionStateMachine.g(map, "transferPriority", "takeConstraint", "borrowConstraint", "unborrowConstraint")) {
                if (i == SessionStateMachine.k) {
                    if (this.b == SessionStateMachine.k) {
                        this.a = 1;
                        this.b = 1;
                        this.c = 0;
                        this.d = 100;
                        this.e = 100;
                        this.f = 100;
                        this.g = false;
                        return true;
                    }
                } else if (this.a == SessionStateMachine.k && this.b == 1) {
                    int i3 = this.c - 1;
                    this.c = i3;
                    if (i3 == 0) {
                        this.a = 1;
                        this.f = 100;
                    }
                }
                return true;
            }
            return false;
        }

        public void f() {
            if (this.a == SessionStateMachine.k && this.b == SessionStateMachine.k) {
                return;
            }
            this.a = SessionStateMachine.k;
            this.b = SessionStateMachine.k;
            this.c = 0;
            this.d = 100;
            this.e = 100;
            this.f = 100;
            this.g = false;
        }

        public /* synthetic */ b(int i) {
            this();
        }
    }
}
