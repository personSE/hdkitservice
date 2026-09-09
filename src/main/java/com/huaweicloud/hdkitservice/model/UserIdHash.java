package com.huaweicloud.hdkitservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_id_hash")
public class UserIdHash {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "domain_id", length = 64)
    private String domainId;

    @Column(name = "first_created")
    private LocalDateTime firstCreated;

    @Column(name = "is_gen_by_ask")
    private Boolean isGenByAsk;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "user_id_hash", length = 128)
    private String userIdHash;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDomainId() { return domainId; }
    public void setDomainId(String domainId) { this.domainId = domainId; }
    public LocalDateTime getFirstCreated() { return firstCreated; }
    public void setFirstCreated(LocalDateTime firstCreated) { this.firstCreated = firstCreated; }
    public Boolean getIsGenByAsk() { return isGenByAsk; }
    public void setIsGenByAsk(Boolean isGenByAsk) { this.isGenByAsk = isGenByAsk; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public String getUserIdHash() { return userIdHash; }
    public void setUserIdHash(String userIdHash) { this.userIdHash = userIdHash; }
}
