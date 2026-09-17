# TODO：1.3 移植涉及的 MC API 抽象

> 状态：待办，尚未动手。
> 来源：26.2 v1.3.0 直移植到 1.21.1 时暴露的版本差异。
> 目标：让业务代码跨版本尽量保持行级一致，把 MC API 差异下沉到 `platform/util` 或平台实现中。

## 原则

- 只抽象 1.3 新涉及、且未来移植大概率会反复遇到、或已经明显造成两边代码分叉的 API。
- 优先复用现有封装：`EntityUtils`、`PlayerUtils`、`ClientUtils`、`ServerUtils`、`TextUtils`、`IdentifierUtils` 等。
- 抽象后的调用点应尽量一致；版本差异只允许存在于少量实现方法里。
- 两个版本分支都要落地，并至少通过编译和 `fabric:runTestServer`。
- 会改变现有行为、数据格式或平台架构的改动，先讨论清楚再实施。

## P1：优先处理

### 1. 点击与悬浮文本组件

- [ ] 在 `TextUtils` 中抽象 `ClickEvent` / `HoverEvent` 的构造。
- [ ] 建议 API：
  - `withRunCommand(Component, String)`
  - `withSuggestCommand(Component, String)`
  - `withOpenUrl(Component, String)`
  - `withShowText(Component, Component)`
- 版本差异：
  - 26.2：`ClickEvent.RunCommand` / `SuggestCommand` / `OpenUrl`，`HoverEvent.ShowText`
  - 1.21.1：`new ClickEvent(ClickEvent.Action.*, String)`，`new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component)`
- 涉及：
  - `CommandManager`
  - `BiologyDictionaryDiscoveryStrategy`
  - `TextUtils.withFallbacks` 的 hover 重建逻辑
- 验收：
  - 业务代码不再直接 `new ClickEvent.*` / `new HoverEvent.*`，只调用 `TextUtils` 封装。

### 2. GameProfile 名字读取

- [ ] 抽象 `GameProfile` 名字读取。
- [ ] 建议 API：
  - `PlayerUtils.getGameProfileName(GameProfile)`
  - 或 `PlayerUtils.getPlayerName(Player)`
- 版本差异：
  - 26.2：`gameProfile.name()`
  - 1.21.1：`gameProfile.getName()`
- 涉及：
  - `ClientUtils.getPlayerName`
  - `ServerUtils.getPlayerName`
  - `BiologyDictionaryDiscoveryStrategy` 的 `/tell` 建议命令
- 备注：
  - 本模组自己的玩家名缓存逻辑可继续保留在 `PlayerNameCache`，这个 TODO 只处理底层 `GameProfile` API 差异。

### 3. Overlay / ActionBar 消息

- [ ] 抽象服务端向玩家发送 overlay 消息。
- [ ] 建议 API：
  - `PlayerUtils.sendOverlayMessage(Player, Component)`
- 版本差异：
  - 26.2：`player.sendOverlayMessage(component)`
  - 1.21.1：`player.displayClientMessage(component, true)`
- 涉及：
  - `ServerNetManager.sendCenteredMessage` 的 fallback 分支
  - 检查 `PlayerUtils` / `ClientUtils` 里已有调用是否也应统一。
- 验收：
  - `ServerNetManager` 不再直接感知 overlay 发送 API 的版本差异。

### 4. Toast 入队

- [ ] 在 `ClientUtils` 中抽象 Toast 添加。
- [ ] 建议 API：
  - `ClientUtils.addToast(Toast)`
- 版本差异：
  - 26.2：`client.gui.toastManager().addToast(toast)`
  - 1.21.1：`client.getToasts().addToast(toast)`
- 涉及：
  - `SendDiscoveryIncrementalPacket`
  - `DiscoveryToast` 自身实现仍需版本适配；这个 TODO 先让调用侧一致。
- 备注：
  - `Toast` 接口本身的 `render` / `update` / `extractRenderState` 差异较大，暂不强求统一实现方式，只统一调用入口。

### 5. Entity NBT 捕获

- [ ] `DiscoveryRecord.standard` 改用现有 `EntityUtils.getNbt(entity)`，不再直接使用 `TagValueOutput` / `saveWithoutId`。
- 版本差异：
  - 26.2：`TagValueOutput.createWithoutContext(...)` + `entity.saveWithoutId(output)`
  - 1.21.1：`new CompoundTag()` + `entity.saveWithoutId(tag)`
- 涉及：
  - `DiscoveryRecord`
  - 确认 `EntityUtils.getNbt` 的语义在两边一致，必要时微调实现。
- 验收：
  - `DiscoveryRecord` 只保留 `keepAppearanceOnly(EntityUtils.getNbt(entity))` 这类共同逻辑。

## P2：集中处理

### 6. CompoundTag 读写工具

- [ ] 评估新增 `NbtUtils`（或扩展现有工具类）。
- [ ] 建议 API：
  - `NbtUtils.getCompound(CompoundTag, String)`
  - `NbtUtils.getString(CompoundTag, String, String fallback)`
  - `NbtUtils.putUuid(CompoundTag, String, UUID)` / `getUuid`
  - `NbtUtils.putUuidList` / `getUuidList`
- 版本差异：
  - 26.2：`getCompoundOrEmpty`、`getString(...).orElse(...)`
  - 1.21.1：`getCompound`、`getString`、`hasUUID`、`getUUID`
- 涉及：
  - `DiscoveryDataMigrator`
  - `SavedDataDiscoveryStorage`
  - 后续可逐步替换 `StringProperty`、`LootTableUtils` 等已有散落用法
- 备注：
  - 先覆盖 1.3 新代码，避免一次性大范围重构。

### 7. Biome 降水查询

- [ ] 抽象生态降水查询。
- [ ] 建议 API：
  - `EntityUtils.getPrecipitationAt(Level, BlockPos)`
- 版本差异：
  - 26.2：`biome.getPrecipitationAt(pos, level.getSeaLevel())`
  - 1.21.1：`biome.getPrecipitationAt(pos)`
- 涉及：
  - `DiscoveryRecord.standard`

### 8. 指令权限与参数类型

- [ ] 评估 `CommandUtils` 封装。
- [ ] 建议 API：
  - `CommandUtils.hasPermission(CommandSourceStack, int level)`
  - 后续视情况抽象实体类型参数
- 版本差异：
  - 26.2：`source.permissions().hasPermission(Permissions.COMMANDS_ADMIN)`
  - 1.21.1：`source.hasPermission(2)`
  - 26.2：`IdentifierArgument.id()`
  - 1.21.1：`ResourceLocationArgument.id()`
- 涉及：
  - `CommandManager`
  - 未来新增指令
- 备注：
  - 指令架构本身还有 `Commands.ENTRIES` vs `CommandRegistry` 差异，先只抽象局部检查/参数，框架差异另议。

### 9. ResourceKey 到资源 ID

- [ ] 评估在 `IdentifierUtils` 中增加 `ResourceKey` 读取封装。
- [ ] 建议 API：
  - `IdentifierUtils.getId(ResourceKey<?>)`
- 版本差异：
  - 26.2：`ResourceKey::identifier`
  - 1.21.1：`ResourceKey::location`
- 涉及：
  - `DiscoveryRecord.standard`
- 备注：
  - 如果只是单点使用，也可以接受保留少量差异，不必强行封装。

## P3：先评估，不保证实施

### 10. SavedData 注册与存储格式

- [ ] 评估是否值得抽 `SavedDataUtils` / 通用存储接口。
- 版本差异：
  - 26.2：`SavedDataType` + `Codec` + `computeIfAbsent(TYPE)`
  - 1.21.1：`SavedData.Factory` + `load/save` + `computeIfAbsent(FACTORY, "id")`
- 现状：
  - 本次 1.3 移植中，`SavedDataDiscoveryStorage` 已保留目标格式，只把新字段、迁移、全局统计适配进去。
- 结论倾向：
  - 差异较大且只涉及少数存储类，暂不强行抽象；先观察后续是否有更多 SavedData 需要跨版本。

### 11. 数据包注册结构

- [ ] 评估是否要统一 `PacketPayloads` 的注册描述方式。
- 版本差异：
  - 26.2：`@PlatformEntry List<Entry<?>>`
  - 1.21.1：`Registrar.register(...)`
- 现状：
  - 两边的最终注册结果一致，只是描述方式不同。
- 结论倾向：
  - 暂不急；等新增包越来越多、或移植反复踩坑时再做统一 facade。

### 12. KeyMapping 构造与注册

- [ ] 评估 `KeyMappingRegistry` 是否再吸收 key 构造差异。
- 版本差异：
  - 26.2：`KeyMapping` 5 参数，带冲突上下文
  - 1.21.1：`KeyMapping` 4 参数
- 现状：
  - 注册入口已经通过 `KeyMappingRegistry` 统一；当前只是构造参数版本差异。
- 结论倾向：
  - 暂不处理；如果未来新增键位多，再考虑 `KeyMappingUtils.create(...)`。

## 已确认不需要新增抽象

- `Identifier` / `ResourceLocation`：已有 `IdentifierUtils`，继续沿用，不再包一层。
- `ServerNetApi.canSend`：已有共同 API，平台实现差异属于预期。
- `ClientNetManager` / `ServerNetManager`：已经是对应网络的门面，新包直接加方法即可。
- `EntityOverviewCache.CacheEntry.isValid`：属于目标仓库已有实现缺失，本次已补，不是一个值得抽象的模式。

## 验收清单

- [ ] P1 项目全部完成。
- [ ] 两边主分支编译通过。
- [ ] `fabric:runTestServer` 全部通过。
- [ ] 移植审计时，业务代码差异明显减少，必要差异能稳定归因到 `platform/util` 或平台入口。
