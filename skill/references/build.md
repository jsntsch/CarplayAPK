# 构建细节 / Build details

`android-project/` 是本仓库唯一可编译工程。本文只讲「怎么稳定编出正确的包」。
`android-project/` is the only buildable project here. This note is about producing the right APK
reliably.

## 1. 工程结构 / Project layout

```
android-project/
├── settings.gradle.kts        仓库名与模块声明 / repository and module declarations
├── build.gradle.kts           AGP 版本声明 / AGP version declaration
├── gradle.properties          JVM 与 AndroidX 开关 / JVM and AndroidX flags
├── gradle/wrapper/            Gradle 9.4.1（腾讯云镜像）/ Gradle 9.4.1 via Tencent mirror
├── local.properties           sdk.dir（不入库）/ SDK path, not committed
├── app/
│   ├── build.gradle.kts       版本号、SDK、签名、导出任务 / version, SDK, signing, export tasks
│   ├── proguard-rules.pro     ProGuard 规则（当前未启用）/ ProGuard rules, currently unused
│   └── src/main/
│       ├── AndroidManifest.xml   权限 / 4 个组件 / queries
│       ├── java/                 273 个源文件（含中英对照注释）
│       └── res/                  ic_launcher.xml（矢量图，透明背景）
└── dist/                      导出产物 / exported artifacts
```

## 2. 四个构建任务 / Four build tasks

| 任务 / Task | 作用 / Purpose | 产物 / Output |
| --- | --- | --- |
| `compileDebugJavaWithJavac` | 只编译，最快发现语法/编码问题 | 无 APK / no APK |
| `exportApk`（默认 / default） | `assembleDebug` + 按版本号改名导出 | `dist/Carplay-V<ver>-debug.apk` |
| `exportRelease` | 显式调用，`assembleRelease` + 导出 | `dist/Carplay-V<ver>-release.apk` |
| `exportAab` | 显式调用，`bundleRelease` + 导出 | `dist/Carplay-V<ver>-release.aab` |

不带参数执行 `gradlew` 只会跑 `exportApk`（根 `build.gradle.kts` 设了
`defaultTasks(":app:exportApk")`），不会顺带编 release / AAB。
With no task argument only the debug APK is built; release and AAB are opt-in.

导出任务的定义（`app/build.gradle.kts`）：

```kotlin
tasks.register<Copy>("exportApk") {
    dependsOn("assembleDebug")
    from(layout.buildDirectory.dir("outputs/apk/debug"))
    include("*.apk")
    into(rootProject.layout.projectDirectory.dir("dist"))
    // 必须用两参数 rename（配置期求值的普通字符串）
    rename(".*\\.apk", "Carplay-V$appVersionName-debug.apk")
}

tasks.register<Copy>("exportAab") {
    dependsOn("bundleRelease")
    from(layout.buildDirectory.dir("outputs/bundle/release"))
    include("*.aab")
    into(rootProject.layout.projectDirectory.dir("dist"))
    rename(".*\\.aab", "Carplay-V$appVersionName-release.aab")
}
```

> **配置缓存坑（V1.7.75 修复）**：不要写 `rename { "Carplay-V$appVersionName-debug.apk" }`。
> 这个 lambda 会捕获 Kotlin DSL 脚本对象；配置缓存条目复用时反序列化失败，报
> `Cannot invoke "Build_gradle.getAppVersionName()" because "this.this$0" is null`，
> 表现为 `assembleDebug` 成功但 `exportApk` 失败、`dist/` 里没有 APK。
> 用 `rename(regex, targetName)` 这种**配置期求值**的字符串重载即可。
>
> **Configuration-cache trap (fixed in V1.7.75)**: a `rename { ... }` lambda captures the script
> object and cannot be deserialized when the configuration-cache entry is reused, so
> `assembleDebug` passes but `exportApk` fails with `this.this$0 is null`. Use the two-arg
> `rename(regex, name)` overload with an eagerly evaluated string.

## 2.1 快速构建 / Fast builds

默认命令每次都会做依赖元数据检查，国内网络下 `google()/mavenCentral()` 可能长时间卡住；
再叠加 Gradle 守护进程被系统回收（`gradlew --status` 全是 STOPPED），单次构建可被拖到 9 分钟以上。
实测把下面两个开关加上后，`clean` 全量构建 **52 秒**、增量出包 **22 秒**：

```bash
# 推荐：离线 + 单次进程（不依赖守护进程存活）
gradlew.bat exportApk --offline --console=plain --no-daemon

# 只改了几行 Java、想最快确认语法
gradlew.bat compileDebugJavaWithJavac --offline --console=plain --no-daemon
```

| 开关 / Flag | 作用 / Effect |
| --- | --- |
| `--offline` | 跳过所有仓库网络检查，只用本地依赖缓存（AGP 已缓存时必开） |
| `--no-daemon` | 守护进程常被环境回收，冷启动反而不如单进程稳定；也避免残留锁 |
| `--console=plain` | 输出可重定向到日志，便于后台跑与排错 |

* **不要**手工 `rm -rf app/build`：被占用时会 `Permission denied`。用 `gradlew clean`
  （内置重试）或 `gradlew clean exportApk`。
* 构建日志建议重定向到文件（如 `logs/build-<ver>.log`），后台跑、完成后再看，
  避免长时间无输出被误判为卡死。
* `clean` 只删 `app/build`；`.gradle`（配置缓存）与 `~/.gradle`（依赖缓存）应保留，
  删掉会让下一次构建退化为冷启动 + 重新配置。

版本号是 `val` 常量，`rename {}` 在配置期就取到值，所以**改完版本号必须重新执行任务**（不是改名文件）。
The version constant is read at configuration time, so change it and re-run the task — do not just
rename the output file.

## 3. 编码 / Encoding

源码含中文注释，必须 UTF-8 编译：

```kotlin
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
```

没有这一行时，Windows 上 javac 可能按 GBK 读源码，出现
`unmappable character for encoding` 或注释乱码（乱码本身不影响字节码，但会让排查变难）。
Without it, javac on Windows may read sources as GBK: you get `unmappable character` errors or
mojibake in comments.

## 4. 依赖与网络 / Dependencies and network

* **无第三方依赖**：Bouncy Castle 子集（`org.bouncycastle.*`）随源码一起编译，不需要额外 AAR。
  No third-party dependencies; the Bouncy Castle subset compiles from source.
* **`android.builtInKotlin=false`**：AGP 9 内置 Kotlin 会往 runtime classpath 塞 `kotlin-stdlib`，
  即使工程一行 Kotlin 都没有。纯 Java 工程关掉后 dex 从 2.94 MB 降到 0.88 MB；删掉这一行包会反弹。
  AGP 9's built-in Kotlin injects `kotlin-stdlib` even for pure-Java projects; disabling it shrinks
  the dex from 2.94 MB to 0.88 MB.
* **不使用 R8**：两个变体 `isMinifyEnabled = false`。反射与协议常量依赖原始类名，混淆/裁剪会破坏连接。
  No R8 in either variant; obfuscation breaks reflection and protocol constants.
* Gradle 分发包走腾讯云镜像：`gradle/wrapper/gradle-wrapper.properties` 的 `distributionUrl`。
  内网环境把它换成可达地址即可。
  The wrapper pulls from the Tencent mirror; repoint `distributionUrl` if blocked.
* AGP 与构建工具从 Google Maven 拉取，首次构建需要能访问（之后走本地缓存）。
  AGP artifacts come from Google Maven on first build, then from the local cache.

## 5. 产物核对 / Verify the artifact

```bash
AAPT2="$ANDROID_HOME/build-tools/36.0.0/aapt2.exe"
APKSIGNER="$ANDROID_HOME/build-tools/36.0.0/apksigner.bat"
DEXDUMP="$ANDROID_HOME/build-tools/36.0.0/dexdump.exe"

"$AAPT2" dump badging dist/Carplay-V1.7.71-debug.apk | head -3
"$APKSIGNER" verify --print-certs dist/Carplay-V1.7.71-debug.apk
"$JAVA_HOME/bin/jarsigner" -verify dist/Carplay-V1.7.71-release.aab   # AAB 用 jarsigner 验签

# 确认本次改动真的进了 dex（用改动引入的新方法名/新常量做锚点）
unzip -o -q dist/Carplay-V1.7.71-debug.apk classes.dex -d /tmp/v && \
  "$DEXDUMP" -d /tmp/v/classes.dex | grep -c "add-int/lit8"
```

三点经验 / three lessons:

1. **源码改了不等于包里改了**。构建缓存、导出任务漏执行都会让 `dist/` 里躺着旧包。
   比对 dex 里的锚点是最快的自证方式。
   Source changed ≠ APK changed. Anchor on a symbol the change introduced.
2. **zip 条目的时间戳不可信**：外壳继承的条目会显示旧日期，dex 是不是新生成的要看重编译时间与内容。
   Entry timestamps are inherited from the shell APK; do not use them to judge freshness.
3. **签名与版本一起核**：`apksigner verify` 通过只说明能装，`aapt2` 才知道装的是哪版。
   Signature proves installability, `aapt2` proves identity.

## 6. 常见构建错误 / Common build failures

| 现象 / Symptom | 根因 / Root cause | 处理 / Fix |
| --- | --- | --- |
| `R$drawable is defined multiple times` | 手写 `R.java` 与 AGP 生成的 R 冲突 | 删除 `src/main/java/io/carplay/app/R.java` |
| `Could not download …gradle-9.4.1-bin.zip` | 镜像不可达 | 改 `distributionUrl` |
| `Unsupported class file major version` | JDK 版本不匹配 | `export JAVA_HOME=<JBR>` |
| `SDK location not found` | 缺 `local.properties` | 写 `sdk.dir=` |
| `unmappable character` | 编译编码不是 UTF-8 | 确认 `options.encoding` |
| `Duplicate class org.bouncycastle…` | 同时引入了外部 BC 依赖 | 本工程不需要任何 BC AAR，移除该依赖 |

## 7. 与旧构建方式的关系 / Relation to the legacy pipeline

早期用「`javac` 编译 → `d8` 生成 dex → 替换进外壳 APK → 对齐 → 签名」的手工流水线，
其源码树（`java-project/`）已不再维护。现在**只有 Gradle 一条路**：
`app/src/main/java` 是唯一源码目录，`dist/` 是唯一产物目录。
The earlier hand-rolled pipeline (javac → d8 → replace dex in a shell APK → align → sign) and its
source tree are retired. Gradle is now the only path: `app/src/main/java` is the only source root
and `dist/` the only artifact directory.
