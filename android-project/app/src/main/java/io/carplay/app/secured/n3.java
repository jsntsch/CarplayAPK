/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 n3。界面事件监听器：处理该界面上的用户交互。
 *   This file defines n3. UI listener handling user interaction on this screen.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import io.carplay.app.prototype.MainActivity;



/**
 * 界面事件监听器：处理该界面上的用户交互。
 *
 * UI listener handling user interaction on this screen.
 */
public final  class n3 implements View.OnClickListener {
    public final  int a;
    public  MainActivity b;
    public  String c;
    public  AlertDialog d;
    public  EditText e;

    public  n3(int i) {
        this.a = i;
    }

    @Override 
    public final void onClick(View view) {
        switch (this.a) {
            case 0:
                this.b.C1(this.e, this.c, this.d, view);
                break;
            default:
                this.b.E1(this.c, this.d, this.e, view);
                break;
        }
    }
}
