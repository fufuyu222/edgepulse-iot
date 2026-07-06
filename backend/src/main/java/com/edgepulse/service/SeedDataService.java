package com.edgepulse.service;

import com.edgepulse.domain.AlarmLevel;
import com.edgepulse.domain.AlarmRule;
import com.edgepulse.domain.RuleOperator;
import com.edgepulse.repository.AlarmRuleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedDataService implements ApplicationRunner {
    private final AlarmRuleRepository alarmRuleRepository;

    public SeedDataService(AlarmRuleRepository alarmRuleRepository) {
        this.alarmRuleRepository = alarmRuleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (alarmRuleRepository.count() > 0) {
            return;
        }
        alarmRuleRepository.save(new AlarmRule("*", "temperature", RuleOperator.GT, 80.0, AlarmLevel.CRITICAL));
        alarmRuleRepository.save(new AlarmRule("*", "voltage", RuleOperator.LT, 180.0, AlarmLevel.WARN));
    }
}
