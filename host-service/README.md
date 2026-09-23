# Host Service

为容器中的 Coding Agent 提供**受严格限制**的宿主机能力。当前只实现 Gradle 能力：
容器里的 Agent 执行 `python gradlew-host.py <task>`，由宿主机上的本服务调用宿主机项目
自己的 Gradle Wrapper（Windows `gradlew.bat` / Linux·macOS `./gradlew`），输出实时回传，
退出码原样返回。

服务本身不叫「Gradle 服务」：HTTP / 认证 / 路由 / 配置 / 生命周期属于服务层，
具体能力放在 `capabilities/`，新增能力只需增加一个模块，不需要动服务层。

```
容器 Agent ──HTTP+JSON──▶ Host Service ──▶ Gradle Capability ──▶ gradlew(.bat) ──▶ 项目
```

## 快速开始

宿主机（Windows 示例）：

```powershell
cd E:\project\minecraft\host-service
D:\python-3.10.11-embed-amd64\python.exe host_service.py
```

Linux / macOS：

```bash
cd host-service
python3 host_service.py
```

首次启动时如果 `config.json` 不存在，会**自动生成一份默认配置**（`config.example.json`
是同一份模板），然后继续启动：

- `dockerRoot` 默认为 `/project/minecraft`；`hostRoot` 是占位值（Windows `C:\minecraft`，
  其他平台 `/srv/minecraft`），**本机几乎肯定不存在**。
- 因此紧接着就会报错退出，提示你改哪一项：

  ```text
  [ERROR] hostRoot does not exist on this machine: C:\minecraft
  [ERROR] edit E:\project\minecraft\host-service\config.json and point hostRoot at ...
  ```

- 这是有意的：不会静默用一个能跑但危险的默认路径。改完 `hostRoot` 再启动即可。
- `config.json` 不入库，所以换机器时它会按上面的流程重新生成，改一下路径就能跑。

常用参数：

```
--config PATH     指定配置文件（默认服务目录下的 config.json）
--host / --port   临时覆盖 server.host / server.port
--verbose         打印每个 HTTP 请求
--show-config     打印生效配置后退出
```

## 配置：只描述环境，不授予权限

```json
{
  "dockerRoot": "/project/minecraft",
  "hostRoot": "E:\\project\\minecraft",
  "server": { "host": "0.0.0.0", "port": 11109 }
}
```

- `dockerRoot`：容器内看到的项目根（POSIX 路径，必须以 `/` 开头）。
- `hostRoot`：同一个目录在宿主机上的路径。
- `server.host`：`0.0.0.0` 才能被容器访问；只在本机自测可改回 `127.0.0.1`。

**改 `config.json` 不会获得任何新的执行权限**：允许哪些能力、哪些命令、
哪些参数，全部写在代码里（`capabilities/gradle.py` 的两张白名单表）。

## HTTP API

请求与响应都是 JSON，`Content-Type: application/json`。

| 方法与路径 | 作用 |
|---|---|
| `POST /gradle` | 运行一个白名单任务，返回 NDJSON 事件流 |
| `POST /gradle/cancel` | 终止同一项目正在运行的任务 |
| `POST /probe` | 客户端自举：问「我这份路径你认得吗」 |
| `GET /health` | 存活检查，返回版本、平台、dockerRoot/hostRoot |

### `POST /gradle`

```json
{ "directory": "/project/minecraft/minecraft-biology-dictionary-26.3", "args": ["fabric:build"] }
```

- `directory`：**容器内**路径。宿主机路径（`E:\...`）、`..` 穿越、`dockerRoot` 之外
  一律拒绝。
- 命令用 `args: ["<command>"]`（与任务书一致）或 `task: "<command>"`，二选一。
- **命令原样交给 Gradle，不做任何转义、改写或拆分**，所以大模型可以直接用
  `build` / `fabric:build` / `neoforge:build` / `fabric:runTestServer` 这类原生写法。
  服务端只做一件事：判断这条命令在不在白名单里。

响应是 chunked NDJSON，每种事件一行：

```json
{"type":"start","task":"fabric:build","hostDirectory":"E:\\...","tasks":["fabric:build"]}
{"type":"task_start","task":"fabric:build","argv":["...\\gradlew.bat","fabric:build"]}
{"type":"stdout","data":"> Task :fabric:compileJava\n"}
{"type":"stderr","data":"..."}
{"type":"task_end","task":"fabric:build","exitCode":0}
{"type":"end","exitCode":0,"reason":"ok"}
```

Gradle 本身失败时 HTTP 仍是 200，`end.exitCode` 非 0 —— 客户端据此退出。

### 白名单命令

代码里就两张表（`capabilities/gradle.py`）：

```python
ALLOWED_SUBPROJECTS = frozenset({"fabric", "neoforge"})
ALLOWED_TASKS = frozenset({"build", "test", "runClient", "runTestServer"})
```

一条命令要么是 `ALLOWED_TASKS` 里的裸任务，要么是 `<subproject>:<task>` 且两段都在表里。
新增允许的命令就往这两张表里加，别的地方不用动。

| 命令 | 实际执行 |
|---|---|
| `build` / `test` / `runClient` / `runTestServer` | `gradlew build` 等，在项目根目录 |
| `fabric:build` / `neoforge:build` | `gradlew fabric:build` 等，原样传递 |
| `fabric:runTestServer` / `neoforge:runClient` | 同上，任意白名单组合都行 |
| `all` | 依次 `gradlew fabric:build`、`gradlew neoforge:build`（便捷别名，非 Gradle 任务） |

### 错误码

| 情况 | 状态码 |
|---|---|
| JSON 非法 / 字段缺失 / 多余字段 / 命令以 `:` 开头 | 400 |
| 路径穿越、宿主机路径、子项目或任务不在白名单 | 403 |
| 项目或 wrapper 不存在 | 404 |
| 该项目已有任务在跑 | 409 |
| 请求体过大 / Content-Type 不对 | 413 / 415 |

## 容器侧：`gradlew-host.py`

放在项目根目录（与 `gradlew`、`gradlew.bat` 同级），用标准库实现，无第三方依赖：

```bash
cd /project/minecraft/minecraft-biology-dictionary-26.3
python gradlew-host.py build             # ≈ ./gradlew build
python gradlew-host.py fabric:build      # ≈ ./gradlew fabric:build
python gradlew-host.py all               # 依次 fabric:build、neoforge:build
python gradlew-host.py build -v          # 额外打印握手与命令行
```

- 先 `POST /probe` 自举：把当前目录、真实路径、`/project/minecraft` 等候选路径报给服务，
  由服务回答哪个能映射（优先含 Gradle Wrapper 的目录，其次路径最长的）。这样容器里目录名
  叫 `/project` 还是 `/project/minecraft` 都不用改宿主机配置。
- 若自举结果不是你想要的目录（例如服务挂载的是更上层的目录），用
  `HOST_SERVICE_DIRECTORY=/project/minecraft/xxx` 直接指定容器路径，跳过探测。
- 输出边产生边写 stdout / stderr，不攒到最后。
- 退出码 = Gradle 退出码；连不上服务、映射不上、任务名非法时退出码 2 并给出可操作提示。
- 服务地址取环境变量 `HOST_SERVICE_URL`，默认 `http://host.docker.internal:11109`，
  走直连、不吃容器里的 `HTTP_PROXY`。

## 安全边界

- **没有** `/shell`、`/exec`、`/cmd`、`/powershell`，也不接受 `{"command": "..."}`。
- 子进程一律 `shell=False` + 参数数组，只用项目自己的 Wrapper，不依赖全局 Gradle。
- 路径先按 POSIX 语义规范化（`..`、`.` 全部收敛），再确认落在 `dockerRoot` 内，
  换算成相对路径后拼到 `hostRoot`，再确认结果仍在 `hostRoot` 内；宿主机侧还会解析
  symlink，防止链接逃逸。
- 容器被视为不可信客户端：鉴权不是重点，**所有限制都由服务端自己执行**。
- 同一项目同时只允许一个任务，第二个请求得到 409；跨项目可并行。项目槽位在响应写完的瞬间
  释放，紧接着的下一个请求不会被误判为冲突。卡死的任务可以用
  `POST /gradle/cancel` 终止（会连同子进程一起杀）；如果取消请求正好落在「已占槽、还没
  拉起进程」的窗口里，服务会记住这次取消并在拉起前直接中止，不会白跑一个构建。
- 当前**没有** Bearer Token（按需去掉的）。因此 `server.host` 默认虽然是 `0.0.0.0`，
  请只在可信网络里使用；如果不想让同网段其他机器访问，可回退 `127.0.0.1` 或加防火墙规则：

  ```powershell
  New-NetFirewallRule -DisplayName "host-service" -Direction Inbound -Action Allow `
    -Protocol TCP -LocalPort 11109 -Profile Private
  ```

## Windows 实测结论（本机）

- 用 Windows 侧 Python 跑服务、WSL 侧访问：`/health`、`/probe`、真实
  `gradlew.bat <command>` 全通。实测在真实项目上跑 `gradlew-host.py test`：
  Gradle 输出逐行实时到达、`BUILD SUCCESSFUL`、退出码 0；故意失败的构建回传
  `exitCode: 1`，退出码原样传给客户端。
- 服务在 Windows、项目在 `E:\` 时，Windows 的 `/` 是「当前盘相对路径」，
  所以容器路径一律用 POSIX 语义处理（`path_mapper`），不要用 `os.path.normpath`。
- Windows 的管道不支持 `os.set_blocking`，输出读取用读线程 + 队列（`StreamPump`），
  跨平台一致。
- 取消任务时 `killpg` 必须作用于子进程自己的进程组（POSIX 用
  `start_new_session=True`、Windows 用 `CREATE_NEW_PROCESS_GROUP` + `taskkill /T`），
  否则会把服务自己一起杀掉。
- Windows 的 embed 版 Python 的 `._pth` 不会把脚本目录放进 `sys.path`，
  所以入口脚本自己 `sys.path.insert(0, 脚本目录)`。

## 自测

用假项目（不需要真 Gradle）跑一遍路径、鉴权、流式、退出码、409、取消：

```bash
python3 tests/test_path_mapper.py   # 路径映射与穿越防护
python3 tests/test_service.py       # 端到端 HTTP 行为
```

## 目录结构

```
host-service/
├── host_service.py        入口：加载配置、启动服务
├── server.py              HTTP、路由、NDJSON 流式、错误映射、并发槽
├── config.py              配置读取、默认生成、校验
├── errors.py              错误类型 → HTTP 状态码
├── path_mapper.py         容器路径 → 宿主机路径（安全边界）
├── gradlew-host.py        容器侧客户端
├── capabilities/
│   ├── base.py            Capability / Context / JobRun / 进程与流式工具
│   ├── gradle.py          Gradle 能力：白名单、Wrapper、实时输出
│   └── probe.py           路径自举能力
├── tests/
├── config.json            本机配置（不入库，缺失时自动生成）
└── config.example.json    模板
```

## 明确不做（第一版）

任意 Shell / PowerShell / CMD API、自动修改白名单、IDEA 配置解析、复杂 Java 自动选择、
Web UI、数据库、消息队列、Docker API、任务队列、远程公网访问、多用户权限、
Windows Service 安装器。
