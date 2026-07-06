# EdgePulse IoT

EdgePulse IoT is a lightweight industrial device monitoring and real-time alerting platform.

The project focuses on a complete IoT data flow:

1. Edge device simulator publishes telemetry data through MQTT.
2. Spring Boot backend receives and processes device data.
3. MySQL stores historical telemetry records.
4. Redis stores real-time device status.
5. Rule-based alert module detects abnormal conditions.
6. Vue dashboard displays device status, charts, and alerts.
7. Docker Compose provides one-command local deployment.

## Planned Tech Stack

- Backend: Java 17, Spring Boot 3
- Messaging: MQTT, Mosquitto, Eclipse Paho
- Storage: MySQL, Redis
- Frontend: Vue 3, Element Plus, ECharts
- Deployment: Docker, Docker Compose

## Development Status

The first implementation focuses on the smallest complete engineering loop:

- MQTT telemetry ingestion
- Device status management
- MySQL historical telemetry storage
- Redis real-time status cache
- Rule-based alarm generation and suppression
- SSE real-time push
- Vue dashboard
- Edge simulator
- Docker Compose deployment

## Repository Structure

```text
backend/          Spring Boot backend
frontend/         Vue dashboard
edge-simulator/   Python MQTT device simulator
infra/            Mosquitto and deployment configuration
docs/             API examples and project notes
```

## Quick Start With Docker

```bash
docker compose up -d --build
```

Then open:

```text
http://localhost:5173
```

Useful backend endpoints:

```text
GET  http://localhost:8080/api/dashboard/summary
GET  http://localhost:8080/api/devices
GET  http://localhost:8080/api/telemetry/latest
GET  http://localhost:8080/api/alarms
POST http://localhost:8080/api/telemetry
```

## Local Development Notes

The backend can run with the default H2 in-memory database for quick development. Docker mode uses MySQL, Redis, and Mosquitto.

```bash
cd backend
mvn spring-boot:run
```

```bash
cd frontend
npm install
npm run dev
```

On this Windows workspace, portable JDK/Maven can be used through:

```powershell
.\scripts\dev-backend.ps1
.\scripts\dev-frontend.ps1
```
