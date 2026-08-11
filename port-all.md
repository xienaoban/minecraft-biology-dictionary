背景：正在从我的世界1.21.11（E:\project\minecraft\minecraft-biology-dictionary-architectury）移植到我的世界1.21.1（当前项目）。当前是一个模块一个模块地回合。注意1.21.1和1.21.11我的世界源码差异还是很大的。
我的世界1.21.11的源码位置在 E:\project\minecraft\mc-source\1.21.11
我的世界1.21.1的源码位置在 E:\project\minecraft\mc-source\1.21.1
请尽量参考本机的1.21.11、1.21.1的源码，因为你在网上找到的版本指不定是啥版本的

背景：正在从我的世界1.21.1（E:\project\minecraft\minecraft-biology-dictionary-architectury-1.21.1）移植到我的世界1.20.1（当前项目）。当前是一个模块一个模块地回合。注意1.21.1和1.20.1我的世界源码差异还是有不少的。
我的世界1.21.1的源码位置在 E:\project\minecraft\mc-source\1.21.1
我的世界1.20.1的源码位置在 E:\project\minecraft\mc-source\1.20.1
请尽量参考本机的1.21.1、1.20.1的源码，因为你在网上找到的版本指不定是啥版本的
还有，1.20.1使用的是JDK17、而1.21.1使用的是JDK21


很好！我已经全面了解了源项目的结构。以下是我建议的移植顺序计划：

  📋 移植计划

  为什么这个顺序？

  Minecraft 版本之间的差异主要体现在：
  1. Mixin 目标类 - MCP 映射名可能变化
  2. 网络 API - 1.21.11 和 1.21.1 的网络系统可能有差异
  3. 渲染 API - 客户端渲染 API 经常变化

  分阶段移植策略

  第一阶段：基础工具类和常量 (platform/util/, Lang.java)
  - 这些是最基础的，不依赖其他模块
  - 可以快速验证编译环境是否正常

  第二阶段：平台抽象层 (platform/)
  - 定义 @ExpectPlatform 接口
  - 建立跨平台架构

  第三阶段：属性系统 (core/property/)
  - 核心功能，独立于 UI 和网络
  - 包含 EntityProperty、VanillaEntityProperties 等

  第四阶段：网络系统 (net/, platform/net/)
  - 数据包定义和网络管理器
  - 需要验证 1.21.1 的网络 API 是否与 1.21.11 兼容

  第五阶段：技能系统 (core/skill/)
  - 依赖于属性系统

  第六阶段：UI 组件 (gui/component/)
  - 独立的 UI 组件类

  第七阶段：屏幕 (gui/screen/)
  - 完整的 GUI 界面

  第八阶段：平台实现 (fabric/ & neoforge/)
  - 平台特定的具体实现

  第九阶段：Mixin (mixin/)
  - 需要检查 1.21.1 的目标类结构

  第十阶段：资源文件和完整测试
  - 语言文件、配置等
  - 完整编译运行测试