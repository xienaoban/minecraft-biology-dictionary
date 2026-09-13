# 共享发现：去掉冗余名字 + 跨版本兼容原则（TODO）

> 状态：设计讨论已定，尚未动手。
> 范围：仅 discovery 相关整改。其他所有涉及新老版本兼容的事项以后再改，不纳入本文档。

## 1. 目标

当前 `DiscoveryRecord` / `GlobalDiscoveryStats.Entry` / `DiscoveryShareLink` 同时保存 UUID 和玩家名字，属于明确冗余，需要收口：

- 记录、存档、网络主数据只使用 UUID。
- 玩家名字仅作为客户端展示层数据，不进入每条发现记录。
- 客户端在会话数据（`ClientWorldSession`）中维护 `UUID -> 名字` 缓存。
- 服务端在全量/增量同步时负责把名字映射喂给客户端。
- UI / toast 显示规则：能解析到名字就显示名字；解析不到就显示 UUID。
- 不保留“发现时叫什么”的历史快照；玩家改名后展示最近已知名字即可。

## 2. 跨版本兼容原则

### 2.1 加载时不一致：主动迁移，集中处理

- 游戏伊始加载存档数据时，若新旧版本数据结构不一致，可以主动做迁移/整改。
- 例如字段 `aaa` 重命名为 `bbb`，加载时主动处理。
- 兼容层代码必须集中在一处管理，避免旧版兼容逻辑散落到业务各处。

### 2.2 运行时不一致：不做版本兼容，默认值兜底

- 运行时（网络包、实时数据流）任何数据都可能因版本不同而不一致，很难全面兼容。
- 因此运行时不考虑其他版本的数据格式差异，也不关心字段重命名。
- 接收方遇到缺失或未知字段时，用默认值兜底即可。

### 2.3 两者不冲突

- 加载时做的迁移整改，如果之后仍与当前真实数据结构对不上（例如开发疏漏），处理方式与运行时一致：默认值兜底。
- 不把“加载时已迁移”当成可以放松运行时兜底的理由。

## 3. 待办

### 3.1 数据模型

- [ ] `DiscoveryRecord`：移除 `discovererName`，只保留 `discoverer` UUID。
- [ ] `DiscoveryShareLink`：移除 `sharerName`，只保留 `sharer` UUID。
- [ ] `GlobalDiscoveryStats.Entry`：只保留 `discoverer` UUID，不再带名字。
- [ ] `DiscoveryRecord.standard(...)`：不再从 `GameProfile` 取名字写入记录。
- [ ] 所有基于名字的构造、getter、toast、UI 改为从 UUID 解析名字。

### 3.2 序列化 / 存档

- [ ] `DiscoveryRecordSerializer`：codec 与网络 buffer 只读写 UUID 相关字段。
- [ ] `DiscoveryDataMigrator`：不再为了显示而补 `discoverer_name`。
- [ ] 旧档中已有的名字字段按“可忽略/默认值”处理，不散落兼容逻辑。
- [ ] 加载时若未来有字段重命名/结构调整，统一放进集中迁移层。

### 3.3 网络 / 同步

- [ ] `SendSharedDiscoveryIncrementalPacket`：主数据不再携带冗余名字。
- [ ] 全量同步时由服务端提供本次记录涉及 UUID 的名字映射，喂给客户端缓存。
- [ ] 增量/共享通知按需要附带 profile 注册信息，避免客户端因没有名字而只能显示 UUID；确实拿不到时允许显示 UUID。
- [ ] 运行时收到缺失/未知字段时默认值兜底，不尝试兼容旧版本格式。

### 3.4 客户端展示

- [ ] `ClientWorldSession` 增加 `Map<UUID, String>` 名字缓存。
- [ ] 详情 widget、共享 toast、相关 UI 统一走该缓存。
- [ ] 找不到名字时显示 UUID。
- [ ] 不保存/不展示“发现时名字”的历史语义。
