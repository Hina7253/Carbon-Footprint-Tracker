package com.example.LatestStable.service;

import com.example.LatestStable.model.WebsiteAnalysis;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;

import java.util.Optional;

public class BadgeService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(BadgeService.class);

    private final WebsiteAnalysisRepository analysisRepository;

    public BadgeService(
            WebsiteAnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }
    // ── GENERATE SVG BADGE ────────────────────────────────────────
    public String generateBadge(String websiteUrl) {
        log.info("Generating badge for: {}", websiteUrl);

        // Find latest analysis for this URL
        Optional<WebsiteAnalysis> analysisOpt =
                analysisRepository
                        .findTopByWebsiteUrlOrderByCreatedAtDesc(
                                websiteUrl);

        if (analysisOpt.isEmpty()) {
            return generateNotAnalyzedBadge(websiteUrl);
        }

        WebsiteAnalysis analysis = analysisOpt.get();
        return buildSvgBadge(analysis);
    }
}
