package io.carplay.app.secured;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public final class w4 {
    private final VideoCipher.b a = new VideoCipher.b();
    private volatile AirPlaySession.g b;

    @FunctionalInterface
    public interface a {
        void a(byte[] bArr);
    }

    @FunctionalInterface
    public interface b {
        void b(h hVar);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void f(a aVar, h hVar) {
        byte[] bArrK = hVar.k();
        byte[] bArrH = hVar.h();
        aVar.a(i.b((bArrK == null || bArrH == null) ? null : i.c(List.of(bArrK, bArrH)), hVar.a(), hVar.e()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void g(b bVar, List list, long j, long j2, long j3) {
        byte[] bArrM = this.a.m();
        byte[] bArrI = this.a.i();
        if (bArrM != null && bArrI != null) {
            this.b = new AirPlaySession.g(bArrM, bArrI);
        }
        bVar.b(new h(list, bArrM, bArrI, list.stream().anyMatch(new w0(20)), j, j2, j3));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean h(byte[] bArr) {
        return (bArr[0] & 31) == 5;
    }

    public synchronized void d(byte[] bArr, a aVar) throws Throwable {
        try {
            try {
                long jNanoTime = System.nanoTime();
                k1 k1Var = new k1(1);
                k1Var.b = aVar;
                VarHandle.storeStoreFence();
                e(bArr, 0L, jNanoTime, 0L, k1Var);
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            throw th;
        }
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
    public synchronized void e(byte[] bArr, long j, long j2, long j3, b bVar) {
        if (j3 < 0) {
            throw new IOException("native AVC generation invalid");
        }
        try {
            VideoCipher.b bVar2 = this.a;
            z2 z2Var = new z2();
            z2Var.a = this;
            z2Var.b = bVar;
            VarHandle.storeStoreFence();
            bVar2.c(bArr, j, j2, j3, z2Var);
            byte[] bArrM = this.a.m();
            byte[] bArrI = this.a.i();
            if (bArrM != null && bArrI != null) {
                this.b = new AirPlaySession.g(bArrM, bArrI);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e2) {
            throw new IOException("native AVC unit parsing failed", e2);
        }
    }

    public AirPlaySession.g i() {
        return this.b;
    }

    public synchronized void j() {
        this.a.k();
        this.b = null;
    }
}
