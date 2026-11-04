/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 SyntheticRefs。编译期引用占位：保证重建代码在缺少原始内部类时仍可编译通过。
 *   This file defines SyntheticRefs. Compile-time reference placeholder keeping the reconstructed code compilable without the original internal classes.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.util.Map;




/**
 * 编译期引用占位：保证重建代码在缺少原始内部类时仍可编译通过。
 *
 * Compile-time reference placeholder keeping the reconstructed code compilable without the original internal classes.
 */
public final  class SyntheticRefs implements EventChannelConnector.b, AirPlaySession.l {
    public static  void b(String str) {
        throw new IllegalArgumentException(str);
    }

    public static  void c(String str) throws IOException {
        throw new IOException(str);
    }

    public static  void d(String str) {
        throw new IllegalStateException(str);
    }

    public static  void f(String str) throws ProtocolIOException {
        throw new ProtocolIOException(str);
    }

    @Override 
    public void a(long j) throws InterruptedException {
        Thread.sleep(j);
    }

    @Override 
    public void e(Map map) {
        AirPlaySession.R0(map);
    }
}
