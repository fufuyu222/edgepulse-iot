package com.edgepulse.controller;

import com.edgepulse.domain.AlarmStatus;
import com.edgepulse.domain.DeviceStatus;
import com.edgepulse.dto.DashboardSummary;
import com.edgepulse.repository.AlarmRepository;
import com.edgepulse.repository.DeviceRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DeviceRepository deviceRepository;
    private final AlarmRepository alarmRepository;

    public DashboardController(DeviceRepository deviceRepository, AlarmRepository alarmRepository) {
        this.deviceRepository = deviceRepository;
        this.alarmRepository = alarmRepository;
    }

    @GetMapping("/summary")
    public DashboardSummary summary() {
        return new DashboardSummary(
                deviceRepository.count(),
                deviceRepository.countByStatus(DeviceStatus.ONLINE),
                alarmRepository.countByStatus(AlarmStatus.ACTIVE),
                alarmRepository.countByCreatedAtAfter(Instant.now().truncatedTo(ChronoUnit.DAYS))
        );
    }
}
