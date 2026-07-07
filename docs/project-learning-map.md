# EdgePulse IoT 从零搭建复盘说明

这份文档不是知识清单，也不是项目使用说明书。

它的目标是帮你在短期内把这个项目变成“你自己的项目”：

- 你知道为什么要做这个项目
- 你知道每一步先做什么、后做什么
- 你知道每个模块解决了什么问题
- 你知道怎么验证它确实跑通了
- 你能在面试里用自己的话讲出来

你不需要先把物联网、Spring Boot、Vue、Docker 全部学完。现在的策略是：

```text
先抓项目主线
再顺着主线补关键知识
最后准备简历和问答
```

## 0. 这个项目到底是什么

这个项目叫：

```text
EdgePulse IoT
```

你可以把它理解成一个简化版工业物联网监控平台。

它做的事很直白：

```text
模拟工业设备不断产生温度、电压数据
设备通过 MQTT 把数据发给平台
后端接收数据
后端保存历史数据和实时状态
后端根据规则判断是否异常
异常时生成告警
前端看板展示设备、曲线、告警和规则
Docker 一键启动整套系统
```

一句话版本：

> 我做了一个基于 Spring Boot + Vue + MQTT 的边缘工业设备监控与实时告警平台，用来模拟设备数据采集、实时状态展示、规则告警和 Docker 部署。

## 1. 为什么要做这个项目

如果只做一个普通后台管理系统，面试时很容易变成：

```text
我做了增删改查
我用了 Spring Boot
我用了 Vue
我连了数据库
```

这个说法太普通。

IoT 项目更有价值的地方在于它不是普通 CRUD，而是有一条数据流：

```text
设备数据上报 -> 平台接入 -> 存储 -> 规则判断 -> 告警 -> 看板展示
```

这条链路能说明你理解真实工程系统，不只是会写页面和接口。

所以这个项目一开始就不是为了做大而全，而是为了做一个完整闭环。

## 2. 第一件事：确定项目边界

一开始不能贪大。

完整 IoT 平台可能包含：

```text
设备管理
产品管理
物模型
多协议接入
规则链
告警
通知
权限
数据转发
大屏
移动端
边缘计算
时序数据库
```

这些全做会崩。

所以第一版只做最核心的闭环：

```text
模拟设备
MQTT 接入
数据入库
实时状态
阈值告警
SSE 推送
Vue 看板
Docker 部署
```

这个边界对本科就业项目比较合适：

- 能做完
- 能演示
- 能讲清
- 能投 Java 后端 / 全栈 / 物联网方向

面试可以这样说：

> 我没有一开始就做完整 IoT 平台，而是先收敛成设备数据采集、规则告警和可视化看板这条核心链路，保证项目能完整跑通。

## 3. 第二件事：搭项目结构

项目采用前后端分离结构：

```text
edgepulse-iot/
  backend/          Spring Boot 后端
  frontend/         Vue 前端
  edge-simulator/   模拟设备
  infra/            MQTT 配置
  docs/             文档和接口示例
  docker-compose.yml
```

为什么这么分？

因为每一块职责不同：

```text
backend       负责业务逻辑和接口
frontend      负责页面展示
edge-simulator 模拟真实设备
infra         放基础设施配置
docker-compose.yml 统一启动全部服务
```

面试可以这样说：

> 我把项目拆成后端、前端、边缘模拟器和基础设施配置几个部分，这样结构比较清晰，也方便 Docker Compose 编排。

## 4. 第三件事：先做模拟设备

真实工业设备你现在没有，所以第一步不是写复杂后端，而是先造一个设备数据源。

项目里用 Python 写了一个模拟器：

```text
edge-simulator/simulator.py
```

它做三件事：

```text
模拟 dev001、dev002、dev003 三台设备
每 5 秒生成一次温度和电压
把数据发布到 MQTT
```

数据长这样：

```json
{
  "deviceId": "dev001",
  "temperature": 86.5,
  "voltage": 220,
  "timestamp": "2026-07-06T11:00:00Z"
}
```

为什么要先做模拟器？

因为没有设备数据，后面的后端、告警、看板都没法验证。

面试可以这样说：

> 因为没有真实硬件设备，所以我先做了一个边缘设备模拟器，用来模拟工业设备周期性上报温度和电压数据。

## 5. 第四件事：引入 MQTT

设备数据不是直接调 HTTP 接口，而是通过 MQTT。

项目里用了 Mosquitto 作为 MQTT Broker。

你可以把 MQTT Broker 理解成一个消息中转站：

```text
设备把数据发给 Broker
后端订阅 Broker 的 topic
Broker 把消息转给后端
```

当前 topic 设计是：

```text
iot/device/{deviceId}/telemetry
```

例如：

```text
iot/device/dev001/telemetry
iot/device/dev002/telemetry
```

后端订阅：

```text
iot/device/+/telemetry
```

这里的 `+` 表示匹配任意一个设备编号。

为什么用 MQTT？

因为 IoT 设备通常是持续上报数据，MQTT 的发布订阅模型更适合这种场景。

面试可以这样说：

> 设备侧使用 MQTT 发布遥测数据，后端统一订阅 `iot/device/+/telemetry`。这样设备只需要按 topic 上报，平台可以统一接入多台设备的数据。

## 6. 第五件事：做 Spring Boot 后端接入

后端的入口是：

```text
MqttTelemetrySubscriber
```

它负责：

```text
连接 MQTT Broker
订阅设备数据 topic
收到消息后解析 JSON
转换成 TelemetryMessage
交给 TelemetryService 处理
```

也就是说，MQTT 模块只负责接消息，不负责业务。

业务处理放在：

```text
TelemetryService
```

它负责：

```text
保存设备信息
更新设备在线状态
保存遥测数据
触发告警判断
推送实时事件
```

这里有一个重要思想：

```text
接入层和业务层分开
```

以后如果不用 MQTT，改成 HTTP、Modbus、OPC UA，也可以先转换成同一个 `TelemetryMessage`，后面的业务流程不变。

面试可以这样说：

> 我把 MQTT 接入和业务处理分开。MQTT 模块只负责订阅和解析消息，业务层统一处理 TelemetryMessage，这样后续扩展其他协议会更容易。

## 7. 第六件事：设计数据库对象

第一版设计了四个核心对象：

```text
Device        设备
TelemetryData 历史遥测数据
AlarmRule     告警规则
Alarm         告警记录
```

### Device

表示一台设备。

保存：

```text
设备 ID
设备名称
设备类型
在线状态
最近上报时间
```

为什么需要设备表？

因为平台不能只看到一条条数据，还要知道这些数据属于哪台设备。

### TelemetryData

表示设备上报的一条历史数据。

保存：

```text
deviceId
temperature
voltage
reportedAt
receivedAt
```

为什么单独存历史数据？

因为曲线、趋势、追溯都需要历史数据。

### AlarmRule

表示告警规则。

当前有两条默认规则：

```text
temperature > 80
voltage < 180
```

为什么规则要放数据库？

因为规则不应该永远写死在代码里。

### Alarm

表示一次告警事件。

保存：

```text
哪台设备
哪个指标
当前值
阈值
告警等级
告警状态
创建时间
```

面试可以这样说：

> 数据库里我主要设计了设备、遥测数据、告警规则和告警记录四类表。设备是基础对象，遥测数据用于历史查询，规则用于判断异常，告警记录用于运维处理。

## 8. 第七件事：区分 MySQL 和 Redis

这个项目里 MySQL 和 Redis 职责不一样。

MySQL 保存：

```text
设备表
历史遥测数据
告警规则
告警记录
```

Redis 保存：

```text
设备实时状态
```

也就是：

```text
device:status:dev001
```

为什么不全放 MySQL？

可以全放 MySQL，但 Redis 更适合存实时状态和热点数据。

为什么不全放 Redis？

Redis 不适合长期保存大量历史遥测数据。

面试可以这样说：

> MySQL 作为权威数据源保存历史数据和规则配置，Redis 作为实时状态缓存，用来保存设备当前状态和最近上报时间。

## 9. 第八件事：做阈值告警

告警逻辑在：

```text
AlarmService
```

当前逻辑是：

```text
收到设备数据
查询启用的告警规则
取出对应指标值
判断是否超过阈值
如果触发，生成告警
```

例如：

```text
temperature = 89.81
规则是 temperature > 80
所以生成 CRITICAL 告警
```

告警状态有三个：

```text
ACTIVE    活动中
ACKED     已确认
RESOLVED  已解决
```

为什么需要这三个状态？

因为真实告警不是生成完就结束。

它一般经历：

```text
系统发现异常
人工确认看到
问题处理完成
```

项目还做了重复告警抑制：

```text
同设备同指标 5 分钟内不重复刷告警
```

面试可以这样说：

> 告警模块支持 ACTIVE、ACKED、RESOLVED 生命周期，并加入了重复告警抑制，避免设备持续异常时不断刷屏。

## 10. 第九件事：做实时推送

前端不能只靠手动刷新。

所以后端提供了 SSE：

```text
/api/events/stream
```

SSE 可以理解成：

```text
浏览器和后端保持一条连接
后端有新事件时主动推给前端
```

为什么用 SSE，不用 WebSocket？

因为这个项目主要是：

```text
后端 -> 前端
```

单向推送为主。

WebSocket 更适合双向通信，比如聊天、游戏、协同编辑。

面试可以这样说：

> 看板主要是接收后端的设备状态和告警事件，所以我用了 SSE 实现轻量级实时推送，而不是引入更复杂的 WebSocket。

## 11. 第十件事：做 Vue 看板

前端不是为了好看而已。

IoT 项目里 Dashboard 很重要，因为设备数据本身很难直接理解。

看板把数据变成：

```text
设备是否在线
是否有异常
指标趋势如何
告警是否处理
规则是否启用
```

当前页面包括：

```text
总览指标
设备列表
遥测曲线
告警中心
告警规则
```

左侧导航对应：

```text
总览
设备
告警
规则
```

前端调用后端接口的位置：

```text
frontend/src/api.js
```

主要页面：

```text
frontend/src/App.vue
```

面试可以这样说：

> 前端看板不是简单展示表格，而是把设备实时状态、历史曲线、告警处理和规则配置集中展示，形成一个可观察的监控台。

## 12. 第十一件事：用 Docker Compose 包装

如果不用 Docker，你需要分别安装：

```text
MySQL
Redis
Mosquitto
Java
Node
Python
```

这对演示和部署很麻烦。

所以项目用 Docker Compose 一次性启动：

```text
mysql
redis
mqtt
backend
frontend
edge-simulator
```

项目启动命令：

```bash
docker compose up -d
```

项目停止命令：

```bash
docker compose down
```

查看容器：

```bash
docker compose ps
```

查看后端日志：

```bash
docker logs -f edgepulse-backend
```

面试可以这样说：

> 我用 Docker Compose 把数据库、缓存、MQTT Broker、后端、前端和设备模拟器统一编排，做到一条命令启动完整项目环境。

## 13. 第十二件事：怎么验证项目

项目不是“页面能打开”就算完成。

你要按链路验证。

### 13.1 看容器是否都启动

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

### 13.2 看模拟器是否在发数据

```bash
docker logs -f edgepulse-edge-simulator
```

应该看到：

```text
iot/device/dev001/telemetry
iot/device/dev002/telemetry
iot/device/dev003/telemetry
```

### 13.3 看后端是否订阅 MQTT

```bash
docker logs -f edgepulse-backend
```

应该看到类似：

```text
Subscribed to MQTT topic iot/device/+/telemetry
```

### 13.4 看接口是否有设备

浏览器打开：

```text
http://localhost:8080/api/devices
```

应该看到：

```text
dev001
dev002
dev003
ONLINE
```

### 13.5 看接口是否有告警

浏览器打开：

```text
http://localhost:8080/api/alarms
```

应该看到：

```text
temperature GT 80
voltage LT 180
```

### 13.6 看前端看板

浏览器打开：

```text
http://localhost:5173
```

检查：

```text
设备总数是否为 3
在线设备是否为 3
曲线是否变化
告警中心是否有记录
规则区域是否有两条规则
```

如果这些都成立，说明整条链路跑通：

```text
模拟设备 -> MQTT -> 后端 -> 数据库 -> 告警 -> 前端
```

## 14. 你应该怎么把它讲成自己的项目

不要一上来背技术栈。

先讲问题，再讲方案。

推荐顺序：

```text
我想做一个轻量级 IoT 监控平台
先用模拟器模拟工业设备数据
用 MQTT 做设备接入
后端用 Spring Boot 处理数据
MySQL 存历史数据
Redis 存实时状态
规则模块负责告警
SSE 把实时事件推给前端
Vue 看板展示设备、曲线和告警
最后用 Docker Compose 一键部署
```

可以这样说：

> 这个项目的核心不是普通后台 CRUD，而是一条完整的设备数据链路。设备模拟器通过 MQTT 上报温度和电压，后端订阅消息后保存历史数据、更新实时状态，并根据数据库中的阈值规则生成告警。前端通过接口和 SSE 展示设备状态、遥测曲线和告警处理流程。最后我用 Docker Compose 把 MySQL、Redis、MQTT、后端、前端和模拟器统一部署。

## 15. 面试官可能会问什么

### Q1：你为什么用 MQTT？

回答：

> 因为设备是持续上报数据，MQTT 的发布订阅模型更适合设备接入。设备只需要往 topic 发数据，后端统一订阅即可。

### Q2：为什么要有模拟器？

回答：

> 没有真实工业设备，所以我写了一个边缘设备模拟器，模拟多台设备周期性上报温度和电压，用来验证整条 IoT 数据链路。

### Q3：Redis 在这里干什么？

回答：

> Redis 用来保存设备实时状态，比如在线状态和最后上报时间。MySQL 保存历史数据和规则配置。

### Q4：告警怎么触发？

回答：

> 后端收到遥测数据后，会查询启用的告警规则，比如 temperature > 80 或 voltage < 180。满足条件就生成 ACTIVE 告警。

### Q5：为什么有 ACKED 和 RESOLVED？

回答：

> ACTIVE 表示系统发现异常，ACKED 表示人工已确认，RESOLVED 表示问题处理完成。这更接近真实运维流程。

### Q6：SSE 和 WebSocket 为什么选 SSE？

回答：

> 这个项目主要是后端把状态和告警推给前端，是单向推送场景。SSE 实现更简单，足够满足监控看板。

### Q7：怎么扩展多协议？

回答：

> 可以抽象协议适配器。不同协议接入后，都转换成统一的 TelemetryMessage，后面的存储、告警和推送流程保持不变。

### Q8：现在有什么不足？

回答：

> 当前版本重点做核心链路，还没有完整的产品管理、物模型管理、设备不活跃告警和权限系统。后续我会优先补设备离线检测和物模型抽象。

## 16. 下一步最该补什么

短期最值得补三件事：

```text
1. 设备不活跃告警
2. 产品管理 + 物模型管理
3. README 架构图和演示截图
```

为什么？

因为这三件事最能提升面试说服力。

### 设备不活跃告警

让项目从“数值异常”扩展到“设备离线异常”。

### 产品管理 + 物模型

让项目更像真正的 IoT 平台。

### README 架构图和演示截图

让 GitHub 仓库更适合投简历。

## 17. 你现在的学习方式

不要这样学：

```text
先系统学 Spring Boot
再系统学 Vue
再系统学 MQTT
再系统学 Docker
最后再看项目
```

太慢。

应该这样学：

```text
先跑起来
再按数据流看代码
再按模块补知识
再准备面试表达
```

具体顺序：

```text
1. 会启动和停止项目
2. 会看 Docker 容器
3. 会看模拟器日志
4. 会看后端日志
5. 会打开前端看板
6. 会解释 MQTT topic
7. 会解释 TelemetryService
8. 会解释 AlarmService
9. 会解释 MySQL 和 Redis 分工
10. 会完整讲一遍项目
```

掌握到这个程度，就足够先进入简历准备阶段。
