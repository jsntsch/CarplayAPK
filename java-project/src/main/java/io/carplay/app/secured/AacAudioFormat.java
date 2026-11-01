package io.carplay.app.secured;

import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.view.Surface;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
/**
 * AacAudioFormat
 *
 * <p>原混淆名：{@code io.carplay.app.secured.d}（jadx 产物 d.java）
 *
 * <p>命名依据：audio/mp4a-latm / AUDIO_AAC_LOCAL_FORMAT
 */
public final class AacAudioFormat implements t {
    private static final String g = "audio/mp4a-latm";
    private final int a;
    private final int b;
    private MediaCodec c;
    private long d;
    private boolean e;
    private final MediaCodec.BufferInfo f = new MediaCodec.BufferInfo();

    public AacAudioFormat(int i, int i2) throws IOException {
        if ((i != 44100 && i != 48000) || i2 != 2) {
            d0.c("AUDIO_AAC_LOCAL_FORMAT");
            throw null;
        }
        this.a = i;
        this.b = i2;
        e();
    }

    public static byte[] b(byte[] bArr, int i, int i2) throws IOException {
        if (bArr.length < 7 || (bArr[0] & 255) != 255) {
            return bArr;
        }
        byte b = bArr[1];
        if ((b & 246) != 240) {
            return bArr;
        }
        int i3 = (b & 1) == 0 ? 9 : 7;
        int i4 = bArr[3];
        int i5 = ((255 & bArr[4]) << 3) | ((i4 & 3) << 11) | ((bArr[5] & 224) >>> 5);
        int i6 = bArr[2];
        int i7 = (i6 >>> 2) & 15;
        int i8 = ((i4 >>> 6) & 3) | ((i6 & 1) << 2);
        int i9 = (i6 >>> 6) & 3;
        if (bArr.length > i3 && i5 == bArr.length && i9 == 1) {
            if (i7 == (i != 44100 ? 3 : 4) && i8 == i2 && (bArr[6] & 3) == 0) {
                return Arrays.copyOfRange(bArr, i3, bArr.length);
            }
        }
        d0.c("AUDIO_AAC_LOCAL_ADTS_MISMATCH");
        return null;
    }

    private static void c(MediaCodec mediaCodec) {
        if (mediaCodec != null) {
            try {
                mediaCodec.stop();
            } catch (RuntimeException unused) {
            }
            try {
                mediaCodec.release();
            } catch (RuntimeException unused2) {
            }
        }
    }

    private void e() throws IOException {
        MediaCodec mediaCodec = null;
        try {
            MediaFormat mediaFormatCreateAudioFormat = MediaFormat.createAudioFormat(g, this.a, this.b);
            mediaFormatCreateAudioFormat.setInteger("aac-profile", 2);
            mediaFormatCreateAudioFormat.setInteger("bitrate", 192000);
            mediaFormatCreateAudioFormat.setInteger("max-input-size", this.b * 2048);
            MediaCodec mediaCodecCreateEncoderByType = MediaCodec.createEncoderByType(g);
            try {
                mediaCodecCreateEncoderByType.configure(mediaFormatCreateAudioFormat, (Surface) null, (MediaCrypto) null, 1);
                mediaCodecCreateEncoderByType.start();
                this.c = mediaCodecCreateEncoderByType;
                this.d = 0L;
            } catch (IOException | RuntimeException e) {
                e = e;
                mediaCodec = mediaCodecCreateEncoderByType;
                c(mediaCodec);
                throw new IOException("AUDIO_AAC_LOCAL_ENCODER_UNAVAILABLE", e);
            }
        } catch (IOException | RuntimeException e2) {
            e = e2;
        }
    }

    @Override // io.carplay.app.secured.t, java.lang.AutoCloseable
    public synchronized void close() {
        if (this.e) {
            return;
        }
        this.e = true;
        MediaCodec mediaCodec = this.c;
        this.c = null;
        c(mediaCodec);
    }

    @Override // io.carplay.app.secured.t
    public synchronized List<byte[]> j(byte[] bArr) {
        MediaCodec mediaCodec;
        ArrayList arrayList;
        int i;
        try {
            if (this.e || (mediaCodec = this.c) == null || bArr == null || bArr.length != this.b * 2048) {
                throw new IOException("AUDIO_AAC_LOCAL_INPUT");
            }
            try {
                int iDequeueInputBuffer = mediaCodec.dequeueInputBuffer(10000L);
                if (iDequeueInputBuffer < 0) {
                    throw new IOException("AUDIO_AAC_LOCAL_INPUT_BACKPRESSURE");
                }
                ByteBuffer inputBuffer = this.c.getInputBuffer(iDequeueInputBuffer);
                if (inputBuffer == null || inputBuffer.capacity() < bArr.length) {
                    throw new IOException("AUDIO_AAC_LOCAL_INPUT_BUFFER");
                }
                inputBuffer.clear();
                inputBuffer.put(bArr);
                this.c.queueInputBuffer(iDequeueInputBuffer, 0, bArr.length, (this.d * 1000000) / ((long) this.a), 0);
                this.d += 1024;
                arrayList = new ArrayList();
                long jNanoTime = System.nanoTime() + 20000000;
                for (int i2 = 0; i2 < 16; i2++) {
                    int iDequeueOutputBuffer = this.c.dequeueOutputBuffer(this.f, arrayList.isEmpty() ? Math.max(0L, Math.min(10000L, (jNanoTime - System.nanoTime()) / 1000)) : 0L);
                    if (iDequeueOutputBuffer == -1) {
                        break;
                    }
                    if (iDequeueOutputBuffer != -2 && iDequeueOutputBuffer != -3) {
                        if (iDequeueOutputBuffer < 0) {
                            throw new IOException("AUDIO_AAC_LOCAL_OUTPUT_STATUS");
                        }
                        try {
                            MediaCodec.BufferInfo bufferInfo = this.f;
                            int i3 = bufferInfo.flags;
                            if ((i3 & 2) == 0) {
                                if ((i3 & 12) != 0) {
                                    throw new IOException("AUDIO_AAC_LOCAL_INCOMPLETE_ACCESS_UNIT");
                                }
                                int i4 = bufferInfo.size;
                                if (i4 != 0) {
                                    if (i4 < 0 || i4 > 16384 || arrayList.size() >= 8) {
                                        throw new IOException("AUDIO_AAC_LOCAL_OUTPUT_LIMIT");
                                    }
                                    ByteBuffer outputBuffer = this.c.getOutputBuffer(iDequeueOutputBuffer);
                                    if (outputBuffer != null && (i = this.f.offset) >= 0) {
                                        int iCapacity = outputBuffer.capacity();
                                        MediaCodec.BufferInfo bufferInfo2 = this.f;
                                        if (i <= iCapacity - bufferInfo2.size) {
                                            outputBuffer.position(bufferInfo2.offset);
                                            MediaCodec.BufferInfo bufferInfo3 = this.f;
                                            outputBuffer.limit(bufferInfo3.offset + bufferInfo3.size);
                                            byte[] bArr2 = new byte[this.f.size];
                                            outputBuffer.get(bArr2);
                                            arrayList.add(b(bArr2, this.a, this.b));
                                        }
                                    }
                                    throw new IOException("AUDIO_AAC_LOCAL_OUTPUT_BUFFER");
                                }
                            }
                            this.c.releaseOutputBuffer(iDequeueOutputBuffer, false);
                        } catch (Throwable th) {
                            this.c.releaseOutputBuffer(iDequeueOutputBuffer, false);
                            throw th;
                        }
                    }
                }
            } catch (RuntimeException e) {
                throw new IOException("AUDIO_AAC_LOCAL_ENCODE_FAILED", e);
            }
        } catch (Throwable th2) {
            throw th2;
        }
        return List.copyOf(arrayList);
    }

    @Override // io.carplay.app.secured.t
    public synchronized void reset() {
        if (this.e) {
            throw new IOException("AUDIO_AAC_LOCAL_CLOSED");
        }
        MediaCodec mediaCodec = this.c;
        this.c = null;
        c(mediaCodec);
        e();
    }
}
