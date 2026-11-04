/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 i。内部实现类：围绕 LinkProperties 组织状态与操作，服务于本子系统。
 *   This file defines i. Internal implementation class grouping state and operations around LinkProperties.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.net.LinkProperties;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;



/**
 * 内部实现类：围绕 LinkProperties 组织状态与操作，服务于本子系统。
 *
 * Internal implementation class grouping state and operations around LinkProperties.
 */
public final  class i implements CarPlayCtrlAdvertiser.c {
    public  LinkProperties a;

    @Override 
    public final ServerSocket a(InetAddress inetAddress, int i) throws IOException {
        return CarPlayCtrlAdvertiser.k(this.a, inetAddress, i);
    }
}
