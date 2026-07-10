# EdgePulse IoT

EdgePulse IoT is an industrial device monitoring and real-time alerting platform built with Spring Boot, Vue 3, MQTT, MySQL, Redis, and Docker Compose.

The project simulates an edge-side device gateway, ingests telemetry through MQTT, maintains real-time device status, stores historical telemetry, evaluates alarm rules, and pushes live events to a web dashboard.

## Architecture

```text
Edge Simulator
  -> MQTT Broker (Mosquitto)
  -> Spring Boot Backend
     -> MySQL: devices, telemetry, alarm rules, alarm records
     -> Redis: latest device status and real-time cache
     -> SSE: live event stream
  -> Vue Dashboard
```

## Core Features

- MQTT telemetry ingestion with wildcard topic subscription: `iot/device/+/telemetry`.
- Automatic device registration and latest-report-time maintenance.
- Historical telemetry persistence for temperature and voltage metrics.
- Redis-based real-time device status cache.
- Configurable alarm rules for telemetry thresholds.
- Alarm lifecycle management: `ACTIVE -> ACKED -> RESOLVED`.
- Server-Sent Events for real-time dashboard updates.
- Vue 3 dashboard with overview, device list, telemetry trend, alarm center, and rule management.
- Docker Compose deployment for MySQL, Redis, Mosquitto, backend, frontend, and edge simulator.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 17, Spring Boot 3, Spring Data JPA |
| Messaging | MQTT, Eclipse Mosquitto, Eclipse Paho |
| Storage | MySQL 8.4, Redis 7.4 |
| Frontend | Vue 3, Element Plus, ECharts, Vite |
| Edge Simulation | Python MQTT client |
| Deployment | Docker, Docker Compose |

## Repository Structure

```text
backend/          Spring Boot backend service
frontend/         Vue 3 monitoring dashboard
edge-simulator/   Python edge device simulator
infra/            Mosquitto and infrastructure configuration
docs/             API request examples
scripts/          Local development helper scripts
```

## Data Flow

1. The edge simulator periodically publishes telemetry for `dev001`, `dev002`, and `dev003`.
2. Mosquitto receives telemetry messages on `iot/device/{deviceId}/telemetry`.
3. The backend MQTT subscriber consumes messages and normalizes telemetry payloads.
4. Device records are created or updated automatically.
5. Telemetry data is written to MySQL for historical query.
6. The latest device state is cached in Redis for fast dashboard access.
7. Alarm rules are evaluated against incoming telemetry.
8. Alarm records are created, acknowledged, or resolved through the backend API.
9. SSE pushes device and alarm events to the frontend in real time.

## Quick Start

Requirements:

- Docker Desktop
- Docker Compose

Start the full stack:

```bash
docker compose up -d --build
```

Open the dashboard:

```text
http://localhost:5173
```

Check running containers:

```bash
docker compose ps
```

Stop the stack:

```bash
docker compose down
```

## Useful Endpoints

```text
GET  /api/dashboard/summary
GET  /api/devices
GET  /api/telemetry/latest
GET  /api/alarms
GET  /api/alarm-rules
POST /api/telemetry
GET  /api/events/stream
```

## Documentation

```text
docs/api.http              API request examples
docs/delivery-manual.md    Deployment and delivery manual
```

## Alarm Model

EdgePulse currently supports threshold-based alarm rules for telemetry fields such as:

- `temperature > 80`
- `voltage < 180`

Alarm records use a simple lifecycle model:

```text
ACTIVE -> ACKED -> RESOLVED
```

This keeps abnormal telemetry, operator acknowledgement, and final resolution as separate states instead of treating alarms as one-time log records.

## Engineering Highlights

- Uses MQTT topic wildcards to support multiple devices without hard-coding device IDs in the subscriber.
- Separates historical telemetry storage from real-time state caching through MySQL and Redis.
- Provides a complete closed loop from edge data generation to backend processing, alarm creation, and frontend visualization.
- Uses SSE for server-to-browser event delivery, reducing the need for frequent polling on the dashboard.
- Keeps local deployment reproducible through Docker Compose, including infrastructure services and the edge simulator.
- Organizes backend code by controller, service, repository, domain, DTO, MQTT, and configuration layers.

## Local Development

Run backend locally:

```bash
cd backend
mvn spring-boot:run
```

Run frontend locally:

```bash
cd frontend
npm install
npm run dev
```

On this Windows workspace, helper scripts are also available:

```powershell
.\scripts\dev-backend.ps1
.\scripts\dev-frontend.ps1
```

## Roadmap

- Product and device model management.
- More flexible rule conditions and notification actions.
- HTTP telemetry ingestion as a second protocol entry.
- Device offline detection based on heartbeat timeout.
- Dashboard screenshots and deployment notes.
