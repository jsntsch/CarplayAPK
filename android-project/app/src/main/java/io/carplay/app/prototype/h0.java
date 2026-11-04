/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 h0。对话框回调：处理弹窗的点击/显示/关闭事件。
 *   This file defines h0. Dialog callback handling click/show/dismiss events.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;



/**
 * 对话框回调：处理弹窗的点击/显示/关闭事件。
 *
 * Dialog callback handling click/show/dismiss events.
 */
public final  class h0 implements DialogInterface.OnShowListener {
    public final  int a;
    public  AlertDialog b;
    public  Activity c;

    public  h0(int i) {
        this.a = i;
    }

    @Override 
    public final void onShow(DialogInterface dialogInterface) {
        UiFactory.q(this.b, this.c, dialogInterface);
    }
}
