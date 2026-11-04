/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 k1。内部实现类：本子系统使用的数据结构与辅助方法。
 *   This file defines k1. Internal implementation class holding data structures and helpers for this subsystem.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.net.Network;
import io.carplay.app.prototype.WirelessCarPlayService;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.List;



/**
 * 内部实现类：本子系统使用的数据结构与辅助方法。
 *
 * Internal implementation class holding data structures and helpers for this subsystem.
 */
public final  class k1 implements VideoCipher.AvcVideoInput.c, w4.b, AirPlaySession.j {
    public final  int a;
    public  Object b;

    public  k1(int i) {
        this.a = i;
    }

    @Override 
    public void a(DatagramSocket datagramSocket) throws IOException {
        ((Network) this.b).bindSocket(datagramSocket);
    }

    @Override 
    public void b(AvcAccessUnit hVar) throws IOException {
        int i = this.a;
        Object obj = this.b;
        switch (i) {
            case AudioSourceCapability.c :
                w4.f((w4.a) obj, hVar);
                break;
            default:
                ((WirelessCarPlayService) obj).R2(hVar);
                break;
        }
    }

    @Override 
    public void c(List list, long j, long j2, long j3) throws IOException {
        VideoCipher.AvcVideoInput.g((VideoCipher.AvcVideoInput.a) this.b, list, j, j2, j3);
    }
}
