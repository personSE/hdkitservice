package com.huaweicloud.hdkitservice.controller;

import com.huaweicloud.hdkitservice.service.IncentiveClient;
import com.huaweicloud.hdkitservice.service.TelemetryHashService;
import com.huaweicloud.hdkitservice.service.UserHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/rest/developer/server/hdkitservice")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final IncentiveClient incentiveClient;
    private final TelemetryHashService hashService;
    private final UserHashService userHashService;

    public UserController(IncentiveClient incentiveClient, TelemetryHashService hashService,
                          UserHashService userHashService) {
        this.incentiveClient = incentiveClient;
        this.hashService = hashService;
        this.userHashService = userHashService;
    }

    @GetMapping("/user/generatorUserIDHash")
    public ResponseEntity<Map<String, String>> generatorUserIDHash(@RequestHeader("X-HW-AK") String ak,
                                                                    @RequestHeader("X-HW-SK") String sk,
                                                                    @RequestHeader(value = "X-HW-Security-Token", required = false) String securityToken) {
        String domainId = null;
        boolean genByAsk = false;
        try {
            domainId = incentiveClient.resolveDomainIdFromIam(ak, sk, securityToken);
        } catch (IncentiveClient.IncentiveException e) {
            log.warn("[generatorUserIDHash] IAM failed, using AK fallback: {}", e.getMessage());
            genByAsk = true;
        }

        String userHash;
        if (domainId != null && !domainId.isEmpty()) {
            userHash = hashService.generateUserHash(domainId);
        } else {
            userHash = hashService.generateFallbackUserHash(ak);
            genByAsk = true;
        }

        userHashService.record(userHash, domainId, genByAsk);

        return ResponseEntity.ok(Map.of("userHash", userHash));
    }
}