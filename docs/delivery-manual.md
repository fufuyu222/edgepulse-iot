# EdgePulse IoT 实施交付手册

本文档面向实施工程师、交付工程师和工业数字化项目现场联调场景，重点说明 EdgePulse IoT 的部署、检查、联调、数据核查和常见问题排查流程。

## 1. 系统说明

EdgePulse IoT 是一个工业设备监控与实时告警平台，包含设备数据接入、实时状态缓存、历史数据存储、规则告警和前端看板。

系统服务组成：

| 服务 | 容器名 | 作用 | 默认端口 |
| --- | --- | --- | --- |
| 前端看板 | `edgepulse-frontend` | 设备状态、遥测曲线、告警中心、规则管理 | `5173` |
| 后端服务 | `edgepulse-backend` | API、MQTT 消费、规则判断、SSE 推送 | `8080` |
| MySQL | `edgepulse-mysql` | 设备、遥测、规则、告警记录 | `3306` |
| Redis | `edgepulse-redis` | 设备实时状态缓存 | `6379` |
| MQTT Broker | `edgepulse-mqtt` | 接收设备/网关上报数据 | `1883` |
| 边缘模拟器 | `edgepulse-edge-simulator` | 模拟设备周期上报温度、电压 | 无对外端口 |

核心数据链路：

```text
设备/边缘网关
  -> MQTT Broker: iot/device/{deviceId}/telemetry
  -> Spring Boot 后端订阅消费
  -> MySQL 保存历史遥测与告警
  -> Redis 保存设备最新状态
  -> SSE 推送实时事件
  -> Vue 看板展示设备、曲线和告警
```

## 2. 部署前检查

本地或服务器需要具备：

- Docker
- Docker Compose
- Git
- 可访问的 5173、8080、1883、3306、6379 端口

Linux 服务器常用检查命令：

```bash
pwd
ls
df -h
free -h
ip addr
docker --version
docker compose version
```

需要关注：

- 磁盘是否足够，避免 MySQL 和 Docker 镜像写满磁盘。
- 内存是否足够，避免多个容器启动后被系统杀掉。
- 端口是否被占用，避免前端、后端、MQTT 启动失败。
- 云服务器安全组或防火墙是否放行前端端口。

## 3. 部署步骤

进入项目目录：

```bash
cd edgepulse-iot
```

启动全部服务：

```bash
docker compose up -d --build
```

查看容器状态：

```bash
docker compose ps
```

正常情况下应看到 MySQL、Redis、MQTT、后端、前端、边缘模拟器均处于运行状态。

访问前端：

```text
http://localhost:5173
```

如果部署在服务器上，将 `localhost` 替换为服务器 IP：

```text
http://服务器IP:5173
```

停止服务：

```bash
docker compose down
```

如需保留数据库数据，不要删除 Docker volume。

## 4. 服务状态检查

查看所有容器：

```bash
docker compose ps
```

查看后端日志：

```bash
docker logs -f edgepulse-backend
```

查看边缘模拟器日志：

```bash
docker logs -f edgepulse-edge-simulator
```

查看 MQTT 服务日志：

```bash
docker logs -f edgepulse-mqtt
```

查看端口监听：

```bash
ss -lntp
```

在 Windows 本地也可以使用：

```powershell
docker compose ps
docker logs -f edgepulse-backend
netstat -ano | findstr ":8080"
```

## 5. 接口联调

后端健康检查可从业务接口开始：

```bash
curl http://localhost:8080/api/dashboard/summary
curl http://localhost:8080/api/devices
curl http://localhost:8080/api/telemetry/latest
curl http://localhost:8080/api/alarms
```

常用接口：

```text
GET  /api/dashboard/summary
GET  /api/devices
GET  /api/telemetry/latest
GET  /api/alarms
GET  /api/alarm-rules
GET  /api/events/stream
```

接口示例见：

```text
docs/api.http
```

## 6. 数据库核查

进入 MySQL 容器：

```bash
docker exec -it edgepulse-mysql mysql -uedgepulse -pedgepulse edgepulse
```

查看设备是否注册：

```sql
select id, name, type, status, last_seen_at
from device;
```

查看最新遥测数据：

```sql
select device_id, temperature, voltage, reported_at, received_at
from telemetry_data
order by reported_at desc
limit 20;
```

查看告警规则：

```sql
select id, device_id, metric_name, operator, threshold, level, enabled, suppress_minutes
from alarm_rule;
```

查看最新告警：

```sql
select id, device_id, metric_name, metric_value, threshold, level, status, created_at, updated_at
from alarm
order by created_at desc
limit 20;
```

实施排查时重点判断：

- `device` 是否出现 `dev001`、`dev002`、`dev003`。
- `telemetry_data` 是否持续新增数据。
- `alarm_rule` 是否启用，阈值和操作符是否符合预期。
- `alarm` 是否在数据越过阈值后生成记录。

## 7. Redis 核查

进入 Redis 容器：

```bash
docker exec -it edgepulse-redis redis-cli
```

查看 key：

```text
keys *
```

如需查看某个实时状态 key，可根据实际 key 名使用：

```text
get key_name
```

Redis 主要用于判断设备最新状态是否被后端写入缓存。现场排查时，Redis 有值但前端无数据，通常继续检查后端接口和前端请求；Redis 无值则继续检查 MQTT 消费和后端处理日志。

## 8. MQTT 接入说明

当前 MQTT Topic：

```text
iot/device/{deviceId}/telemetry
```

示例：

```text
iot/device/dev001/telemetry
```

平台侧订阅：

```text
iot/device/+/telemetry
```

`+` 表示单层通配，可以接收不同设备 ID 的上报数据。

典型遥测数据包含：

```json
{
  "temperature": 82.5,
  "voltage": 176.2,
  "timestamp": "2026-07-10T10:00:00Z"
}
```

真实设备接入时，常见做法是由边缘网关适配 Modbus、HTTP、TCP、OPC UA 或厂商接口，再统一转换为平台 MQTT 遥测格式。

## 9. 常见故障排查

### 前端页面打不开

排查顺序：

1. 查看前端容器是否运行：`docker compose ps`
2. 查看端口是否监听：`ss -lntp`
3. 本机访问：`http://localhost:5173`
4. 服务器部署时检查安全组和防火墙。
5. 查看前端日志：`docker logs -f edgepulse-frontend`

### 页面打开但没有数据

排查顺序：

1. 调后端接口：`curl http://localhost:8080/api/devices`
2. 查看后端日志：`docker logs -f edgepulse-backend`
3. 查看模拟器日志：`docker logs -f edgepulse-edge-simulator`
4. 查询 `telemetry_data` 是否有数据。
5. 检查前端请求地址是否指向正确后端。

### 后端启动失败

排查顺序：

1. 查看后端日志。
2. 检查 MySQL 是否健康。
3. 检查 Redis 是否启动。
4. 检查 MQTT 是否启动。
5. 检查配置文件中的连接地址是否使用容器服务名。

### 设备没有上报数据

排查顺序：

1. 查看模拟器容器是否运行。
2. 查看模拟器日志是否有发布消息。
3. 查看 MQTT 容器是否正常。
4. 查看后端日志是否有订阅或消费记录。
5. 查询 `telemetry_data` 是否新增。

### 告警没有触发

排查顺序：

1. 查询 `alarm_rule` 是否存在且 `enabled = true`。
2. 确认 `metric_name` 是否为 `temperature` 或 `voltage`。
3. 确认操作符和阈值是否正确。
4. 查询最新遥测值是否真的超过阈值。
5. 检查告警抑制时间 `suppress_minutes` 是否导致短时间内不重复生成。
6. 查看后端日志。

### 容器反复重启

排查顺序：

1. `docker compose ps` 查看哪个容器异常。
2. `docker logs 容器名` 查看退出原因。
3. 检查端口冲突。
4. 检查环境变量和配置文件。
5. 检查磁盘和内存。

## 10. 交付验收清单

部署完成后建议逐项确认：

- 所有容器运行正常。
- 前端页面可以打开。
- 后端接口可以访问。
- 模拟设备持续上报数据。
- 设备列表能看到设备在线状态。
- 遥测曲线有温度、电压数据。
- 告警规则存在并启用。
- 触发阈值后能生成告警。
- 告警支持确认和恢复。
- MySQL 中可以查询设备、遥测、规则和告警数据。
- 后端日志无持续报错。
- 端口和访问地址已记录。

## 11. 实施交付表达

面试或项目汇报时，可以按以下顺序介绍：

1. 业务场景：工业设备温度、电压等运行数据需要实时监控。
2. 部署结构：前端、后端、MySQL、Redis、MQTT、边缘模拟器通过 Docker Compose 编排。
3. 数据链路：设备数据经 MQTT 上报，后端消费后入库、缓存、告警，并推送前端。
4. 联调方法：通过容器状态、日志、接口、SQL、端口逐层验证。
5. 排查思路：从页面、接口、服务、消息、数据库、规则配置逐步定位问题。
6. 真实扩展：接入真实设备时，可通过边缘网关适配 Modbus、OPC UA、HTTP 等协议，再统一上报平台。
