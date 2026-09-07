package com.huaweicloud.hdkitservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_id_hash", indexes = {
        @Index(name = "idx_domain_id", columnList = "domain_id"),
        @Index(name = "idx_first_created", columnList = "first_created")
})
public class UserHashRecord {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "user_id_hash", length = 64, nullable = false, unique = true)
    private String userIdHash;

    @Column(name = "domain_id", length = 64)
    private String domainId;

    @Column(name = "is_gen_by_ask", nullable = false)
    private Boolean isGenByAsk;

    @Column(name = "first_created", nullable = false)
    private LocalDateTime firstCreated;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    public UserHashRecord() {
    }

    public UserHashRecord(String id, String userIdHash, String domainId, Boolean isGenByAsk,
                          LocalDateTime firstCreated, LocalDateTime lastUpdated) {
        this.id = id;
        this.userIdHash = userIdHash;
        this.domainId = domainId;
        this.isGenByAsk = isGenByAsk;
        this.firstCreated = firstCreated;
        this.lastUpdated = lastUpdated;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserIdHash() { return userIdHash; }
    public void setUserIdHash(String userIdHash) { this.userIdHash = userIdHash; }

    public String getDomainId() { return domainId; }
    public void setDomainId(String domainId) { this.domainId = domainId; }

    public Boolean getIsGenByAsk() { return isGenByAsk; }
    public void setIsGenByAsk(Boolean isGenByAsk) { this.isGenByAsk = isGenByAsk; }

    public LocalDateTime getFirstCreated() { return firstCreated; }
    public void setFirstCreated(LocalDateTime firstCreated) { this.firstCreated = firstCreated; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}