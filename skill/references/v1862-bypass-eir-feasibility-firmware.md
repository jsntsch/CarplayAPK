# v1862 — 绕过蓝牙 EIR 判定使车机认可 CarPlay 设备并响应触摸：固件可行性结论

> 级别：**A（固件直证）**。所有结论均来自 `system/bin/MediaManagerService`（MMS）、`GMSmartConnect.odex` / `GMCarPlay.odex` 的反汇编与字符串证据，地址与指令可复现。
> 前置：v1861 已锁定根因 = 手机 EIR 缺 128-bit CarPlay UUID `2D8D2466-E14D-451C-88BC-7301ABEA291A`，导致车机 BT 栈在 inquiry 阶段就把本机过滤为普通 Apple 设备。本报告回答唯一遗留问题：**这个 EIR 判定是不是唯一闸门？固件里有没有别的入口（iAP2 身份 / WiFi-mDNS / 配对后手动选择 / 车机 App 手动添加）可以替代或绕过它？**

---

## 1. 一句话结论

**EIR 128-bit UUID 判定是车机认可「Wireless CarPlay 设备」的唯一上游闸门，在固件内不可绕过。**

MMS 层（`MediaManagerService`）**从不**根据手机侧后续发来的任何 iAP2 消息、WiFi/mDNS 广播、配对记录或 App 层指令去「合成」或「升级」设备类型为 `WirelessCarPlayDevice`。它只是车机 BT 栈（vendor 层 EIR 分类）结果的**消费者**：BT 栈在 inquiry 阶段依据 EIR 里的 CarPlay UUID 把设备归类为 `"WirelessCarPlayDevice"` 后，通过 binder `deviceNotify` 把这一**类型串**交给 MMS，MMS 才据此建立无线 CarPlay 设备对象、打开 WiFi/HID 组件、开放触摸反向通道。

因此：**手机侧唯一可行解仍是 v1861 / oppo-eir-128bit-uuid-rewrite 路线——在射频层把 128-bit UUID 注入 EIR。** 不改 EIR，固件内任何下游动作都救不回触摸。

---

## 2. 固件证据链（从上游到触摸）

### 2.1 设备类型分发：`deviceNotify` 比对的是「上游传来的类型串」
`MediaManagerService.carplay.txt` 中 `WirelessCarplay Device connected`(line 440)、`WirelessCarPlayDevice`(line 441)、`Wireless Car Play Device Removed`(line 438) 均出自同一分发函数（xref_gate3.out @ 0x68b89–0x68f6b）。该函数 `repe cmpsb` 依次比对入参类型串：

```
0x68b89  mov  rsi, [rsp+8]            ; 入参 deviceType 串
0x68b8e  mov  ecx, 0xc               ; len("AppleDevice")=12
0x68b93  lea  rdi, "AppleDevice"
0x68b9a  repe cmpsb ; jne 0x68d6c
...
0x68bc0  mov  ecx, 0x16              ; len("WirelessCarPlayDevice")=22
0x68bc5  lea  rdi, "WirelessCarPlayDevice"
0x68bcc  repe cmpsb ; jne 0x69405
0x68bd4  ...                        ; 再比对 action 是否 "remove"
0x68ee1  lea  rdx, "Wireless Carplay Device connected"
0x68f05  mov  byte ptr [r15+0xc8], 1 ; 置 isWirelessCarPlay 标志
```

要点：
- 分发逻辑**只认字符串**，而这个字符串的来源是 binder 调用方（车机 BT 栈），**不是手机经 iAP2/WiFi 传来的任何字段**。
- 只有 `deviceType == "WirelessCarPlayDevice"` 且 `action != "remove"` 时才打印 `Wireless Carplay Device connected` 并置 `[r15+0xc8]=1`。本机现场**从未**出现该日志（v1861 已确认），说明 BT 栈根本没把本机归类成这个串——即 EIR 判定的下游症状。
- 识别到的设备类型集合只有：`AppleDevice` / `WirelessCarPlayDevice` / `CarPlayDevice` / `MTPDevice` / `BTSADevice`。其中 `WirelessCarPlayDevice` 只能由 BT 栈产生。

### 2.2 设备对象创建：`createDevice` 同样只认 `"WirelessCarPlayDevice"` 串
xref_gate3.out @ 0x67c55（createDevice 分支）：

```
0x67c55  lea  rsi, "WirelessCarPlayDevice"
0x67c5f  call 0x64040              ; strcmp
0x67c66  jne  0x67d15             ; 不是该串 → 跳过，不建 WCP
0x67c6c  test byte ptr [rsp+0x108], 1
0x67c74  je   0x67d00             ; 标志未置 → 跳过
...
0x67cca  call 0x7ee80             ; MediaWirelessCarPlay ctor
```

要点：建立无线 CarPlay 设备对象（`MediaWirelessCarPlay`，ctor @ 0x7ee80）**唯一触发条件**就是传入类型串等于 `"WirelessCarPlayDevice"`。没有任何分支允许「先建普通 Apple 设备、再靠后续消息升级为 WCP」。

### 2.3 触摸闸门：WiFi/HID 组件只在 `mEnableWIFIComponent` 置位时注入
证据一——标志位初值 0（xref_gate3.out @ 0x83748，设备基类 ctor 区）：

```
0x8374f  jne  0x83828
0x8375c  mov  byte ptr [rbp+0x1c0], 0   ; mEnableOOBBTPairing = 0
0x83766  mov  byte ptr [rbp+0x1c1], 0   ; mEnableWIFIComponent = 0
```

证据二——`addToMetapool` 以 `+0x1c1` 为闸（xref_gate3.out @ 0x812d3）：

```
0x812de  cmp  byte ptr [rax+0x1c1], 0    ; mEnableWIFIComponent
0x812e5  jne  0x814f5                   ; !=0 → 跳到「Add WIFI component meta entries」
0x812eb  cmp  byte ptr [rax+0x1c0], 0    ; mEnableOOBBTPairing
0x812f7  je   0x813b0                   ; ==0 → 跳过 OOB BT 配对
```

对应字符串（MediaManagerService.carplay.txt）：
- line 217 `MMS: Add WIFI component meta entries`
- line 333 `MMS:addToMetapool(wifi_component_meta_entries) Failed`
- line 457 `m_deviceType = %s, mEnableOOBBTPairing = %d, mEnableWIFIComponent = %d`

要点：`mEnableWIFIComponent(+0x1c1)` 为 0 时，`addToMetapool` 直接跳过 WiFi 组件与随之的 HID 组件注入——这正是「有画面无触摸」的固件侧机理解释（HID 反向通道组件从来没被加进 metapool）。该标志默认 0，仅在设备被判定为 `WirelessCarPlayDevice` 的建链路径中才会被置 1。

### 2.4 完整链条（A 级闭环）
```
[手机 inquiry 广播 EIR]
   └─ 含 128-bit CarPlay UUID 2D8D2466… ?  ──(否)──► 车机 BT 栈归类为普通 AppleDevice
                                                   │ (从来不调 deviceNotify("WirelessCarPlayDevice","add"))
   └─(是)► 车机 BT 栈(vendor/lib64/libbssservice.so, UUID 表@0x254ab0) 归类为 "WirelessCarPlayDevice"
            └─ binder deviceNotify("WirelessCarPlayDevice","add")  → MMS 0x68b89 分发
                 ├─ 打印 "Wireless Carplay Device connected"、置 [r15+0xc8]=1
                 └─ createDevice 0x67c55 匹配串 → call MediaWirelessCarPlay ctor 0x7ee80
                      └─ 置 mEnableWIFIComponent(+0x1c1)=1 / mEnableOOBBTPairing(+0x1c0)=1
                           └─ addToMetapool 0x812de 见 +0x1c1≠0 → 加 WiFi/HID 组件 meta entries
                                └─ 触摸反向通道(HID)可用 ✅
```

我们卡在第一步（EIR 无 UUID）→ 整条链从未启动。

### 2.5 App 层无旁路：车机应用不持有「无线 CarPlay 设备」类型
对 `GMSmartConnect.odex`、`GMCarPlay.odex` 反编译产物（analysis/*.carplay.txt）检索：
- GMSmartConnect 仅引用 `Lgm/media/utilities/devices/CCarPlayDevice;`（USB CarPlay 设备类）、若干校准枚举（`Apple_CarPlay_touchFidelity`、`Enable_Application_Apple_Carplay_CalType` 等）。**无 `WirelessCarPlayDevice` 类型、无 `deviceNotify`/`createDevice` 调用、无手动注册无线设备的任何 API。**
- GMCarPlay.odex 仅有 `isCarPlayWirelessEnabled=` 与「Device does not support wireless projection」等**消费型**判断，不生成设备类型。

⇒ 车机 App 只消费 MMS 已建好的设备对象，**没有手动把手机「加成」无线 CarPlay 设备的入口**。

---

## 3. 四条候选「绕过路径」逐一证伪

| # | 候选绕过 | 固件证据 | 判定 |
|---|---------|---------|------|
| 1 | 手机发 iAP2 Identification / `0x4300` / `0x4E0D` 让车机「升级」设备类型 | iAP2 分派表（MEMORY.md §4，NmeIAP2.cpp 0x164c00~0x165800，42 个 ID）**无 0x4300、无 4E09..4E0E**（只有 4E03）；`deviceNotify` 由 BT 栈 binder 调用，iAP2 处理路径不调用它 | ❌ 不可行 |
| 2 | 手机主动广播 `_carplay-ctrl._tcp` / mDNS 让车机「发现并认」 | mDNS/ctrl 广播是「已被认作 CarPlay 设备」之后的**下游动作**（规范 §23.2.5）；`createDevice` 不会因收到 ctrl 广播而建 WCP 对象 | ❌ 不可行 |
| 3 | 车机 App 手动「添加无线 CarPlay 设备」 | GMSmartConnect/GMCarPlay.odex 无 `WirelessCarPlayDevice` 类型、无注册 API（§2.5） | ❌ 不可行 |
| 4 | 改校准项 `Apple_CarPlay_enableWireless` 让车机放行 | 该项是**车辆级总闸**（getWirelessCarplayCalibration line 310）：关→完全禁用无线；开→仍须 EIR 分类为 WirelessCarPlayDevice 才建链。它是「是否允许无线」的开关，**不是设备类型分类的替代**——缺 EIR 时开了也只认成普通 Apple 设备 | ❌ 不替代 EIR |

---

## 4. 根因再确认（与 v1861 一致，级别 A）

- 车机 BT 栈 `vendor/lib64/libbssservice.so`（Qualcomm Blue SDK）持有 CarPlay UUID 表 @0x254ab0，在 `BT Inquiry and EIR`（规范 §23.2.5.1.2 / Figure 23-3）阶段依据 EIR 中的 128-bit UUID `2D8D2466-…` 区分 CarPlay 设备与普通蓝牙设备。
- AOSP `BTM_AddEirService(uint32_t*,uint16_t)` 只支持 16-bit UUID；`listenUsingRfcommWithServiceRecord()` 只写 SDP ⇒ 本机 EIR 永远装不进该 128-bit UUID ⇒ BT 栈永远不产出 `"WirelessCarPlayDevice"` 分类 ⇒ MMS 整条链不启动。
- ⇒ **iAP2 / EA / HID / WiFi 全是 EIR 判定的下游**；手机侧不碰射频层（注入 EIR），无解。

---

## 5. 结论与唯一解

1. **EIR 判定不可绕过**（固件内）：没有任何 iAP2 消息、mDNS 广播、配对状态或车机 App 操作能把普通 Apple 设备「升级」成无线 CarPlay 设备并打开触摸。
2. **唯一可行解 = 手机侧 EIR 注入**：在蓝牙 inquiry 广播里写入 128-bit CarPlay UUID `2D8D2466-E14D-451C-88BC-7301ABEA291A`。具体路线见 `oppo-eir-128bit-uuid-rewrite.md`（`eir_inject.sh`）。
3. **验证判据（EIR 注入生效后再回车机）**：车机 logcat 出现 `Wireless Carplay Device connected` + `MMS: Add WIFI component meta entries`；本机 `bluetoothctl scan` + `btmon` 抓到 LE 形式 `1a29eaab…` 即证明 EIR 已带 UUID。届时触摸链路 `touch=1 → HID_REPORT_ARRIVED → TOUCH_FORWARDED` 才会打通。

---

## 6. 证据索引

| 证据 | 位置 |
|------|------|
| 设备类型分发 `WirelessCarPlayDevice` 串比对 | xref_gate3.out @ 0x68b89–0x68f6b（尤其 0x68bc5 / 0x68ee1 / 0x68f05） |
| createDevice 仅认该串 | xref_gate3.out @ 0x67c55–0x67cca |
| 标志位初值 0 | xref_gate3.out @ 0x8375c / 0x83766 |
| addToMetapool 以 +0x1c1 为闸 | xref_gate3.out @ 0x812de–0x812f7 |
| 关键日志串 | MediaManagerService.carplay.txt line 217/333/438/440/441/457 |
| App 层无 WirelessCarPlayDevice 类型 | GMSmartConnect.carplay.txt line 152；GMCarPlay.odex.carplay.txt line 6/36 |
| iAP2 无 0x4300/4E0D 分派 | MEMORY.md §4（NmeIAP2.cpp 分派表 42 ID） |
| BT 栈 UUID 表 / AOSP EIR 16-bit 限制 | v1861-why-not-wireless-carplay-device.md |
| EIR 注入路线 | oppo-eir-128bit-uuid-rewrite.md / eir_inject.sh |
