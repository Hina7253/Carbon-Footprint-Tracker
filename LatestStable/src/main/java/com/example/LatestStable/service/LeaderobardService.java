package com.example.LatestStable.service;

import com.example.LatestStable.repository.WebsiteAnalysisRepository;

public class LeaderobardService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LeaderboardService.class);

    private final WebsiteAnalysisRepository analysisRepository;

    public LeaderboardService(
            WebsiteAnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }
}
