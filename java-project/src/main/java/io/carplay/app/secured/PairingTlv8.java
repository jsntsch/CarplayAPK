package io.carplay.app.secured;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
/**
 * PairingTlv8
 *
 * <p>原混淆名：{@code io.carplay.app.secured.l1}（jadx 产物 l1.java）
 *
 * <p>命名依据：application/pairing+tlv8；对应 hypercar pairing.rs / tlv8.rs
 */
public final class PairingTlv8 {
    private static final String c = "application/pairing+tlv8";
    private static final byte[] d = new byte[0];
    private final a a;
    private final c b;

    public static final class a {
        private final String a;
        private final byte[] b;
        private final byte[] c;

        public a(String str, byte[] bArr, byte[] bArr2) {
            if (str == null || str.isEmpty() || str.length() > 512 || bArr.length != 32 || bArr2.length != 32) {
                SyntheticRefs.b("Identity");
                throw null;
            }
            this.a = str;
            this.b = (byte[]) bArr.clone();
            this.c = (byte[]) bArr2.clone();
        }

        public static a d(String str) {
            DerCodec.b bVarF = DerCodec.f();
            return new a(str, bVarF.a(), bVarF.b());
        }

        public String e() {
            return this.a;
        }

        public byte[] f() {
            return (byte[]) this.b.clone();
        }

        public byte[] g() {
            return (byte[]) this.c.clone();
        }
    }

    public static final class b {
        private final String a;
        private final byte[] b;

        public b(String str, byte[] bArr) {
            if (str == null || str.isEmpty() || str.length() > 512 || bArr.length != 32) {
                SyntheticRefs.b("Peer identity");
                throw null;
            }
            this.a = str;
            this.b = (byte[]) bArr.clone();
        }

        public String c() {
            return this.a;
        }

        public boolean d() {
            return false;
        }

        public byte[] e() {
            return (byte[]) this.b.clone();
        }
    }

    public interface c {
        b b(String str);

        void c(b bVar);
    }

    public static final class d {
        static final BigInteger e = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF", 16);
        static final BigInteger f = BigInteger.valueOf(5);
        final byte[] a;
        final byte[] b;
        final byte[] c;
        final byte[] d;

        public d(byte[] bArr, byte[] bArr2, String str) throws GeneralSecurityException {
            BigInteger bigIntegerA = a(bArr2);
            if (bigIntegerA.signum() != 0) {
                BigInteger bigInteger = e;
                if (bigIntegerA.compareTo(bigInteger) < 0) {
                    BigInteger bigIntegerA2 = a(DerCodec.q(32));
                    BigInteger bigInteger2 = f;
                    byte[] bArrB = b(bigInteger2.modPow(bigIntegerA2, bigInteger));
                    this.d = bArrB;
                    BigInteger bigIntegerA3 = a(DerCodec.t(bArrB, b(bigIntegerA)));
                    if (bigIntegerA3.signum() == 0) {
                        throw new GeneralSecurityException("Invalid SRP scrambling parameter");
                    }
                    BigInteger bigIntegerA4 = a(DerCodec.t(bArr, DerCodec.t(DerCodec.a("Pair-Setup:"), str.getBytes(StandardCharsets.UTF_8))));
                    BigInteger bigIntegerModPow = bigIntegerA.subtract(a(DerCodec.t(c(bigInteger), b(bigInteger2))).multiply(bigInteger2.modPow(bigIntegerA4, bigInteger))).mod(bigInteger).modPow(bigIntegerA2.add(bigIntegerA3.multiply(bigIntegerA4)), bigInteger);
                    if (bigIntegerModPow.signum() == 0) {
                        throw new GeneralSecurityException("Invalid SRP shared secret");
                    }
                    this.a = DerCodec.t(c(bigIntegerModPow));
                    byte[] bArrT = DerCodec.t(c(bigInteger));
                    byte[] bArrT2 = DerCodec.t(c(bigInteger2));
                    for (int i = 0; i < bArrT.length; i++) {
                        bArrT[i] = (byte) (bArrT[i] ^ bArrT2[i]);
                    }
                    byte[] bArrT3 = DerCodec.t(bArrT, DerCodec.t(DerCodec.a("Pair-Setup")), bArr, this.d, b(bigIntegerA), this.a);
                    this.b = bArrT3;
                    this.c = DerCodec.t(this.d, bArrT3, this.a);
                    return;
                }
            }
            throw new GeneralSecurityException("Invalid SRP public key B");
        }

        public static BigInteger a(byte[] bArr) {
            return new BigInteger(1, bArr);
        }

        public static byte[] b(BigInteger bigInteger) {
            byte[] bArrC = c(bigInteger);
            byte[] bArr = new byte[384];
            System.arraycopy(bArrC, 0, bArr, 384 - bArrC.length, bArrC.length);
            return bArr;
        }

        public static byte[] c(BigInteger bigInteger) {
            byte[] byteArray = bigInteger.toByteArray();
            return (byteArray.length <= 1 || byteArray[0] != 0) ? byteArray : Arrays.copyOfRange(byteArray, 1, byteArray.length);
        }
    }

    public PairingTlv8(a aVar, c cVar) {
        if (aVar == null) {
            throw new NullPointerException("identity");
        }
        this.a = aVar;
        this.b = cVar;
    }

    private b c(EventChannel eventChannel, String str, Map<Integer, byte[]> map) throws GeneralSecurityException, IOException {
        d dVar = new d(k(map, 2, 16, 64), k(map, 3, 384, 384), str);
        if (!MessageDigest.isEqual(k(e(eventChannel, "/pair-setup", m(6, new byte[]{3}, 3, dVar.d, 4, dVar.b), 4), 4, 64, 64), dVar.c)) {
            throw new GeneralSecurityException("Accessory SRP proof does not match");
        }
        byte[] bArrK = DerCodec.k(dVar.a, "Pair-Setup-Encrypt-Salt", "Pair-Setup-Encrypt-Info");
        byte[] bytes = this.a.a.getBytes(StandardCharsets.UTF_8);
        byte[] bArrM = m(1, bytes, 3, this.a.c, 10, DerCodec.g(this.a.b, DerCodec.d(DerCodec.k(dVar.a, "Pair-Setup-Controller-Sign-Salt", "Pair-Setup-Controller-Sign-Info"), bytes, this.a.c)));
        byte[] bArrN = DerCodec.n("PS-Msg05");
        byte[] bArr = d;
        Map<Integer, byte[]> mapD = d(DerCodec.o(bArrK, DerCodec.n("PS-Msg06"), k(e(eventChannel, "/pair-setup", m(6, new byte[]{5}, 5, DerCodec.s(bArrK, bArrN, bArrM, bArr)), 6), 5, 16, ControlRequestParser.a), bArr));
        byte[] bArrK2 = k(mapD, 1, 1, 512);
        byte[] bArrK3 = k(mapD, 3, 32, 32);
        if (!DerCodec.h(bArrK3, DerCodec.d(DerCodec.k(dVar.a, "Pair-Setup-Accessory-Sign-Salt", "Pair-Setup-Accessory-Sign-Info"), bArrK2, bArrK3), k(mapD, 10, 64, 64))) {
            throw new GeneralSecurityException("Accessory pair-setup signature invalid");
        }
        b bVar = new b(l(bArrK2), bArrK3);
        c cVar = this.b;
        if (cVar != null) {
            b bVarB = cVar.b(bVar.a);
            if (bVarB != null && !MessageDigest.isEqual(bVarB.b, bArrK3)) {
                throw new GeneralSecurityException("Stored accessory identity changed; explicit unpair required");
            }
            this.b.c(bVar);
        }
        Arrays.fill(dVar.a, (byte) 0);
        Arrays.fill(bArrK, (byte) 0);
        return bVar;
    }

    public static Map<Integer, byte[]> d(byte[] bArr) throws IOException {
        if (bArr.length > 65535) {
            SyntheticRefs.c("Pairing TLV exceeds bound");
            return null;
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        int i = -1;
        int i2 = 0;
        int i3 = -1;
        while (i2 < bArr.length) {
            if (bArr.length - i2 < 2) {
                SyntheticRefs.c("Truncated TLV header");
                return null;
            }
            int i4 = i2 + 1;
            int i5 = bArr[i2] & 255;
            int i6 = i2 + 2;
            int i7 = bArr[i4] & 255;
            if (i7 > bArr.length - i6) {
                SyntheticRefs.c("Truncated TLV value");
                return null;
            }
            if (linkedHashMap.containsKey(Integer.valueOf(i5)) && (i != i5 || i3 != 255)) {
                SyntheticRefs.c(w1.d("Duplicate nonfragment TLV field ", i5));
                return null;
            }
            ((ByteArrayOutputStream) linkedHashMap.computeIfAbsent(Integer.valueOf(i5), new r(3))).write(bArr, i6, i7);
            i2 = i6 + i7;
            i3 = i7;
            i = i5;
        }
        LinkedHashMap linkedHashMap2 = new LinkedHashMap();
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            linkedHashMap2.put((Integer) entry.getKey(), ((ByteArrayOutputStream) entry.getValue()).toByteArray());
        }
        return linkedHashMap2;
    }

    private static Map<Integer, byte[]> e(EventChannel eventChannel, String str, byte[] bArr, int i) throws IOException {
        EventChannel.b bVarW = eventChannel.w("POST", str, c, bArr, str.equals("/pair-verify") ? Map.of("X-Apple-HKP", "2", "X-Apple-PD", "1") : Map.of("X-Apple-HKP", "2"));
        bVarW.g();
        Map<Integer, byte[]> mapD = d(bVarW.c());
        if (mapD.containsKey(7)) {
            throw new IOException("Pairing rejected at state " + i + ", peer error " + Arrays.toString(mapD.get(7)));
        }
        byte[] bArr2 = mapD.get(6);
        if (bArr2 != null && bArr2.length == 1 && (bArr2[0] & 255) == i) {
            return mapD;
        }
        SyntheticRefs.c(w1.d("Unexpected pairing state; expected ", i));
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ ByteArrayOutputStream f(Integer num) {
        return new ByteArrayOutputStream();
    }

    private e j(EventChannel eventChannel, b bVar, boolean z) throws GeneralSecurityException, IOException {
        b bVarB;
        DerCodec.b bVarW = DerCodec.w();
        byte[] bArrB = bVarW.b();
        Map<Integer, byte[]> mapE = e(eventChannel, "/pair-verify", m(6, new byte[]{1}, 3, bArrB), 2);
        byte[] bArrK = k(mapE, 3, 32, 32);
        byte[] bArrX = DerCodec.x(bVarW.a(), bArrK);
        byte[] bArrK2 = DerCodec.k(bArrX, "Pair-Verify-Encrypt-Salt", "Pair-Verify-Encrypt-Info");
        byte[] bArrN = DerCodec.n("PV-Msg02");
        byte[] bArrK3 = k(mapE, 5, 16, ControlRequestParser.a);
        byte[] bArr = d;
        Map<Integer, byte[]> mapD = d(DerCodec.o(bArrK2, bArrN, bArrK3, bArr));
        byte[] bArrK4 = k(mapD, 1, 1, 512);
        byte[] bArrK5 = k(mapD, 10, 64, 64);
        String strL = l(bArrK4);
        if (z) {
            bVarB = this.b.b(strL);
            if (bVarB == null) {
                throw new GeneralSecurityException("PAIR_RESUME_STORED_PEER_REQUIRED");
            }
        } else {
            bVarB = bVar;
        }
        if (!bVarB.a.equals(strL) || !DerCodec.h(bVarB.b, DerCodec.d(bArrK, bArrK4, bArrB), bArrK5)) {
            throw new GeneralSecurityException("Accessory pair-verify identity or signature invalid");
        }
        byte[] bytes = this.a.a.getBytes(StandardCharsets.UTF_8);
        e(eventChannel, "/pair-verify", m(6, new byte[]{3}, 5, DerCodec.s(bArrK2, DerCodec.n("PV-Msg03"), m(1, bytes, 10, DerCodec.g(this.a.b, DerCodec.d(bArrB, bytes, bArrK))), bArr)), 4);
        byte[] bArrK6 = DerCodec.k(bArrX, "Control-Salt", "Control-Read-Encryption-Key");
        byte[] bArrK7 = DerCodec.k(bArrX, "Control-Salt", "Control-Write-Encryption-Key");
        eventChannel.i(bArrK6, bArrK7);
        Arrays.fill(bArrK2, (byte) 0);
        return new e(bVarB, bArrX, bArrK6, bArrK7, z, 0);
    }

    private static byte[] k(Map<Integer, byte[]> map, int i, int i2, int i3) throws IOException {
        byte[] bArr = map.get(Integer.valueOf(i));
        if (bArr != null && bArr.length >= i2 && bArr.length <= i3) {
            return bArr;
        }
        throw new IOException("Pairing field " + i + " missing or invalid length");
    }

    private static String l(byte[] bArr) throws IOException {
        try {
            CharsetDecoder charsetDecoderNewDecoder = StandardCharsets.UTF_8.newDecoder();
            CodingErrorAction codingErrorAction = CodingErrorAction.REPORT;
            return charsetDecoderNewDecoder.onMalformedInput(codingErrorAction).onUnmappableCharacter(codingErrorAction).decode(ByteBuffer.wrap(bArr)).toString();
        } catch (CharacterCodingException e2) {
            throw new IOException("Invalid pairing identifier UTF-8", e2);
        }
    }

    public static byte[] m(Object... objArr) {
        if ((objArr.length & 1) != 0) {
            SyntheticRefs.b("TLV pairs");
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int i = 0; i < objArr.length; i += 2) {
            int iIntValue = ((Integer) objArr[i]).intValue();
            byte[] bArr = (byte[]) objArr[i + 1];
            if (iIntValue < 0 || iIntValue > 255 || bArr.length > 65535) {
                SyntheticRefs.b("TLV size/type");
                return null;
            }
            if (bArr.length == 0) {
                byteArrayOutputStream.write(iIntValue);
                byteArrayOutputStream.write(0);
            } else {
                for (int i2 = 0; i2 < bArr.length; i2 += 255) {
                    int iMin = Math.min(255, bArr.length - i2);
                    byteArrayOutputStream.write(iIntValue);
                    byteArrayOutputStream.write(iMin);
                    byteArrayOutputStream.write(bArr, i2, iMin);
                }
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static void n(String str) {
        if (str == null || str.isEmpty() || str.length() > 64) {
            SyntheticRefs.b("Setup code");
        }
    }

    public MfiSap.a b(EventChannel eventChannel, String str) {
        return MfiSap.a(eventChannel, str);
    }

    public b g(EventChannel eventChannel, String str) {
        n(str);
        return c(eventChannel, str, e(eventChannel, "/pair-setup", m(0, new byte[]{0}, 6, new byte[]{1}), 2));
    }

    public e h(EventChannel eventChannel, String str, String str2) {
        n(str);
        try {
            return i(eventChannel, c(eventChannel, str, e(eventChannel, "/pair-setup", m(0, new byte[]{0}, 6, new byte[]{1}), 2)));
        } catch (EventChannel.d e2) {
            if (e2.a == 455 && "PAIR_SETUP".equals(e2.b) && str2 != null && str2.matches("[0-9a-fA-F]{64}") && this.b != null) {
                return j(eventChannel, null, true);
            }
            throw e2;
        }
    }

    public e i(EventChannel eventChannel, b bVar) throws GeneralSecurityException {
        if (bVar != null) {
            return j(eventChannel, bVar, false);
        }
        throw new GeneralSecurityException("Pair-verify requires a previously authenticated accessory key");
    }

    public static final class e {
        private final b a;
        private final byte[] b;
        private final byte[] c;
        private final byte[] d;
        private final boolean e;

        private e(b bVar, byte[] bArr, byte[] bArr2, byte[] bArr3, boolean z) {
            this.a = bVar;
            this.b = (byte[]) bArr.clone();
            this.c = (byte[]) bArr2.clone();
            this.d = (byte[]) bArr3.clone();
            this.e = z;
        }

        public byte[] a() {
            return (byte[]) this.c.clone();
        }

        public byte[] b() {
            return (byte[]) this.d.clone();
        }

        public b c() {
            return this.a;
        }

        public boolean d() {
            return this.e;
        }

        public byte[] e() {
            return (byte[]) this.b.clone();
        }

        public /* synthetic */ e(b bVar, byte[] bArr, byte[] bArr2, byte[] bArr3, boolean z, int i) {
            this(bVar, bArr, bArr2, bArr3, z);
        }
    }
}
