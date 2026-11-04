/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 CarPlusApplication。Application 入口：注册全局异常兜底、初始化偏好设置与进程状态。
 *   This file defines CarPlusApplication. Application entry point registering the global crash handler and initialising preferences and process state.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Process;
import io.carplay.app.secured.ControlChannel;
import io.carplay.app.secured.r1;
import io.carplay.app.secured.ProtoMessageBase;
import java.lang.invoke.VarHandle;


/**
 * Application 入口：注册全局异常兜底、初始化偏好设置与进程状态。
 *
 * Application entry point registering the global crash handler and initialising preferences and process state.
 */
public final class CarPlusApplication extends Application {
    
    public  void b(Thread.UncaughtExceptionHandler uncaughtExceptionHandler, Thread thread, Throwable th) {
        try {
            AppLogger.b(this, thread, th);
            if (uncaughtExceptionHandler != null) {
                uncaughtExceptionHandler.uncaughtException(thread, th);
            } else {
                Process.killProcess(Process.myPid());
            }
        } catch (Throwable unused) {
            if (uncaughtExceptionHandler != null) {
                uncaughtExceptionHandler.uncaughtException(thread, th);
            } else {
                Process.killProcess(Process.myPid());
            }
        }
    }

    @Override 
    public void onCreate() {
        super.onCreate();
        ControlChannel.APP_CTX = this;
        SharedPreferences sharedPreferences = getSharedPreferences(WirelessCarPlayService.b1, 0);
        if (sharedPreferences.getBoolean("running", false) && !WirelessCarPlayService.L1()) {
            AppLogger.g(this, ProtoMessageBase.e("PREVIOUS_SESSION_PROCESS_EXIT state=", sharedPreferences.getString("state", "unknown")));
            sharedPreferences.edit().putBoolean("running", false).putBoolean("ready_for_source", false).putString("state", "STOPPED").putString("reason", "Application process restarted; previous connection ended").apply();
        }
        AppLogger.g(this, ProtoMessageBase.e("APP_START version=", MainActivity.A2(this)));
        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        r1 r1Var = new r1();
        r1Var.a = this;
        r1Var.b = defaultUncaughtExceptionHandler;
        VarHandle.storeStoreFence();
        Thread.setDefaultUncaughtExceptionHandler(r1Var);
    }
}
