/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 l0。界面事件监听器：处理该界面上的用户交互。
 *   This file defines l0. UI listener handling user interaction on this screen.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.app.Activity;
import android.view.View;
import android.view.WindowInsets;



/**
 * 界面事件监听器：处理该界面上的用户交互。
 *
 * UI listener handling user interaction on this screen.
 */
public final  class l0 implements View.OnApplyWindowInsetsListener {
    public  int a;
    public  Activity b;

    @Override 
    public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
        return UiFactory.s(this.a, this.b, view, windowInsets);
    }
}
