# 本工程不启用代码混淆（源码来自反编译重建，混淆会破坏反射与协议常量）。
# 若后续要开启 R8，请保留以下规则。
-keep class io.carplay.app.** { *; }
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
