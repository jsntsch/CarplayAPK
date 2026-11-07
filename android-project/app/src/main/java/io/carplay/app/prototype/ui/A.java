package io.carplay.app.prototype.ui;

/**
 * 参考 APK 的 p.a 断言辅助类（仅做非空/调试检查）。
 * 原实现会抛出异常；这里降级为 no-op，避免无关崩溃。
 */
public final class A {
    private A() {}

    public static void e(String name, Object obj) {
        // 原 p.a.e：非空断言。生产构建下不做任何事。
    }

    public static void d(String msg, Object obj) {
        // 原 p.a.d：调试输出。无操作。
    }

    public static void b(Object obj) {
        // 原 p.a.b：非空断言。无操作。
    }
}
