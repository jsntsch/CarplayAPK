/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   调试开关广播接收器：通过 adb 广播切换反向通道调试总开关（carplayapk-debug.xml 的
 *   ctrl_wait_invitation），默认 false，可一键回滚，不影响生产路径。
 *   Debug broadcast receiver toggling the reverse-channel debug master switch via adb.
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
            try {
                context.getSharedPreferences("carplayapk-debug", 0).edit()
                        .putBoolean("iap2_diag", diag)
                        .putBoolean("iap2_ea_drop_19982", eaDrop).commit();
                ControlChannel.Log("DEBUG_IAP2_SET diag=" + diag + " ea_drop_19982=" + eaDrop);
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
        }
    }
}
