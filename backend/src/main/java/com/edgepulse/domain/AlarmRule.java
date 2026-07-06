package com.edgepulse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AlarmRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceId;
    private String metricName;
    @Enumerated(EnumType.STRING)
    private RuleOperator operator;
    private Double threshold;
    @Enumerated(EnumType.STRING)
    private AlarmLevel level;
    private Boolean enabled = true;
    private Integer suppressMinutes = 5;

    protected AlarmRule() {
    }

    public AlarmRule(String deviceId, String metricName, RuleOperator operator, Double threshold, AlarmLevel level) {
        this.deviceId = deviceId;
        this.metricName = metricName;
        this.operator = operator;
        this.threshold = threshold;
        this.level = level;
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

    public RuleOperator getOperator() {
        return operator;
    }

    public Double getThreshold() {
        return threshold;
    }

    public AlarmLevel getLevel() {
        return level;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Integer getSuppressMinutes() {
        return suppressMinutes;
    }
}
