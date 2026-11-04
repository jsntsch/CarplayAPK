/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 StagedPeer。暂存对端：保存尚未完成验证的对端身份，验证通过后才提交为可信设备。
 *   This file defines StagedPeer. Staged peer: holds a not-yet-verified peer identity until verification succeeds.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Objects;



/**
 * 暂存对端：保存尚未完成验证的对端身份，验证通过后才提交为可信设备。
 *
 * Staged peer: holds a not-yet-verified peer identity until verification succeeds.
 */
public final class StagedPeer implements PairingTlv8.c, AutoCloseable {
    private final PairingTlv8.c a;
    private PairingTlv8.b b;
    private PairingTlv8.b c;
    private boolean d;
    private boolean e;
    private boolean f;

    public StagedPeer(PairingTlv8.c cVar) {
        Objects.requireNonNull(cVar);
        this.a = cVar;
    }

    private void i() throws IOException {
        if (this.d) {
            throw k("STAGED_PEER_CLOSED");
        }
    }

    private static IOException k(String str) {
        return new IOException(str);
    }

    private static void l(PairingTlv8.b bVar, PairingTlv8.b bVar2) throws IOException {
        if (bVar != null) {
            if (!bVar.c().equals(bVar2.c()) || !MessageDigest.isEqual(bVar.e(), bVar2.e())) {
                throw k("STAGED_PEER_IDENTITY_CHANGED");
            }
        }
    }

    @Override 
    public synchronized PairingTlv8.b b(String str) throws IOException {
        i();
        PairingTlv8.b bVarB = this.a.b(str);
        PairingTlv8.b bVar = this.b;
        if (bVar != null && bVar.c().equals(str)) {
            l(bVarB, this.b);
            return this.b;
        }
        return bVarB;
    }

    @Override 
    public synchronized void c(PairingTlv8.b bVar) throws IOException {
        i();
        if (this.e) {
            throw k("STAGED_PEER_ALREADY_COMMITTED");
        }
        if (bVar == null) {
            throw k("STAGED_PEER_MISSING");
        }
        l(this.a.b(bVar.c()), bVar);
        PairingTlv8.b bVar2 = this.b;
        if (bVar2 == null) {
            this.b = new PairingTlv8.b(bVar.c(), bVar.e());
        } else {
            if (!bVar2.c().equals(bVar.c())) {
                throw k("STAGED_PEER_LIMIT");
            }
            l(this.b, bVar);
        }
    }

    @Override 
    public void close() {
        g();
    }

    public synchronized void e() throws IOException {
        i();
        if (this.f) {
            return;
        }
        if (this.e) {
            throw k("STAGED_PEER_COMMIT_ALREADY_ATTEMPTED");
        }
        PairingTlv8.b bVar = this.b;
        if (bVar == null) {
            throw k("STAGED_PEER_MISSING");
        }
        this.e = true;
        this.b = null;
        try {
            l(this.a.b(bVar.c()), bVar);
            this.a.c(bVar);
            this.f = true;
        } catch (IOException e) {
            this.d = true;
            throw e;
        } catch (RuntimeException e2) {
            this.d = true;
            throw new IOException("STAGED_PEER_COMMIT_FAILED", e2);
        }
    }

    public synchronized void f(PairingTlv8.b bVar) throws IOException {
        i();
        if (bVar == null) {
            throw k("STAGED_EXISTING_PEER_MISSING");
        }
        if (this.f) {
            PairingTlv8.b bVar2 = this.c;
            if (bVar2 == null) {
                throw k("STAGED_PEER_ALREADY_COMMITTED");
            }
            l(bVar2, bVar);
            return;
        }
        if (this.e) {
            throw k("STAGED_PEER_COMMIT_ALREADY_ATTEMPTED");
        }
        if (this.b != null) {
            throw k("STAGED_EXISTING_PEER_HAS_NEW_STAGE");
        }
        this.e = true;
        try {
            try {
                PairingTlv8.b bVarB = this.a.b(bVar.c());
                if (bVarB == null) {
                    throw k("STAGED_EXISTING_PEER_NOT_STORED");
                }
                l(bVarB, bVar);
                this.c = new PairingTlv8.b(bVar.c(), bVar.e());
                this.f = true;
            } catch (RuntimeException e) {
                this.d = true;
                throw new IOException("STAGED_EXISTING_PEER_COMMIT_FAILED", e);
            }
        } catch (IOException e2) {
            this.d = true;
            throw e2;
        }
    }

    public synchronized void g() {
        this.d = true;
        this.b = null;
    }
}
