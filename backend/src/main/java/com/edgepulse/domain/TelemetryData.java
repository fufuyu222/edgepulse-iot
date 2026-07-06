package com.edgepulse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(indexes = {
        @Index(name = "idx_telemetry_device_time", columnList = "deviceId, reportedAt")
})
public class TelemetryData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceId;
    private Double temperature;
    private Double voltage;
    private Instant reportedAt;
    private Instant receivedAt;

    protected TelemetryData() {
    }

    public TelemetryData(String deviceId, Double temperature, Double voltage, Instant reportedAt, Instant receivedAt) {
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.voltage = voltage;
        this.reportedAt = reportedAt;
        this.receivedAt = receivedAt;
    }

    public Long getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getVoltage() {
        return voltage;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
