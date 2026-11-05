# 本工程不启用代码混淆。
# 若后续要开启 R8，请保留以下规则。
-keep class io.carplay.app.** { *; }
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
