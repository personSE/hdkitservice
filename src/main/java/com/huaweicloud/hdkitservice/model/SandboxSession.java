package com.huaweicloud.hdkitservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "sandbox_session")
public class SandboxSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ak_hash", nullable = false, length = 64)
    private String akHash;

    @Column(name = "dev_stage_id", length = 64)
    private String devStageId;

    @Column(name = "action", nullable = false, length = 16)
    private String action;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "template_id", length = 64)
    private String templateId;

    @Column(name = "flavor_id", length = 64)
    private String flavorId;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public SandboxSession() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAkHash() { return akHash; }
    public void setAkHash(String akHash) { this.akHash = akHash; }
    public String getDevStageId() { return devStageId; }
    public void setDevStageId(String devStageId) { this.devStageId = devStageId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getFlavorId() { return flavorId; }
    public void setFlavorId(String flavorId) { this.flavorId = flavorId; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
