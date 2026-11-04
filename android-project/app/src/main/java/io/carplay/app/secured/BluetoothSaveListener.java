/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 BluetoothSaveListener。蓝牙名称保存回调：处理「保存设备名」对话框的确认动作。
 *   This file defines BluetoothSaveListener. Bluetooth name save listener handling confirmation of the rename dialog.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.content.DialogInterface;
import android.widget.EditText;
import io.carplay.app.prototype.MainActivity;




/**
 * 蓝牙名称保存回调：处理「保存设备名」对话框的确认动作。
 *
 * Bluetooth name save listener handling confirmation of the rename dialog.
 */
public final class BluetoothSaveListener implements DialogInterface.OnClickListener {
    public  MainActivity a;
    public  EditText b;

    @Override 
    public final void onClick(DialogInterface dialogInterface, int i) {
        MainActivity.v170SaveName(this.a, this.b);
    }
}
