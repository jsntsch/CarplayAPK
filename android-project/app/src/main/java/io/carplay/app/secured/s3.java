/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 s3。对话框回调：处理弹窗的点击/显示/关闭事件。
 *   This file defines s3. Dialog callback handling click/show/dismiss events.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import io.carplay.app.prototype.MainActivity;



/**
 * 对话框回调：处理弹窗的点击/显示/关闭事件。
 *
 * Dialog callback handling click/show/dismiss events.
 */
public final  class s3 implements DialogInterface.OnClickListener {
    public final  int a;
    public  MainActivity b;
    public  Object c;

    public  s3(int i) {
        this.a = i;
    }

    @Override 
    public final void onClick(DialogInterface dialogInterface, int i) {
        switch (this.a) {
            case 0:
                this.b.J1((SharedPreferences) this.c, dialogInterface, i);
                break;
            default:
                this.b.z1((String) this.c, dialogInterface, i);
                break;
        }
    }
}
