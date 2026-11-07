import io

path = "android-project/app/src/main/java/io/carplay/app/prototype/MainActivity.java"
with io.open(path, "r", encoding="utf-8") as f:
    src = f.read()

# ---- ensure ViewGroup import ----
if "import android.view.ViewGroup;" not in src:
    src = src.replace("import android.view.View;\n",
                      "import android.view.View;\nimport android.view.ViewGroup;\n", 1)

new_a2 = '''    public void a2() {
        SharedPreferences sp = getSharedPreferences(WirelessCarPlayService.b1, 0);
        boolean zL1 = WirelessCarPlayService.L1();
        String state = zL1 ? sp.getString("state", "CHECKING") : "STOPPED";
        String reason = zL1 ? sp.getString("reason", "") : "";
        boolean zS0 = S0();
        boolean zE = PairingPreparationService.e();

        StringBuilder detail = new StringBuilder();
        detail.append(zL1 ? StatusText.b(state, zL1, reason) : "本机尚未连接，点下方按钮开始。");
        if (zL1 && state.equals("WIFI") && !sp.getString("connection_route", "bluetooth").equals("bluetooth")) {
            detail.append("\\n").append(sp.getString("connection_route", "").equals("retained_wifi")
                    ? "正在复用这台车仍然连接的 WLAN\\n保持当前网络，正在重新发现并完整认证车机"
                    : "正在当前车机 WLAN 查找投屏服务\\n保持当前网络，后续仍需完整认证");
        }
        if (zL1) {
            String nativeFg = StatusText.c(sp.getString("native_foreground_state", ""));
            if (!nativeFg.isEmpty()) detail.append("\\n").append(nativeFg);
        }
        if (!this.e && !zL1) {
            detail.append("\\n").append("当前手机组件暂不支持\\n请打开使用教程查看兼容说明");
        }
        int nw = sp.getInt("native_width", 0), nh = sp.getInt("native_height", 0);
        int rw = sp.getInt("receiver_width", 0), rh = sp.getInt("receiver_height", 0);
        if (nw > 0 && nh > 0) {
            detail.append("\\n").append((zL1 ? "画面尺寸" : "上次画面尺寸")).append("：").append(nw).append(" × ").append(nh);
        }
        if (!(nw <= 0 || nh <= 0 || !sp.getBoolean("native_exact_geometry_requested", false))) {
            int sw = sp.getInt("source_requested_width", 0);
            int sh = sp.getInt("source_requested_height", 0);
            detail.append("\\n").append((nw == sw && nh == sh) ? "原生 Car+ 已返回目标画布尺寸"
                    : "原生 Car+ 返回的尺寸与请求不同：" + sw + " × " + sh);
        }
        String fit = sp.getString("fit_mode", "");
        if (fit.equals("fitted")) {
            detail.append("\\n").append((zL1 ? "自动适配输出" : "上次适配输出")).append("：")
                    .append(sp.getInt("output_width", 0)).append(" × ").append(sp.getInt("output_height", 0))
                    .append(sp.getBoolean("fill_display", false) ? "（拉伸铺满）" : "（等比显示）");
        } else if (fit.equals("starting")) {
            detail.append("\\n正在准备画面自动适配…");
        }

        String title;
        int stateColor;
        if (zL1) {
            title = StatusText.d(state, true);
            stateColor = 2;
        } else if (!this.e) {
            title = "当前手机组件暂不可用";
            stateColor = 3;
        } else if (!zS0) {
            title = "请先阅读使用说明";
            stateColor = 0;
        } else {
            title = "准备连接你的爱车";
            stateColor = 0;
        }

        String info;
        if (!this.e && !zL1) {
            info = "当前手机组件暂不支持，请打开使用教程查看兼容说明。";
        } else if (!zS0 && !zL1) {
            info = "请先阅读并同意使用说明。";
        } else if (zL1 && state.equals("FORWARDING_NATIVE")) {
            info = "短暂中断会自动尝试恢复，日志持续保存在本机。";
        } else {
            info = "";
        }

        String btnText;
        boolean btnEnabled;
        if (zL1) {
            btnText = "断开连接";
            btnEnabled = true;
        } else if (zE) {
            btnText = "停止等待";
            btnEnabled = true;
        } else if (!zS0 || !this.e) {
            btnText = "开始连接";
            btnEnabled = false;
        } else {
            btnText = "开始连接";
            btnEnabled = true;
        }

        if (homeScreen != null) {
            homeScreen.setConnectionStatus(title, detail.toString(), stateColor, btnText, btnEnabled);
            homeScreen.setAutoConnect(this.autoConnect);
        }
        renderStepsAndResolution(sp, zL1, state);
        if (homeScreen != null) {
            homeScreen.showInfo(zE && !zL1
                    ? (PairingPreparationService.f() ? "识别准备就绪\\n请从车机搜索/配对手机，然后刷新列表、选择车机连接。准备最多两分钟。"
                        : "正在准备蓝牙识别信息…")
                    : info);
        }
        this.k = state;
    }
'''

new_onCreate = '''    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        SharedPreferences dbgPrefs = getSharedPreferences("carplayapk-debug", 0);
        this.logEnabled = dbgPrefs.getBoolean("log_enabled", true);
        this.debugMode = dbgPrefs.getBoolean("debug_mode", false);
        this.autoConnect = dbgPrefs.getBoolean("auto_connect_launch", false);
        AppLogger.setRecordingEnabled(this.logEnabled);

        this.g = bundle != null && bundle.getBoolean("continue_source_intent", false);
        this.a = bundle != null && bundle.getBoolean("pairing_visibility_pending", false);
        this.l = bundle == null ? null : bundle.getString("identity_connect_address");
        this.p = bundle != null ? bundle.getLong("current_wifi_handle", 0L) : 0L;
        this.E = bundle != null && bundle.getBoolean("home_manual_route", false);

        // 应用主题（跟随系统）
        Ui.c(getApplicationContext(), 0);

        // 三屏（移植自参考 APK 的 HomeScreen / SettingsScreen / AboutScreen 布局）
        homeScreen = new HomeScreenUi(this, this);
        settingsScreen = new SettingsScreenUi(this, this);
        aboutScreen = new AboutScreenUi(this, this);

        // 横向翻页容器
        pager = new Pager(this);
        pager.addView(homeScreen, new ViewGroup.LayoutParams(-1, -1));
        pager.addView(settingsScreen, new ViewGroup.LayoutParams(-1, -1));
        pager.addView(aboutScreen, new ViewGroup.LayoutParams(-1, -1));
        pager.setOnPageChangeListener(new Pager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int index) {
                currentPage = index;
                updateTabHighlight();
            }
        });

        // 顶部标签栏
        LinearLayout tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundColor(Ui.f1234b);
        int tabH = Ui.h(this, 52.0f);
        tabBar.setPadding(Ui.h(this, 12.0f), 0, Ui.h(this, 12.0f), 0);
        String[] tabs = {"主页", "设置", "关于"};
        tabButtons = new Button[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            Button tb = new Button(this);
            tb.setText(tabs[i]);
            tb.setAllCaps(false);
            tb.setBackground(null);
            tb.setMinHeight(0);
            tb.setMinimumHeight(0);
            tb.setTextSize(15.0f);
            tb.setTypeface(i == 0 ? Ui.B : Ui.C);
            tb.setTextColor(i == 0 ? Ui.f1241i : Ui.f1244l);
            tb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    i1(idx);
                }
            });
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, tabH, 1.0f);
            tlp.gravity = 16;
            tabBar.addView(tb, tlp);
            tabButtons[i] = tb;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        root.addView(tabBar, new LinearLayout.LayoutParams(-1, -2));
        root.addView(pager, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        setContentView(root);
        Ui.k(this);

        H0();
        if (S0()) {
            T1();
        }
        a2();
        // [UI 重构] 启动后自动连接：已开启且记住车机，延迟触发一次
        if (S0() && this.autoConnect && i2() != null && !WirelessCarPlayService.L1() && !this.autoTriggered) {
            this.autoTriggered = true;
            this.c.postDelayed(new Runnable() {
                @Override public void run() { startConnectFromHome(); }
            }, 600L);
        }
        AppLogger.g(this, ProtoMessageBase.e("USER_UI_OPEN version=", A2(this)));
    }
'''

new_render = '''    private void renderStepsAndResolution(SharedPreferences sp, boolean connected, String state) {
        int nw = sp.getInt("native_width", 0);
        int nh = sp.getInt("native_height", 0);
        int rw = sp.getInt("receiver_width", 0);
        int rh = sp.getInt("receiver_height", 0);
        if (homeScreen != null) {
            homeScreen.setResolution(nw > 0 && nh > 0 ? (nw + " × " + nh) : "—",
                                    rw > 0 && rh > 0 ? (rw + " × " + rh) : "—");
        }
        int cur = 1;
        if (connected) {
            if (state.equals("FORWARDING_NATIVE") || state.equals("CAR_SCREEN_IN_USE") || state.equals("READY_FOR_CARPLUS") || state.equals("RESUMING_NATIVE")) {
                cur = 7;
            } else if (state.equals("WAITING_CARPLAY_ENDPOINT") || state.equals("NATIVE_SOURCE_ARMED") || state.equals("WAITING_NATIVE_VIDEO") || state.equals("VIDEO_OUTPUT_UNAVAILABLE")) {
                cur = 6;
            } else if (state.equals("WAITING_ACCESSORY_START") || state.equals("PAIRING") || state.equals("TRUST_REQUIRED")) {
                cur = 5;
            } else if (state.equals("WIFI")) {
                cur = 3;
            } else {
                cur = 4;
            }
        } else if (state.equals("WIFI")) {
            cur = 3;
        } else if (state.contains("AUTH") || state.contains("CERT") || state.contains("PAIR")) {
            cur = 2;
        } else {
            cur = 1;
        }
        if (homeScreen != null) homeScreen.setStep(cur);
    }
'''

new_onSave = '''    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("continue_source_intent", this.g);
        bundle.putBoolean("pairing_visibility_pending", this.a);
        bundle.putString("identity_connect_address", this.l);
        bundle.putLong("current_wifi_handle", this.p);
        bundle.putBoolean("home_manual_route", this.E);
        bundle.putInt("workspace_page", currentPage);
        super.onSaveInstanceState(bundle);
    }
}
'''

interface_methods = '''    // ===== ScreenActions 接口实现 =====

    @Override
    public void connect() {
        if (WirelessCarPlayService.L1()) {
            startService(new Intent(this, WirelessCarPlayService.class).setAction(WirelessCarPlayService.W0));
            o2("正在断开连接…");
        } else {
            startConnectFromHome();
        }
    }

    @Override
    public void setAutoConnect(boolean on) {
        this.autoConnect = on;
        getSharedPreferences("carplayapk-debug", 0).edit().putBoolean("auto_connect_launch", on).apply();
        if (homeScreen != null) homeScreen.setAutoConnect(on);
        o2(on ? "已开启自动连接，下次启动应用会自动连接车机。" : "已关闭自动连接。");
    }

    @Override
    public void pickResolution() {
        SharedPreferences sp = getSharedPreferences(WirelessCarPlayService.b1, 0);
        int nw = sp.getInt("native_width", 0), nh = sp.getInt("native_height", 0);
        int rw = sp.getInt("receiver_width", 0), rh = sp.getInt("receiver_height", 0);
        String msg = "投屏画面：" + (nw > 0 ? nw + " × " + nh : "—") + "\\n手机屏幕：" + (rw > 0 ? rw + " × " + rh : "—");
        new AlertDialog.Builder(this).setTitle("投屏分辨率").setMessage(msg)
                .setPositiveButton("知道了", (DialogInterface.OnClickListener) null).show();
    }

    @Override
    public void setOrientation(int which) {
        try {
            setRequestedOrientation(which == 1 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            o2(which == 1 ? "已切换为竖屏" : "已切换为横屏");
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void setFitMode(int which) {
        getSharedPreferences("wireless-ui", 0).edit().putInt("fit", which == 1 ? 1 : 0).apply();
        o2(which == 1 ? "画面适配：拉伸铺满" : "画面适配：等比显示");
    }

    @Override
    public void setTheme(int which) {
        Ui.c(getApplicationContext(), which);
        recreate();
    }

    @Override
    public void selectCar() {
        if (!S0()) { P0(); return; }
        T1();
        if (this.r.isEmpty()) {
            o2("还没有已配对的车机，请先点「蓝牙配对」。");
            j2();
            return;
        }
        String[] items = new String[this.r.size()];
        for (int i = 0; i < this.r.size(); i++) items[i] = X1(i);
        int checked = (this.q >= 0 && this.q < this.r.size()) ? this.q : 0;
        new AlertDialog.Builder(this).setTitle("选择车机").setSingleChoiceItems(items, checked,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.q = which;
                        String addr = MainActivity.this.r.get(which).b;
                        getSharedPreferences("wireless-ui", 0).edit().putString("selected_address", addr).apply();
                        dialog.dismiss();
                        z2();
                        a2();
                        o2("已选择：" + X1(which));
                    }
                }).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    @Override
    public void setDebugMode(boolean on) {
        this.debugMode = on;
        getSharedPreferences("carplayapk-debug", 0).edit().putBoolean("debug_mode", on).apply();
        if (settingsScreen != null) settingsScreen.setDebugMode(on);
        o2(on ? "已开启调试模式。" : "已关闭调试模式。");
    }

    @Override
    public void openTutorial() { d1(); }

    @Override
    public void openDeclaration() { k2(); }

    @Override
    public void bluetoothPair() { j2(); }

    @Override
    public void localBtIdentity() { n2(); }

    @Override
    public void openLog() { a1(); }

'''

# ---- a2 ----
s = src.index("    public void a2() {")
e = src.index("    public void b2() {")
src = src[:s] + new_a2 + "\n" + src[e:]

# ---- onCreate ----
s = src.index("    public void onCreate(Bundle bundle) {")
e = src.index("    // ===== [V1.8.26] UI 重构新增方法 =====")
src = src[:s] + new_onCreate + "\n" + src[e:]

# ---- remove buildHomeExtras (comment + method) up to startConnectFromHome ----
s = src.index("    // ===== [V1.8.26] UI 重构新增方法 =====")
e = src.index("    public void startConnectFromHome() {")
src = src[:s] + src[e:]

# ---- renderStepsAndResolution ----
s = src.index("    private void renderStepsAndResolution(SharedPreferences sp, boolean connected, String state) {")
e = src.index("    private void clearLogs() {")
src = src[:s] + new_render + "\n" + src[e:]

# ---- onSaveInstanceState (replace from @Override to end of file) ----
marker = "    public void onSaveInstanceState(Bundle bundle) {"
s = src.index(marker)
o = src.rindex("    @Override", 0, s)
src = src[:o] + new_onSave

# ---- openGit: private -> public + prepend interface methods ----
old_open = "    private void openGit() {"
assert old_open in src, "openGit not found"
src = src.replace(old_open, interface_methods + "    public void openGit() {", 1)

with io.open(path, "w", encoding="utf-8") as f:
    f.write(src)

print("refactor done; len=", len(src))
