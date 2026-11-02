package io.carplay.app.prototype;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import io.carplay.app.secured.u2;
import io.carplay.app.secured.v2;
import java.lang.invoke.VarHandle;

/* JADX INFO: loaded from: classes.dex */
public final class HelpActivity extends Activity {
    private int a;

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void d(View view) {
        startActivity(new Intent(this, (Class<?>) LogActivity.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void e(View view) {
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void f(TextView textView, TextView textView2, String str, View view) {
        boolean z = textView.getVisibility() != 0;
        textView.setVisibility(z ? 0 : 8);
        textView2.setText(str + (z ? "  −" : "  +"));
        textView2.setContentDescription(str + (z ? "，已展开，点按收起" : "，已收起，点按展开"));
    }

    private void g(LinearLayout linearLayout, String str, String str2) {
        LinearLayout linearLayoutJ = m0.j(linearLayout);
        TextView textViewM = m0.m(linearLayoutJ, str, "help");
        textViewM.setMinHeight(m0.l(this, 44));
        int i = 0;
        TextView textViewC = m0.C(linearLayoutJ, str2, 14, false);
        textViewC.setTextColor(-11311232);
        textViewC.setLineSpacing(m0.l(this, 5), 1.0f);
        textViewC.setTextIsSelectable(true);
        textViewC.setPadding(0, m0.l(this, 8), 0, m0.l(this, 4));
        int i2 = this.a;
        this.a = i2 + 1;
        boolean z = i2 == 0;
        textViewC.setVisibility(z ? 0 : 8);
        textViewM.setText(str + (z ? "  −" : "  +"));
        textViewM.setContentDescription(str + (z ? "，已展开，点按收起" : "，已收起，点按展开"));
        textViewM.setFocusable(true);
        v2 v2Var = new v2(i);
        v2Var.c = textViewC;
        v2Var.d = textViewM;
        v2Var.b = str;
        VarHandle.storeStoreFence();
        textViewM.setOnClickListener(v2Var);
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LinearLayout linearLayoutX = m0.x(this);
        m0.D(this, linearLayoutX, "使用教程", "从首次连接到日常使用，一页看明白");
        TextView textViewC = m0.C(linearLayoutX, "轻点卡片标题展开或收起。请在安全停车后完成设置与排查。", 13, false);
        textViewC.setTextColor(-10193265);
        textViewC.setPadding(0, 0, 0, m0.l(this, 16));
        g(linearLayoutX, "第一次连接", "1. 车机正常开机。若有无线 CarPlay 开关，请确认已开启；图标灰色时无需先点击进入。\n\n2. 手机开启蓝牙和 WLAN，在系统蓝牙设置配对车机；已有配对请保留。\n\n3. 回本软件刷新列表，选择车机，点击“连接车机”。允许附近设备、本地网络及系统 WLAN 提示。\n\n4. 首次签名挑战通过后自动记住车机并继续。若已记住车机的证书发生变化，核对设备后再确认更新。\n\n5. 系统确认接入车机 WLAN 后，会继续等待车机完成 CarPlay 协商。\n\n6. 车机就绪后打开系统设置->设备互联->智慧车载->连接车辆->通用连接->无线连接百度Carlife+->选择连接方式改为连接车辆热点，否则连接车机热点后无网络");
        g(linearLayoutX, "两种连接方式怎样切换", "方式一：自动连接（推荐）。保留手机与车机的蓝牙配对，开启蓝牙和 WLAN，选择车机后点“连接车机”，按系统提示接入车机网络。\n\n方式二：使用当前车机 WLAN 连接。需先在手机系统设置连接这台车的蓝牙和车机提供的 CarPlay WLAN；普通上网热点不一定能用于投屏。再回连接页选择同一车机，选中“当前车机 WLAN”，点击“使用当前车机 WLAN 连接”。\n\n一种方式未连接成功时，可以先停止当前流程，再手动尝试另一种；WLAN 方式失败也可回到自动连接。软件不会自动切换网络或并行发起两次连接。\n\n认证、证书、安装包验证或权限未通过时，请先核对相应问题并查看日志。两种方式都执行完整车机认证，切换不是跳过验证。没有车机 WLAN 时，不必盲目改连普通热点。");
        g(linearLayoutX, "以后怎样连接", "底部“连接 / 工具 / 支持”可随时切换页面，不会中断正在运行的连接。顶部“原生 Car+”和“日志”始终可用。\n\n软件会记住上次选择的车机。车机正常开机，保持手机蓝牙和 WLAN 开启，进入本软件点击连接即可发起连接，无需先点击车机上的灰色 CarPlay 图标。\n\n本次软件进程中已经完整认证过的车机，如果仍连接着同一个有效 WLAN，自动连接会直接复用并重新认证，无需断开热点再连接。应用进程重启、网络变化或证书变化后恢复正常引导；不会把任意当前 WLAN 当作车机。\n\n按系统提示加入车机提供的网络。通常不需要自己开手机热点，也不需要手填网络地址。");
        g(linearLayoutX, "手机蓝牙地址怎么填写", "连接前若系统未提供可用的手机蓝牙地址，软件会自动弹出填写框；已保存或可自动读取时直接继续。在“工具”页可查看或修改“本机蓝牙标识”。\n\n1. 先开启手机蓝牙。\n2. 手机设置 → 关于本机 / 关于手机 → 状态信息 → 蓝牙地址。有些系统叫“状态”或“所有参数与信息”。\n3. 找不到时，可在手机设置中搜索“蓝牙地址”或“状态信息”；显示不可用时，开启蓝牙后重新查看。\n4. 填入这部手机显示的真实蓝牙地址，六组字符使用英文冒号分隔。不能填写车机地址、WLAN地址或系统占位地址。\n\n点击“保存并连接”后，会先保存再继续所选车机连接。一部手机保存一次，覆盖升级和更换车机不会主动清除；也可通过工具页入口查看或修改。找不到时点“暂不填写”可继续，不能保证所有车机都需要或仅靠地址就能启动。\n\n地址只保存在本机，不写入连接日志。清除手动地址必须明确点击“清除手动地址”；取消和空输入不会删除原值。");
        g(linearLayoutX, "短暂断线怎么办", "连接会自动尝试恢复，连续不稳定时最多重试 3 次，间隔约 1、3、8 秒；每次重新建立网络和认证本身还需要时间。投屏持续稳定 30 秒后，重试次数才会重新计算。\n\n仍存活的原生 Car+ 会话会保留，不必反复点原生 Connect。网络、音频、视频等通道同时报错时只发起一次恢复，旧连接的回调会被忽略。认证失败、取消系统授权不会无限重试。\n\n如果原生 Car+ 退出或系统结束应用，需要重新连接。软件没有固定使用时长限制。主页“断开连接”和通知中的停止按钮可结束连接。");
        g(linearLayoutX, "画面自动适配", "“工具”页可调整“画面自动适配”，连接前可切换。开启后，在手机编码能力预检通过时，通过原生 CarLife 协议请求 Car+ 使用车机目标画布，不再只依赖组件的固定分辨率档位。长边不超过 1920 时保留车机的偶数宽高；更大画面沿用原来的同比缩小规则。关闭后保留原生手动分辨率选择。\n\n原生实际返回的宽高才是视频和触摸的依据；工具页会显示目标尺寸是否被采用。画布匹配时优先直接转发，减少黑边与二次适配。原生端仍返回其他尺寸时，保留等比硬件适配及明确选择的拉伸铺满；不通过固定车型偏移校正触摸。\n\n原生 Car+ 可能根据这次请求更新自己的显示配置。它保存的安全区域、边距以及系统输入行为仍可能影响效果；已发送请求不代表实车画面或触摸已经验证。\n\n若原生没有采用目标尺寸，可按工具页显示的尺寸查看 Car+：\n齿轮 → 显示设置 → 屏幕分辨率 → 其他分辨率 → 自定义分辨率 → 填宽高 → 右上角确认。\n\n调整原生设置可能结束会话，确认后重连一次。本软件不直接改写受保护的原生设置；车机在当前会话中旋转或更改显示区域仍可能需要重连。");
        g(linearLayoutX, "日志会自动保存", "连接阶段、实际画面尺寸、断线原因、恢复过程及崩溃信息会自动保存在手机本机，退出应用后仍保留。保存最近 5 个日志文件，每个约 1 MiB。\n\n在“连接日志”里可查看最近记录，点击“导出完整日志文件”选择位置保存。无需连接电脑，不会自动上传，也不记录配对私钥或原始音视频。\n\n覆盖升级保留应用数据及日志；卸载或清除应用数据会删除记录。导出的文件由你选择的保存位置保管。");
        g(linearLayoutX, "车机只有 Bluetooth、没有 CarPlay 选项", "已能连接的车机仍使用“连接”页的自动连接方式。只有车机无法识别本手机为 CarPlay 时，才到工具页点击“车机识别修复”。\n\n1. 点击开始准备，允许附近设备及系统蓝牙可被发现提示。识别信息准备好后才弹出可见授权。\n2. 在车机添加/搜索手机并确认系统配对，再回软件刷新列表、选择车机、点击连接。已有配对可先从车机重新搜索或重新连接；软件不会自行删除配对。若车机一直使用旧能力，可按车机提示重新配对。\n3. 准备最多保留两分钟，取消或超时会释放；连接后继续完整车机认证。软件记住为这台车机启用增强识别，其它车机不自动启用。\n\n系统注册成功不等于车机已识别；CarPlay菜单出现也不代表投屏成功。请以车机表现及日志阶段为准，无需修改手机名称或手填热点密码。原有LOG、信任及本机身份保留。");
        g(linearLayoutX, "手动连接车机 WLAN", "若普通连接停在等待启动参数，可先在系统设置连接车机蓝牙和车机提供的 CarPlay WLAN，再在连接页选中“当前车机 WLAN”，点击“使用当前车机 WLAN 连接”。允许无互联网连接，保持当前网络。\n\n软件会在选定 WLAN 查找服务，无需先收到蓝牙启动参数；后续配对、证书挑战和媒体认证仍完整执行。首次会显示证书确认，确认设备与网络后手动重试。普通上网热点不一定提供 CarPlay 服务，找不到时可导出日志。换网会停止本次连接。");
        g(linearLayoutX, "手机究竟要连哪个网络", "本方案会从已认证车机取得CarPlay无线网络参数，再请求手机接入车机提供的本地WLAN。手机实际需要接入这个网络，系统弹出连接确认时请允许。\n\n“不必提前手动连接上网热点”并不是“不需要Wi-Fi连接”。车机没有Wi-Fi菜单或该网络无互联网，都不等于CarPlay无线连接不可用。无需为了测试反复修改车机网络设置。");
        g(linearLayoutX, "车机 WLAN 自动接入", "软件会根据已认证车机下发的信息自动尝试接入，不要求你在 WLAN 列表中查找热点或手动配置。保持手机 WLAN 开启，系统要求连接或授权时按提示允许即可。无需自行创建手机热点。\n\n“工具”页中的“WLAN 帮助”显示当前车机网络名称与实际接入进度。未收到配置时会继续等待车机下发；取得配置后自动尝试接入。普通扫描列表中看不到该网络，不能单独说明车机没有提供网络。\n\n显示“已接入车机 WLAN”后，若后续车机认证或投屏控制失败，请保留日志排查对应阶段，无需反复更改 WLAN 设置。目标网络名会记入 LOG 便于排查，密码不写入 LOG 或设置。");
        g(linearLayoutX, "车机没有Wi-Fi菜单 / 无互联网", "无线CarPlay使用手机和车机之间的本地WLAN，不要求车机能够上网，也不要求车机有可见的Wi-Fi设置菜单。\n\n请保持手机WLAN开启，允许系统接入车机网络的提示；若提示无互联网，请保持该连接。通常不需要自己开启手机热点。");
        g(linearLayoutX, "只有投屏应用没网", "如果手机其他应用可以上网，只有投屏内的应用不能上网，请先保持现有车机 WLAN，保留出问题的应用名称和发生时间。它与整部手机断网是不同问题，无需反复切换热点。\n\n查看原生 Car+ 当前车机的设置，如存在“使用手机网络”选项，可开启后对照；这个选项是否显示取决于原生版本、连接模式和手机能力。也请核对出问题应用的移动数据使用权限。\n\nOcarPlay 的投屏连接只使用选定车机网络，不把整个手机切到车机网络；普通应用不能替原生 Car+ 或其它投屏应用修改受保护的网络策略。若仍无网，请导出日志，并保留对应原生 Car+ 版本信息，不要清除配对或应用数据。");
        g(linearLayoutX, "CarPlay 图标灰色 / 蓝牙已连接", "部分车机在连接完成前，CarPlay 图标为灰色且无法点击，无需先进入该页面。保持车机开机，手机蓝牙和 WLAN 开启，保留现有配对，在本软件选择车机并点击连接。有无线 CarPlay 或添加手机入口的车机，也可按车机提示使用该入口。\n\n系统显示蓝牙电话或音乐已连接，不代表投屏通道已就绪。软件先监听车机接入，等待后会自动尝试主动连接；失败仍继续等待车机发起。看到本软件提示车机就绪后，再按提示进入原生 Car+。无需清除配对，也无需反复点击连接。");
        g(linearLayoutX, "安装与更新", "获取新版安装包后，直接覆盖安装，保留本机 LOG、配对和设置，不要先卸载或清除数据。日志与教程始终可查看、导出。\n\n新建连接前需要手机联网核对版本、发布签名及安装包完整性。验证不可用时请稍后重试；正在运行的投屏及其内部自动恢复不受检查影响，日志与教程仍可使用。手机与车机之间仍需蓝牙和本地 WLAN。\n\n首次使用所选已配对车机时，签名挑战验证通过后会自动记住证书并继续；已记住车机的证书发生变化时，才需要核对并确认。");
        g(linearLayoutX, "返回车机与重进投屏", "投屏连接运行时，可以点“连接”页“返回车机”，请求车机显示自己的界面，同时保留连接与原生 Car+ 会话。需要继续投屏时，点“返回投屏”，请求恢复当前会话的画面；无需清除配对或重新点原生 Connect。\n\n部分车机返回后入口仍显示灰色，可先从本软件恢复已有会话。是否切换成功取决于车机是否接受请求，按钮不会伪报成功。若连接已经断开，请重新点击“连接车机”。“打开原生 Car+”仍可进入原生设置。");
        g(linearLayoutX, "打开原生 Car+ 首页", "顶部“原生 Car+”优先打开原厂“智慧车载”首页，不主动断开投屏。若该系统限制普通应用直达，则打开手机系统设置，并提示搜索“智慧车载”或“Car+车联”。无需 Shizuku，也不新增授权。原生页面名称可能因系统版本不同而变化。");
        g(linearLayoutX, "帧率与流畅度", "软件会按车机声明能力向原生 Car+ 请求帧率，最高 60fps。源端实际只提供 30fps 时，保持真实 30fps，不复制画面凑帧。工具页连接详情显示手机端实测发送帧率；这是发送统计，不代表车机实际显示帧率。实际画面到达和发送节奏会记入本机日志。\n\n尺寸和格式可直接使用时，优先原样转发压缩画面，减少硬件适配开销。确需适配时使用硬件编解码；硬件无法支持目标帧率会记录回退原因。具体手机和车机的 60fps 能力仍需实际验证。");
        g(linearLayoutX, "找不到车机 / 一直连接中", "找不到车机：先在系统蓝牙设置配对，再回软件刷新。\n等待车机：确认车机已开机、所选蓝牙名称正确，保持蓝牙和 WLAN 开启；CarPlay 图标灰色时仍可等待连接。有添加手机入口时，可按车机提示操作。\n等待 WLAN：处理系统连接提示，保持蓝牙和 WLAN 开启。\n等待 Car+：回软件点继续打开 Car+，按原生提示连接。\n恢复失败或画面不动：保留发生时间，进入连接日志导出记录后排查。");
        g(linearLayoutX, "兼容与功能范围", "手机需要可用的原生 Car+ 及匹配组件。已核验基线为一加 / ColorOS 的原厂 Car+ 与 CarLife 组件 8.6.8（260）。其它版本若保持原厂签名、可用入口及必要组件结构，可正常尝试连接，连接时仍会检查所需能力，能否使用以实际连接为准；不代表所有组件版本或车机均已实测兼容。\n\n当前提供无线投屏、媒体声音和触控。通话双向音频、麦克风、手机 USB 直连尚未提供，不能仅凭投屏成功判断车机免提已可用。不同车机的兼容性以实际连接为准。");
        g(linearLayoutX, "交流与反馈", "OcarPlay 交流 QQ 一群1124969763、二群1125859877。在“支持”页可分别复制，两个群都可以交流和反馈。\n\n反馈时请说明手机系统、原生 Car+ 版本、发生时间、连接阶段及操作步骤。完整日志请先检查并遮盖设备或网络隐私。实车异常请先保证行车安全。");
        Button button = new Button(this);
        button.setText("打开连接日志");
        u2 u2Var = new u2(0);
        u2Var.b = this;
        VarHandle.storeStoreFence();
        button.setOnClickListener(u2Var);
        linearLayoutX.addView(button);
        m0.i(button, true);
        Button button2 = new Button(this);
        button2.setText("返回连接首页");
        u2 u2Var2 = new u2(1);
        u2Var2.b = this;
        VarHandle.storeStoreFence();
        button2.setOnClickListener(u2Var2);
        linearLayoutX.addView(button2);
        m0.i(button2, false);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.addView(linearLayoutX);
        setContentView(scrollView);
        m0.w(this);
    }
}
