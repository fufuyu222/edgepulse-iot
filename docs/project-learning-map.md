# EdgePulse IoT 项目学习地图

这份文档的目标不是从零啃完物联网专业体系，而是围绕当前项目形成一套短期高收益的学习路径：

- 能看懂项目为什么这样设计
- 能按开发顺序补知识
- 能把功能讲成工程能力
- 能应对简历和面试追问

当前项目主线：

```text
Edge Simulator
  -> MQTT Broker
  -> Spring Boot Backend
  -> MySQL / Redis
  -> Rule Alert
  -> SSE
  -> Vue Dashboard
  -> Docker Compose
```

## 1. 还需要补充的知识点

你原来的清单已经覆盖了大部分核心内容，但为了更适合投简历，还建议补充这些高收益点。

### 1.1 接口设计与数据流

需要掌握：

- REST API 如何设计
- 前端如何调用后端接口
- DTO、Entity、Repository、Service、Controller 的职责划分
- 一条设备数据从进入系统到展示出来经历了哪些层

项目对应：

- `TelemetryController`
- `TelemetryService`
- `TelemetryData`
- `TelemetryRepository`
- `frontend/src/api.js`

面试讲法：

> 我把数据上报入口放在 TelemetryController，业务处理放在 TelemetryService，数据库访问通过 Repository 完成。这样接口层、业务层、持久层职责比较清楚。

### 1.2 数据库建模

需要掌握：

- 设备表怎么设计
- 遥测数据表怎么设计
- 告警规则表怎么设计
- 告警记录表怎么设计
- 为什么设备状态和历史数据要分开

项目对应：

- `Device`
- `TelemetryData`
- `AlarmRule`
- `Alarm`

面试重点：

> 设备信息是相对稳定的数据，遥测数据是持续增长的历史数据，实时状态适合放 Redis，历史数据适合放 MySQL 或时序数据库。

### 1.3 缓存与实时状态

需要掌握：

- Redis 在这个项目里不是替代数据库，而是存实时状态
- `device:status:{id}` 这种 key 如何表达业务含义
- Redis 挂了以后系统是否还能工作

项目对应：

- `DeviceStatusCache`

面试讲法：

> MySQL 是权威数据源，Redis 是加速层。设备最后在线状态会写数据库，同时同步到 Redis，方便实时查询。

### 1.4 异常处理与系统可用性

需要掌握：

- MQTT broker 不可用怎么办
- Redis 不可用怎么办
- 设备数据格式错误怎么办
- 重复告警怎么避免
- Docker 容器启动顺序怎么控制

项目对应：

- `MqttTelemetrySubscriber`
- `DeviceStatusCache`
- `ApiExceptionHandler`
- `docker-compose.yml`
- `AlarmService`

短期建议：

第一版不需要做到企业级容错，但必须能讲清楚当前做了什么、未来怎么补。

### 1.5 日志与排错

需要掌握：

- 怎么看后端日志
- 怎么看模拟器日志
- 怎么判断 MQTT 有没有收到数据
- 怎么判断告警有没有触发

常用命令：

```bash
docker compose ps
docker logs -f edgepulse-backend
docker logs -f edgepulse-edge-simulator
docker compose down
docker compose up -d
```

面试意义：

> 这说明你不是只会写代码，还知道服务跑起来以后怎么定位问题。

### 1.6 Git / GitHub 工程流程

需要掌握：

- 每完成一个功能做一次 commit
- README 怎么写
- GitHub 仓库如何展示项目
- 面试时如何打开仓库讲项目

当前已经做到：

- 创建 GitHub 仓库
- 提交初始项目
- 后续修复导航功能并推送

建议后续 commit 粒度：

```text
Add product and thing model modules
Add inactive device alarm
Improve README with architecture diagram
Add demo screenshots
```

## 2. 知识点与开发过程对应

### 阶段 1：项目结构搭建

开发目标：

- 搭建 Spring Boot 后端
- 搭建 Vue 前端
- 建立 Docker Compose
- 建立 GitHub 仓库

需要掌握：

- Spring Boot 项目目录结构
- Vue 项目目录结构
- 前后端分离的基本概念
- Git 提交和 GitHub 远程仓库

项目文件：

```text
backend/
frontend/
docker-compose.yml
README.md
```

你需要能回答：

- 为什么后端和前端分成两个目录？
- 为什么不是一个传统 Java Web 项目？
- Docker Compose 是干什么的？
- GitHub 仓库对简历有什么用？

推荐回答：

> 这个项目采用前后端分离结构，后端负责设备接入、业务处理和数据接口，前端负责可视化展示。Docker Compose 用来统一启动 MySQL、Redis、MQTT、后端、前端和模拟器，方便部署和演示。

### 阶段 2：设备模拟与 MQTT 接入

开发目标：

- 用 Python 模拟工业设备
- 通过 MQTT 上报数据
- 后端订阅 MQTT topic
- 接收并解析设备数据

需要掌握：

- MQTT 是什么
- Topic 是什么
- 发布 / 订阅模型
- 为什么 IoT 项目常用 MQTT
- JSON 数据格式

项目文件：

```text
edge-simulator/simulator.py
backend/src/main/java/com/edgepulse/mqtt/MqttTelemetrySubscriber.java
infra/mosquitto/mosquitto.conf
```

当前 Topic：

```text
iot/device/{deviceId}/telemetry
```

当前数据格式：

```json
{
  "deviceId": "dev001",
  "temperature": 86.5,
  "voltage": 220,
  "timestamp": "2026-07-06T11:00:00Z"
}
```

你需要能回答：

- 为什么不用 HTTP 让设备直接上报？
- MQTT 的发布订阅和普通接口调用有什么区别？
- topic 为什么这样设计？
- 后端怎么知道是哪台设备的数据？

推荐回答：

> MQTT 更适合设备侧低开销、持续上报和消息订阅场景。设备只负责往约定 topic 发布遥测数据，平台后端订阅 `iot/device/+/telemetry`，可以统一接收所有设备的数据。

### 阶段 3：设备管理

开发目标：

- 保存设备基础信息
- 维护设备在线状态
- 展示设备列表
- 记录最近上报时间

需要掌握：

- 设备实体怎么抽象
- 设备 ID 的意义
- 设备状态如何更新
- 在线状态和最后上报时间的关系

项目文件：

```text
Device.java
DeviceStatus.java
DeviceController.java
DeviceRepository.java
TelemetryService.java
```

当前实现：

- 设备第一次上报时自动注册
- 设备状态设为 `ONLINE`
- 更新 `lastSeenAt`
- 前端展示设备列表

你需要能回答：

- 设备为什么需要单独建表？
- 设备状态是怎么来的？
- 现在有没有离线判断？
- 如何扩展成正式设备管理？

当前不足：

- 还没有产品管理
- 还没有设备密钥
- 还没有设备主动离线判断
- 还没有设备编辑页面

后续扩展：

```text
Product
DeviceCredential
ThingModel
InactiveDeviceScanner
```

### 阶段 4：产品管理与物模型

当前项目状态：

第一版还没有正式实现产品管理和物模型，但这是非常值得补的第二阶段功能。

需要掌握：

- 产品是什么
- 设备是什么
- 产品和设备的关系
- 物模型是什么
- 属性、事件、命令怎么区分

推荐抽象：

```text
Product
  - id
  - name
  - protocol
  - description

Device
  - id
  - productId
  - name
  - status
  - lastSeenAt

ThingModelProperty
  - productId
  - identifier
  - name
  - dataType
  - unit

ThingModelEvent
  - productId
  - identifier
  - name
  - level

ThingModelCommand
  - productId
  - identifier
  - name
  - inputSchema
```

简化理解：

```text
产品 = 一类设备的模板
设备 = 某一台真实设备
物模型 = 这类设备有哪些属性、事件、命令
```

举例：

```text
产品：温控采集器
设备：dev001
属性：temperature、voltage
事件：high_temperature
命令：restart、set_threshold
```

面试讲法：

> 当前版本先固定了温度和电压两个遥测字段，后续可以抽象产品和物模型，把不同设备类型的属性定义放到数据库中，这样平台就不依赖固定字段。

### 阶段 5：历史数据与实时状态

开发目标：

- MySQL 存历史遥测数据
- Redis 存实时设备状态
- 前端展示最新数据和曲线

需要掌握：

- 历史数据和实时状态的区别
- 为什么遥测数据会越来越大
- MySQL 和时序数据库的差异
- Redis 的作用

项目文件：

```text
TelemetryData.java
TelemetryRepository.java
DeviceStatusCache.java
TelemetryController.java
```

当前实现：

- 每条遥测数据写入 MySQL
- 设备状态写入 Redis
- 前端展示最新 20 条数据和曲线

你需要能回答：

- 为什么不用 Redis 存所有历史数据？
- 为什么现在用 MySQL，不直接上 TDengine / InfluxDB？
- 查询曲线数据为什么需要按设备和时间查？

推荐回答：

> 当前版本为了降低复杂度使用 MySQL 存储历史遥测数据。真实工业场景数据量更大，可以替换为 TDengine、InfluxDB 等时序数据库。

### 阶段 6：阈值告警

开发目标：

- 配置告警规则
- 判断温度、电压是否异常
- 生成告警记录
- 支持确认和解决

需要掌握：

- 规则表达方式
- 阈值判断
- 告警生命周期
- 重复告警抑制

项目文件：

```text
AlarmRule.java
Alarm.java
AlarmService.java
AlarmController.java
AlarmRuleController.java
SeedDataService.java
```

当前规则：

```text
temperature > 80 -> CRITICAL
voltage < 180 -> WARN
```

当前生命周期：

```text
ACTIVE -> ACKED -> RESOLVED
```

你需要能回答：

- 告警规则为什么要做成表，而不是写死？
- 为什么要有 ACKED？
- 为什么要有 RESOLVED？
- 重复告警为什么要抑制？

推荐回答：

> ACTIVE 表示告警刚发生，ACKED 表示人工已确认，RESOLVED 表示问题已处理。重复告警抑制是为了避免设备持续异常时刷屏。

### 阶段 7：设备不活跃告警

当前项目状态：

还没有实现，这是下一步非常值得补的功能。

设计方式：

```text
定时任务每 30 秒扫描一次设备
如果 now - lastSeenAt > 60 秒
则设备状态改为 OFFLINE
并生成 inactive alarm
```

需要掌握：

- 定时任务
- 设备心跳
- lastSeenAt 判断
- 离线状态和离线告警

推荐实现：

```text
InactiveDeviceScanner
DeviceOfflineAlarmService
```

面试讲法：

> 除了设备上报异常值，我还设计了不活跃检测。如果设备超过阈值时间未上报，系统会把它标记为离线并生成告警。

### 阶段 8：SSE 实时推送与 Dashboard

开发目标：

- 后端通过 SSE 推送事件
- 前端收到事件后刷新看板
- 展示设备、曲线、告警

需要掌握：

- SSE 是什么
- SSE 和 WebSocket 的区别
- 为什么监控看板适合 SSE
- Dashboard 为什么是 IoT 平台核心

项目文件：

```text
EventStreamService.java
EventController.java
frontend/src/App.vue
```

推荐回答：

> 这个项目是监控型场景，数据主要从后端推到前端，前端不需要高频双向通信，所以用 SSE 比 WebSocket 更简单。

Dashboard 的价值：

- 设备是否在线
- 当前是否异常
- 异常趋势如何
- 告警是否处理
- 系统是否真的在运行

面试讲法：

> IoT 平台的核心不是简单管理设备，而是把设备数据转成可观察、可判断、可处理的业务状态。Dashboard 就是把数据流变成运维视角。

### 阶段 9：Rule Engine / Rule Chain 思想

当前项目状态：

当前版本是极简规则引擎，不是完整 Rule Chain。

需要掌握：

- Rule Engine 是什么
- Rule Chain 是什么
- 当前项目和 ThingsBoard 的区别
- 如何从简单规则演进到规则链

当前版本：

```text
TelemetryMessage
  -> AlarmService.evaluate()
  -> match rules
  -> generate alarm
```

规则链思想：

```text
数据输入
  -> 过滤节点
  -> 条件判断节点
  -> 告警节点
  -> 通知节点
  -> 数据转发节点
```

短期面试讲法：

> 当前版本实现了基于数据库配置的轻量级规则判断，主要支持阈值告警。它不是完整可视化规则链，但设计上可以继续扩展成规则节点和规则链。

### 阶段 10：多协议接入抽象

当前项目状态：

第一版只实现 MQTT。

需要掌握：

- 为什么工业 IoT 会有多协议
- MQTT、HTTP、Modbus、OPC UA 的差异
- 多协议接入为什么要抽象

推荐抽象：

```text
DeviceProtocolAdapter
  - connect()
  - subscribe()
  - decode()
  - publishCommand()

MqttProtocolAdapter
HttpProtocolAdapter
ModbusProtocolAdapter
OpcUaProtocolAdapter
```

核心思想：

```text
不同协议接入
  -> 统一转换成 TelemetryMessage
  -> 进入同一套业务处理流程
```

面试讲法：

> 当前项目先实现 MQTT 接入，但后端业务层只关心统一的 TelemetryMessage。后续如果接入 HTTP、Modbus 或 OPC UA，只需要新增协议适配器，把不同协议的数据转换为统一模型。

### 阶段 11：Docker / Linux 部署

开发目标：

- 用 Docker Compose 启动整套系统
- 在 Docker Desktop 本地运行
- 后续可以迁移到 Linux 服务器

需要掌握：

- 容器是什么
- 镜像是什么
- Docker Compose 是什么
- 后端、前端、MySQL、Redis、MQTT 为什么拆成多个服务

项目文件：

```text
docker-compose.yml
backend/Dockerfile
frontend/Dockerfile
edge-simulator/Dockerfile
infra/mosquitto/mosquitto.conf
```

当前服务：

```text
mysql
redis
mqtt
backend
frontend
edge-simulator
```

常用命令：

```bash
docker compose up -d --build
docker compose ps
docker logs -f edgepulse-backend
docker logs -f edgepulse-edge-simulator
docker compose down
```

面试讲法：

> 我用 Docker Compose 把后端、前端、数据库、缓存、MQTT Broker 和边缘模拟器编排到一起，可以一条命令启动完整环境，方便本地演示和 Linux 部署。

### 阶段 12：README 和演示图

开发目标：

- GitHub 首页能看懂项目
- 面试官能快速看到价值
- 简历项目能和仓库对应

README 需要包含：

- 项目简介
- 架构图
- 技术栈
- 功能模块
- 快速启动
- 接口示例
- 页面截图
- 项目亮点
- 后续计划

建议架构图：

```text
Edge Simulator
  -> Mosquitto MQTT
  -> Spring Boot Backend
  -> MySQL / Redis
  -> SSE
  -> Vue Dashboard
```

需要补的演示图：

```text
docs/images/dashboard.png
docs/images/alarm-center.png
docs/images/rule-view.png
```

短期建议：

先用浏览器截图，不追求设计稿。重点是 README 里能看到项目已经跑起来。

## 3. 使用与验证清单

### 3.1 验证 Docker 服务

命令：

```bash
docker compose ps
```

应该看到：

```text
edgepulse-mysql
edgepulse-redis
edgepulse-mqtt
edgepulse-backend
edgepulse-frontend
edgepulse-edge-simulator
```

### 3.2 验证设备在线

接口：

```text
GET http://localhost:8080/api/devices
```

预期：

```text
dev001 ONLINE
dev002 ONLINE
dev003 ONLINE
```

### 3.3 验证遥测数据

接口：

```text
GET http://localhost:8080/api/telemetry/latest
```

预期：

```text
temperature
voltage
reportedAt
```

### 3.4 验证告警

接口：

```text
GET http://localhost:8080/api/alarms
```

预期：

```text
temperature GT 80
voltage LT 180
```

### 3.5 验证规则

接口：

```text
GET http://localhost:8080/api/alarm-rules
```

预期：

```text
temperature GT 80 CRITICAL
voltage LT 180 WARN
```

### 3.6 验证前端

浏览器打开：

```text
http://localhost:5173
```

检查：

- 总览指标有数据
- 设备列表有 3 台设备
- 曲线持续变化
- 告警中心有告警记录
- 左侧导航可以跳转
- 规则板块能看到阈值规则

## 4. 高频问题与回答

### Q1：这个项目和普通后台管理系统有什么区别？

普通后台管理系统主要是 CRUD。这个项目重点是设备数据流：

```text
设备上报 -> MQTT 接入 -> 数据存储 -> 规则判断 -> 告警 -> 实时看板
```

它更接近 IoT 平台的核心链路。

### Q2：为什么使用 MQTT？

MQTT 是发布订阅模型，适合设备低开销、持续上报、平台统一订阅的场景。设备不需要直接调用复杂接口，只要往 topic 发消息。

### Q3：为什么要用 Redis？

Redis 用来保存实时状态，例如设备是否在线、最近上报时间。MySQL 保存历史数据，Redis 保存当前状态，两者职责不同。

### Q4：为什么用 SSE，不用 WebSocket？

这个项目主要是后端把设备状态和告警推送给前端，属于单向实时推送。SSE 比 WebSocket 更简单，适合监控看板。

### Q5：为什么现在不用时序数据库？

本科就业项目优先保证完整闭环。MySQL 足够支撑演示和学习。后续可以把遥测表迁移到 TDengine 或 InfluxDB。

### Q6：规则引擎是不是太简单？

当前版本是轻量级规则判断，支持数据库配置的阈值规则。后续可以扩展成 Rule Engine，把规则拆成过滤、判断、告警、通知、转发等节点。

### Q7：设备不在线怎么判断？

当前版本还没有完整离线扫描。设计上可以通过定时任务检查 `lastSeenAt`，超过阈值未上报就标记为 `OFFLINE` 并生成不活跃告警。

### Q8：多协议接入怎么扩展？

新增协议适配器，把 MQTT、HTTP、Modbus、OPC UA 等不同协议的数据统一转换成 `TelemetryMessage`，后面的业务处理流程保持不变。

### Q9：Docker 在项目里起什么作用？

Docker Compose 把 MySQL、Redis、MQTT、后端、前端、模拟器一起启动，解决环境依赖问题，方便演示和部署。

### Q10：这个项目可以写进简历吗？

可以。建议写法：

```text
基于 Spring Boot + MQTT 的边缘工业设备监控与实时告警平台
```

项目描述：

```text
实现设备数据采集、实时状态管理、历史数据存储、规则告警、SSE 推送和 Vue 可视化看板，并通过 Docker Compose 完成一键部署。
```

## 5. 后续最值得补的功能

按短期收益排序：

1. 设备不活跃告警
2. 产品管理
3. 物模型管理
4. README 架构图和演示截图
5. 告警规则新增/编辑页面
6. 设备详情页
7. 登录和权限
8. 多协议适配器接口
9. 通知模块，例如邮件或 Webhook
10. 时序数据库替换方案说明

最推荐下一步：

```text
先做设备不活跃告警 + README 演示图
```

原因：

- 设备不活跃告警能补齐 IoT 监控的重要场景
- README 演示图能立刻提升 GitHub 仓库观感
- 这两项对投简历收益很高

## 6. 学习顺序建议

不要从专业书开始，按项目倒推：

1. 先会启动项目
2. 看懂 Docker 里有哪些服务
3. 看懂模拟器发了什么数据
4. 看懂 MQTT topic
5. 看懂后端如何接收数据
6. 看懂数据如何入库
7. 看懂规则如何触发告警
8. 看懂前端如何展示
9. 再补产品、物模型、多协议这些抽象
10. 最后准备简历和问答

这条路线最适合当前目标：短期形成项目能力，而不是长期理论学习。
