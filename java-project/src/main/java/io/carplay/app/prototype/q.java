package io.carplay.app.prototype;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.security.MessageDigest;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
final class q {
    public String a;
    public String b;
    public String c;
    public String d;
    public long e;
    public long f;
    public long g;
    public long h;

    public static final class a extends IOException {
    }

    public static q a(Context context) throws a, PackageManager.NameNotFoundException {
        ApplicationInfo applicationInfo;
        SigningInfo signingInfo;
        String packageName = context.getPackageName();
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(134217728L));
        if (!"io.carplay.app.prototype".equals(packageName) || !packageName.equals(packageInfo.packageName) || (applicationInfo = packageInfo.applicationInfo) == null || (signingInfo = packageInfo.signingInfo) == null) {
            throw new a("installed package identity unavailable");
        }
        String[] strArr = applicationInfo.splitSourceDirs;
        if (strArr != null && strArr.length != 0) {
            throw new a("unlisted APK splits");
        }
        Signature[] apkContentsSigners = signingInfo.getApkContentsSigners();
        if (apkContentsSigners == null || apkContentsSigners.length != 1) {
            throw new a("multiple or missing APK signers");
        }
        byte[] bArrDigest = MessageDigest.getInstance("SHA-256").digest(apkContentsSigners[0].toByteArray());
        StringBuilder sb = new StringBuilder();
        for (byte b : bArrDigest) {
            sb.append(String.format(Locale.ROOT, "%02x", Integer.valueOf(b & 255)));
        }
        String str = packageInfo.applicationInfo.sourceDir;
        if (str == null || packageInfo.versionName == null) {
            throw new a("installed APK identity unavailable");
        }
        File file = new File(str);
        long length = file.length();
        long jLastModified = file.lastModified();
        if (!file.isFile() || length <= 0 || length > 67108864 || jLastModified <= 0) {
            throw new a("installed APK metadata unavailable");
        }
        String str2 = packageInfo.versionName;
        String string = sb.toString();
        long longVersionCode = packageInfo.getLongVersionCode();
        long j = packageInfo.lastUpdateTime;
        q qVar = new q();
        qVar.a = packageName;
        qVar.b = str2;
        qVar.c = str;
        qVar.d = string;
        qVar.e = longVersionCode;
        qVar.f = j;
        qVar.g = length;
        qVar.h = jLastModified;
        VarHandle.storeStoreFence();
        return qVar;
    }

    public final boolean b(q qVar) {
        return this.a.equals(qVar.a) && this.b.equals(qVar.b) && this.c.equals(qVar.c) && this.d.equals(qVar.d) && this.e == qVar.e && this.f == qVar.f && this.g == qVar.g && this.h == qVar.h;
    }
}
