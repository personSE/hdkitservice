package com.huaweicloud.hdkitservice.service;

import com.huaweicloud.hdkitservice.model.TelemetryEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TelemetryEventCheckService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryEventCheckService.class);

    private final List<TelemetryFilterRule> rules;

    public TelemetryEventCheckService(List<TelemetryFilterRule> rules) {
        this.rules = rules == null ? List.of() : rules;
    }

    public List<TelemetryEventDto> filter(List<TelemetryEventDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }
        List<TelemetryEventDto> accepted = dtos;
        for (TelemetryFilterRule rule : rules) {
            accepted = applyRule(rule, accepted);
            if (accepted.isEmpty()) {
                break;
            }
        }
        return accepted;
    }

    private List<TelemetryEventDto> applyRule(TelemetryFilterRule rule, List<TelemetryEventDto> dtos) {
        List<TelemetryEventDto> result = new ArrayList<>(dtos.size());
        int dropped = 0;
        for (TelemetryEventDto dto : dtos) {
            if (rule.shouldDrop(dto)) {
                dropped++;
                log.debug("[telemetry] filter={} dropped event key={} harness={}",
                        rule.name(), dto.key(), dto.harness());
            } else {
                result.add(dto);
            }
        }
        if (dropped > 0) {
            log.warn("[telemetry] filter={} dropped {} events", rule.name(), dropped);
        }
        return result;
    }
}