package com.example.LatestStable.dto;

import jakarta.validation.constraints.*;

public class AnalysisRequestDTO {

    @NotBlank(message = "Website URL is required")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String url;

    @Min(value = 1)
    @Max(value = 1000000000L)
    private Long monthlyVisits = 10000L;

    @Min(value = 1)
    @Max(value = 10)
    private Integer crawlPages = 1;

    private Boolean enableCrawlMode = false;

    // ── Constructors ──────────────────────────────────────────────
    public AnalysisRequestDTO() {}

    public AnalysisRequestDTO(String url, Long monthlyVisits,
                              Integer crawlPages, Boolean enableCrawlMode) {
        this.url = url;
        this.monthlyVisits = monthlyVisits;
        this.crawlPages = crawlPages;
        this.enableCrawlMode = enableCrawlMode;
    }

    // ── Getters ───────────────────────────────────────────────────
    public String getUrl() { return url; }
    public Long getMonthlyVisits() {
        return monthlyVisits != null ? monthlyVisits : 10000L;
    }
    public Integer getCrawlPages() {
        return crawlPages != null ? crawlPages : 1;
    }
    public Boolean getEnableCrawlMode() {
        return enableCrawlMode != null ? enableCrawlMode : false;
    }

    // ── Setters ───────────────────────────────────────────────────
    public void setUrl(String url) { this.url = url; }
    public void setMonthlyVisits(Long v) { this.monthlyVisits = v; }
    public void setCrawlPages(Integer v) { this.crawlPages = v; }
    public void setEnableCrawlMode(Boolean v) { this.enableCrawlMode = v; }

    // ── Helper Methods ────────────────────────────────────────────
    public String getNormalizedUrl() {
        if (url == null) return null;
        String trimmed = url.trim();
        if (!trimmed.startsWith("http://")
                && !trimmed.startsWith("https://")) {
            return "https://" + trimmed;
        }
        return trimmed;
    }

    public String getBaseDomain() {
        try {
            String normalized = getNormalizedUrl();
            String withoutProtocol =
                    normalized.replaceFirst("https?://", "");
            String withoutWww =
                    withoutProtocol.replaceFirst("^www\\.", "");
            return withoutWww.split("/")[0].split("\\?")[0];
        } catch (Exception e) {
            return url;
        }
    }
}