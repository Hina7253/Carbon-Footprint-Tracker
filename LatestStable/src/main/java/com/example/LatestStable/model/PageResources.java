package com.example.LatestStable.model;


import jakarta.persistence.*;



@Entity
@Table(name = "page_resources")
public class PageResources {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private WebsiteAnalysis websiteAnalysis;

    @Column(length = 2048)
    private String foundOnPage;

    @Column(nullable = false, length = 2048)
    private String resourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType resourceType;

    // ── Long wrapper (NOT primitive long) ─────────────────────────
    // Long = nullable,  long = primitive (can't be null)
    // We need Long so we can check: if (sizeBytes != null)
    @Column
    private Long sizeBytes;

    @Column
    private Double co2ContributionGrams;

    @Column
    private Boolean isCached = false;

    @Column
    private Boolean isThirdParty = false;

    @Column
    private Integer httpStatus;

    @Column(length = 255)
    private String contentType;

    @Column
    private Double optimizationPotential;

    // ── CONSTRUCTORS ──────────────────────────────────────────────
    public PageResources() {}

    private PageResources(Builder builder) {
        this.websiteAnalysis  = builder.websiteAnalysis;
        this.resourceUrl      = builder.resourceUrl;
        this.foundOnPage      = builder.foundOnPage;
        this.resourceType     = builder.resourceType;
        this.isCached         = false;
        this.isThirdParty     = false;
    }

    // ── BUILDER ───────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private WebsiteAnalysis websiteAnalysis;
        private String resourceUrl;
        private String foundOnPage;
        private ResourceType resourceType;

        public Builder websiteAnalysis(WebsiteAnalysis v) {
            this.websiteAnalysis = v; return this;
        }
        public Builder resourceUrl(String v) {
            this.resourceUrl = v; return this;
        }
        public Builder foundOnPage(String v) {
            this.foundOnPage = v; return this;
        }
        public Builder resourceType(ResourceType v) {
            this.resourceType = v; return this;
        }
        public PageResources build() {
            return new PageResources(this);
        }
    }

    // ── HELPER METHOD ─────────────────────────────────────────────
    public void detectThirdParty(String baseDomain) {
        if (this.resourceUrl == null || baseDomain == null) {
            this.isThirdParty = false;
            return;
        }
        this.isThirdParty = !this.resourceUrl.contains(baseDomain);
    }

    // ── GETTERS ───────────────────────────────────────────────────
    public Long getId() { return id; }
    public WebsiteAnalysis getWebsiteAnalysis() { return websiteAnalysis; }
    public String getFoundOnPage() { return foundOnPage; }
    public String getResourceUrl() { return resourceUrl; }
    public ResourceType getResourceType() { return resourceType; }
    public Long getSizeBytes() { return sizeBytes; }
    public Double getCo2ContributionGrams() { return co2ContributionGrams; }
    public Boolean getIsCached() { return isCached; }
    public Boolean getIsThirdParty() { return isThirdParty; }
    public Integer getHttpStatus() { return httpStatus; }
    public String getContentType() { return contentType; }
    public Double getOptimizationPotential() { return optimizationPotential; }

    // Boolean helpers
    public boolean isCached() {
        return Boolean.TRUE.equals(this.isCached);
    }
    public boolean isThirdParty() {
        return Boolean.TRUE.equals(this.isThirdParty);
    }

    // ── SETTERS ───────────────────────────────────────────────────
    public void setId(Long id) { this.id = id; }
    public void setWebsiteAnalysis(WebsiteAnalysis v) { this.websiteAnalysis = v; }
    public void setFoundOnPage(String v) { this.foundOnPage = v; }
    public void setResourceUrl(String v) { this.resourceUrl = v; }
    public void setResourceType(ResourceType v) { this.resourceType = v; }
    public void setSizeBytes(Long v) { this.sizeBytes = v; }
    public void setCo2ContributionGrams(Double v) { this.co2ContributionGrams = v; }
    public void setIsCached(Boolean v) { this.isCached = v; }
    public void setIsThirdParty(Boolean v) { this.isThirdParty = v; }
    public void setHttpStatus(Integer v) { this.httpStatus = v; }
    public void setContentType(String v) { this.contentType = v; }
    public void setOptimizationPotential(Double v) { this.optimizationPotential = v; }
}