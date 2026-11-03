# 未命名类证据表

共 180 个未命名类。按「证据强度」降序；证据不足者保留原名。

## `e4`（117 行，class）

- 包：`io.carplay.app.secured`
- implements：`g4`
- 被引用 22 次：`DisplaySession`×5, `PcmEndpoint`×3, `AvcEncoder`×2, `PairingSession`×2, `AirPlaySession`×2, `DerCodec`×2
- 字段（9）：g4 a, Supplier<b.C0003b> b, long c, long d, g4.g e, boolean f, g4.PcmFormat g, long h
- 方法（7）：void i(long j, g4.g gVar, boolean z), void a(long j, g4.g gVar, g4.PcmFormat eVar), void b(long j, g4.g gVar, g4.VideoMime hVar), void c(g4.c cVar), void e(long j, String str), void f(g4.c cVar), void g(long j, g4.g gVar)
- 常量：`sink/evidence`、`stale/wrong media source`、`video format/clock`、`PCM format/alignment/clock`、`end active source/use a fresh generation`

## `b2`（288 行，class）

- 包：`io.carplay.app.secured`
- 被引用 65 次：`X25519`×21, `CarLifeBridge`×6, `WirelessCarPlayService`×5, `HidCapabilities`×5, `MainActivity`×3, `BinaryPlist`×3
- 方法（6）：String b(String str), boolean c(String str, String... strArr), String d(String str), void a(String str), String b(String str), String c()
- 常量：`RFCOMM_SELECTED_PEER_CONNECTED`、`RFCOMM_SELECTED_PEER_ACCEPTED`、`RFCOMM_LISTENING_SELECTED_PEER`、`RFCOMM_SELECTED_PEER_ACTIVE_CONNECT_START`、`MFI_KEY_POSSESSION_VERIFIED`、`MFI_FRESH_PROOF_VERIFIED`、`IAP2_AUTHENTICATED_WAITING_CARPLAY_START`、`IAP2_AUTHENTICATED_WIFI_CONFIGURATION_RECEIVED`、`WIFI_AUTHENTICATED_TARGET`、`WIFI_NETWORK_READY_WAITING_CARPLAY_ENDPOINT`

## `m1`（220 行，class）

- 包：`io.carplay.app.secured`
- 被引用 25 次：`WirelessCarPlayService`×10, `AirPlaySession`×10, `MainActivity`×1, `PairingSession`×1, `j6`×1, `v6`×1
- 字段（5）：boolean a, int a, HidCapabilities.a b, int c, List<Integer> d
- 方法（18）：boolean b(boolean z), boolean c(d dVar), boolean equals(Object obj), boolean g(), int hashCode(), String toString(), boolean equals(Object obj), List<Integer> g()
- 常量：`REMOTE_NIGHT_MODE_INVALID`、`REMOTE_VIEW_AREA_NOT_DECLARED`、`REMOTE_VIEW_AREA_BINDING_INVALID`、`REMOTE_VIEW_AREA_ADJACENCY_INVALID`、`REMOTE_VIEW_AREA_PARAMETERS_INVALID`、`REMOTE_COMMAND_ENVELOPE_INVALID`

## `c3`（135 行，class）

- 包：`io.carplay.app.secured`
- 被引用 17 次：`EventChannel`×5, `WirelessCarPlayService`×4, `z1`×4, `AudioCapabilities`×3, `AirPlaySession`×1
- 字段（1）：Set<String> a
- 方法（3）：String a(Object obj), int b(Object obj), String c(Map<?, ?> map, SessionStateMachine.a aVa)
- 常量：`NON_STRING`、`OVERSIZE`、`HASH_UNAVAILABLE`、`INITIAL_RESOURCE`、`RESOURCE`、`APP_STATE`、` key_fingerprints=`、` unknown_fields=`、` keys_logged=false values_logged=false`

## `g4`（318 行，interface）

- 包：`io.carplay.app.secured`
- 被引用 82 次：`PcmEndpoint`×35, `e4`×19, `IccoaBridge`×19, `MicCapture`×8, `Iap2Ncm`×1
- 字段（16）：long a, g b, byte[] c, long d, boolean e, String a, int b, int c
- 方法（28）：boolean equals(Object obj), long g(), boolean h(), int hashCode(), byte[] i(), long j(), g k(), String toString()
- 常量：`video/avc`、`video/hevc`、`video dimensions/layout`、`PCM sample encoding unsupported/unverified`

## `j3`（243 行，class）

- 包：`io.carplay.app.secured`
- implements：`q2`
- 被引用 261 次：`WirelessCarPlayService`×41, `AirPlaySession`×29, `m4`×18, `d6`×12, `DisplaySession`×12, `l7`×11
- 字段（16）：long[] p, f2 a, long d, long e, long f, long g, long h, long i
- 方法（14）：long e(long j, long j2, long j3), long f(long j, long j2, long j3), long g(long j), long h(long j), long i(long j), long j(long j), void k(), void b(byte[] bArr, int i, int i2)

## `z`（167 行，class）

- 包：`io.carplay.app.secured`
- 被引用 860 次：`WirelessCarPlayService`×87, `AirPlaySession`×79, `PairingSession`×75, `MainActivity`×60, `MfiCertificateVerifier`×57, `SessionStateMachine`×40
- 字段（7）：int a, LinkedHashMap<String, a> b, String a, int b, int c, int d, int e
- 方法（16）：boolean equals(Object obj), String g(), int h(), int hashCode(), int i(), int j(), int k(), String toString()
- 常量：`CAP_HINT_INVALID`、`CAP_HINT_CACHE_BOUND`、`CAP_HINT_NOT_CURRENT`

## `j2`（71 行，class）

- 包：`io.carplay.app.secured`
- implements：`CipherParameters`
- 被引用 607 次：`WirelessCarPlayService`×129, `AirPlaySession`×68, `DisplaySession`×50, `CarLifeBridge`×24, `d6`×24, `AvcEncoder`×21
- 字段（7）：BigInteger a, BigInteger b, BigInteger c, BigInteger d, int e, int f, k2 g
- 方法（5）：BigInteger a(), BigInteger b(), BigInteger c(), boolean equals(Object obj), int hashCode()
- 常量：`org.bouncycastle.dh.allow_unsafe_p_value`

## `i1`（68 行，class）

- 包：`io.carplay.app.secured`
- implements：`AutoCloseable`
- 被引用 108 次：`WirelessCarPlayService`×85, `AirPlaySession`×13, `MainActivity`×3, `o0`×2, `p0`×1, `PairingSession`×1
- 字段（5）：long e, a a, long b, long c, boolean d
- 方法（4）：Integer b(Map<?, ?> map), long c(), void close(), boolean e(long j)
- 常量：`KEEPALIVE_PORT_INVALID`

## `l2`（48 行，class）

- 包：`io.carplay.app.secured`
- implements：`CipherParameters`
- 被引用 19 次：`i2`×10, `AirPlaySession`×4, `MainActivity`×3, `WirelessCarPlayService`×1, `i7`×1
- 字段（4）：BigInteger a, BigInteger b, BigInteger c, m2 d
- 方法（6）：BigInteger a(), BigInteger b(), BigInteger c(), m2 d(), boolean equals(Object obj), int hashCode()

## `c`（122 行，class）

- 包：`io.carplay.app.secured`
- 被引用 2780 次：`WirelessCarPlayService`×142, `AirPlaySession`×129, `PairingSession`×124, `X25519`×115, `Iap2Csm`×95, `AvcEncoder`×89
- 字段（5）：String a, String b, boolean c, int d, String e
- 方法（9）：boolean equals(Object obj), int g(), String h(), int hashCode(), boolean i(), String j(), String k(), String toString()
- 常量：`AES/ECB/PKCS5Padding`

## `f`（17 行，class）

- 包：`io.carplay.app.secured`
- implements：`CipherParameters`
- 被引用 1173 次：`DisplaySession`×80, `HidCapabilities`×57, `AirPlaySession`×51, `Poly1305`×41, `WirelessCarPlayService`×34, `CarLifeBridge`×33
- 方法（1）：boolean isPrivate()

## `i6`（106 行，class）

- 包：`io.carplay.app.secured`
- 被引用 300 次：`k4`×30, `l7`×25, `HidCapabilities`×22, `X25519`×20, `m4`×18, `ChaCha20Poly1305`×18
- 字段（5）：int a, int b, boolean c, int a, byte[] b
- 方法（7）：boolean equals(Object obj), int g(), byte[] h(), int hashCode(), String toString(), b a(TouchAction aVar, int i, int i2), void b()
- 常量：`touch coordinates/action`

## `b`（321 行，class）

- 包：`io.carplay.app.secured`
- 被引用 3443 次：`WirelessCarPlayService`×206, `PairingSession`×158, `AirPlaySession`×144, `X25519`×132, `DisplaySession`×116, `EncryptProofNeg`×105
- 字段（26）：long a, boolean b, boolean c, boolean d, boolean e, VerifyResult f, VerifyResult g, VerifyResult h
- 方法（35）：boolean g(VerifyResult aVar), boolean equals(Object obj), VerifyResult h(), int hashCode(), boolean i(), VerifyResult j(), long k(), VerifyResult l()

## `h`（175 行，class）

- 包：`io.carplay.app.secured`
- 被引用 898 次：`DisplaySession`×115, `WirelessCarPlayService`×67, `MfiCertificateVerifier`×56, `AvcEncoder`×55, `PairingSession`×54, `HidCapabilities`×44
- 字段（8）：List<byte[]> a, byte[] b, byte[] c, boolean d, long e, long f, long g, int h
- 方法（14）：h d(byte[] bArr, byte[] bArr2, byte[] bArr3,), int m(byte[] bArr, int i), byte[] a(), int b(), long c(), boolean e(), List<byte[]> f(), List<byte[]> g()

## `i5`（160 行，class）

- 包：`io.carplay.app.secured`
- 被引用 443 次：`BinaryPlist`×39, `k4`×38, `X25519`×32, `l7`×26, `HidCapabilities`×23, `d6`×22
- 字段（11）：String a, String b, b c, c a, i5 b, boolean c, LongSupplier a, Supplier<String> b
- 方法（10）：String d(String str), String a(), int b(), b c(), String e(), void close(), i5 e(), String d()

## `j5`（181 行，class）

- 包：`io.carplay.app.secured`
- 被引用 106 次：`m4`×18, `l7`×14, `WirelessCarPlayService`×9, `c6`×9, `d6`×8, `MfiCertificateVerifier`×8
- 字段（10）：long f, Set<Integer> g, boolean a, boolean b, long c, long d, Boolean e, boolean a
- 方法（15）：boolean equals(Object obj), boolean g(), int hashCode(), String toString(), boolean equals(Object obj), boolean g(), long h(), int hashCode()

## `j6`（118 行，interface）

- 包：`io.carplay.app.secured`
- 被引用 105 次：`m4`×22, `DisplaySession`×17, `WirelessCarPlayService`×14, `l7`×14, `d6`×12, `k4`×7
- 字段（7）：boolean a, boolean b, boolean c, boolean d, boolean e, boolean f, boolean g
- 方法（12）：boolean equals(Object obj), boolean g(), boolean h(), int hashCode(), boolean i(), boolean j(), boolean k(), boolean l()

## `l7`（611 行，class）

- 包：`io.carplay.app.secured`
- 被引用 267 次：`X25519`×219, `k7`×48
- 字段（2）：int[] a, int[] b
- 方法（42）：void A(int[] iArr, int[] iArr2), int B(int[] iArr), boolean C(int[] iArr), void D(int[] iArr, int i, int[] iArr2), void E(int[] iArr, int[] iArr2, int[] iArr3), void F(int[] iArr, int[] iArr2), void G(int[] iArr), void H(int[] iArr)

## `p6`（168 行，class）

- 包：`io.carplay.app.secured`
- 被引用 27 次：`WirelessCarPlayService`×11, `AvcEncoder`×10, `AirPlaySession`×4, `r`×2
- 字段（8）：int b, EnumMap<b, a> a, long a, int b, long c, long d, long e, long[] a
- 方法（13）：boolean equals(Object obj), long g(), long h(), int hashCode(), long i(), long j(), int k(), String toString()

## `q6`（218 行，class）

- 包：`io.carplay.app.secured`
- extends：`w1`
- 被引用 16 次：`WirelessCarPlayService`×10, `AvcEncoder`×4, `AirPlaySession`×2
- 字段（10）：int a, int b, int c, int d, int e, int f, int g, int h
- 方法（22）：boolean equals(Object obj), int g(), int h(), int hashCode(), String toString(), q6 i(int i, int i2, int i3, int i4), int l(int i, int i2, int i3, int i4, int i5, i), q6 r(int i, int i2, int i3, int i4)

## `t1`（196 行，class）

- 包：`io.carplay.app.secured`
- 被引用 75 次：`DisplaySession`×46, `EncryptProofNeg`×18, `PairingSession`×3, `WirelessCarPlayService`×3, `MainActivity`×1, `AirPlaySession`×1
- 字段（19）：int a, int b, int c, int d, int e, int f, int g, int h
- 方法（21）：boolean equals(Object obj), int g(), int h(), int hashCode(), String toString(), boolean equals(Object obj), int g(), int h()

## `y4`（69 行，class）

- 包：`io.carplay.app.secured`
- 被引用 32 次：`Poly1305`×16, `a6`×8, `y1`×3, `ChaCha20Poly1305`×3, `ChaCha20`×1, `j3`×1
- 方法（11）：int a(byte[] bArr, int i), long b(byte[] bArr, int i), void c(int i, byte[] bArr, int i2), void d(int i, byte[] bArr, int i2), void e(int[] iArr, byte[] bArr, int i), int f(byte[] bArr, int i), void g(byte[] bArr, int i, int[] iArr), void h(byte[] bArr, int i, int[] iArr, int i2, )

## `g`（63 行，class）

- 包：`io.carplay.app.secured`
- 被引用 1034 次：`WirelessCarPlayService`×74, `DisplaySession`×70, `AirPlaySession`×67, `HidCapabilities`×39, `PairingSession`×36, `IccoaBridge`×35
- 字段（7）：long f, int g, int a, boolean b, long c, long d, int e
- 方法（3）：long a(long j, long j2), long b(long j, int i, long j2), void c()
- 常量：`AUDIO_PACER_RATE`、`AUDIO_PACER_PACKET`

## `e`（86 行，class）

- 包：`io.carplay.app.secured`
- 被引用 1437 次：`DisplaySession`×140, `WirelessCarPlayService`×84, `AirPlaySession`×67, `HidCapabilities`×53, `CarLifeBridge`×42, `X25519`×39
- 方法（9）：boolean a(byte[] bArr, int i, int i2), boolean b(byte[] bArr, byte[] bArr2), void c(byte[] bArr), byte[] d(byte[] bArr), boolean e(int i, byte[] bArr, int i2, byte[] bArr2), void f(byte[] bArr, int i, int i2, byte b), int g(byte[] bArr), void h(byte[] bArr, byte[] bArr2)

## `l4`（314 行，class）

- 包：`io.carplay.app.secured`
- implements：`AutoCloseable`
- 被引用 13 次：`DisplaySession`×7, `EncryptProofNeg`×6
- 字段（17）：int g, int h, int i, long a, l5 b, SecureRandom c, d d, c e
- 方法（23）：boolean equals(Object obj), byte[] g(), long h(), int hashCode(), int i(), String j(), String toString(), boolean equals(Object obj)

## `a2`（47 行，class）

- 包：`io.carplay.app.secured`
- 被引用 70 次：`c6`×44, `MainActivity`×9, `X25519`×6, `AudioSourceCapability`×5, `EcdsaSignature`×3, `p`×1
- 方法（8）：int a(byte[] bArr, int i), int b(byte[] bArr, int i), int c(byte[] bArr, int i), void d(byte[] bArr, int i, int[] iArr, int i2, ), void e(int i, byte[] bArr, int i2), void f(int i, byte[] bArr, int i2), void g(int[] iArr, int i, int i2, byte[] bArr, ), void h(long j, byte[] bArr, int i)

## `o2`（17 行，class）

- 包：`io.carplay.app.secured`
- extends：`IllegalStateException`
- 被引用 54 次：`MainActivity`×48, `AirPlaySession`×4, `WirelessCarPlayService`×1, `w2`×1
- 字段（1）：Throwable a
- 方法（1）：Throwable getCause()

## `z1`（157 行，class）

- 包：`io.carplay.app.secured`
- extends：`ChaCha20`
- 被引用 29 次：`PairingSession`×19, `WirelessCarPlayService`×4, `j0`×2, `MainActivity`×1, `AirPlaySession`×1, `s3`×1
- 方法（1）：void m(int i, int[] iArr, int[] iArr2)

## `h1`（84 行，class）

- 包：`io.carplay.app.secured`
- 被引用 59 次：`WirelessCarPlayService`×41, `AirPlaySession`×8, `HidCapabilities`×6, `MainActivity`×1, `PairingSession`×1, `s`×1
- 方法（4）：String a(Map<?, ?> map), String b(Object obj), boolean c(Object obj, Object obj2), String d(Object obj)
- 常量：`UUID`

## `i2`（118 行，class）

- 包：`io.carplay.app.secured`
- 被引用 1754 次：`AirPlaySession`×139, `X25519`×88, `l7`×74, `WirelessCarPlayService`×71, `k4`×58, `MfiCertificateVerifier`×55
- 字段（18）：Logger a, Permission b, Permission c, Permission d, Permission e, ThreadLocal<Map<String, Object[]>> f, Map<String, Object[]> g, e6 h
- 方法（7）：void a(e2 e2Var), void a(e2 e2Var), int b(int i2), g2 c(), static <T> void d(SecurityProvider bVar, T... tArr), static <T> void e(SecurityProvider bVar, T[] tArr), j2 f(l2 l2Var)

## `t`（21 行，interface）

- 包：`io.carplay.app.secured`
- extends：`AutoCloseable`
- 被引用 298 次：`AirPlaySession`×52, `WirelessCarPlayService`×50, `MfiCertificateVerifier`×28, `SessionStateMachine`×25, `MainActivity`×17, `Poly1305`×12

## `n2`（9 行，class）

- 包：`io.carplay.app.secured`
- extends：`z5`
- 被引用 20 次：`MainActivity`×5, `HelpActivity`×3, `WirelessCarPlayService`×3, `AirPlaySession`×3, `ChaCha20Poly1305`×3, `ChaCha20`×1

## `r5`（17 行，class）

- 包：`io.carplay.app.secured`
- extends：`IOException`
- 被引用 156 次：`DisplaySession`×50, `EncryptProofNeg`×32, `MicCapture`×15, `ProtobufReader`×14, `l4`×10, `VideoStreamNeg`×9
- 字段（1）：long a

## `z5`（9 行，class）

- 包：`io.carplay.app.secured`
- extends：`RuntimeException`
- 被引用 35 次：`HidCapabilities`×9, `MfiCertificateVerifier`×7, `DisplaySession`×6, `MainActivity`×5, `j6`×4, `WirelessCarPlayService`×2

## `d3`（32 行，class）

- 包：`io.carplay.app.secured`
- 被引用 49 次：`z1`×32, `WirelessCarPlayService`×5, `PcmConverter`×4, `k4`×3, `d6`×2, `X25519`×2
- 方法（6）：int a(int i), int b(int i, int i2), int c(int i, int i2), int d(int i), int e(int i), int f(int i, int i2)

## `w1`（33 行，?）

- 包：`io.carplay.app.secured`
- 被引用 361 次：`WirelessCarPlayService`×25, `m4`×21, `AirPlaySession`×19, `DisplaySession`×19, `CarLifeBridge`×16, `HidCapabilities`×13
- 方法（6）：int a(long j, int i, int i2), int b(String str, int i, int i2), long c(long j, long j2, long j3, long j4), String d(String str, int i), String e(String str, String str2), void f(Class cls, StringBuilder sb, String str)

## `x2`（72 行，class）

- 包：`io.carplay.app.secured`
- 被引用 22 次：`MainActivity`×16, `WirelessCarPlayService`×4, `w2`×2
- 字段（2）：byte[] a, byte[] b
- 方法（2）：byte[] a(String str, int i, int i2), void b()
- 常量：`invalid offset and/or length specified`

## `j0`（64 行，?）

- 包：`io.carplay.app.secured`
- 被引用 49 次：`AirPlaySession`×18, `MfiCertificateVerifier`×14, `WirelessCarPlayService`×12, `MainActivity`×3, `DonationDialog`×2
- 字段（2）：int a, AirPlaySession b
- 方法（5）：byte[] a(EventChannel.b bVar), Iap2WirelessSetup.b b(), void c(String str, int i, int i2, boolean z), void d(List list), void e(Map map)

## `y1`（49 行，class）

- 包：`io.carplay.app.secured`
- extends：`ChaCha20`
- 被引用 15 次：`WirelessCarPlayService`×4, `PairingSession`×3, `AirPlaySession`×3, `ChaCha20Poly1305`×3, `MainActivity`×1, `c4`×1
- 方法（6）：void a(), void b(byte[] bArr), String c(), int d(), void j(), void l(byte[] bArr, byte[] bArr2)

## `i`（61 行，class）

- 包：`io.carplay.app.secured`
- 被引用 3942 次：`DisplaySession`×161, `WirelessCarPlayService`×131, `HidCapabilities`×128, `AirPlaySession`×123, `l7`×119, `Iap2Csm`×118
- 方法（3）：byte[] a(byte[] bArr), byte[] b(byte[] bArr, byte[] bArr2, boolean z), byte[] c(List<byte[]> list)

## `k1`（43 行，?）

- 包：`io.carplay.app.secured`
- 被引用 27 次：`WirelessCarPlayService`×14, `PairingSession`×4, `AirPlaySession`×2, `VideoCipher`×2, `w4`×2, `MainActivity`×1
- 字段（2）：int a, Object b
- 方法（3）：void a(DatagramSocket datagramSocket), void b(h hVar), void c(List list, long j, long j2, long j3)

## `z2`（42 行，?）

- 包：`io.carplay.app.secured`
- 被引用 152 次：`HidCapabilities`×25, `DisplaySession`×20, `WirelessCarPlayService`×19, `PairingSession`×17, `MainActivity`×10, `MfiCertificateVerifier`×9
- 字段（2）：Object a, Object b
- 方法（3）：o a(byte[] bArr), void c(List list, long j, long j2, long j3), void run()

## `a`（18 行，class）

- 包：`org.bouncycastle.crypto.signers`
- 被引用 3905 次：`AirPlaySession`×261, `DisplaySession`×256, `WirelessCarPlayService`×235, `PairingSession`×121, `MfiCertificateVerifier`×119, `AvcEncoder`×115
- 方法（2）：e2 a(String str, int i, CipherParameters ciph), f2 b(boolean z)

## `s`（36 行，?）

- 包：`io.carplay.app.prototype`
- 被引用 279 次：`WirelessCarPlayService`×81, `MfiCertificateVerifier`×18, `PairingSession`×17, `AirPlaySession`×16, `EventChannel`×16, `WifiNetworkMonitor`×13
- 字段（2）：int a, Object b
- 方法（2）：void a(boolean z), void c(List list, long j, long j2, long j3)

## `t0`（16 行，?）

- 包：`io.carplay.app.secured`
- 被引用 21 次：`WirelessCarPlayService`×7, `MfiCertificateVerifier`×6, `AirPlaySession`×5, `PairingSession`×3
- 方法（2）：BluetoothSocketException b(Exception exc), boolean e(Exception exc)

## `c0`（26 行，?）

- 包：`io.carplay.app.secured`
- 被引用 73 次：`WirelessCarPlayService`×26, `AirPlaySession`×17, `DisplaySession`×13, `MfiCertificateVerifier`×12, `MainActivity`×3, `BluetoothRfcommProfiles`×2
- 字段（1）：int a
- 方法（2）：public c0(int i), long getAsLong()

## `c2`（54 行，class）

- 包：`io.carplay.app.secured`
- 被引用 73 次：`DisplaySession`×17, `d6`×16, `EventChannel`×11, `EncryptProofNeg`×5, `k4`×4, `w5`×4
- 字段（2）：o a, boolean b
- 方法（2）：byte[] a(CarLifeFrame.c cVar), CarLifeFrame.c b(CarLifeFrame.a aVar, int i, byte[] bArr)

## `l5`（23 行，interface）

- 包：`io.carplay.app.secured`
- 被引用 40 次：`l4`×13, `o5`×10, `EncryptProofNeg`×8, `DisplaySession`×5, `k5`×4
- 方法（2）：l5 b(), a e(String str, String str2, String str3, St)

## `r4`（22 行，?）

- 包：`io.carplay.app.secured`
- 被引用 30 次：`AirPlaySession`×18, `DisplaySession`×7, `AudioRelay`×5
- 字段（2）：int a, DisplaySession.c b
- 方法（2）：public r4(int i), void accept(Object obj)

## `a0`（27 行，?）

- 包：`io.carplay.app.prototype`
- 被引用 23 次：`DisplaySession`×5, `BluetoothRfcommProfiles`×4, `WirelessCarPlayService`×4, `HidCommandSender`×3, `CarPlayCtrlAdvertiser`×2, `MainActivity`×2
- 字段（2）：int a, BluetoothRfcommProfiles b
- 方法（1）：void run()

## `b0`（42 行，?）

- 包：`io.carplay.app.secured`
- 被引用 61 次：`AirPlaySession`×30, `MainActivity`×10, `WirelessCarPlayService`×6, `MfiCertificateVerifier`×6, `DisplaySession`×5, `BluetoothRfcommProfiles`×2
- 字段（2）：int a, AirPlaySession b
- 方法（1）：void run()

## `d`（26 行，?）

- 包：`io.carplay.app.prototype`
- 被引用 1802 次：`WirelessCarPlayService`×206, `AirPlaySession`×91, `DisplaySession`×80, `X25519`×70, `HidCapabilities`×68, `PairingSession`×51
- 字段（3）：int a, int b, Object c
- 方法（1）：void run()

## `e0`（16 行，?）

- 包：`io.carplay.app.secured`
- 被引用 44 次：`WirelessCarPlayService`×17, `AirPlaySession`×14, `MfiCertificateVerifier`×8, `MainActivity`×3, `DonationDialog`×2
- 字段（1）：AtomicBoolean a
- 方法（1）：boolean getAsBoolean()

## `f0`（15 行，?）

- 包：`io.carplay.app.secured`
- 被引用 29 次：`AirPlaySession`×7, `WirelessCarPlayService`×6, `MfiCertificateVerifier`×6, `MainActivity`×3, `UiFactory`×2, `EcdsaSignature`×2
- 字段（1）：AirPlaySession a
- 方法（1）：void accept(int i)

## `g0`（18 行，?）

- 包：`io.carplay.app.secured`
- 被引用 33 次：`AirPlaySession`×17, `WirelessCarPlayService`×6, `MfiCertificateVerifier`×6, `MainActivity`×4
- 字段（4）：AirPlaySession a, long b, EventChannel c, int d
- 方法（1）：void accept(Object obj, Object obj2)

## `h0`（31 行，?）

- 包：`io.carplay.app.secured`
- 被引用 26 次：`WirelessCarPlayService`×9, `AirPlaySession`×8, `MfiCertificateVerifier`×4, `MainActivity`×3, `UiFactory`×2
- 字段（5）：int a, long b, AirPlaySession c, Object d, AutoCloseable e
- 方法（1）：void run()

## `i0`（37 行，?）

- 包：`io.carplay.app.prototype`
- 被引用 30 次：`AirPlaySession`×8, `WirelessCarPlayService`×7, `MfiCertificateVerifier`×7, `MainActivity`×3, `DonationDialog`×2, `PairingSession`×2
- 字段（7）：int a, int b, Object c, Object d, Object e, Object f, Object g
- 方法（1）：void run()

## `k`（28 行，?）

- 包：`io.carplay.app.secured`
- 被引用 580 次：`WirelessCarPlayService`×69, `AirPlaySession`×44, `SessionStateMachine`×44, `DisplaySession`×36, `EncryptProofNeg`×25, `PairingSession`×22
- 字段（1）：int a
- 方法（1）：boolean test(int i)

## `l`（27 行，?）

- 包：`io.carplay.app.prototype`
- 被引用 508 次：`UiFactory`×78, `DisplaySession`×43, `AirPlaySession`×36, `WirelessCarPlayService`×26, `MfiCertificateVerifier`×25, `MainActivity`×23
- 字段（2）：int a, WifiNetworkMonitor.a b
- 方法（1）：void run()

## `l0`（34 行，?）

- 包：`io.carplay.app.secured`
- 被引用 25 次：`AirPlaySession`×10, `MfiCertificateVerifier`×6, `MainActivity`×4, `WirelessCarPlayService`×3, `UiFactory`×2
- 字段（2）：int a, Object b
- 方法（1）：Object get()

## `n`（21 行，class）

- 包：`io.carplay.app.secured`
- 被引用 556 次：`AirPlaySession`×168, `HelpActivity`×40, `DisplaySession`×32, `MainActivity`×31, `WirelessCarPlayService`×29, `j3`×18
- 方法（1）：String a(String str)

## `n0`（39 行，?）

- 包：`io.carplay.app.secured`
- 被引用 51 次：`MfiCertificateVerifier`×24, `WirelessCarPlayService`×11, `AirPlaySession`×8, `MainActivity`×3, `AirplayServiceProbe`×1, `PairingSession`×1
- 字段（1）：int a
- 方法（1）：Thread newThread(Runnable runnable)

## `o0`（15 行，?）

- 包：`io.carplay.app.secured`
- 被引用 26 次：`AirPlaySession`×15, `UiFactory`×4, `WirelessCarPlayService`×4, `MfiCertificateVerifier`×3
- 字段（1）：DatagramSocket a
- 方法（1）：void a(byte[] bArr)

## `p`（32 行，?）

- 包：`io.carplay.app.secured`
- 被引用 378 次：`AirPlaySession`×36, `DisplaySession`×22, `IccoaBridge`×17, `AvcEncoder`×15, `CarPlayCtrlAdvertiser`×15, `PairingSession`×13
- 字段（1）：int a
- 方法（1）：long applyAsLong(Object obj)

## `p0`（54 行，?）

- 包：`io.carplay.app.secured`
- 被引用 32 次：`AirPlaySession`×11, `WirelessCarPlayService`×7, `PairingSession`×4, `MfiCertificateVerifier`×3, `LogActivity`×2, `UiFactory`×2
- 字段（3）：int a, Object b, Object c
- 方法（1）：void run()

## `q`（26 行，?）

- 包：`io.carplay.app.secured`
- 被引用 222 次：`AirPlaySession`×34, `AvcEncoder`×15, `IccoaBridge`×13, `MfiCertificateVerifier`×12, `Poly1305`×11, `AirplayServiceProbe`×10
- 字段（1）：int a
- 方法（1）：int applyAsInt(Object obj)

## `q0`（32 行，?）

- 包：`io.carplay.app.secured`
- 被引用 43 次：`AirPlaySession`×27, `WirelessCarPlayService`×8, `MfiCertificateVerifier`×8
- 字段（4）：int a, Object b, Object c, Object d
- 方法（1）：void run()

## `r`（38 行，?）

- 包：`io.carplay.app.secured`
- 被引用 233 次：`MainActivity`×21, `MfiCertificateVerifier`×18, `CarPlayCtrlAdvertiser`×17, `PairingSession`×17, `EventChannel`×17, `AirPlaySession`×12
- 字段（1）：int a
- 方法（1）：Object apply(Object obj)

## `s0`（21 行，?）

- 包：`io.carplay.app.prototype`
- 被引用 28 次：`WirelessCarPlayService`×13, `MfiCertificateVerifier`×9, `AirPlaySession`×4, `ControlRequestParser`×2
- 字段（6）：WirelessCarPlayService.a a, Network b, Iap2Csm.a c, String d, long e, PairingSession.i f
- 方法（1）：void run()

## `v`（13 行，?）

- 包：`io.carplay.app.secured`
- 被引用 144 次：`MfiCertificateVerifier`×20, `AvcEncoder`×14, `PairingSession`×13, `DisplaySession`×10, `WirelessCarPlayService`×9, `AirPlaySession`×9
- 方法（1）：int compare(Object obj, Object obj2)

## `w`（27 行，?）

- 包：`io.carplay.app.secured`
- 被引用 104 次：`AirPlaySession`×14, `WirelessCarPlayService`×10, `DisplaySession`×10, `MfiCertificateVerifier`×9, `AvcEncoder`×8, `MainActivity`×5
- 字段（1）：int a
- 方法（1）：Object apply(Object obj, Object obj2)

## `x`（17 行，?）

- 包：`io.carplay.app.prototype`
- 被引用 139 次：`WirelessCarPlayService`×21, `DisplaySession`×17, `MfiCertificateVerifier`×17, `AirPlaySession`×14, `PairingSession`×8, `EventChannel`×7
- 方法（1）：Thread newThread(Runnable runnable)

## `y`（29 行，?）

- 包：`io.carplay.app.secured`
- 被引用 128 次：`MfiCertificateVerifier`×22, `PairingSession`×18, `AirPlaySession`×13, `MainActivity`×10, `WirelessCarPlayService`×10, `EncryptProofNeg`×9
- 字段（2）：int a, z.a b
- 方法（1）：boolean test(Object obj)

## `b5`（32 行，?）

- 包：`io.carplay.app.secured`
- 被引用 21 次：`WirelessCarPlayService`×13, `X25519`×5, `PairingPreparationService`×3
- 字段（2）：int a, Service b
- 方法（1）：void accept(Object obj)

## `d1`（44 行，?）

- 包：`io.carplay.app.secured`
- 被引用 32 次：`AvcEncoder`×7, `AirPlaySession`×7, `WirelessCarPlayService`×4, `Iap2WirelessSetup`×4, `LogActivity`×3, `BtDiscovery`×2
- 字段（2）：int a, Object b
- 方法（1）：void run()

## `e3`（9 行，class）

- 包：`io.carplay.app.secured`
- 被引用 57 次：`WirelessCarPlayService`×13, `AirPlaySession`×9, `AvcEncoder`×7, `DisplaySession`×6, `EventChannelConnector`×5, `EventChannel`×4
- 方法（1）：int a(int i)

## `f1`（33 行，?）

- 包：`io.carplay.app.secured`
- 被引用 22 次：`WirelessCarPlayService`×10, `AirPlaySession`×4, `Iap2WirelessSetup`×2, `MainActivity`×1, `PairingSession`×1, `i5`×1
- 字段（1）：int a
- 方法（1）：Object get()

## `g1`（24 行，?）

- 包：`io.carplay.app.secured`
- 被引用 85 次：`WirelessCarPlayService`×63, `AirPlaySession`×16, `Iap2WirelessSetup`×2, `MainActivity`×1, `PairingSession`×1, `o3`×1
- 字段（1）：int a
- 方法（1）：boolean getAsBoolean()

## `i3`（33 行，?）

- 包：`io.carplay.app.secured`
- 被引用 1006 次：`k4`×63, `X25519`×57, `AirPlaySession`×54, `l7`×46, `DisplaySession`×32, `Iap2Csm`×32
- 字段（2）：int a, LogActivity b
- 方法（1）：void onClick(View view)

## `i7`（15 行，?）

- 包：`io.carplay.app.secured`
- 被引用 223 次：`d6`×22, `k4`×21, `m4`×18, `l7`×16, `ChaCha20Poly1305`×16, `HidCapabilities`×14
- 字段（1）：WirelessCarPlayService a
- 方法（1）：void a(String str)

## `j7`（44 行，class）

- 包：`io.carplay.app.secured`
- 被引用 60 次：`m4`×16, `d6`×12, `l7`×12, `k4`×7, `c6`×4, `j3`×4
- 方法（1）：void a(int[] iArr, int i, byte[] bArr)

## `k0`（24 行，?）

- 包：`io.carplay.app.secured`
- 被引用 30 次：`WirelessCarPlayService`×11, `AirPlaySession`×8, `MainActivity`×5, `MfiCertificateVerifier`×4, `StatusText`×2
- 字段（2）：int a, AirPlaySession.n b
- 方法（1）：void accept(Object obj)

## `m`（10 行，class）

- 包：`io.carplay.app.secured`
- 被引用 372 次：`PairingSession`×34, `AirPlaySession`×31, `DisplaySession`×23, `b`×18, `WirelessCarPlayService`×16, `MainActivity`×15
- 方法（1）：int a(int i, int i2, int i3)

## `m0`（16 行，?）

- 包：`io.carplay.app.secured`
- 被引用 32 次：`AirPlaySession`×10, `MfiCertificateVerifier`×10, `WirelessCarPlayService`×7, `MainActivity`×3, `UiFactory`×2
- 字段（2）：AirPlaySession a, EventChannel b
- 方法（1）：boolean getAsBoolean()

## `o3`（95 行，?）

- 包：`io.carplay.app.secured`
- 被引用 45 次：`MainActivity`×45
- 字段（2）：int a, MainActivity b
- 方法（1）：void run()

## `r0`（18 行，?）

- 包：`io.carplay.app.secured`
- 被引用 60 次：`AirPlaySession`×37, `AudioRelay`×12, `MfiCertificateVerifier`×6, `WirelessCarPlayService`×3, `PairingSession`×2
- 字段（2）：AirPlaySession a, int b
- 方法（1）：Socket a(int i)

## `s1`（30 行，?）

- 包：`io.carplay.app.secured`
- 被引用 22 次：`PairingSession`×6, `MainActivity`×5, `AirPlaySession`×5, `EncryptProofNeg`×2, `t1`×2, `WirelessCarPlayService`×1
- 字段（2）：int a, Object b
- 方法（1）：void accept(Object obj, Object obj2)

## `u`（29 行，?）

- 包：`io.carplay.app.secured`
- 被引用 179 次：`PairingSession`×45, `MfiCertificateVerifier`×19, `Poly1305`×13, `AvcEncoder`×11, `AirPlaySession`×11, `BtDiscovery`×9
- 字段（2）：int a, int b
- 方法（1）：boolean test(Object obj)

## `u0`（16 行，?）

- 包：`io.carplay.app.secured`
- 被引用 23 次：`WirelessCarPlayService`×9, `MfiCertificateVerifier`×6, `AirPlaySession`×5, `AirplayServiceProbe`×3
- 字段（1）：AirplayServiceProbe a
- 方法（1）：void execute(Runnable runnable)

## `u6`（62 行，?）

- 包：`io.carplay.app.secured`
- 被引用 30 次：`PairingSession`×30
- 字段（2）：int a, PairingSession b
- 方法（1）：void run()

## `v0`（29 行，?）

- 包：`io.carplay.app.secured`
- 被引用 36 次：`AirPlaySession`×16, `WirelessCarPlayService`×9, `MfiCertificateVerifier`×6, `AirplayServiceProbe`×5
- 字段（2）：int a, AirplayServiceProbe b
- 方法（1）：void run()

## `w0`（64 行，?）

- 包：`io.carplay.app.secured`
- 被引用 42 次：`AirPlaySession`×19, `MfiCertificateVerifier`×8, `WirelessCarPlayService`×3, `HidCapabilities`×3, `AirplayServiceProbe`×2, `AudioCapabilities`×1
- 字段（1）：int a
- 方法（1）：boolean test(Object obj)

## `z3`（16 行，?）

- 包：`io.carplay.app.secured`
- 被引用 72 次：`HidCapabilities`×14, `DisplaySession`×11, `MainActivity`×10, `WirelessCarPlayService`×9, `PairingSession`×8, `MfiCertificateVerifier`×7
- 字段（1）：MainActivity a
- 方法（1）：void onCheckedChanged(RadioGroup radioGroup, int i)

## `z6`（21 行，?）

- 包：`io.carplay.app.secured`
- 被引用 20 次：`WirelessCarPlayService`×6, `PairingSession`×5, `j6`×4, `DisplaySession`×3, `HidCapabilities`×2
- 字段（4）：PairingSession a, BluetoothServerSocket b, BluetoothDevice c, AtomicInteger d
- 方法（1）：void run()

## `o`（9 行，interface）

- 包：`io.carplay.app.secured`
- 被引用 310 次：`AirPlaySession`×64, `AvcEncoder`×19, `DisplaySession`×19, `PairingSession`×16, `WirelessCarPlayService`×12, `EventChannel`×12

## `e2`（6 行，interface）

- 包：`io.carplay.app.secured`
- 被引用 230 次：`AirPlaySession`×56, `PairingSession`×30, `WirelessCarPlayService`×22, `EventChannel`×19, `DisplaySession`×17, `AvcEncoder`×10

## `f2`（16 行，enum）

- 包：`io.carplay.app.secured`
- 被引用 29 次：`UiFactory`×5, `a`×4, `WirelessCarPlayService`×3, `n6`×3, `o6`×3, `p2`×3

## `q2`（13 行，interface）

- 包：`io.carplay.app.secured`
- 被引用 26 次：`MainActivity`×7, `X25519`×6, `AirPlaySession`×5, `a6`×3, `WirelessCarPlayService`×2, `j3`×2

## `c6`（256 行，class）

- 包：`io.carplay.app.secured`
- 被引用 15 次：`X25519`×13, `z1`×2
- 字段（2）：int[] a, int[] b
- 方法（7）：boolean a(byte[] bArr, int[] iArr), void b(byte[] bArr, int[] iArr), void c(int[] iArr, int[] iArr2, int[] iArr3), byte[] d(byte[] bArr), byte[] e(byte[] bArr), boolean f(int[] iArr, int[] iArr2, int[] iArr3), void g(int i, int[] iArr)

## `w5`（242 行，class）

- 包：`io.carplay.app.secured`
- extends：`w1`
- 被引用 3 次：`UpdateChecker`×2, `v5`×1
- 字段（10）：long a, long b, long c, String d, long e, List f, long a, String b
- 方法（11）：boolean equals(Object obj), int hashCode(), String toString(), long g(String str), URI j(URI uri, String str), w5 k(String str, String str2, long j, long j2), boolean equals(Object obj), a h()
- 常量：`/eyessx/carplus-carplay-updates/raw/master/version.json`、`CARPLUS_RELEASES_V1`、`release stale/clock/rollback`

## `m5`（247 行，class）

- 包：`io.carplay.app.secured`
- 被引用 10 次：`MfiSap`×6, `MfiCertificateVerifier`×4
- 字段（4）：X509Certificate a, byte[] b, String c, boolean d
- 方法（10）：boolean c(X509Certificate x509Certificate, Date da), m5 f(byte[] bArr), String g(byte[] bArr), byte[] i(byte[] bArr), byte[] a(), byte[] b(), boolean d(), String e()
- 常量：`RSA/ECB/PKCS1Padding`

## `o5`（116 行，class）

- 包：`io.carplay.app.secured`
- implements：`l5, AutoCloseable`
- 被引用 7 次：`DisplaySession`×6, `HidCommandSender`×1
- 字段（4）：int c, char[] d, byte[] a, boolean b
- 方法（5）：o5 g(), byte[] i(String str), long k(String str), void close(), l5.a f(String str, String str2, String str3, St)
- 常量：`CARLIFE`

## `r1`（16 行，?）

- 包：`io.carplay.app.secured`
- 被引用 17 次：`CarPlusApplication`×3, `PairingSession`×3, `AirPlaySession`×3, `AudioRelay`×3, `WirelessCarPlayService`×2, `CarLifeBridge`×1
- 字段（2）：CarPlusApplication a, Thread.UncaughtExceptionHandler b
- 方法（1）：void uncaughtException(Thread thread, Throwable th)

## `u2`（33 行，?）

- 包：`io.carplay.app.secured`
- 被引用 17 次：`MainActivity`×6, `HelpActivity`×5, `WirelessCarPlayService`×3, `AirPlaySession`×2, `b0`×1
- 字段（2）：int a, Object b
- 方法（1）：void onClick(View view)

## `h2`（40 行，class）

- 包：`io.carplay.app.secured`
- extends：`Permission`
- 被引用 11 次：`MainActivity`×4, `i2`×4, `WirelessCarPlayService`×1, `AirPlaySession`×1, `b7`×1
- 字段（1）：Set<String> a
- 方法（4）：boolean equals(Object obj), String getActions(), int hashCode(), boolean implies(Permission permission)

## `c5`（29 行，class）

- 包：`io.carplay.app.secured`
- implements：`CipherParameters`
- 被引用 10 次：`ChaCha20Poly1305`×5, `ChaCha20`×3, `z1`×2
- 字段（2）：byte[] a, CipherParameters b
- 方法（2）：byte[] a(), CipherParameters b()

## `g5`（76 行，class）

- 包：`io.carplay.app.secured`
- extends：`w1`
- 被引用 3 次：`MfiCertificateVerifier`×3
- 字段（3）：long a, int b, int c
- 方法（9）：g5 g(Instant instant, ZoneId zoneId), g5 j(), boolean equals(Object obj), int h(), int hashCode(), long i(), byte[] k(), int l()
- 常量：`DEVICE_TIME_RANGE`、`DEVICE_TIME_MINUTE_PRECISION`

## `f3`（9 行，class）

- 包：`io.carplay.app.secured`
- extends：`d2`
- 被引用 12 次：`UiFactory`×6, `ChaCha20Poly1305`×4, `WirelessCarPlayService`×2

## `g3`（73 行，class）

- 包：`io.carplay.app.secured`
- implements：`o`
- 被引用 6 次：`WirelessCarPlayService`×4, `DisplaySession`×2
- 字段（4）：String a, Provider b, SecretKey c, AlgorithmParameterSpec d
- 方法（3）：byte[] c(int i, byte[] bArr), byte[] a(byte[] bArr), byte[] b(byte[] bArr)
- 常量：`explicit algorithm/mode/padding required`

## `a1`（28 行，?）

- 包：`io.carplay.app.secured`
- 被引用 15 次：`AirPlaySession`×4, `MainActivity`×2, `WirelessCarPlayService`×2, `m1`×2, `SessionStateMachine`×2, `PairingSession`×1
- 字段（2）：int a, Set b
- 方法（1）：boolean test(Object obj)

## `t6`（40 行，?）

- 包：`io.carplay.app.secured`
- 被引用 15 次：`PairingSession`×15
- 字段（2）：int a, PairingSession b
- 方法（1）：void accept(Object obj)

## `v2`（34 行，?）

- 包：`io.carplay.app.secured`
- 被引用 15 次：`MainActivity`×6, `HelpActivity`×3, `WirelessCarPlayService`×2, `AirPlaySession`×2, `BtDiscovery`×2
- 字段（4）：int a, String b, KeyEvent.Callback c, KeyEvent.Callback d
- 方法（1）：void onClick(View view)

## `d6`（323 行，class）

- 包：`io.carplay.app.secured`
- 被引用 11 次：`c6`×8, `PcmConverter`×3
- 方法（7）：void a(int i, int i2, int[] iArr, int[] iArr2, ), void b(int i, int i2, int[] iArr, int[] iArr2, ), int c(int i, int[] iArr), int d(int i, int[] iArr), boolean e(int i, int[] iArr, int[] iArr2), void f(int i, int i2, int[] iArr, int[] iArr2, ), void g(int i, int i2, int[] iArr, int[] iArr2, )

## `w4`（104 行，class）

- 包：`io.carplay.app.secured`
- 被引用 11 次：`WirelessCarPlayService`×4, `k1`×4, `z2`×2, `w0`×1
- 字段（2）：VideoCipher.AvcVideoInput a, AirPlaySession.g b
- 方法（7）：void f(a aVar, h hVar), void g(b bVar, List list, long j, long j2, long), boolean h(byte[] bArr), void d(byte[] bArr, a aVar), void e(byte[] bArr, long j, long j2, long j3, b), AirPlaySession.g i(), void j()

## `p2`（21 行，class）

- 包：`io.carplay.app.secured`
- implements：`e2`
- 被引用 9 次：`AirPlaySession`×3, `a`×2, `MainActivity`×2, `WirelessCarPlayService`×1, `ChaCha20`×1
- 字段（4）：String a, int b, Object c, f2 d

## `t5`（161 行，class）

- 包：`io.carplay.app.secured`
- 被引用 7 次：`WirelessCarPlayService`×7
- 字段（5）：b a, Map<String, String> b, boolean c, String a, int b
- 方法（13）：boolean equals(Object obj), b g(), boolean h(), int hashCode(), Map<String, String> i(), String toString(), boolean equals(Object obj), int g()
- 常量：`legacy_owner`

## `d2`（16 行，class）

- 包：`io.carplay.app.secured`
- extends：`Exception`
- 被引用 10 次：`PcmConverter`×3, `WirelessCarPlayService`×2, `MainActivity`×1, `AirPlaySession`×1, `f3`×1, `f7`×1
- 字段（1）：Throwable a
- 方法（1）：Throwable getCause()

## `m2`（42 行，class）

- 包：`io.carplay.app.secured`
- 被引用 12 次：`i2`×4, `l2`×3, `AirPlaySession`×2, `MainActivity`×1, `WirelessCarPlayService`×1, `o3`×1
- 字段（3）：int a, byte[] b, int c
- 方法（4）：int a(), byte[] b(), boolean equals(Object obj), int hashCode()

## `g2`（7 行，interface）

- 包：`io.carplay.app.secured`
- 被引用 14 次：`i2`×6, `MainActivity`×3, `AirPlaySession`×3, `q0`×1, `WirelessCarPlayService`×1

## `u3`（42 行，?）

- 包：`io.carplay.app.secured`
- 被引用 13 次：`MainActivity`×13
- 字段（2）：int a, MainActivity b
- 方法（1）：void onClick(DialogInterface dialogInterface, int i)

## `w2`（15 行，class）

- 包：`io.carplay.app.secured`
- 被引用 13 次：`MainActivity`×5, `i2`×4, `WirelessCarPlayService`×2, `AirPlaySession`×2
- 字段（1）：x2 a
- 方法（1）：byte[] a(String str)

## `o1`（15 行，?）

- 包：`io.carplay.app.secured`
- 被引用 12 次：`AirPlaySession`×3, `PairingSession`×2, `WirelessCarPlayService`×2, `AudioSourceCapability`×2, `MainActivity`×1, `o3`×1
- 字段（1）：HidCapabilities.c a
- 方法（1）：Object apply(Object obj)

## `r3`（20 行，?）

- 包：`io.carplay.app.secured`
- 被引用 12 次：`AirPlaySession`×9, `MainActivity`×3
- 字段（4）：MainActivity a, SharedPreferences b, String c, String d
- 方法（1）：void onClick(DialogInterface dialogInterface, int i)

## `m4`（370 行，class）

- 包：`io.carplay.app.secured`
- 被引用 8 次：`c6`×6, `X25519`×2
- 方法（7）：int a(int[] iArr, int i, int[] iArr2, int i2, ), boolean b(int[] iArr, int[] iArr2), void c(int[] iArr, int[] iArr2, int[] iArr3), void d(int[] iArr, int[] iArr2, int[] iArr3), int e(int[] iArr, int[] iArr2, int[] iArr3), void f(int[] iArr, int[] iArr2), int g(int[] iArr, int i, int[] iArr2, int i2, )

## `s5`（142 行，class）

- 包：`io.carplay.app.secured`
- extends：`w1`
- 被引用 2 次：`EncryptProofNeg`×2
- 字段（11）：String a, String b, int c, String d, int e, int f, int g, String h
- 方法（17）：void m(String str), boolean equals(Object obj), String g(), String h(), int hashCode(), int i(), int j(), byte[] k()

## `v6`（37 行，?）

- 包：`io.carplay.app.secured`
- 被引用 11 次：`PairingSession`×11
- 字段（4）：int a, PairingSession b, BluetoothDevice c, AtomicInteger d
- 方法（1）：void run()

## `k2`（28 行，class）

- 包：`io.carplay.app.secured`
- 被引用 10 次：`WirelessCarPlayService`×4, `j2`×2, `MainActivity`×1, `AirPlaySession`×1, `i2`×1, `o3`×1
- 字段（2）：byte[] a, int b
- 方法（2）：boolean equals(Object obj), int hashCode()

## `f4`（99 行，class）

- 包：`io.carplay.app.secured`
- 被引用 5 次：`AirPlaySession`×3, `UiFactory`×2
- 字段（6）：boolean a, long b, long c, long d, long e, long a
- 方法（10）：boolean equals(Object obj), long g(), int hashCode(), String toString(), void a(boolean z, long j, long j2), boolean b(), boolean c(a aVar), a d(long j)

## `a4`（36 行，?）

- 包：`io.carplay.app.secured`
- 被引用 10 次：`WirelessCarPlayService`×5, `MainActivity`×3, `BtDiscovery`×2
- 字段（2）：int a, Object b
- 方法（1）：boolean test(Object obj)

## `n3`（33 行，?）

- 包：`io.carplay.app.secured`
- 被引用 10 次：`MainActivity`×7, `HelpActivity`×3
- 字段（5）：int a, MainActivity b, String c, AlertDialog d, EditText e
- 方法（1）：void onClick(View view)

## `x4`（9 行，class）

- 包：`io.carplay.app.secured`
- extends：`n2`
- 被引用 6 次：`ChaCha20Poly1305`×4, `ChaCha20`×1, `Poly1305`×1

## `k7`（130 行，class）

- 包：`io.carplay.app.secured`
- 被引用 5 次：`X25519PrivateKeyParameters`×4, `X25519`×1
- 字段（1）：a a
- 方法（8）：boolean a(byte[] bArr, int i, byte[] bArr2, int i2), void b(byte[] bArr), void c(byte[] bArr, int i, int[] iArr), void d(SecureRandom secureRandom, byte[] bArr), void e(byte[] bArr, int i, byte[] bArr2, int i2), void f(int[] iArr, int[] iArr2), void g(byte[] bArr, int i, byte[] bArr2, int i2), void h(byte[] bArr, int i, byte[] bArr2, int i2)

## `m3`（34 行，?）

- 包：`io.carplay.app.secured`
- 被引用 9 次：`MainActivity`×9
- 字段（2）：int a, String b
- 方法（1）：boolean test(Object obj)

## `t3`（36 行，?）

- 包：`io.carplay.app.secured`
- 被引用 9 次：`MainActivity`×9
- 字段（2）：int a, MainActivity b
- 方法（1）：void onDismiss(DialogInterface dialogInterface)

## `w6`（35 行，?）

- 包：`io.carplay.app.secured`
- 被引用 9 次：`PairingSession`×9
- 字段（3）：int a, PairingSession b, BluetoothSocket c
- 方法（1）：void run()

## `x3`（36 行，?）

- 包：`io.carplay.app.secured`
- 被引用 9 次：`MainActivity`×9
- 字段（2）：int a, MainActivity b
- 方法（1）：void onClick(View view)

## `n4`（42 行，class）

- 包：`io.carplay.app.secured`
- 被引用 7 次：`HelpActivity`×2, `c6`×2, `k4`×2, `MainActivity`×1
- 方法（4）：int a(int i, int i2, int[] iArr, int[] iArr2), int b(int i), int c(int i, int[] iArr), int d(int i, int[] iArr, int i2)

## `l6`（24 行，class）

- 包：`io.carplay.app.secured`
- 被引用 8 次：`Ed25519PrivateKeyParameters`×2, `Ed25519PublicKeyParameters`×2, `X25519PrivateKeyParameters`×2, `X25519PublicKeyParameters`×2
- 方法（2）：int a(InputStream inputStream, byte[] bArr), int b(InputStream inputStream, byte[] bArr, in)

## `a6`（57 行，class）

- 包：`io.carplay.app.secured`
- extends：`j3`
- 被引用 2 次：`X25519`×2
- 方法（5）：int a(byte[] bArr, int i), String c(), int d(), void p(), e2 r()

## `e6`（6 行，interface）

- 包：`io.carplay.app.secured`
- 被引用 9 次：`i2`×3, `AvcEncoder`×2, `PairingSession`×2, `AirPlaySession`×2

## `l3`（17 行，interface）

- 包：`io.carplay.app.secured`
- 被引用 9 次：`Poly1305`×6, `ChaCha20Poly1305`×3

## `u4`（78 行，class）

- 包：`io.carplay.app.secured`
- 被引用 4 次：`WirelessCarPlayService`×4
- 字段（3）：int a, int b, int c
- 方法（8）：boolean equals(Object obj), int g(), int h(), int hashCode(), int i(), String toString(), boolean a(int i, int i2, int i3, int i4), a b(int i, int i2, int i3)

## `b4`（16 行，?）

- 包：`io.carplay.app.secured`
- 被引用 8 次：`X25519`×5, `MainActivity`×3
- 字段（2）：MainActivity a, boolean b
- 方法（1）：void run()

## `t2`（40 行，class）

- 包：`io.carplay.app.secured`
- 被引用 6 次：`WirelessCarPlayService`×3, `AirPlaySession`×2, `MainActivity`×1
- 方法（1）：List<String> a(String str, Throwable th)
- 常量：`UNKNOWN`

## `d4`（9 行，class）

- 包：`io.carplay.app.secured`
- extends：`z5`
- 被引用 4 次：`PcmConverter`×3, `ChaCha20`×1

## `k4`（402 行，class）

- 包：`io.carplay.app.secured`
- 被引用 2 次：`l7`×2
- 方法（18）：int a(int i, int[] iArr, int[] iArr2), void b(int i, int i2, int[] iArr), void c(int i, int i2, int[] iArr, int[] iArr2), void d(int i, int[] iArr, int[] iArr2), int e(int i, int i2, int i3, int[] iArr), void f(int i, int[] iArr, int[] iArr2), int g(int i, int[] iArr, int i2), boolean h(int i, int[] iArr, int i2)

## `b7`（32 行，?）

- 包：`io.carplay.app.secured`
- 被引用 7 次：`WirelessCarPlayService`×7
- 字段（2）：int a, WirelessCarPlayService b
- 方法（1）：void run()

## `c4`（17 行，?）

- 包：`io.carplay.app.secured`
- 被引用 7 次：`z1`×4, `MainActivity`×3
- 字段（2）：MainActivity a, boolean b
- 方法（1）：void onDismiss(DialogInterface dialogInterface)

## `h3`（28 行，?）

- 包：`io.carplay.app.secured`
- 被引用 7 次：`LogActivity`×5, `MainActivity`×1, `WirelessCarPlayService`×1
- 字段（3）：int a, LogActivity b, String c
- 方法（1）：void run()

## `a5`（29 行，?）

- 包：`io.carplay.app.secured`
- 被引用 5 次：`PairingPreparationService`×5
- 字段（2）：int a, PairingPreparationService b
- 方法（1）：void run()

## `d7`（17 行，?）

- 包：`io.carplay.app.secured`
- 被引用 5 次：`WirelessCarPlayService`×3, `PcmConverter`×2
- 字段（3）：WirelessCarPlayService a, long b, AirPlaySession c
- 方法（1）：void run()

## `e7`（17 行，?）

- 包：`io.carplay.app.secured`
- 被引用 5 次：`WirelessCarPlayService`×3, `AirPlaySession`×2
- 字段（3）：WirelessCarPlayService a, long b, long c
- 方法（1）：void run()

## `g7`（32 行，?）

- 包：`io.carplay.app.secured`
- 被引用 5 次：`WirelessCarPlayService`×5
- 字段（6）：int a, WirelessCarPlayService b, Iap2Bootstrap.a c, Network d, Iap2Csm.a e, String f
- 方法（1）：void run()

## `s3`（30 行，?）

- 包：`io.carplay.app.secured`
- 被引用 5 次：`MainActivity`×5
- 字段（3）：int a, MainActivity b, Object c
- 方法（1）：void onClick(DialogInterface dialogInterface, int i)

## `s6`（32 行，?）

- 包：`io.carplay.app.secured`
- 被引用 5 次：`PairingSession`×5
- 字段（5）：int a, PairingSession b, BluetoothDevice c, AtomicInteger d, int e
- 方法（1）：void run()

## `y6`（28 行，?）

- 包：`io.carplay.app.secured`
- 被引用 5 次：`PairingSession`×5
- 字段（3）：int a, PairingSession b, String c
- 方法（1）：void run()

## `m7`（6 行，class）

- 包：`io.carplay.app.secured`
- extends：`a`
- 被引用 1 次：`i2`×1

## `m6`（39 行，class）

- 包：`io.carplay.app.secured`
- 被引用 3 次：`ChaCha20`×3
- 字段（1）：String a
- 方法（2）：String run(), byte[] a(String str)

## `x5`（46 行，class）

- 包：`io.carplay.app.secured`
- 被引用 3 次：`UpdateChecker`×3
- 字段（7）：g6 a, boolean b, long c, x5 a, long b, long c, boolean d
- 方法（2）：void a(boolean z, long j), a b(long j)

## `p5`（64 行，class）

- 包：`io.carplay.app.secured`
- 被引用 1 次：`j2`×1
- 字段（3）：ThreadLocal a, String a, String a
- 方法（5）：Object run(), Object run(), String a(String str), boolean b(String str), boolean c(String str)

## `a7`（16 行，?）

- 包：`io.carplay.app.secured`
- 被引用 3 次：`WirelessCarPlayService`×3
- 字段（2）：WirelessCarPlayService a, long b
- 方法（1）：void run()

## `h7`（18 行，?）

- 包：`io.carplay.app.secured`
- 被引用 3 次：`WirelessCarPlayService`×3
- 字段（4）：WirelessCarPlayService a, long b, long c, long d
- 方法（1）：void run()

## `p3`（21 行，?）

- 包：`io.carplay.app.secured`
- 被引用 3 次：`MainActivity`×3
- 字段（4）：MainActivity a, AlertDialog b, EditText c, String d
- 方法（1）：void onShow(DialogInterface dialogInterface)

## `q3`（20 行，?）

- 包：`io.carplay.app.secured`
- 被引用 3 次：`MainActivity`×3
- 字段（3）：MainActivity a, EditText b, AlertDialog c
- 方法（1）：void onDismiss(DialogInterface dialogInterface)

## `v3`（18 行，?）

- 包：`io.carplay.app.secured`
- 被引用 3 次：`MainActivity`×3
- 字段（2）：MainActivity a, AlertDialog b
- 方法（1）：void onShow(DialogInterface dialogInterface)

## `w3`（16 行，?）

- 包：`io.carplay.app.secured`
- 被引用 3 次：`MainActivity`×3
- 字段（1）：MainActivity a
- 方法（1）：void onCheckedChanged(CompoundButton compoundButton, boolean z)

## `x6`（20 行，?）

- 包：`io.carplay.app.secured`
- 被引用 3 次：`PairingSession`×3
- 字段（3）：PairingSession a, Network b, String c
- 方法（1）：boolean test(Object obj)

## `y3`（16 行，?）

- 包：`io.carplay.app.secured`
- 被引用 3 次：`MainActivity`×3
- 字段（2）：MainActivity a, int b
- 方法（1）：void run()

## `k3`（16 行，class）

- 包：`io.carplay.app.secured`
- 被引用 2 次：`ChaCha20Poly1305`×2
- 方法（2）：int a(long j, long j2), int b(long j, long j2)

## `c7`（13 行，?）

- 包：`io.carplay.app.secured`
- 被引用 2 次：`z1`×2
- 方法（1）：t a(int i, int i2)

## `f7`（13 行，?）

- 包：`io.carplay.app.secured`
- 被引用 2 次：`WirelessCarPlayService`×2
- 方法（1）：void run()

## `n6`（24 行，class）

- 包：`io.carplay.app.secured`
- 被引用 2 次：`HelpActivity`×1, `a6`×1
- 字段（4）：int a, int b, String c, f2 d
- 方法（1）：e2 a(q2 q2Var, int i, f2 f2Var)

## `v5`（15 行，?）

- 包：`io.carplay.app.secured`
- 被引用 2 次：`w5`×2
- 字段（1）：long a
- 方法（1）：boolean test(Object obj)

## `g6`（9 行，class）

- 包：`io.carplay.app.secured`
- 被引用 2 次：`x5`×2
- 字段（1）：AtomicLong a

## `h4`（13 行，?）

- 包：`io.carplay.app.secured`
- 被引用 1 次：`PcmEndpoint`×1
- 方法（1）：Object apply(int i)

## `k5`（11 行，?）

- 包：`io.carplay.app.secured`
- 被引用 1 次：`l5`×1
- 方法（1）：l5.a f(String str, String str2, String str3, St)

## `o6`（9 行，class）

- 包：`io.carplay.app.secured`
- 被引用 1 次：`ChaCha20`×1
- 方法（1）：f2 a(boolean z)

## `q4`（11 行，?）

- 包：`io.carplay.app.secured`
- 被引用 1 次：`DisplaySession`×1
- 方法（1）：void a(String str)

## `s4`（13 行，?）

- 包：`io.carplay.app.secured`
- 被引用 1 次：`DisplaySession`×1
- 方法（1）：void h(byte[] bArr, long j)

