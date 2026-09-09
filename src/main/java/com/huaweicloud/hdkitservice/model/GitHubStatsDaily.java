package com.huaweicloud.hdkitservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "github_stats_daily")
public class GitHubStatsDaily {

    @Id
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "stars", nullable = false)
    private Long stars;

    @Column(name = "forks", nullable = false)
    private Long forks;

    @Column(name = "watchers", nullable = false)
    private Long watchers;

    @Column(name = "open_issues")
    private Long openIssues;

    @Column(name = "open_prs")
    private Long openPrs;

    @Column(name = "commits")
    private Long commits;

    @Column(name = "releases")
    private Long releases;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public GitHubStatsDaily() {
    }

    public GitHubStatsDaily(LocalDate statDate, Long stars, Long forks, Long watchers,
                            Long openIssues, Long openPrs, Long commits, Long releases) {
        this.statDate = statDate;
        this.stars = stars;
        this.forks = forks;
        this.watchers = watchers;
        this.openIssues = openIssues;
        this.openPrs = openPrs;
        this.commits = commits;
        this.releases = releases;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDate getStatDate() { return statDate; }
    public void setStatDate(LocalDate statDate) { this.statDate = statDate; }
    public Long getStars() { return stars; }
    public void setStars(Long stars) { this.stars = stars; }
    public Long getForks() { return forks; }
    public void setForks(Long forks) { this.forks = forks; }
    public Long getWatchers() { return watchers; }
    public void setWatchers(Long watchers) { this.watchers = watchers; }
    public Long getOpenIssues() { return openIssues; }
    public void setOpenIssues(Long openIssues) { this.openIssues = openIssues; }
    public Long getOpenPrs() { return openPrs; }
    public void setOpenPrs(Long openPrs) { this.openPrs = openPrs; }
    public Long getCommits() { return commits; }
    public void setCommits(Long commits) { this.commits = commits; }
    public Long getReleases() { return releases; }
    public void setReleases(Long releases) { this.releases = releases; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
