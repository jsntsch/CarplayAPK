/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   调试开关广播接收器：通过 adb 广播切换 carplayapk-debug.xml 中的实验开关。
 *   所有开关默认 false，可一键回滚，不影响生产路径。
 *   Debug broadcast receiver toggling experimental switches via adb.
 */

package io.carplay.app.prototype;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import io.carplay.app.secured.ControlChannel;

public final class DebugSwitchReceiver extends BroadcastReceiver {
    public static final String ACTION = "io.carplay.app.action.DEBUG_SET_CTRL_WAIT";
    /** [V1.8.33] P1-1 蓝牙 iAP2 诊断开关：extra diag=true/false、ea_drop_19982=true/false */
    public static final String ACTION_IAP2 = "io.carplay.app.action.DEBUG_SET_IAP2";
    /** [V1.8.34] P1-2 主动发起 iAP2-over-WiFi 开关：extra p1_2=true/false */
    public static final String ACTION_P12 = "io.carplay.app.action.DEBUG_SET_P12";
    /** [V1.8.37] 强制发送 CarPlayAvailability(0x4300)：extra enabled=true/false */
    public static final String ACTION_AVAIL = "io.carplay.app.action.DEBUG_SET_AVAIL";
    /** [V1.8.38] 车机发现增强 mDNS 探针 + 单播通告：extra enabled=true/false */
    public static final String ACTION_MDNS = "io.carplay.app.action.DEBUG_SET_CTRL_MDNS";
    /** [V1.8.42] Identification 参数 A/B：extra ea_param10 / ident_strict */
    public static final String ACTION_IDENT = "io.carplay.app.action.DEBUG_SET_IDENT";
    /** [V0.0.1] 调试模式与连接日志：extra mode=true/false、log=true/false（等价于设置页两个开关）。 */
    public static final String ACTION_LOG = "io.carplay.app.action.DEBUG_SET_LOG";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (ACTION.equals(action)) {
            boolean v = intent.getBooleanExtra("enabled", false);
            try {
                context.getSharedPreferences("carplayapk-debug", 0)
                        .edit().putBoolean("ctrl_wait_invitation", v).commit();
                ControlChannel.Log("DEBUG_CTRL_WAIT_SET value=" + v);
            } catch (Throwable t) {
                ControlChannel.Log("DEBUG_CTRL_WAIT_SET_FAILED type=" + t.getClass().getSimpleName());
            }
        } else if (ACTION_IAP2.equals(action)) {
            boolean diag = intent.getBooleanExtra("diag", false);
            boolean eaDrop = intent.getBooleanExtra("ea_drop_19982", false);
            // [V1.8.48] 新增两个单变量 A/B：ea00_index（EA00 param0 编码）、avail_same_source（Availability 同源）。
            // 不传 extra 时保持默认 true（改动生效）；显式 false 回退 V1.8.47 旧行为。
            boolean ea00Index = intent.getBooleanExtra("ea00_index", true);
            boolean availSame = intent.getBooleanExtra("avail_same_source", true);
            // [V1.8.49] EA00 时序 A/B：ea00_after_auth=false 回退 V1.8.48（识别完成即发 0xEA00）。
            boolean ea00AfterAuth = intent.getBooleanExtra("ea00_after_auth", true);
            // [V1.8.51] P0-a：ea00_session_id=false 回退 V1.8.50（EA00 只发 param 0）。
            boolean ea00SessionId = intent.getBooleanExtra("ea00_session_id", true);
            // [V1.8.52] A/B/C：param24_gate（默认 true）、ident_empty（默认 false）、tx_4e0e（默认 false）。
            boolean param24Gate = intent.getBooleanExtra("param24_gate", true);
            boolean identEmpty = intent.getBooleanExtra("ident_empty", false);
            boolean tx4e0e = intent.getBooleanExtra("tx_4e0e", false);
            try {
                context.getSharedPreferences("carplayapk-debug", 0).edit()
                        .putBoolean("iap2_diag", diag)
                        .putBoolean("iap2_ea_drop_19982", eaDrop)
                        .putBoolean("iap2_ea00_index", ea00Index)
                        .putBoolean("iap2_avail_same_source", availSame)
                        .putBoolean("iap2_ea00_after_auth", ea00AfterAuth)
                        .putBoolean("iap2_ea00_session_id", ea00SessionId)
                        .putBoolean("iap2_param24_gate", param24Gate)
                        .putBoolean("iap2_ident_empty", identEmpty)
                        .putBoolean("iap2_tx_4e0e", tx4e0e).commit();
                ControlChannel.Log("DEBUG_IAP2_SET diag=" + diag + " ea_drop_19982=" + eaDrop
                        + " ea00_index=" + ea00Index + " avail_same_source=" + availSame
                        + " ea00_after_auth=" + ea00AfterAuth + " ea00_session_id=" + ea00SessionId
                        + " param24_gate=" + param24Gate + " ident_empty=" + identEmpty
                        + " tx_4e0e=" + tx4e0e);
            } catch (Throwable t) {
                ControlChannel.Log("DEBUG_IAP2_SET_FAILED type=" + t.getClass().getSimpleName());
            }
        } else if (ACTION_P12.equals(action)) {
            boolean p12 = intent.getBooleanExtra("p1_2", false);
            try {
                context.getSharedPreferences("carplayapk-debug", 0).edit()
                        .putBoolean("p1_2_proactive_iap2", p12).commit();
                ControlChannel.Log("DEBUG_P12_SET p1_2_proactive_iap2=" + p12);
            } catch (Throwable t) {
                ControlChannel.Log("DEBUG_P12_SET_FAILED type=" + t.getClass().getSimpleName());
            }
        } else if (ACTION_AVAIL.equals(action)) {
            boolean enabled = intent.getBooleanExtra("enabled", false);
            try {
                context.getSharedPreferences("carplayapk-debug", 0).edit()
                        .putBoolean("p1_3_force_availability", enabled).commit();
                ControlChannel.Log("DEBUG_AVAIL_SET p1_3_force_availability=" + enabled);
            } catch (Throwable t) {
                ControlChannel.Log("DEBUG_AVAIL_SET_FAILED type=" + t.getClass().getSimpleName());
            }
        } else if (ACTION_MDNS.equals(action)) {
            boolean enabled = intent.getBooleanExtra("enabled", false);
            try {
                context.getSharedPreferences("carplayapk-debug", 0).edit()
                        .putBoolean("ctrl_mdns_enhance", enabled).commit();
                ControlChannel.Log("DEBUG_CTRL_MDNS_SET ctrl_mdns_enhance=" + enabled
                        + " effective=immediate");
            } catch (Throwable t) {
                ControlChannel.Log("DEBUG_CTRL_MDNS_SET_FAILED type=" + t.getClass().getSimpleName());
            }
        } else if (ACTION_IDENT.equals(action)) {
            boolean eaParam10 = intent.getBooleanExtra("ea_param10", true);
            boolean identStrict = intent.getBooleanExtra("ident_strict", false);
            try {
                context.getSharedPreferences("carplayapk-debug", 0).edit()
                        .putBoolean("iap2_ea_param10", eaParam10)
                        .putBoolean("iap2_ident_strict", identStrict).commit();
                ControlChannel.Log("DEBUG_IDENT_SET iap2_ea_param10=" + eaParam10
                        + " iap2_ident_strict=" + identStrict);
            } catch (Throwable t) {
                ControlChannel.Log("DEBUG_IDENT_SET_FAILED type=" + t.getClass().getSimpleName());
            }
        } else if (ACTION_LOG.equals(action)) {
            boolean mode = intent.getBooleanExtra("mode", false);
            boolean log = intent.getBooleanExtra("log", false);
            try {
                context.getSharedPreferences("carplayapk-debug", 0).edit()
                        .putBoolean("debug_mode", mode)
                        .putBoolean("log_enabled", log).commit();
                // 进程内静态闸门同步生效（logcat / 连接日志立即开或立即停）。
                ControlChannel.setDebugMode(mode);
                AppLogger.setRecordingEnabled(mode && log);
                ControlChannel.Log("DEBUG_LOG_SET debug_mode=" + mode + " log_enabled=" + log);
            } catch (Throwable t) {
                ControlChannel.Log("DEBUG_LOG_SET_FAILED type=" + t.getClass().getSimpleName());
            }
        }
    }
}
