/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 PcmEndpoint。PCM 端点：描述一路 PCM 输出的 stream usage 与生命周期，拒绝过期端点写入。
 *   This file defines PcmEndpoint. PCM endpoint describing one output stream's usage and lifetime, rejecting stale writes.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;



/**
 * PCM 端点：描述一路 PCM 输出的 stream usage 与生命周期，拒绝过期端点写入。
 *
 * PCM endpoint describing one output stream's usage and lifetime, rejecting stale writes.
 */
public final class PcmEndpoint {
    private final d a;
    private final Supplier<io.carplay.app.secured.SessionVerificationTracker.C0003b> b;
    private final Map<StreamIdentity, PcmStream> c = new LinkedHashMap();
    private long d;
    private long e;
    private MediaFormat.g f;

    /**
     * 接口实现：实现 MediaFormat 定义的契约。
     *
     * Implementation of the MediaFormat interface.
     */
    public final class PcmStream implements MediaFormat {
        private final StreamIdentity a;
        private final long b;
        private final MediaSourceTracker c;
        private boolean d = true;

        
        /**
         * 接口实现：实现 MediaFormat 定义的契约。
         *
         * Implementation of the MediaFormat interface.
         */
        public class C0001a implements MediaFormat {
            final  StreamIdentity a;
            final  PcmStream b;

            public C0001a(PcmStream aVar, StreamIdentity cVar) {
                this.a = cVar;
                this.b = aVar;
            }

            @Override 
            public void a(long j, MediaFormat.g gVar, MediaFormat.PcmFormat eVar) {
                PcmEndpoint.this.a.b(this.a, j, gVar, eVar);
            }

            @Override 
            public void b(long j, MediaFormat.g gVar, MediaFormat.VideoMime hVar) {
                throw new IllegalStateException("PCM endpoint");
            }

            @Override 
            public void c(MediaFormat.c cVar) {
                throw new IllegalStateException("PCM endpoint");
            }

            @Override 
            public void e(long j, String str) {
                PcmEndpoint.this.a.c(this.a, j, str);
            }

            @Override 
            public void f(MediaFormat.c cVar) {
                PcmEndpoint.this.a.a(this.a, cVar);
            }
        }

        public PcmStream(StreamIdentity cVar, long j, MediaFormat.g gVar) {
            this.a = cVar;
            this.b = j;
            MediaSourceTracker e4Var = new MediaSourceTracker(new C0001a(this, cVar), PcmEndpoint.this.b);
            this.c = e4Var;
            e4Var.g(j, gVar);
        }

        private void g() {
            if (this.d && PcmEndpoint.this.d == this.b) {
                return;
            }
            SyntheticRefs.d("stale PCM endpoint");
        }

        @Override 
        public void a(long j, MediaFormat.g gVar, MediaFormat.PcmFormat eVar) {
            synchronized (PcmEndpoint.this) {
                try {
                    g();
                    if (eVar == null || eVar.n() != this.a.i()) {
                        throw new IllegalArgumentException("stream usage mismatch");
                    }
                    this.c.a(j, gVar, eVar);
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override 
        public void b(long j, MediaFormat.g gVar, MediaFormat.VideoMime hVar) {
            throw new IllegalStateException("PCM endpoint");
        }

        @Override 
        public void c(MediaFormat.c cVar) {
            throw new IllegalStateException("PCM endpoint");
        }

        @Override 
        public void e(long j, String str) {
            synchronized (PcmEndpoint.this) {
                try {
                    if (j == this.b && this.d) {
                        this.d = false;
                        PcmEndpoint.this.c.remove(this.a, this);
                        this.c.e(j, str);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override 
        public void f(MediaFormat.c cVar) {
            synchronized (PcmEndpoint.this) {
                g();
                this.c.f(cVar);
            }
        }

        public void i(String str) {
            this.d = false;
            this.c.e(this.b, str);
        }
    }

    /**
     * 枚举：本子系统使用的状态/类型取值集合。
     *
     * Enumeration: state/type values used by this subsystem.
     */
    public enum b {
        CARLIFE,
        ICCOA
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class StreamIdentity extends ProtoMessageBase {
        private final b a;
        private final int b;
        private final MediaFormat.a c;

        public StreamIdentity(b bVar, int i, MediaFormat.a aVar) {
            if (bVar == null || aVar == null || i < 0 || i > 65535) {
                SyntheticRefs.b("explicit stream identity required");
                throw null;
            }
            this.a = bVar;
            this.b = i;
            this.c = aVar;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof StreamIdentity)) {
                return false;
            }
            StreamIdentity cVar = (StreamIdentity) obj;
            return this.b == cVar.b && Objects.equals(this.a, cVar.a) && Objects.equals(this.c, cVar.c);
        }

        public int g() {
            return this.b;
        }

        public b h() {
            return this.a;
        }

        public final int hashCode() {
            int i = this.b;
            b bVar = this.a;
            MediaFormat.a aVar = this.c;
            int iHashCode = Objects.hashCode(bVar);
            return Objects.hashCode(aVar) + ((iHashCode + (i * 31)) * 31);
        }

        public MediaFormat.a i() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {this.a, Integer.valueOf(this.b), this.c};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(StreamIdentity.class, sb, "[");
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
     * 回调接口：定义单一职责的调用契约，由调用方实现。
     *
     * Callback interface defining a single-responsibility contract implemented by callers.
     */
    public interface d {
        void a(StreamIdentity cVar, MediaFormat.c cVar2);

        void b(StreamIdentity cVar, long j, MediaFormat.g gVar, MediaFormat.PcmFormat eVar);

        void c(StreamIdentity cVar, long j, String str);
    }

    public PcmEndpoint(d dVar, Supplier<io.carplay.app.secured.SessionVerificationTracker.C0003b> supplier) {
        if (dVar == null || supplier == null) {
            SyntheticRefs.b("target/evidence");
            throw null;
        }
        this.a = dVar;
        this.b = supplier;
    }

    
    public static  PcmStream[] h(int i) {
        return new PcmStream[i];
    }

    public synchronized void f(long j, MediaFormat.g gVar) {
        if (this.d != 0 || j <= this.e || gVar == null) {
            throw new IllegalStateException("fresh inactive session required");
        }
        this.e = j;
        this.d = j;
        this.f = gVar;
    }

    public synchronized void g(long j, String str) {
        long j2 = this.d;
        if (j2 != 0 && j2 == j) {
            this.d = 0L;
            RuntimeException e = null;
            this.f = null;
            PcmStream[] aVarArr = (PcmStream[]) this.c.values().toArray(new h4());
            this.c.clear();
            for (PcmStream aVar : aVarArr) {
                try {
                    aVar.i(str);
                } catch (RuntimeException e2) {
                    e = e2;
                }
            }
            if (e != null) {
                throw e;
            }
        }
    }

    public synchronized MediaFormat i(StreamIdentity cVar) throws Throwable {
        Throwable th = null;
        try {
            try {
                if (this.d == 0 || cVar == null) {
                    throw new IllegalStateException("no active stream session");
                }
                PcmStream aVar = this.c.get(cVar);
                if (aVar != null) {
                    return aVar;
                }
                if (this.c.size() >= 16) {
                    throw new IllegalStateException("too many active PCM streams");
                }
                PcmStream aVar2 = new PcmStream(cVar, this.d, this.f);
                this.c.put(cVar, aVar2);
                return aVar2;
            } catch (Throwable thCaught) {
                throw thCaught;
            }
        } catch (Throwable th2) {
            th = th2;
            throw th;
        }
    }
}
