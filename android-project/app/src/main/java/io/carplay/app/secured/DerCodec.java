/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 DerCodec。DER 编解码：解析/生成 MFi 密钥与证书使用的 DER 结构。
 *   This file defines DerCodec. DER codec: parses and emits the DER structures used by MFi keys and certificates.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.signers.Ed25519Signer;



/**
 * DER 编解码：解析/生成 MFi 密钥与证书使用的 DER 结构。
 *
 * DER codec: parses and emits the DER structures used by MFi keys and certificates.
 */
public final class DerCodec {
    private static final SecureRandom a = new SecureRandom();

    /**
     * DerCodec 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in DerCodec, carrying local implementation detail.
     */
    public static final class DerNode {
        final int a;
        final int b;

        public DerNode(int i, int i2) {
            this.a = i;
            this.b = i2;
        }

        public static DerNode a(byte[] bArr, int i, int i2) throws GeneralSecurityException {
            if (bArr != null && i >= 0 && i <= bArr.length - 2) {
                int i3 = i + 1;
                if ((bArr[i] & 255) == i2) {
                    int i4 = i + 2;
                    int i5 = bArr[i3];
                    int i6 = i5 & 255;
                    if ((i5 & Iap2Csm.b) != 0) {
                        int i7 = i5 & 127;
                        if (i7 < 1 || i7 > 3 || i7 > bArr.length - i4) {
                            throw new GeneralSecurityException("Invalid DER length");
                        }
                        int i8 = 0;
                        int i9 = 0;
                        while (i8 < i7) {
                            i9 = (i9 << 8) | (bArr[i4] & 255);
                            i8++;
                            i4++;
                        }
                        i6 = i9;
                    }
                    if (i6 < 0 || i6 > bArr.length - i4) {
                        throw new GeneralSecurityException("Truncated DER key encoding");
                    }
                    return new DerNode(i4, i6 + i4);
                }
            }
            throw new GeneralSecurityException("Invalid DER key encoding");
        }
    }

    private DerCodec() {
    }

    public static byte[] a(String str) {
        return str.getBytes(StandardCharsets.US_ASCII);
    }

    private static Object b(Object obj, String str) throws ReflectiveOperationException {
        return obj.getClass().getMethod(str, null).invoke(obj, null);
    }

    private static void c(byte[] bArr) {
        if (bArr.length == 32) {
            return;
        }
        SyntheticRefs.b("32-byte key required");
    }

    public static byte[] d(byte[]... bArr) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (byte[] bArr2 : bArr) {
            byteArrayOutputStream.write(bArr2, 0, bArr2.length);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static byte[] e(boolean z, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4) throws GeneralSecurityException {
        int i = 0;
        c(bArr);
        if (bArr2.length != 12) {
            SyntheticRefs.b("nonce length");
            return null;
        }
        String[] strArr = {"ChaCha20/Poly1305/NoPadding", "ChaCha20-Poly1305"};
        Exception e = null;
        while (true) {
            if (i >= 2) {
                try {
                    Class cls = Integer.TYPE;
                    Object objNewInstance = AEADParameters.class.getConstructor(KeyParameter.class, cls, byte[].class, byte[].class).newInstance(KeyParameter.class.getConstructor(byte[].class).newInstance(bArr), Integer.valueOf(Iap2Csm.b), bArr2, bArr4);
                    Object objNewInstance2 = ChaCha20Poly1305.class.getConstructor(null).newInstance(null);
                    objNewInstance2.getClass().getMethod("init", Boolean.TYPE, CipherParameters.class).invoke(objNewInstance2, Boolean.valueOf(z), objNewInstance);
                    byte[] bArr5 = new byte[((Integer) objNewInstance2.getClass().getMethod("getOutputSize", cls).invoke(objNewInstance2, Integer.valueOf(bArr3.length))).intValue()];
                    Integer num = (Integer) objNewInstance2.getClass().getMethod("processBytes", byte[].class, cls, cls, byte[].class, cls).invoke(objNewInstance2, bArr3, 0, Integer.valueOf(bArr3.length), bArr5, 0);
                    return Arrays.copyOf(bArr5, num.intValue() + ((Integer) objNewInstance2.getClass().getMethod("doFinal", byte[].class, cls).invoke(objNewInstance2, bArr5, num)).intValue());
                } catch (InvocationTargetException e2) {
                    throw new GeneralSecurityException("ChaCha20-Poly1305 verification failed", e2.getCause());
                } catch (ReflectiveOperationException e3) {
                    throw v("ChaCha20-Poly1305", e, e3);
                }
            }
            try {
                Cipher cipher = Cipher.getInstance(strArr[i]);
                cipher.init(z ? 1 : 2, new SecretKeySpec(bArr, "ChaCha20"), new IvParameterSpec(bArr2));
                cipher.updateAAD(bArr4);
                return cipher.doFinal(bArr3);
            } catch (AEADBadTagException e5) {
                throw e5;
            } catch (IllegalStateException | GeneralSecurityException | ProviderException e4) {
                e = e4;
                i++;
            }
        }
    }

    public static b f() throws GeneralSecurityException {
        return i("Ed25519");
    }

    public static byte[] g(byte[] bArr, byte[] bArr2) throws GeneralSecurityException {
        c(bArr);
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(KeyFactory.getInstance("Ed25519").generatePrivate(new PKCS8EncodedKeySpec(d(j("302e020100300506032b657004220420"), bArr))));
            signature.update(bArr2);
            return signature.sign();
        } catch (IllegalStateException | GeneralSecurityException | ProviderException e) {
            try {
                Object objR = r("Ed25519PrivateKeyParameters", bArr);
                Object objNewInstance = Ed25519Signer.class.getConstructor(null).newInstance(null);
                objNewInstance.getClass().getMethod("init", Boolean.TYPE, CipherParameters.class).invoke(objNewInstance, Boolean.TRUE, objR);
                Class<?> cls = objNewInstance.getClass();
                Class cls2 = Integer.TYPE;
                cls.getMethod("update", byte[].class, cls2, cls2).invoke(objNewInstance, bArr2, 0, Integer.valueOf(bArr2.length));
                return (byte[]) b(objNewInstance, "generateSignature");
            } catch (ReflectiveOperationException e2) {
                throw v("Ed25519", e, e2);
            }
        }
    }

    public static boolean h(byte[] bArr, byte[] bArr2, byte[] bArr3) throws GeneralSecurityException {
        c(bArr);
        if (bArr3.length != 64) {
            return false;
        }
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initVerify(KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(d(j("302a300506032b6570032100"), bArr))));
            signature.update(bArr2);
            return signature.verify(bArr3);
        } catch (IllegalStateException | GeneralSecurityException | ProviderException e) {
            try {
                Object objR = r("Ed25519PublicKeyParameters", bArr);
                Object objNewInstance = Ed25519Signer.class.getConstructor(null).newInstance(null);
                objNewInstance.getClass().getMethod("init", Boolean.TYPE, CipherParameters.class).invoke(objNewInstance, Boolean.FALSE, objR);
                Class<?> cls = objNewInstance.getClass();
                Class cls2 = Integer.TYPE;
                cls.getMethod("update", byte[].class, cls2, cls2).invoke(objNewInstance, bArr2, 0, Integer.valueOf(bArr2.length));
                return ((Boolean) objNewInstance.getClass().getMethod("verifySignature", byte[].class).invoke(objNewInstance, bArr3)).booleanValue();
            } catch (ReflectiveOperationException e2) {
                throw v("Ed25519", e, e2);
            }
        }
    }

    private static b i(String str) throws GeneralSecurityException {
        int i = 0;
        try {
            KeyPair keyPairGenerateKeyPair = KeyPairGenerator.getInstance(str).generateKeyPair();
            return new b(p(keyPairGenerateKeyPair.getPrivate().getEncoded()), u(keyPairGenerateKeyPair.getPublic().getEncoded()), i);
        } catch (IllegalStateException | GeneralSecurityException | ProviderException e) {
            try {
                Object objNewInstance = Class.forName(("org.bouncycastle.crypto.params." + str).concat("PrivateKeyParameters")).getConstructor(SecureRandom.class).newInstance(a);
                return new b((byte[]) b(objNewInstance, "getEncoded"), (byte[]) b(b(objNewInstance, "generatePublicKey"), "getEncoded"), i);
            } catch (ReflectiveOperationException e2) {
                throw v(str, e, e2);
            }
        }
    }

    private static byte[] j(String str) {
        int length = str.length() / 2;
        byte[] bArr = new byte[length];
        for (int i = 0; i < length; i++) {
            int i2 = i * 2;
            bArr[i] = (byte) Integer.parseInt(str.substring(i2, i2 + 2), 16);
        }
        return bArr;
    }

    public static byte[] k(byte[] bArr, String str, String str2) throws NoSuchAlgorithmException, InvalidKeyException {
        return l(bArr, a(str), a(str2), 32);
    }

    public static byte[] l(byte[] bArr, byte[] bArr2, byte[] bArr3, int i) throws NoSuchAlgorithmException, InvalidKeyException {
        if (i < 0 || i > 16320) {
            SyntheticRefs.b("HKDF length");
            return null;
        }
        Mac mac = Mac.getInstance("HmacSHA512");
        if (bArr2.length == 0) {
            bArr2 = new byte[64];
        }
        mac.init(new SecretKeySpec(bArr2, "HmacSHA512"));
        byte[] bArrDoFinal = mac.doFinal(bArr);
        byte[] bArrDoFinal2 = new byte[0];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i2 = 1;
        while (byteArrayOutputStream.size() < i) {
            try {
                mac.init(new SecretKeySpec(bArrDoFinal, "HmacSHA512"));
                mac.update(bArrDoFinal2);
                mac.update(bArr3);
                mac.update((byte) i2);
                bArrDoFinal2 = mac.doFinal();
                byteArrayOutputStream.write(bArrDoFinal2, 0, Math.min(bArrDoFinal2.length, i - byteArrayOutputStream.size()));
                i2++;
            } catch (Throwable th) {
                Arrays.fill(bArrDoFinal, (byte) 0);
                throw th;
            }
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        Arrays.fill(bArrDoFinal, (byte) 0);
        return byteArray;
    }

    public static byte[] m(long j) {
        byte[] bArr = new byte[12];
        for (int i = 0; i < 8; i++) {
            bArr[i + 4] = (byte) (j >>> (i * 8));
        }
        return bArr;
    }

    public static byte[] n(String str) {
        byte[] bArrA = a(str);
        if (bArrA.length == 8) {
            return d(new byte[4], bArrA);
        }
        SyntheticRefs.b("nonce label");
        return null;
    }

    public static byte[] o(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4) throws GeneralSecurityException {
        return e(false, bArr, bArr2, bArr3, bArr4);
    }

    private static byte[] p(byte[] bArr) throws GeneralSecurityException {
        DerNode aVarA = DerNode.a(bArr, DerNode.a(bArr, DerNode.a(bArr, DerNode.a(bArr, 0, 48).a, 2).b, 48).b, 4);
        byte[] bArrCopyOfRange = Arrays.copyOfRange(bArr, aVarA.a, aVarA.b);
        if (bArrCopyOfRange.length == 32) {
            return bArrCopyOfRange;
        }
        DerNode aVarA2 = DerNode.a(bArrCopyOfRange, 0, 4);
        int i = aVarA2.b;
        if (i == bArrCopyOfRange.length) {
            int i2 = aVarA2.a;
            if (i - i2 == 32) {
                return Arrays.copyOfRange(bArrCopyOfRange, i2, i);
            }
        }
        throw new GeneralSecurityException("Unsupported private-key encoding");
    }

    public static byte[] q(int i) {
        byte[] bArr = new byte[i];
        a.nextBytes(bArr);
        return bArr;
    }

    private static Object r(String str, byte[] bArr) throws ReflectiveOperationException {
        return Class.forName("org.bouncycastle.crypto.params." + str).getConstructor(byte[].class, Integer.TYPE).newInstance(bArr, 0);
    }

    public static byte[] s(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4) throws GeneralSecurityException {
        return e(true, bArr, bArr2, bArr3, bArr4);
    }

    public static byte[] t(byte[]... bArr) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        for (byte[] bArr2 : bArr) {
            messageDigest.update(bArr2);
        }
        return messageDigest.digest();
    }

    private static byte[] u(byte[] bArr) throws GeneralSecurityException {
        DerNode aVarA = DerNode.a(bArr, DerNode.a(bArr, DerNode.a(bArr, 0, 48).a, 48).b, 3);
        int i = aVarA.b;
        int i2 = aVarA.a;
        if (i - i2 == 33 && bArr[i2] == 0) {
            return Arrays.copyOfRange(bArr, i2 + 1, i);
        }
        throw new GeneralSecurityException("Unsupported public-key encoding");
    }

    private static GeneralSecurityException v(String str, Exception exc, Exception exc2) {
        GeneralSecurityException generalSecurityException = new GeneralSecurityException(str + " unavailable: bundle BC lightweight implementation or supported JCA provider", exc2);
        if (exc != null) {
            generalSecurityException.addSuppressed(exc);
        }
        return generalSecurityException;
    }

    public static b w() throws GeneralSecurityException {
        return i("X25519");
    }

    public static byte[] x(byte[] bArr, byte[] bArr2) throws GeneralSecurityException {
        byte[] bArrGenerateSecret;
        c(bArr);
        c(bArr2);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("XDH");
            KeyAgreement keyAgreement = KeyAgreement.getInstance("XDH");
            keyAgreement.init(keyFactory.generatePrivate(new PKCS8EncodedKeySpec(d(j("302e020100300506032b656e04220420"), bArr))));
            keyAgreement.doPhase(keyFactory.generatePublic(new X509EncodedKeySpec(d(j("302a300506032b656e032100"), bArr2))), true);
            bArrGenerateSecret = keyAgreement.generateSecret();
        } catch (IllegalStateException | GeneralSecurityException | ProviderException e) {
            try {
                Object objR = r("X25519PrivateKeyParameters", bArr);
                Object objR2 = r("X25519PublicKeyParameters", bArr2);
                byte[] bArr3 = new byte[32];
                objR.getClass().getMethod("generateSecret", objR2.getClass(), byte[].class, Integer.TYPE).invoke(objR, objR2, bArr3, 0);
                bArrGenerateSecret = bArr3;
            } catch (ReflectiveOperationException e2) {
                throw v("X25519", e, e2);
            }
        }
        int i = 0;
        for (byte b2 : bArrGenerateSecret) {
            i |= b2;
        }
        if (i != 0) {
            return bArrGenerateSecret;
        }
        throw new GeneralSecurityException("Invalid all-zero X25519 shared secret");
    }

    /**
     * DerCodec 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in DerCodec, carrying local implementation detail.
     */
    public static final class b {
        private final byte[] a;
        private final byte[] b;

        private b(byte[] bArr, byte[] bArr2) {
            this.a = (byte[]) bArr.clone();
            this.b = (byte[]) bArr2.clone();
        }

        public byte[] a() {
            return (byte[]) this.a.clone();
        }

        public byte[] b() {
            return (byte[]) this.b.clone();
        }

        public  b(byte[] bArr, byte[] bArr2, int i) {
            this(bArr, bArr2);
        }
    }
}
