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
        // [V1.8.60] 移除 setBtIap2Retained / setSeqV2 / setWaitCtrlConnect：
        // 对应的三个设置项已从界面删除，运行时开关收敛为常量 false（见 ControlChannel）。
        void setMdnsEnhance(boolean on); // [V1.8.38] 车机发现增强 mDNS 探针与单播通告
        void setDebugMode(boolean on);  // 调试模式（总开关：logcat + 日志记录的先决条件）
        void setLogRecording(boolean on);// [V0.0.1] 连接日志记录（需先开调试模式）
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
