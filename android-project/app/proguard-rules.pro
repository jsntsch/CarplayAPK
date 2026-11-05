# R8 代码压缩配置
# 项目从 smali 反编译恢复，含反射调用，保留所有类名和成员以保证运行时正确。
# R8 仍会做：字节码优化、类合并、移除未使用字段、裁剪未引用类。

# 保留项目所有类（反射/序列化/HID 描述符解析可能引用任意成员）
-keep class io.carplay.app.** { *; }

# BouncyCastle 子集：JCA 提供者通过类名反射实例化，必须全保留
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Android 组件（manifest 注册，系统实例化）
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.app.Activity
-keep public class * extends android.content.BroadcastReceiver

# 枚举：values()/valueOf() 被反射调用
-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 注解
-keepattributes *Annotation*

# 移除日志（release 包）
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}
