/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 用 WifiNetworkSpecifier + ConnectivityManager.requestNetwork 连接车机热点。
 * Android 10+ 已废弃 WifiManager.addNetwork/enableNetwork，应用无法直接改系统 WiFi；
 * minSdk=33 只能走这条"应用临时网络"路径（首次会弹系统同意对话框）。
 * 与 PairingSession.D1() 同一套参数：隐藏 SSID、移除 INTERNET capability、45s 超时。
 *
 * Connect the head-unit hotspot on Android 13+ via WifiNetworkSpecifier.
 * WifiManager.addNetwork/enableNetwork are no-ops for apps from Android 10.
 */
package io.carplay.app.prototype;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.SystemClock;
import io.carplay.app.secured.ControlChannel;

final class CarPlayWifiConnector {
    interface Callback {
        void onReady(Network network);

        void onUnavailable(String reason);
    }

    private static final Object LOCK = new Object();
    private static ConnectivityManager.NetworkCallback activeCallback;
    private static volatile Network readyNetwork;
    private static volatile long readySince;

    private CarPlayWifiConnector() {
    }

    static Network readyNetwork() {
        return readyNetwork;
    }

    static long readySince() {
        return readySince;
    }

    static synchronized boolean connect(final Context context,
                                        final CarPlayLinkStore.WifiCredentials cred,
                                        final Callback callback) {
        if (context == null || cred == null || callback == null) {
            return false;
        }
        if (context.checkSelfPermission("android.permission.NEARBY_WIFI_DEVICES") != 0) {
            ControlChannel.Log("AUTO_WIFI_PERMISSION_MISSING name=NEARBY_WIFI_DEVICES");
            AppLogger.g(context, "AUTO_WIFI_PERMISSION_MISSING name=NEARBY_WIFI_DEVICES");
            callback.onUnavailable("NEARBY_WIFI_DEVICES_NOT_GRANTED");
            return false;
        }
        try {
            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder()
                    .setSsid(cred.ssid)
                    // Info3.6 实测热点为隐藏 SSID，与 PairingSession.D1() 保持一致。
                    .setIsHiddenSsid(true);
            if (cred.security == 2 || cred.security == 3) {
                builder.setWpa2Passphrase(cred.passphrase);
            } else if (cred.security == 4) {
                builder.setWpa3Passphrase(cred.passphrase);
            } else if (cred.security != 0) {
                callback.onUnavailable("WIFI_SECURITY_UNSUPPORTED");
                return false;
            }
            NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    // 车机热点无互联网，保留 INTERNET capability 永远等不到 onAvailable。
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(builder.build())
                    .build();
            final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
            if (cm == null) {
                callback.onUnavailable("CONNECTIVITY_MANAGER_UNAVAILABLE");
                return false;
            }
            release(context);
            final ConnectivityManager.NetworkCallback cb = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    readyNetwork = network;
                    readySince = SystemClock.elapsedRealtime();
                    ControlChannel.Log("AUTO_WIFI_AVAILABLE ssid_logged=false");
                    AppLogger.g(context, "AUTO_WIFI_AVAILABLE ssid_logged=false");
                    callback.onReady(network);
                }

                @Override
                public void onUnavailable() {
                    readyNetwork = null;
                    ControlChannel.Log("AUTO_WIFI_UNAVAILABLE system_consent_or_timeout");
                    AppLogger.g(context, "AUTO_WIFI_UNAVAILABLE system_consent_or_timeout");
                    callback.onUnavailable("REQUEST_UNAVAILABLE");
                }

                @Override
                public void onLost(Network network) {
                    readyNetwork = null;
                    ControlChannel.Log("AUTO_WIFI_LOST");
                    AppLogger.g(context, "AUTO_WIFI_LOST");
                }
            };
            synchronized (LOCK) {
                activeCallback = cb;
            }
            // 45s 与配对会话一致；系统可能弹出一次"允许连接"对话框。
            cm.requestNetwork(request, cb, 45000);
            ControlChannel.Log("AUTO_WIFI_REQUEST_SUBMITTED system_consent_may_be_required=true");
            AppLogger.g(context, "AUTO_WIFI_REQUEST_SUBMITTED system_consent_may_be_required=true");
            CarPlayLinkStore.noteWifiAttempt(context, SystemClock.elapsedRealtime());
            return true;
        } catch (RuntimeException e) {
            ControlChannel.Log("AUTO_WIFI_REQUEST_FAILED type=" + e.getClass().getSimpleName());
            AppLogger.g(context, "AUTO_WIFI_REQUEST_FAILED type=" + e.getClass().getSimpleName());
            callback.onUnavailable(e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * 仅按 SSID 连接「已保存」的网络（如车机所连的路由器）。
     * 与 {@link #connect} 不同：此处不提供密码、不标隐藏，由系统复用已保存凭据完成接入。
     * 用于方案 B：当 CarPlayLinkStore.wifi() 为 null（无车机热点凭据）但用户已在 UI 手动选定
     * 车机所在的 WLAN 时，先把手机接入该网络，使 mDNS 发现能找到车机。
     */
    static boolean connectToKnownNetwork(final Context context, final String ssid, final Callback callback) {
        if (context == null || ssid == null || ssid.isEmpty() || callback == null) {
            return false;
        }
        if (context.checkSelfPermission("android.permission.NEARBY_WIFI_DEVICES") != 0) {
            ControlChannel.Log("AUTO_WIFI_PERMISSION_MISSING name=NEARBY_WIFI_DEVICES route=MANUAL");
            AppLogger.g(context, "AUTO_WIFI_PERMISSION_MISSING name=NEARBY_WIFI_DEVICES route=MANUAL");
            callback.onUnavailable("NEARBY_WIFI_DEVICES_NOT_GRANTED");
            return false;
        }
        try {
            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder()
                    .setSsid(ssid);
            NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(builder.build())
                    .build();
            final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
            if (cm == null) {
                callback.onUnavailable("CONNECTIVITY_MANAGER_UNAVAILABLE");
                return false;
            }
            release(context);
            final ConnectivityManager.NetworkCallback cb = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    readyNetwork = network;
                    readySince = SystemClock.elapsedRealtime();
                    ControlChannel.Log("AUTO_WIFI_MANUAL_AVAILABLE ssid_logged=false");
                    AppLogger.g(context, "AUTO_WIFI_MANUAL_AVAILABLE ssid_logged=false");
                    callback.onReady(network);
                }

                @Override
                public void onUnavailable() {
                    readyNetwork = null;
                    ControlChannel.Log("AUTO_WIFI_MANUAL_UNAVAILABLE ssid_logged=false");
                    AppLogger.g(context, "AUTO_WIFI_MANUAL_UNAVAILABLE ssid_logged=false");
                    callback.onUnavailable("REQUEST_UNAVAILABLE");
                }

                @Override
                public void onLost(Network network) {
                    readyNetwork = null;
                    ControlChannel.Log("AUTO_WIFI_MANUAL_LOST");
                    AppLogger.g(context, "AUTO_WIFI_MANUAL_LOST");
                }
            };
            synchronized (LOCK) {
                activeCallback = cb;
            }
            cm.requestNetwork(request, cb, 45000);
            ControlChannel.Log("AUTO_WIFI_MANUAL_REQUEST_SUBMITTED ssid_logged=false");
            AppLogger.g(context, "AUTO_WIFI_MANUAL_REQUEST_SUBMITTED ssid_logged=false");
            CarPlayLinkStore.noteWifiAttempt(context, SystemClock.elapsedRealtime());
            return true;
        } catch (RuntimeException e) {
            ControlChannel.Log("AUTO_WIFI_MANUAL_REQUEST_FAILED type=" + e.getClass().getSimpleName());
            AppLogger.g(context, "AUTO_WIFI_MANUAL_REQUEST_FAILED type=" + e.getClass().getSimpleName());
            callback.onUnavailable(e.getClass().getSimpleName());
            return false;
        }
    }

    static void release(Context context) {
        ConnectivityManager.NetworkCallback cb;
        synchronized (LOCK) {
            cb = activeCallback;
            activeCallback = null;
        }
        if (cb == null || context == null) {
            return;
        }
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
            if (cm != null) {
                cm.unregisterNetworkCallback(cb);
            }
        } catch (RuntimeException ignored) {
            // 已注销时系统抛 IllegalArgumentException，忽略。
        }
    }
}
