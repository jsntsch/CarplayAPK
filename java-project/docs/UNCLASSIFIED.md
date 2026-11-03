# 仍保留混淆原名的类（174 个）

> 原则：**没有证据就不改名**。本文件说明剩下的类为什么保持原名，
> 以及要继续推进时该怎么取证。

| 分类 | 数量 | 说明 |
|---|---|---|
| R8 合成桥接类 | 111 | 为 lambda / 方法引用生成，方法体是单行委托，没有独立语义 |
| BouncyCastle 兼容层 | 5 | 类体只跟 BC 类型打交道，本身已是算法实现 |
| 待取证 | 48 | 有真实逻辑，但字节码里没有可判定的协议串/强类型线索 |
| 已是语义名 | 10 | 如 `R` / `HelpActivity`，无需改 |

## 1. R8 合成桥接类（111）

这些类形如：

```java
public final /* synthetic */ class a implements Runnable {
    public /* synthetic */ AvcEncoder b;
    public final void run() { this.b.C(surfaceTexture); }
}
```

字母只是 R8 的去重编号。给它们编语义名等于给编译器生成的胶水代码
编故事，所以**有意保留原名**——真正有意义的语义在它们委托去的方法上。

```
a        io/carplay/app/prototype/a.java                27 行  Runnable
a0       io/carplay/app/prototype/a0.java               27 行  Runnable
b        io/carplay/app/prototype/b.java                15 行  SurfaceTexture.OnFrameAvailableListener
b0       io/carplay/app/prototype/b0.java               29 行  Runnable
c        io/carplay/app/prototype/c.java                16 行  Supplier
d        io/carplay/app/prototype/d.java                26 行  Runnable
e        io/carplay/app/prototype/e.java                13 行  Predicate
e0       io/carplay/app/prototype/e0.java               24 行  Runnable
f0       io/carplay/app/prototype/f0.java               30 行  View.OnClickListener
g0       io/carplay/app/prototype/g0.java               25 行  View.OnClickListener
h0       io/carplay/app/prototype/h0.java               30 行  DialogInterface.OnShowListener
i        io/carplay/app/prototype/i.java                18 行  CarPlayCtrlAdvertiser.c
i0       io/carplay/app/prototype/i0.java               37 行  Runnable
k        io/carplay/app/prototype/k.java                29 行  Predicate
l        io/carplay/app/prototype/l.java                27 行  Runnable
l0       io/carplay/app/prototype/l0.java               18 行  View.OnApplyWindowInsetsListener
n        io/carplay/app/prototype/n.java                16 行  Runnable
n0       io/carplay/app/prototype/n0.java               13 行  CompoundButton.OnCheckedChangeListener
o        io/carplay/app/prototype/o.java                16 行  Predicate
o0       io/carplay/app/prototype/o0.java               29 行  View.OnClickListener
p        io/carplay/app/prototype/p.java                39 行  Runnable
p0       io/carplay/app/prototype/p0.java               17 行  View.OnApplyWindowInsetsListener
q0       io/carplay/app/prototype/q0.java               19 行  Runnable
s        io/carplay/app/prototype/s.java                36 行  UpdateChecker.a
s0       io/carplay/app/prototype/s0.java               21 行  Runnable
t        io/carplay/app/prototype/t.java                15 行  UpdateChecker.a
t0       io/carplay/app/prototype/t0.java               32 行  Runnable
x        io/carplay/app/prototype/x.java                17 行  ThreadFactory
y        io/carplay/app/prototype/y.java                19 行  Runnable
a1       io/carplay/app/secured/a1.java                 28 行  Predicate
a4       io/carplay/app/secured/a4.java                 36 行  Predicate
a5       io/carplay/app/secured/a5.java                 29 行  Runnable
a7       io/carplay/app/secured/a7.java                 16 行  Runnable
b0       io/carplay/app/secured/b0.java                 42 行  Runnable
b4       io/carplay/app/secured/b4.java                 16 行  Runnable
b5       io/carplay/app/secured/b5.java                 32 行  Consumer
b7       io/carplay/app/secured/b7.java                 32 行  Runnable
c0       io/carplay/app/secured/c0.java                 26 行  LongSupplier
c4       io/carplay/app/secured/c4.java                 17 行  DialogInterface.OnDismissListener
c7       io/carplay/app/secured/c7.java                 13 行  t.a
d1       io/carplay/app/secured/d1.java                 44 行  Runnable
d7       io/carplay/app/secured/d7.java                 17 行  Runnable
e0       io/carplay/app/secured/e0.java                 16 行  BooleanSupplier
e7       io/carplay/app/secured/e7.java                 17 行  Runnable
f0       io/carplay/app/secured/f0.java                 15 行  IntConsumer
f1       io/carplay/app/secured/f1.java                 33 行  Supplier
f7       io/carplay/app/secured/f7.java                 13 行  Runnable
g0       io/carplay/app/secured/g0.java                 18 行  BiConsumer
g1       io/carplay/app/secured/g1.java                 24 行  BooleanSupplier
g7       io/carplay/app/secured/g7.java                 32 行  Runnable
h0       io/carplay/app/secured/h0.java                 31 行  Runnable
h3       io/carplay/app/secured/h3.java                 28 行  Runnable
h4       io/carplay/app/secured/h4.java                 13 行  IntFunction
h7       io/carplay/app/secured/h7.java                 18 行  Runnable
i3       io/carplay/app/secured/i3.java                 33 行  View.OnClickListener
i7       io/carplay/app/secured/i7.java                 15 行  DisplaySession.c
j0       io/carplay/app/secured/j0.java                 64 行  Iap2WirelessSetup.c
k        io/carplay/app/secured/k.java                  28 行  IntPredicate
k0       io/carplay/app/secured/k0.java                 24 行  Consumer
k1       io/carplay/app/secured/k1.java                 43 行  VideoCipher.AvcVideoInput.c
k5       io/carplay/app/secured/k5.java                 11 行  l5
l0       io/carplay/app/secured/l0.java                 34 行  Supplier
m0       io/carplay/app/secured/m0.java                 16 行  BooleanSupplier
m3       io/carplay/app/secured/m3.java                 34 行  Predicate
n0       io/carplay/app/secured/n0.java                 39 行  ThreadFactory
n3       io/carplay/app/secured/n3.java                 33 行  View.OnClickListener
o0       io/carplay/app/secured/o0.java                 15 行  KeepAliveServer.a
o1       io/carplay/app/secured/o1.java                 15 行  Function
o3       io/carplay/app/secured/o3.java                 95 行  Runnable
p        io/carplay/app/secured/p.java                  32 行  ToLongFunction
p0       io/carplay/app/secured/p0.java                 54 行  Runnable
p3       io/carplay/app/secured/p3.java                 21 行  DialogInterface.OnShowListener
q        io/carplay/app/secured/q.java                  26 行  ToIntFunction
q0       io/carplay/app/secured/q0.java                 32 行  Runnable
q3       io/carplay/app/secured/q3.java                 20 行  DialogInterface.OnDismissListener
q4       io/carplay/app/secured/q4.java                 11 行  DisplaySession.c
r        io/carplay/app/secured/r.java                  38 行  Function
r0       io/carplay/app/secured/r0.java                 18 行  EventChannelConnector.a
r1       io/carplay/app/secured/r1.java                 16 行  Thread.UncaughtExceptionHandler
r3       io/carplay/app/secured/r3.java                 20 行  DialogInterface.OnClickListener
r4       io/carplay/app/secured/r4.java                 22 行  Consumer
s1       io/carplay/app/secured/s1.java                 30 行  BiConsumer
s3       io/carplay/app/secured/s3.java                 30 行  DialogInterface.OnClickListener
s4       io/carplay/app/secured/s4.java                 13 行  DisplaySession.h
s6       io/carplay/app/secured/s6.java                 32 行  Runnable
t0       io/carplay/app/secured/t0.java                 16 行  -
t3       io/carplay/app/secured/t3.java                 36 行  DialogInterface.OnDismissListener
t6       io/carplay/app/secured/t6.java                 40 行  Consumer
u        io/carplay/app/secured/u.java                  29 行  Predicate
u0       io/carplay/app/secured/u0.java                 16 行  Executor
u2       io/carplay/app/secured/u2.java                 33 行  View.OnClickListener
u3       io/carplay/app/secured/u3.java                 42 行  DialogInterface.OnClickListener
u6       io/carplay/app/secured/u6.java                 62 行  Runnable
v        io/carplay/app/secured/v.java                  13 行  Comparator
v0       io/carplay/app/secured/v0.java                 29 行  Runnable
v2       io/carplay/app/secured/v2.java                 34 行  View.OnClickListener
v3       io/carplay/app/secured/v3.java                 18 行  DialogInterface.OnShowListener
v5       io/carplay/app/secured/v5.java                 15 行  Predicate
v6       io/carplay/app/secured/v6.java                 37 行  Runnable
w        io/carplay/app/secured/w.java                  27 行  BiFunction
w0       io/carplay/app/secured/w0.java                 64 行  Predicate
w3       io/carplay/app/secured/w3.java                 16 行  CompoundButton.OnCheckedChangeListener
w6       io/carplay/app/secured/w6.java                 35 行  Runnable
x3       io/carplay/app/secured/x3.java                 36 行  View.OnClickListener
x6       io/carplay/app/secured/x6.java                 20 行  Predicate
y        io/carplay/app/secured/y.java                  29 行  Predicate
y3       io/carplay/app/secured/y3.java                 16 行  Runnable
y6       io/carplay/app/secured/y6.java                 28 行  Runnable
z2       io/carplay/app/secured/z2.java                 42 行  j5.a
z3       io/carplay/app/secured/z3.java                 16 行  RadioGroup.OnCheckedChangeListener
z6       io/carplay/app/secured/z6.java                 21 行  Runnable
```

## 2. BouncyCastle 兼容层（5）

```
c5                               io/carplay/app/secured/c5.java                 29 行
l2                               io/carplay/app/secured/l2.java                 48 行
l3                               io/carplay/app/secured/l3.java                 17 行
t2                               io/carplay/app/secured/t2.java                 40 行
a                                org/bouncycastle/crypto/signers/a.java         18 行
```

## 3. 待取证（48）

这些类有真实逻辑，只是证据还不够。继续命名的取证顺序：

1. `python tools/peek.py secured/<名>` —— 看父类/接口/字段/方法/字符串常量；
2. `grep -rn "\b<名>\b" src/main/java` —— 看它被谁 `new`、参数是什么；
3. 写进 `tools/plan_batchN.tsv`（`旧全限定名<TAB>新名`），跑
   `python tools/java_rename.py --plan tools/plan_batchN.tsv`；
4. `python tools/compile_check.py --src src/main/java` 必须仍是 0 error。

```
l7     io/carplay/app/secured/l7.java                611 行  public abstract class l7 {
k4     io/carplay/app/secured/k4.java                402 行  public abstract class k4 {
m4     io/carplay/app/secured/m4.java                370 行  public abstract class m4 {
d6     io/carplay/app/secured/d6.java                323 行  abstract class d6 {
c6     io/carplay/app/secured/c6.java                256 行  abstract class c6 {
j3     io/carplay/app/secured/j3.java                243 行  public abstract class j3 implements q2 {
q6     io/carplay/app/secured/q6.java                218 行  public final class q6 extends ProtoMessageBase {
t1     io/carplay/app/secured/t1.java                196 行  public final class t1 {
j5     io/carplay/app/secured/j5.java                181 行  final class j5 {
z      io/carplay/app/secured/z.java                 167 行  public final class z {
t5     io/carplay/app/secured/t5.java                161 行  public final class t5 {
z1     io/carplay/app/secured/z1.java                157 行  public abstract class z1 extends ChaCha20 {
s5     io/carplay/app/secured/s5.java                142 行  public final class s5 extends ProtoMessageBase {
k7     io/carplay/app/secured/k7.java                130 行  public abstract class k7 {
j6     io/carplay/app/secured/j6.java                118 行  public interface j6 {
w4     io/carplay/app/secured/w4.java                104 行  public final class w4 {
f4     io/carplay/app/secured/f4.java                 99 行  public final class f4 {
u4     io/carplay/app/secured/u4.java                 78 行  public final class u4 {
p5     io/carplay/app/secured/p5.java                 64 行  public abstract class p5 {
a6     io/carplay/app/secured/a6.java                 57 行  public class a6 extends j3 {
y1     io/carplay/app/secured/y1.java                 49 行  public class y1 extends ChaCha20 {
x5     io/carplay/app/secured/x5.java                 46 行  public final class x5 {
j7     io/carplay/app/secured/j7.java                 44 行  abstract class j7 {
m2     io/carplay/app/secured/m2.java                 42 行  public class m2 {
n4     io/carplay/app/secured/n4.java                 42 行  public abstract class n4 {
h2     io/carplay/app/secured/h2.java                 40 行  public class h2 extends Permission {
m6     io/carplay/app/secured/m6.java                 39 行  public abstract class m6 {
k2     io/carplay/app/secured/k2.java                 28 行  public class k2 {
l6     io/carplay/app/secured/l6.java                 24 行  public abstract class l6 {
n6     io/carplay/app/secured/n6.java                 24 行  abstract class n6 {
l5     io/carplay/app/secured/l5.java                 23 行  public interface l5 {
n      io/carplay/app/secured/n.java                  21 行  public final class n {
p2     io/carplay/app/secured/p2.java                 21 行  public class p2 implements e2 {
t      io/carplay/app/secured/t.java                  21 行  public interface t extends AutoCloseable {
f2     io/carplay/app/secured/f2.java                 16 行  public enum f2 {
k3     io/carplay/app/secured/k3.java                 16 行  public abstract class k3 {
w2     io/carplay/app/secured/w2.java                 15 行  public abstract class w2 {
q2     io/carplay/app/secured/q2.java                 13 行  public interface q2 {
m      io/carplay/app/secured/m.java                  10 行  public abstract class m {
e3     io/carplay/app/secured/e3.java                  9 行  public abstract class e3 {
g6     io/carplay/app/secured/g6.java                  9 行  public final class g6 {
o      io/carplay/app/secured/o.java                   9 行  public interface o {
o6     io/carplay/app/secured/o6.java                  9 行  abstract class o6 {
g2     io/carplay/app/secured/g2.java                  7 行  public interface g2 {
a      io/carplay/app/secured/a.java                   6 行  public abstract class a {
e2     io/carplay/app/secured/e2.java                  6 行  public interface e2 {
e6     io/carplay/app/secured/e6.java                  6 行  public interface e6 {
m7     io/carplay/app/secured/m7.java                  6 行  public abstract class m7 extends a {
```

## 4. 已是语义名（10）

```
R                        io/carplay/app/R.java                          11 行
HelpActivity             io/carplay/app/prototype/HelpActivity.java    114 行
CipherParameters         org/bouncycastle/crypto/CipherParameters.java    6 行
AEADParameters           org/bouncycastle/crypto/params/AEADParameters.java   40 行
Ed25519PrivateKeyParameters org/bouncycastle/crypto/params/Ed25519PrivateKeyParameters.java  122 行
Ed25519PublicKeyParameters org/bouncycastle/crypto/params/Ed25519PublicKeyParameters.java  107 行
KeyParameter             org/bouncycastle/crypto/params/KeyParameter.java   47 行
X25519PrivateKeyParameters org/bouncycastle/crypto/params/X25519PrivateKeyParameters.java   77 行
X25519PublicKeyParameters org/bouncycastle/crypto/params/X25519PublicKeyParameters.java   52 行
Ed25519Signer            org/bouncycastle/crypto/signers/Ed25519Signer.java   93 行
```
