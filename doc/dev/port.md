# 跨版本移植指南

本文档为本模组在不同 MC 版本之间移植功能时提供通用策略、已知 API 差异和踩坑经验。

## 术语约定

- **源分支 (source)**：功能已经实现的高版本分支
- **目标分支 (target)**：需要被移植到的低版本分支
- **MC 源码**：位于 `../mc-source/<mc-version>/`，用于验证 API

## 移植策略

### 前提

- 两个分支的功能基线应当齐平（移植的 commit 之前功能一致）
- 不要只看 commit diff，diff 之外可能有隐含的依赖关系

### 方法选择

| 方法 | 适用场景 | 注意事项 |
|------|---------|---------|
| **手动逐文件移植** | 渲染相关、API 差异大的文件 | 推荐。读取源分支的 diff，手动应用到目标分支 |
| **Cherry-pick** | 纯逻辑文件（无渲染/注册/API 差异） | 会有大量冲突，渲染文件几乎无法自动解决 |
| **直接复制新文件并适配** | 全新文件 | 直接复制 + API 适配 |

### 流程

1. 分析 commit 改动范围（`git diff-tree --no-commit-id --name-only -r <commit>`）
2. 分类：新增文件 / 修改文件 / 删除文件
3. 对修改文件，区分哪些是功能改动、哪些是 MC 版本适配（只移植功能改动）
4. 新增文件：直接复制 + API 适配
5. 修改文件：手动将功能 diff 应用到目标分支上
6. **完整性检查**（见下方清单）
7. 编译验证

### 区分功能改动 vs MC 版本适配

对比源分支的基线和目标分支的同一文件：
- 差异已存在于基线中 → MC 版本差异，移植时保持目标分支的实现
- 差异仅出现在目标 commit 中 → 功能改动，需要移植

### 移植纪律

- **禁止自作主张调整移植代码**：不要对移植内容做代码格式化、翻译修改、或忽略细微差异。若无冲突应原封不动地移植。仅当存在冲突或方案替换时才调整代码，且应尽可能减小修改面
- **先问再做**：如果某个 API 在目标版本不存在或有显著差异，不要猜测，留下 `// TODO` 注释并询问
- **优先查 MC 源码**：在目标版本的 MC 源码中搜索对应类/方法
- **渲染部分特别小心**：`ScreenRenderingContext`、Mixin、渲染管线是重灾区

---

## 完整性检查清单

移植完成后，**必须**执行以下检查，不要只验证 commit diff 覆盖的文件：

1. **被移动/删除的方法**：`grep -rn "OldClass.methodName" --include="*.java" .` — 方法从类 A 移到类 B 时，diff 外的文件可能也在调用
2. **被修改签名的公共方法**：`grep -rn "\.methodName(" --include="*.java" .` — 添加/删除参数后，所有调用者都需要更新
3. **被删除的类**：`grep -rn "DeletedClassName" --include="*.java" .`
4. **`@ExpectPlatform` 配对**：确认 fabric/ 和 neoforge/ 都有对应实现
5. **import 残留**：确认没有 import 已删除/重命名的类
6. **static → instance 转换**：区分「类型引用」和「方法调用」——内部类类型引用（如 `HighlightManager.HighlightedBlock`）不需要改，只有静态方法调用需要改

> **不要依赖编译来发现遗漏**：编译能发现语法错误，但无法发现语义上的遗漏。完整性检查应该在编译之前就做。

---

## 已知的 API 差异（1.21.1 vs 1.21.11）

### 1. 资源定位符

| 1.21.11 | 1.21.1 |
|---------|--------|
| `Identifier` | `ResourceLocation`（全项目机械替换，含 import） |
| `new Identifier("namespace:path")` | `new ResourceLocation(...)` 公共构造可能受限，用 `ResourceLocation.tryParse("namespace:path")`（返回 `@Nullable`） |

### 2. 渲染管线（最大差异）

| 概念 | 1.21.11 | 1.21.1 |
|------|---------|--------|
| **缓冲源** | `SubmitNodeCollector` | `MultiBufferSource.BufferSource` |
| **实体渲染** | `EntityRenderDispatcher.submit(entityRenderState, ...)` | `EntityRenderDispatcher.render(entity, x, y, z, yaw, partialTick, poseStack, bufferSource, light)` |
| **实体渲染状态** | `EntityRenderState` + `extractRenderState()` + `submit()` 模式 | 直接调用 `render()` 方法 |
| **Level/Camera 渲染状态** | `LevelRenderState` / `CameraRenderState` | 不存在 |
| **时间增量** | `DeltaTracker` / `client.getDeltaTracker().getGameTimeDeltaPartialTick()` | `float partialTick` 作为参数传递 |
| **Feature 渲染** | `FeatureRenderDispatcher.renderAllFeatures()` | `bufferSource.endBatch()` |
| **PoseStack** | `pushMatrix()` / `popMatrix()` / `scale(x, y)` | `pushPose()` / `popPose()` / `scale(x, y, z)` |
| **ScreenRenderingContext** | `RenderPipeline` + `submitGuiElement()` + `GuiRenderState` | `Lighting` + `RenderSystem` + `BufferBuilder` + `Tesselator` |
| **Screen 字体** | `screen.getFont()` 直接可用 | 需要 `ScreenIMixin` 访问器获取 `Screen.font` |

移植渲染代码时需要完全理解源版本的渲染逻辑，然后用目标版本的 API 重新实现。**不要尝试机械替换**。

### 3. 高亮系统

| 方面 | 1.21.11 | 1.21.1 |
|------|---------|--------|
| **Entity highlight** | `@Inject` on `extractVisibleEntities` + `HighlightRenderer.submit()` | `@ModifyExpressionValue` on `renderLevel` |
| **Block highlight** | `HighlightRenderer.submitBlock()` + `submitNodeCollector.submitBlock()` | 创建 `FallingBlockEntity` 代理注入实体渲染流 |
| **HighlightRenderer** | 独立文件 | 不存在（逻辑在 LevelRendererMixin 中） |

### 4. NBT API

| 1.21.11 | 1.21.1 |
|---------|--------|
| `getBoolean/getList/getCompound` 返回 `Optional` | 直接返回值（默认空值） |
| `nbt.read(name, codec)` / `nbt.store(name, codec, value)` | 不存在 |
| `getList(key)` 无类型过滤 | `getList(key, type)` **按 type 过滤元素**，不匹配返回空列表 |

**适配规则**：
- `.orElse(default)` → 直接返回
- `getList(key)` → `getList(key, Tag.TAG_STRING)` 等，**type 参数必须与实际写入的元素类型一致**

> **踩坑**：移植时从 1.21.11 复制的 `getList(key, TAG_COMPOUND)` 如果元素实际是 `StringTag`，在 1.21.1 中会静默返回空列表而不报错。

### 5. 注册表 / 权限 / NeoForge 入口

| 1.21.11 | 1.21.1 |
|---------|--------|
| `registryAccess.lookupOrThrow(Registries.X)` | `registryAccess.registryOrThrow(Registries.X)` |
| `registry.getOptional(id)` 可能返回 `Optional<Holder<T>>` | 返回 `Optional<T>`（直接值，非 Holder） |
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
| `WaterAnimal` 在 `animal.fish` 包 | `WaterAnimal` 在 `animal` 包，实体包结构较平 |

### 7. 权重随机

| 1.21.11 | 1.21.1 |
|---------|--------|
| `Weighted<T>` | `WeightedEntry` / 直接用 `SpawnerData` |
| `WeightedList<T>` | `WeightedRandomList<T>` |
| `weighted.value()` | `WeightedRandomList.unwrap()` |
| `spawnerData.type()` | `spawnerData.type` 公共字段 |

### 8. Packet 系统

两个版本的 payload-based packet 系统架构相同。唯一区别是 `ResourceLocation` vs `Identifier`。

### 9. Mixin 差异

Mixin 的 target method descriptor 在不同 MC 版本中几乎肯定不同。需要：
1. 在目标版本的 MC 源码中找到对应方法
2. 理解源版本 mixin 要拦截的逻辑
3. 用目标版本的方法签名重新编写

**Accessor 编译时转型**：Mixin 接口只在运行时注入到目标类，编译时直接转型会失败，**必须通过 `(Object)` 中间转型**：
```java
// 编译错误：Cannot cast JigsawStructure to JigsawStructureIMixin
((JigsawStructureIMixin) jigsawStructure).method()
// 正确
((JigsawStructureIMixin) (Object) jigsawStructure).method()
```

