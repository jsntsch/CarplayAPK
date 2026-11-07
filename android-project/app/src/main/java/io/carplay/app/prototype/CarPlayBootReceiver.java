/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 开机 / 应用更新后拉起常驻自动重连服务。
 * BOOT_COMPLETED 是 Android 12+ 后台启动前台服务的豁免场景之一；
 * 蓝牙 ACL_CONNECTED 不是可靠的静态广播，改由常驻服务动态注册监听。
 *
 * Boot receiver. BOOT_COMPLETED is one of the exemptions that still allows
 * startForegroundService() from the background on Android 12+.
 */
package io.carplay.app.prototype;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import io.carplay.app.secured.ControlChannel;

public final class CarPlayBootReceiver extends BroadcastReceiver {
    private static final String QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";
    private static final String HTC_QUICKBOOT = "com.htc.intent.action.QUICKBOOT_POWERON";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();
        if (action == null) {
            return;
        }
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
                && !QUICKBOOT_POWERON.equals(action)
                && !HTC_QUICKBOOT.equals(action)) {
            return;
        }
        ControlChannel.Log("AUTO_BOOT_RECEIVED action=" + action);
        try {
            CarPlayAutoReconnectService.ensure(context.getApplicationContext(), "BOOT");
        } catch (Throwable t) {
            ControlChannel.Log("AUTO_BOOT_START_FAILED type=" + t.getClass().getSimpleName());
        }
    }
}
