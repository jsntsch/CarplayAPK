# OPPO 手机蓝牙 EIR 注入 128-bit CarPlay UUID — 深入研究

> 输入：AOSP 蓝牙栈源码（`btm_api.h` / `btm_ble_api.h` / `bta_dm_eir.c`）、Zephyr HCI 头、社区与 GitHub 开源实现（ALN / Espressif bluedroid fork）、OPPO/ColorOS Bootloader 解锁政策（2025–2026）。
> 分级：A = 直证（规范 / AOSP 源码 / 字节码）· B = 推断 · C = 待验证。
> 接续：`v1861-why-not-wireless-carplay-device.md`（根因 = EIR 缺 128-bit UUID `2D8D2466…`）。本文回答"**怎么在 OPPO 上把它写进 EIR**"。

---

## 0. 结论（先给答案）

要把 `2D8D2466-E14D-451C-88BC-7301ABEA291A` 塞进 OPPO 的**经典蓝牙 EIR**，只有两条工程路线：

| 路线 | 做法 | 是否需 BL 解锁 / root | OPPO 可行性 |
| --- | --- | --- | --- |
| **A. root + 裸 HCI 注入** | 直接发 `Write_Extended_Inquiry_Response`（opcode `0x0C52`）写 0x07 段 | 需 root（临时，不需要解锁 BL 的某些 exploit 也能 root，但 OPPO 基本没有） | ★★★ 最快验证假设，**但会被栈周期性覆盖**，需循环重发 |
| **B. Magisk 模块 patch 蓝牙库** | 在 `BTM_WriteEIR` / `bta_dm_set_eir` 处插入 0x07 段，每次栈重写 EIR 都带上 | 需 BL 解锁 + Magisk | ★★ 生产级，但 OPPO 是**高通 QTI 蓝牙库**，不能直接套用 ALN 的 patch，必须自行逆向 QTI 的 EIR 构造 |
| C. 外挂可控 BT 模块 | 手机只走 WiFi，外挂 BlueZ/CSR 模块做发现 + EIR | 不需要动手机 | ★ 工程量最大，但**与机型无关**、最稳 |

**OPPO 的真正门槛不是技术，是 Bootloader 解锁（"极难解锁"）**：ColorOS 需走"深度测试"申请，ColorOS 16 起国行机型还要服务端签名 + 等待期。拿不到 BL 解锁 ⇒ 装不了 Magisk ⇒ 路线 B 直接死；路线 A 也基本需要 root（OPPO 无公开 root exploit）。

⇒ **第一步永远是先确认这台 OPPO 能不能解锁 BL、芯片是高通还是联发科、ColorOS 版本**。以下路线假设你已 root/解锁。

---

## 1. EIR 协议与这条 UUID 的精确编码（A）

### 1.1 为什么是经典 EIR，不是 BLE 广播
CarPlay over Wireless 的发现阶段是 **BR/EDR Inquiry + EIR**（规范 §23.2.5.1.2 / Figure 23-3，v1861 已证）。车机的 Blue SDK 在 `start inquiry` 时读对端的 **EIR**（不是 BLE advertising，也不是 SDP）。所以我们要改的是**经典蓝牙 EIR**，对应 HCI 命令 `Write_Extended_Inquiry_Response`，**不是** `LE Set Advertising Data`。

### 1.2 目标 HCI 命令（A，Zephyr 头直证）
```
OGF  = 0x03 (Control and Baseband)
OCF  = 0x0052
Opcode = (0x03 << 10) | 0x0052 = 0x0C52
```
参数布局（共 241 字节）：
```
byte 0        : FEC_Required = 0x00  (数据不需要 FEC 编码)
byte 1..240   : Extended Inquiry Response Data (240 字节，不足的以 0 填充)
```
EIR 数据上限 `BT_HCI_EIR_MAX_DATA_LEN = 240`（Zephyr `hci_types.h` 直证）。

### 1.3 EIR TLV 结构（A，蓝牙核心规范 Vol 3 Part C §8）
每个数据单元 = `[Length][Data Type][Data]`：
- `Length` = 后续字节数（含 Data Type 本身）
- `Data Type`：
  - `0x02/0x03` = 16-bit UUID（不完整/完整列表）
  - `0x06/0x07` = **128-bit UUID（不完整/完整列表）** ← 我们要用 `0x07`
  - `0x08/0x09` = 设备名（缩短/完整）
  - `0xFF` = 厂商自定义

### 1.4 这条 UUID 的精确字节串（A，关键！）
规范规定 128-bit UUID 在 EIR 中按 **little-endian（最低有效字节在前）** 存储，即把规范的 16 字节网络序**整体反转**。

```
规范串 : 2d8d2466-e14d-451c-88bc-7301abea291a
网络序 : 2d 8d 24 66 e1 4d 45 1c 88 bc 73 01 ab ea 29 1a
EIR LE : 1a 29 ea ab 01 73 bc 88 1c 45 4d e1 66 24 8d 2d
```
（注意 `1a 29 ea ab …` 与 v1861 里"`1a29eaab…`"的判据完全吻合，反向印证编码正确。）

**完整 TLV（一个 UUID 的 0x07 段）：**
```
11 07 1a 29 ea ab 01 73 bc 88 1c 45 4d e1 66 24 8d 2d
│  │  └───────────────────────────────────────────── 16 字节 UUID(LE)
│  └─ Data Type = 0x07 (Complete List of 128-bit Service Class UUIDs)
└──── Length = 17 (1 type + 16 data)
```

**完整 HCI 载荷（FEC + 240 字节 EIR，TLV 打头、其余 0 填充）：**
```
00 11 07 1a 29 ea ab 01 73 bc 88 1c 45 4d e1 66 24 8d 2d
[+ 223 字节 0x00 填充到 240]
```
`hcitool` 等效命令（见 §6.1 脚本）：
```
hcitool cmd 0x03 0x0052 0x00 0x11 0x07 0x1a 0x29 0xea 0xab 0x01 0x73 0xbc 0x88 0x1c 0x45 0x4d 0xe1 0x66 0x24 0x8d 0x2d [+223×0x00]
```

### 1.5 前置动作：手机必须处于可发现（discoverable）
EIR 只在 inquiry scan（可发现）时被回送。务必先：
```
adb shell hciconfig hci0 piscan     # 可连接 + 可发现
# 或 Android API: BluetoothAdapter.setScanMode(SCAN_MODE_CONNECTABLE_DISCOVERABLE)
```
且 App 的 `BtDiscovery` RFCOMM 记录（`s/t/u` 三条 SDP）必须已就绪（v1861 §3.3 现状）。

---

## 2. 为什么 App 层做不到 / AOSP 栈的硬限制（A，源码直证）

### 2.1 `BTM_AddEirService` 只收 16-bit（A）
`stack/include/btm_api.h`：
```c
void BTM_AddEirService(uint32_t* p_eir_uuid, uint16_t uuid16);   // 位掩码 × 16-bit
uint8_t BTM_GetEirSupportedServices(uint32_t*, uint8_t**, uint8_t max_num_uuid16, uint8_t*);
```
EIR 服务位图是 `uint32_t` 位掩码，**装不下 128-bit**。`BTM_GetEirUuidList()` 能**解析** 0x06/0x07 段（`HCI_EIR_COMPLETE_128BITS_UUID_TYPE`），但**不能构造**。

### 2.2 EIR 由栈周期性重写，App 改不了（A）
EIR 构造链：`bta/dm/bta_dm_eir.c` 的 `bta_dm_set_eir()` → 拼装 16-bit 段 → `BTM_WriteEIR()` → 发 `0x0C52`。该函数在以下事件被重调（A，社区 BlueDroid 分析 `waresun/docs`）：
- 蓝牙开启
- **可发现模式变化**（`setBluetoothDiscoverable`）
- 服务启停（`bta_sys_add_uuid`）
- 本地名变化

⇒ 即便你用 `hcitool` 裸发一条 0x0C52，**栈下一次刷新 EIR 就把它覆盖回 16-bit 版本**。这是路线 A（裸 HCI）必须循环重发的根本原因；也是路线 B 必须 hook `BTM_WriteEIR`/`bta_dm_set_eir`、而不是只发一次的根本原因。

### 2.3 `listenUsingRfcommWithServiceRecord` 只写 SDP（A，v1861 §3.2）
我们 App 的 `u`（`2d8d2466…`）已注册，但只进 **SDP** 数据库（连接后查询），**不进 EIR**。车机在发现阶段读 EIR，根本不查 SDP ⇒ 看不到这条 UUID。

### 2.4 反例：Espressif bluedroid fork 确实加了 128-bit 支持（B/A）
GitHub `espressif/esp-idf` commit `5b8abb6` 给 bluedroid 新增了 `BTM_AddCustomEirService(uuid128)` 与 `bta_dm_cb.custom_uuid[]` 数组，在 `bta_dm_set_eir()` 里把 32/128-bit UUID 追加进 EIR。**但 AOSP 手机栈（fluoride）没有这套**，所以手机原生 EIR 永远是 16-bit。

---

## 3. OPPO 的特殊性（ColorOS + 高通 QTI 蓝牙栈）（B）

### 3.1 蓝牙库位置
OPPO 几乎全部采用**高通**平台（少量联发科天玑）。其蓝牙库是 QTI 变体，路径通常是：
```
/system/lib64/libbluetooth_qti.so
/system_ext/lib64/libbluetooth_qti.so
```
而非 AOSP 通用的 `/apex/com.android.btservices/lib64/libbluetooth_jni.so`（ALN 默认目标）。

### 3.2 APEX vs system 分区
- 现代 Android（10+ Mainline）：蓝牙在 `/apex/com.android.btservices/...`，只读 ⇒ 用 Magisk 的 **overlay 挂载**（`post-data-fs.sh`）替换，不碰只读 APEX（ALN 做法，已验证）。
- OPPO 若把蓝牙编进 `/system` 或 `/system_ext`（QTI 常见），则 Magisk 模块直接 overlay 对应路径即可。

### 3.3 与 ALN 项目对比（A，关键难点）
GitHub `kavishdevar/aln`（AirPods L2CAP PSM 0x1001 补丁）是**同构先例**：Magisk 模块 + radare2 二进制 patch 蓝牙库 + APEX overlay。但它**明确拒绝 patch 高通 QTI 库**：
> "Qualcomm QTI Bluetooth libraries cannot be patched." / "libbluetooth_qti.so ... not supported ... cause installation abortion."

原因：ALN 的 patch 是针对 `libbluetooth_jni.so` 里 `l2c_fcr_chk_chan_modes` / `l2cu_send_peer_info_req` 的特定字节，QTI 库里这两个函数签名/偏移不同。

**对我们的启示**：ALN 的"拒绝 QTI"是**它那条 patch 的局限性，不是根本不可能**。我们要做的是**自己逆向 OPPO 的 `libbluetooth_qti.so`，定位其 EIR 构造函数（等价 `bta_dm_set_eir` / `BTM_WriteEIR`），把"追加 0x07 段"插进去**。工作量比 ALN 大，但技术路线完全一致。

---

## 4. 可行路线详解（按代价与风险排序）

### 路线 A：root + 裸 HCI 注入（最快验证假设）
- **原理**：root 后直接用 `hcitool cmd 0x03 0x0052 …` 或原生 HCI socket 发 0x0C52（见 §6.1）。
- **致命问题**：栈会覆盖（§2.2）。对策：**在车机搜索窗口内循环重发**（每 100–300 ms 一次，持续 30–60 s），并锁定 discoverable。因为 CarPlay 发现是一次性配对/连接事件，窗口注入足以验证"EIR 假说"（对应 v1861 §7 的 A0+A1）。
- **风险**：中。可能短暂影响其他 BT 功能；需 root。OPPO 无公开 root exploit ⇒ 实际仍需 BL 解锁或临时漏洞。
- **价值**：**零成本验证 v1861 结论**——若注入后车机认出 CarPlay 设备，根因坐实，再投入路线 B。

### 路线 B：Magisk 模块 hook 蓝牙库（生产级）
- **原理**：在 `BTM_WriteEIR`（或 `bta_dm_set_eir`）处，把 §1.4 的 0x07 TLV 追加进 EIR 缓冲，再交给控制器。每次栈刷新都带上 ⇒ 永久生效。
- **实现**（套用 ALN 框架）：
  1. `customize.sh` 探测目标库：`/apex/com.android.btservices/lib64/libbluetooth_jni.so` → `/system/lib64/libbluetooth_qti.so` → `/system_ext/lib64/libbluetooth_qti.so`。
  2. 用 radare2 在目标库里定位 EIR 构造函数的符号/特征字节（QTI 版需自行找，不能直接用 ALN 的偏移）。
  3. 二进制 patch：在函数返回前，把我们的 0x07 TLV 写入 EIR 缓冲（注意 240 字节上限、与已有 0x09 设备名段共存、长度字段重算）。
  4. APEX 用 `post-data-fs.sh` overlay 挂载；system 路径直接替换。
- **风险**：高。**最大难点 = QTI 库逆向**（ALN 没做）。patch 写错会让蓝牙彻底起不来，需准备卸载开关（Magisk disable module）。
- **依赖**：BL 解锁 + Magisk。**OPPO 解锁极难（§5）** ⇒ 这是整条路线的命门。

### 路线 C：外挂可控 BT 模块（与机型无关）
- **原理**：用一块跑 BlueZ/CSR 固件的 Linux/ESP32 模块，烧入带 0x07 段的 EIR，模拟"苹果设备"被发现；手机侧只负责 WiFi + iAP2 over WiFi（我们已经能做，v1860/v1861 证 iAP2 能力已过）。
- **风险**：工程量大（要重构连接拓扑：模块做 BT 发现/配对，手机做 WiFi 承载），但**不依赖 OPPO 任何系统层**，最稳、最合规。
- **适用**：当你确认 OPPO 解锁无望、又不接受 root 风险时。

### 路线 D：改协议栈源码重编（基本不可行）
- 给 AOSP fluoride 加 `BTM_AddCustomEirService`（仿 Espressif），重编 `libbluetooth` 并替换。需 BL 解锁 + 关 verity + 重编系统蓝牙 ⇒ 在 OPPO 上代价与风险远大于 Magisk 二进制 patch（路线 B），**不推荐**。

---

## 5. OPPO 解锁 Bootloader 这道硬门槛（A，2025–2026 政策）

来源：XDA、Bootloader Unlock Wall of Shame（2025–12）、一加/ColorOS 16 官方公告、CSDN 汇总。

| 品牌 | 评级 | 政策 |
| --- | --- | --- |
| **OPPO** | **极难解锁** | 需申请**深度测试**；等待周期长；**部分机型完全不允许** |
| realme（同门） | 需谨慎 | 大部分国行可申深度测试，名额有限、有等待期 |
| 小米/Redmi | 极难 | 账号等级 + 绑定时长 + 等待期 + 严格风控 |
| 一加（同门，海外） | 需谨慎 | 官方允许，但 ColorOS 16 起也改深度测试；**国行 ColorOS 16 需服务端签名 + 等待** |
| vivo/iQOO、华为、荣耀 | 不可解锁 | 官方通道全关 |

**ColorOS 16 新政（2025-08 起）**：国行机型解锁 BL 必须走"深度测试"且**服务端签名 + 等待期**；之前的 ColorOS 15 及更早版本不受影响、无服务端签名等待。**结论**：若你的 OPPO 还在 ColorOS 15 且能申深度测试 ⇒ 尽快解锁；ColorOS 16 国行 ⇒ 难度显著上升，且随时可能收紧。

⇒ **没有 BL 解锁，路线 B 与 D 直接死亡，路线 A 也几乎拿不到 root。** 这是排在第一位的先决条件。

---

## 6. 落地步骤（PoC → 生产）

### 6.1 PoC 脚本：root + 裸 HCI 注入（见 `skill/scripts/eir_inject.sh`）
```bash
# 需 root 设备，且 hcitool 可用（BlueZ 工具；OPPO 原厂没有，需 busybox/toybox 或自行编译）
# 置可发现
adb shell hciconfig hci0 piscan
# 注入（FEC=0 + 240B EIR，0x07 段打头，其余 0 填充）
adb shell hcitool cmd 0x03 0x0052 \
  0x00 0x11 0x07 0x1a 0x29 0xea 0xab 0x01 0x73 0xbc 0x88 0x1c 0x45 0x4d 0xe1 0x66 0x24 0x8d 0x2d \
  $(printf '0x00 %.0s' $(seq 1 223))
# 循环重发（车机搜索窗口内）
for i in $(seq 1 200); do <上面 hcitool 命令>; sleep 0.15; done
```
判据：注入期间用车机/另一台 `bluetoothctl scan on` + `btmon` 抓本机 EIR，确认出现 `0x07 … 1a 29 ea ab …`；再看车机是否把手机登记为 CarPlay 设备。

### 6.2 Magisk 模块骨架（生产级，需逆向 QTI）
目录结构（`eir-carplay-module/`）：
```
module.prop            # id=eir-carplay, name=CarPlay EIR 128-bit Injector
customize.sh           # 探测库路径 + radare2 patch + overlay 挂载
post-data-fs.sh        # APEX overlay 挂载 patched lib
radare2-android.tar.gz # Android aarch64 radare2
```
`customize.sh` 核心逻辑（伪代码，套 ALN）：
```bash
# 1. 探测目标库（优先级：QTI system_ext > QTI system > APEX jni）
for lib in /system_ext/lib64/libbluetooth_qti.so \
          /system/lib64/libbluetooth_qti.so \
          /apex/com.android.btservices/lib64/libbluetooth_jni.so; do
  [ -f "$lib" ] && TARGET="$lib" && break
done
# 2. radare2 定位 EIR 构造函数的追加点（QTI 需自行找符号/特征）
#    目标：在 BTM_WriteEIR 返回前，把 0x07 TLV 写进 EIR 缓冲，重算长度
# 3. 二进制 patch（示例字节需按实际偏移生成，不能直接抄 ALN）
# 4. APEX 走 overlay；system 直接覆盖
```
**与 ALN 的本质差异**：ALN 拒 QTI、且 patch 的是 L2CAP 函数；我们**必须支持 QTI** 且 patch 的是 **EIR 构造**。这部分没有现成字节可抄，需对 OPPO 具体机型的 `libbluetooth_qti.so` 做逆向（参考项目里 `.workbuddy/scripts/` 的 `fwfind.py`/`dexread.py` 思路，但对象是 ELF/SO 而非 dex）。

---

## 7. 风险与注意事项

1. **BL 解锁 = 第一前提**：OPPO 极难，先确认机型/ColorOS 版本能否解锁，否则路线 B/D 免谈。
2. **QTI 逆向是硬骨头**：ALN 明确不碰 QTI；OPPO 几乎全是 QTI ⇒ 我们自己逆向 EIR 构造，工作量大。
3. **栈覆盖问题**：路线 A 必须循环重发；路线 B 必须 hook `BTM_WriteEIR` 而非一次性写。
4. **240 字节上限 + 段共存**：追加 0x07 段时，必须保留已有的 0x09（设备名）等段，并重算各段 Length、不超 240。
5. **配对档案缓存**（v1861 §6）：即使 EIR 成功，车机若"以前配对过"且档案里无 CarPlay 标记，仍不认。需在车机删除本机、手机"忘记"车机，并在 App SDP 记录就绪窗口内重配对。
6. **变砖风险**：路线 B 的 patch 写错会让蓝牙起不来 ⇒ Magisk 模块必须带一键禁用开关，且先在小范围验证。
7. **合规性**：改 EIR 是本地射频行为，不改变任何加密/认证；但 BL 解锁会使保修/部分功能失效（相机等），需用户知情同意。

---

## 8. 结论与下一步建议

1. **先验证，再投入**：用路线 A 的循环注入 PoC（§6.1）在已 root 的 OPPO 上跑一次，确认车机因 EIR 多出的 0x07 段而把手机登记为 CarPlay 设备。**这一步零成本高价值，直接坐实 v1861 根因。**
2. **确认解锁可行性**：查清这台 OPPO 的机型 + ColorOS 版本 + 芯片（高通/联发科），判断 BL 能否解锁。能解锁才值得做路线 B。
3. **路线 B 的工程化**：若解锁可行，基于 ALN 框架 fork 一个 QTI 版模块，**重点投入 `libbluetooth_qti.so` 的 EIR 构造逆向**（定位等价 `bta_dm_set_eir`/`BTM_WriteEIR`，注入 0x07 TLV）。这是唯一"手机侧干净解决"的路。
4. **兜底路线 C**：若解锁无望，评估外挂可控 BT 模块方案（手机只做 WiFi 承载），与机型彻底解耦。

> 一句话：**技术上 128-bit UUID 进 EIR 只有"裸 HCI 注入"和"patch 蓝牙栈"两招；OPPO 上真正的拦路虎是 Bootloader 解锁（极难）+ 高通 QTI 库必须自行逆向。先 root 跑一次循环注入验假说，再决定是否投入 Magisk 模块。**

---

## 附：关键证据索引

| 项 | 位置 |
| --- | --- |
| 128-bit UUID 在 EIR 中 little-endian 编码 | 蓝牙核心规范 Vol 3 Part C §8.1.4.3（Service Class UUID → LE） |
| `Write_Extended_Inquiry_Response` opcode 0x0C52 | Zephyr `hci_types.h`（`BT_HCI_OP_WRITE_EXT_INQUIRY_RESPONSE /* 0x0c52 */`）；OGF 0x03 / OCF 0x0052 |
| EIR 类型常量 0x07=128-bit complete | AOSP `stack/include/btm_ble_api.h`（`BTM_BLE_AD_TYPE_128SRV_CMPL = HCI_EIR_COMPLETE_128BITS_UUID_TYPE = 0x07`） |
| AOSP EIR 只支持 16-bit | AOSP `stack/include/btm_api.h`（`BTM_AddEirService(uint32_t*, uint16_t)`） |
| bluedroid 已加 128-bit 支持（反例） | `espressif/esp-idf` commit `5b8abb6`（非手机 fluoride） |
| 同构 Magisk patch 先例（拒 QTI） | GitHub `kavishdevar/aln` + `jay-tau/aln`（radare2 patch `libbluetooth_jni.so`，明确不碰 `libbluetooth_qti.so`） |
| OPPO BL 解锁政策 | XDA + Bootloader Unlock Wall of Shame 2025-12 + ColorOS 16 官方公告 |
| 本地实现现状 | `android-project/.../secured/BtDiscovery.java:45-47`（UUID `u` 仅进 SDP） |
