/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 RfcommSessionLog。RFCOMM 会话日志：按统一格式记录建链、选路与失败原因。
 *   This file defines RfcommSessionLog. RFCOMM session log: records link setup, profile selection and failure reasons in a uniform format.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;


/**
 * RFCOMM 会话日志：按统一格式记录建链、选路与失败原因。
 *
 * RFCOMM session log: records link setup, profile selection and failure reasons in a uniform format.
 */
public final class RfcommSessionLog {

    /**
     * 枚举：本子系统使用的状态/类型取值集合。
     *
     * Enumeration: state/type values used by this subsystem.
     */
    public enum a {
        NONE,
        BLUETOOTH,
        AUTH,
        START,
        WLAN,
        CONTROL,
        SOURCE,
        VIDEO
    }

    private RfcommSessionLog() {
    }

    public static String b(String str) {
        if (str == null) {
            str = "";
        }
        return str + (str.endsWith("\n") ? "" : "\n") + "\n中文诊断摘要：" + d(str) + "\n";
    }

    
    public static boolean c(String str, String... strArr) {
        for (String str2 : strArr) {
            if (str.contains(str2)) {
                return true;
            }
        }
        return false;
    }

    public static String d(String str) {
        if (str == null || str.isBlank()) {
            return "暂无可判断的连接记录，具体原因尚未确认。";
        }
        int iIndexOf = str.indexOf("\n===== 历史 HISTORY");
        if (iIndexOf < 0) {
            iIndexOf = str.length();
        }
        int iLastIndexOf = str.lastIndexOf(" SESSION START |", iIndexOf);
        int i = 0;
        if (iLastIndexOf < 0) {
            iLastIndexOf = 0;
        }
        b bVar = new b(i);
        while (iLastIndexOf < iIndexOf) {
            int iIndexOf2 = str.indexOf(10, iLastIndexOf);
            if (iIndexOf2 < 0 || iIndexOf2 > iIndexOf) {
                iIndexOf2 = iIndexOf;
            }
            bVar.a(str.substring(iLastIndexOf, Math.min(iIndexOf2, iLastIndexOf + 4096)));
            iLastIndexOf = iIndexOf2 + 1;
        }
        return bVar.c();
    }

    /**
     * RfcommSessionLog 的内部辅助类：承担该类的局部实现细节。
     *
     * Helper nested in RfcommSessionLog, carrying local implementation detail.
     */
    public static final class b {
        a a;
        boolean b;
        boolean c;
        boolean d;
        boolean e;
        boolean f;
        boolean g;
        boolean h;
        String i;

        private b() {
            this.a = a.NONE;
        }

        public void a(String str) {
            if (RfcommSessionLog.c(str, "RFCOMM_SELECTED_PEER_CONNECTED", "RFCOMM_SELECTED_PEER_ACCEPTED")) {
                this.a = a.AUTH;
                this.b = false;
                this.i = null;
                this.c = false;
                this.e = false;
                this.f = false;
                this.g = false;
                this.h = false;
            }
            if (RfcommSessionLog.c(str, "RFCOMM_LISTENING_SELECTED_PEER", "RFCOMM_SELECTED_PEER_ACTIVE_CONNECT_START") && this.a == a.NONE) {
                this.a = a.BLUETOOTH;
            }
            if (RfcommSessionLog.c(str, "MFI_KEY_POSSESSION_VERIFIED", "MFI_FRESH_PROOF_VERIFIED")) {
                this.b = true;
                int iOrdinal = this.a.ordinal();
                a aVar = a.START;
                if (iOrdinal < aVar.ordinal()) {
                    this.a = aVar;
                }
                this.i = null;
            }
            if (RfcommSessionLog.c(str, "IAP2_AUTHENTICATED_WAITING_CARPLAY_START", "waiting=CARPLAY_START_SESSION")) {
                this.a = a.START;
                this.b = true;
            }
            if (RfcommSessionLog.c(str, "IAP2_AUTHENTICATED_WIFI_CONFIGURATION_RECEIVED", "WIFI_AUTHENTICATED_TARGET", "WIFI_NETWORK_READY_WAITING_CARPLAY_ENDPOINT")) {
                this.a = a.WLAN;
                this.i = null;
            }
            if (RfcommSessionLog.c(str, "WIFI_AND_CARPLAY_ENDPOINT_READY_SOCKET_BINDING_REQUIRED", "CARPLAY_CONTROL_CONNECTING", "CARPLAY_PAIR_SETUP", "CARPLAY_INITIAL_SETUP")) {
                this.a = a.CONTROL;
                this.i = null;
            }
            if (RfcommSessionLog.c(str, "CARPLAY_MEDIA_READY", "Native source initialized but has not produced video bytes", "Normal Car+ entry requested")) {
                this.a = a.SOURCE;
                this.i = null;
            }
            if (RfcommSessionLog.c(str, "VIDEO_EPOCH_FIRST_IDR_SENT=", "Actual source-derived IDR sent on authenticated transport")) {
                this.a = a.VIDEO;
                this.i = null;
                this.h = false;
            }
            if (str.contains("CARPLAY_SCREEN_OWNER_PHONE=false")) {
                this.e = true;
            }
            if (str.contains("CARPLAY_SCREEN_OWNER_PHONE=true")) {
                this.e = false;
            }
            if (RfcommSessionLog.c(str, "AUDIO_OUTPUT_UNAVAILABLE", "AUDIO_ROUTE_UNAVAILABLE", "AUDIO_SENDER_UNAVAILABLE", "AUDIO_DECLARATION_REJECTED")) {
                this.f = true;
            }
            if (RfcommSessionLog.c(str, "AUDIO_READY ", "RECOVERY_MEDIA_AUDIO_RELEASED")) {
                this.f = false;
            }
            if (RfcommSessionLog.c(str, "AUDIO_RESOURCE_NOT_OWNED", "AUDIO_USER_REQUEST_REJECTED")) {
                this.g = true;
            }
            if (str.contains("AUDIO_READY ") && (str.contains("stream=100 ") || str.contains("stream=102 "))) {
                this.g = str.contains("main_owned=false");
            }
            if (RfcommSessionLog.c(str, "AUDIO_MAIN_MODES_CONFIRMED", "AUDIO_USER_REQUEST_ALREADY_OWNED", "RECOVERY_MEDIA_AUDIO_RELEASED")) {
                this.g = false;
            }
            if (RfcommSessionLog.c(str, "EVENT_CHANNEL_", "CONTROL_CHANNEL_", "VIDEO_WRITE_", "Recovering selected receiver")) {
                this.h = this.a.ordinal() >= a.CONTROL.ordinal();
            }
            String strB = b(str);
            if (strB != null) {
                this.i = strB;
            }
            if (str.contains("Stopped by user")) {
                this.c = true;
            }
            if (str.contains("APP_CRASH ")) {
                this.d = true;
            }
        }

        public String b(String str) {
            if (RfcommSessionLog.c(str, "MFI_CERTIFICATE_REJECTED reason=EXPIRED", "MFI_CERTIFICATE_INVALID reason=EXPIRED")) {
                return "收到的车机证书已过有效期，认证在签名挑战前停止；请先核对手机自动日期时间，尚不能认定车机私钥验证失败";
            }
            if (RfcommSessionLog.c(str, "MFI_CERTIFICATE_REJECTED reason=NOT_YET_VALID", "MFI_CERTIFICATE_INVALID reason=NOT_YET_VALID")) {
                return "收到的车机证书尚未到有效日期，请先核对手机自动日期时间，认证尚未完成";
            }
            if (RfcommSessionLog.c(str, "RELEASE_CHECK result=CHECK_FAILED_UnknownHostException")) {
                return "新建连接被版本验证的域名解析失败阻止，尚未重新进入车机协商；请保留车机WLAN并开启手机移动数据重试";
            }
            if (RfcommSessionLog.c(str, "NATIVE_CHANNEL_FAIL channel=CMD")) {
                return "原生Car+命令通道已中断，投屏随源端结束，日志未证明车机或手机整机重启";
            }
            if (RfcommSessionLog.c(str, "RFCOMM_NOTIFICATION_ACK_ENDED_AFTER_ENDPOINT_HANDOFF")) {
                return null;
            }
            if (RfcommSessionLog.c(str, "BLUETOOTH_CONNECT_PERMISSION_REQUIRED", "NEARBY_WIFI_DEVICES_PERMISSION_REQUIRED", "REQUIRED_PERMISSION_MISSING")) {
                return "连接所需的手机权限未具备，流程尚未完成";
            }
            if (RfcommSessionLog.c(str, "reviewed original signer mismatch", "reviewed original APK hash/version mismatch")) {
                return "本机原生组件版本或签名检查未通过，连接已在本机停止";
            }
            if (RfcommSessionLog.c(str, "MFI_CERTIFICATE_INVALID", "MFI_CHALLENGE_SIGNATURE_FAILED")) {
                return "车机证书或新鲜挑战验证未通过，认证已停止，具体校验原因需结合原始记录";
            }
            if (RfcommSessionLog.c(str, "MFI_PIN_MISMATCH", "Certificate confirmation", "TRUST_REQUIRED")) {
                return "车机信任身份需要确认，尚未继续认证";
            }
            if (RfcommSessionLog.c(str, "IAP2_LSP_RANGE_", "IAP2_LSP_INVALID", "IAP2_CONTROL_SESSION_INVALID", "IAP2_CONTROL_SESSION_MISSING")) {
                return "已收到车机蓝牙链路参数，但本机参数检查拒绝，尚未进入证书认证；需核对终态链路数值，不能归为未建立蓝牙通道";
            }
            if (RfcommSessionLog.c(str, "RFCOMM_SELECTED_PEER_UNREACHABLE", "RFCOMM_CONNECT_UNAVAILABLE")) {
                return "尚未建立选中车机的蓝牙协议通道，不能仅凭此判断车机不支持无线连接";
            }
            if (str.contains("RFCOMM_EOF") && str.contains("authenticated=false") && str.contains("rx_bytes=0 tx_bytes=0")) {
                return "蓝牙通道建立后在协议收发前被关闭，尚未进入证书或网络协商，具体关断原因尚未确认";
            }
            if (RfcommSessionLog.c(str, "IAP2_ACK_TIMEOUT_")) {
                return "蓝牙协商消息未获完整确认，尚不能确定车端未响应的具体原因";
            }
            if (RfcommSessionLog.c(str, "startup_input=INCOMPLETE_ASSEMBLY", "IAP2_START_DEADLINE input=INCOMPLETE_ASSEMBLY")) {
                return "启动等待截止时仍有未收齐的数据，尚不能归为车机纯沉默";
            }
            if (RfcommSessionLog.c(str, "startup_input=READER_BACKLOG", "IAP2_START_DEADLINE input=READER_BACKLOG")) {
                return "启动等待截止时仍有读入数据尚未处理，需继续核对收包和处理进度";
            }
            if (RfcommSessionLog.c(str, "IAP2_IDENTIFICATION_AUTH_BOOTSTRAP_DEADLINE", "IAP2_START_DEADLINE")) {
                return this.b ? "蓝牙认证已通过，但车机尚未给出完整启动参数，具体启动前置条件尚未确认" : "蓝牙识别或认证未在期限内完成，具体停点需结合原始记录";
            }
            if (RfcommSessionLog.c(str, "WIFI_CONNECTION_NOT_ESTABLISHED", "WIFI_UNAVAILABLE_OR_CONSENT_DECLINED", "WIFI_SELECTED_NETWORK_LOST")) {
                return "车机无线网络未建立或已丢失，日志不足以直接认定密码错误或用户拒绝授权";
            }
            if (RfcommSessionLog.c(str, "CURRENT_WIFI_ENDPOINT_DISCOVERY_DEADLINE")) {
                return "在用户所选WLAN内未及时发现可用CarPlay服务，尚未进入证书和媒体协商；不能据此认定车机不支持";
            }
            if (RfcommSessionLog.c(str, "CURRENT_WIFI_SELECTED_NETWORK_LOST", "CURRENT_WIFI_SELECTED_NETWORK_CHANGED", "CURRENT_WIFI_LINK_CHANGED_RESTART_REQUIRED", "CURRENT_WIFI_LOCAL_ADDRESS_LOST")) {
                return "用户所选车机WLAN或本机接口发生变化，本次发现已结束，尚不能认定车机认证失败";
            }
            if (RfcommSessionLog.c(str, "CARPLAY_INFO_MODES_UNSUPPORTED", "SCREEN_MODES_APPLICATION_REJECTED")) {
                return "车机资源或能力协商未通过，尚未进入可用投屏状态";
            }
            if (RfcommSessionLog.c(str, "CARPLAY_RESPONSE op=PAIR_SETUP status=455", "CONTROL_RESPONSE_REJECTED op=PAIR_SETUP status=455")) {
                return "车机在首次配对请求返回455，当前状态不接受该配对操作，尚未进入证书和媒体验证；不能据此判断证书过期";
            }
            if (str.contains("CARPLAY_RESPONSE ") && str.matches(".* status=(?:4[0-9]{2}|5[0-9]{2})(?: |$).*")) {
                return "车机在会话协商中返回拒绝状态，具体原因需结合对应步骤";
            }
            if (RfcommSessionLog.c(str, "Native frame rate exceeds receiver maximum")) {
                return "源端画面能力与车机声明不匹配，当前画面适配未完成";
            }
            if (RfcommSessionLog.c(str, "BOOTSTRAP_IOException", "CarPlay connection ended:") && this.i == null) {
                return "本次连接已在当前阶段中断，具体原因尚未确认";
            }
            return null;
        }

        public String c() {
            if (this.d) {
                return "日志记录到应用进程异常，不能据此认定车机整机重启，具体原因需结合异常位置。";
            }
            String str = this.i;
            if (str == null && this.h) {
                str = "会话通道曾中断，正在等待恢复，具体断开原因尚未确认";
            }
            if (str == null && this.f) {
                str = "日志记录到音频输出或声明失败，尚不能据此认定车机实际无声的唯一原因";
            }
            if (str == null && this.g) {
                str = "车机尚未交出主音频资源，请播放音乐后点“恢复车机声音”；画面发送不代表车机实际显示";
            }
            if (str == null && this.e) {
                str = "车机当前占用屏幕资源，投屏仍在等待屏幕交还";
            }
            if (str == null) {
                switch (this.a.ordinal()) {
                    case 0:
                        str = "本段没有足够的连接阶段记录，具体原因尚未确认";
                        break;
                    case AudioSourceCapability.c :
                        str = "仍在等待建立车机蓝牙协议通道，日志结束时尚未完成连接";
                        break;
                    case 2:
                        str = "蓝牙通道已建立，识别和认证尚未完成";
                        break;
                    case AudioSourceCapability.d :
                        str = "蓝牙认证已通过，仍在等待车机返回启动参数，具体前置条件尚未确认";
                        break;
                    case 4:
                        str = "已进入车机无线网络或端点获取阶段，后续连接尚未确认完成";
                        break;
                    case 5:
                        str = "已进入车机会话认证和能力协商，媒体通道尚未确认就绪";
                        break;
                    case 6:
                        str = "车机媒体通道已就绪，仍等待原生Car+画面输出或首帧发送";
                        break;
                    case 7:
                        str = "日志已确认画面从手机发送，车机实际显示和播放效果仍需现场确认";
                        break;
                    default:
                        throw new IncompatibleClassChangeError();
                }
            }
            return (this.c ? "本次已由用户停止；停止前，" : "本次，") + str + "。";
        }

        public  b(int i) {
            this();
        }
    }
}
