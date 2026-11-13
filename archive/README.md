# archive/ — 排查期逆向归档

本目录是排查三轮崩溃时产出的**中间资料**，原样收录供复现与教学参考，**权利归上游原权利人，不在 MIT 授权范围内**。详见根目录 `THIRD_PARTY.md` 与 `NOTICE.md`。

```
archive/
├── decompiled-classes/   1731 个 .class + index.txt
│                         从上游 DLL 中提取并解密的 Java 字节码
└── javap/                4 个 javap -p -c -l -constants 输出文本
                          LoggerPatcher / BridgeManager /
                          ResourceManager / MapParser
```

## 这些文件是怎么来的

上游 DLL 中的 class 以单字节 XOR 加密存放。排查时按 `CA FE BA BE` 魔数穷举 256 个密钥定位偏移，解密后导出为 `.class`，再用 JDK 8 的 `javap` 反汇编关键类，据此确认：

- `LoggerPatcher.lookup()` 硬编码 `rmi://localhost:1099/LoggerIsTrash`；
- `loadFromSelf()` 通过 `gt()` 远程索取**所有**资源，包括 `resource/189Srg.srg`；
- `MapParser` 的 `CL:/FD:/MD:` 格式（第一列 MCP 名、第二列运行时 srg 名）；
- `p()` 抛异常会触发 `System.exit(1)`；
- `BridgeManager` 的 NPE 来自映射表为空导致包装类为 null。

这些结论已固化在 `src/LoggerServer.java` 的实现与 `CHANGELOG.md` 里，日常使用**不需要**本目录。

## 注意

- 这些是上游程序的衍生内容，仅用于本机离线的崩溃诊断与逆向方法学习；
- 请勿将其用于任何在线服务、或任何影响他人的场景；
- 如需二次分发，请保留来源标注，并自行确认合规性。
