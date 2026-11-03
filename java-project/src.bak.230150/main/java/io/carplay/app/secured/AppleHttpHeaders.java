package io.carplay.app.secured;

import java.util.Map;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
/**
 * AppleHttpHeaders
 *
 * <p>原混淆名：{@code io.carplay.app.secured.xah}（jadx 产物 xah.java）
 *
 * <p>命名依据：X-Apple-Client-Name / X-Apple-ProtocolVersion 注入
 */
public final class AppleHttpHeaders {
    public static volatile boolean LOGGED;

    public static void Apply(Map map) {
        if (map != null) {
            try {
                String strServiceName = ControlChannel.ServiceName();
                String strDeviceMac = ControlChannel.DeviceMac();
                map.put("X-Apple-Client-Name", strServiceName);
                map.put("X-Apple-ProtocolVersion", "1");
                if (strDeviceMac != null) {
                    map.put("X-Apple-Device-ID", strDeviceMac);
                }
                if (LOGGED) {
                    return;
                }
                LOGGED = true;
                ControlChannel.Log("V170_XAPPLE_HEADERS applied");
            } catch (Throwable th) {
            }
        }
    }
}
