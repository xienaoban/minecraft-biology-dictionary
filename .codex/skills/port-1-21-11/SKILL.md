---
name: port-1-21-11
description: 用于把 Biology Dictionary 从 git branch main-architectury-1.21.11 移植到 main-26.1.2；尤其适用于判断代码应放在 common、Fabric、NeoForge、Platform.load 服务、@PlatformEntry 静态定义，还是迁移占位 facade。
---

# Port 1.21.11

用于处理 Biology Dictionary 从 git branch `main-architectury-1.21.11` 迁移到 `main-26.1.2` 的任务。

参考项目相对路径：

```text
../minecraft-biology-dictionary-architectury-1.21.11
```

目标是当前 `minecraft-biology-dictionary-26.1.2` 仓库。

完整策略、理由和典型案例见：

```text
docs/dev/port-from-1.21.11.md
```

## 规则

- 不引入 Architectury、`@ExpectPlatform`、Architectury API 或 Architectury Gradle 插件。
- `common` 不得导入 Fabric/Forge/NeoForge 包。
- 使用 Minecraft official mappings，不使用 Yarn 命名。
- 从 1.21.11 迁移 MC API 前，先核对 26.1.2 API。
- 优先让平台注册生命周期清晰可见，不为了统一 API 隐藏注册时机。

## 决策模式

common 定义、平台必须注册的内容，使用 `@PlatformEntry` 静态定义：

- key mappings
- packet payload 清单
- creative tab entries
- commands
- client/server event listener 清单
- 后续 renderer/model/screen/reload registrations

`Platform.load(...)` 只用于窄的运行时服务：

- 平台信息和 config 目录
- mod 是否加载、版本查询
- 网络发送 API
- 即时 client/server 工具动作

不要把平台注册顺序藏进 common 的 `init()` 顺序里。平台入口应在正确的 Fabric 或 NeoForge 生命周期中消费 common 定义。

## 当前典型案例

- `KeyMappings`：common 静态定义 + `@PlatformEntry`，平台 client 入口注册。
- `DevUtils`：common 窄服务接口 + `Platform.load(...)`，平台 `Impl` 提供实现。
- `ClientEventRegistry -> ClientEvents`：旧 registry facade 保留为空的 `TODO: delete after port` 占位；真实事件 list 放在 `ClientEvents`，由平台注册。

新增模式前，先看 `docs/dev/port-from-1.21.11.md`。

## 验证

代码改动后通常运行：

```bash
./gradlew common:compileJava fabric:compileJava neoforge:compileJava checkCommonPlatformImports
```

资源或打包相关改动，额外运行对应的 `processResources` 或 `build` 任务。
