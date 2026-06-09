# 跨版本移植指南

本文档为本模组在不同 MC 版本之间移植功能时提供通用策略、已知 API 差异和踩坑经验。

## 术语约定

- **源分支 (source)**：功能已经实现的高版本分支（1.21.1，NeoForge）
- **目标分支 (target)**：需要被移植到的低版本分支（1.20.1，Forge）
- **MC 源码**：位于 `../mc-source/<mc-version>/`，用于验证 API

## 移植策略

### 前提

- 两个分支的功能基线应当齐平（移植的 commit 之前功能一致）
- 不要只看 commit diff，diff 之外可能有隐含的依赖关系

### 方法选择

| 方法 | 适用场景 | 注意事项 |
|------|---------|---------|
| **手动逐文件移植** | 涉及新文件、平台映射、API 差异大的 commit（**推荐默认方法**） | 读取源分支的 diff，手动应用到目标分支 |
| **直接复制新文件并适配** | 全新文件 | `git show <commit>:<path>` 提取 + API 适配 |
| **git format-patch + git am** | 文件数少（≤ 10）、纯修改无新文件、无平台映射的 commit | 冲突少时效率最高；大 commit 冲突率极高，不推荐 |

> **教训**：实测 ~78 文件的 commit 使用 `git am` 产生 ~15 个冲突，最终全部 abort 改为手动移植。
> 大 commit（涉及新文件 + 平台映射 + 大量修改）直接手动移植更可控。

### 流程

1. 分析 commit 改动范围（`git diff-tree --no-commit-id --name-only -r <commit>`）
2. 分类：新增文件 / 修改文件 / 删除文件
3. 仅小 commit（≤ 10 文件）尝试 `git format-patch + git am`，大 commit 直接手动移植
4. 新增文件直接提取，修改文件读 diff 手动应用
5. **完整性检查**（见下方清单）
6. 编译验证

### 区分功能改动 vs MC 版本适配

对比源分支的基线和目标分支的同一文件：
- 差异已存在于基线中 → MC 版本差异，移植时保持目标分支的实现
- 差异仅出现在目标 commit 中 → 功能改动，需要移植

### 区分 MC API 差异 vs mod 自定义类

> **这是最容易出错的地方。**

| 类型 | 两个分支间是否一致 | 是否需要 TODO |
|------|---------|--------|
| MC 原生类/方法（如 `VertexConsumer`、`NbtIo`、`Mob.finalizeSpawn`） | **可能不同** | 差异处需要适配，无法适配时留 TODO |
| mod 自定义类/方法（如 `DiscoveryRecord`、`ConfigsManager.onUpdated()`） | **一致** | **不要留 TODO**，直接使用 |

**验证方法**：先在 `../mc-source/<version>/` 中搜索确认 MC API 是否存在，再在目标分支项目中 grep 搜索确认 mod 自定义类是否存在。

> **教训**：AI agent 经常会对实际存在的 API/mod 类误留 TODO（因为名称暗示了版本差异）。
> 本次移植中，`DiscoveryRecord`、`ServerboundClientCommandPacket`、`isAllowOverviewForUndiscoveredEntities`
> 都被 agent 误判为"1.20.1 不存在"，但实际上都存在。

### 平台差异

| 方面 | 源分支 (1.21.1) | 目标分支 (1.20.1) |
|------|---------|--------|
| **Mod Loader** | NeoForge | Forge |
| **平台目录** | `neoforge/` | `forge/` |
| **构造函数** | `BiologyDictionaryNeoForge()` 无参 | `BiologyDictionaryNeoForge(IEventBus modBus)` 有参 |
| **命令注册** | `@SubscribeEvent` on `RegisterCommandsEvent` | 相同，但需要 `@Mod.EventBusSubscriber` 注解 |
| **玩家登录事件** | `PlayerEvent.PlayerLoggedInEvent` | Fabric: `ServerPlayConnectionEvents.JOIN`；Forge: `PlayerEvent.PLAYER_JOIN` |

移植时注意：源分支中 `neoforge/` 目录下的文件对应目标分支的 `forge/` 目录。
新文件可能是 neoforge 专有实现，需要编写对应的 forge 实现。

### 移植纪律

- **禁止自作主张调整移植代码**：不要对移植内容做代码格式化、翻译修改、或忽略细微差异。若无冲突应原封不动地移植。仅当存在冲突或方案替换时才调整代码，且应尽可能减小修改面
- **先验证再决定**：如果某个 API 在目标版本是否存在不确定，不要猜测，先搜索确认
- **优先查 MC 源码**：在目标版本的 MC 源码中搜索对应类/方法
- **渲染部分特别小心**：`ScreenRenderingContext`、Mixin、渲染管线是重灾区

---

## 完整性检查清单

移植完成后，**必须**执行以下检查，不要只验证 commit diff 覆盖的文件：

### 必做检查（每次移植都要执行）

1. **被删除的类**：`grep -rn "DeletedClassName" --include="*.java" .`
2. **`@ExpectPlatform` 配对**：确认 fabric/ 和 forge/ 都有对应实现
3. **残留的 TODO**：`grep -rn "TODO.*adapt\|TODO.*1.21.1\|TODO.*1.20.1" --include="*.java" .`
   - 对每个 TODO 验证是否真的需要：MC API 差异 → 保留；mod 自定义类 → 删除 TODO 并实现
4. **Java 版本不兼容 API**：
   - `grep -rn "\.addFirst(" --include="*.java" .` （Java 21 only）
   - `grep -rn "ByteBufferBuilder" --include="*.java" .` （1.21.1 only）
5. **被修改签名的方法**：确认所有调用者都已更新
6. **残留的旧 API 调用**（根据本次移植的实际改动）：
   - `grep -rn "ConfigsManager\.broadcast" --include="*.java" .`
   - `grep -rn "EntityManager\.getInstance" --include="*.java" .`
7. **neoforge → forge 路径**：`grep -rn "neoforge\|net\.neoforged" forge/ --include="*.java"`
8. **static → instance 转换**：区分「类型引用」和「方法调用」——内部类类型引用（如 `HighlightManager.HighlightedBlock`）不需要改，只有静态方法调用需要改

### 按需检查（根据本次移植涉及的内容）

9. **import 残留**：确认没有 import 已删除/重命名的类
10. **被注释掉的代码**：`grep -rn "// public static\|// public void" --include="*.java" .`
    - 确认没有应该取消注释但遗漏的方法

> **不要依赖编译来发现遗漏**：编译能发现语法错误，但无法发现语义上的遗漏。完整性检查应该在编译之前就做。

---

## 已知的 API 差异（1.21.1 vs 1.20.1）

> 1.21.1 和 1.20.1 的 MC 第一方逻辑大部分相同，差异较少。

### Java 版本差异

| 1.21.1 (Java 21) | 1.20.1 (Java 17) |
|---------|--------|
| `List.addFirst(e)` | `list.add(0, e)` |
| `List.addLast(e)` | `list.add(list.size(), e)` |
| `SequencedCollection` | 不存在 |
| `ByteBufferBuilder` | `BufferBuilder` |

### NBT

| 1.21.1 | 1.20.1 |
|---------|--------|
| `NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap())` | `NbtIo.readCompressed(in)` |
| `NbtAccounter.unlimitedHeap()` | `NbtAccounter.UNLIMITED`（常量） |
| Optional 返回风格的 NBT get 方法 | 直接返回默认值 |

### 实体

| 1.21.1 | 1.20.1 |
|---------|--------|
| `MobSpawnType` | `MobSpawnType`（相同，但在 1.21.1 中已被 `EntitySpawnReason` 替代） |
| `mob.finalizeSpawn(level, difficulty, spawnType, spawnData)` （4 参数） | `mob.finalizeSpawn(level, difficulty, spawnType, spawnData, tag)` （5 参数） |
| `ownable.getOwner()` 返回 `@Nullable LivingEntity` | `ownable.getOwnerUUID()` 返回 `@Nullable UUID` |

### VertexConsumer / 渲染

| 1.21.1 | 1.20.1 |
|---------|--------|
| `new VertexConsumer() { addVertex(); setColor(); }` （分离式方法） | `.vertex().color().uv().endVertex()` （链式方法） |
| `VertexConsumer` 无 `defaultColor`/`unsetDefaultColor` | `VertexConsumer` 有 `defaultColor(int,int,int,int)` 和 `unsetDefaultColor()` 抽象方法 |
| NOOP 实现只需 override `addVertex`/`setColor` 等 | NOOP 实现还必须 override `defaultColor`/`unsetDefaultColor` |
| `DefaultedVertexConsumer` 用于持有默认颜色 | 相同 |

> **渲染适配策略**：对于需要 override VertexConsumer 的场景，优先继承 `DefaultedVertexConsumer`
> 而非直接实现 `VertexConsumer`，可以自动获得 `defaultColor`/`unsetDefaultColor` 的默认实现。

### Network / Packet

| 1.21.1 | 1.20.1 |
|---------|--------|
| `ServerboundClientCommandPacket` | 相同（**存在**，不要误判为不存在） |

### Forge 入口

| 1.21.1 (NeoForge) | 1.20.1 (Forge) |
|---------|--------|
| 无参构造 `BiologyDictionaryNeoForge()` | 有参构造 `BiologyDictionaryNeoForge(IEventBus modBus)` |
| `neoforge/` 平台目录 | `forge/` 平台目录 |

### 刷怪蛋获取

| 1.21.x (NeoForge/Fabric) | 1.20.1 (Forge/Fabric) |
|---------|--------|
| `SpawnEggItem.byId(entityType)` | `ItemUtils.getSpawnEggItem(entityType)` |

> 涉及 `SpawnEggItem.byId` 的地方统一走 `ItemUtils.getSpawnEggItem`，否则 1.20.1 Forge 上模组生物刷怪蛋可能无法显示或发放。

---

## 踩坑记录

### v0.8.1 移植（2026-04）

1. **`git am` 对大 commit 不可用**：78 文件的 commit 产生 15 个冲突，全部 abort 改为手动
2. **AI agent 误留 TODO**：
   - `DiscoveryRecord` — mod 自定义类，两分支一致，agent 误判为"1.20.1 不存在"
   - `ServerboundClientCommandPacket` — MC 原生类，1.20.1 中存在，agent 误判
   - `isAllowOverviewForUndiscoveredEntities` — Configs 中已有此字段，agent 误判
3. **Java 21 API 残留**：`List.addFirst()` 是 Java 21 方法，1.20.1 运行在 Java 17 上
4. **VertexConsumer NOOP 缺少方法**：1.20.1 的 VertexConsumer 接口有 `defaultColor()`/`unsetDefaultColor()` 抽象方法，NOOP 匿名类必须 override
5. **`NbtIo.readCompressed` 签名差异**：1.21.1 接受 `NbtAccounter` 参数，1.20.1 不接受
6. **`Mob.finalizeSpawn` 参数数量**：1.21.1 有 4 参数，1.20.1 有 5 参数（多了 `@Nullable CompoundTag`）
7. **并行 agent 修改同一文件**：不同 agent 可能对同一文件的不同行做 Edit，后运行的可能覆盖先运行的改动。Phase 3 可以捕获
8. **`ownable.getOwner().getUUID()`**：1.21.1 中 `getOwner()` 返回 `LivingEntity`（可能为 null），1.20.1 中改为 `getOwnerUUID()` 直接返回 `UUID`（可能为 null）
9. **模组刷怪蛋显示/发放**：涉及 `SpawnEggItem.byId(entityType)` 的地方统一走 `ItemUtils.getSpawnEggItem(entityType)`。
