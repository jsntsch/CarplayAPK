/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 i3。界面事件监听器：处理该界面上的用户交互。
 *   This file defines i3. UI listener handling user interaction on this screen.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.view.View;
import io.carplay.app.prototype.LogActivity;



/**
 * 界面事件监听器：处理该界面上的用户交互。
 *
 * UI listener handling user interaction on this screen.
 */
public final  class i3 implements View.OnClickListener {
    public final  int a;
    public  LogActivity b;

    public  i3(int i) {
        this.a = i;
    }

    @Override 
    public final void onClick(View view) {
        int i = this.a;
        LogActivity logActivity = this.b;
        switch (i) {
            case 0:
                logActivity.l(view);
                break;
            case AudioSourceCapability.c :
                logActivity.m(view);
                break;
            default:
                logActivity.n(view);
                break;
        }
    }
}
