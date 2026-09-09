package com.huaweicloud.hdkitservice.service;

import com.huaweicloud.hdkitservice.config.DashboardConfig;
import com.huaweicloud.hdkitservice.model.GitHubStatsDaily;
import com.huaweicloud.hdkitservice.repository.GitHubStatsDailyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class GitHubStatsFetchTask {

    private static final Logger log = LoggerFactory.getLogger(GitHubStatsFetchTask.class);

    private final GitHubStatsClient githubClient;
    private final GitHubStatsDailyRepository githubRepo;
    private final DashboardConfig config;

    public GitHubStatsFetchTask(GitHubStatsClient githubClient,
                                GitHubStatsDailyRepository githubRepo,
                                DashboardConfig config) {
        this.githubClient = githubClient;
        this.githubRepo = githubRepo;
        this.config = config;
    }

    @Scheduled(cron = "0 30 6 * * *")
    public void fetch() {
        if (!config.githubFetchEnabled()) {
            return;
        }
        LocalDate today = LocalDate.now();
        log.info("[github-fetch] start for {}", today);

        try {
            GitHubStatsClient.RepoStats repo = githubClient.fetchRepoStats();
            long openPrs = githubClient.fetchOpenPrs();

            Optional<GitHubStatsDaily> existing = githubRepo.findByStatDate(today);
            GitHubStatsDaily stats;
            if (existing.isPresent()) {
                stats = existing.get();
                stats.setStars(repo.stars());
                stats.setForks(repo.forks());
                stats.setWatchers(repo.watchers());
                stats.setOpenIssues(repo.openIssues());
                stats.setOpenPrs(openPrs);
                stats.setCommits(repo.commits());
                stats.setReleases(repo.releases());
                stats.setUpdatedAt(java.time.LocalDateTime.now());
            } else {
                stats = new GitHubStatsDaily(today, repo.stars(), repo.forks(),
                        repo.watchers(), repo.openIssues(), openPrs,
                        repo.commits(), repo.releases());
            }
            githubRepo.save(stats);
            log.info("[github-fetch] done: stars={}, forks={}, commits={}, releases={}, openIssues={}, openPrs={}",
                    repo.stars(), repo.forks(), repo.commits(), repo.releases(),
                    repo.openIssues(), openPrs);
        } catch (Exception e) {
            log.error("[github-fetch] failed: {}", e.getMessage(), e);
        }
    }
}
