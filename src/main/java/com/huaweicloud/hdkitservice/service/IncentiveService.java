package com.huaweicloud.hdkitservice.service;

import com.huaweicloud.hdkitservice.config.HdkitConfig;
import com.huaweicloud.hdkitservice.model.VoucherClaimLog;
import com.huaweicloud.hdkitservice.model.VoucherRecord;
import com.huaweicloud.hdkitservice.repository.VoucherClaimLogRepository;
import com.huaweicloud.hdkitservice.repository.VoucherRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class IncentiveService {

    private static final Logger log = LoggerFactory.getLogger(IncentiveService.class);

    private final IncentiveClient client;
    private final HdkitConfig config;
    private final VoucherClaimLogRepository claimLogRepo;
    private final VoucherRecordRepository voucherRecordRepo;

    public IncentiveService(IncentiveClient client, HdkitConfig config,
                            VoucherClaimLogRepository claimLogRepo,
                            VoucherRecordRepository voucherRecordRepo) {
        this.client = client;
        this.config = config;
        this.claimLogRepo = claimLogRepo;
        this.voucherRecordRepo = voucherRecordRepo;
    }

    public VoucherStatusResult voucherStatus(String ak, String sk, String securityToken, String providedDomainId) {
        if ("test".equalsIgnoreCase(config.deployEnv())
                && (providedDomainId == null || providedDomainId.isEmpty())) {
            return new VoucherStatusResult(false, "测试环境需提供 domain_id");
        }
        String domainId;
        try {
            domainId = "test".equalsIgnoreCase(config.deployEnv())
                    ? providedDomainId : client.resolveDomainIdFromIam(ak, sk, securityToken);
        } catch (IncentiveClient.IncentiveException e) {
            return new VoucherStatusResult(false, "查询失败");
        }

        IncentiveClient.CheckResult check = client.checkCouponIssued(domainId);
        if (check.serviceError()) {
            return new VoucherStatusResult(false, "查询失败");
        }
        if (check.issued()) {
            return new VoucherStatusResult(true, "已领取");
        }
        return new VoucherStatusResult(false, "未领取");
    }

    public VoucherClaimResult voucherClaim(String ak, String sk, String securityToken, String providedDomainId) {
        String akHash = sha256(ak);

        if ("test".equalsIgnoreCase(config.deployEnv())
                && (providedDomainId == null || providedDomainId.isEmpty())) {
            recordVoucher(akHash, null, null, null, "fail", "NO_DOMAIN_ID");
            return new VoucherClaimResult(false, null, 0, "测试环境需提供 domain_id");
        }
        String domainId;
        try {
            domainId = "test".equalsIgnoreCase(config.deployEnv())
                    ? providedDomainId : client.resolveDomainIdFromIam(ak, sk, securityToken);
        } catch (IncentiveClient.IncentiveException e) {
            recordVoucher(akHash, null, null, null, "fail", "IAM_FAILED");
            return new VoucherClaimResult(false, null, 0, "激励服务查询失败，请稍后重试");
        }

        IncentiveClient.CheckResult check = client.checkCouponIssued(domainId);
        if (check.serviceError()) {
            recordVoucher(akHash, sha256(domainId), null, null, "fail", "CHECK_FAILED");
            return new VoucherClaimResult(false, null, 0, "激励服务查询失败，请稍后重试");
        }
        if (check.issued()) {
            recordVoucher(akHash, sha256(domainId), null, null, "already_claimed", null);
            return new VoucherClaimResult(true, null, 0, "已领取过");
        }

        IncentiveClient.IssueResult issue = client.issueCoupon(domainId);
        if (!issue.success()) {
            if ("HD.60620016".equals(issue.errorCode())) {
                recordVoucher(akHash, sha256(domainId), null, null, "already_claimed", "HD.60620016");
                return new VoucherClaimResult(true, null, 0, "已领取过");
            }
            if ("HD.60630042".equals(issue.errorCode())) {
                recordVoucher(akHash, sha256(domainId), null, null, "fail", "HD.60630042");
                return new VoucherClaimResult(false, null, 0,
                        "本月代金券总额度已用完，所有账号均无法领取，请下月再重试");
            }
            recordVoucher(akHash, sha256(domainId), null, null, "fail", issue.errorCode());
            String errMsg = "发券失败: " + issue.error();
            if ("HD.60630022".equals(issue.errorCode())) {
                errMsg += " 请先完成实名认证：https://account.huaweicloud.com/usercenter/"
                        + "?region=cn-north-4&locale=zh-cn#/accountindex/realNameAuthing";
            }
            return new VoucherClaimResult(false, null, 0, errMsg);
        }

        int amount = Math.min(config.incentiveFaceAmount(), 500);

        logVoucherClaim(domainId, issue.couponId(), amount);
        recordVoucher(akHash, sha256(domainId), issue.couponId(), amount * 100, "success", null);

        return new VoucherClaimResult(true, issue.couponId(), amount, "领取成功");
    }

    private void recordVoucher(String akHash, String domainHash, String couponId,
                               Integer faceAmountInCents, String status, String errorCode) {
        try {
            VoucherRecord record = new VoucherRecord();
            record.setAkHash(akHash);
            record.setDomainHash(domainHash);
            record.setCouponId(couponId);
            record.setFaceAmount(faceAmountInCents);
            record.setVoucherType("coupon");
            record.setStatus(status);
            record.setErrorCode(errorCode);
            record.setActivityId("open-capability-2026");
            record.setSource("新手活动");
            record.setCreatedAt(LocalDateTime.now());
            voucherRecordRepo.save(record);
        } catch (Exception e) {
            log.warn("[voucher] recordVoucher failed: {}", e.getMessage());
        }
    }

    private void logVoucherClaim(String domainId, String couponId, int amountYuan) {
        try {
            String claimId = "V-" + LocalDate.now().toString().replace("-", "")
                    + "-" + String.format("%06d", System.nanoTime() % 1000000);
            VoucherClaimLog logEntry = new VoucherClaimLog(
                    claimId,
                    sha256(domainId),
                    amountYuan * 100,
                    couponId,
                    "新手活动",
                    LocalDateTime.now()
            );
            claimLogRepo.save(logEntry);
        } catch (Exception e) {
            log.warn("[voucher] failed to log claim: {}", e.getMessage());
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return input;
        }
    }

    public record VoucherStatusResult(boolean claimed, String message) {}
    public record VoucherClaimResult(boolean claimed, String voucherId, int amount, String message) {}
}