package com.edgepulse.repository;

import com.edgepulse.domain.AlarmRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRuleRepository extends JpaRepository<AlarmRule, Long> {
    List<AlarmRule> findByEnabledTrueAndDeviceIdIn(List<String> deviceIds);
}
