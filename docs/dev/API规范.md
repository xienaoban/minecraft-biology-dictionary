# API 规范

本文记录 `api` 包面向第三方模组的 API 通用规范。改动这些 API 前先读本文，避免无意间破坏既定契约。

## 规范：查询方法优先不抛异常，管理器不可用时返回默认值

所有 API 方法在"发现管理器不可用"（会话未初始化、获取失败等）时返回默认值，而不是抛异常：

| 方法 | 不可用时 |
|---|---|
| `isDiscovered` | `false` |
| `getRecord` | `Optional.empty()` |
| `getProgress` | `(0, 0)` |
| `recordDiscovery` | `false` |

原因：第三方调用时机不可控（可能在初始化早期、世界加载前就调用），不该逼第三方为进度查询这类低风险调用做 try-catch。降级值是语义自然的状态——未初始化 = 什么都没发现/没有记录/请求未提交。

代价是调用方无法区分"真 false"与"降级 false"，但正常游戏运行中该场景几乎不出现（第三方在服务端路径调用时会话基本都在），可接受。

javadoc 统一用抽象的"if the discovery manager is unavailable"表述，不写具体是哪个 session 未初始化。

此语义只在 API 门面（`ServerDiscoveryApi` / `ClientDiscoveryApi`）兑现；`core` 层的 Manager/策略不承诺，内部代码需要自己的错误处理。
