/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 DhParameters。Diffie-Hellman 参数：保存并校验 DH 的 p/g/l 参数。
 *   This file defines DhParameters. Diffie-Hellman parameters: stores and validates the p/g/l values.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;


/**
 * Diffie-Hellman 参数：保存并校验 DH 的 p/g/l 参数。
 *
 * Diffie-Hellman parameters: stores and validates the p/g/l values.
 */
public class DhParameters implements CipherParameters {
    private BigInteger a;
    private BigInteger b;
    private BigInteger c;
    private BigInteger d;
    private int e;
    private int f;
    private k2 g;

    public DhParameters(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, int i, int i2, BigInteger bigInteger4, k2 k2Var) {
        if (i2 != 0) {
            if (i2 > bigInteger.bitLength()) {
                SyntheticRefs.b("when l value specified, it must satisfy 2^(l-1) <= p");
                throw null;
            }
            if (i2 < i) {
                SyntheticRefs.b("when l value specified, it may not be less than m value");
                throw null;
            }
        }
        if (i > bigInteger.bitLength() && !p5.b("org.bouncycastle.dh.allow_unsafe_p_value")) {
            SyntheticRefs.b("unsafe p value so small specific l required");
            throw null;
        }
        this.a = bigInteger2;
        this.b = bigInteger;
        this.c = bigInteger3;
        this.e = i;
        this.f = i2;
        this.d = bigInteger4;
        this.g = k2Var;
    }

    public BigInteger a() {
        return this.a;
    }

    public BigInteger b() {
        return this.b;
    }

    public BigInteger c() {
        return this.c;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DhParameters)) {
            return false;
        }
        DhParameters j2Var = (DhParameters) obj;
        if (c() != null) {
            if (!c().equals(j2Var.c())) {
                return false;
            }
        } else if (j2Var.c() != null) {
            return false;
        }
        return j2Var.b().equals(this.b) && j2Var.a().equals(this.a);
    }

    public int hashCode() {
        return (c() != null ? c().hashCode() : 0) ^ (b().hashCode() ^ a().hashCode());
    }
}
