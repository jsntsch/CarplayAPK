/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 AudioSourceCapability。音频源能力：描述 media / alert 等音频源的格式与优先级。
 *   This file defines AudioSourceCapability. Audio source capability describing formats and priorities of media / alert sources.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;



/**
 * 音频源能力：描述 media / alert 等音频源的格式与优先级。
 *
 * Audio source capability describing formats and priorities of media / alert sources.
 */
public final class AudioSourceCapability {
    public static final int c = 1;
    public static final int d = 3;
    private final HidCapabilities a2;
    private final List<HidCapabilities.c> b;

    /**
     * AudioSourceCapability 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in AudioSourceCapability, carrying local implementation detail.
     */
    public static  class a {
        static final  int[] a;

        static {
            int[] iArr = new int[CarLifeFrame.a.values().length];
            a = iArr;
            try {
                iArr[CarLifeFrame.a.MEDIA.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                a[CarLifeFrame.a.TTS.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                a[CarLifeFrame.a.VR.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class b extends ProtoMessageBase {
        private final int a;
        private final int b;
        private final int c;

        public b(int i, int i2, int i3) {
            this.a = i;
            this.b = i2;
            this.c = i3;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && this.b == bVar.b && this.c == bVar.c;
        }

        public int g() {
            return this.c;
        }

        public int h() {
            return this.b;
        }

        public final int hashCode() {
            int i = this.a;
            return (((i * 31) + this.b) * 31) + this.c;
        }

        public int i() {
            return this.a;
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), Integer.valueOf(this.b), Integer.valueOf(this.c)};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
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
    }

    private AudioSourceCapability(HidCapabilities xVar, List<HidCapabilities.c> list) {
        this.a2 = xVar;
        this.b = List.copyOf(list);
    }

    public static List<AudioCapabilities.d> d(AudioCapabilities sVar, CarLifeFrame.a aVar, int i, int i2, int i3, Set<Integer> set) throws IOException {
        String str;
        int i4 = a.a[aVar.ordinal()];
        if (i4 == 1) {
            str = "media";
        } else if (i4 == 2) {
            str = "alert";
        } else {
            if (i4 != 3) {
                SyntheticRefs.c("AUDIO_SOURCE_CHANNEL_UNSUPPORTED");
                return null;
            }
            str = "speechRecognition";
        }
        ArrayList arrayList = new ArrayList();
        for (AudioCapabilities.d dVar : sVar.x(str, i, i2, i3)) {
            if (!set.contains(Integer.valueOf(dVar.p()))) {
                arrayList.add(dVar);
                if (arrayList.size() == 3) {
                    break;
                }
            }
        }
        return List.copyOf(arrayList);
    }

    public static AudioSourceCapability f(HidCapabilities xVar, z zVar) throws IOException {
        Objects.requireNonNull(xVar);
        Objects.requireNonNull(zVar);
        HidCapabilities.c cVarB = zVar.b(xVar, 1);
        ArrayList arrayList = new ArrayList(xVar.i());
        o1 o1Var = new o1();
        o1Var.a = cVarB;
        VarHandle.storeStoreFence();
        arrayList.sort(Comparator.comparing(o1Var).thenComparing(new r(2)).thenComparing(Comparator.comparingLong(new p(3)).reversed()));
        arrayList.removeIf(new w0(18));
        if (!arrayList.isEmpty()) {
            return new AudioSourceCapability(xVar, arrayList);
        }
        SyntheticRefs.c("CAP_MAIN_DISPLAY_UNAVAILABLE");
        return null;
    }

    
    public static  Boolean h(HidCapabilities.c cVar, HidCapabilities.c cVar2) {
        return Boolean.valueOf(cVar2.p() != cVar.p());
    }

    
    public static  long i(HidCapabilities.c cVar) {
        return ((long) cVar.v()) * ((long) cVar.j());
    }

    
    public static  boolean j(HidCapabilities.c cVar) {
        return (cVar.q() == 110 && cVar.k()) ? false : true;
    }

    public static boolean k(int i) {
        return i == 400 || i == 406 || i == 415;
    }

    public static b l(int i, int i2, int i3) {
        if (i < 8 || i2 < 8) {
            SyntheticRefs.b("receiver dimensions too small");
            return null;
        }
        double dMin = Math.min(1.0d, 1920.0d / ((double) Math.max(i, i2)));
        return new b(Math.max(8, (((int) (((double) i) * dMin)) / 8) * 8), Math.max(8, (((int) (((double) i2) * dMin)) / 8) * 8), Math.max(1, Math.min(60, i3)));
    }

    public AudioCapabilities e() {
        return this.a2.f();
    }

    public List<HidCapabilities.c> g() {
        return this.b;
    }

    public String m() {
        return "CAPABILITY_PLAN policy=1 fingerprint=" + this.a2.o() + " display_candidates=" + this.b.size() + " audio_table_valid=" + this.a2.h() + " max_route_attempts=3 physical_receipt_confirmed=false";
    }
}
