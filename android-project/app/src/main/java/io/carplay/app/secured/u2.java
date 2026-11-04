/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 u2。界面事件监听器：处理该界面上的用户交互。
 *   This file defines u2. UI listener handling user interaction on this screen.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.view.View;
import io.carplay.app.prototype.HelpActivity;



/**
 * 界面事件监听器：处理该界面上的用户交互。
 *
 * UI listener handling user interaction on this screen.
 */
public final  class u2 implements View.OnClickListener {
    public final  int a;
    public  Object b;

    public  u2(int i) {
        this.a = i;
    }

    @Override 
    public final void onClick(View view) {
        int i = this.a;
        Object obj = this.b;
        switch (i) {
            case 0:
                ((HelpActivity) obj).d(view);
                break;
            case AudioSourceCapability.c :
                ((HelpActivity) obj).e(view);
                break;
            default:
                ((Runnable) obj).run();
                break;
        }
    }
}
