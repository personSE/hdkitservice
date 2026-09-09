package com.huaweicloud.hdkitservice.service;

import com.huaweicloud.hdkitservice.model.ExternalMetric;
import com.huaweicloud.hdkitservice.repository.ExternalMetricRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExternalMetricService {

    private static final Logger log = LoggerFactory.getLogger(ExternalMetricService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ExternalMetricRepository externalMetricRepository;

    public ExternalMetricService(ExternalMetricRepository externalMetricRepository) {
        this.externalMetricRepository = externalMetricRepository;
    }

    public int saveBatch(List<ExternalMetricDto> metrics, String source) {
        List<ExternalMetric> entities = new ArrayList<>();
        for (ExternalMetricDto dto : metrics) {
            if (dto.metricKey() == null || dto.metricKey().isBlank()) {
                continue;
            }
            if (dto.metricValue() == null || dto.metricValue() < 0) {
                continue;
            }
            LocalDate date = dto.metricDate() != null ? dto.metricDate() : LocalDate.now();
            String extraJson = null;
            if (dto.extra() != null && !dto.extra().isEmpty()) {
                try {
                    extraJson = objectMapper.writeValueAsString(dto.extra());
                } catch (Exception e) {
                    log.warn("Failed to serialize extra for metric_key={}", dto.metricKey());
                }
            }
            ExternalMetric metric = new ExternalMetric(
                    dto.metricKey(),
                    dto.metricValue(),
                    date,
                    source != null ? source : "unknown",
                    extraJson
            );
            entities.add(metric);
        }
        if (!entities.isEmpty()) {
            externalMetricRepository.saveAll(entities);
        }
        return entities.size();
    }

    public record ExternalMetricDto(
            String metricKey,
            Long metricValue,
            LocalDate metricDate,
            Map<String, Object> extra
    ) {}
}
