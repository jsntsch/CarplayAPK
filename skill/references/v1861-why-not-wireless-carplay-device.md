# v1861 — 为什么车机不把本机识别为 Wireless CarPlay Device

> 输入：**项目内 `docs/Apple-CarPlay-Specification.pdf`**（106 页，本轮全文提取）+ `docs/iAP2-Accessory-Interface-Spec.pdf`
> + 完整固件（system/vendor）+ 社区与 GitHub 开源实现。
> 分级：A = 直证（规范原文 / 字节码 / AOSP 源码）· B = 推断 · C = 待验证。

---

## 0. 结论

**车机在"蓝牙发现层"就把我们过滤掉了，不是在 iAP2 层。** 三条独立证据指向同一处：

1. **规范写死了这道门（A）**：CarPlay over Wireless 的**第一步**是 `BT Inquiry and EIR`（Figure 23-3）；
   Apple 明确规定"支持无线 CarPlay 的 Apple 设备必须在 EIR 里广播 128-bit UUID
   `0x2D8D2466E14D451C88BC7301ABEA291A`"，"**车机以此区别普通蓝牙设备和支持 CarPlay 的 Apple 设备**"。
2. **车机固件里确实有这个 UUID 的判定表（A/B）**：`vendor/lib64/libbssservice.so`（Qualcomm Blue SDK 的 GAP 服务）
   的 `gap.c` 里有一张服务列表，硬编码了 `2d8d2466-e14d-451c-88bc-7301abea291a`，用于设备搜索（`start inquiry`）。
3. **Android 原生协议栈根本发不出这个 UUID（A）**：AOSP `BTM_AddEirService(uint32_t*, uint16_t)` /
   `BTM_GetEirSupportedServices()` 决定本地 EIR 只写 **16-bit** Service Class UUID 列表；
   `listenUsingRfcommWithServiceRecord()` 只写 **SDP**，不影响 EIR。

⇒ 我们的 App 已经把这条 UUID 注册进了 **SDP**（`BtDiscovery.u`），但**SDP ≠ EIR**。
车机在 EIR 里看不到暗号 ⇒ 判定为"普通蓝牙设备" ⇒ 后面所有现象（无 `WirelessCarPlayDevice`、
`1D01` 无 param 24、`SEND=NONE`、不连 `_carplay-ctrl`、不发 WiFi 凭证、HID 组件缺失）**全都是这个判定的下游**。

---

## 1. 规范怎么说（A，`docs/Apple-CarPlay-Specification.pdf`）

### 1.1 判定发生在 EIR（§23.2.5.1.2，原文）

> "Apple devices which support CarPlay over wireless and are discoverable via Bluetooth will advertise
> the following 128-bit UUID: **0x2D8D2466E14D451C88BC7301ABEA291A**, in one of the following EIR data types:
> Incomplete List of 128-bit Service Class UUIDs / Complete List of 128-bit Service Class UUIDs."
>
> "**Accessories may use this information to distinguish between devices with Apple CarPlay enabled and
> general Bluetooth devices.**"

配件侧对应的那条是 `0xEC884348CD4140A29727575D50BF1FD3`（车机放进自己的 EIR，手机据此认定"这台车支持无线"）。

### 1.2 完整时序（Figure 23-3，原文）

```
BT Inquiry and EIR  →  BT Pairing  →  iAP2 Profile Setup  →  iAP2 (WiFi Parameters)
  →  Bonjour Discovery of CarPlay-enabled devices (CarPlay Control)
  →  Primary CarPlay device selection and session initiation (CarPlay Control)
  →  CarPlay Session start  →  iAP2 over CarPlay Start
```

注意两点：

- **`_carplay-ctrl` 的方向是"车机连手机"**：§23.3.1.1 要求配件 `GET /ctrl-int/1/connect`，
  且必须带 `AirPlay-Receiver-Device-ID`（配件的 MAC）。我们 `V160_CTRL_TCP_ACCEPT` 恒 0，
  正是"车机没把我们列进 CarPlay 设备表"的直接后果。
- **WiFi 凭证走 iAP2 over BT**（§23.2.5.2）：SSID / Passphrase / 信道由配件下发 —— 对应 0x5702/0x5703。

### 1.3 规范对"普通蓝牙设备"的定义

§23.2.5 开头："Setup for CarPlay over wireless **begins with Bluetooth discovery**…
The accessory and the Apple device may choose to show **only devices that support CarPlay over wireless
by querying the Bluetooth Extended Inquiry Response (EIR)**."

⇒ 这不是可选优化，是**发现阶段的能力甄别**。

---

## 2. 车机固件里的判定实现（A/B）

### 2.1 判定表在 `vendor/lib64/libbssservice.so`（A）

`libbssservice.so`（2.9 MB，Qualcomm **Blue SDK** 的 BSS GAP 服务，源路径串
`subsystem\WICOME\services\bssservice\src\bss_service\gap\gap.c`）里存在一张 UUID 表：

```
0x254ab0  2d 8d 24 66 e1 4d 45 1c 88 bc 73 01 ab ea 29 1a   ← Apple 设备 CarPlay UUID
0x254ac0  2d 8d 24 66 e1 4d 45 1c 88 bc 73 01 ab ea 29 1a   ← 第二份（另一张表）
0x254ad0  00 00 be 5e …
0x254b00  00 00 11 24 00 00 10 00 80 00 00 80 5f 9b 34 fb   ← HID 1124（说明这是"服务 UUID 列表"格式）
0x254b10  00 00 00 02 …                                        ← 后续更多服务项
```

引用点（A）：

| 地址 | 上下文 |
| --- | --- |
| 0xd9e0e | `gap.c` 的 `AddServiceListParameters()` 附近，紧随日志串 `service_list`（`GCFStruct_AppendStringListFV() for GCF parameter 'service_list'`） |
| 0xddede / 0xdde89 / 0xdde0c | `gap.c` 的 UUID 列表拷贝逻辑（`memcmp` 0x10 字节），按 16/32/128-bit 选不同表 |

同库另有 `CallDeviceSearchCB(): start inquiry(DEVICE_SEARCH_S1)` / `ExecDeviceSearchS1(): start inquiry.` /
`BSS_INQUIRY_RESULT` 等串 ⇒ **这是一条"设备搜索 + 服务列表匹配"的链路**（B：Blue SDK 的 service_list
匹配走对端 EIR/advertising，而非 SDP —— 与规范一致）。

> 对照：**车机自己的 CarPlay UUID（`ec884348…`）在 system/vendor 里一个字节都没有**（全盘 BE/LE 双向扫描）。
> 说明配件侧那条 EIR 不是由这个 Blue SDK 实例发出来的（可能在 HostOS/VIP 里，或本车型直接依赖手机侧判断）。

### 2.2 我们看到的 `1D01` 是"判定结果"，不是"判定过程"（A）

车机的设备登记链是 **binder**，与 iAP2 无关（`v1860` 已证）：

```
binder gm.media.MediaManagerService
  → MediaDeviceManager::deviceNotify()  @0x60f40   {deviceType@+0, action@+0xff}
  → createDevice() @0x673c3  → 只有 "WirelessCarPlayDevice" 分支多一个 NmeString 参数
  → MediaWirelessCarPlay ctor @0x7ee80
```

`MediaAppleInterface` 构造（@0x83748）才决定 `1D01` 里有没有 `param 24`：
`getWirelessCarplayCalibration()` 为真 **且** 设备类型命中，才注入 `wifi_component_meta_entries`。

⇒ 手机侧发什么 iAP2 报文都到不了这里。

### 2.3 iAP2 层根本没有"CarPlay 判定"的消息（A）

车机 iAP2 分派表（`NmeIAP2.cpp` @0x164c00~0x165800，共 42 个 ID）里：

- **没有 `0x4300`（CarPlayAvailability）** —— 全库也无此常量；
- **没有 `0x4E0D`（WirelessCarPlayUpdate）** —— 全库无此常量（社区文档确认 0x4E0D 就是它）；
- **没有 `4E09..4E0E`**（只有 4E03）—— 尽管 `1D01` 把它们列进 RECEIVE。

⇒ 我们对 0x4300 的 12 次强发、对 4E0E 的 5 种载荷猜测，失败原因是**车机没有对应实现**，
不是载荷写错。这条线永久关闭。

---

## 3. 手机侧为什么过不了这道门（A，AOSP 源码）

### 3.1 本地 EIR 的服务列表只有 16-bit

```c
/* stack/include/btm_api.h */
/* p_eir_uuid - bit mask of UUID list for EIR ; uuid16 - UUID 16-bit */
void BTM_AddEirService(uint32_t* p_eir_uuid, uint16_t uuid16);

/* BTM_GetEirSupportedServices() 只会写 HCI_EIR_COMPLETE_16BITS_UUID_TYPE */
uint8_t BTM_GetEirSupportedServices(uint32_t* p_eir_uuid, uint8_t** p,
                                    uint8_t max_num_uuid16, uint8_t* p_num_uuid16);
```

```c
/* bta/sys/bta_sys.h */
void bta_sys_add_uuid(uint16_t uuid16);      /* 唯一的"往 EIR 加 UUID"入口 */
```

- EIR 的服务位图是 **`uint32_t` 位掩码 × 16-bit UUID**，天然装不下 128-bit UUID。
- `BTM_GetEirUuidList(..., uuid_size, ...)` 能**解析** 128-bit（0x06/0x07 段），但**不能构造**。

### 3.2 `listenUsingRfcommWithServiceRecord` 只写 SDP

Android 官方文档原文："该字符串…系统会自动将其写入到设备上的新**服务发现协议 (SDP)** 数据库条目"。
SDP 与 EIR 是两套东西：SDP 是**连上之后**查询的服务数据库；EIR 是**发现阶段**广播的应答数据。

### 3.3 我们代码的现状（A）

`android-project/.../secured/BtDiscovery.java`：

```java
static final UUID s = UUID.fromString("00000000-deca-fade-deca-deafdecacafe");   // iAP2
static final UUID t = UUID.fromString("02030302-1d19-415f-86f2-22a2106a0a77");   // Wireless iAP v2
static final UUID u = UUID.fromString("2d8d2466-e14d-451c-88bc-7301abea291a");   // ★ CarPlay 设备 UUID
...
adapter.listenUsingRfcommWithServiceRecord("CarPlus Wireless Discovery", u);      // 只进 SDP
...
this.g.accept("PHONE_DISCOVERY_RECORDS_READY secure_records=" + n
            + " eir_requested=true radio_confirmed=false");                       // ← 自己承认没在射频侧确认
```

⇒ **`u` 已注册，但只到 SDP。** 而车机要的是 EIR。

---

## 4. 社区与 GitHub 交叉验证（B）

| 来源 | 说法 | 与本文关系 |
| --- | --- | --- |
| CSDN《CarPlay wireless（蓝牙+WiFi）连接方案》等 3 篇 | "Apple 设备接收到车机的 EIR 会检测其中是否含 CarPlay UUID…车机也要求 Apple 设备在自己的广播里带 `0x2D8D2466…`；**车机以此区别普通蓝牙设备和支持 CarPlay 的 Apple 设备**" | 与 §1.1 规范逐字一致 |
| 同上（实测口吻） | "**我测试过，如果修改这个 UUID，车机就无法识别为 CarPlay 设备**" | 说明该 UUID 是硬判据 |
| icode.best《carplay 集成开发之无线连接》 | 明确列出三个 UUID（配件 CarPlay / 配件 iAP2 `0xFFCACADEAFDECADEDEFACADE00000000` / 苹果设备 `0x2D8D2466…`）；"**车机端能够通过扫描蓝牙设备…128bit UUID 来识别该设备支持 CarPlay**"；"原生协议栈不支持 128 bit 解析，需自己新增" | 佐证厂商都要**改协议栈**才能处理这条 UUID |
| GitHub `luka-dev/mib2q-carplay-rgi`（真车机逆向） | iAP2 能力用 **SDP 查询 `…decacafe`**（`Checking iAP2 capability` → `iAP2 supported`）；CarPlay 侧则看 EIR/记录；无线消息 `0x5700/0x5701/0x5702/0x5703` + **`WirelessCarPlayUpdate = 0x4E0D`**；"**规范说手机从 inquiry response 决定无线支持**" | 两条独立判据并存：iAP2 走 SDP（我们已通过），CarPlay 走 EIR（我们过不了） |
| GitHub `f-io/LIVI`、`shilapi/xcertplay`、Gitee `glz/WirelessCarPlay` | 都是**配件侧**实现：必须自建 AP + 蓝牙配对 + EIR/记录 + MFi。它们在 Linux/Android 车机上可以随意改 EIR（root/原生层） | 反证：这条 UUID 只能由协议栈/射频层给，App 层给不了 |

> 综合：**iAP2 能力**由 SDP 探测（我们已过，所以能建 iAP2 会话、能 MFi 认证、能出画面）；
> **CarPlay 身份**由 EIR 判定（我们过不了）。两者互不替代 —— 这正好解释"能投屏但不像 CarPlay 设备"。

---

## 5. 与全部历史现象对账

| 历史现象 | EIR 判定失败能否解释 |
| --- | --- |
| `1D01` 无 `param 24`（WiFi 传输）、无 `18/19/20`（HID） | 能：没建 `WirelessCarPlayDevice` ⇒ metapool 无 wifi/HID 组件 |
| `1D01` 的 `SEND=NONE` | 能：CarPlay 档案未加载，车机没有任何要走的消息 |
| `V160_CTRL_TCP_ACCEPT = 0`（车机从不连 `_carplay-ctrl`） | 能：§23.3.1.1 的 `GET /ctrl-int/1/connect` 只对"CarPlay 设备"发 |
| 强发 `0x5702` 39 次，无 `0x5703` | 能：车机未进 CarPlay 模式，凭证通道未启用（且 `1D01` 未声明 5703 可发） |
| `/command iAPSendMessage` 回 200 但 `rx_bytes=0` | 能：没有 iAP2-over-WiFi transport |
| 有画面、有音频、无触摸 | 能：AirPlay 引擎由 `CARPLAY_USB||CARPLAY_WIRELESS` 放行，与"设备登记"是两条线 |
| 强发 `0x4300` → `PEER_RESTARTED` | 能：车机无此消息实现（与 EIR 无关，但同为"车机侧没有 CarPlay 语义"的证据） |

---

## 6. 如果 EIR 是唯一门，还需要排除的三个次因

1. **配对档案缓存**：车机的 BT 属性（含服务 UUID 列表）通常在**配对/发现时写库**。
   本机与车机是"装 App 之前就配对过"的老关系，档案里没有 CarPlay 标记。
   ⇒ 必须在**车机上删除该手机**、手机"忘记"车机，并在 App 三条 SDP 记录就绪的窗口内重新配对。
2. **时机**：`BtDiscovery` 的记录只保留 **120 s**（`ttl_ms=120000`）。若车机的搜索发生在窗口外，同样看不到。
3. **`_carplay-ctrl` 的 TXT**：规范只定义 `id`（BT MAC）与 `srcvers` 两个键。我们广播 11 个键，
   若 `id` 与车机记的 MAC 不一致（Android 6+ `getAddress()` 返回全 0 的老坑），即使连上也匹配不上。

---

## 7. 怎么办（按代价排序）

| 路线 | 做法 | 代价 / 风险 |
| --- | --- | --- |
| **A0 验证**（先做） | Linux 主机 `bluetoothctl scan on` + `btmon`，手机置为可被发现、App 记录就绪，抓自己的 EIR，看有没有 `0x07 Complete List of 128-bit UUIDs: 1a29eaab…` | 零风险；**这一步能直接判定本文结论** |
| **A1 射频注入** | root 后直接发 HCI `Write_Extended_Inquiry_Response`（OGF 0x03 / OCF 0x0052），写入 0x07 段 | 需 root；与协议栈抢 EIR，需在每次 discoverable 前重写 |
| **A2 改协议栈** | 给 `libbluetooth.so` 的 EIR 构造打补丁（`BTM_GetEirSupportedServices` 之后追加 128-bit 段） | 需 root + 关 verity；OPPO 机型难度高 |
| **A3 外挂射频** | 用可控 EIR 的 BT 模块（Linux/BlueZ / CSR）做桥，手机侧只走 WiFi | 工程量大，但**唯一与手机型号无关** |
| **B 绕过** | 让车机"认为已有 CarPlay 设备" | **不可行**：判定与登记都在车机内部（binder + Java 服务），手机无入口 |

---

## 8. 一句话回答用户

> 车机不是"没认出我们是 CarPlay"，而是**在蓝牙发现阶段就没把我们当候选**：
> 它按 Apple 规范在自己的 Blue SDK 里维护一张服务 UUID 表（含 `2d8d2466…`），
> 只有 EIR 里带这条 128-bit UUID 的手机才会被登记成 `WirelessCarPlayDevice`；
> 而 **Android 原生协议栈的 EIR 只能放 16-bit UUID**，App 能改的只有 SDP —— 所以我们永远差这一步。
> 除非在射频/协议栈层把这条 UUID 塞进 EIR，否则 iAP2 侧再怎么做都不会改变车机的判定。

---

## 附：本轮新增证据索引

| 项 | 位置 |
| --- | --- |
| 规范 EIR 判定原文 | `docs/Apple-CarPlay-Specification.pdf` §23.2.5.1.2 / Figure 23-3（已提取为 `.workbuddy/tmp_img/carplay-spec.txt`） |
| `_carplay-ctrl` 与 `ctrl-int/1/connect` | 同上 §23.3.1.1 |
| 车机 Blue SDK CarPlay UUID 表 | `vendor.img` → `/lib64/libbssservice.so` @0x254ab0（文件内偏移），引用点 0xd9e0e / 0xddede |
| AOSP EIR 只支持 16-bit | `stack/include/btm_api.h`（`BTM_AddEirService` / `BTM_GetEirSupportedServices`）、`bta/sys/bta_sys.h`（`bta_sys_add_uuid(uint16_t)`） |
| 本地实现 | `android-project/.../secured/BtDiscovery.java:45-47, 225-229, 265` |
