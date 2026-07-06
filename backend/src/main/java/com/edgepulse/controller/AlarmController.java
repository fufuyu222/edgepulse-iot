package com.edgepulse.controller;

import com.edgepulse.domain.Alarm;
import com.edgepulse.domain.AlarmStatus;
import com.edgepulse.repository.AlarmRepository;
import com.edgepulse.service.AlarmService;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alarms")
public class AlarmController {
    private final AlarmRepository alarmRepository;
    private final AlarmService alarmService;

    public AlarmController(AlarmRepository alarmRepository, AlarmService alarmService) {
        this.alarmRepository = alarmRepository;
        this.alarmService = alarmService;
    }

    @GetMapping
    public List<Alarm> list() {
        return alarmRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @PatchMapping("/{id}/status")
    public Alarm changeStatus(@PathVariable Long id, @RequestParam AlarmStatus status) {
        return alarmService.changeStatus(id, status);
    }
}
