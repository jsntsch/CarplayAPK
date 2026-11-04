/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 AEADParameters。AEAD 参数：承载密钥、nonce 与关联数据。
 *   This file defines AEADParameters. AEAD parameters carrying key, nonce and associated data.
 *
 * 所属子系统 / Subsystem:
 *   随包内置的 Bouncy Castle 精简实现（Ed25519 / X25519 / ChaCha20-Poly1305） / vendored Bouncy Castle subset (Ed25519 / X25519 / ChaCha20-Poly1305)
 */

package org.bouncycastle.crypto.params;

import io.carplay.app.secured.ByteArrays;
import org.bouncycastle.crypto.CipherParameters;


/**
 * AEAD 参数：承载密钥、nonce 与关联数据。
 *
 * AEAD parameters carrying key, nonce and associated data.
 */
public class AEADParameters implements CipherParameters {
    private byte[] associatedText;
    private KeyParameter key;
    private int macSize;
    private byte[] nonce;

    public AEADParameters(KeyParameter keyParameter, int i, byte[] bArr, byte[] bArr2) {
        this.key = keyParameter;
        this.nonce = ByteArrays.d(bArr);
        this.macSize = i;
        this.associatedText = ByteArrays.d(bArr2);
    }

    public byte[] getAssociatedText() {
        return ByteArrays.d(this.associatedText);
    }

    public KeyParameter getKey() {
        return this.key;
    }

    public int getMacSize() {
        return this.macSize;
    }

    public byte[] getNonce() {
        return ByteArrays.d(this.nonce);
    }

    public AEADParameters(KeyParameter keyParameter, int i, byte[] bArr) {
        this(keyParameter, i, bArr, null);
    }
}
