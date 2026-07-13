---
name: port-1-21-11
description: 仅用于把 Biology Dictionary 的特性从 git branch main-architectury-1.21.11 移植到 main-26.1.2；尤其适用于判断代码应放在 common、Fabric、NeoForge、Platform.load 服务、@PlatformEntry 静态定义，还是迁移占位 facade。普通 bug 修复、局部重构、代码清理和既有代码维护不得使用。
---

# Port 1.21.11

用于处理 Biology Dictionary 从 git branch `main-architectury-1.21.11` 迁移到 `main-26.1.2` 的任务。

## 适用范围

仅在实际把旧分支中的特性或实现移植到目标分支时使用本 skill。普通 bug 修复、局部重构、代码清理、代码审查、问题解释或既有目标分支代码维护均不得使用。

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
- 迁移具体代码时，默认先从 1.21.11 复制原实现，再处理编译和 API 变化。若需要重写、裁剪大块逻辑、改变行为或引入新的迁移策略，先通知维护者并说明原因；只有已知的小型 API/命名修正可以直接处理。
- 对于临时占位、裁剪实现、未完整移植或后续必须恢复的内容，必须在代码或迁移文档中就地添加 TODO，明确后续要移植/恢复什么。

## 决策模式

common 定义、平台必须注册的内容，使用 `@PlatformEntry` 静态定义：

- key mappings
- packet payload 清单
- creative tab entries
- commands
- client/server event listener 清单
- 后续 renderer/model/screen/reload registrations

静态注册清单里放真实 entry，例如 key mapping、payload entry、creative tab entry、command builder、listener entry。不要为了注册清单额外包一层函数式接口或动态 lambda；平台层遍历 entry 后在各自生命周期里注册。

命名上，某个定义类只有一组待注册清单时字段叫 `ENTRIES`；同类中有多组待注册清单时叫 `XX_ENTRIES`。事件清单按事件时机命名，例如 `STARTED`、`STOPPING`，不强行套 `ENTRIES`。

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
