package com.edgepulse.controller;

import com.edgepulse.domain.AlarmRule;
import com.edgepulse.repository.AlarmRuleRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alarm-rules")
public class AlarmRuleController {
    private final AlarmRuleRepository alarmRuleRepository;

    public AlarmRuleController(AlarmRuleRepository alarmRuleRepository) {
        this.alarmRuleRepository = alarmRuleRepository;
    }

    @GetMapping
    public List<AlarmRule> list() {
        return alarmRuleRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }
}
