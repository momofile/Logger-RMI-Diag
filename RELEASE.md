# 发布指南 / RELEASE

第一次开源发布不用慌，按这份文件做就行。**下面带「可直接复制」的段落都可以原样粘贴**，只有尖括号 `<...>` 里的内容需要换成你自己的。

---

## 一、先花一分钟看懂要发布什么

开源发布 = 两件事：

1. **一个 Git 仓库**：放源码、文档和归档资料，别人能看、能提 Issue、能提 PR。
2. **一个 Release（版本）**：给当前内容打 `v1.0.0` 标记，附上一份 zip，别人能直接下载。

本项目已经准备好了 `Logger-RMI-Diag-v1.0.0.zip`（**完整归档版**，压缩后约 5.5 MB、解压后约 26 MB）和同名 `.sha256` 校验文件，可直接作为 Release 附件上传。

## 二、发布前检查清单

- [ ] 署名 **momofile** 已在 `LICENSE`、`AUTHORS.md`、`README.md`、下面第四节文案里出现（已写好，扫一眼即可）
- [ ] `AUTHORS.md` 里的 `<你的ID>` 换成你的 GitHub / Gitee 用户名
- [ ] 确认 `NOTICE.md`、`THIRD_PARTY.md` 都在包里 —— 这两份是「标注来源、不冒认版权」的关键
- [ ] 把 `Logger-RMI-Diag-v1.0.0.zip.sha256` 一并作为 Release 附件上传，方便别人校验
- [ ] 确认仓库里**没有** `requests.log`、崩溃报告、启动器日志（含凭据和本机路径，`.gitignore` 已挡住）
- [ ] 本地跑一次 `build.bat` + `start-server.bat` + `verify.bat`，看到 `ALL OK`，且 `189Srg.srg` 显示 4467544 字节
- [ ] 新建仓库时 License 选 **MIT**（仓库已有 `LICENSE`，别让平台再生成一份重复的）；注意 `LICENSE` 末尾写了授权范围，第三方归档不在 MIT 内
- [ ] 心里有数：仓库会包含约 15 MB 上游资源 + 10 MB 逆向字节码，GitHub 单仓库建议控制在 1 GB 内，完全没问题

## 三、仓库信息（新建 GitHub/Gitee 仓库时填）

| 项 | 建议内容 |
|---|---|
| 仓库名 | `Logger-RMI-Diag` |
| 一句话描述 | `本地 RMI 诊断桩 + 上游资料完整归档：复现并定位 Minecraft 1.8.9 注入式客户端的启动崩溃` |
| 简介（About） | `Minimal local RMI diagnostic stub + full third-party archive. Original code MIT; archived upstream materials attributed, not MIT.` |
| Topics / 标签 | `java` `rmi` `minecraft` `reverse-engineering` `crash-analysis` `archival` `diagnostics` `mit` |
| License | MIT（选已有文件即可） |
| 可见性 | Public |
| 初始化 | **不要**勾选 "Add a README"（本地已有） |

## 四、Release 文案（可直接复制）

**Tag / 版本号**：`v1.0.0`
**标题**：`v1.0.0 — 首个公开版本（完整归档版）`

**正文**：

```markdown
## Logger-RMI-Diag v1.0.0

一个最小的本地 RMI 诊断桩，用来复现和定位「Minecraft 1.8.9 注入式客户端
因本机缺少配套服务而崩溃」这一类问题；同时完整归档排查期间用到的上游资料，
让整条链路可复现、可对照。

by **momofile**（本项目发起人 / 相对上游的二创作者）

### 它做了什么

- 在 `127.0.0.1:1099` 建立 RMI registry 并绑定诊断对象
- 实现互操作所需的 `gt / hs / p / gtk` 四个远程方法
- 修掉了排查中最坑的一个 bug：文本映射表被误返回成 DLL 字节，
  解析出 `Srg[1 lines]`，导致后续 BridgeManager 一路 NPE
- 每次调用写 `requests.log`，缺失资源以 `MISS` 明示
- 附带独立探针，不开游戏也能验证整条链路

### 快速开始

Windows：`build.bat` → `start-server.bat` → `verify.bat`
macOS / Linux：`./run.sh verify`

资源已随包附带，正常情况下会看到
`[probe] gt(resource/189Srg.srg) = 4467544 bytes (real map present)`
和 `[probe] ALL OK - RMI chain is usable`。

### 关于授权，请花 30 秒看一下

本仓库分两层：

- **原创层**（`src/`、`tools/`、脚本、全部文档、占位 DLL）—— **MIT**，
  Copyright (c) 2026 momofile and contributors
- **归档层**（`payload/resource/`、`payload/DebugNative.dll`、`archive/`）——
  原样收录的**第三方内容，权利归各自原权利人，不在 MIT 授权范围内**。
  逐项来源、体积、哈希与字体许可提示见 `THIRD_PARTY.md`。

归档是对一个已停止维护项目的资料留存。我们如实记录：包内唯一的
`META-INF/LICENSE` 是 JNA 依赖的双许可声明，并非上游项目自身的开源授权；
MANIFEST 带有加壳保护标记。原作者停止维护并不等于权利自动消灭，
所以这里不冒认版权、不做再许可声明，并保留全部来源标注。
若相关权利人有异议，欢迎开 Issue，会及时处理。

### 用途边界

**本项目是第三方非官方诊断工具**，与文中提到的客户端及其作者无任何关联。
仅供本机离线的崩溃诊断、逆向方法学习与资料存档；不含任何游戏内功能，
不实现 10234 WebSocket 通道，请勿用于公共服务器或任何影响他人的场景。
完整声明见 `NOTICE.md`。

### 未收录

`requests.log` 等运行日志、崩溃报告（含凭据与本机路径）、以及原始第三方 jar
本体（含完整客户端主类）出于隐私与相关性考虑未收录。

---

完整排查过程见 `CHANGELOG.md`，发布说明见 `RELEASE.md`。
```

## 五、发到群 / 论坛 / 动态的短文案（可直接复制）

> 把自己排查 Minecraft 注入崩溃时做的东西开源了，叫 Logger-RMI-Diag，顺手把整套资料也归档了。
> 起因是客户端启动时会连本机 1099 的 RMI 服务，连不上就把异常抛到渲染线程直接崩；
> 补上服务后又踩了个坑：文本映射表被返回成 DLL 字节，只解析出 1 行，后面一路 NPE。
> 最后写出来的是一个 Java 8、零依赖的最小诊断桩，带独立探针和请求日志，
> 外加 1731 个反编译 class、4 份 javap 文本和全部上游资源，方便后来人复现。
> 原创代码 MIT，第三方归档单独标注来源、不套 MIT；仅供本机离线排查和存档用。
> 署名 momofile。地址：<填你的仓库链接>

想更短就发第一句 + 最后两句。

**英文简介（可选）**：

> A tiny self-hosted Java RMI stub for reproducing and diagnosing client crashes caused by a missing local service, shipped with a full archive of the upstream resources used during the investigation. Original code MIT; third-party materials attributed and explicitly out of MIT scope. Offline debugging / archival only. By momofile.

## 六、别人大概率会问的四件事

**「原作者都退网这么多年了，为什么还写这么一段声明？」**
> 因为「停更 / 退网」在法律上不等于著作权消灭，而且包里的字体权利人是另一批人。
> 写清楚来源、不冒认版权，比装作没这回事更安全，也不影响任何人下载使用。
> 事实依据都在 `THIRD_PARTY.md`（JNA 的 LICENSE、MANIFEST 加壳标记）。

**「这么多文件，是不是可以直接拿来玩？」**
> 不是。它不含任何游戏内功能，只是一个本地 RMI 服务 + 日志工具 + 资料归档。
> 想干净玩游戏，不注入任何 DLL、并从启动器 JVM 参数里移除 `-agentpath:` 项即可。

**「我只要纯源码，不想要 25 MB 归档怎么办？」**
> 删掉 `payload/resource/`、`payload/DebugNative.dll`、`archive/` 三个位置即可，
> 服务端照常编译运行（`gt()` 会记 `MISS`）。步骤写在 `THIRD_PARTY.md` 最后一节。

**「10234 端口报错怎么办？」**
> 那是客户端后续的 WebSocket 通道，本项目明确不实现。日志出现 10234 连接失败
> 说明链路已经走得更靠后了，属于已知限制，见 README「已知限制」。

## 七、命令示例（Git，把 `<你的ID>` 换掉）

```bash
cd Logger-RMI-Diag
git init
git status                 # 确认没有 .log / crash-*.txt，确认归档文件都在
git add .
git status                 # 再看一眼暂存区，这一步别省
git commit -m "Initial release: minimal RMI diagnostic stub + full archive"
git branch -M main
git remote add origin https://github.com/<你的ID>/Logger-RMI-Diag.git
git push -u origin main
git tag -a v1.0.0 -m "v1.0.0"
git push origin v1.0.0
```

然后在仓库页面 → Releases → **Draft a new release** → 选 `v1.0.0` 标签 → 粘贴上面第四节文案 → 把 zip 和 `.sha256` 一起拖进附件 → **Publish release**。

> 压缩后 5.5 MB 的 zip 走 GitHub Release 附件没问题（解压后约 26 MB）；直接 `git push` 时 1731 个小 class 文件会让首次推送稍慢，属正常。

## 八、发布之后

- 有人问资源来源，直接给 `THIRD_PARTY.md`；问用途边界，给 `NOTICE.md`。
- 分享别人发来的 `requests.log` 前先确认没有凭据；服务端提供 `-Ddiag.redact=true` 脱敏开关。
- 后续改动更新 `CHANGELOG.md`，按 `v1.0.1` / `v1.1.0` 递增。
- 若相关权利人联系你，按 `NOTICE.md` 第 6 节处理：核实、沟通、必要时移除对应文件并补一版说明。姿态摆正了，处理起来很简单。
