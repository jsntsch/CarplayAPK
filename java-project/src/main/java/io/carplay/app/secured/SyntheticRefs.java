package io.carplay.app.secured;

import java.io.IOException;
import java.util.Map;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
/**
 * SyntheticRefs
 *
 * <p>原混淆名：{@code io.carplay.app.secured.d0}（jadx 产物 d0.java）
 *
 * <p>命名依据：R8 合成的方法引用宿主：静态 b/c/d/f 分别是 IllegalArgumentException::new / IOException::new / IllegalStateException::new / r5::new；实例 a(long)=Thread::sleep、e(Map)=AirPlaySession.R0 委托
 */
public final /* synthetic */ class SyntheticRefs implements EventChannelConnector.b, AirPlaySession.l {
    public static /* synthetic */ void b(String str) {
        throw new IllegalArgumentException(str);
    }

    public static /* synthetic */ void c(String str) throws IOException {
        throw new IOException(str);
    }

    public static /* synthetic */ void d(String str) {
        throw new IllegalStateException(str);
    }

    public static /* synthetic */ void f(String str) throws r5 {
        throw new r5(str);
    }

    @Override // io.carplay.app.secured.EventChannelConnector.b
    public void a(long j) throws InterruptedException {
        Thread.sleep(j);
    }

    @Override // io.carplay.app.secured.AirPlaySession.l
    public void e(Map map) {
        AirPlaySession.R0(map);
    }
}
