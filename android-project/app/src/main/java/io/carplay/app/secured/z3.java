/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 z3。单选组回调：处理选项切换。
 *   This file defines z3. Radio-group callback handling option changes.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.widget.RadioGroup;
import io.carplay.app.prototype.MainActivity;



/**
 * 单选组回调：处理选项切换。
 *
 * Radio-group callback handling option changes.
 */
public final  class z3 implements RadioGroup.OnCheckedChangeListener {
    public  MainActivity a;

    @Override 
    public final void onCheckedChanged(RadioGroup radioGroup, int i) {
        this.a.m1(radioGroup, i);
    }
}
