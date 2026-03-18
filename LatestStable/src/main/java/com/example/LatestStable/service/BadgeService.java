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

    // ── BUILD SVG BADGE ───────────────────────────────────────────
    private String buildSvgBadge(WebsiteAnalysis analysis) {

        String grade  = analysis.getGrade() != null
                ? analysis.getGrade() : "?";
        String co2    = analysis.getCo2PerVisitGrams() != null
                ? String.format("%.3fg CO₂", analysis.getCo2PerVisitGrams())
                : "N/A";

        // Badge color by grade
        String color  = getBadgeColor(grade);
        String emoji  = getBadgeEmoji(grade);
        String label  = "Carbon Grade";

        return """
            <svg xmlns="http://www.w3.org/2000/svg"
                 width="200" height="60"
                 viewBox="0 0 200 60">
              <defs>
                <linearGradient id="grad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0" stop-color="#f8f9fa"/>
                  <stop offset="1" stop-color="#e9ecef"/>
                </linearGradient>
              </defs>

              <!-- Background -->
              <rect width="200" height="60" rx="8"
                    fill="url(#grad)" stroke="#dee2e6"
                    stroke-width="1"/>

              <!-- Left colored section -->
              <rect width="70" height="60" rx="8"
                    fill="%s"/>
              <rect x="62" width="8" height="60"
                    fill="%s"/>

              <!-- Grade text -->
              <text x="35" y="35" font-family="Arial,sans-serif"
                    font-size="26" font-weight="bold"
                    fill="white" text-anchor="middle">%s</text>

              <!-- Label -->
              <text x="135" y="18"
                    font-family="Arial,sans-serif"
                    font-size="10" fill="#6c757d"
                    text-anchor="middle">%s</text>

              <!-- CO2 value -->
              <text x="135" y="36"
                    font-family="Arial,sans-serif"
                    font-size="13" font-weight="bold"
                    fill="#212529" text-anchor="middle">%s</text>

              <!-- Emoji + Carbon Scope -->
              <text x="135" y="52"
                    font-family="Arial,sans-serif"
                    font-size="9" fill="#6c757d"
                    text-anchor="middle">%s Carbon Scope</text>
            </svg>
            """.formatted(color, color, grade,
                label, co2, emoji);
    }
}
