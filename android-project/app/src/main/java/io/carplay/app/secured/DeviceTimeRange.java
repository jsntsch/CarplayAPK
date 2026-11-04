/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 DeviceTimeRange。设备时间范围消息：表示设备侧的时间区间与精度。
 *   This file defines DeviceTimeRange. Device time range message representing a device-side time interval and precision.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.zone.ZoneRules;


/**
 * 设备时间范围消息：表示设备侧的时间区间与精度。
 *
 * Device time range message representing a device-side time interval and precision.
 */
public final class DeviceTimeRange extends ProtoMessageBase {
    private final long a;
    private final int b;
    private final int c;

    public DeviceTimeRange(long j, int i, int i2) {
        if (j < 0 || i < -32768 || i > 32767 || i2 < -128 || i2 > 127) {
            SyntheticRefs.b("DEVICE_TIME_RANGE");
            throw null;
        }
        this.a = j;
        this.b = i;
        this.c = i2;
    }

    public static DeviceTimeRange g(Instant instant, ZoneId zoneId) {
        ZoneRules rules = zoneId.getRules();
        int totalSeconds = rules.getOffset(instant).getTotalSeconds();
        long seconds = rules.getDaylightSavings(instant).getSeconds();
        if (totalSeconds % 60 == 0 && seconds % 60 == 0) {
            return new DeviceTimeRange(instant.getEpochSecond(), totalSeconds / 60, Math.toIntExact(seconds / 60));
        }
        SyntheticRefs.b("DEVICE_TIME_MINUTE_PRECISION");
        return null;
    }

    public static DeviceTimeRange j() {
        return g(Instant.now(), ZoneId.systemDefault());
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof DeviceTimeRange)) {
            return false;
        }
        DeviceTimeRange g5Var = (DeviceTimeRange) obj;
        return this.b == g5Var.b && this.c == g5Var.c && this.a == g5Var.a;
    }

    public int h() {
        return this.c;
    }

    public final int hashCode() {
        int i = this.b;
        int i2 = this.c;
        return Long.hashCode(this.a) + (((i * 31) + i2) * 31);
    }

    public long i() {
        return this.a;
    }

    public byte[] k() throws IOException {
        byte[] bArrB = Iap2Csm.B(0, ByteBuffer.allocate(8).putLong(this.a).array());
        int i = this.b;
        return Iap2Csm.f(bArrB, Iap2Csm.B(1, new byte[]{(byte) (i >>> 8), (byte) i}), Iap2Csm.B(2, new byte[]{(byte) this.c}));
    }

    public int l() {
        return this.b;
    }

    public String toString() {
        return "Device time snapshot; values redacted";
    }
}
