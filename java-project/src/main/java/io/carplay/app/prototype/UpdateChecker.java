package io.carplay.app.prototype;

import android.net.Network;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import io.carplay.app.secured.EcdsaSignature;
import io.carplay.app.secured.PcmConverter;
import io.carplay.app.secured.SyntheticRefs;
import io.carplay.app.secured.ReleaseCatalog;
import io.carplay.app.secured.x5;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.VarHandle;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;

/* JADX INFO: loaded from: classes.dex */
/**
 * UpdateChecker
 *
 * <p>原混淆名：{@code io.carplay.app.prototype.z}（jadx 产物 z.java）
 *
 * <p>命名依据：APK integrity + gitee 更新地址；对应 hypercar update/UpdateChecker.kt
 */
final class UpdateChecker {
    public static final ExecutorService a = Executors.newSingleThreadExecutor(new x());
    public static final x5 b = new x5();
    public static InstalledPackageIdentity c;
    public static EcdsaSignature d;
    public static String e;

    public interface a {
        void a(boolean z);
    }

    private UpdateChecker() {
    }

    public static String a(String str) throws NoSuchAlgorithmException, IOException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        FileInputStream fileInputStream = new FileInputStream(str);
        try {
            byte[] bArr = new byte[PcmConverter.t];
            long j = 0;
            while (true) {
                int i = fileInputStream.read(bArr);
                if (i == -1) {
                    fileInputStream.close();
                    byte[] bArrDigest = messageDigest.digest();
                    StringBuilder sb = new StringBuilder();
                    for (byte b2 : bArrDigest) {
                        sb.append(String.format(Locale.ROOT, "%02x", Integer.valueOf(b2 & 255)));
                    }
                    return sb.toString();
                }
                j += (long) i;
                if (j > 67108864) {
                    throw new IOException("APK integrity size limit");
                }
                messageDigest.update(bArr, 0, i);
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

    public static void b(MainActivity mainActivity, a aVar) {
        Handler handler = new Handler(Looper.getMainLooper());
        y yVar = new y();
        yVar.a = aVar;
        yVar.b = true;
        VarHandle.storeStoreFence();
        handler.post(yVar);
    }

    public static boolean c() {
        return false;
    }

    public static String d(Network network) throws IOException {
        long jElapsedRealtime = SystemClock.elapsedRealtime() + 10000;
        URI uriCreate = URI.create("https://gitee.com/eyessx/carplus-carplay-updates/raw/master/version.json");
        for (int i = 0; i < 3; i++) {
            if (SystemClock.elapsedRealtime() > jElapsedRealtime) {
                SyntheticRefs.c("catalog redirect deadline");
                return null;
            }
            URL url = uriCreate.toURL();
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) (network == null ? url.openConnection() : network.openConnection(url));
            httpsURLConnection.setInstanceFollowRedirects(false);
            httpsURLConnection.setConnectTimeout(4000);
            httpsURLConnection.setReadTimeout(4000);
            httpsURLConnection.setUseCaches(false);
            httpsURLConnection.setRequestProperty("Cache-Control", "no-cache");
            httpsURLConnection.setRequestProperty("Accept", "application/json");
            try {
                int responseCode = httpsURLConnection.getResponseCode();
                if (responseCode != 301 && responseCode != 302 && responseCode != 303 && responseCode != 307 && responseCode != 308) {
                    if (responseCode != 200) {
                        throw new IOException("catalog HTTP unavailable");
                    }
                    InputStream inputStream = httpsURLConnection.getInputStream();
                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] bArr = new byte[1024];
                        while (true) {
                            int i2 = inputStream.read(bArr);
                            if (i2 == -1) {
                                String string = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
                                inputStream.close();
                                httpsURLConnection.disconnect();
                                return string;
                            }
                            if (byteArrayOutputStream.size() + i2 > 16384 || SystemClock.elapsedRealtime() > jElapsedRealtime) {
                                break;
                            }
                            byteArrayOutputStream.write(bArr, 0, i2);
                        }
                        throw new IOException("catalog size/deadline");
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
                uriCreate = ReleaseCatalog.j(uriCreate, httpsURLConnection.getHeaderField("Location"));
                httpsURLConnection.disconnect();
            } catch (Throwable th3) {
                httpsURLConnection.disconnect();
                throw th3;
            }
        }
        SyntheticRefs.c("too many catalog redirects");
        return null;
    }
}
