# TextBlock 文本块组件设计

## 定位

一个不可变的独立文本块组件（`ScreenElement` 子类），为词典 UI 提供统一的文本展示能力：单行/多行、缩放、对齐、省略号、行数限制、hover 全文。替代 `EntityDescriptionWidget` / `EntityCardWidget` 里各自手写的文本布局逻辑。

## 需求

1. **单行 / 多行**：`splitLines` 开关，多行按 box 宽度换行。
2. **全文统一文字大小**：`scale` 作用于全部行。
3. **默认字体颜色**：`color`，默认黑色。
4. **省略号**：
   - 单行：行末 `...`（`FontUtils.truncateByWidth`）；
   - 多行：追加一行 `...`（多行"最后一行行末省略号"实现成本高，妥协为独立省略行）；
   - 不开省略号时，超出 box 就超出，不做处理。
5. **对齐方式**：水平 `horizontalAlignment` LEFT / CENTER / RIGHT（默认左对齐）；垂直 `verticalAlignment` TOP / CENTER / BOTTOM（默认居中）。
6. **最大行数三态**（`maxLines`）：
   - `>0`：硬性行数上限，省略号行计入，不受内容区高度约束；
   - `0`：按内容区高度能容纳的整行数，不超出；
   - `-1`：无限制，全部行都绘制；
   - 超限且未开省略号：直接截断。
7. **hover 展示全文**：无省略号、tooltip 宽度与 box 一致、tooltip 首行与可见文本首行同高。
8. **行间距与 padding**：相邻行之间可加额外间距；内容区为 box 内缩上下左右 padding 后的区域（从 `box.left + leftPadding`、`box.top + topPadding` 起算），换行、截断、对齐全部基于内容区。

## 关键语义决策

### 内容区与行间距

padding 把 box 内缩为内容区：`left + paddingLeft` / `top + paddingTop` / `right - paddingRight` / `bottom - paddingBottom`。换行宽度、可见行数、水平/垂直对齐、单行截断、hover 全文的换行宽度全部基于内容区，而不是原始 box——内容区才是真正的布局边界。

行间距 `lineSpacing` 是相邻两行之间的额外像素间距（与 `EntityDescriptionWidget` 的 `LINE_SPACING` 同语义，不乘 `scale`），参与行高累计与可见行数计算：n 行总高 = `n * lineHeight + (n-1) * lineSpacing`，可见行数按 `floor((contentHeight + lineSpacing) / (lineHeight + lineSpacing))` 推算。

### 行数限制三态

`maxLines > 0` 是硬性行数，与 box 高度无关（使用者自行保证布局空间）。行数可能超出 box 高度，此时垂直偏移一律归零（从 box 顶部开始画），避免负偏移。

垂直方向由 `verticalAlignment` 决定：TOP 顶对齐 / CENTER 垂直居中 / BOTTOM 底对齐；行数放得下时按对应语义布局，放不下时回退到顶部。

`0` 按 box 高度换算整行数；`-1`（及任意负值）无限制，行数永远不截断，也永不触发省略号。

### truncated（被截断）与 ellipsis 解耦

`truncated` 的语义是"显示内容 ≠ 全文"，只由行数/宽度限制决定，与是否开省略号无关。`ellipsis` 仅是截断处的视觉标记（单行行末 / 多行追加行）。

由此，"截断但没开省略号"同样算 truncated，同样能触发 hover 全文。

### hover 全文是独立开关

`showFullOnHover` 与 `ellipsis` 无关：不开这个开关，hover 不显示任何内容。

- hover 展示全文（按内容区宽度重新换行），不再带省略号；
- tooltip 宽度不需要给 `renderComponentTooltip` 加最大宽度参数：用"内容区宽 / scale"作为预换行宽度即可让每行不超出内容区，`renderLinedTooltipCentered` 居中绘制；
- tooltip 首行 y 与可见文本首行 y 对齐（-2 微调），沿用 `EntityDescriptionWidget` 的做法；
- 性能：hover 每帧重算全文行，暂不做缓存（全局同一时刻只有一个 hover 元素，先不加复杂度）。

### 单行模式

- 单行不开省略号：渲染完整文本，超出 box 就溢出，内容未丢失，不算 truncated，无 hover 全文——与"超出就超出"的语义一致；
- 单行开省略号：`truncateByWidth` 截断，truncated 为真。

### 缓存

渲染行结果按 box 宽高缓存，box 变化时重算；文本等配置不可变，无需失效。

已知边界：单行省略走 `FontUtils.truncateByWidth`，截断结果以 `literal` 返回，会丢失原 `Component` 的样式（颜色/格式）——截断带格式文本时需注意。

## API 形态

- `Builder`（`TextBlock.create()`）：`text` / `scale` / `color` / `horizontalAlignment` / `verticalAlignment` / `lineSpacing` / `padding` / `splitLines` / `ellipsis` / `maxLines` / `showFullOnHover`；
- 简版构造器 `new TextBlock(text, scale, color[, alignment])`：单行、无省略、无 hover、`maxLines` 0；
- 默认值：`scale` 0.5、`color` BLACK、左对齐、垂直居中、行间距 0、padding 0、单行、无省略号、`maxLines` 0、不 hover。

## 使用示例

- `WarningDialog` 标题：简版构造器（单行，无省略）；
- `WarningDialog` 正文：Builder 开 `splitLines` + `ellipsis` + `showFullOnHover`（多行省略 + hover 全文）；
- 多行省略与 hover 首行对齐参照 `EntityDescriptionWidget`，单行省略参照 `EntityCardWidget`。
