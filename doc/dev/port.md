# MC 1.21.11 -> 1.21.1 移植指南

本文档总结了将本模组从 MC 1.21.11 移植到 1.21.1 时的经验、策略和已发现的 API 差异。

## 移植策略

### 前提

- 两个分支的功能基线应当齐平（移植的 commit 之前功能一致）
- MC 源码位于 `<project-root>/../mc-source/<mc-version>/`，用于验证 API 差异

### 方法选择

| 方法 | 适用场景 | 注意事项 |
|------|---------|---------|
| **手动逐文件移植** | 渲染相关文件、两个版本 API 差异大的文件 | 推荐。直接读取 1.21.11 的 diff，手动应用到 1.21.1 的文件上 |
| **Cherry-pick** | 纯逻辑文件（无渲染/注册/API 差异） | 会有大量冲突，尤其渲染文件几乎无法自动解决 |
| **直接复制新文件并适配** | 全新文件（两个分支上都不存在的前置文件除外） | 新文件直接从 1.21.11 复制，然后做 API 适配 |

### 流程

1. 分析 commit 改动范围（`git show --stat <commit>`）
2. 分类：新增文件 / 修改文件 / 删除文件
3. 对修改文件，分析哪些是功能改动、哪些是 MC 版本适配
4. 新增文件：直接复制 + API 适配
5. 修改文件：手动将功能 diff 应用到 1.21.1 版本上
6. 编译验证

### 遇到不确定的情况

- **先问再做**：如果某个 API 在 1.21.1 不存在或有显著差异，不要猜测，留下 `// TODO: adapt for 1.21.1` 注释并询问
- **优先查 MC 源码**：`<project-root>/../mc-source/1.21.1/` 中搜索对应类/方法
- **渲染部分特别小心**：ScreenRenderingContext、FirstPersonShoulderEntityRenderer、Mixin 是重灾区

---

## 已知的 API 差异（1.21.1 vs 1.21.11）

### 1. 资源定位符命名

| 1.21.11 | 1.21.1 |
|---------|--------|
| `net.minecraft.resources.Identifier` | `net.minecraft.resources.ResourceLocation` |
| `Identifier.DEFAULT_NAMESPACE` | `ResourceLocation.DEFAULT_NAMESPACE` |
| `Identifier.tryParse(s)` | `ResourceLocation.tryParse(s)` |
| `Identifier.CODEC` | `ResourceLocation.CODEC` |

**规律**：全项目范围的机械替换。

### 2. 渲染管线（最大的差异）

| 概念 | 1.21.11 | 1.21.1 |
|------|---------|--------|
| **缓冲源** | `SubmitNodeCollector` | `MultiBufferSource.BufferSource` |
| **实体渲染** | `EntityRenderDispatcher.submit(entityRenderState, ...)` | `EntityRenderDispatcher.render(entity, x, y, z, yaw, partialTick, poseStack, bufferSource, light)` |
| **实体渲染状态** | `EntityRenderState` + `extractRenderState()` + `submit()` 模式 | 直接调用 `render()` 方法 |
| **Level 渲染状态** | `LevelRenderState`（含 `cameraRenderState`, `haveGlowingEntities`） | 不存在 |
| **时间增量** | `DeltaTracker` / `client.getDeltaTracker().getGameTimeDeltaPartialTick()` | `float partialTick` 作为参数传递 |
| **Camera 渲染状态** | `CameraRenderState` | 不存在 |
| **Feature 渲染** | `FeatureRenderDispatcher.renderAllFeatures()` | `bufferSource.endBatch()` |
| **PoseStack** | `pushMatrix()` / `popMatrix()` / `scale(x, y)` | `pushPose()` / `popPose()` / `scale(x, y, z)` |
| **ScreenRenderingContext** | `RenderPipeline` + `submitGuiElement()` + `GuiRenderState` | `Lighting` + `RenderSystem` + `BufferBuilder` + `Tesselator` |
| **Screen 字体** | `screen.getFont()` 直接可用 | 需要 `ScreenIMixin` 访问器获取 `Screen.font` |

### 3. 高亮系统（架构完全不同）

| 方面 | 1.21.11 | 1.21.1 |
|------|---------|--------|
| **Entity highlight** | `LevelRendererMixin` 注入 `extractVisibleEntities` + `HighlightRenderer.submit()` | `LevelRendererMixin` 使用 `@ModifyExpressionValue` on `renderLevel` |
| **Block highlight** | `HighlightRenderer.submitBlock()` + `submitNodeCollector.submitBlock()` | 创建 `FallingBlockEntity` 代理 (`ClientHighlightedBlockEntity`) 注入实体渲染流 |
| **HighlightManager** | instance-based (per session) | static 方法 + `ConcurrentHashMap` |
| **HighlightRenderer** | 独立文件，使用新渲染管线 | 不存在（逻辑在 LevelRendererMixin 中） |

### 4. 肩部实体渲染

| 方面 | 1.21.11 | 1.21.1 |
|------|---------|--------|
| **获取肩部实体** | `getShoulderParrotLeft()` / `getShoulderParrotRight()` 返回 `Optional<Parrot.Variant>` | `getShoulderEntityLeft()` / `getShoulderEntityRight()` 返回 `CompoundTag` |
| **创建实体** | `VanillaEntityProperties.OfParrot.createVariantProperty()` 创建 | `EntityType.create(nbt, level)` 从 NBT 创建 |
| **渲染参数** | `SubmitNodeCollector` | `MultiBufferSource.BufferSource` |
| **渲染辅助** | `RenderUtils.extractRenderState()`, `RenderUtils.renderBodyOnly()` | 不存在 |

### 5. NBT API

| 1.21.11 | 1.21.1 |
|---------|--------|
| `nbt.getBoolean(KEY)` 返回 `Optional<Boolean>` | `nbt.getBoolean(KEY)` 返回 `boolean`（默认 false） |
| `nbt.getLong(KEY)` 返回 `Optional<Long>` | `nbt.getLong(KEY)` 返回 `long`（默认 0） |
| `nbt.getList(KEY)` 返回 `Optional<ListTag>` | `nbt.getList(KEY, tagType)` 返回 `ListTag`（需指定 tag 类型） |
| `nbt.getCompound(KEY)` 返回 `Optional<CompoundTag>` | `nbt.getCompound(KEY)` 返回 `CompoundTag`（默认空） |
| `nbt.read(name, codec)` / `nbt.store(name, codec, value)` | 不存在 |

**适配规则**：`.orElse(default)` → 直接返回（默认值由方法保证）；`.getList(key)` → `.getList(key, ListTag.TAG_COMPOUND)`（或相应的 tag type 常量）。

### 6. 注册表 / Tag 系统

| 1.21.11 | 1.21.1 |
|---------|--------|
| `registryAccess.lookupOrThrow(Registries.X)` | `registryAccess.registryOrThrow(Registries.X)` |
| `biomeEntry.getKey().identifier()` | `biomeEntry.getKey().location()` |
| `holders.key().location()` | `holders.getFirst().location()` |
| `holders.stream()` | `holders.getSecond().stream()` |

### 7. 实体相关

| 1.21.11 | 1.21.1 |
|---------|--------|
| `EntitySpawnReason.NATURAL` | `MobSpawnType.NATURAL` |
| `ownable.getOwnerReference().getUUID()` | `ownable.getOwner().getUUID()` |
| `EntityReference<T>` | 不存在，直接用 `getOwner()` |

### 8. 权重随机

| 1.21.11 | 1.21.1 |
|---------|--------|
| `Weighted<T>` | `WeightedEntry` / 直接用 `SpawnerData` |
| `WeightedList<T>` | `WeightedRandomList<T>` |
| `weighted.value()` 访问包装值 | `WeightedRandomList.unwrap()` 直接返回 `List<SpawnerData>` |
| `spawnerData.type()` 方法访问 | `spawnerData.type` 公共字段直接访问 |

### 9. Structure 生成覆盖

两个版本都存在 `Structure.spawnOverrides()`，返回 `Map<MobCategory, StructureSpawnOverride>`，`StructureSpawnOverride.spawns()` 返回 `WeightedRandomList<SpawnerData>`（1.21.1）。

### 10. 权限系统

| 1.21.11 | 1.21.1 |
|---------|--------|
| `Permissions.COMMANDS_ADMIN` | `source.hasPermission(2)` |

### 11. 生物分类

| 1.21.11 | 1.21.1 |
|---------|--------|
| `net.minecraft.world.entity.animal.fish.WaterAnimal` | `net.minecraft.world.entity.animal.WaterAnimal` |
| `net.minecraft.world.entity.animal.AgeableWaterCreature` | 不存在 |
| 实体包结构有大量子包重组（feline, equine, cow, chicken, fox, goat 等） | 较平的包结构 |

### 12. 物品/数据组件

| 1.21.11 | 1.21.1 |
|---------|--------|
| `CustomDataIMixin` 访问 `CustomData.tag` | `CustomData.contains(key)` 直接可用 |
| `CustomModelData(int)` 构造函数 | `CustomModelData(int)` 相同 |

### 13. Packet 系统

两个版本的 payload-based packet 系统架构相同。唯一区别是 `ResourceLocation` vs `Identifier` 的命名。

### 14. Mixin 差异

| Mixin | 1.21.11 | 1.21.1 |
|-------|---------|--------|
| **LevelRendererMixin** | `@Inject` on `extractVisibleEntities` + `submitEntities` | `@ModifyExpressionValue` on `renderLevel` |
| **ItemInHandRendererMixin** | `SubmitNodeCollector` 参数，target `FeatureRenderDispatcher.renderAllFeatures()` | `MultiBufferSource.BufferSource` 参数，target `bufferSource.endBatch()` |
| **GuiGraphicsIMixin** | 有 `getScissorStack()` + `getGuiRenderState()` | 只有 `getScissorStack()` |
| **ScreenIMixin** | 不存在 | 存在 — 访问 `Screen.font` |
| **GuiTextRenderStateIMixin** | 存在 — 访问 `GuiTextRenderState` 字段 | 不存在 |
| **FallingBlockEntityIMixin** | 不存在 | 存在 — `setBlockState` 访问器 |
| **FallingBlockRendererMixin** | 不存在 | 存在 |
| **AbstractClientPlayerIMixin** | 不存在 | 存在 |
| **CustomDataIMixin** | 存在 — `CustomData.tag` 访问器 | 不存在 |

### 15. NeoForge 入口

| 1.21.11 | 1.21.1 |
|---------|--------|
| `BiologyDictionaryNeoForge()` 无参构造 | `BiologyDictionaryNeoForge(IEventBus modBus)` 有参构造 |
| 同理 Client / Server | 需要手动注册 lifecycle events |

---

## 移植实例：v0.8.1 Support Discovery

### 改动概要

- **新增文件**：29 个（discovery 系统、session 系统、命令系统、网络包、辅助类）
- **修改文件**：~50 个（入口点、配置、GUI、网络、技能、Mixin 等）
- **删除文件**：1 个（`EntityTypeOverviewCache.java`）
- **跳过**：ScreenRenderingContext 剪影效果（1.21.11 的 `EntityRenderState.isInvisible` 方案在 1.21.1 不可用）

### 新增文件适配要点

| 文件 | 适配内容 |
|------|---------|
| `SavedDataDiscoveryStorage` | `Identifier`→`ResourceLocation`，NBT optional API→直接返回 |
| `EntitySpawnManager` | `Identifier`→`ResourceLocation`，`lookupOrThrow`→`registryOrThrow`，`identifier()`→`location()`，`Weighted`→`SpawnerData` 直接访问，`EntitySpawnReason`→`MobSpawnType` |
| `EntityOverviewCache` | `EntitySpawnReason`→`MobSpawnType` |
| Discovery 网络包 | `Identifier`→`ResourceLocation` |
| `CommandManager` | `Permissions.COMMANDS_ADMIN`→`source.hasPermission(2)` |
| 其他新文件 | 无需适配或仅需 import 调整 |

### 修改文件适配要点

| 文件 | 适配内容 |
|------|---------|
| `BiologyDictionary.java` | 移除 `servers` set 和 `justGiveMeALevel()`，改用 `WorldSession`/`ServerWorldSession` |
| `BiologyDictionaryClient.java` | 移除直接引用 `HighlightManager`/`FirstPersonShoulderEntityRenderer`/`EntityManager`，改用 session |
| `Configs.java` | 新增 `discoveryStrategy` 和 `allowOverviewForUndiscoveredEntities` 配置项，移除 `skillCostsCache` 字段 |
| `ConfigsManager.java` | 移除 `broadcast()`，新增 `onUpdated()` + `broadcastServerConfigs()` |
| `HighlightManager` | static→instance（但保持 1.21.1 的渲染实现不变） |
| `EntityManager` | 移除 static singleton，改为 `create(Level)` 工厂方法 |
| `SkillCost.java` / `EntityTargetedSkill.java` / `GeneralSkill.java` | `ConfigsManager.getServer().getSkillCost()`→`WorldSession.get().getSkillCostsCache().getSkillCost()` |

---

## 常见问题

### Q: 如何判断一个 diff 是功能改动还是 MC 版本适配？

A: 对比 c3a41b6（1.21.11 基线）和当前 1.21.1 分支的同一文件。如果差异已存在于基线中，说明是 MC 版本差异，移植时保持 1.21.1 的实现；如果差异仅出现在目标 commit 中，说明是功能改动，需要移植。

### Q: 遇到 1.21.11 独有的类/方法怎么办？

A: 优先在 MC 1.21.1 源码中搜索替代方案。如果没有直接替代，考虑：
1. 是否可以简化实现（降级功能）
2. 是否可以用不同方式达到相同效果
3. 如果确实无法实现，留下 TODO 并询问

### Q: ScreenRenderingContext 怎么处理？

A: 这是重灾区。1.21.11 使用 `RenderPipeline` + `submitGuiElement()` + `EntityRenderState`，1.21.1 使用 `Lighting` + `RenderSystem` + `BufferBuilder` + 直接 `entityRenderDispatcher.render()`。移植时需要完全理解 1.21.11 的渲染逻辑，然后用 1.21.1 的 API 重新实现。**不要尝试机械替换**。

### Q: Mixin 怎么处理？

A: Mixin 的 target method descriptor（方法签名）在不同 MC 版本中几乎肯定不同。需要：
1. 在 MC 1.21.1 源码中找到对应的方法
2. 理解 1.21.11 mixin 要拦截的逻辑
3. 用 1.21.1 的方法签名和上下文重新编写 @Inject/@ModifyExpressionValue 等
