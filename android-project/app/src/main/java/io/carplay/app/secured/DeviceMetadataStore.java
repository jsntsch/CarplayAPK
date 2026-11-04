/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 DeviceMetadataStore。设备元数据存储：校验并保存设备 UUID、显示名等配对元数据。
 *   This file defines DeviceMetadataStore. Device metadata store: validates and persists device UUID and display name used in pairing.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Supplier;


/**
 * 设备元数据存储：校验并保存设备 UUID、显示名等配对元数据。
 *
 * Device metadata store: validates and persists device UUID and display name used in pairing.
 */
public final class DeviceMetadataStore {
    private final String a;
    private final String b;
    private final b c;

    /**
     * 枚举：本子系统使用的状态/类型取值集合。
     *
     * Enumeration: state/type values used by this subsystem.
     */
    public enum b {
        BLUETOOTH_ADAPTER,
        ANDROID_MODEL
    }

    public DeviceMetadataStore(String str, String str2, b bVar) {
        if (str == null || !str.matches("[a-fA-F0-9]{8}(?:-[a-fA-F0-9]{4}){3}-[a-fA-F0-9]{12}")) {
            SyntheticRefs.b("invalid temporary device UUID");
            throw null;
        }
        String strD = d(str2);
        if (strD == null) {
            SyntheticRefs.b("invalid device display name");
            throw null;
        }
        this.a = UUID.fromString(str).toString();
        this.b = strD;
        Objects.requireNonNull(bVar);
        this.c = bVar;
    }

    public static String d(String str) {
        if (str != null && !str.isBlank()) {
            int i = 0;
            while (i < str.length()) {
                char cCharAt = str.charAt(i);
                if (Character.isISOControl(cCharAt)) {
                    return null;
                }
                if (Character.isHighSurrogate(cCharAt)) {
                    i++;
                    if (i >= str.length() || !Character.isLowSurrogate(str.charAt(i))) {
                        return null;
                    }
                } else if (Character.isLowSurrogate(cCharAt)) {
                    return null;
                }
                i++;
            }
            if (str.getBytes(StandardCharsets.UTF_8).length <= 248) {
                return str;
            }
        }
        return null;
    }

    public String a() {
        return this.b;
    }

    public int b() {
        return this.b.getBytes(StandardCharsets.UTF_8).length;
    }

    public b c() {
        return this.c;
    }

    public String e() {
        return this.a;
    }

    /**
     * 可关闭资源：持有需要显式释放的通道或缓冲区。
     *
     * Closeable resource holding a channel or buffer that must be released.
     */
    public static final class a implements AutoCloseable {
        private final c a;
        private final DeviceMetadataStore b;
        private boolean c;

        private a(c cVar, DeviceMetadataStore i5Var) {
            this.a = cVar;
            this.b = i5Var;
        }

        @Override 
        public void close() {
            this.a.e(this);
        }

        public DeviceMetadataStore e() {
            return this.b;
        }

        public  a(c cVar, DeviceMetadataStore i5Var, int i) {
            this(cVar, i5Var);
        }
    }

    /**
     * DeviceMetadataStore 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in DeviceMetadataStore, carrying local implementation detail.
     */
    public static final class c {
        private final LongSupplier a;
        private final Supplier<String> b;
        private DeviceMetadataStore c;
        private int d;
        private long e;

        public c(LongSupplier longSupplier, Supplier<String> supplier) {
            Objects.requireNonNull(longSupplier);
            this.a = longSupplier;
            Objects.requireNonNull(supplier);
            this.b = supplier;
        }

        
        public static  String d() {
            return UUID.randomUUID().toString();
        }

        
        public synchronized void e(a aVar) {
            if (aVar.c) {
                return;
            }
            aVar.c = true;
            int i = this.d - 1;
            this.d = i;
            if (i == 0) {
                this.e = this.a.getAsLong();
            }
        }

        public synchronized a c(String str, b bVar) {
            try {
                long asLong = this.a.getAsLong();
                if (this.c == null) {
                    this.c = new DeviceMetadataStore(this.b.get(), str, bVar);
                } else if (this.d == 0) {
                    long j = this.e;
                    if (asLong >= j && asLong - j >= 60000) {
                        this.c = new DeviceMetadataStore(this.b.get(), str, bVar);
                    }
                }
                int i = this.d;
                if (i == Integer.MAX_VALUE) {
                    throw new IllegalStateException("too many device metadata owners");
                }
                this.d = i + 1;
            } catch (Throwable th) {
                throw th;
            }
            return new a(this, this.c, 0);
        }

        public c(LongSupplier longSupplier) {
            this(longSupplier, new f1(4));
        }
    }
}
