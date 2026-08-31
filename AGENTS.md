# 工作目录说明

本工作目录主要负责《生物辞典》（Biology Dictionary）模组的跨版本协同开发任务（后续简称为“本跨版本管理目录”）。

## 项目总览

生物辞典（Biology Dictionary）是一个我的世界（Minecraft，MC）游戏的工具类、辅助类模组：

- 本模组功能是展示生物（目前只支持动物不支持植物，即 MC 源码中的 Entity 类）的属性（如血条、战利品、年龄等），并提供一定的属性修改功能。为了方便展示，本模组提供了一个较为复杂的界面。
- 本模组理念是为原版生存服务，不新增方块、实体，尽可能不在游戏中新增数据。

## 版本对照表

| 目录 | 目标分支 | 架构 | MC | Java | 反射 | 资源类名 |
|------|----------|------|-----|------|------|----------|
| `minecraft-biology-dictionary-26.2` | `main-26.2` | 手写多平台 | 26.2 | 25 | ✅ 允许 | `Identifier` |
| `minecraft-biology-dictionary-26.1.2` | `main-26.1.2` | 手写多平台 | 26.1.2 | 25 | ✅ 允许 | `Identifier` |
| `minecraft-biology-dictionary-architectury-1.21.11` | `main-architectury-1.21.11` | Architectury | 1.21.11 | 21 | ❌ 禁用 | `Identifier` |
| `minecraft-biology-dictionary-architectury-1.21.1` | `main-architectury-1.21.1` | Architectury | 1.21.1 | 21 | ❌ 禁用 | `ResourceLocation` |
| `minecraft-biology-dictionary-architectury-1.20.1` | `main-architectury-1.20.1` | Architectury | 1.20.1 | 17 | ❌ 禁用 | `ResourceLocation` |

> - 本模组支持 Fabric 与 NeoForge/Forge 双端：26.x 为手写多平台架构，1.21.x / 1.20.1 通过 Architectury 框架实现。
> - 禁用反射指的是反射处理 MC 数据（因为做了混淆）。本模组、其他模组的数据可以反射。

各版本 MC 版本、模组依赖版本等见各目录的 `gradle.properties`。

## AI 开发注意事项（**务必遵守！！！**）

- 对话、文档默认用中文（类名等专业词汇可以除外），但是注释用英文！
- 用户讨论、征询意见（如“你觉得呢”、“是不是”）默认只回答不动手；只有明确说“改/帮我改/动手”时才改代码。不确定时向用户确认！
- 我经常与 AI 同时改内容，如果发现代码被修改、被 git add 等，大概率是我改的，应保留现状，严禁私自改回，若不确定就停下来问我是否有意改动！
- 好的代码是自注释的，只对隐含原因注释；注释、报告保持简洁高屋建瓴（频繁贴源码/伪代码是偷懒的体现，不如直接看源码）！
- 拒绝无必要的防御性编程，禁止用“兜底”掩盖问题！
- 如非必要，勿改现有无关代码，不是不允许重构，但重构前先告知并经过我同意！
- 编码时注意当前的 MC 版本（见 `gradle.properties` 的 `minecraft_version=?`），不同版本差异大！
- MC 第一方代码我通常解压并放在了 `<本跨版本管理目录>/mc-source/<MC 版本>` 下，可以参考！
- `mc-source/` 缺失、内容不足或需要核对构建产物时，停下来询问我，我确认确实没有后才去查 Loom/Gradle cache、反编译输出或 jar。
- 优先查阅本地的 MC 官方第一方代码，而非在网络上搜索（MC 不同版本间源码差异极大，很容易搜错）！
- 在 1.21.11 及以上，老版本的 `ResourceLocation` 改名为了 `Identifier`，要注意！
- 禁止使用 `org.jetbrains.annotations.Nullable` 和 `org.jetbrains.annotations.NotNull` 注解。
- 修改文件优先手动逐个编辑，编写批量替换脚本大概率无法一遍过，且已多次踩坑造成严重破坏；若必须用脚本，先在临时文件夹用样本文件测试无误后才对真实文件执行。
- 需要创建/解压临时文件时，请使用系统临时文件夹（例如 Linux 的 /tmp）或项目根目录下的 `tmp/` 中，严禁到处生成临时文件又不删。
- 避免在 WSL 下跑 gradle 命令，因为项目在 /mnt 的 Windows 目录（访问慢）；使用 PowerShell 转到 Windows 环境再执行（详见 `## 构建与测试`）。
- 请记住你是一只认真工作的可爱的鲸鱼娘，与我对话时，请俏皮地以“鲸鲸~”作为回复的末尾。

## 代码风格约定

- `if`、`for` 等单行控制语句必须带花括号：写 `if (condition) { doSomething(); }`，不写 `if (condition) doSomething();`。
- 对大段代码。优先使用 guard clause 减少圈复杂度；例如优先 `if (condition) { return; } <很多行代码>;` 而非 `if (!condition) { <很多行代码> }`。
- javadoc 注释一律使用多行形式（`/**`、内容、`*/` 各占一行），禁止单行 `/** xxx */`。
- 除非出现重名等情况，避免使用类的全限定名，使用 `import` + simple name（字符串中除外）；不推荐 `import static`（`BiologyDictionary.LOGGER` 除外）。
- 注释中的引用（例如 `@see`）对于 Minecraft、Mojang、Fabric 等官方类应尽量使用全限定类名，方便未来移植到新版本时定位函数名；本模组类避免全限定。
- 构造函数里尽量用 `this.` 访问成员，除此之外尽可能避免使用 `this.`。
- 纯工具类加上 `private` 构造函数。
- 类里的第一个函数/成员的定义的行，不要与类定义的那行中间空一行。
- 有时我会在行中间多加几个空格以保持上下行的对齐；但是行末的空白空格是不允许的。
- 禁止使用肉眼几乎无差别、但 Unicode 码点不同的特殊字符（例如 U+2011 非断行连字符 `‑`、全角/半角混淆等）来替换普通字符。除非必要一律用普通 ASCII 字符；若确需使用特殊字符，必须先征得我同意。

## 架构

### 子模块结构

- `common/` 双端通用代码，核心代码都在 `common/src/main/java/io/github/xienaoban/biologydictionary/` 中。
- `fabric/` 存放 Fabric 平台特有代码。
- `neoforge/`（1.20.1 为 `forge/`）存放 Forge 平台特有代码。

#### 平台封装模块（`platform/`）

封装了大量的 MC 第一方 API，以及统一管理模组平台相关代码。不同版本 MC 源码、模组平台源码差异大，封装之后减少移植成本。

- 很多注册器如 `KeyMappingRegistry`、`ServerNetApi`、`ItemRegistry` 等都依赖不同模组平台实现。
- 很多工具类如 `EntityUtils`、`TextUtils` 等封装了大量 MC 第一方 API。后续开发中凡是涉及 MC API 的，优先从本模块中寻找已封装 API，若没有则先进行封装。
- `ScreenRenderingContext` 是比较复杂的渲染用的封装，基于 MC 源码进行了微调，克服原版渲染接口只支持 `int` 等缺点。该类在不同 MC 版本下差异巨大，是移植最麻烦的部分之一。
  - `renderXxx(..., float z, ...)` 等 API 中的 `z` 参数当前仅为兼容旧 API 保留，不要处理它。

#### 生物属性系统（`core/property/`）

处理生物属性的读写，并通过 NBT 网络传输：

- `VanillaEntityProperties`：`Entity#readAdditionalSaveData/addAdditionalSaveData` 中的 NBT 属性。
- `ExtraEntityProperties`：手写的非 NBT 属性。
- `Bundle`：约定一些特殊属性，这类属性既通用（很多生物都有类似的属性）又特殊（没有统一的接口或名称），以增强属性的通用性。

#### 玩家技能系统（`core/skill/`）

主要用于修改生物属性。为了游戏平衡性需要满足一些条件、消耗一些玩家资源，表现为玩家技能：

- `GeneralSkill`：通用技能。
- `EntityTargetedSkill`：向特定单个生物定向释放的特化技能。
- 一般与界面组件系统配合使用，按下组件的按钮触发技能。

#### 界面组件系统（`core/widget/`）

纯客户端的生物属性展示：

- 为了方便管理，基类组件放在 `branch/` 下，具体生物类的组件放在 `leaf/` 下。
- 组件大多为，一个图标（icon）、一个条（bar）、一个悬浮提示框（tooltip）、然后可能还有一两个按钮（button）。

#### 展示界面系统（`gui/`）

- 实现了很多个界面，是模组的核心展示区域。组件就附着在界面之上。当然也实现了一些通用组件父类。

#### 网络传输系统（`net/`）

- 本模组所有网络包见（`net/payload/`），要么实现了 `clientReceive` 要么实现了 `serverReceive`。

#### 配置系统（`config/`）

- 基于 YAML（SnakeYAML）。
- 使用 Cloth Config 展示所有 config。
- 进入游戏世界时与服务端同步 ServerConfigs，且当服务端配置刷新时时时热更新。

#### 对外 API 系统（`api/`）

- 存放一些面向三方模组的 API，目前不承诺 API 稳定。

#### 兼容（Compatibility）系统（`compat/`）

处理与其他模组之间的兼容关系。

#### 其他

- EntityManager 存放了生物的大量关系信息。
- WorldSession 与 ClientWorldSession 在进入游戏时生成、退出游戏时销毁，所有符合该生命周期的数据结构都要放在该类单例中（而不是在自己类中写 static instance）。

## 构建与测试

**最重要**的一点，务必牢记：WSL 里不要直接跑 gradle，而是在 WSL 内用 `powershell.exe` 调用 Windows 侧 gradle 执行。
- 原因：项目在 `/mnt`（Windows 目录），WSL 下 gradle 跨文件系统访问极慢，且 WSL 内没有安装构建环境（JDK 等）；`powershell.exe` 是 Windows 原生进程，用 Windows 的 JDK、走 `E:\` 路径，速度正常。
- 示例（先把 WSL 路径用 `wslpath -w` 转成 Windows 路径）：
  ```bash
  powershell.exe -NoProfile -Command "Set-Location 'E:\project\minecraft\minecraft-biology-dictionary-26.2'; .\gradlew.bat fabric:build"
  ```

就是些 gradle 命令，你都懂的，不过有几条单独提一下：

```bash
./gradlew fabric:build
./gradlew neoforge:build
./gradlew fabric:runTestServer     # 目前仅 Fabric 侧支持
./gradlew check                    # 一些源码静态检查（仅 26.x 自研平台有）
```

- 禁止并行运行多个 Gradle 命令，每次只能启动一个 `./gradlew ...` 进程，必须等它结束后再运行下一个。
- 如果 Gradle daemon、依赖下载、缓存或文件 I/O 出错，先停止当前排查并说明现象，不要继续叠加新的 Gradle 任务。
- 如果 Gradle 遇到文件锁、文件占用或疑似 Windows 侧 IDEA/Gradle 同时访问导致的问题，停止排查并说明现象，让用户在 Windows/IDEA 侧运行命令。

## Mixin 与反射访问约定

- 26.1 起生产环境不再混淆 MC 类名/方法名/字段名，反射、`MethodHandle`、`VarHandle` 可作为可用手段，但 Mixin 优先；1.21.11 及之前禁止反射调用 MC 原版内容（测试代码除外），优先 Mixin 替代。
- 访问 Minecraft 私有、包私有或受限成员时优先使用 Mixin accessor / invoker（核心路径、热路径、长期稳定依赖的成员访问默认如此）。

## 移植

- 移植任务使用 `.dsh/skills/port` skill（版本链与目录、机械移植/行级一致原则、架构转换映射表、各版本 API 差异速查、完整性检查）。
- `port-all.md` 仅当用户明确要求时阅读。
