/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 c4。对话框回调：处理弹窗的点击/显示/关闭事件。
 *   This file defines c4. Dialog callback handling click/show/dismiss events.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.content.DialogInterface;
import io.carplay.app.prototype.MainActivity;



/**
 * 对话框回调：处理弹窗的点击/显示/关闭事件。
 *
 * Dialog callback handling click/show/dismiss events.
 */
public final  class c4 implements DialogInterface.OnDismissListener {
    public  MainActivity a;
    public  boolean b;

    @Override 
    public final void onDismiss(DialogInterface dialogInterface) {
        this.a.y1(this.b, dialogInterface);
    }
}
