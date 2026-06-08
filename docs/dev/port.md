# 跨版本移植指南

MC 源码位于 `../mc-source/<mc-version>/`，用于验证 API。

## 移植纪律

- **禁止自作主张调整移植代码**：不要格式化、翻译修改、忽略细微差异。若无冲突原封不动移植，仅 API 差异允许调整且修改面尽可能小
  - 注释和 Javadoc 必须原封不动保留
  - 空行、缩进、命名必须一致
  - 旧类名/方法名引用也要更新（测试代码中的字符串常量等）
- **移植后必须逐文件 diff 审计**：`git show <commit>:<path>` vs 本地，每个差异都需 API 适配理由
- **先问再做**：API 不确定留 `// TODO` 并询问
- **渲染部分特别小心**：`ScreenRenderingContext`、Mixin、渲染管线是重灾区
- **非代码文件也要移植**：`.md`（changelog 等）、lang json、配置文件等，凡是 patch 涉及修改的都要检查

## 完整性检查清单

移植完成后**必须**执行，不要依赖编译发现遗漏：

1. `grep -rn "OldClass.methodName" --include="*.java"` — 方法移动后 diff 外的调用者
2. `grep -rn "\.methodName(" --include="*.java"` — 签名变更后所有调用者
3. `grep -rn "DeletedClassName" --include="*.java"` — 被删除的类
4. `grep -rn "@ExpectPlatform" common/ --include="*.java"` — fabric/ 和 neoforge/ 配对
5. 检查 import 残留（已删除/重命名的类）
6. static → instance 转换：类型引用不需要改（如 `HighlightManager.HighlightedBlock`），只有静态方法调用需要改
7. `.md`、`.json`、changelog 等非代码文件的变更

## 已知的 API 差异（1.21.1 vs 1.21.11）

### 1. 资源定位符

| 1.21.11 | 1.21.1 |
|---------|--------|
| `Identifier` | `ResourceLocation`（全项目机械替换，含 import） |
| `new Identifier("namespace:path")` | `ResourceLocation.tryParse("namespace:path")`（返回 `@Nullable`） |

### 2. 渲染管线（最大差异，不要机械替换）

| 概念 | 1.21.11 | 1.21.1 |
|------|---------|--------|
| **缓冲源** | `SubmitNodeCollector` | `MultiBufferSource.BufferSource` |
| **实体渲染** | `EntityRenderDispatcher.submit(entityRenderState, ...)` | `EntityRenderDispatcher.render(entity, x, y, z, yaw, partialTick, poseStack, bufferSource, light)` |
| **实体渲染状态** | `EntityRenderState` + `extractRenderState()` + `submit()` | 直接调用 `render()` |
| **Level/Camera 渲染状态** | `LevelRenderState` / `CameraRenderState` | 不存在 |
| **时间增量** | `DeltaTracker` / `client.getDeltaTracker().getGameTimeDeltaPartialTick()` | `float partialTick` 参数 |
| **Feature 渲染** | `FeatureRenderDispatcher.renderAllFeatures()` | `bufferSource.endBatch()` |
| **PoseStack** | `pushMatrix()` / `popMatrix()` / `scale(x, y)` | `pushPose()` / `popPose()` / `scale(x, y, z)` |
| **ScreenRenderingContext** | `RenderPipeline` + `submitGuiElement()` + `GuiRenderState` | `Lighting` + `RenderSystem` + `BufferBuilder` + `Tesselator` |
| **Screen 字体** | `screen.getFont()` 直接可用 | 需要 `ScreenIMixin` 访问器获取 `Screen.font` |

#### ScreenRenderingContext.renderEntity

| 方面 | 1.21.11 | 1.21.1 |
|------|---------|--------|
| **EntityRenderingCache** | 存在，需要传入 cache 参数 | 不存在 |
| **调用方式** | `ctx.renderEntityCentered(entity, cache, left, top, right, bottom, rotateX, rotateY)` | `ctx.renderEntityCentered(entity, left, top, right, bottom, rotateX, rotateY)` |

### 3. 高亮系统

| 方面 | 1.21.11 | 1.21.1 |
|------|---------|--------|
| **Entity highlight** | `@Inject` on `extractVisibleEntities` + `HighlightRenderer.submit()` | `@ModifyExpressionValue` on `renderLevel` |
| **Block highlight** | `HighlightRenderer.submitBlock()` + `submitNodeCollector.submitBlock()` | 创建 `FallingBlockEntity` 代理注入 |
| **HighlightRenderer** | 独立文件 | 不存在（逻辑在 LevelRendererMixin 中） |

### 4. NBT API

| 1.21.11 | 1.21.1 |
|---------|--------|
| `getBoolean/getList/getCompound` 返回 `Optional` | 直接返回值（默认空值） |
| `nbt.read(name, codec)` / `nbt.store(name, codec, value)` | 不存在 |
| `getList(key)` 无类型过滤 | `getList(key, type)` 按 type 过滤，不匹配返回空列表 |

**适配规则**：`.orElse(default)` → 直接返回；`getList(key)` → `getList(key, Tag.TAG_STRING)` 等。

> **踩坑**：`getList(key, TAG_COMPOUND)` 如果元素实际是 `StringTag`，会静默返回空列表不报错。

### 5. 注册表 / 权限 / NeoForge 入口

| 1.21.11 | 1.21.1 |
|---------|--------|
| `registryAccess.lookupOrThrow(Registries.X)` | `registryAccess.registryOrThrow(Registries.X)` |
| `registry.getOptional(id)` 返回 `Optional<Holder<T>>` | 返回 `Optional<T>` |
| `holders.key().location()` | `holders.getFirst().location()` |
| `holders.stream()` | `holders.getSecond().stream()` |
| `Permissions.COMMANDS_ADMIN` | `source.hasPermission(2)` |
| 无参构造 `BiologyDictionaryNeoForge()` | 有参构造 `BiologyDictionaryNeoForge(IEventBus modBus)` |

### 6. 实体相关

| 1.21.11 | 1.21.1 |
|---------|--------|
| `EntitySpawnReason.NATURAL` | `MobSpawnType.NATURAL` |
| `ownable.getOwnerReference().getUUID()` | `ownable.getOwner().getUUID()` |
| `EntityReference<T>` | 不存在，直接用 `getOwner()` |
| `getShoulderParrotLeft()` 返回 `Optional<Parrot.Variant>` | `getShoulderEntityLeft()` 返回 `CompoundTag` |
| `WaterAnimal` 在 `animal.fish` 包 | `WaterAnimal` 在 `animal` 包 |

### 7. 权重随机

| 1.21.11 | 1.21.1 |
|---------|--------|
| `Weighted<T>` / `WeightedList<T>` | `WeightedEntry` / `WeightedRandomList<T>` |
| `weighted.value()` | `WeightedRandomList.unwrap()` |
| `spawnerData.type()` | `spawnerData.type` 公共字段 |

### 8. 数据存储

| 方面 | 1.21.11 | 1.21.1 |
|------|---------|--------|
| **SavedData** | `SavedDataType<T>` + `Codec` | 传统 `SavedData` + `SavedData.Factory<T>` + 手动 `CompoundTag` 读写 |
| **write/read** | `TagValueOutput` / `TagValueInput` + codec | `CompoundTag.put()` / `getCompound()` / `getList()` 等 |
| **注册方式** | `computeIfAbsent(TYPE)` | `computeIfAbsent(FACTORY, "dataId")` |

### 9. Toast / GUI

| 1.21.11 | 1.21.1 |
|---------|--------|
| `ToastManager` | `ToastComponent` |
| `client.getToastManager()` | `client.getToasts()` |
| `guiGraphics.drawString(Component, ...)` | `guiGraphics.drawString(Font, Component, ...)` |
| `@Nullable` (jspecify) | 不使用 |

### 10. 事件 / 输入

| 1.21.11 | 1.21.1 |
|---------|--------|
| `keyPressed(KeyEvent)` | `keyPressed(int keyCode, int scanCode, int modifiers)` |
| `Player.killedEntity(Entity, DamageSource)` | `Player.killedEntity(Entity)` |
| `distanceTo(Entity)` | `distanceToSqr(Entity)` |
| `isWithinEntityInteractionRange(Entity)` | `canInteractWithEntity(Entity)` |
| `Biome.getPrecipitationAt(BlockPos, int)` | `Biome.getPrecipitationAt(BlockPos)` |

### 11. Mixin 差异

Mixin target method descriptor 几乎肯定不同：在目标 MC 源码找对应方法，理解拦截逻辑，用目标签名重写。

**Accessor 编译时转型**（必须 `(Object)` 中间转型）：
```java
// 错误：Cannot cast JigsawStructure to JigsawStructureIMixin
((JigsawStructureIMixin) jigsawStructure).method()
// 正确
((JigsawStructureIMixin) (Object) jigsawStructure).method()
```
