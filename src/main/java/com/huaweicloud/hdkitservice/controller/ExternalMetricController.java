package com.huaweicloud.hdkitservice.controller;

import com.huaweicloud.hdkitservice.service.ExternalMetricService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest/developer/server/hdkitservice/external")
public class ExternalMetricController {

    private final ExternalMetricService externalMetricService;

    public ExternalMetricController(ExternalMetricService externalMetricService) {
        this.externalMetricService = externalMetricService;
    }

    @PostMapping(value = "/metrics", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> postMetrics(@RequestBody List<ExternalMetricService.ExternalMetricDto> metrics,
                                                           HttpServletRequest request) {
        if (metrics == null || metrics.isEmpty()) {
            return ResponseEntity.ok(Map.of("received", 0));
        }
        String source = (String) request.getAttribute("source");
        int received = externalMetricService.saveBatch(metrics, source);
        return ResponseEntity.ok(Map.of("received", received));
    }
}
