package io.carplay.app.prototype;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import io.carplay.app.secured.w1;
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

/* JADX INFO: loaded from: classes.dex */
/**
 * CarLifeBridge
 *
 * <p>原混淆名：{@code io.carplay.app.prototype.u}（jadx 产物 u.java）
 *
 * <p>命名依据：com.oplus.ocar / com.baidu.carlife.oppo —— CarLife/Car+ 虚拟屏桥
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

    public enum c {
        REVIEWED,
        CANDIDATE,
        BLOCKED
    }

    public static final class d extends w1 {
        private final c a;
        private final String b;
        private final long c;
        private final long d;
        private final boolean e;

        public d(c cVar, String str, long j, long j2, boolean z) {
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
            return this.a != c.BLOCKED;
        }

        public String h() {
            return "NATIVE_COMPONENT_SUPPORT status=" + String.valueOf(this.a) + " reason=" + this.b + " host_code=" + this.c + " plugin_code=" + this.d + " producer_entry_observed=" + this.e + " runtime_handshake_required=true compatibility_confirmed=false";
        }

        public final int hashCode() {
            boolean z = this.e;
            long j = this.c;
            long j2 = this.d;
            c cVar = this.a;
            String str = this.b;
            return Objects.hashCode(str) + ((Objects.hashCode(cVar) + w1.a(j2, w1.a(j, Boolean.hashCode(UpdateChecker) * 31, 31), 31)) * 31);
        }

        public long i() {
            return this.c;
        }

        public c j() {
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
            return this.a == c.REVIEWED;
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
            w1.f(d.class, sb, "[");
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
        return new d(c.BLOCKED, str, j2, j3, false);
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
                }
            }
            throw new f("APK_FILE_BOUNDS");
        }
        return List.copyOf(arrayList2);
    }

    private static byte[] e(ZipFile zipFile, String str, int i2) throws IOException {
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

    /* JADX WARN: Code restructure failed: missing block: B:47:0x00c8, code lost:
    
        if (r23.checkSelfPermission(r1) != 0) goto L48;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r1v21 */
    /* JADX WARN: Type inference failed for: r1v22 */
    /* JADX WARN: Type inference failed for: r1v23 */
    /* JADX WARN: Type inference failed for: r1v24 */
    /* JADX WARN: Type inference failed for: r1v28 */
    /* JADX WARN: Type inference failed for: r1v3 */
    /* JADX WARN: Type inference failed for: r1v4 */
    /* JADX WARN: Type inference failed for: r1v5 */
    /* JADX WARN: Type inference failed for: r1v6 */
    /* JADX WARN: Type inference failed for: r6v0 */
    /* JADX WARN: Type inference failed for: r6v1, types: [long] */
    /* JADX WARN: Type inference failed for: r6v12 */
    /* JADX WARN: Type inference failed for: r6v13 */
    /* JADX WARN: Type inference failed for: r6v14 */
    /* JADX WARN: Type inference failed for: r6v2, types: [long] */
    /* JADX WARN: Type inference failed for: r6v6 */
    /* JADX WARN: Type inference failed for: r6v8 */
    /* JADX WARN: Unreachable blocks removed: 1, instructions: 2 */
    /* JADX WARN: Unreachable blocks removed: 2, instructions: 3 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static io.carplay.app.prototype.CarLifeBridge.d f(android.content.Context r23) {
        /*
            Method dump skipped, instruction units count: 544
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: io.carplay.app.prototype.CarLifeBridge.f(android.content.Context):io.carplay.app.prototype.CarLifeBridge$d");
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

    private static String i(e eVar) throws NoSuchAlgorithmException, IOException {
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
                while (true) {
                    if (enumerationEntries.hasMoreElements()) {
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

    public static final class a extends w1 {
        private final b a;
        private final c b;

        private a(b bVar, c cVar) {
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
            c cVar = this.b;
            return Objects.hashCode(cVar) + (Objects.hashCode(bVar) * 31);
        }

        public b i() {
            return this.a;
        }

        public c j() {
            return this.b;
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
            StringBuilder sb = new StringBuilder();
            w1.f(a.class, sb, "[");
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

        public /* synthetic */ a(b bVar, c cVar, int i) {
            this(bVar, cVar);
        }
    }

    public static final class e extends w1 {
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
            return Objects.hashCode(str) + w1.a(j2, Long.hashCode(AirplayServiceProbe) * 31, 31);
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
            w1.f(e.class, sb, "[");
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

        public /* synthetic */ e(String str, long j, long j2, int i) {
            this(str, j, j2);
        }
    }

    public static final class b extends w1 {
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
            return Objects.hashCode(list2) + ((Objects.hashCode(list) + w1.b(str, w1.a(j4, w1.a(j3, w1.a(j2, Long.hashCode(AirplayServiceProbe) * 31, 31), 31), 31), 31)) * 31);
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
            w1.f(b.class, sb, "[");
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

        public /* synthetic */ b(long j, long j2, long j3, long j4, String str, List list, List list2, int i) {
            this(j, j2, j3, j4, str, list, list2);
        }
    }
}
