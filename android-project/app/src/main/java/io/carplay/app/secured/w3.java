/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 w3。开关控件回调：处理勾选状态变化。
 *   This file defines w3. Checkbox callback handling checked-state changes.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.widget.CompoundButton;
import io.carplay.app.prototype.MainActivity;



/**
 * 开关控件回调：处理勾选状态变化。
 *
 * Checkbox callback handling checked-state changes.
 */
public final  class w3 implements CompoundButton.OnCheckedChangeListener {
    public  MainActivity a;

    @Override 
    public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        this.a.q1(compoundButton, z);
    }
}
