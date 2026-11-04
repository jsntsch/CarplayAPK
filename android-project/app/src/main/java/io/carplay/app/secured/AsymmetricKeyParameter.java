/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 AsymmetricKeyParameter。非对称密钥参数基类：Ed25519 / X25519 密钥参数的公共父类。
 *   This file defines AsymmetricKeyParameter. Asymmetric key parameter base class shared by Ed25519 / X25519 key parameters.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import org.bouncycastle.crypto.CipherParameters;


/**
 * 非对称密钥参数基类：Ed25519 / X25519 密钥参数的公共父类。
 *
 * Asymmetric key parameter base class shared by Ed25519 / X25519 key parameters.
 */
public abstract class AsymmetricKeyParameter implements CipherParameters {
    boolean privateKey;

    public AsymmetricKeyParameter(boolean z) {
        this.privateKey = z;
    }

    public boolean isPrivate() {
        return this.privateKey;
    }
}
