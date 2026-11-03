package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public abstract class w2 {
    private static final HexCodec a = new HexCodec();

    public static byte[] a(String str) {
        try {
            return a.a(str, 0, str.length());
        } catch (Exception e) {
            throw new DecoderException("exception decoding Hex string: " + e.getMessage(), e);
        }
    }
}
