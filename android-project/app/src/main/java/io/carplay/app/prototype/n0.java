/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 n0。开关控件回调：处理勾选状态变化。
 *   This file defines n0. Checkbox callback handling checked-state changes.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.widget.CompoundButton;



/**
 * 开关控件回调：处理勾选状态变化。
 *
 * Checkbox callback handling checked-state changes.
 */
public final  class n0 implements CompoundButton.OnCheckedChangeListener {
    @Override 
    public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        compoundButton.invalidate();
    }
}
