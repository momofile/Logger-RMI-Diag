# Logger-RMI-Diag（完整归档版）

一个**最小的本地 RMI 诊断桩 + 完整资料归档**：用来复现、定位并验证「Minecraft 1.8.9 注入式客户端因本机缺少配套服务而崩溃」这一类问题。原创部分纯 Java 8、零第三方依赖、编译即跑；归档部分完整保留排查所需的上游资源与逆向资料。

> **本仓库分两层，请先看这段**
>
> - **原创层**（`src/`、`tools/`、脚本、全部文档、`payload/DebugNative.stub.dll`）—— 由 **momofile** 主导整理，**MIT 许可**。
> - **归档层**（`payload/resource/`、`payload/DebugNative.dll`、`archive/`）—— 原样收录的**第三方内容，权利归各自原权利人，不在 MIT 授权范围内**。逐项来源、体积与哈希见 [`THIRD_PARTY.md`](THIRD_PARTY.md)。
>
> **非官方**：本项目与上游客户端及其作者无任何关联，不是它的组件、插件、后端或替代品。
> **用途限定**：仅供本机离线的崩溃诊断、逆向方法学习与已停维护项目的资料存档；不含任何游戏内功能，不实现 WebSocket 通道，请勿用于公共服务器或任何影响他人的场景。完整声明见 [`NOTICE.md`](NOTICE.md)。

**English summary:** A tiny self-hosted Java RMI stub that reproduces and diagnoses a family of client crashes caused by a missing local service on port 1099, shipped together with a full archive of the third-party resources used during the investigation. Original code is MIT licensed; archived upstream materials are included verbatim with source attribution and are NOT covered by MIT. Offline debugging / archival / education only.

**署名：momofile**（本项目发起人；相对上游原项目为二创作者）。见 [`AUTHORS.md`](AUTHORS.md)。

---

## 背景：它是怎么来的

排查连续三次客户端崩溃时，发现崩溃点会顺着一条依赖链往后移动。每补齐一环，就暴露出下一环：

| 轮次 | 现象 | 真实原因 |
|---|---|---|
| 1 | `java.rmi.ConnectException: Connection refused to host: localhost` → `Unreported exception thrown!` 崩在渲染主线程 | 客户端启动补丁阶段硬编码 `Naming.lookup("rmi://localhost:1099/LoggerIsTrash")`，本机没有 registry |
| 2 | 崩溃点后移到 `BridgeManager` 的 `NullPointerException`，日志刷出几百条 `Cannot wrap` | 诊断桩把**所有** `gt()` 请求都当成 DLL 返回，1024 字节 PE 被映射表解析器当文本读，只解析出 `Srg[1 lines]` |
| 3 | 崩溃栈与第 2 轮完全一致 | 改成返回空字节仍然是空映射表；缺的是那份真实的 1.8.9 srg 映射文件 |
| 4 | 全程通过 | 补齐真实映射表与原生模块后，启动链路不再中断 |

核心结论：`gt()` 是**通用资源获取接口**，不只是取 DLL；文本资源必须返回文本，DLL 请求才返回 PE 字节。这个仓库就是这个结论的可运行实现，外加让结论可复现的全部资料。

## 它做什么

- 在 `127.0.0.1:1099` 建立 RMI registry，绑定名为 `LoggerIsTrash` 的远程对象；
- 实现互操作所需的四个方法，让客户端的 `lookup()` 能成功强转；
- 按资源类型分派 `gt()`：优先读 `payload/` 下的文件，只有缺失的 `*.dll` 才回退到内置的 1024 字节无副作用占位 PE；
- 把每一次调用写进 `requests.log`，缺失资源记为 `MISS`，一眼看出链路停在哪一步；
- 附带 `LoggerProbe`，不用启动游戏就能验证 registry 和四个方法是否可用。

**它不做什么**：不提供任何游戏功能、不实现 `ws://127.0.0.1:10234`、不做认证或授权、不联网。

## 快速开始

环境：JDK 8 及以上。无需 Maven/Gradle，无需联网。**归档资源已随包附带，开箱即可复现完整链路。**

**Windows**

```bat
build.bat            :: 编译到 .\out
start-server.bat     :: 启动服务，保持窗口开着
verify.bat           :: 另开一个窗口，验证链路
```

**macOS / Linux / Git Bash**

```bash
chmod +x run.sh
./run.sh build       # 编译
./run.sh start       # 启动服务（前台，Ctrl+C 结束）
./run.sh verify      # 一键编译 + 启动 + 探针 + 自动收尾
```

`verify` 输出末尾出现 `[probe] ALL OK - RMI chain is usable` 即成功。资源齐全时会看到：

```
[probe] gt(DebugNative.dll) = 17920 bytes, magic = 4D5A
[probe] gt(resource/189Srg.srg) = 4467544 bytes (real map present)
[probe]   189Srg.srg : 4467544 bytes
```

> 若你把 `payload/resource/` 精简掉了，会显示 `0 bytes (not supplied)` 与启动告警——这只影响完整启动链路，**不影响 RMI 服务本身的验证**。

## 目录结构

```
Logger-RMI-Diag/
├── README.md              本文件
├── LICENSE                MIT（仅覆盖原创层，含范围说明）
├── NOTICE.md              权利边界、事实记录与用途限制
├── THIRD_PARTY.md         第三方归档逐项清单 + 哈希 + 来源
├── AUTHORS.md             署名（momofile）
├── CHANGELOG.md           版本记录
├── RELEASE.md             发布指南：仓库信息、Release 文案、检查清单
├── .gitignore
├── src/                   全部原创 Java 源码（MIT）
│   ├── LoggerServer.java        RMI 服务与资源分派
│   ├── LoggerProbe.java         独立探针
│   ├── StubDll.java             内置占位 PE（Base64 常量）
│   └── reflect/nya/al/protocol/LoggerProtocol.java   互操作所需接口声明
├── payload/
│   ├── README.md          资源说明、格式与查找顺序
│   ├── DebugNative.stub.dll   1024 字节占位 PE（原创，MIT）
│   ├── DebugNative.dll        上游真实原生模块（第三方归档）
│   └── resource/          上游映射表 / 字体 / 贴图 / 着色器（第三方归档）
├── archive/               排查期逆向资料（第三方衍生内容）
│   ├── README.md
│   ├── decompiled-classes/     1731 个 .class + index.txt
│   └── javap/                  4 个关键类反汇编文本
├── tools/make_stub_dll.py 重新生成占位 PE 与 StubDll.java
├── build.bat / start-server.bat / verify.bat
└── run.sh                 跨平台入口
```

## 四个远程方法

仅为互操作而复刻的声明，无任何上游实现代码：

```java
public interface LoggerProtocol extends Remote {
    byte[] gt(String name) throws RemoteException;   // 通用资源获取（DLL 与文本/图片）
    void   hs()            throws RemoteException;   // 握手
    void   p(String msg)   throws RemoteException;   // 进度 / 凭据回显
    String gtk()           throws RemoteException;   // 取 key
}
```

| 方法 | 本项目的行为 | 为什么这样写 |
|---|---|---|
| `hs()` | 只记一行日志 | 客户端握手，必须成功返回 |
| `p(s)` | 只记一行日志 | 客户端的 `catch` 分支会 `System.exit(1)`，**抛异常会直接关掉游戏且不生成崩溃报告** |
| `gtk()` | 返回 `"stub-key"` | 诊断桩不做真实校验，非空即可 |
| `gt(name)` | `payload/<路径>` → `payload/<文件名>` → 仅 `*.dll` 回退占位 PE → 其它返回空字节并记 `MISS` | 把 PE 字节返回给文本资源会污染映射表解析，是第 2 轮崩溃的根因 |

`gt()` 会拒绝绝对路径和含 `..` 的名字，避免目录穿越；每次请求实时读磁盘，补文件无需重启。

**隐私提示**：`p()` 回显的是客户端原文，`requests.log` 可能包含用户名或令牌。**分享日志前请清理**，或加 `-Ddiag.redact=true` 启动服务（回显内容将只记录长度）。仓库不收录任何运行日志。

## 资源说明

详见 [`payload/README.md`](payload/README.md)。要点：

- 映射表是 `CL:/FD:/MD:` 文本格式，**第一列是 MCP 名、第二列是运行时 srg 名**（与标准 `joined.srg` 方向相反，不能直接改名拿来用）；
- `189Srg.srg` 共 27,884 行；解析正确时客户端日志里的 `Srg[1 lines]` 会变成 `Srg[27884 lines]`；
- 有客户端对含 `vsh` 的名字会重复加一层 `resource/` 前缀，因此包里也放了 `payload/resource/resource/vertex.vsh`；
- **`payload/resource/` 与 `payload/DebugNative.dll` 属于第三方归档，不在 MIT 授权内**，再分发请保留 `THIRD_PARTY.md` 的来源标注，字体另见该文件的许可说明。

## 排错

| 现象 | 处理 |
|---|---|
| `BUILD FAILED` / 找不到 javac | 安装 JDK 8+ 并设置 `JAVA_HOME`，或确认 `javac` 在 PATH 中 |
| 端口 1099 被占用 | `netstat -ano \| findstr ":1099"` 找到 PID 后结束旧进程再启动；端口是客户端硬编码的，不能改 |
| `lookup` 成功但调用报 `Connection refused` | registry 里是上一个已退出进程的死引用，关掉旧窗口、重新启动服务 |
| 一直出现 `MISS <名字>` | 对照 `requests.log` 里的请求名逐字检查 `payload/` 下的路径 |
| 想干净地玩游戏 | 不注入任何第三方 DLL，并从启动器的 Java 虚拟机参数里移除 `-agentpath:` 项 |

## 已知限制

- 未实现 `ws://127.0.0.1:10234` 的 WebSocket 通道；若日志报 10234 连接失败，说明链路已走到更后面，本项目不覆盖。
- 内置占位 DLL 只是能被 `LoadLibrary` 加载的空壳，不注册任何 JNI；归档里的真实原生模块行为不在本项目范围内。
- 客户端补丁流程里还存在 `System.exit(1)` 分支与直接内存写入；即使资源齐全，也可能在更后面的步骤停止。
- 归档层的权利状态未获授权确认，见 `NOTICE.md` 第 3 节与 `THIRD_PARTY.md`。

## 参与

欢迎提 Issue 和 PR。请不要在 Issue 里粘贴含凭据的日志。提交原创代码即表示同意以 MIT 许可证发布。

## 许可证与致谢

原创层以 [MIT](LICENSE) 发布；归档层权利归原权利人，不在 MIT 范围内，见 [NOTICE.md](NOTICE.md) 与 [THIRD_PARTY.md](THIRD_PARTY.md)。

**momofile** — 本项目发起人与发布者、相对上游的二创作者（见 [AUTHORS.md](AUTHORS.md)）。
