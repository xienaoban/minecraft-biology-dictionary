# AGENTS.md

给 AI 开发代理看的本项目协作向导与当前需求说明。

## 项目定位

本仓库是 `minecraft-biology-dictionary-26.1.2`，目标 MC 版本见 `gradle.properties`，当前为 `minecraft_version=26.1.2`、`java_version=25`。

本项目不是 Architectury 项目，也不要把它改造成 Architectury 项目。当前仓库采用自己搭建的 multi-platform 结构：

- `common/`：跨平台通用代码与资源。
- `fabric/`：Fabric 平台代码、资源与构建配置。该目录来自 Fabric 官网模板/生成器。
- `neoforge/`：NeoForge 平台代码、资源与构建配置。该目录来自 NeoForge 官网模板/生成器。

根项目负责把 `common`、`fabric`、`neoforge` 组织到同一个 Gradle 多项目中，但平台侧仍以各自官方工具链为准。

## 重要架构约束

- 禁止引入 Architectury 框架、`architectury-plugin`、`dev.architectury.loom`、`@ExpectPlatform` 或 Architectury API 作为生产依赖。
- Fabric 侧使用 Fabric Loader、Fabric API 与 Fabric Loom。注意：从 MC 26.1 开始没有 Yarn mappings，本项目只能使用 Minecraft official mappings，不要引入或按 Yarn 命名编写代码。
- NeoForge 侧使用 NeoForge ModDevGradle。
- `common` 代码不得直接依赖 Fabric、Forge、NeoForge 平台包。根项目已有 `checkCommonPlatformImports` 用于检查 `common/src/main/java` 中的违规导入。
- `common` 项目的构建可以参考 Fabric 项目并使用 Fabric Loom 等基础工具链，但应最小化依赖；当前只要求 `common` 能编译通过，不要求 `common` 可作为独立平台运行。
- 平台差异应通过本项目自己的接口、桥接类、注册入口或服务封装解决，而不是照搬 Architectury 的 expect/actual 机制。
- `fabric/`、`neoforge/` 是从各自官网模板下载/生成后改造的目录；后续修改应尽量保留官方模板的构建习惯，只做必要整合。

## 参考项目

参考项目路径：

```text
/mnt/e/project/minecraft/minecraft-biology-dictionary-architectury-1.21.11
```

该参考项目是 MC `1.21.11` 版本的 Biology Dictionary，并且使用 Architectury 架构。它是本次移植的功能来源，不是本仓库的架构模板。

从参考项目中可以复用或迁移：

- 业务功能设计。
- 通用领域代码。
- UI、网络、配置、属性、技能、兼容、资源等实现思路。
- README、docs、raw 资源中仍适用于 26.1.2 的内容。

不能直接复用的部分：

- Architectury 构建脚本与插件配置。
- `@ExpectPlatform` 调用与实现方式。
- Architectury 平台 API。
- 与 MC 1.21.11、Java 21、旧命名或旧平台 API 强绑定的代码。

## 当前需求

在本 26.1.2 仓库中，不使用 Architectury，把参考项目 `minecraft-biology-dictionary-architectury-1.21.11` 的功能移植过来，并最终跑通 Fabric 与 NeoForge。

这里的“跑通”至少包含：

- 根项目 Gradle 配置可正常同步。
- `common` 可以编译，且不包含 Fabric/NeoForge/Forge 直接导入。
- `fabric` 可以编译并能启动客户端。
- `neoforge` 可以编译并能启动客户端。
- 平台入口、mod metadata、mixin 配置、资源路径、依赖声明与打包产物都与 26.1.2 当前架构一致。
- 从 1.21.11 迁移来的核心功能在 26.1.2 的 MC API 下完成必要适配。

## 移植原则

- 先读当前 26.1.2 仓库，再读参考 1.21.11 仓库，不要凭记忆改 MC/API 代码。
- 优先保留当前仓库的自建 multi-platform 形态。
- 优先把可跨平台的业务逻辑放入 `common`。
- 涉及注册、网络、客户端事件、配置界面、资源加载、按键绑定、屏幕打开、数据同步等平台相关行为时，分别在 `fabric` 与 `neoforge` 实现，再用本项目自己的封装给 `common` 调用。
- 不要把平台侧的实现细节泄漏到 `common`。
- 每次迁移时都要检查 MC 26.1.2 API 与 1.21.11 API 的差异，尤其是渲染、资源标识、网络包、注册器、实体/NBT、组件文本、客户端屏幕相关代码。
- 禁止用反射调用 MC 原版内容来规避混淆问题；需要访问私有或受限逻辑时优先考虑 Mixin。
- 修改尽量小步提交、可编译验证，避免一次性大搬运后再排错。

## 建议执行顺序

1. 固化构建骨架：确保根项目、`common`、`fabric`、`neoforge` 的 Gradle 配置能独立完成基础编译。
2. 迁移公共入口与常量：如 mod id、logger、通用初始化流程。
3. 设计并实现自建平台桥接层，用于替代参考项目中的 Architectury expect/platform 调用。
4. 迁移资源与 metadata：icon、lang、mixin json、assets、docs 中适用内容。
5. 迁移核心业务模块：属性系统、技能系统、组件系统、GUI、网络、配置、兼容模块等。
6. 分别接通 Fabric 和 NeoForge 的平台入口、事件、网络、注册、客户端初始化。
7. 分平台运行 `compileJava`、`build`、`runClient`，根据错误逐项适配 26.1.2 API。
8. 最后补充必要测试、文档和已知限制。

## 构建与验证命令

常用命令：

```bash
./gradlew check
./gradlew common:compileJava
./gradlew fabric:compileJava
./gradlew neoforge:compileJava
./gradlew fabric:build
./gradlew neoforge:build
./gradlew fabric:runClient
./gradlew neoforge:runClient
```

如果某个平台官方模板要求从子目录执行 wrapper，先确认根项目任务是否已经覆盖需求；不要盲目新增第二套互相冲突的构建流程。

## 代码风格与协作约定

- 与用户对话使用中文；类名、API 名、错误信息等专业内容可以保留英文。
- 代码注释使用英文，且只在确实能降低理解成本时添加。
- 除构造函数或确有歧义的场景外，尽量避免 `this.`。
- 避免使用类的全限定名；除字符串或特殊情况外，优先使用 `import` 和 simple name。
- 不要做与当前任务无关的重构。
- 不要回滚用户已有改动。
- 注意当前目标是 MC 26.1.2，不是 1.21.11。
- 本地 MC 源码如果存在，优先参考 `<本项目根目录>/../mc-source/<MC 版本>`。
- 禁止使用 `org.jetbrains.annotations.Nullable` 和 `org.jetbrains.annotations.NotNull`。

## 当前工作边界

本次只需要把上述需求写成文档。文档写完后停止，不开始移植、不跑构建、不修改业务代码，等待用户继续说明“今天要跑通什么”。
