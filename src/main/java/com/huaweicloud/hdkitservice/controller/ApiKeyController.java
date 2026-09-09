package com.huaweicloud.hdkitservice.controller;

import com.huaweicloud.hdkitservice.model.ApiKey;
import com.huaweicloud.hdkitservice.repository.ApiKeyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/rest/developer/server/hdkitservice/dashboard/api-keys")
public class ApiKeyController {

    private final ApiKeyRepository repository;

    public ApiKeyController(ApiKeyRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createKey(@RequestParam String source) {
        String key = UUID.randomUUID().toString().replace("-", "");
        ApiKey apiKey = new ApiKey(key, source);
        repository.save(apiKey);
        return ResponseEntity.ok(Map.of(
                "id", apiKey.getId(),
                "apiKey", key,
                "source", source
        ));
    }

    @GetMapping
    public List<ApiKey> listKeys() {
        return repository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> disableKey(@PathVariable Long id) {
        ApiKey apiKey = repository.findById(id).orElse(null);
        if (apiKey == null) {
            return ResponseEntity.status(404).body(Map.of("error", "API key not found"));
        }
        apiKey.setEnabled(false);
        repository.save(apiKey);
        return ResponseEntity.ok(Map.of("status", "disabled"));
    }
}
