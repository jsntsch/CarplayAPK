/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 j0。内部实现类：围绕 AirPlaySession 组织状态与操作，服务于本子系统。
 *   This file defines j0. Internal implementation class grouping state and operations around AirPlaySession.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.util.List;
import java.util.Map;



/**
 * 内部实现类：围绕 AirPlaySession 组织状态与操作，服务于本子系统。
 *
 * Internal implementation class grouping state and operations around AirPlaySession.
 */
public final  class j0 implements Iap2WirelessSetup.c, AirPlaySession.l, EventChannel.c, EventChannel.e, VideoCipher.AvcVideoInput.a {
    public final  int a;
    public  AirPlaySession b;

    public  j0(AirPlaySession airPlaySession) {
        this.a = 3;
        this.b = airPlaySession;
    }

    @Override 
    public byte[] a(EventChannel.b bVar) throws IOException {
        int i = this.a;
        AirPlaySession airPlaySession = this.b;
        switch (i) {
            case 4:
                return airPlaySession.S0(bVar);
            default:
                return airPlaySession.O0(bVar);
        }
    }

    @Override 
    public Iap2WirelessSetup.b b() throws IOException, InterruptedException {
        int i = this.a;
        AirPlaySession airPlaySession = this.b;
        switch (i) {
            case 0:
                return airPlaySession.x1();
            case AudioSourceCapability.c :
            default:
                return airPlaySession.z1();
            case 2:
                return airPlaySession.z1();
        }
    }

    @Override 
    public void c(String str, int i, int i2, boolean z) {
        this.b.T0(str, i, i2, z);
    }

    @Override 
    public void d(List list) throws IOException {
        this.b.Y0(list);
    }

    @Override 
    public void e(Map map) throws IOException {
        this.b.K0(map);
    }

    public  j0(int i) {
        this.a = i;
    }
}
