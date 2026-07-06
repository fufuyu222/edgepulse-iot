package com.edgepulse.service;

import com.edgepulse.domain.Alarm;
import com.edgepulse.domain.AlarmRule;
import com.edgepulse.domain.AlarmStatus;
import com.edgepulse.domain.RuleOperator;
import com.edgepulse.dto.TelemetryMessage;
import com.edgepulse.repository.AlarmRepository;
import com.edgepulse.repository.AlarmRuleRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlarmService {
    private final AlarmRuleRepository ruleRepository;
    private final AlarmRepository alarmRepository;
    private final EventStreamService eventStreamService;

    public AlarmService(
            AlarmRuleRepository ruleRepository,
            AlarmRepository alarmRepository,
            EventStreamService eventStreamService
    ) {
        this.ruleRepository = ruleRepository;
        this.alarmRepository = alarmRepository;
        this.eventStreamService = eventStreamService;
    }

    @Transactional
    public List<Alarm> evaluate(TelemetryMessage message) {
        List<AlarmRule> rules = ruleRepository.findByEnabledTrueAndDeviceIdIn(List.of(message.getDeviceId(), "*"));
        List<Alarm> generated = new ArrayList<>();
        for (AlarmRule rule : rules) {
            Double value = metricValue(message, rule.getMetricName());
            if (value == null || !matches(value, rule.getOperator(), rule.getThreshold())) {
                continue;
            }
            if (isSuppressed(rule, message.getDeviceId())) {
                continue;
            }
            String text = "%s %s %.2f, current value %.2f".formatted(
                    rule.getMetricName(),
                    rule.getOperator().name(),
                    rule.getThreshold(),
                    value
            );
            Alarm alarm = alarmRepository.save(new Alarm(
                    message.getDeviceId(),
                    rule.getMetricName(),
                    value,
                    rule.getThreshold(),
                    rule.getLevel(),
                    text
            ));
            generated.add(alarm);
            eventStreamService.publish("alarm", alarm);
        }
        return generated;
    }

    @Transactional
    public Alarm changeStatus(Long id, AlarmStatus status) {
        Alarm alarm = alarmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alarm not found: " + id));
        alarm.setStatus(status);
        Alarm saved = alarmRepository.save(alarm);
        eventStreamService.publish("alarm-status", saved);
        return saved;
    }

    private boolean isSuppressed(AlarmRule rule, String deviceId) {
        return alarmRepository.findTopByDeviceIdAndMetricNameAndStatusOrderByCreatedAtDesc(
                        deviceId,
                        rule.getMetricName(),
                        AlarmStatus.ACTIVE
                )
                .filter(alarm -> Duration.between(alarm.getCreatedAt(), Instant.now())
                        .toMinutes() < rule.getSuppressMinutes())
                .isPresent();
    }

    private Double metricValue(TelemetryMessage message, String metricName) {
        return switch (metricName) {
            case "temperature" -> message.getTemperature();
            case "voltage" -> message.getVoltage();
            default -> null;
        };
    }

    private boolean matches(Double value, RuleOperator operator, Double threshold) {
        return switch (operator) {
            case GT -> value > threshold;
            case GTE -> value >= threshold;
            case LT -> value < threshold;
            case LTE -> value <= threshold;
            case EQ -> value.equals(threshold);
        };
    }
}
