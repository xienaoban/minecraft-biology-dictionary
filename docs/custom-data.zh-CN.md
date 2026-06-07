# 自定义数据

## 自定义生物描述（资源包）

生物辞典支持在每个生物的详情页面上显示自定义描述。描述通过 Minecraft 标准的**语言文件**系统定义 — 在模组的语言 JSON 中添加条目即可。

> **注意：** 语言文件属于资源包，仅在**客户端**生效。请放在客户端资源包中。

### 翻译键

模组按以下顺序检查翻译键，使用第一个找到的：

| 优先级 | 键格式 | 示例 |
|:------:|:-------|:-----|
| 1 | `entity.<命名空间>.<路径>.description` | `entity.minecraft.cow.description` |
| 2 | `entity.<命名空间>.<路径>.desc` | `entity.minecraft.cow.desc` |
| 3 | `lore.<命名空间>.<路径>` | `lore.minecraft.cow` |

### 文件位置

将翻译条目放在标准语言文件中：

```
assets/<命名空间>/lang/<语言代码>.json
```

### 示例

在 `assets/mymod/lang/zh_cn.json` 中：

```json
{
  "entity.mymod.custom_mob.description": "一种出没于深层地下的神秘生物。"
}
```

### 行为

- 如果没有匹配的翻译键，描述组件默认隐藏。可在客户端配置中修改此行为（`Hide Entity Description Widget If Not Found`）。
- 描述支持 Minecraft 的文本格式（颜色代码、可翻译组件等），因为它们作为标准 `Component` 渲染。

---

## 自定义生成描述（数据包）

生物辞典通过 Minecraft 内置的注册表数据读取生物的生成群系和结构信息。对于大多数原版和模组生物，这可以直接生效，但部分生物的生成方式比较特殊（如蜜蜂在花朵附近生成、监守者在远古城市中生成），仅靠注册表数据无法准确捕获。

为了弥补这些不足，模组支持**数据包覆盖**，文件路径为：

> **注意：** 这些文件属于数据包，仅在**服务端**生效。请放在服务端数据包中。

```
data/<命名空间>/biologydictionary/entity_spawn/<实体命名空间>.<实体路径>.json
```

- `<命名空间>` — 数据包/模组的命名空间（如 `mymod`）
- `<实体命名空间>.<实体路径>` — 完整实体 ID，以 `.` 分隔（如 `minecraft.bee`、`mymod.custom_mob`）

### JSON 格式

```json
{
  "biomes": {
    "add": ["<群系ID>", "#<群系标签>"],
    "remove": ["<群系ID>"],
    "overwrite": ["<群系ID>"]
  },
  "structures": {
    "add": ["<结构ID>"],
    "remove": ["<结构ID>"],
    "overwrite": ["<结构ID>"]
  }
}
```

`biomes` 和 `structures` 都是可选的，可以只写其中一个。

### 操作类型

| 操作 | 说明                              |
|:----:|:--------------------------------|
| `add` | 在已有列表中追加条目。重复条目会被跳过并输出警告。       |
| `remove` | 从已有列表中移除条目。不存在的条目会被跳过并输出警告。     |
| `overwrite` | 完全替换列表。不可与 `add`/`remove` 同时使用。 |

> **注意：** 如果同时使用 `add`/`remove` + `overwrite`，只生效 `overwrite`（并输出警告）。当多个数据包对同一实体使用 `overwrite` 时，仅最高优先级的生效，因此不推荐在多包场景下使用。

### 标识符

- **直接 ID**：`"minecraft:flower_forest"`
- **标签引用**（以 `#` 开头）：`"#minecraft:is_forest"` — 解析为该标签下的所有群系/结构

### 示例

为蜜蜂添加生成群系：

```json
{
  "biomes": {
    "add": ["minecraft:flower_forest", "minecraft:meadow", "minecraft:cherry_grove", "minecraft:forest", "minecraft:birch_forest", "minecraft:old_growth_birch_forest"]
  }
}
```

为监守者添加生成结构：

```json
{
  "structures": {
    "add": ["minecraft:ancient_city"]
  }
}
```

### 校验

- 文件路径中未知的实体类型会被跳过并输出警告。
- 未知的群系/结构 ID 会被跳过并输出警告。
- 未知的标签会被跳过并输出警告。
- JSON 格式错误会输出日志错误，但不会导致崩溃。
