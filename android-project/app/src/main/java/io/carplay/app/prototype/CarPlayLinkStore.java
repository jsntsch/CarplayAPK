/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 自动重连的持久化状态：车机蓝牙 MAC、车机下发的 WiFi 凭证、车机 receiver id、
 * 以及按车机 MAC 索引的最近一次 pairing identifier。
 * Persisted state for automatic reconnect: head-unit BT MAC, its Wi-Fi credentials,
 * receiver id and the last pairing identifier keyed by BT MAC.
 *
 * 注意 / Note:
 *   - 文件位于应用私有目录且 AndroidManifest 已设 android:allowBackup="false"，
 *     但 WiFi passphrase 仍是明文存储。真机交付前若需更高等级，应改用
 *     EncryptedSharedPreferences / Android Keystore（本工程离线构建不引入额外依赖）。
 *   - 不记录任何值到日志，只记录"是否存在"与长度，避免凭据外泄。
 */
package io.carplay.app.prototype;

import android.content.Context;
import android.content.SharedPreferences;
import io.carplay.app.secured.ControlChannel;
import io.carplay.app.secured.Iap2Csm;

public final class CarPlayLinkStore {
    private static final String PREFS = "carplay-auto-link";
    private static final String UI_PREFS = "wireless-ui";

    private static final String KEY_ENABLED = "auto_reconnect_enabled";
    private static final String KEY_BT_MAC = "head_unit_bt_mac";
    private static final String KEY_SSID = "head_unit_wifi_ssid";
    private static final String KEY_PASSPHRASE = "head_unit_wifi_passphrase";
    private static final String KEY_SECURITY = "head_unit_wifi_security";
    private static final String KEY_RECEIVER_ID = "airplay_receiver_device_id";
    private static final String KEY_LAST_PAIR_PREFIX = "last_pairing_identifier_";
    private static final String KEY_PAIR_FALLBACKS = "pair_verify_fallback_count";
    private static final String KEY_LAST_WIFI_ATTEMPT = "last_wifi_attempt_at";
    // 方案 B：用户在主页「车机→WLAN」手动选定的车机所在 WLAN（保存在 wireless-ui）。
    private static final String KEY_MANUAL_WLAN_SSID = "manual_wlan_ssid";

    private CarPlayLinkStore() {
    }

    private static SharedPreferences p(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, 0);
    }

    /** 车机侧 devid 等价物：来自 GET /ctrl-int/1/connect 的 AirPlay-Receiver-Device-ID。 */
    public static void saveReceiverId(Context context, String receiverId) {
        if (context == null || receiverId == null || receiverId.isEmpty()) {
            return;
        }
        p(context).edit().putString(KEY_RECEIVER_ID, receiverId).apply();
        ControlChannel.Log("AUTO_LINK_RECEIVER_ID_SAVED present=true len=" + receiverId.length());
    }

    public static String receiverId(Context context) {
        return context == null ? null : p(context).getString(KEY_RECEIVER_ID, null);
    }

    /** 保存 iAP2 下发的车机热点凭证（配对成功时由 WirelessCarPlayService.t() 调用）。 */
    public static void saveWifi(Context context, String btMac, Iap2Csm.g credentials) {
        if (context == null || credentials == null) {
            return;
        }
        String passphrase = credentials.c();
        p(context).edit()
                .putString(KEY_BT_MAC, btMac)
                .putString(KEY_SSID, credentials.a)
                .putString(KEY_PASSPHRASE, passphrase)
                .putInt(KEY_SECURITY, credentials.b)
                .apply();
        ControlChannel.Log("AUTO_LINK_WIFI_SAVED ssid_present=" + (credentials.a != null && !credentials.a.isEmpty())
                + " security=" + credentials.b + " passphrase_present=" + (passphrase != null && !passphrase.isEmpty())
                + " values_logged=false");
        AppLogger.g(context.getApplicationContext(), "AUTO_LINK_WIFI_SAVED security=" + credentials.b);
    }

    public static final class WifiCredentials {
        public final String ssid;
        public final String passphrase;
        public final int security;
        public final String btMac;

        WifiCredentials(String ssid, String passphrase, int security, String btMac) {
            this.ssid = ssid;
            this.passphrase = passphrase;
            this.security = security;
            this.btMac = btMac;
        }
    }

    public static WifiCredentials wifi(Context context) {
        if (context == null) {
            return null;
        }
        SharedPreferences sp = p(context);
        String ssid = sp.getString(KEY_SSID, null);
        if (ssid == null || ssid.isEmpty()) {
            return null;
        }
        return new WifiCredentials(ssid, sp.getString(KEY_PASSPHRASE, ""),
                sp.getInt(KEY_SECURITY, 0), sp.getString(KEY_BT_MAC, null));
    }

    /** 用户在 UI 上最后选择的车机蓝牙 MAC；自动重连以此为准。 */
    public static String selectedAddress(Context context) {
        if (context == null) {
            return null;
        }
        String selected = context.getApplicationContext()
                .getSharedPreferences(UI_PREFS, 0).getString("selected_address", null);
        if (selected != null && !selected.isEmpty()) {
            return selected;
        }
        return p(context).getString(KEY_BT_MAC, null);
    }

    /**
     * 方案 B：读取用户手动选定的车机 WLAN（SSID）。
     * 用于 CarPlayLinkStore.wifi() 为 null（iAP2 从未下发车机热点凭证，如车机接路由器）时，
     * 让自动重连服务也能把手机接入车机所在网络后再发起 CarPlay 会话。
     */
    public static String manualWlanSsid(Context context) {
        if (context == null) {
            return null;
        }
        String ssid = context.getApplicationContext()
                .getSharedPreferences(UI_PREFS, 0).getString(KEY_MANUAL_WLAN_SSID, null);
        return (ssid == null || ssid.trim().isEmpty()) ? null : ssid.trim();
    }

    public static boolean enabled(Context context) {
        return context != null && p(context).getBoolean(KEY_ENABLED, true);
    }

    public static void setEnabled(Context context, boolean enabled) {
        if (context != null) {
            p(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
        }
    }

    /** pair-verify 复用失败时按车机 MAC 记下诊断信息，不删除既有配对记录。 */
    public static void notePairVerifyFallback(Context context, String btMac) {
        if (context == null) {
            return;
        }
        SharedPreferences sp = p(context);
        sp.edit().putInt(KEY_PAIR_FALLBACKS, sp.getInt(KEY_PAIR_FALLBACKS, 0) + 1).apply();
        ControlChannel.Log("AUTO_LINK_PAIR_VERIFY_FALLBACK mac_present=" + (btMac != null));
    }

    public static int pairVerifyFallbacks(Context context) {
        return context == null ? 0 : p(context).getInt(KEY_PAIR_FALLBACKS, 0);
    }

    public static void saveLastPairingIdentifier(Context context, String btMac, String identifier) {
        if (context == null || btMac == null || identifier == null || identifier.isEmpty()) {
            return;
        }
        p(context).edit().putString(KEY_LAST_PAIR_PREFIX + btMac.replace(":", ""), identifier).apply();
    }

    public static String lastPairingIdentifier(Context context, String btMac) {
        if (context == null || btMac == null) {
            return null;
        }
        return p(context).getString(KEY_LAST_PAIR_PREFIX + btMac.replace(":", ""), null);
    }

    /** 60 秒冷却，避免开机/前台反复触发 WiFi 请求弹窗。 */
    public static boolean wifiAttemptAllowed(Context context, long nowElapsed) {
        if (context == null) {
            return false;
        }
        long last = p(context).getLong(KEY_LAST_WIFI_ATTEMPT, -1L);
        return last < 0 || nowElapsed - last >= 60000;
    }

    public static void noteWifiAttempt(Context context, long nowElapsed) {
        if (context != null) {
            p(context).edit().putLong(KEY_LAST_WIFI_ATTEMPT, nowElapsed).apply();
        }
    }
}
