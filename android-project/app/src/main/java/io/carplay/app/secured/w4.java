/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 w4。原生 AVC 单元解析：把原生视频通道回传的单元拆成可校验的 AVC 帧。
 *   This file defines w4. Native AVC unit parsing: splits units coming back from the native video channel into verifiable AVC frames.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.util.List;


/**
 * 原生 AVC 单元解析：把原生视频通道回传的单元拆成可校验的 AVC 帧。
 *
 * Native AVC unit parsing: splits units coming back from the native video channel into verifiable AVC frames.
 */
public final class w4 {
    private final VideoCipher.AvcVideoInput a = new VideoCipher.AvcVideoInput();
    private volatile AirPlaySession.g b;

    @FunctionalInterface
    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface a {
        void a(byte[] bArr);
    }

    @FunctionalInterface
    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface b {
        void b(AvcAccessUnit hVar) throws IOException;
    }

    
    public static  void f(a aVar, AvcAccessUnit hVar) throws IOException {
        byte[] bArrK = hVar.k();
        byte[] bArrH = hVar.h();
        aVar.a(AvcLengthParser.b((bArrK == null || bArrH == null) ? null : AvcLengthParser.c(List.of(bArrK, bArrH)), hVar.a(), hVar.e()));
    }

    
    public  void g(b bVar, List list, long j, long j2, long j3) throws IOException {
        byte[] bArrM = this.a.m();
        byte[] bArrI = this.a.i();
        if (bArrM != null && bArrI != null) {
            this.b = new AirPlaySession.g(bArrM, bArrI);
        }
        bVar.b(new AvcAccessUnit(list, bArrM, bArrI, list.stream().anyMatch(new w0(20)), j, j2, j3));
    }

    
    public static  boolean h(byte[] bArr) {
        return (bArr[0] & 31) == 5;
    }

    public synchronized void d(byte[] bArr, a aVar) throws Throwable {
        Throwable th = null;
        try {
            try {
                long jNanoTime = System.nanoTime();
                k1 k1Var = new k1(1);
                k1Var.b = aVar;
                VarHandle.storeStoreFence();
                e(bArr, 0L, jNanoTime, 0L, k1Var);
            } catch (Throwable thCaught) {
                throw thCaught;
            }
        } catch (Throwable th2) {
            th = th2;
            throw th;
        }
    }

    
    public synchronized void e(byte[] bArr, long j, long j2, long j3, b bVar) throws IOException {
        if (j3 < 0) {
            throw new IOException("native AVC generation invalid");
        }
        try {
            VideoCipher.AvcVideoInput bVar2 = this.a;
            z2 z2Var = new z2();
            z2Var.a = this;
            z2Var.b = bVar;
            VarHandle.storeStoreFence();
            bVar2.c(bArr, j, j2, j3, z2Var);
            byte[] bArrM = this.a.m();
            byte[] bArrI = this.a.i();
            if (bArrM != null && bArrI != null) {
                this.b = new AirPlaySession.g(bArrM, bArrI);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e2) {
            throw new IOException("native AVC unit parsing failed", e2);
        }
    }

    public AirPlaySession.g i() {
        return this.b;
    }

    public synchronized void j() {
        this.a.k();
        this.b = null;
    }
}
