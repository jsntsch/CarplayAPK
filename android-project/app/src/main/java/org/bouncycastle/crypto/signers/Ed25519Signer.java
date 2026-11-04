/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 Ed25519Signer。Ed25519 签名器：生成与验证签名。
 *   This file defines Ed25519Signer. Ed25519 signer producing and verifying signatures.
 *
 * 所属子系统 / Subsystem:
 *   随包内置的 Bouncy Castle 精简实现（Ed25519 / X25519 / ChaCha20-Poly1305） / vendored Bouncy Castle subset (Ed25519 / X25519 / ChaCha20-Poly1305)
 */

package org.bouncycastle.crypto.signers;

import io.carplay.app.secured.Iap2Csm;
import io.carplay.app.secured.SyntheticRefs;
import io.carplay.app.secured.ByteArrays;
import io.carplay.app.secured.CryptoServicesRegistrar;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;


/**
 * Ed25519 签名器：生成与验证签名。
 *
 * Ed25519 signer producing and verifying signatures.
 */
public class Ed25519Signer {
    private final b buffer = new b();
    private boolean forSigning;
    private Ed25519PrivateKeyParameters privateKey;
    private Ed25519PublicKeyParameters publicKey;

    /**
     * 扩展实现：在 ByteArrayOutputStream 基础上补充 Ed25519Signer 需要的局部行为。
     *
     * Extension of ByteArrayOutputStream adding the local behaviour Ed25519Signer needs.
     */
    public static final class b extends ByteArrayOutputStream {
        private b() {
        }

        public synchronized byte[] b(Ed25519PrivateKeyParameters ed25519PrivateKeyParameters) {
            byte[] bArr;
            bArr = new byte[64];
            ed25519PrivateKeyParameters.sign(0, null, this.buf, 0, this.count, bArr, 0);
            reset();
            return bArr;
        }

        public synchronized boolean c(Ed25519PublicKeyParameters ed25519PublicKeyParameters, byte[] bArr) {
            if (64 != bArr.length) {
                reset();
                return false;
            }
            boolean zVerify = ed25519PublicKeyParameters.verify(0, null, this.buf, 0, this.count, bArr, 0);
            reset();
            return zVerify;
        }

        @Override 
        public synchronized void reset() {
            ByteArrays.f(this.buf, 0, this.count, (byte) 0);
            this.count = 0;
        }
    }

    public byte[] generateSignature() {
        Ed25519PrivateKeyParameters ed25519PrivateKeyParameters;
        if (this.forSigning && (ed25519PrivateKeyParameters = this.privateKey) != null) {
            return this.buffer.b(ed25519PrivateKeyParameters);
        }
        SyntheticRefs.d("Ed25519Signer not initialised for signature generation.");
        return null;
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        this.forSigning = z;
        Ed25519PublicKeyParameters ed25519PublicKeyParameters = null;
        if (z) {
            this.privateKey = (Ed25519PrivateKeyParameters) cipherParameters;
        } else {
            this.privateKey = null;
            ed25519PublicKeyParameters = (Ed25519PublicKeyParameters) cipherParameters;
        }
        this.publicKey = ed25519PublicKeyParameters;
        CryptoServicesRegistrar.a(org.bouncycastle.crypto.signers.a.a("Ed25519", Iap2Csm.b, cipherParameters, z));
        reset();
    }

    public void reset() {
        this.buffer.reset();
    }

    public void update(byte b2) throws IOException {
        this.buffer.write(b2);
    }

    public boolean verifySignature(byte[] bArr) {
        Ed25519PublicKeyParameters ed25519PublicKeyParameters;
        if (!this.forSigning && (ed25519PublicKeyParameters = this.publicKey) != null) {
            return this.buffer.c(ed25519PublicKeyParameters, bArr);
        }
        SyntheticRefs.d("Ed25519Signer not initialised for verification");
        return false;
    }

    public void update(byte[] bArr, int i, int i2) throws IOException {
        this.buffer.write(bArr, i, i2);
    }
}
