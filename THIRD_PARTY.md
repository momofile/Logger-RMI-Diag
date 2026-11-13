# 第三方内容清单 / THIRD PARTY

本文件逐项列出本仓库中**原样收录、权利归原权利人、不在 MIT 授权范围内**的内容。配套声明见 `NOTICE.md`。

## 来源

归档内容来自一份第三方资源包（本地文件名 `lycoris.jar`，1717 个条目），属于已停止维护的上游项目。其 `META-INF/MANIFEST.MF` 记录：

```
Main-Class: reflect.nya.al.Main
Protected-By: fl0wowp4rty
```

包内 `META-INF/LICENSE`（788 字节）内容是 **Java Native Access (JNA) 的 `Apache-2.0 OR LGPL-2.1` 双许可声明**，属于所捆绑的依赖库，**不是上游项目自身的开源授权**。未在包内发现任何授予本项目再分发或再许可权利的声明。

> 记录这些事实的目的：使用者和后来者能自行核对来源，不必猜。它不改变发布决定，也不构成法律意见。

## 收录清单

| 路径 | 数量 / 体积 | 内容 | 权利状态 |
|---|---|---|---|
| `payload/resource/` | 42 个文件，15,153,632 字节 | 3 份 srg 映射表、3 个 TTF 字体、UI 贴图（`biggod/`、`clickgui/`、`rightmenu/`、`template/`）、`.frag` / `.vsh` 着色器、`moe.txt` 等 | 第三方，**不在 MIT 内** |
| `payload/DebugNative.dll` | 17,920 字节 | 上游真实原生模块（x64 PE） | 第三方，**不在 MIT 内** |
| `archive/decompiled-classes/` | 1731 个 `.class` + `index.txt` | 排查期间从上游 DLL 中提取、解密并导出的 Java 字节码 | 上游衍生内容，**不在 MIT 内** |
| `archive/javap/` | 4 个文本 | 关键类（`LoggerPatcher`、`BridgeManager`、`ResourceManager`、`MapParser`）的 javap 反汇编输出 | 上游衍生内容，**不在 MIT 内** |

**原创、可 MIT 分发**的对照项：`payload/DebugNative.stub.dll`（1024 字节，由 `tools/make_stub_dll.py` 生成的无副作用空壳 PE）、`src/`、`tools/`、脚本与全部文档。

## 关键文件校验（SHA-256 前 16 位）

| 文件 | 字节 | SHA-256 前缀 |
|---|---:|---|
| `payload/resource/189Srg.srg` | 4,467,544 | `a8486671a5e85153` |
| `payload/resource/189Notch.srg` | 2,713,296 | `75c42edab2c26fb9` |
| `payload/resource/1122Srg.srg` | 6,035,673 | `c97483dc4a2fff93` |
| `payload/DebugNative.dll` | 17,920 | `51d5c7c0ecfed405` |
| `payload/DebugNative.stub.dll`（原创） | 1,024 | `b2e23ef529049a93` |
| `payload/resource/harmonyosbo.ttf` | 155,300 | `16ee0db98387afba` |
| `payload/resource/harmonyosre.ttf` | 155,988 | `79b3218f139ad2be` |

## 字体特别说明

`payload/resource/` 内含 `intersemibold.ttf`（Inter，通常适用 SIL Open Font License 1.1）、`harmonyosbo.ttf` / `harmonyosre.ttf`（HarmonyOS Sans，适用其自身许可）。**字体的权利人不是上游原作者**，其许可条款独立于本项目。若二次分发这些字体，请一并附上对应的原许可文本并遵守其要求（常见要求包括保留版权声明、不得单独售卖字体、衍生字体不得使用保留名称等）。

## 我们的姿态

- 不冒认版权：归档层明确不套用 MIT，不声明拥有再分发授权；
- 保留来源：保留本文件与 `NOTICE.md`，标明上游项目与依赖库许可的事实；
- 非商业、研究/存档导向：不含任何付费内容、账号体系或授权校验；
- 可追溯：关键文件给出体积与哈希，便于核对是否被改动；
- 尊重权利人：如有异议，按 `NOTICE.md` 第 6 节通过 Issue 联系，会及时处理。

## 如果你要精简发布

删除 `payload/resource/`、`payload/DebugNative.dll`、`archive/` 三个位置，服务端仍可正常编译运行（只是 `gt()` 会记 `MISS`），并同步把 `README.md` / `RELEASE.md` 里「完整归档版」的措辞改回「源码版」。`.gitignore` 里保留的排除规则可作为参考。
