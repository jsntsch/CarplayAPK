package io.carplay.app.prototype;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.LinkProperties;
import android.net.Network;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import io.carplay.app.secured.EcdsaSignature;
import io.carplay.app.secured.Iap2Csm;
import io.carplay.app.secured.AudioSourceCapability;
import io.carplay.app.secured.ReleaseCatalog;
import io.carplay.app.secured.x5;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.net.ServerSocket;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import org.json.JSONObject;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class ReleaseCheckTask implements Runnable {
    public final /* synthetic */ int a;
    public /* synthetic */ Object b;
    public /* synthetic */ Object c;

    public /* synthetic */ ReleaseCheckTask(int i) {
        this.a = i;
    }

    /* JADX WARN: Code duplicated, block: B:84:0x026c A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:86:0x026f  */
    /* JADX WARN: Code duplicated, block: B:89:0x0274  */
    /* JADX WARN: Code duplicated, block: B:90:0x0275  */
    /* JADX WARN: Code duplicated, block: B:93:0x027a  */
    /* JADX WARN: Code duplicated, block: B:94:0x027c  */
    @Override // java.lang.Runnable
    public final void run() {
        EcdsaSignature ecdsaSignatureA = null;
        InstalledPackageIdentity qVarA = null;
        String strA = null;
        boolean z = false;
        String strConcat = null;
        String str = null;
        String str2 = null;
        boolean z2 = false;
        x5 x5Var = null;
        String str3 = null;
        SharedPreferences sharedPreferences = null;
        ReleaseCatalog w5Var = null;
        // jadx 漏了方法级声明：下面多处 catch 用 `e = eN;` 转发异常，再取 e.getClass()，
        // 但方法里没有 `Exception e;`，e 会报「找不到符号」。
        Exception e = null;
        switch (this.a) {
            case 0:
                ((CarPlayCtrlAdvertiser) this.b).l((ServerSocket) this.c);
                return;
            case AudioSourceCapability.c /* 1 */:
                ((WifiNetworkMonitor.a) this.b).e((LinkProperties) this.c);
                return;
            case 2:
                ((WifiNetworkMonitor.c) this.b).g((String) this.c);
                return;
            case AudioSourceCapability.d /* 3 */:
                Context context = (Context) this.b;
                UpdateChecker.a aVar = (UpdateChecker.a) this.c;
                ExecutorService executorService = UpdateChecker.a;
                try {
                    try {
                        qVarA = InstalledPackageIdentity.a(context);
                        String str4 = qVarA.d;
                        if (!str4.equals("5e0af769bf067917a098ded05ddc92a5f4b5cc693c89ad4485746b6827c6a032")) {
                            throw new SecurityException("APK signing identity mismatch");
                        }
                        Network networkA = NetworkSelector.a(context);
                        z = true;
                        AppLogger.g(context, "RELEASE_NETWORK_ROUTE scoped=" + (networkA != null) + " process_binding_unchanged=true values_logged=false");
                        JSONObject jSONObject = new JSONObject(UpdateChecker.d(networkA));
                        if (!"carplus-release-v1".equals(jSONObject.getString("format")) || jSONObject.length() != 3) {
                            throw new IOException("catalog envelope schema");
                        }
                        SharedPreferences sharedPreferences2 = context.getSharedPreferences("release-verification", 0);
                        ReleaseCatalog w5VarK = ReleaseCatalog.k(jSONObject.getString("payload"), jSONObject.getString("signature"), System.currentTimeMillis() / 1000, sharedPreferences2.getLong("revision", 0L));
                        if (!sharedPreferences2.edit().putLong("revision", w5VarK.a).commit()) {
                            throw new IOException("catalog revision persistence failed");
                        }
                        strA = UpdateChecker.a(qVarA.c);
                        long j = qVarA.e;
                        try {
                            if (!qVarA.b(InstalledPackageIdentity.a(context))) {
                                throw new InstalledPackageIdentity.a("installed APK changed during check");
                            }
                            if (!w5VarK.d.equals(qVarA.a)) {
                                throw new InstalledPackageIdentity.a("installed package not in catalog");
                            }
                            try {
                                try {
                                    if (w5VarK.h().a > j) {
                                        str = "发现新版本 " + w5VarK.h().b + "，请覆盖更新后使用；不更新请退出软件。";
                                        strConcat = "UPDATE_REQUIRED";
                                        sharedPreferences = sharedPreferences2;
                                        w5Var = w5VarK;
                                    } else {
                                        sharedPreferences = sharedPreferences2;
                                        w5Var = w5VarK;
                                        if (!w5Var.i(j, qVarA.b, strA, str4)) {
                                            str = "安装包未通过发布与完整性核对，请获取原安装包后覆盖安装。日志仍可导出。";
                                            strConcat = "HASH_OR_RELEASE_MISMATCH";
                                        } else {
                                            if (j >= w5Var.e) {
                                                ecdsaSignatureA = EcdsaSignature.a(jSONObject.getString("payload"), jSONObject.getString("signature"));
                                                try {
                                                    str = w5Var.h().a > j ? "有新版本 " + w5Var.h().b + "，请获取安装包后覆盖安装。" : "已是最新版本 · " + qVarA.b;
                                                    strConcat = "VERIFIED";
                                                    str2 = strA;
                                                    sharedPreferences.edit().putString("installed_sha256", strA).putString("latest_version", w5Var.h().b).putLong("latest_code", w5Var.h().a).putLong("last_success", System.currentTimeMillis()).apply();
                                                    break;
                                                } catch (SecurityException unused2) {
                                                    str = "安装包签名与发布者不符，请重新覆盖安装原安装包。";
                                                    strConcat = "APK_SIGNER_MISMATCH";
                                                    str2 = strA;
                                                    z2 = z;
                                                    context.getSharedPreferences("release-verification", 0).edit().putString("message", str).putString("result", strConcat).putBoolean("allowed", z2).apply();
                                                    AppLogger.g(context, "RELEASE_CHECK result=" + strConcat + " new_session_allowed=" + z2);
                                                    x5Var = UpdateChecker.b;
                                                    synchronized (x5Var) {
                                                        if (!z2) {
                                                            qVarA = null;
                                                        }
                                                        UpdateChecker.c = qVarA;
                                                        if (z2) {
                                                            ecdsaSignatureA = null;
                                                        }
                                                        UpdateChecker.d = ecdsaSignatureA;
                                                        if (z2) {
                                                            str3 = str2;
                                                        } else {
                                                            str3 = null;
                                                        }
                                                        UpdateChecker.e = str3;
                                                        x5Var.a(z2, SystemClock.elapsedRealtime());
                                                        Handler handler2 = new Handler(Looper.getMainLooper());
                                                        y yVar2 = new y();
                                                        yVar2.a = aVar;
                                                        yVar2.b = z2;
                                                        VarHandle.storeStoreFence();
                                                        handler2.post(yVar2);
                                                        return;
                                                    }
                                                } catch (Exception eCaught) {
                                                    strConcat = "CHECK_FAILED_".concat(eCaught.getClass().getSimpleName());
                                                    str = "暂时无法完成版本验证，请开启手机移动数据后重试；车机 WLAN 可能只支持投屏、不提供互联网。已运行的连接不受影响，日志仍可导出。";
                                                    str2 = strA;
                                                    z2 = z;
                                                    context.getSharedPreferences("release-verification", 0).edit().putString("message", str).putString("result", strConcat).putBoolean("allowed", z2).apply();
                                                    AppLogger.g(context, "RELEASE_CHECK result=" + strConcat + " new_session_allowed=" + z2);
                                                    x5Var = UpdateChecker.b;
                                                    synchronized (x5Var) {
                                                        if (!z2) {
                                                            qVarA = null;
                                                        }
                                                        UpdateChecker.c = qVarA;
                                                        if (z2) {
                                                            ecdsaSignatureA = null;
                                                        }
                                                        UpdateChecker.d = ecdsaSignatureA;
                                                        if (z2) {
                                                            str3 = str2;
                                                        } else {
                                                            str3 = null;
                                                        }
                                                        UpdateChecker.e = str3;
                                                        x5Var.a(z2, SystemClock.elapsedRealtime());
                                                        Handler handler4 = new Handler(Looper.getMainLooper());
                                                        y yVar4 = new y();
                                                        yVar4.a = aVar;
                                                        yVar4.b = z2;
                                                        VarHandle.storeStoreFence();
                                                        handler4.post(yVar4);
                                                        return;
                                                    }
                                                }
                                            }
                                            str = "当前版本已停用，请覆盖升级至 " + w5Var.h().b + " 后连接。已运行的连接不受影响。";
                                            strConcat = "OLD_VERSION_DISABLED";
                                        }
                                    }
                                    sharedPreferences.edit().putString("installed_sha256", strA).putString("latest_version", w5Var.h().b).putLong("latest_code", w5Var.h().a).putLong("last_success", System.currentTimeMillis()).apply();
                                    break;
                                } catch (InstalledPackageIdentity.a unused4) {
                                    strA = str2;
                                    str = "无法确认原安装包身份或安装文件已变化，请重新覆盖安装原安装包；日志仍可导出。";
                                    strConcat = "INSTALLATION_IDENTITY_INVALID";
                                    str2 = strA;
                                } catch (SecurityException unused5) {
                                    strA = str2;
                                    str = "安装包签名与发布者不符，请重新覆盖安装原安装包。";
                                    strConcat = "APK_SIGNER_MISMATCH";
                                    str2 = strA;
                                } catch (Exception e2) {
                                    e = e2;
                                    strA = str2;
                                    strConcat = "CHECK_FAILED_".concat(e.getClass().getSimpleName());
                                    str = "暂时无法完成版本验证，请开启手机移动数据后重试；车机 WLAN 可能只支持投屏、不提供互联网。已运行的连接不受影响，日志仍可导出。";
                                    str2 = strA;
                                }
                                context.getSharedPreferences("release-verification", 0).edit().putString("message", str).putString("result", strConcat).putBoolean("allowed", z2).apply();
                                AppLogger.g(context, "RELEASE_CHECK result=" + strConcat + " new_session_allowed=" + z2);
                                x5Var = UpdateChecker.b;
                                synchronized (x5Var) {
                                    if (!z2) {
                                        qVarA = null;
                                    }
                                    UpdateChecker.c = qVarA;
                                    if (z2) {
                                        ecdsaSignatureA = null;
                                    }
                                    UpdateChecker.d = ecdsaSignatureA;
                                    if (z2) {
                                        str3 = str2;
                                    } else {
                                        str3 = null;
                                    }
                                    UpdateChecker.e = str3;
                                    x5Var.a(z2, SystemClock.elapsedRealtime());
                                    Handler handler6 = new Handler(Looper.getMainLooper());
                                    y yVar6 = new y();
                                    yVar6.a = aVar;
                                    yVar6.b = z2;
                                    VarHandle.storeStoreFence();
                                    handler6.post(yVar6);
                                    return;
                                }
                            } catch (Throwable th) {
                                x5 x5Var2 = UpdateChecker.b;
                                synchronized (x5Var2) {
                                    if (!z2) {
                                        qVarA = null;
                                    }
                                    UpdateChecker.c = qVarA;
                                    if (!z2) {
                                        ecdsaSignatureA = null;
                                    }
                                    UpdateChecker.d = ecdsaSignatureA;
                                    UpdateChecker.e = z2 ? str2 : null;
                                    x5Var2.a(z2, SystemClock.elapsedRealtime());
                                    throw th;
                                }
                            }
                        } catch (InstalledPackageIdentity.a e3) {
                            throw e3;
                        } catch (Exception e4) {
                            throw new InstalledPackageIdentity.a("installed identity read failed", e4);
                        }
                    } catch (InstalledPackageIdentity.a e5) {
                        throw e5;
                    } catch (Exception e6) {
                        throw new InstalledPackageIdentity.a("installed identity read failed", e6);
                    }
                } catch (InstalledPackageIdentity.a unused7) {
                    ecdsaSignatureA = null;
                    qVarA = null;
                    strA = null;
                    z = false;
                } catch (SecurityException unused8) {
                    ecdsaSignatureA = null;
                    qVarA = null;
                    strA = null;
                    z = false;
                } catch (Exception e7) {
                    e = e7;
                    ecdsaSignatureA = null;
                    qVarA = null;
                    strA = null;
                    z = false;
                }
                break;
            default:
                ((PairingSession.a) this.b).o((Iap2Csm.a) this.c);
                return;
        }
    }
}
