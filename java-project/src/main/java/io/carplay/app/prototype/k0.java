package io.carplay.app.prototype;

import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
final class k0 {
    private k0() {
    }

    public static String a(String str, boolean z) {
        if (z) {
            return "连接进行中，请等待当前流程；需要换一种方式时，先断开连接。";
        }
        String upperCase = str == null ? "" : str.toUpperCase(Locale.ROOT);
        if ((upperCase.contains("AUTH") && !upperCase.contains("AUTHENTICATED=TRUE")) || upperCase.contains("CERT") || upperCase.contains("SIGNATURE") || upperCase.contains("TRUST") || upperCase.contains("MFI") || upperCase.contains("PROOF") || upperCase.contains("PIN") || upperCase.contains("VERIFY") || upperCase.contains("VERIFICATION") || upperCase.contains("SECURITY") || upperCase.contains("INSTALL") || upperCase.contains("RELEASE") || upperCase.contains("PAIR")) {
            return "认证或验证未通过，请先核对设备并查看连接日志。两种方式都必须通过完整认证。";
        }
        if (upperCase.contains("USER") || upperCase.contains("CANCEL") || upperCase.contains("STOPPED_BY_OWNER")) {
            return "已停止连接，可按当前网络条件重新选择连接方式。";
        }
        if (upperCase.contains("DEADLINE") || upperCase.contains("TIMEOUT") || upperCase.contains("UNREACHABLE") || upperCase.contains("SOCKET") || upperCase.contains("DISCOVERY") || upperCase.contains("NO_ENDPOINT") || upperCase.contains("RFCOMM") || upperCase.contains("EOF")) {
            return "本次连接未完成，可停止后换另一种方式重试。手动 WLAN 需先连这台车的蓝牙和 CarPlay WLAN；仍需完整认证。";
        }
        return !upperCase.isBlank() ? "请到工具页查看连接详情或打开日志，确认原因后再选择连接方式。" : "一种方式未连接成功，可停止后尝试另一种；切换后仍需完整认证。";
    }

    public static String b(String str, boolean z, String str2) {
        if (!z) {
            if (str2.contains("MFI_CERTIFICATE_INVALID reason=EXPIRED")) {
                return "车机证书已超过有效期，认证尚未完成\n请先确认手机日期和时间已自动同步；时间正确时，请保留日志核查车机旧证书，无需删除配对";
            }
            if (str2.contains("MFI_CERTIFICATE_INVALID reason=NOT_YET_VALID")) {
                return "车机证书尚未到有效日期，认证尚未完成\n请先确认手机日期和时间已自动同步，保留日志和原配对后核查";
            }
            if (str2.contains("MFI_CERTIFICATE_INVALID")) {
                return "车机证书未通过检查，认证尚未完成\n请导出本次日志核对具体原因；不要删除配对或反复连接，软件不会跳过证书验证";
            }
            if (str2.contains("RFCOMM_SELECTED_PEER_UNREACHABLE")) {
                return "车机尚未建立 CarPlay 蓝牙通道\n若手机没有 CarPlay 选项，可在工具页使用“车机识别修复”，允许系统蓝牙可见提示后从车机重新搜索；无需删除配对";
            }
            if (str2.contains("authenticated=true") && str2.contains("waiting=CARPLAY_START_SESSION")) {
                return "车机已通过证书挑战，但尚未返回 CarPlay 启动参数\n请检查车机是否允许这部手机使用无线 CarPlay；可查看“本机蓝牙标识”帮助并保留日志";
            }
            if (str2.contains("Release verification") || str2.contains("RELEASE_SESSION_BLOCKED")) {
                return "当前未连接\n请点击连接车机；日志与教程仍可查看";
            }
            if (str2.contains("Certificate confirmation")) {
                return "请先确认车机证书变更\n确认后重新点击连接";
            }
            if (str2.contains("Native source")) {
                return "原生 Car+ 会话已结束\n如刚调整显示设置，请重新连接";
            }
            if (str2.contains("WIFI_CONNECTION_NOT_ESTABLISHED") || str2.contains("WIFI_UNAVAILABLE_OR_CONSENT_DECLINED")) {
                return "车机 WLAN 尚未接入\n软件未能完成自动接入，请保持无线 CarPlay 开启并保留日志，无需手填密码";
            }
            if (str2.contains("WIFI") || str2.contains("bootstrap")) {
                return "连接未完成\n请确认车机无线 CarPlay 已开启，蓝牙和 WLAN 保持开启后重试";
            }
            return (str2.isEmpty() || str2.contains("Stopped by user")) ? "尚未连接\n请选择车机，然后点击连接" : "连接已结束\n请重新连接；需要排查时可导出日志";
        }
        str.getClass();
        switch (str) {
            case "WAITING_CARPLAY_ENDPOINT":
                return "已接入车机 WLAN\n正在等待车机完成 CarPlay 协商，请稍候";
            case "RECONNECTING":
                return "连接暂时中断，正在自动恢复\n已保留原生 Car+ 会话，无需反复点击连接";
            case "CHECKING":
            case "BLUETOOTH":
                return "正在建立车机连接\n保持车机开机、蓝牙和 WLAN 开启；CarPlay 图标灰色时也可等待连接";
            case "WAITING_ACCESSORY_START":
                return "车机已通过证书挑战\n正在等待 CarPlay 启动参数，尚未取得车机 WLAN 配置；请留意车机上的连接确认提示";
            case "FORWARDING_NATIVE":
                return "正在向车机发送画面\n请以车机实际显示为准；仍无画面时请导出日志\n通话目前仍由手机处理";
            case "TRUST_REQUIRED":
                return "车机证书发生变化\n请核对设备确认提示";
            case "CAR_SCREEN_IN_USE":
                return "车机正在使用屏幕\n连接和原生 Car+ 保持中，等待车机交还屏幕";
            case "READY_FOR_CARPLUS":
                return "车机已就绪\n正在打开原生 Car+，也可点击继续按钮";
            case "WAITING_NATIVE_VIDEO":
                return "车机通道已连接，Car+ 尚未输出画面\n可打开原生 Car+ 查看连接或授权提示；请保留本次日志";
            case "VIDEO_OUTPUT_UNAVAILABLE":
                return "连接仍保持，画面帧率暂无法适配\n源端帧率超出车机声明能力，硬件适配未成功；请保留日志排查";
            case "PAIRING":
                return "正在认证车机\n请稍候";
            case "WIFI":
                return "正在自动接入车机 WLAN\n仅在系统提示时允许连接，无需手动查找热点或输入密码";
            case "NATIVE_SOURCE_ARMED":
                return "等待 Car+ 开始投屏\n首次请按原生界面提示完成连接";
            case "RESUMING_NATIVE":
                return "无线已重新连接\n正在等待画面恢复";
            case "RETRYING_CONNECTION":
                return "连接尚未完成，正在重试\n请保持蓝牙和 WLAN 开启，无需反复点击连接";
            case "BLUETOOTH_DISCOVERY_RECOVERY":
                return "已补充手机 CarPlay 识别信息，正在等待车机建立蓝牙协议通道\n可在车机蓝牙页刷新或查看 CarPlay 选项；原配对保持，注册就绪不代表车机已识别";
            default:
                return "连接正在准备\n请稍候";
        }
    }

    public static String c(String str) {
        if (str == null) {
            str = "";
        }
        switch (str) {
            case "unconfirmed":
                return "前台请求已发送，但尚未收到原生前台事件；请查看车机和日志";
            case "observed":
                return "已收到原生 Car+ 前台事件；车机实际显示仍需现场确认";
            case "waiting":
                return "正在等待原生 Car+ 就绪后请求前台；车机实际显示尚未确认";
            case "written":
                return "前台请求已发送；尚未收到原生前台事件，车机实际显示尚未确认";
            default:
                return "";
        }
    }

    public static String d(String str, boolean z) {
        if (!z) {
            return "连接车机";
        }
        str.getClass();
        switch (str) {
            case "RECONNECTING":
                return "正在自动恢复…";
            case "FORWARDING_NATIVE":
                return "画面发送中";
            case "CAR_SCREEN_IN_USE":
                return "连接保持中 · 车机正在使用屏幕";
            case "WAITING_NATIVE_VIDEO":
                return "等待 Car+ 输出画面…";
            case "VIDEO_OUTPUT_UNAVAILABLE":
                return "连接保持中 · 画面适配未完成";
            case "RESUMING_NATIVE":
                return "通道已连接 · 正在恢复画面";
            case "RETRYING_CONNECTION":
                return "正在自动重试连接…";
            default:
                return "正在连接…";
        }
    }
}
