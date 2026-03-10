package com.example.LatestStable.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "website_analyses")
public class WebsiteAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String websiteUrl;

    @Column(nullable = false)
    private Long monthlyVisits = 10000L;

    @Column(nullable = false)
    private Integer crawlPages = 1;

    @Column
    private Long totalTransferBytes;

    @Column
    private Double co2PerVisitGrams;

    @Column
    private Double co2YearlyKg;

    @Column
    private Double energyUsageKwh;

    @Column(length = 2)
    private String grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status = AnalysisStatus.PENDING;

    @Column(length = 1000)
    private String errorMessage;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String aiSuggestions;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "websiteAnalysis",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<PageResources> resources = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── ENUM ──────────────────────────────────────────────────────
    public enum AnalysisStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    // ── CONSTRUCTORS ──────────────────────────────────────────────
    public WebsiteAnalysis() {}

    private WebsiteAnalysis(Builder builder) {
        this.websiteUrl       = builder.websiteUrl;
        this.monthlyVisits    = builder.monthlyVisits;
        this.crawlPages       = builder.crawlPages;
        this.status           = builder.status;
    }

    // ── BUILDER ───────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String websiteUrl;
        private Long monthlyVisits = 10000L;
        private Integer crawlPages = 1;
        private AnalysisStatus status = AnalysisStatus.PENDING;

        public Builder websiteUrl(String v) {
            this.websiteUrl = v; return this;
        }
        public Builder monthlyVisits(Long v) {
            this.monthlyVisits = v; return this;
        }
        public Builder crawlPages(Integer v) {
            this.crawlPages = v; return this;
        }
        public Builder status(AnalysisStatus v) {
            this.status = v; return this;
        }
        public WebsiteAnalysis build() {
            return new WebsiteAnalysis(this);
        }
    }

    // ── GETTERS ───────────────────────────────────────────────────
    public Long getId() { return id; }
    public String getWebsiteUrl() { return websiteUrl; }
    public Long getMonthlyVisits() { return monthlyVisits; }
    public Integer getCrawlPages() { return crawlPages; }
    public Long getTotalTransferBytes() { return totalTransferBytes; }
    public Double getCo2PerVisitGrams() { return co2PerVisitGrams; }
    public Double getCo2YearlyKg() { return co2YearlyKg; }
    public Double getEnergyUsageKwh() { return energyUsageKwh; }
    public String getGrade() { return grade; }
    public AnalysisStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public String getAiSuggestions() { return aiSuggestions; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public List<PageResources> getResources() { return resources; }

    // ── SETTERS ───────────────────────────────────────────────────
    public void setId(Long id) { this.id = id; }
    public void setWebsiteUrl(String v) { this.websiteUrl = v; }
    public void setMonthlyVisits(Long v) { this.monthlyVisits = v; }
    public void setCrawlPages(Integer v) { this.crawlPages = v; }
    public void setTotalTransferBytes(Long v) { this.totalTransferBytes = v; }
    public void setCo2PerVisitGrams(Double v) { this.co2PerVisitGrams = v; }
    public void setCo2YearlyKg(Double v) { this.co2YearlyKg = v; }
    public void setEnergyUsageKwh(Double v) { this.energyUsageKwh = v; }
    public void setGrade(String v) { this.grade = v; }
    public void setStatus(AnalysisStatus v) { this.status = v; }
    public void setErrorMessage(String v) { this.errorMessage = v; }
    public void setAiSuggestions(String v) { this.aiSuggestions = v; }
    public void setCompletedAt(LocalDateTime v) { this.completedAt = v; }
    public void setResources(List<PageResources> v) { this.resources = v; }
}