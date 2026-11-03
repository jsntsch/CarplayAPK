package io.carplay.app.secured;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
final class a0 {
    private static final Set<String> b = Set.of("requestUI", "requestSiri", "changeModes", "modesChanged", "forceKeyFrame", "hidSendReport", "hidSetInputMode", "setHIDInputMode", "updateVehicleInformation", "setNightMode", "setLimitedUI", "updateViewArea", "setViewArea", "setUIContext", "uiContextChanged", "setAppearance", "setInputMode", "setProximity", "setOrientation", "setProperty", "setRate", "duckAudio", "unduckAudio", "setProxyParameters", "disableBluetooth", "suggestUI", "changeVideoPlaybackToAudioOnly", "iAPSendMessage", "setAudioLinkQuality", "setCarPlayState");
    private static final List<String> c = List.of("url", "urls", "hidInputMode", "uuid", "data", "streams", "uiContext", "context", "nightMode", "limitedUI", "limitedUIElements", "viewArea", "viewAreaIndex", "screenUUID", "width", "height", "x", "y", "mode", "state", "status", "resources", "appStates", "reasonStr", "deviceID", "value", "name", "requestID", "commandID", "durationMs", "volume", "siriAction");
    private final Set<String> a = new HashSet();

    private static String a(String str) {
        try {
            byte[] bArrDigest = MessageDigest.getInstance("SHA-256").digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {
                sb.append(Character.forDigit((bArrDigest[i] >>> 4) & 15, 16));
                sb.append(Character.forDigit(bArrDigest[i] & 15, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException unused) {
            return "HASH_UNAVAILABLE";
        }
    }

    private static int b(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Boolean) {
            return 1;
        }
        if ((obj instanceof Byte) || (obj instanceof Short) || (obj instanceof Integer) || (obj instanceof Long) || (obj instanceof BigInteger)) {
            return 2;
        }
        if (obj instanceof Number) {
            return 3;
        }
        if (obj instanceof String) {
            return 4;
        }
        if (obj instanceof byte[]) {
            return 5;
        }
        if (obj instanceof List) {
            return 6;
        }
        return obj instanceof Map ? 7 : 8;
    }

    public synchronized String c(Map<String, Object> map, int i, String str) {
        String str2;
        String str3;
        try {
            if (this.a.size() >= 32) {
                return null;
            }
            Object obj = map.get("type");
            if (obj instanceof String) {
                str2 = (String) obj;
                if (!b.contains(str2)) {
                    str2 = "UNKNOWN_" + a(str2);
                }
            } else {
                str2 = obj == null ? "MISSING" : "INVALID";
            }
            Object obj2 = map.get("params");
            String str4 = "params_type=" + b(obj2);
            if (obj2 instanceof Map) {
                Map map2 = (Map) obj2;
                long j = 0;
                int i2 = 0;
                int i3 = 0;
                long jB = 0;
                while (true) {
                    List<String> list = c;
                    if (i2 >= list.size()) {
                        break;
                    }
                    if (map2.containsKey(list.get(i2))) {
                        j |= 1 << i2;
                        i3++;
                        jB |= 1 << b(map2.get(list.get(i2)));
                    }
                    i2++;
                }
                str4 = str4 + " params_known_mask=" + Long.toHexString(j) + " params_types_mask=" + Long.toHexString(jB) + " params_unknown_fields=" + (map2.size() - i3);
            }
            if ("CONTROL".equals(str)) {
                str3 = "CONTROL";
            } else {
                str3 = "EVENT".equals(str) ? "EVENT" : "UNSPECIFIED";
            }
            if (!this.a.add(str2 + " " + str4 + " carrier=" + str3)) {
                return null;
            }
            return "CARPLAY_COMMAND_SCHEMA kind=" + str2 + " " + str4 + " carrier=" + str3 + " body_bytes=" + Math.max(0, Math.min(i, o5.c)) + " values_logged=false";
        } catch (Throwable th) {
            throw th;
        }
    }
}
