package com.edgepulse.repository;

import com.edgepulse.domain.TelemetryData;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryRepository extends JpaRepository<TelemetryData, Long> {
    List<TelemetryData> findTop100ByDeviceIdOrderByReportedAtDesc(String deviceId);

    List<TelemetryData> findTop20ByOrderByReportedAtDesc();
}
