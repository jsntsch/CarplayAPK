/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 AppLogger。应用内日志：写入 files/connection-log-N.txt，按大小轮转并保留历史。
 *   This file defines AppLogger. In-app logger writing files/connection-log-N.txt with size-based rotation and history.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import io.carplay.app.secured.ProtoMessageBase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;



/**
 * 应用内日志：写入 files/connection-log-N.txt，按大小轮转并保留历史。
 *
 * In-app logger writing files/connection-log-N.txt with size-based rotation and history.
 */
final class AppLogger {
    private AppLogger() {
    }

    public static synchronized void a(Context context) {
        try {
            f(context);
        } catch (IOException unused) {
            Log.w("CarPlusWireless", "Log history rotation unavailable");
        }
        g(context, "SESSION START | Car+ → CarPlay | " + Build.MODEL + " | " + Build.DISPLAY);
    }

    public static synchronized void b(Context context, Thread thread, Throwable th) {
        try {
            g(context, "APP_CRASH thread=" + thread.getName() + " type=" + th.getClass().getName());
            int i = 0;
            while (th != null && i < 4) {
                if (i > 0) {
                    g(context, "CAUSED_BY type=" + th.getClass().getName());
                }
                int i2 = 0;
                for (StackTraceElement stackTraceElement : th.getStackTrace()) {
                    if (stackTraceElement.getClassName().startsWith("io.carplay.")) {
                        int i3 = i2 + 1;
                        if (i2 < 12) {
                            g(context, "  at " + String.valueOf(stackTraceElement));
                        }
                        i2 = i3;
                    }
                }
                i++;
                th = th.getCause();
            }
        } catch (Throwable th2) {
            throw th2;
        }
    }

    private static File c(Context context, int i) {
        return new File(context.getFilesDir(), "connection-log-" + i + ".txt");
    }

    public static String d(Context context) {
        int i;
        StringBuilder sb = new StringBuilder("Car+ → CarPlay 连接历史\n本机保留最近 5 个日志文件，每个约 1 MiB；不自动上传。时间戳使用 UTC（北京时间加 8 小时）。\n");
        ArrayList arrayList = new ArrayList();
        synchronized (AppLogger.class) {
            i = 0;
            for (int i2 = 0; i2 < 5; i2++) {
                File fileC = c(context, i2);
                if (fileC.isFile()) {
                    try {
                        arrayList.add(new a(new FileInputStream(fileC), (int) Math.min(fileC.length(), 1100000L), i2, i));
                    } catch (IOException unused) {
                        sb.append("Could not open history ");
                        sb.append(i2);
                        sb.append('\n');
                    }
                }
            }
        }
        try {
            int size = arrayList.size();
            int i3 = 0;
            while (i3 < size) {
                Object obj = arrayList.get(i3);
                i3++;
                a aVar = (a) obj;
                try {
                    sb.append("\n===== ");
                    sb.append(aVar.i() == 0 ? "当前 / 最近一次" : "历史 HISTORY " + aVar.i());
                    sb.append(" =====\n");
                    sb.append(e(aVar));
                } catch (IOException unused2) {
                    sb.append("Could not read history ");
                    sb.append(aVar.i());
                    sb.append('\n');
                }
            }
            int size2 = arrayList.size();
            while (i < size2) {
                Object obj2 = arrayList.get(i);
                i++;
                try {
                    ((a) obj2).g().close();
                } catch (IOException unused3) {
                }
            }
            return sb.toString();
        } catch (Throwable th) {
            int size3 = arrayList.size();
            while (i < size3) {
                Object obj3 = arrayList.get(i);
                i++;
                try {
                    ((a) obj3).g().close();
                } catch (IOException unused4) {
                }
            }
            throw th;
        }
    }

    private static String e(a aVar) throws IOException {
        int iH = aVar.h();
        byte[] bArr = new byte[iH];
        int i = 0;
        while (i < iH) {
            int i2 = aVar.g().read(bArr, i, iH - i);
            if (i2 == -1) {
                break;
            }
            i += i2;
        }
        return new String(bArr, 0, i, StandardCharsets.UTF_8);
    }

    private static void f(Context context) throws IOException {
        for (int i = 4; i >= 1; i--) {
            File fileC = c(context, i - 1);
            File fileC2 = c(context, i);
            if (fileC.isFile()) {
                Files.move(fileC.toPath(), fileC2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public static synchronized void g(Context context, String str) {
        try {
            String lowerCase = str.toLowerCase(Locale.ROOT);
            if (lowerCase.contains("bodyhex=") || lowerCase.contains("wirehex=") || lowerCase.contains("framehex=") || lowerCase.contains("payloadhex=") || lowerCase.contains("privatekey=") || lowerCase.contains("seed=") || lowerCase.contains("sharedsecret=")) {
                return;
            }
            try {
                if (c(context, 0).length() > 1048576) {
                    f(context);
                }
                if (str.length() > 4000) {
                    str = str.substring(0, 4000).concat(" [truncated]");
                }
                FileOutputStream fileOutputStream = new FileOutputStream(c(context, 0), true);
                try {
                    fileOutputStream.write((String.valueOf(Instant.now()) + " " + str + "\n").getBytes(StandardCharsets.UTF_8));
                    fileOutputStream.close();
                } catch (Throwable th) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            } catch (IOException unused) {
                Log.w("CarPlusWireless", "Persistent operation log unavailable");
            }
        } catch (Throwable th3) {
            throw th3;
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class a extends ProtoMessageBase {
        private final FileInputStream a;
        private final int b;
        private final int c;

        private a(FileInputStream fileInputStream, int i, int i2) {
            this.a = fileInputStream;
            this.b = i;
            this.c = i2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.b == aVar.b && this.c == aVar.c && Objects.equals(this.a, aVar.a);
        }

        public FileInputStream g() {
            return this.a;
        }

        public int h() {
            return this.b;
        }

        public final int hashCode() {
            int i = this.b;
            int i2 = this.c;
            return Objects.hashCode(this.a) + (((i * 31) + i2) * 31);
        }

        public int i() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {this.a, Integer.valueOf(this.b), Integer.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
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

        public  a(FileInputStream fileInputStream, int i, int i2, int i3) {
            this(fileInputStream, i, i2);
        }
    }
}
