package com.example.LatestStable.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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

}
