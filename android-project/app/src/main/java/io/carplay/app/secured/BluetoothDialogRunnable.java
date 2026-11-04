/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 BluetoothDialogRunnable。蓝牙开启对话框任务：在 UI 线程弹出系统蓝牙请求。
 *   This file defines BluetoothDialogRunnable. Bluetooth enable dialog task posting the system Bluetooth request on the UI thread.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.MainActivity;




/**
 * 蓝牙开启对话框任务：在 UI 线程弹出系统蓝牙请求。
 *
 * Bluetooth enable dialog task posting the system Bluetooth request on the UI thread.
 */
public final class BluetoothDialogRunnable implements Runnable {
    public  MainActivity a;

    @Override 
    public final void run() {
        MainActivity.v170NameDialog(this.a);
    }
}
