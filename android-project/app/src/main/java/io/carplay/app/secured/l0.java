/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 l0。惰性取值：按需提供对象。
 *   This file defines l0. Supplier providing a value on demand.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.function.Supplier;



/**
 * 惰性取值：按需提供对象。
 *
 * Supplier providing a value on demand.
 */
public final  class l0 implements Supplier {
    public final  int a;
    public  Object b;

    public  l0(AirPlaySession.n nVar) {
        this.a = 1;
        this.b = nVar;
    }

    @Override 
    public final Object get() {
        int i = this.a;
        Object obj = this.b;
        switch (i) {
            case 0:
                return ((AirPlaySession.n) obj).e();
            case AudioSourceCapability.c :
                return ((AirPlaySession.n) obj).e();
            default:
                return ((MfiCertificateVerifier.c) obj).e();
        }
    }

    public  l0(int i) {
        this.a = i;
    }
}
