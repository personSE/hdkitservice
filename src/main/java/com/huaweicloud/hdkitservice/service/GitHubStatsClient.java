package com.huaweicloud.hdkitservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.huaweicloud.hdkitservice.config.DashboardConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GitHubStatsClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubStatsClient.class);
    private static final String API_BASE = "https://api.github.com";

    private final RestClient restClient;
    private final DashboardConfig config;

    public GitHubStatsClient(DashboardConfig config) {
        this.config = config;
        RestClient.Builder builder = RestClient.builder().baseUrl(API_BASE)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");
        if (config.githubToken() != null && !config.githubToken().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.githubToken());
        }
        this.restClient = builder.build();
    }

    public record RepoStats(long stars, long forks, long watchers, long openIssues,
                            long commits, long releases) {}

    public record PrStats(long openPrs) {}

    public RepoStats fetchRepoStats() {
        String repo = config.githubRepo();
        long stars = 0, forks = 0, watchers = 0, openIssues = 0, commits = 0, releases = 0;

        try {
            JsonNode node = restClient.get().uri("/repos/" + repo).retrieve().body(JsonNode.class);
            if (node != null) {
                stars = node.path("stargazers_count").asLong();
                forks = node.path("forks_count").asLong();
                watchers = node.path("subscribers_count").asLong();
                openIssues = node.path("open_issues_count").asLong();
            }
        } catch (Exception e) {
            log.warn("[github] fetch repo stats failed: {}", e.getMessage());
        }

        try {
            JsonNode node = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/repos/" + repo + "/commits")
                            .queryParam("per_page", 1).queryParam("page", 1).build())
                    .retrieve().body(JsonNode.class);
            if (node != null && node.isArray() && node.size() > 0) {
                String lastPage = restClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/repos/" + repo + "/commits")
                                .queryParam("per_page", 1).queryParam("page", 1).build())
                        .retrieve()
                        .toEntity(JsonNode.class)
                        .getHeaders()
                        .getFirst("Link");
                if (lastPage != null) {
                    java.util.regex.Matcher m = java.util.regex.Pattern
                            .compile("page=(\\d+)>; rel=\"last\"").matcher(lastPage);
                    if (m.find()) {
                        commits = Long.parseLong(m.group(1));
                    }
                }
                if (commits == 0) {
                    commits = 1;
                }
            }
        } catch (Exception e) {
            log.warn("[github] fetch commits failed: {}", e.getMessage());
        }

        try {
            JsonNode node = restClient.get().uri("/repos/" + repo + "/releases?per_page=100").retrieve().body(JsonNode.class);
            if (node != null && node.isArray()) {
                releases = node.size();
            }
        } catch (Exception e) {
            log.warn("[github] fetch releases failed: {}", e.getMessage());
        }

        return new RepoStats(stars, forks, watchers, openIssues, commits, releases);
    }

    public long fetchOpenPrs() {
        String repo = config.githubRepo();
        try {
            JsonNode node = restClient.get()
                    .uri("/search/issues?q=repo:" + repo + "+type:pr+state:open&per_page=1")
                    .retrieve().body(JsonNode.class);
            if (node != null && node.has("total_count")) {
                return node.get("total_count").asLong();
            }
        } catch (Exception e) {
            log.warn("[github] fetch open PRs failed: {}", e.getMessage());
        }
        return 0;
    }
}
