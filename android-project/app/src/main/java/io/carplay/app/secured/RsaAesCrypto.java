/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 RsaAesCrypto。RSA + AES 混合加密：RSA 交换会话密钥，AES 承载后续加密载荷。
 *   This file defines RsaAesCrypto. RSA + AES hybrid crypto: exchanges the session key with RSA and protects later payloads with AES.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;



/**
 * RSA + AES 混合加密：RSA 交换会话密钥，AES 承载后续加密载荷。
 *
 * RSA + AES hybrid crypto: exchanges the session key with RSA and protects later payloads with AES.
 */
public final class RsaAesCrypto implements AutoCloseable {
    private KeyPair a;
    private final a b;
    private boolean c;

    @FunctionalInterface
    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface a {
        o a(byte[] bArr);
    }

    public RsaAesCrypto(a aVar) throws NoSuchAlgorithmException {
        if (aVar == null) {
            SyntheticRefs.b("verified target AES configuration required");
            throw null;
        }
        this.b = aVar;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        this.a = keyPairGenerator.generateKeyPair();
    }

    public synchronized o b(String str) throws GeneralSecurityException {
        if (this.a == null || this.c) {
            throw new IllegalStateException("closed/consumed exchange");
        }
        this.c = true;
        if (str == null || str.length() > 4096) {
            throw new GeneralSecurityException("invalid RSA payload length");
        }
        byte[] bArrDecode = null;
        byte[] bArrDoFinal = null;
        o oVarA;
        try {
            try {
                bArrDecode = Base64.getDecoder().decode(str);
            } catch (IllegalArgumentException unused) {
                throw new GeneralSecurityException("invalid RSA payload encoding");
            }
            if (bArrDecode.length != 256) {
                throw new GeneralSecurityException("RSA-2048 ciphertext length mismatch");
            }
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(2, this.a.getPrivate());
            bArrDoFinal = cipher.doFinal(bArrDecode);
            if (bArrDoFinal.length != 16 && bArrDoFinal.length != 24 && bArrDoFinal.length != 32) {
                throw new GeneralSecurityException("invalid negotiated AES key length");
            }
            oVarA = this.b.a(bArrDoFinal);
            if (oVarA == null) {
                throw new GeneralSecurityException("missing negotiated body cipher");
            }
            return oVarA;
        } finally {
            if (bArrDecode != null) {
                Arrays.fill(bArrDecode, (byte) 0);
            }
            if (bArrDoFinal != null) {
                Arrays.fill(bArrDoFinal, (byte) 0);
            }
            this.a = null;
        }
    }

    public synchronized String c() {
        if (this.a == null || this.c) {
            throw new IllegalStateException("closed/consumed exchange");
        }
        return Base64.getEncoder().encodeToString(this.a.getPublic().getEncoded());
    }

    @Override 
    public synchronized void close() {
        this.a = null;
        this.c = true;
    }
}
