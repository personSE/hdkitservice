package com.huaweicloud.hdkitservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TelemetryRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TelemetryRateLimitFilter.class);

    private static final String TELEMETRY_PATH = "/rest/developer/server/hdkitservice/telemetry/events";
    private static final String USER_HASH_PATH = "/rest/developer/server/hdkitservice/user/generatorUserIDHash";

    private final int telemetryGlobalConcurrency;
    private final int telemetryPerIp;
    private final int telemetryPerInstall;
    private final int userHashGlobalConcurrency;
    private final int userHashPerIp;
    private final int userHashPerAk;

    private final Semaphore telemetrySemaphore;
    private final Semaphore userHashSemaphore;

    private final Map<String, long[]> telemetryIpWindows = new ConcurrentHashMap<>();
    private final Map<String, long[]> telemetryInstallIdWindows = new ConcurrentHashMap<>();
    private final Map<String, long[]> userHashIpWindows = new ConcurrentHashMap<>();
    private final Map<String, long[]> userHashAkWindows = new ConcurrentHashMap<>();

    public TelemetryRateLimitFilter(
            @Value("${hdkit.rate-limit.telemetry.global-concurrency:200}") int telemetryGlobalConcurrency,
            @Value("${hdkit.rate-limit.telemetry.per-ip-per-second:100}") int telemetryPerIp,
            @Value("${hdkit.rate-limit.telemetry.per-install-per-second:20}") int telemetryPerInstall,
            @Value("${hdkit.rate-limit.userhash.global-concurrency:50}") int userHashGlobalConcurrency,
            @Value("${hdkit.rate-limit.userhash.per-ip-per-second:50}") int userHashPerIp,
            @Value("${hdkit.rate-limit.userhash.per-ak-per-second:10}") int userHashPerAk) {
        this.telemetryGlobalConcurrency = telemetryGlobalConcurrency;
        this.telemetryPerIp = telemetryPerIp;
        this.telemetryPerInstall = telemetryPerInstall;
        this.userHashGlobalConcurrency = userHashGlobalConcurrency;
        this.userHashPerIp = userHashPerIp;
        this.userHashPerAk = userHashPerAk;
        this.telemetrySemaphore = new Semaphore(telemetryGlobalConcurrency);
        this.userHashSemaphore = new Semaphore(userHashGlobalConcurrency);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (TELEMETRY_PATH.equals(uri)) {
            handleTelemetry(request, response, chain);
        } else if (USER_HASH_PATH.equals(uri)) {
            handleUserHash(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    private void handleTelemetry(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!telemetrySemaphore.tryAcquire()) {
            writeRateResponse(response, HttpStatus.SERVICE_UNAVAILABLE.value(), "HDKIT_OVERLOADED", "Server overloaded");
            return;
        }
        try {
            String ip = getClientIp(request);
            if (!checkRate(telemetryIpWindows, ip, telemetryPerIp)) {
                writeRateResponse(response, HttpStatus.TOO_MANY_REQUESTS.value(), "HDKIT_RATE_LIMITED", "Too many requests");
                return;
            }
            String installId = request.getHeader("X-Install-ID");
            if (installId != null && !installId.isEmpty()) {
                if (!checkRate(telemetryInstallIdWindows, installId, telemetryPerInstall)) {
                    writeRateResponse(response, HttpStatus.TOO_MANY_REQUESTS.value(), "HDKIT_RATE_LIMITED", "Too many requests");
                    return;
                }
            }
            chain.doFilter(request, response);
        } finally {
            telemetrySemaphore.release();
        }
    }

    private void handleUserHash(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!userHashSemaphore.tryAcquire()) {
            writeRateResponse(response, HttpStatus.SERVICE_UNAVAILABLE.value(), "HDKIT_OVERLOADED", "Server overloaded");
            return;
        }
        try {
            String ip = getClientIp(request);
            if (!checkRate(userHashIpWindows, ip, userHashPerIp)) {
                writeRateResponse(response, HttpStatus.TOO_MANY_REQUESTS.value(), "HDKIT_RATE_LIMITED", "Too many requests");
                return;
            }
            String ak = request.getHeader("X-HW-AK");
            if (ak != null && !ak.isEmpty()) {
                if (!checkRate(userHashAkWindows, ak, userHashPerAk)) {
                    writeRateResponse(response, HttpStatus.TOO_MANY_REQUESTS.value(), "HDKIT_RATE_LIMITED", "Too many requests");
                    return;
                }
            }
            chain.doFilter(request, response);
        } finally {
            userHashSemaphore.release();
        }
    }

    private void writeRateResponse(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }

    private boolean checkRate(Map<String, long[]> windows, String key, int maxPerSecond) {
        long now = System.currentTimeMillis();
        long[] entry = windows.computeIfAbsent(key, k -> new long[]{now, 0});

        synchronized (entry) {
            if (now - entry[0] >= 1000) {
                entry[0] = now;
                entry[1] = 1;
                return true;
            }
            if (entry[1] >= maxPerSecond) {
                return false;
            }
            entry[1]++;
            return true;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}