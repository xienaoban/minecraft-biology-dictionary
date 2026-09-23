# Host Service 特性设计说明书

## 1. 项目概述

开发一个运行在宿主机上的轻量级 **Host Service**，为运行在 Docker 容器中的 Coding Agent 提供经过严格限制的宿主机能力。

当前第一阶段只实现 **Gradle 项目执行能力**，主要用于 Minecraft Mod 开发：

```text
Docker / Coding Agent
        │
        │ HTTP + JSON
        ▼
   Host Service
        │
        ▼
   Gradle Runner
        │
        ▼
  宿主机项目目录
        │
        ▼
 gradlew.bat / ./gradlew
```

但服务本身**不要命名或设计成仅支持 Gradle 的服务**。

未来可以继续增加其他明确受控的宿主机能力，因此应采用类似 Capability / Handler 的模块化设计。

---

# 2. 核心目标

### 2.1 解决的问题

Coding Agent 运行在 Docker 中，而 Minecraft 开发环境主要运行在宿主机：

- IntelliJ IDEA 在宿主机运行
- Minecraft Client / Server 在宿主机运行
- Windows 上的 Gradle / Java / Minecraft 开发环境在宿主机运行
- Docker 中的 Agent 需要修改源码，并能够触发宿主机上的 Gradle

不希望：

1. 在 Docker / WSL 中直接执行 Gradle
2. 把项目复制到 Docker，再复制回来
3. 在宿主机开放任意 Shell / PowerShell / CMD 执行接口
4. 给 Agent 一个可以执行任意 Windows 命令的 API

因此建立一个受控的宿主机服务。

---

# 3. 总体架构

目标结构：

```text
Host OS
├── IntelliJ IDEA
├── Minecraft Client / Test Server
├── Host Service
│
└── E:\project\minecraft\
    ├── minecraft-biology-dictionary-26.1.2
    ├── minecraft-biology-dictionary-26.3
    └── ...
             ▲
             │
             │ Docker Bind Mount
             │
WSL2
└── Docker
    └── Coding Agent
         │
         ├── 读取 / 修改源码
         │
         └── ./gradlew-host build
                    │
                    │ HTTP + JSON
                    ▼
              Host Service
                    │
                    ▼
              Gradle Capability
                    │
                    ▼
              gradlew.bat
                    │
                    ▼
                  Gradle
```

Docker 中看到：

```text
/project/minecraft/
```

宿主机实际目录：

```text
E:\project\minecraft\
```

两者指向同一个目录，不存在源码同步或复制过程。

---

# 4. 服务定位

服务名称不要强调 Gradle。

推荐名称：

```text
host-service
```

或者：

```text
host-agent-service
```

服务的概念应该是：

> 为容器中的 Agent 提供经过安全限制的宿主机能力。

而不是：

> 一个远程 Gradle 服务。

当前能力：

```text
Gradle
```

未来可以扩展：

```text
Gradle
Minecraft
其他明确允许的宿主机能力
```

但每一种能力都必须经过代码明确实现和授权。

---

# 5. 技术选型

## 5.1 使用 Python 3

使用 Python 3 实现 Host Service。

原因：

- 当前没有性能要求
- 核心工作主要是 HTTP、JSON、路径处理和子进程管理
- Python 在开发环境中已经存在
- 修改代码后可以直接重启，无需编译
- 跨平台
- Windows / Linux / macOS 均可运行
- 标准库已经足够完成第一版
- 便于未来快速修改和扩展

第一版应尽可能：

> **只使用 Python 标准库，不强制引入第三方依赖。**

主要使用：

```text
http.server / asyncio
json
subprocess
pathlib
os
secrets
threading / asyncio
```

具体 HTTP 实现可以根据代码结构选择 `http.server` 或 `asyncio`，不要求为了使用某个框架而增加依赖。

---

# 6. 项目结构

建议：

```text
host-service/
├── host_service.py
├── config.py
├── server.py
├── auth.py
├── path_mapper.py
│
├── capabilities/
│   ├── __init__.py
│   └── gradle.py
│
├── config.json
│
└── README.md
```

职责：

### `host_service.py`

程序入口：

- 加载配置
- 初始化服务
- 启动 HTTP Server

### `config.py`

负责：

- 读取 `config.json`
- 第一次启动时生成默认配置
- 配置校验

### `server.py`

负责：

- HTTP 请求
- JSON 解析
- Authentication
- 请求分发
- HTTP Streaming

### `auth.py`

负责：

- Token 验证
- Authorization Header 处理

### `path_mapper.py`

负责：

```text
Docker Path
    ↓
验证
    ↓
映射
    ↓
Host Path
```

这是安全边界的重要组成部分。

### `capabilities/gradle.py`

实现当前的 Gradle 能力：

- Gradle Task 白名单
- Gradle Wrapper 调用
- stdout/stderr Streaming
- exit code 返回

未来新增能力时继续增加模块。

---

# 7. 配置文件

服务第一次启动时，如果不存在配置文件，应自动生成默认配置。

默认配置：

```json
{
  "dockerRoot": "/project/minecraft",
  "hostRoot": "E:\\project\\minecraft",
  "server": {
    "host": "127.0.0.1",
    "port": 48721
  }
}
```

实际 `host` 的绑定地址需要根据 Docker → Host 的网络访问方式确定，不要假设所有 Windows / WSL 环境都使用同一个地址。

---

## 7.1 配置原则

配置文件只负责：

> **环境相关配置**

例如：

- Docker 根目录
- Host 根目录
- 服务监听地址
- 服务端口

配置文件**不负责安全策略和能力授权**。

不要加入：

```json
{
  "allowedCommands": [...]
}
```

之类的配置。

---

# 8. Docker Root / Host Root

默认：

```text
Docker Root:
/project/minecraft

Host Root:
E:\project\minecraft
```

例如 Docker 中：

```text
/project/minecraft/minecraft-biology-dictionary-26.1.2
```

映射到：

```text
E:\project\minecraft\minecraft-biology-dictionary-26.1.2
```

另一个项目：

```text
/project/minecraft/minecraft-biology-dictionary-26.3
```

映射到：

```text
E:\project\minecraft\minecraft-biology-dictionary-26.3
```

不需要为每一个 Minecraft 项目单独配置路径。

---

# 9. 路径映射安全要求

Docker 请求中只允许提供：

```text
Docker 侧路径
```

绝对不能允许客户端直接提交：

```text
E:\xxx\xxx
```

等 Host 路径。

例如合法请求：

```json
{
  "directory": "/project/minecraft/minecraft-biology-dictionary-26.1.2",
  "args": ["build"]
}
```

Host Service 自己计算：

```text
/project/minecraft/minecraft-biology-dictionary-26.1.2
        ↓
E:\project\minecraft\minecraft-biology-dictionary-26.1.2
```

---

## 9.1 防止路径穿越

必须防止：

```text
/project/minecraft/../Windows
/project/minecraft/foo/../../xxx
```

等路径。

不要简单使用字符串 `replace()` 进行路径映射。

应该使用 Python：

```python
pathlib.Path
```

或等价的规范化路径方法。

基本流程：

```text
收到 Docker Path
        ↓
规范化
        ↓
确认位于 dockerRoot 内
        ↓
计算 relative path
        ↓
拼接 hostRoot
        ↓
规范化 Host Path
        ↓
确认位于 hostRoot 内
        ↓
允许执行
```

如果不满足条件：

```text
拒绝请求
```

---

# 10. Gradle Capability

第一阶段只实现：

```text
Gradle Capability
```

它的职责是：

> 在经过验证的 Host 项目目录中调用该项目自己的 Gradle Wrapper。

Windows：

```text
gradlew.bat
```

Linux / macOS：

```text
./gradlew
```

不要依赖全局安装的 Gradle。

---

# 11. Gradle Task 白名单

第一版建议：

```python
TASKS = {
    "build": ["build"],
    "test": ["test"],
    "runClient": ["runClient"],
    "runTestServer": ["runTestServer"]
}
```

具体是否保留 `runTestServer` 等任务，可以根据实际 Minecraft 项目 Gradle 配置调整。

核心原则：

> Agent 只能请求代码中明确允许的 Task。

例如 Agent 请求：

```json
{
  "directory": "/project/minecraft/minecraft-biology-dictionary-26.1.2",
  "args": ["build"]
}
```

服务将其转换为：

```text
gradlew.bat build
```

但如果 Agent 请求：

```text
powershell ...
cmd ...
del ...
format ...
```

或者其他未注册能力：

```text
拒绝
```

---

# 12. 不允许任意 Shell

这是本项目的重要安全原则。

禁止：

```text
shell=True
```

禁止通过：

```text
cmd.exe /c ...
powershell.exe -Command ...
bash -c ...
sh -c ...
```

拼接 Agent 提供的命令。

不要实现：

```text
POST /shell
```

或者：

```json
{
  "command": "whatever command"
}
```

Host Service 绝对不能成为：

> Docker Agent → 任意 Windows 命令执行器。

应该采用：

```text
Agent
 ↓
Capability
 ↓
固定允许的参数
 ↓
固定程序
```

而不是：

```text
Agent
 ↓
任意命令
 ↓
Host Shell
```

---

# 13. Gradle 参数设计

第一版不要接受任意命令字符串。

推荐采用逻辑 Task：

```text
build
test
runClient
runTestServer
```

映射：

```text
build
    → ["build"]

test
    → ["test"]

runClient
    → ["runClient"]

runTestServer
    → ["runTestServer"]
```

如果以后确实需要：

```text
--stacktrace
--info
--debug
```

等参数，应单独设计参数白名单。

不要为了方便直接开放：

```text
args: arbitrary string[]
```

然后把它们拼进 Shell。

---

# 14. HTTP API

当前第一阶段使用 HTTP + JSON。

示例：

```http
POST /gradle
Authorization: Bearer <token>
Content-Type: application/json
```

请求：

```json
{
  "directory": "/project/minecraft/minecraft-biology-dictionary-26.1.2",
  "args": ["build"]
}
```

其中：

```text
directory
```

是 Docker 内路径。

---

# 15. Authentication

服务必须具有 Token Authentication。

请求：

```http
Authorization: Bearer <token>
```

服务验证 Token。

Token 不应写死在代码中，也不应该提交到 Git。

可以使用：

- 环境变量
- 本机 Secret
- 单独的未提交配置

具体实现以简单可靠为主。

---

# 16. 服务监听范围

Host Service 的目标不是成为公网服务。

默认应尽可能：

> 只允许本机 / WSL / Docker 所需的网络范围访问。

不要默认暴露到：

```text
0.0.0.0
```

除非确实需要，并同时配置防火墙和认证。

Token 不是让服务可以随意暴露公网的理由。

---

# 17. `gradlew-host`

每个 Minecraft 项目根目录放置：

```text
gradlew-host
```

与：

```text
gradlew
gradlew.bat
```

处于同一级。

例如：

```text
minecraft-biology-dictionary-26.1.2/
├── gradlew
├── gradlew.bat
├── gradlew-host
├── build.gradle
├── settings.gradle
└── src/
```

---

# 18. `gradlew-host` 的职责

`gradlew-host` 必须保持非常简单。

它不负责：

- 路径安全
- Host 路径转换
- Gradle 白名单
- 权限判断
- Windows 命令执行

它只负责：

```text
获取当前工作目录
        ↓
获取命令行参数
        ↓
发送 HTTP 请求
        ↓
接收 Streaming Output
        ↓
输出到终端
        ↓
返回 Host Service 的 exit code
```

---

# 19. Agent 使用方式

对 Agent 来说，体验应该尽可能接近普通 Gradle：

正常：

```bash
./gradlew build
```

改成：

```bash
./gradlew-host build
```

例如：

```bash
./gradlew-host build
```

然后看到：

```text
> Configure project :
...

> Task :compileJava
...

> Task :build

BUILD SUCCESSFUL
```

并且 Agent 应能根据最终退出码判断：

```text
0 = 成功
非 0 = 失败
```

---

# 20. 输出必须实时转发

这是重要需求。

不要：

```text
Gradle 开始
    ↓
等待 Gradle 完成
    ↓
一次性返回全部输出
```

否则 Coding Agent 会认为程序卡死。

正确行为：

```text
Gradle
  │
  ├── stdout ──┐
  └── stderr ──┤
               ▼
          Host Service
               │
               │ Streaming
               ▼
          gradlew-host
               │
               ▼
          Agent Terminal
```

Gradle 输出产生后应尽快发送。

允许有非常轻微的缓冲，例如：

```text
几十到几百毫秒
```

但不能等到整个进程结束才返回。

---

# 21. stdout / stderr

尽可能保留 stdout 和 stderr 的语义。

至少应该保证：

- 输出顺序基本合理
- 不会因为缓冲造成长时间无输出
- 最终 exit code 正确返回

如果 HTTP Streaming 层难以完全保持 stdout/stderr 两条独立流，可以设计统一的事件流，例如：

```json
{
  "type": "stdout",
  "data": "..."
}
```

或者：

```json
{
  "type": "stderr",
  "data": "..."
}
```

但 `gradlew-host` 最终应把它们正确呈现在 Agent 的终端环境中。

---

# 22. Gradle 进程启动

Windows：

```text
工作目录：
E:\project\minecraft\minecraft-biology-dictionary-26.1.2

执行：
gradlew.bat build
```

Linux / macOS：

```text
工作目录：
/home/user/project/minecraft/minecraft-biology-dictionary-26.1.2

执行：
./gradlew build
```

不要通过 Shell 启动。

使用 Python `subprocess` 的参数数组形式。

---

# 23. Java 环境

Gradle 实际运行在 Host 上，因此：

> Java 选择也由 Host Service / Host 环境负责。

不同 Minecraft / Gradle 项目可能要求不同 Java 版本。

第一版不要过度复杂化。

建议优先支持：

```text
JAVA_HOME
PATH
```

或者服务配置中的明确 Java 路径。

例如未来可以扩展：

```json
{
  "java": {
    "mode": "environment"
  }
}
```

或者：

```json
{
  "java": {
    "mode": "fixed",
    "path": "..."
  }
}
```

但第一版不要求自动读取 IntelliJ IDEA 的 Gradle JVM 配置。

未来如果确实有需要，可以增加：

> 自动识别 IntelliJ IDEA / 项目 Gradle JVM 配置

但不要让第一版依赖 IDEA 的内部配置格式。

---

# 24. 长时间运行任务

`build` / `test` 这种任务可以直接：

```text
POST /gradle
        ↓
Streaming Response
        ↓
进程结束
        ↓
返回 exit code
```

但是：

```text
runClient
runTestServer
```

可能是长期运行进程。

未来可以提供 Job API：

```text
POST /jobs
```

返回：

```json
{
  "jobId": "abc123",
  "status": "running"
}
```

然后：

```text
GET /jobs/abc123
```

获取：

- 状态
- 输出
- exit code

第一版如果实现同步 Streaming 已经足够，不需要为了未来需求提前实现完整 Job 系统。

---

# 25. 并发控制

同一个 Minecraft 项目不建议同时启动多个 Gradle 任务。

例如：

```text
项目 A
├── build
└── runClient
```

同时运行可能产生冲突。

建议未来加入：

```text
同一 project directory：
最多一个 Host Service Gradle Job
```

如果已有任务：

```text
HTTP 409 Conflict
```

或者进入队列。

不同项目之间可以允许并行。

例如：

```text
项目 A → build
项目 B → build
```

可以同时执行。

第一版可以先实现简单拒绝，不需要复杂任务队列。

---

# 26. 安全模型

安全边界必须明确：

```text
Docker Agent
```

被视为：

> **不可信客户端。**

即使 Agent 能在容器里执行任意命令，也不应因此获得 Host 任意命令执行能力。

Host Service 必须独立验证：

```text
Authentication
        ↓
Request Schema
        ↓
Capability
        ↓
Docker Path
        ↓
Host Path
        ↓
Allowed Task
        ↓
Process
```

任何一层失败：

```text
拒绝
```

---

# 27. 不信任 Docker

不要认为：

> 因为 HTTP 只从 Docker 访问，所以请求一定安全。

必须假设 Agent 可以发送：

```text
任意 HTTP 请求
任意 JSON
任意路径
任意 Task
```

因此所有安全限制必须由 Host Service 自己执行。

---

# 28. Host Service 不应提供的接口

第一版明确禁止设计：

```text
/shell
/exec
/powershell
/cmd
/bash
/run
```

等通用执行接口。

尤其禁止：

```json
{
  "command": "..."
}
```

这种 API。

如果未来确实需要某个宿主机功能：

> 单独增加一个 Capability，并为其设计固定的 API。

例如：

```text
/gradle
/minecraft/...
```

而不是开放：

```text
/exec
```

---

# 29. Capability 设计

建议建立统一概念：

```python
Capability
```

每个 Capability 负责：

```text
请求验证
参数验证
权限限制
实际执行
输出处理
```

例如：

```text
capabilities/
└── gradle.py
```

未来：

```text
capabilities/
├── gradle.py
├── minecraft.py
└── ...
```

服务层负责：

```text
HTTP
认证
路由
配置
生命周期
```

Capability 层负责：

```text
具体能力
```

这样可以避免 Host Service 最后变成一个巨大的：

```text
if command == ...
```

文件。

---

# 30. 配置与代码权限必须分离

配置：

```text
dockerRoot
hostRoot
host
port
```

属于：

> 环境配置。

代码：

```text
允许哪些 Capability
允许哪些 Gradle Task
允许哪些参数
```

属于：

> 安全策略。

两者必须分开。

特别是：

> 修改 config.json 不应该获得新的宿主机执行权限。

---

# 31. 错误处理

Host Service 应返回明确的错误。

例如：

### Token 错误

```text
401 Unauthorized
```

### Capability 不存在

```text
404 Not Found
```

### Task 不允许

```text
403 Forbidden
```

### 路径非法

```text
403 Forbidden
```

或：

```text
400 Bad Request
```

### 项目不存在

```text
404 Not Found
```

### gradlew 不存在

```text
400 / 404
```

### Gradle 执行失败

HTTP 请求本身可以正常完成，但返回：

```text
exitCode != 0
```

使 `gradlew-host` 最终以相同的非零退出码退出。

---

# 32. 日志

Host Service 自己应有少量日志，例如：

```text
[INFO] Host Service started
[INFO] Gradle request
[INFO] Project: ...
[INFO] Task: build
[INFO] Process started
[INFO] Process exited with code 0
```

但不要记录：

```text
Authorization Token
```

等敏感信息。

日志不应该把 Gradle 全部输出重复记录一遍，否则会造成大量冗余。

---

# 33. 首次启动行为

如果：

```text
config.json
```

不存在：

1. 创建默认配置
2. 提示用户配置文件已经生成
3. 根据设计选择继续启动或退出等待配置

推荐第一次启动时：

```text
生成配置
提示用户检查配置
正常启动
```

如果配置不合法：

```text
明确报错
退出
```

不要静默使用危险的默认值。

---

# 34. Windows 使用方式

Windows 下服务可以直接：

```powershell
python host_service.py
```

启动。

开发阶段不要求注册成 Windows Service。

以后如果使用稳定版本，可以考虑：

```text
Windows Service
```

或其他后台启动方式。

但不要在第一版加入复杂的系统服务安装逻辑。

---

# 35. Linux / macOS

服务设计必须从第一天保持跨平台。

Linux：

```text
python3 host_service.py
```

macOS：

```text
python3 host_service.py
```

Gradle 使用：

```text
./gradlew
```

Windows：

```text
gradlew.bat
```

不要在核心代码中写死：

```text
Windows only
```

例如不要把 Host Service 设计成只能调用：

```text
cmd.exe
```

---

# 36. Git / 项目文件

Host Service 本身应该可以放进一个独立 Git 仓库。

不要把：

```text
config.json
```

中的真实 Token 提交进去。

可以提供：

```text
config.example.json
```

例如：

```json
{
  "dockerRoot": "/project/minecraft",
  "hostRoot": "E:\\project\\minecraft",
  "server": {
    "host": "127.0.0.1",
    "port": 48721
  }
}
```

真实：

```text
config.json
```

加入 `.gitignore`，如果其中包含机器相关信息或 Secret。

---

# 37. 第一阶段实现范围

第一版必须完成：

### Host Service

- [ ] Python 3
- [ ] HTTP Server
- [ ] JSON 请求
- [ ] Bearer Token Authentication
- [ ] 配置文件自动生成
- [ ] Docker Root / Host Root 配置
- [ ] Docker Path → Host Path 映射
- [ ] 路径穿越保护
- [ ] Capability 架构
- [ ] Gradle Capability
- [ ] Gradle Task 白名单
- [ ] Windows `gradlew.bat`
- [ ] Linux/macOS `./gradlew`
- [ ] stdout 实时 Streaming
- [ ] stderr 实时 Streaming
- [ ] exit code 正确传递
- [ ] 明确的 HTTP 错误
- [ ] 基础日志

### Docker 侧

- [ ] `gradlew-host`
- [ ] 获取当前目录
- [ ] 获取 argv
- [ ] POST Host Service
- [ ] 实时显示输出
- [ ] 正确返回 exit code
- [ ] 服务不可用时给出清晰错误

---

# 38. 第一阶段明确不做

暂时不要实现：

- 任意 Shell API
- PowerShell API
- CMD API
- 任意程序执行 API
- 自动修改安全白名单
- IDEA 配置自动解析
- 复杂 Java 自动选择
- Web UI
- 数据库
- Redis
- 消息队列
- Docker API
- 复杂任务调度系统
- 远程公网访问
- 多用户权限系统
- 完整 Windows Service 安装器

保持第一版简单。

---

# 39. 后续可扩展方向

未来可以逐步加入：

```text
Host Service
│
├── Gradle Capability
│
├── Minecraft Capability
│
├── File Capability
│
└── Other Capabilities
```

但每一个 Capability 都必须遵循：

```text
明确用途
明确参数
明确权限
明确资源范围
禁止任意命令执行
```

例如如果未来需要“启动 Minecraft”，也应该提供：

```text
POST /minecraft/runClient
```

而不是：

```text
POST /exec
{
    "command": "..."
}
```

---

# 40. 设计原则总结

整个项目遵循以下原则：

### 原则 1：一个源码目录

Docker 与 Host 使用同一份源码。

不做：

```text
Docker checkout
→ 修改
→ push
→ Host pull
```

而是：

```text
Host filesystem
        ↑
        │ bind mount
        │
Docker
```

---

### 原则 2：Gradle 在 Host 执行

不要在：

```text
WSL / Docker
```

中执行 Minecraft Gradle。

实际执行：

```text
Host
└── Gradle Wrapper
```

这样：

- 使用 Host Java
- 使用 Host Gradle Cache
- 使用 Host Maven Cache
- 使用 Host Minecraft 开发环境
- 避免 `/mnt` 文件系统性能问题

---

### 原则 3：Agent 不直接控制 Host

Agent 只能：

```text
请求 Capability
```

不能：

```text
执行 Host Shell
```

---

### 原则 4：服务才是安全边界

Docker 不是安全边界。

即使 Agent 可以在容器里执行：

```text
rm
bash
python
curl
```

Host Service 仍必须保证：

```text
不能访问 hostRoot 之外的路径
不能执行未授权 Task
不能执行任意 Host 程序
```

---

### 原则 5：配置环境，代码决定权限

配置：

```text
“我的机器在哪里”
```

代码：

```text
“Agent 可以做什么”
```

---

### 原则 6：保持通用服务定位

不要：

```text
Gradle Host Service
```

而是：

```text
Host Service
```

目前：

```text
Host Service
└── Gradle Capability
```

以后：

```text
Host Service
├── Gradle Capability
├── Minecraft Capability
└── ...
```

---

### 原则 7：第一版不要过度工程化

目标不是开发一个企业级远程执行平台。

目标是：

> **用一个简单、透明、可修改、跨平台的 Python 服务，为 Docker Coding Agent 提供有限且安全的宿主机能力。**

优先保证：

```text
简单
可靠
可调试
可扩展
安全边界明确
```

而不是提前引入复杂框架。

---

# 41. 验收标准

完成后，应至少能够完成以下流程：

```text
1. Host 启动 Host Service
        ↓
2. 自动生成 / 读取 config.json
        ↓
3. Docker 启动 Coding Agent
        ↓
4. Agent 进入：
   /project/minecraft/minecraft-biology-dictionary-26.1.2
        ↓
5. Agent 执行：
   ./gradlew-host build
        ↓
6. gradlew-host 获取当前目录和参数
        ↓
7. POST /gradle
        ↓
8. Host Service 验证 Token
        ↓
9. Docker Path 映射到：
   E:\project\minecraft\minecraft-biology-dictionary-26.1.2
        ↓
10. 验证 Task：
    build
        ↓
11. 执行：
    gradlew.bat build
        ↓
12. Gradle 输出实时返回 Docker
        ↓
13. Agent 实时看到 Gradle 输出
        ↓
14. Gradle 结束
        ↓
15. Host Service 返回 exit code
        ↓
16. gradlew-host 使用相同 exit code 退出
```

同时以下请求必须被拒绝：

```text
/project/minecraft/../Windows
```

```text
E:\Windows\...
```

```text
powershell ...
```

```text
cmd /c ...
```

```text
任意未注册 Gradle Task
```

```text
无效 Token
```

```text
Host Root 之外的路径
```

最终形成：

```text
Docker Coding Agent
        │
        │ 受控 HTTP API
        ▼
┌───────────────────────┐
│     Host Service      │
│                       │
│  Auth                 │
│  Path Mapping         │
│  Capability Dispatch  │
│                       │
│  ┌─────────────────┐  │
│  │ Gradle          │  │
│  │ Capability      │  │
│  └────────┬────────┘  │
└───────────┼───────────┘
            ▼
       Host Gradle
            ▼
     Minecraft Project
```

这就是第一阶段的完整目标架构。