/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 v2。界面事件监听器：处理该界面上的用户交互。
 *   This file defines v2. UI listener handling user interaction on this screen.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import io.carplay.app.prototype.HelpActivity;
import io.carplay.app.prototype.MainActivity;



/**
 * 界面事件监听器：处理该界面上的用户交互。
 *
 * UI listener handling user interaction on this screen.
 */
public final  class v2 implements View.OnClickListener {
    public final  int a;
    public  String b;
    public  KeyEvent.Callback c;
    public  KeyEvent.Callback d;

    public  v2(int i) {
        this.a = i;
    }

    @Override 
    public final void onClick(View view) {
        switch (this.a) {
            case 0:
                HelpActivity.f((TextView) this.c, (TextView) this.d, this.b, view);
                break;
            default:
                ((MainActivity) this.c).D1((AlertDialog) this.d, this.b, view);
                break;
        }
    }
}
