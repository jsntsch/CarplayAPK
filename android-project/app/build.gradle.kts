plugins {
    id("com.android.application")
}

/** 版本号：产物名为 Carplay-V<versionName>-debug.apk / Carplay-V<versionName>-release.apk */
val appVersionName = "1.8.24"
val appVersionCode = 361

android {
    namespace = "io.carplay.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.carplay.app"
        minSdk = 33
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // 源码含中英对照注释（UTF-8），显式指定编码，避免不同机器默认字符集导致乱码或编译失败。
    // Sources carry bilingual UTF-8 comments; pin the encoding so other machines cannot break the build.
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    buildTypes {
        debug {
            isDebuggable = true
            // 不使用 R8：源码来自反编译重建，反射与协议常量依赖原始类名，混淆/裁剪会破坏运行。
            // No R8: reflection and protocol constants rely on the original class names.
            isMinifyEnabled = false
        }
        release {
            // 发布变体：关闭调试，避免 run-as / 调试器附加；现场排障请使用 -debug 变体。
            // Release variant: debuggable disabled; use the -debug variant for on-device troubleshooting.
            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            // 内部工具：沿用 debug 证书签发，保证 assembleRelease 产物可直接 adb install -r 覆盖安装。
            // 对外分发时请替换为自有 keystore。
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/*.kotlin_module"
            )
        }
    }
}

dependencies {
    // 无第三方依赖：BouncyCastle 子集随源码一起编译（org.bouncycastle.*）
}

/** 把 debug 产物拷到 dist/：Carplay-V1.7.75-debug.apk */
tasks.register<Copy>("exportApk") {
    dependsOn("assembleDebug")
    from(layout.buildDirectory.dir("outputs/apk/debug"))
    include("*.apk")
    into(rootProject.layout.projectDirectory.dir("dist"))
    // 配置缓存兼容：用两参数 rename（配置期求值的普通字符串），
    // 不要用 rename { "Carplay-V$appVersionName..." } 闭包——闭包会捕获脚本对象，
    // 配置缓存复用时报 "getAppVersionName() ... this.this$0 is null"。
    // Configuration-cache safe: the two-arg rename takes an eager String; a lambda would
    // capture the script object and fail when the configuration-cache entry is reused.
    rename(".*\\.apk", "Carplay-V$appVersionName-debug.apk")
}

/** release 变体导出：dist/Carplay-V1.7.75-release.apk */
tasks.register<Copy>("exportRelease") {
    dependsOn("assembleRelease")
    from(layout.buildDirectory.dir("outputs/apk/release"))
    include("*.apk")
    into(rootProject.layout.projectDirectory.dir("dist"))
    rename(".*\\.apk", "Carplay-V$appVersionName-release.apk")
}

/** 发布用 AAB（显式调用）：dist/Carplay-V<版本>-release.aab，不能 adb 安装 */
tasks.register<Copy>("exportAab") {
    dependsOn("bundleRelease")
    from(layout.buildDirectory.dir("outputs/bundle/release"))
    include("*.aab")
    into(rootProject.layout.projectDirectory.dir("dist"))
    rename(".*\\.aab", "Carplay-V$appVersionName-release.aab")
}
