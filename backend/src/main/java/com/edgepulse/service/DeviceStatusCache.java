package com.edgepulse.service;

import com.edgepulse.domain.Device;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeviceStatusCache {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public DeviceStatusCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${edgepulse.redis.enabled:true}") boolean enabled
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    public void save(Device device) {
        if (!enabled) {
            return;
        }
        try {
            String value = objectMapper.writeValueAsString(Map.of(
                    "status", device.getStatus().name(),
                    "lastSeenAt", String.valueOf(device.getLastSeenAt())
            ));
            redisTemplate.opsForValue().set("device:status:" + device.getId(), value);
        } catch (Exception ignored) {
            // Redis is an acceleration layer here. Database state remains authoritative.
        }
    }
}
