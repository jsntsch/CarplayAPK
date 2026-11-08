/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 常驻自动重连前台服务。
 *
 * 解决的问题（对照固件侧 CarPlayControlClient）：车机点火后是连接发起方——
 * 它恢复已配对手机表、browse _carplay-ctrl、发 GET /ctrl-int/1/connect。
 * 本 App 原先是普通前台应用：没有 receiver、START_NOT_STICKY、ctrl 广播只在
 * MainActivity.onResume 注册，车机单方面回连时手机侧根本没有进程在监听。
 *
 * 本服务做三件事：
 *   1) 常驻（START_STICKY + 开机豁免），动态监听蓝牙 ACL_CONNECTED；
 *   2) 匹配已保存车机 MAC 后，用 WifiNetworkSpecifier 自动连车机热点；
 *   3) 连上后重新注册 _carplay-ctrl 广播并自动发起 CarPlay 会话；
 *      车机主动发来 ctrl connect 邀请时也会自动起会话。
 *
 * Android 平台约束 / Platform constraints (minSdk 33, targetSdk 37):
 *   - Android 12+ 禁止后台 startForegroundService，BOOT_COMPLETED 是豁免之一；
 *     蓝牙 ACL 广播不可靠地静态注册，故在常驻服务里动态注册。
 *   - Android 10+ 应用无法用 WifiManager.addNetwork/enableNetwork 改系统 WiFi，
 *     只能走 WifiNetworkSpecifier + requestNetwork（首次可能弹系统同意框）。
 *   - 前台服务类型 connectedDevice 已在 AndroidManifest 声明；API 34+ 用三参
 *     startForeground 显式传该 type，API 33 用两参。
 */
package io.carplay.app.prototype;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import io.carplay.app.secured.ControlChannel;

public final class CarPlayAutoReconnectService extends Service {
    private static final String CHANNEL = "carplay-auto-link";
    private static final int NOTIFICATION_ID = 75;

    public static final String ACTION_ENSURE = "io.carplay.app.prototype.AUTO_ENSURE";
    public static final String ACTION_CTRL_INVITATION = "io.carplay.app.prototype.AUTO_CTRL_INVITATION";
    public static final String EXTRA_REASON = "reason";

    private boolean receiverRegistered;
    private ConnectivityManager.NetworkCallback wifiMonitor;
    private boolean wifiMonitorRegistered;
    private android.os.Handler invitationHandler;

    /** [V1.8.32 P0-1] 开关开启时让「等车机 ctrl 回连」成为主路径：先播 ctrl，8s 内收到邀请即起会话，超时兜底主动连。 */
    private void scheduleInvitationTimeout(final String reason) {
        if (invitationHandler == null) {
            invitationHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        }
        invitationHandler.removeCallbacksAndMessages(null);
        invitationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (WirelessCarPlayService.L1()) {
                    ControlChannel.Log("AUTO_WAIT_INVITATION_SKIP_FALLBACK reason=SESSION_ALREADY_RUNNING");
                    return;
                }
                ControlChannel.Log("AUTO_WAIT_INVITATION_TIMEOUT fallback=maybeConnectWifi reason=" + reason);
                maybeConnectWifi(reason == null ? "INVITATION_TIMEOUT" : reason);
            }
        }, 8000L);
    }

    /** 最近一次匹配到车机蓝牙 ACL_CONNECTED 的时间；WiFi 监听只在这个窗口内生效，避免连家里 WiFi 误触发。 */
    private volatile long lastHeadUnitBtAt = -1L;

    /** 从任意前台场景（App 打开 / 开机广播）拉起常驻服务。 */
    public static void ensure(Context context, String reason) {
        if (context == null) {
            return;
        }
        try {
            Intent intent = new Intent(context.getApplicationContext(), CarPlayAutoReconnectService.class)
                    .setAction(ACTION_ENSURE).putExtra(EXTRA_REASON, reason);
            context.getApplicationContext().startForegroundService(intent);
            ControlChannel.Log("AUTO_SERVICE_ENSURE_REQUESTED reason=" + reason);
        } catch (Throwable t) {
            ControlChannel.Log("AUTO_SERVICE_ENSURE_FAILED reason=" + reason
                    + " type=" + t.getClass().getSimpleName());
        }
    }

    /** 车机 GET /ctrl-int/1/connect 到达时由 CarPlayCtrlAdvertiser.StartEarly 的回调调用。 */
    public static void onCtrlInvitation(Context context) {
        if (context == null) {
            return;
        }
        ControlChannel.Log("AUTO_CTRL_INVITATION_RECEIVED");
        AppLogger.g(context.getApplicationContext(), "AUTO_CTRL_INVITATION_RECEIVED");
        try {
            context.getApplicationContext().startForegroundService(
                    new Intent(context.getApplicationContext(), CarPlayAutoReconnectService.class)
                            .setAction(ACTION_CTRL_INVITATION));
        } catch (Throwable t) {
            ControlChannel.Log("AUTO_CTRL_INVITATION_START_FAILED type=" + t.getClass().getSimpleName());
        }
    }

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())) {
                return;
            }
            if (checkSelfPermission("android.permission.BLUETOOTH_CONNECT") != 0) {
                ControlChannel.Log("AUTO_BT_PERMISSION_MISSING name=BLUETOOTH_CONNECT");
                return;
            }
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String mac = device == null ? null : device.getAddress();
            handleBluetoothConnected(mac);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        ControlChannel.APP_CTX = getApplicationContext();
        NotificationManager nm = (NotificationManager) getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(new NotificationChannel(CHANNEL, "CarPlay 自动重连",
                    NotificationManager.IMPORTANCE_LOW));
        }
        Notification notification = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("CarPlay 自动重连")
                .setContentText("等待车机蓝牙与 WiFi")
                .setOngoing(true)
                .build();
        try {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(NOTIFICATION_ID, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (Throwable t) {
            ControlChannel.Log("AUTO_SERVICE_FOREGROUND_FAILED type=" + t.getClass().getSimpleName());
        }
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            registerReceiver(btReceiver, filter);
            receiverRegistered = true;
            ControlChannel.Log("AUTO_BT_RECEIVER_REGISTERED acl_connected=true");
        } catch (Throwable t) {
            ControlChannel.Log("AUTO_BT_RECEIVER_FAILED type=" + t.getClass().getSimpleName());
        }
        // Info3.6 实测 wifi_request=false：车机可能从不下发热点凭证，但系统里已保存过该车机热点。
        // 此时蓝牙一连上，系统自己会回连 WiFi；监听 WiFi 可用即可接着重连 CarPlay。
        try {
            final ConnectivityManager cm = (ConnectivityManager) getSystemService(ConnectivityManager.class);
            if (cm != null) {
                wifiMonitor = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(final Network network) {
                        handleWifiBecameAvailable(network);
                    }
                };
                cm.registerNetworkCallback(new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(), wifiMonitor);
                wifiMonitorRegistered = true;
                ControlChannel.Log("AUTO_WIFI_MONITOR_REGISTERED");
            }
        } catch (Throwable t) {
            ControlChannel.Log("AUTO_WIFI_MONITOR_FAILED type=" + t.getClass().getSimpleName());
        }
        AppLogger.g(this, "AUTO_SERVICE_CREATED sticky=true");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_ENSURE : intent.getAction();
        if (action == null) {
            action = ACTION_ENSURE;
        }
        String reason = intent == null ? "STICKY_RESTART" : intent.getStringExtra(EXTRA_REASON);
        ControlChannel.Log("AUTO_SERVICE_COMMAND action=" + action + " reason=" + reason);
        AppLogger.g(this, "AUTO_SERVICE_COMMAND action=" + action + " reason=" + reason);
        if (ACTION_CTRL_INVITATION.equals(action)) {
            startSession(CarPlayWifiConnector.readyNetwork());
        } else {
            handleEnsure(reason);
        }
        // START_STICKY：进程被杀后系统重建会带 null intent，我们当作 ENSURE 处理。
        return START_STICKY;
    }

    private void handleEnsure(String reason) {
        if (!CarPlayLinkStore.enabled(this)) {
            ControlChannel.Log("AUTO_SERVICE_DISABLED");
            return;
        }
        String selected = CarPlayLinkStore.selectedAddress(this);
        if (selected == null || !BluetoothAdapter.checkBluetoothAddress(selected)) {
            ControlChannel.Log("AUTO_ENSURE_SKIPPED reason=NO_SELECTED_RECEIVER");
            return;
        }
        // 只要当前已有 WiFi，就把 ctrl 广播挂上（StartEarly 内部幂等）。
        CarPlayCtrlAdvertiser.StartEarly(this);
        if (ControlChannel.CtrlWaitInvitationEnabled()) {
            // [V1.8.32 P0-1] 让「等车机 ctrl 回连」成为主路径：先播 ctrl，等 GET /ctrl-int/1/connect
            // （AUTO_CTRL_INVITATION_RECEIVED → startSession）；8s 内未收到邀请则兜底主动连，
            // 确保不丢失已稳定的投屏能力。
            ControlChannel.Log("AUTO_WAIT_INVITATION_ENABLED timeout_ms=8000 reason=" + reason);
            scheduleInvitationTimeout(reason);
            return;
        }
        maybeConnectWifi(reason == null ? "ENSURE" : reason);
    }

    private void handleBluetoothConnected(String mac) {
        String selected = CarPlayLinkStore.selectedAddress(this);
        if (selected == null || mac == null) {
            ControlChannel.Log("AUTO_BT_IGNORED reason=NO_SELECTED_OR_MAC");
            return;
        }
        if (!mac.equalsIgnoreCase(selected)) {
            ControlChannel.Log("AUTO_BT_IGNORED reason=NOT_HEAD_UNIT match=false");
            return;
        }
        ControlChannel.Log("AUTO_BT_HEAD_UNIT_CONNECTED match=true");
        AppLogger.g(this, "AUTO_BT_HEAD_UNIT_CONNECTED match=true");
        lastHeadUnitBtAt = SystemClock.elapsedRealtime();
        if (!CarPlayLinkStore.enabled(this)) {
            return;
        }
        maybeConnectWifi("BT_ACL_CONNECTED");
    }

    private void maybeConnectWifi(final String reason) {
        long now = SystemClock.elapsedRealtime();
        if (!CarPlayLinkStore.wifiAttemptAllowed(this, now)) {
            ControlChannel.Log("AUTO_WIFI_SKIPPED reason=COOLDOWN trigger=" + reason);
            return;
        }
        final CarPlayLinkStore.WifiCredentials cred = CarPlayLinkStore.wifi(this);
        if (cred == null) {
            // 方案 B：iAP2 从未下发车机热点凭证（车机接路由器时本就不会下发）。
            // 若用户已手动选定车机所在 WLAN，则接入/复用该网络并发起会话，而不是干等 ctrl 邀请。
            String manual = CarPlayLinkStore.manualWlanSsid(this);
            if (manual != null) {
                maybeConnectManualWlan(manual, reason);
                return;
            }
            // Info3.6 实测 wifi_request=false，可能从未通过 iAP2 拿到凭证。
            // 此时不能硬连，但保持 ctrl 广播，让车机从自己那一侧发起 connect。
            ControlChannel.Log("AUTO_WIFI_SKIPPED reason=NO_SAVED_CREDENTIALS trigger=" + reason
                    + " ctrl_advertiser_retained=true");
            AppLogger.g(this, "AUTO_WIFI_SKIPPED reason=NO_SAVED_CREDENTIALS");
            return;
        }
        ControlChannel.Log("AUTO_WIFI_CONNECTING trigger=" + reason + " ssid_logged=false");
        CarPlayWifiConnector.connect(this, cred, new CarPlayWifiConnector.Callback() {
            @Override
            public void onReady(final Network network) {
                restartCtrlAndStartSessionAsync(network);
            }

            @Override
            public void onUnavailable(String why) {
                ControlChannel.Log("AUTO_WIFI_NOT_READY reason=" + why
                        + " fallback=wait_ctrl_invitation");
            }
        });
    }

    /**
     * 方案 B：无 iAP2 下发的车机热点凭证时，用用户手动选定的车机 WLAN 兜底。
     * 车机接路由器时手机与车机在同一 LAN，只要手机在该 WLAN 上即可直接起会话；
     * 不在则按 SSID 请求系统复用「已保存网络」接入（不填密码，由系统用已保存凭据）。
     */
    private void maybeConnectManualWlan(final String ssid, final String reason) {
        String current = currentSsid();
        if (ssid.equalsIgnoreCase(current)) {
            ControlChannel.Log("AUTO_WIFI_MANUAL_ALREADY_ON_SSID ssid_logged=false trigger=" + reason);
            AppLogger.g(this, "AUTO_WIFI_MANUAL_ALREADY_ON_SSID trigger=" + reason);
            Network wifi = null;
            try {
                wifi = WifiNetworkMonitor.B(this);
            } catch (RuntimeException ignored) {
            }
            restartCtrlAndStartSessionAsync(wifi);
            return;
        }
        ControlChannel.Log("AUTO_WIFI_MANUAL_CONNECTING ssid_logged=false trigger=" + reason);
        AppLogger.g(this, "AUTO_WIFI_MANUAL_CONNECTING trigger=" + reason);
        CarPlayWifiConnector.connectToKnownNetwork(this, ssid, new CarPlayWifiConnector.Callback() {
            @Override
            public void onReady(final Network network) {
                restartCtrlAndStartSessionAsync(network);
            }

            @Override
            public void onUnavailable(String why) {
                ControlChannel.Log("AUTO_WIFI_MANUAL_NOT_READY reason=" + why
                        + " fallback=ctrl_advertiser_retained");
                AppLogger.g(CarPlayAutoReconnectService.this, "AUTO_WIFI_MANUAL_NOT_READY reason=" + why);
            }
        });
    }

    /** 读取当前已连接 WiFi 的 SSID（去引号、不可读值归 null），供方案 B 判断是否已在目标网络。 */
    private String currentSsid() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WifiManager.class);
            if (wm == null) return null;
            WifiInfo info = wm.getConnectionInfo();
            if (info == null) return null;
            String s = info.getSSID();
            if (s == null) return null;
            if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
                s = s.substring(1, s.length() - 1);
            }
            if (s.trim().isEmpty() || s.equals("<unknown ssid>")) return null;
            return s;
        } catch (RuntimeException unused) {
            return null;
        }
    }

    /** 系统自己回连到已保存的车机热点时（App 没有凭证），在蓝牙事件窗口内接着重连 CarPlay。 */
    private void handleWifiBecameAvailable(final Network network) {
        long bt = lastHeadUnitBtAt;
        boolean inBtWindow = bt >= 0 && SystemClock.elapsedRealtime() - bt <= 300000L;
        if (!inBtWindow) {
            ControlChannel.Log("AUTO_WIFI_MONITOR_SKIPPED reason=NO_RECENT_HEAD_UNIT_BT");
            return;
        }
        ControlChannel.Log("AUTO_WIFI_MONITOR_READY source=SYSTEM_SAVED_NETWORK");
        AppLogger.g(this, "AUTO_WIFI_MONITOR_READY source=SYSTEM_SAVED_NETWORK");
        restartCtrlAndStartSessionAsync(network);
    }

    /** StopEarly 内部等 mDNS 注销 latch（最多 400ms），放工作线程避免卡主线程。 */
    private void restartCtrlAndStartSessionAsync(final Network network) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                restartCtrlAndStartSession(network);
            }
        }, "CarPlayAPK-auto-wifi-ready").start();
    }

    /** synchronized：两条 WiFi 触发路径串行，避免 StopEarly/StartEarly 并发竞态。 */
    private synchronized void restartCtrlAndStartSession(Network network) {
        CarPlayCtrlAdvertiser.StopEarly();
        CarPlayCtrlAdvertiser.StartEarly(this);
        startSession(network);
    }

    private void startSession(Network network) {
        String mac = CarPlayLinkStore.selectedAddress(this);
        if (mac == null || !BluetoothAdapter.checkBluetoothAddress(mac)) {
            ControlChannel.Log("AUTO_SESSION_SKIPPED reason=NO_SELECTED_RECEIVER");
            return;
        }
        if (WirelessCarPlayService.L1()) {
            ControlChannel.Log("AUTO_SESSION_ALREADY_RUNNING ctrl_advertiser_retained=true");
            return;
        }
        Intent intent = new Intent(this, WirelessCarPlayService.class)
                .setAction(WirelessCarPlayService.V0)
                .putExtra("selectedBtAddress", mac);
        if (network != null) {
            intent.putExtra("useCurrentWifi", true);
            intent.putExtra("currentWifiHandle", network.getNetworkHandle());
        }
        try {
            startForegroundService(intent);
            ControlChannel.Log("AUTO_SESSION_START_REQUESTED use_current_wifi=" + (network != null));
            AppLogger.g(this, "AUTO_SESSION_START_REQUESTED use_current_wifi=" + (network != null));
        } catch (Throwable t) {
            ControlChannel.Log("AUTO_SESSION_START_FAILED type=" + t.getClass().getSimpleName());
            AppLogger.g(this, "AUTO_SESSION_START_FAILED type=" + t.getClass().getSimpleName());
        }
    }

    @Override
    public void onDestroy() {
        if (receiverRegistered) {
            try {
                unregisterReceiver(btReceiver);
            } catch (Throwable ignored) {
            }
            receiverRegistered = false;
        }
        if (wifiMonitorRegistered) {
            try {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(ConnectivityManager.class);
                if (cm != null) {
                    cm.unregisterNetworkCallback(wifiMonitor);
                }
            } catch (Throwable ignored) {
            }
            wifiMonitorRegistered = false;
        }
        ControlChannel.Log("AUTO_SERVICE_DESTROYED");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
