package com.huaweicloud.hdkitservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "external_metric", uniqueConstraints = {
    @UniqueConstraint(name = "uk_key_date_source", columnNames = {"metric_key", "metric_date", "source"})
})
public class ExternalMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_key", length = 128, nullable = false)
    private String metricKey;

    @Column(name = "metric_value", nullable = false)
    private Long metricValue;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "source", length = 64, nullable = false)
    private String source;

    @Column(name = "extra", columnDefinition = "TEXT")
    private String extra;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ExternalMetric() {
    }

    public ExternalMetric(String metricKey, Long metricValue, LocalDate metricDate,
                          String source, String extra) {
        this.metricKey = metricKey;
        this.metricValue = metricValue;
        this.metricDate = metricDate;
        this.source = source;
        this.extra = extra;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMetricKey() { return metricKey; }
    public void setMetricKey(String metricKey) { this.metricKey = metricKey; }
    public Long getMetricValue() { return metricValue; }
    public void setMetricValue(Long metricValue) { this.metricValue = metricValue; }
    public LocalDate getMetricDate() { return metricDate; }
    public void setMetricDate(LocalDate metricDate) { this.metricDate = metricDate; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getExtra() { return extra; }
    public void setExtra(String extra) { this.extra = extra; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
