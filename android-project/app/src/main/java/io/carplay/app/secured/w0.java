/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 w0。过滤谓词：按条件筛选候选对象。
 *   This file defines w0. Predicate filtering candidate objects by a condition.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import io.carplay.app.prototype.AirplayServiceProbe;
import java.net.InetAddress;
import java.util.function.Predicate;



/**
 * 过滤谓词：按条件筛选候选对象。
 *
 * Predicate filtering candidate objects by a condition.
 */
public final  class w0 implements Predicate {
    public final  int a;

    public  w0(int i) {
        this.a = i;
    }

    @Override 
    public final boolean test(Object obj) {
        switch (this.a) {
            case 0:
                return HidBackParser.h((HidBackParser.c) obj);
            case AudioSourceCapability.c :
                return HidConsumerParser.g((HidConsumerParser.c) obj);
            case 2:
                return MfiCertificateVerifier.L((MfiCertificateVerifier.d) obj);
            case AudioSourceCapability.d :
                return MfiCertificateVerifier.J((MfiCertificateVerifier.d) obj);
            case 4:
                return MfiCertificateVerifier.N((MfiCertificateVerifier.d) obj);
            case 5:
                return MfiCertificateVerifier.O((MfiCertificateVerifier.d) obj);
            case 6:
                return AudioCapabilities.p((AudioCapabilities.d) obj);
            case 7:
                return ((HidCapabilities.e) obj).k();
            case 8:
                return ((HidCapabilities.e) obj).j();
            case 9:
                return HidCapabilities.x((HidCapabilities.c) obj);
            case 10:
                return AirPlaySession.Q0((byte[]) obj);
            case 11:
                return AirPlaySession.f1((byte[]) obj);
            case 12:
                return "url".equals((String) obj);
            case 13:
                return AirPlaySession.W0(obj);
            case 14:
                return AirplayServiceProbe.w((InetAddress) obj);
            case 15:
                return HidBackParser.e(((Integer) obj).intValue());
            case Iap2Csm.e :
                return HidConsumerParser.h(((Integer) obj).intValue());
            case 17:
                return SessionStateMachine.C(obj);
            case 18:
                return AudioSourceCapability.j((HidCapabilities.c) obj);
            case 19:
                return EncryptProofNeg.m((EncryptProofNeg.b) obj);
            default:
                return w4.h((byte[]) obj);
        }
    }
}
