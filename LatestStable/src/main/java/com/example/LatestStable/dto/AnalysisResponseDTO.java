
package com.example.LatestStable.dto;

import com.example.LatestStable.model.ResourceType;
import com.example.LatestStable.model.WebsiteAnalysis;
import jdk.jshell.Snippet;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalysisResponseDTO {
    private Long id;
    private String websiteUrl;
    private WebsiteAnalysis.AnalysisStatus status;
    private String grade;               // "A", "B", "C", "D", or "F"
    private LocalDateTime analyzedAt;
    private String errorMessage;        // Only set if status = FAILED

    // ── CARBON METRICS ────────────────────────────────────────────
    // Nested object keeps the response organized
    private CarbonMetrics carbonMetrics;

    // ── RESOURCE SUMMARY ─────────────────────────────────────────
    private ResourceSummary resourceSummary;

    // ── HOTSPOTS ──────────────────────────────────────────────────
    // Top heaviest resources — these are shown prominently in UI
    private List<ResourceDetail> hotspots;

    // ── AI SUGGESTIONS ───────────────────────────────────────────
    private String aiSuggestions;

    // ── COMPARISON DATA ──────────────────────────────────────────
    // How does this site compare to the average?
    private ComparisonData comparison;

    public static Snippet builder() {
    }

    @Data
    @Builder
    public static class CarbonMetrics {
        private Double co2PerVisitGrams;
        private Double co2YearlyKg;
        private Double energyUsageKwh;
        private CarbonEquivalents equivalents;

        public static Object builder() {
            return null;
        }
    }

    @Data
    @Builder
    public static class CarbonEquivalents {
        private Double kmDriven;
        private Double treesNeeded;
        private Double smartphoneCharges;
        private Double googleSearches;

        public static Object builder() {
            return null;
        }
    }

    @Data
    @Builder
    public static class ResourceSummary {
        private Long totalTransferBytes;
        private String totalTransferFormatted;
        private Integer totalResourceCount;
        private Integer thirdPartyCount;
        private Map<ResourceType, ResourceTypeBreakdown> breakdown;
    }

    @Data
    @Builder
    public static class ResourceTypeBreakdown {
        private ResourceType type;
        private Integer count;
        private Long totalBytes;
        private String totalBytesFormatted;
        private Double percentageOfTotal;
    }

    @Data
    @Builder
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
    }

    @Data
    @Builder
    public static class ComparisonData {
        private Double averageWebsiteCo2Grams;
        private Double percentileBetter;
        private String performanceCategory;
    }
}