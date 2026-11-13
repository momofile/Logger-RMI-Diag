# payload/ — 资源说明

这个目录是 `gt()` 的默认查找根。**服务端每次请求都实时读磁盘，增删文件后无需重启。**

## 目录里有什么

| 文件 | 来源 | 授权 |
|---|---|---|
| `DebugNative.stub.dll` | 本项目 `tools/make_stub_dll.py` 生成的 1024 字节空壳 PE | ✅ 原创，MIT |
| `DebugNative.dll` | 上游真实原生模块（17,920 字节） | ⚠️ 第三方归档，**不在 MIT 内** |
| `resource/`（42 个文件，约 15 MB） | 上游映射表、字体、贴图、着色器、文本资源 | ⚠️ 第三方归档，**不在 MIT 内** |

第三方内容的来源、逐项清单、体积与哈希见根目录 `THIRD_PARTY.md`；权利边界见 `NOTICE.md`。再分发时请一并保留这两个文件。

## 查找顺序

对请求名 `raw`（`\` 统一成 `/`，拒绝绝对路径和 `..`）：

1. `payload/<raw>` —— 精确路径，例如 `payload/resource/189Srg.srg`
2. `payload/<文件名>` —— 扁平回退，例如 `payload/DebugNative.dll`
3. 仅当 `raw` 以 `.dll` 结尾 → 返回内置占位 PE
4. 其它情况 → 返回空字节，并在 `requests.log` 记一条 `MISS`

**判定是否放对位置**：看 `requests.log`。出现 `MISS <名字>` 时，把文件放到 `payload/<名字>` 即可，名字要逐字一致。

## 文本资源格式（重要）

映射表是纯文本 `CL: / FD: / MD:` 格式，每行一条：

```
CL: <MCP类名> <运行时srg类名>
FD: <MCP类名>/<字段名> <运行时srg类名>/<字段名>
MD: <MCP类名>/<方法名> <描述符> <运行时srg类名>/<方法名> <描述符>
```

⚠️ **第一列是 MCP 名，第二列是运行时 srg 名**，方向与标准 MCP `joined.srg` 相反。把常见的 `joined.srg` 直接改名放进来无效，解析器会解析出一堆对不上的条目，表现仍然是满屏 `Cannot wrap`。

**为什么不能塞二进制进去**：解析器按 `\n` 切行。如果 `gt()` 返回一段 PE 字节，只会被切成 1 行（日志里的 `Srg[1 lines]`），映射表等于空的，后续包装类全部为 null，最终在桥接阶段抛 `NullPointerException`。这正是 v1.0.0 修掉的核心 bug。

## 已附带的资源

```
resource/189Srg.srg          4,467,544 字节 / 27,884 行   (Forge 1.8.9)
resource/189Notch.srg        2,713,296 字节               (Vanilla 1.8.9)
resource/1122Srg.srg         6,035,673 字节
resource/harmonyosbo.ttf / harmonyosre.ttf / intersemibold.ttf
resource/biggod/  clickgui/  rightmenu/  template/       (PNG 贴图)
resource/*.frag  *.vsh  *.png  *.txt                      (着色器 / UI / 文本)
resource/resource/vertex.vsh                              (兼容 vsh 双重前缀)
```

**为什么有 `resource/resource/vertex.vsh`**：客户端 `loadFromSelf()` 对含 `vsh` 的名字会再叠加一层 `resource/` 前缀，不补这层会在 shader 阶段出现 `MISS`。

## 验证

启动服务后运行 `verify.bat`（或 `./run.sh verify`），探针会打印：

```
[probe] gt(DebugNative.dll) = 17920 bytes, magic = 4D5A
[probe] gt(resource/189Srg.srg) = 4467544 bytes (real map present)
[probe]   189Srg.srg : 4467544 bytes
[probe] ALL OK - RMI chain is usable
```

若显示 `0 bytes (not supplied)`，只说明资源被移除了，**不影响 RMI 链路本身的验证**。

## 精简用法

只想要纯 MIT 源码包时，删除 `resource/` 与 `DebugNative.dll`，保留 `DebugNative.stub.dll`。服务端仍可编译运行，`gt()` 会对缺失项记 `MISS`。对应说明见 `THIRD_PARTY.md` 最后一节。
