/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 a。Ed25519 双倍点运算辅助：签名/验签过程中的点运算。
 *   This file defines a. Ed25519 point-doubling helper used during signing and verification.
 *
 * 所属子系统 / Subsystem:
 *   随包内置的 Bouncy Castle 精简实现（Ed25519 / X25519 / ChaCha20-Poly1305） / vendored Bouncy Castle subset (Ed25519 / X25519 / ChaCha20-Poly1305)
 */

package org.bouncycastle.crypto.signers;

import io.carplay.app.secured.e2;
import io.carplay.app.secured.f2;
import io.carplay.app.secured.p2;
import org.bouncycastle.crypto.CipherParameters;


/**
 * Ed25519 双倍点运算辅助：签名/验签过程中的点运算。
 *
 * Ed25519 point-doubling helper used during signing and verification.
 */
abstract class a {
    public static e2 a(String str, int i, CipherParameters cipherParameters, boolean z) {
        return new p2(str, i, cipherParameters, b(z));
    }

    public static f2 b(boolean z) {
        return z ? f2.SIGNING : f2.VERIFYING;
    }
}
