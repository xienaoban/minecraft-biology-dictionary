# TODO

- 同步回 1.21.11：`ScreenRenderingContext` 里保留 tooltip 宽度，不要改成依赖 `Widget.TOOLTIP_WIDTH`；`platform` 是可复用的 GUI 支撑层，不应该依赖更上层的 widget 代码。
- 26.1.2 vanilla property 移植后重新核对年龄锁逻辑：模组在 1.21.11 已经支持自己的 lock age，而 vanilla 现在在 `AgeableMob`、`Tadpole` 等实体上暴露了 `AgeLocked`；后续需要确认 Minecraft 年龄逻辑是否改过，以及它应该如何和模组的锁年龄行为组合。
