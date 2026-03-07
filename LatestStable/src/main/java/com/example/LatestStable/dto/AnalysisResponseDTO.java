package com.example.LatestStable.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AnalysisResponseDTO {
    private Long id;
    private String websiteUrl;
    private AnalysisStatus status;
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

    /**
     * All carbon-related metrics in one object.
     */
    @Data
    @Builder
    public static class CarbonMetrics {
        // Per visit
        private Double co2PerVisitGrams;

        // Annual (based on monthly traffic × 12)
        private Double co2YearlyKg;

        // Energy
        private Double energyUsageKwh;

        // Human-readable equivalents (makes data relatable!)
        private CarbonEquivalents equivalents;
    }

    /**
     * Real-world equivalents to make CO2 numbers understandable.
     * Example: "Your site emits as much as driving 450km per year"
     *
     * CONVERSION FACTORS USED:
     *   - 1 km driving ≈ 0.21 kg CO2
     *   - 1 tree absorbs ≈ 21 kg CO2 per year
     *   - 1 smartphone charge ≈ 0.005 kg CO2
     *   - 1 Google search ≈ 0.0002 kg CO2
     */
    @Data
    @Builder
    public static class CarbonEquivalents {
        private Double kmDriven;          // equivalent km driven by car
        private Double treesNeeded;       // trees needed to offset per year
        private Double smartphoneCharges; // equivalent smartphone charges
        private Double googleSearches;    // equivalent Google searches
    }

    /**
     * Summary of all resources found on the page.
     */
    @Data
    @Builder
    public static class ResourceSummary {
        private Long totalTransferBytes;
        private String totalTransferFormatted; // "2.4 MB" (human readable)
        private Integer totalResourceCount;
        private Integer thirdPartyCount;

        // Breakdown by resource type:
        // { "IMAGE": { count: 24, totalBytes: 1234567 }, "SCRIPT": {...} }
        private Map<ResourceType, ResourceTypeBreakdown> breakdown;
    }

    /**
     * Statistics for one resource type (IMAGE, SCRIPT, etc.)
     */
    @Data
    @Builder
    public static class ResourceTypeBreakdown {
        private ResourceType type;
        private Integer count;
        private Long totalBytes;
        private String totalBytesFormatted;
        private Double percentageOfTotal; // e.g., 65.4 (%)
    }

    **
            * Details for a single "hotspot" resource.
     * Shown in the top-heaviest-resources section of UI.
            */
    @Data
    @Builder
    public static class ResourceDetail {
        private String url;
        private ResourceType type;
        private Long sizeBytes;
        private String sizeFormatted;       // "1.2 MB"
        private Double co2Grams;
        private Boolean isThirdParty;
        private Boolean isCached;
        private Double optimizationPotential; // 0.0 to 1.0
        private String optimizationTip;       // Short advice for this resource
    }

    /**
     * How does this site compare to benchmarks?
     * Helps contextualize the results.
     */
    @Data
    @Builder
    public static class ComparisonData {
        private Double averageWebsiteCo2Grams;   // Industry average: ~0.5g
        private Double percentileBetter;          // "Better than 70% of websites"
        private String performanceCategory;       // "Clean", "Average", "Dirty"
    }



}
