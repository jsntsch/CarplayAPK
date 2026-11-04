/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 KeyParameter。对称密钥参数：包装原始密钥字节。
 *   This file defines KeyParameter. Symmetric key parameter wrapping raw key bytes.
 *
 * 所属子系统 / Subsystem:
 *   随包内置的 Bouncy Castle 精简实现（Ed25519 / X25519 / ChaCha20-Poly1305） / vendored Bouncy Castle subset (Ed25519 / X25519 / ChaCha20-Poly1305)
 */

package org.bouncycastle.crypto.params;

import io.carplay.app.secured.SyntheticRefs;
import io.carplay.app.secured.ByteArrays;
import org.bouncycastle.crypto.CipherParameters;


/**
 * 对称密钥参数：包装原始密钥字节。
 *
 * Symmetric key parameter wrapping raw key bytes.
 */
public class KeyParameter implements CipherParameters {
    private byte[] key;

    public KeyParameter(byte[] bArr, int i, int i2) {
        this(i2);
        System.arraycopy(bArr, i, this.key, 0, i2);
    }

    public void copyTo(byte[] bArr, int i, int i2) {
        byte[] bArr2 = this.key;
        if (bArr2.length == i2) {
            System.arraycopy(bArr2, 0, bArr, i, i2);
        } else {
            SyntheticRefs.b("len");
        }
    }

    public byte[] getKey() {
        return this.key;
    }

    public int getKeyLength() {
        return this.key.length;
    }

    public KeyParameter reverse() {
        KeyParameter keyParameter = new KeyParameter(this.key.length);
        ByteArrays.h(this.key, keyParameter.key);
        return keyParameter;
    }

    public KeyParameter(byte[] bArr) {
        this(bArr, 0, bArr.length);
    }

    private KeyParameter(int i) {
        this.key = new byte[i];
    }
}
