/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 DataLengthException。数据长度异常：输入/输出缓冲区长度不满足算法要求。
 *   This file defines DataLengthException. Data-length exception raised when buffers do not satisfy the algorithm requirements.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * 数据长度异常：输入/输出缓冲区长度不满足算法要求。
 *
 * Data-length exception raised when buffers do not satisfy the algorithm requirements.
 */
public class DataLengthException extends RuntimeCryptoException {
    public DataLengthException(String str) {
        super(str);
    }
}
