/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 o0。界面事件监听器：处理该界面上的用户交互。
 *   This file defines o0. UI listener handling user interaction on this screen.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.app.AlertDialog;
import android.view.View;



/**
 * 界面事件监听器：处理该界面上的用户交互。
 *
 * UI listener handling user interaction on this screen.
 */
public final  class o0 implements View.OnClickListener {
    public final  int a;
    public  Runnable b;
    public  AlertDialog c;

    public  o0(int i) {
        this.a = i;
    }

    @Override 
    public final void onClick(View view) {
        switch (this.a) {
            case 0:
                UiFactory.o(this.b, this.c, view);
                break;
            default:
                UiFactory.p(this.b, this.c, view);
                break;
        }
    }
}
