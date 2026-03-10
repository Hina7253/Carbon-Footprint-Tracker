package com.example.LatestStable.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "page_resources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResources {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private WebsiteAnalysis websiteAnalysis;

    @Column(length = 2048)
    private String foundOnPage;

    @Column(nullable = false, length = 2048)
    private String resourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType resourceType;

    @Column
    private Long sizeBytes;

    @Column
    private Double co2ContributionGrams;

    @Column
    @Builder.Default
    private Boolean isCached = false;

    @Column
    @Builder.Default
    private Boolean isThirdParty = false;

    @Column
    private Integer httpStatus;

    @Column(length = 255)
    private String contentType;

    @Column
    private Double optimizationPotential;

    public static Object builder() {
        return null;
    }

    public void detectThirdParty(String baseDomain) {
        if (this.resourceUrl == null || baseDomain == null) {
            this.isThirdParty = false;
            return;
        }
        this.isThirdParty = !this.resourceUrl.contains(baseDomain);
    }

    public ResourceType getResourceType() {
        return null;
    }

    public void setHttpStatus(int code) {
    }

    public void setSizeBytes(long sizeBytes) {
    }

    public void setCo2ContributionGrams(double co2) {
    }

    public void setOptimizationPotential(double potential) {
    }

    public void setContentType(String trim) {
    }

    public void setIsCached(boolean b) {
    }

    public boolean getIsCached() {
        return false;
    }

    public String getContentType() {
        return "";
    }

    public void setWebsiteAnalysis(WebsiteAnalysis analysis) {
    }

    public void setResourceUrl(String resourceUrl) {

    }

    public void setFoundOnPage(String foundOnPage) {

    }

    public void setResourceType(ResourceType resourceType) {

    }

    public long getSizeBytes() {
        return false;
    }

    public Boolean getIsThirdParty() {
        return null;
    }

    public Object getOptimizationPotential() {
        return null;

    }

    public Double getCo2ContributionGrams() {
        return 0.0;
    }

    public Object getResourceUrl() {
        return null;
    }
    public boolean isThirdParty() {
        return Boolean.TRUE.equals(this.isThirdParty);
    }

    public boolean isCached() {
        return Boolean.TRUE.equals(this.isCached);
    }

    public void setIsCached(Boolean isCached) {
        this.isCached = isCached;
    }

    public void setIsThirdParty(Boolean isThirdParty) {
        this.isThirdParty = isThirdParty;
    }
}

