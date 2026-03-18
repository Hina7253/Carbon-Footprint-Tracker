package com.example.LatestStable.service;

import com.example.LatestStable.model.WebsiteAnalysis;
import com.example.LatestStable.repository.WebsiteAnalysisRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    // ── TOP 10 DIRTIEST WEBSITES ──────────────────────────────────
    public List<Map<String, Object>> getDirtiestWebsites() {
        log.info("Fetching dirtiest websites leaderboard");

        return analysisRepository.findTopPolluters()
                .stream()
                .limit(10)
                .map(this::buildLeaderboardEntry)
                .collect(Collectors.toList());
    }

    // ── FULL LEADERBOARD ──────────────────────────────────────────
    public Map<String, Object> getFullLeaderboard() {
        Map<String, Object> leaderboard = new HashMap<>();

        leaderboard.put("cleanest", getCleanestWebsites());
        leaderboard.put("dirtiest", getDirtiestWebsites());
        leaderboard.put("totalAnalyzed",
                analysisRepository.count());
        leaderboard.put("averageCo2Grams",
                calculateAverageCo2());

        return leaderboard;
    }

    // ── CALCULATE AVERAGE CO2 ─────────────────────────────────────
    private double calculateAverageCo2() {
        List<WebsiteAnalysis> all =
                analysisRepository.findTopPolluters();

        if (all.isEmpty()) return 0.0;

        return all.stream()
                .filter(a -> a.getCo2PerVisitGrams() != null)
                .mapToDouble(WebsiteAnalysis::getCo2PerVisitGrams)
                .average()
                .orElse(0.0);
    }


}
