/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 CarLifeBridge。原生组件桥接：启动 Car+ / CarLife 投屏界面、查询安装状态并传递通道句柄。
 *   This file defines CarLifeBridge. Native bridge starting the Car+ / CarLife projection UI, querying installation state and handing over channel handles.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import io.carplay.app.secured.ProtoMessageBase;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;



/**
 * 原生组件桥接：启动 Car+ / CarLife 投屏界面、查询安装状态并传递通道句柄。
 *
 * Native bridge starting the Car+ / CarLife projection UI, querying installation state and handing over channel handles.
 */
public final class CarLifeBridge {
    public static final String a = "com.oplus.ocar";
    public static final String b = "com.baidu.carlife.oppo";
    private static final String c = "com.oplus.ocar.CarlifeActivity";
    private static final String d = "com.baidu.carlife.service.CarlifeConnectService";
    private static final String e = "e49802409584ce53152a9000820a51e4fa8a723b7bcc263e335240acf100bf9e";
    private static final String f = "fc98dae63ad39626c8c67fbe83f2f06f74932a9cd146b92cecfc6a047a904386";
    private static final String g = "509e03e376b676b25adc41c3dbda6dfa5d0ff12dba1c8c0ca32ad110eea78667";
    private static final String h = "34ba512ff2b5d88cb8a8870f3e1c86771d7452a3354bfec62aae69ec2348c781";
    private static final long i = 536870912;
    private static volatile a j;

    /**
     * 枚举：本子系统使用的状态/类型取值集合。
     *
     * Enumeration: state/type values used by this subsystem.
     */
    public enum ReviewState {
        REVIEWED,
        CANDIDATE,
        BLOCKED
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class d extends ProtoMessageBase {
        private final ReviewState a;
        private final String b;
        private final long c;
        private final long d;
        private final boolean e;

        public d(ReviewState cVar, String str, long j, long j2, boolean z) {
            this.a = cVar;
            this.b = str;
            this.c = j;
            this.d = j2;
            this.e = z;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof d)) {
                return false;
            }
            d dVar = (d) obj;
            return this.e == dVar.e && this.c == dVar.c && this.d == dVar.d && Objects.equals(this.a, dVar.a) && Objects.equals(this.b, dVar.b);
        }

        public boolean g() {
            return this.a != ReviewState.BLOCKED;
        }

        public String h() {
            return "NATIVE_COMPONENT_SUPPORT status=" + String.valueOf(this.a) + " reason=" + this.b + " host_code=" + this.c + " plugin_code=" + this.d + " producer_entry_observed=" + this.e + " runtime_handshake_required=true compatibility_confirmed=false";
        }

        public final int hashCode() {
            boolean z = this.e;
            long j = this.c;
            long j2 = this.d;
            ReviewState cVar = this.a;
            String str = this.b;
            return Objects.hashCode(str) + ((Objects.hashCode(cVar) + ProtoMessageBase.a(j2, ProtoMessageBase.a(j, Boolean.hashCode(z) * 31, 31), 31)) * 31);
        }

        public long i() {
            return this.c;
        }

        public ReviewState j() {
            return this.a;
        }

        public long k() {
            return this.d;
        }

        public boolean l() {
            return this.e;
        }

        public String m() {
            return this.b;
        }

        public boolean n() {
            return this.a == ReviewState.REVIEWED;
        }

        public String o() {
            if (n()) {
                return "原厂组件已核验";
            }
            return g() ? "原厂组件，兼容性待实际连接确认" : "当前原生 Car+ 组件不可用，请查看兼容说明。";
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b, Long.valueOf(this.c), Long.valueOf(this.d), Boolean.valueOf(this.e)};
            String[] strArrSplit = "a;b;c;d;e".length() == 0 ? new String[0] : "a;b;c;d;e".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(d.class, sb, "[");
            for (int i = 0; i < strArrSplit.length; i++) {
                sb.append(strArrSplit[i]);
                sb.append("=");
                sb.append(objArr[i]);
                if (i != strArrSplit.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    /**
     * 扩展实现：在 Exception 基础上补充 CarLifeBridge 需要的局部行为。
     *
     * Extension of Exception adding the local behaviour CarLifeBridge needs.
     */
    public static final class f extends Exception {
        private static final long b = 1;
        final String a;

        public f(String str) {
            this.a = str;
        }
    }

    private CarLifeBridge() {
    }

    private static d a(String str, long j2, long j3) {
        j = null;
        return new d(ReviewState.BLOCKED, str, j2, j3, false);
    }

    private static boolean b(PackageManager packageManager, ComponentName componentName) {
        return h(packageManager.getComponentEnabledSetting(componentName));
    }

    private static boolean c(PackageManager packageManager, String str, ApplicationInfo applicationInfo) {
        return applicationInfo != null && applicationInfo.enabled && h(packageManager.getApplicationEnabledSetting(str));
    }

    private static List<e> d(ApplicationInfo applicationInfo) throws f {
        if (applicationInfo == null || applicationInfo.sourceDir == null) {
            throw new f("APK_PATH_UNAVAILABLE");
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(applicationInfo.sourceDir);
        String[] strArr = applicationInfo.splitSourceDirs;
        if (strArr != null) {
            if (strArr.length > 31) {
                throw new f("SPLIT_SET_BOUNDS");
            }
            arrayList.addAll(Arrays.asList(strArr));
        }
        ArrayList arrayList2 = new ArrayList();
        HashSet hashSet = new HashSet();
        int size = arrayList.size();
        long j2 = 0;
        int i2 = 0;
        while (i2 < size) {
            Object obj = arrayList.get(i2);
            i2++;
            String str = (String) obj;
            if (str == null || !hashSet.add(str)) {
                throw new f("SPLIT_SET_INVALID");
            }
            File file = new File(str);
            long length = file.length();
            
            
            if (file.isFile() && file.canRead() && length >= 1 && length <= i) {
                j2 += length;
                if (j2 <= 1073741824) {
                    arrayList2.add(new e(str, length, file.lastModified(), 0));
                    continue;
                }
            }
            throw new f("APK_FILE_BOUNDS");
        }
        return List.copyOf(arrayList2);
    }

    private static byte[] e(ZipFile zipFile, String str, int i2) throws IOException, f {
        ZipEntry entry = zipFile.getEntry(str);
        int i3 = 0;
        if (entry == null || entry.isDirectory() || entry.getSize() < i2) {
            return new byte[0];
        }
        InputStream inputStream = zipFile.getInputStream(entry);
        try {
            byte[] bArrCopyOf = new byte[i2];
            while (i3 < i2) {
                int i4 = inputStream.read(bArrCopyOf, i3, i2 - i3);
                if (i4 == -1) {
                    break;
                }
                if (i4 == 0) {
                    throw new f("APK_HEADER_READ_FAILED");
                }
                i3 += i4;
            }
            if (i3 != i2) {
                bArrCopyOf = Arrays.copyOf(bArrCopyOf, i3);
            }
            if (inputStream != null) {
                inputStream.close();
            }
            return bArrCopyOf;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static d f(Context context) {
        long hostVersion = -1L;
        long pluginVersion = -1L;
        boolean serviceReady = false;
        try {
            PackageManager pm = context.getPackageManager();

            
            if (!j(pm, a, e) && !j(pm, a, f)) {
                return a("HOST_SIGNER", -1L, -1L);
            }
            
            if (!j(pm, b, g)) {
                return a("PLUGIN_SIGNER", -1L, -1L);
            }

            PackageInfo hostInfo = pm.getPackageInfo(a, PackageManager.PackageInfoFlags.of(0L));
            hostVersion = hostInfo.getLongVersionCode();
            PackageInfo pluginInfo = pm.getPackageInfo(b, PackageManager.PackageInfoFlags.of(0L));
            pluginVersion = pluginInfo.getLongVersionCode();

            
            if (!a.equals(hostInfo.packageName) || !b.equals(pluginInfo.packageName)) {
                return a("PACKAGE_IDENTITY", hostVersion, pluginVersion);
            }
            
            if (!c(pm, a, hostInfo.applicationInfo) || !c(pm, b, pluginInfo.applicationInfo)) {
                return a("APPLICATION_DISABLED", hostVersion, pluginVersion);
            }

            
            ComponentName hostComponent = new ComponentName(a, c);
            ComponentName pluginComponent = new ComponentName(b, d);
            ActivityInfo activityInfo =
                    pm.getActivityInfo(hostComponent, PackageManager.ComponentInfoFlags.of(0L));
            if (!activityInfo.enabled
                    || !activityInfo.exported
                    || activityInfo.applicationInfo == null
                    || !activityInfo.applicationInfo.enabled
                    || !a.equals(activityInfo.packageName)
                    || !c.equals(activityInfo.name)
                    || !b(pm, hostComponent)) {
                return a("HOST_ENTRY_UNAVAILABLE", hostVersion, pluginVersion);
            }
            
            if (activityInfo.permission != null
                    && context.checkSelfPermission(activityInfo.permission) != 0) {
                return a("HOST_ENTRY_UNAVAILABLE", hostVersion, pluginVersion);
            }

            
            try {
                ServiceInfo serviceInfo =
                        pm.getServiceInfo(pluginComponent, PackageManager.ComponentInfoFlags.of(0L));
                serviceReady = serviceInfo.enabled
                        && serviceInfo.exported
                        && serviceInfo.applicationInfo != null
                        && serviceInfo.applicationInfo.enabled
                        && b.equals(serviceInfo.packageName)
                        && d.equals(serviceInfo.name)
                        && b(pm, pluginComponent);
            } catch (Exception unused) {
                serviceReady = false;
            }

            
            b identity = g(hostInfo, pluginInfo);
            ReviewState state;
            a cached = CarLifeBridge.j;
            if (cached != null && cached.i().equals(identity)) {
                state = cached.j();
            } else {
                k(identity.i());
                if ("8.6.8".equals(pluginInfo.versionName)
                        && pluginVersion == 260L
                        && identity.i().size() == 1
                        && h.equals(i(identity.i().get(0)))) {
                    state = ReviewState.REVIEWED;
                } else {
                    state = ReviewState.CANDIDATE;
                }
            }

            
            PackageInfo hostAgain = pm.getPackageInfo(a, PackageManager.PackageInfoFlags.of(0L));
            PackageInfo pluginAgain = pm.getPackageInfo(b, PackageManager.PackageInfoFlags.of(0L));
            if (!identity.equals(g(hostAgain, pluginAgain))) {
                return a("INSTALLATION_CHANGED", hostVersion, pluginVersion);
            }
            
            if ((!j(pm, a, e) && !j(pm, a, f)) || !j(pm, b, g)) {
                return a("SIGNER_CHANGED", hostVersion, pluginVersion);
            }
            
            if (!c(pm, a, hostAgain.applicationInfo)
                    || !c(pm, b, pluginAgain.applicationInfo)) {
                return a("APPLICATION_DISABLED", hostVersion, pluginVersion);
            }

            CarLifeBridge.j = new a(identity, state);
            return new d(state,
                    state == ReviewState.REVIEWED
                            ? "REVIEWED_ARTIFACT" : "OEM_STRUCTURE_RUNTIME_PENDING",
                    hostVersion, pluginVersion, serviceReady);
        } catch (CarLifeBridge.f e2) {
            return a(e2.a, hostVersion, pluginVersion);
        } catch (Exception unused) {
            return a("COMPONENT_INSPECTION_UNAVAILABLE", hostVersion, pluginVersion);
        }
    }

    private static b g(PackageInfo packageInfo, PackageInfo packageInfo2) throws f {
        if (a.equals(packageInfo.packageName) && b.equals(packageInfo2.packageName)) {
            return new b(packageInfo.getLongVersionCode(), packageInfo.lastUpdateTime, packageInfo2.getLongVersionCode(), packageInfo2.lastUpdateTime, packageInfo2.versionName, d(packageInfo.applicationInfo), d(packageInfo2.applicationInfo), 0);
        }
        throw new f("PACKAGE_IDENTITY");
    }

    private static boolean h(int i2) {
        return i2 == 0 || i2 == 1;
    }

    private static String i(e eVar) throws NoSuchAlgorithmException, IOException, f {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        FileInputStream fileInputStream = new FileInputStream(eVar.a);
        try {
            byte[] bArr = new byte[262144];
            long j2 = 0;
            while (true) {
                int i2 = fileInputStream.read(bArr);
                if (i2 == -1) {
                    fileInputStream.close();
                    StringBuilder sb = new StringBuilder(64);
                    for (byte b2 : messageDigest.digest()) {
                        sb.append(Character.forDigit((b2 >>> 4) & 15, 16));
                        sb.append(Character.forDigit(b2 & 15, 16));
                    }
                    return sb.toString();
                }
                j2 += (long) i2;
                if (j2 > i) {
                    throw new f("APK_FILE_BOUNDS");
                }
                messageDigest.update(bArr, 0, i2);
            }
        } catch (Throwable th) {
            try {
                fileInputStream.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    private static boolean j(PackageManager packageManager, String str, String str2) {
        byte[] bArr = new byte[32];
        for (int i2 = 0; i2 < 32; i2++) {
            int i3 = i2 * 2;
            bArr[i2] = (byte) Integer.parseInt(str2.substring(i3, i3 + 2), 16);
        }
        return packageManager.hasSigningCertificate(str, bArr, 1);
    }

    private static void k(List<e> list) throws f, IOException {
        byte b2;
        byte b3;
        boolean z = false;
        boolean z2 = false;
        for (int i2 = 0; i2 < list.size(); i2++) {
            ZipFile zipFile = new ZipFile(list.get(i2).a);
            try {
                HashSet hashSet = new HashSet();
                Enumeration<? extends ZipEntry> enumerationEntries = zipFile.entries();
                int i3 = 0;
                while (enumerationEntries.hasMoreElements()) {
                    ZipEntry zipEntryNextElement = enumerationEntries.nextElement();
                    i3++;
                    if (i3 > 50000) {
                        throw new f("APK_ENTRY_BOUNDS");
                    }
                    String name = zipEntryNextElement.getName();
                    if (name.equals("AndroidManifest.xml") || name.equals("classes.dex")) {
                        if (!hashSet.add(name)) {
                            throw new f("APK_DUPLICATE_CAPABILITY_ENTRY");
                        }
                    }
                }
                if (i2 == 0) {
                    boolean zEquals = Arrays.equals(e(zipFile, "AndroidManifest.xml", 4), new byte[]{3, 0, 8, 0});
                    byte[] bArrE = e(zipFile, "classes.dex", 8);
                    boolean z3 = bArrE.length == 8 && bArrE[0] == 100 && bArrE[1] == 101 && bArrE[2] == 120 && bArrE[3] == 10 && bArrE[4] == 48 && (b2 = bArrE[5]) >= 51 && b2 <= 52 && (b3 = bArrE[6]) >= 48 && b3 <= 57 && bArrE[7] == 0;
                    z = zEquals;
                    z2 = z3;
                }
                zipFile.close();
            } catch (Throwable th) {
                try {
                    zipFile.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        }
        if (!z || !z2) {
            throw new f("PROTOCOL_STRUCTURE_UNAVAILABLE");
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class a extends ProtoMessageBase {
        private final b a;
        private final ReviewState b;

        private a(b bVar, ReviewState cVar) {
            this.a = bVar;
            this.b = cVar;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return Objects.equals(this.a, aVar.a) && Objects.equals(this.b, aVar.b);
        }

        public final int hashCode() {
            b bVar = this.a;
            ReviewState cVar = this.b;
            return Objects.hashCode(cVar) + (Objects.hashCode(bVar) * 31);
        }

        public b i() {
            return this.a;
        }

        public ReviewState j() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(a.class, sb, "[");
            for (int i = 0; i < strArrSplit.length; i++) {
                sb.append(strArrSplit[i]);
                sb.append("=");
                sb.append(objArr[i]);
                if (i != strArrSplit.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        public  a(b bVar, ReviewState cVar, int i) {
            this(bVar, cVar);
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class e extends ProtoMessageBase {
        private final String a;
        private final long b;
        private final long c;

        private e(String str, long j, long j2) {
            this.a = str;
            this.b = j;
            this.c = j2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof e)) {
                return false;
            }
            e eVar = (e) obj;
            return this.b == eVar.b && this.c == eVar.c && Objects.equals(this.a, eVar.a);
        }

        public long h() {
            return this.c;
        }

        public final int hashCode() {
            long j = this.b;
            long j2 = this.c;
            String str = this.a;
            return Objects.hashCode(str) + ProtoMessageBase.a(j2, Long.hashCode(j) * 31, 31);
        }

        public String i() {
            return this.a;
        }

        public long j() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {this.a, Long.valueOf(this.b), Long.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(e.class, sb, "[");
            for (int i = 0; i < strArrSplit.length; i++) {
                sb.append(strArrSplit[i]);
                sb.append("=");
                sb.append(objArr[i]);
                if (i != strArrSplit.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        public  e(String str, long j, long j2, int i) {
            this(str, j, j2);
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class b extends ProtoMessageBase {
        private final long a;
        private final long b;
        private final long c;
        private final long d;
        private final String e;
        private final List<e> f;
        private final List<e> g;

        private b(long j, long j2, long j3, long j4, String str, List<e> list, List<e> list2) {
            this.a = j;
            this.b = j2;
            this.c = j3;
            this.d = j4;
            this.e = str;
            this.f = list;
            this.g = list2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && this.b == bVar.b && this.c == bVar.c && this.d == bVar.d && Objects.equals(this.e, bVar.e) && Objects.equals(this.f, bVar.f) && Objects.equals(this.g, bVar.g);
        }

        public long h() {
            return this.a;
        }

        public final int hashCode() {
            long j = this.a;
            long j2 = this.b;
            long j3 = this.c;
            long j4 = this.d;
            String str = this.e;
            List<e> list = this.f;
            List<e> list2 = this.g;
            return Objects.hashCode(list2) + ((Objects.hashCode(list) + ProtoMessageBase.b(str, ProtoMessageBase.a(j4, ProtoMessageBase.a(j3, ProtoMessageBase.a(j2, Long.hashCode(j) * 31, 31), 31), 31), 31)) * 31);
        }

        public List<e> i() {
            return this.f;
        }

        public long j() {
            return this.b;
        }

        public long k() {
            return this.c;
        }

        public List<e> l() {
            return this.g;
        }

        public long m() {
            return this.d;
        }

        public String n() {
            return this.e;
        }

        public final String toString() {
            Object[] objArr = {Long.valueOf(this.a), Long.valueOf(this.b), Long.valueOf(this.c), Long.valueOf(this.d), this.e, this.f, this.g};
            String[] strArrSplit = "a;b;c;d;e;f;g".length() == 0 ? new String[0] : "a;b;c;d;e;f;g".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(b.class, sb, "[");
            for (int i = 0; i < strArrSplit.length; i++) {
                sb.append(strArrSplit[i]);
                sb.append("=");
                sb.append(objArr[i]);
                if (i != strArrSplit.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        public  b(long j, long j2, long j3, long j4, String str, List list, List list2, int i) {
            this(j, j2, j3, j4, str, list, list2);
        }
    }
}
