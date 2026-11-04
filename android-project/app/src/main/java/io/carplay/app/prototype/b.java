/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 b。内部实现类：围绕 AvcEncoder 组织状态与操作，服务于本子系统。
 *   This file defines b. Internal implementation class grouping state and operations around AvcEncoder.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.graphics.SurfaceTexture;



/**
 * 内部实现类：围绕 AvcEncoder 组织状态与操作，服务于本子系统。
 *
 * Internal implementation class grouping state and operations around AvcEncoder.
 */
public final  class b implements SurfaceTexture.OnFrameAvailableListener {
    public  AvcEncoder a;

    @Override 
    public final void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.a.C(surfaceTexture);
    }
}
