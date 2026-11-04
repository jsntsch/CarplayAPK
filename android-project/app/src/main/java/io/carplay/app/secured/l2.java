/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 l2。内部实现类：围绕 BigInteger 组织状态与操作，服务于本子系统。
 *   This file defines l2. Internal implementation class grouping state and operations around BigInteger.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;


/**
 * 内部实现类：围绕 BigInteger 组织状态与操作，服务于本子系统。
 *
 * Internal implementation class grouping state and operations around BigInteger.
 */
public class l2 implements CipherParameters {
    private BigInteger a;
    private BigInteger b;
    private BigInteger c;
    private m2 d;

    public l2(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, m2 m2Var) {
        this.a = bigInteger3;
        this.c = bigInteger;
        this.b = bigInteger2;
        this.d = m2Var;
    }

    public BigInteger a() {
        return this.a;
    }

    public BigInteger b() {
        return this.c;
    }

    public BigInteger c() {
        return this.b;
    }

    public m2 d() {
        return this.d;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof l2)) {
            return false;
        }
        l2 l2Var = (l2) obj;
        return l2Var.b().equals(this.c) && l2Var.c().equals(this.b) && l2Var.a().equals(this.a);
    }

    public int hashCode() {
        return a().hashCode() ^ (b().hashCode() ^ c().hashCode());
    }
}
