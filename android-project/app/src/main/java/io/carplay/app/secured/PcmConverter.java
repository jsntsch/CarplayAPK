/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 PcmConverter。PCM 格式转换：重采样到 48 kHz、位深/声道转换并校验输出上限。
 *   This file defines PcmConverter. PCM format conversion: resampling to 48 kHz, bit-depth/channel conversion and output limit checks.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;



/**
 * PCM 格式转换：重采样到 48 kHz、位深/声道转换并校验输出上限。
 *
 * PCM format conversion: resampling to 48 kHz, bit-depth/channel conversion and output limit checks.
 */
public final class PcmConverter implements AutoCloseable {
    public static final int t = 65536;
    private static final int u = 1048576;
    private static final int v = 256;
    private final int a;
    private final int b;
    private final int c;
    private final int d;
    private final int e;
    private final int f;
    private final int g;
    private final int h;
    private final short[][] i;
    private final short[] j;
    private final short[] k;
    private final byte[] l = new byte[4];
    private final double[][] m;
    private long n;
    private long o;
    private long p;
    private int q;
    private boolean r;
    private boolean s;

    public PcmConverter(int i, int i2, int i3, int i4) throws IOException {
        double dSin;
        if (io.carplay.app.secured.AudioCapabilities.v(i, i2, 16) == 0 || io.carplay.app.secured.AudioCapabilities.v(i3, i4, 16) == 0) {
            throw i("PCM_CONVERTER_FORMAT_UNSUPPORTED");
        }
        this.a = i;
        this.b = i2;
        this.c = i3;
        this.d = i4;
        char c = 2;
        this.e = i2 * 2;
        int iL = l(i, i3);
        this.f = i / iL;
        int i5 = i3 / iL;
        this.g = i5;
        int i6 = 0;
        int iCeil = i == i3 ? 0 : (int) Math.ceil(Math.max(1.0d, ((double) i) / ((double) i3)) * 16.0d);
        this.h = iCeil;
        this.i = (short[][]) Array.newInstance((Class<?>) Short.TYPE, v, i4);
        this.j = new short[i4];
        this.k = new short[i4];
        this.m = iCeil == 0 ? new double[0][] : (double[][]) Array.newInstance((Class<?>) Double.TYPE, i5, iCeil * 2);
        double dMin = Math.min(1.0d, ((double) i3) / ((double) i)) * 0.45d;
        int i7 = 0;
        while (i7 < this.m.length) {
            double d = ((double) i7) / ((double) this.g);
            char c2 = c;
            int i8 = i6;
            double d2 = 0.0d;
            while (true) {
                int i9 = this.h;
                if (i8 >= i9 * 2) {
                    break;
                }
                double d3 = ((double) ((i8 - i9) + 1)) - d;
                double d4 = 2.0d * dMin;
                double d5 = d4 * d3;
                if (Math.abs(d5) < 1.0E-12d) {
                    dSin = 1.0d;
                } else {
                    double d6 = d5 * 3.141592653589793d;
                    dSin = Math.sin(d6) / d6;
                }
                double dAbs = Math.abs(d3);
                int i10 = this.h;
                double d7 = d;
                double dCos = d4 * dSin * (dAbs >= ((double) i10) ? 0.0d : (Math.cos((d3 * 3.141592653589793d) / ((double) i10)) * 0.5d) + 0.5d);
                this.m[i7][i8] = dCos;
                d2 += dCos;
                i8++;
                d = d7;
            }
            for (int i11 = 0; i11 < this.h * 2; i11++) {
                double[] dArr = this.m[i7];
                dArr[i11] = dArr[i11] / d2;
            }
            i7++;
            c = c2;
            i6 = 0;
        }
    }

    private void b(ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        short s;
        byte[] bArr = this.l;
        short s2 = (short) ((bArr[0] & 255) | (bArr[1] << 8));
        if (this.b == 2) {
            s = (short) ((bArr[3] << 8) | (bArr[2] & 255));
        } else {
            s = s2;
        }
        short[][] sArr = this.i;
        long j = this.n;
        short[] sArr2 = sArr[(int) (j % 256)];
        int i = this.d;
        if (i == 1) {
            sArr2[0] = (short) ((s2 + s) / 2);
        } else {
            sArr2[0] = s2;
            sArr2[1] = s;
        }
        if (j == 0) {
            System.arraycopy(sArr2, 0, this.j, 0, i);
        }
        System.arraycopy(sArr2, 0, this.k, 0, this.d);
        this.n++;
        if (this.h == 0) {
            u(byteArrayOutputStream, sArr2);
            this.p += (long) this.f;
        } else {
            while ((this.p / ((long) this.g)) + ((long) this.h) < this.n) {
                f(byteArrayOutputStream);
            }
        }
    }

    private void e() {
        this.q = 0;
        Arrays.fill(this.l, (byte) 0);
        Arrays.fill(this.j, (short) 0);
        Arrays.fill(this.k, (short) 0);
        for (short[] sArr : this.i) {
            Arrays.fill(sArr, (short) 0);
        }
    }

    private void f(ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        if (byteArrayOutputStream.size() > 1048576 - (this.d * 2)) {
            throw i("PCM_CONVERTER_OUTPUT_LIMIT");
        }
        long j = this.p;
        int i = this.g;
        long j2 = j / ((long) i);
        int i2 = (int) (j % ((long) i));
        for (int i3 = 0; i3 < this.d; i3++) {
            double dT = 0.0d;
            int i4 = 0;
            int i5 = this.h;
            while (i4 < i5 * 2) {
                dT += ((double) t(((((long) i4) + j2) - ((long) i5)) + 1, i3)) * this.m[i2][i4];
                i4++;
            }
            int iMax = (int) Math.max(-32768L, Math.min(32767L, Math.round(dT)));
            byteArrayOutputStream.write(iMax);
            byteArrayOutputStream.write(iMax >>> 8);
        }
        this.o++;
        this.p += (long) this.f;
    }

    private void g() throws IOException {
        if (this.s || this.r) {
            throw i("PCM_CONVERTER_CLOSED");
        }
    }

    private static IOException i(String str) {
        return new IOException(str);
    }

    private static int l(int i, int i2) {
        while (true) {
            int i3 = i2;
            int i4 = i;
            i = i3;
            if (i == 0) {
                return i4;
            }
            i2 = i4 % i;
        }
    }

    private short t(long j, int i) throws IOException {
        if (j < 0) {
            return this.j[i];
        }
        long j2 = this.n;
        if (j >= j2) {
            return this.k[i];
        }
        if (j >= j2 - 256) {
            return this.i[(int) (j % 256)][i];
        }
        throw i("PCM_CONVERTER_HISTORY_LIMIT");
    }

    private void u(ByteArrayOutputStream byteArrayOutputStream, short[] sArr) throws IOException {
        if (byteArrayOutputStream.size() > 1048576 - (this.d * 2)) {
            throw i("PCM_CONVERTER_OUTPUT_LIMIT");
        }
        for (short s : sArr) {
            byteArrayOutputStream.write(s);
            byteArrayOutputStream.write(s >>> 8);
        }
        this.o++;
    }

    public synchronized byte[] c(byte[] bArr) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream;
        {
            g();
            if (bArr == null || bArr.length > 65536) {
                throw i("PCM_CONVERTER_INPUT_LIMIT");
            }
            if (this.n > ((((Long.MAX_VALUE - ((long) this.f)) / ((long) this.g)) - (((long) (bArr.length + this.q)) / ((long) this.e))) - ((long) this.h)) - 1) {
                throw i("PCM_CONVERTER_LENGTH_LIMIT");
            }
            byteArrayOutputStream = new ByteArrayOutputStream();
            for (byte b : bArr) {
                byte[] bArr2 = this.l;
                int i = this.q;
                int i2 = i + 1;
                this.q = i2;
                bArr2[i] = b;
                if (i2 == this.e) {
                    b(byteArrayOutputStream);
                    this.q = 0;
                }
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override 
    public synchronized void close() {
        if (this.s) {
            return;
        }
        this.s = true;
        e();
    }

    public synchronized byte[] k() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream;
        {
            g();
            if (this.q != 0) {
                throw i("PCM_CONVERTER_TRUNCATED_FRAME");
            }
            byteArrayOutputStream = new ByteArrayOutputStream();
            while (this.p < this.n * ((long) this.g)) {
                f(byteArrayOutputStream);
            }
            this.r = true;
        }
        return byteArrayOutputStream.toByteArray();
    }

    public int m() {
        return this.b;
    }

    public synchronized long n() {
        return this.n;
    }

    public int o() {
        return this.a;
    }

    public int p() {
        return this.h;
    }

    public int q() {
        return this.d;
    }

    public synchronized long r() {
        return this.o;
    }

    public synchronized void reset() throws IOException {
        g();
        this.n = 0L;
        this.o = 0L;
        this.p = 0L;
        e();
    }

    public int s() {
        return this.c;
    }
}
