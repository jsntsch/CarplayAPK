# -*- coding: utf-8 -*-
"""给重命名后的类注入中文职责注释（javadoc）。

只加注释，不改任何代码 —— 注释内容全部来自 rename_map.CORE 的依据字段，
外加若干人工确认的补充说明（EXTRA）。
"""
import os, re, sys
import rename_map as RM

DST = r"C:/Users/SCH/WorkBuddy/2026-10-24-16-47-09/OcarPlay-Java-V1729/src/main/java"

# 核心类的补充说明（在自动依据之外）
EXTRA = {
    "AirPlaySession": """
 * <p><b>这是整个 App 的心脏。</b>RTSP 会话的 SETUP / INFO / RECORD、
 * POST /command、iAP2 转发、音频/视频流的调度都在这一个类里，
 * 也正因为如此它有 4400 行、300 多个字段，字段名全是 R8 混淆后的单字母。
 *
 * <p>读它的时候按这几个锚点走：
 * <ul>
 *   <li>{@code sendSetup} 附近 —— 构造 SETUP 的 9 个键 + V1.7.28 加的 features 数组</li>
 *   <li>{@code "CARPLAY_SETUP_KEYS"} —— 打印真正发出去的键</li>
 *   <li>{@code "CARPLAY_SETUP_RESP_KEYS"} —— 打印车机回来的键（有没有 eventPort）</li>
 *   <li>{@code eventPort} 读取点 —— 事件通道端口来自 SETUP <b>应答</b>，不是请求</li>
 * </ul>
 *
 * <p><b>不要在这里找 HID。</b>HID 相关字段（HIDMODE / SWEEP）是历史插桩残留，
 * 真正的触摸链路是「车机 → 事件通道 → hidSendReport」，见 EventChannel。
""",
    "EventChannel": """
 * <p>事件通道 = AirPlay 的 RTSP 事件流（车机 → 手机）。触摸、方向盘按键、
 * 车机发来的 {@code POST /command} 全部走这里。它自己实现 RTSP 的
 * 请求/应答解析（cseq、Content-Length、状态行），不依赖外部库。
 *
 * <p>关键点：<b>连上了不等于会收到东西</b>。TCP 建链成功只说明端口对，
 * 车机愿不愿意推事件取决于会话协商出来的 enabledFeatures。
 * 判定「车机到底有没有发过东西」的唯一硬指标是 {@code INBOUND_REQ chan=EVENT} 计数。
""",
    "ControlChannel": """
 * <p>控制通道（iAP2 / ctrl TCP）。车机侧的「邀请」与 {@code /command} 走这条。
 *
 * <p>读它的时候注意 {@code V1711_DIAL_GATE=*} 这一族日志 —— 那是「拨号闸门」的
 * 状态机，用来判断当前卡在「等邀请」还是「已邀请未建链」。
""",
    "CarPlayCtrlAdvertiser": """
 * <p>把手机自己伪装成 {@code _carplay-ctrl._tcp} 服务广播出去，并在注册成功后
 * <b>立刻 resolve 自己一次</b>做自检（验证 mDNS 上真实 SRV 端口）。
 *
 * <p><b>V1.7.29 修的就是这个类：</b>内部类里的回调方法原来叫 {@code onResolved}，
 * 而 {@code NsdManager.ResolveListener} 要求 {@code onServiceResolved}。
 * 因为原厂 resolve 在入口就抛异常（serviceType 为空），这段代码从没执行过，
 * 方法名写错也无人察觉；V1.7.25 修好入口后它第一次被回调，直接
 * {@code AbstractMethodError} 杀进程。详见 docs/v1729-nsd-resolve-crash.md。
""",
    "AirplayServiceProbe": """
 * <p>发现车机的 {@code _airplay._tcp} 服务并从中挑一个候选（candidate）来连。
 * 车机可能同时广播多条记录（不同网卡、IPv4/IPv6、CarPlay 与 AirPlay 并存），
 * 所以这里有「多候选择优」和「拒绝非车机网络」的逻辑。
""",
}


def main():
    idx = {}
    for fq, new, why in RM.CORE:
        pkg, old = fq.rsplit('.', 1)
        idx[(pkg, new)] = (old, why)

    n = 0
    for (pkg, new), (old, why) in idx.items():
        path = os.path.join(DST, pkg.replace('.', os.sep), new + '.java')
        if not os.path.exists(path):
            print('MISSING', path)
            continue
        text = open(path, encoding='utf-8', errors='replace').read()
        if '原混淆名' in text:
            continue
        extra = EXTRA.get(new, '')
        doc = (
            "/**\n"
            " * %s\n"
            " *\n"
            " * <p>原混淆名：{@code %s.%s}（jadx 产物 %s.java）\n"
            " *\n"
            " * <p>命名依据：%s\n"
            "%s"
            " */\n"
        ) % (new, pkg, old, old, why, extra)

        m = re.search(r'/\* JADX INFO: loaded from: classes[\w]*\.dex \*/\n', text)
        if m:
            text = text[:m.end()] + doc + text[m.end():]
        else:
            m2 = re.search(r'^(?=(?:public |final |abstract |class )*class )', text, re.M)
            if not m2:
                print('NO ANCHOR', path)
                continue
            text = text[:m2.start()] + doc + text[m2.start():]
        open(path, 'w', encoding='utf-8', newline='\n').write(text)
        n += 1
    print('annotated', n, 'files')


if __name__ == '__main__':
    main()
