/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 VideoEncoderStats。编码器统计：采集帧率、码率与发送用量等运行指标。
 *   This file defines VideoEncoderStats. Encoder statistics: collects frame rate, bitrate and transmit-usage metrics.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;



/**
 * 编码器统计：采集帧率、码率与发送用量等运行指标。
 *
 * Encoder statistics: collects frame rate, bitrate and transmit-usage metrics.
 */
final class VideoEncoderStats {
    private VideoEncoderStats() {
    }

    public static byte[] a(boolean z, long j, long j2, int i, long j3) throws IOException {
        byte[] bArrF;
        if (j < 0 || j2 < 0 || i < 0 || i > 12 || j3 < 1) {
            SyntheticRefs.c("SCREEN_STATS_INVALID");
            return null;
        }
        if (z) {
            double d = j3 / 1.0E9d;
            bArrF = BinaryPlist.f(Map.of("txUsageAvg", Double.valueOf(j2 / d), "encoderCurrentFPS", Long.valueOf(Math.min(240L, Math.round(j / d))), "sentFramesAvg", Long.valueOf(Math.min(4294967295L, j)), "queuedFramesAvg", Integer.valueOf(i), "lossAvg", Double.valueOf(0.0d)));
        } else {
            bArrF = new byte[0];
        }
        byte[] bArr = new byte[Iap2Csm.b];
        ByteBuffer byteBufferOrder = ByteBuffer.wrap(bArr).order(ByteOrder.LITTLE_ENDIAN);
        byteBufferOrder.putInt(bArrF.length);
        bArr[4] = (byte) (z ? 5 : 2);
        if (z) {
            byteBufferOrder.putFloat(BtDiscovery.v, 0.0f).putFloat(124, bArrF.length);
        }
        return DerCodec.d(bArr, bArrF);
    }
}
