package com.edgepulse.repository;

import com.edgepulse.domain.Device;
import com.edgepulse.domain.DeviceStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, String> {
    long countByStatus(DeviceStatus status);

    List<Device> findTop20ByOrderByLastSeenAtDesc();
}
