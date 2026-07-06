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

This repository is being prepared as an employment-oriented engineering project for learning and portfolio presentation.
