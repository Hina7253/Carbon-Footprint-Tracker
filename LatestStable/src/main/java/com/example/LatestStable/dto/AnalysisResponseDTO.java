package com.example.LatestStable.dto;

import com.example.LatestStable.model.ResourceType;
import com.example.LatestStable.model.WebsiteAnalysis.AnalysisStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AnalysisResponseDTO {

    private Long id;
    private String websiteUrl;
    private AnalysisStatus status;
    private String grade;
    private LocalDateTime analyzedAt;
    private String errorMessage;
    private CarbonMetrics carbonMetrics;
    private ResourceSummary resourceSummary;
    private List<ResourceDetail> hotspots;
    private String aiSuggestions;
    private ComparisonData comparison;

    // ── Getters and Setters ──
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public AnalysisStatus getStatus() { return status; }
    public void setStatus(AnalysisStatus status) { this.status = status; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public CarbonMetrics getCarbonMetrics() { return carbonMetrics; }
    public void setCarbonMetrics(CarbonMetrics carbonMetrics) { this.carbonMetrics = carbonMetrics; }

    public ResourceSummary getResourceSummary() { return resourceSummary; }
    public void setResourceSummary(ResourceSummary resourceSummary) { this.resourceSummary = resourceSummary; }

    public List<ResourceDetail> getHotspots() { return hotspots; }
    public void setHotspots(List<ResourceDetail> hotspots) { this.hotspots = hotspots; }

    public String getAiSuggestions() { return aiSuggestions; }
    public void setAiSuggestions(String aiSuggestions) { this.aiSuggestions = aiSuggestions; }

    public ComparisonData getComparison() { return comparison; }
    public void setComparison(ComparisonData comparison) { this.comparison = comparison; }

    // ── BUILDER ──
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AnalysisResponseDTO obj = new AnalysisResponseDTO();
        public Builder id(Long id) { obj.id = id; return this; }
        public Builder websiteUrl(String v) { obj.websiteUrl = v; return this; }
        public Builder status(AnalysisStatus v) { obj.status = v; return this; }
        public Builder grade(String v) { obj.grade = v; return this; }
        public Builder analyzedAt(LocalDateTime v) { obj.analyzedAt = v; return this; }
        public Builder errorMessage(String v) { obj.errorMessage = v; return this; }
        public Builder carbonMetrics(CarbonMetrics v) { obj.carbonMetrics = v; return this; }
        public Builder resourceSummary(ResourceSummary v) { obj.resourceSummary = v; return this; }
        public Builder hotspots(List<ResourceDetail> v) { obj.hotspots = v; return this; }
        public Builder aiSuggestions(String v) { obj.aiSuggestions = v; return this; }
        public Builder comparison(ComparisonData v) { obj.comparison = v; return this; }
        public AnalysisResponseDTO build() { return obj; }
    }

    // ════════════════════════════════════════════
    // NESTED CLASS: CarbonMetrics
    // ════════════════════════════════════════════
    public static class CarbonMetrics {
        private Double co2PerVisitGrams;
        private Double co2YearlyKg;
        private Double energyUsageKwh;
        private CarbonEquivalents equivalents;

        public Double getCo2PerVisitGrams() { return co2PerVisitGrams; }
        public Double getCo2YearlyKg() { return co2YearlyKg; }
        public Double getEnergyUsageKwh() { return energyUsageKwh; }
        public CarbonEquivalents getEquivalents() { return equivalents; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final CarbonMetrics obj = new CarbonMetrics();
            public Builder co2PerVisitGrams(Double v) { obj.co2PerVisitGrams = v; return this; }
            public Builder co2YearlyKg(Double v) { obj.co2YearlyKg = v; return this; }
            public Builder energyUsageKwh(Double v) { obj.energyUsageKwh = v; return this; }
            public Builder equivalents(CarbonEquivalents v) { obj.equivalents = v; return this; }
            public CarbonMetrics build() { return obj; }
        }
    }

    // ════════════════════════════════════════════
    // NESTED CLASS: CarbonEquivalents
    // ════════════════════════════════════════════
    public static class CarbonEquivalents {
        private Double kmDriven;
        private Double treesNeeded;
        private Double smartphoneCharges;
        private Double googleSearches;

        public Double getKmDriven() { return kmDriven; }
        public Double getTreesNeeded() { return treesNeeded; }
        public Double getSmartphoneCharges() { return smartphoneCharges; }
        public Double getGoogleSearches() { return googleSearches; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final CarbonEquivalents obj = new CarbonEquivalents();
            public Builder kmDriven(Double v) { obj.kmDriven = v; return this; }
            public Builder treesNeeded(Double v) { obj.treesNeeded = v; return this; }
            public Builder smartphoneCharges(Double v) { obj.smartphoneCharges = v; return this; }
            public Builder googleSearches(Double v) { obj.googleSearches = v; return this; }
            public CarbonEquivalents build() { return obj; }
        }
    }

    // ════════════════════════════════════════════
    // NESTED CLASS: ResourceSummary
    // ════════════════════════════════════════════
    public static class ResourceSummary {
        private Long totalTransferBytes;
        private String totalTransferFormatted;
        private Integer totalResourceCount;
        private Integer thirdPartyCount;
        private Map<ResourceType, ResourceTypeBreakdown> breakdown;

        public Long getTotalTransferBytes() { return totalTransferBytes; }
        public String getTotalTransferFormatted() { return totalTransferFormatted; }
        public Integer getTotalResourceCount() { return totalResourceCount; }
        public Integer getThirdPartyCount() { return thirdPartyCount; }
        public Map<ResourceType, ResourceTypeBreakdown> getBreakdown() { return breakdown; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final ResourceSummary obj = new ResourceSummary();
            public Builder totalTransferBytes(Long v) { obj.totalTransferBytes = v; return this; }
            public Builder totalTransferFormatted(String v) { obj.totalTransferFormatted = v; return this; }
            public Builder totalResourceCount(Integer v) { obj.totalResourceCount = v; return this; }
            public Builder thirdPartyCount(Integer v) { obj.thirdPartyCount = v; return this; }
            public Builder breakdown(Map<ResourceType, ResourceTypeBreakdown> v) { obj.breakdown = v; return this; }
            public ResourceSummary build() { return obj; }
        }
    }

    // ════════════════════════════════════════════
    // NESTED CLASS: ResourceTypeBreakdown
    // ════════════════════════════════════════════
    public static class ResourceTypeBreakdown {
        private ResourceType type;
        private Integer count;
        private Long totalBytes;
        private String totalBytesFormatted;
        private Double percentageOfTotal;

        public ResourceType getType() { return type; }
        public Integer getCount() { return count; }
        public Long getTotalBytes() { return totalBytes; }
        public String getTotalBytesFormatted() { return totalBytesFormatted; }
        public Double getPercentageOfTotal() { return percentageOfTotal; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final ResourceTypeBreakdown obj = new ResourceTypeBreakdown();
            public Builder type(ResourceType v) { obj.type = v; return this; }
            public Builder count(Integer v) { obj.count = v; return this; }
            public Builder totalBytes(Long v) { obj.totalBytes = v; return this; }
            public Builder totalBytesFormatted(String v) { obj.totalBytesFormatted = v; return this; }
            public Builder percentageOfTotal(Double v) { obj.percentageOfTotal = v; return this; }
            public ResourceTypeBreakdown build() { return obj; }
        }
    }

    // ════════════════════════════════════════════
    // NESTED CLASS: ResourceDetail
    // ════════════════════════════════════════════
    public static class ResourceDetail {
        private String url;
        private ResourceType type;
        private Long sizeBytes;
        private String sizeFormatted;
        private Double co2Grams;
        private Boolean isThirdParty;
        private Boolean isCached;
        private Double optimizationPotential;
        private String optimizationTip;

        public String getUrl() { return url; }
        public ResourceType getType() { return type; }
        public Long getSizeBytes() { return sizeBytes; }
        public String getSizeFormatted() { return sizeFormatted; }
        public Double getCo2Grams() { return co2Grams; }
        public Boolean getIsThirdParty() { return isThirdParty; }
        public Boolean getIsCached() { return isCached; }
        public Double getOptimizationPotential() { return optimizationPotential; }
        public String getOptimizationTip() { return optimizationTip; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final ResourceDetail obj = new ResourceDetail();
            public Builder url(String v) { obj.url = v; return this; }
            public Builder type(ResourceType v) { obj.type = v; return this; }
            public Builder sizeBytes(Long v) { obj.sizeBytes = v; return this; }
            public Builder sizeFormatted(String v) { obj.sizeFormatted = v; return this; }
            public Builder co2Grams(Double v) { obj.co2Grams = v; return this; }
            public Builder isThirdParty(Boolean v) { obj.isThirdParty = v; return this; }
            public Builder isCached(Boolean v) { obj.isCached = v; return this; }
            public Builder optimizationPotential(Double v) { obj.optimizationPotential = v; return this; }
            public Builder optimizationTip(String v) { obj.optimizationTip = v; return this; }
            public ResourceDetail build() { return obj; }
        }
    }

    // ════════════════════════════════════════════
    // NESTED CLASS: ComparisonData
    // ════════════════════════════════════════════
    public static class ComparisonData {
        private Double averageWebsiteCo2Grams;
        private Double percentileBetter;
        private String performanceCategory;

        public Double getAverageWebsiteCo2Grams() { return averageWebsiteCo2Grams; }
        public Double getPercentileBetter() { return percentileBetter; }
        public String getPerformanceCategory() { return performanceCategory; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final ComparisonData obj = new ComparisonData();
            public Builder averageWebsiteCo2Grams(Double v) { obj.averageWebsiteCo2Grams = v; return this; }
            public Builder percentileBetter(Double v) { obj.percentileBetter = v; return this; }
            public Builder performanceCategory(String v) { obj.performanceCategory = v; return this; }
            public ComparisonData build() { return obj; }
        }
    }
}