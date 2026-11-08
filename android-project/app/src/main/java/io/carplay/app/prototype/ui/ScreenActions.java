package io.carplay.app.prototype.ui;

/** 首页/设置/关于页的回调接口（由 MainActivity 实现，接入既有 CarPlay 逻辑）。 */
public interface ScreenActions {
    /** 首页动作 */
    interface Home {
        void connect();
        void setAutoConnect(boolean on);
        void pickResolution();
        void setBtStatus(String status);    // 蓝牙连接状态（副文本）
        void setWlanStatus(String status);  // WLAN 连接状态（副文本）
        void selectHeadUnitBt();            // 车机→蓝牙：选择已配对蓝牙作为车机
        void selectSavedWlan();             // 车机→WLAN：选择已保存的 WLAN
        void setHubBt(String name);         // 已选车机蓝牙名
        void setHubWlan(String ssid);       // 已选车机 WLAN
    }
    /** 设置页动作 */
    interface Settings {
        void applyTheme(int which);     // 0=跟随系统 1=亮色 2=暗色（避免与 Activity.setTheme 冲突）
        void bluetoothPair();           // 蓝牙配对 (j2)
        void localBtIdentity();         // 本地蓝牙标识 (n2)
        // selectCar() 已移除：车机选择统一在 主页 → 车 机 → 蓝牙。
        void setProactiveIap2(boolean on); // [V1.8.35] 主动发起 iAP2-over-WiFi（P1-2）
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
