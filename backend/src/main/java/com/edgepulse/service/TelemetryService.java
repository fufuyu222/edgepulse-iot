package com.edgepulse.service;

import com.edgepulse.domain.Device;
import com.edgepulse.domain.DeviceStatus;
import com.edgepulse.domain.TelemetryData;
import com.edgepulse.dto.TelemetryMessage;
import com.edgepulse.repository.DeviceRepository;
import com.edgepulse.repository.TelemetryRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TelemetryService {
    private final DeviceRepository deviceRepository;
    private final TelemetryRepository telemetryRepository;
    private final DeviceStatusCache statusCache;
    private final AlarmService alarmService;
    private final EventStreamService eventStreamService;

    public TelemetryService(
            DeviceRepository deviceRepository,
            TelemetryRepository telemetryRepository,
            DeviceStatusCache statusCache,
            AlarmService alarmService,
            EventStreamService eventStreamService
    ) {
        this.deviceRepository = deviceRepository;
        this.telemetryRepository = telemetryRepository;
        this.statusCache = statusCache;
        this.alarmService = alarmService;
        this.eventStreamService = eventStreamService;
    }

    @Transactional
    public TelemetryData ingest(TelemetryMessage message) {
        Instant now = Instant.now();
        Instant reportedAt = message.getTimestamp() == null ? now : message.getTimestamp();
        Device device = deviceRepository.findById(message.getDeviceId())
                .orElseGet(() -> new Device(message.getDeviceId(), "Device " + message.getDeviceId(), "simulator"));
        device.setStatus(DeviceStatus.ONLINE);
        device.setLastSeenAt(now);
        deviceRepository.save(device);
        statusCache.save(device);

        TelemetryData data = telemetryRepository.save(new TelemetryData(
                message.getDeviceId(),
                message.getTemperature(),
                message.getVoltage(),
                reportedAt,
                now
        ));
        eventStreamService.publish("telemetry", data);
        eventStreamService.publish("device", device);
        alarmService.evaluate(message);
        return data;
    }
}
