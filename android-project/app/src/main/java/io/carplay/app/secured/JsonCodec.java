/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 JsonCodec。最小 JSON 序列化器，仅用于 /command 请求体的备用编码。
 *   This file defines JsonCodec, a minimal JSON serializer used as an alternate encoding for /command bodies.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.nio.charset.StandardCharsets;
import java.util.Map;



/**
 * 最小 JSON 序列化器。
 *
 * 只覆盖 AirPlay `/command` 请求体需要的类型：Map / Iterable / String / Number / Boolean / null。
 * 不做语法校验，也不处理循环引用 —— 调用方传入的都是即时构造的小对象。
 *
 * Minimal JSON serializer covering only the types an AirPlay `/command` body needs.
 */
public final class JsonCodec {

    private JsonCodec() {
    }

    /**
     * 把对象序列化为 UTF-8 JSON 字节。
     *
     * Serialize the given value into UTF-8 JSON bytes.
     */
    public static byte[] f(Object obj) {
        StringBuilder sb = new StringBuilder(256);
        write(sb, obj);
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void write(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
            return;
        }
        if (value instanceof String) {
            quote(sb, (String) value);
            return;
        }
        if (value instanceof Boolean) {
            sb.append(((Boolean) value).booleanValue() ? "true" : "false");
            return;
        }
        if (value instanceof Number) {
            sb.append(value.toString());
            return;
        }
        if (value instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                quote(sb, String.valueOf(entry.getKey()));
                sb.append(':');
                write(sb, entry.getValue());
            }
            sb.append('}');
            return;
        }
        if (value instanceof Iterable) {
            sb.append('[');
            boolean first = true;
            for (Object item : (Iterable<?>) value) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                write(sb, item);
            }
            sb.append(']');
            return;
        }
        quote(sb, String.valueOf(value));
    }

    private static void quote(StringBuilder sb, String text) {
        sb.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", Integer.valueOf(c)));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        sb.append('"');
    }
}
