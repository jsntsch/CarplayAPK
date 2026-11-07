plugins {
    id("com.android.application") version "9.2.0" apply false
}

// 不带参数执行 gradlew 时只编 debug（导出 dist/Carplay-V<ver>-debug.apk）。
// release / AAB 只在显式调用 exportRelease / exportAab 时才会构建。
// Default build is debug-only; release and AAB are opt-in via their explicit tasks.
defaultTasks(":app:exportApk")
