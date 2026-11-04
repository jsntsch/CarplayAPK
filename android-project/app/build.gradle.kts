plugins {
    id("com.android.application")
}

/** 版本号：产物名为 Carplay-V<versionName>-debug.apk / Carplay-V<versionName>-release.apk */
val appVersionName = "1.7.61"
val appVersionCode = 298

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
            isMinifyEnabled = false
        }
        release {
            // 发布变体：关闭调试，避免 run-as / 调试器附加；现场排障请使用 -debug 变体。
            // Release variant: debuggable disabled; use the -debug variant for on-device troubleshooting.
            isDebuggable = false
            isMinifyEnabled = false
            // 内部工具：沿用 debug 证书签发，保证 assembleRelease 产物可直接 adb install -r 覆盖安装。
            // 对外分发时请替换为自有 keystore。
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

/** 把 debug 产物拷到 dist/：Carplay-V1.7.61-debug.apk */
tasks.register<Copy>("exportApk") {
    dependsOn("assembleDebug")
    from(layout.buildDirectory.dir("outputs/apk/debug"))
    include("*.apk")
    into(rootProject.layout.projectDirectory.dir("dist"))
    rename { "Carplay-V$appVersionName-debug.apk" }
}

/** release 变体导出：dist/Carplay-V1.7.61-release.apk */
tasks.register<Copy>("exportRelease") {
    dependsOn("assembleRelease")
    from(layout.buildDirectory.dir("outputs/apk/release"))
    include("*.apk")
    into(rootProject.layout.projectDirectory.dir("dist"))
    rename { "Carplay-V$appVersionName-release.apk" }
}
