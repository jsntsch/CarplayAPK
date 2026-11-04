/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 KeepAliveServer。UDP 保活服务：按 5 秒间隔向车机发送保活包，避免车机端看门狗判定链路失效。
 *   This file defines KeepAliveServer. UDP keep-alive server: sends a keep-alive datagram every 5 seconds so the head-unit watchdog does not declare the link dead.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;


/**
 * UDP 保活服务：按 5 秒间隔向车机发送保活包，避免车机端看门狗判定链路失效。
 *
 * UDP keep-alive server: sends a keep-alive datagram every 5 seconds so the head-unit watchdog does not declare the link dead.
 */
final class KeepAliveServer implements AutoCloseable {
    
    static final long SEND_INTERVAL_MS = 5000;
    private final a a;
    private long b = -1;
    private long c;
    private boolean d;

    @FunctionalInterface
    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface a {
        void a(byte[] bArr);
    }

    public KeepAliveServer(a aVar) {
        Objects.requireNonNull(aVar);
        this.a = aVar;
    }

    public static Integer b(Map<?, ?> map) throws IOException {
        Object obj = map.get("keepAlivePort");
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof Byte) && !(obj instanceof Short) && !(obj instanceof Integer) && !(obj instanceof Long)) {
            SyntheticRefs.c("KEEPALIVE_PORT_INVALID");
            return null;
        }
        long jLongValue = ((Number) obj).longValue();
        if (jLongValue == 0) {
            return null;
        }
        if (jLongValue >= 1 && jLongValue <= 65535) {
            return Integer.valueOf((int) jLongValue);
        }
        SyntheticRefs.c("KEEPALIVE_PORT_INVALID");
        return null;
    }

    public synchronized long c() {
        return this.c;
    }

    @Override 
    public synchronized void close() {
        this.d = true;
    }

    public synchronized boolean e(long j) {
        if (!this.d && j >= 0) {
            long j2 = this.b;
            if (j2 >= 0 && j >= j2 && j - j2 < SEND_INTERVAL_MS) {
                return false;
            }
            this.b = j;
            this.c++;
            this.a.a(DerCodec.q(4));
            return true;
        }
        return false;
    }
}
