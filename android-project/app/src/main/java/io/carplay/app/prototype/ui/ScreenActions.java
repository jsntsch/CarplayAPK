package io.carplay.app.prototype.ui;

/** 首页/设置/关于页的回调接口（由 MainActivity 实现，接入既有 CarPlay 逻辑）。 */
public interface ScreenActions {
    /** 首页动作 */
    interface Home {
        void connect();
        void setAutoConnect(boolean on);
        void pickResolution();
        void setOrientation(int which); // 0=横屏 1=竖屏
        void setFitMode(int which);     // 0=适应 1=拉伸
    }
    /** 设置页动作 */
    interface Settings {
        void setTheme(int which);       // 0=跟随系统 1=亮色 2=暗色
        void bluetoothPair();           // 蓝牙配对 (j2)
        void localBtIdentity();         // 本地蓝牙标识 (n2)
        void selectCar();               // 选择已配对车机
        void setDebugMode(boolean on);  // 调试模式
        void openLog();                 // 运行日志 (a1)
        void openTutorial();            // 使用教程 (d1)
        void openDeclaration();         // 免责声明 (k2)
    }
    /** 关于页动作 */
    interface About {
        void openGit();
        void openTutorial();
        void openDeclaration();
    }
}
