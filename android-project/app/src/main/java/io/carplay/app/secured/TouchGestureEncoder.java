/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 TouchGestureEncoder。触摸手势编码：把 HID 报告转换成 CarLife TOUCH 帧（含坐标缩放）。
 *   This file defines TouchGestureEncoder. Touch gesture encoder: converts HID reports into CarLife TOUCH frames with coordinate scaling.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.Objects;


/**
 * 触摸手势编码：把 HID 报告转换成 CarLife TOUCH 帧（含坐标缩放）。
 *
 * Touch gesture encoder: converts HID reports into CarLife TOUCH frames with coordinate scaling.
 */
public final class TouchGestureEncoder {
    private final int a;
    private final int b;
    private boolean c;

    /**
     * 枚举：本子系统使用的状态/类型取值集合。
     *
     * Enumeration: state/type values used by this subsystem.
     */
    public enum TouchAction {
        DOWN,
        MOVE,
        UP
    }

    /**
     * protobuf 消息：承载一组协议字段并负责序列化。
     *
     * Protobuf message carrying a group of protocol fields and its serialization.
     */
    public static final class b extends ProtoMessageBase {
        private final int a;
        private final byte[] b;

        public b(int i, byte[] bArr) {
            byte[] bArr2 = (byte[]) bArr.clone();
            this.a = i;
            this.b = bArr2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof b)) {
                return false;
            }
            b bVar = (b) obj;
            return this.a == bVar.a && Objects.equals(this.b, bVar.b);
        }

        public int g() {
            return this.a;
        }

        public byte[] h() {
            return (byte[]) this.b.clone();
        }

        public final int hashCode() {
            int i = this.a;
            return Objects.hashCode(this.b) + (i * 31);
        }

        public final String toString() {
            Object[] objArr = {Integer.valueOf(this.a), this.b};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
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

    public TouchGestureEncoder(int i, int i2) {
        if (i < 1 || i2 < 1 || i > 8192 || i2 > 8192) {
            SyntheticRefs.b("touch dimensions");
            throw null;
        }
        this.a = i;
        this.b = i2;
    }

    public synchronized b a(TouchAction aVar, int i, int i2) {
        int i3;
        if (aVar == null || i < 0 || i2 < 0 || i >= this.a || i2 >= this.b) {
            throw new IllegalArgumentException("touch coordinates/action");
        }
        TouchAction aVar2 = TouchAction.DOWN;
        if (aVar == aVar2 && this.c) {
            throw new IllegalStateException("duplicate down");
        }
        if (aVar != aVar2 && !this.c) {
            throw new IllegalStateException("no active gesture");
        }
        int iOrdinal = aVar.ordinal();
        if (iOrdinal == 0) {
            i3 = 425986;
        } else if (iOrdinal == 1) {
            i3 = 425988;
        } else {
            if (iOrdinal != 2) {
                throw new IncompatibleClassChangeError();
            }
            i3 = 425987;
        }
        this.c = aVar != TouchAction.UP;
        return new b(i3, new ProtobufReader.a().c(1, i).c(2, i2).g());
    }

    public synchronized void b() {
        this.c = false;
    }
}
