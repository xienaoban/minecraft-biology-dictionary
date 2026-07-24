# 重构 EntityManager 框架

## 目标

将“实体类型是否被词典收录”与“当前能否创建实体实例、渲染模型、提供实例资料”彻底分离。

`EntityType` 是词典条目的唯一身份。创建或渲染失败只降低条目的可用能力，绝不能让条目从首页、标签、搜索或发现统计中消失。

## 问题根源

当前 `EntityClassInfo` 同时承担了以下职责：

- 词典条目：`EntityType` 与词典显示顺序；
- 实体类与尺寸等实例派生资料；
- Java 类树、默认分类和属性页的输入；
- 可以创建 GUI 展示模型的前提。

初始化时创建失败会使条目不进入 `EntityClassInfo` 集合；后续首页又会跳过后来创建失败的 type。发现进度仍按原条目列表统计，因此总数和实际展示数可能不一致。渲染失败则另存于客户端会话中的独立集合，形成第二套状态。

## 核心模型

每个被词典收录的 `EntityType` 恰好对应一个、且始终存在的条目：

```text
EntityType
   │
   └── EntityDictionaryEntry
          ├── type
          ├── sortId
          ├── clazz?
          └── instanceCreationFailed
```

`EntityDictionaryEntry` 可以替代或重构现有 `EntityClassInfo`。它首先是词典目录条目，而不是“成功创建过实体后才存在的信息”。

id、名称和 namespace 不作为条目字段缓存：分别由 `EntityType.getKey(type)`、
`type.getDescription()` 和 id 的 namespace 稳定派生，这些读取不属于实体能力探测，不进入失败状态。
`EntityType.getDimensions()` 同理可直接读取，不再保存现有的 `box`。

`sortId` 是 Biology Dictionary 按自定义规则计算的显示顺序，不是实体身份，也不是注册表数字 id。
在不存在显式 `EntityOrder` 的情况下，`isInstanceCreationFailed()` 为 `true` 的条目排在同一
namespace 的末尾；该排序只根据初始化探测结果计算，不因后续展示创建失败而改变。

### 条目的最终字段

`EntityDictionaryEntry` 只保存：

| 字段 | 类型 | 含义 |
| --- | --- | --- |
| `type` | `EntityType<?>` | 条目的唯一身份 |
| `sortId` | `int` | 词典显示顺序 |
| `clazz` | `final Class<? extends Entity>`，可缺失 | 条目构造时立即创建临时实体取得的 class |
| `instanceCreationFailed` | `boolean` | 本世界会话内是否已发生过实例创建失败 |

`clazz` 与 `instanceCreationFailed` 允许同时存在：初始探测可能已经取得 class，后续创建展示实例仍可能失败。
业务层通过 `getClazz()` 读取 class，通过 `isInstanceCreationFailed()` 判断创建状态。
首次失败时写入完整异常日志，不保存异常或失败摘要。

`tags`、id、字符串 id、名称、namespace、dimensions、living 判定和 properties 都不是条目字段。
tag 成员关系由 `Tag` 单向保存；其余信息由 `type`、`clazz` 或当前 target entity 派生。

## 收录范围

初始化遍历注册表时，每个应收录的 `EntityType` 都立即创建临时实体，并在构造
`EntityDictionaryEntry` 时一次性初始化其 `final clazz`。创建失败时 `clazz` 为缺失值，但条目仍照常构造。
全部条目构造完成后，仅使用具有 `clazz` 的条目构建 class tree 和依赖 class 的分类。

未启用 feature 的 type 不收录；`PLAYER` 收录，但不为它提供特殊 class 或展示模型处理。
创建成功但不是 `LivingEntity` 的 type 不收录。
创建失败时无法判定其类，暂按 `LivingEntity` 收录；这类条目排在同一 namespace 的末尾，除非它具有显式 `EntityOrder`。
运行时失败不再影响收录范围。

全局不变量：

> 首页、标签、搜索、选择模式和发现进度引用同一个条目集合。条目要么因明确的收录策略处处不存在，要么处处存在，不能因运行时异常局部消失。

“只显示已发现”时，已发现但不可创建的条目仍必须显示为降级卡片。

## 能力状态

只保留两条独立、可累积的能力状态：

| 能力 | 含义 | 失败后的影响 |
| --- | --- | --- |
| `instance` | 能否按 `EntityType` 创建词典展示或探测所需的实例 | 无法取得 class、依赖实例的属性页和真实模型 |
| `render` | 能否在 BD GUI 中成功渲染真实模型 | 改用盔甲架占位模型，其他信息保留 |

二者不互斥。成功创建但渲染失败的条目仍拥有完整实例资料；创建失败时真实模型不可用。

将 instance 标记为失败时，只在 `instanceCreationFailed` 从 `false` 变为 `true` 的第一次记录完整日志；
之后保持 `true` 并不重复打印。内存只保存失败状态，UI 使用固定提示。

所谓“黑名单”仅是本次世界会话内避免重复创建或每帧重复抛异常的缓存策略，不应成为条目是否存在的设计概念。

## 实体资料探测

对每个条目立即进行一次初始化探测：

1. 尝试创建临时实体；
2. 成功时只记录实体 class，其他资料从 `EntityType`、class 或实际 target entity 派生；
3. 失败时仅标记 `instance` 失败，条目仍保留；
4. 再根据已取得的资料建立标签和分类。

`EntityType` 无法可靠地直接反推实体 class。其创建器在运行时可能是 lambda、方法引用或包装逻辑，泛型也已擦除；Minecraft API 没有稳定的 class 查询接口。因此，成功创建临时实例并读取 `getClass()` 是正式路径。失败时 class 保持未知，不应靠反射猜测。

从 factory 字节码猜 class 也不作为备用路径。`EntityFactory.create` 擦除后只能看到
`Entity`返回类型；构造器方法引用的目标存在于注册调用点的 `invokedynamic` 参数中，运行时 factory
通常是不可直接读取 class 文件的 hidden lambda class。而包装 factory 还可能根据捕获参数、配置或分支
创建不同 class。字节码分析无法对任意模组注册提供唯一、稳定的答案。

如果未来确实要让“无法创建但必须进入 class tree”的类型拥有 class，可靠方式是提供显式的
`EntityType -> Class` 兼容注册，不是扫描 factory 字节码。

不要让业务层散落 `getClazz() == null`。条目应提供能力导向 API，例如：

```text
entry.getClazz()
entry.isInstanceCreationFailed()
```

对于没有实例资料的条目：

- 进入 namespace tag 和 MC registry tag；
- 不进入 Java class/interface tree 和依赖 `instanceof` 的默认分类；
- 禁用或隐藏依赖实体实例的资料区；
- 仍展示 type 的名称、id、namespace 与不可用说明。

默认分类新增“实例创建失败”一类，收录初始化探测阶段 `isInstanceCreationFailed()` 为 `true` 的条目。
后续创建展示实例失败仍会将该状态设为 `true` 并记录首次异常，但不改变默认分类或词典排序。

## target entity 与 display model

禁止将“model entity 必须与 target entity 同 type”作为跨层隐含约束。

- `target entity`：玩家实际查看、发现或交互的实体，用于发现记录、NBT 和属性资料；
- `display model`：仅为 GUI 呈现创建的实体，通常同 type，失败时可为盔甲架。

所有面向实体的 Home widget、Overview screen 和 Detail screen 均在构造时取得并保存其
`EntityDictionaryEntry`。条目始终用于表达实体身份；实际 target entity 可以为 `null`，但渲染用的 entity 不可以为 `null`。
没有词典条目的非 living entity 不允许打开 Detail screen。

界面统一请求展示模型：

```text
DisplayModel createDisplayModel(entry, level)
DisplayModel createDisplayModel(entry, target entity)
├── renderedEntity
├── sourceType
└── kind: ORIGINAL | PLACEHOLDER
```

展示模型工厂集中处理创建失败和盔甲架降级。如果原模型无法创建或为 `null`，统一改用盔甲架；
盔甲架作为原版已知可创建的占位模型，因此 `renderedEntity` 始终非空。只有模型工厂决定是否同步 NBT：

- `ORIGINAL`：同步 target 的展示 NBT；
- `PLACEHOLDER`：不写入 target NBT，仅配置盔甲架外观。

卡片、详情标题、发现统计和选中项始终以 `sourceType` 为准，不能因展示模型被替换而改变实体身份。

## 渲染降级

渲染故障由展示层统一处理，页面不自行捕获后决定降级：

1. 首次渲染真实模型失败时，记录该 type 的 `render` 失败状态和完整日志；
2. 本次会话后续直接渲染 placeholder，避免每帧重复抛异常；

渲染状态是客户端会话性质，由 `ClientWorldSession` 以 `Set<EntityType<?>>` 保存已失败的 type，对外只提供状态查询。

`EntityManager` 只维护词典目录与静态探测结果，不负责 GUI 模型创建或每帧渲染故障；展示模型工厂和渲染器负责模型与降级。

## UI 合同

页面不再因创建失败而 `continue`。每个条目都生成正常或降级视图：

- 正常条目：真实展示模型和完整属性、标签；
- 降级条目：盔甲架模型、type 名称/id/namespace，以及简短不可用说明；仅展示不依赖实例的资料。

建议的提示文案：

- 无法创建此实体的展示实例，部分资料不可用。
- 此实体的模型渲染失败，现以占位模型显示。

详情页必须仍可打开。`BdAboutScreen` 的失败列表仅作为诊断汇总，应按创建失败、渲染失败、资料探测失败分类，不能作为玩家得知条目消失的唯一途径。

## 发现进度

发现进度唯一口径为当前页面使用的条目集合：

```text
total      = 当前筛选后的 entries 数
discovered = 其中 discoveryCache 已发现的 entries 数
visible    = 上述 entries 经“仅显示已发现”等显示筛选后的数
```

创建和渲染失败均不参与排除，只改变卡片的呈现能力。这样新加入一个创建失败的实体时，发现总数、搜索结果、首页条目和选择模式数量始终一致。

## 实施顺序

1. 将 `EntityClassInfo` 改为始终存在的 `EntityDictionaryEntry`，删除“创建失败就不加入目录”的路径；
2. 将创建失败状态并入条目，删除 `failedCreatedEntityTypes` 与首页跳过失败条目的逻辑；默认分类和排序仅使用初始化探测结果；
3. 收敛 model 创建、placeholder 选择与 NBT 同步到单一展示模型工厂；
4. 将渲染失败收敛为客户端会话的状态查询，页面只调用统一展示接口；
5. 重建 tags、class tree 和默认分类，使其显式依赖拥有实例资料的条目子集。
