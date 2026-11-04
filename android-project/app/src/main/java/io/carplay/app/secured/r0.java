/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 r0。内部实现类：围绕 AirPlaySession 组织状态与操作，服务于本子系统。
 *   This file defines r0. Internal implementation class grouping state and operations around AirPlaySession.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;

import java.net.Socket;



/**
 * 内部实现类：围绕 AirPlaySession 组织状态与操作，服务于本子系统。
 *
 * Internal implementation class grouping state and operations around AirPlaySession.
 */
public final  class r0 implements EventChannelConnector.a {
    public  AirPlaySession a;
    public  int b;

    @Override 
    public final Socket a(int i) throws IOException {
        return this.a.L0(this.b, i);
    }
}
