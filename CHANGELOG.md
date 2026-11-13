# 更新记录 / Changelog

本项目采用语义化版本。所有版本均以 [MIT](LICENSE) 发布。

## v1.0.0 — 首个公开发布版

**从三轮崩溃排查中提炼出的可运行诊断桩**

- 实现 `127.0.0.1:1099` 的 RMI registry 与 `LoggerIsTrash` 绑定；
- 实现互操作所需的四个远程方法：`gt()` / `hs()` / `p()` / `gtk()`；
- 修复「把 DLL 字节返回给文本资源」的错误分派：`gt()` 现在按资源类型处理，只有缺失的 `*.dll` 才回退占位 PE，其余缺失资源返回空字节并记 `MISS`；
- 所有调用写入 `requests.log`，缺失资源可一眼定位；
- `p()` 不再抛异常（客户端的 catch 分支会直接 `System.exit(1)`）；
- 增加 `LoggerProbe` 独立探针，含「PE 字节泄漏到文本资源」检测与本地资源预检；
- 增加 `-Ddiag.redact=true`，分享日志前可对客户端回显脱敏；
- `gt()` 增加目录穿越校验（拒绝绝对路径与 `..`）。

**发布版调整（相对内部排查版本）**

- 采用「完整归档版」：收录 `payload/resource/`（42 个文件 / 约 15 MB）、真实 `DebugNative.dll`、
  `archive/`（1731 个反编译 class + 4 份 javap 文本）；原创层与归档层的授权边界写入
  `LICENSE`、`NOTICE.md`、`THIRD_PARTY.md`；
- 有意**不收录** `requests.log`、崩溃报告、启动器日志与原始第三方 jar 本体
  （含凭据、本机路径或完整客户端主类）；
- 移除脚本中的本机绝对路径，改为 PATH → `JAVA_HOME` → 常见 JDK 路径的探测顺序；
- 增加跨平台 `run.sh`，补充 `LICENSE` / `NOTICE.md` / `THIRD_PARTY.md` / `AUTHORS.md` /
  `RELEASE.md` / `.gitignore`，统一署名 momofile；
- 占位 DLL 生成脚本改为相对路径，并使其与 `StubDll.java` 的常量严格一致。

## 内部排查阶段（未发布）

| 阶段 | 结论 |
|---|---|
| round 1 | 1099 无监听 → `ConnectException` → 崩在 `lookup()` |
| round 2 | RMI 通了，但 `gt()` 错误回退 PE 字节 → 映射表只解析出 1 行 → `BridgeManager` NPE |
| round 3 | 空字节同样是空映射表，崩溃栈不变；`requests.log` 确认唯一缺口是 `189Srg.srg` |
| round 4 | 补齐使用者自备资源后，启动链路全程通过 |

## 后续可能的方向

- `ws://127.0.0.1:10234` 的可选 WebSocket 诊断桩（当前未实现）；
- 资源清单与校验工具（`MISS` 汇总、文件哈希）；
- 更细粒度的日志脱敏规则。

不计划提供：任何上游二进制、任何游戏内功能、任何认证/授权后端。
