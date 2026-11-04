/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 AesCipher。AES 加解密封装：显式指定算法/模式/填充，失败时给出明确错误。
 *   This file defines AesCipher. AES cipher wrapper: requires explicit algorithm/mode/padding and reports clear errors.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.security.InvalidAlgorithmParameterException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


/**
 * AES 加解密封装：显式指定算法/模式/填充，失败时给出明确错误。
 *
 * AES cipher wrapper: requires explicit algorithm/mode/padding and reports clear errors.
 */
public final class AesCipher implements o {
    private final String a;
    private final Provider b;
    private final SecretKey c;
    private final AlgorithmParameterSpec d;

    public AesCipher(String str, Provider provider, SecretKey secretKey, AlgorithmParameterSpec algorithmParameterSpec) throws NoSuchPaddingException, NoSuchAlgorithmException {
        Objects.requireNonNull(str);
        this.a = str;
        Objects.requireNonNull(provider);
        this.b = provider;
        Objects.requireNonNull(secretKey);
        this.c = secretKey;
        this.d = algorithmParameterSpec;
        String[] strArrSplit = str.split("/", -1);
        if (strArrSplit.length != 3 || strArrSplit[0].isBlank() || strArrSplit[1].isBlank() || strArrSplit[2].isBlank()) {
            SyntheticRefs.b("explicit algorithm/mode/padding required");
            throw null;
        }
        if (strArrSplit[1].equalsIgnoreCase("ECB") || algorithmParameterSpec != null) {
            Cipher.getInstance(str, provider);
        } else {
            SyntheticRefs.b("explicit parameters required for non-ECB mode");
            throw null;
        }
    }

    private byte[] c(int i, byte[] bArr) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(this.a, this.b);
        AlgorithmParameterSpec algorithmParameterSpec = this.d;
        SecretKey secretKey = this.c;
        if (algorithmParameterSpec == null) {
            cipher.init(i, secretKey);
        } else {
            cipher.init(i, secretKey, algorithmParameterSpec);
        }
        Objects.requireNonNull(bArr);
        return cipher.doFinal(bArr);
    }

    @Override 
    public byte[] a(byte[] bArr) {
        try {
            return c(1, bArr);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES encryption failed", e);
        }
    }

    @Override 
    public byte[] b(byte[] bArr) {
        try {
            return c(2, bArr);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES decryption failed", e);
        }
    }
}
