/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 EventChannelConnector。事件通道连接器：建立 TCP 连接、区分超时/取消/IO 失败并给出可诊断的错误码。
 *   This file defines EventChannelConnector. Event-channel connector: establishes the TCP connection and classifies timeout / cancel / IO failures into diagnosable error codes.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.LongSupplier;



/**
 * 事件通道连接器：建立 TCP 连接、区分超时/取消/IO 失败并给出可诊断的错误码。
 *
 * Event-channel connector: establishes the TCP connection and classifies timeout / cancel / IO failures into diagnosable error codes.
 */
final class EventChannelConnector {
    private static final long a = 2000000000;
    private static final long b = 500000000;

    @FunctionalInterface
    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface a {
        Socket a(int i) throws IOException;
    }

    @FunctionalInterface
    /**
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface b {
        void a(long j) throws InterruptedException;
    }

    private EventChannelConnector() {
    }

    private static void a(BooleanSupplier booleanSupplier) throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted() || booleanSupplier.getAsBoolean()) {
            throw new InterruptedIOException("EVENT_CONNECT_CANCELLED");
        }
    }

    public static Socket b(a aVar, LongSupplier longSupplier, b bVar, BooleanSupplier booleanSupplier, IntConsumer intConsumer) throws IOException {
        Objects.requireNonNull(aVar);
        Objects.requireNonNull(longSupplier);
        Objects.requireNonNull(bVar);
        Objects.requireNonNull(booleanSupplier);
        Objects.requireNonNull(intConsumer);
        long asLong = longSupplier.getAsLong();
        int i = 10000;
        int i2 = 1;
        while (true) {
            a(booleanSupplier);
            long asLong2 = longSupplier.getAsLong();
            try {
                Socket socketA = aVar.a(i);
                if (socketA == null) {
                    throw new IOException("EVENT_CONNECT_NO_SOCKET");
                }
                try {
                    a(booleanSupplier);
                    return socketA;
                } catch (InterruptedIOException e) {
                    try {
                        socketA.close();
                        throw e;
                    } catch (IOException e2) {
                        e.addSuppressed(e2);
                        throw e;
                    }
                }
            } catch (ConnectException e3) {
                a(booleanSupplier);
                long asLong3 = longSupplier.getAsLong();
                long j = asLong3 - asLong2;
                long j2 = asLong3 - asLong;
                if (i2 >= 3 || j < 0 || j > b || j2 < 0 || j2 >= a) {
                    throw e3;
                }
                long j3 = i2 == 1 ? 150L : 300L;
                if (a - j2 <= j3 * 1000000) {
                    throw e3;
                }
                try {
                    bVar.a(j3);
                    a(booleanSupplier);
                    long asLong4 = longSupplier.getAsLong() - asLong;
                    if (asLong4 < 0 || asLong4 >= a) {
                        throw e3;
                    }
                    int i3 = (int) ((a - asLong4) / 1000000);
                    if (i3 < 1) {
                        throw e3;
                    }
                    i2++;
                    try {
                        intConsumer.accept(i2);
                    } catch (RuntimeException unused) {
                    }
                    i = i3;
                } catch (InterruptedException e4) {
                    Thread.currentThread().interrupt();
                    InterruptedIOException interruptedIOException = new InterruptedIOException("EVENT_CONNECT_INTERRUPTED");
                    interruptedIOException.initCause(e4);
                    throw interruptedIOException;
                }
            }
        }
    }
}
