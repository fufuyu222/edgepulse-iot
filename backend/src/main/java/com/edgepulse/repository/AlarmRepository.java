package com.edgepulse.repository;

import com.edgepulse.domain.Alarm;
import com.edgepulse.domain.AlarmStatus;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    long countByStatus(AlarmStatus status);

    Optional<Alarm> findTopByDeviceIdAndMetricNameAndStatusOrderByCreatedAtDesc(
            String deviceId,
            String metricName,
            AlarmStatus status
    );

    long countByCreatedAtAfter(Instant createdAt);
}
