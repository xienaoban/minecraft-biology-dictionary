# 移植审计日志

源项目：`../minecraft-biology-dictionary-architectury-1.21.11`（`main-architectury-1.21.11`）

目标基线：`30c34dc`

范围：所有可维护项目文件。源/目标文件集合对比时排除生成产物、Gradle 缓存、IDE 元数据、run 目录和本地运行时文件。

## 已修改

- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/ExampleMixin.java`
- `common/src/main/resources/biologydictionary.mixins.json`
  - 删除 `ExampleMixin` 及其 mixin 配置项。
  - 原因：这是 26.1.2 模板残留，1.21.11 没有；它只对 `MinecraftServer#loadLevel` 做空注入，没有业务作用，属于多出来的内容。

- `common/src/main/resources/data/biologydictionary/biologydictionary/entity_spawn/*.json`
  - 从 1.21.11 恢复 9 个实体生成覆盖数据：bee、breeze、cave_spider、creaking、elder_guardian、ender_dragon、shulker、vex、warden。
  - 原因：26.1.2 的 `EntitySpawnManager` 仍读取 `biologydictionary/entity_spawn`，目标仓库没有替代数据资源，因此这些是漏移植的行为数据。

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

- `common/src/main/java/io/github/xienaoban/biologydictionary/config/ClothConfigScreenProvider.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/gui/screen/misc/InventoryStealingMenu.java`
  - 恢复到 1.21.11 同路径文件形态。
  - 原因：目标端差异只是不必要的注释删除、变量重命名、局部布局调整和等价控制流改写；没有 26 API 或非 Architectury 架构理由。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/EntitySpawnManager.java`
  - 恢复 1.21.11 的注释和 data pack override 局部结构，删除额外新增的 `Identifier.tryParse(...) == null` 防御分支。
  - 原因：注释/布局删除是无必要漂移；额外 invalid id 分支属于源端没有的新行为，不是 26 API 适配。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/discovery/DiscoveryRecord.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/discovery/DiscoverySource.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/discovery/strategy/BiologyDictionaryDiscoveryStrategy.java`
  - 恢复源端注释、局部变量名、codec/record 缩进和两处换行形态。
  - 原因：除 `server.getDataStorage()` 之外，这些差异没有 26 API 或架构理由。

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
- `raw/*`
  - 从 1.21.11 恢复项目说明、许可证、更新日志、文档素材、自定义数据文档和原始素材。
  - 原因：目标仓库根 `README.md` 只是模板占位，`docs` 和 `raw` 下缺少实际用户/开发文档、素材和原始素材；这些不是 Minecraft 26 或加载器架构差异。

- `common/src/testServer/java/**`
- `fabric/src/testServer/java/**`
- `fabric/src/testClient/resources/fabric.mod.json`
- `fabric/src/testServer/resources/fabric.mod.json`
  - 从 1.21.11 恢复 GameTest / 静态检查测试源码和 Fabric 测试资源。
  - 适配点：
    - `AbstractVisitorWrapper`：JavaParser 版本中 Javadoc comment visitor 类型改为 `TraditionalJavadocComment`。
    - `RegistrarsTest`：`PacketPayloads` 已从旧 `registerBuiltIn(Registrar)` facade 迁到 `@PlatformEntry` 的 `ENTRIES` 列表，测试改为遍历 `ENTRIES` 并验证 `FACTORY` 来源。
    - `VanillaEntitySkillTest`：`VariantHandler#variantToNbt` 现在需要 entity 参数。
    - `VanillaEntitySkillTest`：旧 `AgeableMobSetForcedAgeSkill` 在 26.1.2 拆为 `AgeableMobSetBreedingCooldownSkill`、`AgeableMobSetAgeLockedSkill`、`TadpoleSetAgeLockedSkill`，测试工厂同步拆分。
    - `fabric.mod.json`：恢复所有 `fabric-gametest` entrypoint，不只注册 `VanillaEntityNbtTestImpl`。
  - 原因：Gradle 中仍配置了 `testServer` sourceSet 和 Fabric GameTest 运行项，但源码/entrypoint 资源缺失或不完整；这是漏移植，适配仅限当前主代码 API 变化。

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
  - 原因：这些构造器和方法重排不是 26 API 迁移需要；`Permissions` 中物品显示名恢复为源端的 `Item#getName(...)` 路径，26.1.2 API 需要传入 `ItemStack` 参数。

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

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/EntityOverviewCache.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/property/VanillaEntityProperties.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/property/extra/EntitySpawnCountedProperty.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/BiologySkills.java`
  - 恢复无必要删除/新增的注释、空行、局部变量名、import 展开和长字符串换行。
  - 原因：这些差异不属于 26 API、NBT 自动生成结果或去 Architectury 架构迁移；保留差异只会降低可比较性。

- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/ReplyBeehiveInfoPacket.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/ReplyEntityDataPacket.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/ReplyEntityOverviewPacket.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/ReplyHighlightEntitiesPacket.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/RequestEntityDataPacket.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/RequestEntityOverviewPacket.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/SendCenteredMessagePacket.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/SendDiscoveryIncrementalPacket.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/SendStealingDetectedPacket.java`
  - 恢复 payload 组中被删除的源端注释、本地 `CO.receive(..., ctx)` 结构、局部变量名、记录字段直接访问、构造/写入方法行形态和 float 字面量后缀。
  - 原因：这些差异不来自 26 网络 API 或当前平台架构；`PacketPayloads`/`ClientNetApi`/`ServerNetApi` 已经承担必要迁移，payload 业务实现应尽量与 1.21.11 可逐行对照。

- `common/src/main/java/io/github/xienaoban/biologydictionary/client/KeyMappings.java`
- `fabric/src/main/java/io/github/xienaoban/biologydictionary/FabricBiologyDictionaryClient.java`
- `neoforge/src/main/java/io/github/xienaoban/biologydictionary/NeoForgeBiologyDictionaryClient.java`
  - `KeyMappings` 恢复使用 `Lang.KEY_OPEN_HANDBOOK` / `Lang.KEY_DEBUG`，并移除 `DEBUG` 的 `@PlatformEntry` 与 Fabric/NeoForge 平台注册，只保留 `OPEN_HANDBOOK` 注册。
  - 原因：1.21.11 只把打开手册键注册到全局 key mapping；debug 键仅用于 screen 内 `matches(...)` 判断。目标端把 debug 也注册进控制设置属于无来源的额外行为。

- `fabric/src/main/java/io/github/xienaoban/biologydictionary/config/ModMenuConfigScreenProvider.java`
  - 恢复源端 ModMenu/Cloth Config 集成说明注释。
  - 原因：包路径从 `config.fabric` 迁到 `config` 是 Fabric 当前资源入口配置所需；删除注释不是必要迁移。

- `gradle.properties`
- `fabric/src/main/resources/fabric.mod.json`
- `neoforge/src/main/templates/META-INF/neoforge.mods.toml`
  - `mod_license` 从模板残留的 `All Rights Reserved` 改为 `LGPL-3.0`；Fabric metadata 恢复源端描述和 contact 信息；NeoForge metadata 恢复 issue tracker、display URL 和源端描述，保留 26.1.2 NeoForge 模板原有注释，同时不恢复 Architectury dependency。
  - 原因：项目根 `LICENSE` 已恢复为 LGPL-3.0，构建产物 metadata 不能继续声明 All Rights Reserved；26.1.2 NeoForge 模板原有注释可保留，但实际 `todo` 描述和 Architectury 依赖是旧模板/旧架构内容，不应移植。

- `README.md`
- `README.zh-CN.md`
  - 将加载器/版本徽章从源端 `Fabric + NeoForge + Forge`、`MC 1.21.11 | 1.21.1 | 1.20.1` 改为当前目标的 `Fabric + NeoForge`、`MC 26.1.2`；依赖表移除 Forge 和 Architectury API。
  - 原因：README 内容来自 1.21.11，但目标分支不再使用 Architectury，也不是 Forge/旧 MC 版本产物；用户可见文档必须反映当前 26.1.2 构建。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/EntityGiftPetSkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/LivingEntityStealInventorySkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/MobForcePersistentSkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/MobSetNoAiSkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/VillagerForceRestockSkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/general/GetSpawnEggSkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/general/HighlightEntitiesSkill.java`
  - 恢复 skill 组中无理由删除的说明注释、局部变量布局、源端嵌套控制流和 `HighlightEntitiesSkill` 的源端 literal 写法；删除 `VillagerForceRestockSkill#getRealCost` 中无来源的 `factor == 0 -> SkillCost.empty()` fallback。
  - 原因：`SkillCost` 从 `ItemStack` 迁到 `ItemCost`、spawn egg lookup 改为 26 Optional/holder API、variant handler 增加 entity 参数是必要差异；注释删除、公共方法抽取、早退重写和额外 fallback 不是必要迁移。

- `common/src/main/java/io/github/xienaoban/biologydictionary/client/DiscoveryToast.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/client/FirstPersonShoulderEntityRenderer.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/client/HighlightManager.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/gui/screen/BdHomeScreen.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/gui/screen/misc/InventoryStealingScreen.java`
  - 恢复客户端/GUI 代码中无理由删除的注释、局部变量名、源端行形态和 spawn egg null 判断结构。
  - 原因：`GuiGraphicsExtractor`、toast `extractRenderState`、26 spawn egg Optional API、shoulder camera render state accessor 是必要迁移；注释删除和纯格式压缩不是必要迁移。

- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/gui/screen/ElementScreen.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/gui/screen/CommonScreen.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/gui/screen/util/ScreenRenderingContext.java`
  - 恢复 `ElementScreen` 捕获异常后调用 `BiologyDictionaryClient.printThrowableToLoggerAndGame(...)` 的源端行为，并补回 screen scale、mouse 坐标和几何渲染说明注释；`renderText(Component, int, float, float, float, float)` 恢复源端调用 `component.getVisualOrderText()` 的写法。
  - 原因：26 GUI API 方法名/类型变化必须保留；但异常只写 logger 会让玩家看不到原本会显示的错误信息，属于行为回归。注释和 overload 改写也不是必要迁移。

- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/MinecraftMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/net/Packet.java`
  - `MinecraftMixin` 恢复源端传入 `(Minecraft) (Object) this`；`Packet#clientReceive` 恢复 `@ClientOnly` 标注。
  - 原因：前者只是无必要的等价写法变化；后者会削弱 client-only 边界说明，不属于 26 网络迁移需要。

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
  - 原因：旧 Architectury event/registry facade 被目标项目的 `@PlatformEntry` 静态入口和平台 registrar 替代；旧 facade 不恢复。

- `common/src/main/java/io/github/xienaoban/biologydictionary/BiologyDictionary.java`
  - 保留删除 `CompatibilityManager.init()`、`ServerNetManager.init()`、`BiologyDictionaryItem.init()`、`CommandManager.init()`、旧 `ServerEventRegistry` 注册的差异。
  - 原因：Modern UI 高级文本兼容层已不再使用；网络、创造栏、命令和 server lifecycle 已分别迁到 Fabric/NeoForge registrar 与 `ServerEvents` 静态清单。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/BiologyDictionaryItem.java`
  - 保留 `@PlatformEntry` creative tab entry、`CustomData.copyTag()`、`getGameTime()`、`ItemCost` 交易构造和书页 `\u00a7` 转义。
  - 原因：这些对应当前无 Architectury 注册结构和 Minecraft 26.1.2 item/trade/data component API；已将局部变量名/控制流恢复到源端形态。

- `fabric/src/main/java/io/github/xienaoban/biologydictionary/fabric/*`、`neoforge/src/main/java/io/github/xienaoban/biologydictionary/neoforge/*` 旧包路径
- `fabric/src/main/java/io/github/xienaoban/biologydictionary/*`、`neoforge/src/main/java/io/github/xienaoban/biologydictionary/*` 新包路径
- `fabric/src/main/java/io/github/xienaoban/biologydictionary/platform/**/fabric/*`、`neoforge/src/main/java/io/github/xienaoban/biologydictionary/platform/**/neoforge/*` 旧平台 impl 路径
- `fabric/src/main/java/io/github/xienaoban/biologydictionary/platform/**`、`neoforge/src/main/java/io/github/xienaoban/biologydictionary/platform/**` 新平台 registrar/impl 路径
  - 原因：26.1.2 不再使用 Architectury 的 `xxx.fabric`/`xxx.neoforge` impl 约定，改为当前加载器入口和 registrar 组织方式。

- `common/src/main/java/io/github/xienaoban/biologydictionary/compat/CompatibilityManager.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/compat/CompatibilityOptions.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/CreativeModeTabsIMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/CustomDataIMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/ListPoolElementIMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/rendering/GuiGraphicsIMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/rendering/GuiTextRenderStateIMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/rendering/GuiGraphicsExtractorIMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/gui/screen/util/ScreenRenderingContext.java`
  - 原因：1.21.11 的 Modern UI 兼容开关和 `GuiTextRenderState` 高级文本路径在 26.1.2 的 GUI 渲染 API 下已不可原样移植；当前代码改用 `GuiGraphicsExtractor` 路径。`CreativeModeTabsIMixin` 被 registry key 构造替代，`CustomDataIMixin` 被 `CustomData.copyTag()` 替代，`ListPoolElementIMixin` 被 26 公开的 `ListPoolElement#getElements()` 替代。`z` 参数仍按项目约定只作为兼容旧 API 保留。

- `fabric/src/main/resources/biologydictionary.accesswidener`
- `common/src/main/resources/biologydictionary.accesswidener`
  - 旧 Fabric access widener 不恢复到 Fabric 子项目；当前访问项集中放在 common access widener，并从 named/classTweaker 迁为 official/accessWidener v2。
  - 原因：当前项目使用 official mappings 和 common mixin/访问配置；旧 `GuiGraphics$ScissorStack`、`GuiTextRenderState` 访问项已被 26 的 `GuiGraphicsExtractor$ScissorStack` 访问项替代。

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

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/SkillCost.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/EntitySetSoundSkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/WanderingTraderRetainSkill.java`
  - 保留目标端 `ItemCost` / `ItemLike` 成本模型，以及调用处 `SkillCost.ofItems(Items.X)` 的写法。
  - 原因：26.1.2 当前成本序列化和比较已从 `ItemStack` 切到 `SkillCost.ItemCost`，源端 `new ItemStack(...)` 调用不再匹配现有 API。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/skill/entity/EntitySetVariantSkill.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/widget/variant/AbstractEntityStandardVariantWidget.java`
  - 保留 variant NBT/列表/名称转换需要传入 entity 的差异，以及 `readNbt(NbtAccounter)`。
  - 原因：目标端 `EntityVariantPropertyBundle` 需要根据具体 entity 处理 Holder/Enum/reflection variant；`FriendlyByteBuf#readNbt` 在 26.1.2 需要显式 accounter。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/EntitySpawnManager.java`
  - 保留 `ListPoolElement#getElements()`、`Registry#getTagOrEmpty(...)` 和 `SPAWN_OVERRIDE_PATH_PREFIX` 常量位置差异。
  - 原因：前两项是 26.1.2 API 替代旧 mixin/tag optional 访问；常量提前是为了用于 `FileToIdConverter.json(SPAWN_OVERRIDE_PATH)` 附近，不改变行为。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/discovery/storage/SavedDataDiscoveryStorage.java`
  - 保留 `SavedDataType` 从字符串 id 迁为 `Identifier`。
  - 原因：这是 26.1.2 `SavedDataType` API 变化。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/widget/EntityPropertyWidgets.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/widget/leaf/VillagerScheduleWidget.java`
  - 保留注册 `TadpoleGrowthWidget` 和 schedule 当前时间取 `getLevelData().getGameTime() % 24000L`。
  - 原因：前者对应 26 的 tadpole age locked/age UI 拆分；后者对应当前 client level 时间 API。

- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/ReplyEntityDataPacket.java`
  - 保留 `BiologyDictionaryClient.getHitEntity()` / `getHitEntityProperties()` 的 static 调用形式，不恢复源端 `BDC.get...`。
  - 原因：目标端 `BiologyDictionaryClient` 当前状态访问器已经迁为 static；这是现有 26 代码形态的必要适配。

- `common/src/main/java/io/github/xienaoban/biologydictionary/client/BiologyDictionaryEvent.java`
  - 保留 `BiologyDictionaryClient.setHitXxx(...)` 的 static 调用形式，不恢复源端 `BDC.set...`。
  - 原因：目标端 `BiologyDictionaryClient` 当前状态访问器已经迁为 static；这是现有 26 代码形态的必要适配。

- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/ReplyHighlightEntitiesPacket.java`
  - 保留 `BiologyDictionaryClient.sendCenteredMessage(...)`，不恢复源端 `ClientUtils.sendCenteredMessage(...)`。
  - 原因：目标端需要在上层入口根据当前是否处于 Biology Dictionary screen 选择 screen message 或原版 overlay；这类业务状态分发不应下沉到 `platform` 工具类。

- `common/src/main/java/io/github/xienaoban/biologydictionary/net/payload/RequestEntityOverviewPacket.java`
  - 保留 `cached.isValid()` 对 `ReplyEntityOverviewPacket.notNull` 的控制。
  - 原因：这是目标端服务端 overview cache / 失败 sentinel 特性的一部分，防止生成失败的 `(null, null)` 被当作有效 overview 数据发送。

- `common/src/main/java/io/github/xienaoban/biologydictionary/net/PacketPayloads.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/ClientNetManager.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/net/ServerNetManager.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/net/ClientNetApi.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/net/ServerNetApi.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/net/PacketUtil.java`
- `fabric/src/main/java/io/github/xienaoban/biologydictionary/platform/net/ServerNetRegistrar.java`
  - 保留 `registerBuiltIn(Registrar)` 到 `@PlatformEntry ENTRIES`、网络发送 `Platform.load` 窄服务、`RegistryFriendlyByteBuf` play codec、`PacketUtil.registerType` 幂等化。
  - 原因：当前平台入口在 Fabric/NeoForge 生命周期中消费 packet 清单；Fabric/NeoForge client receiver 注册会再次经过同一 packet 类型，`registerType` 需要幂等以支持分阶段注册。Fabric server 侧仍沿用源端“统一注册 clientbound，再按需注册 serverbound receiver”的行为，避免 server 注册阶段依赖 client receiver 判定。

- `common/src/main/java/io/github/xienaoban/biologydictionary/client/HighlightRenderer.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/client/TelescopeDiscoveryIndicatorRenderer.java`
  - 保留 26.1.2 render submit API、`GuiGraphicsExtractor`、block model layer 分层提交和第一人称跳过玩家实体高亮。
  - 原因：旧 `submitBlock`/`GuiGraphics` 路径不再对应当前渲染 API；第一人称跳过玩家实体避免高亮自身模型，属于当前渲染路径下的行为修正。

- `common/src/main/java/io/github/xienaoban/biologydictionary/gui/screen/AbstractBiologyDictionaryScreen.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/gui/screen/misc/BeehiveScreen.java`
  - 保留 `KeyMappingManager` 到 `KeyMappings`、`OPEN_BIOLOGY_DICTIONARY_SCREEN` 到 `OPEN_HANDBOOK`、`TOGGLE_DEBUG` 到 `DEBUG` 的命名差异。
  - 原因：旧 manager 已迁为当前 `@PlatformEntry` 静态 key mapping 定义类；debug 仍只在 screen 内使用，不进入平台全局注册。

- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/gui/screen/util/ScreenElement.java`
  - 保留删除 `org.jetbrains.annotations.Nullable` 的差异。
  - 原因：当前移植约束禁止使用 JetBrains `Nullable`/`NotNull` 注解；字段和 getter 的可空语义通过现有调用约定保留。

- `common/src/main/java/io/github/xienaoban/biologydictionary/core/property/.nbt-tag-import.log`
- `common/src/main/java/io/github/xienaoban/biologydictionary/core/property/.nbt-tag-list.log`
  - 保留目标端 26.1.2 重新扫描结果，不同步回 1.21.11 内容。
  - 原因：这些日志反映当前 Minecraft 版本实体/NBT API，例如 `AgeLocked`、各类 `sound_variant`、`VillagerDataFinalized`、`VibrationSystem.Data` 等新增/改名项。

- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/entity/AgeableMobMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/entity/PlayerMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/entity/WanderingTraderMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/loot/SetItemCountFunctionIMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/rendering/GuiMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/rendering/LevelRendererMixin.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/mixin/rendering/PictureInPictureRendererMixin.java`
  - 保留目标端 mixin 方法签名、字段名和渲染 API 变更。
  - 原因：这些差异来自 Minecraft 26.1.2 内部 API：`Player#interactOn` 增加 hit position、`WanderingTrader#updateTrades` 增加 `ServerLevel` 参数、`SetItemCountFunction` 字段名变为 `count`、GUI/level render state 包名和提交流程变化；`AgeableMobMixin` 则对应 26 目标端 age locked / breeding cooldown 拆分。

- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/util/ClientUtils.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/util/PlayerUtils.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/util/LootTableUtils.java`
  - 保留 `displayClientMessage` 到 `sendSystemMessage` / `sendOverlayMessage`、loot condition `getType()` 到 `codec()`、item display name 取法的 26 API 迁移。
  - 原因：这是 Minecraft 26.1.2 official API 变化，不是业务逻辑改写。

- `build.gradle`
- `settings.gradle`
- `common/build.gradle`
- `fabric/build.gradle`
- `neoforge/build.gradle`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle/wrapper/gradle-wrapper.jar`
  - 保留当前 Gradle 9.4.1、Java 25、Fabric Loom、NeoForge ModDevGradle、多项目 source artifact 和 repository 配置。
  - 原因：源端是 Architectury/Loom 多加载器骨架；目标端不是 Architectury 项目，构建骨架不能按源端同步。

- `neoforge/src/main/resources/META-INF/accesstransformer.cfg`
  - 保留访问项从 `GuiGraphics$ScissorStack` / `GuiTextRenderState` 迁到 `GuiGraphicsExtractor$ScissorStack`。
  - 原因：26.1.2 GUI 渲染访问点已经迁到 `GuiGraphicsExtractor`，旧高级文本 render state 路径不再使用。

- `common/src/testServer/java/io/github/xienaoban/biologydictionary/RegistrarsTest.java`
- `common/src/testServer/java/io/github/xienaoban/biologydictionary/VanillaEntitySkillTest.java`
- `common/src/testServer/java/io/github/xienaoban/biologydictionary/core/property/AbstractVisitorWrapper.java`
- `common/src/testServer/java/io/github/xienaoban/biologydictionary/core/property/AstParser.java`
- `common/src/testServer/java/io/github/xienaoban/biologydictionary/core/property/BytecodeDecompiler.java`
- `common/src/testServer/java/io/github/xienaoban/biologydictionary/core/property/ClassTypeCollector.java`
- `common/src/testServer/java/io/github/xienaoban/biologydictionary/core/property/NbtTagCollector.java`
  - 保留测试/属性生成工具中的 Java 25 language level、`TraditionalJavadocComment`、Fabric sources jar 读取、继承内部类解析、`ValueInput`/`ValueOutput` 参数名识别、`ResourceKey.codec(...)` 类型推断，以及 packet/variant/age skill API 适配。
  - 原因：这些差异分别对应当前 JavaParser 版本、Minecraft 26 源码形态、当前 testServer source provider 和主代码 API；不是生产逻辑额外功能。

## 不确定 / 需要复核

- `.github/workflows/*` 和 `.github/scripts/*`
  - 1.21.11 有完整 CI/发布脚本，目标仓库目前没有。
  - 暂未复制原因：源端脚本与 Architectury/旧发布结构强相关，26.1.2 已改为当前 Fabric/NeoForge 多项目结构，需要单独适配而不是原样迁入。

## 其他建议

- 为 `.github/workflows/*` 和 `.github/scripts/*` 单独做一次 26.1.2 发布链路迁移，不要直接复制 1.21.11 的 Architectury 发布流程。
- `TODO.md` 当前保留 3 条“同步回 1.21.11”的目标侧待办，不属于源端漏迁文件；建议后续按独立任务处理。

## 已验证

- `./gradlew common:compileJava fabric:compileJava neoforge:compileJava checkCommonPlatformImports fabric:processResources neoforge:generateModMetadata`
  - 结果：通过。
- `./gradlew common:compileJava fabric:compileJava neoforge:compileJava checkCommonPlatformImports`
  - 结果：通过。
- `./gradlew common:compileJava fabric:compileJava neoforge:compileJava checkCommonPlatformImports fabric:processTestServerResources`
  - 结果：通过。

## 确认不移植

- `CLAUDE.md`
  - 原因：当前目标仓库使用 `AGENTS.md` 和 `.codex/skills/port-1-21-11` 作为维护/移植约定；不再保留 Claude 专用约定文件。

- `.claude/settings.json`
- `.claude/settings.local.json`
  - 原因：这是 Claude 本地/工具配置，不属于目标仓库当前 Codex/AGENTS 工作流；不从 1.21.11 迁入。

- `docs/dev/neoforge-dep-architectury-removal.md`
  - 原因：该 NeoForge 去 Architectury 分析文档对应的迁移已经在当前 26.1.2 结构中实现，用户已删除；不再作为待恢复文档保留。

- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/client/ClientEventRegistry.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/client/KeyMappingRegistry.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/server/CommandRegistry.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/server/ItemRegistry.java`
- `common/src/main/java/io/github/xienaoban/biologydictionary/platform/server/ServerEventRegistry.java`
  - 原因：旧 Architectury facade 已由 `ClientEvents`、`KeyMappings`、`Commands`、`BiologyDictionaryItem` creative tab entry、`ServerEvents` 和平台 registrar 替代；保留空壳会误导后续移植审计。

- `neoforge/gradle.properties`
  - 原因：源端文件只有 `loom.platform=neoforge`，用于旧 Architectury/Loom 子项目结构；当前 26.1.2 NeoForge 子项目使用 ModDevGradle，版本和 NeoForge 配置来自根 `gradle.properties` 与 `neoforge/build.gradle`，没有使用该文件。
