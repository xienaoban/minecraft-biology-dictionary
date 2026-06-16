# TODO

- 同步回 1.21.11：`ScreenRenderingContext` 里保留 tooltip 宽度，不要改成依赖 `Widget.TOOLTIP_WIDTH`；`platform` 是可复用的 GUI 支撑层，不应该依赖更上层的 widget 代码。
- 同步回 1.21.11：居中消息分层要保持清晰；`SendCenteredMessagePacket` 仍应投递 Biology Dictionary 的居中消息，具体路由放在 `BiologyDictionaryClient` 这类上层客户端入口里。标准 MC 游戏 overlay 可以通过 `platform` 工具发送，但是否路由到 Biology Dictionary screen 内消息不要下沉到 `platform`。
- 同步回 1.21.11：过一遍所有依赖 `ClientUtils.sendCenteredMessage(...)` 的调用点，区分哪些确实只需要原版游戏内 overlay，哪些属于 Biology Dictionary 上层消息、在词典 screen 打开时也应该显示到 screen 内消息区域。
- 后续恢复/核对旧高级书本 tooltip 渲染路径：`CompatibilityOptions.useAdvancedTextRendering`、自定义 book tooltip background/frame 需要基于 26.1.2 `GuiGraphicsExtractor` API 重新接入。
- 26.1.2 vanilla property 移植后重新核对年龄锁逻辑：模组在 1.21.11 已经支持自己的 lock age，而 vanilla 现在在 `AgeableMob`、`Tadpole` 等实体上暴露了 `AgeLocked`；后续需要确认 Minecraft 年龄逻辑是否改过，以及它应该如何和模组的锁年龄行为组合。
