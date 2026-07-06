package com.edgepulse.controller;

import com.edgepulse.domain.TelemetryData;
import com.edgepulse.dto.TelemetryMessage;
import com.edgepulse.repository.TelemetryRepository;
import com.edgepulse.service.TelemetryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {
    private final TelemetryService telemetryService;
    private final TelemetryRepository telemetryRepository;

    public TelemetryController(TelemetryService telemetryService, TelemetryRepository telemetryRepository) {
        this.telemetryService = telemetryService;
        this.telemetryRepository = telemetryRepository;
    }

    @PostMapping
    public TelemetryData ingest(@Valid @RequestBody TelemetryMessage message) {
        return telemetryService.ingest(message);
    }

    @GetMapping("/latest")
    public List<TelemetryData> latest() {
        return telemetryRepository.findTop20ByOrderByReportedAtDesc();
    }

    @GetMapping("/{deviceId}")
    public List<TelemetryData> byDevice(@PathVariable String deviceId) {
        return telemetryRepository.findTop100ByDeviceIdOrderByReportedAtDesc(deviceId);
    }
}
