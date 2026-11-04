/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 ReleaseCheckTask。任务分发器：把网络/套接字等回调统一切回工作线程执行。
 *   This file defines ReleaseCheckTask. Task dispatcher that hands network/socket callbacks back to a worker thread.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.net.LinkProperties;
import io.carplay.app.secured.AudioSourceCapability;
import io.carplay.app.secured.Iap2Csm;
import java.net.ServerSocket;



/**
 * 任务分发器：把网络/套接字等回调统一切回工作线程执行。
 *
 * Task dispatcher that hands network/socket callbacks back to a worker thread.
 */
public final  class ReleaseCheckTask implements Runnable {
    public final  int a;
    public  Object b;
    public  Object c;

    public  ReleaseCheckTask(int i) {
        this.a = i;
    }

    @Override 
    public final void run() {
        switch (this.a) {
            case 0:
                ((CarPlayCtrlAdvertiser) this.b).l((ServerSocket) this.c);
                return;
            case AudioSourceCapability.c :
                ((WifiNetworkMonitor.a) this.b).e((LinkProperties) this.c);
                return;
            case 2:
                ((WifiNetworkMonitor.c) this.b).g((String) this.c);
                return;
            default:
                ((PairingSession.a) this.b).o((Iap2Csm.a) this.c);
                return;
        }
    }
}
