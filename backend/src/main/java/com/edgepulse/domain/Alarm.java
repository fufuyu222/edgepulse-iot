package com.edgepulse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class Alarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceId;
    private String metricName;
    private Double metricValue;
    private Double threshold;
    @Enumerated(EnumType.STRING)
    private AlarmLevel level;
    @Enumerated(EnumType.STRING)
    private AlarmStatus status = AlarmStatus.ACTIVE;
    private String message;
    private Instant createdAt;
    private Instant updatedAt;

    protected Alarm() {
    }

    public Alarm(String deviceId, String metricName, Double metricValue, Double threshold, AlarmLevel level, String message) {
        this.deviceId = deviceId;
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.threshold = threshold;
        this.level = level;
        this.message = message;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getMetricName() {
        return metricName;
    }

    public Double getMetricValue() {
        return metricValue;
    }

    public Double getThreshold() {
        return threshold;
    }

    public AlarmLevel getLevel() {
        return level;
    }

    public AlarmStatus getStatus() {
        return status;
    }

    public void setStatus(AlarmStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
