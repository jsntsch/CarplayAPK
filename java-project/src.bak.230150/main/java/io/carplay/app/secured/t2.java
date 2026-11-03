package io.carplay.app.secured;

import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public final class t2 {
    private t2() {
    }

    public static List<String> a(String str, Throwable th) {
        if (str == null || !str.matches("[A-Z0-9_]{1,48}")) {
            str = "UNKNOWN";
        }
        ArrayList arrayList = new ArrayList();
        int i = 0;
        while (th != null && i < 4) {
            arrayList.add("CONNECT_FAILURE phase=" + str + " cause=" + i + " type=" + th.getClass().getName());
            if (th instanceof EventChannel.d) {
                EventChannel.d dVar = (EventChannel.d) th;
                arrayList.add("CONTROL_RESPONSE_REJECTED op=" + dVar.b + " status=" + dVar.a);
            }
            int i2 = 0;
            for (StackTraceElement stackTraceElement : th.getStackTrace()) {
                String className = stackTraceElement.getClassName();
                if (className.startsWith("io.carplus.") || className.startsWith("org.bouncycastle.") || className.startsWith("java.security.") || className.startsWith("javax.crypto.") || className.startsWith("android.net.")) {
                    int i3 = i2 + 1;
                    if (i2 < 8) {
                        arrayList.add("  at " + className + "." + stackTraceElement.getMethodName() + ":" + stackTraceElement.getLineNumber());
                    }
                    i2 = i3;
                }
            }
            i++;
            th = th.getCause();
        }
        return List.copyOf(arrayList);
    }
}
