/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 PairingPreparationService。配对准备服务：响应 PREPARE_PAIRING / STOP_PAIRING 指令，预热蓝牙与网络。
 *   This file defines PairingPreparationService. Pairing preparation service responding to PREPARE_PAIRING / STOP_PAIRING and warming up Bluetooth and network.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.R;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.os.PowerManager;
import io.carplay.app.secured.a5;
import io.carplay.app.secured.b5;
import io.carplay.app.secured.BtDiscovery;
import java.lang.invoke.VarHandle;


/**
 * 配对准备服务：响应 PREPARE_PAIRING / STOP_PAIRING 指令，预热蓝牙与网络。
 *
 * Pairing preparation service responding to PREPARE_PAIRING / STOP_PAIRING and warming up Bluetooth and network.
 */
public final class PairingPreparationService extends Service {
    public static final String e = "io.carplus.PREPARE_PAIRING";
    public static final String f = "io.carplus.STOP_PAIRING";
    private static final Object g = new Object();
    private static PairingPreparationService h;
    private BtDiscovery a;
    private PowerManager.WakeLock b;
    private boolean c;
    private boolean d;

    
    public void d() {
        BtDiscovery h5Var;
        synchronized (g) {
            try {
                this.c = true;
                if (h == this) {
                    h = null;
                }
                h5Var = this.a;
                this.a = null;
            } catch (Throwable th) {
                throw th;
            }
        }
        if (h5Var != null) {
            h5Var.close();
        }
        synchronized (this) {
            PowerManager.WakeLock wakeLock = this.b;
            if (wakeLock != null) {
                try {
                    if (wakeLock.isHeld()) {
                        this.b.release();
                    }
                } catch (RuntimeException unused) {
                }
                this.b = null;
            }
        }
        stopForeground(1);
        stopSelf();
    }

    public static boolean e() {
        boolean z;
        synchronized (g) {
            try {
                PairingPreparationService pairingPreparationService = h;
                z = (pairingPreparationService == null || pairingPreparationService.c) ? false : true;
            } catch (Throwable th) {
                throw th;
            }
        }
        return z;
    }

    public static boolean f() {
        boolean z;
        BtDiscovery h5Var;
        synchronized (g) {
            try {
                PairingPreparationService pairingPreparationService = h;
                z = (pairingPreparationService == null || (h5Var = pairingPreparationService.a) == null || !h5Var.m()) ? false : true;
            } catch (Throwable th) {
                throw th;
            }
        }
        return z;
    }

    
    public  void g() {
        b5 b5Var = new b5(0);
        b5Var.b = this;
        VarHandle.storeStoreFence();
        a5 a5Var = new a5(1);
        a5Var.b = this;
        VarHandle.storeStoreFence();
        BtDiscovery h5Var = new BtDiscovery(b5Var, a5Var);
        synchronized (g) {
            try {
                if (this.c) {
                    h5Var.close();
                    return;
                }
                this.a = h5Var;
                try {
                    h5Var.s(this);
                } catch (Exception e2) {
                    AppLogger.g(this, "PHONE_DISCOVERY_PREPARATION_FAILED kind=".concat(e2.getClass().getSimpleName()));
                    d();
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    
    public  void h(String str) {
        AppLogger.g(this, str);
    }

    public static BtDiscovery i() {
        BtDiscovery h5Var;
        synchronized (g) {
            PairingPreparationService pairingPreparationService = h;
            if (pairingPreparationService != null && (h5Var = pairingPreparationService.a) != null && h5Var.m()) {
                BtDiscovery h5Var2 = pairingPreparationService.a;
                pairingPreparationService.a = null;
                pairingPreparationService.c = true;
                h = null;
                pairingPreparationService.d();
                return h5Var2;
            }
            return null;
        }
    }

    @Override 
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override 
    public void onDestroy() {
        d();
        super.onDestroy();
    }

    @Override 
    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent == null || !e.equals(intent.getAction()) || WirelessCarPlayService.L1()) {
            d();
            return 2;
        }
        synchronized (g) {
            if (!this.d && !this.c) {
                this.d = true;
                h = this;
                ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(new NotificationChannel("carplus-pairing", "车机重新识别", 2));
                startForeground(77, new Notification.Builder(this, "carplus-pairing").setSmallIcon(android.R.drawable.stat_notify_sync).setContentTitle("正在准备车机重新识别").setContentText("请回软件按提示操作，准备最多保留两分钟").setContentIntent(PendingIntent.getActivity(this, 77, new Intent(this, (Class<?>) MainActivity.class), 201326592)).setOngoing(true).addAction(new Notification.Action.Builder((Icon) null, "停止准备", PendingIntent.getService(this, 78, new Intent(this, (Class<?>) PairingPreparationService.class).setAction(f), 201326592)).build()).build(), 16);
                int i3 = 0;
                try {
                    PowerManager.WakeLock wakeLockNewWakeLock = ((PowerManager) getSystemService(PowerManager.class)).newWakeLock(1, "CarPlus:PairingPreparation");
                    this.b = wakeLockNewWakeLock;
                    wakeLockNewWakeLock.setReferenceCounted(false);
                    this.b.acquire(125000L);
                } catch (RuntimeException unused) {
                    AppLogger.g(this, "PAIRING_CPU_LEASE unavailable=true");
                }
                a5 a5Var = new a5(i3);
                a5Var.b = this;
                VarHandle.storeStoreFence();
                new Thread(a5Var, "carplay-prepare-records").start();
                return 2;
            }
            return 2;
        }
    }
}
