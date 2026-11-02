package io.carplay.app.prototype;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Process;
import io.carplay.app.secured.ControlChannel;
import io.carplay.app.secured.r1;
import io.carplay.app.secured.w1;
import java.lang.invoke.VarHandle;

/* JADX INFO: loaded from: classes.dex */
public final class CarPlusApplication extends Application {
    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void b(Thread.UncaughtExceptionHandler uncaughtExceptionHandler, Thread thread, Throwable th) {
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

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        ControlChannel.APP_CTX = this;
        SharedPreferences sharedPreferences = getSharedPreferences(WirelessCarPlayService.b1, 0);
        if (sharedPreferences.getBoolean("running", false) && !WirelessCarPlayService.L1()) {
            AppLogger.g(this, w1.e("PREVIOUS_SESSION_PROCESS_EXIT state=", sharedPreferences.getString("state", "unknown")));
            sharedPreferences.edit().putBoolean("running", false).putBoolean("ready_for_source", false).putString("state", "STOPPED").putString("reason", "Application process restarted; previous connection ended").apply();
        }
        AppLogger.g(this, w1.e("APP_START version=", MainActivity.A2(this)));
        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        r1 r1Var = new r1();
        r1Var.a = this;
        r1Var.b = defaultUncaughtExceptionHandler;
        VarHandle.storeStoreFence();
        Thread.setDefaultUncaughtExceptionHandler(r1Var);
    }
}
