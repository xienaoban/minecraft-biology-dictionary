---
name: port
description: 跨 MC 版本移植助手：把 Biology Dictionary 的功能改动从源版本移植到目标版本，保证行级一致（机械移植）。普通 bug 修复、局部重构、代码清理和既有代码维护不得使用。
whenToUse: 需要把某个 MC 版本分支（或 commit）的功能移植到另一个 MC 版本分支时使用。
---

# Port — 跨 MC 版本移植指南

## 适用范围

普通 bug 修复、局部重构、代码清理、代码审查、问题解释或既有目标分支代码维护均不得使用本 skill。
仅在用户明确要求把源版本分支中的功能/commit 移植到目标版本时使用。

## ⚠️ 移植纪律（最高优先级，每次移植都必须遵守）

1. **原封不动移植**：每一行、每个词、每条注释、每个空行都必须与源补丁一致。不要 reformat、不要删注释、不要改命名风格。仅当 API 差异导致编译错误时才调整，且修改面尽可能小。
2. **旧引用必须更新**：测试代码中的字符串常量、注释中的旧类名等引用也要同步更新。
3. **移植后逐文件 diff 审计**：对每个移植文件 `git show <commit>:<path>` vs 本地文件，确认每个差异都有 API 适配理由。**不能跳过。**
4. **非 diff 部分禁止被移植**：两边框架差异都是有原因的，不要顺手把目标框架一起移植/改造。
5. **及时沟通**：渲染逻辑找不到等价实现、某个功能在目标版本可能需要完全不同的实现方式、不确定某个改动是功能性还是版本适配性时，停下来问用户。

## 分工原则

| 适合命令 / 工具 | 适合 AI |
|---------|--------|
| `git show` 提取文件内容 | 理解 diff 意图 |
| `git diff-tree` 获取文件列表 | 判断「功能改动」vs「MC 版本适配」 |
| grep 搜索、sed 机械替换 | 复杂 API 适配方案的选择 |
| 编译 | 理解上下文并做出合理的代码改写 |
| | 渲染逻辑的理解和等价实现 |

---

# 不同 MC 版本的 Mod 移植指南

先阅读 `AGENTS.md`（工作目录通用开发守则），该规范同样适用于此。

## 版本链与目录

需要用户手动指定回合的内容（通常为 tag:vX.X.X ~ HEAD 或 main-X.X.X ~ dev 的所有内容）。

链式移植方法：
- 26.3 移植到 26.2
- 26.2 移植到 26.1.2
- 移植后的 26.1.2 移植到 1.21.11
- 移植后的 1.21.11 移植到 1.21.1
- 移植后的 1.21.1 移植到 1.20.1

不要 **26.3** -> 26.1.2、**26.3** -> 1.21.11、**26.3** -> 1.21.1 ... 相邻版本移植改动更小，全都从 26.3 出发会有大量重复工作。

这里描述的是“高 MC 版本 → 低 MC 版本”的移植顺序，反过来“低 → 高”原理是一样的，只是知识要反着使用。

移植顺序：按上述链路，每做完一个版本停下来让维护者检查，不要连续移植，因为出问题概率还挺大的。

各版本主要特征与差异：

| 目录 | 目标分支 | 架构 | MC | Java | 反射 | 资源类名 |
|------|----------|------|-----|------|------|----------|
| `minecraft-biology-dictionary-26.3` | `main-26.3` | 手写多平台 | 26.3 | 25 | ✅ 允许 | `Identifier` |
| `minecraft-biology-dictionary-26.2` | `main-26.2` | 手写多平台 | 26.2 | 25 | ✅ 允许 | `Identifier` |
| `minecraft-biology-dictionary-26.1.2` | `main-26.1.2` | 手写多平台 | 26.1.2 | 25 | ✅ 允许 | `Identifier` |
| `minecraft-biology-dictionary-1.21.11` | `main-1.21.11` | 手写多平台 | 1.21.11 | 21 | ❌ 禁用 | `Identifier` |
| `minecraft-biology-dictionary-1.21.1` | `main-1.21.1` | 手写多平台 | 1.21.1 | 21 | ❌ 禁用 | `ResourceLocation` |
| `minecraft-biology-dictionary-1.20.1` | `main-1.20.1` | 手写多平台 | 1.20.1 | 17 | ❌ 禁用 | `ResourceLocation` |

> 禁用反射指的是反射处理 MC 数据（因为做了混淆）。本模组、其他模组的数据可以反射。

1.21.11 / 1.21.1 / 1.20.1 三个版本均已完成“去 Architectury”改造，架构形态与 26.x 完全一致：
`Platform.load(X.class)` + 同包 `*Impl`、`@PlatformEntry` 静态入口列表、平台目录 `fabric/` + `forge/`（1.21.x 为 `neoforge/`）。

所有目录共享同一 git remote (`git@github.com:xienaoban/minecraft-biology-dictionary.git`)，不同分支。

MC 第一方源码：`mc-source/<MC 版本>/`，可供查询。

## 工作流程

1. 进入目标移植项目目录，拉取最新的目标分支（有冲突停下询问）
2. 创建本地工作分支（如 `port-v1.2.3-from-26.3`），此分支内自由 commit
3. **获取增量改动清单**：用户指定要移植的 commit/branch 区域、或者已知的上次移植的前序版本 `port-v1.2.3-from-26.3`、`port-v1.2.3-from-26.2`、`port-v1.2.3-from-1.21.1`
4. **机械移植**，核心流程，后面我会重点讲什么是我定义的“机械移植”
5. 小步提交，编译验证
6. 全部完成后执行完整性检查

## 核心原则与目标：机械移植 = 行级一致

所谓机械移植，就是不仅功能一致，还要求源码尽可能 **行级一致**。未来移植任务会非常多，尽可能统一不同版本源码可避免逐步失控。

以下为“行级一致”的解释：
- 本项目不同 MC 版本间始终保持着绝大部分代码的完全一致，只有少量代码因 MC 版本差异、模组平台架构差异而存在中等差异甚至完全不同
- 因此大部分来自源 diff 的代码，都能在目标代码中找到完美匹配的位置进行插入/替换/删除，且无需修改任何源 diff 内容
- 行级一致就是要达到这种类似 apply diff 的效果（之所以说是类似，因为 apply 未必成功），几乎仅需适配一下 MC 源码变化
- 对于少量无法行级一致的代码，可以变通处理，偏向于少量适配，极端情况允许完全重写

### 豁免范围：这些地方不要求行级一致

“行级一致”约束的是业务代码（payload 定义、`*NetManager`、`core` / `gui` / `config` 的逻辑）。以下区域**豁免**，只要求 API 面尽量一致、行为等价，允许各版本实现完全不同：

- **`platform/**`**：跨平台封装层，专门用来吸收版本与平台差异。判据是 API（类名、方法签名、`@PlatformEntry` 入口列表）尽量一致，**实现不要求一致**，可以整段重写。例如 `PacketUtil` 在 26.x / 1.21.11 用 `Type` + `StreamCodec`，1.20.1 用 `ResourceLocation` + id 缓存；两者的 registrar 也各写各的。
  - 网络层是这类差异最典型的一处：`Packet` 在 26.x / 1.21.11 / 1.21.1 里 `extends CustomPacketPayload` 并带 `type()` 默认实现，1.20.1 没有 payload API，它什么都不继承，只有 `write` + `clientReceive` / `serverReceive` + `Factory`。**这是设计不是缺陷**——`Packet` 的职责就是让业务代码不关心底层。别去“统一”，也别在 `Packet` 外面再套一层 wrapper（试过 `PacketBinding`，等于在已有隔离层上再包一层，纯增开销，已废弃）。
- **注册器**：registrar 的注册流程、`@Mod` / entrypoint 挂载、事件订阅方式，按各平台写法来，不要求同形。
- **渲染相关**：渲染管线、`GuiGraphics` / `RenderSystem` 调用、`blit` / buffer / vertex 写法、着色器与模型层，随 MC 渲染 API 版本变化，允许完全不同的实现。
- **`mixin/**`**：**完全不做要求**。target 签名、注入点、refmap / accesstransformer 因映射与版本而异，只要效果等价即可，不要求行级一致，也不要照抄源版本的结构。
- **构建脚本与资源清单**（`*.gradle`、`gradle.properties`、`mixins.json`、`*.accesswidener`、`mods.toml` / `neoforge.mods.toml`、`fabric.mod.json`）：按目标平台模板写。

豁免不等于可以随手改：豁免区之外的代码仍要逐行对齐；豁免区之内也不要为了“实现统一”去动已经能工作的版本，**尤其是已打 tag 的版本**（格式与命名是另一回事，见下节）。

### 命名与代码风格：全局完全统一，不属于版本差异

命名规范、代码风格、缩进、换行、空行、注释排版这些与游戏逻辑无关的部分，要求所有版本**完全一致**。它们不受上面的豁免保护，**不能**用“版本差异”“历史遗留”“当年就是这么写的”来解释差异 —— 长得不一样就是不一致，就是要统一的目标。

这类统一走独立的 `Normalize …` 提交，不要混进功能移植的 diff（移植提交要能被逐行 review）。反过来，做移植时也不要顺手把格式差异一起改掉。

**一个约定：你可以认为，除了我让你移植的部分以外，各个版本的 main 分支的已实现功能是几乎完全一致的（版本差异导致的功能缺失除外）。**
因此当遇到两边框架不一致时，不要想着把源框架顺手一起移植过来，而是必须在目标框架上适配（基本上，差异都是有原因的，尝试移植框架大概率失败）。
也就是说，“行级一致”不仅要求 diff 部分被正确移植，还要求**非 diff 部分禁止被移植**。

移植完成后，最终需要能通过逐行的 diff 审计，**每个移植差异点都必须可归因**。

典型的不符合行级一致的错误案例举例（基本都是手贱的自以为是，我对此深恶痛绝）：

1. 注释、字符串、名称被自说自话地“优化润色”：
   ```java
   /**
    * ABCD
    */
   ```
   被错误移植为
   ```java
   /** ABCD */
   ```
   `"This is a pet of mine" + "设置文件"` 被错误移植为 `"This is my pet" + "配置文件"`，
   `PoseStack poseStack = null;` 被错误移植为 `PoseStack ps = null;`
2. 一些无所谓顺序的变量/字段，被随意安放：
   ```diff
     "key1": "value",
   + "key2": "value",
   ```
   被错误移植为
   ```diff
   + "key2": "value",
     "key1": "value",
   ```
3. 代码结构被自说自话地 format：
   ```java
   if (A) { return 1; }
   ```
   被错误移植为
   ```java
   if (A) {
      return 1;
   }
   ```
   ```java
   if (!A) B;
   else C;
   ```
   被错误移植为
   ```java
   if (A) C;
   else B;
   ```
4. 移植不仅移植了 diff，还错误地顺手把框架移植了（违反“非 diff 部分禁止被移植”）
   - 事实上每一处现有差异几乎都是有版本差异导致的原因的、是刻意为之

还有一个要点：不同 MC 版本的同一功能的 API 可能不同，为此我会将其封装为自用 API，例如 `ClientUtils.isSingleplayer()`，该函数不同版本内部实现不同，但外部都调用它，保持了 API 一致。
对于这种情况，也是要移植的。不要觉得“是 API 不一致导致的被迫封装，目标仓库当前语义已正确，不必移植”。不停封装 MC API 也是为了行级一致。

不过 Java 的那几行 import 不在“行级一致”的范围，名字变了 import 顺序也变，是正常的，但是不用过度关心 import 顺序，后面我会单独 format imports。

### 移植策略

**我并不限制你的移植策略**，反正原则和目标都和你说了，最终要达成目标、通过 diff 审查，以及我会手动进行测试。

但是我不得不警告你，有时改动的代码量会非常大非常杂，历史上你经常陷于其中久久不能收敛问题。因此以下有几点经验可供参考：

1. 梳理要移植的内容，对于能够明确分段的内容，**分段移植**，每段移植完进行编译，编译通过后移植下一段，例如
   - 类名重命名、类移动与包名修改，可批量完成
   - 两个功能几乎无交集，可各自独立移植
   - 新增大量文件，直接复制过来
2. 可以尝试 apply diff，如果大量失败则放弃此方案，否则可以考虑适配少量 apply 失败的
3. 有时代码比较大块，将所有代码移植完后可能要多次编译仍然有较多失败，记得及时反思问题所在，必要时停下与我讨论
4. 涉及修改双方原实现差异大的代码，多参考 mc-source，理解修改的原本意图，适配/重新开发（但是历史上有时你会判断错意图，建议找我问一下），这部分代码的合理性会受严格质询
5. 修改面非常大的代码，可以考虑直接复制，然后反向适配目标版本
6. 可能有少量代码是源版本特有的，如新生物的适配、特定版本特有问题修复、自制模组平台框架加固等，无需移植，拿不定主意就问我
7. 涉及多个 commit 的移植，可以合并移植、也可以逐个移植，灵活应对

以上可供参考，但最终的移植方案需要你基于实际情况灵活考虑，例如
- 从 26.3 移植到 26.2，或从 26.2 移植到 26.1.2 有时甚至直接 git cherry pick 即可完成，因为这些相邻 MC 版本差异极小；
- 但同样的内容从 26.1.2 移植到 1.21.11 可能就非常复杂，因为两者 MC 代码差异大、反射支持情况不同、模组平台也不同。
对于复杂场景，建议你决定方案后找我对一下。

## 注意事项

### 需要手动适配的文件

- 差异的二进制文件可以直接复制
- 涉及版本号、构建的文件需要注意，可能需要部分手动适配合适的版本，例如
  - `gradle.properties` 的依赖项版本，必须是目标版本可用的（建议问我）；模组版本可以同步更新；MC 版本禁止变动
  - `build.gradle`、`gradle/wrapper/*` 的构建工具版本、写法
  - Github Action 里涉及的 Java 版本、MC 版本等版本不要动；旧版本已无 Architectury 依赖，依赖清单只保留 Cloth Config
  - `fabric.mod.json`
  - `ref/` 下我放了一些与本模组相关的三方模组，若需要理解不同版本下其他模组的 API，可访问
- 涉及不同 MC 版本的文件，经常要重新寻找访问的 MC 接口，且差异巨大的不强求一致、允许按目标版本完全重写，例如
  - Mixin 类与 Mixin 配置（按目标版本重新找注入点与 target 签名；别为对齐源版本留下用不到的 `@Shadow` 死代码）
  - Access 文件（`*.accesswidener`、`accesstransformer.cfg`）
- 源实现差异巨大的代码同理（高亮生物/方块的渲染、第一人称生物的渲染、渲染管线等客户端渲染模块）

### 构建环境（去 Architectury 后统一）

| 项 | 取值 | 备注 |
|----|------|------|
| Gradle | 9.7.1 | `gradle/wrapper/gradle-wrapper.properties` |
| Fabric 侧 | `fabric-loom-remap` 1.18.4 | **插件自身要求 Gradle 守护进程 JVM ≥ 25**，与 MC 编译目标（17/21）无关 |
| Forge 侧 | `net.neoforged.moddev.legacyforge` 2.0.147 | 构建 Forge 1.20.1，loader 仍是 MinecraftForge 47.4.10 |
| NeoForge 侧 | `net.neoforged.moddev` | 1.21.x |
| daemon JVM | `gradle/gradle-daemon-jvm.properties` 的 `toolchainVersion=25` | 让任意外部 launcher JDK 都能构建 |
| 仓库 | fabricmc / neoforged / **minecraftforge** / mavenCentral | MDG legacyforge 不会给 `neoFormRuntimeExternalTools` 挂 Forge 官方仓库，漏了会解析失败 |

其他 Forge 1.20.1 专有事项：MixinExtras 只从自己的 mixin config plugin 引导 → 需 `modLocalRuntime`；`@Mod` 无 `dist` 属性 → 在 `FMLEnvironment.dist == Dist.CLIENT` 下手工调用 Client 入口；Mixin refmap 不自动生成 → 需显式 `annotationProcessor "org.spongepowered:mixin:0.8.5:processor"`，`processResources` 注入 `"refmap"` 键，`mods.toml` 写 `[[mixins]]`。

### 架构一致，无需转换

26.x 与 1.21.11 / 1.21.1 / 1.20.1 现在**都是手写多平台**，架构一一对应（同名类、同包 `*Impl`、同样的 `@PlatformEntry` 入口列表），移植时直接照搬即可，不需要做架构转换。


## 各版本 API 差异速查

常见问题就直接基于下表修了，避免每次都吭哧吭哧查半天源码。

### 26.3 → 26.2

| 方面 | 26.3 | 26.2 |
|------|------|------|
| 输入枚举 | `InputConstants.Type.KEYBOARD` | `InputConstants.Type.KEYSYM` |
| 键位常量 | `InputConstants.KEY_*` | `GLFW.GLFW_KEY_*` |
| 鼠标常量 | `InputConstants.MOUSE_BUTTON_*` | `GLFW.GLFW_MOUSE_BUTTON_*` |
| 玩家掉落 | `player.drop(stack, false, Prediction.SERVER_ONLY)` | `player.drop(stack, false)` |
| 玩家挥手 | `player.swing(hand, SwingAnimation.DEFAULT, false)` | `player.swing(hand)` |
| 第一人称渲染器 | `FirstPersonHandsAndItemsRenderer` | `ItemInHandRenderer` |
| 第一人称渲染参数 | `PlayerRenderState` / `FirstPersonHandsAndItemsRenderState` | 旧 `LocalPlayer` / light 参数 |
| PoseStack | `poseStack.rotate(...)` | `poseStack.mulPose(...)` |
| GUI PIP outline | `PreparedFrame.executeOutline(RenderPass)` | 旧 PIP silhouette fog 方案 |
| 渲染管线包名 | `com.mojang.renderpearl.api.pipeline.RenderPipeline` | `com.mojang.blaze3d.pipeline.RenderPipeline` |
| GUI tooltip | `tooltip(..., positioner, texture, false)` | `tooltip(..., positioner, texture)` |
| Loot 容器 | `UniformContainerBase` | `LootPoolSingletonContainer` |
| 数量 provider | `ContextIntProvider` | `NumberProvider` |
| Nested loot | `HolderSet<LootTable>` | `Either<ResourceKey<LootTable>, LootTable>` |
| Loot condition | `Optional<Holder<LootItemCondition>>` | `List<LootItemCondition>` |
| Biome spawn | `EnvironmentAttributes.NATURAL_MOB_SPAWNS` | `Biome#getMobSettings()` |
| Spawn 查询 | `MobSpawnSettings#getMobsToSpawn(category)` | `MobSpawnSettings#getMobs(category)` |
| 私有 codec | 需要 accessor mixin | 视 26.2 类成员可见性而定 |

26.3 新增/调整实体、特殊 spawner datapack、Parrot 食物特判等属于功能/数据差异，移植时按源 diff 处理，不要当作 MC API 映射强行套用。

### 26.2 → 26.1.2

TODO
有个 EntityType ↔ EntityTypes 的变化。

### 26.1.2 → 1.21.11（大版本跨越）

| 方面 | 26.1.2 | 1.21.11 |
|------|--------|---------|
| 实体类型常量 | `EntityTypes` | `EntityType` |
| 渲染管线 | `RenderPipeline` + `GuiRenderState` | `RenderSystem` + `BufferBuilder` |
| Entity 渲染 | `EntityRenderDispatcher.submit(state, ...)` | `EntityRenderDispatcher.render(entity, ...)` |
| PoseStack | `pushMatrix()`/`popMatrix()`/`scale(x,y)` | `pushPose()`/`popPose()`/`scale(x,y,z)` |
| Screen 字体 | `screen.getFont()` | 需 mixin accessor |
| NBT | codec based | Optional 返回 |
| SavedData 注册 | `computeIfAbsent(TYPE)` | `computeIfAbsent(FACTORY, "dataId")` |
| 反射 | ✅ 允许 | ❌ 禁止 |
| NeoForge 入口 | 有参 | 无参 |

### 1.21.11 → 1.21.1

| 1.21.11 | 1.21.1 |
|---------|--------|
| `Identifier` | `ResourceLocation`（全项目机械替换） |
| `registryAccess.lookupOrThrow(X)` | `registryAccess.registryOrThrow(X)` |
| `EntitySpawnReason` | `MobSpawnType` |
| `ToastManager` | `ToastComponent` |
| `client.getToastManager()` | `client.getToasts()` |
| `keyPressed(KeyEvent)` | `keyPressed(int keyCode, int scanCode, int modifiers)` |
| `Weighted<T>` / `WeightedList<T>` | `WeightedEntry` / `WeightedRandomList<T>` |
| NeoForge 入口无参 | NeoForge 入口有参 `(IEventBus)` |

渲染管线在 1.21.11→1.21.1 重新设计，是最大差异区（P3 重灾区）。

### 1.21.1 → 1.20.1

| 1.21.1 | 1.20.1 |
|---------|--------|
| `neoforge/` | `forge/`（目录名机械映射） |
| `net.neoforged` | `net.minecraftforge` |
| Java 21 | Java 17（`List.addFirst()` → `list.add(0, e)`） |
| `mob.finalizeSpawn(level, diff, type, data)` 4参 | 5参（多了 `@Nullable CompoundTag`） |
| 物品数据：`DataComponents` / `CustomData` / `ItemLore` / `CustomModelData` | 物品 NBT：`stack.getOrCreateTag()` / `setHoverName` / `tag.putInt("CustomModelData", …)` |
| 装备槽：7 个（含 `BODY`）、`ContainerSingleItem` | 6 个（无 `BODY`）、`Container`（`setByPlayer(ItemStack)` 只有 1 参） |
| 附魔判断 `EnchantmentHelper.has(stack, EnchantmentEffectComponents.X)` | `EnchantmentHelper.getItemEnchantmentLevel(Enchantments.X, stack) > 0` |
| `ServerPlayer(server, level, CommonListenerCookie)` | `ServerPlayer(server, level, GameProfile)` + `gameMode.changeGameModeForPlayer(...)` |
| record pattern `instanceof Pair<?,?>(a, b)` | Java 17 不支持，改常规模式匹配 + 显式取值 |

## 完整性检查

移植完成后进行移植完整性检查，宗旨是：
- 不许少移植
- 不许多移植
- 不许自作主张、自作聪明、自说自话、自以为是

### 逐文件 diff 审计

对每个被移植文件，拉取其在源仓库的 diff、与其在目标仓库移植的 diff；
对每一处 diff，逐句逐字审计，确保没有不符合要求的差异，对每处差异归因。
- MC 类名变更、API 差异导致的细微差别是合理的
- 对于差异大的部分，需要二次判断实现是否合理

完整性检查而修改的内容不要 git commit --amend，要区分开，方便我 review。
最终给出总结报告，给我审阅。注意，审查的是涉及修改的部分，是双方改动的 diff，双方本次都没动过的部分（即既有版本差异）不必审查。

在工程上，tree diff 不显示新增类，检查时要注意别漏了。

## 已知坑

- **不要猜测 API 是否存在**：先查 `../mc-source/<version>/`
- **Mixin target 签名**：跨版本几乎肯定不同，需在目标 MC 源码找对应方法
- **渲染部分**：`ScreenRenderingContext`、Mixin、渲染管线是手动移植问题重灾区

## 参考

- 编码规范：工作目录 `AGENTS.md`，以及目标项目目录内的 `AGENTS.md`
- `port-all.md` 仅当用户明确要求时阅读
