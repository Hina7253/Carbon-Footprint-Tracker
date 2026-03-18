package com.example.LatestStable.service;

import com.example.LatestStable.model.WebsiteAnalysis;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;

import java.util.Map;

public class LeaderobardService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LeaderboardService.class);

    private final WebsiteAnalysisRepository analysisRepository;

    public LeaderboardService(
            WebsiteAnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    // ── TOP 10 CLEANEST WEBSITES ──────────────────────────────────
    public List<Map<String, Object>> getCleanestWebsites() {
        log.info("Fetching cleanest websites leaderboard");

        List<WebsiteAnalysis> allCompleted =
                analysisRepository.findTopPolluters();

        // Reverse — polluters are DESC, we want cleanest first
        Collections.reverse(allCompleted);

        return allCompleted.stream()
                .limit(10)
                .map(this::buildLeaderboardEntry)
                .collect(Collectors.toList());
    }

}
