# 高 MC 版本 → 低 MC 版本移植指南

阅读 `minecraft-biology-dictionary-26.2/AGENTS.md`，该规范同样适用于此。

## 版本链与目录

需要用户手动指定回合的内容，通常为 vX.X.X ~ HEAD 的所有内容。

链式移植方法：
- 26.2 移植到 26.1.2
- 移植后的 26.1.2 移植到 1.21.11
- 移植后的 1.21.11 移植到 1.21.1
- 移植后的 1.21.1 移植到 1.20.1

不要 26.2 -> 26.1.2、26.2 -> 1.21.11、26.2 ->1.21.1 ... 相邻版本移植改动更小，全都从 26.2 回合会有大量重复工作。

移植顺序：按上述链路，每做完一个版本停下来让维护者检查，不要连续移植，因为出问题概率还挺大的。

| 目录 | 目标分支 | 架构 | MC | Java | 反射 | `资源`类名 |
|------|----------|------|-----|------|------|----------|
| `minecraft-biology-dictionary-26.2` | `main-26.2` | 手写多平台 | 26.2 | 25 | ✅ 允许 | `Identifier` |
| `minecraft-biology-dictionary-26.1.2` | `main-26.1.2` | 手写多平台 | 26.1.2 | 25 | ✅ 允许 | `Identifier` |
| `minecraft-biology-dictionary-architectury-1.21.11` | `main-architectury-1.21.11` | Architectury | 1.21.11 | 21 | ❌ 禁用 | `Identifier` |
| `minecraft-biology-dictionary-architectury-1.21.1` | `main-architectury-1.21.1` | Architectury | 1.21.1 | 21 | ❌ 禁用 | `ResourceLocation` |
| `minecraft-biology-dictionary-architectury-1.20.1` | `main-architectury-1.20.1` | Architectury | 1.20.1 | 17 | ❌ 禁用 | `ResourceLocation` |

所有目录共享同一 git remote (`git@github.com:xienaoban/minecraft-biology-dictionary.git`)，不同分支。

MC 第一方源码：`mc-source/<MC 版本>/`。

## 工作流程

1. 进入移植目标项目目录，`git pull` 目标分支（有冲突停下询问）
2. 创建本地工作分支（如 `port-v1.2.3-from-26.2`），可自由 commit
3. **获取增量改动清单**：
   - 26.2 → 26.1.2：用户指定要移植的 commit/branch 区域
   - 26.1.2 → 1.21.11：移植 26.1.2 的 `port-v1.2.3-from-26.2`
   - 1.21.11 → 1.21.1：移植 1.21.11 的 `port-v1.2.3-from-26.1.2`
   - 以此类推
4. **逐文件遍历清单**，按 P1→P2→P3 策略处理，每个文件不可跳过
5. 小步提交，编译验证
6. 全部完成后执行完整性检查

### 为什么是增量链路

```
26.2 dev ──diff打入──→ 26.1.2 port-from-26.2 ──diff打入──→ 1.21.11 port-from-26.2 ──diff打入──→ ...
```

全程用 **diff 打入**（以目标底本为基础）。

## 核心原则：机械移植 = 行级一致

所谓机械移植，就是不仅功能一致，还要求源码尽可能 **行级一致**。未来移植任务会非常多，统一不同版本源码可避免逐步失控。

最终要能逐行 diff 审计，**每个与 26.2 dev 原版的差异都必须可归因**。

### P1→P2→P3 策略

| 优先级 | 策略 | 说明 |
|--------|------|------|
| **P1** | 以目标文件为底本，将源 diff 打入（A 新增文件可直接复制，无底本冲突） | 绝不整体复制源文件 |
| **P2** | 编译报错 → 最小 API 适配 | 只改报错行，改动面收窄到最小 |
| **P3** | API 差异太大（渲染管线等）→ 理解原意、等价重写 | 不得已才用，差异需记录归因 |

核心：**绝不整体复制源文件**。以目标文件为底本，用 `git diff <源base>..<源commit> -- <path>` 查看改动，逐处打入目标——只打入逻辑变更，不改架构相关代码（如 `Platform.load()` vs `@ExpectPlatform` 是基线差异，不是本次 diff）。

### 三类文件处理

| 类型 | 处理 |
|------|------|
| **A 新增** | 直接复制（新文件没有底本冲突）→ P2 适配 |
| **M 修改** | 以目标文件为底本，打入源 diff → P2 适配 |
| **D 删除** | 删除目标对应文件，grep 清除残留引用 |
| **R 重命名** | 执行重命名，旧文件删除 |

### 需要手动适配的文件

以下文件不能简单 P1 复制或 diff 打入（版本差异太大），需逐文件手动处理：

- 构建系统：`build.gradle`、`settings.gradle`、`gradle.properties`、`gradle/wrapper/*`、`buildSrc/**`
- 平台入口：`FabricBiologyDictionary.java`、`NeoForgeBiologyDictionary.java`、`BiologyDictionaryFabric.java`、`BiologyDictionaryNeoForge.java` 及其 Client 变体
- Mixin 配置：`*.mixins.json`
- 平台 metadata：`fabric.mod.json`、`META-INF/neoforge.mods.toml`
- access 文件：`*.accesswidener`、`accesstransformer.cfg`
- `architectury.common.json`
- `.codex/`、`AGENTS.md`、`CLAUDE.md`、`TODO.md`
- 图片二进制：`*.png`、`*.aseprite`

## 架构转换映射表

26.x（手写多平台）与 1.21.x（Architectury）之间的等价关系：

| 26.x 模式 | 1.21.x 等价 |
|-----------|-------------|
| `Platform.load(Xxx.class)` 窄服务 | `@ExpectPlatform` static method |
| `@PlatformEntry` 静态字段 | `@ExpectPlatform` static method，或用现有 registry facade |
| `ClientEvents` / `ServerEvents` 静态 list | 现有 `ClientEventRegistry` / `ServerEventRegistry` facade |
| `KeyMappings` 静态定义类 | 现有 `KeyMappingRegistry` facade |
| `Commands` 静态定义类 | 现有 `CommandRegistry` facade |
| `FabricBiologyDictionary` 等入口 | **不覆盖**，保持目标版本 |
| `PluginLookup + Bridge + Impl` | `PluginLookup` + `@ExpectPlatform static getBridge()` + 平台 `Impl` |

## 各段 API 差异速查

### 26.2 → 26.1.2

架构相同，API 差异极小。基本 P1 直接复制即可通过。

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

## 操作流程（Phase by Phase）

### Phase 0：分析

```bash
git diff --name-status origin/main-26.2..origin/dev
```
输出分类汇总：A 新增 / M 修改 / D 删除 / R 重命名。**只看不做**。

### Phase 1：逐文件移植

按依赖从底层到上层（core → session → net → skill/widget → GUI → mixin）。

1. 对 A 文件：直接复制（目标无底本冲突）
2. 对 M 文件：以目标文件为底本，用 `git diff <源base>..<源commit> -- <path>` 查看改动，优先将 diff 打入，失败太多则逐行手动打入目标——只打入逻辑变更，不改架构相关代码。尽可能与源代码行级一致，形成类似 diff apply 的效果。
3. 简单大块改动（如类重命名、包移动）可理解意图后按功能批量进行，不必拘泥于逐行。
4. 对 D 文件：删除
5. 对 R 文件：重命名

每完成一批逻辑相关的文件，编译一次。内容基本对齐后，解决编译问题。

最后按 diff 逐行审计是否机械，有问题再补。

### Phase 2：编译 + 适配

编译报错 → 按 P2 最小适配。常见适配：
- `EntityTypes` → `EntityType`（26.2→旧版）
- `Identifier` → `ResourceLocation`（1.21.1/1.20.1）
- `net.neoforged` → `net.minecraftforge`（1.20.1）
- `Platform.load()` → `@ExpectPlatform`（26.x→Architectury）
- `List.addFirst()` → `list.add(0, e)`（1.20.1）
- 架构差异大到无法 P2 → P3 重写

### Phase 3：完整性检查

```bash
# 被删除的类是否仍有引用
grep -rn "DeletedClassName" --include="*.java" .

# @ExpectPlatform 配对（fabric/ 和 neoforge/或forge/ 都有实现）
grep -rn "@ExpectPlatform" common/ --include="*.java"

# Java 21 API 残留（仅 1.20.1）
grep -rn "\.addFirst(" --include="*.java" .

# neoforge → forge 路径（仅 1.20.1）
grep -rn "neoforge\|net\.neoforged" forge/ --include="*.java"

# 残留 TODO
grep -rn "TODO.*adapt\|TODO.*port" --include="*.java" .
```

### Phase 4：逐文件 diff 审计

对每个移植文件：
```bash
diff <(cd ../minecraft-biology-dictionary-26.2 && git show origin/dev:<path>) <target-path>
```
每个差异归因：P2 适配 / 架构差异 / P3 重写 / 文件不适用。

## 构建命令

### 26.x 项目
```bash
./gradlew common:compileJava fabric:compileJava neoforge:compileJava
./gradlew fabric:runTestServer
```

### Architectury 项目（1.21.x / 1.20.1）
```bash
./gradlew fabric:compileJava neoforge:compileJava    # 1.20.1: forge:compileJava
./gradlew build
./gradlew fabric:runTestServer
```

## 编码约定（来自 AGENTS.md）

- 26.x 之前（1.21.x、1.20.1）**禁止反射**调用 MC 原版，必须用 Mixin（混淆环境）
- 26.x 允许反射但 Mixin 仍优先
- 禁止 `org.jetbrains.annotations.Nullable` 和 `@NotNull`
- `if`/`for` 等必须带花括号
- 避免使用全限定类名，用 `import` + simple name
- 不要格式化、重构或改动已有代码

## 已知坑

1. **不要用 `git am` / `git format-patch`**：大 commit 冲突多，直接逐文件手动移植
2. **不要对 mod 自定义类留 TODO**：先在目标项目 grep 搜索确认是否存在
3. **不要猜测 API 是否存在**：先查 `../mc-source/<version>/`
4. **Mixin target 签名**：跨版本几乎肯定不同，需在目标 MC 源码找对应方法
5. **Accessor 转型**：1.21.1 需要 `(XxxIMixin) (Object) obj` 中间转型
6. **渲染部分**：`ScreenRenderingContext`、Mixin、渲染管线是 P3 重灾区
7. **WSL 编译慢/I/O 错**：让用户在 Windows/IDEA 侧编译
