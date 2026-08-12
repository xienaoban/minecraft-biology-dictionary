# AGENTS.md

给 Codex 看的本项目的向导。

## 项目总览

生物辞典（Biology Dictionary）是一个我的世界（Minecraft，MC）游戏的工具类、辅助类模组：
- 本模组功能是展示生物（目前只支持动物不支持植物，即 MC 源码中的 Entity 类）的属性（如血条、氧气、战利品、年龄等），并提供一定的属性修改功能。为了方便展示，本模组提供了一个较为复杂的界面。
- 本模组通过 Architectury 框架实现支持 Fabric 和 NeoForge/Forge（>1.21 为 NeoForge, 1.20.1 为 Forge）双端；不同 MC 版本的模组源码实现存在于不同 git branch 中。
- 本模组理念是为原版生存服务，不新增方块、实体，尽可能不在游戏中新增数据。

* 为了方便描述，后面 NeoForge 与 Forge 统称为 Forge。

本模组 MC 版本、模组依赖版本等各个版本信息见 `gradle.properties`。

## 构建指令

### 项目构建

```bash
./gradlew build                    # Build all platforms
./gradlew fabric:build             # Build Fabric only
./gradlew neoforge:build           # Build NeoForge only
```

### 启动游戏（带着模组）

```bash
./gradlew fabric:runClient         # Run Fabric client
./gradlew fabric:runServer         # Run Fabric server
./gradlew neoforge:runClient       # Run NeoForge client
./gradlew neoforge:runServer       # Run NeoForge server
```

### 测试

```bash
./gradlew fabric:testServer        # Run Fabric server tests
```

### 其他

```bash
./gradlew fabric:compileJava       # Compile Fabric Java sources
./gradlew neoforge:compileJava     # Compile NeoForge Java sources
./gradlew dependencies             # View dependency tree
```

## 架构

### 子模块结构

遵循经典的 Architectury 项目目录结构：
- `common/` 存放绝大部分代码，Fabric 与 NeoForge/Forge 双端通用；对于平台相关代码，标以 `@ExpectPlatform`。
- `fabric/` 存放 Fabric 平台特有代码，如实现 `@ExpectPlatform`、模组平台入口注册、Modmenu 支持等；其 metadata 在 `resources/fabric.mod.json`。
- `neoforge/` 或 `forge/` 存放 Forge 平台特有代码，如实现 `@ExpectPlatform`、模组平台入口注册等；其 metadata 在 `resources/META-INF/neoforge.mods.toml`。

### 核心代码

全部都在 `common/src/main/java/io/github/xienaoban/biologydictionary/` 中。

#### 平台封装（Platform）模块（`platform/`）

封装了大量的 MC 第一方 API，以及统一管理模组平台相关代码（`@ExpectPlatform`）。这么做是因为本模组支持多个 MC 版本，不同版本的 MC 源码、模组平台源码差异大，封装之后减少移植成本。
- 很多注册器如 `KeyMappingRegistry`、`ServerNetApi`、`ItemRegistry` 等都依赖不同模组平台实现，Fabric 的基本基于 Fabric API，Forge 的基本基于 Architectury API。
- 很多工具类如 `EntityUtils`、`TextUtils` 等封装了大量 MC 第一方 API。后续开发中凡是涉及 MC API 的，优先从本模块中寻找已封装 API，若没有则先进行封装。
- `ScreenRenderingContext` 是比较复杂的渲染用的封装，基于 MC 源码进行了微调，克服原版渲染接口只支持 `int` 等缺点。该类在不同 MC 版本下差异巨大，是移植最麻烦的部分之一。

#### 生物属性（Property）系统（`core/property/`）

处理生物属性的读写，并通过 NBT 网络传输：
- `VanillaEntityProperties`：对于 MC 第一方生物或基类，自动支持全量 NBT 属性（`net.minecraft.world.entity.Entity#readAdditionalSaveData/addAdditionalSaveData` 中的）。
- `ExtraEntityProperties`：对于 MC 第一方生物或基类，支持一些不在 NBT 中的属性。
- `Bundle`：约定一些特殊属性，这类属性既通用（很多生物都有类似的属性）又特殊（没有统一的接口或名称），以增强属性的通用性。

#### 玩家技能（Skill）系统（`core/skill/`）

主要用于修改生物属性。为了游戏平衡性需要满足一些条件、消耗一些玩家资源，表现为玩家技能：
- `GeneralSkill`：通用技能。
- `EntityTargetedSkill`：向特定单个生物定向释放的特化技能。
- `SkillCost`：支持的技能消耗的资源类型，支持玩家在配置文件中配置。
- 一般与界面组件系统配合使用，按下组件的按钮触发技能。

#### 界面组件（Widget）系统（`core/widget/`）

纯客户端的生物属性展示：
- 为了方便管理，属于抽象类、基类（具体生物类的父类）的组件放在 `branch/` 下（类继承树的分支节点），具体生物类的组件放在 `leaf/` 下（类继承树的叶子节点）。
- 组件大多为，一个图标（icon）、一个条（bar）、一个悬浮提示框（tooltip）、然后可能还有一两个按钮（button），图标纯展示、条内展示具体信息、悬浮框展示详细信息、按钮触发技能。

#### 展示界面（Screen）系统（`gui/`）

实现了很多个界面，是模组的核心展示区域。组件就附着在界面之上。当然也实现了一些通用组件父类。

#### 网络传输（Networking）系统（`net/`）

本模组所有网络包见（`net/payload/`），要么实现了 `clientReceive` 要么实现了 `serverReceive`。

#### 配置（Config）系统（`config/`）

- 基于 YAML（SnakeYAML）
- 使用 Cloth Config 展示所有 config
- 进入游戏世界时与服务端同步 ServerConfigs，且当服务端配置刷新时时时热更新。

#### 兼容（`Compatibility`）系统（`compat/`）

处理与其他模组之间的兼容关系。

#### 其他

- EntityManager 存放了生物的大量关系信息。
- WorldSession 与 ClientWorldSession 在进入游戏时生成、退出游戏时销毁，所有符合该生命周期的数据结构都要放在该类单例中（而不是在自己类中写 static instance）。
- 本模组新增的注册器都遵循 `registerBuiltIn(Registrar registrar)` 的形式，未来会开放接口给三方模组。

### Mixin（`mixin/`）

Mixin 配置文件：`common/src/main/resources/biologydictionary.mixins.json`

### Testing

- 目前仅支持 `testServer`，测试文件在 `common/src/testServer/`。
- 目前仅支持 Fabric 侧测试：`fabric:testServer`。
- 里面还有通过 AST 解析来自动获取所有 MC NBT 属性的内容。

## AI 开发注意事项（**务必遵守！！！**）

- 对话用中文（类名等专业词汇可以除外），但是注释用英文！
- 好的代码是自注释的，啰嗦的注释不要加！
- 除了构造函数，尽可能避免使用 `this.`！
- 除非出现重名等情况，避免使用类的全限定名，而是使用 `import` + simple name 的方式（字符串中除外）！
- 如非必要，勿改现有代码！
- 编码时注意当前的 MC 版本（见 `gradle.properties` 的 `minecraft_version=?`），不同版本差异大！
- MC 第一方代码我通常解压并放在了 `<本项目根目录>/../mc-source/<MC 版本>` 下，可以参考！
- 优先查阅本地的 MC 官方第一方代码，而非在网络上搜索！
- 若必须网络检索 MC 相关内容，务必校验所参考内容与目标 MC 版本的兼容性。MC 不同版本间源码差异极大！
- 禁止使用反射调用 MC 原版内容（测试代码除外），因为 MC 生产运行环境会对字段和方法名进行混淆处理。优先使用 Mixin 替代。
- 在 1.21.11 及以上，老版本的 `ResourceLocation` 改名为了 `Identifier`，要注意是新版本还是老版本！
- 禁止使用 `org.jetbrains.annotations.Nullable` 和 `org.jetbrains.annotations.NotNull` 注解。
- 最后，与我对话时，请严肃地以“喵~”作为回复的末尾。
