package io.carplay.app.secured;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public final class z0 {
    private static final int d = 4096;
    private static final int e = 1024;
    private static final int f = 256;
    private static final int g = 16;
    private static final int h = 10;
    private final Map<Integer, d> a;
    private final boolean b;
    private final Set<Integer> c;

    public static final class f extends w1 {
        private final int a;
        private final int b;
        private final int c;
        private final int d;
        private final int e;
        private final int f;
        private final int g;
        private final int h;
        private final boolean i;

        public f(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, boolean z) {
            this.a = i;
            this.b = i2;
            this.c = i3;
            this.d = i4;
            this.e = i5;
            this.f = i6;
            this.g = i7;
            this.h = i8;
            this.i = z;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof f)) {
                return false;
            }
            f fVar = (f) obj;
            return this.i == fVar.i && this.a == fVar.a && this.b == fVar.b && this.c == fVar.c && this.d == fVar.d && this.e == fVar.e && this.f == fVar.f && this.g == fVar.g && this.h == fVar.h;
        }

        public final int hashCode() {
            boolean z = this.i;
            int i = this.a;
            int i2 = this.b;
            int i3 = this.c;
            int i4 = this.d;
            int i5 = this.e;
            int i6 = this.f;
            int i7 = this.g;
            return (((((((((((((((Boolean.hashCode(z) * 31) + i) * 31) + i2) * 31) + i3) * 31) + i4) * 31) + i5) * 31) + i6) * 31) + i7) * 31) + this.h;
        }

        public int p() {
            return this.b;
        }

        public boolean q() {
            return this.i;
        }

        public int r() {
            return this.f;
        }

        public int s() {
            return this.h;
        }

        public int t() {
            return this.e;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c), Integer.valueOf(this.d), Integer.valueOf(this.e), Integer.valueOf(this.f), Integer.valueOf(this.g), Integer.valueOf(this.h), Boolean.valueOf(this.i)};
            String[] strArrSplit = "a;b;c;d;e;f;g;h;i".length() == 0 ? new String[0] : "a;b;c;d;e;f;g;h;i".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(f.class, sb, "[");
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

        public int u() {
            return this.g;
        }

        public double v() {
            long j = this.c;
            int i = this.e;
            return (j - ((long) i)) / (((long) this.f) - ((long) i));
        }

        public double w() {
            long j = this.d;
            int i = this.g;
            return (j - ((long) i)) / (((long) this.h) - ((long) i));
        }

        public int x() {
            return this.c;
        }

        public int y() {
            return this.d;
        }

        public int z() {
            return this.a;
        }
    }

    private z0(Map<Integer, d> map, boolean z) {
        this.a = Map.copyOf(map);
        this.b = z;
        HashSet hashSet = new HashSet();
        for (Map.Entry<Integer, d> entry : map.entrySet()) {
            if (!entry.getValue().c.isEmpty()) {
                hashSet.add(entry.getKey());
            }
        }
        this.c = Set.copyOf(hashSet);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static IOException c(String str) {
        return new IOException(str);
    }

    private static boolean d(int i, int i2, int i3) {
        long j = i < 0 ? -(1 << (i3 - 1)) : 0L;
        if (i < 0) {
            i3--;
        }
        return ((long) i) >= j && ((long) i2) <= (1 << i3) - 1;
    }

    /* JADX WARN: Failed to calculate best type for var: r10v10 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r10v10 ??, new type: long
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(Unknown Source)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /* JADX WARN: Failed to calculate best type for var: r10v11 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r10v11 ??, new type: long
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(Unknown Source)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /* JADX WARN: Failed to calculate best type for var: r10v12 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r10v12 ??, new type: long
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(Unknown Source)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /* JADX WARN: Failed to calculate best type for var: r10v14 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r10v14 ??, new type: long
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(Unknown Source)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /* JADX WARN: Failed to calculate best type for var: r10v15 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r10v15 ??, new type: long
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(Unknown Source)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /* JADX WARN: Failed to calculate best type for var: r10v16 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r10v16 ??, new type: long
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(Unknown Source)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /* JADX WARN: Failed to calculate best type for var: r10v21 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r10v21 ??, new type: long
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(Unknown Source)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /* JADX WARN: Failed to calculate best type for var: r10v9 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r10v9 ??, new type: long
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.calculateFromBounds(FixTypesVisitor.java:159)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.setBestType(FixTypesVisitor.java:136)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.deduceType(FixTypesVisitor.java:241)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryDeduceTypes(FixTypesVisitor.java:224)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 6 more
     */
    /* JADX WARN: Failed to calculate best type for var: r10v9 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r10v9 ??, new type: long
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(Unknown Source)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /*  JADX ERROR: Types fix failed
        jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r10v9 ??, new type: long
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryPossibleTypes(FixTypesVisitor.java:186)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.deduceType(FixTypesVisitor.java:245)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryDeduceTypes(FixTypesVisitor.java:224)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
        Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
        	... 5 more
        */
    public static io.carplay.app.secured.z0 e(byte[] r39) {
        /*
            Method dump skipped, instruction units count: 1314
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: io.carplay.app.secured.z0.e(byte[]):io.carplay.app.secured.z0");
    }

    private static int g(long j, int i, int i2) throws IOException {
        if (i == 0) {
            throw c("HID_USAGE_RANGE");
        }
        if (i == 4) {
            return (int) j;
        }
        if (j > 65535) {
            throw c("HID_USAGE_RANGE");
        }
        return ((int) j) | (i2 << 16);
    }

    /* JADX WARN: Type inference fix 'apply assigned field type' failed
    java.lang.UnsupportedOperationException: ArgType.getObject(), call class: class jadx.core.dex.instructions.args.ArgType$ArrayArg
    	at jadx.core.dex.instructions.args.ArgType.getObject(ArgType.java:596)
    	at jadx.core.dex.attributes.nodes.ClassTypeVarsAttr.getTypeVarsMapFor(ClassTypeVarsAttr.java:35)
    	at jadx.core.dex.nodes.utils.TypeUtils.replaceClassGenerics(TypeUtils.java:177)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.insertExplicitUseCast(FixTypesVisitor.java:397)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryFieldTypeWithNewCasts(FixTypesVisitor.java:359)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.applyFieldType(FixTypesVisitor.java:309)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
     */
    public f b(byte[] bArr, f fVar) throws IOException {
        b bVar;
        if (bArr == null || bArr.length == 0 || bArr.length > e) {
            throw c("HID_REPORT_LIMIT");
        }
        boolean z = this.b;
        int i = 0;
        int i2 = z ? bArr[0] & 255 : 0;
        d dVar = this.a.get(Integer.valueOf(i2));
        if (dVar == null) {
            throw c("HID_UNKNOWN_REPORT_ID");
        }
        if (dVar.c.isEmpty()) {
            return null;
        }
        if (bArr.length != ((dVar.a + 7) / 8) + (z ? 1 : 0)) {
            throw c("HID_REPORT_LENGTH");
        }
        int iN = 0;
        int i3 = 0;
        e eVar = null;
        for (e eVar2 : dVar.c.values()) {
            if (eVar2.c.n(bArr, z ? 1 : 0) != 0) {
                i++;
                if (i > 1) {
                    throw c("HID_MULTITOUCH_UNSUPPORTED");
                }
                b bVar2 = eVar2.e;
                if ((bVar2 != null && bVar2.n(bArr, z ? 1 : 0) == 0) || ((bVar = eVar2.f) != null && bVar.n(bArr, z ? 1 : 0) == 0)) {
                    throw c("HID_INVALID_CONTACT");
                }
                b bVar3 = eVar2.d;
                iN = bVar3 == null ? i3 : bVar3.n(bArr, z ? 1 : 0);
                eVar = eVar2;
            }
            i3++;
        }
        b bVar4 = dVar.b;
        if (bVar4 != null && bVar4.n(bArr, z ? 1 : 0) != i) {
            throw c("HID_CONTACT_COUNT_MISMATCH");
        }
        if (i == 0) {
            if (fVar != null && fVar.i && fVar.a == i2) {
                return new f(i2, fVar.b, fVar.c, fVar.d, fVar.e, fVar.f, fVar.g, fVar.h, false);
            }
            return null;
        }
        if (fVar == null || !fVar.i || (fVar.a == i2 && fVar.b == iN)) {
            return new f(i2, iN, eVar.a.j(bArr, z ? 1 : 0), eVar.b.j(bArr, z ? 1 : 0), eVar.a.c, eVar.a.d, eVar.b.c, eVar.b.d, true);
        }
        throw c("HID_CONTACT_CHANGED_WITHOUT_UP");
    }

    public Set<Integer> f() {
        return this.c;
    }

    public static final class c {
        int a;
        int b;
        int c;
        int d;
        int e;
        int f;
        boolean g;
        boolean h;

        public /* synthetic */ c(int i) {
            this();
        }

        public c a() {
            c cVar = new c();
            cVar.a = this.a;
            cVar.b = this.b;
            cVar.c = this.c;
            cVar.d = this.d;
            cVar.e = this.e;
            cVar.f = this.f;
            cVar.g = this.g;
            cVar.h = this.h;
            return cVar;
        }

        private c() {
        }
    }

    public static final class e {
        b a;
        b b;
        b c;
        b d;
        b e;
        b f;

        public /* synthetic */ e(int i) {
            this();
        }

        public void a(int i, b bVar) throws IOException {
            switch (i) {
                case 65584:
                    if (this.a != null) {
                        throw z0.c("HID_AMBIGUOUS_AXIS");
                    }
                    this.a = bVar;
                    return;
                case 65585:
                    if (this.b != null) {
                        throw z0.c("HID_AMBIGUOUS_AXIS");
                    }
                    this.b = bVar;
                    return;
                case 852016:
                case 852040:
                case 852041:
                    return;
                case 852018:
                    if (this.f != null) {
                        throw z0.c("HID_AMBIGUOUS_IN_RANGE");
                    }
                    this.f = bVar;
                    return;
                case 852019:
                case 852034:
                    if (this.c != null) {
                        throw z0.c("HID_AMBIGUOUS_TIP");
                    }
                    this.c = bVar;
                    return;
                case 852024:
                case 852049:
                    if (this.d != null) {
                        throw z0.c("HID_AMBIGUOUS_CONTACT_ID");
                    }
                    this.d = bVar;
                    return;
                case 852039:
                    if (this.e != null) {
                        throw z0.c("HID_AMBIGUOUS_CONFIDENCE");
                    }
                    this.e = bVar;
                    return;
                default:
                    throw z0.c("HID_UNSUPPORTED_TOUCH_USAGE");
            }
        }

        public void b() throws IOException {
            if (this.a == null || this.b == null || this.c == null) {
                throw z0.c("HID_INCOMPLETE_CONTACT");
            }
        }

        private e() {
        }
    }

    public static final class a extends w1 {
        private final boolean a;
        private final int b;

        private a(boolean z, int i) {
            this.a = z;
            this.b = i;
        }

        public static /* bridge */ /* synthetic */ int g(a aVar) {
            return aVar.b;
        }

        public static /* bridge */ /* synthetic */ boolean h(a aVar) {
            return aVar.a;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.a == aVar.a && this.b == aVar.b;
        }

        public final int hashCode() {
            boolean z = this.a;
            return (Boolean.hashCode(z) * 31) + this.b;
        }

        public int i() {
            return this.b;
        }

        public boolean j() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Boolean.valueOf(this.a), Integer.valueOf(this.b)};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(a.class, sb, "[");
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

        public /* synthetic */ a(int i, int i2, boolean z) {
            this(z, i);
        }
    }

    public static final class d {
        int a;
        b b;
        final Map<Integer, e> c;

        private d() {
            this.c = new LinkedHashMap();
        }

        public e a(int i) throws IOException {
            e eVar = this.c.get(Integer.valueOf(i));
            if (eVar != null) {
                return eVar;
            }
            if (this.c.size() >= z0.h) {
                throw z0.c("HID_SLOT_LIMIT");
            }
            e eVar2 = new e(0);
            this.c.put(Integer.valueOf(i), eVar2);
            return eVar2;
        }

        public /* synthetic */ d(int i) {
            this();
        }
    }

    public static final class b extends w1 {
        private final int a;
        private final int b;
        private final int c;
        private final int d;

        private b(int i, int i2, int i3, int i4) {
            this.a = i;
            this.b = i2;
            this.c = i3;
            this.d = i4;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && this.b == bVar.b && this.c == bVar.c && this.d == bVar.d;
        }

        public final int hashCode() {
            int i = this.a;
            int i2 = this.b;
            return (((((i * 31) + i2) * 31) + this.c) * 31) + this.d;
        }

        public int i() {
            return this.b;
        }

        public int j(byte[] bArr, int i) throws IOException {
            int iN = n(bArr, i);
            if (iN < this.c || iN > this.d) {
                throw z0.c("HID_COORDINATE_RANGE");
            }
            return iN;
        }

        public int k() {
            return this.d;
        }

        public int l() {
            return this.c;
        }

        public int m() {
            return this.a;
        }

        public int n(byte[] bArr, int i) throws IOException {
            int i2;
            int i3 = 0;
            long j = 0;
            while (true) {
                i2 = this.b;
                if (i3 >= i2) {
                    break;
                }
                int i4 = this.a + i3;
                j |= ((long) ((bArr[(i4 / 8) + i] >>> (i4 % 8)) & 1)) << i3;
                i3++;
            }
            if (this.c < 0 && ((1 << (i2 - 1)) & j) != 0) {
                j -= 1 << i2;
            }
            if (j < -2147483648L || j > 2147483647L) {
                throw z0.c("HID_VALUE_RANGE");
            }
            return (int) j;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c), Integer.valueOf(this.d)};
            String[] strArrSplit = "a;b;c;d".length() == 0 ? new String[0] : "a;b;c;d".split(";");
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

        public /* synthetic */ b(int i, int i2, int i3, int i4, int i5) {
            this(i, i2, i3, i4);
        }
    }
}
