package com.huaweicloud.hdkitservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_record")
public class VoucherRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ak_hash", nullable = false, length = 64)
    private String akHash;

    @Column(name = "domain_hash", length = 64)
    private String domainHash;

    @Column(name = "coupon_id", length = 128)
    private String couponId;

    @Column(name = "face_amount")
    private Integer faceAmount;

    @Column(name = "voucher_type", length = 16)
    private String voucherType = "coupon";

    @Column(name = "status", nullable = false, length = 24)
    private String status;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "activity_id", length = 64)
    private String activityId = "open-capability-2026";

    @Column(name = "batch_no", length = 64)
    private String batchNo;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "source", length = 64)
    private String source;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public VoucherRecord() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAkHash() { return akHash; }
    public void setAkHash(String akHash) { this.akHash = akHash; }
    public String getDomainHash() { return domainHash; }
    public void setDomainHash(String domainHash) { this.domainHash = domainHash; }
    public String getCouponId() { return couponId; }
    public void setCouponId(String couponId) { this.couponId = couponId; }
    public Integer getFaceAmount() { return faceAmount; }
    public void setFaceAmount(Integer faceAmount) { this.faceAmount = faceAmount; }
    public String getVoucherType() { return voucherType; }
    public void setVoucherType(String voucherType) { this.voucherType = voucherType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
