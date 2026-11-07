# CarPlayAPK（io.carplay.app）— Android Studio 工程

**标准 Gradle / Android Studio 工程**，可以直接打开、编译、调试、出包。功能与已验证的 `Carplay-V1.7.71` 一致（含触摸与音频修复）。

## 环境要求

| 项                     | 版本                                              |
| ---------------------- | ------------------------------------------------- |
| JDK                    | 17+（本机用 Android Studio 自带 JBR 25 验证通过） |
| Gradle                 | 9.4.1（wrapper 自带，无需预装）                   |
| Android Gradle Plugin  | 9.2.0                                             |
| compileSdk / targetSdk | 37                                                |
| minSdk                 | 33                                                |
| Build Tools            | 36.0.0                                            |

SDK 路径写在 `local.properties`（已被 .gitignore 忽略，换机器需自行生成）。

## 构建

```bash
# Windows
gradlew.bat                  # 默认：只编 debug 并导出（= exportApk）
gradlew.bat exportApk        # 产物：dist/Carplay-V1.7.71-debug.apk   （debuggable = true）
gradlew.bat exportRelease    # 显式调用：dist/Carplay-V1.7.71-release.apk（debuggable = false）
gradlew.bat exportAab        # 显式调用：dist/Carplay-V1.7.71-release.aab（发布用，不能 adb 安装）

# macOS / Linux
./gradlew
```

> 不带参数时只构建 debug（`build.gradle.kts` 的 `defaultTasks(":app:exportApk")`），
> 不会顺带跑 release / bundle / R8。
> With no task argument only debug is built; release and AAB are opt-in.

也可以直接用 Android Studio 打开本目录（`android-project`），Build → Make Project。

## 目录结构

```
android-project/
├── settings.gradle.kts          # 仓库与模块声明
├── build.gradle.kts             # AGP 版本
├── gradle.properties            # 构建参数
├── gradle/wrapper/              # Gradle 9.4.1 wrapper
├── local.properties             # 本机 SDK 路径（不入库）
├── dist/                        # 导出的 APK / AAB
└── app/
    ├── build.gradle.kts         # 版本号、SDK、签名、导出任务
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml  # 权限 / 组件 / queries（与基线逐项一致）
        ├── java/                # 273 个源文件（io.carplay.app.* + org.bouncycastle.*）
        └── res/                 # ic_launcher.xml（矢量，透明背景）
```

## 版本号

只改 `app/build.gradle.kts` 顶部两个常量：

```kotlin
val appVersionName = "1.7.71"
val appVersionCode = 308
```

导出的文件名会自动跟随 versionName：`Carplay-V1.7.NN-debug.apk` / `Carplay-V1.7.NN-release.apk` /
`Carplay-V1.7.NN-release.aab`。

|           | `android-project/`（本工程）                                |
| --------- | ------------------------------------------------------------- |
| 用途      | 日常开发、调试、正式出包                                      |
| 资源/清单 | 完整（`src/main/res` + `AndroidManifest.xml`）            |
| 构建      | `gradlew exportApk` / `exportRelease` / `exportAab`           |
| 源码      | `app/src/main/java`（**已删除 R.java**，由 AGP 生成） |

## 构建配置说明

- **debug / release 的调试开关分离**：`debug` 变体 `debuggable = true`，
  `release` 变体 `debuggable = false`。现场需要 `adb shell run-as io.carplay.app cat files/connection-log-*.txt`
  读日志时请装 `-debug` 包；`-release` 包无法 `run-as`。
- **签名**：debug 与 release 均使用本机 `~/.android/debug.keystore`，可与历史包直接覆盖安装
  （`adb install -r`）。对外分发请替换为自有 keystore。
- **默认只编 debug**：根 `build.gradle.kts` 的 `defaultTasks(":app:exportApk")`；release / AAB
  只在显式调用 `exportRelease` / `exportAab` 时构建。
- **不使用 R8**：两个变体都是 `isMinifyEnabled = false`（release 也不开 `shrinkResources`）。
  反射与协议常量依赖原始类名，混淆/裁剪会破坏连接。
- **关闭内置 Kotlin**：`gradle.properties` 的 `android.builtInKotlin=false`。工程没有 Kotlin 源码，
  不关会把 `kotlin-stdlib` 打进 dex（实测 +2.06 MB、1080 个类、占 dex 70%）。
- **AAB 用同一把调试证书签**：可传内部测试轨道；正式上架需换 Play 上传密钥，
  且 `.aab` 不能 `adb install`（装机用 APK）。
- **Gradle 分发走腾讯云镜像**：`gradle/wrapper/gradle-wrapper.properties` 里已配置，
  官方源在国内常超时。

## 现场排障

```bash
adb install -r dist/Carplay-V1.7.71-debug.apk      # 排障用 debug 包，可 run-as 读日志
adb shell am start -n io.carplay.app/.prototype.MainActivity
adb shell run-as io.carplay.app cat files/connection-log-0.txt | tail -50
```

关键日志：`APP_BUILD` / `HID_CAPABILITIES` / `VIDEO_PROGRESS` / `AUDIO_FLOW` / `TOUCH_FORWARDED`。
音频正常时 `AUDIO_FLOW` 的 `sent_packets_total` 约为 **136 包/秒**，`dropped_frames_total` 应停止增长。
