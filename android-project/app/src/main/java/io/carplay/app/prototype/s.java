/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 s。编码回调适配器：把编码器的视频帧回调转交给 AvcEncoder。
 *   This file defines s. Encoder callback adapter forwarding video frame callbacks to AvcEncoder.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import io.carplay.app.secured.VideoCipher;
import java.io.IOException;
import java.util.List;



/**
 * 编码回调适配器：把编码器的视频帧回调转交给 AvcEncoder。
 *
 * Encoder callback adapter forwarding video frame callbacks to AvcEncoder.
 */
public final  class s implements VideoCipher.AvcVideoInput.c {
    public final  int a;
    public  Object b;

    public  s(int i) {
        this.a = i;
    }

    @Override 
    public void c(List list, long j, long j2, long j3) throws IOException {
        ((AvcEncoder) this.b).x(list, j, j2, j3);
    }
}
