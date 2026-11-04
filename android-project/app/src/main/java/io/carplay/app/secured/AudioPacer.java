/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 AudioPacer。音频发送节拍器：按 PCM 时长计算发送时刻，避免一次性灌包。
 *   This file defines AudioPacer. Audio pacing: derives send deadlines from PCM duration so packets are not burst-injected.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;


/**
 * 音频发送节拍器：按 PCM 时长计算发送时刻，避免一次性灌包。
 *
 * Audio pacing: derives send deadlines from PCM duration so packets are not burst-injected.
 */
public final class AudioPacer {
    private static final long f = 20000000;
    private static final int g = 4;
    private final int a;
    private boolean b;
    private long c;
    private long d;
    private int e;

    public AudioPacer(int i) throws IOException {
        if (AudioCapabilities.v(i, 1, 16) != 0) {
            this.a = i;
        } else {
            SyntheticRefs.c("AUDIO_PACER_RATE");
            throw null;
        }
    }

    public static long a(long j, long j2) {
        long j3 = j2 - j;
        if (j3 > 0) {
            return j3;
        }
        return 0L;
    }

    public synchronized long b(long j, int i, long j2) throws IOException {
        long j3;
        if (i < 1 || ((i > 352 && i != 1024) || j2 < 0)) {
            throw new IOException("AUDIO_PACER_PACKET");
        }
        long j4 = j - this.c;
        if (!this.b || j2 > 0 || j4 > f || (j4 > 0 && this.e >= g)) {
            this.b = true;
            this.c = j;
            this.d = 0L;
            this.e = 0;
        } else if (j4 > 0) {
            this.e++;
        } else {
            this.e = 0;
        }
        j3 = this.c;
        long j5 = (((long) i) * 1000000000) + this.d;
        int i2 = this.a;
        this.c = (j5 / ((long) i2)) + j3;
        this.d = j5 % ((long) i2);
        return j3;
    }

    public synchronized void c() {
        this.b = false;
        this.c = 0L;
        this.d = 0L;
        this.e = 0;
    }
}
