# TODO

- 同步回 1.21.11：`ScreenRenderingContext` 里保留 tooltip 宽度，不要改成依赖 `Widget.TOOLTIP_WIDTH`；`platform` 是可复用的 GUI 支撑层，不应该依赖更上层的 widget 代码。
- 同步回 1.21.11：居中消息分层要保持清晰；`SendCenteredMessagePacket` 仍应投递 Biology Dictionary 的居中消息，具体路由放在 `BiologyDictionaryClient` 这类上层客户端入口里。标准 MC 游戏 overlay 可以通过 `platform` 工具发送，但是否路由到 Biology Dictionary screen 内消息不要下沉到 `platform`。
- 同步回 1.21.11：过一遍所有依赖 `ClientUtils.sendCenteredMessage(...)` 的调用点，区分哪些确实只需要原版游戏内 overlay，哪些属于 Biology Dictionary 上层消息、在词典 screen 打开时也应该显示到 screen 内消息区域。
- 同步回 1.21.11：`EntityOverviewCache.getOrCreate(...)` 当前没有真正读写服务端 cache，失败时返回的 `CacheEntry(null, null)` 还会被请求包、回复包和 screen 当成有效 overview 数据；需要保留失败 sentinel 避免重复生成，但对调用方返回/发送/应用时必须按无效数据处理。
- 26.1.2 年龄锁逻辑待刷新：已确认原版 `AgeableMob`/`Tadpole` 支持用金蒲公英切换 `AgeLocked`，并且 `AgeLocked` 会阻止幼年生物自然成长；BD 从 1.21.11 继承的锁年龄按钮仍是写 `ForcedAge` + `Age` 模拟“永不长大/永不发情”，后续需要决定是否改用或兼容原版 `AgeLocked`。
