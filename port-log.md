# 移植审计日志

源项目：`../minecraft-biology-dictionary-architectury-1.21.11`（`main-architectury-1.21.11`）

目标基线：`a1faecd`

范围：所有可维护项目文件。源/目标文件集合对比时排除生成产物、Gradle 缓存、IDE 元数据、run 目录和本地运行时文件。

## 已修改

- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/ExampleMixin.java`
- `common/src/main/resources/biologydictionary.mixins.json`
  - 删除 `ExampleMixin` 及其 mixin 配置项。
  - 原因：这是 26.1.2 模板残留，1.21.11 没有；它只对 `MinecraftServer#loadLevel` 做空注入，没有业务作用，属于多出来的内容。

- `common/src/main/resources/data/biologydictionary/biologydictionary/entity_spawn/*.json`
  - 从 1.21.11 恢复 9 个实体生成覆盖数据：bee、breeze、cave_spider、creaking、elder_guardian、ender_dragon、shulker、vex、warden。
  - 原因：26.1.2 的 `EntitySpawnManager` 仍读取 `biologydictionary/entity_spawn`，目标仓库没有替代数据资源，因此这些是漏移植的行为数据。

- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/client/ClientEventRegistry.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/client/KeyMappingRegistry.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/server/CommandRegistry.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/server/ItemRegistry.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/server/ServerEventRegistry.java`
  - 新增旧 Architectury facade 的空壳占位，带 `TODO: delete after port`。
  - 原因：真实注册已经迁到 `ClientEvents`、`KeyMappings`、`Commands`、`BiologyDictionaryItem` creative tab entry、`ServerEvents` 并由平台入口消费；空壳用于保留迁移索引，避免后续搜索旧代码时漏项。

- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/util/TextUtils.java`
  - 恢复使用 `Lang.TEXT_COMMA`，删除本地重复常量。
  - 原因：`Lang.TEXT_COMMA` 在 26.1.2 仍存在，本地重复常量是无必要差异。

- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/util/ListenerList.java`
  - 恢复 1.21.11 的 Javadoc。
  - 原因：common 注释允许提 Fabric/NeoForge 类；删除注释是无必要差异。

- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/util/DevUtils.java`
  - 恢复原注释、变量名、`Lang.BIOLOGY_DICTIONARY` 和原有布局，只保留 `Platform.load(PlatformBridge.class)` 必要迁移。
  - 原因：去 Architectury 是必要差异；注释删除、变量重命名、常量来源改动、私有构造器新增都不是必要迁移。

- 一批 common 同路径文件
  - 恢复 1.21.11 的纯格式或注释内容，代码 token 不变。
  - 涉及文件：`RequestBeehiveInfoPacket`、配置注解、discovery 接口与简单策略、skill 接口、部分 packet 注释。
  - 原因：这些差异不涉及 26 API，也不改变逻辑；属于无必要的可比较性漂移。

- `common/src/main/java/io/github/xienaoban/biologydictionary/client/TelescopeManager.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/discovery/DelegatingClientDiscoveryCache.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/discovery/DiscoveryManager.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/property/extra/EntitySpawnCountedProperty.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/property/extra/MobTemptProperty.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/SheepForceEatGrassSkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/entity/AnimalMixin.java`
  - 补回 1.21.11 中保留的 Javadoc 和行为说明注释。
  - 原因：这些注释解释目标切换、发现系统职责、mob cap/TemptGoal/EatBlockGoal/繁殖静音继承等行为依据；26.1.2 没有对应 API 迁移理由需要删除它们。

- `README.md`
- `README.zh-CN.md`
- `CHANGELOG.md`
- `LICENSE`
- `docs/assets/*`
- `docs/custom-data.md`
- `docs/custom-data.zh-CN.md`
- `docs/dev/neoforge-dep-architectury-removal.md`
  - 从 1.21.11 恢复项目说明、许可证、更新日志、文档素材、自定义数据文档和 NeoForge 去 Architectury 参考文档。
  - 原因：目标仓库根 `README.md` 只是模板占位，`docs` 下缺少实际用户/开发文档和素材；这些不是 Minecraft 26 或加载器架构差异。

- `common/src/testServer/java/**`
- `fabric/src/testServer/java/**`
- `fabric/src/testClient/resources/fabric.mod.json`
  - 从 1.21.11 恢复 GameTest / 静态检查测试源码和 Fabric 测试资源。
  - 适配点：
    - `AbstractVisitorWrapper`：JavaParser 版本中 Javadoc comment visitor 类型改为 `TraditionalJavadocComment`。
    - `RegistrarsTest`：`PacketPayloads` 已从旧 `registerBuiltIn(Registrar)` facade 迁到 `@PlatformEntry` 的 `ENTRIES` 列表，测试改为遍历 `ENTRIES` 并验证 `FACTORY` 来源。
    - `VanillaEntitySkillTest`：`VariantHandler#variantToNbt` 现在需要 entity 参数。
    - `VanillaEntitySkillTest`：旧 `AgeableMobSetForcedAgeSkill` 在 26.1.2 拆为 `AgeableMobSetBreedingCooldownSkill`、`AgeableMobSetAgeLockedSkill`、`TadpoleSetAgeLockedSkill`，测试工厂同步拆分。
  - 原因：Gradle 中仍配置了 `testServer` sourceSet 和 Fabric GameTest 运行项，但源码缺失；这是漏移植，适配仅限当前主代码 API 变化。

- `fabric/.gitattributes`
- `fabric/.github/workflows/build.yml`
- `fabric/.gitignore`
- `fabric/LICENSE`
- `fabric/README.md`
- `fabric/gradle/wrapper/*`
- `fabric/gradlew`
- `fabric/gradlew.bat`
- `neoforge/.gitattributes`
- `neoforge/.github/workflows/build.yml`
- `neoforge/.gitignore`
- `neoforge/README.md`
- `neoforge/TEMPLATE_LICENSE.txt`
- `neoforge/gradle/wrapper/*`
- `neoforge/gradlew`
- `neoforge/gradlew.bat`
- `fabric/build.gradle`
  - 删除 Fabric/NeoForge 子项目里的独立模板项目残留，并将 Fabric jar 任务的许可证来源改为根 `LICENSE`。
  - 原因：目标仓库是根 `settings.gradle` 管理的多项目，不应在子项目内保留独立 wrapper、workflow、README、模板许可证；Fabric 子项目原 `from("LICENSE")` 会打包模板 CC0 许可证，而不是项目真实许可证。

- `common/src/main/java/io/github/xienaoban/biologydictionary/client/ClientEvents.java`
  - 在 `WORLD_DISCONNECTING` 中恢复清空 `BiologyDictionaryClient` 的命中实体、命中方块和命中实体属性缓存。
  - 原因：1.21.11 的 `BiologyDictionaryClient` 断开世界事件里会清理这些状态；26.1.2 将事件迁到 `ClientEvents` 后漏掉了这部分，可能导致跨世界残留客户端目标状态。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/BiologySkills.java`
  - 恢复源端注册顺序、静态字段位置和 fake entity 注释，只保留 `AgeableMobSetForcedAgeSkill` 拆成三个 26 技能的必要差异。
  - 原因：注册顺序和布局重排没有 26 API 理由，会降低两边可比性；技能拆分是 26 实际行为变化。

- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/gui/screen/util/ScaleRAII.java`
  - 恢复兼容旧 `z` 参数构造器上的 `TODO` 注释和无操作 `translate(0, 0)`。
  - 原因：26.1.2 只需要把 `GuiGraphics` 类型迁到 `GuiGraphicsExtractor`；删除源端兼容注释/无操作调用不是必要迁移。

- `common/src/main/java/io/github/xienaoban/biologydictionary/config/Configs.java`
  - 恢复 1.21.11 源端整文件，包括配置说明注释、`ClientConfigs`/`ServerConfigs` 构造器和 `completeSkillCosts` 的源端写法。
  - 原因：该文件没有 26 API 必要改动；删除构造器会让默认 `ServerConfigs` 在首次使用前不主动补全 `skillCosts`，属于实质漏迁风险。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/property/ExtraEntityProperties.java`
  - 恢复 1.21.11 源端整文件。
  - 原因：目标端仅有 import 展开、静态字段位置移动、私有构造器新增和参数缩进变化，没有 26 API 理由。

- `common/src/main/java/io/github/xienaoban/biologydictionary/net/PacketPayloads.java`
  - 在保留 `@PlatformEntry ENTRIES` 的前提下，恢复 payload 注册顺序与 1.21.11 `registerBuiltIn` 一致。
  - 原因：从 registrar 迁到静态 entry 列表是 26 平台架构必要差异；重排 payload 顺序没有必要理由。

- `common/src/main/java/io/github/xienaoban/biologydictionary/client/HighlightRenderer.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/ClientNetManager.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/ServerNetManager.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/Permissions.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/gui/util/Colors.java`
  - 删除无理由新增的 private 构造器，并恢复网络 manager 的源端方法顺序；`ClientNetManager` 恢复 `@ClientOnly`。
  - 原因：这些构造器和方法重排不是 26 API 迁移需要；`Permissions` 中 `Item#getName()` 到 `ItemStack#getHoverName()` 的行为/API 差异保留。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/BiologyDictionaryItem.java`
  - 恢复 wandering trader 交易前的 `bookItemObtainableFromWanderingTrader` 配置检查，并补回源端书本兼容性、交易概率和翻译说明注释；删除无理由 private 构造器。
  - 原因：目标端漏掉配置判断会导致关闭配置后仍添加 Biology Dictionary 交易，是实质行为回归；`CustomData.copyTag()`、component 写法和 `@PlatformEntry` creative tab entry 是 26 必要差异。

- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/net/ClientNetApi.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/net/ServerNetApi.java`
  - 删除无理由 private 构造器，`ClientNetApi` 恢复 `@ClientOnly` 标注；保留 `Platform.load` bridge。
  - 原因：去掉 `@ExpectPlatform` 是 26 架构必要差异；新增 private 构造器和删除 client-only 语义不是必要迁移。

- `build.gradle`
  - `checkCommonPlatformImports` 从全文搜索改为只检查 `import` 行。
  - 原因：common 源码可以在 Javadoc/注释中提到 Fabric/NeoForge 类；构建检查的目标应是禁止 common 真实导入平台包，而不是禁止注释说明。

- `common/src/main/java/io/github/xienaoban/biologydictionary/config/ConfigsManager.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/session/ClientWorldSession.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/session/ServerWorldSession.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/session/WorldSession.java`
  - 恢复 1.21.11 源端形态。
  - 原因：目标端差异主要是注释删除、getter/字段顺序重排、异常处理和空值保护等非 26 API 必要改动；这些改变会降低可比性，应单独提交而不是混入移植。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/discovery/DiscoveryRecord.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/discovery/storage/SavedDataDiscoveryStorage.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/discovery/strategy/BiologyDictionaryDiscoveryStrategy.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/discovery/strategy/BiologyDictionaryClientDiscoveryCache.java`
  - 补回 discovery 记录/存储/策略/cache 的源端说明注释，恢复 `BiologyDictionaryClientDiscoveryCache#onFullSync` 中的 `this.cache` 写法。
  - 原因：`SavedDataType` id 和 `server.getDataStorage()` 是 26 API 必要差异；删除说明注释和移除 `this.` 不是必要迁移。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/EntityOrder.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/property/EntityProperties.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/property/vanilla/VariantProperty.java`
  - 恢复 1.21.11 源端整文件。
  - 原因：`EntityOrder` 中兔子/鸡顺序互换、`EntityProperties` extra property key 从 Class 改 name、init 顺序重排，以及 `VariantProperty` 局部变量改名都没有 26 API 理由。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/SkillCostsCache.java`
  - 移除目标端新增的 `SkillCost.empty()` fallback，恢复源端 `cache.get(skillClass)`，并补回 cache rebuild 注释。
  - 原因：`Configs.ServerConfigs#completeSkillCosts` 已负责补全所有技能成本；静默 fallback 会掩盖注册/配置缺漏，并可能把异常技能免费化。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/SkillCost.java`
  - 补回源端分段注释和 creative 免费说明；保留 `ItemCost(item,count)` 结构。
  - 原因：注释删除无必要；`ItemCost` 用稳定 item/count 表达技能成本，避免 26 `ItemStack` component 状态混入配置序列化。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/EntityOverviewCache.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/RequestEntityOverviewPacket.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/gui/screen/BdEntityOverviewScreen.java`
  - 给 `CacheEntry` 增加 `isValid()`，服务端 overview 生成失败时以 `notNull=false` 回复，客户端 overview screen 也不再把 `(null, null)` cache entry 当有效数据。
  - 原因：这是 26 目标端新增的服务端 overview cache / 失败 sentinel 特性；当前 26 侧调用方需要配套按无效数据处理。`TODO.md` 中“同步回 1.21.11”的待办保留，不应作为当前分支已完成项删除。

## 已确认的合理差异

- `common/src/main/resources/architectury.common.json`
  - 目标仓库没有恢复。
  - 原因：26.1.2 目标项目不再使用 Architectury。

- `common/src/main/resources/biologydictionary.accesswidener`
  - 从 1.21.11 的 `classTweaker v1 named` / `GuiGraphics` / `GuiTextRenderState` 改为 26.1.2 的 `accessWidener v2 official` / `GuiGraphicsExtractor$ScissorStack`。
  - 原因：26.1.2 只使用 official mappings，GUI 渲染访问点也从旧 `GuiGraphics`/`GuiTextRenderState` 路径迁移到 `GuiGraphicsExtractor`。

- `common/src/main/resources/biologydictionary.mixins.json`
  - `compatibilityLevel` 改为 `JAVA_25`，移除 Architectury/旧 GUI 访问相关 mixin，新增 26.1.2 所需 `ArmadilloStateIMixin`、`GuiGraphicsExtractorIMixin`、`LevelRendererIMixin`。
  - 原因：这些是 Minecraft 26.1.2 与当前渲染/实体 API 的必要差异。

- `common/src/main/java/io/github/xienaoban/biologydictionary/client/KeyMappingManager.java` -> `common/src/main/java/io/github/xienaoban/biologydictionary/client/KeyMappings.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/server/CommandManager.java` -> `common/src/main/java/io/github/xienaoban/biologydictionary/server/Commands.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/client/ClientEvents.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/server/ServerEvents.java`
  - 原因：旧 Architectury event/registry facade 被目标项目的 `@PlatformEntry` 静态入口替代。上面已新增旧 facade 空壳作为迁移索引。

- `fabric/src/main/java/io/github/xienaoban/biologydictionary/fabric/*`、`neoforge/src/main/java/io/github/xienaoban/biologydictionary/neoforge/*` 旧包路径
- `fabric/src/main/java/io/github/xienaoban/biologydictionary/*`、`neoforge/src/main/java/io/github/xienaoban/biologydictionary/*` 新包路径
- `fabric/src/main/java/io/github/xienaoban/biologydictionary/platform/**/fabric/*`、`neoforge/src/main/java/io/github/xienaoban/biologydictionary/platform/**/neoforge/*` 旧平台 impl 路径
- `fabric/src/main/java/io/github/xienaoban/biologydictionary/platform/**`、`neoforge/src/main/java/io/github/xienaoban/biologydictionary/platform/**` 新平台 registrar/impl 路径
  - 原因：26.1.2 不再使用 Architectury 的 `xxx.fabric`/`xxx.neoforge` impl 约定，改为当前加载器入口和 registrar 组织方式。

- `common/src/main/java/io/github/xienaoban/biologydictionary/compat/CompatibilityManager.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/compat/CompatibilityOptions.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/rendering/GuiGraphicsIMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/rendering/GuiTextRenderStateIMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/rendering/GuiGraphicsExtractorIMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/gui/screen/util/ScreenRenderingContext.java`
  - 原因：1.21.11 的 Modern UI 兼容开关和 `GuiTextRenderState` 高级文本路径在 26.1.2 的 GUI 渲染 API 下已不可原样移植；当前代码改用 `GuiGraphicsExtractor` 路径。`z` 参数仍按项目约定只作为兼容旧 API 保留。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/AgeableMobSetForcedAgeSkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/AgeableMobSetBreedingCooldownSkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/AgeableMobSetAgeLockedSkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/TadpoleSetAgeLockedSkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/widget/branch/AgeableMobBreedingCooldownWidget.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/widget/branch/AgeableMobGrowthWidget.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/widget/branch/TadpoleGrowthWidget.java`
  - 原因：26.1.2 的 age/age locked/tadpole 数据与 UI 行为拆分，旧 `AgeableMobSetForcedAgeSkill` 不再一对一对应。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/property/bundle/EntityVariantPropertyBundle.java`
  - 保留目标端以 `MethodHandle`/官方名反射发现 `getVariant`/`setVariant`、处理 Holder/Enum 变体、并给 variant NBT 转换增加 entity 参数的改动。
  - 原因：26.1.2 变体 API 范围扩大，旧版只从 `VanillaEntityProperties` 静态 property 推断会漏掉新的标准变体；该反射集中在一个兼容层内，符合本仓库 26.1 对低频兼容探测可集中使用 handle 的约定。

## 不确定 / 需要复核

- `.github/workflows/*` 和 `.github/scripts/*`
  - 1.21.11 有完整 CI/发布脚本，目标仓库目前没有。
  - 暂未复制原因：源端脚本与 Architectury/旧发布结构强相关，26.1.2 已改为当前 Fabric/NeoForge 多项目结构，需要单独适配而不是原样迁入。

## 其他建议

- 为 `.github/workflows/*` 和 `.github/scripts/*` 单独做一次 26.1.2 发布链路迁移，不要直接复制 1.21.11 的 Architectury 发布流程。
- `TODO.md` 当前保留 3 条“同步回 1.21.11”的目标侧待办，不属于源端漏迁文件；建议后续按独立任务处理。

## 确认不移植

- `CLAUDE.md`
  - 原因：当前目标仓库使用 `AGENTS.md` 和 `.codex/skills/port-1-21-11` 作为维护/移植约定；不再保留 Claude 专用约定文件。

- `neoforge/gradle.properties`
  - 原因：源端文件只有 `loom.platform=neoforge`，用于旧 Architectury/Loom 子项目结构；当前 26.1.2 NeoForge 子项目使用 ModDevGradle，版本和 NeoForge 配置来自根 `gradle.properties` 与 `neoforge/build.gradle`，没有使用该文件。
