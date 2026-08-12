# 不同 MC 版本的 Mod 移植指南

先阅读 `minecraft-biology-dictionary-26.2/AGENTS.md`，该规范同样适用于此。

## 版本链与目录

需要用户手动指定回合的内容（通常为 tag:vX.X.X ~ HEAD 或 main-X.X.X ~ dev 的所有内容）。

链式移植方法：
- 26.2 移植到 26.1.2
- 移植后的 26.1.2 移植到 1.21.11
- 移植后的 1.21.11 移植到 1.21.1
- 移植后的 1.21.1 移植到 1.20.1

不要 **26.2** -> 26.1.2、**26.2** -> 1.21.11、**26.2** ->1.21.1 ... 相邻版本移植改动更小，全都从 26.2 回合会有大量重复工作。

这里描述的是“高 MC 版本 → 低 MC 版本”的移植顺序，反过来“低 → 高”原理是一样的，只是知识要反着使用。

移植顺序：按上述链路，每做完一个版本停下来让维护者检查，不要连续移植，因为出问题概率还挺大的。

各版本主要特征与差异：

| 目录 | 目标分支 | 架构 | MC | Java | 反射 | 资源类名 |
|------|----------|------|-----|------|------|----------|
| `minecraft-biology-dictionary-26.2` | `main-26.2` | 手写多平台 | 26.2 | 25 | ✅ 允许 | `Identifier` |
| `minecraft-biology-dictionary-26.1.2` | `main-26.1.2` | 手写多平台 | 26.1.2 | 25 | ✅ 允许 | `Identifier` |
| `minecraft-biology-dictionary-architectury-1.21.11` | `main-architectury-1.21.11` | Architectury | 1.21.11 | 21 | ❌ 禁用 | `Identifier` |
| `minecraft-biology-dictionary-architectury-1.21.1` | `main-architectury-1.21.1` | Architectury | 1.21.1 | 21 | ❌ 禁用 | `ResourceLocation` |
| `minecraft-biology-dictionary-architectury-1.20.1` | `main-architectury-1.20.1` | Architectury | 1.20.1 | 17 | ❌ 禁用 | `ResourceLocation` |

> 禁用反射指的是反射处理 MC 数据（因为做了混淆）。本模组、其他模组的数据可以反射。

所有目录共享同一 git remote (`git@github.com:xienaoban/minecraft-biology-dictionary.git`)，不同分支。

MC 第一方源码：`mc-source/<MC 版本>/`，可供查询。

## 工作流程

1. 进入目标移植项目目录，拉取最新的目标分支（有冲突停下询问）
2. 创建本地工作分支（如 `port-v1.2.3-from-26.2`），此分支内自由 commit
3. **获取增量改动清单**：用户指定要移植的 commit/branch 区域、或者已知的上次移植的前序版本 `port-v1.2.3-from-26.2`、`port-v1.2.3-from-1.21.1`
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

**一个约定：你可以认为，除了我让你移植的部分以外，各个版本的 main 分支的已实现功能是几乎完全一致的（版本差异导致的功能缺失除外）。**
因此当遇到两边框架不一致时，不要想着把源框架顺手一起移植过来，而是必须在目标框架上适配（基本上，差异都是有原因的，尝试移植框架大概率失败）。
也就是说，“行级一致”不仅要求 diff 部分被正确移植，还要求**非 diff 部分禁止被移植**。

移植完成后，最终需要能通过逐行的 diff 审计，**每个移植差异点都必须可归因**。

典型的不符合行级一致的错误案例举例（基本都是手贱的自以为是，我对此深恶痛绝）：

1. 注释、字符串被自说自话地“优化润色”：
   ```java
   /**
    * ABCD
    */
   ```
   被移植为
   ```java
   /** ABCD */
   ```
   ，
   ```java
   "This is a pet of mine" + "设置文件"
   ```
   被移植为
   ```java
   "This is my pet" + "配置文件"
   ```
2. 一些无所谓顺序的变量/字段，被随意安放：
   ```diff
     "key1": "value",
   + "key2": "value",
   ```
   被移植为
   ```diff
   + "key2": "value",
     "key1": "value",
   ```
3. 代码结构被自说自话地 format：
   ```java
   if (A) { return 1; }
   ```
   被移植为
   ```java
   if (A) {
      return 1;
   }
   ```
   ，
   ```java
   if (!A) B;
   else C;
   ```
   被移植为
   ```java
   if (A) C;
   else B;
   ```
4. 移植不仅移植了 diff，还顺手把新框架移植了（违反“非 diff 部分禁止被移植”）

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

以上可供参考，但最终的移植方案需要你基于实际情况灵活考虑，例如
- 从 26.2 移植到 26.1.2 有时甚至直接 git cherry pick 即可完成，因为这两个 MC 版本差异极小；
- 但同样的内容从 26.1.2 移植到 1.21.11 可能就非常复杂，因为两者 MC 代码差异大、反射支持情况不同、模组平台也不同。
对于复杂场景，建议你决定方案后找我对一下。

## 注意事项

### 需要手动适配的文件

- 差异的二进制文件可以直接复制
- 涉及版本号、构建的文件需要注意，可能需要部分手动适配合适的版本，例如
  - `gradle.properties` 的依赖项版本，必须是目标版本可用的（建议问我）；模组版本可以同步更新；MC 版本禁止变动
  - `build.gradle`、`gradle/wrapper/*` 的构建工具版本、写法
  - Github Action 里涉及的 Java 版本、MC 版本等版本不要动，老版本还多 Architectury 依赖，也不动
  - `fabric.mod.json`
- 涉及不同 MC 版本的文件，经常要重新寻找访问的 MC 接口，例如
  - Mixin 类与 Mixin 配置
  - Access 文件（`*.accesswidener`、`accesstransformer.cfg`）
- 当然，还有源实现差异巨大的代码要手动适配，例如
  - 高亮生物/方块的渲染、第一人称生物的渲染等各自客户端渲染模块

### 架构转换映射表

26.x（手写多平台）与 1.21.x（Architectury）差异非常大，里面有一些等价关系：

| 26.x 模式 | 1.21.x 等价 |
|-----------|-------------|
| `Platform.load(Xxx.class)` 窄服务 | `@ExpectPlatform` static method |
| `@PlatformEntry` 静态字段 | `@ExpectPlatform` static method，或用现有 registry facade |
| `ClientEvents` / `ServerEvents` 静态 list | 现有 `ClientEventRegistry` / `ServerEventRegistry` facade |
| `KeyMappings` 静态定义类 | 现有 `KeyMappingRegistry` facade |
| `Commands` 静态定义类 | 现有 `CommandRegistry` facade |
| `FabricBiologyDictionary` 等入口 | **不覆盖**，保持目标版本 |
| `PluginLookup + Bridge + Impl` | `PluginLookup` + `@ExpectPlatform static getBridge()` + 平台 `Impl` |

这部分框架改变，要求你必须使用目标平台的方式去实现。

## 各版本 API 差异速查

常见问题就直接基于下表修了，避免每次都吭哧吭哧查半天源码。

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
| `NbtIo.readCompressed(in, accounter)` | `NbtIo.readCompressed(in)` |
| `mob.finalizeSpawn(level, diff, type, data)` 4参 | 5参（多了 `@Nullable CompoundTag`） |
| `ownable.getOwner()` → `LivingEntity` | `ownable.getOwnerUUID()` → `UUID` |
| `VertexConsumer` 分离式方法 | 链式 `.vertex().color().uv().endVertex()` |
| `VertexConsumer` 无 `defaultColor` | 有 `defaultColor`/`unsetDefaultColor` 抽象方法 |
| `SpawnEggItem.byId(entityType)` | 走 `ItemUtils.getSpawnEggItem(entityType)` |

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
