/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 x6。过滤谓词：按条件筛选候选对象。（服务于 PairingSession）
 *   This file defines x6. Predicate filtering candidate objects by a condition. (serves PairingSession.)
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.net.Network;
import io.carplay.app.prototype.PairingSession;
import java.net.InetAddress;
import java.util.function.Predicate;



/**
 * 过滤谓词：按条件筛选候选对象。（服务于 PairingSession）
 *
 * Predicate filtering candidate objects by a condition. (serves PairingSession.)
 */
public final  class x6 implements Predicate {
    public  PairingSession a;
    public  Network b;
    public  String c;

    @Override 
    public final boolean test(Object obj) {
        return this.a.o1(this.b, this.c, (InetAddress) obj);
    }
}
