package com.huaweicloud.hdkitservice.service;

import com.huaweicloud.hdkitservice.config.HdkitConfig;
import com.huaweicloud.hdkitservice.model.CheckUserResponse;
import com.huaweicloud.hdkitservice.model.ConnectRequest;
import com.huaweicloud.hdkitservice.model.ConnectResponse;
import com.huaweicloud.hdkitservice.model.CredentialsRequest;
import com.huaweicloud.hdkitservice.model.CredentialsResponse;
import com.huaweicloud.hdkitservice.model.SandboxSession;
import com.huaweicloud.hdkitservice.model.SignAgreementResponse;
import com.huaweicloud.hdkitservice.repository.SandboxSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class SandboxService {

    private static final Logger log = LoggerFactory.getLogger(SandboxService.class);
    private static final String STATUS_READY = "0004";
    private static final String STATUS_RUNNING = "0002";

    private final DevStationClient devStation;
    private final HdkitConfig config;
    private final SandboxSessionRepository sandboxSessionRepo;

    public SandboxService(DevStationClient devStation, HdkitConfig config,
                          SandboxSessionRepository sandboxSessionRepo) {
        this.devStation = devStation;
        this.config = config;
        this.sandboxSessionRepo = sandboxSessionRepo;
    }

public ConnectResponse connect(ConnectRequest req, String ak, String sk, String securityToken) {
        String akHash = sha256(ak);
        long start = System.currentTimeMillis();
        String templateId = (req.templateId() == null || req.templateId().isEmpty())
                ? config.templateId() : req.templateId();
        String flavorId = (req.flavorId() == null || req.flavorId().isEmpty())
                ? config.flavorId() : req.flavorId();

        boolean created = false;
        String devStageId = null;
        try {
            // 协议门禁：协议非最新版（sign_status==2 已签旧版）也视为未签署，
            // 否则上游 /open-api-public/v2/devenvs 会以 HD.83700031 拒绝，且异常被兜底打成 500 内部错误。
            if (!allAgreementsSigned(devStation.agreements(ak, sk, securityToken))) {
                throw new HdkitException("HDKIT_NOT_AGREEMENT", "用户未签署最新版协议，签署需由用户本人确认后完成", null);
            }

            List<DevStationClient.Devenv> actual = devStation.list("", ak, sk, securityToken);
            DevStationClient.Devenv existing = findHcdkInstance(actual);

            if (existing != null) {
                // 复用已有实例
                devStageId = existing.id();
            } else {
                // 按账号实时计数：云上现存环境数（含手动创建的）达到上限则拒绝
                if (actual.size() >= config.maxConcurrent()) {
                    throw new HdkitException("HDKIT_CONFLICT", "已达最大并发沙箱数 " + config.maxConcurrent(), null);
                }
                // 新建实例（name 内部生成，保证唯一可识别）
                String name = "hcdk" + Long.toString(System.currentTimeMillis(), 36);
                devStageId = devStation.create(name, templateId, flavorId, req.source(), req.env(), req.git(), ak, sk, securityToken);
                created = true;
            }

            if (created) {
                waitForStatus(devStageId, STATUS_READY, config.connectTimeout(), ak, sk, securityToken);
            }
            ensureRunning(devStageId, ak, sk, securityToken);
            devStation.autoConfig(devStageId, true, ak, sk, securityToken); // 注入临时 AK/SK

            DevStationClient.Connections conns = devStation.connections(devStageId, config.source(), ak, sk, securityToken);
            long connectionId = pickConnected(conns);
            DevStationClient.ConnectionAddress addr = devStation.address(devStageId, connectionId, ak, sk, securityToken);
            String address = addr.url() + "&source=" + addr.source();

            // 无本地会话：session_id 等价 dev_stage_id
            recordSession(akHash, devStageId, created ? "create" : "reuse",
                    "success", null, System.currentTimeMillis() - start, templateId, flavorId);
            return new ConnectResponse(devStageId, devStageId, String.valueOf(connectionId), address, "connected");
        } catch (HdkitException e) {
            recordSession(akHash, devStageId, created ? "create" : "reuse",
                    "fail", e.code(), System.currentTimeMillis() - start, templateId, flavorId);
            // 业务异常原样抛出（HDKIT_NOT_AGREEMENT / HDKIT_CONFLICT），不被吞成通用错误
            throw e;
        } catch (DevStationClient.DevStationException e) {
            recordSession(akHash, devStageId, created ? "create" : "reuse",
                    "fail", "HDKIT_UPSTREAM_ERROR", System.currentTimeMillis() - start, templateId, flavorId);
            // 上游/编排失败：暴露真实原因，避免被打成 500 内部错误
            log.error("[connect] upstream failed: {}", e.getMessage());
            if (created) {
                try { releaseById(devStageId, ak, sk, securityToken); } catch (Exception ex) {
                    log.error("[connect] rollback release failed: {}", ex.getMessage());
                }
            }
            throw new HdkitException("HDKIT_UPSTREAM_ERROR", "沙箱编排上游调用失败: " + e.getMessage(), e);
        } catch (Exception e) {
            recordSession(akHash, devStageId, created ? "create" : "reuse",
                    "fail", "HDKIT_CONNECT_FAILED", System.currentTimeMillis() - start, templateId, flavorId);
            log.error("[connect] failed: {}", e.getMessage());
            if (created) {
                try { releaseById(devStageId, ak, sk, securityToken); } catch (Exception ex) {
                    log.error("[connect] rollback release failed: {}", ex.getMessage());
                }
            }
            throw new HdkitException("HDKIT_CONNECT_FAILED", "连接沙箱失败", e);
        }
    }

    private DevStationClient.Devenv findHcdkInstance(List<DevStationClient.Devenv> actual) {
        for (DevStationClient.Devenv d : actual) {
            if (d.name() != null && d.name().startsWith("hcdk")) {
                return d;
            }
        }
        return null;
    }

    private void ensureRunning(String devStageId, String ak, String sk, String securityToken) {
        String status = devStation.statusOf(devStageId, ak, sk, securityToken);
        if (!isStatus(status, STATUS_RUNNING)) {
            devStation.start(devStageId, config.source(), ak, sk, securityToken);
            waitForStatus(devStageId, STATUS_RUNNING, config.connectTimeout(), ak, sk, securityToken);
        }
    }

    public CredentialsResponse credentials(CredentialsRequest req, String ak, String sk, String securityToken) {
        String devStageId = resolveDevStageId(req.sessionId(), req.devStageId());
        if (devStageId == null) {
            throw new HdkitException("HDKIT_INVALID_REQUEST", "缺少 session_id 或 dev_stage_id", null);
        }

        String status = devStation.statusOf(devStageId, ak, sk, securityToken);
        if (status == null) {
            throw new HdkitException("HDKIT_SANDBOX_NOT_FOUND", "环境不存在或已被删除", null);
        }
        if (!isStatus(status, STATUS_RUNNING)) {
            throw new HdkitException("HDKIT_NOT_RUNNING", "环境未处于 RUNNING，无法注入临时 AK/SK", null);
        }

        boolean enableSts = req.enableSts() == null || req.enableSts();
        String expiresAt = devStation.autoConfig(devStageId, enableSts, ak, sk, securityToken);

        return new CredentialsResponse(devStageId, expiresAt);
    }

private void recordSession(String akHash, String devStageId, String action,
                              String status, String errorCode, long durationMs,
                              String templateId, String flavorId) {
        try {
            SandboxSession session = new SandboxSession();
            session.setAkHash(akHash);
            session.setDevStageId(devStageId);
            session.setAction(action);
            session.setStatus(status);
            session.setDurationMs(durationMs);
            session.setTemplateId(templateId);
            session.setFlavorId(flavorId);
            session.setErrorCode(errorCode);
            session.setCreatedAt(LocalDateTime.now());
            sandboxSessionRepo.save(session);
        } catch (Exception e) {
            log.warn("[sandbox] recordSession failed: {}", e.getMessage());
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private void releaseById(String devStageId, String ak, String sk, String securityToken) {
        if (devStation.statusOf(devStageId, ak, sk, securityToken) == null) {
            return; // 幂等：环境已不存在，视为已释放
        }
        devStation.close(devStageId, config.source(), ak, sk, securityToken);
        waitForStatus(devStageId, STATUS_READY, config.releaseTimeout(), ak, sk, securityToken);
        devStation.delete(devStageId, config.source(), ak, sk, securityToken);
        waitForGone(devStageId, config.releaseTimeout(), ak, sk, securityToken);
    }

    public CheckUserResponse checkUser(String ak, String sk, String securityToken) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        CompletableFuture<Boolean> realnameFuture = CompletableFuture.supplyAsync(
                () -> withMdc(mdc, () -> "2".equals(devStation.realNameStatus(ak, sk, securityToken))));
        CompletableFuture<Boolean> agreementFuture = CompletableFuture.supplyAsync(
                () -> withMdc(mdc, () -> allAgreementsSigned(devStation.agreements(ak, sk, securityToken))));

        boolean realnameOk = await(realnameFuture, "查询实名状态失败");
        boolean agreementOk = await(agreementFuture, "查询协议状态失败");

        // 实名与协议并行判定，按缺失情况返回对应 403，方便调用方分别引导用户
        if (!realnameOk && !agreementOk) {
            throw new HdkitException("HDKIT_NOT_REALNAME_AND_AGREEMENT",
                    "用户未完成实名认证且未签署最新版协议，请分别完成实名认证与协议签署", null);
        }
        if (!realnameOk) {
            throw new HdkitException("HDKIT_NOT_REALNAME",
                    "用户未完成实名认证，请在华为云控制台完成实名认证", null);
        }
        if (!agreementOk) {
            throw new HdkitException("HDKIT_NOT_AGREEMENT",
                    "用户未签署最新版协议，签署需由用户本人确认后完成", null);
        }
        return new CheckUserResponse(true, true);
    }

    private static <T> T withMdc(Map<String, String> mdc, java.util.function.Supplier<T> supplier) {
        if (mdc != null) {
            MDC.setContextMap(mdc);
        }
        try {
            return supplier.get();
        } finally {
            MDC.clear();
        }
    }

    public SignAgreementResponse signAgreement(String ak, String sk, String securityToken) {
        List<DevStationClient.Agreement> agreements = devStation.agreements(ak, sk, securityToken);
        List<DevStationClient.SignReq> toSign = new ArrayList<>();
        for (DevStationClient.Agreement a : agreements) {
            if (a.signStatus() == 2 || a.signStatus() == 3) {
                toSign.add(new DevStationClient.SignReq(a.agrType(), a.country(), a.language(), a.version()));
            }
        }
        if (!toSign.isEmpty()) {
            devStation.signAgreements(toSign, ak, sk, securityToken);
        }
        return new SignAgreementResponse(true, toSign.size());
    }

    private boolean allAgreementsSigned(List<DevStationClient.Agreement> agreements) {
        if (agreements.isEmpty()) return false;
        for (DevStationClient.Agreement a : agreements) {
            // 仅最新版（sign_status==1）视为已签署；
            // sign_status==2 表示已签旧版，DevStation 上游会拒绝所有请求，必须补签
            if (a.signStatus() != 1) return false;
        }
        return true;
    }

    private boolean await(CompletableFuture<Boolean> f, String errMsg) {
        try {
            return f.join();
        } catch (CompletionException e) {
            Throwable c = e.getCause();
            if (c instanceof DevStationClient.DevStationException) {
                throw new HdkitException("HDKIT_UPSTREAM_ERROR", errMsg, c);
            }
            throw new HdkitException("HDKIT_INTERNAL", errMsg, c);
        }
    }

    private long pickConnected(DevStationClient.Connections conns) {
        for (DevStationClient.Conn c : conns.list()) {
            if ("CONNECTED".equals(c.status())) return c.connectionId();
        }
        return conns.connectionId();
    }

    private void waitForStatus(String devStageId, String target, long timeoutMs, String ak, String sk, String securityToken) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            String status = devStation.statusOf(devStageId, ak, sk, securityToken);
            if (isStatus(status, target)) return;
            sleep(config.pollIntervalMs());
        }
        throw new HdkitException("HDKIT_TIMEOUT", "等待状态 " + target + " 超时", null);
    }

    private boolean isStatus(String actual, String code) {
        if (actual == null) return false;
        int dot = actual.lastIndexOf('.');
        return (dot >= 0 ? actual.substring(dot + 1) : actual).equals(code);
    }

    private void waitForGone(String devStageId, long timeoutMs, String ak, String sk, String securityToken) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (devStation.statusOf(devStageId, ak, sk, securityToken) == null) return;
            sleep(config.pollIntervalMs());
        }
        throw new HdkitException("HDKIT_RELEASE_TIMEOUT", "等待释放完成超时", null);
    }

    private String resolveDevStageId(String sessionId, String devStageId) {
        // session_id 是 dev_stage_id 的别名（历史兼容）
        if (sessionId != null && !sessionId.isEmpty()) return sessionId;
        return (devStageId != null && !devStageId.isEmpty()) ? devStageId : null;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static class HdkitException extends RuntimeException {
        private final String code;
        public HdkitException(String code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }
        public String code() { return code; }
    }
}
