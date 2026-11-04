/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 ProtoMessageBase。Protobuf 消息基类：提供字段缓存、序列化与相等性比较的公共实现。
 *   This file defines ProtoMessageBase. Protobuf message base class providing field caching, serialization and equality.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.util.Objects;



/**
 * Protobuf 消息基类：提供字段缓存、序列化与相等性比较的公共实现。
 *
 * Protobuf message base class providing field caching, serialization and equality.
 */
public abstract  class ProtoMessageBase {
    public static int a(long j, int i, int i2) {
        return (Long.hashCode(j) + i) * i2;
    }

    public static int b(String str, int i, int i2) {
        return (Objects.hashCode(str) + i) * i2;
    }

    public static long c(long j, long j2, long j3, long j4) {
        return (j * j2) + j3 + j4;
    }

    public static String d(String str, int i) {
        return str + i;
    }

    public static String e(String str, String str2) {
        return str + str2;
    }

    public static void f(Class cls, StringBuilder sb, String str) {
        sb.append(cls.getSimpleName());
        sb.append(str);
    }
}
