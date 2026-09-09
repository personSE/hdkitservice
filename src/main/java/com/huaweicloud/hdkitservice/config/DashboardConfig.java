package com.huaweicloud.hdkitservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DashboardConfig {

    @Value("${DASHBOARD_NPM_PACKAGE_NAME:huaweicloud-devkit}")
    private String npmPackageName;

    @Value("${DASHBOARD_NPM_PUBLISH_DATE:2026-08-09}")
    private String npmPublishDate;

    @Value("${DASHBOARD_NPM_API_BASE:https://api.npmjs.org}")
    private String npmApiBase;

    @Value("${DASHBOARD_AGGREGATION_ENABLED:true}")
    private boolean aggregationEnabled;

    @Value("${DASHBOARD_NPM_FETCH_ENABLED:true}")
    private boolean npmFetchEnabled;

    @Value("${DASHBOARD_GITHUB_REPO:huaweicloud/huaweicloud-devkit}")
    private String githubRepo;

    @Value("${DASHBOARD_GITHUB_TOKEN:}")
    private String githubToken;

    @Value("${DASHBOARD_GITHUB_FETCH_ENABLED:true}")
    private boolean githubFetchEnabled;

    public String npmPackageName() { return npmPackageName; }
    public String npmPublishDate() { return npmPublishDate; }
    public String npmApiBase() { return npmApiBase; }
    public boolean aggregationEnabled() { return aggregationEnabled; }
    public boolean npmFetchEnabled() { return npmFetchEnabled; }
    public String githubRepo() { return githubRepo; }
    public String githubToken() { return githubToken; }
    public boolean githubFetchEnabled() { return githubFetchEnabled; }

    public void setNpmPackageName(String v) { this.npmPackageName = v; }
    public void setNpmPublishDate(String v) { this.npmPublishDate = v; }
    public void setNpmApiBase(String v) { this.npmApiBase = v; }
    public void setAggregationEnabled(boolean v) { this.aggregationEnabled = v; }
    public void setNpmFetchEnabled(boolean v) { this.npmFetchEnabled = v; }
    public void setGithubRepo(String v) { this.githubRepo = v; }
    public void setGithubToken(String v) { this.githubToken = v; }
    public void setGithubFetchEnabled(boolean v) { this.githubFetchEnabled = v; }
}
