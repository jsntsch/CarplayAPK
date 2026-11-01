package io.carplay.app.secured;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
final class n1 {
    private n1() {
    }

    public static byte[] a(boolean z, long j, long j2, int i, long j3) throws IOException {
        byte[] bArrF;
        if (j < 0 || j2 < 0 || i < 0 || i > 12 || j3 < 1) {
            d0.c("SCREEN_STATS_INVALID");
            return null;
        }
        if (z) {
            double d = j3 / 1.0E9d;
            bArrF = BinaryPlist.f(Map.of("txUsageAvg", Double.valueOf(j2 / d), "encoderCurrentFPS", Long.valueOf(Math.min(240L, Math.round(j / d))), "sentFramesAvg", Long.valueOf(Math.min(4294967295L, j)), "queuedFramesAvg", Integer.valueOf(i), "lossAvg", Double.valueOf(0.0d)));
        } else {
            bArrF = new byte[0];
        }
        byte[] bArr = new byte[Iap2Csm.b];
        ByteBuffer byteBufferOrder = ByteBuffer.wrap(bArr).order(ByteOrder.LITTLE_ENDIAN);
        byteBufferOrder.putInt(bArrF.length);
        bArr[4] = (byte) (z ? 5 : 2);
        if (z) {
            byteBufferOrder.putFloat(h5.v, 0.0f).putFloat(124, bArrF.length);
        }
        return DerCodec.d(bArr, bArrF);
    }
}
