/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 Iap2Ncm。iAP2 NCM 接口：定义通知/控制消息的媒体格式契约。
 *   This file defines Iap2Ncm. iAP2 NCM interface: media-format contract for notification and control messages.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.List;
import java.util.Objects;



/**
 * iAP2 NCM 接口：定义通知/控制消息的媒体格式契约。
 *
 * iAP2 NCM interface: media-format contract for notification and control messages.
 */
public interface Iap2Ncm extends MediaFormat, AutoCloseable {

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class a extends ProtoMessageBase {
        private final b a;
        private final boolean b;
        private final List<String> c;

        public a(b bVar, boolean z, List<String> list) {
            List<String> listCopyOf = List.copyOf(list);
            this.a = bVar;
            this.b = z;
            this.c = listCopyOf;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.b == aVar.b && Objects.equals(this.a, aVar.a) && Objects.equals(this.c, aVar.c);
        }

        public boolean g() {
            return this.b;
        }

        public b h() {
            return this.a;
        }

        public final int hashCode() {
            boolean z = this.b;
            b bVar = this.a;
            List<String> list = this.c;
            return Objects.hashCode(list) + ((Objects.hashCode(bVar) + (Boolean.hashCode(z) * 31)) * 31);
        }

        public List<String> i() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {this.a, Boolean.valueOf(this.b), this.c};
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
    }

    /**
     * 枚举：本子系统使用的状态/类型取值集合。
     *
     * Enumeration: state/type values used by this subsystem.
     */
    public enum b {
        WIRELESS,
        PHONE_USB_DIRECT,
        EXTERNAL_USB_BRIDGE
    }

    static List<a> d() {
        return List.of(new a(b.WIRELESS, false, List.of("Bluetooth/iAP2 bootstrap", "normal verified pairing/authentication", "vehicle Wi-Fi/RTSP/media/input")), new a(b.PHONE_USB_DIRECT, false, List.of("initial DEVICE descriptor/EP0", "0x51 response and role swap", "HOST iAP2/NCM", "normal authentication/media")), new a(b.EXTERNAL_USB_BRIDGE, false, List.of("optional added hardware", "UDC/role/power", "bridge-side normal CarPlay", "phone media/control link")));
    }

    @Override 
    void close();

    a h();
}
