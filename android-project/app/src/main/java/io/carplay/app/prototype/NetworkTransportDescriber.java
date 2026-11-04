/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 NetworkTransportDescriber。传输类型描述：把 NetworkCapabilities 翻译成 WIFI / CELLULAR / UNKNOWN。
 *   This file defines NetworkTransportDescriber. Transport describer mapping NetworkCapabilities to WIFI / CELLULAR / UNKNOWN.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.provider.Settings;


/**
 * 传输类型描述：把 NetworkCapabilities 翻译成 WIFI / CELLULAR / UNKNOWN。
 *
 * Transport describer mapping NetworkCapabilities to WIFI / CELLULAR / UNKNOWN.
 */
final class NetworkTransportDescriber {
    private NetworkTransportDescriber() {
    }

    private static void a(StringBuilder sb, NetworkCapabilities networkCapabilities, int i, String str) {
        if (networkCapabilities.hasTransport(i)) {
            if (sb.length() != 0) {
                sb.append('|');
            }
            sb.append(str);
        }
    }

    private static String b(ConnectivityManager connectivityManager, Network network, String str) {
        if (network == null) {
            return str;
        }
        if (connectivityManager == null) {
            return "UNKNOWN";
        }
        try {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities == null) {
                return "UNKNOWN";
            }
            StringBuilder sb = new StringBuilder();
            a(sb, networkCapabilities, 0, "CELLULAR");
            a(sb, networkCapabilities, 1, "WIFI");
            a(sb, networkCapabilities, 2, "BLUETOOTH");
            a(sb, networkCapabilities, 3, "ETHERNET");
            a(sb, networkCapabilities, 4, "VPN");
            if (sb.length() == 0) {
                sb.append("OTHER");
            }
            return "[transport=" + String.valueOf(sb) + ",internet=" + networkCapabilities.hasCapability(12) + ",validated=" + networkCapabilities.hasCapability(16) + ",restricted=" + (true ^ networkCapabilities.hasCapability(13)) + ",vpn=" + networkCapabilities.hasTransport(4) + "]";
        } catch (RuntimeException unused) {
            return "UNKNOWN";
        }
    }

    private static String c(boolean z, Network network, Network network2) {
        return (!z || network2 == null) ? "UNKNOWN" : Boolean.toString(network2.equals(network));
    }

    public static String d(Context context, Network network) {
        String string = "UNKNOWN";
        ConnectivityManager connectivityManager;
        Network activeNetwork;
        boolean z;
        boolean z2 = true;
        try {
            int i = Settings.Global.getInt(context.getContentResolver(), "ocar_force_use_phone_net");
            string = (i == 0 || i == 1) ? Integer.toString(i) : "UNKNOWN";
        } catch (Settings.SettingNotFoundException | RuntimeException unused) {
        }
        Network boundNetworkForProcess = null;
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
        } catch (RuntimeException unused2) {
            connectivityManager = null;
        }
        if (connectivityManager != null) {
            try {
                activeNetwork = connectivityManager.getActiveNetwork();
                z = true;
            } catch (RuntimeException unused3) {
                activeNetwork = null;
                z = false;
            }
        } else {
            activeNetwork = null;
            z = false;
        }
        if (connectivityManager != null) {
            try {
                boundNetworkForProcess = connectivityManager.getBoundNetworkForProcess();
            } catch (RuntimeException unused4) {
                z2 = false;
            }
        } else {
            z2 = false;
        }
        return "NATIVE_NETWORK_SNAPSHOT phone_net_setting=" + string + " default=" + (z ? b(connectivityManager, activeNetwork, "NONE") : "UNKNOWN") + " process_bound=" + (z2 ? b(connectivityManager, boundNetworkForProcess, "SYSTEM_DEFAULT") : "UNKNOWN") + " selected=" + b(connectivityManager, network, "NONE") + " default_is_selected=" + c(z, activeNetwork, network) + " bound_is_selected=" + c(z2, boundNetworkForProcess, network) + " our_process_route_only=true projected_app_route_observed=false";
    }
}
